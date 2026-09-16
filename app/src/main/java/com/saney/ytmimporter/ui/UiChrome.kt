package com.saney.ytmimporter.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
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
import com.saney.ytmimporter.R

object UiChrome {
    data class MenuAction(
        val label: String,
        val onClick: () -> Unit
    )

    enum class ActionTone {
        NORMAL,
        ACCENT,
        DANGER
    }

    enum class DialogActionLayout {
        AUTO,
        PRIMARY_TOP
    }

    data class DialogAction(
        val label: String,
        val tone: ActionTone = ActionTone.NORMAL,
        val onClick: () -> Unit
    )

    fun alertBuilder(
        context: Context
    ): AlertDialog.Builder =
        AlertDialog.Builder(
            context,
            R.style.YtmAlertDialogTheme
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
        subtitle: String? = null,
        onNegative: (() -> Unit)? = null
    ) {
        val dialog = alertBuilder(activity).create()
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        actions.forEachIndexed { index, action ->
            card.addView(
                menuButton(activity, action.label) {
                    dialog.dismiss()
                    action.onClick()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
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
                onNegative?.invoke()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(activity, 14)
            }
        )

        showCustomDialog(
            activity = activity,
            dialog = dialog,
            card = card
        )
    }

    fun showMessageDialog(
        activity: Activity,
        title: String,
        message: String,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.AUTO
    ) {
        val dialog = alertBuilder(activity).create()
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        card.addView(
            TextView(activity).apply {
                text = message
                textSize = 15f
                setTextColor(Color.rgb(230, 231, 234))
                setTextIsSelectable(true)
                setLineSpacing(0f, 1.08f)
                setPadding(
                    0,
                    dp(context, 6),
                    0,
                    dp(context, 16)
                )
            }
        )

        addDialogActions(
            activity = activity,
            dialog = dialog,
            card = card,
            actions = actions,
            actionLayout = actionLayout
        )

        showCustomDialog(
            activity = activity,
            dialog = dialog,
            card = card
        )
    }

    fun autoSizeButton(
        button: Button,
        minSp: Int = 11,
        maxSp: Int = 15
    ) {
        button.setAutoSizeTextTypeUniformWithConfiguration(
            minSp,
            maxSp,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )
    }

    private fun addDialogActions(
        activity: Activity,
        dialog: AlertDialog,
        card: LinearLayout,
        actions: List<DialogAction>,
        actionLayout: DialogActionLayout
    ) {
        if (actions.isEmpty()) return

        if (
            actionLayout == DialogActionLayout.PRIMARY_TOP &&
            actions.size == 3
        ) {
            val primary = actions.first()

            card.addView(
                dialogActionButton(
                    activity = activity,
                    action = primary
                ) {
                    dialog.dismiss()
                    primary.onClick()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            actions.drop(1).forEachIndexed { index, action ->
                row.addView(
                    dialogActionButton(
                        activity = activity,
                        action = action
                    ) {
                        dialog.dismiss()
                        action.onClick()
                    },
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        if (index > 0) marginStart = dp(activity, 8)
                    }
                )
            }

            card.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(activity, 9) }
            )
            return
        }

        val trailingTextAction =
            actions.size == 3 &&
                actionLayout == DialogActionLayout.AUTO &&
                actions.last().tone == ActionTone.ACCENT &&
                actions.last().label in setOf(
                    "Закрити",
                    "Назад",
                    "Не зараз"
                )

        if (trailingTextAction) {
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            actions.take(2).forEachIndexed { index, action ->
                row.addView(
                    dialogActionButton(
                        activity = activity,
                        action = action
                    ) {
                        dialog.dismiss()
                        action.onClick()
                    },
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        if (index > 0) {
                            marginStart = dp(activity, 8)
                        }
                    }
                )
            }

            card.addView(row)

            val closeAction = actions.last()
            card.addView(
                flatDialogActionButton(
                    activity = activity,
                    action = closeAction
                ) {
                    dialog.dismiss()
                    closeAction.onClick()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(activity, 6)
                }
            )
            return
        }

        val compactRow = actions.size <= 3

        if (compactRow) {
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            actions.forEachIndexed { index, action ->
                row.addView(
                    dialogActionButton(
                        activity = activity,
                        action = action
                    ) {
                        dialog.dismiss()
                        action.onClick()
                    },
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        if (index > 0) {
                            marginStart = dp(activity, 8)
                        }
                    }
                )
            }

            card.addView(row)
            return
        }

        actions.forEachIndexed { index, action ->
            card.addView(
                dialogActionButton(
                    activity = activity,
                    action = action
                ) {
                    dialog.dismiss()
                    action.onClick()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin = dp(activity, 9)
                    }
                }
            )
        }
    }

    private fun dialogCard(
        activity: Activity
    ): LinearLayout =
        LinearLayout(activity).apply {
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

    private fun addDialogHeader(
        activity: Activity,
        card: LinearLayout,
        title: String,
        subtitle: String?
    ) {
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
                    setPadding(
                        0,
                        dp(context, 6),
                        0,
                        dp(context, 12)
                    )
                }
            )
        } else {
            card.addView(
                SpaceView(activity, dp(activity, 8))
            )
        }
    }

    private fun showCustomDialog(
        activity: Activity,
        dialog: AlertDialog,
        card: LinearLayout
    ) {
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
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            minHeight = dp(context, 60)
            minimumHeight = dp(context, 60)
            minWidth = 0
            minimumWidth = 0
            maxLines = 3
            setPadding(
                dp(context, 18),
                dp(context, 12),
                dp(context, 18),
                dp(context, 12)
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
            autoSizeButton(this, minSp = 12, maxSp = 15)
            setOnClickListener { onClick() }
        }

    private fun dialogActionButton(
        activity: Activity,
        action: DialogAction,
        onClick: () -> Unit
    ): Button =
        Button(activity).apply {
            text = action.label
            isAllCaps = false
            gravity = Gravity.CENTER
            minHeight = dp(context, 54)
            minimumHeight = dp(context, 54)
            minWidth = 0
            minimumWidth = 0
            maxLines = 2
            setPadding(
                dp(context, 10),
                dp(context, 9),
                dp(context, 10),
                dp(context, 9)
            )
            setTextColor(
                when (action.tone) {
                    ActionTone.NORMAL -> Color.WHITE
                    ActionTone.ACCENT -> ACCENT
                    ActionTone.DANGER -> Color.rgb(255, 100, 115)
                }
            )
            background = roundedBackground(
                context = context,
                color = ROW_SURFACE,
                radiusDp = 12,
                strokeColor = BORDER
            )
            autoSizeButton(this, minSp = 10, maxSp = 14)
            setOnClickListener { onClick() }
        }

    private fun flatDialogActionButton(
        activity: Activity,
        action: DialogAction,
        onClick: () -> Unit
    ): Button =
        Button(activity).apply {
            text = action.label
            isAllCaps = false
            gravity = Gravity.CENTER
            minHeight = dp(context, 44)
            minimumHeight = dp(context, 44)
            minWidth = 0
            minimumWidth = 0
            maxLines = 1
            setPadding(
                dp(context, 14),
                dp(context, 6),
                dp(context, 14),
                dp(context, 6)
            )
            setTextColor(
                when (action.tone) {
                    ActionTone.NORMAL -> Color.WHITE
                    ActionTone.ACCENT -> ACCENT
                    ActionTone.DANGER -> Color.rgb(255, 100, 115)
                }
            )
            background = ColorDrawable(Color.TRANSPARENT)
            autoSizeButton(this, minSp = 11, maxSp = 14)
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
