package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

class ListSelectorActivity : Activity() {
    private lateinit var mode: Mode
    private lateinit var labels: List<String>
    private lateinit var values: List<String>
    private lateinit var selected: BooleanArray
    private lateinit var content: LinearLayout
    private lateinit var selectionSummary: TextView
    private lateinit var confirmButton: Button

    private var titleText: String = "Вибір"
    private var subtitleText: String = ""
    private var helpTitle: String = "Що це за список?"
    private var helpMessage: String = ""
    private var confirmLabel: String = "Вибрати"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        mode = runCatching {
            Mode.valueOf(
                intent.getStringExtra(EXTRA_MODE)
                    ?: Mode.SINGLE.name
            )
        }.getOrDefault(Mode.SINGLE)

        labels =
            intent.getStringArrayListExtra(EXTRA_LABELS)
                ?.toList()
                .orEmpty()

        values =
            intent.getStringArrayListExtra(EXTRA_VALUES)
                ?.toList()
                .orEmpty()

        require(labels.size == values.size) {
            "Selector labels/values size mismatch"
        }

        titleText =
            intent.getStringExtra(EXTRA_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: "Вибір"

        subtitleText =
            intent.getStringExtra(EXTRA_SUBTITLE)
                .orEmpty()

        helpTitle =
            intent.getStringExtra(EXTRA_HELP_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: "Що це за список?"

        helpMessage =
            intent.getStringExtra(EXTRA_HELP_MESSAGE)
                .orEmpty()

        confirmLabel =
            intent.getStringExtra(EXTRA_CONFIRM_LABEL)
                ?.takeIf { it.isNotBlank() }
                ?: if (mode == Mode.MULTI) "Далі" else "Вибрати"

        val initial =
            (
                savedInstanceState
                    ?.getStringArrayList(
                        STATE_SELECTED_VALUES
                    )
                    ?: intent.getStringArrayListExtra(
                        EXTRA_SELECTED_VALUES
                    )
            )
                ?.toSet()
                .orEmpty()

        selected =
            BooleanArray(values.size) { index ->
                values[index] in initial
            }

        if (
            mode == Mode.SINGLE &&
            selected.count { it } > 1
        ) {
            var firstFound = false
            selected.indices.forEach { index ->
                if (selected[index]) {
                    if (firstFound) {
                        selected[index] = false
                    } else {
                        firstFound = true
                    }
                }
            }
        }

        render()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        val selectedValues =
            arrayListOf<String>()

        selected.indices.forEach { index ->
            if (selected[index]) {
                selectedValues += values[index]
            }
        }

        outState.putStringArrayList(
            STATE_SELECTED_VALUES,
            selectedValues
        )

        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }

    private fun render() {
        val palette =
            AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(palette.background)
            }

        root.addView(topBar())

        if (subtitleText.isNotBlank()) {
            root.addView(
                TextView(this).apply {
                    text = subtitleText
                    textSize = 13f
                    setTextColor(palette.muted)
                    setPadding(
                        dp(18),
                        0,
                        dp(18),
                        dp(8)
                    )
                }
            )
        }

        selectionSummary =
            TextView(this).apply {
                textSize = 12.5f
                setTextColor(palette.muted)
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(8)
                )
            }

        root.addView(selectionSummary)

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
                clipToPadding = false
            }

        content =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(12)
                )
            }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(footer())

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)

        renderItems()
        refreshSelectionState()
    }

    private fun topBar(): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            addView(
                UiChrome.backButton(
                    activity = this@ListSelectorActivity,
                    onClick = { finish() }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                TextView(this@ListSelectorActivity).apply {
                    text = titleText
                    textSize = 20f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(palette.text)
                    setPadding(
                        dp(12),
                        0,
                        dp(8),
                        0
                    )
                    maxLines = 2
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            if (helpMessage.isNotBlank()) {
                addView(
                    Button(this@ListSelectorActivity).apply {
                        text = "?"
                        isAllCaps = false
                        textSize = 20f
                        setTypeface(typeface, Typeface.BOLD)
                        setTextColor(palette.text)
                        minWidth = 0
                        minimumWidth = 0
                        minHeight = 0
                        minimumHeight = 0
                        setPadding(0, 0, 0, 0)
                        background =
                            AppThemeManager.neutralButtonDrawable(
                                this@ListSelectorActivity
                            )
                        setOnClickListener {
                            showHelp()
                        }
                    },
                    LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                    )
                )
            }
        }
    }

    private fun footer(): LinearLayout {
        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(
                    dp(12),
                    dp(10),
                    dp(12),
                    dp(10)
                )
            }

        confirmButton =
            footerButton(
                label = confirmLabel,
                primary = true
            ) {
                finishWithSelection()
            }

        root.addView(
            confirmButton,
            LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            )
        )

        root.addView(
            footerButton(
                label = "Скасувати",
                primary = false
            ) {
                finish()
            },
            LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            ).apply {
                marginStart = dp(8)
            }
        )

        return root
    }

    private fun renderItems() {
        content.removeAllViews()

        if (labels.isEmpty()) {
            val palette =
                AppThemeManager.palette(this)

            content.addView(
                TextView(this).apply {
                    text = "Немає доступних елементів."
                    textSize = 15f
                    gravity = Gravity.CENTER
                    setTextColor(palette.muted)
                    setPadding(
                        dp(18),
                        dp(36),
                        dp(18),
                        dp(36)
                    )
                }
            )
            return
        }

        labels.indices.forEach { index ->
            val view =
                if (mode == Mode.MULTI) {
                    multiChoiceView(index)
                } else {
                    singleChoiceView(index)
                }

            content.addView(
                view,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin = dp(9)
                    }
                }
            )
        }
    }

    private fun singleChoiceView(index: Int): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text =
                buildString {
                    if (selected[index]) {
                        append("✓ ")
                    }
                    append(labels[index])
                }
            isAllCaps = false
            textSize = 15f
            gravity =
                Gravity.START or Gravity.CENTER_VERTICAL
            setTextColor(palette.text)
            setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
            )
            minimumHeight = dp(72)
            background =
                if (selected[index]) {
                    AppThemeManager.accentButtonDrawable(
                        this@ListSelectorActivity
                    )
                } else {
                    AppThemeManager.surfaceDrawable(
                        context = this@ListSelectorActivity,
                        fill = palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )
                }
            setOnClickListener {
                selected.indices.forEach {
                    selected[it] = it == index
                }
                renderItems()
                refreshSelectionState()
            }
        }
    }

    private fun multiChoiceView(index: Int): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        val row =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
                gravity =
                    Gravity.CENTER_VERTICAL
                minimumHeight =
                    dp(72)
                background =
                    AppThemeManager.surfaceDrawable(
                        context =
                            this@ListSelectorActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )
                setPadding(
                    dp(6),
                    dp(8),
                    dp(14),
                    dp(8)
                )
            }

        val checkBox =
            CheckBox(this).apply {
                text = ""
                isChecked =
                    selected[index]
                gravity =
                    Gravity.CENTER
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0
                setPadding(
                    0,
                    0,
                    0,
                    0
                )
                setOnCheckedChangeListener {
                        _,
                        value ->
                    selected[index] =
                        value
                    refreshSelectionState()
                }
            }

        val checkColumn =
            FrameLayout(this).apply {
                addView(
                    checkBox,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                )
            }

        row.addView(
            checkColumn,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        row.addView(
            TextView(this).apply {
                text =
                    labels[index]
                textSize =
                    15f
                gravity =
                    Gravity.START or
                        Gravity.CENTER_VERTICAL
                setTextColor(
                    palette.text
                )
                setPadding(
                    dp(8),
                    0,
                    0,
                    0
                )
                setOnClickListener {
                    checkBox.isChecked =
                        !checkBox.isChecked
                }
            },
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.setOnClickListener {
            checkBox.isChecked =
                !checkBox.isChecked
        }

        return row
    }

    private fun refreshSelectionState() {
        val count =
            selected.count { it }

        selectionSummary.text =
            if (mode == Mode.MULTI) {
                "Вибрано: $count із ${labels.size}"
            } else {
                if (count == 0) {
                    "Виберіть один елемент"
                } else {
                    "Вибрано 1 із ${labels.size}"
                }
            }

        confirmButton.isEnabled =
            count > 0

        confirmButton.alpha =
            if (confirmButton.isEnabled) 1f else 0.5f
    }

    private fun showHelp() {
        UiChrome.showMessageDialog(
            activity = this,
            title = helpTitle,
            message = helpMessage,
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Зрозуміло",
                        tone = UiChrome.ActionTone.ACCENT
                    ) {}
                )
        )
    }

    private fun finishWithSelection() {
        val resultValues =
            arrayListOf<String>()

        selected.indices.forEach { index ->
            if (selected[index]) {
                resultValues += values[index]
            }
        }

        if (resultValues.isEmpty()) {
            return
        }

        setResult(
            RESULT_OK,
            Intent().putStringArrayListExtra(
                EXTRA_SELECTED_VALUES,
                resultValues
            )
        )

        finish()
    }

    private fun footerButton(
        label: String,
        primary: Boolean,
        onClick: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            setTextColor(palette.text)
            background =
                if (primary) {
                    AppThemeManager.accentButtonDrawable(
                        this@ListSelectorActivity
                    )
                } else {
                    AppThemeManager.neutralButtonDrawable(
                        this@ListSelectorActivity
                    )
                }
            setOnClickListener {
                onClick()
            }
        }
    }

    private fun dp(value: Int): Int =
        (
            value *
                resources.displayMetrics.density
        ).toInt()

    private enum class Mode {
        SINGLE,
        MULTI
    }

    companion object {
        const val EXTRA_SELECTED_VALUES =
            "selector_selected_values"

        private const val STATE_SELECTED_VALUES =
            "selector_state_selected_values"

        private const val EXTRA_TITLE =
            "selector_title"
        private const val EXTRA_SUBTITLE =
            "selector_subtitle"
        private const val EXTRA_HELP_TITLE =
            "selector_help_title"
        private const val EXTRA_HELP_MESSAGE =
            "selector_help_message"
        private const val EXTRA_LABELS =
            "selector_labels"
        private const val EXTRA_VALUES =
            "selector_values"
        private const val EXTRA_MODE =
            "selector_mode"
        private const val EXTRA_CONFIRM_LABEL =
            "selector_confirm_label"

        fun singleIntent(
            activity: Activity,
            title: String,
            subtitle: String,
            labels: List<String>,
            values: List<String>,
            helpTitle: String,
            helpMessage: String,
            confirmLabel: String = "Вибрати"
        ): Intent =
            baseIntent(
                activity = activity,
                title = title,
                subtitle = subtitle,
                labels = labels,
                values = values,
                mode = Mode.SINGLE,
                selectedValues = emptyList(),
                helpTitle = helpTitle,
                helpMessage = helpMessage,
                confirmLabel = confirmLabel
            )

        fun multiIntent(
            activity: Activity,
            title: String,
            subtitle: String,
            labels: List<String>,
            values: List<String>,
            selectedValues: List<String>,
            helpTitle: String,
            helpMessage: String,
            confirmLabel: String = "Далі"
        ): Intent =
            baseIntent(
                activity = activity,
                title = title,
                subtitle = subtitle,
                labels = labels,
                values = values,
                mode = Mode.MULTI,
                selectedValues = selectedValues,
                helpTitle = helpTitle,
                helpMessage = helpMessage,
                confirmLabel = confirmLabel
            )

        private fun baseIntent(
            activity: Activity,
            title: String,
            subtitle: String,
            labels: List<String>,
            values: List<String>,
            mode: Mode,
            selectedValues: List<String>,
            helpTitle: String,
            helpMessage: String,
            confirmLabel: String
        ): Intent =
            Intent(
                activity,
                ListSelectorActivity::class.java
            ).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
                putStringArrayListExtra(
                    EXTRA_LABELS,
                    ArrayList(labels)
                )
                putStringArrayListExtra(
                    EXTRA_VALUES,
                    ArrayList(values)
                )
                putStringArrayListExtra(
                    EXTRA_SELECTED_VALUES,
                    ArrayList(selectedValues)
                )
                putExtra(EXTRA_MODE, mode.name)
                putExtra(EXTRA_HELP_TITLE, helpTitle)
                putExtra(EXTRA_HELP_MESSAGE, helpMessage)
                putExtra(EXTRA_CONFIRM_LABEL, confirmLabel)
            }
    }
}
