package com.saney.ytmimporter.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

object UiChrome {
    data class MenuAction(
        val label: String,
        val onClick: () -> Unit
    )

    fun applyScreenInsets(
        activity: Activity,
        root: View,
        extraTopDp: Int = 8,
        extraBottomDp: Int = 10
    ) {
        WindowCompat.setDecorFitsSystemWindows(
            activity.window,
            false
        )

        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                initialLeft,
                initialTop + bars.top + dp(view.context, extraTopDp),
                initialRight,
                initialBottom + bars.bottom + dp(view.context, extraBottomDp)
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }

    fun showMenuDialog(
        activity: Activity,
        title: String,
        actions: List<MenuAction>,
        negativeLabel: String = "Закрити",
        subtitle: String? = null
    ) {
        val dialog = AlertDialog.Builder(activity).create()

        val outer = FrameLayout(activity).apply {
            setPadding(
                dp(context, 18),
                dp(context, 18),
                dp(context, 18),
                dp(context, 18)
            )
        }

        val scroll = ScrollView(activity).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(context, 18),
                dp(context, 18),
                dp(context, 18),
                dp(context, 18)
            )
            background = roundedBackground(
                context = context,
                color = SURFACE,
                radiusDp = 18,
                strokeColor = BORDER
            )
        }

        card.addView(
            TextView(activity).apply {
                text = title
                textSize = 22f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }
        )

        if (!subtitle.isNullOrBlank()) {
            card.addView(
                TextView(activity).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(MUTED)
                    setPadding(0, dp(context, 6), 0, dp(context, 12))
                }
            )
        } else {
            card.addView(
                SpaceView(activity, dp(activity, 8))
            )
        }

        actions.forEachIndexed { index, action ->
            card.addView(
                menuButton(activity, action.label) {
                    dialog.dismiss()
                    action.onClick()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(activity, 52)
                ).apply {
                    if (index > 0) {
                        topMargin = dp(activity, 10)
                    }
                }
            )
        }

        card.addView(
            menuButton(
                activity = activity,
                label = negativeLabel,
                accent = true
            ) {
                dialog.dismiss()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 52)
            ).apply {
                topMargin = dp(activity, 14)
            }
        )

        scroll.addView(
            card,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        outer.addView(scroll)

        dialog.setView(outer)
        dialog.show()
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
    }

    private fun menuButton(
        activity: Activity,
        label: String,
        accent: Boolean = false,
        onClick: () -> Unit
    ): Button =
        Button(activity).apply {
            text = label
            isAllCaps = false
            textSize = 14.5f
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            minHeight = 0
            minimumHeight = 0
            setPadding(
                dp(context, 16),
                0,
                dp(context, 16),
                0
            )
            setTextColor(
                if (accent) ACCENT else Color.WHITE
            )
            background = roundedBackground(
                context = context,
                color = ROW_SURFACE,
                radiusDp = 14,
                strokeColor = BORDER
            )
            setOnClickListener { onClick() }
        }

    private fun roundedBackground(
        context: Context,
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, radiusDp).toFloat()
            setColor(color)
            if (strokeColor != null) {
                setStroke(dp(context, 1), strokeColor)
            }
        }

    private fun dp(
        context: Context,
        value: Int
    ): Int =
        (value * context.resources.displayMetrics.density).toInt()

    private class SpaceView(
        context: Context,
        heightPx: Int
    ) : View(context) {
        init {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightPx
            )
        }
    }

    private val SURFACE = Color.rgb(25, 27, 32)
    private val ROW_SURFACE = Color.rgb(31, 33, 39)
    private val BORDER = Color.rgb(48, 51, 59)
    private val MUTED = Color.rgb(165, 167, 173)
    private val ACCENT = Color.rgb(255, 70, 95)
}
