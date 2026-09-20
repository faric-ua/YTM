package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

class MenuActivity : Activity() {
    private var themeDialogOpen = false
    private var themeDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        themeDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_THEME_DIALOG_OPEN,
                    false
                )
                ?: false

        render()

        if (themeDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showThemePicker()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_THEME_DIALOG_OPEN,
            themeDialogOpen
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        themeDialog
            ?.setOnDismissListener(null)
        themeDialog = null
        super.onDestroy()
    }

    private fun render() {
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
                    "Додаткові дії та сервісні інструменти."
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

        addAction(
            content = content,
            title = "Тема",
            subtitle = "Neon / Blue / Green",
            action = ACTION_THEME
        )

        addAction(
            content = content,
            title = "Поточний проєкт",
            subtitle = "Перевірити, зберегти або поділитися",
            action = ACTION_PROJECT
        )

        addAction(
            content = content,
            title = "Заміни",
            subtitle = "Перевірити ручні заміни",
            action = ACTION_REPLACEMENTS
        )

        addAction(
            content = content,
            title = "Відкрити останній плейлист у YTM",
            subtitle = "Перейти до останнього створеного або відкритого плейлиста",
            action = ACTION_OPEN_YTM
        )

        addAction(
            content = content,
            title = "Дані",
            subtitle = "Export / backup / restore",
            action = ACTION_DATA
        )

        addAction(
            content = content,
            title = "Сервіс",
            subtitle = "Допомога / diagnostics / cache",
            action = ACTION_SERVICE
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
                        this@MenuActivity,
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
                    activity = this@MenuActivity,
                    label = "Меню"
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

    private fun showThemePicker() {
        if (themeDialog?.isShowing == true) {
            return
        }

        val active =
            AppThemeManager.currentStyle(this)

        themeDialogOpen = true

        themeDialog =
            UiChrome.showMenuDialog(
                activity = this,
                title = "Тема оформлення",
                subtitle =
                    "Один інтерфейс — три палітри. Тема зберігається на пристрої.",
                actions =
                    AppThemeManager.ThemeStyle
                        .values()
                        .map { style ->
                            UiChrome.MenuAction(
                                label =
                                    (
                                        if (style == active) {
                                            "✓ "
                                        } else {
                                            ""
                                        }
                                    ) +
                                        style.marker +
                                        "  " +
                                        style.label,
                                onClick = {
                                    if (style != active) {
                                        AppThemeManager
                                            .setStyle(
                                                this,
                                                style
                                            )
                                        recreate()
                                    }
                                }
                            )
                        }
            ).also { dialog ->
                dialog.setOnDismissListener {
                    themeDialogOpen = false
                    themeDialog = null
                }
            }
    }

    private fun addAction(
        content: LinearLayout,
        title: String,
        subtitle: String,
        action: String
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
                    AppThemeManager
                        .surfaceDrawable(
                            context =
                                this@MenuActivity,
                            fill =
                                palette.surfaceAlt,
                            radiusDp = 12,
                            accentStroke = false
                        )
                setOnClickListener {
                    if (action == ACTION_THEME) {
                        showThemePicker()
                    } else {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(
                                EXTRA_ACTION,
                                action
                            )
                        )
                        finish()
                    }
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

    private fun dp(value: Int): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    companion object {
        private const val STATE_THEME_DIALOG_OPEN =
            "menu_theme_dialog_open"

        const val EXTRA_ACTION =
            "menu_action"

        const val ACTION_THEME =
            "THEME"
        const val ACTION_PROJECT =
            "PROJECT"
        const val ACTION_REPLACEMENTS =
            "REPLACEMENTS"
        const val ACTION_OPEN_YTM =
            "OPEN_YTM"
        const val ACTION_DATA =
            "DATA"
        const val ACTION_SERVICE =
            "SERVICE"
    }
}
