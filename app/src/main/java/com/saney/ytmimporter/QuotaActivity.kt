package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

class QuotaActivity : Activity() {
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var pendingJobStore: PendingJobStore
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        quotaTracker =
            QuotaTracker(this)
        pendingJobStore =
            PendingJobStore(this)

        render()
    }

    override fun onResume() {
        super.onResume()

        if (::root.isInitialized) {
            render()
        }
    }

    private fun render() {
        val palette =
            AppThemeManager.palette(this)

        root =
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
                    dp(18)
                )
            }

        val quota =
            quotaTracker.snapshot()
        val jobs =
            pendingJobStore.getAll()

        content.addView(
            sectionTitle(
                "Локальна оцінка"
            )
        )

        content.addView(
            card().apply {
                addView(
                    statLine(
                        "Search запити",
                        "${quota.searchCalls}/${QuotaTracker.SEARCH_DAILY_LIMIT}"
                    )
                )
                addView(
                    statLine(
                        "Залишилось пошуків",
                        "≈ ${quota.searchRemaining}"
                    )
                )
                addView(
                    statLine(
                        "Загальні API units",
                        "${quota.generalUnits}/${QuotaTracker.GENERAL_DAILY_LIMIT}"
                    )
                )
                addView(
                    statLine(
                        "Залишилось API units",
                        "≈ ${quota.generalRemaining}"
                    )
                )
                addView(
                    statLine(
                        "Попадань у кеш",
                        quota.cacheHits.toString()
                    )
                )
                addView(
                    statLine(
                        "Завдань у черзі",
                        jobs.size.toString()
                    )
                )
            }
        )

        content.addView(
            sectionTitle(
                "День квоти"
            )
        )

        content.addView(
            card().apply {
                addView(
                    bodyText(
                        "${quota.dayKey} (Pacific Time)\n\n" +
                            "Це локальна оцінка лише тих операцій, " +
                            "які YTM Importer зафіксував на цьому телефоні. " +
                            "Точний стан квоти знаходиться в Google Cloud Console."
                    )
                )
            }
        )

        if (!quota.lastQuotaError.isNullOrBlank()) {
            content.addView(
                sectionTitle(
                    "Остання помилка квоти"
                )
            )

            content.addView(
                card().apply {
                    addView(
                        bodyText(
                            quota.lastQuotaError
                                ?: ""
                        )
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

        root.addView(
            footer()
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
                        this@QuotaActivity,
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
                    activity = this@QuotaActivity,
                    label = "Квота API"
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

    private fun footer():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
            )

            addView(
                actionButton(
                    label = "Google Cloud",
                    primary = false
                ) {
                    openGoogleCloudQuota()
                },
                LinearLayout.LayoutParams(
                    0,
                    dp(54),
                    1f
                )
            )

            addView(
                actionButton(
                    label = "Черга",
                    primary = true
                ) {
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(
                            EXTRA_ACTION,
                            ACTION_OPEN_QUEUE
                        )
                    )
                    finish()
                },
                LinearLayout.LayoutParams(
                    0,
                    dp(54),
                    1f
                ).apply {
                    marginStart =
                        dp(8)
                }
            )
        }

    private fun card():
        LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
            )
            background =
                AppThemeManager
                    .largeCardDrawable(
                        context =
                            this@QuotaActivity,
                        fill =
                            palette.surface
                    )
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(8)
                }
        }
    }

    private fun sectionTitle(
        label: String
    ): TextView {
        val palette =
            AppThemeManager.palette(this)

        return TextView(this).apply {
            text = label
            textSize = 13f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                palette.muted
            )
            setPadding(
                dp(4),
                dp(8),
                0,
                dp(6)
            )
        }
    }

    private fun statLine(
        label: String,
        value: String
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                0,
                dp(4),
                0,
                dp(4)
            )

            addView(
                TextView(
                    this@QuotaActivity
                ).apply {
                    text = label
                    textSize = 14f
                    setTextColor(
                        palette.muted
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(
                    this@QuotaActivity
                ).apply {
                    text = value
                    textSize = 14.5f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                }
            )
        }
    }

    private fun bodyText(
        value: String
    ): TextView {
        val palette =
            AppThemeManager.palette(this)

        return TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(
                palette.text
            )
            setLineSpacing(
                0f,
                1.08f
            )
            setTextIsSelectable(true)
        }
    }

    private fun actionButton(
        label: String,
        primary: Boolean,
        onClick: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            setTextColor(
                palette.text
            )
            background =
                if (primary) {
                    AppThemeManager
                        .accentButtonDrawable(
                            this@QuotaActivity
                        )
                } else {
                    AppThemeManager
                        .neutralButtonDrawable(
                            this@QuotaActivity
                        )
                }
            setOnClickListener {
                onClick()
            }
        }
    }

    private fun openGoogleCloudQuota() {
        val url =
            "https://console.cloud.google.com/apis/api/" +
                "youtube.googleapis.com/quotas"

        runCatching {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }
    }

    private fun dp(value: Int): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    companion object {
        const val EXTRA_ACTION =
            "quota_action"
        const val ACTION_OPEN_QUEUE =
            "OPEN_QUEUE"
    }
}
