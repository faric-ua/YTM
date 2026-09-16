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
        PRIMARY_TOP,
        VERTICAL_WITH_TEXT_CLOSE
    }

    data class DialogRecord(
        val title: String,
        val detail: String,
        val tone: ActionTone = ActionTone.NORMAL
    )

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

    fun showRecordDialog(
        activity: Activity,
        title: String,
        records: List<DialogRecord>,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE
    ) {
        val dialog = alertBuilder(activity).create()
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        records.forEachIndexed { index, record ->
            card.addView(
                recordCard(
                    activity = activity,
                    record = record
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin = dp(activity, 8)
                    }
                }
            )
        }

        if (records.isNotEmpty()) {
            card.addView(
                SpaceView(
                    activity,
                    dp(activity, 14)
                )
            )
        }

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
            actionLayout == DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE &&
            actions.isNotEmpty()
        ) {
            val boxed =
                if (
                    actions.last().tone == ActionTone.ACCENT &&
                    actions.last().label in setOf(
                        "Закрити",
                        "Назад",
                        "Не зараз"
                    )
                ) {
                    actions.dropLast(1)
                } else {
                    actions
                }

            boxed.forEachIndexed { index, action ->
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
                            topMargin = dp(activity, 8)
                        }
                    }
                )
            }

            if (boxed.size != actions.size) {
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
            }

            return
        }

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

    private fun recordCard(
        activity: Activity,
        record: DialogRecord
    ): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(context, 14),
                dp(context, 12),
                dp(context, 14),
                dp(context, 12)
            )
            background = roundedBackground(
                context = context,
                color = ROW_SURFACE,
                radiusDp = 13,
                strokeColor = BORDER
            )

            addView(
                TextView(activity).apply {
                    text = record.title
                    textSize = 14.5f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(
                        when (record.tone) {
                            ActionTone.NORMAL -> Color.WHITE
                            ActionTone.ACCENT -> Color.rgb(110, 215, 145)
                            ActionTone.DANGER -> Color.rgb(255, 120, 130)
                        }
                    )
                    setLineSpacing(0f, 1.05f)
                }
            )

            addView(
                TextView(activity).apply {
                    text = record.detail
                    textSize = 13f
                    setTextColor(MUTED)
                    setPadding(0, dp(context, 5), 0, 0)
                    setLineSpacing(0f, 1.08f)
                }
            )
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
        val horizontalInset =
            dp(activity, 18)

        val minimumVerticalInset =
            dp(activity, 12)

        val outer =
            FrameLayout(activity).apply {
                // Do not show provisional geometry before final insets arrive.
                alpha = 0f
            }

        val scroll =
            ScrollView(activity).apply {
                isFillViewport = true
                overScrollMode =
                    View.OVER_SCROLL_IF_CONTENT_SCROLLS
                isVerticalScrollBarEnabled = true
            }

        /*
         * Important:
         * custom dialogs are intentionally TOP anchored.
         *
         * v1.4.9 hid the provisional frame until insets arrived, but a tall
         * card could still visibly move after becoming visible because
         * CENTER_VERTICAL depends on the final measured content height.
         *
         * TOP anchoring makes the card position independent of its measured
         * height, so More / Quota / Problem Tracks and other custom dialogs
         * do not appear centered first and then jump upward.
         */
        val holder =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
                gravity =
                    Gravity.TOP
            }

        holder.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        scroll.addView(
            holder,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        outer.addView(
            scroll,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        fun configureWindow() {
            val window = dialog.window ?: return

            WindowCompat.setDecorFitsSystemWindows(
                window,
                false
            )

            window.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        dialog.setView(outer)

        // Best effort before the first rendered frame.
        configureWindow()

        dialog.setOnShowListener {
            configureWindow()

            ViewCompat.setOnApplyWindowInsetsListener(
                outer
            ) { view, insets ->
                val safeInsets =
                    insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or
                            WindowInsetsCompat.Type.displayCutout()
                    )

                view.setPadding(
                    horizontalInset,
                    maxOf(
                        minimumVerticalInset,
                        safeInsets.top +
                            dp(activity, 8)
                    ),
                    horizontalInset,
                    maxOf(
                        minimumVerticalInset,
                        safeInsets.bottom +
                            dp(activity, 8)
                    )
                )

                // First visible frame is already in its final safe position.
                if (view.alpha == 0f) {
                    view.alpha = 1f
                }

                insets
            }

            ViewCompat.requestApplyInsets(
                outer
            )
        }

        dialog.show()
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
