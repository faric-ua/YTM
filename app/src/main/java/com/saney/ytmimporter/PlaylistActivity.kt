package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)
        currentPlaylistStore =
            CurrentPlaylistStore(this)
    }

    override fun onResume() {
        super.onResume()
        render()
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
                "Усі треки, фільтри, ручний вибір і YTM Project",
            primary = false
        ) {
            startActivityForResult(
                Intent(
                    this,
                    ReviewActivity::class.java
                ),
                reviewRequestCode
            )
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
            title = "Заміни / проблемні треки",
            subtitle =
                "Ручні заміни, пропуски, дублікати та помилки",
            primary = false
        ) {
            finishWithAction(
                ACTION_REPLACEMENTS
            )
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
                finishWithAction(
                    ACTION_OPEN_YTM
                )
            }

            addAction(
                content = content,
                title = "Копіювати посилання",
                subtitle =
                    "Скопіювати URL цільового YTM плейлиста",
                primary = false
            ) {
                finishWithAction(
                    ACTION_COPY_LINK
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
                        "\${tracks.size} треків • ✓ \$ready  ! \$review  " +
                            "⧉ \$duplicates  ⏳ \$pending  × \$problems"
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
                        "Джерело: \${snapshot.sourceLabel}"
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
                    "\$title\n\$subtitle"
                isAllCaps = false
                textSize = 15f
                gravity =
                    Gravity.START or
                        Gravity.CENTER_VERTICAL
                setTextColor(
                    palette.text
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
    }
}
