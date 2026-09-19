package com.saney.ytmimporter.ui

import android.app.Activity
// Native platform alert builder intentionally not used.
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
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

    fun useHorizontalActionRow(
        context: Context,
        actionCount: Int,
        minButtonWidthDp: Int = 150
    ): Boolean {
        if (actionCount <= 1) {
            return false
        }

        val screenWidthDp =
            context.resources
                .configuration
                .screenWidthDp

        val requiredWidthDp =
            48 +
                actionCount * minButtonWidthDp +
                (actionCount - 1) * 8

        return screenWidthDp >= requiredWidthDp
    }

    fun addAdaptiveActionButtons(
        activity: Activity,
        container: LinearLayout,
        buttons: List<Button>,
        buttonHeightDp: Int = 54
    ) {
        if (buttons.isEmpty()) {
            return
        }

        val horizontal =
            useHorizontalActionRow(
                context = activity,
                actionCount = buttons.size
            )

        container.orientation =
            if (horizontal) {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }

        buttons.forEachIndexed { index, button ->
            container.addView(
                button,
                if (horizontal) {
                    LinearLayout.LayoutParams(
                        0,
                        dp(activity, buttonHeightDp),
                        1f
                    ).apply {
                        if (index > 0) {
                            marginStart = dp(activity, 8)
                        }
                    }
                } else {
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(activity, buttonHeightDp)
                    ).apply {
                        if (index > 0) {
                            topMargin = dp(activity, 8)
                        }
                    }
                }
            )
        }
    }

    private data class LegacyDialogAction(
        val label: String,
        val listener: DialogInterface.OnClickListener?
    )

    class StableAlertBuilder internal constructor(
        private val activity: Activity
    ) {
        private var title: CharSequence = ""
        private var message: CharSequence = ""
        private var contentView: View? = null

        private var choiceItems: Array<out CharSequence>? = null
        private var choiceChecked: BooleanArray? = null
        private var choiceListener: DialogInterface.OnMultiChoiceClickListener? = null

        private var positiveAction: LegacyDialogAction? = null
        private var negativeAction: LegacyDialogAction? = null
        private var neutralAction: LegacyDialogAction? = null

        fun setTitle(value: CharSequence): StableAlertBuilder =
            apply { title = value }

        fun setMessage(value: CharSequence): StableAlertBuilder =
            apply { message = value }

        fun setView(view: View): StableAlertBuilder =
            apply { contentView = view }

        fun setMultiChoiceItems(
            items: Array<out CharSequence>,
            checkedItems: BooleanArray?,
            listener: DialogInterface.OnMultiChoiceClickListener?
        ): StableAlertBuilder =
            apply {
                choiceItems = items
                choiceChecked = checkedItems?.copyOf() ?: BooleanArray(items.size)
                choiceListener = listener
            }

        fun setPositiveButton(
            label: CharSequence,
            listener: DialogInterface.OnClickListener?
        ): StableAlertBuilder =
            apply {
                positiveAction = LegacyDialogAction(label.toString(), listener)
            }

        fun setNegativeButton(
            label: CharSequence,
            listener: DialogInterface.OnClickListener?
        ): StableAlertBuilder =
            apply {
                negativeAction = LegacyDialogAction(label.toString(), listener)
            }

        fun setNeutralButton(
            label: CharSequence,
            listener: DialogInterface.OnClickListener?
        ): StableAlertBuilder =
            apply {
                neutralAction = LegacyDialogAction(label.toString(), listener)
            }

        fun show(): Dialog {
            lateinit var shownDialog: Dialog

            fun mappedAction(
                source: LegacyDialogAction?,
                which: Int,
                tone: ActionTone
            ): DialogAction? =
                source?.let { stored ->
                    DialogAction(
                        label = stored.label,
                        tone = tone
                    ) {
                        stored.listener?.onClick(shownDialog, which)
                    }
                }

            val positiveTone =
                if (positiveAction?.label in setOf("Видалити", "Очистити", "Відкотити")) {
                    ActionTone.DANGER
                } else {
                    ActionTone.ACCENT
                }

            val actions =
                listOfNotNull(
                    mappedAction(positiveAction, DialogInterface.BUTTON_POSITIVE, positiveTone),
                    mappedAction(neutralAction, DialogInterface.BUTTON_NEUTRAL, ActionTone.NORMAL),
                    mappedAction(negativeAction, DialogInterface.BUTTON_NEGATIVE, ActionTone.NORMAL)
                )

            val choices = choiceItems

            shownDialog =
                when {
                    choices != null ->
                        UiChrome.showMultiChoiceDialog(
                            activity = activity,
                            title = title.toString(),
                            items = choices.map { it.toString() },
                            checked = choiceChecked?.copyOf() ?: BooleanArray(choices.size),
                            onCheckedChange = { index, isChecked ->
                                choiceListener?.onClick(shownDialog, index, isChecked)
                            },
                            actions = actions
                        )

                    contentView != null ->
                        UiChrome.showContentDialog(
                            activity = activity,
                            title = title.toString(),
                            message = message.toString(),
                            content = requireNotNull(contentView),
                            actions = actions
                        )

                    else ->
                        UiChrome.showMessageDialog(
                            activity = activity,
                            title = title.toString(),
                            message = message.toString(),
                            actions = actions
                        )
                }

            return shownDialog
        }
    }

    fun alertBuilder(activity: Activity): StableAlertBuilder =
        StableAlertBuilder(activity)

    fun backButton(
        activity: Activity,
        onClick: () -> Unit
    ): ImageButton =
        ImageButton(activity).apply {
            contentDescription = "Назад"
            setImageResource(R.drawable.ic_arrow_back_24)
            imageTintList =
                android.content.res.ColorStateList.valueOf(
                    AppThemeManager.palette(activity).text
                )
            scaleType =
                android.widget.ImageView.ScaleType.CENTER
            setPadding(0, 0, 0, 0)
            minimumWidth = 0
            minimumHeight = 0
            background =
                AppThemeManager.surfaceDrawable(
                    context = activity,
                    fill = AppThemeManager.palette(activity).surface,
                    radiusDp = 12,
                    accentStroke = false
                )
            setOnClickListener {
                onClick()
            }
        }

    private fun customDialog(
        activity: Activity
    ): Dialog =
        Dialog(
            activity,
            R.style.YtmAlertDialogTheme
        ).apply {
            setCanceledOnTouchOutside(true)
        }

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
        val dialog = customDialog(activity)
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
        val dialog = customDialog(activity)
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
    ): Dialog {
        val dialog = customDialog(activity)
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

        return showCustomDialog(
            activity = activity,
            dialog = dialog,
            card = card
        )
    }

    fun showDangerConfirmDialog(
        activity: Activity,
        title: String,
        message: String,
        confirmLabel: String,
        onConfirm: () -> Unit
    ): Dialog =
        showMessageDialog(
            activity = activity,
            title = title,
            message = message,
            actions =
                listOf(
                    DialogAction(
                        label = confirmLabel,
                        tone = ActionTone.DANGER,
                        onClick = onConfirm
                    ),
                    DialogAction(
                        label = "Скасувати",
                        tone = ActionTone.NORMAL,
                        onClick = {}
                    )
                )
        )

    fun showContentDialog(
        activity: Activity,
        title: String,
        message: String,
        content: View,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.AUTO
    ): Dialog {
        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        if (message.isNotBlank()) {
            card.addView(
                TextView(activity).apply {
                    text = message
                    textSize = 15f
                    setTextColor(Color.rgb(230, 231, 234))
                    setTextIsSelectable(true)
                    setLineSpacing(0f, 1.08f)
                    setPadding(0, dp(context, 6), 0, dp(context, 12))
                }
            )
        }

        card.addView(
            content,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(activity, 16)
            }
        )

        addDialogActions(
            activity = activity,
            dialog = dialog,
            card = card,
            actions = actions,
            actionLayout = actionLayout
        )

        return showCustomDialog(
            activity = activity,
            dialog = dialog,
            card = card
        )
    }

    fun showMultiChoiceDialog(
        activity: Activity,
        title: String,
        items: List<String>,
        checked: BooleanArray,
        onCheckedChange: (index: Int, isChecked: Boolean) -> Unit,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.AUTO
    ): Dialog {
        require(items.size == checked.size)

        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        val choices = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        items.forEachIndexed { index, label ->
            choices.addView(
                CheckBox(activity).apply {
                    text = label
                    textSize = 14f
                    setTextColor(Color.WHITE)
                    setPadding(
                        dp(context, 12),
                        dp(context, 9),
                        dp(context, 12),
                        dp(context, 9)
                    )
                    background = roundedBackground(
                        context = context,
                        color = ROW_SURFACE,
                        radiusDp = 12,
                        strokeColor = BORDER
                    )
                    isChecked = checked[index]
                    setOnCheckedChangeListener { _, value ->
                        checked[index] = value
                        onCheckedChange(index, value)
                    }
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

        card.addView(
            choices,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(activity, 16)
            }
        )

        addDialogActions(
            activity = activity,
            dialog = dialog,
            card = card,
            actions = actions,
            actionLayout = actionLayout
        )

        return showCustomDialog(
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

    /*
     * Horizontal modal action contract:
     * - primary/confirm action is on the left;
     * - dismissive action (Cancel/Close/Not now) is on the right;
     * - a third secondary action sits between them when applicable.
     *
     * Callers must pass horizontal actions in that semantic order.
     * Vertical action sheets keep their explicit top-to-bottom order.
     */
    private fun addDialogActions(
        activity: Activity,
        dialog: Dialog,
        card: LinearLayout,
        actions: List<DialogAction>,
        actionLayout: DialogActionLayout
    ) {
        if (actions.isEmpty()) return

        if (
            useHorizontalActionRow(
                context = activity,
                actionCount = actions.size
            )
        ) {
            val row =
                LinearLayout(activity).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                }

            orderHorizontalActions(actions)
                .forEachIndexed { index, action ->
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
                                marginStart =
                                    dp(activity, 8)
                            }
                        }
                    )
                }

            card.addView(row)
            return
        }

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

            orderHorizontalActions(actions.drop(1)).forEachIndexed { index, action ->
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

            orderHorizontalActions(actions).forEachIndexed { index, action ->
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

    private fun orderHorizontalActions(
        actions: List<DialogAction>
    ): List<DialogAction> {
        if (actions.size <= 1) return actions

        val active =
            actions.filterNot {
                isDismissiveAction(it)
            }

        val dismissive =
            actions.filter {
                isDismissiveAction(it)
            }

        return active + dismissive
    }

    private fun isDismissiveAction(
        action: DialogAction
    ): Boolean =
        action.label.trim() in
            setOf(
                "Скасувати",
                "Закрити",
                "Не зараз",
                "Назад"
            )

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
        dialog: Dialog,
        card: LinearLayout
    ): Dialog {
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

            /*
             * BUG-002 first-frame rule:
             * configure the dedicated Dialog Window before it is attached to
             * WindowManager. There must be no post-show geometry correction.
             *
             * The custom Dialog is full-screen and transparent; UiChrome
             * positions the card inside the safe viewport itself.
             */
            window.setWindowAnimations(0)
            window.attributes =
                window.attributes.apply {
                    windowAnimations = 0
                }

            window.setGravity(
                Gravity.TOP or
                    Gravity.CENTER_HORIZONTAL
            )

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

        var insetsApplied = false
        var stablePreDraws = 0
        var lastGeometry: String? = null

        dialog.setContentView(outer)

        fun hideDecor() {
            dialog.window?.decorView?.alpha = 0f
        }

        fun revealAfterStableGeometry() {
            val decor = dialog.window?.decorView ?: return

            decor.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        if (!insetsApplied) {
                            return true
                        }

                        val geometry = listOf(
                            outer.measuredWidth,
                            outer.measuredHeight,
                            outer.paddingLeft,
                            outer.paddingTop,
                            outer.paddingRight,
                            outer.paddingBottom,
                            card.measuredWidth,
                            card.measuredHeight
                        ).joinToString(":")

                        if (
                            outer.measuredWidth <= 0 ||
                            outer.measuredHeight <= 0 ||
                            card.measuredWidth <= 0 ||
                            card.measuredHeight <= 0
                        ) {
                            stablePreDraws = 0
                            return true
                        }

                        if (geometry == lastGeometry) {
                            stablePreDraws += 1
                        } else {
                            lastGeometry = geometry
                            stablePreDraws = 0
                        }

                        if (stablePreDraws >= 1) {
                            if (decor.viewTreeObserver.isAlive) {
                                decor.viewTreeObserver.removeOnPreDrawListener(this)
                            }
                            outer.alpha = 1f
                            decor.alpha = 1f
                        }

                        return true
                    }
                }
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(outer) { view, insets ->
            val safeInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )

            view.setPadding(
                horizontalInset,
                maxOf(minimumVerticalInset, safeInsets.top + dp(activity, 8)),
                horizontalInset,
                maxOf(minimumVerticalInset, safeInsets.bottom + dp(activity, 8))
            )

            insetsApplied = true
            stablePreDraws = 0
            lastGeometry = null
            view.requestLayout()
            insets
        }

        /*
         * Some Android builds normalize Dialog Window attributes at attach.
         * Configure both before and immediately after show(), but keep the
         * entire decor invisible through that attach-time normalization.
         */
        hideDecor()
        configureWindow()

        dialog.show()

        hideDecor()
        configureWindow()
        revealAfterStableGeometry()

        ViewCompat.requestApplyInsets(outer)

        return dialog
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
