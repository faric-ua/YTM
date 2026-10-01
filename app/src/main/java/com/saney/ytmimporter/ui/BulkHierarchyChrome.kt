package com.saney.ytmimporter.ui

import android.app.Activity
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Presentation-only primitives for the Bulk Preview / Session hierarchy.
 *
 * Domain state, selection, execution, rollback, quota and persistence stay owned
 * by the existing Bulk policies. This object only turns already-computed values
 * into a consistent visual hierarchy.
 */
object BulkHierarchyChrome {
    enum class Tone {
        NORMAL,
        ACCENT,
        SUCCESS,
        WARNING,
        DANGER
    }

    fun card(
        activity: Activity,
        useAltSurface: Boolean = false,
        accentTone: Tone? = null,
        radiusDp: Int = 14
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(activity)

        return LinearLayout(activity).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(activity, 14),
                dp(activity, 12),
                dp(activity, 14),
                dp(activity, 12)
            )
            background =
                AppThemeManager.surfaceDrawable(
                    context = activity,
                    fill =
                        if (useAltSurface) {
                            palette.surfaceAlt
                        } else {
                            palette.surface
                        },
                    radiusDp = radiusDp,
                    accentStroke =
                        accentTone != null,
                    accentOverride =
                        accentTone?.let {
                            toneColor(
                                palette,
                                it
                            )
                        }
                )
        }
    }

    fun title(
        activity: Activity,
        text: CharSequence
    ): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = 15f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                AppThemeManager
                    .palette(activity)
                    .text
            )
        }

    fun badge(
        activity: Activity,
        text: CharSequence,
        tone: Tone
    ): TextView {
        val palette =
            AppThemeManager.palette(activity)

        return TextView(activity).apply {
            this.text = text
            textSize = 11.5f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                toneColor(
                    palette,
                    tone
                )
            )
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                dp(activity, 9),
                dp(activity, 4),
                dp(activity, 9),
                dp(activity, 4)
            )
            background =
                AppThemeManager.surfaceDrawable(
                    context = activity,
                    fill =
                        toneFill(
                            palette,
                            tone
                        ),
                    radiusDp = 999,
                    accentStroke = false
                )
        }
    }

    fun metrics(
        activity: Activity,
        text: CharSequence
    ): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = 13f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                AppThemeManager
                    .palette(activity)
                    .text
            )
            setPadding(
                0,
                dp(activity, 7),
                0,
                0
            )
        }

    fun primary(
        activity: Activity,
        text: CharSequence,
        tone: Tone = Tone.ACCENT
    ): TextView {
        val palette =
            AppThemeManager.palette(activity)

        return TextView(activity).apply {
            this.text = text
            textSize = 13.5f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                toneColor(
                    palette,
                    tone
                )
            )
            setPadding(
                0,
                dp(activity, 7),
                0,
                0
            )
        }
    }

    fun body(
        activity: Activity,
        text: CharSequence
    ): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = 12.5f
            setTextColor(
                AppThemeManager
                    .palette(activity)
                    .text
            )
            setPadding(
                0,
                dp(activity, 6),
                0,
                0
            )
        }

    fun secondary(
        activity: Activity,
        text: CharSequence
    ): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = 12f
            setTextColor(
                AppThemeManager
                    .palette(activity)
                    .muted
            )
            setPadding(
                0,
                dp(activity, 5),
                0,
                0
            )
        }

    private fun toneColor(
        palette: AppThemeManager.SkinPalette,
        tone: Tone
    ): Int =
        when (tone) {
            Tone.NORMAL ->
                palette.text

            Tone.ACCENT ->
                palette.accent

            Tone.SUCCESS ->
                palette.semantic.success

            Tone.WARNING ->
                palette.semantic.warning

            Tone.DANGER ->
                palette.semantic.danger
        }

    private fun toneFill(
        palette: AppThemeManager.SkinPalette,
        tone: Tone
    ): Int =
        when (tone) {
            Tone.NORMAL ->
                palette.surface

            Tone.ACCENT ->
                palette.accentFill

            Tone.SUCCESS ->
                palette.semantic.successFill

            Tone.WARNING ->
                palette.semantic.warningFill

            Tone.DANGER ->
                palette.semantic.dangerFill
        }

    private fun dp(
        activity: Activity,
        value: Int
    ): Int =
        (
            value *
                activity.resources
                    .displayMetrics
                    .density
        ).toInt()
}
