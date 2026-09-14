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
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.parser.PlaylistParser
import com.saney.ytmimporter.ui.TrackAdapter
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val fileRequestCode = 1001
    private val authRequestCode = 9001
    private val executor = Executors.newSingleThreadExecutor()
    private val api = YouTubeApi()

    private lateinit var searchCache: SearchCache

    private var playlist: ImportedPlaylist? = null
    private var accessToken: String? = null
    private var createdPlaylistId: String? = null
    private var pendingAfterAuth: (() -> Unit)? = null

    private lateinit var statusText: TextView
    private lateinit var summaryText: TextView
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
            setPadding(dp(18), dp(18), dp(18), dp(12))
        }
        header.addView(TextView(this).apply {
            text = "YTM Importer"
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        })
        header.addView(TextView(this).apply {
            text = "CSV/TXT/текст → пошук → перевірка → плейлист у YouTube Music"
            textSize = 13f
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
        actions.addView(button("2. Google") { authorize(null) })
        actions.addView(button("3. Знайти") { searchAll() })
        actions.addView(button("4. Створити") { createPlaylist() })
        actions.addView(button("Відкрити в ютм") { openInYtm() })
        actions.addView(button("Заміни") { showReplacementLog() })
        scroll.addView(actions)
        root.addView(scroll)

        summaryText = TextView(this).apply {
            setPadding(dp(18), dp(6), dp(18), dp(2))
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            text = "Плейлист ще не імпортовано"
        }
        root.addView(summaryText)

        statusText = TextView(this).apply {
            setPadding(dp(18), dp(4), dp(18), dp(8))
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
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)).apply {
                marginEnd = dp(8)
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
                        accessToken = token
                        status("Google підключено. Доступ до YouTube дозволено.")
                        val action = pendingAfterAuth
                        pendingAfterAuth = null
                        action?.invoke()
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

    private fun authorize(after: (() -> Unit)?) {
        if (!accessToken.isNullOrBlank()) {
            after?.invoke()
            return
        }

        pendingAfterAuth = after
        status("Відкриваю доступ Google…")

        val request =
            AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(YOUTUBE_SCOPE)))
                .build()

        Identity.getAuthorizationClient(this)
            .authorize(request)
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
                        toast("Не вдалося відкрити Google: ${e.message}")
                    }
                } else {
                    val token = result.accessToken
                    if (token.isNullOrBlank()) {
                        toast("Google не повернув access token")
                    } else {
                        accessToken = token
                        status("Google підключено.")
                        val action = pendingAfterAuth
                        pendingAfterAuth = null
                        action?.invoke()
                    }
                }
            }
            .addOnFailureListener { e ->
                toast("Авторизація Google: ${e.message}")
            }
    }

    private fun searchAll() {
        val p = playlist ?: return toast("Спочатку імпортуйте список треків")

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
                                cachedCandidates
                            } else if (quotaBlocked) {
                                track.status = TrackStatus.FAILED
                                track.error =
                                    "Немає в кеші, а денна квота YouTube API вже закінчилась"
                                emptyList()
                            } else {
                                // Only this branch spends one YouTube search request.
                                apiSearches += 1
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

                        if (e.message.orEmpty().contains("quota", ignoreCase = true)) {
                            quotaBlocked = true

                            if (!quotaToastShown) {
                                quotaToastShown = true
                                runOnUiThread {
                                    toast(
                                        "Закінчилась денна квота YouTube API. " +
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
                    choosePrivacyAndCreate(p, selected)
                }
                .show()
        } else {
            choosePrivacyAndCreate(p, selected)
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
            .setPositiveButton("Створити") { _, _ ->
                actuallyCreatePlaylist(
                    p = p,
                    selected = selected,
                    privacyStatus = values[selectedIndex]
                )
            }
            .create()

        dialog.show()
    }

    private fun actuallyCreatePlaylist(
        p: ImportedPlaylist,
        selected: List<Track>,
        privacyStatus: String
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.max = selected.size + 1
            progress.progress = 0
            status("Створюю плейлист…")

            executor.execute {
                try {
                    val playlistId = api.createPlaylist(token, p.name, privacyStatus)
                    createdPlaylistId = playlistId

                    runOnUiThread {
                        progress.progress = 1
                    }

                    for ((index, track) in selected.withIndex()) {
                        val videoId = track.selectedVideoId ?: continue

                        try {
                            api.addVideo(token, playlistId, videoId)
                            track.status = TrackStatus.ADDED
                        } catch (e: Exception) {
                            track.status = TrackStatus.FAILED
                            track.error = e.message
                        }

                        runOnUiThread {
                            progress.progress = index + 2
                            status("Додаю ${index + 1}/${selected.size}…")
                            adapter.notifyDataSetChanged()
                        }
                    }

                    runOnUiThread {
                        progress.visibility = View.GONE
                        updateSummary()
                        status(
                            "Готово. Плейлист створено: ${privacyLabel(privacyStatus)}."
                        )

                        val addedCount =
                            selected.count { it.status == TrackStatus.ADDED }
                        val failedCount =
                            selected.count { it.status == TrackStatus.FAILED }

                        showPlaylistResult(
                            playlistName = p.name,
                            addedCount = addedCount,
                            failedCount = failedCount,
                            privacyStatus = privacyStatus
                        )
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        progress.visibility = View.GONE
                        toast(e.message ?: "Не вдалося створити плейлист")
                    }
                }
            }
        }
    }

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
        privacyStatus: String
    ) {
        val url = playlistUrl() ?: return

        resultTitleText.text = "✓ $playlistName"
        resultDetailsText.text =
            buildString {
                append("Додано: $addedCount")
                if (failedCount > 0) append(" • Не додано: $failedCount")
                append(" • ${privacyLabel(privacyStatus)}")
            }
        resultLinkText.text = url
        resultPanel.visibility = View.VISIBLE
    }

    private fun playlistUrl(): String? {
        val id = createdPlaylistId ?: return null
        return "https://music.youtube.com/playlist?list=$id"
    }

    private fun openInYtm() {
        val url = playlistUrl() ?: return toast("Спочатку створіть плейлист")
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
        val url = playlistUrl() ?: return toast("Спочатку створіть плейлист")

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

        val missing =
            p.tracks.count {
                it.status in setOf(TrackStatus.MISSING, TrackStatus.FAILED)
            }

        summaryText.text =
            "${p.name} • ${p.tracks.size} треків • " +
                "✓ $matched  ! $review  × $missing"
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
    }
}
