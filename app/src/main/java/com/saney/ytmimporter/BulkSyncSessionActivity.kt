package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
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
import com.saney.ytmimporter.ui.RestorableModalController
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors

class BulkSyncSessionActivity : Activity() {
    private enum class SessionModal {
        ROLLBACK_CONFIRM
    }

    private val worker =
        Executors.newSingleThreadExecutor()

    private lateinit var sessionStore:
        BulkSyncSessionStore

    private lateinit var statusText:
        TextView

    private lateinit var summaryText:
        TextView

    private lateinit var rowsContainer:
        LinearLayout

    private lateinit var primaryButton:
        Button

    private lateinit var closeButton:
        Button

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
                QuotaTracker(this)
        )
    }

    private var sessionId:
        String? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

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

        super.onSaveInstanceState(
            outState
        )
    }

    override fun onResume() {
        super.onResume()

        if (!running) {
            sessionId
                ?.let(
                    sessionStore::get
                )
                ?.let(::render)
        }
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
                "Дочекайтеся завершення поточної mutation/відкату або закрийте застосунок."
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
                        "Bulk-сесія зараз виконує mutation."
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
                label = "Bulk-сесія"
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

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
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

        summaryText =
            TextView(this).apply {
                textSize = 14f
                setTextColor(
                    palette.text
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
                                this@BulkSyncSessionActivity,
                            fill =
                                palette.surface,
                            radiusDp = 14,
                            accentStroke = true
                        )
            }

        content.addView(
            summaryText,
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
                        "Відкат використовує тільки exact IDs з ledger цієї Bulk-сесії.\n\n" +
                            "Буде відкочено: додані елементи — " +
                            inserts +
                            ", створені цією сесією плейлисти — " +
                            creates +
                            ".\n\n" +
                            "Попередньо існуючі плейлисти/елементи не видаляються за назвою. " +
                            "Після старту відкат не продовжується автоматично після restart.",
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
            toast(
                exactnessError
            )
            render(session)
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
                        error.message
                            ?: "Помилка відкату Bulk-сесії"
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
                            "Bulk-сесію не знайдено"
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
                        error.message
                            ?: "Помилка Bulk-сесії"
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

        summaryText.text =
            buildString {
                append(
                    "Плейлистів у плані: "
                )
                append(
                    session.plan.size
                )
                append("\n")
                append(
                    "Створено плейлистів: "
                )
                append(
                    appliedCreates
                )
                append(
                    " • додано треків: "
                )
                append(
                    appliedInserts
                )
                append("\n")
                append(
                    "Checkpoint: "
                )
                append(
                    session.checkpointId
                        .take(8)
                )
                append(
                    "…"
                )
                append("\n")
                append(
                    "Remote baseline: "
                )
                append(
                    session.remoteBaseline
                        .playlists
                        .size
                )
                append(
                    " linked playlist(s)"
                )

                if (prepared > 0) {
                    append("\n")
                    append(
                        "⚠ PREPARED без підтвердження: "
                    )
                    append(prepared)
                }

                if (terminalFailed > 0) {
                    append("\n")
                    append(
                        "Не додано треків: "
                    )
                    append(
                        terminalFailed
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
                    append("\n")
                    append(
                        "Відкочено mutations: "
                    )
                    append(
                        rolledBack
                    )
                    append(
                        " • залишилось: "
                    )
                    append(
                        rollbackRemaining
                    )
                }

                if (
                    !session.lastError
                        .isNullOrBlank()
                ) {
                    append("\n\n")
                    append(
                        session.lastError
                    )
                }

                append("\n\n")
                append(
                    "Restart ніколи не продовжує сесію автоматично. " +
                        "Wave 3 виконує тільки нові локальні плейлисти; " +
                        "пов’язані LINKED лишаються відкладеними до append-safe wave."
                )
            }

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
    }

    private fun rowView(
        session: BulkSyncSession,
        index: Int,
        row: BulkSyncSessionRow
    ): TextView {
        val palette =
            AppThemeManager.palette(this)

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

        return TextView(this).apply {
            text =
                buildString {
                    append(
                        index + 1
                    )
                    append(
                        ". "
                    )
                    append(
                        row.playlistName
                    )
                    append("\n")
                    append(
                        rowStateLabel(
                            row.state
                        )
                    )

                    if (
                        row.originalPlanState.name ==
                        "NEW"
                    ) {
                        append(
                            " • "
                        )
                        append(
                            appliedInserts
                        )
                        append(
                            "/"
                        )
                        append(
                            row.tracks.size
                        )
                    }

                    if (
                        !row.remotePlaylistId
                            .isNullOrBlank()
                    ) {
                        append("\nYTM ID: ")
                        append(
                            row.remotePlaylistId
                        )
                    }

                    if (
                        terminalFailures.isNotEmpty()
                    ) {
                        append("\nНе додано: ")
                        append(
                            terminalFailures.size
                        )

                        terminalFailures
                            .take(5)
                            .forEach {
                                    (track, mutation) ->
                                append("\n• ")
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
                            terminalFailures.size > 5
                        ) {
                            append("\n… ще ")
                            append(
                                terminalFailures.size - 5
                            )
                        }
                    } else if (
                        !row.lastError
                            .isNullOrBlank()
                    ) {
                        append("\n")
                        append(
                            row.lastError
                        )
                    }
                }

            textSize = 13.5f
            setTextColor(
                palette.text
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
                            this@BulkSyncSessionActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )
        }
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
                "Пауза — Search quota"

            BulkSyncSessionState.PAUSED_WRITE_QUOTA ->
                "Пауза — write quota"

            BulkSyncSessionState.PAUSED_RATE_LIMIT ->
                "Пауза — rate/resource limit"

            BulkSyncSessionState.PAUSED_AUTH ->
                "Пауза — потрібна авторизація"

            BulkSyncSessionState.PAUSED_INTERRUPTED ->
                "Пауза — попередній запуск перервано"

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
                "Готове add-only доповнення"

            BulkSyncSessionRowState.CREATING ->
                "Створення плейлиста…"

            BulkSyncSessionRowState.INSERTING ->
                "Додавання треків…"

            BulkSyncSessionRowState.COMPLETED ->
                "Завершено"

            BulkSyncSessionRowState.COMPLETED_NOOP ->
                "Уже синхронізовано — без змін"

            BulkSyncSessionRowState.DEFERRED_LINKED ->
                "Пов’язано з YTM — add-only відкладено до наступної хвилі"

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
            "Bulk-сесію не знайдено."
        summaryText.text =
            "Створіть її через Меню → Синхронізувати всі."
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

        const val EXTRA_SESSION_ID =
            "bulk_sync_session_id"
    }
}
