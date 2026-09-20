package com.saney.ytmimporter
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.destination.DestinationRemoteOperations
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

import android.app.Activity
import android.app.Dialog
import android.content.Context
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
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class DestinationActivity : Activity() {
    private var currentMode: String = MODE_START

    private var remoteProgressDialog:
        Dialog? = null

    private var remoteProgressText:
        TextView? = null

    private val remoteListener:
        (DestinationRemoteOperations.State) -> Unit =
        { state ->
            handleRemoteState(state)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        currentMode =
            savedInstanceState
                ?.getString(
                    STATE_CURRENT_MODE
                )
                ?: intent.getStringExtra(EXTRA_MODE)
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

    override fun onStart() {
        super.onStart()
        DestinationRemoteOperations
            .addListener(
                remoteListener
            )
    }

    override fun onStop() {
        DestinationRemoteOperations
            .removeListener(
                remoteListener
            )
        super.onStop()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putString(
            STATE_CURRENT_MODE,
            currentMode
        )
        super.onSaveInstanceState(
            outState
        )
    }

    override fun onDestroy() {
        remoteProgressDialog
            ?.setOnDismissListener(null)
        remoteProgressDialog = null
        remoteProgressText = null
        super.onDestroy()
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
                openExistingPlaylists()
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

            requestDuplicateScan(
                item
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
        setMode(
            MODE_START
        )
        showStartScreen()
    }

    private fun backToExistingList() {
        openExistingPlaylists()
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
                .putExtra(
                    EXTRA_TARGET_ID,
                    intent.getStringExtra(
                        EXTRA_TARGET_ID
                    )
                )
                .putExtra(
                    EXTRA_TARGET_TITLE,
                    intent.getStringExtra(
                        EXTRA_TARGET_TITLE
                    )
                )
                .putExtra(
                    EXTRA_TARGET_PRIVACY,
                    intent.getStringExtra(
                        EXTRA_TARGET_PRIVACY
                    )
                )
                .putExtra(
                    EXTRA_TARGET_COUNT,
                    intent.getLongExtra(
                        EXTRA_TARGET_COUNT,
                        0L
                    )
                )
                .putExtra(
                    EXTRA_LOCAL_SKIP_POSITIONS,
                    intent.getIntArrayExtra(
                        EXTRA_LOCAL_SKIP_POSITIONS
                    ) ?: IntArray(0)
                )
        )
    }

    private fun finishWith(data: Intent) {
        setResult(RESULT_OK, data)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun openExistingPlaylists() {
        if (
            intent.hasExtra(
                EXTRA_EXISTING_IDS
            )
        ) {
            setMode(
                MODE_EXISTING_LIST
            )
            showExistingListScreen()
        } else {
            requestExistingPlaylists()
        }
    }

    private fun requestExistingPlaylists() {
        DestinationRemoteOperations
            .startLoad(
                this
            )
    }

    private fun requestDuplicateScan(
        item: ExistingItem
    ) {
        DestinationRemoteOperations
            .startScan(
                context = this,
                target =
                    YouTubePlaylistInfo(
                        id = item.id,
                        title = item.title,
                        privacyStatus =
                            item.privacy,
                        itemCount =
                            item.itemCount
                    )
            )
    }

    private fun handleRemoteState(
        state:
            DestinationRemoteOperations.State
    ) {
        if (isFinishing || isDestroyed) {
            return
        }

        if (state.running) {
            showRemoteProgress(
                state
            )
            return
        }

        remoteProgressDialog
            ?.setOnDismissListener(null)
        remoteProgressDialog
            ?.dismiss()
        remoteProgressDialog = null
        remoteProgressText = null

        if (state.terminalSerial == 0L) {
            return
        }

        DestinationRemoteOperations
            .acknowledgeTerminal(
                state.terminalSerial
            )

        when {
            state.playlists != null -> {
                val playlists =
                    state.playlists

                if (playlists.isEmpty()) {
                    toast(
                        "У цьому YouTube/YTM профілі немає доступних плейлистів."
                    )
                    setMode(
                        MODE_START
                    )
                    showStartScreen()
                } else {
                    storeExistingPlaylists(
                        playlists
                    )
                    setMode(
                        MODE_EXISTING_LIST
                    )
                    showExistingListScreen()
                }
            }

            state.scan != null -> {
                storeScan(
                    state.scan
                )
                setMode(
                    MODE_EXISTING_CONFIRM
                )
                showExistingConfirmScreen()
            }

            state.errorMessage != null -> {
                if (
                    state.authorizationInvalidated
                ) {
                    toast(
                        "Авторизацію Google / YTM потрібно відновити на головному екрані"
                    )
                    finish()
                    return
                }

                val target =
                    state.target

                if (
                    state.kind ==
                        DestinationRemoteOperations
                            .Kind.SCAN_DUPLICATES &&
                    target != null
                ) {
                    storeTarget(
                        target
                    )
                    intent.putExtra(
                        EXTRA_SCAN_ERROR,
                        state.errorMessage
                    )
                    intent.putExtra(
                        EXTRA_QUOTA_ALL,
                        quotaPlanForWrite(
                            trackCount =
                                intent.getIntExtra(
                                    EXTRA_SELECTED_COUNT,
                                    0
                                ),
                            createPlaylist =
                                false
                        )
                    )
                    setMode(
                        MODE_EXISTING_SCAN_FAILED
                    )
                    showExistingScanFailedScreen()
                } else {
                    toast(
                        state.errorMessage
                    )
                    setMode(
                        MODE_START
                    )
                    showStartScreen()
                }
            }
        }
    }

    private fun showRemoteProgress(
        state:
            DestinationRemoteOperations.State
    ) {
        if (
            remoteProgressDialog
                ?.isShowing == true
        ) {
            remoteProgressText
                ?.text =
                state.message
            return
        }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(18),
                    dp(8),
                    dp(18),
                    dp(12)
                )
            }

        val message =
            TextView(this).apply {
                text =
                    state.message
                textSize =
                    14f
                setTextColor(
                    Color.WHITE
                )
                setPadding(
                    0,
                    0,
                    0,
                    dp(12)
                )
            }

        remoteProgressText =
            message

        content.addView(
            message
        )
        content.addView(
            ProgressBar(this).apply {
                isIndeterminate =
                    true
            }
        )

        remoteProgressDialog =
            UiChrome
                .alertBuilder(this)
                .setTitle(
                    if (
                        state.kind ==
                        DestinationRemoteOperations
                            .Kind.LOAD_PLAYLISTS
                    ) {
                        "Існуючий плейлист"
                    } else {
                        "Перевірка перед додаванням"
                    }
                )
                .setView(
                    content
                )
                .show()
                .also {
                    dialog ->
                    dialog.setCancelable(
                        false
                    )
                }
    }

    private fun storeExistingPlaylists(
        playlists:
            List<YouTubePlaylistInfo>
    ) {
        intent.putStringArrayListExtra(
            EXTRA_EXISTING_IDS,
            ArrayList(
                playlists.map {
                    it.id
                }
            )
        )
        intent.putStringArrayListExtra(
            EXTRA_EXISTING_TITLES,
            ArrayList(
                playlists.map {
                    it.title
                }
            )
        )
        intent.putStringArrayListExtra(
            EXTRA_EXISTING_PRIVACY,
            ArrayList(
                playlists.map {
                    it.privacyStatus
                }
            )
        )
        intent.putExtra(
            EXTRA_EXISTING_COUNTS,
            playlists
                .map {
                    it.itemCount
                }
                .toLongArray()
        )
    }

    private fun storeScan(
        scan:
            DestinationRemoteOperations.ScanPayload
    ) {
        storeTarget(
            scan.target
        )

        intent.putExtra(
            EXTRA_ALREADY_COUNT,
            scan.alreadyCount
        )
        intent.putExtra(
            EXTRA_REPEATED_COUNT,
            scan.repeatedCount
        )
        intent.putExtra(
            EXTRA_NEW_COUNT,
            scan.newCount
        )
        intent.putExtra(
            EXTRA_SCAN_REQUESTS,
            scan.requestCount
        )
        intent.putExtra(
            EXTRA_LOCAL_SKIP_POSITIONS,
            scan.skipPositions
        )
        intent.putExtra(
            EXTRA_QUOTA_SKIP,
            quotaPlanForWrite(
                trackCount =
                    scan.newCount,
                createPlaylist =
                    false
            )
        )
        intent.putExtra(
            EXTRA_QUOTA_ALL,
            quotaPlanForWrite(
                trackCount =
                    intent.getIntExtra(
                        EXTRA_SELECTED_COUNT,
                        0
                    ),
                createPlaylist =
                    false
            )
        )
    }

    private fun storeTarget(
        target:
            YouTubePlaylistInfo
    ) {
        intent.putExtra(
            EXTRA_TARGET_ID,
            target.id
        )
        intent.putExtra(
            EXTRA_TARGET_TITLE,
            target.title
        )
        intent.putExtra(
            EXTRA_TARGET_PRIVACY,
            target.privacyStatus
        )
        intent.putExtra(
            EXTRA_TARGET_COUNT,
            target.itemCount
        )
    }

    private fun setMode(
        mode: String
    ) {
        currentMode =
            mode
        intent.putExtra(
            EXTRA_MODE,
            mode
        )
    }

    private fun quotaPlanForWrite(
        trackCount: Int,
        createPlaylist: Boolean
    ): String {
        val required =
            trackCount *
                QuotaTracker
                    .PLAYLIST_ITEM_INSERT_COST +
                if (createPlaylist) {
                    QuotaTracker
                        .PLAYLIST_CREATE_COST
                } else {
                    0
                }

        val quota =
            QuotaTracker(this)
                .snapshot()

        return "Квота API (оцінка):\n" +
            "Потрібно приблизно: $required units (одиниць)\n" +
            "Локально залишилось приблизно: ${quota.generalRemaining}/" +
            "${QuotaTracker.GENERAL_DAILY_LIMIT}"
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
            buttonTintList =
                android.content.res.ColorStateList.valueOf(
                    AppThemeManager
                        .palette(this@DestinationActivity)
                        .accent
                )
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
                UiChrome.backButton(
                    activity = this@DestinationActivity,
                    onClick = { onBack() }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                UiChrome.emphasizedTitle(
                    activity = this@DestinationActivity,
                    label = title
                ).apply {
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
            radiusDp >= 14 ||
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

    private fun toast(
        message: String
    ) {
        Toast
            .makeText(
                this,
                message,
                Toast.LENGTH_LONG
            )
            .show()
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
        fun startIntent(
            context: Context,
            snapshot:
                CurrentPlaylistSnapshot
        ): Intent? {
            val selected =
                snapshot
                    .playlist
                    .tracks
                    .filter {
                        track ->
                        !track.selectedVideoId
                            .isNullOrBlank() &&
                            track.status !=
                                TrackStatus.SKIPPED
                    }

            if (selected.isEmpty()) {
                return null
            }

            val questionable =
                selected.count {
                    it.status ==
                        TrackStatus.REVIEW
                }

            val auth =
                AuthSessionStore.current()

            val quota =
                QuotaTracker(context)
                    .snapshot()

            val required =
                selected.size *
                    QuotaTracker
                        .PLAYLIST_ITEM_INSERT_COST +
                    QuotaTracker
                        .PLAYLIST_CREATE_COST

            val quotaText =
                "Квота API (оцінка):\n" +
                    "Потрібно приблизно: $required units (одиниць)\n" +
                    "Локально залишилось приблизно: ${quota.generalRemaining}/" +
                    "${QuotaTracker.GENERAL_DAILY_LIMIT}"

            return Intent(
                context,
                DestinationActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_MODE,
                    MODE_START
                )
                putExtra(
                    EXTRA_PLAYLIST_NAME,
                    snapshot.playlist.name
                )
                putExtra(
                    EXTRA_IMPORTED_COUNT,
                    snapshot.playlist.tracks.size
                )
                putExtra(
                    EXTRA_SELECTED_COUNT,
                    selected.size
                )
                putExtra(
                    EXTRA_QUESTIONABLE_COUNT,
                    questionable
                )
                putExtra(
                    EXTRA_GOOGLE_LABEL,
                    auth.googleAccountInfo
                        ?.email
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "буде перевірено перед записом"
                )
                putExtra(
                    EXTRA_CHANNEL_LABEL,
                    auth.youtubeChannelInfo
                        ?.title
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "буде перевірено перед записом"
                )
                putExtra(
                    EXTRA_NEW_QUOTA_PLAN,
                    quotaText
                )
            }
        }

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
        const val EXTRA_LOCAL_SKIP_POSITIONS = "destination_local_skip_positions"

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

        private const val STATE_CURRENT_MODE =
            "destination_current_mode"

        private val BACKGROUND = Color.rgb(15, 16, 19)
        private val SURFACE = Color.rgb(25, 27, 32)
        private val BORDER = Color.rgb(48, 51, 59)
        private val MUTED = Color.rgb(165, 167, 173)
    }
}
