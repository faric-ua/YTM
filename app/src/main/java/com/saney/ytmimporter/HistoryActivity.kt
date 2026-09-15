package com.saney.ytmimporter
import com.saney.ytmimporter.ui.UiChrome

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : Activity() {
    private lateinit var historyStore: HistoryStore
    private lateinit var pendingJobStore: PendingJobStore

    private var currentEntryId: String? = null
    private var pendingExportContent: String? = null
    private var pendingExportSuccessMessage: String? = null

    private val saveExportRequestCode = 3201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyStore = HistoryStore(this)
        pendingJobStore = PendingJobStore(this)

        val restoredEntryId =
            savedInstanceState?.getString(KEY_CURRENT_ENTRY_ID)

        if (!restoredEntryId.isNullOrBlank()) {
            val entry = historyStore.get(restoredEntryId)

            if (entry != null) {
                showDetailScreen(entry)
                return
            }
        }

        showListScreen()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(
            KEY_CURRENT_ENTRY_ID,
            currentEntryId
        )
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentEntryId != null) {
            showListScreen()
        } else {
            super.onBackPressed()
        }
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

        if (
            requestCode == saveExportRequestCode &&
            resultCode == RESULT_OK
        ) {
            val uri = data?.data ?: return
            writePendingExport(uri)
        }
    }

    private fun showListScreen() {
        currentEntryId = null

        val root = baseRoot()

        root.addView(
            topBar(
                title = "Історія",
                onBack = {
                    finish()
                },
                actionLabel = "Очистити",
                onAction = {
                    confirmClearHistory()
                }
            )
        )

        root.addView(
            TextView(this).apply {
                text =
                    "Створені плейлисти, спроби імпорту та " +
                        "відновлювані YTM Project."
                textSize = 13f
                setTextColor(MUTED)
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(10)
                )
            }
        )

        val search = EditText(this).apply {
            hint = "Пошук за назвою, джерелом або каналом"
            setSingleLine(true)
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(
                Color.rgb(
                    120,
                    123,
                    130
                )
            )
            setPadding(
                dp(14),
                0,
                dp(14),
                0
            )
            background =
                roundedBackground(
                    color = SURFACE,
                    radiusDp = 12,
                    strokeColor = BORDER
                )
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                setMargins(
                    dp(12),
                    0,
                    dp(12),
                    dp(10)
                )
            }
        )

        val countText = TextView(this).apply {
            textSize = 12f
            setTextColor(MUTED)
            setPadding(
                dp(18),
                0,
                dp(18),
                dp(6)
            )
        }
        root.addView(countText)

        val entries = historyStore.getAll()

        if (entries.isEmpty()) {
            countText.text = "Записів немає"

            root.addView(
                TextView(this).apply {
                    text =
                        "Історія поки порожня.\n\n" +
                            "Після створення або доповнення плейлиста " +
                            "запис з'явиться тут."
                    gravity = Gravity.CENTER
                    textSize = 15f
                    setTextColor(MUTED)
                    setPadding(
                        dp(24),
                        dp(32),
                        dp(24),
                        dp(32)
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
            return
        }

        val list = ListView(this).apply {
            divider = null
            dividerHeight = dp(6)
            clipToPadding = false
            setPadding(
                dp(10),
                0,
                dp(10),
                dp(14)
            )
            setBackgroundColor(BACKGROUND)
        }

        root.addView(
            list,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)

        val adapter =
            HistoryListAdapter(entries)

        list.adapter = adapter
        countText.text = "${entries.size} записів"

        list.setOnItemClickListener { _, _, position, _ ->
            adapter
                .getItem(position)
                ?.let(::showDetailScreen)
        }

        search.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val query =
                        s
                            ?.toString()
                            .orEmpty()
                            .trim()

                    adapter.filter(query)

                    countText.text =
                        if (query.isBlank()) {
                            "${adapter.count} записів"
                        } else {
                            "Знайдено: ${adapter.count}"
                        }
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    private fun showDetailScreen(
        entry: HistoryEntry
    ) {
        currentEntryId = entry.id

        val root = baseRoot()

        root.addView(
            topBar(
                title = entry.playlistName,
                onBack = {
                    showListScreen()
                },
                actionLabel = "Дії",
                onAction = {
                    showActions(entry)
                }
            )
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(12),
                0,
                dp(12),
                dp(20)
            )
        }

        val status = effectiveHistoryStatus(entry)

        content.addView(
            card().apply {
                addView(
                    TextView(this@HistoryActivity).apply {
                        text =
                            "${historyStatusIcon(status)} " +
                                historyStatusLabel(status)
                        textSize = 18f
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                        setTextColor(
                            historyStatusColor(status)
                        )
                    }
                )

                addView(
                    metaText(
                        "Дата: ${formatHistoryDate(entry.updatedAt)}\n" +
                            "Джерело: ${entry.sourceLabel}\n" +
                            "Тип: ${destinationLabel(entry.destination)}\n" +
                            "Приватність: ${privacyLabel(entry.privacyStatus)}"
                    )
                )
            }
        )

        content.addView(
            sectionTitle("Результат")
        )

        content.addView(
            card().apply {
                addView(
                    statLine(
                        "Додано",
                        "${entry.addedCount}/${entry.writeTargetCount}"
                    )
                )

                if (entry.duplicateCount > 0) {
                    addView(
                        statLine(
                            "Дублікати",
                            entry.duplicateCount.toString()
                        )
                    )
                }

                if (entry.skippedCount > 0) {
                    addView(
                        statLine(
                            "Пропущено",
                            entry.skippedCount.toString()
                        )
                    )
                }

                if (entry.pendingCount > 0) {
                    addView(
                        statLine(
                            "Очікує",
                            entry.pendingCount.toString()
                        )
                    )
                }

                if (entry.failedCount > 0) {
                    addView(
                        statLine(
                            "Помилки",
                            entry.failedCount.toString()
                        )
                    )
                }

                if (entry.missingCount > 0) {
                    addView(
                        statLine(
                            "Не знайдено",
                            entry.missingCount.toString()
                        )
                    )
                }
            }
        )

        val problemTracks =
            entry.tracks.filter(
                ::isHistoryProblemTrack
            )

        if (problemTracks.isNotEmpty()) {
            content.addView(
                sectionTitle(
                    "Заміни та проблеми"
                )
            )

            content.addView(
                card().apply {
                    problemTracks
                        .take(10)
                        .forEachIndexed { index, track ->
                            addView(
                                TextView(
                                    this@HistoryActivity
                                ).apply {
                                    text =
                                        "${index + 1}. " +
                                            "${track.originalArtist} — " +
                                            "${track.originalTitle}\n" +
                                            "→ ${historyReplacementLabel(track)}"
                                    textSize = 13f
                                    setTextColor(Color.WHITE)
                                    setPadding(
                                        0,
                                        if (index == 0) 0 else dp(8),
                                        0,
                                        dp(4)
                                    )
                                }
                            )
                        }

                    if (problemTracks.size > 10) {
                        addView(
                            metaText(
                                "Ще ${problemTracks.size - 10} — " +
                                    "повний список доступний у «Дії»."
                            )
                        )
                    }
                }
            )
        }

        if (
            !entry.youtubeChannelTitle.isNullOrBlank() ||
            !entry.googleEmail.isNullOrBlank()
        ) {
            content.addView(
                sectionTitle("Акаунт")
            )

            content.addView(
                card().apply {
                    addView(
                        metaText(
                            buildString {
                                if (
                                    !entry.youtubeChannelTitle
                                        .isNullOrBlank()
                                ) {
                                    append(
                                        "YouTube/YTM: " +
                                            entry.youtubeChannelTitle
                                    )
                                }

                                if (
                                    !entry.googleEmail
                                        .isNullOrBlank()
                                ) {
                                    if (isNotEmpty()) {
                                        append("\n")
                                    }

                                    append(
                                        "Google: " +
                                            maskEmail(
                                                entry.googleEmail
                                            )
                                    )
                                }
                            }
                        )
                    )
                }
            )
        }

        if (!entry.lastError.isNullOrBlank()) {
            content.addView(
                sectionTitle("Остання помилка")
            )

            content.addView(
                card().apply {
                    addView(
                        TextView(
                            this@HistoryActivity
                        ).apply {
                            text = entry.lastError
                            textSize = 13f
                            setTextColor(
                                Color.rgb(
                                    255,
                                    150,
                                    150
                                )
                            )
                            setTextIsSelectable(true)
                        }
                    )
                }
            )
        }

        content.addView(
            sectionTitle("Швидкі дії")
        )

        val quickActions = card()

        if (!entry.playlistId.isNullOrBlank()) {
            quickActions.addView(
                actionButton(
                    "Відкрити плейлист у YTM"
                ) {
                    openPlaylistInYtm(
                        entry.playlistId
                    )
                }
            )
        }

        quickActions.addView(
            actionButton(
                "Зберегти YTM Project"
            ) {
                confirmProjectScope(
                    entry = entry,
                    actionLabel = "Зберегти"
                ) {
                    saveHistoryProject(entry)
                }
            }
        )

        quickActions.addView(
            actionButton(
                "Поділитися YTM Project"
            ) {
                confirmProjectScope(
                    entry = entry,
                    actionLabel = "Поділитися"
                ) {
                    shareHistoryProject(entry)
                }
            }
        )

        content.addView(quickActions)

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

    private fun showActions(
        entry: HistoryEntry
    ) {
        val actions =
            mutableListOf<UiChrome.MenuAction>()

        if (!entry.playlistId.isNullOrBlank()) {
            actions +=
                UiChrome.MenuAction(
                    "Копіювати посилання на плейлист"
                ) {
                    entry.playlistId
                        ?.let { id ->
                            copyText(
                                label = "YTM playlist",
                                text = playlistUrl(id),
                                message = "Посилання скопійовано"
                            )
                        }
                }
        }

        actions +=
            UiChrome.MenuAction(
                "Копіювати підсумок"
            ) {
                copyText(
                    label = "YTM Importer history summary",
                    text = buildHistorySummary(entry),
                    message = "Підсумок скопійовано"
                )
            }

        if (buildHistoryProblemLog(entry) != null) {
            actions +=
                UiChrome.MenuAction(
                    "Переглянути журнал проблем"
                ) {
                    showProblemLog(entry)
                }

            actions +=
                UiChrome.MenuAction(
                    "Копіювати журнал проблем"
                ) {
                    buildHistoryProblemLog(entry)
                        ?.let { text ->
                            copyText(
                                label = "YTM Importer history problems",
                                text = text,
                                message = "Журнал проблем скопійовано"
                            )
                        }
                }
        }

        actions +=
            UiChrome.MenuAction(
                "Видалити локальний запис"
            ) {
                confirmDeleteHistoryEntry(entry)
            }

        UiChrome.showMenuDialog(
            activity = this,
            title = "Дії",
            subtitle = "Дії з локальним записом історії.",
            actions = actions
        )
    }

    private fun showProblemLog(
        entry: HistoryEntry
    ) {
        val text =
            buildHistoryProblemLog(entry)
                ?: return toast(
                    "У цьому записі немає проблемних треків"
                )

        UiChrome.alertBuilder(this)
            .setTitle("Заміни та проблеми")
            .setMessage(text)
            .setNegativeButton(
                "Закрити",
                null
            )
            .setPositiveButton(
                "Копіювати"
            ) { _, _ ->
                copyText(
                    label = "YTM Importer history problems",
                    text = text,
                    message = "Журнал проблем скопійовано"
                )
            }
            .show()
    }

    private fun confirmProjectScope(
        entry: HistoryEntry,
        actionLabel: String,
        after: () -> Unit
    ) {
        if (
            entry.destination ==
                PendingDestination.NEW_PLAYLIST
        ) {
            after()
            return
        }

        UiChrome.alertBuilder(this)
            .setTitle(
                "$actionLabel YTM Project?"
            )
            .setMessage(
                "Цей History-запис стосується додавання до вже " +
                    "існуючого плейлиста.\n\n" +
                    "Project міститиме тільки треки цієї операції " +
                    "імпорту, а не повний старий плейлист у YTM."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                actionLabel
            ) { _, _ ->
                after()
            }
            .show()
    }

    private fun saveHistoryProject(
        entry: HistoryEntry
    ) {
        val content =
            PlaylistProjectCodec.exportHistoryEntry(
                entry = entry,
                appVersion = BuildConfig.VERSION_NAME
            )

        pendingExportContent = content
        pendingExportSuccessMessage = "YTM Project збережено"

        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type = "application/json"
                putExtra(
                    Intent.EXTRA_TITLE,
                    historyProjectFileName(entry)
                )
            }

        runCatching {
            startActivityForResult(
                intent,
                saveExportRequestCode
            )
        }.onFailure { error ->
            pendingExportContent = null
            pendingExportSuccessMessage = null

            toast(
                "Не вдалося відкрити вибір файлу: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }
    }

    private fun shareHistoryProject(
        entry: HistoryEntry
    ) {
        val content =
            PlaylistProjectCodec.exportHistoryEntry(
                entry = entry,
                appVersion = BuildConfig.VERSION_NAME
            )

        shareTextFile(
            fileName = historyProjectFileName(entry),
            mimeType = "application/json",
            content = content,
            chooserTitle = "Поділитися YTM Project"
        )
    }

    private fun writePendingExport(
        uri: Uri
    ) {
        val content =
            pendingExportContent
                ?: return

        runCatching {
            contentResolver
                .openOutputStream(uri)
                ?.bufferedWriter(
                    Charsets.UTF_8
                )
                ?.use {
                    it.write(content)
                }
                ?: error(
                    "Не вдалося відкрити файл для запису"
                )
        }.onSuccess {
            toast(
                pendingExportSuccessMessage
                    ?: "Файл збережено"
            )
        }.onFailure { error ->
            toast(
                "Не вдалося записати файл: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }

        pendingExportContent = null
        pendingExportSuccessMessage = null
    }

    private fun shareTextFile(
        fileName: String,
        mimeType: String,
        content: String,
        chooserTitle: String
    ) {
        runCatching {
            val directory =
                File(
                    cacheDir,
                    "shared_exports"
                ).apply {
                    mkdirs()
                }

            directory
                .listFiles()
                ?.filter {
                    System.currentTimeMillis() -
                        it.lastModified() >
                        24L * 60L * 60L * 1000L
                }
                ?.forEach(File::delete)

            val file =
                File(
                    directory,
                    fileName
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
                    type = mimeType
                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    clipData =
                        ClipData.newRawUri(
                            fileName,
                            uri
                        )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    chooserTitle
                )
            )
        }.onFailure { error ->
            toast(
                "Не вдалося поділитися файлом: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }
    }

    private fun openPlaylistInYtm(
        playlistId: String
    ) {
        val uri =
            Uri.parse(
                playlistUrl(playlistId)
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

        val openedInYtm =
            runCatching {
                startActivity(ytmIntent)
                true
            }.getOrDefault(false)

        if (!openedInYtm) {
            runCatching {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        uri
                    )
                )
            }.onFailure {
                toast(
                    "Не вдалося відкрити плейлист"
                )
            }
        }
    }

    private fun playlistUrl(
        playlistId: String
    ): String =
        "https://music.youtube.com/playlist?list=$playlistId"

    private fun confirmDeleteHistoryEntry(
        entry: HistoryEntry
    ) {
        UiChrome.alertBuilder(this)
            .setTitle(
                "Видалити запис?"
            )
            .setMessage(
                "Буде видалено тільки локальний History-запис " +
                    "«${entry.playlistName}».\n\n" +
                    "Плейлист у YouTube/YTM не зміниться."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Видалити"
            ) { _, _ ->
                historyStore.remove(
                    entry.id
                )
                toast(
                    "Запис видалено"
                )
                showListScreen()
            }
            .show()
    }

    private fun confirmClearHistory() {
        val entries =
            historyStore.getAll()

        if (entries.isEmpty()) {
            toast(
                "Історія вже порожня"
            )
            return
        }

        UiChrome.alertBuilder(this)
            .setTitle(
                "Очистити всю історію?"
            )
            .setMessage(
                "Буде видалено ${entries.size} локальних записів.\n\n" +
                    "Плейлисти YouTube/YTM і Pending Queue не змінюються."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Очистити"
            ) { _, _ ->
                historyStore.clear()
                toast(
                    "Історію очищено"
                )
                showListScreen()
            }
            .show()
    }

    private fun buildHistorySummary(
        entry: HistoryEntry
    ): String =
        buildString {
            val status =
                effectiveHistoryStatus(entry)

            append(
                "YTM Importer — історія\n"
            )
            append(
                "Плейлист: ${entry.playlistName}\n"
            )
            append(
                "Статус: ${historyStatusLabel(status)}\n"
            )
            append(
                "Дата: ${formatHistoryDate(entry.updatedAt)}\n"
            )
            append(
                "Джерело: ${entry.sourceLabel}\n"
            )
            append(
                "Додано: ${entry.addedCount}/" +
                    "${entry.writeTargetCount}\n"
            )
            append(
                "Помилок: ${entry.failedCount}\n"
            )
            append(
                "Очікує: ${entry.pendingCount}\n"
            )
            append(
                "Пропущено: ${entry.skippedCount}\n"
            )
            append(
                "Дублікатів: ${entry.duplicateCount}\n"
            )
            append(
                "Не знайдено: ${entry.missingCount}\n"
            )

            if (
                !entry.playlistId
                    .isNullOrBlank()
            ) {
                append(
                    "YTM: " +
                        playlistUrl(
                            entry.playlistId
                        ) +
                        "\n"
                )
            }

            if (
                !entry.lastError
                    .isNullOrBlank()
            ) {
                append(
                    "Остання помилка: " +
                        entry.lastError +
                        "\n"
                )
            }
        }.trimEnd()

    private fun buildHistoryProblemLog(
        entry: HistoryEntry
    ): String? {
        val tracks =
            entry.tracks.filter(
                ::isHistoryProblemTrack
            )

        if (tracks.isEmpty()) {
            return null
        }

        return buildString {
            append(
                "YTM Importer — заміни та проблеми\n"
            )
            append(
                "Плейлист: ${entry.playlistName}\n\n"
            )

            tracks.forEachIndexed {
                    index,
                    track ->

                append(index + 1)
                append(". ")
                append(
                    track.originalArtist
                )
                append(" — ")
                append(
                    track.originalTitle
                )
                append("\n→ ")
                append(
                    historyReplacementLabel(
                        track
                    )
                )

                if (
                    !track.selectedChannel
                        .isNullOrBlank()
                ) {
                    append(
                        "\nКанал: " +
                            track.selectedChannel
                    )
                }

                if (
                    !track.videoId
                        .isNullOrBlank()
                ) {
                    append(
                        "\nYTM: " +
                            "https://music.youtube.com/" +
                            "watch?v=${track.videoId}"
                    )
                }

                if (
                    !track.error
                        .isNullOrBlank()
                ) {
                    append(
                        "\nПомилка: " +
                            track.error
                    )
                }

                if (
                    index != tracks.lastIndex
                ) {
                    append("\n\n")
                }
            }
        }
    }

    private fun isHistoryProblemTrack(
        track: HistoryTrack
    ): Boolean =
        track.manuallySelected ||
            track.status ==
                TrackStatus.SKIPPED.name ||
            track.status ==
                TrackStatus.DUPLICATE.name ||
            track.status ==
                TrackStatus.MISSING.name ||
            track.status ==
                TrackStatus.PENDING.name ||
            track.status ==
                TrackStatus.FAILED.name

    private fun historyReplacementLabel(
        track: HistoryTrack
    ): String =
        when {
            track.status ==
                TrackStatus.SKIPPED.name ->
                "[пропущено]"

            track.status ==
                TrackStatus.DUPLICATE.name ->
                "[дублікат — уже є у плейлисті]"

            track.status ==
                TrackStatus.MISSING.name ->
                "[не знайдено]"

            track.status ==
                TrackStatus.PENDING.name ->
                "[очікує в черзі]"

            track.status ==
                TrackStatus.FAILED.name &&
                track.selectedTitle
                    .isNullOrBlank() ->
                "[помилка]"

            track.selectedTitle ==
                "Ручне посилання" ->
                "[ручне YouTube/YTM посилання]"

            !track.selectedTitle
                .isNullOrBlank() ->
                track.selectedTitle

            else ->
                "[без заміни]"
        }

    private fun effectiveHistoryStatus(
        entry: HistoryEntry
    ): HistoryStatus {
        if (
            entry.status ==
                HistoryStatus.RUNNING &&
            pendingJobStore
                .getAll()
                .any {
                    it.id == entry.id
                }
        ) {
            return HistoryStatus.PARTIAL
        }

        return entry.status
    }

    private fun historyStatusLabel(
        status: HistoryStatus
    ): String =
        when (status) {
            HistoryStatus.RUNNING ->
                "Виконується"

            HistoryStatus.COMPLETED ->
                "Завершено"

            HistoryStatus.PARTIAL ->
                "Частково / очікує продовження"

            HistoryStatus.PENDING_QUOTA ->
                "Очікує квоти"

            HistoryStatus.FAILED ->
                "Помилка"
        }

    private fun historyStatusIcon(
        status: HistoryStatus
    ): String =
        when (status) {
            HistoryStatus.RUNNING -> "▶"
            HistoryStatus.COMPLETED -> "✓"
            HistoryStatus.PARTIAL -> "◐"
            HistoryStatus.PENDING_QUOTA -> "⏳"
            HistoryStatus.FAILED -> "×"
        }

    private fun historyStatusColor(
        status: HistoryStatus
    ): Int =
        when (status) {
            HistoryStatus.COMPLETED ->
                Color.rgb(
                    105,
                    210,
                    135
                )

            HistoryStatus.RUNNING,
            HistoryStatus.PARTIAL,
            HistoryStatus.PENDING_QUOTA ->
                Color.rgb(
                    255,
                    195,
                    80
                )

            HistoryStatus.FAILED ->
                Color.rgb(
                    255,
                    105,
                    105
                )
        }

    private fun formatHistoryDate(
        timestamp: Long
    ): String =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(
            Date(timestamp)
        )

    private fun destinationLabel(
        destination: PendingDestination
    ): String =
        when (destination) {
            PendingDestination.NEW_PLAYLIST ->
                "Новий плейлист"

            PendingDestination.EXISTING_PLAYLIST ->
                "Існуючий плейлист"
        }

    private fun privacyLabel(
        value: String
    ): String =
        when (value) {
            "public" ->
                "Публічний"

            "unlisted" ->
                "За посиланням"

            else ->
                "Приватний"
        }

    private fun historyProjectFileName(
        entry: HistoryEntry
    ): String {
        val safeName =
            entry.playlistName
                .replace(
                    Regex(
                        "[^\\p{L}\\p{N}._-]+"
                    ),
                    "_"
                )
                .trim('_')
                .take(60)
                .ifBlank {
                    "playlist"
                }

        return "YTM_Project_${safeName}_" +
            "${exportTimestamp()}.ytm.json"
    }

    private fun exportTimestamp(): String =
        SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(
            Date()
        )

    private fun maskEmail(
        email: String?
    ): String {
        val value =
            email
                ?.trim()
                .orEmpty()

        if (value.isBlank()) {
            return "—"
        }

        val at =
            value.indexOf('@')

        if (at <= 1) {
            return "•••"
        }

        return value.take(1) +
            "•••" +
            value.substring(at)
    }

    private fun copyText(
        label: String,
        text: String,
        message: String
    ) {
        val clipboard =
            getSystemService(
                CLIPBOARD_SERVICE
            ) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                label,
                text
            )
        )

        toast(message)
    }

    private fun baseRoot(): LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setBackgroundColor(
                BACKGROUND
            )
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
                Button(
                    this@HistoryActivity
                ).apply {
                    text = "‹"
                    isAllCaps = false
                    textSize = 26f
                    setTextColor(Color.WHITE)
                    setPadding(
                        0,
                        0,
                        0,
                        dp(2)
                    )
                    background =
                        roundedBackground(
                            color = SURFACE,
                            radiusDp = 12,
                            strokeColor = BORDER
                        )
                    setOnClickListener {
                        onBack()
                    }
                },
                LinearLayout.LayoutParams(
                    dp(46),
                    dp(46)
                )
            )

            addView(
                TextView(
                    this@HistoryActivity
                ).apply {
                    text =
                        title.take(50)
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setPadding(
                        dp(12),
                        0,
                        dp(8),
                        0
                    )
                    maxLines = 2
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
                        this@HistoryActivity
                    ).apply {
                        text = actionLabel
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

    private fun card(): LinearLayout =
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
                dp(8),
                0,
                dp(6)
            )
        }

    private fun metaText(
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
                dp(8),
                0,
                0
            )
            setTextIsSelectable(true)
        }

    private fun statLine(
        label: String,
        value: String
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                0,
                dp(3),
                0,
                dp(3)
            )

            addView(
                TextView(
                    this@HistoryActivity
                ).apply {
                    text = label
                    textSize = 13.5f
                    setTextColor(MUTED)
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(
                    this@HistoryActivity
                ).apply {
                    text = value
                    textSize = 14f
                    setTextColor(Color.WHITE)
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                }
            )
        }

    private fun actionButton(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(16), dp(11), dp(16), dp(11))
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 14
            )
            background =
                roundedBackground(
                    color =
                        Color.rgb(
                            37,
                            39,
                            46
                        ),
                    radiusDp = 11,
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

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            minimumHeight = dp(58)
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): GradientDrawable =
        GradientDrawable().apply {
            shape =
                GradientDrawable.RECTANGLE
            cornerRadius =
                dp(radiusDp).toFloat()
            setColor(color)

            if (strokeColor != null) {
                setStroke(
                    dp(1),
                    strokeColor
                )
            }
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

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources.displayMetrics.density
        ).toInt()

    private inner class HistoryListAdapter(
        entries: List<HistoryEntry>
    ) : BaseAdapter() {
        private val allEntries =
            entries.toList()

        private val visibleEntries =
            entries.toMutableList()

        fun filter(
            query: String
        ) {
            visibleEntries.clear()

            if (query.isBlank()) {
                visibleEntries.addAll(
                    allEntries
                )
            } else {
                val normalized =
                    query.lowercase(
                        Locale.getDefault()
                    )

                visibleEntries.addAll(
                    allEntries.filter { entry ->
                        entry.playlistName
                            .lowercase(
                                Locale.getDefault()
                            )
                            .contains(
                                normalized
                            ) ||
                            entry.sourceLabel
                                .lowercase(
                                    Locale.getDefault()
                                )
                                .contains(
                                    normalized
                                ) ||
                            entry.youtubeChannelTitle
                                .orEmpty()
                                .lowercase(
                                    Locale.getDefault()
                                )
                                .contains(
                                    normalized
                                )
                    }
                )
            }

            notifyDataSetChanged()
        }

        override fun getCount(): Int =
            visibleEntries.size

        override fun getItem(
            position: Int
        ): HistoryEntry? =
            visibleEntries.getOrNull(
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
            val entry =
                visibleEntries[position]

            val row =
                convertView as? LinearLayout
                    ?: createRow()

            val title =
                row.getChildAt(0) as TextView

            val meta =
                row.getChildAt(1) as TextView

            val status =
                effectiveHistoryStatus(
                    entry
                )

            title.text =
                "${historyStatusIcon(status)} " +
                    entry.playlistName

            title.setTextColor(
                historyStatusColor(
                    status
                )
            )

            meta.text =
                buildString {
                    append(
                        formatHistoryDate(
                            entry.updatedAt
                        )
                    )
                    append(" • ")
                    append(
                        historyStatusLabel(
                            status
                        )
                    )
                    append("\n")
                    append(
                        "Додано " +
                            "${entry.addedCount}/" +
                            "${entry.writeTargetCount}"
                    )

                    if (
                        entry.pendingCount > 0
                    ) {
                        append(
                            " • Очікує " +
                                entry.pendingCount
                        )
                    }

                    if (
                        entry.failedCount > 0
                    ) {
                        append(
                            " • Помилок " +
                                entry.failedCount
                        )
                    }

                    if (
                        entry.duplicateCount > 0
                    ) {
                        append(
                            " • Дублікати " +
                                entry.duplicateCount
                        )
                    }
                }

            return row
        }

        private fun createRow(): LinearLayout =
            LinearLayout(
                this@HistoryActivity
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
                        this@HistoryActivity
                    ).apply {
                        textSize = 15.5f
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                        maxLines = 2
                    }
                )

                addView(
                    TextView(
                        this@HistoryActivity
                    ).apply {
                        textSize = 12.5f
                        setTextColor(MUTED)
                        setPadding(
                            0,
                            dp(5),
                            0,
                            0
                        )
                    }
                )
            }
    }

    companion object {
        private const val KEY_CURRENT_ENTRY_ID =
            "current_history_entry_id"

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
