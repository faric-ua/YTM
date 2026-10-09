package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.bulk.BulkSyncExecutionPolicy
import com.saney.ytmimporter.bulk.BulkSyncExecutor
import com.saney.ytmimporter.bulk.BulkCreateBatchPolicy
import com.saney.ytmimporter.bulk.BulkWriteRetryGuard
import com.saney.ytmimporter.bulk.BulkSyncHelpContent
import com.saney.ytmimporter.bulk.BulkSyncQaFaultPolicy
import com.saney.ytmimporter.bulk.BulkSyncRollbackExecutor
import com.saney.ytmimporter.bulk.BulkSyncRollbackPolicy
import com.saney.ytmimporter.bulk.BulkSyncMutationStatus
import com.saney.ytmimporter.bulk.BulkSyncMutationType
import com.saney.ytmimporter.bulk.BulkSyncSession
import com.saney.ytmimporter.bulk.BulkSyncSessionRow
import com.saney.ytmimporter.bulk.BulkSyncSessionRowState
import com.saney.ytmimporter.bulk.BulkSyncSessionState
import com.saney.ytmimporter.storage.BulkSyncQaFaultStore
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.BulkHierarchyChrome
import com.saney.ytmimporter.ui.RestorableModalController
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.ui.ScrollPositionState
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors
import java.text.DateFormat
import java.util.Date

class BulkSyncSessionActivity : Activity() {
    private enum class SessionModal {
        ROLLBACK_CONFIRM,
        CREATE_BATCH_SIZE
    }

    private val worker =
        Executors.newSingleThreadExecutor()

    private lateinit var sessionStore:
        BulkSyncSessionStore

    private lateinit var statusText:
        TextView

    private lateinit var summaryPanel:
        LinearLayout

    private lateinit var rowsContainer:
        LinearLayout

    private lateinit var primaryButton:
        Button

    private lateinit var closeButton:
        Button

    private lateinit var scrollView:
        ScrollView

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    @Volatile
    private var running =
        false

    private var helpDialogOpen =
        false

    private var helpDialog:
        Dialog? = null

    private lateinit var sessionModalController:
        RestorableModalController

    private val api by lazy {
        YouTubeApi(
            accessTokenRecovery =
                GoogleAccessTokenRecovery(this)
        )
    }

    private val bulkExecutor by lazy {
        BulkSyncExecutor(
            api = api,
            sessionStore =
                sessionStore,
            restorableStore =
                RestorablePlaylistStore(this),
            currentPlaylistStore =
                CurrentPlaylistStore(this),
            quotaTracker =
                QuotaTracker(this),
            qaInsertFault = {
                BulkSyncQaFaultStore(this)
                    .consume()
                    ?.let(
                        BulkSyncQaFaultPolicy::asException
                    )
            }
        )
    }

    private val rollbackExecutor by lazy {
        BulkSyncRollbackExecutor(
            api = api,
            sessionStore =
                sessionStore,
            restorableStore =
                RestorablePlaylistStore(this),
            currentPlaylistStore =
                CurrentPlaylistStore(this),
            quotaTracker =
                QuotaTracker(this),
            qaInterruptAfterAppliedMutation = {
                BulkSyncQaFaultStore(this)
                    .consumeRollbackInterruptAfterOne()
            }
        )
    }

    private var sessionId:
        String? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        scrollPosition.restore(
            savedInstanceState
        )

        sessionStore =
            BulkSyncSessionStore(this)

        sessionModalController =
            RestorableModalController(
                activity = this,
                stateKey =
                    STATE_SESSION_MODAL
            )
        sessionModalController.restore(
            savedInstanceState
        )

        helpDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_HELP_DIALOG_OPEN,
                    false
                )
                ?: false

        sessionId =
            intent.getStringExtra(
                EXTRA_SESSION_ID
            )
                ?: sessionStore
                    .active()
                    ?.sessionId
                ?: sessionStore
                    .latest()
                    ?.sessionId

        buildUi()

        val id =
            sessionId

        if (id == null) {
            renderMissing()
            return
        }

        val stored =
            sessionStore.get(id)

        if (stored == null) {
            renderMissing()
            return
        }

        val normalized =
            BulkSyncExecutionPolicy
                .normalizeAfterColdOpen(
                    stored
                )

        if (normalized != stored) {
            sessionStore.upsert(
                normalized
            )
        }

        render(normalized)

        sessionModalController
            .restoreAfterContentReady(
                renderer =
                    ::renderSessionModal
            )

        if (helpDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showHelp()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_HELP_DIALOG_OPEN,
            helpDialogOpen
        )

        sessionModalController.save(
            outState
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

    override fun onResume() {
        super.onResume()

        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }

        if (!running) {
            val stored =
                sessionId
                    ?.let(
                        sessionStore::get
                    )

            if (stored != null) {
                val normalized =
                    BulkSyncExecutionPolicy
                        .normalizeAfterColdOpen(
                            stored
                        )

                if (normalized != stored) {
                    sessionStore.upsert(
                        normalized
                    )
                }

                render(
                    normalized
                )
            }
        }
    }

    override fun onPause() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }
        super.onPause()
    }

    override fun onDestroy() {
        helpDialog
            ?.setOnDismissListener(null)
        helpDialog = null
        sessionModalController.onDestroy()
        worker.shutdownNow()
        super.onDestroy()
    }

    @Deprecated(
        "Use OnBackPressedDispatcher when the app migrates to AndroidX activity."
    )
    override fun onBackPressed() {
        if (running) {
            toast(
                "Дочекайтеся завершення поточної дії або закрийте застосунок."
            )
            return
        }

        super.onBackPressed()
    }

    private fun buildUi() {
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

        val topBar =
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
            }

        topBar.addView(
            UiChrome.backButton(
                activity = this
            ) {
                if (!running) {
                    finish()
                } else {
                    toast(
                        "Синхронізація зараз виконує дію."
                    )
                }
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        topBar.addView(
            UiChrome.emphasizedTitle(
                activity = this,
                label = "Сесія синхронізації"
            ).apply {
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

        topBar.addView(
            Button(this).apply {
                text = "?"
                isAllCaps = false
                textSize = 20f
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
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
                            this@BulkSyncSessionActivity
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

        root.addView(topBar)

        statusText =
            TextView(this).apply {
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

        root.addView(statusText)

        scrollView =
            ScrollView(this).apply {
                isFillViewport = true
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
                    dp(12)
                )
            }

        summaryPanel =
            BulkHierarchyChrome.card(
                activity = this,
                useAltSurface = false,
                accentTone =
                    BulkHierarchyChrome
                        .Tone
                        .ACCENT,
                radiusDp = 14
            )

        content.addView(
            summaryPanel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin =
                    dp(10)
            }
        )

        rowsContainer =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        content.addView(
            rowsContainer
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

        val actions =
            LinearLayout(this).apply {
                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(12)
                )
            }

        primaryButton =
            Button(this).apply {
                isAllCaps = false
                textSize = 15f
                setOnClickListener {
                    handlePrimaryAction()
                }
            }

        closeButton =
            Button(this).apply {
                text = "Закрити"
                isAllCaps = false
                textSize = 15f
                setOnClickListener {
                    if (!running) {
                        finish()
                    }
                }
            }

        UiChrome.addAdaptiveActionButtons(
            activity = this,
            container = actions,
            buttons =
                listOf(
                    primaryButton,
                    closeButton
                ),
            buttonHeightDp = 54
        )

        root.addView(actions)

        setContentView(root)

        UiChrome.applyScreenInsets(
            this,
            root
        )

        scrollPosition.restoreInto(
            scrollView
        )
    }

    private fun handlePrimaryAction() {
        if (running) {
            return
        }

        val id =
            sessionId
                ?: return

        val session =
            sessionStore.get(id)
                ?: return renderMissing()

        when {
            BulkSyncRollbackPolicy
                .canStartRollback(
                    session
                ) -> showRollbackConfirmation(
                    session
                )

            BulkSyncRollbackPolicy
                .canResumeRollback(
                    session
                ) -> startRollback()

            else ->
                startOrResume()
        }
    }

    private fun showBatchSizePicker() {
        if (running) return
        sessionModalController.show(
            modalId = SessionModal.CREATE_BATCH_SIZE.name,
            renderer = ::renderSessionModal
        )
    }

    private fun setBatchSize(size: Int) {
        if (running || size !in BulkCreateBatchPolicy.SIZES) return
        val id = sessionId ?: return
        val current = sessionStore.get(id) ?: return
        if (current.state !in setOf(
                BulkSyncSessionState.READY,
                BulkSyncSessionState.PAUSED_CREATE_BATCH,
                BulkSyncSessionState.PAUSED_INTERRUPTED,
                BulkSyncSessionState.PAUSED_WRITE_QUOTA,
                BulkSyncSessionState.PAUSED_RATE_LIMIT,
                BulkSyncSessionState.PAUSED_AUTH
            )
        ) return
        sessionStore.upsert(current.copy(
            maxCreatesPerRun = size,
            updatedAt = System.currentTimeMillis()
        ))
        sessionStore.get(id)?.let(::render)
    }

    private fun showRollbackConfirmation(
        session: BulkSyncSession
    ) {
        sessionModalController.show(
            modalId =
                SessionModal
                    .ROLLBACK_CONFIRM
                    .name,
            renderer =
                ::renderSessionModal
        )
    }

    private fun renderSessionModal(
        modalId: String,
        args: Bundle
    ): Dialog? {
        val modal =
            SessionModal.values()
                .firstOrNull {
                    it.name == modalId
                }
                ?: return null

        val session =
            sessionId
                ?.let(
                    sessionStore::get
                )
                ?: return null

        return when (modal) {
            SessionModal.CREATE_BATCH_SIZE -> {
                if (running || session.isTerminal ||
                    session.state == BulkSyncSessionState.RUNNING ||
                    BulkCreateBatchPolicy.remainingCreates(session) == 0
                ) return null

                UiChrome.showFixedFooterMessageDialog(
                    activity = this,
                    title = "Нових плейлістів за запуск",
                    message =
                        "Це обережна кількість для одного ручного запуску, " +
                        "а не офіційний денний ліміт YouTube. " +
                        "Після пакета програма зупиниться. " +
                        "Наступний пакет запускається тільки вручну.",
                    actions =
                        BulkCreateBatchPolicy.SIZES.sorted().map { size ->
                            UiChrome.DialogAction(label = "$size за запуск") {
                                sessionModalController.clearState()
                                setBatchSize(size)
                            }
                        } + UiChrome.DialogAction(label = "Скасувати") {
                            sessionModalController.clearState()
                        }
                )
            }

            SessionModal.ROLLBACK_CONFIRM -> {
                if (
                    !BulkSyncRollbackPolicy
                        .canStartRollback(
                            session
                        )
                ) {
                    return null
                }

                val creates =
                    session.mutationLedger
                        .count {
                            it.type ==
                                BulkSyncMutationType
                                    .CREATE_PLAYLIST &&
                                it.status ==
                                    BulkSyncMutationStatus
                                        .APPLIED
                        }

                val inserts =
                    session.mutationLedger
                        .count {
                            it.type ==
                                BulkSyncMutationType
                                    .INSERT_PLAYLIST_ITEM &&
                                it.status ==
                                    BulkSyncMutationStatus
                                        .APPLIED
                        }

                UiChrome.showFixedFooterMessageDialog(
                    activity = this,
                    title =
                        "Відкотити цю синхронізацію?",
                    message =
                        "Програма відкочує тільки дії, точно записані в цій сесії.\n\n" +
                            "Буде видалено доданих треків: " +
                            inserts +
                            ". Створених цією сесією плейлистів: " +
                            creates +
                            ".\n\n" +
                            "Існуючі раніше плейлисти та треки не визначаються за назвою і не видаляються. " +
                            "Після перезапуску відкат не продовжиться автоматично.",
                    actions =
                        listOf(
                            UiChrome.DialogAction(
                                label =
                                    "Відкотити",
                                tone =
                                    UiChrome.ActionTone
                                        .DANGER
                            ) {
                                sessionModalController
                                    .clearState()
                                startRollback()
                            },
                            UiChrome.DialogAction(
                                label =
                                    "Скасувати"
                            ) {
                                sessionModalController
                                    .clearState()
                            }
                        )
                )
            }
        }
    }

    private fun startRollback() {
        if (running) {
            return
        }

        val id =
            sessionId
                ?: return

        val session =
            sessionStore.get(id)
                ?: return renderMissing()

        val exactnessError =
            BulkSyncRollbackPolicy
                .exactnessError(
                    session
                )

        if (exactnessError != null) {
            render(session)
            statusText.text =
                "Відкат не розпочато.\n" +
                    exactnessError
            toast(
                "Відкат не розпочато. Деталі залишилися на екрані."
            )
            return
        }

        val token =
            AuthSessionStore.current()
                .accessToken
                ?.takeIf(
                    String::isNotBlank
                )

        if (token == null) {
            val paused =
                session.copy(
                    state =
                        BulkSyncSessionState
                            .ROLLBACK_PAUSED,
                    updatedAt =
                        System.currentTimeMillis(),
                    lastError =
                        "Для продовження відкату потрібна Google/YTM авторизація."
                )

            sessionStore.upsert(
                paused
            )
            render(paused)
            return
        }

        running = true
        primaryButton.isEnabled =
            false
        closeButton.isEnabled =
            false

        worker.execute {
            try {
                rollbackExecutor.rollback(
                    accessToken =
                        token,
                    sessionId =
                        id,
                    onProgress = {
                            progress ->
                        runOnUiThread {
                            if (
                                !isFinishing &&
                                !isDestroyed
                            ) {
                                render(
                                    progress
                                )
                            }
                        }
                    }
                )
            } catch (error: Throwable) {
                runOnUiThread {
                    toast(
                        "Відкат зупинено. Перевірте стан і деталі на екрані."
                    )
                }
            } finally {
                running = false

                runOnUiThread {
                    if (
                        !isFinishing &&
                        !isDestroyed
                    ) {
                        sessionStore.get(id)
                            ?.let(::render)
                    }
                }
            }
        }
    }

    private fun startOrResume() {
        if (running) {
            return
        }

        val id =
            sessionId
                ?: return

        val session =
            sessionStore.get(id)
                ?: return renderMissing()

        if (BulkWriteRetryGuard.isWaiting(session)) {
            render(session)
            toast("Захисна пауза ще діє. Час відновлення показано на екрані.")
            return
        }

        val token =
            AuthSessionStore.current()
                .accessToken
                ?.takeIf(
                    String::isNotBlank
                )

        if (token == null) {
            val paused =
                session.copy(
                    state =
                        BulkSyncSessionState
                            .PAUSED_AUTH,
                    updatedAt =
                        System.currentTimeMillis(),
                    lastError =
                        "Підключіть Google / YTM на Home, потім поверніться і натисніть «Продовжити»."
                )

            sessionStore.upsert(
                paused
            )
            render(paused)
            return
        }

        running = true
        primaryButton.isEnabled =
            false
        closeButton.isEnabled =
            false

        worker.execute {
            try {
                val progressCallback:
                    (BulkSyncSession) -> Unit =
                    {
                            progress ->
                        runOnUiThread {
                            if (
                                !isFinishing &&
                                !isDestroyed
                            ) {
                                render(
                                    progress
                                )
                            }
                        }
                    }

                var executable =
                    sessionStore.get(id)
                        ?: throw IllegalStateException(
                            "Сесію синхронізації не знайдено"
                        )

                if (
                    BulkSyncExecutionPolicy
                        .hasUncertainPreparedMutation(
                            executable
                        )
                ) {
                    executable =
                        bulkExecutor
                            .reconcilePrepared(
                                accessToken =
                                    token,
                                sessionId =
                                    id,
                                onProgress =
                                    progressCallback
                            )
                }

                if (
                    !BulkSyncExecutionPolicy
                        .hasUncertainPreparedMutation(
                            executable
                        )
                ) {
                    bulkExecutor.execute(
                        accessToken =
                            token,
                        sessionId =
                            id,
                        onProgress =
                            progressCallback
                    )
                }
            } catch (error: Throwable) {
                runOnUiThread {
                    toast(
                        "Синхронізацію зупинено. Перевірте стан і деталі на екрані."
                    )
                }
            } finally {
                running = false

                runOnUiThread {
                    if (
                        !isFinishing &&
                        !isDestroyed
                    ) {
                        sessionStore.get(id)
                            ?.let(::render)
                    }
                }
            }
        }
    }

    private fun render(
        session: BulkSyncSession
    ) {
        val appliedCreates =
            session.mutationLedger
                .count {
                    it.type ==
                        BulkSyncMutationType
                            .CREATE_PLAYLIST &&
                        it.status ==
                            BulkSyncMutationStatus
                                .APPLIED
                }

        val appliedInserts =
            session.mutationLedger
                .count {
                    it.type ==
                        BulkSyncMutationType
                            .INSERT_PLAYLIST_ITEM &&
                        it.status ==
                            BulkSyncMutationStatus
                                .APPLIED
                }

        val prepared =
            session.mutationLedger
                .count {
                    it.status ==
                        BulkSyncMutationStatus
                            .PREPARED
                }

        val terminalFailed =
            session.mutationLedger
                .count {
                    it.status ==
                        BulkSyncMutationStatus
                            .TERMINAL_FAILED
                }

        statusText.text =
            "Стан: " +
                sessionStateLabel(
                    session.state
                )

        summaryPanel.removeAllViews()

        summaryPanel.addView(
            BulkHierarchyChrome.title(
                activity = this,
                text = "Стан Сесії синхронізації"
            )
        )

        summaryPanel.addView(
            BulkHierarchyChrome.badge(
                activity = this,
                text =
                    sessionStateLabel(
                        session.state
                    ),
                tone =
                    sessionTone(
                        session.state
                    )
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin =
                    dp(7)
            }
        )

        summaryPanel.addView(
            BulkHierarchyChrome.metrics(
                activity = this,
                text =
                    "Плейлистів у плані: " +
                        session.plan.size
            )
        )

        val remainingCreates = BulkCreateBatchPolicy.remainingCreates(session)
        if (remainingCreates > 0 && !session.isTerminal) {
            val batchSize = session.maxCreatesPerRun
            summaryPanel.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text = "Нових ще в плані: $remainingCreates • " +
                        "за один запуск: " +
                        (batchSize?.toString() ?: "без обмеження (стара сесія)")
                )
            )
            if (!running && session.state != BulkSyncSessionState.RUNNING) {
                val batchButton = Button(this).apply {
                    text = "Змінити розмір пакета"
                    isAllCaps = false
                    setOnClickListener { showBatchSizePicker() }
                }
                UiChrome.styleAdaptiveActionButton(
                    activity = this,
                    button = batchButton,
                    tone = UiChrome.ActionTone.NORMAL
                )
                summaryPanel.addView(batchButton)
            }
        }

        summaryPanel.addView(
            BulkHierarchyChrome.primary(
                activity = this,
                text =
                    "Створено: " +
                        appliedCreates +
                        " плейлистів • додано: " +
                        appliedInserts +
                        " треків",
                tone =
                    if (
                        session.state ==
                        BulkSyncSessionState.COMPLETED
                    ) {
                        BulkHierarchyChrome
                            .Tone
                            .SUCCESS
                    } else {
                        BulkHierarchyChrome
                            .Tone
                            .ACCENT
                    }
            )
        )

        if (prepared > 0) {
            summaryPanel.addView(
                BulkHierarchyChrome.primary(
                    activity = this,
                    text =
                        "Потребує перевірки незавершених дій: " +
                            prepared,
                    tone =
                        BulkHierarchyChrome
                            .Tone
                            .WARNING
                )
            )
        }

        if (terminalFailed > 0) {
            summaryPanel.addView(
                BulkHierarchyChrome.primary(
                    activity = this,
                    text =
                        "Не додано треків: " +
                            terminalFailed,
                    tone =
                        BulkHierarchyChrome
                            .Tone
                            .DANGER
                )
            )
        }

        val rolledBack =
            BulkSyncRollbackPolicy
                .rolledBackCount(
                    session
                )

        val rollbackRemaining =
            BulkSyncRollbackPolicy
                .remainingAppliedCount(
                    session
                )

        if (
            rolledBack > 0 ||
            session.state in
                setOf(
                    BulkSyncSessionState
                        .ROLLING_BACK,
                    BulkSyncSessionState
                        .ROLLBACK_PAUSED,
                    BulkSyncSessionState
                        .ROLLED_BACK
                )
        ) {
            summaryPanel.addView(
                BulkHierarchyChrome.primary(
                    activity = this,
                    text =
                        "Відкочено дій: " +
                            rolledBack +
                            " • залишилось: " +
                            rollbackRemaining,
                    tone =
                        if (
                            rollbackRemaining >
                            0
                        ) {
                            BulkHierarchyChrome
                                .Tone
                                .WARNING
                        } else {
                            BulkHierarchyChrome
                                .Tone
                                .SUCCESS
                        }
                )
            )
        }

        summaryPanel.addView(
            BulkHierarchyChrome.secondary(
                activity = this,
                text =
                    "Контрольна точка: збережена • Знімок YTM: " +
                        session.remoteBaseline
                            .playlists
                            .size +
                        " плейлистів"
            )
        )

        if (
            !session.lastError
                .isNullOrBlank()
        ) {
            summaryPanel.addView(
                BulkHierarchyChrome.badge(
                    activity = this,
                    text = "Потрібна увага",
                    tone =
                        BulkHierarchyChrome
                            .Tone
                            .DANGER
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(7)
                }
            )

            summaryPanel.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text =
                        "Деталі: " +
                            session.lastError
                )
            )
        }

        val retryNotBefore = session.retryNotBeforeEpochMs
        if (
            session.state == BulkSyncSessionState.PAUSED_RATE_LIMIT &&
            retryNotBefore != null
        ) {
            val waiting = BulkWriteRetryGuard.isWaiting(session)
            val localTime = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT
            ).format(Date(retryNotBefore))
            summaryPanel.addView(
                BulkHierarchyChrome.primary(
                    activity = this,
                    text =
                        if (waiting) {
                            "Захисна пауза до $localTime (час телефону). " +
                                "До цього часу нові записи не запускаються."
                        } else {
                            "Мінімальна пауза минула. YouTube не гарантує " +
                                "розблокування — продовження лише вручну."
                        },
                    tone = BulkHierarchyChrome.Tone.WARNING
                )
            )
        }

        summaryPanel.addView(
            BulkHierarchyChrome.secondary(
                activity = this,
                text =
                    "Після перезапуску нічого не продовжується автоматично. " +
                        "Продовження синхронізації або відкат запускаються тільки явною дією користувача."
            )
        )

        rowsContainer.removeAllViews()

        session.plan.forEachIndexed {
                index,
                row ->
            rowsContainer.addView(
                rowView(
                    session =
                        session,
                    index =
                        index,
                    row =
                        row
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(8)
                }
            )
        }

        val resumable =
            BulkSyncExecutionPolicy
                .canExplicitlyResume(
                    session
                )

        val rollbackStartable =
            BulkSyncRollbackPolicy
                .canStartRollback(
                    session
                )

        val rollbackResumable =
            BulkSyncRollbackPolicy
                .canResumeRollback(
                    session
                )

        primaryButton.text =
            when {
                rollbackStartable ->
                    "Відкотити цю синхронізацію"

                rollbackResumable ->
                    "Продовжити відкат"

                session.state ==
                    BulkSyncSessionState.ROLLED_BACK ->
                    "Відкочено"

                session.state ==
                    BulkSyncSessionState.READY ->
                    "Почати синхронізацію"

                session.state == BulkSyncSessionState.PAUSED_CREATE_BATCH ->
                    "Продовжити наступний пакет"

                else ->
                    "Продовжити"
            }

        primaryButton.isEnabled =
            (
                resumable ||
                    rollbackStartable ||
                    rollbackResumable
            ) &&
                !running

        closeButton.isEnabled =
            !running

        UiChrome.styleAdaptiveActionButton(
            activity = this,
            button =
                primaryButton,
            tone =
                if (
                    rollbackStartable ||
                    rollbackResumable
                ) {
                    UiChrome.ActionTone.DANGER
                } else {
                    UiChrome.ActionTone.ACCENT
                }
        )

        UiChrome.styleAdaptiveActionButton(
            activity = this,
            button =
                closeButton,
            tone =
                UiChrome.ActionTone.NORMAL
        )
    }

    private fun rowView(
        session: BulkSyncSession,
        index: Int,
        row: BulkSyncSessionRow
    ): View {
        val appliedInserts =
            row.tracks.count {
                track ->
            val operationId =
                BulkSyncExecutionPolicy
                    .insertOperationId(
                        sessionId =
                            session.sessionId,
                        localPlaylistId =
                            row.localPlaylistId,
                        trackIndex =
                            track.trackIndex,
                        videoId =
                            track.videoId
                    )

            session.mutationLedger.any {
                it.operationId ==
                    operationId &&
                    it.status ==
                        BulkSyncMutationStatus
                            .APPLIED
            }
        }

        val terminalFailures =
            row.tracks.mapNotNull {
                track ->
            val operationId =
                BulkSyncExecutionPolicy
                    .insertOperationId(
                        sessionId =
                            session.sessionId,
                        localPlaylistId =
                            row.localPlaylistId,
                        trackIndex =
                            track.trackIndex,
                        videoId =
                            track.videoId
                    )

            session.mutationLedger
                .lastOrNull {
                    it.operationId ==
                        operationId &&
                        it.status ==
                            BulkSyncMutationStatus
                                .TERMINAL_FAILED
                }
                ?.let {
                    mutation ->
                    track to mutation
                }
        }

        val tone =
            rowTone(
                row.state
            )

        val card =
            BulkHierarchyChrome.card(
                activity = this,
                useAltSurface = true,
                accentTone = tone,
                radiusDp = 12
            )

        card.addView(
            BulkHierarchyChrome.title(
                activity = this,
                text =
                    (index + 1)
                        .toString() +
                        ". " +
                        row.playlistName
            )
        )

        card.addView(
            BulkHierarchyChrome.badge(
                activity = this,
                text =
                    rowStateLabel(
                        row.state
                    ),
                tone = tone
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin =
                    dp(7)
            }
        )

        if (
            row.originalPlanState.name ==
            "NEW"
        ) {
            card.addView(
                BulkHierarchyChrome.metrics(
                    activity = this,
                    text =
                        "Додано: " +
                            appliedInserts +
                            "/" +
                            row.tracks.size +
                            " треків"
                )
            )
        }

        if (
            terminalFailures.isNotEmpty()
        ) {
            card.addView(
                BulkHierarchyChrome.primary(
                    activity = this,
                    text =
                        "Не додано: " +
                            terminalFailures.size,
                    tone =
                        BulkHierarchyChrome
                            .Tone
                            .DANGER
                )
            )

            val details =
                buildString {
                    terminalFailures
                        .take(5)
                        .forEachIndexed {
                                failureIndex,
                                (track, mutation) ->
                            if (failureIndex > 0) {
                                append("\n")
                            }

                            append("• ")
                            append(
                                track.originalArtist
                                    .takeIf {
                                        it.isNotBlank()
                                    }
                                    ?.let {
                                        it + " — "
                                    }
                                    .orEmpty()
                            )
                            append(
                                track.originalTitle
                            )

                            mutation.error
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?.let {
                                    reason ->
                                    append(": ")
                                    append(reason)
                                }
                        }

                    if (
                        terminalFailures.size >
                        5
                    ) {
                        append("\n… ще ")
                        append(
                            terminalFailures.size -
                                5
                        )
                    }
                }

            card.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text =
                        "Деталі помилок:\n" +
                            details
                )
            )
        } else if (
            !row.lastError
                .isNullOrBlank()
        ) {
            card.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text =
                        "Деталі: " +
                            row.lastError
                )
            )
        }

        if (
            !row.remotePlaylistId
                .isNullOrBlank()
        ) {
            card.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text =
                        "YTM ID: " +
                            row.remotePlaylistId
                )
            )
        }

        return card
    }

    private fun sessionTone(
        state: BulkSyncSessionState
    ): BulkHierarchyChrome.Tone =
        when (state) {
            BulkSyncSessionState.PREVIEW ->
                BulkHierarchyChrome
                    .Tone
                    .NORMAL

            BulkSyncSessionState.READY,
            BulkSyncSessionState.RUNNING ->
                BulkHierarchyChrome
                    .Tone
                    .ACCENT

            BulkSyncSessionState.PAUSED_SEARCH_QUOTA,
            BulkSyncSessionState.PAUSED_WRITE_QUOTA,
            BulkSyncSessionState.PAUSED_RATE_LIMIT,
            BulkSyncSessionState.PAUSED_CREATE_BATCH,
            BulkSyncSessionState.PAUSED_AUTH,
            BulkSyncSessionState.PAUSED_INTERRUPTED,
            BulkSyncSessionState.ROLLING_BACK,
            BulkSyncSessionState.ROLLBACK_PAUSED ->
                BulkHierarchyChrome
                    .Tone
                    .WARNING

            BulkSyncSessionState.COMPLETED,
            BulkSyncSessionState.ROLLED_BACK ->
                BulkHierarchyChrome
                    .Tone
                    .SUCCESS

            BulkSyncSessionState.PARTIAL_FAILED ->
                BulkHierarchyChrome
                    .Tone
                    .DANGER
        }

    private fun rowTone(
        state: BulkSyncSessionRowState
    ): BulkHierarchyChrome.Tone =
        when (state) {
            BulkSyncSessionRowState.READY,
            BulkSyncSessionRowState.READY_APPEND,
            BulkSyncSessionRowState.CREATING,
            BulkSyncSessionRowState.INSERTING ->
                BulkHierarchyChrome
                    .Tone
                    .ACCENT

            BulkSyncSessionRowState.COMPLETED,
            BulkSyncSessionRowState.COMPLETED_NOOP ->
                BulkHierarchyChrome
                    .Tone
                    .SUCCESS

            BulkSyncSessionRowState.DEFERRED_LINKED,
            BulkSyncSessionRowState.NEEDS_SEARCH,
            BulkSyncSessionRowState.PENDING ->
                BulkHierarchyChrome
                    .Tone
                    .WARNING

            BulkSyncSessionRowState.BLOCKED,
            BulkSyncSessionRowState.PARTIAL_FAILED,
            BulkSyncSessionRowState.FAILED ->
                BulkHierarchyChrome
                    .Tone
                    .DANGER
        }

    private fun sessionStateLabel(
        state: BulkSyncSessionState
    ): String =
        when (state) {
            BulkSyncSessionState.PREVIEW ->
                "Попередній перегляд"

            BulkSyncSessionState.READY ->
                "Готово до запуску"

            BulkSyncSessionState.RUNNING ->
                "Виконується"

            BulkSyncSessionState.PAUSED_SEARCH_QUOTA ->
                "Пауза — ліміт пошуку"

            BulkSyncSessionState.PAUSED_WRITE_QUOTA ->
                "Пауза — квота запису"

            BulkSyncSessionState.PAUSED_RATE_LIMIT ->
                "Пауза — тимчасовий ліміт API"

            BulkSyncSessionState.PAUSED_CREATE_BATCH ->
                "Пакет створення завершено"

            BulkSyncSessionState.PAUSED_AUTH ->
                "Пауза — потрібна авторизація"

            BulkSyncSessionState.PAUSED_INTERRUPTED ->
                "Пауза — попередню синхронізацію перервано"

            BulkSyncSessionState.COMPLETED ->
                "Завершено"

            BulkSyncSessionState.ROLLING_BACK ->
                "Виконується відкат"

            BulkSyncSessionState.ROLLBACK_PAUSED ->
                "Відкат на паузі"

            BulkSyncSessionState.ROLLED_BACK ->
                "Відкочено"

            BulkSyncSessionState.PARTIAL_FAILED ->
                "Частково завершено з помилкою"
        }

    private fun rowStateLabel(
        state: BulkSyncSessionRowState
    ): String =
        when (state) {
            BulkSyncSessionRowState.READY ->
                "Готовий новий плейлист"

            BulkSyncSessionRowState.READY_APPEND ->
                "Готово до доповнення"

            BulkSyncSessionRowState.CREATING ->
                "Створення плейлиста…"

            BulkSyncSessionRowState.INSERTING ->
                "Додавання треків…"

            BulkSyncSessionRowState.COMPLETED ->
                "Завершено"

            BulkSyncSessionRowState.COMPLETED_NOOP ->
                "Уже синхронізовано — без змін"

            BulkSyncSessionRowState.DEFERRED_LINKED ->
                "Пов’язано з YTM — зараз не виконується"

            BulkSyncSessionRowState.NEEDS_SEARCH ->
                "Потрібен пошук — пропущено"

            BulkSyncSessionRowState.PENDING ->
                "У черзі — не дублюємо існуюче завдання"

            BulkSyncSessionRowState.BLOCKED ->
                "Заблоковано"

            BulkSyncSessionRowState.PARTIAL_FAILED ->
                "Завершено частково"

            BulkSyncSessionRowState.FAILED ->
                "Помилка"
        }

    private fun renderMissing() {
        statusText.text =
            "Сесію синхронізації не знайдено."

        summaryPanel.removeAllViews()
        summaryPanel.addView(
            BulkHierarchyChrome.title(
                activity = this,
                text = "Сесія синхронізації недоступна"
            )
        )
        summaryPanel.addView(
            BulkHierarchyChrome.secondary(
                activity = this,
                text =
                    "Створіть її через Меню → Синхронізувати всі."
            )
        )

        rowsContainer.removeAllViews()
        primaryButton.isEnabled =
            false
        closeButton.isEnabled =
            true
    }

    private fun showHelp() {
        if (
            helpDialog?.isShowing == true
        ) {
            return
        }

        helpDialogOpen = true

        helpDialog =
            UiChrome.showFixedFooterMessageDialog(
                activity = this,
                title =
                    BulkSyncHelpContent
                        .SESSION_TITLE,
                message =
                    BulkSyncHelpContent
                        .sessionMessage,
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label = "Зрозуміло",
                            tone =
                                UiChrome.ActionTone
                                    .ACCENT
                        ) {}
                    )
            ).also {
                dialog ->
                dialog.setOnDismissListener {
                    helpDialogOpen = false
                    helpDialog = null
                }
            }
    }

    private fun toast(
        message: String
    ) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
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

    companion object {
        private const val STATE_HELP_DIALOG_OPEN =
            "bulk_sync_session_help_dialog_open"

        private const val STATE_SESSION_MODAL =
            "bulk_sync_session_modal"

        private const val STATE_SCROLL_POSITION =
            "bulk_sync_session_scroll_position"

        const val EXTRA_SESSION_ID =
            "bulk_sync_session_id"
    }
}
