package com.saney.ytmimporter.ui

import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.saney.ytmimporter.R

/**
 * Batch 17: the Import «?» window, in the style of the Library help faric
 * liked (05:53: «дуже красива, дуже шикарно описана»): sections, and for each
 * button an icon, its name and what it does. Words — [ImportHelpText].
 */
object ImportHelpWindow {
    fun show(
        activity: Activity,
        help: HelpContent = ImportHelpText,
        onDone: () -> Unit
    ): Dialog {
        val landscape = activity.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        val content = content(activity, help, withLead = landscape)
        val done = UiChrome.DialogAction("Зрозуміло") { onDone() }
        return if (landscape) {
            UiChrome.showWideContentDialog(
                activity = activity,
                title = help.title,
                content = content,
                doneAction = done
            )
        } else {
            UiChrome.showContentDialog(
                activity = activity,
                title = help.title,
                subtitle = help.subtitle,
                message = help.lead,
                content = content,
                actions = listOf(done)
            )
        }
    }

    /** The «?» in the Import header — same look as the «←» next to it. */
    fun button(
        activity: Activity,
        description: String = "Довідка імпорту",
        onClick: () -> Unit
    ): android.widget.ImageButton =
        android.widget.ImageButton(activity).apply {
            contentDescription = description
            setImageResource(R.drawable.ic_ytm_help)
            imageTintList = android.content.res.ColorStateList.valueOf(
                AppThemeManager.palette(activity).text)
            scaleType = ImageView.ScaleType.CENTER
            setPadding(0, 0, 0, 0)
            minimumWidth = 0
            minimumHeight = 0
            background = AppThemeManager.surfaceDrawable(
                context = activity, fill = AppThemeManager.palette(activity).surface,
                radiusDp = 12, accentStroke = false)
            setOnClickListener { onClick() }
        }

    private fun content(activity: Activity, help: HelpContent, withLead: Boolean): View {
        val palette = AppThemeManager.palette(activity)
        fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()
        fun text(value: String, size: Float, bold: Boolean, colour: Int) =
            TextView(activity).apply {
                text = value
                textSize = size
                setTextColor(colour)
                if (bold) setTypeface(typeface, Typeface.BOLD)
            }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(2), 0, dp(7))
        }
        if (withLead) {
            content.addView(text(help.lead, 13f, false, palette.muted).apply {
                setPadding(dp(4), 0, dp(4), dp(4))
            })
        }
        help.sections.forEach { section ->
            content.addView(text(section.title, 15f, true, palette.accent).apply {
                setPadding(dp(4), dp(10), 0, dp(6))
            })
            section.entries.forEach { entry ->
                val row = LinearLayout(activity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.TOP
                    setPadding(dp(7), dp(9), dp(7), dp(9))
                    background = AppThemeManager.surfaceDrawable(
                        context = activity, fill = palette.surfaceAlt, radiusDp = 10)
                }
                row.addView(ImageView(activity).apply {
                    setImageResource(iconOf(entry.icon))
                    setColorFilter(if (entry.icon == ImportHelpText.Icon.YOUTUBE) palette.accent
                        else palette.text)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = entry.title
                }, LinearLayout.LayoutParams(dp(30), dp(32)).apply { marginEnd = dp(13) })
                val labels = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
                labels.addView(text(entry.title, 14f, true, palette.text))
                labels.addView(text(entry.text, 12.5f, false, palette.muted).apply {
                    setPadding(0, dp(4), 0, 0)
                })
                row.addView(labels, LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                content.addView(row, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(5) })
            }
        }
        return content
    }

    private fun iconOf(icon: ImportHelpText.Icon): Int = when (icon) {
        ImportHelpText.Icon.CURRENT -> R.drawable.ic_ytm_library_current
        ImportHelpText.Icon.YOUTUBE -> R.drawable.ic_ytm_youtube_play
        ImportHelpText.Icon.BACKUP -> R.drawable.ic_ytm_download
        ImportHelpText.Icon.FILE -> R.drawable.ic_ytm_music
        ImportHelpText.Icon.TEXT -> R.drawable.ic_ytm_edit
        ImportHelpText.Icon.HELP -> R.drawable.ic_ytm_help
        ImportHelpText.Icon.QUOTA -> R.drawable.ic_ytm_quota
    }
}
