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
import android.widget.Toast
import com.saney.ytmimporter.bulk.BulkSyncQaFaultKind
import com.saney.ytmimporter.storage.BulkSyncQaFaultStore
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.ScrollPositionState
import com.saney.ytmimporter.ui.SelectableTextSurfaceState
import com.saney.ytmimporter.ui.UiChrome

class QuotaActivity : Activity() {
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var pendingJobStore: PendingJobStore
    private lateinit var root: LinearLayout
    private lateinit var scrollView: ScrollView

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    private val selectableTextSurfaceState =
        SelectableTextSurfaceState(
            STATE_SELECTABLE_TEXT
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        quotaTracker =
            QuotaTracker(this)
        pendingJobStore =
            PendingJobStore(this)

        scrollPosition.restore(
            savedInstanceState
        )
        selectableTextSurfaceState.restore(
            savedInstanceState
        )

        render()
    }

    override fun onResume() {
        super.onResume()

        if (::root.isInitialized) {
            captureScrollPosition()
            render()
        }
    }

    override fun onPause() {
        captureScrollPosition()
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
        selectableTextSurfaceState.save(
            outState
        )
        super.onSaveInstanceState(
            outState
        )
    }

    private fun captureScrollPosition() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }
    }

    private fun render() {
        captureScrollPosition()

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
                        "Інші API units",
                        "${quota.generalUnits}/${QuotaTracker.GENERAL_DAILY_LIMIT}"
                    )
                )
                addView(
                    statLine(
                        "Залишилось інших units",
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
                            "Search має окрему денну квоту. " +
                            "10 000 units стосуються інших YouTube Data API endpoint-ів.\n\n" +
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

        if (BuildConfig.DEBUG) {
            val qaFaultStore =
                BulkSyncQaFaultStore(this)
            val armedFault =
                qaFaultStore.peek()
            val rollbackInterruptArmed =
                qaFaultStore
                    .isRollbackInterruptAfterOneArmed()
    
            content.addView(
                sectionTitle(
                    "QA — Test 5"
                )
            )
    
            content.addView(
                card().apply {
                    addView(
                        bodyText(
                            if (armedFault == null) {
                                "Одноразовий контрольований тест паузи. " +
                                    "Після ввімкнення наступний Bulk insert не буде " +
                                    "відправлено в YouTube: застосунок отримає " +
                                    "симульоване повідомлення про вичерпання добової квоти. " +
                                    "Після одного спрацювання QA fault автоматично вимикається."
                            } else {
                                "QA fault увімкнено: наступний Bulk insert симулює " +
                                    "вичерпання добової квоти без remote insert. " +
                                    "Після спрацювання режим вимкнеться автоматично."
                            }
                        )
                    )
    
                    addView(
                        actionButton(
                            label =
                                if (armedFault == null) {
                                    "Увімкнути Test 5 quota pause"
                                } else {
                                    "Скасувати Test 5 fault"
                                },
                            primary =
                                armedFault == null
                        ) {
                            if (armedFault == null) {
                                qaFaultStore.arm(
                                    BulkSyncQaFaultKind
                                        .DAILY_QUOTA
                                )
                                Toast.makeText(
                                    this@QuotaActivity,
                                    "Test 5: наступний Bulk insert симулює daily quota.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                qaFaultStore.clear()
                                Toast.makeText(
                                    this@QuotaActivity,
                                    "Test 5 fault вимкнено.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
    
                            render()
                        },
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dp(54)
                        ).apply {
                            topMargin =
                                dp(12)
                        }
                    )
                }
            )
    
            content.addView(
                sectionTitle(
                    "QA — Test 8"
                )
            )
    
            content.addView(
                card().apply {
                    addView(
                        bodyText(
                            if (!rollbackInterruptArmed) {
                                "Одноразовий контрольований тест interrupted rollback. " +
                                    "Після ввімкнення наступний Bulk rollback виконає " +
                                    "одну exact reverse mutation, збереже її як ROLLED_BACK " +
                                    "і навмисно обірве worker, залишивши сесію у ROLLING_BACK. " +
                                    "Після перезапуску застосунку сесія має перейти у " +
                                    "ROLLBACK_PAUSED без автоматичного продовження."
                            } else {
                                "Test 8 interrupt увімкнено: наступний Bulk rollback " +
                                    "зупиниться одразу після першої успішно збереженої " +
                                    "reverse mutation. Fault одноразовий."
                            }
                        )
                    )
    
                    addView(
                        actionButton(
                            label =
                                if (!rollbackInterruptArmed) {
                                    "Увімкнути Test 8"
                                } else {
                                    "Скасувати Test 8"
                                },
                            primary =
                                !rollbackInterruptArmed
                        ) {
                            if (!rollbackInterruptArmed) {
                                qaFaultStore
                                    .armRollbackInterruptAfterOne()
                                Toast.makeText(
                                    this@QuotaActivity,
                                    "Test 8: rollback буде перервано після 1 mutation.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                qaFaultStore
                                    .clearRollbackInterruptAfterOne()
                                Toast.makeText(
                                    this@QuotaActivity,
                                    "Test 8 interrupt вимкнено.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
    
                            render()
                        },
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dp(54)
                        ).apply {
                            topMargin =
                                dp(12)
                        }
                    )
                }
            )
    
    
        }

        scrollView.addView(content)

        root.addView(
            scrollView,
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

        scrollPosition.restoreInto(
            scrollView
        )
        selectableTextSurfaceState.attach(
            root = root,
            newSurfaceId = SURFACE_QUOTA
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
        private const val STATE_SCROLL_POSITION =
            "quota_scroll_position"

        private const val STATE_SELECTABLE_TEXT =
            "quota_selectable_text"

        private const val SURFACE_QUOTA =
            "quota"

        const val EXTRA_ACTION =
            "quota_action"
        const val ACTION_OPEN_QUEUE =
            "OPEN_QUEUE"
    }
}
