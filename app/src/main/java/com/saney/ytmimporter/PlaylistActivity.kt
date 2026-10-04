package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.saney.ytmimporter.model.PlaylistLinkagePolicy
import com.saney.ytmimporter.model.PlaylistLinkageState
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import com.saney.ytmimporter.storage.SafTreeFileWriter
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.ui.ScrollPositionState
import com.saney.ytmimporter.ui.SafFileSaveFlow
import com.saney.ytmimporter.ui.SelectableTextSurfaceState
import java.io.File
import kotlin.math.roundToInt

class PlaylistActivity : Activity() {
    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private var scrollView:
        ScrollView? =
        null

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    private val selectableTextSurfaceState =
        SelectableTextSurfaceState(
            STATE_SELECTABLE_TEXT
        )

    private var renderedLocalPlaylistId:
        String? =
        null

    private val reviewRequestCode =
        4701

    private val destinationRequestCode =
        4702

    private val projectSaveRequestCode =
        4703

    private var projectDialogOpen =
        false

    private var projectDialog:
        Dialog? =
        null

    private var pendingProjectExport:
        String? =
        null

    private var pendingProjectDisplayName:
        String? =
        null

    private var pendingProjectSuggestedFileName:
        String? =
        null

    private var replacementDialogOpen =
        false

    private var replacementDialog:
        Dialog? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)
        currentPlaylistStore =
            CurrentPlaylistStore(this)

        projectDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_PROJECT_DIALOG_OPEN,
                    false
                )
                ?: false

        replacementDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_REPLACEMENT_DIALOG_OPEN,
                    false
                )
                ?: false

        scrollPosition.restore(
            savedInstanceState
        )
        selectableTextSurfaceState.restore(
            savedInstanceState
        )
        renderedLocalPlaylistId =
            savedInstanceState
                ?.getString(
                    STATE_RENDERED_LOCAL_PLAYLIST_ID
                )
    }

    override fun onResume() {
        super.onResume()
        captureScrollPosition()
        render()

        if (
            projectDialogOpen &&
            projectDialog
                ?.isShowing != true
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showProjectActions()
                }
            }
        }

        if (
            replacementDialogOpen &&
            replacementDialog
                ?.isShowing != true
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showReplacementLog()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_PROJECT_DIALOG_OPEN,
            projectDialogOpen
        )

        outState.putBoolean(
            STATE_REPLACEMENT_DIALOG_OPEN,
            replacementDialogOpen
        )

        scrollPosition.save(
            outState,
            scrollView
        )
        selectableTextSurfaceState.save(
            outState
        )
        outState.putString(
            STATE_RENDERED_LOCAL_PLAYLIST_ID,
            renderedLocalPlaylistId
        )
        super.onSaveInstanceState(
            outState
        )
    }

    override fun onPause() {
        captureScrollPosition()
        super.onPause()
    }

    private fun captureScrollPosition() {
        scrollPosition.capture(
            scrollView
        )
    }

    override fun onDestroy() {
        projectDialog
            ?.setOnDismissListener(
                null
            )
        projectDialog =
            null

        replacementDialog
            ?.setOnDismissListener(
                null
            )
        replacementDialog =
            null
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

        if (
            resultCode != RESULT_OK ||
            data == null
        ) {
            if (
                requestCode ==
                    projectSaveRequestCode
            ) {
                clearPendingProjectExport()
            }
            return
        }

        when (requestCode) {
            projectSaveRequestCode -> {
                val uri =
                    data.data
                        ?: return clearPendingProjectExport()

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

            destinationRequestCode ->
                forwardDestinationResult(
                    data
                )

            reviewRequestCode ->
                when {
                    data.getBooleanExtra(
                        ReviewActivity.EXTRA_DESTINATION_RESULT,
                        false
                    ) ->
                        forwardDestinationResult(
                            data
                        )

                    data.getBooleanExtra(
                        ReviewActivity.EXTRA_REPEAT_SEARCH,
                        false
                    ) ->
                        finishWithAction(
                            ACTION_REPEAT_SEARCH
                        )

                    data.getBooleanExtra(
                        ReviewActivity.EXTRA_OPEN_DESTINATION,
                        false
                    ) ->
                        openDestination()

                    !data.getStringExtra(
                        ReviewActivity.EXTRA_MANUAL_VIDEO_ID
                    ).isNullOrBlank() ->
                        finishWithAction(
                            ACTION_MANUAL_VIDEO,
                            data
                        )
                }
        }
    }

    private fun render() {
        captureScrollPosition()

        val snapshot =
            currentPlaylistStore.load()

        if (snapshot == null) {
            renderedLocalPlaylistId = null
            scrollPosition.reset()
            scrollView = null
            renderEmpty()
        } else {
            if (
                renderedLocalPlaylistId != null &&
                renderedLocalPlaylistId !=
                    snapshot.localPlaylistId
            ) {
                scrollPosition.reset()
            }

            renderedLocalPlaylistId =
                snapshot.localPlaylistId
            renderPlaylist(snapshot)
        }
    }

    private fun renderEmpty() {
        val palette =
            AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }

        root.addView(
            topBar()
        )

        root.addView(
            TextView(this).apply {
                text =
                    "Немає активного плейлиста.\n\n" +
                        "Поверніться на Home і почніть з «1. Імпорт»."
                gravity =
                    Gravity.CENTER
                textSize = 15f
                setTextColor(
                    palette.muted
                )
                setPadding(
                    dp(24),
                    dp(48),
                    dp(24),
                    dp(48)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        UiChrome.applyScreenInsets(
            this,
            root
        )
        selectableTextSurfaceState.attach(
            root = root,
            newSurfaceId = SURFACE_EMPTY
        )
    }

    private fun renderPlaylist(
        snapshot: CurrentPlaylistSnapshot
    ) {
        val palette =
            AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }

        root.addView(
            topBar()
        )

        scrollView =
            ScrollView(this).apply {
                isFillViewport = true
            }

        val scroll =
            requireNotNull(
                scrollView
            )

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(20)
                )
            }

        content.addView(
            summaryCard(snapshot)
        )

        addAction(
            content = content,
            title = "Треки / перевірка",
            subtitle =
                "Усі треки, фільтри та ручний вибір",
            primary = false
        ) {
            openReview()
        }

        addAction(
            content = content,
            title = "Знайти / перевірити",
            subtitle =
                "Запустити пошук лише там, де він потрібен",
            primary = false
        ) {
            openReview(
                autoSearch = true
            )
        }

        addAction(
            content = content,
            title = "Створити / додати в YTM",
            subtitle =
                "Новий плейлист або додавання в існуючий",
            primary = true
        ) {
            openDestination()
        }

        addAction(
            content = content,
            title = "Проєкт YTM / експорт",
            subtitle =
                "Зберегти або поділитися поточним робочим проєктом",
            primary = false
        ) {
            showProjectActions()
        }

        addAction(
            content = content,
            title = "Заміни / проблемні треки",
            subtitle =
                "Ручні заміни, пропуски, дублікати та помилки",
            primary = false
        ) {
            showReplacementLog()
        }

        if (
            !snapshot
                .destinationPlaylistId
                .isNullOrBlank()
        ) {
            addAction(
                content = content,
                title = "Відкрити в YouTube Music",
                subtitle =
                    "Перейти до останнього цільового плейлиста",
                primary = false
            ) {
                openTargetInYtm(
                    requireNotNull(
                        snapshot.destinationPlaylistId
                    )
                )
            }

            addAction(
                content = content,
                title = "Копіювати посилання",
                subtitle =
                    "Скопіювати URL цільового YTM плейлиста",
                primary = false
            ) {
                copyTargetLink(
                    requireNotNull(
                        snapshot.destinationPlaylistId
                    )
                )
            }
        } else {
            content.addView(
                TextView(this).apply {
                    text =
                        "Посилання на YTM з’явиться тут після створення " +
                            "або вибору цільового плейлиста."
                    textSize = 12.5f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(12),
                        dp(4),
                        dp(12),
                        dp(10)
                    )
                }
            )
        }

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
        UiChrome.applyScreenInsets(
            this,
            root
        )
        scrollPosition.restoreInto(
            scroll
        )
        selectableTextSurfaceState.attach(
            root = root,
            newSurfaceId =
                "playlist:" +
                    snapshot.localPlaylistId
        )
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
                UiChrome.backButton(
                    activity =
                        this@PlaylistActivity,
                    onClick = {
                        finish()
                    }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                UiChrome.emphasizedTitle(
                    activity =
                        this@PlaylistActivity,
                    label =
                        "Поточний плейлист",
                    textSizeSp = 20f,
                    maxLines = 2
                ).apply {
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

    private fun summaryCard(
        snapshot: CurrentPlaylistSnapshot
    ):
        LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        val tracks =
            snapshot.playlist.tracks

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

        val duplicates =
            tracks.count {
                it.status ==
                    TrackStatus.DUPLICATE
            }

        val pending =
            tracks.count {
                it.status ==
                    TrackStatus.PENDING
            }

        val problems =
            tracks.count {
                it.status in
                    setOf(
                        TrackStatus.MISSING,
                        TrackStatus.FAILED
                    )
            }

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
            )
            background =
                AppThemeManager
                    .largeCardDrawable(
                        context =
                            this@PlaylistActivity,
                        fill =
                            palette.surface,
                        radiusDp = 14,
                        accentOverride =
                            palette.accent
                    )

            addView(
                TextView(
                    this@PlaylistActivity
                ).apply {
                    text =
                        snapshot.playlist.name
                    textSize = 20f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                    maxLines = 3
                }
            )

            val linkageState =
                PlaylistLinkagePolicy.current(
                    tracks = tracks,
                    destinationPlaylistId =
                        snapshot.destinationPlaylistId
                )

            addView(
                TextView(
                    this@PlaylistActivity
                ).apply {
                    text =
                        buildString {
                            append(
                                PlaylistLinkagePolicy
                                    .label(linkageState)
                            )

                            if (
                                linkageState in
                                    setOf(
                                        PlaylistLinkageState
                                            .LINKED_YTM,
                                        PlaylistLinkageState
                                            .PENDING_WRITE
                                    ) &&
                                !snapshot
                                    .destinationPlaylistTitle
                                    .isNullOrBlank()
                            ) {
                                append(": ")
                                append(
                                    snapshot
                                        .destinationPlaylistTitle
                                )
                            }

                            if (
                                !snapshot
                                    .destinationPlaylistId
                                    .isNullOrBlank()
                            ) {
                                append("\nYTM ID: ")
                                append(
                                    snapshot
                                        .destinationPlaylistId
                                )
                            }
                        }
                    textSize = 13f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.accent
                    )
                    setTextIsSelectable(true)
                    setPadding(
                        0,
                        dp(7),
                        0,
                        0
                    )
                }
            )

            addView(
                playlistResultBlock(
                    total = tracks.size,
                    ready = ready,
                    review = review,
                    duplicates = duplicates,
                    pending = pending,
                    problems = problems
                )
            )

            addView(
                TextView(
                    this@PlaylistActivity
                ).apply {
                    text =
                        "Джерело: ${snapshot.sourceLabel}"
                    textSize = 12.5f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        0,
                        dp(5),
                        0,
                        0
                    )
                    maxLines = 2
                }
            )
        }.also { card ->
            card.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(12)
                }
        }
    }

    private fun playlistResultBlock(
        total: Int,
        ready: Int,
        review: Int,
        duplicates: Int,
        pending: Int,
        problems: Int
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                0,
                dp(8),
                0,
                0
            )

            addView(
                TextView(
                    this@PlaylistActivity
                ).apply {
                    text =
                        "Усього треків: $total • Готові: $ready"
                    textSize =
                        13.5f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                }
            )

            addView(
                TextView(
                    this@PlaylistActivity
                ).apply {
                    text =
                        "Перевірити: $review • Дублікати: $duplicates\n" +
                            "Очікує: $pending • Проблеми: $problems"
                    textSize =
                        13f
                    setTextColor(
                        if (
                            problems > 0
                        ) {
                            palette.semantic.warning
                        } else {
                            palette.text
                        }
                    )
                    setPadding(
                        0,
                        dp(4),
                        0,
                        0
                    )
                }
            )
        }
    }

    private fun addAction(
        content: LinearLayout,
        title: String,
        subtitle: String,
        primary: Boolean,
        onClick: () -> Unit
    ) {
        val palette =
            AppThemeManager.palette(this)

        val button =
            Button(this).apply {
                text =
                    "$title\n$subtitle"
                isAllCaps = false
                textSize = 15f
                gravity =
                    Gravity.START or
                        Gravity.CENTER_VERTICAL
                setTextColor(
                    if (primary) {
                        Color.WHITE
                    } else {
                        palette.text
                    }
                )
                setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(12)
                )
                minimumHeight =
                    dp(76)
                background =
                    if (primary) {
                        AppThemeManager
                            .accentButtonDrawable(
                                context =
                                    this@PlaylistActivity,
                                radiusDp = 12
                            )
                    } else {
                        AppThemeManager
                            .surfaceDrawable(
                                context =
                                    this@PlaylistActivity,
                                fill =
                                    palette.surfaceAlt,
                                radiusDp = 12,
                                accentStroke = false
                            )
                    }
                setOnClickListener {
                    onClick()
                }
            }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin =
                    dp(9)
            }
        )
    }

    private fun showReplacementLog() {
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return toast(
                    "Немає імпортованого плейлиста"
                )

        val problemTracks =
            snapshot.playlist.tracks
                .filter { track ->
                    track.manuallySelected ||
                        track.status ==
                            TrackStatus.SKIPPED ||
                        track.status ==
                            TrackStatus.DUPLICATE ||
                        track.status ==
                            TrackStatus.MISSING ||
                        track.status ==
                            TrackStatus.PENDING ||
                        track.status ==
                            TrackStatus.FAILED
                }

        if (problemTracks.isEmpty()) {
            replacementDialogOpen = false
            return toast(
                "Замін, пропусків або проблемних треків поки немає"
            )
        }

        replacementDialogOpen = true

        val shortText =
            buildShortReplacementText(
                problemTracks
            )

        val fullText =
            buildFullReplacementText(
                problemTracks
            )

        replacementDialog =
            UiChrome.showRecordDialog(
                activity = this,
                title =
                    "Заміни / проблемні треки: " +
                        problemTracks.size,
                subtitle =
                    "Кожна позиція показана окремою плиткою.",
                records =
                    problemTracks.mapIndexed {
                        index,
                        track ->
                        UiChrome.DialogRecord(
                            title =
                                "${index + 1}. " +
                                    "${track.originalArtist} — " +
                                    track.originalTitle,
                            detail =
                                replacementRecordLabel(
                                    track
                                ),
                            tone =
                                if (
                                    track.manuallySelected
                                ) {
                                    UiChrome.ActionTone.ACCENT
                                } else {
                                    UiChrome.ActionTone.NORMAL
                                }
                        )
                    },
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            "TikTok список"
                        ) {
                            copyText(
                                label =
                                    "YTM Importer TikTok replacements",
                                text =
                                    shortText,
                                successMessage =
                                    "Короткий список для TikTok скопійовано"
                            )
                        },
                        UiChrome.DialogAction(
                            "Повний текст"
                        ) {
                            copyText(
                                label =
                                    "YTM Importer replacement log",
                                text =
                                    fullText,
                                successMessage =
                                    "Повний журнал скопійовано"
                            )
                        },
                        UiChrome.DialogAction(
                            label = "Закрити",
                            tone =
                                UiChrome.ActionTone.ACCENT
                        ) {}
                    ),
                actionLayout =
                    UiChrome.DialogActionLayout
                        .VERTICAL_WITH_TEXT_CLOSE
            ).also { dialog ->
                dialog.setOnDismissListener {
                    replacementDialogOpen = false
                    replacementDialog = null
                }
            }
    }

    private fun replacementRecordLabel(
        track: Track
    ): String =
        when {
            track.manuallySelected &&
                !track.selectedTitle
                    .isNullOrBlank() ->
                buildString {
                    append(
                        "Ручний вибір: "
                    )
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

            track.status ==
                TrackStatus.SKIPPED ->
                "Пропущено"

            track.status ==
                TrackStatus.DUPLICATE ->
                "Дублікат у цільовому плейлисті"

            track.status ==
                TrackStatus.MISSING ->
                "Не знайдено"

            track.status ==
                TrackStatus.PENDING ->
                "Очікує в Pending Queue"

            track.status ==
                TrackStatus.FAILED ->
                track.error
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        "Помилка: $it"
                    }
                    ?: "Помилка"

            else ->
                replacementLabel(
                    track
                )
        }

    private fun replacementLabel(
        track: Track
    ): String =
        when {
            track.status ==
                TrackStatus.SKIPPED ->
                "[пропущено]"

            track.status ==
                TrackStatus.DUPLICATE ->
                "[дублікат — запит на запис пропущено]"

            track.status ==
                TrackStatus.MISSING ->
                "[не знайдено]"

            track.status ==
                TrackStatus.PENDING ->
                "[очікує в черзі]"

            track.status ==
                TrackStatus.FAILED &&
                track.selectedTitle
                    .isNullOrBlank() ->
                "[помилка]"

            track.selectedTitle ==
                "Ручне посилання" ->
                "[ручне YouTube/YTM посилання]"

            !track.selectedTitle
                .isNullOrBlank() ->
                track.selectedTitle.orEmpty()

            else ->
                "[без заміни]"
        }

    private fun buildShortReplacementText(
        tracks: List<Track>
    ): String =
        buildString {
            append(
                "Заміни / недоступні треки:\n"
            )

            tracks.forEachIndexed {
                index,
                track ->
                append(index + 1)
                append(". ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append(" → ")
                append(
                    replacementLabel(
                        track
                    )
                )

                if (
                    index !=
                    tracks.lastIndex
                ) {
                    append('\n')
                }
            }
        }

    private fun buildFullReplacementText(
        tracks: List<Track>
    ): String =
        buildString {
            append(
                "YTM Importer — журнал замін\n\n"
            )

            tracks.forEachIndexed {
                index,
                track ->
                append(index + 1)
                append(". Оригінал: ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append('\n')
                append("   Результат: ")
                append(
                    replacementLabel(
                        track
                    )
                )
                append('\n')

                if (
                    !track.selectedChannel
                        .isNullOrBlank()
                ) {
                    append("   Канал: ")
                    append(
                        track.selectedChannel
                    )
                    append('\n')
                }

                if (
                    !track.selectedVideoId
                        .isNullOrBlank()
                ) {
                    append(
                        "   YTM: https://music.youtube.com/watch?v="
                    )
                    append(
                        track.selectedVideoId
                    )
                    append('\n')
                }

                if (
                    !track.error
                        .isNullOrBlank()
                ) {
                    append(
                        "   Помилка: "
                    )
                    append(
                        track.error
                    )
                    append('\n')
                }

                if (
                    index !=
                    tracks.lastIndex
                ) {
                    append('\n')
                }
            }
        }

    private fun copyText(
        label: String,
        text: String,
        successMessage: String
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

        toast(
            successMessage
        )
    }

    private fun showProjectActions() {
        if (
            projectDialog
                ?.isShowing == true
        ) {
            return
        }

        if (
            currentPlaylistStore
                .load() == null
        ) {
            projectDialogOpen =
                false
            return toast(
                "Немає активного плейлиста"
            )
        }

        projectDialogOpen =
            true

        projectDialog =
            UiChrome.showMenuDialog(
                activity = this,
                title =
                    "Поточний проєкт YTM",
                subtitle =
                    "Збереження та обмін робочим проєктом.",
                actions =
                    listOf(
                        UiChrome.MenuAction(
                            "Зберегти проєкт YTM"
                        ) {
                            saveCurrentProject()
                        },
                        UiChrome.MenuAction(
                            "Поділитися проєктом YTM"
                        ) {
                            shareCurrentProject()
                        }
                    )
            ).also { dialog ->
                dialog.setOnDismissListener {
                    projectDialogOpen =
                        false
                    projectDialog =
                        null
                }
            }
    }

    private fun currentProjectJson():
        String? {
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return null

        return PlaylistProjectCodec
            .exportWorkingPlaylist(
                playlist =
                    snapshot.playlist,
                sourceLabel =
                    snapshot.sourceLabel,
                appVersion =
                    BuildConfig.VERSION_NAME,
                sourcePlaylistId =
                    snapshot.destinationPlaylistId,
                sourcePlaylistTitle =
                    snapshot.destinationPlaylistTitle,
                sourceLocalPlaylistId =
                    snapshot.localPlaylistId
            )
    }

    private fun saveCurrentProject() {
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return toast(
                    "Немає активного плейлиста"
                )

        val content =
            currentProjectJson()
                ?: return toast(
                    "Немає активного плейлиста"
                )

        val projectName =
            snapshot.playlist.name
                .trim()
                .ifBlank {
                    "Проєкт YTM"
                }

        val suggestedFileName =
            projectFileName(
                snapshot
            )

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
                    "Куди зберегти проєкт YTM?",
                suggestedFileName =
                    suggestedFileName,
                mimeType =
                    "application/json",
                requestCode =
                    projectSaveRequestCode
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
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return toast(
                    "Немає активного плейлиста"
                )

        val content =
            currentProjectJson()
                ?: return toast(
                    "Немає активного плейлиста"
                )

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
                    projectFileName(
                        snapshot
                    )
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
                    type =
                        "application/json"
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
                    "Поділитися проєктом YTM"
                )
            )
        }.onFailure { error ->
            toast(
                "Не вдалося поділитися проєктом: " +
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
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return clearPendingProjectExport()

        val content =
            pendingProjectExport
                ?: currentProjectJson()
                ?: return clearPendingProjectExport()

        val fileName =
            pendingProjectSuggestedFileName
                ?: projectFileName(
                    snapshot
                )

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
                "Не вдалося зберегти проєкт: " +
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
                ?: currentProjectJson()
                ?: return clearPendingProjectExport()

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
                    writer.write(
                        content
                    )
                }
                ?: error(
                    "Android не відкрив файл для запису"
                )
        }.onSuccess {
            showProjectSaved(
                queryDocumentName(
                    uri
                )
                    ?: pendingProjectSuggestedFileName
            )
        }.onFailure { error ->
            toast(
                "Не вдалося зберегти проєкт: " +
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
                ?: currentPlaylistStore
                    .load()
                    ?.playlist
                    ?.name
                    ?.trim()
                    ?.ifBlank {
                        "Проєкт YTM"
                    }
                ?: "Проєкт YTM"

        toast(
            buildString {
                append(
                    "Проєкт «$projectName» збережено"
                )

                if (
                    !savedFileName
                        .isNullOrBlank()
                ) {
                    append("\n")
                    append(
                        savedFileName
                    )
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
                        cursor.getString(
                            index
                        )
                    } else {
                        null
                    }
                }
        }.getOrNull()

    private fun clearPendingProjectExport() {
        pendingProjectExport =
            null
        pendingProjectDisplayName =
            null
        pendingProjectSuggestedFileName =
            null
    }

    private fun projectFileName(
        snapshot: CurrentPlaylistSnapshot
    ): String {
        val safeName =
            snapshot.playlist.name
                .trim()
                .replace(
                    Regex(
                        "[\\/:*?\"<>|\\p{Cntrl}]"
                    ),
                    "_"
                )
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim(
                    ' ',
                    '.'
                )
                .take(100)
                .ifBlank {
                    "YTM Project"
                }

        return "$safeName.ytm.json"
    }

    private fun targetUrl(
        playlistId: String
    ): String =
        "https://music.youtube.com/playlist?list=" +
            playlistId

    private fun openTargetInYtm(
        playlistId: String
    ) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    targetUrl(
                        playlistId
                    )
                )
            )
        )
    }

    private fun copyTargetLink(
        playlistId: String
    ) {
        copyText(
            label =
                "YouTube Music playlist",
            text =
                targetUrl(
                    playlistId
                ),
            successMessage =
                "Посилання на плейлист скопійовано"
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

    private fun openReview(
        autoSearch: Boolean = false
    ) {
        startActivityForResult(
            Intent(
                this,
                ReviewActivity::class.java
            ).apply {
                putExtra(
                    ReviewActivity.EXTRA_RETURN_TO_PLAYLIST,
                    true
                )
                putExtra(
                    ReviewActivity.EXTRA_AUTO_SEARCH,
                    autoSearch
                )
            },
            reviewRequestCode
        )
    }

    private fun openDestination() {
        val current =
            currentPlaylistStore
                .load()
                ?: return toast(
                    "Немає активного плейлиста"
                )

        val targetIntent =
            DestinationActivity
                .startIntent(
                    context = this,
                    snapshot = current
                )
                ?: return toast(
                    "Немає треків для запису"
                )

        startActivityForResult(
            targetIntent,
            destinationRequestCode
        )
    }

    private fun forwardDestinationResult(
        data: Intent
    ) {
        setResult(
            RESULT_OK,
            Intent(data)
                .putExtra(
                    EXTRA_ACTION,
                    ACTION_DESTINATION_RESULT
                )
        )
        finish()
        overridePendingTransition(0, 0)
    }

    private fun finishWithAction(
        action: String,
        source: Intent? = null
    ) {
        setResult(
            RESULT_OK,
            Intent(source)
                .putExtra(
                    EXTRA_ACTION,
                    action
                )
        )
        finish()
        overridePendingTransition(0, 0)
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).roundToInt()

    companion object {
        const val EXTRA_ACTION =
            "playlist_hub_action"

        const val ACTION_SEARCH =
            "SEARCH"
        const val ACTION_REPEAT_SEARCH =
            "REPEAT_SEARCH"
        const val ACTION_CREATE =
            "CREATE"
        const val ACTION_DESTINATION_RESULT =
            "DESTINATION_RESULT"
        const val ACTION_REPLACEMENTS =
            "REPLACEMENTS"
        const val ACTION_OPEN_YTM =
            "OPEN_YTM"
        const val ACTION_COPY_LINK =
            "COPY_LINK"
        const val ACTION_MANUAL_VIDEO =
            "MANUAL_VIDEO"

        private const val STATE_PROJECT_DIALOG_OPEN =
            "playlist_project_dialog_open"

        private const val STATE_REPLACEMENT_DIALOG_OPEN =
            "playlist_replacement_dialog_open"

        private const val STATE_SCROLL_POSITION =
            "playlist_scroll_position"

        private const val STATE_SELECTABLE_TEXT =
            "playlist_selectable_text"

        private const val SURFACE_EMPTY =
            "playlist:empty"

        private const val STATE_RENDERED_LOCAL_PLAYLIST_ID =
            "playlist_rendered_local_playlist_id"
    }
}
