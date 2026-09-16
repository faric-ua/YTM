package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.youtube.SearchCache

class ServiceActivity : Activity() {
    private lateinit var searchCache: SearchCache
    private lateinit var quotaTracker: QuotaTracker

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        searchCache = SearchCache(this)
        quotaTracker = QuotaTracker(this)

        buildUi()
    }

    override fun onResume() {
        super.onResume()

        if (::searchCache.isInitialized) {
            buildUi()
        }
    }

    private fun buildUi() {
        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(BACKGROUND)
            }

        root.addView(topBar())

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
            }

        val content =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(24)
                )
            }

        val cache = searchCache.stats()
        val quota = quotaTracker.snapshot()

        content.addView(
            statusCard(
                cacheEntries = cache.validEntries,
                searchCalls = quota.searchCalls,
                searchLimit = QuotaTracker.SEARCH_DAILY_LIMIT
            )
        )

        content.addView(sectionTitle("Допомога"))
        content.addView(
            serviceCard(
                title = "Швидкий старт",
                subtitle = "Як створити плейлист у 4 кроки",
                action = ACTION_QUICK_START
            )
        )
        content.addView(
            serviceCard(
                title = "Приватність",
                subtitle = "Які дані використовуються та що зберігається локально",
                action = ACTION_PRIVACY
            )
        )

        content.addView(sectionTitle("Діагностика"))
        content.addView(
            serviceCard(
                title = "Діагностика",
                subtitle = "Стан застосунку, quota, cache та History",
                action = ACTION_DIAGNOSTICS
            )
        )
        content.addView(
            serviceCard(
                title = "Поділитися Diagnostics TXT",
                subtitle = "Надіслати технічний звіт без OAuth token",
                action = ACTION_SHARE_DIAGNOSTICS
            )
        )
        content.addView(
            serviceCard(
                title = "Зберегти Diagnostics TXT",
                subtitle = "Записати технічний звіт у файл",
                action = ACTION_SAVE_DIAGNOSTICS
            )
        )

        content.addView(sectionTitle("API та локальні дані"))
        content.addView(
            serviceCard(
                title = "SearchCache",
                subtitle = "Розмір, записи та очищення кешу",
                action = ACTION_SEARCH_CACHE
            )
        )
        content.addView(
            serviceCard(
                title = "Google Cloud Console",
                subtitle = "Квота YouTube Data API",
                action = ACTION_GOOGLE_CLOUD
            )
        )

        content.addView(sectionTitle("Про застосунок"))
        content.addView(
            serviceCard(
                title = "Про YTM Importer",
                subtitle = "Версія, можливості та технічна інформація",
                action = ACTION_ABOUT
            )
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

    private fun topBar(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(10)
            )

            addView(
                Button(this@ServiceActivity).apply {
                    text = "‹"
                    isAllCaps = false
                    textSize = 26f
                    setTextColor(Color.WHITE)
                    background = roundedBackground(
                        color = SURFACE,
                        radiusDp = 12,
                        strokeColor = BORDER
                    )
                    setOnClickListener { finish() }
                },
                LinearLayout.LayoutParams(
                    dp(46),
                    dp(46)
                )
            )

            addView(
                TextView(this@ServiceActivity).apply {
                    text = "Сервіс"
                    textSize = 22f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(14), 0, 0, 0)
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }

    private fun statusCard(
        cacheEntries: Int,
        searchCalls: Int,
        searchLimit: Int
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
            )
            background = roundedBackground(
                color = SURFACE,
                radiusDp = 16,
                strokeColor = BORDER
            )

            addView(
                TextView(this@ServiceActivity).apply {
                    text = "Стан"
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                }
            )

            addView(
                TextView(this@ServiceActivity).apply {
                    text =
                        "Cache: $cacheEntries активних записів  •  " +
                            "Search quota: $searchCalls/$searchLimit"
                    textSize = 13f
                    setTextColor(MUTED)
                    setPadding(0, dp(6), 0, 0)
                }
            )
        }

    private fun sectionTitle(
        value: String
    ): TextView =
        TextView(this).apply {
            text = value
            textSize = 13f
            setTextColor(MUTED)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(
                dp(4),
                dp(14),
                0,
                dp(7)
            )
        }

    private fun serviceCard(
        title: String,
        subtitle: String,
        action: String
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(16),
                dp(13),
                dp(12),
                dp(13)
            )
            background = roundedBackground(
                color = ROW_SURFACE,
                radiusDp = 14,
                strokeColor = BORDER
            )
            isClickable = true
            isFocusable = true
            setOnClickListener {
                returnAction(action)
            }

            val textColumn =
                LinearLayout(this@ServiceActivity).apply {
                    orientation = LinearLayout.VERTICAL
                }

            textColumn.addView(
                TextView(this@ServiceActivity).apply {
                    text = title
                    textSize = 15.5f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    maxLines = 2
                }
            )

            textColumn.addView(
                TextView(this@ServiceActivity).apply {
                    text = subtitle
                    textSize = 12.5f
                    setTextColor(MUTED)
                    setPadding(0, dp(4), 0, 0)
                    maxLines = 3
                }
            )

            addView(
                textColumn,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@ServiceActivity).apply {
                    text = "›"
                    textSize = 25f
                    setTextColor(MUTED)
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(
                    dp(28),
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
        }

    private fun returnAction(
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

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(color)

            if (strokeColor != null) {
                setStroke(
                    dp(1),
                    strokeColor
                )
            }
        }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources.displayMetrics.density
        ).toInt()

    companion object {
        const val EXTRA_ACTION =
            "service_action"

        const val ACTION_QUICK_START =
            "quick_start"

        const val ACTION_PRIVACY =
            "privacy"

        const val ACTION_DIAGNOSTICS =
            "diagnostics"

        const val ACTION_SHARE_DIAGNOSTICS =
            "share_diagnostics"

        const val ACTION_SAVE_DIAGNOSTICS =
            "save_diagnostics"

        const val ACTION_SEARCH_CACHE =
            "search_cache"

        const val ACTION_GOOGLE_CLOUD =
            "google_cloud"

        const val ACTION_ABOUT =
            "about"

        private val BACKGROUND =
            Color.rgb(15, 16, 19)

        private val SURFACE =
            Color.rgb(25, 27, 32)

        private val ROW_SURFACE =
            Color.rgb(31, 33, 39)

        private val BORDER =
            Color.rgb(48, 51, 59)

        private val MUTED =
            Color.rgb(165, 167, 173)
    }
}
