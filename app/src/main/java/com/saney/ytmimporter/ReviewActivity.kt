package com.saney.ytmimporter
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import com.saney.ytmimporter.storage.SafTreeFileWriter
import com.saney.ytmimporter.ui.SafFileSaveFlow
import java.io.File
import kotlin.math.roundToInt

class ReviewActivity : Activity() {
    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private lateinit var snapshot:
        CurrentPlaylistSnapshot

    private var currentTrackHistoryIndex:
        Int? = null

    private var pendingProjectExport:
        String? = null

    private var pendingProjectDisplayName:
        String? = null

    private var pendingProjectSuggestedFileName:
        String? = null

    private val saveProjectRequestCode =
        3301

    private val saveProjectFolderRequestCode =
        3302

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        currentPlaylistStore =
            CurrentPlaylistStore(this)

        val loaded =
            currentPlaylistStore.load()

        if (loaded == null) {
            showEmptyState()
            return
        }

        snapshot = loaded

        val restoredTrackIndex =
            savedInstanceState
                ?.getInt(
                    KEY_TRACK_HISTORY_INDEX,
                    Int.MIN_VALUE
                )
                ?.takeIf {
                    it != Int.MIN_VALUE
                }

        val requestedTrackIndex =
            intent
                .getIntExtra(
                    EXTRA_FOCUS_HISTORY_INDEX,
                    Int.MIN_VALUE
                )
                .takeIf {
                    it != Int.MIN_VALUE
                }

        val focus =
            restoredTrackIndex
                ?: requestedTrackIndex

        if (focus != null) {
            findTrackByHistoryIndex(
                focus
            )?.let { track ->
                showTrackScreen(track)
                return
            }
        }

        showListScreen()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (resultCode != RESULT_OK) {
            return
        }

        when (requestCode) {
            saveProjectRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::writePendingProject
                    )

            saveProjectFolderRequestCode -> {
                val uri =
                    data
                        ?.data
                        ?: return

                if (
                    data.getStringExtra(
                        StorageChooserActivity.EXTRA_RESULT_KIND
                    ) ==
                        StorageChooserActivity.RESULT_DOCUMENT
                ) {
                    writePendingProject(uri)
                } else {
                    writePendingProjectToTree(uri)
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        currentTrackHistoryIndex
            ?.let { value ->
                outState.putInt(
                    KEY_TRACK_HISTORY_INDEX,
                    value
                )
            }

        super.onSaveInstanceState(
            outState
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (
            currentTrackHistoryIndex != null
        ) {
            showListScreen()
        } else {
            super.onBackPressed()
        }
    }

    private fun showEmptyState() {
        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(AppThemeManager.palette(this@ReviewActivity).background)
            }

        root.addView(
            topBar(
                title =
                    "Перевірка треків",
                onBack = {
                    finish()
                }
            )
        )

        root.addView(
            TextView(this).apply {
                text =
                    "Немає активного імпортованого списку.\n\n" +
                        "Поверніться на головний екран і почніть з «1. Імпорт»."
                gravity =
                    Gravity.CENTER
                textSize = 15f
                setTextColor(MUTED)
                setPadding(
                    dp(24),
                    dp(40),
                    dp(24),
                    dp(40)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    private fun showListScreen() {
        currentTrackHistoryIndex = null

        reloadSnapshot()

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(AppThemeManager.palette(this@ReviewActivity).background)
            }

        root.addView(
            topBar(
                title =
                    "Перевірка треків",
                onBack = {
                    finish()
                },
                actionLabel =
                    "Проект",
                onAction = {
                    showProjectActions()
                }
            )
        )

        val summary =
            TextView(this).apply {
                text =
                    buildSummaryText()
                textSize = 13f
                setTextColor(
                    Color.rgb(
                        205,
                        207,
                        213
                    )
                )
                setPadding(
                    dp(14),
                    dp(12),
                    dp(14),
                    dp(12)
                )
                background =
                    roundedBackground(
                        color = SURFACE,
                        radiusDp = 14,
                        strokeColor = BORDER
                    )
            }

        root.addView(
            summary,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    dp(12),
                    0,
                    dp(12),
                    dp(8)
                )
            }
        )

        val projectRow =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                isBaselineAligned = false
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(12), 0, dp(12), dp(7))
            }

        val projectActions =
            listOf(
                "▣ Зберегти" to { saveCurrentProject() },
                "↗ Поділитись" to { shareCurrentProject() },
                "↻ Пошук" to { requestRepeatSearch() }
            )

        projectActions.forEachIndexed { index, action ->
            projectRow.addView(
                compactToolbarButton(action.first, action.second),
                LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1f
                ).apply {
                    if (index > 0) marginStart = dp(6)
                }
            )
        }

        root.addView(projectRow)

        val filterRow =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                isBaselineAligned = false
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(12), 0, dp(12), dp(8))
            }

        val list =
            ListView(this).apply {
                divider = null
                dividerHeight = dp(6)
                clipToPadding = false
                setPadding(dp(10), 0, dp(10), dp(14))
                setBackgroundColor(AppThemeManager.palette(this@ReviewActivity).background)
            }

        val adapter = ReviewListAdapter(snapshot.playlist.tracks)
        list.adapter = adapter

        val filters =
            listOf(
                "≡ Усі" to ReviewFilter.ALL,
                "! Перев." to ReviewFilter.REVIEW,
                "✓ Готові" to ReviewFilter.READY,
                "× Пробл." to ReviewFilter.PROBLEMS
            )

        filters.forEachIndexed { index, pair ->
            filterRow.addView(
                compactFilterButton(pair.first) {
                    adapter.setFilter(pair.second)
                },
                LinearLayout.LayoutParams(
                    0,
                    dp(44),
                    1f
                ).apply {
                    if (index > 0) marginStart = dp(5)
                }
            )
        }

        root.addView(filterRow)

        root.addView(
            list,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val canOpenDestination =
            snapshot.playlist.tracks.any { track ->
                !track.selectedVideoId.isNullOrBlank() &&
                    track.status != TrackStatus.SKIPPED
            }

        if (canOpenDestination) {
            root.addView(
                actionButton(
                    label = "Далі → Створити / додати",
                    primary = true
                ) {
                    saveSnapshot()
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(
                            EXTRA_OPEN_DESTINATION,
                            true
                        )
                    )
                    finish()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(56)
                ).apply {
                    setMargins(dp(12), 0, dp(12), dp(10))
                }
            )
        }

        list.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            adapter
                .getItem(position)
                ?.let(
                    ::showTrackScreen
                )
        }

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    private fun showTrackScreen(
        track: Track
    ) {
        currentTrackHistoryIndex =
            track.historyIndex

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(AppThemeManager.palette(this@ReviewActivity).background)
            }

        root.addView(
            topBar(
                title =
                    "${track.originalArtist} — ${track.originalTitle}",
                onBack = {
                    showListScreen()
                },
                actionLabel =
                    "Проект",
                onAction = {
                    showProjectActions()
                }
            )
        )

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(24)
                )
            }

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@ReviewActivity
                    ).apply {
                        text =
                            statusLabel(track)
                        textSize = 17f
                        setTextColor(
                            statusColor(
                                track.status
                            )
                        )
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    infoText(
                        buildString {
                            append(
                                "Оригінал:\n"
                            )
                            append(
                                track.originalArtist
                            )
                            append(" — ")
                            append(
                                track.originalTitle
                            )

                            if (
                                !track.selectedTitle
                                    .isNullOrBlank()
                            ) {
                                append(
                                    if (track.manuallySelected) {
                                        "\n\nРучний вибір:\n"
                                    } else {
                                        "\n\nЗнайдено:\n"
                                    }
                                )
                                append(
                                    track.selectedTitle
                                )

                                if (
                                    !track.selectedChannel
                                        .isNullOrBlank()
                                ) {
                                    append("\n")
                                    append(
                                        track.selectedChannel
                                    )
                                }
                            }

                            if (
                                !track.error
                                    .isNullOrBlank()
                            ) {
                                append(
                                    "\n\nПомилка:\n"
                                )
                                append(
                                    track.error
                                )
                            }
                        }
                    )
                )
            }
        )

        content.addView(
            sectionTitle("Кандидати")
        )

        if (
            track.candidates
                .isEmpty()
        ) {
            content.addView(
                card().apply {
                    addView(
                        infoText(
                            "Кандидатів немає. Можна вставити YouTube/YTM URL " +
                                "або повернутися на головний екран і запустити пошук."
                        )
                    )
                }
            )
        } else {
            track.candidates
                .take(10)
                .forEach { candidate ->
                    content.addView(
                        candidateCard(
                            track = track,
                            candidate =
                                candidate
                        )
                    )
                }
        }

        content.addView(
            sectionTitle("Ручна дія")
        )

        content.addView(
            card().apply {
                addView(
                    actionButton(
                        label =
                            "Вставити YouTube / YTM URL",
                        primary =
                            false
                    ) {
                        showManualUrlDialog(
                            track
                        )
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Пропустити цей трек",
                        primary =
                            false
                    ) {
                        skipTrack(track)
                    }
                )
            }
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    private fun candidateCard(
        track: Track,
        candidate: SearchCandidate
    ): LinearLayout {
        val isSelected =
            candidate.videoId ==
                track.selectedVideoId

        return card().apply {
            addView(
                TextView(
                    this@ReviewActivity
                ).apply {
                    text =
                        buildString {
                            if (isSelected) {
                                append("✓ ")
                            }

                            append(
                                (
                                    candidate.score *
                                        100
                                ).roundToInt()
                            )
                            append("%  ")
                            append(
                                candidate.title
                            )
                        }
                    textSize = 15f
                    setTextColor(
                        if (isSelected) {
                            Color.rgb(
                                105,
                                210,
                                135
                            )
                        } else {
                            Color.WHITE
                        }
                    )
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                }
            )

            addView(
                infoText(
                    candidate.channelTitle
                )
            )

            val row =
                LinearLayout(
                    this@ReviewActivity
                ).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                }

            row.addView(
                smallButton(
                    if (isSelected) {
                        "Вибрано"
                    } else {
                        "Використати"
                    }
                ) {
                    useCandidate(
                        track = track,
                        candidate = candidate
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    dp(50),
                    1f
                )
            )

            row.addView(
                smallButton(
                    "Відкрити YTM"
                ) {
                    openCandidateInYtm(
                        candidate.videoId
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    dp(50),
                    1f
                ).apply {
                    marginStart = dp(8)
                }
            )

            addView(row)
        }
    }

    private fun useCandidate(
        track: Track,
        candidate: SearchCandidate
    ) {
        track.selectedVideoId =
            candidate.videoId
        track.selectedTitle =
            candidate.title
        track.selectedChannel =
            candidate.channelTitle
        track.manuallySelected = true
        track.status =
            TrackStatus.MATCHED
        track.error = null

        saveSnapshot()

        toast(
            "Вибрано: ${candidate.title}"
        )

        showTrackScreen(track)
    }

    private fun skipTrack(
        track: Track
    ) {
        track.status =
            TrackStatus.SKIPPED
        track.selectedVideoId = null
        track.selectedTitle = null
        track.selectedChannel = null
        track.manuallySelected = true
        track.error = null

        saveSnapshot()

        toast(
            "Трек пропущено"
        )

        showListScreen()
    }

    private fun showManualUrlDialog(
        track: Track
    ) {
        val input =
            EditText(this).apply {
                hint =
                    "https://music.youtube.com/watch?v=…"
                setSingleLine(true)
                setPadding(
                    dp(14),
                    dp(8),
                    dp(14),
                    dp(8)
                )
            }

        UiChrome.alertBuilder(this)
            .setTitle(
                "Ручне посилання"
            )
            .setMessage(
                "YTM Importer повернеться на головний екран, " +
                    "отримає реальну назву та канал через YouTube API, " +
                    "а потім знову відкриє цей трек."
            )
            .setView(input)
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Використати"
            ) { _, _ ->
                val videoId =
                    extractVideoId(
                        input
                            .text
                            .toString()
                    )

                if (videoId == null) {
                    toast(
                        "Не бачу YouTube video ID"
                    )
                    return@setPositiveButton
                }

                val historyIndex =
                    track.historyIndex

                if (historyIndex == null) {
                    toast(
                        "Не вдалося визначити позицію треку"
                    )
                    return@setPositiveButton
                }

                setResult(
                    RESULT_OK,
                    Intent()
                        .putExtra(
                            EXTRA_MANUAL_VIDEO_ID,
                            videoId
                        )
                        .putExtra(
                            EXTRA_MANUAL_HISTORY_INDEX,
                            historyIndex
                        )
                )

                finish()
            }
            .show()
    }

    private fun extractVideoId(
        value: String
    ): String? {
        val text =
            value.trim()

        val regexes =
            listOf(
                Regex(
                    "[?&]v=([A-Za-z0-9_-]{11})"
                ),
                Regex(
                    "youtu\\.be/([A-Za-z0-9_-]{11})"
                ),
                Regex(
                    "youtube\\.com/shorts/([A-Za-z0-9_-]{11})"
                ),
                Regex(
                    "youtube\\.com/live/([A-Za-z0-9_-]{11})"
                ),
                Regex(
                    "^([A-Za-z0-9_-]{11})$"
                )
            )

        return regexes
            .firstNotNullOfOrNull {
                it.find(text)
                    ?.groupValues
                    ?.getOrNull(1)
            }
    }

    private fun openCandidateInYtm(
        videoId: String
    ) {
        val uri =
            Uri.parse(
                "https://music.youtube.com/watch?v=$videoId"
            )

        val ytmIntent =
            Intent(
                Intent.ACTION_VIEW,
                uri
            ).apply {
                setPackage(
                    "com.google.android.apps.youtube.music"
                )
            }

        val opened =
            runCatching {
                startActivity(
                    ytmIntent
                )
                true
            }.getOrDefault(false)

        if (!opened) {
            runCatching {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        uri
                    )
                )
            }.onFailure {
                toast(
                    "Не вдалося відкрити YouTube Music"
                )
            }
        }
    }

    private fun showProjectActions() {
        UiChrome.showMenuDialog(
            activity = this,
            title = "Поточний YTM Project",
            subtitle = "Збереження та обмін робочим проектом.",
            actions = listOf(
                UiChrome.MenuAction(
                    "Зберегти YTM Project"
                ) {
                    saveCurrentProject()
                },
                UiChrome.MenuAction(
                    "Поділитися YTM Project"
                ) {
                    shareCurrentProject()
                }
            )
        )
    }

    private fun currentProjectJson(): String {
        reloadSnapshot()

        return PlaylistProjectCodec
            .exportWorkingPlaylist(
                playlist =
                    snapshot.playlist,
                sourceLabel =
                    snapshot.sourceLabel,
                appVersion =
                    BuildConfig.VERSION_NAME
            )
    }

    private fun saveCurrentProject() {
        val content =
            currentProjectJson()

        val projectName =
            snapshot.playlist.name
                .trim()
                .ifBlank {
                    "YTM Project"
                }

        val suggestedFileName =
            projectFileName()

        pendingProjectExport =
            content
        pendingProjectDisplayName =
            projectName
        pendingProjectSuggestedFileName =
            suggestedFileName

        runCatching {
            SafFileSaveFlow.show(
                activity = this,
                title =
                    "Куди зберегти YTM Project?",
                suggestedFileName =
                    suggestedFileName,
                mimeType =
                    "application/json",
                requestCode =
                    saveProjectFolderRequestCode
            )
        }.onFailure { error ->
            clearPendingProjectExport()

            toast(
                "Не вдалося відкрити вибір збереження: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }
    }

    private fun shareCurrentProject() {
        val content =
            currentProjectJson()

        runCatching {
            val directory =
                File(
                    cacheDir,
                    "shared_exports"
                ).apply {
                    mkdirs()
                }

            val file =
                File(
                    directory,
                    projectFileName()
                ).apply {
                    writeText(
                        content,
                        Charsets.UTF_8
                    )
                }

            val uri =
                FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    file
                )

            val intent =
                Intent(
                    Intent.ACTION_SEND
                ).apply {
                    type = "application/json"
                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    clipData =
                        ClipData.newRawUri(
                            file.name,
                            uri
                        )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    "Поділитися YTM Project"
                )
            )
        }.onFailure { error ->
            toast(
                "Не вдалося поділитися Project: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }
    }

    private fun writePendingProjectToTree(
        treeUri: Uri
    ) {
        val content =
            pendingProjectExport
                ?: return

        val fileName =
            pendingProjectSuggestedFileName
                ?: projectFileName()

        runCatching {
            SafTreeFileWriter.writeText(
                context = this,
                treeUri = treeUri,
                preferredFileName =
                    fileName,
                mimeType =
                    "application/json",
                content = content
            )
        }.onSuccess { result ->
            showProjectSaved(
                result.fileName
            )
        }.onFailure { error ->
            toast(
                "Не вдалося зберегти Project: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }

        clearPendingProjectExport()
    }

    private fun writePendingProject(
        uri: Uri
    ) {
        val content =
            pendingProjectExport
                ?: return

        runCatching {
            contentResolver
                .openOutputStream(
                    uri,
                    "w"
                )
                ?.bufferedWriter(
                    Charsets.UTF_8
                )
                ?.use { writer ->
                    writer.write(content)
                }
                ?: error(
                    "Android не відкрив файл для запису"
                )
        }.onSuccess {
            showProjectSaved(
                queryDocumentName(uri)
                    ?: pendingProjectSuggestedFileName
            )
        }.onFailure { error ->
            toast(
                "Не вдалося зберегти Project: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }

        clearPendingProjectExport()
    }

    private fun showProjectSaved(
        savedFileName: String?
    ) {
        val projectName =
            pendingProjectDisplayName
                ?: snapshot.playlist.name
                    .trim()
                    .ifBlank {
                        "YTM Project"
                    }

        toastLong(
            buildString {
                append(
                    "Project «$projectName» збережено"
                )

                if (
                    !savedFileName.isNullOrBlank()
                ) {
                    append("\n")
                    append(savedFileName)
                }
            }
        )
    }

    private fun queryDocumentName(
        uri: Uri
    ): String? =
        runCatching {
            contentResolver
                .query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    val index =
                        cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )

                    if (
                        index >= 0 &&
                        cursor.moveToFirst()
                    ) {
                        cursor.getString(index)
                    } else {
                        null
                    }
                }
        }.getOrNull()

    private fun clearPendingProjectExport() {
        pendingProjectExport = null
        pendingProjectDisplayName = null
        pendingProjectSuggestedFileName = null
    }

    private fun projectFileName(): String {
        val safeName =
            snapshot.playlist.name
                .trim()
                .replace(
                    Regex("[\\/:*?\"<>|\\p{Cntrl}]"),
                    "_"
                )
                .replace(Regex("\\s+"), " ")
                .trim(' ', '.')
                .take(100)
                .ifBlank { "YTM Project" }

        return "$safeName.ytm.json"
    }

    private fun requestRepeatSearch() {
        UiChrome.alertBuilder(this)
            .setTitle("Повторити пошук?")
            .setMessage(
                "YTM Importer повернеться на головний екран і повторить пошук " +
                    "лише для треків, яким він справді потрібен. " +
                    "Треки з точним videoId буде збережено без нового search.list. " +
                    "Кешовані результати також не витрачають search.list quota."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Повторити"
            ) { _, _ ->
                setResult(
                    RESULT_OK,
                    Intent()
                        .putExtra(
                            EXTRA_REPEAT_SEARCH,
                            true
                        )
                )
                finish()
            }
            .show()
    }

    private fun reloadSnapshot() {
        currentPlaylistStore
            .load()
            ?.let {
                snapshot = it
            }
    }

    private fun saveSnapshot() {
        currentPlaylistStore.save(
            playlist =
                snapshot.playlist,
            sourceLabel =
                snapshot.sourceLabel,
            destinationPlaylistId =
                snapshot.destinationPlaylistId
        )
    }

    private fun findTrackByHistoryIndex(
        historyIndex: Int
    ): Track? =
        snapshot
            .playlist
            .tracks
            .firstOrNull {
                it.historyIndex ==
                    historyIndex
            }

    private fun buildSummaryText():
        String {
        val tracks =
            snapshot
                .playlist
                .tracks

        val ready =
            tracks.count {
                it.status in
                    setOf(
                        TrackStatus.MATCHED,
                        TrackStatus.ADDED
                    )
            }

        val review =
            tracks.count {
                it.status ==
                    TrackStatus.REVIEW
            }

        val problems =
            tracks.count {
                it.status in
                    setOf(
                        TrackStatus.MISSING,
                        TrackStatus.FAILED,
                        TrackStatus.SKIPPED
                    )
            }

        return (
            "${snapshot.playlist.name}\n" +
                "${tracks.size} треків • " +
                "✓ $ready • ! $review • × $problems"
            )
    }

    private fun statusLabel(
        track: Track
    ): String =
        when (track.status) {
            TrackStatus.NEW ->
                "○ Новий"

            TrackStatus.SEARCHING ->
                "… Пошук"

            TrackStatus.MATCHED ->
                "✓ Готовий"

            TrackStatus.REVIEW ->
                "! Потрібна перевірка"

            TrackStatus.MISSING ->
                "× Не знайдено"

            TrackStatus.SKIPPED ->
                "— Пропущено"

            TrackStatus.DUPLICATE ->
                "⧉ Дублікат"

            TrackStatus.PENDING ->
                "⏳ Черга"

            TrackStatus.ADDED ->
                "✓ Додано"

            TrackStatus.FAILED ->
                "× Помилка"
        }

    private fun statusColor(
        status: TrackStatus
    ): Int {
        val palette =
            AppThemeManager.palette(this)

        return when (status) {
            TrackStatus.MATCHED,
            TrackStatus.ADDED ->
                palette.success

            TrackStatus.REVIEW,
            TrackStatus.PENDING ->
                palette.warning

            TrackStatus.DUPLICATE ->
                palette.duplicate

            TrackStatus.MISSING,
            TrackStatus.FAILED ->
                palette.danger

            else ->
                palette.muted
        }
    }

    private fun topBar(
        title: String,
        onBack: () -> Unit,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            addView(
                UiChrome.backButton(
                    activity = this@ReviewActivity,
                    onClick = { onBack() }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                UiChrome.emphasizedTitle(
                    activity = this@ReviewActivity,
                    label = title.take(70),
                    textSizeSp = 19f,
                    maxLines = 2
                ).apply {
                    setPadding(
                        dp(12),
                        0,
                        dp(8),
                        0
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            if (
                !actionLabel.isNullOrBlank() &&
                onAction != null
            ) {
                addView(
                    Button(
                        this@ReviewActivity
                    ).apply {
                        text =
                            actionLabel
                        isAllCaps = false
                        textSize = 12f
                        setTextColor(
                            Color.WHITE
                        )
                        background =
                            roundedBackground(
                                color = SURFACE,
                                radiusDp = 12,
                                strokeColor = BORDER
                            )
                        setOnClickListener {
                            onAction()
                        }
                    },
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(42)
                    )
                )
            }
        }

    private fun card():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
            )
            background =
                roundedBackground(
                    color = SURFACE,
                    radiusDp = 14,
                    strokeColor = BORDER
                )
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
        }

    private fun sectionTitle(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(MUTED)
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setPadding(
                dp(4),
                dp(9),
                0,
                dp(6)
            )
        }

    private fun infoText(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(
                Color.rgb(
                    202,
                    204,
                    210
                )
            )
            setPadding(
                0,
                dp(7),
                0,
                dp(8)
            )
            setTextIsSelectable(true)
        }

    private fun compactToolbarButton(
        label: String,
        action: () -> Unit
    ): Button =
        smallButton(label, action).apply {
            maxLines = 1
            setPadding(dp(6), dp(5), dp(6), dp(5))
            UiChrome.autoSizeButton(this, minSp = 8, maxSp = 12)
        }

    private fun compactFilterButton(
        label: String,
        action: () -> Unit
    ): Button =
        smallButton(label, action).apply {
            maxLines = 1
            setPadding(dp(4), dp(4), dp(4), dp(4))
            UiChrome.autoSizeButton(this, minSp = 8, maxSp = 11)
        }

    private fun smallButton(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 11.5f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(14), dp(8), dp(14), dp(8))
            UiChrome.autoSizeButton(
                this,
                minSp = 10,
                maxSp = 13
            )
            background =
                roundedBackground(
                    color =
                        Color.rgb(
                            37,
                            39,
                            46
                        ),
                    radiusDp = 10,
                    strokeColor =
                        Color.rgb(
                            63,
                            66,
                            76
                        )
                )
            setOnClickListener {
                action()
            }
        }

    private fun actionButton(
        label: String,
        primary: Boolean,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(16), dp(10), dp(16), dp(10))
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 14
            )
            background =
                roundedBackground(
                    color =
                        if (primary) {
                            Color.rgb(
                                196,
                                0,
                                42
                            )
                        } else {
                            Color.rgb(
                                37,
                                39,
                                46
                            )
                        },
                    radiusDp = 11,
                    strokeColor =
                        if (primary) {
                            null
                        } else {
                            Color.rgb(
                                63,
                                66,
                                76
                            )
                        }
                )
            setOnClickListener {
                action()
            }
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    bottomMargin = dp(7)
                }
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): android.graphics.drawable.Drawable {
        val palette =
            AppThemeManager.palette(this)

        val mappedFill =
            when (color) {
                Color.rgb(15, 16, 19) ->
                    palette.background

                Color.rgb(25, 27, 32) ->
                    palette.surface

                Color.rgb(31, 33, 39),
                Color.rgb(37, 39, 46) ->
                    palette.surfaceAlt

                Color.rgb(196, 0, 42) ->
                    palette.accentFill

                Color.rgb(39, 25, 27),
                Color.rgb(31, 29, 24) ->
                    palette.surface

                else ->
                    color
            }

        val accentOverride =
            when (strokeColor) {
                Color.rgb(95, 48, 52) ->
                    palette.danger

                Color.rgb(83, 68, 37) ->
                    palette.warning

                else ->
                    null
            }

        val useAccentStroke =
            radiusDp >= 14 ||
                color == Color.rgb(196, 0, 42) ||
                accentOverride != null

        return AppThemeManager.surfaceDrawable(
            context = this,
            fill = mappedFill,
            radiusDp = radiusDp,
            accentStroke = useAccentStroke,
            accentOverride = accentOverride
        )
    }

    private fun toast(
        message: String
    ) {
        Toast
            .makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            )
            .show()
    }

    private fun toastLong(
        message: String
    ) {
        Toast
            .makeText(
                this,
                message,
                Toast.LENGTH_LONG
            )
            .show()
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    private inner class ReviewListAdapter(
        tracks: List<Track>
    ) : BaseAdapter() {
        private val allTracks =
            tracks.toList()

        private val visibleTracks =
            tracks.toMutableList()

        private var filter =
            ReviewFilter.ALL

        fun setFilter(
            value: ReviewFilter
        ) {
            filter = value
            applyFilter()
        }

        private fun applyFilter() {
            visibleTracks.clear()

            visibleTracks.addAll(
                allTracks.filter { track ->
                    when (filter) {
                        ReviewFilter.ALL ->
                            true

                        ReviewFilter.REVIEW ->
                            track.status ==
                                TrackStatus.REVIEW

                        ReviewFilter.READY ->
                            track.status in
                                setOf(
                                    TrackStatus.MATCHED,
                                    TrackStatus.ADDED
                                )

                        ReviewFilter.PROBLEMS ->
                            track.status in
                                setOf(
                                    TrackStatus.MISSING,
                                    TrackStatus.FAILED,
                                    TrackStatus.SKIPPED,
                                    TrackStatus.DUPLICATE,
                                    TrackStatus.PENDING
                                )
                    }
                }
            )

            notifyDataSetChanged()
        }

        override fun getCount():
            Int =
            visibleTracks.size

        override fun getItem(
            position: Int
        ): Track? =
            visibleTracks.getOrNull(
                position
            )

        override fun getItemId(
            position: Int
        ): Long =
            position.toLong()

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val track =
                visibleTracks[position]

            val row =
                convertView as?
                    LinearLayout
                    ?: createRow()

            val title =
                row.getChildAt(0)
                    as TextView

            val meta =
                row.getChildAt(1)
                    as TextView

            title.text =
                "${statusGlyph(track.status)} " +
                    "${track.originalArtist} — " +
                    track.originalTitle

            title.setTextColor(
                statusColor(
                    track.status
                )
            )

            meta.text =
                when {
                    !track.selectedTitle
                        .isNullOrBlank() ->
                        buildString {
                            if (track.manuallySelected) {
                                append("Ручний вибір: ")
                            } else {
                                append("Знайдено: ")
                            }

                            append(
                                track.selectedTitle
                            )

                            if (
                                !track.selectedChannel
                                    .isNullOrBlank()
                            ) {
                                append(" • ")
                                append(
                                    track.selectedChannel
                                )
                            }
                        }

                    !track.error
                        .isNullOrBlank() ->
                        track.error

                    else ->
                        "Кандидат ще не вибрано"
                }

            return row
        }

        private fun createRow():
            LinearLayout =
            LinearLayout(
                this@ReviewActivity
            ).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(14),
                    dp(12),
                    dp(14),
                    dp(12)
                )
                background =
                    roundedBackground(
                        color = SURFACE,
                        radiusDp = 13,
                        strokeColor = BORDER
                    )
                layoutParams =
                    AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                addView(
                    TextView(
                        this@ReviewActivity
                    ).apply {
                        textSize = 15f
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                        maxLines = 2
                    }
                )

                addView(
                    TextView(
                        this@ReviewActivity
                    ).apply {
                        textSize = 12.5f
                        setTextColor(MUTED)
                        setPadding(
                            0,
                            dp(5),
                            0,
                            0
                        )
                        maxLines = 2
                    }
                )
            }

        private fun statusGlyph(
            status: TrackStatus
        ): String =
            when (status) {
                TrackStatus.NEW ->
                    "○"

                TrackStatus.SEARCHING ->
                    "…"

                TrackStatus.MATCHED,
                TrackStatus.ADDED ->
                    "✓"

                TrackStatus.REVIEW ->
                    "!"

                TrackStatus.DUPLICATE ->
                    "⧉"

                TrackStatus.PENDING ->
                    "⏳"

                TrackStatus.SKIPPED ->
                    "—"

                TrackStatus.MISSING,
                TrackStatus.FAILED ->
                    "×"
            }
    }

    private enum class ReviewFilter {
        ALL,
        REVIEW,
        READY,
        PROBLEMS
    }

    companion object {
        const val EXTRA_FOCUS_HISTORY_INDEX =
            "review_focus_history_index"

        const val EXTRA_REPEAT_SEARCH =
            "review_repeat_search"

        const val EXTRA_OPEN_DESTINATION =
            "review_open_destination"

        const val EXTRA_MANUAL_VIDEO_ID =
            "review_manual_video_id"

        const val EXTRA_MANUAL_HISTORY_INDEX =
            "review_manual_history_index"

        private const val KEY_TRACK_HISTORY_INDEX =
            "review_current_track_history_index"

        private val BACKGROUND =
            Color.rgb(
                15,
                16,
                19
            )

        private val SURFACE =
            Color.rgb(
                25,
                27,
                32
            )

        private val BORDER =
            Color.rgb(
                48,
                51,
                59
            )

        private val MUTED =
            Color.rgb(
                165,
                167,
                173
            )
    }
}
