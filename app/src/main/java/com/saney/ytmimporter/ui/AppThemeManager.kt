package com.saney.ytmimporter.ui

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.view.WindowCompat
import kotlin.math.max

object AppThemeManager {
    enum class ThemeStyle(
        val storageKey: String,
        val label: String,
        val marker: String
    ) {
        NEON_DARK("neon_dark", "Neon Dark", "◆"),
        BLUE_DARK("blue_dark", "Blue Dark", "●"),
        GREEN_DARK("green_dark", "Green Dark", "▲")
    }

    data class SemanticPalette(
        val success: Int,
        val successFill: Int,
        val warning: Int,
        val warningFill: Int,
        val danger: Int,
        val dangerFill: Int,
        val duplicate: Int
    )

    data class SkinPalette(
        val background: Int,
        val surface: Int,
        val surfaceAlt: Int,
        val border: Int,
        val text: Int,
        val muted: Int,
        val mutedDim: Int,
        val accent: Int,
        val accentFill: Int,
        val semantic: SemanticPalette
    )

    data class Skin(
        val style: ThemeStyle,
        val palette: SkinPalette
    )

    private const val PREFS =
        "ytm_theme_prefs_v1"

    private const val KEY_STYLE =
        "theme_style"

    private val BUILT_IN_SKINS: Map<ThemeStyle, Skin> =
        listOf(
            Skin(
                style = ThemeStyle.NEON_DARK,
                palette =
                    SkinPalette(
                        background = Color.rgb(12, 14, 20),
                        surface = Color.rgb(22, 25, 34),
                        surfaceAlt = Color.rgb(31, 35, 47),
                        border = Color.rgb(67, 72, 91),
                        text = Color.rgb(245, 247, 250),
                        muted = Color.rgb(169, 175, 190),
                        mutedDim = Color.rgb(118, 124, 139),
                        accent = Color.rgb(255, 45, 104),
                        accentFill = Color.rgb(171, 17, 64),
                        semantic =
                            SemanticPalette(
                                success = Color.rgb(70, 220, 130),
                                successFill = Color.rgb(28, 118, 74),
                                warning = Color.rgb(255, 197, 77),
                                warningFill = Color.rgb(142, 97, 20),
                                danger = Color.rgb(255, 100, 120),
                                dangerFill = Color.rgb(154, 35, 52),
                                duplicate = Color.rgb(124, 176, 255)
                            )
                    )
            ),
            Skin(
                style = ThemeStyle.BLUE_DARK,
                palette =
                    SkinPalette(
                        background = Color.rgb(7, 17, 30),
                        surface = Color.rgb(14, 31, 50),
                        surfaceAlt = Color.rgb(19, 43, 69),
                        border = Color.rgb(39, 91, 139),
                        text = Color.rgb(243, 248, 255),
                        muted = Color.rgb(158, 180, 205),
                        mutedDim = Color.rgb(100, 126, 154),
                        accent = Color.rgb(52, 164, 255),
                        accentFill = Color.rgb(22, 102, 188),
                        semantic =
                            SemanticPalette(
                                success = Color.rgb(70, 220, 130),
                                successFill = Color.rgb(27, 112, 70),
                                warning = Color.rgb(255, 199, 83),
                                warningFill = Color.rgb(137, 96, 24),
                                danger = Color.rgb(255, 103, 125),
                                dangerFill = Color.rgb(149, 37, 56),
                                duplicate = Color.rgb(111, 181, 255)
                            )
                    )
            ),
            Skin(
                style = ThemeStyle.GREEN_DARK,
                palette =
                    SkinPalette(
                        background = Color.rgb(5, 22, 16),
                        surface = Color.rgb(12, 40, 29),
                        surfaceAlt = Color.rgb(17, 53, 38),
                        border = Color.rgb(34, 105, 75),
                        text = Color.rgb(242, 252, 247),
                        muted = Color.rgb(157, 194, 174),
                        mutedDim = Color.rgb(99, 143, 119),
                        accent = Color.rgb(34, 221, 126),
                        accentFill = Color.rgb(16, 132, 75),
                        semantic =
                            SemanticPalette(
                                success = Color.rgb(74, 225, 137),
                                successFill = Color.rgb(25, 124, 72),
                                warning = Color.rgb(255, 201, 87),
                                warningFill = Color.rgb(136, 98, 28),
                                danger = Color.rgb(255, 105, 126),
                                dangerFill = Color.rgb(148, 40, 58),
                                duplicate = Color.rgb(114, 181, 255)
                            )
                    )
            )
        ).associateBy {
            it.style
        }

    fun builtInSkins(): List<Skin> =
        ThemeStyle
            .values()
            .map { style ->
                requireNotNull(
                    BUILT_IN_SKINS[style]
                )
            }

    fun currentStyle(
        context: Context
    ): ThemeStyle {
        val stored =
            context
                .getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                )
                .getString(
                    KEY_STYLE,
                    ThemeStyle.NEON_DARK.storageKey
                )

        return ThemeStyle
            .values()
            .firstOrNull {
                it.storageKey == stored
            }
            ?: ThemeStyle.NEON_DARK
    }

    fun setStyle(
        context: Context,
        style: ThemeStyle
    ) {
        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_STYLE,
                style.storageKey
            )
            .apply()
    }

    fun skin(
        context: Context
    ): Skin =
        requireNotNull(
            BUILT_IN_SKINS[
                currentStyle(context)
            ]
        )

    fun palette(
        context: Context
    ): SkinPalette =
        skin(context).palette

    fun applyWindow(
        activity: Activity
    ) {
        val palette =
            palette(activity)

        activity.window.statusBarColor =
            palette.background

        activity.window.navigationBarColor =
            palette.background

        WindowCompat
            .getInsetsController(
                activity.window,
                activity.window.decorView
            )
            .apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
    }

    fun surfaceDrawable(
        context: Context,
        fill: Int = palette(context).surface,
        radiusDp: Int = 14,
        accentStroke: Boolean = false,
        accentOverride: Int? = null
    ): Drawable {
        val palette =
            palette(context)

        return SketchRoundedDrawable(
            density =
                context.resources
                    .displayMetrics
                    .density,
            fillColor = fill,
            borderColor = palette.border,
            accentColor =
                accentOverride
                    ?: palette.accent,
            radiusDp = radiusDp,
            accentStroke = accentStroke
        )
    }

    fun largeCardDrawable(
        context: Context,
        fill: Int = palette(context).surface,
        radiusDp: Int = 14,
        accentOverride: Int? = null
    ): Drawable =
        surfaceDrawable(
            context = context,
            fill = fill,
            radiusDp = radiusDp,
            accentStroke = true,
            accentOverride = accentOverride
        )

    fun neutralButtonDrawable(
        context: Context,
        radiusDp: Int = 12
    ): Drawable =
        surfaceDrawable(
            context = context,
            fill = palette(context).surfaceAlt,
            radiusDp = radiusDp,
            accentStroke = false
        )

    fun accentButtonDrawable(
        context: Context,
        radiusDp: Int = 12
    ): Drawable =
        surfaceDrawable(
            context = context,
            fill = palette(context).accentFill,
            radiusDp = radiusDp,
            accentStroke = true
        )

    fun accentCircleDrawable(
        context: Context
    ): Drawable =
        surfaceDrawable(
            context = context,
            fill = palette(context).accentFill,
            radiusDp = 999,
            accentStroke = false
        )

    fun stateButtonDrawable(
        context: Context,
        fillColor: Int,
        radiusDp: Int = 12,
        accentOverride: Int? = null
    ): Drawable =
        surfaceDrawable(
            context = context,
            fill = fillColor,
            radiusDp = radiusDp,
            accentStroke = true,
            accentOverride = accentOverride
        )

    private class SketchRoundedDrawable(
        density: Float,
        private val fillColor: Int,
        private val borderColor: Int,
        private val accentColor: Int,
        radiusDp: Int,
        private val accentStroke: Boolean
    ) : Drawable() {
        private val radius =
            radiusDp * density

        private val borderWidth =
            max(1f, density)

        private val accentWidth =
            max(1.2f, 1.35f * density)

        private val fillPaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                style =
                    Paint.Style.FILL
                color =
                    fillColor
            }

        private val borderPaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                style =
                    Paint.Style.STROKE
                strokeWidth =
                    borderWidth
                color =
                    borderColor
            }

        private val accentPaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                style =
                    Paint.Style.STROKE
                strokeCap =
                    Paint.Cap.ROUND
                strokeWidth =
                    accentWidth
                color =
                    accentColor
                alpha =
                    218
            }

        override fun draw(
            canvas: Canvas
        ) {
            val inset =
                borderWidth / 2f

            val rect =
                RectF(
                    bounds.left + inset,
                    bounds.top + inset,
                    bounds.right - inset,
                    bounds.bottom - inset
                )

            canvas.drawRoundRect(
                rect,
                radius,
                radius,
                fillPaint
            )

            canvas.drawRoundRect(
                rect,
                radius,
                radius,
                borderPaint
            )

            if (!accentStroke) {
                return
            }

            val width =
                rect.width()

            val horizontal =
                max(
                    radius * 1.35f,
                    width * 0.23f
                )

            // Two quiet contour strokes only:
            // one near the top-left edge and one near the bottom-right edge.
            canvas.drawLine(
                rect.left + radius * 0.75f,
                rect.top,
                (rect.left + horizontal)
                    .coerceAtMost(
                        rect.right - radius
                    ),
                rect.top,
                accentPaint
            )

            canvas.drawLine(
                (rect.right - horizontal)
                    .coerceAtLeast(
                        rect.left + radius
                    ),
                rect.bottom,
                rect.right - radius * 0.75f,
                rect.bottom,
                accentPaint
            )
        }

        override fun setAlpha(
            alpha: Int
        ) {
            fillPaint.alpha = alpha
            borderPaint.alpha = alpha
            accentPaint.alpha = alpha
            invalidateSelf()
        }

        override fun setColorFilter(
            colorFilter: ColorFilter?
        ) {
            fillPaint.colorFilter =
                colorFilter
            borderPaint.colorFilter =
                colorFilter
            accentPaint.colorFilter =
                colorFilter
            invalidateSelf()
        }

        @Deprecated(
            "Deprecated in Java"
        )
        override fun getOpacity():
            Int =
            PixelFormat.TRANSLUCENT
    }
}
