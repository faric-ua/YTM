package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.ScrollPositionState
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotAvailability
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotDuplicateMode
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotDuplicatePolicy
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotLocalCommitter
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotReadPolicy
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotRemoteOperations
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotResolvedItem
import com.saney.ytmimporter.urlsnapshot.UrlSnapshotUnavailableReason
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UrlSnapshotActivity : Activity() {
    private lateinit var urlInput:
        EditText

    private var enteredUrl:
        String =
        ""

    private var commitStarted =
        false

    private var duplicateChoiceOpen =
        false

    private lateinit var scrollView:
        ScrollView

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    private val remoteListener:
        (UrlSnapshotRemoteOperations.State) -> Unit = {
            captureInput()
            buildUi()
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        AppThemeManager
            .applyWindow(
                this
            )

        enteredUrl =
            savedInstanceState
                ?.getString(
                    STATE_URL_INPUT
                )
                ?: UrlSnapshotRemoteOperations
                    .current()
                    .inputUrl

        duplicateChoiceOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_DUPLICATE_CHOICE_OPEN,
                    false
                )
                ?: false

        scrollPosition.restore(
            savedInstanceState
        )

        buildUi()
    }

    override fun onStart() {
        super.onStart()

        UrlSnapshotRemoteOperations
            .addListener(
                remoteListener
            )
    }

    override fun onStop() {
        UrlSnapshotRemoteOperations
            .removeListener(
                remoteListener
            )

        super.onStop()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        captureInput()

        outState.putString(
            STATE_URL_INPUT,
            enteredUrl
        )

        outState.putBoolean(
            STATE_DUPLICATE_CHOICE_OPEN,
            duplicateChoiceOpen
        )

        scrollPosition.save(
            outState,
            if (::scrollView.isInitialized) {
                scrollView
            } else {
                null
            }
        )

        super.onSaveInstanceState(
            outState
        )
    }

    override fun onPause() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }
        super.onPause()
    }

    @Deprecated(
        "Deprecated in Java"
    )
    override fun onBackPressed() {
        finish()
    }

    private fun captureInput() {
        if (::urlInput.isInitialized) {
            enteredUrl =
                urlInput
                    .text
                    ?.toString()
                    .orEmpty()
        }
    }

    private fun buildUi() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }

        val palette =
            AppThemeManager
                .palette(
                    this
                )

        val state =
            UrlSnapshotRemoteOperations
                .current()

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

        scrollView =
            ScrollView(this).apply {
                isFillViewport =
                    true
            }

        val scroll =
            scrollView

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(28)
                )
            }

        content.addView(
            sectionTitle(
                "YouTube / YTM URL"
            )
        )

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@UrlSnapshotActivity
                    ).apply {
                        text =
                            "Snapshot із посилання"

                        textSize =
                            16f

                        setTextColor(
                            Color.WHITE
                        )

                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    infoText(
                        "Вставте URL плейлиста YouTube або YouTube Music. " +
                            "Читання запускається тільки кнопкою нижче. " +
                            "Попередній перегляд не змінює поточний локальний список."
                    )
                )

                urlInput =
                    EditText(
                        this@UrlSnapshotActivity
                    ).apply {
                        hint =
                            "https://music.youtube.com/playlist?list=..."

                        setSingleLine(
                            false
                        )

                        setHorizontallyScrolling(
                            false
                        )

                        minLines =
                            2

                        maxLines =
                            3

                        gravity =
                            Gravity.TOP or
                                Gravity.START

                        inputType =
                            InputType
                                .TYPE_CLASS_TEXT or
                                InputType
                                    .TYPE_TEXT_VARIATION_URI or
                                InputType
                                    .TYPE_TEXT_FLAG_MULTI_LINE

                        textSize =
                            14f

                        setTextColor(
                            Color.WHITE
                        )

                        setHintTextColor(
                            Color.rgb(
                                120,
                                123,
                                130
                            )
                        )

                        setPadding(
                            dp(12),
                            dp(10),
                            dp(52),
                            dp(10)
                        )

                        background =
                            AppThemeManager
                                .surfaceDrawable(
                                    context =
                                        this@UrlSnapshotActivity,
                                    fill =
                                        palette.surfaceAlt,
                                    radiusDp =
                                        10,
                                    accentStroke =
                                        true
                                )

                        setText(
                            enteredUrl
                        )

                        setSelection(
                            text.length
                        )
                    }

                val urlField =
                    FrameLayout(
                        this@UrlSnapshotActivity
                    ).apply {
                        addView(
                            urlInput,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams
                                    .MATCH_PARENT,
                                ViewGroup.LayoutParams
                                    .WRAP_CONTENT
                            )
                        )

                        addView(
                            ImageButton(
                                this@UrlSnapshotActivity
                            ).apply {
                                contentDescription =
                                    "Очистити URL"

                                setImageResource(
                                    R.drawable.ic_ytm_clear
                                )

                                setColorFilter(
                                    palette.muted
                                )

                                background = null

                                setPadding(
                                    dp(10),
                                    dp(10),
                                    dp(10),
                                    dp(10)
                                )

                                setOnClickListener {
                                    urlInput.setText("")
                                    enteredUrl = ""
                                    urlInput.requestFocus()
                                }
                            },
                            FrameLayout.LayoutParams(
                                dp(44),
                                dp(44),
                                Gravity.END or
                                    Gravity.CENTER_VERTICAL
                            )
                        )
                    }

                addView(
                    urlField,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams
                            .MATCH_PARENT,
                        ViewGroup.LayoutParams
                            .WRAP_CONTENT
                    )
                )

                val resolve =
                    actionButton(
                        label =
                            if (state.running) {
                                "Читаю…"
                            } else {
                                "Прочитати URL"
                            },
                        primary =
                            true,
                        topMarginDp =
                            10
                    ) {
                        captureInput()
                        if (
                            UrlSnapshotReadPolicy.canRead(
                                enteredUrl,
                                UrlSnapshotRemoteOperations.current().running
                            )
                        ) {
                            duplicateChoiceOpen =
                                false
                            UrlSnapshotRemoteOperations
                                .startResolve(
                                    context =
                                        this@UrlSnapshotActivity,
                                    rawUrl =
                                        enteredUrl
                                )
                        } else {
                            // No remote state mutation for blank input.
                            urlInput.requestFocus()
                        }
                    }

                fun refreshResolveButton() {
                    val enabled =
                        UrlSnapshotReadPolicy.canRead(
                            enteredUrl,
                            state.running
                        )

                    resolve.isEnabled =
                        enabled
                    resolve.alpha =
                        if (enabled) {
                            1f
                        } else {
                            0.45f
                        }
                }

                urlInput.addTextChangedListener(
                    object : TextWatcher {
                        override fun beforeTextChanged(
                            text: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                            // No-op.
                        }

                        override fun onTextChanged(
                            text: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            // No-op.
                        }

                        override fun afterTextChanged(
                            text: Editable?
                        ) {
                            enteredUrl =
                                text?.toString().orEmpty()
                            refreshResolveButton()
                        }
                    }
                )
                refreshResolveButton()
                addView(
                    resolve
                )
            }
        )

        when (state.phase) {
            UrlSnapshotRemoteOperations
                .Phase.IDLE -> {
                content.addView(
                    infoCard(
                        title =
                            "Готово до читання",
                        body =
                            "Нічого не запускається автоматично. " +
                                "Вставте URL і натисніть «Прочитати URL»."
                    )
                )
            }

            UrlSnapshotRemoteOperations
                .Phase.RESOLVING -> {
                content.addView(
                    infoCard(
                        title =
                            "Читання",
                        body =
                            state.message
                    )
                )
            }

            UrlSnapshotRemoteOperations
                .Phase.RESOLVED -> {
                showResolvedPreview(
                    content =
                        content,
                    state =
                        state
                )
            }

            UrlSnapshotRemoteOperations
                .Phase.UNSUPPORTED -> {
                content.addView(
                    infoCard(
                        title =
                            "Mix не підтримується цим resolver",
                        body =
                            state.message
                    )
                )

                content.addView(
                    cancelPreviewButton()
                )
            }

            UrlSnapshotRemoteOperations
                .Phase.ERROR -> {
                content.addView(
                    infoCard(
                        title =
                            if (
                                state.authorizationInvalidated
                            ) {
                                "Потрібне повторне підключення Google / YTM"
                            } else {
                                "Не вдалося прочитати URL"
                            },
                        body =
                            state.message
                    )
                )

                content.addView(
                    cancelPreviewButton()
                )
            }
        }

        scroll.addView(
            content
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams
                    .MATCH_PARENT,
                0,
                1f
            )
        )

        buildActionFooter(
            state
        )
            ?.let {
                footer ->
                root.addView(
                    footer,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams
                            .MATCH_PARENT,
                        ViewGroup.LayoutParams
                            .WRAP_CONTENT
                    )
                )
            }

        setContentView(
            root
        )

        UiChrome
            .applyScreenInsets(
                this,
                root
            )

        scrollPosition.restoreInto(
            scrollView
        )
    }

    private fun showResolvedPreview(
        content: LinearLayout,
        state:
            UrlSnapshotRemoteOperations.State
    ) {
        val resolved =
            state.resolved
                ?: return

        content.addView(
            sectionTitle(
                "Попередній перегляд"
            )
        )

        val duplicateAnalysis =
            UrlSnapshotDuplicatePolicy
                .analyze(
                    resolved.items
                )

        val summaryCard =
            infoCard(
                title =
                    if (state.fromCache) {
                        "Snapshot із локального кешу"
                    } else {
                        "Snapshot прочитано"
                    },
                body = ""
            )

        resolved.playlistTitle
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let {
                title ->
                summaryCard.addView(
                    snapshotIdentityText(
                        "Плейлист: $title"
                    )
                )
            }

        summaryCard.addView(
            snapshotStatLine(
                label = "Елементів",
                value =
                    resolved.items.size
                        .toString()
            )
        )
        summaryCard.addView(
            snapshotStatLine(
                label =
                    "Унікальних exact videoId",
                value =
                    duplicateAnalysis
                        .uniqueExactIdCount
                        .toString()
            )
        )
        summaryCard.addView(
            snapshotStatLine(
                label =
                    "Повторних входжень",
                value =
                    duplicateAnalysis
                        .duplicateOccurrences
                        .toString()
            )
        )

        if (
            resolved.unavailableCount >
                0
        ) {
            summaryCard.addView(
                snapshotStatLine(
                    label =
                        "Недоступних",
                    value =
                        resolved
                            .unavailableCount
                            .toString(),
                    valueColor =
                        AppThemeManager
                            .palette(
                                this
                            )
                            .semantic
                            .warning
                )
            )
        }

        summaryCard.addView(
            infoText(
                "Діагностика: ${state.message}"
            )
        )

        state.cachedAt
            ?.let {
                cachedAt ->
                summaryCard.addView(
                    infoText(
                        "Локальний snapshot: " +
                            formatDateTime(
                                cachedAt
                            )
                    )
                )
            }

        summaryCard.addView(
            infoText(
                "Поточний локальний список не змінено. " +
                    "Жоден Review/Search/write flow автоматично не запускається."
            )
        )

        if (state.fromCache && resolved.playlistTitle.isNullOrBlank()) {
            summaryCard.addView(
                actionButton(
                    label = "Отримати назву плейлиста • 1 API",
                    primary = false,
                    topMarginDp = 2
                ) {
                    duplicateChoiceOpen = false
                    captureInput()
                    UrlSnapshotRemoteOperations.loadMissingPlaylistTitle(this@UrlSnapshotActivity)
                }
            )
        }

        if (state.fromCache) {
            summaryCard.addView(
                actionButton(
                    label =
                        "Оновити з YouTube",
                    primary =
                        false,
                    topMarginDp =
                        2
                ) {
                    duplicateChoiceOpen =
                        false
                    captureInput()

                    UrlSnapshotRemoteOperations
                        .startResolve(
                            context =
                                this@UrlSnapshotActivity,
                            rawUrl =
                                enteredUrl,
                            forceRemote =
                                true
                        )
                }
            )
        }

        content.addView(
            summaryCard
        )

        resolved.items.forEachIndexed {
                position,
                item ->

            content.addView(
                previewItem(
                    item = item,
                    duplicateFirstIndex =
                        duplicateAnalysis
                            .firstOccurrenceByDuplicateIndex[
                                position
                            ]
                )
            )
        }
    }

    private fun commitResolved(
        resolved:
            com.saney.ytmimporter.urlsnapshot
                .UrlSnapshotResolutionResult
                .Resolved,
        duplicateMode:
            UrlSnapshotDuplicateMode
    ) {
        if (commitStarted) {
            return
        }

        commitStarted =
            true

        val result =
            runCatching {
                UrlSnapshotLocalCommitter(
                    this
                )
                    .commit(
                        resolved =
                            resolved,
                        duplicateMode =
                            duplicateMode
                    )
            }

        result.onSuccess {
                receipt ->

            captureInput()

            duplicateChoiceOpen =
                false

            UrlSnapshotRemoteOperations
                .clearTerminal()

            setResult(
                RESULT_OK,
                Intent()
                    .putExtra(
                        EXTRA_COMMIT_MESSAGE,
                        receipt.message
                    )
                    .putExtra(
                        EXTRA_HISTORY_ENTRY_ID,
                        receipt.historyEntryId
                    )
            )

            finish()
        }.onFailure {
                error ->

            commitStarted =
                false

            UiChrome.showMessageDialog(
                activity =
                    this,
                title =
                    "Не вдалося зберегти snapshot",
                message =
                    error.message
                        ?: "Локальний список не вдалося оновити.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label =
                                "Закрити",
                            tone =
                                UiChrome.ActionTone.NORMAL,
                            onClick = {}
                        )
                    )
            )
        }
    }

    private fun previewItem(
        item:
            UrlSnapshotResolvedItem,
        duplicateFirstIndex:
            Int?
    ): TextView {
        val unavailable =
            item.availability ==
                UrlSnapshotAvailability
                    .UNAVAILABLE

        return TextView(this).apply {
            text =
                buildString {
                    append(
                        item.index + 1
                    )

                    append(
                        ". "
                    )

                    append(
                        item.title
                            ?: if (unavailable) {
                                "Недоступний елемент"
                            } else {
                                "Без назви"
                            }
                    )

                    item.channelTitle
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            channel ->
                            append(
                                "\n"
                            )

                            append(
                                channel
                            )
                        }

                    append(
                        "\nvideoId: "
                    )

                    append(
                        item.videoId
                            ?: "недоступний"
                    )

                    if (duplicateFirstIndex != null) {
                        append(
                            "\n⧉ Повтор exact videoId • перша поява #"
                        )
                        append(
                            duplicateFirstIndex + 1
                        )
                    }

                    if (unavailable) {
                        append(
                            "\n⚠ "
                        )

                        append(
                            unavailableLabel(
                                item.unavailableReason
                            )
                        )
                    }
                }

            textSize =
                13f

            setTextColor(
                Color.WHITE
            )

            setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
            )

            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@UrlSnapshotActivity,
                        fill =
                            AppThemeManager
                                .palette(
                                    this@UrlSnapshotActivity
                                )
                                .surface,
                        radiusDp =
                            12,
                        accentStroke =
                            true,
                        accentOverride =
                            if (
                                unavailable ||
                                duplicateFirstIndex != null
                            ) {
                                AppThemeManager
                                    .palette(
                                        this@UrlSnapshotActivity
                                    )
                                    .semantic
                                    .warning
                            } else {
                                null
                            }
                    )

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams
                        .MATCH_PARENT,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(7)
                }
        }
    }

    private fun unavailableLabel(
        reason:
            UrlSnapshotUnavailableReason?
    ): String =
        when (reason) {
            UrlSnapshotUnavailableReason
                .PRIVATE_VIDEO ->
                "Приватне відео"

            UrlSnapshotUnavailableReason
                .DELETED_VIDEO ->
                "Видалене відео"

            UrlSnapshotUnavailableReason
                .MISSING_VIDEO_ID ->
                "YouTube не повернув точний videoId"

            null ->
                "Елемент недоступний"
        }

    private fun cancelPreviewButton():
        Button =
        actionButton(
            label =
                "Скасувати preview",
            primary =
                false,
            topMarginDp =
                4
        ) {
            duplicateChoiceOpen =
                false
            captureInput()

            UrlSnapshotRemoteOperations
                .clearTerminal()
        }

    private fun buildActionFooter(
        state:
            UrlSnapshotRemoteOperations.State
    ): LinearLayout? =
        when (state.phase) {
            UrlSnapshotRemoteOperations
                .Phase.RESOLVED ->
                state.resolved
                    ?.let(
                        ::resolvedActionFooter
                    )

            UrlSnapshotRemoteOperations
                .Phase.UNSUPPORTED,
            UrlSnapshotRemoteOperations
                .Phase.ERROR ->
                footerShell().apply {
                    addView(
                        cancelPreviewButton()
                    )
                }

            else ->
                null
        }

    private fun resolvedActionFooter(
        resolved:
            com.saney.ytmimporter.urlsnapshot
                .UrlSnapshotResolutionResult
                .Resolved
    ): LinearLayout {
        val analysis =
            UrlSnapshotDuplicatePolicy
                .analyze(
                    resolved.items
                )

        return footerShell().apply {
            if (
                duplicateChoiceOpen &&
                analysis.duplicateOccurrences > 0
            ) {
                addView(
                    TextView(
                        this@UrlSnapshotActivity
                    ).apply {
                        text =
                            "Знайдено ${analysis.duplicateOccurrences} повторних входжень exact videoId."
                        textSize =
                            12.5f
                        setTextColor(
                            MUTED
                        )
                        setPadding(
                            dp(4),
                            0,
                            dp(4),
                            dp(6)
                        )
                    }
                )

                val allButton =
                    actionButton(
                        label =
                            "Всі (${resolved.items.size})",
                        primary =
                            true
                    ) {
                        commitResolved(
                            resolved =
                                resolved,
                            duplicateMode =
                                UrlSnapshotDuplicateMode
                                    .KEEP_ALL
                        )
                    }

                val uniqueButton =
                    actionButton(
                        label =
                            "Унікальні (${resolved.items.size - analysis.duplicateOccurrences})",
                        primary =
                            false
                    ) {
                        commitResolved(
                            resolved =
                                resolved,
                            duplicateMode =
                                UrlSnapshotDuplicateMode
                                    .DROP_REPEATED_EXACT_VIDEO_IDS
                        )
                    }

                val cancelButton =
                    actionButton(
                        label =
                            "Скасувати",
                        primary =
                            false
                    ) {
                        duplicateChoiceOpen =
                            false
                        buildUi()
                    }

                val choiceActions =
                    LinearLayout(
                        this@UrlSnapshotActivity
                    ).apply {
                        orientation =
                            LinearLayout.VERTICAL
                    }

                UiChrome
                    .addAdaptiveActionButtons(
                        activity =
                            this@UrlSnapshotActivity,
                        container =
                            choiceActions,
                        buttons =
                            listOf(
                                allButton,
                                uniqueButton,
                                cancelButton
                            ),
                        buttonHeightDp =
                            58,
                        tones =
                            listOf(
                                UiChrome.ActionTone.ACCENT,
                                UiChrome.ActionTone.NORMAL,
                                UiChrome.ActionTone.NORMAL
                            )
                    )

                addView(
                    choiceActions,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams
                            .MATCH_PARENT,
                        ViewGroup.LayoutParams
                            .WRAP_CONTENT
                    )
                )
            } else {
                val saveButton =
                    actionButton(
                        label =
                            "Зберегти як поточний список",
                        primary =
                            true
                    ) {
                        if (
                            analysis.duplicateOccurrences > 0
                        ) {
                            duplicateChoiceOpen =
                                true
                            buildUi()
                        } else {
                            commitResolved(
                                resolved =
                                    resolved,
                                duplicateMode =
                                    UrlSnapshotDuplicateMode
                                        .KEEP_ALL
                            )
                        }
                    }

                val cancelButton =
                    cancelPreviewButton()

                UiChrome
                    .addAdaptiveActionButtons(
                        activity =
                            this@UrlSnapshotActivity,
                        container =
                            this,
                        buttons =
                            listOf(
                                saveButton,
                                cancelButton
                            ),
                        buttonHeightDp =
                            58
                    )
            }
        }
    }

    private fun footerShell():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(12),
                dp(7),
                dp(12),
                dp(10)
            )
            setBackgroundColor(
                AppThemeManager
                    .palette(
                        this@UrlSnapshotActivity
                    )
                    .background
            )
        }

    private fun formatDateTime(
        timestamp: Long
    ): String =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(
            Date(timestamp)
        )

    private fun topBar():
        LinearLayout =
        LinearLayout(this).apply {
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
                UiChrome
                    .backButton(
                        activity =
                            this@UrlSnapshotActivity,
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
                UiChrome
                    .emphasizedTitle(
                        activity =
                            this@UrlSnapshotActivity,
                        label =
                            "URL snapshot"
                    )
                    .apply {
                        setPadding(
                            dp(12),
                            0,
                            0,
                            0
                        )
                    },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT,
                    1f
                )
            )
        }

    private fun sectionTitle(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text =
                text

            textSize =
                13f

            setTextColor(
                MUTED
            )

            setTypeface(
                typeface,
                Typeface.BOLD
            )

            setPadding(
                dp(4),
                dp(10),
                0,
                dp(6)
            )
        }

    private fun card():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
            )

            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@UrlSnapshotActivity,
                        fill =
                            AppThemeManager
                                .palette(
                                    this@UrlSnapshotActivity
                                )
                                .surface,
                        radiusDp =
                            14,
                        accentStroke =
                            true
                    )

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams
                        .MATCH_PARENT,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(8)
                }
        }

    private fun infoCard(
        title: String,
        body: String
    ): LinearLayout =
        card().apply {
            addView(
                TextView(
                    this@UrlSnapshotActivity
                ).apply {
                    text =
                        title

                    textSize =
                        15f

                    setTextColor(
                        Color.WHITE
                    )

                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                }
            )

            if (body.isNotBlank()) {
                addView(
                    infoText(
                        body
                    )
                )
            }
        }

    private fun snapshotIdentityText(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text =
                text
            textSize =
                14f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                Color.WHITE
            )
            setPadding(
                0,
                dp(8),
                0,
                dp(4)
            )
            maxLines =
                3
        }

    private fun snapshotStatLine(
        label: String,
        value: String,
        valueColor: Int =
            Color.WHITE
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                0,
                dp(4),
                0,
                dp(4)
            )

            addView(
                TextView(
                    this@UrlSnapshotActivity
                ).apply {
                    text =
                        label
                    textSize =
                        12.5f
                    setTextColor(
                        MUTED
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(
                    this@UrlSnapshotActivity
                ).apply {
                    text =
                        value
                    textSize =
                        13f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        valueColor
                    )
                    gravity =
                        Gravity.END
                }
            )
        }

    private fun infoText(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text =
                text

            textSize =
                12.5f

            setTextColor(
                MUTED
            )

            setPadding(
                0,
                dp(7),
                0,
                dp(10)
            )
        }

    private fun actionButton(
        label: String,
        primary: Boolean,
        topMarginDp: Int = 0,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text =
                label

            isAllCaps =
                false

            textSize =
                13f

            setTextColor(
                Color.WHITE
            )

            gravity =
                Gravity.CENTER

            maxLines =
                2

            minimumHeight =
                dp(58)

            minHeight =
                dp(58)

            setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(10)
            )

            UiChrome
                .autoSizeButton(
                    this,
                    minSp =
                        11,
                    maxSp =
                        14
                )

            background =
                if (primary) {
                    AppThemeManager
                        .accentButtonDrawable(
                            this@UrlSnapshotActivity,
                            11
                        )
                } else {
                    AppThemeManager
                        .neutralButtonDrawable(
                            this@UrlSnapshotActivity,
                            11
                        )
                }

            setOnClickListener {
                action()
            }

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams
                        .MATCH_PARENT,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(
                            topMarginDp
                        )
                }
        }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
            )
            .toInt()

    companion object {
        private const val STATE_URL_INPUT =
            "url_snapshot_input"

        private const val STATE_DUPLICATE_CHOICE_OPEN =
            "url_snapshot_duplicate_choice_open"

        private const val STATE_SCROLL_POSITION =
            "url_snapshot_scroll_position"

        const val EXTRA_COMMIT_MESSAGE =
            "url_snapshot_commit_message"

        const val EXTRA_HISTORY_ENTRY_ID =
            "url_snapshot_history_entry_id"

        private val MUTED =
            Color.rgb(
                165,
                167,
                173
            )
    }
}
