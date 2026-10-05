package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.recovery.RecoveryCenterSource
import com.saney.ytmimporter.recovery.RecoveryClassification
import com.saney.ytmimporter.recovery.RecoveryItem
import com.saney.ytmimporter.recovery.RecoveryRoute
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.ScrollPositionState
import com.saney.ytmimporter.ui.UiChrome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecoveryCenterActivity : Activity() {
    private lateinit var source:
        RecoveryCenterSource

    private lateinit var scrollView:
        ScrollView

    private var firstResume = true

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        source =
            RecoveryCenterSource(this)

        scrollPosition.restore(
            savedInstanceState
        )

        render()
    }

    override fun onResume() {
        super.onResume()

        if (firstResume) {
            firstResume = false
            return
        }

        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }

        render()
    }

    override fun onPause() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }

        super.onPause()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        scrollPosition.save(
            outState,
            if (::scrollView.isInitialized) {
                scrollView
            } else {
                null
            }
        )

        super.onSaveInstanceState(
            outState
        )
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
            requestCode !=
                REQUEST_PENDING ||
            resultCode != RESULT_OK ||
            data == null
        ) {
            return
        }

        val jobId =
            data.getStringExtra(
                PendingActivity
                    .EXTRA_RESUME_JOB_ID
            )

        if (!jobId.isNullOrBlank()) {
            setResult(
                RESULT_OK,
                Intent().putExtra(
                    PendingActivity
                        .EXTRA_RESUME_JOB_ID,
                    jobId
                )
            )
            finish()
        }
    }

    private fun render() {
        val palette =
            AppThemeManager.palette(this)

        val snapshot =
            source.snapshot()

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
                    "Тут зібрані незавершені або перервані " +
                        "операції з локальних збережених станів. " +
                        "Відкриття цього екрана нічого не продовжує автоматично."
                textSize = 13f
                setTextColor(
                    palette.muted
                )
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(10)
                )
            }
        )

        scrollView =
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

        if (snapshot.items.isEmpty()) {
            content.addView(
                emptyState()
            )
        } else {
            if (
                snapshot.actionableItems
                    .isNotEmpty()
            ) {
                content.addView(
                    sectionTitle(
                        "Потребує уваги • " +
                            snapshot
                                .actionableCount
                    )
                )

                snapshot.actionableItems
                    .forEach {
                        content.addView(
                            itemCard(it)
                        )
                    }
            }

            if (
                snapshot.warningItems
                    .isNotEmpty()
            ) {
                content.addView(
                    sectionTitle(
                        "Завершено з попередженням"
                    )
                )

                snapshot.warningItems
                    .forEach {
                        content.addView(
                            itemCard(it)
                        )
                    }
            }
        }

        scrollView.addView(
            content
        )

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams
                    .MATCH_PARENT,
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
            scrollView
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
                        this@RecoveryCenterActivity,
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
                        this@RecoveryCenterActivity,
                    label =
                        "Потребує уваги"
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
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT,
                    1f
                )
            )
        }

    private fun sectionTitle(
        value: String
    ): TextView =
        TextView(this).apply {
            text = value
            textSize = 17f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                AppThemeManager
                    .palette(
                        this@RecoveryCenterActivity
                    )
                    .text
            )
            setPadding(
                dp(6),
                dp(12),
                dp(6),
                dp(8)
            )
        }

    private fun emptyState():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                dp(18),
                dp(30),
                dp(18),
                dp(30)
            )
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@RecoveryCenterActivity,
                        fill =
                            AppThemeManager
                                .palette(
                                    this@RecoveryCenterActivity
                                )
                                .surface,
                        radiusDp = 16,
                        accentStroke = true
                    )

            addView(
                TextView(
                    this@RecoveryCenterActivity
                ).apply {
                    text =
                        "✓ Нічого незавершеного"
                    textSize = 18f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        Color.rgb(
                            70,
                            215,
                            130
                        )
                    )
                }
            )

            addView(
                TextView(
                    this@RecoveryCenterActivity
                ).apply {
                    text =
                        "Черга, Bulk-сесії та History " +
                            "не містять станів, що потребують дії."
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setTextColor(
                        AppThemeManager
                            .palette(
                                this@RecoveryCenterActivity
                            )
                            .muted
                    )
                    setPadding(
                        0,
                        dp(8),
                        0,
                        0
                    )
                }
            )
        }

    private fun itemCard(
        item: RecoveryItem
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(14),
                dp(13),
                dp(14),
                dp(13)
            )
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@RecoveryCenterActivity,
                        fill =
                            palette.surface,
                        radiusDp = 14,
                        accentStroke =
                            item.classification ==
                                RecoveryClassification
                                    .ACTION_REQUIRED
                    )

            addView(
                TextView(
                    this@RecoveryCenterActivity
                ).apply {
                    text =
                        if (
                            item.classification ==
                            RecoveryClassification
                                .ACTION_REQUIRED
                        ) {
                            "⚠ " + item.title
                        } else {
                            "ⓘ " + item.title
                        }
                    textSize = 17f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        if (
                            item.classification ==
                            RecoveryClassification
                                .ACTION_REQUIRED
                        ) {
                            Color.rgb(
                                255,
                                195,
                                80
                            )
                        } else {
                            palette.text
                        }
                    )
                }
            )

            addView(
                TextView(
                    this@RecoveryCenterActivity
                ).apply {
                    text =
                        item.stateLabel +
                            "
" +
                            "Оновлено: " +
                            formatDate(
                                item.updatedAt
                            )
                    textSize = 13f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        0,
                        dp(6),
                        0,
                        0
                    )
                }
            )

            addView(
                TextView(
                    this@RecoveryCenterActivity
                ).apply {
                    text =
                        buildString {
                            append(
                                item.happenedLabel
                            )

                            item.remainingLabel
                                ?.let {
                                    append("\n")
                                    append(it)
                                }

                            item.reason
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?.let {
                                    append("\nПричина: ")
                                    append(it)
                                }
                        }
                    textSize = 13f
                    setTextColor(
                        palette.text
                    )
                    setPadding(
                        0,
                        dp(8),
                        0,
                        dp(10)
                    )
                    setTextIsSelectable(
                        true
                    )
                }
            )

            addView(
                Button(
                    this@RecoveryCenterActivity
                ).apply {
                    text =
                        routeLabel(
                            item.route
                        )
                    isAllCaps = false
                    setOnClickListener {
                        openItem(item)
                    }
                }
            )
        }.also {
            it.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams
                        .MATCH_PARENT,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(10)
                }
        }
    }

    private fun openItem(
        item: RecoveryItem
    ) {
        when (item.route) {
            RecoveryRoute.BULK_SESSION ->
                startActivity(
                    Intent(
                        this,
                        BulkSyncSessionActivity
                            ::class.java
                    ).putExtra(
                        BulkSyncSessionActivity
                            .EXTRA_SESSION_ID,
                        item.routeId
                    )
                )

            RecoveryRoute.PENDING_QUEUE ->
                startActivityForResult(
                    Intent(
                        this,
                        PendingActivity::class.java
                    ).putExtra(
                        PendingActivity
                            .EXTRA_OPEN_JOB_ID,
                        item.routeId
                    ),
                    REQUEST_PENDING
                )

            RecoveryRoute.HISTORY_DETAIL ->
                startActivity(
                    Intent(
                        this,
                        HistoryActivity::class.java
                    ).putExtra(
                        HistoryActivity
                            .EXTRA_OPEN_ENTRY_ID,
                        item.routeId
                    )
                )
        }
    }

    private fun routeLabel(
        route: RecoveryRoute
    ): String =
        when (route) {
            RecoveryRoute.BULK_SESSION ->
                "Відкрити синхронізацію"

            RecoveryRoute.PENDING_QUEUE ->
                "Відкрити чергу"

            RecoveryRoute.HISTORY_DETAIL ->
                "Переглянути History"
        }

    private fun formatDate(
        value: Long
    ): String =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(
            Date(value)
        )

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
        private const val
            STATE_SCROLL_POSITION =
                "recovery_center_scroll_position"

        private const val REQUEST_PENDING =
            6101
    }
}
