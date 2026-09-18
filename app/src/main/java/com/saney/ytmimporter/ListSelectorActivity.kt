package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

class ListSelectorActivity : Activity() {
    private lateinit var mode: Mode
    private var titleText: String = "Вибір"
    private var subtitleText: String = ""
    private var helpText: String = ""

    private var itemIds: List<String> =
        emptyList()
    private var itemTitles: List<String> =
        emptyList()
    private var itemDetails: List<String> =
        emptyList()

    private val selectedIds =
        linkedSetOf<String>()

    private lateinit var selectionSummary:
        TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        mode =
            runCatching {
                Mode.valueOf(
                    intent.getStringExtra(
                        EXTRA_MODE
                    )
                        ?: Mode.SINGLE.name
                )
            }.getOrDefault(
                Mode.SINGLE
            )

        titleText =
            intent.getStringExtra(
                EXTRA_TITLE
            )
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Вибір"

        subtitleText =
            intent.getStringExtra(
                EXTRA_SUBTITLE
            )
                ?.trim()
                .orEmpty()

        helpText =
            intent.getStringExtra(
                EXTRA_HELP
            )
                ?.trim()
                .orEmpty()

        itemIds =
            intent.getStringArrayListExtra(
                EXTRA_ITEM_IDS
            )
                ?.toList()
                .orEmpty()

        itemTitles =
            intent.getStringArrayListExtra(
                EXTRA_ITEM_TITLES
            )
                ?.toList()
                .orEmpty()

        itemDetails =
            intent.getStringArrayListExtra(
                EXTRA_ITEM_DETAILS
            )
                ?.toList()
                .orEmpty()

        selectedIds +=
            intent.getStringArrayListExtra(
                EXTRA_SELECTED_IDS
            )
                .orEmpty()

        if (
            itemIds.size !=
                itemTitles.size ||
            itemIds.size !=
                itemDetails.size
        ) {
            Toast.makeText(
                this,
                "Не вдалося відкрити список: дані пошкоджено",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        render()
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
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }

        root.addView(
            topBar()
        )

        if (subtitleText.isNotBlank()) {
            root.addView(
                TextView(this).apply {
                    text =
                        subtitleText
                    textSize = 13f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(18),
                        0,
                        dp(18),
                        dp(10)
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

        itemIds.indices.forEach {
                index ->

            val id =
                itemIds[index]

            content.addView(
                if (
                    mode ==
                        Mode.MULTI
                ) {
                    multiItem(
                        id = id,
                        title =
                            itemTitles[index],
                        detail =
                            itemDetails[index]
                    )
                } else {
                    singleItem(
                        id = id,
                        title =
                            itemTitles[index],
                        detail =
                            itemDetails[index]
                    )
                },
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
                    text =
                        titleText
                    textSize = 20f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
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

            if (helpText.isNotBlank()) {
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

    private fun singleItem(
        id: String,
        title: String,
        detail: String
    ): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text =
                buildString {
                    append(title)

                    if (
                        detail.isNotBlank()
                    ) {
                        append("\n")
                        append(detail)
                    }
                }
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
                finishWithSelection(
                    listOf(id)
                )
            }
        }
    }

    private fun multiItem(
        id: String,
        title: String,
        detail: String
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                dp(8),
                dp(8),
                dp(12),
                dp(8)
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

            addView(
                CheckBox(
                    this@ListSelectorActivity
                ).apply {
                    isChecked =
                        id in selectedIds
                    setOnCheckedChangeListener {
                            _,
                            checked ->

                        if (checked) {
                            selectedIds +=
                                id
                        } else {
                            selectedIds -=
                                id
                        }

                        updateSelectionSummary()
                    }
                },
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                TextView(
                    this@ListSelectorActivity
                ).apply {
                    text =
                        buildString {
                            append(title)

                            if (
                                detail.isNotBlank()
                            ) {
                                append("\n")
                                append(detail)
                            }
                        }
                    textSize = 15f
                    setTextColor(
                        palette.text
                    )
                    setLineSpacing(
                        0f,
                        1.05f
                    )
                    setPadding(
                        dp(6),
                        dp(6),
                        0,
                        dp(6)
                    )
                    setOnClickListener {
                        val checkBox =
                            (
                                parent as
                                    LinearLayout
                            )
                                .getChildAt(
                                    0
                                ) as
                                CheckBox

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
        }
    }

    private fun footer():
        LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(10)
                )
            }

        selectionSummary =
            TextView(this).apply {
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

        if (
            mode ==
                Mode.MULTI
        ) {
            root.addView(
                selectionSummary
            )
            updateSelectionSummary()

            root.addView(
                footerButton(
                    label = "Далі",
                    primary = true
                ) {
                    if (
                        selectedIds.isEmpty()
                    ) {
                        Toast.makeText(
                            this,
                            "Виберіть хоча б один пункт",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        finishWithSelection(
                            selectedIds
                                .toList()
                        )
                    }
                }
            )
        }

        root.addView(
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
                if (
                    mode ==
                        Mode.MULTI
                ) {
                    topMargin =
                        dp(8)
                }
            }
        )

        return root
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
            maxLines = 1
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
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                )
        }
    }

    private fun updateSelectionSummary() {
        if (
            ::selectionSummary.isInitialized
        ) {
            selectionSummary.text =
                "Вибрано: " +
                    selectedIds.size
        }
    }

    private fun showHelp() {
        UiChrome.showMessageDialog(
            activity = this,
            title =
                "Про цей список",
            message =
                helpText,
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label =
                            "Зрозуміло",
                        tone =
                            UiChrome.ActionTone.ACCENT
                    ) {}
                )
        )
    }

    private fun finishWithSelection(
        ids: List<String>
    ) {
        setResult(
            RESULT_OK,
            Intent().putStringArrayListExtra(
                EXTRA_RESULT_IDS,
                ArrayList(ids)
            )
        )
        finish()
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    private enum class Mode {
        SINGLE,
        MULTI
    }

    companion object {
        const val EXTRA_RESULT_IDS =
            "selector_result_ids"

        private const val EXTRA_TITLE =
            "selector_title"
        private const val EXTRA_SUBTITLE =
            "selector_subtitle"
        private const val EXTRA_HELP =
            "selector_help"
        private const val EXTRA_MODE =
            "selector_mode"
        private const val EXTRA_ITEM_IDS =
            "selector_item_ids"
        private const val EXTRA_ITEM_TITLES =
            "selector_item_titles"
        private const val EXTRA_ITEM_DETAILS =
            "selector_item_details"
        private const val EXTRA_SELECTED_IDS =
            "selector_selected_ids"

        fun singleIntent(
            activity: Activity,
            title: String,
            subtitle: String,
            help: String,
            ids: List<String>,
            titles: List<String>,
            details: List<String>
        ): Intent =
            baseIntent(
                activity = activity,
                title = title,
                subtitle = subtitle,
                help = help,
                mode = Mode.SINGLE,
                ids = ids,
                titles = titles,
                details = details,
                selectedIds =
                    emptyList()
            )

        fun multiIntent(
            activity: Activity,
            title: String,
            subtitle: String,
            help: String,
            ids: List<String>,
            titles: List<String>,
            details: List<String>,
            selectedIds: List<String>
        ): Intent =
            baseIntent(
                activity = activity,
                title = title,
                subtitle = subtitle,
                help = help,
                mode = Mode.MULTI,
                ids = ids,
                titles = titles,
                details = details,
                selectedIds =
                    selectedIds
            )

        private fun baseIntent(
            activity: Activity,
            title: String,
            subtitle: String,
            help: String,
            mode: Mode,
            ids: List<String>,
            titles: List<String>,
            details: List<String>,
            selectedIds: List<String>
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
                    EXTRA_HELP,
                    help
                )
                putExtra(
                    EXTRA_MODE,
                    mode.name
                )
                putStringArrayListExtra(
                    EXTRA_ITEM_IDS,
                    ArrayList(ids)
                )
                putStringArrayListExtra(
                    EXTRA_ITEM_TITLES,
                    ArrayList(titles)
                )
                putStringArrayListExtra(
                    EXTRA_ITEM_DETAILS,
                    ArrayList(details)
                )
                putStringArrayListExtra(
                    EXTRA_SELECTED_IDS,
                    ArrayList(
                        selectedIds
                    )
                )
            }
    }
}
