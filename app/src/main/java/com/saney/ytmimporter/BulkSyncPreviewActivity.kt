package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
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
import com.saney.ytmimporter.bulk.BulkSyncBaselineItem
import com.saney.ytmimporter.bulk.BulkSyncBaselinePlaylist
import com.saney.ytmimporter.bulk.BulkSyncLocalPlaylist
import com.saney.ytmimporter.bulk.BulkSyncPlanRow
import com.saney.ytmimporter.bulk.BulkSyncPlanState
import com.saney.ytmimporter.bulk.BulkSyncPlanSummary
import com.saney.ytmimporter.bulk.BulkSyncPreflightPolicy
import com.saney.ytmimporter.bulk.BulkSyncRemoteBaseline
import com.saney.ytmimporter.bulk.BulkSyncRemoteSnapshot
import com.saney.ytmimporter.bulk.BulkSyncSessionFactory
import com.saney.ytmimporter.search.SearchCoordinator
import com.saney.ytmimporter.storage.BulkSyncCheckpointStore
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.storage.LocalBackupManager
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.ui.AppThemeManager
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
    private lateinit var summaryText:
        TextView
    private lateinit var rowsContainer:
        LinearLayout
    private lateinit var confirmationButton:
        Button

    private var plan:
        BulkSyncPlanSummary? = null
    private var remoteReadUnitsUsed =
        0
    private var loading =
        false

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

        super.onSaveInstanceState(
            outState
        )
    }

    override fun onDestroy() {
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
                    0,
                    0
                )
            },
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
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
                                this@BulkSyncPreviewActivity,
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
                orientation =
                    LinearLayout.VERTICAL
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
                    "Створити Bulk-сесію"
                isAllCaps = false
                isEnabled = false
                setOnClickListener {
                    confirmPlan()
                }
            }

        actions.addView(
            confirmationButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        actions.addView(
            Button(this).apply {
                text = "Скасувати"
                isAllCaps = false
                setOnClickListener {
                    finish()
                }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin =
                    dp(8)
            }
        )

        root.addView(actions)

        setContentView(root)

        UiChrome.applyScreenInsets(
            this,
            root
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
        summaryText.text =
            "Аналіз локальних плейлистів і remote snapshot…"
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
        statusText.text =
            "Preview готовий. Жодних remote mutations не виконано."

        summaryText.text =
            buildString {
                append(
                    "Плейлистів: "
                )
                append(
                    summary.rows.size
                )
                append("\n")
                append(
                    BulkSyncPlanState
                        .values()
                        .joinToString(
                            " • "
                        ) {
                            state ->
                            state.name +
                                " " +
                                summary.count(
                                    state
                                )
                        }
                )
                append("\n\n")
                append(
                    "Search API: "
                )
                append(
                    summary
                        .estimatedSearchCalls
                )
                append(
                    " викликів"
                )
                append("\n")
                append(
                    "Інші API units після явного підтвердження: "
                )
                append(
                    summary
                        .estimatedWriteUnits
                )
                append("\n")
                append(
                    "Read-only remote snapshot уже використав: "
                )
                append(
                    remoteReadUnitsUsed
                )
                append(
                    " units"
                )
                append("\n\n")
                append(
                    "Wave 3: після підтвердження створюється durable session. " +
                        "Remote writes стартують тільки окремою дією на екрані сесії. " +
                        "У цій хвилі виконуються лише NEW; LINKED відкладено."
                )
            }

        confirmationButton.isEnabled =
            !loading &&
                summary.count(
                    BulkSyncPlanState.NEW
                ) > 0

        rowsContainer.removeAllViews()

        if (summary.rows.isEmpty()) {
            rowsContainer.addView(
                planText(
                    "Немає локальних плейлистів для Bulk Sync."
                )
            )
            return
        }

        summary.rows.forEach {
                row ->
            rowsContainer.addView(
                planRow(row),
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

    private fun planRow(
        row: BulkSyncPlanRow
    ): TextView {
        val palette =
            AppThemeManager.palette(this)

        return TextView(this).apply {
            text =
                buildString {
                    append(
                        row.state.name
                    )
                    append(
                        " · "
                    )
                    append(
                        row.playlistName
                    )
                    append("\n")
                    append(
                        "Треки: "
                    )
                    append(
                        row.trackCount
                    )
                    append(
                        " • ready videoId: "
                    )
                    append(
                        row.selectedCount
                    )
                    append(
                        " • unresolved: "
                    )
                    append(
                        row.unresolvedCount
                    )

                    if (
                        row.estimatedSearchCalls >
                        0
                    ) {
                        append("\nSearch: ")
                        append(
                            row
                                .estimatedSearchCalls
                        )
                        append(
                            " • cache: "
                        )
                        append(
                            row.cacheHits
                        )
                    }

                    if (
                        row.plannedCreate ||
                        row.plannedInsertCount >
                        0
                    ) {
                        append("\nПісля підтвердження: ")

                        if (
                            row.plannedCreate
                        ) {
                            append(
                                "create 1"
                            )

                            if (
                                row.plannedInsertCount >
                                0
                            ) {
                                append(
                                    " • "
                                )
                            }
                        }

                        if (
                            row.plannedInsertCount >
                            0
                        ) {
                            append(
                                "insert "
                            )
                            append(
                                row
                                    .plannedInsertCount
                            )
                        }

                        append(
                            " • "
                        )
                        append(
                            row
                                .estimatedWriteUnits
                        )
                        append(
                            " units"
                        )
                    }

                    append("\n")
                    append(
                        row.reason
                    )
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
                            this@BulkSyncPreviewActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )

            if (
                row.state ==
                BulkSyncPlanState.BLOCKED
            ) {
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
            }
        }
    }

    private fun confirmPlan() {
        val summary =
            plan
                ?: return

        val sessionStore =
            BulkSyncSessionStore(this)

        val existing =
            sessionStore.active()

        if (
            existing != null &&
            !existing.isTerminal
        ) {
            UiChrome.alertBuilder(this)
                .setTitle(
                    "Є незавершена Bulk-сесія"
                )
                .setMessage(
                    "Спочатку відкрийте вже створену сесію. " +
                        "Нова сесія не буде створена поверх незавершеної."
                )
                .setNegativeButton(
                    "Скасувати",
                    null
                )
                .setPositiveButton(
                    "Відкрити"
                ) { _, _ ->
                    openSession(
                        existing.sessionId
                    )
                }
                .show()
            return
        }

        val newCount =
            summary.count(
                BulkSyncPlanState.NEW
            )

        if (newCount <= 0) {
            toast(
                "У Wave 3 немає NEW-плейлистів для виконання."
            )
            return
        }

        UiChrome.alertBuilder(this)
            .setTitle(
                "Створити Bulk-сесію?"
            )
            .setMessage(
                "Буде створено локальний Full Backup checkpoint і свіжий " +
                    "read-only remote baseline.\n\n" +
                    "До сесії потрапить " +
                    newCount +
                    " NEW-плейлист(ів). Нові YTM-плейлисти створюються як приватні.\n\n" +
                    "LINKED / NEEDS_SEARCH / PENDING / BLOCKED у Wave 3 " +
                    "не виконуються. Після створення сесії remote writes " +
                    "ще не стартують автоматично."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Створити сесію"
            ) { _, _ ->
                prepareSession(
                    summary
                )
            }
            .show()
    }

    private fun prepareSession(
        summary: BulkSyncPlanSummary
    ) {
        if (loading) {
            return
        }

        val auth =
            AuthSessionStore.current()

        val token =
            auth.accessToken
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return toast(
                    "Підключіть Google / YTM перед створенням Bulk-сесії."
                )

        loading = true
        confirmationButton.isEnabled =
            false
        statusText.text =
            "Створюю local checkpoint і свіжий read-only remote baseline…"

        executor.execute {
            try {
                val checkpointJson =
                    LocalBackupManager(this)
                        .createBulkSyncCheckpointJson()

                val checkpoint =
                    BulkSyncCheckpointStore(this)
                        .save(
                            checkpointJson
                        )

                val baselineResult =
                    captureBaseline(
                        accessToken =
                            token,
                        auth =
                            auth
                    )

                if (
                    baselineResult.second >
                    0
                ) {
                    quotaTracker.recordGeneralUnits(
                        baselineResult.second
                    )
                }

                val session =
                    BulkSyncSessionFactory
                        .create(
                            summary =
                                summary,
                            snapshots =
                                restorableStore
                                    .getAll(),
                            checkpointId =
                                checkpoint
                                    .checkpointId,
                            baseline =
                                baselineResult
                                    .first
                        )

                BulkSyncSessionStore(this)
                    .upsert(
                        session
                    )

                runOnUiThread {
                    if (
                        !isFinishing &&
                        !isDestroyed
                    ) {
                        loading = false
                        openSession(
                            session.sessionId
                        )
                    }
                }
            } catch (error: Throwable) {
                runOnUiThread {
                    if (
                        !isFinishing &&
                        !isDestroyed
                    ) {
                        loading = false
                        confirmationButton.isEnabled =
                            summary.count(
                                BulkSyncPlanState.NEW
                            ) > 0
                        statusText.text =
                            "Preview готовий. Сесію не створено."
                        toast(
                            "Не вдалося створити Bulk-сесію: " +
                                safeError(
                                    error
                                )
                        )
                    }
                }
            }
        }
    }

    private fun captureBaseline(
        accessToken: String,
        auth: AuthSessionStore.Snapshot
    ): Pair<BulkSyncRemoteBaseline, Int> {
        var readUnits = 0

        val owned =
            api.listMyPlaylists(
                accessToken =
                    accessToken
            ) {
                readUnits +=
                    QuotaTracker
                        .SIMPLE_LIST_COST
            }

        val ownedById =
            owned.associateBy {
                it.id
            }

        val playlists =
            owned.map {
                    info ->
                val snapshot =
                    api.listPlaylistSnapshotItems(
                        accessToken =
                            accessToken,
                        playlistId =
                            info.id
                    ) {
                        readUnits +=
                            QuotaTracker
                                .SIMPLE_LIST_COST
                    }

                BulkSyncBaselinePlaylist(
                    playlistId =
                        info.id,
                    title =
                        info.title,
                    privacyStatus =
                        info.privacyStatus,
                    items =
                        snapshot.items.map {
                            item ->
                            BulkSyncBaselineItem(
                                playlistItemId =
                                    item.playlistItemId,
                                sourcePosition =
                                    item.sourcePosition,
                                videoId =
                                    item.videoId
                            )
                        }
                )
            }

        return Pair(
            BulkSyncRemoteBaseline(
                capturedAt =
                    System.currentTimeMillis(),
                googleEmail =
                    auth.googleAccountInfo
                        ?.email,
                youtubeChannelId =
                    auth.youtubeChannelInfo
                        ?.id,
                youtubeChannelTitle =
                    auth.youtubeChannelInfo
                        ?.title,
                playlists =
                    playlists
            ),
            readUnits
        )
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
    }
}
