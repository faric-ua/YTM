package com.saney.ytmimporter
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.parser.PlaylistParser
import com.saney.ytmimporter.storage.AccountLibraryExporter
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import com.saney.ytmimporter.storage.PlaylistProjectImport
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors

class ImportActivity : Activity() {
    private val fileRequestCode =
        2301

    private val exportFolderRequestCode =
        2302

    private val executor =
        Executors.newSingleThreadExecutor()

    private val api =
        YouTubeApi()

    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private lateinit var playlistNameInput:
        EditText

    private lateinit var tracksInput:
        EditText

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        AppThemeManager.applyWindow(this)

        currentPlaylistStore =
            CurrentPlaylistStore(this)

        buildUi()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
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
            fileRequestCode ->
                data
                    ?.data
                    ?.let(::loadFile)

            exportFolderRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::exportAllYtmPlaylistsToFolder
                    )
        }
    }

    private fun buildUi() {
        val palette = AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }

        root.addView(topBar())

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

        currentPlaylistStore
            .load()
            ?.let { current ->
                content.addView(
                    sectionTitle(
                        "Поточний робочий список"
                    )
                )

                content.addView(
                    card().apply {
                        addView(
                            TextView(
                                this@ImportActivity
                            ).apply {
                                text =
                                    current.playlist.name
                                textSize = 16f
                                setTextColor(
                                    Color.WHITE
                                )
                                setTypeface(
                                    typeface,
                                    Typeface.BOLD
                                )
                            }
                        )

                        addView(
                            infoText(
                                "${current.playlist.tracks.size} треків • " +
                                    current.sourceLabel +
                                    "\nАвтовідновлення зберігає тільки останній робочий список. " +
                                    "Для кількох списків використовуйте YTM Project."
                            )
                        )

                        addView(
                            actionButton(
                                label =
                                    "Очистити поточний список",
                                primary = false
                            ) {
                                confirmClearWorkspace(
                                    current.playlist.name
                                )
                            }
                        )
                    }
                )
            }

        content.addView(
            sectionTitle(
                "Імпорт із YouTube/YTM"
            )
        )

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@ImportActivity
                    ).apply {
                        text =
                            "Плейлист з підключеного акаунта"
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    infoText(
                        "Read-only імпорт: застосунок лише читає список плейлистів " +
                            "та їх треки. Плейлист у YouTube/YTM не змінюється. " +
                            "Треки відкриваються локально вже з точними videoId."
                    )
                )

                addView(
                    actionButton(
                        label =
                            "Вибрати плейлист з YTM",
                        primary = true
                    ) {
                        importFromYtmAccount()
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Експортувати всі плейлисти в папку",
                        primary = false,
                        topMarginDp = 10
                    ) {
                        chooseYtmExportFolder()
                    }
                )
            }
        )

        content.addView(
            sectionTitle("Імпорт із файлу")
        )

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@ImportActivity
                    ).apply {
                        text =
                            "CSV, TXT або YTM Project"
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    infoText(
                        "Android file picker приймає будь-який MIME type, " +
                            "бо деякі providers неправильно позначають CSV."
                    )
                )

                addView(
                    actionButton(
                        label = "Вибрати файл",
                        primary = true
                    ) {
                        chooseFile()
                    }
                )
            }
        )

        content.addView(
            sectionTitle("Вставити текст")
        )

        content.addView(
            card().apply {
                playlistNameInput =
                    EditText(
                        this@ImportActivity
                    ).apply {
                        hint =
                            "Назва плейлиста (необов'язково)"
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
                            dp(12),
                            dp(8),
                            dp(12),
                            dp(8)
                        )
                        background =
                            roundedBackground(
                                color =
                                    Color.rgb(
                                        31,
                                        33,
                                        39
                                    ),
                                radiusDp = 10,
                                strokeColor =
                                    BORDER
                            )
                    }

                addView(
                    playlistNameInput,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(48)
                    )
                )

                tracksInput =
                    EditText(
                        this@ImportActivity
                    ).apply {
                        hint =
                            "Solarstone & JES - Like a Waterfall\n" +
                                "Sultan & Tone Depth - Moments\n" +
                                "Ahmet Ertenu - Why"
                        minLines = 10
                        gravity =
                            Gravity.TOP or
                                Gravity.START
                        inputType =
                            InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        textSize = 14f
                        setTextColor(Color.WHITE)
                        setHintTextColor(
                            Color.rgb(
                                110,
                                113,
                                120
                            )
                        )
                        setPadding(
                            dp(12),
                            dp(10),
                            dp(12),
                            dp(10)
                        )
                        background =
                            roundedBackground(
                                color =
                                    Color.rgb(
                                        31,
                                        33,
                                        39
                                    ),
                                radiusDp = 10,
                                strokeColor =
                                    BORDER
                            )
                    }

                addView(
                    tracksInput,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(8)
                    }
                )

                addView(
                    infoText(
                        "Один трек на рядок: Artist - Track. " +
                            "Підтримуються -, – та —, а також 1. / 2)."
                    )
                )

                addView(
                    actionButton(
                        label =
                            "Імпортувати текст",
                        primary = true
                    ) {
                        importText()
                    }
                )
            }
        )

        content.addView(
            sectionTitle("Що буде далі")
        )

        content.addView(
            TextView(this).apply {
                text =
                    "Після імпорту ви повернетеся на головний екран. " +
                        "Крок 3 «Знайти / перевірити» запускає пошук, " +
                        "а потім відкриває окремий Review screen."
                textSize = 13f
                setTextColor(MUTED)
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

    private fun importFromYtmAccount() {
        val token =
            AuthSessionStore
                .current()
                .accessToken

        if (token.isNullOrBlank()) {
            toast(
                "Спочатку підключіть Google/YTM у кроці 2 на головному екрані."
            )
            return
        }

        toast(
            "Завантажую плейлисти YouTube/YTM…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listMyPlaylists(token)
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        playlists ->

                    if (playlists.isEmpty()) {
                        toast(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                        return@onSuccess
                    }

                    showYtmPlaylistPicker(
                        token = token,
                        playlists = playlists
                    )
                }.onFailure { error ->
                    toast(
                        error.message
                            ?: "Не вдалося завантажити список плейлистів"
                    )
                }
            }
        }
    }

    private fun showYtmPlaylistPicker(
        token: String,
        playlists: List<YouTubePlaylistInfo>
    ) {
        val labels =
            playlists.map { playlist ->
                buildString {
                    append(playlist.title)
                    append("\n")
                    append(playlist.itemCount)
                    append(" треків • ")
                    append(
                        when (
                            playlist.privacyStatus
                        ) {
                            "public" ->
                                "публічний"

                            "unlisted" ->
                                "за посиланням"

                            else ->
                                "приватний"
                        }
                    )
                }
            }

        UiChrome.showMenuDialog(
            activity = this,
            title =
                "Вибрати плейлист YouTube/YTM",
            subtitle =
                "Read-only: виберіть плейлист для локального імпорту.",
            actions =
                playlists.mapIndexed {
                        index,
                        playlist ->

                    UiChrome.MenuAction(
                        label = labels[index],
                        onClick = {
                            loadYtmPlaylist(
                                token = token,
                                playlistInfo =
                                    playlist
                            )
                        }
                    )
                },
            negativeLabel = "Скасувати"
        )
    }

    private fun loadYtmPlaylist(
        token: String,
        playlistInfo: YouTubePlaylistInfo
    ) {
        toast(
            "Завантажую «${playlistInfo.title}»…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listPlaylistTracks(
                        accessToken = token,
                        playlistId =
                            playlistInfo.id
                    )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        loaded ->

                    if (loaded.tracks.isEmpty()) {
                        toast(
                            "Плейлист «${playlistInfo.title}» порожній або не містить доступних відео."
                        )
                        return@onSuccess
                    }

                    val imported =
                        ImportedPlaylist(
                            name =
                                playlistInfo.title,
                            tracks =
                                loaded.tracks
                                    .toMutableList()
                        )

                    finishImport(
                        imported = imported,
                        sourceLabel =
                            "YouTube/YTM (${playlistInfo.title})",
                        message =
                            "YTM playlist імпортовано: " +
                                "${imported.tracks.size} треків • " +
                                "точних videoId: ${imported.tracks.size} • " +
                                "playlistItems.list: ${loaded.requestCount} request(s)."
                    )
                }.onFailure { error ->
                    toast(
                        error.message
                            ?: "Не вдалося завантажити плейлист"
                    )
                }
            }
        }
    }

    private fun chooseYtmExportFolder() {
        val token =
            AuthSessionStore
                .current()
                .accessToken

        if (token.isNullOrBlank()) {
            toast(
                "Спочатку підключіть Google/YTM у кроці 2 на головному екрані."
            )
            return
        }

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT_TREE
            ).apply {
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            exportFolderRequestCode
        )
    }

    private fun exportAllYtmPlaylistsToFolder(
        treeUri: Uri
    ) {
        val token =
            AuthSessionStore
                .current()
                .accessToken

        if (token.isNullOrBlank()) {
            toast(
                "Авторизація Google/YTM недоступна. Підключіть акаунт ще раз."
            )
            return
        }

        runCatching {
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
        }

        toast(
            "Готую read-only експорт плейлистів…"
        )

        executor.execute {
            val result =
                runCatching {
                    val playlists =
                        api.listMyPlaylists(token)

                    if (playlists.isEmpty()) {
                        error(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                    }

                    runOnUiThread {
                        if (
                            !isFinishing &&
                            !isDestroyed
                        ) {
                            toast(
                                "Знайдено ${playlists.size} плейлистів. Експортую…"
                            )
                        }
                    }

                    val session =
                        AccountLibraryExporter
                            .createSessionFolder(
                                resolver =
                                    contentResolver,
                                treeUri =
                                    treeUri
                            )

                    val records =
                        mutableListOf<
                            AccountLibraryExporter.ExportRecord
                        >()

                    playlists.forEach {
                            playlistInfo ->

                        if (
                            playlistInfo.itemCount <= 0
                        ) {
                            records +=
                                AccountLibraryExporter.ExportRecord(
                                    playlistId =
                                        playlistInfo.id,
                                    title =
                                        playlistInfo.title,
                                    privacyStatus =
                                        playlistInfo.privacyStatus,
                                    sourceItemCount =
                                        playlistInfo.itemCount,
                                    exportedTrackCount = 0,
                                    playlistItemsRequests = 0,
                                    status =
                                        "SKIPPED_EMPTY",
                                    fileName = null
                                )

                            return@forEach
                        }

                        runCatching {
                            api.listPlaylistTracks(
                                accessToken =
                                    token,
                                playlistId =
                                    playlistInfo.id
                            )
                        }.onSuccess {
                                loaded ->

                            if (
                                loaded.tracks.isEmpty()
                            ) {
                                records +=
                                    AccountLibraryExporter.ExportRecord(
                                        playlistId =
                                            playlistInfo.id,
                                        title =
                                            playlistInfo.title,
                                        privacyStatus =
                                            playlistInfo.privacyStatus,
                                        sourceItemCount =
                                            playlistInfo.itemCount,
                                        exportedTrackCount = 0,
                                        playlistItemsRequests =
                                            loaded.requestCount,
                                        status =
                                            "SKIPPED_NO_ACCESSIBLE_TRACKS",
                                        fileName = null
                                    )
                            } else {
                                val imported =
                                    ImportedPlaylist(
                                        name =
                                            playlistInfo.title,
                                        tracks =
                                            loaded.tracks
                                                .toMutableList()
                                    )

                                val fileName =
                                    AccountLibraryExporter
                                        .writePlaylistProject(
                                            resolver =
                                                contentResolver,
                                            session =
                                                session,
                                            playlistInfo =
                                                playlistInfo,
                                            playlist =
                                                imported,
                                            appVersion =
                                                BuildConfig.VERSION_NAME
                                        )

                                records +=
                                    AccountLibraryExporter.ExportRecord(
                                        playlistId =
                                            playlistInfo.id,
                                        title =
                                            playlistInfo.title,
                                        privacyStatus =
                                            playlistInfo.privacyStatus,
                                        sourceItemCount =
                                            playlistInfo.itemCount,
                                        exportedTrackCount =
                                            imported.tracks.size,
                                        playlistItemsRequests =
                                            loaded.requestCount,
                                        status =
                                            "EXPORTED",
                                        fileName =
                                            fileName
                                    )
                            }
                        }.onFailure {
                                error ->

                            records +=
                                AccountLibraryExporter.ExportRecord(
                                    playlistId =
                                        playlistInfo.id,
                                    title =
                                        playlistInfo.title,
                                    privacyStatus =
                                        playlistInfo.privacyStatus,
                                    sourceItemCount =
                                        playlistInfo.itemCount,
                                    exportedTrackCount = 0,
                                    playlistItemsRequests = 0,
                                    status =
                                        "FAILED",
                                    fileName = null,
                                    error =
                                        error.message
                                            ?: error
                                                .javaClass
                                                .simpleName
                                )
                        }
                    }

                    val manifestFile =
                        AccountLibraryExporter
                            .writeManifest(
                                resolver =
                                    contentResolver,
                                session =
                                    session,
                                appVersion =
                                    BuildConfig.VERSION_NAME,
                                records =
                                    records
                            )

                    BulkExportResult(
                        folderName =
                            session.folderName,
                        manifestFile =
                            manifestFile,
                        playlistCount =
                            records.size,
                        exportedProjects =
                            records.count {
                                it.status ==
                                    "EXPORTED"
                            },
                        skippedPlaylists =
                            records.count {
                                it.status
                                    .startsWith(
                                        "SKIPPED"
                                    )
                            },
                        failedPlaylists =
                            records.count {
                                it.status ==
                                    "FAILED"
                            },
                        playlistItemsRequests =
                            records.sumOf {
                                it.playlistItemsRequests
                            }
                    )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        summary ->

                    UiChrome.showMessageDialog(
                        activity = this,
                        title =
                            "Експорт завершено",
                        message =
                            "Плейлистів акаунта: ${summary.playlistCount}\n" +
                                "Збережено YTM Project: ${summary.exportedProjects}\n" +
                                "Пропущено: ${summary.skippedPlaylists}\n" +
                                "Помилок: ${summary.failedPlaylists}\n" +
                                "playlistItems.list: ${summary.playlistItemsRequests} request(s)\n\n" +
                                "Папка: ${summary.folderName}\n" +
                                "Індекс: ${summary.manifestFile}",
                        actions =
                            listOf(
                                UiChrome.DialogAction(
                                    label =
                                        "Закрити",
                                    tone =
                                        UiChrome.ActionTone.ACCENT,
                                    onClick = {}
                                )
                            )
                    )
                }.onFailure { error ->
                    UiChrome.showMessageDialog(
                        activity = this,
                        title =
                            "Експорт не завершено",
                        message =
                            error.message
                                ?: "Невідома помилка експорту",
                        actions =
                            listOf(
                                UiChrome.DialogAction(
                                    label =
                                        "Закрити",
                                    tone =
                                        UiChrome.ActionTone.ACCENT,
                                    onClick = {}
                                )
                            )
                    )
                }
            }
        }
    }

    private data class BulkExportResult(
        val folderName: String,
        val manifestFile: String,
        val playlistCount: Int,
        val exportedProjects: Int,
        val skippedPlaylists: Int,
        val failedPlaylists: Int,
        val playlistItemsRequests: Int
    )

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

    private fun loadFile(
        uri: Uri
    ) {
        try {
            contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
        } catch (_: Exception) {
        }

        val fileName =
            queryFileName(uri)
                ?: "playlist.csv"

        val text =
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
                        "Не вдалося прочитати файл"
                    )
            }.getOrElse { error ->
                toast(
                    error.message
                        ?: "Не вдалося прочитати файл"
                )
                return
            }

        if (
            PlaylistProjectCodec
                .isProject(text)
        ) {
            runCatching {
                PlaylistProjectCodec
                    .importProject(text)
            }.onSuccess { project ->
                finishProjectImport(
                    project = project,
                    fileName = fileName
                )
            }.onFailure { error ->
                toast(
                    error.message
                        ?: "Не вдалося завантажити YTM Project"
                )
            }

            return
        }

        runCatching {
            PlaylistParser.parse(
                fileName,
                text
            )
        }.onSuccess { imported ->
            finishImport(
                imported = imported,
                sourceLabel =
                    "Файл ($fileName)",
                message =
                    "Файл імпортовано: " +
                        "${imported.tracks.size} треків."
            )
        }.onFailure { error ->
            toast(
                error.message
                    ?: "Помилка імпорту"
            )
        }
    }

    private fun importText() {
        val raw =
            tracksInput
                .text
                .toString()

        if (raw.isBlank()) {
            tracksInput.error =
                "Вставте хоча б один трек"
            return
        }

        runCatching {
            PlaylistParser.parse(
                "Вставлений список.txt",
                raw
            ).also { imported ->
                playlistNameInput
                    .text
                    .toString()
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        imported.name = it
                    }
            }
        }.onSuccess { imported ->
            finishImport(
                imported = imported,
                sourceLabel =
                    "Текст",
                message =
                    "Текст імпортовано: " +
                        "${imported.tracks.size} треків."
            )
        }.onFailure { error ->
            tracksInput.error =
                error.message
                    ?: "Не вдалося розібрати список"
        }
    }

    private fun finishProjectImport(
        project: PlaylistProjectImport,
        fileName: String
    ) {
        val scopeNote =
            if (
                project.sourceDestination ==
                    PendingDestination
                        .EXISTING_PLAYLIST
            ) {
                " Це import batch, а не повна копія " +
                    "старого існуючого плейлиста."
            } else {
                ""
            }

        finishImport(
            imported = project.playlist,
            sourceLabel =
                "YTM Project ($fileName)",
            message =
                "YTM Project: " +
                    "${project.playlist.tracks.size} треків. " +
                    "Точних videoId: " +
                    "${project.exactSelectionCount}. " +
                    "Без videoId: " +
                    "${project.unresolvedCount}." +
                    scopeNote
        )
    }

    private fun finishImport(
        imported: ImportedPlaylist,
        sourceLabel: String,
        message: String
    ) {
        imported.tracks
            .forEachIndexed {
                    index,
                    track ->

                track.historyIndex =
                    index
            }

        currentPlaylistStore.save(
            playlist = imported,
            sourceLabel = sourceLabel
        )

        setResult(
            RESULT_OK,
            Intent()
                .putExtra(
                    EXTRA_IMPORT_MESSAGE,
                    message
                )
        )

        finish()
    }

    private fun confirmClearWorkspace(
        playlistName: String
    ) {
        UiChrome.alertBuilder(this)
            .setTitle(
                "Очистити поточний список?"
            )
            .setMessage(
                "Автозбережений робочий список «$playlistName» буде видалено з пристрою.\n\n" +
                    "YTM Project-файли та плейлисти в YouTube/YTM не змінюються."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Очистити"
            ) { _, _ ->
                currentPlaylistStore.clear()

                setResult(
                    RESULT_OK,
                    Intent()
                        .putExtra(
                            EXTRA_CLEAR_WORKSPACE,
                            true
                        )
                )

                finish()
            }
            .show()
    }

    private fun queryFileName(
        uri: Uri
    ): String? {
        contentResolver
            .query(
                uri,
                null,
                null,
                null,
                null
            )
            ?.use { cursor ->
                val index =
                    cursor.getColumnIndex(
                        OpenableColumns
                            .DISPLAY_NAME
                    )

                if (
                    index >= 0 &&
                    cursor.moveToFirst()
                ) {
                    return cursor
                        .getString(index)
                }
            }

        return uri.lastPathSegment
    }

    private fun topBar():
        LinearLayout =
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
                    this@ImportActivity
                ).apply {
                    text = "‹"
                    isAllCaps = false
                    textSize = 26f
                    setTextColor(Color.WHITE)
                    background =
                        roundedBackground(
                            color = SURFACE,
                            radiusDp = 12,
                            strokeColor = BORDER
                        )
                    setOnClickListener {
                        finish()
                    }
                },
                LinearLayout.LayoutParams(
                    dp(46),
                    dp(46)
                )
            )

            addView(
                TextView(
                    this@ImportActivity
                ).apply {
                    text = "Імпорт"
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
                AppThemeManager.surfaceDrawable(
                    context = this@ImportActivity,
                    fill = AppThemeManager.palette(this@ImportActivity).surface,
                    radiusDp = 14,
                    accentStroke = true
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
                dp(10),
                0,
                dp(6)
            )
        }

    private fun infoText(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 12.5f
            setTextColor(MUTED)
            setPadding(
                0,
                dp(7),
                0,
                dp(10)
            )
        }

    private fun actionButton(
        label: String,
        primary: Boolean,
        topMarginDp: Int = 0,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            minimumHeight = dp(58)
            minHeight = dp(58)
            setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(10)
            )
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 14
            )
            background =
                if (primary) {
                    AppThemeManager.accentButtonDrawable(this@ImportActivity, 11)
                } else {
                    AppThemeManager.neutralButtonDrawable(this@ImportActivity, 11)
                }
            setOnClickListener {
                action()
            }
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(topMarginDp)
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

    companion object {
        const val EXTRA_IMPORT_MESSAGE =
            "import_message"

        const val EXTRA_CLEAR_WORKSPACE =
            "clear_current_workspace"

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
