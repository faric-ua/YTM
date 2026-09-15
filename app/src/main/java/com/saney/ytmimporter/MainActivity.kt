package com.saney.ytmimporter

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.saney.ytmimporter.model.GoogleAccountInfo
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingTrack
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.model.YouTubeChannelInfo
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.parser.PlaylistParser
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.TrackAdapter
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val fileRequestCode = 1001
    private val authRequestCode = 9001
    private val executor = Executors.newSingleThreadExecutor()
    private val api = YouTubeApi()

    private lateinit var searchCache: SearchCache
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var pendingJobStore: PendingJobStore

    private var playlist: ImportedPlaylist? = null
    private var accessToken: String? = null
    private var googleAccountInfo: GoogleAccountInfo? = null
    private var youtubeChannelInfo: YouTubeChannelInfo? = null
    private var createdPlaylistId: String? = null
    private var pendingAfterAuth: (() -> Unit)? = null

    private lateinit var statusText: TextView
    private lateinit var summaryText: TextView
    private lateinit var accountButton: Button
    private lateinit var quotaButton: Button
    private lateinit var pendingButton: Button
    private lateinit var progress: ProgressBar
    private lateinit var resultPanel: LinearLayout
    private lateinit var resultTitleText: TextView
    private lateinit var resultDetailsText: TextView
    private lateinit var resultLinkText: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: TrackAdapter
    private val visibleTracks = mutableListOf<Track>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchCache = SearchCache(this)
        quotaTracker = QuotaTracker(this)
        pendingJobStore = PendingJobStore(this)
        buildUi()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 16, 19))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(6))
        }
        header.addView(TextView(this).apply {
            text = "YTM Importer"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        })
        header.addView(TextView(this).apply {
            text = "CSV/TXT/текст → пошук → перевірка → плейлист у YouTube Music"
            textSize = 12f
            setTextColor(Color.rgb(165, 167, 173))
        })
        root.addView(header)

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), 0, dp(12), dp(8))
        }
        actions.addView(button("1. Файл") { chooseFile() })
        actions.addView(button("1б. Текст") { showPasteTrackListDialog() })
        accountButton = button("2. Акаунт") { showAccountDialog() }
        actions.addView(accountButton)
        actions.addView(button("3. Знайти") { searchAll() })
        actions.addView(button("4. Створити") { createPlaylist() })
        actions.addView(button("Відкрити в ютм") { openInYtm() })
        actions.addView(button("Заміни") { showReplacementLog() })
        quotaButton = button("Квота") { showQuotaDialog() }
        actions.addView(quotaButton)
        pendingButton = button("Черга") { showPendingJobs() }
        actions.addView(pendingButton)
        scroll.addView(actions)
        root.addView(scroll)

        summaryText = TextView(this).apply {
            setPadding(dp(16), dp(4), dp(16), 0)
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            text = "Плейлист ще не імпортовано"
        }
        root.addView(summaryText)

        statusText = TextView(this).apply {
            setPadding(dp(16), dp(2), dp(16), dp(5))
            setTextColor(Color.rgb(165, 167, 173))
            textSize = 13f
            text = "Виберіть CSV/TXT або вставте список Artist - Track."
        }
        root.addView(statusText)

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }
        root.addView(
            progress,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4))
        )

        resultPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(dp(18), dp(14), dp(18), dp(14))
            setBackgroundColor(Color.rgb(27, 29, 34))
        }

        resultTitleText = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        resultPanel.addView(resultTitleText)

        resultDetailsText = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(190, 192, 198))
            setPadding(0, dp(5), 0, dp(6))
        }
        resultPanel.addView(resultDetailsText)

        resultLinkText = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.rgb(140, 185, 255))
            setTextIsSelectable(true)
            setPadding(0, 0, 0, dp(10))
        }
        resultPanel.addView(resultLinkText)

        val resultActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        resultActions.addView(button("Відкрити в YTM") { openInYtm() })
        resultActions.addView(button("Копіювати посилання") { copyPlaylistLink() })
        resultPanel.addView(resultActions)

        root.addView(
            resultPanel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(12), dp(10), dp(12), dp(10))
            }
        )

        listView = ListView(this).apply {
            divider = null
            dividerHeight = dp(1)
            setBackgroundColor(Color.rgb(15, 16, 19))
        }
        adapter = TrackAdapter(this, visibleTracks)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            showTrackDialog(visibleTracks[position])
        }
        root.addView(
            listView,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        setContentView(root)
        updateQuotaPanel()
        updatePendingButton()
    }

    private fun button(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 13f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(34, 36, 42))
        setPadding(dp(12), 0, dp(12), 0)
        setOnClickListener { action() }
        layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)).apply {
                marginEnd = dp(6)
            }
    }

    /**
     * Deliberately accepts any file type because some Android file providers
     * expose CSV files with unexpected MIME types.
     */
    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, fileRequestCode)
    }

    @Deprecated("Deprecated in Android API but kept for a minimal dependency-free Activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return

        when (requestCode) {
            fileRequestCode -> data.data?.let(::loadFile)
            authRequestCode -> {
                try {
                    val result =
                        Identity.getAuthorizationClient(this)
                            .getAuthorizationResultFromIntent(data)
                    val token = result.accessToken
                    if (token.isNullOrBlank()) {
                        toast("Google не повернув access token")
                    } else {
                        handleAuthorizedToken(token)
                    }
                } catch (e: ApiException) {
                    toast("Авторизація не вдалася: ${e.statusCode}")
                }
            }
        }
    }

    private fun loadFile(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }

        val fileName = queryFileName(uri) ?: "playlist.csv"
        val text =
            contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: return toast("Не вдалося прочитати файл")

        runCatching { PlaylistParser.parse(fileName, text) }
            .onSuccess {
                applyImportedPlaylist(
                    imported = it,
                    sourceLabel = "Файл"
                )
            }
            .onFailure { toast(it.message ?: "Помилка імпорту") }
    }

    private fun showPasteTrackListDialog() {
        val playlistNameInput = EditText(this).apply {
            hint = "Назва плейлиста (необов’язково)"
            setSingleLine(true)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val tracksInput = EditText(this).apply {
            hint =
                "Solarstone & JES - Like a Waterfall\n" +
                    "Sultan & Tone Depth - Moments\n" +
                    "Ahmet Ertenu - Why"
            minLines = 9
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(4), dp(18), 0)
            addView(
                playlistNameInput,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                tracksInput,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Вставити список треків")
            .setMessage(
                "Один трек на рядок: Artist - Track. " +
                    "Підтримуються також – та — і нумерація 1. / 2)."
            )
            .setView(container)
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Імпортувати", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val rawText = tracksInput.text.toString()
                if (rawText.isBlank()) {
                    tracksInput.error = "Вставте хоча б один трек"
                    return@setOnClickListener
                }

                runCatching {
                    PlaylistParser.parse("Вставлений список.txt", rawText).also { imported ->
                        playlistNameInput.text.toString().trim()
                            .takeIf { it.isNotBlank() }
                            ?.let { imported.name = it }
                    }
                }.onSuccess { imported ->
                    applyImportedPlaylist(
                        imported = imported,
                        sourceLabel = "Текст"
                    )
                    dialog.dismiss()
                }.onFailure { error ->
                    tracksInput.error = error.message ?: "Не вдалося розібрати список"
                }
            }
        }

        dialog.show()
    }

    private fun applyImportedPlaylist(
        imported: ImportedPlaylist,
        sourceLabel: String
    ) {
        playlist = imported
        createdPlaylistId = null
        resultPanel.visibility = View.GONE
        visibleTracks.clear()
        visibleTracks.addAll(imported.tracks)
        adapter.notifyDataSetChanged()
        updateSummary()
        status(
            "$sourceLabel імпортовано: ${imported.tracks.size} треків. " +
                "Натисніть «Знайти». Відомі треки будуть взяті з кешу."
        )
    }

    private fun queryFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) return cursor.getString(index)
        }
        return uri.lastPathSegment
    }

    private fun showAccountDialog() {
        if (accessToken.isNullOrBlank()) {
            authorize()
            return
        }

        val googleText =
            googleAccountInfo?.let {
                listOf(it.name, it.email)
                    .filter { value -> value.isNotBlank() }
                    .joinToString(" • ")
            }.orEmpty().ifBlank { "Google підключено, профіль не завантажено" }

        val youtubeText =
            youtubeChannelInfo?.let {
                "${it.title}\nChannel ID (ID каналу): ${it.id}"
            } ?: "YouTube/YTM канал не визначено"

        AlertDialog.Builder(this)
            .setTitle("Акаунт")
            .setMessage(
                "Google:\n$googleText\n\n" +
                    "YouTube / YouTube Music:\n$youtubeText\n\n" +
                    "Плейлисти записуються саме в цей YouTube/YTM профіль."
            )
            .setNegativeButton("Закрити", null)
            .setPositiveButton("Змінити акаунт") { _, _ ->
                authorize(after = null, forceAccountPicker = true)
            }
            .show()
    }

    private fun authorize(
        forceAccountPicker: Boolean = false,
        after: (() -> Unit)? = null
    ) {
        if (!forceAccountPicker && !accessToken.isNullOrBlank()) {
            if (googleAccountInfo == null || youtubeChannelInfo == null) {
                loadAccountIdentity(accessToken!!, after)
            } else {
                after?.invoke()
            }
            return
        }

        if (forceAccountPicker) {
            accessToken = null
            googleAccountInfo = null
            youtubeChannelInfo = null
            updateAccountPanel()
        }

        pendingAfterAuth = after
        status(
            if (forceAccountPicker) {
                "Виберіть Google акаунт…"
            } else {
                "Відкриваю доступ Google…"
            }
        )

        val builder =
            AuthorizationRequest.builder()
                .setRequestedScopes(
                    listOf(
                        Scope(YOUTUBE_SCOPE),
                        Scope(USERINFO_EMAIL_SCOPE),
                        Scope(USERINFO_PROFILE_SCOPE)
                    )
                )

        if (forceAccountPicker) {
            builder.setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
        }

        Identity.getAuthorizationClient(this)
            .authorize(builder.build())
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent!!.intentSender,
                            authRequestCode,
                            null,
                            0,
                            0,
                            0
                        )
                    } catch (e: Exception) {
                        pendingAfterAuth = null
                        toast("Не вдалося відкрити Google: ${e.message}")
                    }
                } else {
                    val token = result.accessToken
                    if (token.isNullOrBlank()) {
                        pendingAfterAuth = null
                        toast("Google не повернув access token")
                    } else {
                        handleAuthorizedToken(token)
                    }
                }
            }
            .addOnFailureListener { e ->
                pendingAfterAuth = null
                toast("Авторизація Google: ${e.message}")
            }
    }

    private fun handleAuthorizedToken(token: String) {
        accessToken = token
        googleAccountInfo = null
        youtubeChannelInfo = null
        updateAccountPanel()
        status("Google підключено. Завантажую дані акаунта і YouTube каналу…")

        val action = pendingAfterAuth
        pendingAfterAuth = null

        loadAccountIdentity(token, action)
    }

    private fun loadAccountIdentity(
        token: String,
        after: (() -> Unit)?
    ) {
        executor.execute {
            val googleResult =
                runCatching { api.getGoogleAccountInfo(token) }

            quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)
            val channelResult =
                runCatching { api.getMyYouTubeChannel(token) }

            runOnUiThread {
                googleAccountInfo = googleResult.getOrNull()
                youtubeChannelInfo = channelResult.getOrNull()
                updateAccountPanel()
                updateQuotaPanel()

                val channel = youtubeChannelInfo
                status(
                    if (channel != null) {
                        "Підключено YouTube/YTM: ${channel.title}"
                    } else {
                        "Google підключено, але YouTube канал не вдалося визначити."
                    }
                )

                after?.invoke()
            }
        }
    }

    private fun updateAccountPanel() {
        if (!::accountButton.isInitialized) return

        accountButton.text =
            when {
                accessToken.isNullOrBlank() ->
                    "2. Акаунт"

                youtubeChannelInfo != null ->
                    "2. Акаунт ✓"

                else ->
                    "2. Акаунт …"
            }
    }

    private fun searchAll() {
        val p = playlist ?: return toast("Спочатку імпортуйте список треків")

        val cachedCount =
            p.tracks.count { searchCache.get(it) != null }

        val apiNeeded = p.tracks.size - cachedCount
        val quota = quotaTracker.snapshot()

        val warning =
            if (apiNeeded > quota.searchRemaining) {
                "\n\n⚠ Локальна оцінка показує, що search quota " +
                    "(квоти пошуку) може не вистачити."
            } else {
                ""
            }

        AlertDialog.Builder(this)
            .setTitle("План пошуку (Search plan)")
            .setMessage(
                "Треків: ${p.tracks.size}\n" +
                    "Вже є в кеші: $cachedCount\n" +
                    "Потрібно нових search.list: $apiNeeded\n\n" +
                    "Локально використано сьогодні: ${quota.searchCalls}/" +
                    "${QuotaTracker.SEARCH_DAILY_LIMIT}\n" +
                    "Локальна оцінка залишку: ${quota.searchRemaining}" +
                    warning +
                    "\n\nЦе не точний залишок Google Cloud. " +
                    "Інші пристрої або клієнти того самого API project " +
                    "(проєкту API) можуть теж витрачати квоту."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Почати") { _, _ ->
                startSearch(p)
            }
            .show()
    }

    private fun startSearch(p: ImportedPlaylist) {
        authorize {
            val token = accessToken ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.max = p.tracks.size
            progress.progress = 0
            status("Пошук 0/${p.tracks.size} • кеш 0 • API 0")

            executor.execute {
                var cacheHits = 0
                var apiSearches = 0
                var quotaBlocked = false
                var quotaToastShown = false

                for ((index, track) in p.tracks.withIndex()) {
                    if (Thread.currentThread().isInterrupted) break

                    track.status = TrackStatus.SEARCHING
                    track.error = null
                    refreshRow(index)

                    try {
                        val cachedCandidates = searchCache.get(track)

                        val candidates =
                            if (cachedCandidates != null) {
                                cacheHits += 1
                                quotaTracker.recordCacheHit()
                                cachedCandidates
                            } else if (quotaBlocked) {
                                track.status = TrackStatus.FAILED
                                track.error =
                                    "Немає в кеші, а квота YouTube search API вже закінчилась"
                                emptyList()
                            } else {
                                apiSearches += 1
                                quotaTracker.recordSearchCall()
                                val freshCandidates = api.search(token, track)
                                searchCache.put(track, freshCandidates)
                                freshCandidates
                            }

                        if (track.status != TrackStatus.FAILED) {
                            applySearchCandidates(track, candidates)
                        }
                    } catch (e: Exception) {
                        track.status = TrackStatus.FAILED
                        track.error = e.message

                        if (isQuotaError(e)) {
                            quotaBlocked = true
                            quotaTracker.recordQuotaError(
                                e.message ?: "Search quota exceeded"
                            )

                            if (!quotaToastShown) {
                                quotaToastShown = true
                                runOnUiThread {
                                    toast(
                                        "Закінчилась квота пошуку YouTube API. " +
                                            "Треки, які вже є в кеші, програма ще обробить."
                                    )
                                }
                            }
                        }
                    }

                    runOnUiThread {
                        progress.progress = index + 1
                        status(
                            "Пошук ${index + 1}/${p.tracks.size} • " +
                                "кеш $cacheHits • API $apiSearches"
                        )
                        adapter.notifyDataSetChanged()
                        updateSummary()
                        updateQuotaPanel()
                    }
                }

                runOnUiThread {
                    progress.visibility = View.GONE

                    status(
                        if (quotaBlocked) {
                            "Готово: з кешу $cacheHits, API-запитів $apiSearches. " +
                                "Квота закінчилась; некешовані треки залишились без пошуку."
                        } else {
                            "Пошук завершено: з кешу $cacheHits, " +
                                "нових API-пошуків $apiSearches. " +
                                "Жовті треки краще перевірити натисканням."
                        }
                    )

                    updateSummary()
                    updateQuotaPanel()
                }
            }
        }
    }

    private fun applySearchCandidates(
        track: Track,
        candidates: List<SearchCandidate>
    ) {
        track.candidates = candidates

        val best = candidates.firstOrNull()
        if (best == null) {
            track.status = TrackStatus.MISSING
            track.selectedVideoId = null
            track.selectedTitle = null
            track.selectedChannel = null
            return
        }

        applyCandidate(track, best, manual = false)

        track.status =
            when {
                best.score >= 0.72 -> TrackStatus.MATCHED
                else -> TrackStatus.REVIEW
            }
    }

    private fun createPlaylist() {
        val p = playlist ?: return toast("Спочатку імпортуйте список треків")
        val selected =
            p.tracks.filter {
                !it.selectedVideoId.isNullOrBlank() && it.status != TrackStatus.SKIPPED
            }

        if (selected.isEmpty()) return toast("Спочатку знайдіть треки")

        val questionable = selected.count { it.status == TrackStatus.REVIEW }
        if (questionable > 0) {
            AlertDialog.Builder(this)
                .setTitle("Є $questionable неперевірених треків")
                .setMessage(
                    "Можна створити плейлист зараз, але краще переглянути " +
                        "жовті позиції. Продовжити?"
                )
                .setNegativeButton("Перевірю") { _, _ -> }
                .setPositiveButton("Продовжити") { _, _ ->
                    chooseDestination(p, selected)
                }
                .show()
        } else {
            chooseDestination(p, selected)
        }
    }

    private fun chooseDestination(
        p: ImportedPlaylist,
        selected: List<Track>
    ) {
        val labels = arrayOf(
            "➕ Створити новий плейлист",
            "📚 Додати до існуючого плейлиста"
        )

        AlertDialog.Builder(this)
            .setTitle("Куди додавати треки?")
            .setItems(labels) { _, which ->
                when (which) {
                    0 -> choosePrivacyAndCreate(p, selected)
                    1 -> chooseExistingPlaylist(p, selected)
                }
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun chooseExistingPlaylist(
        p: ImportedPlaylist,
        selected: List<Track>
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.isIndeterminate = true
            status("Завантажую ваші існуючі плейлисти…")

            executor.execute {
                quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)

                val result = runCatching {
                    api.listMyPlaylists(token)
                }

                runOnUiThread {
                    progress.isIndeterminate = false
                    progress.visibility = View.GONE
                    updateQuotaPanel()

                    result.onSuccess { playlists ->
                        if (playlists.isEmpty()) {
                            toast(
                                "У цьому YouTube/YTM профілі немає доступних плейлистів."
                            )
                        } else {
                            showExistingPlaylistDialog(
                                p = p,
                                selected = selected,
                                playlists = playlists
                            )
                        }
                    }.onFailure { error ->
                        toast(
                            error.message ?: "Не вдалося завантажити плейлисти"
                        )
                    }
                }
            }
        }
    }

    private fun showExistingPlaylistDialog(
        p: ImportedPlaylist,
        selected: List<Track>,
        playlists: List<YouTubePlaylistInfo>
    ) {
        val searchInput = EditText(this).apply {
            hint = "Пошук плейлиста за назвою"
            setSingleLine(true)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val list = ListView(this)
        val visible = playlists.toMutableList()
        val labels = visible.map(::existingPlaylistLabel).toMutableList()

        val listAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                labels
            )
        list.adapter = listAdapter

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(4), dp(16), 0)
            addView(
                searchInput,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                list,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(420)
                ).apply {
                    topMargin = dp(8)
                }
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Існуючі плейлисти: ${playlists.size}")
            .setView(container)
            .setNegativeButton("Скасувати", null)
            .create()

        fun applyFilter(query: String) {
            val normalized = query.trim().lowercase()
            visible.clear()
            visible.addAll(
                if (normalized.isBlank()) {
                    playlists
                } else {
                    playlists.filter {
                        it.title.lowercase().contains(normalized)
                    }
                }
            )

            listAdapter.clear()
            listAdapter.addAll(visible.map(::existingPlaylistLabel))
            listAdapter.notifyDataSetChanged()
        }

        searchInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    applyFilter(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: Editable?) = Unit
            }
        )

        list.setOnItemClickListener { _, _, position, _ ->
            val target = visible.getOrNull(position) ?: return@setOnItemClickListener
            dialog.dismiss()
            confirmAppendToExisting(
                p = p,
                selected = selected,
                target = target
            )
        }

        dialog.show()
    }

    private fun existingPlaylistLabel(item: YouTubePlaylistInfo): String =
        buildString {
            append(item.title)
            append("\n")
            append(item.itemCount)
            append(" треків • ")
            append(privacyLabel(item.privacyStatus))
        }

    private fun confirmAppendToExisting(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo
    ) {
        val google =
            googleAccountInfo?.email
                ?.takeIf { it.isNotBlank() }
                ?: "Google акаунт підключено"

        val channel =
            youtubeChannelInfo?.let {
                "${it.title} (${it.id})"
            } ?: "поточний YouTube/YTM канал"

        AlertDialog.Builder(this)
            .setTitle("Додати до існуючого?")
            .setMessage(
                "Плейлист:\n${target.title}\n\n" +
                    "Треків буде додано: ${selected.size}\n" +
                    "Google: $google\n" +
                    "YouTube/YTM: $channel\n\n" +
                    quotaPlanForWrite(
                        trackCount = selected.size,
                        createPlaylist = false
                    ) +
                    "\n\nУ v0.10.1 дублікати ще не відсіюються автоматично."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Додати") { _, _ ->
                actuallyAppendToExisting(
                    p = p,
                    selected = selected,
                    target = target
                )
            }
            .show()
    }

    private fun actuallyAppendToExisting(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            val job =
                buildPendingJob(
                    playlistName = target.title,
                    playlistId = target.id,
                    privacyStatus = target.privacyStatus,
                    destination = PendingDestination.EXISTING_PLAYLIST,
                    tracks = selected
                )

            pendingJobStore.upsert(job)
            updatePendingButton()

            createdPlaylistId = target.id
            prepareWriteUi(
                total = selected.size,
                message = "Додаю треки до «${target.title}»…"
            )

            executeWriteJob(
                initialJob = job,
                tracks = selected,
                token = token,
                operationLabel = "оновлено існуючий"
            )
        }
    }

    private fun choosePrivacyAndCreate(
        p: ImportedPlaylist,
        selected: List<Track>
    ) {
        val labels = arrayOf(
            "🔒 Приватний — тільки ви",
            "🔗 За посиланням — бачать ті, хто має посилання",
            "🌍 Публічний — видно всім"
        )

        val values = arrayOf(
            "private",
            "unlisted",
            "public"
        )

        var selectedIndex = 0

        val dialog = AlertDialog.Builder(this)
            .setTitle("Приватність плейлиста")
            .setSingleChoiceItems(labels, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Далі") { _, _ ->
                confirmCreateWithQuota(
                    p = p,
                    selected = selected,
                    privacyStatus = values[selectedIndex]
                )
            }
            .create()

        dialog.show()
    }

    private fun confirmCreateWithQuota(
        p: ImportedPlaylist,
        selected: List<Track>,
        privacyStatus: String
    ) {
        val google =
            googleAccountInfo?.email
                ?.takeIf { it.isNotBlank() }
                ?: "Google акаунт підключено"

        val channel =
            youtubeChannelInfo?.let {
                "${it.title} (${it.id})"
            } ?: "поточний YouTube/YTM канал"

        AlertDialog.Builder(this)
            .setTitle("Підтвердження створення")
            .setMessage(
                "Новий плейлист: ${p.name}\n" +
                    "Треків: ${selected.size}\n" +
                    "Приватність: ${privacyLabel(privacyStatus)}\n" +
                    "Google: $google\n" +
                    "YouTube/YTM: $channel\n\n" +
                    quotaPlanForWrite(
                        trackCount = selected.size,
                        createPlaylist = true
                    )
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Створити") { _, _ ->
                actuallyCreatePlaylist(
                    p = p,
                    selected = selected,
                    privacyStatus = privacyStatus
                )
            }
            .show()
    }

    private fun actuallyCreatePlaylist(
        p: ImportedPlaylist,
        selected: List<Track>,
        privacyStatus: String
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            val job =
                buildPendingJob(
                    playlistName = p.name,
                    playlistId = null,
                    privacyStatus = privacyStatus,
                    destination = PendingDestination.NEW_PLAYLIST,
                    tracks = selected
                )

            pendingJobStore.upsert(job)
            updatePendingButton()

            createdPlaylistId = null
            prepareWriteUi(
                total = selected.size + 1,
                message = "Створюю плейлист…"
            )

            executeWriteJob(
                initialJob = job,
                tracks = selected,
                token = token,
                operationLabel = "створено новий"
            )
        }
    }

    private fun prepareWriteUi(
        total: Int,
        message: String
    ) {
        progress.visibility = View.VISIBLE
        progress.isIndeterminate = false
        progress.max = total.coerceAtLeast(1)
        progress.progress = 0
        status(message)
    }

    private fun executeWriteJob(
        initialJob: PendingJob,
        tracks: List<Track>,
        token: String,
        operationLabel: String
    ) {
        executor.execute {
            var job = initialJob
            var playlistId = job.playlistId

            if (playlistId.isNullOrBlank()) {
                quotaTracker.recordGeneralUnits(QuotaTracker.PLAYLIST_CREATE_COST)

                try {
                    playlistId =
                        api.createPlaylist(
                            token,
                            job.playlistName,
                            job.privacyStatus
                        )

                    createdPlaylistId = playlistId

                    job =
                        job.copy(
                            playlistId = playlistId,
                            updatedAt = System.currentTimeMillis(),
                            lastError = null
                        )

                    pendingJobStore.upsert(job)

                    runOnUiThread {
                        progress.progress = 1
                        updateQuotaPanel()
                        updatePendingButton()
                    }
                } catch (e: Exception) {
                    if (isQuotaError(e)) {
                        quotaTracker.recordQuotaError(
                            e.message ?: "Quota exceeded while creating playlist"
                        )

                        job =
                            job.copy(
                                updatedAt = System.currentTimeMillis(),
                                lastError = e.message
                            )

                        pendingJobStore.upsert(job)

                        runOnUiThread {
                            progress.visibility = View.GONE
                            markAllPending(tracks, e.message)
                            updateQuotaPanel()
                            updatePendingButton()
                            showQuotaPausedDialog(job)
                        }
                    } else {
                        pendingJobStore.remove(job.id)

                        runOnUiThread {
                            progress.visibility = View.GONE
                            updatePendingButton()
                            toast(e.message ?: "Не вдалося створити плейлист")
                        }
                    }

                    return@execute
                }
            } else {
                createdPlaylistId = playlistId
            }

            val createOffset =
                if (job.destination == PendingDestination.NEW_PLAYLIST) 1 else 0

            for ((index, track) in tracks.withIndex()) {
                val videoId = track.selectedVideoId

                if (videoId.isNullOrBlank()) {
                    track.status = TrackStatus.FAILED
                    track.error = "Немає videoId для додавання"

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            failedCount = job.failedCount + 1,
                            remainingTracks = job.remainingTracks.drop(1),
                            lastError = track.error
                        )

                    pendingJobStore.upsert(job)

                    runOnUiThread {
                        progress.progress = index + 1 + createOffset
                        adapter.notifyDataSetChanged()
                        updateSummary()
                        updatePendingButton()
                    }

                    continue
                }

                quotaTracker.recordGeneralUnits(
                    QuotaTracker.PLAYLIST_ITEM_INSERT_COST
                )

                try {
                    api.addVideo(token, playlistId!!, videoId)

                    track.status = TrackStatus.ADDED
                    track.error = null

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            addedCount = job.addedCount + 1,
                            remainingTracks = job.remainingTracks.drop(1),
                            lastError = null
                        )

                    pendingJobStore.upsert(job)
                } catch (e: Exception) {
                    if (isQuotaError(e)) {
                        quotaTracker.recordQuotaError(
                            e.message ?: "Quota exceeded while adding track"
                        )

                        val remaining =
                            tracks.drop(index).mapNotNull(::trackToPendingTrack)

                        job =
                            job.copy(
                                updatedAt = System.currentTimeMillis(),
                                remainingTracks = remaining,
                                lastError = e.message
                            )

                        pendingJobStore.upsert(job)

                        runOnUiThread {
                            tracks.drop(index).forEach { pendingTrack ->
                                pendingTrack.status = TrackStatus.PENDING
                                pendingTrack.error =
                                    "Очікує продовження: " +
                                        (e.message ?: "закінчилась квота API")
                            }

                            progress.visibility = View.GONE
                            adapter.notifyDataSetChanged()
                            updateSummary()
                            updateQuotaPanel()
                            updatePendingButton()
                            showQuotaPausedDialog(job)
                        }

                        return@execute
                    }

                    track.status = TrackStatus.FAILED
                    track.error = e.message

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            failedCount = job.failedCount + 1,
                            remainingTracks = job.remainingTracks.drop(1),
                            lastError = e.message
                        )

                    pendingJobStore.upsert(job)
                }

                runOnUiThread {
                    progress.progress = index + 1 + createOffset
                    status(
                        "Додаю ${index + 1}/${tracks.size}… " +
                            "залишилось ${job.remainingTracks.size}"
                    )
                    adapter.notifyDataSetChanged()
                    updateSummary()
                    updateQuotaPanel()
                    updatePendingButton()
                }
            }

            pendingJobStore.remove(job.id)

            runOnUiThread {
                progress.visibility = View.GONE
                updatePendingButton()
                updateQuotaPanel()
                updateSummary()

                val completedAdded = job.addedCount
                val completedFailed = job.failedCount

                status(
                    "Готово. ${job.playlistName}: " +
                        "додано $completedAdded, помилок $completedFailed."
                )

                showPlaylistResult(
                    playlistName = job.playlistName,
                    addedCount = completedAdded,
                    failedCount = completedFailed,
                    privacyStatus = job.privacyStatus,
                    operationLabel = operationLabel
                )
            }
        }
    }

    private fun buildPendingJob(
        playlistName: String,
        playlistId: String?,
        privacyStatus: String,
        destination: PendingDestination,
        tracks: List<Track>
    ): PendingJob {
        val now = System.currentTimeMillis()

        return PendingJob(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            playlistName = playlistName,
            playlistId = playlistId,
            privacyStatus = privacyStatus,
            destination = destination,
            googleEmail = googleAccountInfo?.email,
            youtubeChannelId = youtubeChannelInfo?.id,
            youtubeChannelTitle = youtubeChannelInfo?.title,
            totalCount = tracks.size,
            addedCount = 0,
            failedCount = 0,
            remainingTracks = tracks.mapNotNull(::trackToPendingTrack),
            lastError = null
        )
    }

    private fun trackToPendingTrack(track: Track): PendingTrack? {
        val videoId = track.selectedVideoId ?: return null

        return PendingTrack(
            originalTitle = track.originalTitle,
            originalArtist = track.originalArtist,
            videoId = videoId,
            selectedTitle = track.selectedTitle,
            selectedChannel = track.selectedChannel
        )
    }

    private fun pendingTrackToTrack(item: PendingTrack): Track =
        Track(
            originalTitle = item.originalTitle,
            originalArtist = item.originalArtist,
            selectedVideoId = item.videoId,
            selectedTitle = item.selectedTitle,
            selectedChannel = item.selectedChannel,
            status = TrackStatus.PENDING,
            manuallySelected = false
        )

    private fun markAllPending(
        tracks: List<Track>,
        error: String?
    ) {
        tracks.forEach {
            it.status = TrackStatus.PENDING
            it.error =
                "Очікує продовження: " +
                    (error ?: "операцію зупинено")
        }

        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun showQuotaPausedDialog(job: PendingJob) {
        val playlistInfo =
            if (job.playlistId.isNullOrBlank()) {
                "Плейлист ще не створений."
            } else {
                "Playlist ID (ID плейлиста): ${job.playlistId}"
            }

        AlertDialog.Builder(this)
            .setTitle("Операцію призупинено")
            .setMessage(
                "YouTube API повідомив про вичерпання квоти.\n\n" +
                    "Вже додано: ${job.addedCount}/${job.totalCount}\n" +
                    "Помилок: ${job.failedCount}\n" +
                    "У черзі: ${job.remainingTracks.size}\n" +
                    "$playlistInfo\n\n" +
                    "Невиконані треки збережені локально. " +
                    "Відкрийте «Черга» і натисніть «Продовжити», " +
                    "коли квота відновиться."
            )
            .setNegativeButton("Закрити", null)
            .setPositiveButton("Відкрити чергу") { _, _ ->
                showPendingJobs()
            }
            .show()
    }

    private fun showPendingJobs() {
        val jobs = pendingJobStore.getAll()

        if (jobs.isEmpty()) {
            return toast("Черга порожня — недороблених плейлистів немає")
        }

        val labels =
            jobs.map { job ->
                buildString {
                    append(job.playlistName)
                    append("\n")
                    append("⏳ ")
                    append(job.remainingTracks.size)
                    append(" очікують • ✓ ")
                    append(job.addedCount)
                    append("/")
                    append(job.totalCount)

                    if (job.failedCount > 0) {
                        append(" • × ")
                        append(job.failedCount)
                    }
                }
            }

        AlertDialog.Builder(this)
            .setTitle("Черга (Pending Queue): ${jobs.size}")
            .setItems(labels.toTypedArray()) { _, which ->
                jobs.getOrNull(which)?.let(::showPendingJobDetails)
            }
            .setNegativeButton("Закрити", null)
            .show()
    }

    private fun showPendingJobDetails(job: PendingJob) {
        val destination =
            when (job.destination) {
                PendingDestination.NEW_PLAYLIST ->
                    "новий плейлист"
                PendingDestination.EXISTING_PLAYLIST ->
                    "існуючий плейлист"
            }

        val playlistId =
            job.playlistId ?: "ще не створений"

        AlertDialog.Builder(this)
            .setTitle(job.playlistName)
            .setMessage(
                "Тип: $destination\n" +
                    "Додано: ${job.addedCount}/${job.totalCount}\n" +
                    "Помилок: ${job.failedCount}\n" +
                    "Очікує: ${job.remainingTracks.size}\n\n" +
                    "Google: ${job.googleEmail ?: "не збережено"}\n" +
                    "YouTube/YTM: ${job.youtubeChannelTitle ?: "не збережено"}\n" +
                    "Channel ID: ${job.youtubeChannelId ?: "—"}\n" +
                    "Playlist ID: $playlistId\n\n" +
                    "Остання помилка:\n${job.lastError ?: "—"}"
            )
            .setNegativeButton("Закрити", null)
            .setNeutralButton("Видалити") { _, _ ->
                confirmDeletePendingJob(job)
            }
            .setPositiveButton("Продовжити") { _, _ ->
                resumePendingJob(job)
            }
            .show()
    }

    private fun confirmDeletePendingJob(job: PendingJob) {
        AlertDialog.Builder(this)
            .setTitle("Видалити із черги?")
            .setMessage(
                "Локальний запис «${job.playlistName}» буде видалений. " +
                    "Вже додані в YouTube/YTM треки не видаляються."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Видалити") { _, _ ->
                pendingJobStore.remove(job.id)
                updatePendingButton()
                toast("Запис видалено з черги")
            }
            .show()
    }

    private fun resumePendingJob(job: PendingJob) {
        authorize {
            val token = accessToken ?: return@authorize

            val currentChannel = youtubeChannelInfo?.id
            val currentEmail = googleAccountInfo?.email

            val channelMismatch =
                !job.youtubeChannelId.isNullOrBlank() &&
                    !currentChannel.isNullOrBlank() &&
                    job.youtubeChannelId != currentChannel

            val emailMismatch =
                !job.googleEmail.isNullOrBlank() &&
                    !currentEmail.isNullOrBlank() &&
                    !job.googleEmail.equals(currentEmail, ignoreCase = true)

            if (channelMismatch || emailMismatch) {
                AlertDialog.Builder(this)
                    .setTitle("Потрібен інший акаунт")
                    .setMessage(
                        "Це завдання було створено для:\n" +
                            "Google: ${job.googleEmail ?: "—"}\n" +
                            "YouTube/YTM: ${job.youtubeChannelTitle ?: "—"}\n" +
                            "Channel ID: ${job.youtubeChannelId ?: "—"}\n\n" +
                            "Зараз підключений інший акаунт або канал."
                    )
                    .setNegativeButton("Скасувати", null)
                    .setPositiveButton("Змінити акаунт") { _, _ ->
                        authorize(forceAccountPicker = true) {
                            resumePendingJob(job)
                        }
                    }
                    .show()

                return@authorize
            }

            val tracks =
                job.remainingTracks.map(::pendingTrackToTrack)

            playlist =
                ImportedPlaylist(
                    name = job.playlistName,
                    tracks = tracks.toMutableList()
                )

            visibleTracks.clear()
            visibleTracks.addAll(tracks)
            adapter.notifyDataSetChanged()
            createdPlaylistId = job.playlistId
            updateSummary()

            prepareWriteUi(
                total =
                    tracks.size +
                        if (
                            job.destination == PendingDestination.NEW_PLAYLIST &&
                            job.playlistId.isNullOrBlank()
                        ) 1 else 0,
                message = "Продовжую «${job.playlistName}»…"
            )

            executeWriteJob(
                initialJob = job.copy(
                    updatedAt = System.currentTimeMillis(),
                    lastError = null
                ),
                tracks = tracks,
                token = token,
                operationLabel = "продовжено з черги"
            )
        }
    }

    private fun quotaPlanForWrite(
        trackCount: Int,
        createPlaylist: Boolean
    ): String {
        val required =
            trackCount * QuotaTracker.PLAYLIST_ITEM_INSERT_COST +
                if (createPlaylist) QuotaTracker.PLAYLIST_CREATE_COST else 0

        val quota = quotaTracker.snapshot()

        val warning =
            if (required > quota.generalRemaining) {
                "\n⚠ Локальна оцінка показує, що квоти може не вистачити. " +
                    "Якщо Google поверне quotaExceeded, залишок автоматично " +
                    "піде в Чергу (Pending Queue)."
            } else {
                ""
            }

        return "Квота API (оцінка):\n" +
            "Потрібно приблизно: $required units (одиниць)\n" +
            "Локально залишилось приблизно: ${quota.generalRemaining}/" +
            "${QuotaTracker.GENERAL_DAILY_LIMIT}" +
            warning
    }

    private fun showQuotaDialog() {
        val quota = quotaTracker.snapshot()
        val jobs = pendingJobStore.getAll()

        AlertDialog.Builder(this)
            .setTitle("Квота API (локальна оцінка)")
            .setMessage(
                "Search Queries (пошук):\n" +
                    "${quota.searchCalls}/${QuotaTracker.SEARCH_DAILY_LIMIT} використано\n" +
                    "≈ ${quota.searchRemaining} залишилось\n\n" +
                    "General quota (загальна квота):\n" +
                    "${quota.generalUnits}/${QuotaTracker.GENERAL_DAILY_LIMIT} використано\n" +
                    "≈ ${quota.generalRemaining} залишилось\n\n" +
                    "Попадань у кеш сьогодні: ${quota.cacheHits}\n" +
                    "Недороблених завдань: ${jobs.size}\n" +
                    "День квоти Google: ${quota.dayKey} (Pacific Time)\n\n" +
                    "Це локальна оцінка тільки для операцій, які цей застосунок " +
                    "зафіксував на цьому телефоні. Точний стан знаходиться в " +
                    "Google Cloud Console." +
                    if (!quota.lastQuotaError.isNullOrBlank()) {
                        "\n\nОстання quota error (помилка квоти):\n" +
                            quota.lastQuotaError
                    } else {
                        ""
                    }
            )
            .setNegativeButton("Закрити", null)
            .setPositiveButton("Черга") { _, _ ->
                showPendingJobs()
            }
            .show()
    }

    private fun updateQuotaPanel() {
        if (!::quotaButton.isInitialized) return

        val quota = quotaTracker.snapshot()

        quotaButton.text =
            if (quota.lastQuotaError.isNullOrBlank()) {
                "Квота"
            } else {
                "Квота ⚠"
            }
    }

    private fun updatePendingButton() {
        if (!::pendingButton.isInitialized) return

        val count = pendingJobStore.getAll().size
        pendingButton.text =
            if (count > 0) {
                "Черга ($count)"
            } else {
                "Черга"
            }
    }

    private fun isQuotaError(error: Throwable): Boolean =
        (error as? YouTubeApiException)?.isQuotaError == true ||
            error.message.orEmpty().contains("quota", ignoreCase = true) ||
            error.message.orEmpty().contains("daily limit", ignoreCase = true)


    private fun showTrackDialog(track: Track) {
        val candidates = track.candidates
        val labels = mutableListOf<String>()

        candidates.forEachIndexed { index, candidate ->
            val selectedMark =
                if (candidate.videoId == track.selectedVideoId) "✓ " else ""

            labels +=
                "$selectedMark${index + 1}. ${(candidate.score * 100).roundToInt()}%  " +
                    "${candidate.title}\n${candidate.channelTitle}"
        }

        labels += "🔗 Вставити YouTube / YouTube Music URL"
        labels += "⏭ Пропустити цей трек"

        AlertDialog.Builder(this)
            .setTitle(
                if (candidates.isEmpty()) {
                    "${track.originalArtist} — ${track.originalTitle}\nКандидатів немає"
                } else {
                    "${track.originalArtist} — ${track.originalTitle}\n" +
                        "Кандидати: ${candidates.size}"
                }
            )
            .setItems(labels.toTypedArray()) { _, which ->
                when {
                    which < candidates.size -> {
                        showCandidateDialog(track, candidates[which])
                    }

                    which == candidates.size -> {
                        showPasteUrlDialog(track)
                    }

                    else -> {
                        track.status = TrackStatus.SKIPPED
                        track.selectedVideoId = null
                        track.selectedTitle = null
                        track.selectedChannel = null
                        track.manuallySelected = true
                        adapter.notifyDataSetChanged()
                        updateSummary()
                        status(
                            "Пропущено: ${track.originalArtist} — ${track.originalTitle}"
                        )
                    }
                }
            }
            .setNegativeButton("Закрити", null)
            .show()
    }

    private fun showCandidateDialog(
        track: Track,
        candidate: SearchCandidate
    ) {
        val scorePercent = (candidate.score * 100).roundToInt()
        val isCurrent = candidate.videoId == track.selectedVideoId

        AlertDialog.Builder(this)
            .setTitle(candidate.title)
            .setMessage(
                buildString {
                    append("Канал: ${candidate.channelTitle}\n")
                    append("Збіг: $scorePercent%")
                    if (isCurrent) {
                        append("\n\n✓ Зараз вибрано для цього треку")
                    }
                }
            )
            .setNegativeButton("Назад") { _, _ ->
                showTrackDialog(track)
            }
            .setNeutralButton("Відкрити в YTM") { _, _ ->
                openCandidateInYtm(candidate.videoId)
            }
            .setPositiveButton(
                if (isCurrent) "Залишити" else "Використати"
            ) { _, _ ->
                applyCandidate(track, candidate, manual = true)
                track.status = TrackStatus.MATCHED
                adapter.notifyDataSetChanged()
                updateSummary()

                status(
                    "Вибрано вручну: ${track.originalArtist} — " +
                        "${track.originalTitle} → ${candidate.title}"
                )
            }
            .show()
    }

    private fun openCandidateInYtm(videoId: String) {
        val uri = Uri.parse("https://music.youtube.com/watch?v=$videoId")

        val ytmIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.youtube.music")
        }

        val openedInYtm =
            runCatching {
                startActivity(ytmIntent)
                true
            }.getOrDefault(false)

        if (openedInYtm) return

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            toast("Не вдалося відкрити кандидат у YouTube Music")
        }
    }

    private fun showPasteUrlDialog(track: Track) {
        val input = EditText(this).apply {
            hint = "https://music.youtube.com/watch?v=…"
            setSingleLine(true)
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        AlertDialog.Builder(this)
            .setTitle("Вставити посилання")
            .setView(input)
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Використати") { _, _ ->
                val videoId = extractVideoId(input.text.toString())

                if (videoId == null) {
                    toast("Не бачу YouTube video ID у посиланні")
                } else {
                    track.selectedVideoId = videoId
                    track.selectedTitle = "Ручне посилання"
                    track.selectedChannel = "YouTube"
                    track.status = TrackStatus.MATCHED
                    track.manuallySelected = true
                    adapter.notifyDataSetChanged()
                    updateSummary()
                }
            }
            .show()
    }

    private fun extractVideoId(value: String): String? {
        val text = value.trim()

        val regexes =
            listOf(
                Regex("[?&]v=([A-Za-z0-9_-]{11})"),
                Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
                Regex("^([A-Za-z0-9_-]{11})$")
            )

        return regexes.firstNotNullOfOrNull {
            it.find(text)?.groupValues?.getOrNull(1)
        }
    }

    private fun applyCandidate(
        track: Track,
        candidate: SearchCandidate,
        manual: Boolean
    ) {
        track.selectedVideoId = candidate.videoId
        track.selectedTitle = candidate.title
        track.selectedChannel = candidate.channelTitle
        track.manuallySelected = manual
        track.error = null
    }

    private fun showPlaylistResult(
        playlistName: String,
        addedCount: Int,
        failedCount: Int,
        privacyStatus: String,
        operationLabel: String
    ) {
        val url = playlistUrl() ?: return

        resultTitleText.text = "✓ $playlistName"
        resultDetailsText.text =
            buildString {
                append("Додано: $addedCount")
                if (failedCount > 0) append(" • Не додано: $failedCount")
                append(" • ${privacyLabel(privacyStatus)}")
                append(" • $operationLabel")
                youtubeChannelInfo?.title?.let {
                    append("\nYouTube/YTM: $it")
                }
            }
        resultLinkText.text = url
        resultPanel.visibility = View.VISIBLE
    }

    private fun playlistUrl(): String? {
        val id = createdPlaylistId ?: return null
        return "https://music.youtube.com/playlist?list=$id"
    }

    private fun openInYtm() {
        val url = playlistUrl() ?: return toast("Спочатку створіть або виберіть плейлист")
        val uri = Uri.parse(url)

        val ytmIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.youtube.music")
        }

        val openedInYtm =
            runCatching {
                startActivity(ytmIntent)
                true
            }.getOrDefault(false)

        if (openedInYtm) return

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            toast("Не вдалося відкрити YouTube Music")
        }
    }

    private fun copyPlaylistLink() {
        val url = playlistUrl() ?: return toast("Спочатку створіть або виберіть плейлист")

        val clipboard =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText("YTM playlist", url)
        )

        toast("Посилання на плейлист скопійовано")
    }

    private fun showReplacementLog() {
        val p = playlist ?: return toast("Немає імпортованого плейлиста")

        val problemTracks =
            p.tracks.filter { track ->
                track.manuallySelected ||
                    track.status == TrackStatus.SKIPPED ||
                    track.status == TrackStatus.MISSING ||
                    track.status == TrackStatus.PENDING ||
                    track.status == TrackStatus.FAILED
            }

        if (problemTracks.isEmpty()) {
            return toast("Замін, пропусків або проблемних треків поки немає")
        }

        val shortText = buildShortReplacementText(problemTracks)
        val fullText = buildFullReplacementText(problemTracks)

        AlertDialog.Builder(this)
            .setTitle("Заміни / проблемні треки: ${problemTracks.size}")
            .setMessage(shortText)
            .setNegativeButton("Закрити", null)
            .setNeutralButton("Копіювати повний") { _, _ ->
                copyText(
                    label = "YTM Importer replacement log",
                    text = fullText,
                    successMessage = "Повний журнал скопійовано"
                )
            }
            .setPositiveButton("Копіювати TikTok") { _, _ ->
                copyText(
                    label = "YTM Importer TikTok replacements",
                    text = shortText,
                    successMessage = "Короткий список для TikTok скопійовано"
                )
            }
            .show()
    }

    private fun buildShortReplacementText(tracks: List<Track>): String =
        buildString {
            append("Заміни / недоступні треки:\n")

            tracks.forEachIndexed { index, track ->
                append(index + 1)
                append(". ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append(" → ")
                append(replacementLabel(track))

                if (index != tracks.lastIndex) {
                    append('\n')
                }
            }
        }

    private fun buildFullReplacementText(tracks: List<Track>): String =
        buildString {
            append("YTM Importer — журнал замін\n\n")

            tracks.forEachIndexed { index, track ->
                append(index + 1)
                append(". Оригінал: ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append('\n')

                append("   Результат: ")
                append(replacementLabel(track))
                append('\n')

                if (!track.selectedChannel.isNullOrBlank()) {
                    append("   Канал: ")
                    append(track.selectedChannel)
                    append('\n')
                }

                if (!track.selectedVideoId.isNullOrBlank()) {
                    append("   YTM: https://music.youtube.com/watch?v=")
                    append(track.selectedVideoId)
                    append('\n')
                }

                if (!track.error.isNullOrBlank()) {
                    append("   Помилка: ")
                    append(track.error)
                    append('\n')
                }

                if (index != tracks.lastIndex) {
                    append('\n')
                }
            }
        }

    private fun replacementLabel(track: Track): String =
        when {
            track.status == TrackStatus.SKIPPED -> "[пропущено]"
            track.status == TrackStatus.MISSING -> "[не знайдено]"
            track.status == TrackStatus.PENDING -> "[очікує в черзі]"
            track.status == TrackStatus.FAILED &&
                track.selectedTitle.isNullOrBlank() -> "[помилка]"
            track.selectedTitle == "Ручне посилання" ->
                "[ручне YouTube/YTM посилання]"
            !track.selectedTitle.isNullOrBlank() ->
                track.selectedTitle!!
            else ->
                "[не знайдено]"
        }

    private fun copyText(
        label: String,
        text: String,
        successMessage: String
    ) {
        val clipboard =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(label, text)
        )

        toast(successMessage)
    }

    private fun privacyLabel(value: String): String =
        when (value) {
            "public" -> "публічний"
            "unlisted" -> "за посиланням"
            else -> "приватний"
        }

    private fun updateSummary() {
        val p = playlist ?: return

        val matched =
            p.tracks.count {
                it.status in setOf(TrackStatus.MATCHED, TrackStatus.ADDED)
            }

        val review =
            p.tracks.count {
                it.status == TrackStatus.REVIEW
            }

        val pending =
            p.tracks.count {
                it.status == TrackStatus.PENDING
            }

        val missing =
            p.tracks.count {
                it.status in setOf(TrackStatus.MISSING, TrackStatus.FAILED)
            }

        summaryText.text =
            "${p.name} • ${p.tracks.size} треків • " +
                "✓ $matched  ! $review  ⏳ $pending  × $missing"
    }

    private fun refreshRow(index: Int) {
        runOnUiThread {
            if (index in visibleTracks.indices) {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun status(message: String) {
        statusText.text = message
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        status(message)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    companion object {
        private const val YOUTUBE_SCOPE =
            "https://www.googleapis.com/auth/youtube.force-ssl"

        private const val USERINFO_EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email"

        private const val USERINFO_PROFILE_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile"
    }
}
