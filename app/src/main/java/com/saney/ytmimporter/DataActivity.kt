package com.saney.ytmimporter

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.LocalBackupManager
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.youtube.SearchCache
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataActivity : Activity() {
    private lateinit var historyStore: HistoryStore
    private lateinit var pendingJobStore: PendingJobStore
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var searchCache: SearchCache
    private lateinit var localBackupManager: LocalBackupManager

    private lateinit var summaryText: TextView
    private lateinit var rollbackButton: Button

    private var pendingExportContent: String? = null
    private var pendingExportSuccessMessage: String? = null

    private val saveExportRequestCode = 4201
    private val restoreBackupRequestCode = 4202

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyStore = HistoryStore(this)
        pendingJobStore = PendingJobStore(this)
        quotaTracker = QuotaTracker(this)
        searchCache = SearchCache(this)
        localBackupManager = LocalBackupManager(this)

        buildUi()
    }

    override fun onResume() {
        super.onResume()

        if (::summaryText.isInitialized) {
            refreshSummary()
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

        if (resultCode != RESULT_OK) {
            return
        }

        when (requestCode) {
            saveExportRequestCode -> {
                val uri = data?.data ?: return
                writePendingExport(uri)
            }

            restoreBackupRequestCode -> {
                val uri = data?.data ?: return
                prepareRestoreBackup(uri)
            }
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BACKGROUND)
        }

        root.addView(
            topBar(
                title = "Дані та резервні копії",
                onBack = {
                    finish()
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
                dp(24)
            )
        }

        summaryText = TextView(this).apply {
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
        }

        content.addView(summaryText)

        content.addView(
            sectionTitle("Backup та Restore")
        )

        content.addView(
            actionCard(
                title = "Повний backup",
                description =
                    "History + Черга + локальна квота + SearchCache. " +
                        "Має SHA-256 integrity check.",
                buttonLabel = "Зберегти backup",
                primary = true,
                action = ::createFullBackup
            )
        )

        content.addView(
            actionCard(
                title = "Restore",
                description =
                    "Відновлює локальні дані з YTM_Backup_*.json. " +
                        "Перед Restore автоматично створюється safety snapshot.",
                buttonLabel = "Вибрати backup",
                primary = false,
                action = ::chooseBackupForRestore
            )
        )

        val rollbackCard =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
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
            }

        rollbackCard.addView(
            TextView(this).apply {
                text = "Відкат останнього Restore"
                textSize = 16f
                setTextColor(Color.WHITE)
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
            }
        )

        rollbackCard.addView(
            TextView(this).apply {
                text =
                    "Повертає локальний стан, який був перед останнім Restore."
                textSize = 12.5f
                setTextColor(MUTED)
                setPadding(
                    0,
                    dp(5),
                    0,
                    dp(10)
                )
            }
        )

        rollbackButton =
            actionButton(
                label = "Відкотити останній Restore",
                primary = false
            ) {
                confirmRestoreSafetySnapshot()
            }

        rollbackCard.addView(rollbackButton)

        content.addView(
            rollbackCard,
            cardParams()
        )

        content.addView(
            sectionTitle("Експорт")
        )

        content.addView(
            twoActionCard(
                title = "Історія",
                description =
                    "TXT — читабельний звіт. JSON — технічний експорт History.",
                firstLabel = "History TXT",
                firstAction = ::exportHistoryTxt,
                secondLabel = "History JSON",
                secondAction = ::exportHistoryJson
            )
        )

        content.addView(
            actionCard(
                title = "Pending Queue",
                description =
                    "Технічний JSON поточної черги відкладених операцій.",
                buttonLabel = "Черга → JSON",
                primary = false,
                action = ::exportPendingJson
            )
        )

        content.addView(
            sectionTitle("Поділитися")
        )

        content.addView(
            twoActionCard(
                title = "Android Share",
                description =
                    "YTM Importer не завантажує ці файли на власний сервер.",
                firstLabel = "History TXT",
                firstAction = ::shareHistoryTxt,
                secondLabel = "Повний backup",
                secondAction = ::confirmShareFullBackup
            )
        )

        content.addView(
            sectionTitle("Безпека")
        )

        content.addView(
            TextView(this).apply {
                text =
                    "OAuth access token, паролі та signing keys не входять " +
                        "у Full Backup.\n\n" +
                        "Але backup може містити Google email, YouTube Channel ID, " +
                        "назви плейлистів, History, Queue та SearchCache. " +
                        "Не надсилайте backup туди, де не готові розкрити ці дані."
                textSize = 12.5f
                setTextColor(MUTED)
                setPadding(
                    dp(14),
                    dp(14),
                    dp(14),
                    dp(14)
                )
                background =
                    roundedBackground(
                        color =
                            Color.rgb(
                                31,
                                29,
                                24
                            ),
                        radiusDp = 14,
                        strokeColor =
                            Color.rgb(
                                83,
                                68,
                                37
                            )
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
        refreshSummary()
    }

    private fun refreshSummary() {
        val historyCount =
            historyStore.getAll().size

        val pendingCount =
            pendingJobStore.getAll().size

        val quota =
            quotaTracker.snapshot()

        val cache =
            searchCache.stats()

        val snapshotSummary =
            runCatching {
                localBackupManager.inspectSafetySnapshot()
            }.getOrNull()

        summaryText.text =
            buildString {
                append("Локальні дані\n\n")
                append("History: $historyCount записів\n")
                append("Черга: $pendingCount завдань\n")
                append(
                    "SearchCache: ${cache.validEntries} активних"
                )

                if (cache.expiredEntries > 0) {
                    append(
                        " • ${cache.expiredEntries} прострочених"
                    )
                }

                append("\n")
                append(
                    "Search quota: ${quota.searchCalls}/" +
                        "${QuotaTracker.SEARCH_DAILY_LIMIT} • " +
                        "cache hits ${quota.cacheHits}\n"
                )
                append(
                    "General quota estimate: ${quota.generalUnits}/" +
                        "${QuotaTracker.GENERAL_DAILY_LIMIT}\n"
                )

                if (snapshotSummary != null) {
                    append(
                        "Safety snapshot: доступний • " +
                            formatDate(
                                snapshotSummary.exportedAt
                            )
                    )
                } else {
                    append("Safety snapshot: немає")
                }
            }

        rollbackButton.isEnabled =
            snapshotSummary != null

        rollbackButton.alpha =
            if (rollbackButton.isEnabled) {
                1f
            } else {
                0.5f
            }
    }

    private fun createFullBackup() {
        val content =
            runCatching {
                localBackupManager.createBackupJson()
            }.getOrElse { error ->
                toast(
                    error.message
                        ?: "Не вдалося створити backup"
                )
                return
            }

        AlertDialog.Builder(this)
            .setTitle("Зберегти повний backup?")
            .setMessage(
                "Буде збережено:\n" +
                    "• History\n" +
                    "• Pending Queue\n" +
                    "• локальні quota counters\n" +
                    "• SearchCache\n\n" +
                    "Backup може містити Google email, Channel ID " +
                    "та назви плейлистів.\n\n" +
                    "OAuth token, паролі та signing keys не входять.\n\n" +
                    "Файл має SHA-256 integrity check."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Зберегти"
            ) { _, _ ->
                createDocumentForExport(
                    fileName =
                        "YTM_Backup_${exportTimestamp()}.json",
                    mimeType =
                        "application/json",
                    content = content,
                    successMessage =
                        "Повний backup збережено"
                )
            }
            .show()
    }

    private fun chooseBackupForRestore() {
        AlertDialog.Builder(this)
            .setTitle("Відновити backup?")
            .setMessage(
                "Restore замінить локальні:\n\n" +
                    "• History\n" +
                    "• Чергу\n" +
                    "• локальну квоту\n" +
                    "• SearchCache\n\n" +
                    "YouTube/YTM плейлисти в інтернеті не змінюються.\n\n" +
                    "Перед Restore буде створено safety snapshot."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Вибрати backup"
            ) { _, _ ->
                val intent =
                    Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                    ).apply {
                        addCategory(
                            Intent.CATEGORY_OPENABLE
                        )
                        type = "application/json"
                    }

                startActivityForResult(
                    intent,
                    restoreBackupRequestCode
                )
            }
            .show()
    }

    private fun prepareRestoreBackup(
        uri: Uri
    ) {
        val raw =
            runCatching {
                contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    ?: error(
                        "Не вдалося прочитати backup"
                    )
            }.getOrElse { error ->
                toast(
                    "Помилка читання backup: " +
                        (
                            error.message
                                ?: "невідома помилка"
                        )
                )
                return
            }

        val summary =
            runCatching {
                localBackupManager.inspectBackup(raw)
            }.getOrElse { error ->
                toast(
                    "Backup не підходить: " +
                        (
                            error.message
                                ?: "невідома помилка"
                        )
                )
                return
            }

        AlertDialog.Builder(this)
            .setTitle("Підтвердити Restore")
            .setMessage(
                "Backup YTM Importer\n\n" +
                    "Schema: ${summary.schemaVersion}\n" +
                    "Версія застосунку: ${summary.appVersion}\n" +
                    "Дата: ${formatDate(summary.exportedAt)}\n" +
                    "Груп даних: ${summary.preferenceGroups}\n" +
                    "Значень: ${summary.valueCount}\n" +
                    "Integrity: " +
                    if (summary.integrityProtected) {
                        if (summary.integrityVerified) {
                            "SHA-256 ✓\n\n"
                        } else {
                            "SHA-256 ?\n\n"
                        }
                    } else {
                        "legacy backup без checksum\n\n"
                    } +
                    "Перед Restore буде автоматично створено " +
                    "safety snapshot поточного стану."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Відновити"
            ) { _, _ ->
                restoreBackupNow(raw)
            }
            .show()
    }

    private fun restoreBackupNow(
        raw: String
    ) {
        val result =
            runCatching {
                localBackupManager.restoreBackupJson(raw)
            }.getOrElse { error ->
                toast(
                    "Restore не виконано: " +
                        (
                            error.message
                                ?: "невідома помилка"
                        )
                )
                return
            }

        refreshSummary()

        AlertDialog.Builder(this)
            .setTitle("Backup відновлено")
            .setMessage(
                "Груп даних: ${result.preferenceGroups}\n" +
                    "Відновлено значень: ${result.restoredValues}\n\n" +
                    "History, Черга, локальна квота та SearchCache " +
                    "вже відновлені.\n\n" +
                    if (result.safetySnapshotCreated) {
                        "Safety snapshot стану ДО Restore збережено."
                    } else {
                        ""
                    }
            )
            .setNeutralButton(
                "Відкотити"
            ) { _, _ ->
                confirmRestoreSafetySnapshot()
            }
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun confirmRestoreSafetySnapshot() {
        val summary =
            runCatching {
                localBackupManager.inspectSafetySnapshot()
            }.getOrElse { error ->
                toast(
                    "Safety snapshot пошкоджено: " +
                        (
                            error.message
                                ?: "невідома помилка"
                        )
                )
                return
            }

        if (summary == null) {
            toast(
                "Safety snapshot ще не створено. " +
                    "Він з'явиться автоматично перед Restore."
            )
            return
        }

        AlertDialog.Builder(this)
            .setTitle(
                "Відкотити останній Restore?"
            )
            .setMessage(
                "Буде повернуто локальний стан ДО останнього Restore.\n\n" +
                    "Дата: ${formatDate(summary.exportedAt)}\n" +
                    "Версія: ${summary.appVersion}\n" +
                    "Груп: ${summary.preferenceGroups}\n" +
                    "Значень: ${summary.valueCount}\n\n" +
                    "YouTube/YTM плейлисти в інтернеті не змінюються."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Відкотити"
            ) { _, _ ->
                restoreSafetySnapshotNow()
            }
            .show()
    }

    private fun restoreSafetySnapshotNow() {
        val result =
            runCatching {
                localBackupManager.restoreSafetySnapshot()
            }.getOrElse { error ->
                toast(
                    "Відкат не виконано: " +
                        (
                            error.message
                                ?: "невідома помилка"
                        )
                )
                return
            }

        refreshSummary()

        AlertDialog.Builder(this)
            .setTitle("Відкат виконано")
            .setMessage(
                "Локальний стан ДО останнього Restore повернуто.\n\n" +
                    "Груп: ${result.preferenceGroups}\n" +
                    "Відновлено значень: ${result.restoredValues}."
            )
            .setNegativeButton(
                "Видалити snapshot"
            ) { _, _ ->
                localBackupManager.clearSafetySnapshot()
                refreshSummary()
                toast(
                    "Safety snapshot видалено"
                )
            }
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun exportHistoryTxt() {
        val text =
            buildHistoryExportTxt()
                ?: return toast(
                    "Історія порожня — експортувати нічого"
                )

        createDocumentForExport(
            fileName =
                "YTM_History_${exportTimestamp()}.txt",
            mimeType =
                "text/plain",
            content = text,
            successMessage =
                "History TXT збережено"
        )
    }

    private fun exportHistoryJson() {
        if (historyStore.getAll().isEmpty()) {
            return toast(
                "Історія порожня — експортувати нічого"
            )
        }

        createDocumentForExport(
            fileName =
                "YTM_History_${exportTimestamp()}.json",
            mimeType =
                "application/json",
            content =
                historyStore.exportJson(),
            successMessage =
                "History JSON збережено"
        )
    }

    private fun exportPendingJson() {
        if (pendingJobStore.getAll().isEmpty()) {
            return toast(
                "Черга порожня — експортувати нічого"
            )
        }

        createDocumentForExport(
            fileName =
                "YTM_Pending_${exportTimestamp()}.json",
            mimeType =
                "application/json",
            content =
                pendingJobStore.exportJson(),
            successMessage =
                "Pending Queue JSON збережено"
        )
    }

    private fun shareHistoryTxt() {
        val content =
            buildHistoryExportTxt()
                ?: return toast(
                    "Історія порожня — ділитися нічим"
                )

        shareTextFile(
            fileName =
                "YTM_History_${exportTimestamp()}.txt",
            mimeType =
                "text/plain",
            content = content,
            chooserTitle =
                "Поділитися History"
        )
    }

    private fun confirmShareFullBackup() {
        AlertDialog.Builder(this)
            .setTitle(
                "Поділитися повним backup?"
            )
            .setMessage(
                "Backup може містити Google email, Channel ID, " +
                    "назви плейлистів, History, Queue та SearchCache.\n\n" +
                    "OAuth token, паролі та signing keys у файл не входять.\n\n" +
                    "Надсилайте backup лише туди, де довіряєте одержувачу."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Поділитися"
            ) { _, _ ->
                val content =
                    runCatching {
                        localBackupManager.createBackupJson()
                    }.getOrElse { error ->
                        toast(
                            error.message
                                ?: "Не вдалося створити backup"
                        )
                        return@setPositiveButton
                    }

                shareTextFile(
                    fileName =
                        "YTM_Backup_${exportTimestamp()}.json",
                    mimeType =
                        "application/json",
                    content = content,
                    chooserTitle =
                        "Поділитися повним backup"
                )
            }
            .show()
    }

    private fun buildHistoryExportTxt(): String? {
        val entries =
            historyStore.getAll()

        if (entries.isEmpty()) {
            return null
        }

        return buildString {
            append(
                "YTM Importer — History export\n"
            )
            append(
                "Версія: ${BuildConfig.VERSION_NAME}\n"
            )
            append(
                "Експортовано: " +
                    formatDate(
                        System.currentTimeMillis()
                    ) +
                    "\n"
            )
            append(
                "Записів: ${entries.size}\n\n"
            )

            entries.forEachIndexed {
                    index,
                    entry ->

                append(
                    "========================================\n"
                )
                append(
                    "${index + 1}. ${entry.playlistName}\n"
                )
                append(
                    "========================================\n"
                )
                append(
                    buildHistorySummary(entry)
                )

                val problemText =
                    buildProblemLog(entry)

                if (!problemText.isNullOrBlank()) {
                    append("\n\n")
                    append(problemText)
                }

                if (
                    index != entries.lastIndex
                ) {
                    append("\n\n")
                }
            }
        }
    }

    private fun buildHistorySummary(
        entry: HistoryEntry
    ): String =
        buildString {
            append(
                "Статус: ${historyStatusLabel(entry.status)}\n"
            )
            append(
                "Дата: ${formatDate(entry.updatedAt)}\n"
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

            if (!entry.playlistId.isNullOrBlank()) {
                append(
                    "YTM: https://music.youtube.com/playlist?list=" +
                        entry.playlistId +
                        "\n"
                )
            }

            if (!entry.lastError.isNullOrBlank()) {
                append(
                    "Остання помилка: " +
                        entry.lastError +
                        "\n"
                )
            }
        }.trimEnd()

    private fun buildProblemLog(
        entry: HistoryEntry
    ): String? {
        val problems =
            entry.tracks.filter {
                it.manuallySelected ||
                    it.status ==
                        TrackStatus.SKIPPED.name ||
                    it.status ==
                        TrackStatus.DUPLICATE.name ||
                    it.status ==
                        TrackStatus.MISSING.name ||
                    it.status ==
                        TrackStatus.PENDING.name ||
                    it.status ==
                        TrackStatus.FAILED.name
            }

        if (problems.isEmpty()) {
            return null
        }

        return buildString {
            append("Заміни та проблеми\n\n")

            problems.forEachIndexed {
                    index,
                    track ->

                append(
                    "${index + 1}. " +
                        "${track.originalArtist} — " +
                        "${track.originalTitle}\n"
                )
                append(
                    "→ ${problemTrackLabel(track)}"
                )

                if (!track.selectedChannel.isNullOrBlank()) {
                    append(
                        "\nКанал: ${track.selectedChannel}"
                    )
                }

                if (!track.error.isNullOrBlank()) {
                    append(
                        "\nПомилка: ${track.error}"
                    )
                }

                if (index != problems.lastIndex) {
                    append("\n\n")
                }
            }
        }
    }

    private fun problemTrackLabel(
        track: HistoryTrack
    ): String =
        when {
            track.status ==
                TrackStatus.SKIPPED.name ->
                "[пропущено]"

            track.status ==
                TrackStatus.DUPLICATE.name ->
                "[дублікат]"

            track.status ==
                TrackStatus.MISSING.name ->
                "[не знайдено]"

            track.status ==
                TrackStatus.PENDING.name ->
                "[очікує]"

            track.status ==
                TrackStatus.FAILED.name &&
                track.selectedTitle
                    .isNullOrBlank() ->
                "[помилка]"

            !track.selectedTitle
                .isNullOrBlank() ->
                track.selectedTitle

            else ->
                "[без заміни]"
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
                "Частково"

            HistoryStatus.PENDING_QUOTA ->
                "Очікує квоти"

            HistoryStatus.FAILED ->
                "Помилка"
        }

    private fun createDocumentForExport(
        fileName: String,
        mimeType: String,
        content: String,
        successMessage: String
    ) {
        pendingExportContent = content
        pendingExportSuccessMessage =
            successMessage

        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type = mimeType
                putExtra(
                    Intent.EXTRA_TITLE,
                    fileName
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

    private fun writePendingExport(
        uri: Uri
    ) {
        val content =
            pendingExportContent
                ?: return toast(
                    "Немає підготовлених даних для експорту"
                )

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
            toast(
                pendingExportSuccessMessage
                    ?: "Файл збережено"
            )
        }.onFailure { error ->
            toast(
                "Помилка запису файлу: " +
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
                ?.forEach(
                    File::delete
                )

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

    private fun actionCard(
        title: String,
        description: String,
        buttonLabel: String,
        primary: Boolean,
        action: () -> Unit
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
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

            addView(
                TextView(
                    this@DataActivity
                ).apply {
                    text = title
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                }
            )

            addView(
                TextView(
                    this@DataActivity
                ).apply {
                    text = description
                    textSize = 12.5f
                    setTextColor(MUTED)
                    setPadding(
                        0,
                        dp(5),
                        0,
                        dp(10)
                    )
                }
            )

            addView(
                actionButton(
                    label = buttonLabel,
                    primary = primary,
                    action = action
                )
            )

            layoutParams =
                cardParams()
        }

    private fun twoActionCard(
        title: String,
        description: String,
        firstLabel: String,
        firstAction: () -> Unit,
        secondLabel: String,
        secondAction: () -> Unit
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
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

            addView(
                TextView(
                    this@DataActivity
                ).apply {
                    text = title
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                }
            )

            addView(
                TextView(
                    this@DataActivity
                ).apply {
                    text = description
                    textSize = 12.5f
                    setTextColor(MUTED)
                    setPadding(
                        0,
                        dp(5),
                        0,
                        dp(10)
                    )
                }
            )

            val row =
                LinearLayout(
                    this@DataActivity
                ).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                }

            row.addView(
                actionButton(
                    label = firstLabel,
                    primary = false,
                    action = firstAction
                ),
                LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1f
                )
            )

            row.addView(
                actionButton(
                    label = secondLabel,
                    primary = false,
                    action = secondAction
                ),
                LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1f
                ).apply {
                    marginStart =
                        dp(8)
                }
            )

            addView(row)

            layoutParams =
                cardParams()
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
        }

    private fun topBar(
        title: String,
        onBack: () -> Unit
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
                    this@DataActivity
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
                    this@DataActivity
                ).apply {
                    text = title
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setPadding(
                        dp(12),
                        0,
                        0,
                        0
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
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
                dp(14),
                0,
                dp(6)
            )
        }

    private fun cardParams():
        LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin =
                dp(8)
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

    private fun exportTimestamp(): String =
        SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(
            Date()
        )

    private fun formatDate(
        timestamp: Long
    ): String =
        if (timestamp > 0L) {
            SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
            ).format(
                Date(timestamp)
            )
        } else {
            "невідомо"
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

    companion object {
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
