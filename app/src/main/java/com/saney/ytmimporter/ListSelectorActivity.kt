package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome
import org.json.JSONArray
import org.json.JSONObject

class ListSelectorActivity : Activity() {
    private lateinit var mode: Mode
    private lateinit var titleText: String
    private var subtitleText: String? = null
    private var helpText: String? = null
    private var items: List<SelectorItem> = emptyList()
    private val selectedValues = linkedSetOf<String>()
    private var selectedCountText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        mode =
            runCatching {
                Mode.valueOf(
                    intent.getStringExtra(EXTRA_MODE)
                        ?: Mode.SINGLE.name
                )
            }.getOrDefault(
                Mode.SINGLE
            )

        titleText =
            intent.getStringExtra(EXTRA_TITLE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "Вибір"

        subtitleText =
            intent.getStringExtra(EXTRA_SUBTITLE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        helpText =
            intent.getStringExtra(EXTRA_HELP_TEXT)
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        items =
            decodeItems(
                intent.getStringExtra(
                    EXTRA_ITEMS_JSON
                )
            )

        selectedValues +=
            decodeStringArray(
                savedInstanceState
                    ?.getString(
                        STATE_SELECTED_JSON
                    )
                    ?: intent.getStringExtra(
                        EXTRA_SELECTED_JSON
                    )
            )

        render()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putString(
            STATE_SELECTED_JSON,
            encodeStringArray(
                selectedValues
            )
        )
        super.onSaveInstanceState(
            outState
        )
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

        subtitleText?.let { subtitle ->
            root.addView(
                TextView(this).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(18),
                        0,
                        dp(18),
                        dp(8)
                    )
                }
            )
        }

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
                clipToPadding = false
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(12)
                )
            }

        if (items.isEmpty()) {
            content.addView(
                TextView(this).apply {
                    text =
                        "Немає доступних елементів."
                    gravity =
                        Gravity.CENTER
                    textSize = 15f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(18),
                        dp(42),
                        dp(18),
                        dp(42)
                    )
                }
            )
        } else {
            items.forEachIndexed {
                    index,
                    item ->

                val view =
                    if (
                        mode ==
                            Mode.MULTI
                    ) {
                        multiItemRow(
                            item
                        )
                    } else {
                        singleItemButton(
                            item
                        )
                    }

                content.addView(
                    view,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) {
                            topMargin =
                                dp(9)
                        }
                    }
                )
            }
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

        root.addView(
            footer()
        )

        setContentView(root)
        UiChrome.applyScreenInsets(
            this,
            root
        )

        updateSelectedCount()
    }

    private fun topBar():
        LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
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
                        this@ListSelectorActivity,
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
                TextView(
                    this@ListSelectorActivity
                ).apply {
                    text = titleText
                    textSize = 20f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                    maxLines = 2
                    setPadding(
                        dp(12),
                        0,
                        dp(8),
                        0
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            if (!helpText.isNullOrBlank()) {
                addView(
                    Button(
                        this@ListSelectorActivity
                    ).apply {
                        text = "?"
                        isAllCaps = false
                        textSize = 20f
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                        setTextColor(
                            palette.text
                        )
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
                        background =
                            AppThemeManager
                                .neutralButtonDrawable(
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

    private fun footer():
        LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(10)
            )

            if (mode == Mode.MULTI) {
                selectedCountText =
                    TextView(
                        this@ListSelectorActivity
                    ).apply {
                        textSize = 12.5f
                        setTextColor(
                            palette.muted
                        )
                        gravity =
                            Gravity.CENTER_HORIZONTAL
                        setPadding(
                            0,
                            0,
                            0,
                            dp(6)
                        )
                    }

                addView(
                    selectedCountText
                )

                addView(
                    footerButton(
                        label = "Далі",
                        primary = true
                    ) {
                        if (
                            selectedValues
                                .isEmpty()
                        ) {
                            toast(
                                "Виберіть хоча б один елемент."
                            )
                        } else {
                            finishMultiSelection()
                        }
                    },
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(54)
                    )
                )
            }

            addView(
                footerButton(
                    label = "Скасувати",
                    primary = false
                ) {
                    finish()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    if (mode == Mode.MULTI) {
                        topMargin =
                            dp(8)
                    }
                }
            )
        }
    }

    private fun singleItemButton(
        item: SelectorItem
    ): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text = item.label
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
                dp(72)
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@ListSelectorActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )
            setOnClickListener {
                finishSingleSelection(
                    item.value
                )
            }
        }
    }

    private fun multiItemRow(
        item: SelectorItem
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        var checkBox:
            CheckBox? =
            null

        fun setSelected(
            selected: Boolean
        ) {
            if (selected) {
                selectedValues +=
                    item.value
            } else {
                selectedValues -=
                    item.value
            }

            checkBox?.let {
                    view ->

                if (
                    view.isChecked !=
                        selected
                ) {
                    view.isChecked =
                        selected
                }
            }

            updateSelectedCount()
        }

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                dp(10),
                dp(10),
                dp(12),
                dp(10)
            )
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@ListSelectorActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )

            checkBox =
                CheckBox(
                    this@ListSelectorActivity
                ).apply {
                    isChecked =
                        item.value in
                            selectedValues
                    buttonTintList =
                        ColorStateList.valueOf(
                            palette.accent
                        )
                    setOnCheckedChangeListener {
                            _,
                            isChecked ->

                        if (
                            (
                                item.value in
                                    selectedValues
                            ) !=
                                isChecked
                        ) {
                            setSelected(
                                isChecked
                            )
                        }
                    }
                }

            addView(
                requireNotNull(
                    checkBox
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                TextView(
                    this@ListSelectorActivity
                ).apply {
                    text = item.label
                    textSize = 15f
                    setTextColor(
                        palette.text
                    )
                    setLineSpacing(
                        0f,
                        1.04f
                    )
                    setOnClickListener {
                        setSelected(
                            item.value !in
                                selectedValues
                        )
                    }
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            setOnClickListener {
                setSelected(
                    item.value !in
                        selectedValues
                )
            }
        }
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
            textSize = 15f
            setTextColor(
                palette.text
            )
            background =
                if (primary) {
                    AppThemeManager
                        .accentButtonDrawable(
                            this@ListSelectorActivity
                        )
                } else {
                    AppThemeManager
                        .neutralButtonDrawable(
                            this@ListSelectorActivity
                        )
                }
            setOnClickListener {
                onClick()
            }
        }
    }

    private fun showHelp() {
        val message =
            helpText
                ?: return

        UiChrome.showMessageDialog(
            activity = this,
            title = "Про цей список",
            message = message,
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Зрозуміло",
                        tone =
                            UiChrome.ActionTone.ACCENT,
                        onClick = {}
                    )
                )
        )
    }

    private fun finishSingleSelection(
        value: String
    ) {
        setResult(
            RESULT_OK,
            Intent().putExtra(
                EXTRA_SELECTED_VALUE,
                value
            )
        )
        finish()
    }

    private fun finishMultiSelection() {
        setResult(
            RESULT_OK,
            Intent().putExtra(
                EXTRA_SELECTED_JSON,
                encodeStringArray(
                    selectedValues
                )
            )
        )
        finish()
    }

    private fun updateSelectedCount() {
        selectedCountText
            ?.text =
            "Вибрано: " +
                selectedValues.size
    }

    private fun toast(
        message: String
    ) {
        android.widget.Toast
            .makeText(
                this,
                message,
                android.widget.Toast.LENGTH_SHORT
            )
            .show()
    }

    private fun decodeItems(
        raw: String?
    ): List<SelectorItem> =
        runCatching {
            val array =
                JSONArray(
                    raw ?: "[]"
                )

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    val item =
                        array.getJSONObject(
                            index
                        )

                    add(
                        SelectorItem(
                            label =
                                item.getString(
                                    "label"
                                ),
                            value =
                                item.getString(
                                    "value"
                                )
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )

    private fun decodeStringArray(
        raw: String?
    ): List<String> =
        runCatching {
            val array =
                JSONArray(
                    raw ?: "[]"
                )

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    add(
                        array.getString(
                            index
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )

    private fun encodeStringArray(
        values: Collection<String>
    ): String =
        JSONArray().apply {
            values.forEach {
                put(it)
            }
        }.toString()

    private fun dp(value: Int): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    private data class SelectorItem(
        val label: String,
        val value: String
    )

    private enum class Mode {
        SINGLE,
        MULTI
    }

    companion object {
        const val EXTRA_TITLE =
            "selector_title"
        const val EXTRA_SUBTITLE =
            "selector_subtitle"
        const val EXTRA_HELP_TEXT =
            "selector_help_text"
        const val EXTRA_MODE =
            "selector_mode"
        const val EXTRA_ITEMS_JSON =
            "selector_items_json"
        const val EXTRA_SELECTED_JSON =
            "selector_selected_json"
        const val EXTRA_SELECTED_VALUE =
            "selector_selected_value"

        private const val STATE_SELECTED_JSON =
            "selector_state_selected_json"

        fun singleIntent(
            activity: Activity,
            title: String,
            subtitle: String?,
            helpText: String?,
            items:
                List<Pair<String, String>>
        ): Intent =
            baseIntent(
                activity = activity,
                title = title,
                subtitle = subtitle,
                helpText = helpText,
                mode = Mode.SINGLE,
                items = items,
                selectedValues =
                    emptyList()
            )

        fun multiIntent(
            activity: Activity,
            title: String,
            subtitle: String?,
            helpText: String?,
            items:
                List<Pair<String, String>>,
            selectedValues:
                Collection<String>
        ): Intent =
            baseIntent(
                activity = activity,
                title = title,
                subtitle = subtitle,
                helpText = helpText,
                mode = Mode.MULTI,
                items = items,
                selectedValues =
                    selectedValues
            )

        private fun baseIntent(
            activity: Activity,
            title: String,
            subtitle: String?,
            helpText: String?,
            mode: Mode,
            items:
                List<Pair<String, String>>,
            selectedValues:
                Collection<String>
        ): Intent =
            Intent(
                activity,
                ListSelectorActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_TITLE,
                    title
                )
                putExtra(
                    EXTRA_SUBTITLE,
                    subtitle
                )
                putExtra(
                    EXTRA_HELP_TEXT,
                    helpText
                )
                putExtra(
                    EXTRA_MODE,
                    mode.name
                )
                putExtra(
                    EXTRA_ITEMS_JSON,
                    JSONArray().apply {
                        items.forEach {
                                item ->

                            put(
                                JSONObject()
                                    .put(
                                        "label",
                                        item.first
                                    )
                                    .put(
                                        "value",
                                        item.second
                                    )
                            )
                        }
                    }.toString()
                )
                putExtra(
                    EXTRA_SELECTED_JSON,
                    JSONArray().apply {
                        selectedValues.forEach {
                            put(it)
                        }
                    }.toString()
                )
            }
    }
}
