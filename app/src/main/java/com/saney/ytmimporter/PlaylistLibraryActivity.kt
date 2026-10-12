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
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.destination.DestinationRemoteOperations
import com.saney.ytmimporter.history.HistoryRecoveryPolicy
import com.saney.ytmimporter.library.LocalLibraryItem
import com.saney.ytmimporter.library.LocalLibrarySource
import com.saney.ytmimporter.library.PlaylistLibraryIdentityPolicy
import com.saney.ytmimporter.library.PlaylistLibraryPolicy
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.QuotaTracker
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
    private enum class SortMode { RECENT, NAME_ASC, NAME_DESC, TRACKS_DESC }
    private var showAll = true
    private var sortMode = SortMode.RECENT
    private var cardTarget: LinearLayout? = null
    private val currentStore by lazy { CurrentPlaylistStore(this) }
    private val savedStore by lazy { RestorablePlaylistStore(this) }
    private val historyStore by lazy { HistoryStore(this) }
    private val quotaTracker by lazy { QuotaTracker(this) }
    private val api by lazy { YouTubeApi() }
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
    private var deletingRemoteId: String? = null
    private val remoteDeleteListener: (DestinationRemoteOperations.State) -> Unit =
        { state -> onRemoteDeleteState(state) }

    private lateinit var detailActions: LinearLayout
    private lateinit var detailCurrentButton: ImageButton
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var scroller: ScrollView
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
        columns = savedInstanceState?.getInt("columns") ?: 2
        filterText = savedInstanceState?.getString("filter").orEmpty()
        landscapeSearchOpen = savedInstanceState?.getBoolean("landscape_search")
            ?: filterText.isNotBlank()
        selectedLocal = savedInstanceState?.getString("local")
        selectedRemote = savedInstanceState?.getString("remote")
        visibleTrackLimit = savedInstanceState?.getInt("limit") ?: 60
        deletingRemoteId = savedInstanceState?.getString("deleting_remote")
        ensureRemoteAccountIsolation()
        modalController = RestorableModalController(this, STATE_MODAL)
        modalController.restore(savedInstanceState)
        buildUi()
        renderContent()
        modalController.restoreAfterContentReady(::renderConfirmation)
        if (selectedRemote != null && !cachedTracks.containsKey(selectedRemote)) {
            loadRemoteTracks(selectedRemote!!)
        } else if (showsRemote() && cachedPlaylists == null && token() != null) {
            loadRemoteList()
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
            if (showsRemote() && cachedPlaylists == null && token() != null) {
                if (!loadingList) loadRemoteList()
            } else if (showsLocal()) renderContent()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("online", onlineTab)
        outState.putBoolean("show_all", showAll)
        outState.putString("sort_mode", sortMode.name)
        outState.putInt("columns", columns)
        outState.putString("filter", filterText)
        outState.putBoolean("landscape_search", landscapeSearchOpen)
        outState.putString("local", selectedLocal)
        outState.putString("remote", selectedRemote)
        outState.putInt("limit", visibleTrackLimit)
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
            renderContent()
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
        selectedLocal = null
        selectedRemote = null
        visibleTrackLimit = 60
        ensureRemoteAccountIsolation()
        updateTopControls()
        renderContent()
        scroller.scrollTo(0, 0)
        if (showsRemote() && cachedPlaylists == null && token() != null) {
            loadRemoteList()
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
    }

    private fun renderContent() {
        if (!::body.isInitialized) return
        updateTopControls()
        val oldY = scroller.scrollY
        body.removeAllViews()
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

    private fun renderCatalogue() {
        val allLocal = localItems()
        val localVisible = if (showsLocal()) allLocal else emptyList()
        val remoteEnabled = showsRemote()
        val access = token()
        val remoteItems = if (remoteEnabled && access != null)
            cachedPlaylists.orEmpty() else emptyList()
        // Cross-reference only IDs in the current account's already fetched
        // catalogue. Retain BOTH cards and NEVER infer identical track lists.
        val exactLinks = PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            allLocal, remoteItems)

        // A single large frame owns the count toolbar and the artwork grid.
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(10))
            background = surface(palette.surface, 14)
        }
        // Keep the two source counters visually centered in the WHOLE panel.
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
            (if (access == null) "—" else cachedPlaylists?.size?.toString() ?: "—") +
                if (!showAll && onlineTab) " ✓" else "",
            palette.accent)
        summary.addView(counts, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        val sortButton = iconButton(R.drawable.ic_ytm_sort,
            "Фільтрація і сортування плейлістів") {}
        sortButton.setOnClickListener { showSortMenu(sortButton) }
        summary.addView(sortButton, FrameLayout.LayoutParams(
            dp(44), dp(46), Gravity.END or Gravity.CENTER_VERTICAL
        ))
        panel.addView(summary)
        panel.addView(text(
            when {
                showAll -> "Всі"
                onlineTab -> "YouTube"
                else -> "На телефоні"
            } + " • " + when (sortMode) {
                SortMode.RECENT -> "Нові (локально)"
                SortMode.NAME_ASC -> "За абеткою А–Я"
                SortMode.NAME_DESC -> "За абеткою Я–А"
                SortMode.TRACKS_DESC -> "За кількістю треків"
            }, 12f, false, palette.muted
        ).apply { setPadding(dp(6), dp(1), dp(6), dp(8)) })

        val cards = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        panel.addView(cards)
        body.addView(panel)
        cardTarget = cards

        val all = mutableListOf<CardInfo>()
        localVisible.forEach { item ->
            all += CardInfo(
                title = item.title,
                subtitle = (if (item.destinationPlaylistId?.let {
                    it in exactLinks
                } == true) "Збіг ID • " else "") +
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
                onClick = {
                    selectedLocal = item.identity
                    visibleTrackLimit = 60
                    renderContent()
                    scroller.scrollTo(0, 0)
                }
            )
        }
        remoteItems.forEach { item ->
            all += CardInfo(
                title = item.title,
                subtitle = (if (item.id in exactLinks) "Збіг ID • " else "") +
                    "${item.itemCount} відео • ${item.privacyStatus}",
                videoId = null,
                thumbnail = item.thumbnailUrl,
                badge = "YouTube",
                trackCount = item.itemCount,
                onClick = {
                    selectedRemote = item.id
                    visibleTrackLimit = 60
                    renderContent()
                    scroller.scrollTo(0, 0)
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
        }
        addCards(ordered)
        if (filtered.isEmpty() && filterText.isNotBlank()) {
            panelInfo(cards, "Нічого не знайдено за запитом.")
        } else if (filtered.isEmpty() && !remoteEnabled) {
            panelInfo(cards, "Локальних плейлістів ще немає.")
        }
        if (remoteEnabled && access == null) {
            panelInfo(cards, "YouTube не підключено • підключіть Google/YTM на головному екрані.")
        } else if (remoteEnabled && cachedPlaylists == null) {
            panelInfo(cards, if (loadingList) "Читаю список YouTube…"
                else "Список YouTube ще не завантажено.")
        }
        if (remoteEnabled && loadError != null) {
            panelInfo(cards, "Не вдалося оновити YouTube: $loadError")
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

    private fun panelInfo(target: LinearLayout, value: String) {
        target.addView(text(value, 12f, false, palette.muted).apply {
            setPadding(dp(6), dp(5), dp(6), dp(6))
        })
    }

    private fun showSortMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val entries = listOf(
            1 to "Всі плейлісти",
            2 to "Тільки на телефоні",
            3 to "Тільки YouTube",
            10 to "За датою локального збереження",
            11 to "За абеткою • А–Я",
            12 to "За абеткою • Я–А",
            13 to "За кількістю треків"
        )
        entries.forEachIndexed { index, entry ->
            val group = if (index < 3) 1 else 2
            popup.menu.add(group, entry.first, index, entry.second).apply {
                isCheckable = true
                isChecked = when (entry.first) {
                    1 -> showAll
                    2 -> !showAll && !onlineTab
                    3 -> !showAll && onlineTab
                    10 -> sortMode == SortMode.RECENT
                    11 -> sortMode == SortMode.NAME_ASC
                    12 -> sortMode == SortMode.NAME_DESC
                    else -> sortMode == SortMode.TRACKS_DESC
                }
            }
        }
        popup.menu.setGroupCheckable(1, true, true)
        popup.menu.setGroupCheckable(2, true, true)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    showAll = true
                    selectedLocal = null; selectedRemote = null
                }
                2 -> {
                    showAll = false; onlineTab = false
                    selectedLocal = null; selectedRemote = null
                }
                3 -> {
                    showAll = false; onlineTab = true
                    selectedLocal = null; selectedRemote = null
                }
                10 -> sortMode = SortMode.RECENT
                11 -> sortMode = SortMode.NAME_ASC
                12 -> sortMode = SortMode.NAME_DESC
                13 -> sortMode = SortMode.TRACKS_DESC
            }
            ensureRemoteAccountIsolation()
            renderContent()
            scroller.scrollTo(0, 0)
            if (showsRemote() && cachedPlaylists == null && token() != null) {
                loadRemoteList()
            }
            true
        }
        popup.show()
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
        statusLabel.text = "YouTube • ${item.itemCount} відео • тільки читання"
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

    private fun showConfirmation(type: String, id: String) {
        modalController.show(
            modalId = type,
            args = Bundle().apply { putString("item", id) },
            renderer = ::renderConfirmation
        )
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
                    "Поточний плейлист, збережені локальні копії та YouTube не зміняться.",
                confirmLabel = "Видалити з History",
                onCancel = { modalController.clearState() }
            ) {
                modalController.clearState()
                if (historyStore.get(entryId) != null) {
                    historyStore.remove(entryId)
                    selectedLocal = null
                    renderContent()
                    toast("Запис History видалено")
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
                    "Локальні копії та History залишаться. Цю дію неможливо скасувати.",
                confirmLabel = "Видалити з YouTube",
                onCancel = { modalController.clearState() }
            ) {
                modalController.clearState()
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
                    "зміняться. Архів History може залишитися видимим окремо.",
                confirmLabel = "Видалити локально",
                onCancel = { modalController.clearState() }
            ) {
                modalController.clearState()
                deleteLocalCopy(localId)
            }
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
            else renderContent()
        } else {
            toast(state.errorMessage ?: "Не вдалося видалити плейлист із YouTube")
            renderContent()
        }
    }

    private fun deleteLocalCopy(localId: String) {
        // Check the exact opaque ID again after rotation/confirmation.
        val target = localItems().firstOrNull {
            it.localPlaylistId == localId &&
                it.source != LocalLibrarySource.HISTORY
        }
        if (target == null) {
            toast("Локальна копія вже недоступна")
            selectedLocal = null
            renderContent()
            return
        }
        val clearedCurrent = currentStore.clearIfMatches(localId)
        val removedSaved = savedStore.remove(localId)
        if (!clearedCurrent && !removedSaved) {
            toast("Локальну копію вже видалено")
            return
        }
        if (clearedCurrent) {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_CURRENT_CHANGED, true))
        }
        selectedLocal = null
        visibleTrackLimit = 60
        renderContent()
        scroller.scrollTo(0, 0)
        toast("Локальну копію видалено")
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
            "«Всі / тільки телефон / тільки YouTube», за локальною датою, " +
                "за абеткою А–Я / Я–А або за кількістю треків. " +
                "Для YouTube дата може бути невідомою.")
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
        section("Обкладинки та дублікати")
        entry(R.drawable.ic_ytm_music, "Немає обкладинки",
            "Мініатюра береться з доступних YouTube-даних за videoId. " +
                "Коли дійсного ID немає, показується нейтральна нота. " +
                "Для архівів може знадобитися відновлення метаданих.")
        entry(R.drawable.ic_ytm_view_grid, "Схожі або повторні записи",
            "Один плейліст може бути поточним, збереженим, в History і на YouTube. " +
                "Самі назви не підтверджують, що це точні дублікати: " +
                "поки записи не об’єднуються автоматично.")
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
                result.onSuccess { cachedTracks[id] = it.tracks }
                    .onFailure { loadError = it.message ?: "Помилка YouTube API" }
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
        val title: String,
        val subtitle: String,
        val videoId: String?,
        val thumbnail: String? = null,
        val badge: String = "",
        val localSaved: Boolean = false,
        val linkedYtm: Boolean = false,
        val updatedAt: Long = 0,
        val trackCount: Long = 0,
        val onClick: () -> Unit
    )

    private fun addCards(items: List<CardInfo>) {
        val useTwo = columns == 2 && resources.configuration.screenWidthDp >= 340
        val rowCount = if (useTwo) 2 else 1
        items.chunked(rowCount).forEach { group ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            group.forEachIndexed { index, item ->
                val card = makeCard(item, useTwo)
                row.addView(card, LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
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
        labels.addView(footer, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(24)
        ))
        card.addView(labels, if (tile) LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
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
