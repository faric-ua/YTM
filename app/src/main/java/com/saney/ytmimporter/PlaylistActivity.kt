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
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome
import kotlin.math.roundToInt

class PlaylistActivity : Activity() {
    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private val reviewRequestCode =
        4701

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

        replacementDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_REPLACEMENT_DIALOG_OPEN,
                    false
                )
                ?: false
    }

    override fun onResume() {
        super.onResume()
        render()

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
            STATE_REPLACEMENT_DIALOG_OPEN,
            replacementDialogOpen
        )
        super.onSaveInstanceState(
            outState
        )
    }

    override fun onDestroy() {
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
            requestCode != reviewRequestCode ||
            resultCode != RESULT_OK ||
            data == null
        ) {
            return
        }

        when {
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
                finishWithAction(
                    ACTION_CREATE
                )

            !data.getStringExtra(
                ReviewActivity.EXTRA_MANUAL_VIDEO_ID
            ).isNullOrBlank() -> {
                setResult(
                    RESULT_OK,
                    Intent()
                        .putExtra(
                            EXTRA_ACTION,
                            ACTION_MANUAL_VIDEO
                        )
                        .putExtra(
                            ReviewActivity.EXTRA_MANUAL_VIDEO_ID,
                            data.getStringExtra(
                                ReviewActivity.EXTRA_MANUAL_VIDEO_ID
                            )
                        )
                        .putExtra(
                            ReviewActivity.EXTRA_MANUAL_HISTORY_INDEX,
                            data.getIntExtra(
                                ReviewActivity.EXTRA_MANUAL_HISTORY_INDEX,
                                Int.MIN_VALUE
                            )
                        )
                )
                finish()
            }
        }
    }

    private fun render() {
        val snapshot =
            currentPlaylistStore.load()

        if (snapshot == null) {
            renderEmpty()
        } else {
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
            finishWithAction(
                ACTION_SEARCH
            )
        }

        addAction(
            content = content,
            title = "Створити / додати в YTM",
            subtitle =
                "Новий плейлист або додавання в існуючий",
            primary = true
        ) {
            finishWithAction(
                ACTION_CREATE
            )
        }

        addAction(
            content = content,
            title = "YTM Project / export",
            subtitle =
                "Зберегти або поділитися поточним робочим проєктом",
            primary = false
        ) {
            openReview(
                openProjectActions = true
            )
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

            addView(
                TextView(
                    this@PlaylistActivity
                ).apply {
                    text =
                        "${tracks.size} треків • ✓ $ready  ! $review  " +
                            "⧉ $duplicates  ⏳ $pending  × $problems"
                    textSize = 13.5f
                    setTextColor(
                        palette.text
                    )
                    setPadding(
                        0,
                        dp(8),
                        0,
                        0
                    )
                }
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
                "[дублікат — write-запит пропущено]"

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
        openProjectActions: Boolean = false
    ) {
        startActivityForResult(
            Intent(
                this,
                ReviewActivity::class.java
            ).apply {
                putExtra(
                    ReviewActivity.EXTRA_OPEN_PROJECT_ACTIONS,
                    openProjectActions
                )
            },
            reviewRequestCode
        )
    }

    private fun finishWithAction(
        action: String
    ) {
        setResult(
            RESULT_OK,
            Intent().putExtra(
                EXTRA_ACTION,
                action
            )
        )
        finish()
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
        const val ACTION_REPLACEMENTS =
            "REPLACEMENTS"
        const val ACTION_OPEN_YTM =
            "OPEN_YTM"
        const val ACTION_COPY_LINK =
            "COPY_LINK"
        const val ACTION_MANUAL_VIDEO =
            "MANUAL_VIDEO"

        private const val STATE_REPLACEMENT_DIALOG_OPEN =
            "playlist_replacement_dialog_open"
    }
}
