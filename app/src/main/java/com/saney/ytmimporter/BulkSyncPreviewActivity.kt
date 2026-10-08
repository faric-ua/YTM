package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.bulk.BulkSyncBaselineItem
import com.saney.ytmimporter.bulk.BulkSyncBaselinePlaylist
import com.saney.ytmimporter.bulk.BulkSyncHelpContent
import com.saney.ytmimporter.bulk.BulkSyncLocalPlaylist
import com.saney.ytmimporter.bulk.BulkSyncPlanRow
import com.saney.ytmimporter.bulk.BulkSyncPlanState
import com.saney.ytmimporter.bulk.BulkSyncPlanSummary
import com.saney.ytmimporter.bulk.BulkSyncPreflightPolicy
import com.saney.ytmimporter.bulk.BulkSyncRemoteBaseline
import com.saney.ytmimporter.bulk.BulkSyncRemoteSnapshot
import com.saney.ytmimporter.bulk.BulkSyncSelectionPolicy
import com.saney.ytmimporter.bulk.BulkSessionPreparationCoordinator
import com.saney.ytmimporter.bulk.BulkSyncSessionFactory
import com.saney.ytmimporter.search.SearchCoordinator
import com.saney.ytmimporter.storage.BulkSyncCheckpointStore
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.storage.LocalBackupManager
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.BulkHierarchyChrome
import com.saney.ytmimporter.ui.RestorableModalController
import com.saney.ytmimporter.ui.ScrollPositionState
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors

/**
 * Read-only Bulk Sync preflight.
 *
 * Test 3 foundation intentionally owns no remote mutation path. It may read
 * remote playlist inventory/items and local Search cache state, then renders a
 * deterministic plan. Execution is a later wave and requires a separate,
 * explicit confirmation contract.
 */
class BulkSyncPreviewActivity : Activity() {
    private enum class PreviewModal {
        ACTIVE_SESSION,
        CREATE_SESSION
    }

    private val executor =
        Executors.newSingleThreadExecutor()

    private val api by lazy {
        YouTubeApi(
            accessTokenRecovery =
                GoogleAccessTokenRecovery(this)
        )
    }

    private lateinit var restorableStore:
        RestorablePlaylistStore
    private lateinit var pendingJobStore:
        PendingJobStore
    private lateinit var quotaTracker:
        QuotaTracker
    private lateinit var searchCoordinator:
        SearchCoordinator

    private lateinit var statusText:
        TextView
    private lateinit var summaryPanel:
        LinearLayout
    private lateinit var rowsContainer:
        LinearLayout
    private lateinit var confirmationButton:
        Button
    private lateinit var loadingPanel:
        LinearLayout
    private lateinit var loadingLabel:
        TextView
    private lateinit var scrollView:
        ScrollView

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    private var plan:
        BulkSyncPlanSummary? = null
    private var remoteReadUnitsUsed =
        0
    private var loading =
        false

    private val includedExecutableIds =
        linkedSetOf<String>()
    private var selectionInitialized =
        false

    private var helpDialogOpen =
        false

    private var helpDialog:
        Dialog? = null

    private lateinit var previewModalController:
        RestorableModalController

    private var preparationDialog: Dialog? = null
    private var preparationSteps: TextView? = null
    private var preparationFailureDialog: Dialog? = null

    private val preparationObserver:
        (BulkSessionPreparationCoordinator.Status) -> Unit =
        { status -> renderPreparationStatus(status) }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        restorableStore =
            RestorablePlaylistStore(this)
        pendingJobStore =
            PendingJobStore(this)
        quotaTracker =
            QuotaTracker(this)

        searchCoordinator =
            SearchCoordinator(
                api = api,
                searchCache =
                    SearchCache(this),
                quotaTracker =
                    quotaTracker
            )

        helpDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_HELP_DIALOG_OPEN,
                    false
                )
                ?: false

        selectionInitialized =
            savedInstanceState
                ?.getBoolean(
                    STATE_SELECTION_INITIALIZED,
                    false
                )
                ?: false

        if (selectionInitialized) {
            includedExecutableIds.addAll(
                savedInstanceState
                    ?.getStringArrayList(
                        STATE_INCLUDED_EXECUTABLE_IDS
                    )
                    .orEmpty()
            )
        }

        previewModalController =
            RestorableModalController(
                activity = this,
                stateKey =
                    STATE_PREVIEW_MODAL
            )
        previewModalController.restore(
            savedInstanceState
        )
        scrollPosition.restore(
            savedInstanceState
        )

        buildUi()

        @Suppress("DEPRECATION")
        val restoredPlan =
            savedInstanceState
                ?.getSerializable(
                    STATE_PLAN
                ) as?
                BulkSyncPlanSummary

        remoteReadUnitsUsed =
            savedInstanceState
                ?.getInt(
                    STATE_REMOTE_READ_UNITS,
                    0
                )
                ?: 0

        if (restoredPlan != null) {
            plan = restoredPlan
            renderPlan(restoredPlan)
        } else {
            loadPreview()
        }

        previewModalController
            .restoreAfterContentReady(
                renderer =
                    ::renderPreviewModal
            )

        if (helpDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showHelp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Subscribe to the process-scoped single-flight operation.
        // A finished session must not leave a stale preparing subtitle.
        BulkSessionPreparationCoordinator.observe(preparationObserver)
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        plan?.let {
            outState.putSerializable(
                STATE_PLAN,
                it
            )
        }

        outState.putInt(
            STATE_REMOTE_READ_UNITS,
            remoteReadUnitsUsed
        )

        outState.putBoolean(
            STATE_HELP_DIALOG_OPEN,
            helpDialogOpen
        )

        outState.putBoolean(
            STATE_SELECTION_INITIALIZED,
            selectionInitialized
        )

        outState.putStringArrayList(
            STATE_INCLUDED_EXECUTABLE_IDS,
            ArrayList(
                includedExecutableIds
            )
        )

        previewModalController.save(
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

    override fun onPause() {
        BulkSessionPreparationCoordinator.detach(preparationObserver)
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }
        super.onPause()
    }

    override fun onDestroy() {
        preparationDialog?.dismiss()
        preparationDialog = null
        preparationSteps = null
        preparationFailureDialog?.dismiss()
        preparationFailureDialog = null
        helpDialog
            ?.setOnDismissListener(null)
        helpDialog = null
        previewModalController.onDestroy()
        executor.shutdownNow()
        super.onDestroy()
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
                finish()
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        topBar.addView(
            UiChrome.emphasizedTitle(
                activity = this,
                label =
                    "Синхронізувати всі"
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
                            this@BulkSyncPreviewActivity
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

        loadingPanel =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                visibility =
                    View.GONE
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(12)
                )
            }

        loadingLabel =
            TextView(this).apply {
                textSize = 13f
                setTextColor(
                    palette.muted
                )
                setPadding(
                    0,
                    0,
                    0,
                    dp(6)
                )
            }

        loadingPanel.addView(
            loadingLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        loadingPanel.addView(
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                isIndeterminate = true
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(6)
            )
        )

        root.addView(loadingPanel)

        scrollView =
            ScrollView(this).apply {
                isFillViewport = true
            }

        val scroll = scrollView

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

        content.addView(rowsContainer)
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

        confirmationButton =
            Button(this).apply {
                text =
                    "Створити сесію синхронізації"
                isAllCaps = false
                textSize = 15f
                isEnabled = false
                setOnClickListener {
                    confirmPlan()
                }
            }

        val cancelButton =
            Button(this).apply {
                text = "Скасувати"
                isAllCaps = false
                textSize = 15f
                setOnClickListener {
                    finish()
                }
            }

        UiChrome.addAdaptiveActionButtons(
            activity = this,
            container = actions,
            buttons =
                listOf(
                    confirmationButton,
                    cancelButton
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

    private fun loadPreview() {
        if (loading) {
            return
        }

        loading = true
        confirmationButton.isEnabled =
            false
        statusText.text =
            "Будую read-only preview. Віддалені зміни не виконуються."
        summaryPanel.removeAllViews()
        summaryPanel.addView(
            BulkHierarchyChrome.secondary(
                activity = this,
                text =
                    "Аналіз локальних плейлистів і remote snapshot…"
            )
        )
        showLoading(
            "Аналізую локальні плейлисти та remote snapshot…"
        )
        rowsContainer.removeAllViews()

        val snapshots =
            restorableStore.getAll()
        val pendingJobs =
            pendingJobStore.getAll()
        val auth =
            AuthSessionStore.current()
        val token =
            auth.accessToken
                ?.takeIf {
                    it.isNotBlank()
                }

        executor.execute {
            var readUnits = 0

            val locals =
                snapshots.map {
                        snapshot ->
                    val searchPlan =
                        searchCoordinator.plan(
                            playlist =
                                snapshot.playlist,
                            preserveExistingExact =
                                true
                        )

                    BulkSyncLocalPlaylist(
                        localPlaylistId =
                            snapshot.localPlaylistId,
                        playlistName =
                            snapshot.playlist.name,
                        sourceLabel =
                            snapshot.sourceLabel,
                        tracks =
                            snapshot.playlist.tracks
                                .toList(),
                        destinationPlaylistId =
                            snapshot
                                .destinationPlaylistId,
                        estimatedSearchCalls =
                            searchPlan.apiNeeded,
                        cacheHits =
                            searchPlan.cachedCount
                    )
                }

            val remote =
                if (token == null) {
                    BulkSyncRemoteSnapshot(
                        inventoryAvailable =
                            false,
                        ownedPlaylistIds =
                            emptySet(),
                        orderedVideoIdsByPlaylistId =
                            emptyMap()
                    )
                } else {
                    try {
                        val ownedPlaylists =
                            api.listMyPlaylists(
                                accessToken =
                                    token
                            ) {
                                readUnits +=
                                    QuotaTracker
                                        .SIMPLE_LIST_COST
                            }

                        val ownedIds =
                            ownedPlaylists
                                .map {
                                    it.id
                                }
                                .toSet()

                        val ordered =
                            linkedMapOf<
                                String,
                                List<String>
                            >()

                        val errors =
                            linkedMapOf<
                                String,
                                String
                            >()

                        locals
                            .mapNotNull {
                                it.destinationPlaylistId
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                            }
                            .distinct()
                            .filter {
                                it in ownedIds
                            }
                            .forEach {
                                    playlistId ->
                                try {
                                    val snapshot =
                                        api
                                            .listPlaylistSnapshotItems(
                                                accessToken =
                                                    token,
                                                playlistId =
                                                    playlistId
                                            ) {
                                                readUnits +=
                                                    QuotaTracker
                                                        .SIMPLE_LIST_COST
                                            }

                                    ordered[
                                        playlistId
                                    ] =
                                        snapshot.items
                                            .mapNotNull {
                                                it.videoId
                                                    ?.takeIf(
                                                        String::isNotBlank
                                                    )
                                            }
                                } catch (
                                    error: Exception
                                ) {
                                    errors[
                                        playlistId
                                    ] =
                                        "Remote snapshot недоступний: " +
                                            safeError(
                                                error
                                            )
                                }
                            }

                        BulkSyncRemoteSnapshot(
                            inventoryAvailable =
                                true,
                            ownedPlaylistIds =
                                ownedIds,
                            orderedVideoIdsByPlaylistId =
                                ordered,
                            errorsByPlaylistId =
                                errors
                        )
                    } catch (
                        error: Exception
                    ) {
                        BulkSyncRemoteSnapshot(
                            inventoryAvailable =
                                false,
                            ownedPlaylistIds =
                                emptySet(),
                            orderedVideoIdsByPlaylistId =
                                emptyMap()
                        )
                    }
                }

            val summary =
                BulkSyncPreflightPolicy
                    .build(
                        localPlaylists =
                            locals,
                        pendingJobs =
                            pendingJobs,
                        connected =
                            token != null,
                        remote =
                            remote
                    )

            if (readUnits > 0) {
                quotaTracker
                    .recordGeneralUnits(
                        readUnits
                    )
            }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                loading = false
                remoteReadUnitsUsed =
                    readUnits
                plan = summary
                renderPlan(summary)
            }
        }
    }

    private fun renderPlan(
        summary: BulkSyncPlanSummary
    ) {
        ensureSelection(
            summary
        )
        hideLoading()

        statusText.text =
            "План готовий. У YouTube Music нічого не змінено."

        renderSelectionSummary(
            summary
        )

        rowsContainer.removeAllViews()

        if (summary.rows.isEmpty()) {
            rowsContainer.addView(
                planText(
                    "Немає локальних плейлистів для синхронізації."
                )
            )
            return
        }

        summary.rows.forEach {
                row ->
            rowsContainer.addView(
                planRow(
                    summary = summary,
                    row = row
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
    }

    private fun renderSelectionSummary(
        summary: BulkSyncPlanSummary
    ) {
        val selected =
            selectedPlanSummary(
                summary
            )

        val totalExecutable =
            BulkSyncSelectionPolicy
                .defaultIncludedIds(
                    summary
                )
                .size

        val selectedExecutable =
            BulkSyncSelectionPolicy
                .selectedExecutableCount(
                    summary = summary,
                    includedIds =
                        includedExecutableIds
                )

        val excludedExecutable =
            totalExecutable -
                selectedExecutable

        val stateCounts =
            BulkSyncPlanState
                .values()
                .mapNotNull {
                    state ->
                    val count =
                        summary.count(
                            state
                        )

                    if (count > 0) {
                        planStateLabel(
                            state
                        ) +
                            " " +
                            count
                    } else {
                        null
                    }
                }
                .joinToString(
                    " • "
                )

        summaryPanel.removeAllViews()

        summaryPanel.addView(
            BulkHierarchyChrome.title(
                activity = this,
                text = "План синхронізації"
            )
        )

        summaryPanel.addView(
            BulkHierarchyChrome.metrics(
                activity = this,
                text =
                    "Плейлистів: " +
                        summary.rows.size +
                        " • до сесії: " +
                        selectedExecutable +
                        " • виключено: " +
                        excludedExecutable
            )
        )

        if (stateCounts.isNotBlank()) {
            summaryPanel.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text =
                        "Стани: " +
                            stateCounts
                )
            )
        }

        summaryPanel.addView(
            BulkHierarchyChrome.primary(
                activity = this,
                text =
                    "Заплановано: нових " +
                        selected.count(
                            BulkSyncPlanState.NEW
                        ) +
                        " • доповнити " +
                        selected.count(
                            BulkSyncPlanState.LINKED
                        ),
                tone =
                    if (selectedExecutable > 0) {
                        BulkHierarchyChrome
                            .Tone
                            .ACCENT
                    } else {
                        BulkHierarchyChrome
                            .Tone
                            .NORMAL
                    }
            )
        )

        summaryPanel.addView(
            BulkHierarchyChrome.metrics(
                activity = this,
                text =
                    "Орієнтовний запис API: " +
                        selected
                            .estimatedWriteUnits +
                        " од."
            )
        )

        summaryPanel.addView(
            BulkHierarchyChrome.secondary(
                activity = this,
                text =
                    "Діагностика: Пошук " +
                        summary
                            .estimatedSearchCalls +
                        " • перевірка YTM " +
                        remoteReadUnitsUsed +
                        " од. API"
            )
        )

        summaryPanel.addView(
            BulkHierarchyChrome.secondary(
                activity = this,
                text =
                    "До сесії потраплять лише позначені нові або пов’язані плейлисти. " +
                        "Пошук, черга та заблоковані рядки не виконуються. " +
                        "Запис у YouTube Music почнеться тільки після окремого підтвердження " +
                        "на екрані сесії."
            )
        )

        confirmationButton.isEnabled =
            !loading &&
                selectedExecutable >
                0
    }

    private fun previewTone(
        state: BulkSyncPlanState
    ): BulkHierarchyChrome.Tone =
        when (state) {
            BulkSyncPlanState.NEW,
            BulkSyncPlanState.LINKED ->
                BulkHierarchyChrome
                    .Tone
                    .ACCENT

            BulkSyncPlanState.ALREADY_SYNCED ->
                BulkHierarchyChrome
                    .Tone
                    .SUCCESS

            BulkSyncPlanState.NEEDS_SEARCH,
            BulkSyncPlanState.PENDING ->
                BulkHierarchyChrome
                    .Tone
                    .WARNING

            BulkSyncPlanState.BLOCKED ->
                BulkHierarchyChrome
                    .Tone
                    .DANGER
        }

    private fun planStateLabel(
        state: BulkSyncPlanState
    ): String =
        when (state) {
            BulkSyncPlanState.NEW ->
                "Новий"

            BulkSyncPlanState.LINKED ->
                "Пов’язано з YTM"

            BulkSyncPlanState.ALREADY_SYNCED ->
                "Уже синхронізовано"

            BulkSyncPlanState.NEEDS_SEARCH ->
                "Потрібен пошук"

            BulkSyncPlanState.PENDING ->
                "У черзі"

            BulkSyncPlanState.BLOCKED ->
                "Заблоковано"
        }

    private fun ensureSelection(
        summary: BulkSyncPlanSummary
    ) {
        if (!selectionInitialized) {
            includedExecutableIds.clear()
            includedExecutableIds.addAll(
                BulkSyncSelectionPolicy
                    .defaultIncludedIds(
                        summary
                    )
            )
            selectionInitialized = true
            return
        }

        val sanitized =
            BulkSyncSelectionPolicy
                .sanitizeIncludedIds(
                    summary = summary,
                    includedIds =
                        includedExecutableIds
                )

        includedExecutableIds.clear()
        includedExecutableIds.addAll(
            sanitized
        )
    }

    private fun selectedPlanSummary(
        summary: BulkSyncPlanSummary
    ): BulkSyncPlanSummary =
        BulkSyncSelectionPolicy
            .selectedSummary(
                summary = summary,
                includedIds =
                    includedExecutableIds
            )

    private fun setRowIncluded(
        summary: BulkSyncPlanSummary,
        row: BulkSyncPlanRow,
        included: Boolean
    ) {
        if (
            !BulkSyncSelectionPolicy
                .isExecutable(
                    row
                )
        ) {
            return
        }

        if (included) {
            includedExecutableIds.add(
                row.localPlaylistId
            )
        } else {
            includedExecutableIds.remove(
                row.localPlaylistId
            )
        }

        renderSelectionSummary(
            summary
        )
    }

    private fun planRow(
        summary: BulkSyncPlanSummary,
        row: BulkSyncPlanRow
    ): View {
        val tone =
            previewTone(
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
                text = row.playlistName
            )
        )

        card.addView(
            BulkHierarchyChrome.badge(
                activity = this,
                text =
                    planStateLabel(
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
            BulkSyncSelectionPolicy
                .isExecutable(
                    row
                )
        ) {
            val included =
                row.localPlaylistId in
                    includedExecutableIds

            val selector =
                CheckBox(this).apply {
                    isChecked =
                        included
                    text =
                        if (included) {
                            "Буде синхронізовано"
                        } else {
                            "Не синхронізувати"
                        }
                    textSize = 13f
                    setTextColor(
                        AppThemeManager
                            .palette(
                                this@BulkSyncPreviewActivity
                            )
                            .text
                    )
                    setPadding(
                        0,
                        dp(5),
                        0,
                        0
                    )
                    setOnCheckedChangeListener {
                            button,
                            checked ->
                        button.text =
                            if (checked) {
                                "Буде синхронізовано"
                            } else {
                                "Не синхронізувати"
                            }

                        setRowIncluded(
                            summary = summary,
                            row = row,
                            included = checked
                        )
                    }
                }

            card.addView(
                selector,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        card.addView(
            BulkHierarchyChrome.metrics(
                activity = this,
                text =
                    "Треки: " +
                        row.trackCount +
                        " • готово: " +
                        row.selectedCount +
                        " • не готово: " +
                        row.unresolvedCount
            )
        )

        if (
            row.plannedCreate ||
            row.plannedInsertCount >
            0
        ) {
            val planned =
                buildString {
                    if (
                        row.plannedCreate
                    ) {
                        append(
                            "створити плейлист"
                        )
                    }

                    if (
                        row.plannedInsertCount >
                        0
                    ) {
                        if (isNotEmpty()) {
                            append(
                                " • "
                            )
                        }

                        append(
                            "додати "
                        )
                        append(
                            row
                                .plannedInsertCount
                        )
                        append(
                            " треків"
                        )
                    }
                }

            card.addView(
                BulkHierarchyChrome.primary(
                    activity = this,
                    text =
                        "Буде виконано: " +
                            planned,
                    tone =
                        BulkHierarchyChrome
                            .Tone
                            .ACCENT
                )
            )
        }

        card.addView(
            BulkHierarchyChrome.body(
                activity = this,
                text =
                    "Причина: " +
                        row.reason
            )
        )

        val diagnostics =
            buildList {
                if (
                    row.estimatedSearchCalls >
                    0 ||
                    row.cacheHits >
                    0
                ) {
                    add(
                        "Пошук " +
                            row.estimatedSearchCalls +
                            " • cache " +
                            row.cacheHits
                    )
                }

                if (
                    row.estimatedWriteUnits >
                    0
                ) {
                    add(
                        "запис API " +
                            row.estimatedWriteUnits +
                            " од."
                    )
                }
            }

        if (diagnostics.isNotEmpty()) {
            card.addView(
                BulkHierarchyChrome.secondary(
                    activity = this,
                    text =
                        "Діагностика: " +
                            diagnostics.joinToString(
                                " • "
                            )
                )
            )
        }

        return card
    }

    private fun confirmPlan() {
        val rawSummary =
            plan
                ?: return

        val summary =
            selectedPlanSummary(
                rawSummary
            )

        val sessionStore =
            BulkSyncSessionStore(this)

        val existing =
            sessionStore.active()

        if (
            existing != null &&
            !existing.isTerminal
        ) {
            previewModalController.show(
                modalId =
                    PreviewModal
                        .ACTIVE_SESSION
                        .name,
                args =
                    Bundle().apply {
                        putString(
                            ARG_SESSION_ID,
                            existing.sessionId
                        )
                    },
                renderer =
                    ::renderPreviewModal
            )
            return
        }

        val newCount =
            summary.count(
                BulkSyncPlanState.NEW
            )

        val linkedCount =
            summary.count(
                BulkSyncPlanState.LINKED
            )

        if (
            newCount +
                linkedCount <=
            0
        ) {
            toast(
                "Немає вибраних плейлистів, які можна синхронізувати."
            )
            return
        }

        previewModalController.show(
            modalId =
                PreviewModal
                    .CREATE_SESSION
                    .name,
            renderer =
                ::renderPreviewModal
        )
    }

    private fun renderPreviewModal(
        modalId: String,
        args: Bundle
    ): Dialog? {
        val modal =
            PreviewModal
                .values()
                .firstOrNull {
                    it.name ==
                        modalId
                }
                ?: return null

        return when (modal) {
            PreviewModal.ACTIVE_SESSION -> {
                val sessionId =
                    args.getString(
                        ARG_SESSION_ID
                    )
                        ?.takeIf(
                            String::isNotBlank
                        )
                        ?: return null

                UiChrome.alertBuilder(this)
                    .setTitle(
                        "Є незавершена синхронізація"
                    )
                    .setMessage(
                        "Спочатку відкрийте вже створену сесію. " +
                            "Нова сесія не буде створена поверх незавершеної."
                    )
                    .setNegativeButton(
                        "Скасувати"
                    ) { _, _ ->
                        previewModalController
                            .clearState()
                    }
                    .setPositiveButton(
                        "Відкрити"
                    ) { _, _ ->
                        previewModalController
                            .clearState()
                        openSession(
                            sessionId
                        )
                    }
                    .show()
            }

            PreviewModal.CREATE_SESSION -> {
                val rawSummary =
                    plan
                        ?: return null

                val summary =
                    selectedPlanSummary(
                        rawSummary
                    )

                val newCount =
                    summary.count(
                        BulkSyncPlanState.NEW
                    )

                val linkedCount =
                    summary.count(
                        BulkSyncPlanState.LINKED
                    )

                if (
                    newCount +
                        linkedCount <=
                    0
                ) {
                    return null
                }

                UiChrome.alertBuilder(this)
                    .setTitle(
                        "Створити сесію синхронізації?"
                    )
                    .setMessage(
                        "Перед початком буде створено локальну контрольну копію і перевірено " +
                            "поточний стан плейлистів у YouTube Music.\n\n" +
                            "Нові плейлисти: " +
                            newCount +
                            " • пов’язані для доповнення: " +
                            linkedCount +
                            ". Нові плейлисти в YTM будуть приватними.\n\n" +
                            "Пов’язані плейлисти лише доповнюються: існуючі remote елементи " +
                            "не видаляються й не переставляються.\n\n" +
                            "Пошук, черга та заблоковані рядки не виконуються. " +
                            "Після створення сесії запис у YTM не почнеться автоматично."
                    )
                    .setNegativeButton(
                        "Скасувати"
                    ) { _, _ ->
                        previewModalController
                            .clearState()
                    }
                    .setPositiveButton(
                        "Створити сесію"
                    ) { _, _ ->
                        previewModalController
                            .clearState()
                        prepareSession(
                            summary
                        )
                    }
                    .show()
            }
        }
    }

    private fun prepareSession(
        summary: BulkSyncPlanSummary
    ) {
        if (loading ||
            BulkSessionPreparationCoordinator.current() is
                BulkSessionPreparationCoordinator.Status.Preparing
        ) {
            return
        }

        val auth = AuthSessionStore.current()
        val token =
            auth.accessToken?.takeIf(String::isNotBlank)
                ?: return toast(
                    "Підключіть Google / YTM перед підготовкою сесії."
                )

        if (
            BulkSessionPreparationCoordinator.start(
                context = applicationContext,
                summary = summary,
                accessToken = token,
                auth = auth
            )
        ) {
            loading = true
            confirmationButton.isEnabled = false
            renderPreparationStatus(
                BulkSessionPreparationCoordinator.current()
            )
        }
    }

    private fun renderPreparationStatus(
        status: BulkSessionPreparationCoordinator.Status
    ) {
        if (isFinishing || isDestroyed ||
            !::confirmationButton.isInitialized
        ) return

        when (status) {
            is BulkSessionPreparationCoordinator.Status.Preparing -> {
                loading = true
                confirmationButton.isEnabled = false
                statusText.text = "Підготовка сесії синхронізації…"
                // Preview-only in-page loading must never overlap preparation.
                hideLoading()
                showPreparationDialog(status.step)
            }

            is BulkSessionPreparationCoordinator.Status.Ready -> {
                closePreparationDialog()
                loading = false
                plan?.let(::renderPlan)
                if (
                    BulkSessionPreparationCoordinator
                        .consumeReadyNavigation(status.sessionId)
                ) {
                    openSession(status.sessionId)
                }
            }

            is BulkSessionPreparationCoordinator.Status.Failed -> {
                closePreparationDialog()
                loading = false
                plan?.let(::renderPlan)
                statusText.text =
                    "Підготовку не завершено. Запис у YouTube Music не починався."
                showPreparationFailure(status.message)
            }

            BulkSessionPreparationCoordinator.Status.Idle -> {
                closePreparationDialog()
                // Do not interfere with a separately running preview read.
                if (plan != null && !loading) {
                    plan?.let(::renderPlan)
                }
            }
        }
    }

    private fun showPreparationDialog(
        currentStep: BulkSessionPreparationCoordinator.Step
    ) {
        if (preparationDialog?.isShowing != true) {
            val palette = AppThemeManager.palette(this)
            val content =
                LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(20), dp(14), dp(20), dp(16))
                }
            content.addView(
                ProgressBar(this).apply {
                    isIndeterminate = true
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(28)
                )
            )
            preparationSteps =
                TextView(this).apply {
                    textSize = 15f
                    setTextColor(palette.text)
                    setPadding(0, dp(16), 0, dp(4))
                }
            content.addView(preparationSteps)
            preparationDialog =
                UiChrome.alertBuilder(this)
                    .setTitle("Підготовка Bulk-сесії")
                    .setView(content)
                    .show()
                    .also { dialog ->
                        dialog.setCancelable(false)
                        dialog.setCanceledOnTouchOutside(false)
                    }
        }

        preparationSteps?.text =
            BulkSessionPreparationCoordinator.Step.values()
                .mapIndexed { index, step ->
                    when {
                        index < currentStep.ordinal ->
                            "✓ ${step.label}"
                        index == currentStep.ordinal ->
                            "● ${step.label}"
                        else ->
                            "○ ${step.label}"
                    }
                }
                .joinToString("\n")
    }

    private fun closePreparationDialog() {
        preparationDialog?.dismiss()
        preparationDialog = null
        preparationSteps = null
    }

    private fun showPreparationFailure(message: String) {
        if (preparationFailureDialog?.isShowing == true) return

        preparationFailureDialog =
            UiChrome.alertBuilder(this)
                .setTitle("Не вдалося підготувати сесію")
                .setMessage(
                    "Запис у YouTube Music не починався.\n\n" +
                        "Причина: $message"
                )
                .setNegativeButton("Назад") { _, _ ->
                    BulkSessionPreparationCoordinator.clearFailure()
                    preparationFailureDialog = null
                    plan?.let(::renderPlan)
                }
                .setPositiveButton("Повторити") { _, _ ->
                    BulkSessionPreparationCoordinator.clearFailure()
                    preparationFailureDialog = null
                    plan?.let {
                        prepareSession(selectedPlanSummary(it))
                    }
                }
                .show()
    }

    private fun openSession(
        sessionId: String
    ) {
        startActivity(
            Intent(
                this,
                BulkSyncSessionActivity::class.java
            ).putExtra(
                BulkSyncSessionActivity
                    .EXTRA_SESSION_ID,
                sessionId
            )
        )
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
                        .PREVIEW_TITLE,
                message =
                    BulkSyncHelpContent
                        .previewMessage,
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

    private fun showLoading(
        message: String
    ) {
        loadingLabel.text =
            message
        loadingPanel.visibility =
            View.VISIBLE
    }

    private fun hideLoading() {
        loadingPanel.visibility =
            View.GONE
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

    private fun planText(
        value: String
    ): TextView {
        val palette =
            AppThemeManager.palette(this)

        return TextView(this).apply {
            text = value
            textSize = 13.5f
            setTextColor(
                palette.muted
            )
            setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
            )
        }
    }

    private fun safeError(
        error: Throwable
    ): String =
        error.message
            ?.trim()
            ?.take(180)
            ?.takeIf {
                it.isNotBlank()
            }
            ?: error.javaClass
                .simpleName

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
        private const val STATE_PLAN =
            "bulk_sync_preview_plan"

        private const val STATE_REMOTE_READ_UNITS =
            "bulk_sync_preview_remote_read_units"

        private const val STATE_HELP_DIALOG_OPEN =
            "bulk_sync_preview_help_dialog_open"

        private const val STATE_SELECTION_INITIALIZED =
            "bulk_sync_preview_selection_initialized"

        private const val STATE_INCLUDED_EXECUTABLE_IDS =
            "bulk_sync_preview_included_executable_ids"

        private const val STATE_PREVIEW_MODAL =
            "bulk_sync_preview_modal"

        private const val STATE_SCROLL_POSITION =
            "bulk_sync_preview_scroll_position"

        private const val ARG_SESSION_ID =
            "session_id"
    }
}
