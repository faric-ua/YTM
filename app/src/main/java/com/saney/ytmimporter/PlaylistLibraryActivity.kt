package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.TextUtils
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.auth.SilentGoogleSessionRestorer
import com.saney.ytmimporter.destination.DestinationRemoteOperations
import com.saney.ytmimporter.history.HistoryRecoveryPolicy
import com.saney.ytmimporter.library.GroupNumberPolicy
import com.saney.ytmimporter.library.LibraryFilterPolicy
import com.saney.ytmimporter.library.LocalLibraryItem
import com.saney.ytmimporter.library.LocalLibrarySource
import com.saney.ytmimporter.library.PlaylistLibraryDuplicateGroupPolicy
import com.saney.ytmimporter.library.PlaylistLibraryDuplicatePolicy
import com.saney.ytmimporter.library.PlaylistLibraryIdentityPolicy
import com.saney.ytmimporter.library.PlaylistLibraryPolicy
import com.saney.ytmimporter.library.RemoteSongMemoryPolicy
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.GroupNumberStore
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RemoteSongMemoryStore
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.PlaylistCoverLoader
import com.saney.ytmimporter.ui.RestorableModalController
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors

/**
 * One discoverable playlist hub. Local browsing is completely offline.
 * Remote browsing explicitly reads the user's playlists and track metadata,
 * only executes destructive operations after an explicit source-specific
 * confirmation; opening or recreating a screen never writes to YouTube.
 */
class PlaylistLibraryActivity : Activity() {
    private enum class SortMode { RECENT, NAME_ASC, NAME_DESC, TRACKS_DESC, SIMILAR_FIRST }
    private var showAll = true
    private var sortMode = SortMode.RECENT
    private var privacyMode = LibraryFilterPolicy.Privacy.ALL
    private var similarOnly = false
    private var cardTarget: LinearLayout? = null
    private val currentStore by lazy { CurrentPlaylistStore(this) }
    private val savedStore by lazy { RestorablePlaylistStore(this) }
    private val historyStore by lazy { HistoryStore(this) }
    private val quotaTracker by lazy { QuotaTracker(this) }
    // HTTP 401 (expired token) → one silent token refresh and retry.
    private val api by lazy { YouTubeApi(GoogleAccessTokenRecovery(this)) }
    private val sessionRestorer by lazy { SilentGoogleSessionRestorer(this) }
    private val persistentAuth by lazy { PersistentAuthStateStore(this) }
    // Batch 4: checked YouTube songs survive the app being closed (owner «так»).
    private val songMemoryStore by lazy { RemoteSongMemoryStore(this) }
    // Batch 6: permanent group numbers (owner 2026-10-18 04:44).
    private val groupNumberStore by lazy { GroupNumberStore(this) }
    private var groupNumbers: GroupNumberPolicy.Memory? = null
    private var songMemoryOwner: String? = null
    private var songMemory: Map<String, RemoteSongMemoryPolicy.Entry> = emptyMap()
    private val worker = Executors.newSingleThreadExecutor()

    private var onlineTab = false
    private var columns = 2
    private var filterText = ""
    private var landscapeSearchOpen = false
    private var selectedLocal: String? = null
    private var selectedRemote: String? = null
    private var visibleTrackLimit = 60
    private var loadingList = false
    private var loadingTracks = false
    private var loadError: String? = null
    // BUG-052: silent Google/YTM restore when the screen starts without a token.
    private var restoringGoogle = false
    private var googleNeedsUser = false
    private var googleRestoreFailed = false
    private var deletingRemoteId: String? = null
    private val remoteDeleteListener: (DestinationRemoteOperations.State) -> Unit =
        { state -> onRemoteDeleteState(state) }

    private lateinit var detailActions: LinearLayout
    private lateinit var detailCurrentButton: ImageButton
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var scroller: ScrollView
    private lateinit var catalogueToolbar: LinearLayout
    private lateinit var tabsRow: LinearLayout
    private lateinit var filtersRow: LinearLayout
    private lateinit var compactLocalTabButton: ImageButton
    private lateinit var compactOnlineTabButton: ImageButton
    private lateinit var compactSearchButton: ImageButton
    private lateinit var compactCurrentButton: ImageButton
    private lateinit var compactLayoutButton: ImageButton
    private lateinit var compactHelpButton: ImageButton
    private lateinit var localTabButton: Button
    private lateinit var onlineTabButton: Button
    private lateinit var searchBox: EditText
    private lateinit var statusLabel: TextView
    private lateinit var modalController: RestorableModalController
    /** Catalogue scroll position to return to from a playlist (Back). */
    private var catalogueScrollY = 0
    /** Last duplicate analysis, shared by cards, details and the group window. */
    private var dupCandidates: List<PlaylistLibraryDuplicatePolicy.Candidate> = emptyList()
    private var dupHints: Map<String, PlaylistLibraryDuplicatePolicy.Hint> = emptyMap()
    private var dupMembers: Map<String, DupMember> = emptyMap()
    /** Cards shown by the last catalogue render; null = catalogue not built yet. */
    private var visibleCardIdentities: Set<String>? = null
    /** Running «Перевірити пісні YouTube» (batch 3); null = not running. */
    private var songsCheck: SongsCheck? = null
    private var songsCheckProgress: TextView? = null
    /** Batch 5: after a YouTube removal from the group window, reopen the group. */
    private var pendingGroupReturn: String? = null
    /** Phone/History entries linked to a playlist the account no longer has. */
    private var missingOnYouTube: Set<String> = emptySet()
    /** YouTube playlists whose songs YouTube did not give during this visit. */
    private val songsCheckFailed = mutableMapOf<String, String>()

    private val palette get() = AppThemeManager.palette(this)
    private val landscape get() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)
        onlineTab = savedInstanceState?.getBoolean("online") ?: false
        showAll = savedInstanceState?.getBoolean("show_all") ?: true
        sortMode = runCatching {
            SortMode.valueOf(savedInstanceState?.getString("sort_mode") ?: "RECENT")
        }.getOrDefault(SortMode.RECENT)
        privacyMode = runCatching {
            LibraryFilterPolicy.Privacy.valueOf(savedInstanceState?.getString("privacy_mode") ?: "ALL")
        }.getOrDefault(LibraryFilterPolicy.Privacy.ALL)
        similarOnly = savedInstanceState?.getBoolean("similar_only") ?: false
        columns = savedInstanceState?.getInt("columns") ?: 2
        filterText = savedInstanceState?.getString("filter").orEmpty()
        landscapeSearchOpen = savedInstanceState?.getBoolean("landscape_search")
            ?: filterText.isNotBlank()
        selectedLocal = savedInstanceState?.getString("local")
        selectedRemote = savedInstanceState?.getString("remote")
        visibleTrackLimit = savedInstanceState?.getInt("limit") ?: 60
        catalogueScrollY = savedInstanceState?.getInt("catalogue_scroll") ?: 0
        deletingRemoteId = savedInstanceState?.getString("deleting_remote")
        ensureRemoteAccountIsolation()
        modalController = RestorableModalController(this, STATE_MODAL)
        modalController.restore(savedInstanceState)
        buildUi()
        renderContent()
        modalController.restoreAfterContentReady(::renderConfirmation)
        if (selectedRemote != null && !cachedTracks.containsKey(selectedRemote) &&
            token() != null
        ) {
            loadRemoteTracks(selectedRemote!!)
        } else {
            ensureRemoteReady()
        }
    }

    override fun onStart() {
        super.onStart()
        DestinationRemoteOperations.addListener(remoteDeleteListener)
    }

    override fun onStop() {
        DestinationRemoteOperations.removeListener(remoteDeleteListener)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        ensureRemoteAccountIsolation()
        if (::body.isInitialized) {
            if (!ensureRemoteReady() && showsLocal()) renderContent()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("online", onlineTab)
        outState.putBoolean("show_all", showAll)
        outState.putString("sort_mode", sortMode.name)
        outState.putString("privacy_mode", privacyMode.name)
        outState.putBoolean("similar_only", similarOnly)
        outState.putInt("columns", columns)
        outState.putString("filter", filterText)
        outState.putBoolean("landscape_search", landscapeSearchOpen)
        outState.putString("local", selectedLocal)
        outState.putString("remote", selectedRemote)
        outState.putInt("limit", visibleTrackLimit)
        outState.putInt("catalogue_scroll", catalogueScrollY)
        outState.putString("deleting_remote", deletingRemoteId)
        modalController.save(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        modalController.onDestroy()
        worker.shutdownNow()
        super.onDestroy()
    }

    @Deprecated("Android Activity back callback compatibility")
    override fun onBackPressed() {
        if (selectedLocal != null || selectedRemote != null) {
            selectedLocal = null
            selectedRemote = null
            visibleTrackLimit = 60
            // Back returns to the catalogue where the owner left it.
            renderContent(catalogueScrollY)
        } else {
            super.onBackPressed()
        }
    }

    private fun showsLocal(): Boolean = showAll || !onlineTab
    private fun showsRemote(): Boolean = showAll || onlineTab

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(palette.background)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(if (landscape) 8 else 12), dp(4),
                dp(if (landscape) 8 else 12), dp(4))
        }
        header.addView(button("‹") { onBackPressed() },
            LinearLayout.LayoutParams(dp(if (landscape) 46 else 54), dp(48)))
        header.addView(text("Бібліотека плейлістів",
            if (landscape) 18f else 21f, true, palette.accent).apply {
            setPadding(dp(9), 0, 0, 0)
            maxLines = if (landscape) 1 else 2
            ellipsize = TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        // The same labelled icon actions are used in both orientations.
        // On a narrow portrait screen they get their own compact row.
        compactLocalTabButton = iconButton(
            R.drawable.ic_ytm_device, "На телефоні"
        ) { selectTab(false) }
        compactOnlineTabButton = iconButton(
            R.drawable.ic_ytm_youtube_play, "YouTube"
        ) { selectTab(true) }
        compactSearchButton = iconButton(
            R.drawable.ic_ytm_search, "Знайти плейліст"
        ) {
            landscapeSearchOpen = !landscapeSearchOpen
            updateTopControls()
            if (landscapeSearchOpen) searchBox.requestFocus()
            else searchBox.clearFocus()
        }
        compactCurrentButton = iconButton(
            R.drawable.ic_ytm_library_current, "Відкрити поточний плейліст"
        ) { startActivity(Intent(this, PlaylistActivity::class.java)) }
        compactLayoutButton = iconButton(
            R.drawable.ic_ytm_view_list, "Список одним стовпчиком"
        ) {
            columns = if (columns == 2) 1 else 2
            updateTopControls()
            renderContent()
        }
        compactHelpButton = iconButton(
            R.drawable.ic_ytm_help, "Довідка бібліотеки плейлістів"
        ) { showHelp() }
        val actions = listOf(compactLocalTabButton, compactOnlineTabButton,
            compactSearchButton, compactCurrentButton, compactLayoutButton,
            compactHelpButton)
        // A detail screen has only Current and Help on its right edge.
        // Landscape reuses their existing header buttons; portrait has its own pair.
        detailActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        detailCurrentButton = iconButton(
            R.drawable.ic_ytm_library_current, "Відкрити поточний плейліст"
        ) { startActivity(Intent(this, PlaylistActivity::class.java)) }
        detailActions.addView(detailCurrentButton, LinearLayout.LayoutParams(
            dp(44), dp(48)
        ).apply { marginStart = dp(4) })
        detailActions.addView(iconButton(
            R.drawable.ic_ytm_help, "Довідка бібліотеки плейлістів"
        ) { showHelp() }, LinearLayout.LayoutParams(dp(44), dp(48)).apply {
            marginStart = dp(4)
        })
        if (!landscape) header.addView(detailActions)
        if (landscape) {
            actions.forEach {
                header.addView(it, LinearLayout.LayoutParams(dp(44), dp(48)).apply {
                    marginStart = dp(4)
                })
            }
        }
        root.addView(header)
        tabsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(2), dp(12), dp(4))
        }
        if (!landscape) actions.forEach {
            tabsRow.addView(it, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginStart = dp(2)
            })
        }
        if (!landscape) root.addView(tabsRow)
        // Compat controls retained as invisible state mirrors for existing tests;
        // no large portrait tabs or separate tall grid button are rendered.
        localTabButton = button("На телефоні") { selectTab(false) }
        onlineTabButton = button("YouTube") { selectTab(true) }

        filtersRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(3), dp(12), dp(4))
        }
        searchBox = EditText(this).apply {
            hint = "Знайти плейліст"
            setSingleLine(true)
            textSize = 14f
            setTextColor(palette.text)
            setHintTextColor(palette.muted)
            setText(filterText)
            setPadding(dp(14), dp(7), dp(10), dp(7))
            background = surface(palette.surface, 12)
        }
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterText = s?.toString().orEmpty()
                if (selectedLocal == null && selectedRemote == null) renderContent()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        filtersRow.addView(searchBox, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(49)))
        root.addView(filtersRow)

        statusLabel = text("", 13f, false, palette.muted).apply {
            setPadding(dp(12), dp(5), dp(12), dp(5))
            if (landscape) {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        }
        root.addView(statusLabel)
        // The counter/filter toolbar belongs outside the scrollable playlist grid.
        catalogueToolbar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = surface(palette.surface, 14)
        }
        root.addView(catalogueToolbar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { leftMargin = dp(10); rightMargin = dp(10) })
        scroller = ScrollView(this).apply { isFillViewport = true }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(10), dp(12))
        }
        scroller.addView(body)
        root.addView(scroller, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
        updateTopControls()
    }

    private fun selectTab(online: Boolean) {
        // Tapping the active source again deselects it and means "Всі".
        val wasSelected = !showAll && onlineTab == online
        showAll = wasSelected
        onlineTab = online
        onSourceChanged()
        selectedLocal = null
        selectedRemote = null
        visibleTrackLimit = 60
        ensureRemoteAccountIsolation()
        updateTopControls()
        renderContent(0)
        ensureRemoteReady()
    }

    private fun currentSource(): LibraryFilterPolicy.Source =
        LibraryFilterPolicy.source(showAll, onlineTab)

    /** Leaving «Тільки YouTube» resets YouTube privacy (owner 2026-10-16). */
    private fun onSourceChanged() {
        privacyMode = LibraryFilterPolicy.privacyAfterSourceChange(
            currentSource(), privacyMode)
    }

    /**
     * Makes sure the YouTube list is (being) loaded. If the screen has no
     * token but the device was authorized before (app was killed in the
     * background, BUG-052), restores Google silently first.
     * Returns true when it started work that will re-render the screen.
     */
    private fun ensureRemoteReady(): Boolean {
        if (!showsRemote()) return false
        if (token() != null) {
            if (cachedPlaylists == null && !loadingList) {
                loadRemoteList()
                return true
            }
            return false
        }
        if (googleRestoreFailed) return false
        if (!LibraryFilterPolicy.shouldRestoreGoogle(
                showsRemote = true,
                hasToken = false,
                hadPriorAuthorization = persistentAuth.hadSuccessfulAuthorization(),
                restoring = restoringGoogle,
                userActionRequired = googleNeedsUser
            )
        ) return false
        restoreGoogleSession()
        return true
    }

    private fun restoreGoogleSession() {
        restoringGoogle = true
        googleRestoreFailed = false
        renderContent()
        worker.execute {
            val result = runCatching {
                sessionRestorer.restoreBlocking(api) {
                    quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)
                }
            }.getOrDefault(SilentGoogleSessionRestorer.Result.FAILED)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                restoringGoogle = false
                when (result) {
                    SilentGoogleSessionRestorer.Result.RESTORED -> {
                        googleNeedsUser = false
                        ensureRemoteAccountIsolation()
                        val pendingDetail = selectedRemote
                            ?.takeIf { token() != null && !cachedTracks.containsKey(it) }
                        if (pendingDetail != null) loadRemoteTracks(pendingDetail)
                        if (token() != null && showsRemote() && cachedPlaylists == null) {
                            loadRemoteList()
                        } else if (pendingDetail == null) renderContent()
                    }
                    SilentGoogleSessionRestorer.Result.NEEDS_USER -> {
                        googleNeedsUser = true
                        renderContent()
                    }
                    SilentGoogleSessionRestorer.Result.NEVER_AUTHORIZED ->
                        renderContent()
                    SilentGoogleSessionRestorer.Result.FAILED -> {
                        googleRestoreFailed = true
                        renderContent()
                    }
                }
            }
        }
    }

    private fun updateTopControls() {
        val catalogue = selectedLocal == null && selectedRemote == null
        val localSelected = !showAll && !onlineTab
        val remoteSelected = !showAll && onlineTab
        compactLocalTabButton.setColorFilter(
            if (localSelected) palette.accent else palette.text)
        compactOnlineTabButton.setColorFilter(
            if (remoteSelected) palette.accent else palette.text)
        compactLocalTabButton.isSelected = localSelected
        compactOnlineTabButton.isSelected = remoteSelected
        compactLocalTabButton.visibility = if (catalogue) View.VISIBLE else View.GONE
        compactOnlineTabButton.visibility = if (catalogue) View.VISIBLE else View.GONE
        compactSearchButton.visibility = if (catalogue) View.VISIBLE else View.GONE
        compactLayoutButton.visibility = if (catalogue) View.VISIBLE else View.GONE
        if (!landscape) {
            tabsRow.visibility = if (catalogue) View.VISIBLE else View.GONE
            detailActions.visibility = if (catalogue) View.GONE else View.VISIBLE
        }
        filtersRow.visibility =
            if (catalogue && landscapeSearchOpen) View.VISIBLE else View.GONE
        compactLayoutButton.setImageResource(
            if (columns == 2) R.drawable.ic_ytm_view_list else R.drawable.ic_ytm_view_grid
        )
        compactLayoutButton.contentDescription =
            if (columns == 2) "Список одним стовпчиком" else "Плитки у два стовпчики"
        compactSearchButton.setColorFilter(
            if (landscapeSearchOpen) palette.accent else palette.text)
        compactCurrentButton.isEnabled = currentStore.load() != null
        compactCurrentButton.alpha = if (compactCurrentButton.isEnabled) 1f else 0.45f
        detailCurrentButton.isEnabled = compactCurrentButton.isEnabled
        detailCurrentButton.alpha = compactCurrentButton.alpha
        // Catalogue context is inside the bordered card; only track details
        // retain the existing narrow status caption.
        statusLabel.visibility = if (catalogue) View.GONE else View.VISIBLE
        catalogueToolbar.visibility = if (catalogue) View.VISIBLE else View.GONE
    }

    /**
     * Rebuilds the screen. [restoreY] null keeps the current scroll position
     * (refresh of the same screen); a number scrolls there after layout.
     * The old version always restored the previous position in a post{},
     * which overrode the caller's scrollTo(0, 0): a playlist opened from deep
     * in the catalogue started half-way down (owner screenshot 2026-10-12).
     */
    private fun renderContent(restoreY: Int? = null) {
        if (!::body.isInitialized) return
        updateTopControls()
        val oldY = restoreY ?: scroller.scrollY
        body.removeAllViews()
        catalogueToolbar.removeAllViews()
        cardTarget = null
        when {
            selectedLocal != null -> renderLocalDetails(selectedLocal!!)
            selectedRemote != null -> renderRemoteDetails(selectedRemote!!)
            else -> renderCatalogue()
        }
        scroller.post { if (!isDestroyed) scroller.scrollTo(0, oldY) }
    }

    private fun localItems(): List<LocalLibraryItem> =
        PlaylistLibraryPolicy.build(
            savedStore.getAll(), currentStore.load(), historyStore.getAll()
        )

    private fun renderLocalCatalogue() = renderCatalogue()

    private class SongsCheck(val total: Int) {
        var done = 0
    }

    /** One playlist as shown in the «Група №…» window. */
    private class DupMember(
        val title: String,
        val source: String,
        val isRemote: Boolean,
        val remoteTracksLoaded: Boolean,
        val open: () -> Unit,
        /** Phone: first saved; History: import; YouTube: created there. 0 = unknown. */
        val date: Long = 0L,
        val dateWord: String = "",
        /** YouTube playlist this phone/History entry is linked to. */
        val linkedRemoteId: String? = null,
        /** When remembered songs (not read in this visit) were checked. */
        val rememberedAt: Long = 0L,
        /** Songs with titles, when known (for «чим відрізняється»). */
        val songs: List<PlaylistLibraryDuplicateGroupPolicy.Song>? = null,
        /** Existing confirmation that removes exactly this copy, and its id. */
        val removeType: String? = null,
        val removeId: String? = null
    )

    /** Remembered songs of the current account (loaded once per account). */
    private fun currentSongMemory(): Map<String, RemoteSongMemoryPolicy.Entry> {
        val owner = cachedOwner
        if (owner.isNullOrBlank() || token() == null) return emptyMap()
        if (owner != songMemoryOwner) {
            songMemoryOwner = owner
            songMemory = songMemoryStore.load(owner)
        }
        return songMemory
    }

    /** Called on the UI thread after the songs of a YouTube playlist were read. */
    private fun rememberRemoteSongs(id: String, tracks: List<Track>) {
        val owner = cachedOwner?.takeIf { it.isNotBlank() } ?: return
        val itemCount = cachedPlaylists?.firstOrNull { it.id == id }?.itemCount
            ?: tracks.size.toLong()
        // Small (≤ 500 short lines) and SharedPreferences writes in the
        // background itself; done here so a closing screen cannot drop it.
        val updated = songMemoryStore.remember(owner, id, itemCount,
            tracks.map { it.selectedVideoId })
        if (songMemoryOwner == owner) songMemory = updated
    }

    private fun trackSong(videoId: String?, selected: String?, title: String, artist: String) =
        PlaylistLibraryDuplicateGroupPolicy.Song(
            videoId,
            selected?.takeIf { it.isNotBlank() }
                ?: listOf(artist, title).filter { it.isNotBlank() }.joinToString(" — ")
                    .ifBlank { "Без назви" }
        )

    private fun shortDate(millis: Long): String =
        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.ROOT)
            .format(java.util.Date(millis))

    /**
     * Metadata-only candidates. Do not spend API quota fetching all remote
     * tracks or treat an equal linked playlistId as track equality.
     * Remembers the result for the group window and the detail button.
     */
    private fun analyseDuplicates(
        saved: List<RestorablePlaylistSnapshot>,
        active: CurrentPlaylistSnapshot?,
        archive: List<HistoryEntry>,
        allLocal: List<LocalLibraryItem>,
        allRemote: List<YouTubePlaylistInfo>
    ): Map<String, PlaylistLibraryDuplicatePolicy.Hint> {
        val savedById = saved.associateBy { it.localPlaylistId }
        val archiveById = archive.associateBy { it.id }
        val memory = currentSongMemory()
        missingOnYouTube = if (token() == null) emptySet() else
            PlaylistLibraryIdentityPolicy.missingOnYouTube(
                linked = allLocal.map { item ->
                    PlaylistLibraryIdentityPolicy.LinkedLocal(
                        item.identity, item.destinationPlaylistId,
                        item.historyEntryId?.let { archiveById[it]?.youtubeChannelId }
                    )
                },
                remoteIds = allRemote.map { it.id }.toSet(),
                catalogueComplete = PlaylistLibraryIdentityPolicy.catalogueComplete(cachedPlaylists),
                currentChannelId = AuthSessionStore.current().youtubeChannelInfo?.id
            )
        val candidates = allLocal.map { item ->
            val videoIds = when (item.source) {
                LocalLibrarySource.HISTORY ->
                    archiveById[item.historyEntryId]?.tracks?.map { it.videoId.orEmpty() }
                else -> (active?.takeIf { it.localPlaylistId == item.localPlaylistId }
                    ?.playlist ?: savedById[item.localPlaylistId]?.playlist)
                    ?.tracks?.map { it.selectedVideoId.orEmpty() }
            }
            PlaylistLibraryDuplicatePolicy.Candidate(
                item.identity, item.title, item.trackCount, videoIds
            )
        } + allRemote.map { remote ->
            PlaylistLibraryDuplicatePolicy.Candidate(
                "youtube:${remote.id}", remote.title,
                remote.itemCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                cachedTracks[remote.id]?.map { it.selectedVideoId.orEmpty() }
                    ?: RemoteSongMemoryPolicy.usable(memory[remote.id], remote.itemCount)
                        ?.videoIds
            )
        }
        val found = PlaylistLibraryDuplicatePolicy.find(candidates)
        val memoryBefore = groupNumbers ?: groupNumberStore.load().also { groupNumbers = it }
        val numbered = GroupNumberPolicy.renumber(candidates, found, memoryBefore)
        // Save only when the YouTube part is really there (or Google was never
        // used): numbers given while YouTube is still loading are temporary,
        // otherwise the first save after the update could shift «№30».
        val complete = !persistentAuth.hadSuccessfulAuthorization() ||
            (token() != null && cachedPlaylists != null)
        if (complete && numbered.memory != memoryBefore) {
            groupNumbers = numbered.memory
            groupNumberStore.save(numbered.memory)
        }
        val hints = numbered.hints
        dupCandidates = candidates
        dupHints = hints
        dupMembers = allLocal.associate { item ->
            val history = item.historyEntryId?.let { archiveById[it] }
                ?.takeIf { item.source == LocalLibrarySource.HISTORY }
            val snapshot = savedById[item.localPlaylistId]
            val playlist = if (history == null) (active?.takeIf {
                it.localPlaylistId == item.localPlaylistId }?.playlist
                ?: snapshot?.playlist) else null
            item.identity to DupMember(
                date = when {
                    history != null -> history.createdAt
                    snapshot != null -> snapshot.createdAt
                    else -> item.updatedAt
                },
                dateWord = if (history != null) "імпорт" else "збережено",
                removeType = when {
                    item.source == LocalLibrarySource.HISTORY -> MODAL_DELETE_HISTORY
                    !item.localPlaylistId.isNullOrBlank() -> MODAL_DELETE_LOCAL
                    else -> null
                },
                removeId = item.identity,
                linkedRemoteId = (history?.playlistId ?: item.destinationPlaylistId)
                    ?.takeIf { it.isNotBlank() },
                songs = history?.tracks?.map {
                    trackSong(it.videoId, it.selectedTitle, it.originalTitle, it.originalArtist)
                } ?: playlist?.tracks?.map {
                    trackSong(it.selectedVideoId, it.selectedTitle, it.originalTitle, it.originalArtist)
                },
                title = item.title,
                source = when (item.source) {
                    LocalLibrarySource.CURRENT -> "Телефон • поточний"
                    LocalLibrarySource.SAVED -> "Телефон"
                    LocalLibrarySource.HISTORY -> "Архів History"
                },
                isRemote = false,
                remoteTracksLoaded = false,
                open = {
                    rememberCatalogueScroll()
                    selectedLocal = item.identity
                    selectedRemote = null
                    visibleTrackLimit = 60
                    renderContent(0)
                }
            )
        } + allRemote.associate { remote ->
            val loaded = cachedTracks[remote.id]
            val remembered = if (loaded == null)
                RemoteSongMemoryPolicy.usable(memory[remote.id], remote.itemCount) else null
            "youtube:${remote.id}" to DupMember(
                date = remote.publishedAtMillis,
                dateWord = "створено на YouTube",
                removeType = MODAL_DELETE_REMOTE,
                removeId = remote.id,
                rememberedAt = remembered?.checkedAt ?: 0L,
                songs = loaded?.map {
                    trackSong(it.selectedVideoId, it.selectedTitle, it.originalTitle, it.originalArtist)
                } ?: remembered?.videoIds?.map {
                    PlaylistLibraryDuplicateGroupPolicy.Song(it, "")
                },
                title = remote.title,
                source = "YouTube • ${privacyLabel(remote.privacyStatus)}",
                isRemote = true,
                remoteTracksLoaded = loaded != null || remembered != null,
                open = {
                    rememberCatalogueScroll()
                    selectedRemote = remote.id
                    selectedLocal = null
                    visibleTrackLimit = 60
                    renderContent(0)
                    if (!cachedTracks.containsKey(remote.id)) loadRemoteTracks(remote.id)
                }
            )
        }
        return hints
    }

    /** Only the catalogue position is remembered, not another playlist's. */
    private fun rememberCatalogueScroll() {
        if (selectedLocal == null && selectedRemote == null) {
            catalogueScrollY = scroller.scrollY
        }
    }

    /** Fresh analysis when a detail page or the group window needs it. */
    private fun refreshDuplicates() {
        val saved = savedStore.getAll()
        val active = currentStore.load()
        val archive = historyStore.getAll()
        analyseDuplicates(saved, active, archive,
            PlaylistLibraryPolicy.build(saved, active, archive),
            if (token() != null) cachedPlaylists.orEmpty() else emptyList())
    }

    private fun showDuplicateGroup(identity: String) {
        modalController.show(
            modalId = MODAL_DUP_GROUP,
            args = Bundle().apply { putString("item", identity) },
            renderer = ::renderConfirmation
        )
    }

    private fun showDuplicateDifference(identity: String) {
        modalController.show(
            modalId = MODAL_DUP_DIFF,
            args = Bundle().apply { putString("item", identity) },
            renderer = ::renderConfirmation
        )
    }

    /**
     * «Чим відрізняється» (owner 2026-10-17 05:27: which one is the original,
     * why songs are missing). Compares this playlist with the song list most
     * other playlists of the same group and the same number of songs share.
     * Read-only. Returns (subtitle, content) or null when the group is gone.
     */
    private fun createDifferenceContent(identity: String): Pair<String, View>? {
        val hint = dupHints[identity] ?: return null
        val member = dupMembers[identity] ?: return null
        val songs = member.songs ?: return null
        val members = PlaylistLibraryDuplicateGroupPolicy.members(
            hint.groupNumber, dupCandidates, dupHints)
        val target = members.firstOrNull { it.identity == identity } ?: return null
        val band = members.filter { it.trackCount == target.trackCount }
        val referenceId = PlaylistLibraryDuplicateGroupPolicy.referenceFor(identity, band)
        val reference = referenceId?.let { dupMembers[it] }
        // Remembered YouTube songs have no titles: borrow them from any member.
        val titles = members.flatMap { dupMembers[it.identity]?.songs.orEmpty() }
            .filter { it.title.isNotBlank() && it.videoId != null }
            .associate { it.videoId!! to it.title }
        fun named(list: List<PlaylistLibraryDuplicateGroupPolicy.Song>) = list.map { song ->
            when {
                song.title.isNotBlank() -> song.title
                song.videoId.isNullOrBlank() -> "пісня без відео (назва на YouTube невідома)"
                else -> titles[song.videoId] ?: "відео ${song.videoId}"
            }
        }
        val diff = PlaylistLibraryDuplicateGroupPolicy.difference(songs, reference?.songs)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun line(value: String, size: Float, bold: Boolean, colour: Int, top: Int = 0) {
            container.addView(text(value, size, bold, colour).apply {
                setPadding(dp(4), dp(top), dp(4), dp(3))
            })
        }
        line(member.title + " • " + member.source, 14f, true, palette.text)
        if (reference != null) {
            line("Порівняно з: ${reference.title} • ${reference.source}" +
                " (такий самий склад у більшості плейлістів групи з " +
                "${target.trackCount} ${PlaylistLibraryDuplicateGroupPolicy.songsWord(target.trackCount)})",
                12.5f, false, palette.muted)
        } else {
            line("Немає з чим порівняти: у групі нема іншого плейліста з " +
                "${target.trackCount} ${PlaylistLibraryDuplicateGroupPolicy.songsWord(target.trackCount)}, " +
                "пісні якого відомі.", 12.5f, false, palette.muted)
        }
        fun section(title: String, why: String, list: List<PlaylistLibraryDuplicateGroupPolicy.Song>,
                    colour: Int) {
            if (list.isEmpty()) return
            line("$title (${list.size})", 14.5f, true, colour, top = 10)
            line(why, 12f, false, palette.muted)
            named(list).forEachIndexed { i, name -> line("${i + 1}. $name", 13.5f, false, palette.text) }
        }
        section("Є тільки тут", "Цих відео нема в плейлісті, з яким порівняно. " +
            "Часто це та сама пісня, але інше відео на YouTube.", diff.onlyHere,
            palette.semantic.warning)
        section("Немає тут, але є в копіях", "Ці відео є в плейлісті, з яким порівняно.",
            diff.missingHere, palette.semantic.warning)
        section("Без відео YouTube", "Для цих пісень під час імпорту не знайшлося " +
            "відео на YouTube — тому плейліст не можна перевірити до кінця.",
            diff.withoutVideo, palette.muted)
        if (diff.isEmpty) {
            line(if (reference != null) "Відмінностей у піснях немає." else
                "Усі пісні мають відео YouTube.", 14f, true, palette.semantic.success, top = 10)
        }
        line("Нічого не змінюється й не видаляється.", 12f, false, palette.muted, top = 10)
        return "група №${hint.groupNumber} • ${target.trackCount} " +
            PlaylistLibraryDuplicateGroupPolicy.songsWord(target.trackCount) to container
    }

    /** Detail button «Схожі: група №…» when this playlist has look-alikes. */
    private fun addDuplicateGroupButton(identity: String) {
        refreshDuplicates()
        val hint = dupHints[identity] ?: return
        addFullButton("Схожі плейлісти: група №${hint.groupNumber} " +
            "(${hint.groupSize}) — порівняти ›") {
            showDuplicateGroup(identity)
        }
    }

    /**
     * The «Група №…» window (owner 2026-10-16 21:57: which ones are full copies
     * and which are not was unclear even after «?»). All members of the group,
     * also those hidden by the current filter, laid out by number of songs.
     * Returns the summary line and the content, or null when the group is gone.
     */
    private fun createDuplicateGroupContent(
        identity: String,
        close: () -> Unit
    ): Pair<String, View>? {
        val hint = dupHints[identity] ?: return null
        val members = PlaylistLibraryDuplicateGroupPolicy.members(
            hint.groupNumber, dupCandidates, dupHints)
        val overview = PlaylistLibraryDuplicateGroupPolicy.overview(members)
        val byId = members.associateBy { it.identity }
        val oldest = PlaylistLibraryDuplicateGroupPolicy.oldest(
            members.associate { it.identity to (dupMembers[it.identity]?.date ?: 0L) })
        val groupRemoteIds = members.mapNotNull { remotePlaylistId(it.identity) }.toSet()
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        container.addView(text(
            "Тут усі плейлісти цієї групи — і ті, що сховані фільтром. " +
                "Порівнюються цілі плейлісти, не окремі пісні.\n" +
                "✓ однакові пісні — справжня копія\n" +
                "≠ пісні інші — кількість та сама, а склад різний\n" +
                "? не перевірено — пісні ще не порівнювались\n" +
                "Різна кількість пісень — різні версії.\n" +
                "«Оригінал» застосунок знати не може: «найстаріший» — той, " +
                "що з'явився найраніше (на телефоні — збережено, YouTube — створено).",
            12.5f, false, palette.muted).apply {
            setPadding(dp(4), 0, dp(4), dp(8))
            setLineSpacing(0f, 1.1f)
        })
        addSongsCheckBlock(container, identity, members)
        overview.bands.forEach { band ->
            container.addView(text(
                "${band.trackCount} " +
                    PlaylistLibraryDuplicateGroupPolicy.songsWord(band.trackCount) +
                    " — ${band.rows.size} " +
                    PlaylistLibraryDuplicateGroupPolicy.playlistsWord(band.rows.size),
                15f, true, palette.accent).apply {
                setPadding(dp(4), dp(10), dp(4), dp(5))
            })
            band.rows.forEach { row ->
                val member = dupMembers[row.identity]
                val candidate = byId[row.identity]
                val symbol: String
                val colour: Int
                when (row.mark) {
                    PlaylistLibraryDuplicateGroupPolicy.Mark.SAME -> {
                        symbol = "✓"; colour = palette.semantic.success
                    }
                    PlaylistLibraryDuplicateGroupPolicy.Mark.DIFFERENT -> {
                        symbol = "≠"; colour = palette.semantic.warning
                    }
                    PlaylistLibraryDuplicateGroupPolicy.Mark.UNCHECKED -> {
                        symbol = "?"; colour = palette.muted
                    }
                }
                val videoIds = candidate?.videoIds
                val explain = when (row.mark) {
                    PlaylistLibraryDuplicateGroupPolicy.Mark.SAME ->
                        "однакові пісні ще з ${row.sameWith}"
                    PlaylistLibraryDuplicateGroupPolicy.Mark.DIFFERENT -> "пісні інші"
                    PlaylistLibraryDuplicateGroupPolicy.Mark.UNCHECKED -> when {
                        remotePlaylistId(row.identity)?.let { it in songsCheckFailed } == true ->
                            "не перевірено: YouTube не віддав пісні"
                        member != null && member.isRemote && !member.remoteTracksLoaded ->
                            "не перевірено: кнопка «Перевірити пісні YouTube» вгорі"
                        videoIds == null -> "не перевірено: склад недоступний"
                        videoIds.any { !VIDEO_ID.matches(it) } ->
                            "не перевірено: не в усіх пісень є відео YouTube"
                        videoIds.size != (candidate?.trackCount ?: -1) ->
                            "не перевірено: список пісень неповний"
                        else -> "не перевірено: немає з чим порівняти"
                    }
                }
                val hidden = visibleCardIdentities?.let { row.identity !in it } ?: false
                val rowView = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    minimumHeight = dp(52)
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                    background = GradientDrawable().apply {
                        setColor(palette.surfaceAlt)
                        cornerRadius = dp(12).toFloat()
                        setStroke(dp(1), palette.border)
                    }
                    isClickable = member != null
                    isFocusable = member != null
                    contentDescription = (member?.title ?: row.identity) + ", " + explain
                    if (member != null) {
                        setOnClickListener {
                            close()
                            member.open()
                        }
                    }
                }
                rowView.addView(text(symbol, 20f, true, colour).apply {
                    gravity = Gravity.CENTER
                }, LinearLayout.LayoutParams(dp(30), ViewGroup.LayoutParams.WRAP_CONTENT))
                val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                labels.addView(text(member?.title ?: candidate?.title.orEmpty(),
                    14f, true, palette.text).apply {
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                })
                val remembered = member?.rememberedAt?.takeIf { it > 0L }
                    ?.let { "перевірено ${shortDate(it)}" }
                val dated = member?.let { m ->
                    m.date.takeIf { it > 0L }?.let { "${m.dateWord} ${shortDate(it)}" }
                }
                val linked = if (row.identity in missingOnYouTube)
                    "пов'язаний з YouTube-плейлістом, якого вже немає" else
                    member?.linkedRemoteId?.takeIf { it in groupRemoteIds }
                    ?.let { id ->
                        "пов'язаний з YouTube (" +
                            (dupMembers["youtube:$id"]?.source?.substringAfter("YouTube • ")
                                ?: "плейліст") + ")"
                    }
                labels.addView(text(
                    listOfNotNull(member?.source, explain, remembered,
                        if (hidden) "сховано фільтром" else null).joinToString(" • "),
                    12f, false, palette.muted).apply { setPadding(0, dp(2), 0, 0) })
                val facts = listOfNotNull(dated, linked)
                if (facts.isNotEmpty() || row.identity == oldest) {
                    labels.addView(text(
                        listOfNotNull(
                            if (row.identity == oldest) "найстаріший" else null
                        ).plus(facts).joinToString(" • "),
                        12f, row.identity == oldest,
                        if (row.identity == oldest) palette.accent else palette.muted
                    ).apply { setPadding(0, dp(1), 0, 0) })
                }
                val canDiff = row.mark != PlaylistLibraryDuplicateGroupPolicy.Mark.SAME &&
                    member?.songs != null
                if (canDiff) {
                    labels.addView(text("чим відрізняється ›", 13f, true, palette.accent).apply {
                        setPadding(0, dp(6), dp(12), dp(4))
                        isClickable = true
                        isFocusable = true
                        contentDescription = "Чим відрізняється " + (member?.title ?: "")
                        setOnClickListener { showDuplicateDifference(row.identity) }
                    })
                }
                val removeType = member?.removeType
                val removeId = member?.removeId
                if (removeType != null && removeId != null &&
                    !(removeType == MODAL_DELETE_REMOTE && deletingRemoteId != null)
                ) {
                    val warn = PlaylistLibraryDuplicateGroupPolicy.removalWarnings(
                        row.identity, overview, oldest).joinToString("\n")
                    val back = members.firstOrNull { it.identity != row.identity }?.identity
                    labels.addView(text(
                        if (removeType == MODAL_DELETE_REMOTE) "Прибрати з YouTube…"
                        else "Прибрати цю копію…",
                        13f, true, palette.semantic.warning).apply {
                        setPadding(0, dp(6), dp(12), dp(4))
                        isClickable = true
                        isFocusable = true
                        contentDescription = "Прибрати " + member?.title.orEmpty()
                        setOnClickListener {
                            close()
                            showConfirmation(removeType, removeId, warn, back)
                        }
                    })
                }
                rowView.addView(labels, LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                if (member != null) {
                    rowView.addView(text("›", 20f, true, palette.muted).apply {
                        setPadding(dp(8), 0, 0, 0)
                    })
                }
                container.addView(rowView, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(5) })
            }
        }
        container.addView(text(
            "Нічого не видаляється й не об’єднується само. Торкніться рядка, " +
                "щоб відкрити плейліст.",
            12f, false, palette.muted).apply { setPadding(dp(4), dp(8), dp(4), 0) })
        return PlaylistLibraryDuplicateGroupPolicy.summary(overview) to container
    }

    private fun remotePlaylistId(identity: String): String? =
        identity.takeIf { it.startsWith("youtube:") }?.removePrefix("youtube:")

    /**
     * Batch 3 (owner 2026-10-17 02:10): most «?» in a group were YouTube
     * playlists never opened. One button reads their songs, read-only
     * (playlistItems.list only, nothing is written to YouTube), so the
     * marks become ✓ / ≠. Shows how much quota it takes before the tap.
     */
    private fun addSongsCheckBlock(
        container: LinearLayout,
        groupIdentity: String,
        members: List<PlaylistLibraryDuplicatePolicy.Candidate>
    ) {
        val running = songsCheck
        if (running != null) {
            val line = text(songsCheckProgressText(running), 13.5f, true, palette.accent).apply {
                setPadding(dp(10), dp(6), dp(10), dp(10))
            }
            songsCheckProgress = line
            container.addView(line)
            return
        }
        songsCheckProgress = null
        val pending = members.filter { candidate ->
            candidate.identity.startsWith("youtube:") &&
                dupMembers[candidate.identity]?.remoteTracksLoaded == false
        }
        if (pending.isEmpty()) return
        if (token() == null) {
            container.addView(text(
                "Щоб перевірити пісні YouTube, підключіть Google на Головній.",
                12.5f, false, palette.muted).apply { setPadding(dp(4), 0, dp(4), dp(8)) })
            return
        }
        val units = PlaylistLibraryDuplicateGroupPolicy.songsCheckQuota(
            pending.map { it.trackCount })
        val label = "Перевірити пісні YouTube (${pending.size} " +
            PlaylistLibraryDuplicateGroupPolicy.playlistsWord(pending.size) + ") ›"
        val button = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            minimumHeight = dp(52)
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = GradientDrawable().apply {
                setColor(palette.surfaceAlt)
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), palette.accent)
            }
            isClickable = true
            isFocusable = true
            contentDescription = label
            setOnClickListener {
                startSongsCheck(pending.mapNotNull { remotePlaylistId(it.identity) })
            }
        }
        button.addView(text(label, 14.5f, true, palette.accent))
        button.addView(text(
            "Лише читання — у YouTube нічого не змінюється. " +
                "≈$units од. квоти з " +
                String.format(java.util.Locale.ROOT, "%,d", QuotaTracker.GENERAL_DAILY_LIMIT)
                    .replace(',', ' ') + " на день.",
            12f, false, palette.muted).apply { setPadding(0, dp(2), 0, 0) })
        container.addView(button, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(8) })
    }

    private fun songsCheckProgressText(check: SongsCheck): String =
        "Перевіряю пісні YouTube: ${minOf(check.done + 1, check.total)} з ${check.total}…"

    /**
     * Reads the songs of [ids] one after another on the screen worker.
     * Each answer goes into the shared track cache at once, so a rotation
     * (which stops the worker) keeps what was already read; the window then
     * offers the rest. Never loops, never writes to YouTube.
     */
    private fun startSongsCheck(ids: List<String>) {
        val access = token() ?: return
        if (songsCheck != null || ids.isEmpty()) return
        val check = SongsCheck(ids.size)
        songsCheck = check
        ids.forEach { songsCheckFailed.remove(it) }
        redrawVisibleDuplicateGroup()
        worker.execute {
            for (id in ids) {
                if (Thread.currentThread().isInterrupted) break
                val result = runCatching {
                    api.listPlaylistTracks(access, id) {
                        quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)
                    }
                }
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    result.onSuccess {
                        cachedTracks[id] = it.tracks
                        rememberRemoteSongs(id, it.tracks)
                    }.onFailure { songsCheckFailed[id] = it.message ?: "Помилка YouTube API" }
                    check.done++
                    songsCheckProgress?.text = songsCheckProgressText(check)
                }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                songsCheck = null
                songsCheckProgress = null
                refreshDuplicates()
                if (selectedLocal == null && selectedRemote == null) renderContent()
                redrawVisibleDuplicateGroup()
            }
        }
    }

    /** Redraws the «Група №…» window only if it is still open. */
    private fun redrawVisibleDuplicateGroup() {
        val args = modalController.visibleArgs(MODAL_DUP_GROUP) ?: return
        modalController.show(
            modalId = MODAL_DUP_GROUP,
            args = args,
            renderer = ::renderConfirmation
        )
    }

    private fun renderCatalogue() {
        val saved = savedStore.getAll()
        val active = currentStore.load()
        val archive = historyStore.getAll()
        val allLocal = PlaylistLibraryPolicy.build(saved, active, archive)
        val localVisible = if (showsLocal()) allLocal else emptyList()
        val remoteEnabled = showsRemote()
        val access = token()
        val allRemote = if (access != null) cachedPlaylists.orEmpty() else emptyList()
        val privacy = LibraryFilterPolicy.effectivePrivacy(currentSource(), privacyMode)
        val remoteItems = if (remoteEnabled) allRemote.filter {
            privacy == LibraryFilterPolicy.Privacy.ALL ||
                it.privacyStatus.equals(privacy.name, ignoreCase = true)
        } else emptyList()
        val hints = analyseDuplicates(saved, active, archive, allLocal, allRemote)
        // Cross-reference only IDs in the current account's already fetched
        // catalogue. Retain BOTH cards and NEVER infer identical track lists.
        val exactLinks = PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            allLocal, remoteItems)

        // Only the card grid is scrollable. Counts and filters live above it.
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(10))
            background = surface(palette.surface, 14)
        }
        // Keep source counters centered within the pinned toolbar.
        // Sort is overlaid at the right, not part of the counter's measured width.
        val summary = FrameLayout(this).apply {
            minimumHeight = dp(52)
        }
        val counts = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            contentDescription = "Кількість плейлістів за джерелами"
        }
        fun counter(icon: Int, label: String, value: String, tint: Int) {
            counts.addView(ImageView(this).apply {
                setImageResource(icon)
                setColorFilter(tint)
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = label
            }, LinearLayout.LayoutParams(dp(23), dp(25)))
            counts.addView(text(": $value", 16f, true, palette.muted).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(4), 0, dp(13), 0)
                contentDescription = "$label: $value"
            })
        }
        counter(R.drawable.ic_ytm_device, "На телефоні",
            allLocal.size.toString() +
                if (!showAll && !onlineTab) " ✓" else "",
            palette.semantic.duplicate)
        counter(R.drawable.ic_ytm_youtube_play, "YouTube",
            (if (restoringGoogle || (access != null && loadingList &&
                    cachedPlaylists == null)) "…"
                else if (access == null) "—"
                else cachedPlaylists?.size?.toString() ?: "—") +
                if (!showAll && onlineTab) " ✓" else "",
            palette.accent)
        summary.addView(counts, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        val sortButton = iconButton(R.drawable.ic_ytm_sort,
            "Фільтрація і сортування плейлістів") {}
        sortButton.setOnClickListener { showFilterWindow() }
        summary.addView(sortButton, FrameLayout.LayoutParams(
            dp(44), dp(46), Gravity.END or Gravity.CENTER_VERTICAL
        ))
        catalogueToolbar.addView(summary)
        catalogueToolbar.addView(text(
            when {
                showAll -> "Всі"
                onlineTab -> "YouTube"
                else -> "На телефоні"
            } + " • " + when (sortMode) {
                SortMode.RECENT -> LibraryFilterPolicy.recentScopeLabel(currentSource())
                SortMode.NAME_ASC -> "За абеткою А–Я"
                SortMode.NAME_DESC -> "За абеткою Я–А"
                SortMode.TRACKS_DESC -> "За кількістю треків"
                SortMode.SIMILAR_FIRST -> "Спочатку можливі дублікати"
            } + (if (privacy != LibraryFilterPolicy.Privacy.ALL && remoteEnabled)
                " • YouTube: ${privacyLabel(privacy.name)}" else "") +
                (if (similarOnly) " • Тільки схожі" else ""),
            12f, false, palette.muted
        ).apply { setPadding(dp(6), dp(1), dp(6), dp(2)) })
        // Owner 2026-10-15: the «Показано…» line stays pinned, it no longer
        // scrolls away with the cards. Text is filled in after filtering.
        val shownLine = text("", 12f, false, palette.muted).apply {
            setPadding(dp(6), 0, dp(6), dp(4))
        }
        catalogueToolbar.addView(shownLine)
        // YouTube connection problems are shown here too, not only at the very
        // bottom of a long list (BUG-052: the owner only saw «—»).
        remoteStatusText(remoteEnabled, access)?.let { status ->
            catalogueToolbar.addView(text(status, 12f, true, palette.accent).apply {
                setPadding(dp(6), 0, dp(6), dp(6))
            })
        }

        val cards = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        panel.addView(cards)
        body.addView(panel)
        cardTarget = cards

        val all = mutableListOf<CardInfo>()
        localVisible.forEach { item ->
            val hint = hints[item.identity]
            if (similarOnly && hint == null) return@forEach
            all += CardInfo(
                identity = item.identity,
                title = item.title,
                subtitle = (when {
                    item.identity in missingOnYouTube -> "немає на YouTube • "
                    item.destinationPlaylistId?.let { it in exactLinks } == true -> "Збіг ID • "
                    else -> ""
                }) +
                    "${item.trackCount} треків • " + when (item.source) {
                        LocalLibrarySource.CURRENT -> "Поточний"
                        LocalLibrarySource.SAVED -> "На телефоні"
                        LocalLibrarySource.HISTORY -> "Архів History"
                    },
                videoId = item.sampleVideoId,
                localSaved = item.source != LocalLibrarySource.HISTORY,
                linkedYtm = item.destinationPlaylistId != null,
                updatedAt = item.updatedAt,
                trackCount = item.trackCount.toLong(),
                duplicateHint = hint,
                onClick = {
                    catalogueScrollY = scroller.scrollY
                    selectedLocal = item.identity
                    visibleTrackLimit = 60
                    renderContent(0)
                }
            )
        }
        remoteItems.forEach { item ->
            val hint = hints["youtube:${item.id}"]
            if (similarOnly && hint == null) return@forEach
            all += CardInfo(
                identity = "youtube:${item.id}",
                title = item.title,
                subtitle = (if (item.id in exactLinks) "Збіг ID • " else "") +
                    "${item.itemCount} відео • ${privacyLabel(item.privacyStatus)}",
                videoId = null,
                thumbnail = item.thumbnailUrl,
                badge = "YouTube",
                trackCount = item.itemCount,
                // «Нові»: creation date on YouTube (0 = unknown → at the end).
                updatedAt = item.publishedAtMillis,
                duplicateHint = hint,
                onClick = {
                    catalogueScrollY = scroller.scrollY
                    selectedRemote = item.id
                    visibleTrackLimit = 60
                    renderContent(0)
                    if (!cachedTracks.containsKey(item.id)) loadRemoteTracks(item.id)
                }
            )
        }
        val filtered = all.filter {
            it.title.contains(filterText, ignoreCase = true) ||
                it.subtitle.contains(filterText, ignoreCase = true)
        }
        val ordered = when (sortMode) {
            SortMode.RECENT -> filtered.sortedWith(
                compareByDescending<CardInfo> { it.updatedAt }
                    .thenBy { it.title.lowercase() })
            SortMode.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortMode.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortMode.TRACKS_DESC -> filtered.sortedWith(
                compareByDescending<CardInfo> { it.trackCount }
                    .thenBy { it.title.lowercase() })
            SortMode.SIMILAR_FIRST -> filtered.sortedWith(
                compareBy<CardInfo> { if (it.duplicateHint == null) 1 else 0 }
                    .thenBy { it.duplicateHint?.groupNumber ?: Int.MAX_VALUE }
                    .thenBy { it.title.lowercase() })
        }
        visibleCardIdentities = filtered.map { it.identity }.toSet()
        val groupCount = filtered.mapNotNull { it.duplicateHint?.groupNumber }
            .distinct().size
        shownLine.text = "Показано: ${filtered.size} • груп схожих: $groupCount" +
            " • архівів History: ${allLocal.count { it.source == LocalLibrarySource.HISTORY }}"
        addCards(ordered)
        if (filtered.isEmpty() && similarOnly) {
            panelInfo(cards, "У вибраному фільтрі схожих плейлистів немає. " +
                "Список YouTube може бути ще не завантажений.")
        } else if (filtered.isEmpty() && filterText.isNotBlank()) {
            panelInfo(cards, "Нічого не знайдено за запитом.")
        } else if (filtered.isEmpty() && !remoteEnabled) {
            panelInfo(cards, "Локальних плейлістів ще немає.")
        }
        if (remoteEnabled && access == null && !restoringGoogle && !googleNeedsUser &&
            persistentAuth.hadSuccessfulAuthorization()
        ) {
            val retry = button("↻ Відновити підключення Google") {
                googleRestoreFailed = false
                ensureRemoteReady()
            }
            cards.addView(retry, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(45)
            ).apply { topMargin = dp(6) })
        }
        if (remoteEnabled && access != null) {
            val refresh = button(if (loadingList) "Оновлення…" else "↻ Оновити YouTube") {
                if (!loadingList) loadRemoteList()
            }.apply { isEnabled = !loadingList }
            cards.addView(refresh, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(45)
            ).apply { topMargin = dp(6) })
        }
        cardTarget = null
    }

    /** One short line about the YouTube connection, or null when all is fine. */
    private fun remoteStatusText(remoteEnabled: Boolean, access: String?): String? {
        if (!remoteEnabled) return null
        return when {
            restoringGoogle -> "Відновлюю підключення Google/YTM…"
            access == null && googleNeedsUser ->
                "Google просить підтвердження • Головна → «2. Google / YTM»."
            access == null && googleRestoreFailed ->
                "Не вдалося відновити Google/YTM • натисніть «↻ Відновити підключення Google» внизу."
            access == null ->
                "YouTube не підключено • підключіть Google/YTM на головному екрані."
            loadError != null -> "Не вдалося оновити YouTube: $loadError"
            cachedPlaylists == null && loadingList -> "Читаю список YouTube…"
            cachedPlaylists == null -> "Список YouTube ще не завантажено."
            else -> null
        }
    }

    private fun privacyLabel(status: String) = when (status.lowercase()) {
        "public" -> "публічний"
        "private" -> "приватний"
        "unlisted" -> "за посиланням"
        else -> status
    }

    private fun panelInfo(target: LinearLayout, value: String) {
        target.addView(text(value, 12f, false, palette.muted).apply {
            setPadding(dp(6), dp(5), dp(6), dp(6))
        })
    }

    /**
     * Themed filter window (owner 2026-10-15/16). Replaces the native grey
     * PopupMenu, which was off-theme, cut long labels and closed on rotation.
     * It is a RestorableModalController modal like «?», so it survives
     * rotation; every choice applies at once and the window stays open.
     */
    private fun showFilterWindow() {
        modalController.show(
            modalId = MODAL_FILTER,
            args = Bundle(),
            renderer = ::renderConfirmation
        )
    }

    private fun createFilterContent(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(4))
        }
        fillFilterContent(container)
        return container
    }

    private fun fillFilterContent(root: LinearLayout) {
        root.removeAllViews()
        // Portrait: one column. Landscape: Джерело + Доступність YouTube on the
        // left, Сортування + Дублікати on the right (owner 2026-10-16 21:57).
        var container = root
        var leftColumn = root
        var rightColumn = root
        if (landscape) {
            root.orientation = LinearLayout.HORIZONTAL
            leftColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            rightColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            root.addView(leftColumn, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(8) })
            root.addView(rightColumn, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) })
            container = leftColumn
        } else {
            root.orientation = LinearLayout.VERTICAL
        }
        fun section(title: String, note: String? = null) {
            container.addView(text(title, 15f, true, palette.accent).apply {
                setPadding(dp(4), dp(10), 0, dp(5))
            })
            if (note != null) {
                container.addView(text(note, 12.5f, false, palette.muted).apply {
                    setPadding(dp(4), 0, dp(4), dp(6))
                })
            }
        }
        fun option(label: String, selected: Boolean, hint: String? = null,
                   onPick: () -> Unit) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = dp(46)
                setPadding(dp(12), dp(8), dp(10), dp(8))
                background = GradientDrawable().apply {
                    setColor(palette.surfaceAlt)
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(if (selected) 2 else 1),
                        if (selected) palette.accent else palette.border)
                }
                isClickable = true
                isFocusable = true
                contentDescription = label + if (selected) ", вибрано" else ""
            }
            val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            labels.addView(text(label, 14.5f, selected,
                if (selected) palette.text else palette.muted))
            if (hint != null) {
                labels.addView(text(hint, 12f, false, palette.muted).apply {
                    setPadding(0, dp(2), 0, 0)
                })
            }
            row.addView(labels, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    if (selected) setColor(palette.accent)
                    else setColor(android.graphics.Color.TRANSPARENT)
                    setStroke(dp(2), if (selected) palette.accent else palette.muted)
                }
            }, LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginStart = dp(10) })
            row.setOnClickListener {
                onPick()
                applyFilterChange()
                fillFilterContent(root)
            }
            container.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(5) })
        }
        fun pickSource(all: Boolean, online: Boolean) {
            showAll = all
            if (!all) onlineTab = online
            onSourceChanged()
            selectedLocal = null
            selectedRemote = null
            visibleTrackLimit = 60
        }
        val source = currentSource()
        section("Джерело")
        option("Всі плейлісти", source == LibraryFilterPolicy.Source.ALL) {
            pickSource(all = true, online = onlineTab)
        }
        option("Тільки на телефоні", source == LibraryFilterPolicy.Source.PHONE) {
            pickSource(all = false, online = false)
        }
        option("Тільки YouTube", source == LibraryFilterPolicy.Source.YOUTUBE) {
            pickSource(all = false, online = true)
        }
        container = rightColumn
        section("Сортування")
        option("Нові спочатку", sortMode == SortMode.RECENT,
            hint = LibraryFilterPolicy.recentOptionHint(source)) {
            sortMode = SortMode.RECENT
        }
        option("За абеткою • А–Я", sortMode == SortMode.NAME_ASC) {
            sortMode = SortMode.NAME_ASC
        }
        option("За абеткою • Я–А", sortMode == SortMode.NAME_DESC) {
            sortMode = SortMode.NAME_DESC
        }
        option("За кількістю треків", sortMode == SortMode.TRACKS_DESC) {
            sortMode = SortMode.TRACKS_DESC
        }
        option("Спочатку можливі дублікати", sortMode == SortMode.SIMILAR_FIRST,
            hint = "Схожі плейлісти стоять поруч, за номером групи") {
            sortMode = SortMode.SIMILAR_FIRST
        }
        container = leftColumn
        if (LibraryFilterPolicy.privacyAvailable(source)) {
            section("Доступність YouTube")
            option("Будь-яка", privacyMode == LibraryFilterPolicy.Privacy.ALL) {
                privacyMode = LibraryFilterPolicy.Privacy.ALL
            }
            option("Публічні", privacyMode == LibraryFilterPolicy.Privacy.PUBLIC) {
                privacyMode = LibraryFilterPolicy.Privacy.PUBLIC
            }
            option("Приватні", privacyMode == LibraryFilterPolicy.Privacy.PRIVATE) {
                privacyMode = LibraryFilterPolicy.Privacy.PRIVATE
            }
            option("За посиланням", privacyMode == LibraryFilterPolicy.Privacy.UNLISTED) {
                privacyMode = LibraryFilterPolicy.Privacy.UNLISTED
            }
        } else {
            section("Доступність YouTube",
                "Доступно, коли в «Джерело» вибрано «Тільки YouTube».")
        }
        container = rightColumn
        section("Дублікати")
        option("Показати всі записи", !similarOnly) { similarOnly = false }
        option("Лише можливі дублікати", similarOnly,
            hint = "Лише плейлісти з рядком «Копія… / Схоже на копію… / Інша версія…». " +
                "Торкніться цього рядка на картці — побачите всю групу") {
            similarOnly = true
        }
    }

    private fun applyFilterChange() {
        ensureRemoteAccountIsolation()
        updateTopControls()
        renderContent(0)
        ensureRemoteReady()
    }

    private fun showHelp() {
        modalController.show(
            modalId = MODAL_HELP,
            args = Bundle(),
            renderer = ::renderConfirmation
        )
    }

    private fun renderLocalDetails(identity: String) {
        val item = localItems().firstOrNull { it.identity == identity }
        if (item == null) {
            selectedLocal = null
            renderLocalCatalogue()
            return
        }
        val current = currentStore.load()?.takeIf {
            it.localPlaylistId == item.localPlaylistId
        }
        val saved = item.localPlaylistId?.let(savedStore::get)
        val history = item.historyEntryId?.let(historyStore::get)
        val plan = history?.let {
            HistoryRecoveryPolicy.plan(it, saved, "history-library-${it.id}")
        }
        val playlist = current?.playlist ?: saved?.playlist ?: plan?.playlist
        if (playlist == null) {
            statusLabel.text = "На телефоні • ${item.trackCount} треків"
            addHeading(item.title)
            addInfo("Склад плейліста недоступний. Можливо, його дані вже видалено.")
            if (item.source == LocalLibrarySource.HISTORY) {
                addFullButton("Видалити запис History") {
                    showConfirmation(MODAL_DELETE_HISTORY, item.identity)
                }
            }
            return
        }
        statusLabel.text = "На телефоні • ${item.trackCount} треків"
        // The header Back arrow already returns to the catalogue.
        addHeading(playlist.name)
        addInfo(
            when (item.source) {
                LocalLibrarySource.HISTORY -> "Архів History • ${item.sourceLabel}"
                else -> "${item.sourceLabel} • локальна копія"
            } + if (item.destinationPlaylistId != null) " • Пов’язано з YTM" else ""
        )
        if (!item.isCurrent) {
            addFullButton("Зробити поточним плейлістом") {
                showConfirmation(MODAL_SWITCH, item.identity)
            }
        }
        item.destinationPlaylistId?.let { remoteId ->
            addFullButton("Переглянути плейліст на YouTube Music ↗") {
                openExternal("https://music.youtube.com/playlist?list=" +
                    Uri.encode(remoteId))
            }
        }
        if (item.source == LocalLibrarySource.HISTORY) {
            addFullButton("Видалити запис History") {
                showConfirmation(MODAL_DELETE_HISTORY, item.identity)
            }
        } else if (!item.localPlaylistId.isNullOrBlank()) {
            addFullButton("Видалити локальну копію") {
                showConfirmation(MODAL_DELETE_LOCAL, item.identity)
            }
        }
        addDuplicateGroupButton(item.identity)
        addHeading("Треки • ${playlist.tracks.size}")
        showTracks(playlist.tracks)
    }

    private fun renderOnlineCatalogue() = renderCatalogue()

    private fun renderRemoteDetails(playlistId: String) {
        val item = cachedPlaylists?.firstOrNull { it.id == playlistId }
        if (item == null) {
            selectedRemote = null
            renderOnlineCatalogue()
            return
        }
        statusLabel.text = "YouTube • ${item.itemCount} відео • віддалений плейлист"
        // One header Back path is shared by local and YouTube details.
        addHeading(item.title)
        addInfo("На YouTube • ${item.privacyStatus}. Треки не завантажуються на телефон.")
        addFullButton("Відкрити в YouTube Music ↗") {
            openExternal("https://music.youtube.com/playlist?list=" +
                Uri.encode(playlistId))
        }
        if (deletingRemoteId == playlistId) {
            addInfo("Видаляю плейлист із YouTube Music…")
        } else if (deletingRemoteId == null) {
            addFullButton("Видалити з YouTube Music") {
                showConfirmation(MODAL_DELETE_REMOTE, playlistId)
            }
        }
        addDuplicateGroupButton("youtube:$playlistId")
        val tracks = cachedTracks[playlistId]
        if (tracks == null) {
            addInfo(
                if (loadingTracks) "Завантажую список треків…"
                else "Список треків не завантажено."
            )
            if (!loadingTracks) addFullButton("Завантажити треки") {
                loadRemoteTracks(playlistId)
            }
            loadError?.let { addInfo("Помилка: $it") }
            return
        }
        val linked = savedStore.getAll().firstOrNull {
            it.destinationPlaylistId == playlistId
        }
        addFullButton(
            if (linked == null) "Зберегти локальну копію як поточну"
            else "Зробити локальну копію поточною"
        ) {
            showConfirmation(MODAL_SAVE_REMOTE, playlistId)
        }
        addHeading("Треки • ${tracks.size}")
        showTracks(tracks)
    }

    private fun showConfirmation(
        type: String,
        id: String,
        warning: String? = null,
        groupReturn: String? = null
    ) {
        modalController.show(
            modalId = type,
            args = Bundle().apply {
                putString("item", id)
                warning?.takeIf { it.isNotBlank() }?.let { putString("warn", it) }
                groupReturn?.let { putString("group_return", it) }
            },
            renderer = ::renderConfirmation
        )
    }

    /**
     * «Скасувати» in a removal started from «Група №…» goes back to that group
     * (owner 2026-10-17 21:27: it dropped him into the list at another place).
     */
    private fun cancelBackToGroup(args: Bundle) {
        modalController.clearState()
        val back = args.getString("group_return") ?: return
        window.decorView.post {
            if (!isDestroyed && dupHints[back] != null) showDuplicateGroup(back)
        }
    }

    /**
     * The History record of the same import that the Library hides while the
     * phone copy exists (PlaylistLibraryPolicy.build). After the copy is
     * removed it shows up (owner recording 2026-10-18 01:03: «nothing changed»).
     */
    private fun hiddenArchiveOf(localId: String): HistoryEntry? =
        historyStore.getAll()
            .filter { it.localPlaylistId == localId && it.tracks.isNotEmpty() }
            .maxByOrNull { it.updatedAt }

    /** Text added under a removal message when it came from the group window. */
    private fun warningBlock(args: Bundle): String =
        args.getString("warn")?.let { "\n\n$it" }.orEmpty()

    /**
     * After a removal started in «Група №…», show that group again (through
     * any other member). If only one playlist is left, there is no group.
     */
    private fun reopenGroup(identity: String?) {
        identity ?: return
        refreshDuplicates()
        if (dupHints[identity] != null) {
            window.decorView.post { if (!isDestroyed) showDuplicateGroup(identity) }
        } else {
            toast("Групи більше нема: лишився один плейліст")
        }
    }

    private fun renderConfirmation(type: String, args: Bundle): Dialog? {
        if (type == MODAL_DELETE_HISTORY) {
            val identity = args.getString("item") ?: return null
            val entryId = localItems().firstOrNull {
                it.identity == identity && it.source == LocalLibrarySource.HISTORY
            }?.historyEntryId ?: return null
            val entry = historyStore.get(entryId) ?: return null
            return UiChrome.showDangerConfirmDialog(
                activity = this,
                title = "Видалити запис History?",
                message = "Буде видалено лише запис історії «${entry.playlistName}».\n\n" +
                    "Поточний плейлист, збережені локальні копії та YouTube не зміняться." +
                    warningBlock(args),
                confirmLabel = "Видалити з History",
                onCancel = { cancelBackToGroup(args) }
            ) {
                modalController.clearState()
                if (historyStore.get(entryId) != null) {
                    historyStore.remove(entryId)
                    selectedLocal = null
                    renderContent()
                    toast("Запис History видалено")
                    reopenGroup(args.getString("group_return"))
                }
            }
        }
        if (type == MODAL_DELETE_REMOTE) {
            val id = args.getString("item") ?: return null
            val item = cachedPlaylists?.firstOrNull { it.id == id }
                ?: return null
            if (deletingRemoteId != null) return null
            return UiChrome.showDangerConfirmDialog(
                activity = this,
                title = "Видалити плейлист із YouTube Music?",
                message = "«${item.title}» (${item.itemCount} відео) " +
                    "буде назавжди видалено з YouTube Music.\n" +
                    "ID: ${item.id}\n\n" +
                    "Локальні копії та History залишаться. Цю дію неможливо скасувати." +
                    warningBlock(args),
                confirmLabel = "Видалити з YouTube",
                onCancel = { cancelBackToGroup(args) }
            ) {
                modalController.clearState()
                pendingGroupReturn = args.getString("group_return")
                startRemoteDelete(id)
            }
        }
        if (type == MODAL_DELETE_LOCAL) {
            val identity = args.getString("item") ?: return null
            val target = localItems().firstOrNull {
                it.identity == identity && it.source != LocalLibrarySource.HISTORY
            } ?: return null
            val localId = target.localPlaylistId?.takeIf(String::isNotBlank)
                ?: return null
            return UiChrome.showDangerConfirmDialog(
                activity = this,
                title = "Видалити локальну копію?",
                message = "Плейлист «${target.title}» буде видалено з локальної " +
                    "бібліотеки. Якщо він поточний, Home очиститься.\n\n" +
                    "Інші локальні копії, History, YTM Project і YouTube не " +
                    "зміняться. " + (hiddenArchiveOf(localId)?.let { archive ->
                        "Після цього в Бібліотеці з’явиться архів History цього " +
                            "імпорту (${shortDate(archive.createdAt)}): " +
                            "${archive.tracks.size} " +
                            PlaylistLibraryDuplicateGroupPolicy.songsWord(archive.tracks.size) +
                            ". Він не видаляється."
                    } ?: "Архів History може залишитися видимим окремо.") +
                    warningBlock(args),
                confirmLabel = "Видалити локально",
                onCancel = { cancelBackToGroup(args) }
            ) {
                modalController.clearState()
                if (deleteLocalCopy(localId)) reopenGroup(args.getString("group_return"))
            }
        }
        if (type == MODAL_FILTER) {
            val done = UiChrome.DialogAction("Готово") { modalController.clearState() }
            // Owner 2026-10-16 21:57: in landscape the window showed about one
            // option. There: title and «Готово» in one row, options in two columns.
            if (landscape) {
                return UiChrome.showWideContentDialog(
                    activity = this,
                    title = "Фільтр і сортування",
                    content = createFilterContent(),
                    doneAction = done
                )
            }
            return UiChrome.showContentDialog(
                activity = this,
                title = "Фільтр і сортування",
                subtitle = "Вибір діє одразу",
                message = "",
                content = createFilterContent(),
                actions = listOf(done)
            )
        }
        if (type == MODAL_DUP_GROUP) {
            val identity = args.getString("item") ?: return null
            if (dupHints[identity] == null) refreshDuplicates()
            val number = dupHints[identity]?.groupNumber ?: return null
            var dialog: Dialog? = null
            val built = createDuplicateGroupContent(identity) {
                modalController.clearState()
                dialog?.dismiss()
            } ?: return null
            dialog = UiChrome.showContentDialog(
                activity = this,
                title = "Група №$number — схожі плейлісти",
                subtitle = built.first,
                message = "",
                content = built.second,
                actions = listOf(UiChrome.DialogAction("Закрити") {
                    modalController.clearState()
                })
            )
            return dialog
        }
        if (type == MODAL_DUP_DIFF) {
            val identity = args.getString("item") ?: return null
            if (dupHints[identity] == null) refreshDuplicates()
            val built = createDifferenceContent(identity) ?: return null
            return UiChrome.showContentDialog(
                activity = this,
                title = "Чим відрізняється",
                subtitle = built.first,
                message = "",
                content = built.second,
                actions = listOf(UiChrome.DialogAction("Назад до групи") {
                    modalController.clearState()
                    window.decorView.post { if (!isDestroyed) showDuplicateGroup(identity) }
                })
            )
        }
        if (type == MODAL_HELP) {
            return UiChrome.showContentDialog(
                activity = this,
                title = "Бібліотека плейлістів • довідка",
                subtitle = "Піктограми, джерела, сортування та обкладинки",
                message = "Натискайте на піктограми для перегляду, пошуку й навігації. " +
                    "Перегляд не змінює плейлісти у YouTube Music.",
                content = createLibraryHelpContent(),
                actions = listOf(UiChrome.DialogAction("Зрозуміло") {
                    modalController.clearState()
                })
            )
        }
        val id = args.getString("item") ?: return null
        val title: String
        val message: String
        val actionLabel: String
        when (type) {
            MODAL_SWITCH -> {
                if (localItems().none { it.identity == id }) return null
                title = "Змінити поточний плейліст?"
                message = "Попередній плейліст збережений локально. " +
                    "Ця дія не запускає пошук або запис у YouTube."
                actionLabel = "Перемкнути"
            }
            MODAL_SAVE_REMOTE -> {
                if (cachedTracks[id] == null ||
                    cachedPlaylists?.none { it.id == id } != false
                ) return null
                title = "Зберегти плейліст на телефоні?"
                message = "Збережу назви та videoId треків локально й зроблю " +
                    "плейліст поточним. Медіафайли не завантажуються, " +
                    "YouTube Music не змінюється."
                actionLabel = "Зберегти"
            }
            else -> return null
        }
        return UiChrome.showFixedFooterMessageDialog(
            activity = this,
            title = title,
            message = message,
            actions = listOf(
                UiChrome.DialogAction(
                    label = actionLabel,
                    tone = UiChrome.ActionTone.ACCENT
                ) {
                    modalController.clearState()
                    if (type == MODAL_SWITCH) setLocalCurrent(id)
                    else saveRemoteAsCurrent(id)
                },
                UiChrome.DialogAction("Скасувати") {
                    modalController.clearState()
                }
            )
        )
    }

    private fun startRemoteDelete(id: String) {
        val target = cachedPlaylists?.firstOrNull { it.id == id } ?: run {
            toast("Плейлист більше не знайдено в каталозі")
            return
        }
        if (deletingRemoteId != null) return
        deletingRemoteId = id
        if (!DestinationRemoteOperations.startDelete(this, target)) {
            deletingRemoteId = null
            toast("Не вдалося почати видалення. Перевірте підключення.")
        }
        renderContent()
    }

    private fun onRemoteDeleteState(state: DestinationRemoteOperations.State) {
        val id = deletingRemoteId ?: return
        if (state.kind != DestinationRemoteOperations.Kind.DELETE_PLAYLIST ||
            state.target?.id != id
        ) return
        if (state.running || state.terminalSerial == 0L) return
        deletingRemoteId = null
        DestinationRemoteOperations.acknowledgeTerminal(state.terminalSerial)
        if (state.successMessage != null) {
            // Re-fetch exact server state; no optimistic count is treated as authoritative.
            cachedPlaylists = null
            cachedTracks.remove(id)
            selectedRemote = null
            visibleTrackLimit = 60
            toast("Плейлист видалено з YouTube. Оновлюю каталог…")
            if (showsRemote() && token() != null) loadRemoteList()
            else {
                renderContent()
                pendingGroupReturn?.let { back ->
                    pendingGroupReturn = null
                    reopenGroup(back)
                }
            }
        } else {
            pendingGroupReturn = null
            toast(state.errorMessage ?: "Не вдалося видалити плейлист із YouTube")
            renderContent()
        }
    }

    private fun deleteLocalCopy(localId: String): Boolean {
        // Check the exact opaque ID again after rotation/confirmation.
        val target = localItems().firstOrNull {
            it.localPlaylistId == localId &&
                it.source != LocalLibrarySource.HISTORY
        }
        if (target == null) {
            toast("Локальна копія вже недоступна")
            selectedLocal = null
            renderContent()
            return false
        }
        val clearedCurrent = currentStore.clearIfMatches(localId)
        val removedSaved = savedStore.remove(localId)
        if (!clearedCurrent && !removedSaved) {
            toast("Локальну копію вже видалено")
            return false
        }
        if (clearedCurrent) {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_CURRENT_CHANGED, true))
        }
        selectedLocal = null
        visibleTrackLimit = 60
        renderContent(0)
        toast("Локальну копію видалено" + (hiddenArchiveOf(localId)?.let {
            " • з’явився архів History ${shortDate(it.createdAt)}"
        } ?: ""))
        return true
    }

    private fun createLibraryHelpContent(): View {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(2), 0, dp(7))
        }
        fun section(name: String) {
            content.addView(text(name, 15f, true, palette.accent).apply {
                setPadding(dp(4), dp(10), 0, dp(6))
            })
        }
        fun entry(icon: Int, title: String, description: String, tint: Int = palette.text) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                setPadding(dp(7), dp(9), dp(7), dp(9))
                background = surface(palette.surfaceAlt, 10)
            }
            row.addView(ImageView(this).apply {
                setImageResource(icon)
                setColorFilter(tint)
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = title
            }, LinearLayout.LayoutParams(dp(30), dp(32)).apply { marginEnd = dp(13) })
            val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            labels.addView(text(title, 14f, true, palette.text))
            labels.addView(text(description, 12.5f, false, palette.muted).apply {
                setPadding(0, dp(4), 0, 0)
            })
            row.addView(labels, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ))
            content.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(5) })
        }
        section("Верхня панель")
        entry(R.drawable.ic_ytm_device, "Телефон",
            "Показує локальні плейлісти, поточний список і архів History. " +
                "Натисніть вибрану кнопку вдруге, щоб повернутися до «Всі».",
            palette.semantic.duplicate)
        entry(R.drawable.ic_ytm_youtube_play, "YouTube",
            "Показує плейлісти підключеного YouTube-акаунта. " +
                "Каталог читається з інтернету; простий перегляд нічого не записує.",
            palette.accent)
        entry(R.drawable.ic_ytm_search, "Пошук",
            "Відкриває поле для пошуку за назвою та описом у вибраному каталозі.")
        entry(R.drawable.ic_ytm_library_current, "Поточний плейліст",
            "Переходить до поточного локального плейліста, не перемикаючи інші.")
        entry(R.drawable.ic_ytm_view_list, "Список / плитки",
            "Перемикає один широкий рядок із деталями або сітку з двох карток.")
        entry(R.drawable.ic_ytm_help, "Довідка (?)",
            "Відкриває ці пояснення. Після закриття ви повернетеся на той самий екран.")
        section("Каталог, фільтри та сортування")
        entry(R.drawable.ic_ytm_sort, "Фільтр і сортування",
            "Панель з лічильниками й фільтром не прокручується разом із " +
                "картками. У вікні фільтра: джерело, сортування, можливі " +
                "дублікати. Доступність YouTube (публічні, приватні, за " +
                "посиланням) з’являється лише при «Тільки YouTube»; коли " +
                "джерело змінюється, вона скидається на «Будь-яка». " +
                "Вікно не закривається при повороті екрана.")
        entry(R.drawable.ic_ytm_sort, "Нові спочатку",
            "Плейлісти на телефоні — за датою збереження на телефоні; " +
                "плейлісти YouTube — за датою створення на YouTube. " +
                "Рядок під лічильниками пише, яка дата діє.")
        entry(R.drawable.ic_ytm_device, "Лічильник телефону",
            "Кількість локальних записів. ✓ біля ЧИСЛА означає лише " +
                "вибраний фільтр «Телефон», а не перевірені треки.",
            palette.semantic.duplicate)
        entry(R.drawable.ic_ytm_youtube_play, "Лічильник YouTube",
            "Кількість плейлістів, прочитаних з акаунта. ✓ біля ЧИСЛА " +
                "означає вибраний фільтр «YouTube». Без ✓ фільтр не вибрано; " +
                "«—» — кількість невідома. Це не стан синхронізації.",
            palette.accent)
        section("Позначки внизу карток")
        entry(R.drawable.ic_ytm_device, "Телефон ✓",
            "Назви, ID та список треків збережені локально в застосунку. " +
                "Це НЕ означає завантаження музики чи відео без інтернету.",
            palette.semantic.duplicate)
        entry(R.drawable.ic_ytm_youtube_play, "YouTube ✓",
            "Локальний запис має збережений ID пов’язаного плейліста " +
                "YouTube Music. Це НЕ гарантує синхронізацію кожного треку.",
            palette.accent)
        entry(R.drawable.ic_ytm_youtube_play, "YouTube (без галочки)",
            "Напис «YouTube» означає плейліст із каталогу акаунта. " +
                "На відміну від YouTube ✓, локальний зв’язок за ID тут " +
                "не підтверджено. Жоден із цих значків не перевіряє всі треки.",
            palette.accent)
        entry(R.drawable.ic_ytm_help, "Повнота треків — окремий стан",
            "Не можна знати, чи кожен трек присутній на YouTube, доки " +
                "не порівняно склади плейлистів. Позначки ✓, ? або помилку " +
                "для повноти можна показувати лише після такої перевірки.")
        section("Видалення")
        entry(R.drawable.ic_ytm_view_grid, "Прибрати цю копію…",
            "У вікні групи біля кожного плейліста. Відкриває те саме " +
                "підтвердження, що й на сторінці плейліста, і додає підказку: " +
                "чи загубляться пісні (✓ — ні, лишаються копії; ≠ / ? — можуть). " +
                "Лише по одному, лише вашим натисканням. Після видалення " +
                "вікно групи відкривається знову.", palette.semantic.warning)
        entry(R.drawable.ic_ytm_device, "немає на YouTube",
            "Плейліст на телефоні пов’язаний з YouTube-плейлістом, якого вже " +
                "нема у вашому акаунті (список YouTube завантажено повністю). " +
                "Лише позначка: копія на телефоні лишається, нічого не " +
                "відв’язується й не видаляється само.")
        entry(R.drawable.ic_ytm_device, "Локальна копія",
            "Видаляє лише точний локальний ID. Якщо це поточний список, " +
                "головна сторінка очиститься. History та YouTube не зміняться.")
        entry(R.drawable.ic_ytm_music, "Архів History",
            "Видаляє лише конкретний запис операції з History, " +
                "але не локальний і не YouTube-плейлист.")
        entry(R.drawable.ic_ytm_youtube_play, "YouTube Music",
            "Назавжди видаляє вибраний плейлист із YouTube за точним ID " +
                "після окремого попередження. Локальні копії та History зберігаються. " +
                "Після успіху каталог YouTube перечитується.", palette.accent)
        // Owner 2026-10-16 03:45 and 21:57: the duplicate line was not
        // understandable, even with the old legend. Now the card says it in
        // plain words and a tap opens the whole group side by side.
        section("Рядок про копії на картці")
        entry(R.drawable.ic_ytm_view_grid, "Порівнюються цілі плейлісти",
            "Не окремі пісні, а плейлісти (альбоми) цілком — на телефоні, " +
                "в архіві History і на YouTube. Нічого не видаляється й не " +
                "об’єднується само.", palette.accent)
        entry(R.drawable.ic_ytm_view_grid, "Копія",
            "Пісні перевірено — вони точно ті самі. Справжня копія.")
        entry(R.drawable.ic_ytm_view_grid, "Схоже на копію",
            "Та сама назва і стільки ж пісень, але самі пісні ще не " +
                "порівнювались. Для YouTube — у вікні групи кнопка " +
                "«Перевірити пісні YouTube» (лише читає, нічого не змінює), " +
                "або відкрийте плейліст.")
        entry(R.drawable.ic_ytm_view_grid, "Інша версія",
            "Та сама назва, але інша кількість пісень.")
        // Owner 2026-10-17 03:02: «(2)» was read as 2 songs, «№7» as a rank.
        entry(R.drawable.ic_ytm_view_grid, "група №8 (4) ›",
            "(4) — у групі 4 ПЛЕЙЛІСТИ (не пісні; скільки пісень — рядком вище). " +
                "№8 — просто ярлик, щоб знайти плейлісти однієї групи: у них " +
                "однаковий номер. Сам номер нічого не означає, але він постійний: " +
                "група тримає його, поки існує, а нові групи отримують нові " +
                "номери. Торкніться рядка — відкриється " +
                "«Група №8»: усі 4 плейлісти поруч, з позначками ✓ однакові пісні, " +
                "≠ пісні інші, ? не перевірено, з датою і позначкою «найстаріший».",
            palette.accent)
        entry(R.drawable.ic_ytm_view_grid, "чим відрізняється ›",
            "У вікні групи біля ≠ і ?: показує, які саме пісні інші, яких " +
                "бракує і для яких нема відео YouTube. Лише показує.")
        entry(R.drawable.ic_ytm_youtube_play, "перевірено 17.10",
            "Пісні YouTube-плейліста, перевірені раніше, телефон пам’ятає " +
                "(лише коди відео, без назв, для кожного акаунта окремо), поки " +
                "кількість пісень у плейлісті та сама.")
        entry(R.drawable.ic_ytm_device, "Архів History",
            "Запис про минулий імпорт, а не окремий плейліст. Тому в однієї " +
                "назви може бути багато таких записів у групі.",
            palette.semantic.duplicate)
        section("Обкладинки та зв’язки")
        entry(R.drawable.ic_ytm_music, "Немає обкладинки",
            "Мініатюра береться з доступних YouTube-даних за videoId. " +
                "Коли дійсного ID немає, показується нейтральна нота. " +
                "Для архівів може знадобитися відновлення метаданих.")
        entry(R.drawable.ic_ytm_youtube_play, "Збіг ID",
            "У локальній та YouTube-картці знайдено однаковий playlistId " +
                "у вже завантаженому каталозі поточного акаунта. " +
                "Це НЕ підтверджує однакові треки, повну синхронізацію " +
                "чи власника історичного запису. Обидві картки збережено.",
            palette.accent)
        return content
    }

    private fun setLocalCurrent(identity: String) {
        val item = localItems().firstOrNull { it.identity == identity } ?: return
        val saved = item.localPlaylistId?.let(savedStore::get)
        val current = currentStore.load()?.takeIf {
            it.localPlaylistId == item.localPlaylistId
        }
        val history = item.historyEntryId?.let(historyStore::get)
        val plan = history?.let {
            HistoryRecoveryPolicy.plan(it, saved, "history-library-${it.id}")
        }
        val playlist = current?.playlist ?: saved?.playlist ?: plan?.playlist ?: return
        val resolvedId = item.localPlaylistId ?: plan?.localPlaylistId ?: return
        currentStore.save(
            playlist = playlist,
            sourceLabel = item.sourceLabel,
            destinationPlaylistId = item.destinationPlaylistId,
            destinationPlaylistTitle = item.destinationTitle,
            localPlaylistId = resolvedId,
            sourceHistoryId = item.historyEntryId
        )
        setResult(RESULT_OK, Intent().putExtra(EXTRA_CURRENT_CHANGED, true))
        selectedLocal = "saved:$resolvedId"
        toast("Плейліст став поточним")
        renderContent()
    }

    private fun saveRemoteAsCurrent(id: String) {
        val item = cachedPlaylists?.firstOrNull { it.id == id } ?: return
        val tracks = cachedTracks[id] ?: return
        val linked = savedStore.getAll().firstOrNull {
            it.destinationPlaylistId == id
        }
        val chosenTracks = linked?.playlist?.tracks
            ?: tracks.map { it.copy() }.toMutableList()
        currentStore.save(
            playlist = ImportedPlaylist(item.title, chosenTracks),
            sourceLabel = "YouTube • ${item.title}",
            destinationPlaylistId = item.id,
            destinationPlaylistTitle = item.title,
            localPlaylistId = linked?.localPlaylistId
        )
        setResult(RESULT_OK, Intent().putExtra(EXTRA_CURRENT_CHANGED, true))
        toast("Збережено локально. Нічого не записано в YouTube.")
    }

    private fun loadRemoteList() {
        val access = token() ?: return
        if (loadingList) return
        loadingList = true
        loadError = null
        renderContent()
        worker.execute {
            val result = runCatching {
                api.listMyPlaylists(access) {
                    quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)
                }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                loadingList = false
                result.onSuccess { cachedPlaylists = it }
                    .onFailure { loadError = it.message ?: "Помилка YouTube API" }
                renderContent()
                if (result.isSuccess) {
                    pendingGroupReturn?.let { back ->
                        pendingGroupReturn = null
                        reopenGroup(back)
                    }
                }
            }
        }
    }

    private fun loadRemoteTracks(id: String) {
        val access = token() ?: return
        if (loadingTracks) return
        loadingTracks = true
        loadError = null
        renderContent()
        worker.execute {
            val result = runCatching {
                api.listPlaylistTracks(access, id) {
                    quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)
                }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                loadingTracks = false
                result.onSuccess {
                    cachedTracks[id] = it.tracks
                    rememberRemoteSongs(id, it.tracks)
                }.onFailure { loadError = it.message ?: "Помилка YouTube API" }
                if (selectedRemote == id) renderContent()
            }
        }
    }

    private fun ensureRemoteAccountIsolation() {
        val auth = AuthSessionStore.current()
        if (auth.accessToken == null) {
            cachedPlaylists = null
            cachedTracks.clear()
            cachedOwner = null
            return
        }
        val owner = auth.youtubeChannelInfo?.id
            ?: auth.googleAccountInfo?.email ?: ""
        if (owner != cachedOwner) {
            cachedOwner = owner
            cachedPlaylists = null
            cachedTracks.clear()
        }
    }

    private fun token(): String? = AuthSessionStore.current().accessToken

    private fun showTracks(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            addInfo("У цьому плейлісті немає треків.")
            return
        }
        // Shared number width is based on the largest index, not a fixed 37dp
        // even for single-digit playlists. Leave a small gap before the cover.
        val numberWidth = dp(19 + tracks.size.toString().length * 8)
        tracks.take(visibleTrackLimit).forEachIndexed { i, track ->
            val id = track.selectedVideoId?.takeIf { VIDEO_ID.matches(it) }
            if (!landscape) {
                addPortraitTrack(track, i, id)
                return@forEachIndexed
            }
            // Accepted landscape row layout below remains unchanged.
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(7), dp(if (landscape) 5 else 8),
                    dp(8), dp(if (landscape) 5 else 8))
                background = surface(palette.surface, 10)
            }
            val ordinal = text("${i + 1}.", 13f, true, palette.muted).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
            row.addView(ordinal, LinearLayout.LayoutParams(
                numberWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(4)
            })
            if (id != null) {
                val thumbnail = ImageView(this).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.ic_ytm_playlist_add)
                    setColorFilter(palette.muted)
                    background = surface(palette.surfaceAlt, 8)
                    contentDescription = "Прев’ю: ${track.originalTitle}"
                }
                row.addView(thumbnail, LinearLayout.LayoutParams(
                    dp(if (landscape) 76 else 68), dp(43)
                ).apply { marginEnd = dp(8) })
                PlaylistCoverLoader.bind(this, thumbnail, id)
            }
            val title = track.selectedTitle?.takeIf(String::isNotBlank)
                ?: track.originalTitle
            val channel = track.selectedChannel?.takeIf(String::isNotBlank)
                ?: track.originalArtist
            // Purely visual: the original video title and channel/IDs remain
            // untouched for saved metadata, playback and future export.
            val prefixed = "$channel - "
            val displayTitle = if (channel.isNotBlank() &&
                title.startsWith(prefixed, ignoreCase = true)
            ) title.substring(prefixed.length).ifBlank { title } else title
            val detail = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                contentDescription = "Назва відео: $title. Канал: $channel"
            }
            detail.addView(text(displayTitle, 14f, false, palette.text).apply {
                maxLines = 3
                ellipsize = TextUtils.TruncateAt.END
            })
            if (channel.isNotBlank()) {
                detail.addView(text("Канал: $channel", 12f, false, palette.muted).apply {
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                })
            }
            row.addView(detail, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ))
            if (id != null) {
                val play = button("▶") {
                    startActivity(
                        Intent(this, YouTubePlayerActivity::class.java)
                            .putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, id)
                            .putExtra(YouTubePlayerActivity.EXTRA_TITLE, title)
                    )
                }.apply { contentDescription = "Відтворити $title у застосунку" }
                row.addView(play, LinearLayout.LayoutParams(dp(54), dp(48)).apply {
                    marginStart = dp(5)
                })
            }
            body.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) })
        }
        if (tracks.size > visibleTrackLimit) {
            addFullButton("Показати ще • ${tracks.size - visibleTrackLimit} залишилося") {
                val before = scroller.scrollY
                visibleTrackLimit += 60
                renderContent()
                scroller.post { if (!isDestroyed) scroller.scrollTo(0, before) }
            }
        }
    }

    /** Portrait-only tile; metadata and player video IDs are never changed. */
    private fun addPortraitTrack(track: Track, index: Int, id: String?) {
        val title = track.selectedTitle?.takeIf(String::isNotBlank)
            ?: track.originalTitle
        val channel = track.selectedChannel?.takeIf(String::isNotBlank)
            ?: track.originalArtist
        val prefixed = "$channel - "
        val displayTitle = if (channel.isNotBlank() &&
            title.startsWith(prefixed, ignoreCase = true)
        ) title.substring(prefixed.length).ifBlank { title } else title

        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(7), dp(7), dp(7), dp(9))
            background = surface(palette.surface, 10)
        }
        val artwork = FrameLayout(this).apply {
            minimumHeight = dp(112)
            background = surface(palette.surfaceAlt, 9)
        }
        val cover = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.ic_ytm_music)
            setColorFilter(palette.muted)
            alpha = 0.45f
            contentDescription = "Прев’ю: ${track.originalTitle}"
        }
        artwork.addView(cover, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(112)
        ))
        if (id != null) PlaylistCoverLoader.bind(this, cover, id)

        artwork.addView(text("${index + 1}.", 14f, true, palette.text).apply {
            setPadding(dp(7), dp(3), dp(7), dp(3))
            background = surface(palette.surface, 7)
            contentDescription = "Трек ${index + 1}"
        }, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        ).apply { leftMargin = dp(6); topMargin = dp(6) })

        if (id != null) {
            val play = button("▶") {
                startActivity(
                    Intent(this, YouTubePlayerActivity::class.java)
                        .putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, id)
                        .putExtra(YouTubePlayerActivity.EXTRA_TITLE, title)
                )
            }.apply {
                contentDescription = "Відтворити $title у застосунку"
                textSize = 18f
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0
                setPadding(0, 0, 0, 0)
                setTextColor(android.graphics.Color.WHITE)
                background = GradientDrawable().apply {
                    setColor(0x77000000)
                    cornerRadius = dp(12).toFloat()
                }
            }
            artwork.addView(play, FrameLayout.LayoutParams(
                dp(42), dp(42), Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply { topMargin = dp(6) })
        }
        tile.addView(artwork, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(112)
        ))
        tile.addView(text(displayTitle, 15f, true, palette.text).apply {
            maxLines = 4
            ellipsize = TextUtils.TruncateAt.END
            setPadding(dp(4), dp(7), dp(4), 0)
            contentDescription = "Назва відео: $title. Канал: $channel"
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        if (channel.isNotBlank()) {
            tile.addView(text("Канал: $channel", 12f, false, palette.muted).apply {
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setPadding(dp(4), dp(3), dp(4), 0)
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
        body.addView(tile, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(6) })
    }

    private data class CardInfo(
        val identity: String,
        val title: String,
        val subtitle: String,
        val videoId: String?,
        val thumbnail: String? = null,
        val badge: String = "",
        val localSaved: Boolean = false,
        val linkedYtm: Boolean = false,
        val updatedAt: Long = 0,
        val trackCount: Long = 0,
        val duplicateHint: PlaylistLibraryDuplicatePolicy.Hint? = null,
        val onClick: () -> Unit
    )

    private fun addCards(items: List<CardInfo>) {
        val useTwo = columns == 2 && resources.configuration.screenWidthDp >= 340
        val rowCount = if (useTwo) 2 else 1
        items.chunked(rowCount).forEach { group ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            group.forEachIndexed { index, item ->
                val card = makeCard(item, useTwo)
                // MATCH_PARENT in a wrap-content row = every tile in the row
                // gets the height of the tallest one (BACKLOG «Uneven grid tiles»).
                row.addView(card, LinearLayout.LayoutParams(
                    0, if (useTwo) ViewGroup.LayoutParams.MATCH_PARENT
                    else ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ).apply { if (index > 0) marginStart = dp(8) })
            }
            if (useTwo && group.size == 1) {
                row.addView(View(this), LinearLayout.LayoutParams(
                    0, dp(1), 1f
                ).apply { marginStart = dp(8) })
            }
            (cardTarget ?: body).addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(9) })
        }
    }

    private fun makeCard(info: CardInfo, tile: Boolean): View {
        val card = LinearLayout(this).apply {
            orientation = if (tile) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            gravity = if (tile) Gravity.TOP else Gravity.CENTER_VERTICAL
            background = surface(palette.surface, 14)
            setPadding(dp(9), dp(9), dp(9), dp(9))
            isClickable = true
            isFocusable = true
            setOnClickListener { info.onClick() }
        }
        val cover = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.ic_ytm_music)
            setColorFilter(palette.muted)
            background = surface(palette.surfaceAlt, 10)
            contentDescription = "Обкладинка плейліста"
        }
        card.addView(cover, LinearLayout.LayoutParams(
            if (tile) ViewGroup.LayoutParams.MATCH_PARENT else dp(78),
            if (tile) dp(106) else dp(78)
        ).apply { if (!tile) marginEnd = dp(10) })
        PlaylistCoverLoader.bind(this, cover, info.videoId, info.thumbnail)
        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, if (tile) dp(7) else 0, 0, 0)
        }
        labels.addView(text(info.title, 14f, true, palette.text).apply {
            minLines = if (tile) 2 else 1
            maxLines = if (tile) 2 else 3
            ellipsize = TextUtils.TruncateAt.END
        })
        labels.addView(text(info.subtitle, 12f, false, palette.muted).apply {
            minLines = if (tile) 2 else 1
            maxLines = if (tile) 2 else 3
            ellipsize = TextUtils.TruncateAt.END
        })
        info.duplicateHint?.let { hint ->
            // Owner 2026-10-16: plain verdict instead of «Схожі №8 (4) • збіг
            // складу»; a tap shows the whole group side by side.
            val colour = when (hint.evidence) {
                PlaylistLibraryDuplicatePolicy.Evidence.SAME_TRACKS -> palette.accent
                PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE_COUNT -> palette.semantic.warning
                PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE -> palette.muted
            }
            // Owner 2026-10-17 00:25: the long verdict was cut on 2-line tiles
            // and hid «група №N ›». Short verdict; on a tile it has its own line.
            labels.addView(text(
                PlaylistLibraryDuplicateGroupPolicy.cardLine(hint, tile),
                11.5f, true, colour).apply {
                minLines = if (tile) 2 else 1
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setPadding(0, dp(4), 0, dp(4))
                isClickable = true
                isFocusable = true
                contentDescription = "Схожі плейлісти, група ${hint.groupNumber}. Порівняти"
                setOnClickListener { showDuplicateGroup(info.identity) }
            })
        }
        // Reserve the same footer slot in every grid tile, even when there is
        // no linked YTM destination. Do not imply media was downloaded.
        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(24)
        }
        if (info.localSaved) {
            footer.addView(ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setImageResource(R.drawable.ic_ytm_device)
                setColorFilter(palette.semantic.duplicate)
                contentDescription = "Дані плейліста збережені на телефоні"
            }, LinearLayout.LayoutParams(dp(17), dp(19)))
            footer.addView(text("✓", 12f, true, palette.semantic.duplicate).apply {
                setPadding(dp(3), 0, 0, 0)
            })
        }
        if (info.linkedYtm || info.badge == "YouTube") {
            footer.addView(ImageView(this).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(R.drawable.ic_ytm_youtube_play)
                setColorFilter(palette.accent)
                contentDescription = if (info.linkedYtm)
                    "Плейліст пов’язаний з YouTube Music"
                else "Плейліст з каталогу YouTube"
            }, LinearLayout.LayoutParams(dp(18), dp(19)).apply {
                marginStart = dp(if (info.localSaved) 9 else 0)
            })
            footer.addView(text(if (info.linkedYtm) "✓" else "YouTube",
                12f, true, palette.accent).apply {
                setPadding(dp(3), 0, 0, 0)
            })
        } else if (info.badge.isNotBlank()) {
            footer.addView(text(info.badge, 11f, true, palette.accent))
        }
        if (tile) {
            // Pushes the footer down so footers line up across a row.
            labels.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        }
        labels.addView(footer, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(24)
        ))
        card.addView(labels, if (tile) LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ) else LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return card
    }

    private fun addInfo(value: String) {
        body.addView(text(value, 14f, false, palette.muted).apply {
            setPadding(dp(7), dp(10), dp(7), dp(12))
        })
    }

    private fun addHeading(value: String) {
        body.addView(text(value, 19f, true, palette.text).apply {
            setPadding(dp(5), dp(12), dp(5), dp(8))
        })
    }

    private fun addFullButton(label: String, onClick: () -> Unit) {
        val b = button(label, onClick)
        body.addView(b, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        })
        b.minHeight = dp(54)
    }

    /** Icon-only actions are ImageButtons, never text/emoji glyphs that wrap. */
    private fun iconButton(
        icon: Int, description: String, onClick: () -> Unit
    ): ImageButton = ImageButton(this).apply {
        setImageResource(icon)
        setColorFilter(palette.text)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setPadding(dp(10), dp(10), dp(10), dp(10))
        contentDescription = description
        minimumHeight = dp(48)
        minimumWidth = dp(46)
        background = surface(palette.surfaceAlt, 12)
        isFocusable = true
        setOnClickListener { onClick() }
    }

    private fun button(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            maxLines = 2
            minHeight = dp(48)
            setTextColor(palette.text)
            background = surface(palette.surfaceAlt, 12)
            setOnClickListener { onClick() }
        }

    private fun text(
        value: String, size: Float, bold: Boolean, color: Int
    ): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun surface(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        setStroke(dp(1), palette.border)
    }

    private fun openExternal(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure { toast("Немає застосунку для відкриття YouTube.") }
    }

    private fun toast(value: String) =
        Toast.makeText(this, value, Toast.LENGTH_LONG).show()

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        const val EXTRA_CURRENT_CHANGED = "playlist_library_current_changed"
        private const val STATE_MODAL = "playlist_library_modal"
        private const val MODAL_SWITCH = "switch_local_current"
        private const val MODAL_DELETE_LOCAL = "delete_saved_local_copy"
        private const val MODAL_DELETE_HISTORY = "delete_history_entry"
        private const val MODAL_DELETE_REMOTE = "delete_remote_playlist"
        private const val MODAL_SAVE_REMOTE = "save_remote_local"
        private const val MODAL_HELP = "playlist_library_help"
        private const val MODAL_FILTER = "playlist_library_filter"
        private const val MODAL_DUP_GROUP = "playlist_library_duplicate_group"
        private const val MODAL_DUP_DIFF = "playlist_library_duplicate_difference"
        private val VIDEO_ID = Regex("^[A-Za-z0-9_-]{11}$")

        // Temporary, account-scoped metadata cache survives Android rotation,
        // never serializes an OAuth token or a private remote playlist to disk.
        /** Called on main thread after a confirmed remote delete in any screen. */
        fun invalidateDeletedRemotePlaylist(id: String) {
            cachedPlaylists = null
            cachedTracks.remove(id)
        }

        private var cachedOwner: String? = null
        private var cachedPlaylists: List<YouTubePlaylistInfo>? = null
        private val cachedTracks = mutableMapOf<String, List<Track>>()
    }
}
