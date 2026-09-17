package com.saney.ytmimporter
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

class DestinationActivity : Activity() {
    private var currentMode: String = MODE_START

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        currentMode =
            intent.getStringExtra(EXTRA_MODE)
                ?: MODE_START

        when (currentMode) {
            MODE_EXISTING_LIST ->
                showExistingListScreen()

            MODE_EXISTING_CONFIRM ->
                showExistingConfirmScreen()

            MODE_EXISTING_SCAN_FAILED ->
                showExistingScanFailedScreen()

            else ->
                showStartScreen()
        }
    }

    private fun showStartScreen() {
        val root = baseRoot()
        root.addView(
            topBar(
                title = "Створити / додати",
                onBack = { finish() }
            )
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }

        content.addView(workspaceSummaryCard())

        content.addView(sectionTitle("Новий плейлист"))

        val newCard = card()
        newCard.addView(
            TextView(this).apply {
                text = "Створити новий playlist у YouTube / YTM"
                textSize = 16f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }
        )
        newCard.addView(
            infoText(
                "Виберіть приватність. Назва нового плейлиста буде взята " +
                    "з поточного Project / імпортованого списку."
            )
        )

        val privacyGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        val privateButton = privacyRadio(
            "🔒 Приватний — тільки ви",
            "private"
        )
        val unlistedButton = privacyRadio(
            "🔗 За посиланням — бачать ті, хто має посилання",
            "unlisted"
        )
        val publicButton = privacyRadio(
            "🌍 Публічний — видно всім",
            "public"
        )

        privacyGroup.addView(privateButton)
        privacyGroup.addView(unlistedButton)
        privacyGroup.addView(publicButton)
        privacyGroup.check(privateButton.id)
        newCard.addView(privacyGroup)

        newCard.addView(
            quotaText(
                intent.getStringExtra(EXTRA_NEW_QUOTA_PLAN)
                    .orEmpty()
            )
        )

        newCard.addView(
            actionButton(
                label = "Створити новий плейлист",
                primary = true
            ) {
                val checked =
                    privacyGroup.findViewById<RadioButton>(
                        privacyGroup.checkedRadioButtonId
                    )

                val privacy =
                    checked?.tag?.toString()
                        ?: "private"

                finishWith(
                    Intent()
                        .putExtra(
                            EXTRA_ACTION,
                            ACTION_CREATE_NEW
                        )
                        .putExtra(
                            EXTRA_PRIVACY,
                            privacy
                        )
                )
            }
        )

        content.addView(newCard)

        content.addView(sectionTitle("Існуючий плейлист"))

        val existingCard = card()
        existingCard.addView(
            TextView(this).apply {
                text = "Додати треки до вже існуючого плейлиста"
                textSize = 16f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }
        )
        existingCard.addView(
            infoText(
                "Список ваших плейлистів буде завантажено лише після натискання " +
                    "кнопки нижче. Потім YTM Importer перевірить дублікати за точним videoId."
            )
        )
        existingCard.addView(
            actionButton(
                label = "Вибрати існуючий плейлист",
                primary = false
            ) {
                finishWith(
                    Intent()
                        .putExtra(
                            EXTRA_ACTION,
                            ACTION_LOAD_EXISTING
                        )
                )
            }
        )
        content.addView(existingCard)

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    private fun showExistingListScreen() {
        val root = baseRoot()
        root.addView(
            topBar(
                title = "Існуючий плейлист",
                onBack = { backToStart() }
            )
        )

        root.addView(
            TextView(this).apply {
                text =
                    "Оберіть playlist, до якого потрібно додати поточні треки."
                textSize = 13f
                setTextColor(MUTED)
                setPadding(dp(18), 0, dp(18), dp(10))
            }
        )

        val ids =
            intent.getStringArrayListExtra(EXTRA_EXISTING_IDS)
                ?: arrayListOf()
        val titles =
            intent.getStringArrayListExtra(EXTRA_EXISTING_TITLES)
                ?: arrayListOf()
        val privacy =
            intent.getStringArrayListExtra(EXTRA_EXISTING_PRIVACY)
                ?: arrayListOf()
        val counts =
            intent.getLongArrayExtra(EXTRA_EXISTING_COUNTS)
                ?: LongArray(0)

        val items =
            ids.indices.mapNotNull { index ->
                val id = ids.getOrNull(index)
                    ?: return@mapNotNull null
                val title = titles.getOrNull(index)
                    ?: "Без назви"
                ExistingItem(
                    id = id,
                    title = title,
                    privacy =
                        privacy.getOrNull(index)
                            ?: "private",
                    itemCount =
                        counts.getOrNull(index)
                            ?: 0L
                )
            }

        if (items.isEmpty()) {
            root.addView(
                TextView(this).apply {
                    text =
                        "У цьому YouTube / YTM профілі немає доступних плейлистів."
                    gravity = Gravity.CENTER
                    textSize = 15f
                    setTextColor(MUTED)
                    setPadding(dp(24), dp(40), dp(24), dp(40))
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
            setContentView(root)
        UiChrome.applyScreenInsets(this, root)
            return
        }

        val search = EditText(this).apply {
            hint = "Пошук плейлиста за назвою"
            setSingleLine(true)
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(120, 123, 130))
            setPadding(dp(14), 0, dp(14), 0)
            background = roundedBackground(
                color = SURFACE,
                radiusDp = 12,
                strokeColor = BORDER
            )
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                setMargins(dp(12), 0, dp(12), dp(10))
            }
        )

        val countText = TextView(this).apply {
            textSize = 12f
            setTextColor(MUTED)
            setPadding(dp(18), 0, dp(18), dp(6))
        }
        root.addView(countText)

        val list = ListView(this).apply {
            divider = null
            dividerHeight = dp(6)
            clipToPadding = false
            setPadding(dp(10), 0, dp(10), dp(14))
            setBackgroundColor(AppThemeManager.palette(this@DestinationActivity).background)
        }
        root.addView(
            list,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val visible = items.toMutableList()
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            visible.map(::existingItemLabel).toMutableList()
        )
        list.adapter = adapter
        countText.text = "${visible.size} плейлистів"

        fun applyFilter(query: String) {
            val normalized = query.trim().lowercase()
            visible.clear()
            visible.addAll(
                if (normalized.isBlank()) {
                    items
                } else {
                    items.filter {
                        it.title.lowercase().contains(normalized)
                    }
                }
            )

            adapter.clear()
            adapter.addAll(visible.map(::existingItemLabel))
            adapter.notifyDataSetChanged()
            countText.text =
                if (normalized.isBlank()) {
                    "${visible.size} плейлистів"
                } else {
                    "Знайдено: ${visible.size}"
                }
        }

        search.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    applyFilter(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: Editable?) = Unit
            }
        )

        list.setOnItemClickListener { _, _, position, _ ->
            val item = visible.getOrNull(position)
                ?: return@setOnItemClickListener

            finishWith(
                Intent()
                    .putExtra(EXTRA_ACTION, ACTION_SELECT_EXISTING)
                    .putExtra(EXTRA_TARGET_ID, item.id)
                    .putExtra(EXTRA_TARGET_TITLE, item.title)
                    .putExtra(EXTRA_TARGET_PRIVACY, item.privacy)
                    .putExtra(EXTRA_TARGET_COUNT, item.itemCount)
            )
        }

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    private fun showExistingConfirmScreen() {
        val root = baseRoot()
        root.addView(
            topBar(
                title = "Перевірка перед додаванням",
                onBack = { backToExistingList() }
            )
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }

        content.addView(workspaceSummaryCard())

        val targetTitle =
            intent.getStringExtra(EXTRA_TARGET_TITLE)
                ?: "Плейлист"
        val targetPrivacy =
            intent.getStringExtra(EXTRA_TARGET_PRIVACY)
                ?: "private"
        val targetCount =
            intent.getLongExtra(EXTRA_TARGET_COUNT, 0L)
        val already =
            intent.getIntExtra(EXTRA_ALREADY_COUNT, 0)
        val repeated =
            intent.getIntExtra(EXTRA_REPEATED_COUNT, 0)
        val newCount =
            intent.getIntExtra(EXTRA_NEW_COUNT, 0)
        val selectedCount =
            intent.getIntExtra(EXTRA_SELECTED_COUNT, 0)
        val scanRequests =
            intent.getIntExtra(EXTRA_SCAN_REQUESTS, 0)
        val duplicates = already + repeated

        content.addView(sectionTitle("Цільовий плейлист"))
        content.addView(
            card().apply {
                addView(
                    TextView(this@DestinationActivity).apply {
                        text = targetTitle
                        textSize = 17f
                        setTextColor(Color.WHITE)
                        setTypeface(typeface, Typeface.BOLD)
                    }
                )
                addView(
                    infoText(
                        "$targetCount треків • ${privacyLabel(targetPrivacy)}"
                    )
                )
            }
        )

        content.addView(sectionTitle("Дублікати"))
        content.addView(
            card().apply {
                addView(statLine("Вибрано для запису", selectedCount.toString()))
                addView(statLine("Уже є у playlist", already.toString()))
                addView(statLine("Повтори в імпорті", repeated.toString()))
                addView(statLine("Нових треків", newCount.toString()))
                addView(statLine("playlistItems.list", "$scanRequests request(s)"))
                addView(
                    infoText(
                        "Порівняння виконується за точним YouTube videoId, " +
                            "а не за назвою треку."
                    )
                )
            }
        )

        if (duplicates > 0) {
            content.addView(sectionTitle("Виберіть режим"))

            content.addView(
                card().apply {
                    addView(
                        TextView(this@DestinationActivity).apply {
                            text = "Пропустити дублікати"
                            textSize = 16f
                            setTextColor(Color.WHITE)
                            setTypeface(typeface, Typeface.BOLD)
                        }
                    )
                    addView(
                        infoText(
                            "Буде записано $newCount нових треків. " +
                                "Дублікати залишаться позначені локально як пропущені."
                        )
                    )
                    addView(
                        quotaText(
                            intent.getStringExtra(EXTRA_QUOTA_SKIP)
                                .orEmpty()
                        )
                    )
                    addView(
                        actionButton(
                            label = "Пропустити дублікати й додати",
                            primary = true
                        ) {
                            finishExistingConfirm(DUPLICATE_MODE_SKIP)
                        }
                    )
                }
            )

            content.addView(
                card().apply {
                    addView(
                        TextView(this@DestinationActivity).apply {
                            text = "Додати все одно"
                            textSize = 16f
                            setTextColor(Color.WHITE)
                            setTypeface(typeface, Typeface.BOLD)
                        }
                    )
                    addView(
                        infoText(
                            "Буде зроблена спроба записати всі $selectedCount треків, " +
                                "включно з уже наявними або повтореними videoId."
                        )
                    )
                    addView(
                        quotaText(
                            intent.getStringExtra(EXTRA_QUOTA_ALL)
                                .orEmpty()
                        )
                    )
                    addView(
                        actionButton(
                            label = "Додати все одно",
                            primary = false
                        ) {
                            finishExistingConfirm(DUPLICATE_MODE_ALL)
                        }
                    )
                }
            )
        } else {
            content.addView(sectionTitle("Підтвердження"))
            content.addView(
                card().apply {
                    addView(
                        infoText(
                            "Дублікатів не знайдено. Буде додано $selectedCount треків."
                        )
                    )
                    addView(
                        quotaText(
                            intent.getStringExtra(EXTRA_QUOTA_ALL)
                                .orEmpty()
                        )
                    )
                    addView(
                        actionButton(
                            label = "Додати до плейлиста",
                            primary = true
                        ) {
                            finishExistingConfirm(DUPLICATE_MODE_SKIP)
                        }
                    )
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
        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    private fun showExistingScanFailedScreen() {
        val root = baseRoot()
        root.addView(
            topBar(
                title = "Не вдалося перевірити дублікати",
                onBack = { backToExistingList() }
            )
        )

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }

        content.addView(workspaceSummaryCard())
        content.addView(sectionTitle("Цільовий плейлист"))
        content.addView(
            card().apply {
                addView(
                    TextView(this@DestinationActivity).apply {
                        text =
                            intent.getStringExtra(EXTRA_TARGET_TITLE)
                                ?: "Плейлист"
                        textSize = 17f
                        setTextColor(Color.WHITE)
                        setTypeface(typeface, Typeface.BOLD)
                    }
                )
                addView(
                    infoText(
                        "Перевірка duplicate videoId не завершилась."
                    )
                )
            }
        )

        content.addView(
            TextView(this).apply {
                text =
                    intent.getStringExtra(EXTRA_SCAN_ERROR)
                        ?: "Невідома помилка"
                textSize = 13f
                setTextColor(Color.rgb(255, 150, 150))
                setPadding(dp(14), dp(14), dp(14), dp(14))
                setTextIsSelectable(true)
                background = roundedBackground(
                    color = Color.rgb(39, 25, 27),
                    radiusDp = 14,
                    strokeColor = Color.rgb(95, 48, 52)
                )
            }
        )

        content.addView(
            card().apply {
                addView(
                    infoText(
                        "Можна скасувати або продовжити без перевірки. " +
                            "У другому випадку можливі повтори."
                    )
                )
                addView(
                    quotaText(
                        intent.getStringExtra(EXTRA_QUOTA_ALL)
                            .orEmpty()
                    )
                )
                addView(
                    actionButton(
                        label = "Продовжити без перевірки",
                        primary = true
                    ) {
                        finishExistingConfirm(DUPLICATE_MODE_NO_SCAN)
                    }
                )
            }
        )

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (currentMode) {
            MODE_EXISTING_LIST ->
                backToStart()

            MODE_EXISTING_CONFIRM,
            MODE_EXISTING_SCAN_FAILED ->
                backToExistingList()

            else ->
                super.onBackPressed()
        }
    }

    private fun backToStart() {
        finishWith(
            Intent()
                .putExtra(
                    EXTRA_ACTION,
                    ACTION_BACK_TO_START
                )
        )
    }

    private fun backToExistingList() {
        finishWith(
            Intent()
                .putExtra(
                    EXTRA_ACTION,
                    ACTION_BACK_TO_EXISTING_LIST
                )
        )
    }

    private fun finishExistingConfirm(mode: String) {
        finishWith(
            Intent()
                .putExtra(
                    EXTRA_ACTION,
                    ACTION_CONFIRM_EXISTING
                )
                .putExtra(
                    EXTRA_DUPLICATE_MODE,
                    mode
                )
        )
    }

    private fun finishWith(data: Intent) {
        setResult(RESULT_OK, data)
        finish()
    }

    private fun workspaceSummaryCard(): LinearLayout {
        val playlistName =
            intent.getStringExtra(EXTRA_PLAYLIST_NAME)
                ?: "Поточний список"
        val importedCount =
            intent.getIntExtra(EXTRA_IMPORTED_COUNT, 0)
        val selectedCount =
            intent.getIntExtra(EXTRA_SELECTED_COUNT, 0)
        val questionableCount =
            intent.getIntExtra(EXTRA_QUESTIONABLE_COUNT, 0)
        val googleLabel =
            intent.getStringExtra(EXTRA_GOOGLE_LABEL)
                ?: "буде перевірено перед записом"
        val channelLabel =
            intent.getStringExtra(EXTRA_CHANNEL_LABEL)
                ?: "буде перевірено перед записом"

        return card().apply {
            addView(
                TextView(this@DestinationActivity).apply {
                    text = playlistName
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                }
            )
            addView(
                infoText(
                    "В імпорті: $importedCount\n" +
                        "Готово до запису: $selectedCount\n" +
                        "Потребують перевірки: $questionableCount\n\n" +
                        "Google: $googleLabel\n" +
                        "YouTube/YTM: $channelLabel"
                )
            )

            if (questionableCount > 0) {
                addView(
                    TextView(this@DestinationActivity).apply {
                        text =
                            "⚠ Є $questionableCount неперевірених треків. " +
                                "Їх можна записати, але краще повернутися в Review."
                        textSize = 12.5f
                        setTextColor(Color.rgb(255, 195, 80))
                        setPadding(0, dp(6), 0, 0)
                    }
                )
            }
        }
    }

    private fun existingItemLabel(item: ExistingItem): String =
        buildString {
            append(item.title)
            append("\n")
            append(item.itemCount)
            append(" треків • ")
            append(privacyLabel(item.privacy))
        }

    private fun privacyLabel(value: String): String =
        when (value) {
            "public" -> "Публічний"
            "unlisted" -> "За посиланням"
            else -> "Приватний"
        }

    private fun privacyRadio(
        label: String,
        value: String
    ): RadioButton =
        RadioButton(this).apply {
            id = View.generateViewId()
            text = label
            tag = value
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(0, dp(4), 0, dp(4))
        }

    private fun baseRoot(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppThemeManager.palette(this@DestinationActivity).background)
        }

    private fun topBar(
        title: String,
        onBack: () -> Unit
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))

            addView(
                Button(this@DestinationActivity).apply {
                    text = "‹"
                    isAllCaps = false
                    textSize = 26f
                    setTextColor(Color.WHITE)
                    background = roundedBackground(
                        color = SURFACE,
                        radiusDp = 12,
                        strokeColor = BORDER
                    )
                    setOnClickListener { onBack() }
                },
                LinearLayout.LayoutParams(dp(46), dp(46))
            )

            addView(
                TextView(this@DestinationActivity).apply {
                    text = title
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(12), 0, 0, 0)
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }

    private fun card(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBackground(
                color = SURFACE,
                radiusDp = 14,
                strokeColor = BORDER
            )
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
        }

    private fun sectionTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(MUTED)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(4), dp(10), 0, dp(6))
        }

    private fun infoText(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.rgb(202, 204, 210))
            setPadding(0, dp(7), 0, dp(8))
            setTextIsSelectable(true)
        }

    private fun quotaText(text: String): TextView =
        TextView(this).apply {
            this.text =
                if (text.isBlank()) {
                    "Quota estimate unavailable"
                } else {
                    text
                }
            textSize = 12.5f
            setTextColor(MUTED)
            setPadding(0, dp(8), 0, dp(10))
        }

    private fun statLine(
        label: String,
        value: String
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(3), 0, dp(3))

            addView(
                TextView(this@DestinationActivity).apply {
                    text = label
                    textSize = 13.5f
                    setTextColor(MUTED)
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@DestinationActivity).apply {
                    text = value
                    textSize = 14f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                }
            )
        }

    private fun actionButton(
        label: String,
        primary: Boolean,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(16), dp(7), dp(16), dp(7))
            background = roundedBackground(
                color =
                    if (primary) {
                        Color.rgb(196, 0, 42)
                    } else {
                        Color.rgb(37, 39, 46)
                    },
                radiusDp = 11,
                strokeColor =
                    if (primary) {
                        null
                    } else {
                        Color.rgb(63, 66, 76)
                    }
            )
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                bottomMargin = dp(7)
            }
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): android.graphics.drawable.Drawable {
        val palette =
            AppThemeManager.palette(this)

        val mappedFill =
            when (color) {
                Color.rgb(15, 16, 19) ->
                    palette.background

                Color.rgb(25, 27, 32) ->
                    palette.surface

                Color.rgb(31, 33, 39),
                Color.rgb(37, 39, 46) ->
                    palette.surfaceAlt

                Color.rgb(196, 0, 42) ->
                    palette.accentFill

                Color.rgb(39, 25, 27),
                Color.rgb(31, 29, 24) ->
                    palette.surface

                else ->
                    color
            }

        val accentOverride =
            when (strokeColor) {
                Color.rgb(95, 48, 52) ->
                    palette.danger

                Color.rgb(83, 68, 37) ->
                    palette.warning

                else ->
                    null
            }

        val useAccentStroke =
            color == Color.rgb(196, 0, 42) ||
                accentOverride != null

        return AppThemeManager.surfaceDrawable(
            context = this,
            fill = mappedFill,
            radiusDp = radiusDp,
            accentStroke = useAccentStroke,
            accentOverride = accentOverride
        )
    }

    private fun dp(value: Int): Int =
        (
            value *
                resources.displayMetrics.density
        ).toInt()

    private data class ExistingItem(
        val id: String,
        val title: String,
        val privacy: String,
        val itemCount: Long
    )

    companion object {
        const val EXTRA_MODE = "destination_mode"
        const val EXTRA_ACTION = "destination_action"
        const val EXTRA_PRIVACY = "destination_privacy"
        const val EXTRA_PLAYLIST_NAME = "destination_playlist_name"
        const val EXTRA_IMPORTED_COUNT = "destination_imported_count"
        const val EXTRA_SELECTED_COUNT = "destination_selected_count"
        const val EXTRA_QUESTIONABLE_COUNT = "destination_questionable_count"
        const val EXTRA_GOOGLE_LABEL = "destination_google_label"
        const val EXTRA_CHANNEL_LABEL = "destination_channel_label"
        const val EXTRA_NEW_QUOTA_PLAN = "destination_new_quota_plan"

        const val EXTRA_EXISTING_IDS = "destination_existing_ids"
        const val EXTRA_EXISTING_TITLES = "destination_existing_titles"
        const val EXTRA_EXISTING_PRIVACY = "destination_existing_privacy"
        const val EXTRA_EXISTING_COUNTS = "destination_existing_counts"

        const val EXTRA_TARGET_ID = "destination_target_id"
        const val EXTRA_TARGET_TITLE = "destination_target_title"
        const val EXTRA_TARGET_PRIVACY = "destination_target_privacy"
        const val EXTRA_TARGET_COUNT = "destination_target_count"

        const val EXTRA_ALREADY_COUNT = "destination_already_count"
        const val EXTRA_REPEATED_COUNT = "destination_repeated_count"
        const val EXTRA_NEW_COUNT = "destination_new_count"
        const val EXTRA_SCAN_REQUESTS = "destination_scan_requests"
        const val EXTRA_SCAN_ERROR = "destination_scan_error"
        const val EXTRA_QUOTA_SKIP = "destination_quota_skip"
        const val EXTRA_QUOTA_ALL = "destination_quota_all"
        const val EXTRA_DUPLICATE_MODE = "destination_duplicate_mode"

        const val MODE_START = "start"
        const val MODE_EXISTING_LIST = "existing_list"
        const val MODE_EXISTING_CONFIRM = "existing_confirm"
        const val MODE_EXISTING_SCAN_FAILED = "existing_scan_failed"

        const val ACTION_CREATE_NEW = "create_new"
        const val ACTION_LOAD_EXISTING = "load_existing"
        const val ACTION_SELECT_EXISTING = "select_existing"
        const val ACTION_CONFIRM_EXISTING = "confirm_existing"
        const val ACTION_BACK_TO_START = "back_to_start"
        const val ACTION_BACK_TO_EXISTING_LIST = "back_to_existing_list"

        const val DUPLICATE_MODE_SKIP = "skip"
        const val DUPLICATE_MODE_ALL = "all"
        const val DUPLICATE_MODE_NO_SCAN = "no_scan"

        private val BACKGROUND = Color.rgb(15, 16, 19)
        private val SURFACE = Color.rgb(25, 27, 32)
        private val BORDER = Color.rgb(48, 51, 59)
        private val MUTED = Color.rgb(165, 167, 173)
    }
}
