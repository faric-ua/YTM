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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.history.HistoryRecoveryPolicy
import com.saney.ytmimporter.library.LocalLibraryItem
import com.saney.ytmimporter.library.LocalLibrarySource
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
 * never starts Bulk Sync or any create/insert/delete YTM operation.
 */
class PlaylistLibraryActivity : Activity() {
    private val currentStore by lazy { CurrentPlaylistStore(this) }
    private val savedStore by lazy { RestorablePlaylistStore(this) }
    private val historyStore by lazy { HistoryStore(this) }
    private val quotaTracker by lazy { QuotaTracker(this) }
    private val api by lazy { YouTubeApi() }
    private val worker = Executors.newSingleThreadExecutor()

    private var onlineTab = false
    private var columns = 2
    private var filterText = ""
    private var selectedLocal: String? = null
    private var selectedRemote: String? = null
    private var visibleTrackLimit = 60
    private var loadingList = false
    private var loadingTracks = false
    private var loadError: String? = null

    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var scroller: ScrollView
    private lateinit var tabsRow: LinearLayout
    private lateinit var filtersRow: LinearLayout
    private lateinit var compactLocalTabButton: Button
    private lateinit var compactOnlineTabButton: Button
    private lateinit var localTabButton: Button
    private lateinit var onlineTabButton: Button
    private lateinit var layoutButton: Button
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
        columns = savedInstanceState?.getInt("columns") ?: 2
        filterText = savedInstanceState?.getString("filter").orEmpty()
        selectedLocal = savedInstanceState?.getString("local")
        selectedRemote = savedInstanceState?.getString("remote")
        visibleTrackLimit = savedInstanceState?.getInt("limit") ?: 60
        ensureRemoteAccountIsolation()
        modalController = RestorableModalController(this, STATE_MODAL)
        modalController.restore(savedInstanceState)
        buildUi()
        renderContent()
        modalController.restoreAfterContentReady(::renderConfirmation)
        if (onlineTab && cachedPlaylists == null && token() != null) {
            loadRemoteList()
        } else if (onlineTab && selectedRemote != null &&
            !cachedTracks.containsKey(selectedRemote)
        ) {
            loadRemoteTracks(selectedRemote!!)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::body.isInitialized && !onlineTab) renderContent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("online", onlineTab)
        outState.putInt("columns", columns)
        outState.putString("filter", filterText)
        outState.putString("local", selectedLocal)
        outState.putString("remote", selectedRemote)
        outState.putInt("limit", visibleTrackLimit)
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

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(palette.background)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(if (landscape) 8 else 12), dp(if (landscape) 3 else 8),
                dp(if (landscape) 8 else 12), dp(if (landscape) 3 else 6))
        }
        header.addView(button("‹") { onBackPressed() },
            LinearLayout.LayoutParams(dp(if (landscape) 46 else 54), dp(48)))
        header.addView(text("Бібліотека плейлістів",
            if (landscape) 18f else 21f, true, palette.accent).apply {
            setPadding(dp(9), 0, 0, 0)
            maxLines = if (landscape) 1 else 2
            ellipsize = TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        // In landscape, keep the two library sources in the existing header.
        // This gives the track list the vertical space used by large tabs.
        compactLocalTabButton = button("📱") { selectTab(false) }.apply {
            contentDescription = "На телефоні"
            textSize = 18f
        }
        compactOnlineTabButton = button("YT") { selectTab(true) }.apply {
            contentDescription = "YouTube"
            textSize = 14f
        }
        if (landscape) {
            header.addView(compactLocalTabButton,
                LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                    marginStart = dp(7)
                })
            header.addView(compactOnlineTabButton,
                LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                    marginStart = dp(7)
                })
        }
        root.addView(header)

        tabsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(4), dp(12), dp(4))
            visibility = if (landscape) View.GONE else View.VISIBLE
        }
        localTabButton = button("На телефоні") { selectTab(false) }
        onlineTabButton = button("YouTube") { selectTab(true) }
        tabsRow.addView(localTabButton, LinearLayout.LayoutParams(0, dp(54), 1f))
        tabsRow.addView(onlineTabButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            marginStart = dp(8)
        })
        root.addView(tabsRow)

        filtersRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(5), dp(12), dp(5))
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
        filtersRow.addView(searchBox, LinearLayout.LayoutParams(0, dp(51), 1f))
        layoutButton = button("▦") {
            columns = if (columns == 2) 1 else 2
            updateTopControls()
            renderContent()
        }
        filtersRow.addView(layoutButton, LinearLayout.LayoutParams(dp(58), dp(51)).apply {
            marginStart = dp(8)
        })
        root.addView(filtersRow)

        statusLabel = text("", 13f, false, palette.muted).apply {
            setPadding(dp(if (landscape) 12 else 17), dp(5),
                dp(if (landscape) 12 else 17), dp(if (landscape) 4 else 8))
            if (landscape) {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        }
        root.addView(statusLabel)
        scroller = ScrollView(this).apply {
            isFillViewport = true
        }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(16))
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
        if (onlineTab == online) {
            selectedLocal = null
            selectedRemote = null
        } else {
            onlineTab = online
            selectedLocal = null
            selectedRemote = null
        }
        visibleTrackLimit = 60
        ensureRemoteAccountIsolation()
        updateTopControls()
        renderContent()
        if (online && cachedPlaylists == null && token() != null) loadRemoteList()
    }

    private fun updateTopControls() {
        localTabButton.setTextColor(if (!onlineTab) palette.accent else palette.text)
        onlineTabButton.setTextColor(if (onlineTab) palette.accent else palette.text)
        compactLocalTabButton.setTextColor(if (!onlineTab) palette.accent else palette.text)
        compactOnlineTabButton.setTextColor(if (onlineTab) palette.accent else palette.text)
        filtersRow.visibility =
            if (selectedLocal == null && selectedRemote == null) View.VISIBLE else View.GONE
        layoutButton.text = if (columns == 2) "☰" else "▦"
        layoutButton.contentDescription =
            if (columns == 2) "Список одним стовпчиком" else "Плитки у два стовпчики"
        searchBox.visibility = View.VISIBLE
        layoutButton.visibility = View.VISIBLE
    }

    private fun renderContent() {
        if (!::body.isInitialized) return
        updateTopControls()
        val oldY = scroller.scrollY
        body.removeAllViews()
        when {
            selectedLocal != null -> renderLocalDetails(selectedLocal!!)
            selectedRemote != null -> renderRemoteDetails(selectedRemote!!)
            onlineTab -> renderOnlineCatalogue()
            else -> renderLocalCatalogue()
        }
        scroller.post { if (!isDestroyed) scroller.scrollTo(0, if (selectedLocal == null &&
            selectedRemote == null) 0 else oldY) }
    }

    private fun localItems(): List<LocalLibraryItem> =
        PlaylistLibraryPolicy.build(
            savedStore.getAll(), currentStore.load(), historyStore.getAll()
        )

    private fun renderLocalCatalogue() {
        val all = localItems()
        statusLabel.text = "На телефоні: ${all.size} • Збережені та архівні плейлісти"
        addFullButton("Поточний плейліст →") {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }
        if (all.isEmpty()) {
            addInfo("Локальних плейлістів ще немає. Імпортуйте файл або збережіть плейліст.")
            return
        }
        val filtered = all.filter {
            it.title.contains(filterText, true) ||
                it.sourceLabel.contains(filterText, true)
        }
        addCards(filtered.map { item ->
            CardInfo(
                title = item.title,
                subtitle = "${item.trackCount} треків • " +
                    when (item.source) {
                        LocalLibrarySource.CURRENT -> "Поточний"
                        LocalLibrarySource.SAVED -> "На телефоні"
                        LocalLibrarySource.HISTORY -> "Архів History"
                    },
                videoId = item.sampleVideoId,
                badge = if (item.destinationPlaylistId != null) "YTM ✓" else "",
                onClick = {
                    selectedLocal = item.identity
                    visibleTrackLimit = 60
                    renderContent()
                    scroller.scrollTo(0, 0)
                }
            )
        })
        if (filtered.isEmpty()) addInfo("Нічого не знайдено за запитом.")
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
            addInfo("Склад плейліста недоступний. Можливо, його дані вже видалено.")
            return
        }
        statusLabel.text = "На телефоні • ${item.trackCount} треків"
        addFullButton("← До всіх плейлістів") {
            selectedLocal = null
            renderContent()
        }
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
        } else {
            addFullButton("Відкрити поточний плейліст →") {
                startActivity(Intent(this, PlaylistActivity::class.java))
            }
        }
        item.destinationPlaylistId?.let { remoteId ->
            addFullButton("Переглянути плейліст на YouTube Music ↗") {
                openExternal("https://music.youtube.com/playlist?list=" +
                    Uri.encode(remoteId))
            }
        }
        addHeading("Треки • ${playlist.tracks.size}")
        showTracks(playlist.tracks)
    }

    private fun renderOnlineCatalogue() {
        val token = token()
        statusLabel.text = "YouTube • тільки читання, без змін плейлістів"
        if (token == null) {
            addInfo("Google/YTM зараз не підключено. Поверніться на головний " +
                "екран і натисніть «2. Google / YTM», потім відкрийте бібліотеку.")
            return
        }
        addFullButton(if (loadingList) "Завантажую…" else "↻ Оновити список YouTube") {
            if (!loadingList) loadRemoteList()
        }
        if (loadingList) addInfo("Читаю назви плейлістів з YouTube…")
        loadError?.let { addInfo("Не вдалося оновити: $it") }
        val lists = cachedPlaylists
        if (lists == null) {
            if (!loadingList) addInfo("Натисніть «Оновити» для отримання списку.")
            return
        }
        val filtered = lists.filter { it.title.contains(filterText, true) }
        addInfo("Доступно в акаунті: ${lists.size} плейлістів. Треки читаються лише при відкритті.")
        addCards(filtered.map { item ->
            CardInfo(
                title = item.title,
                subtitle = "${item.itemCount} відео • ${item.privacyStatus}",
                videoId = null,
                thumbnail = item.thumbnailUrl,
                badge = "YouTube",
                onClick = {
                    selectedRemote = item.id
                    visibleTrackLimit = 60
                    renderContent()
                    scroller.scrollTo(0, 0)
                    if (!cachedTracks.containsKey(item.id)) loadRemoteTracks(item.id)
                }
            )
        })
        if (lists.isEmpty()) addInfo("У цьому YouTube-акаунті немає доступних власних плейлістів.")
    }

    private fun renderRemoteDetails(playlistId: String) {
        val item = cachedPlaylists?.firstOrNull { it.id == playlistId }
        if (item == null) {
            selectedRemote = null
            renderOnlineCatalogue()
            return
        }
        statusLabel.text = "YouTube • ${item.itemCount} відео • тільки читання"
        addFullButton("← До плейлістів YouTube") {
            selectedRemote = null
            renderContent()
        }
        addHeading(item.title)
        addInfo("На YouTube • ${item.privacyStatus}. Треки не завантажуються на телефон.")
        addFullButton("Відкрити в YouTube Music ↗") {
            openExternal("https://music.youtube.com/playlist?list=" +
                Uri.encode(playlistId))
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
        tracks.take(visibleTrackLimit).forEachIndexed { i, track ->
            val id = track.selectedVideoId?.takeIf { VIDEO_ID.matches(it) }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(if (landscape) 5 else 8),
                    dp(8), dp(if (landscape) 5 else 8))
                background = surface(palette.surface, 10)
            }
            row.addView(text("${i + 1}.", 13f, true, palette.muted),
                LinearLayout.LayoutParams(dp(37), ViewGroup.LayoutParams.WRAP_CONTENT))
            if (id != null) {
                val thumbnail = ImageView(this).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.ic_ytm_playlist_add)
                    setColorFilter(palette.muted)
                    background = surface(palette.surfaceAlt, 8)
                    contentDescription = "Прев’ю: ${track.originalTitle}"
                }
                row.addView(thumbnail, LinearLayout.LayoutParams(
                    dp(if (landscape) 76 else 68), dp(if (landscape) 43 else 43)
                ).apply { marginEnd = dp(9) })
                PlaylistCoverLoader.bind(this, thumbnail, id)
            }
            val title = track.selectedTitle?.takeIf(String::isNotBlank)
                ?: track.originalTitle
            val detail = text(
                title + "\n" + (track.selectedChannel?.takeIf(String::isNotBlank)
                    ?: track.originalArtist), 14f, false, palette.text
            ).apply { maxLines = 3 }
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
                row.addView(play, LinearLayout.LayoutParams(dp(54), dp(48)))
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

    private data class CardInfo(
        val title: String,
        val subtitle: String,
        val videoId: String?,
        val thumbnail: String? = null,
        val badge: String = "",
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
            body.addView(row, LinearLayout.LayoutParams(
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
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.ic_ytm_playlist_add)
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
            maxLines = 2
        })
        labels.addView(text(info.subtitle, 12f, false, palette.muted).apply {
            maxLines = 2
        })
        if (info.badge.isNotBlank()) {
            labels.addView(text(info.badge, 11f, true, palette.accent))
        }
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
        private const val MODAL_SAVE_REMOTE = "save_remote_local"
        private val VIDEO_ID = Regex("^[A-Za-z0-9_-]{11}$")

        // Temporary, account-scoped metadata cache survives Android rotation,
        // never serializes an OAuth token or a private remote playlist to disk.
        private var cachedOwner: String? = null
        private var cachedPlaylists: List<YouTubePlaylistInfo>? = null
        private val cachedTracks = mutableMapOf<String, List<Track>>()
    }
}
