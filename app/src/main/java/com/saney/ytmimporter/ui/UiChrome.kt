package com.saney.ytmimporter.ui

import android.app.Activity
// Native platform alert builder intentionally not used.
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
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

    enum class NoticeTone {
        INFO,
        SUCCESS,
        WARNING,
        DANGER
    }

    fun inlineNotice(
        activity: Activity,
        message: CharSequence,
        tone: NoticeTone
    ): TextView {
        val palette =
            AppThemeManager.palette(activity)

        val textColor =
            when (tone) {
                NoticeTone.INFO ->
                    palette.text
                NoticeTone.SUCCESS ->
                    palette.semantic.success
                NoticeTone.WARNING ->
                    palette.semantic.warning
                NoticeTone.DANGER ->
                    palette.semantic.danger
            }

        val fill =
            when (tone) {
                NoticeTone.INFO ->
                    palette.surfaceAlt
                NoticeTone.SUCCESS ->
                    palette.semantic.successFill
                NoticeTone.WARNING ->
                    palette.semantic.warningFill
                NoticeTone.DANGER ->
                    palette.semantic.dangerFill
            }

        val accent =
            when (tone) {
                NoticeTone.INFO ->
                    palette.accent
                NoticeTone.SUCCESS ->
                    palette.semantic.success
                NoticeTone.WARNING ->
                    palette.semantic.warning
                NoticeTone.DANGER ->
                    palette.semantic.danger
            }

        return TextView(activity).apply {
            text = message
            textSize = 13f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                textColor
            )
            setTextIsSelectable(true)
            setPadding(
                dp(activity, 14),
                dp(activity, 10),
                dp(activity, 14),
                dp(activity, 10)
            )
            background =
                AppThemeManager.surfaceDrawable(
                    context = activity,
                    fill = fill,
                    radiusDp = 12,
                    accentStroke = true,
                    accentOverride = accent
                )
        }
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
        val dismissOnClick: Boolean = true,
        val onClick: () -> Unit
    )

    data class TileAction(
        val iconRes: Int,
        val contentDescription: String,
        val tone: ActionTone = ActionTone.NORMAL,
        val onClick: () -> Unit
    )

    data class InteractiveSummaryCard(
        val root: LinearLayout,
        val title: TextView,
        val subtitle: TextView?
    )

    fun interactiveSummaryCard(
        activity: Activity,
        eyebrow: CharSequence? = null,
        title: CharSequence,
        subtitle: CharSequence? = null,
        fill: Int,
        accentOverride: Int? = null,
        subtitleAccent: Boolean = false,
        onClick: () -> Unit
    ): InteractiveSummaryCard {
        val palette =
            AppThemeManager.palette(activity)

        val root =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(activity, 14),
                    dp(activity, 11),
                    dp(activity, 14),
                    dp(activity, 11)
                )
                background =
                    AppThemeManager.largeCardDrawable(
                        context = activity,
                        fill = fill,
                        radiusDp = 14,
                        accentOverride =
                            accentOverride
                    )
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onClick()
                }
            }

        if (!eyebrow.isNullOrBlank()) {
            root.addView(
                TextView(activity).apply {
                    text = eyebrow
                    textSize = 11.5f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        0,
                        0,
                        0,
                        dp(activity, 4)
                    )
                }
            )
        }

        val titleView =
            TextView(activity).apply {
                text = title
                textSize = 15f
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
                setTextColor(
                    palette.text
                )
            }

        root.addView(titleView)

        val subtitleView =
            subtitle?.let { value ->
                TextView(activity).apply {
                    text = value
                    textSize = 12.5f
                    setTextColor(
                        if (subtitleAccent) {
                            palette.accent
                        } else {
                            palette.muted
                        }
                    )
                    setPadding(
                        0,
                        dp(activity, 4),
                        0,
                        0
                    )
                }.also { view ->
                    root.addView(view)
                }
            }

        return InteractiveSummaryCard(
            root = root,
            title = titleView,
            subtitle = subtitleView
        )
    }


    fun actionTile(
        activity: Activity,
        title: CharSequence,
        subtitle: CharSequence? = null,
        actions: List<TileAction> = emptyList(),
        onClick: (() -> Unit)? = null,
        onLongClick: (() -> Unit)? = null
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(
                activity
            )

        val root =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(activity, 14),
                    dp(activity, 10),
                    dp(activity, 8),
                    dp(activity, 10)
                )

                background =
                    AppThemeManager
                        .largeCardDrawable(
                            context = activity,
                            fill = palette.surface,
                            radiusDp = 14
                        )

                isClickable =
                    onClick != null

                isFocusable =
                    onClick != null

                if (onClick != null) {
                    setOnClickListener {
                        onClick()
                    }
                }

                if (onLongClick != null) {
                    setOnLongClickListener {
                        onLongClick()
                        true
                    }
                }
            }

        val contentColumn =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    0,
                    0,
                    dp(activity, 10),
                    0
                )
            }

        contentColumn.addView(
            TextView(activity).apply {
                text =
                    title

                textSize =
                    15f

                maxLines =
                    3

                setTypeface(
                    typeface,
                    Typeface.BOLD
                )

                setTextColor(
                    palette.text
                )
            }
        )

        if (!subtitle.isNullOrBlank()) {
            contentColumn.addView(
                TextView(activity).apply {
                    text =
                        subtitle

                    textSize =
                        12.5f

                    maxLines =
                        2

                    setTextColor(
                        palette.muted
                    )

                    setPadding(
                        0,
                        dp(activity, 6),
                        0,
                        0
                    )
                }
            )
        }

        root.addView(
            contentColumn,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        if (actions.isNotEmpty()) {
            val actionRail =
                LinearLayout(activity).apply {
                    orientation =
                        LinearLayout.VERTICAL

                    gravity =
                        Gravity.CENTER_HORIZONTAL
                }

            actions.forEachIndexed {
                    index,
                    action ->

                actionRail.addView(
                    ImageButton(activity).apply {
                        contentDescription =
                            action.contentDescription

                        setImageResource(
                            action.iconRes
                        )

                        imageTintList =
                            android.content.res
                                .ColorStateList
                                .valueOf(
                                    when (
                                        action.tone
                                    ) {
                                        ActionTone.DANGER ->
                                            palette.semantic.danger

                                        ActionTone.ACCENT ->
                                            palette.accent

                                        else ->
                                            palette.text
                                    }
                                )

                        scaleType =
                            android.widget
                                .ImageView
                                .ScaleType
                                .CENTER

                        setPadding(
                            dp(activity, 8),
                            dp(activity, 8),
                            dp(activity, 8),
                            dp(activity, 8)
                        )

                        minimumWidth = 0
                        minimumHeight = 0

                        background =
                            AppThemeManager
                                .surfaceDrawable(
                                    context = activity,
                                    fill =
                                        palette.surfaceAlt,
                                    radiusDp = 10,
                                    accentStroke = false
                                )

                        setOnClickListener {
                            action.onClick()
                        }
                    },
                    LinearLayout.LayoutParams(
                        dp(activity, 40),
                        dp(activity, 40)
                    ).apply {
                        if (index > 0) {
                            topMargin =
                                dp(activity, 6)
                        }
                    }
                )
            }

            root.addView(
                actionRail,
                LinearLayout.LayoutParams(
                    dp(activity, 40),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        return root
    }

    private fun fitsHorizontalActionGroup(
        context: Context,
        actionCount: Int,
        labels: List<CharSequence>,
        minButtonWidthDp: Int,
        textSizeSp: Float,
        chromeWidthDp: Float
    ): Boolean {
        if (actionCount <= 1) {
            return false
        }

        val metrics =
            context.resources.displayMetrics
        val density =
            metrics.density
        val paint =
            Paint().apply {
                textSize =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        textSizeSp,
                        metrics
                    )
            }

        val actionWidthsDp =
            if (
                labels.size ==
                    actionCount
            ) {
                labels.map { label ->
                    maxOf(
                        minButtonWidthDp
                            .toFloat(),
                        paint.measureText(
                            label.toString()
                        ) /
                            density +
                            32f
                    )
                }
            } else {
                List(
                    actionCount
                ) {
                    minButtonWidthDp
                        .toFloat()
                }
            }

        val gapsDp =
            (actionCount - 1) *
                8f
        val requiredWidthDp =
            chromeWidthDp +
                actionWidthsDp.sum() +
                gapsDp

        return context.resources
            .configuration
            .screenWidthDp
            .toFloat() >=
            requiredWidthDp
    }

    fun useHorizontalActionRow(
        context: Context,
        actionCount: Int,
        minButtonWidthDp: Int = 180,
        labels: List<CharSequence> = emptyList()
    ): Boolean =
        fitsHorizontalActionGroup(
            context = context,
            actionCount = actionCount,
            labels = labels,
            minButtonWidthDp = minButtonWidthDp,
            textSizeSp = 15f,
            chromeWidthDp = 48f
        )

    private fun useHorizontalDialogActionRow(
        context: Context,
        actions: List<DialogAction>,
        minButtonWidthDp: Int = 132
    ): Boolean =
        useHorizontalActionRow(
            context = context,
            actionCount = actions.size,
            minButtonWidthDp = minButtonWidthDp,
            labels =
                actions.map {
                    it.label
                }
        )

    fun addAdaptiveActionButtons(
        activity: Activity,
        container: LinearLayout,
        buttons: List<Button>,
        buttonHeightDp: Int = 54,
        tones: List<ActionTone> = emptyList()
    ) {
        if (buttons.isEmpty()) {
            return
        }

        val horizontal =
            useHorizontalActionRow(
                context = activity,
                actionCount = buttons.size,
                minButtonWidthDp = 132,
                labels =
                    buttons.map {
                        it.text
                    }
            )

        container.orientation =
            if (horizontal) {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }

        buttons.forEachIndexed { index, button ->
            styleAdaptiveActionButton(
                activity = activity,
                button = button,
                tone =
                    tones.getOrNull(
                        index
                    )
                        ?: if (index == 0) {
                            ActionTone.ACCENT
                        } else {
                            ActionTone.NORMAL
                        }
            )

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

    fun styleAdaptiveActionButton(
        activity: Activity,
        button: Button,
        tone: ActionTone
    ) {
        val palette =
            AppThemeManager.palette(
                activity
            )

        val enabledColor =
            when (tone) {
                ActionTone.NORMAL ->
                    palette.text

                ActionTone.ACCENT ->
                    palette.accent

                ActionTone.DANGER ->
                    palette.semantic.danger
            }

        button.isAllCaps =
            false
        button.gravity =
            Gravity.CENTER
        button.maxLines =
            1
        button.setPadding(
            dp(activity, 12),
            dp(activity, 8),
            dp(activity, 12),
            dp(activity, 8)
        )
        button.setTextColor(
            android.content.res.ColorStateList(
                arrayOf(
                    intArrayOf(
                        android.R.attr.state_enabled
                    ),
                    intArrayOf()
                ),
                intArrayOf(
                    enabledColor,
                    palette.muted
                )
            )
        )
        button.background =
            roundedBackground(
                context = activity,
                color =
                    palette.surfaceAlt,
                radiusDp = 12,
                strokeColor =
                    when (tone) {
                        ActionTone.NORMAL ->
                            palette.border

                        ActionTone.ACCENT ->
                            palette.accent

                        ActionTone.DANGER ->
                            palette.semantic.danger
                    }
            )
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
        extraBottomDp: Int = 10,
        includeIme: Boolean = false
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
            val insetTypes =
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    (if (includeIme) WindowInsetsCompat.Type.ime() else 0)
            val bars = insets.getInsets(insetTypes)

            view.setPadding(
                initialLeft + bars.left,
                initialTop + bars.top + dp(view.context, extraTopDp),
                initialRight + bars.right,
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
    ): Dialog {
        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        val content =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        actions.forEachIndexed { index, action ->
            content.addView(
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

        return showFixedFooterContentDialog(
            activity = activity,
            dialog = dialog,
            card = card,
            content = content,
            actions =
                listOf(
                    DialogAction(
                        label = negativeLabel,
                        tone = ActionTone.ACCENT
                    ) {
                        onNegative?.invoke()
                    }
                ),
            actionLayout =
                DialogActionLayout.AUTO
        )
    }

    fun showRecordDialog(
        activity: Activity,
        title: String,
        records: List<DialogRecord>,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE
    ): Dialog {
        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        val content =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        records.forEachIndexed { index, record ->
            content.addView(
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
            content.addView(
                SpaceView(
                    activity,
                    dp(activity, 4)
                )
            )
        }

        return showFixedFooterContentDialog(
            activity = activity,
            dialog = dialog,
            card = card,
            content = content,
            actions = actions,
            actionLayout = actionLayout
        )
    }

    fun showFixedFooterMessageDialog(
        activity: Activity,
        title: String,
        message: String,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.AUTO
    ): Dialog =
        showMessageDialog(
            activity = activity,
            title = title,
            message = message,
            actions = actions,
            subtitle = subtitle,
            actionLayout = actionLayout
        )

    fun showMessageDialog(
        activity: Activity,
        title: String,
        message: String,
        actions: List<DialogAction>,
        subtitle: String? = null,
        actionLayout: DialogActionLayout = DialogActionLayout.AUTO,
        heightFraction: Float = 1f
    ): Dialog {
        val palette =
            AppThemeManager.palette(activity)

        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        val content =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        content.addView(
            TextView(activity).apply {
                text = message
                textSize = 15f
                setTextColor(palette.text)
                setTextIsSelectable(true)
                setLineSpacing(0f, 1.08f)
                setPadding(
                    0,
                    dp(context, 6),
                    0,
                    dp(context, 12)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        return showFixedFooterContentDialog(
            activity = activity,
            dialog = dialog,
            card = card,
            content = content,
            actions = actions,
            actionLayout = actionLayout
        )
    }

    fun showDangerConfirmDialog(
        activity: Activity,
        title: String,
        message: String,
        confirmLabel: String,
        onCancel: () -> Unit = {},
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
                        onClick = onCancel
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
        val palette =
            AppThemeManager.palette(activity)

        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        val body =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        if (message.isNotBlank()) {
            body.addView(
                TextView(activity).apply {
                    text = message
                    textSize = 15f
                    setTextColor(palette.text)
                    setTextIsSelectable(true)
                    setLineSpacing(0f, 1.08f)
                    setPadding(
                        0,
                        dp(context, 6),
                        0,
                        dp(context, 12)
                    )
                }
            )
        }

        body.addView(
            content,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin =
                    dp(activity, 4)
            }
        )

        return showFixedFooterContentDialog(
            activity = activity,
            dialog = dialog,
            card = card,
            content = body,
            actions = actions,
            actionLayout = actionLayout,
            heightFraction = heightFraction
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

        val palette =
            AppThemeManager.palette(activity)

        val dialog = customDialog(activity)
        val card = dialogCard(activity)

        addDialogHeader(
            activity = activity,
            card = card,
            title = title,
            subtitle = subtitle
        )

        val choices =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        items.forEachIndexed { index, label ->
            choices.addView(
                CheckBox(activity).apply {
                    text = label
                    textSize = 14f
                    setTextColor(palette.text)
                    setPadding(
                        dp(context, 12),
                        dp(context, 9),
                        dp(context, 12),
                        dp(context, 9)
                    )
                    background =
                        roundedBackground(
                            context = context,
                            color = palette.surfaceAlt,
                            radiusDp = 12,
                            strokeColor = palette.border
                        )
                    isChecked =
                        checked[index]
                    setOnCheckedChangeListener {
                            _,
                            value ->
                        checked[index] =
                            value
                        onCheckedChange(
                            index,
                            value
                        )
                    }
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin =
                            dp(activity, 8)
                    }
                }
            )
        }

        return showFixedFooterContentDialog(
            activity = activity,
            dialog = dialog,
            card = card,
            content = choices,
            actions = actions,
            actionLayout = actionLayout
        )
    }

    fun emphasizedTitle(
        activity: Activity,
        label: CharSequence,
        textSizeSp: Float = 20f,
        maxLines: Int = 1
    ): TextView =
        TextView(activity).apply {
            text = label
            textSize = textSizeSp
            setTextColor(
                AppThemeManager
                    .palette(activity)
                    .accent
            )
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            this.maxLines = maxLines
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
        if (actions.isEmpty()) {
            return
        }

        fun actionView(
            action: DialogAction
        ): Button =
            dialogActionButton(
                activity = activity,
                action = action
            ) {
                if (
                    action.dismissOnClick
                ) {
                    dialog.dismiss()
                }

                action.onClick()
            }

        fun addVertical(
            items: List<DialogAction>,
            initialTopMarginDp: Int = 0
        ) {
            items.forEachIndexed {
                    index,
                    action ->
                card.addView(
                    actionView(action),
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin =
                            when {
                                index == 0 ->
                                    dp(
                                        activity,
                                        initialTopMarginDp
                                    )
                                else ->
                                    dp(activity, 8)
                            }
                    }
                )
            }
        }

        fun addHorizontal(
            items: List<DialogAction>,
            initialTopMarginDp: Int = 0
        ) {
            val row =
                LinearLayout(activity).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                    isBaselineAligned =
                        false
                }

            orderHorizontalActions(items)
                .forEachIndexed {
                        index,
                        action ->
                    row.addView(
                        actionView(action),
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

            card.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(
                            activity,
                            initialTopMarginDp
                        )
                }
            )
        }

        if (
            actionLayout ==
            DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE
        ) {
            addVertical(actions)
            return
        }

        if (
            actionLayout ==
                DialogActionLayout.PRIMARY_TOP &&
            actions.size == 3
        ) {
            val primary =
                actions.first()

            card.addView(
                actionView(primary),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            val secondary =
                actions.drop(1)

            if (
                useHorizontalDialogActionRow(
                    context = activity,
                    actions = secondary
                )
            ) {
                addHorizontal(
                    items = secondary,
                    initialTopMarginDp = 9
                )
            } else {
                addVertical(
                    items = secondary,
                    initialTopMarginDp = 9
                )
            }
            return
        }

        if (
            actionLayout ==
                DialogActionLayout.AUTO &&
            useHorizontalDialogActionRow(
                context = activity,
                actions = actions
            )
        ) {
            addHorizontal(actions)
            return
        }

        addVertical(actions)
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
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(activity)

        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(context, 14),
                dp(context, 12),
                dp(context, 14),
                dp(context, 12)
            )
            background = roundedBackground(
                context = context,
                color = palette.surfaceAlt,
                radiusDp = 13,
                strokeColor = palette.border
            )

            addView(
                TextView(activity).apply {
                    text = record.title
                    textSize = 14.5f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(
                        when (record.tone) {
                            ActionTone.NORMAL -> palette.text
                            ActionTone.ACCENT -> palette.accent
                            ActionTone.DANGER -> palette.semantic.danger
                        }
                    )
                    setLineSpacing(0f, 1.05f)
                }
            )

            addView(
                TextView(activity).apply {
                    text = record.detail
                    textSize = 13f
                    setTextColor(palette.muted)
                    setPadding(0, dp(context, 5), 0, 0)
                    setLineSpacing(0f, 1.08f)
                }
            )
        }
    }

    private fun dialogCard(
        activity: Activity
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(activity)

        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(context, 18),
                dp(context, 18),
                dp(context, 18),
                dp(context, 18)
            )
            background = roundedBackground(
                context = context,
                color = palette.surface,
                radiusDp = 18,
                strokeColor = palette.border
            )
        }
    }

    private fun addDialogHeader(
        activity: Activity,
        card: LinearLayout,
        title: String,
        subtitle: String?
    ) {
        val palette =
            AppThemeManager.palette(activity)

        card.addView(
            emphasizedTitle(
                activity = activity,
                label = title,
                textSizeSp = 22f,
                maxLines = 2
            )
        )

        if (!subtitle.isNullOrBlank()) {
            card.addView(
                TextView(activity).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(palette.muted)
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

    private fun showFixedFooterContentDialog(
        activity: Activity,
        dialog: Dialog,
        card: LinearLayout,
        content: View,
        actions: List<DialogAction>,
        actionLayout: DialogActionLayout,
        heightFraction: Float = 1f
    ): Dialog {
        val contentScroll =
            ScrollView(activity).apply {
                isFillViewport = true
                overScrollMode =
                    View.OVER_SCROLL_IF_CONTENT_SCROLLS
                isVerticalScrollBarEnabled = true
            }

        contentScroll.addView(
            content,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            contentScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        if (actions.isNotEmpty()) {
            val footer =
                LinearLayout(activity).apply {
                    orientation =
                        LinearLayout.VERTICAL
                    setPadding(
                        0,
                        dp(context, 10),
                        0,
                        0
                    )
                }

            addDialogActions(
                activity = activity,
                dialog = dialog,
                card = footer,
                actions = actions,
                actionLayout = actionLayout
            )

            card.addView(
                footer,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        return showFixedFooterDialog(
            activity = activity,
            dialog = dialog,
            card = card,
            heightFraction = heightFraction
        )
    }

    private fun showFixedFooterDialog(
        activity: Activity,
        dialog: Dialog,
        card: LinearLayout,
        heightFraction: Float = 1f
    ): Dialog {
        val boundedHeightFraction =
            heightFraction
                .coerceIn(
                    0.45f,
                    1f
                )
        val horizontalInset =
            dp(activity, 18)

        val minimumVerticalInset =
            dp(activity, 12)

        val outer =
            FrameLayout(activity).apply {
                alpha = 0f
            }

        outer.addView(
            card,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams
                    .MATCH_PARENT,
                ViewGroup.LayoutParams
                    .MATCH_PARENT
            )
        )

        if (
            boundedHeightFraction <
            0.999f
        ) {
            outer.addOnLayoutChangeListener {
                    view,
                    _,
                    _,
                    _,
                    _,
                    _,
                    _,
                    _,
                    _ ->
                val availableHeight =
                    (
                        view.height -
                            view.paddingTop -
                            view.paddingBottom
                        )
                        .coerceAtLeast(0)

                if (
                    availableHeight >
                    0
                ) {
                    val preferredHeight =
                        (
                            availableHeight *
                                boundedHeightFraction
                            )
                            .toInt()
                            .coerceAtLeast(
                                dp(
                                    activity,
                                    320
                                )
                            )
                            .coerceAtMost(
                                availableHeight
                            )

                    val layoutParams =
                        card.layoutParams as
                            FrameLayout.LayoutParams

                    if (
                        layoutParams.height !=
                        preferredHeight
                    ) {
                        layoutParams.height =
                            preferredHeight
                        layoutParams.gravity =
                            Gravity.TOP or
                                Gravity.CENTER_HORIZONTAL
                        card.layoutParams =
                            layoutParams
                    }
                }
            }
        }

        fun configureWindow() {
            val window =
                dialog.window
                    ?: return

            window.setWindowAnimations(0)
            window.attributes =
                window.attributes.apply {
                    windowAnimations = 0
                }

            window.setGravity(
                Gravity.TOP or
                    Gravity.CENTER_HORIZONTAL
            )

            WindowCompat
                .setDecorFitsSystemWindows(
                    window,
                    false
                )

            window.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

            window.setLayout(
                ViewGroup.LayoutParams
                    .MATCH_PARENT,
                ViewGroup.LayoutParams
                    .MATCH_PARENT
            )
        }

        var insetsApplied =
            false

        var stablePreDraws =
            0

        var lastGeometry:
            String? = null

        dialog.setContentView(
            outer
        )

        fun hideDecor() {
            dialog.window
                ?.decorView
                ?.alpha = 0f
        }

        fun revealAfterStableGeometry() {
            val decor =
                dialog.window
                    ?.decorView
                    ?: return

            decor.viewTreeObserver
                .addOnPreDrawListener(
                    object :
                        ViewTreeObserver
                            .OnPreDrawListener {
                        override fun onPreDraw():
                            Boolean {
                            if (!insetsApplied) {
                                return true
                            }

                            val geometry =
                                listOf(
                                    outer.measuredWidth,
                                    outer.measuredHeight,
                                    outer.paddingLeft,
                                    outer.paddingTop,
                                    outer.paddingRight,
                                    outer.paddingBottom,
                                    card.measuredWidth,
                                    card.measuredHeight
                                ).joinToString(
                                    ":"
                                )

                            if (
                                outer.measuredWidth <= 0 ||
                                outer.measuredHeight <= 0 ||
                                card.measuredWidth <= 0 ||
                                card.measuredHeight <= 0
                            ) {
                                stablePreDraws = 0
                                return true
                            }

                            if (
                                geometry ==
                                lastGeometry
                            ) {
                                stablePreDraws += 1
                            } else {
                                lastGeometry =
                                    geometry
                                stablePreDraws = 0
                            }

                            if (
                                stablePreDraws >= 1
                            ) {
                                if (
                                    decor
                                        .viewTreeObserver
                                        .isAlive
                                ) {
                                    decor
                                        .viewTreeObserver
                                        .removeOnPreDrawListener(
                                            this
                                        )
                                }

                                outer.alpha = 1f
                                decor.alpha = 1f
                            }

                            return true
                        }
                    }
                )
        }

        ViewCompat
            .setOnApplyWindowInsetsListener(
                outer
            ) {
                    view,
                    insets ->
                val safeInsets =
                    insets.getInsets(
                        WindowInsetsCompat.Type
                            .systemBars() or
                            WindowInsetsCompat.Type
                                .displayCutout()
                    )

                view.setPadding(
                    horizontalInset +
                        safeInsets.left,
                    maxOf(
                        minimumVerticalInset,
                        safeInsets.top +
                            dp(
                                activity,
                                8
                            )
                    ),
                    horizontalInset +
                        safeInsets.right,
                    maxOf(
                        minimumVerticalInset,
                        safeInsets.bottom +
                            dp(
                                activity,
                                8
                            )
                    )
                )

                insetsApplied = true
                stablePreDraws = 0
                lastGeometry = null
                view.requestLayout()

                insets
            }

        hideDecor()
        configureWindow()

        dialog.show()

        hideDecor()
        configureWindow()
        revealAfterStableGeometry()

        ViewCompat
            .requestApplyInsets(
                outer
            )

        return dialog
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
                horizontalInset + safeInsets.left,
                maxOf(minimumVerticalInset, safeInsets.top + dp(activity, 8)),
                horizontalInset + safeInsets.right,
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
    ): Button {
        val palette =
            AppThemeManager.palette(activity)

        return Button(activity).apply {
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
                if (accent) palette.accent else palette.text
            )
            background = roundedBackground(
                context = context,
                color = palette.surfaceAlt,
                radiusDp = 14,
                strokeColor = palette.border
            )
            autoSizeButton(this, minSp = 12, maxSp = 15)
            setOnClickListener { onClick() }
        }
    }

    private fun dialogActionButton(
        activity: Activity,
        action: DialogAction,
        onClick: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(activity)

        return Button(activity).apply {
            text = action.label
            isAllCaps = false
            gravity = Gravity.CENTER
            minHeight = dp(context, 54)
            minimumHeight = dp(context, 54)
            minWidth = 0
            minimumWidth = 0
            textSize = 14f
            maxLines = 1
            setPadding(
                dp(context, 12),
                dp(context, 9),
                dp(context, 12),
                dp(context, 9)
            )
            setTextColor(
                when (action.tone) {
                    ActionTone.NORMAL -> palette.text
                    ActionTone.ACCENT -> palette.accent
                    ActionTone.DANGER -> palette.semantic.danger
                }
            )
            background = roundedBackground(
                context = context,
                color = palette.surfaceAlt,
                radiusDp = 12,
                strokeColor = palette.border
            )
            setOnClickListener { onClick() }
        }
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
