package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.write.WritePauseAction
import com.saney.ytmimporter.write.WritePausePolicy
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import com.saney.ytmimporter.youtube.YouTubeLimitKind

class BulkSyncExecutor(
    private val api: YouTubeApi,
    private val sessionStore: BulkSyncSessionStore,
    private val restorableStore: RestorablePlaylistStore,
    private val currentPlaylistStore: CurrentPlaylistStore,
    private val quotaTracker: QuotaTracker,
    private val qaInsertFault:
        (() -> YouTubeApiException?)? = null
) {
    fun execute(
        accessToken: String,
        sessionId: String,
        onProgress: (BulkSyncSession) -> Unit = {}
    ): BulkSyncSession {
        var session =
            sessionStore.get(sessionId)
                ?: throw IllegalStateException(
                    "Bulk-сесію не знайдено"
                )

        // An early manual retry must not issue even a single new remote write.
        if (BulkWriteRetryGuard.isWaiting(session)) {
            return session
        }

        if (
            BulkSyncExecutionPolicy
                .hasUncertainPreparedMutation(
                    session
                )
        ) {
            session =
                session.copy(
                    state =
                        BulkSyncSessionState
                            .PAUSED_INTERRUPTED,
                    updatedAt =
                        System.currentTimeMillis(),
                    lastError =
                        "Є PREPARED mutation з невідомим remote результатом. " +
                            "Автоматичний retry заблоковано, щоб не створити дублікат."
                )

            save(
                session,
                onProgress
            )
            return session
        }

        session =
            session.copy(
                state =
                    BulkSyncSessionState.RUNNING,
                updatedAt =
                    System.currentTimeMillis(),
                lastError = null,
                retryNotBeforeEpochMs = null
            )

        save(
            session,
            onProgress
        )

        while (true) {
            val next =
                BulkSyncExecutionPolicy
                    .nextMutation(
                        session
                    )

            if (next == null) {
                val completed =
                    completeRows(
                        session
                    )

                val terminalFailures =
                    completed.mutationLedger
                        .count {
                            it.status ==
                                BulkSyncMutationStatus
                                    .TERMINAL_FAILED
                        }

                session =
                    completed.copy(
                        state =
                            if (terminalFailures > 0) {
                                BulkSyncSessionState
                                    .PARTIAL_FAILED
                            } else {
                                BulkSyncSessionState
                                    .COMPLETED
                            },
                        currentPlanIndex =
                            completed.plan.size,
                        updatedAt =
                            System.currentTimeMillis(),
                        lastError =
                            if (terminalFailures > 0) {
                                "Не додано треків: " +
                                    terminalFailures +
                                    ". Решту операцій завершено."
                            } else {
                                null
                            }
                    )

                save(
                    session,
                    onProgress
                )
                return session
            }

            session =
                when (next) {
                    is BulkSyncNextMutation
                        .CreatePlaylist ->
                        createPlaylist(
                            accessToken =
                                accessToken,
                            session =
                                session,
                            next =
                                next,
                            onProgress =
                                onProgress
                        )

                    is BulkSyncNextMutation
                        .InsertPlaylistItem ->
                        insertItem(
                            accessToken =
                                accessToken,
                            session =
                                session,
                            next =
                                next,
                            onProgress =
                                onProgress
                        )
                }

            if (
                session.state !=
                BulkSyncSessionState.RUNNING
            ) {
                return session
            }
        }
    }

    fun reconcilePrepared(
        accessToken: String,
        sessionId: String,
        onProgress: (BulkSyncSession) -> Unit = {}
    ): BulkSyncSession {
        var session =
            sessionStore.get(
                sessionId
            ) ?: throw IllegalStateException(
                "Bulk-сесію не знайдено"
            )

        val prepared =
            session.mutationLedger
                .filter {
                    it.status ==
                        BulkSyncMutationStatus
                            .PREPARED
                }

        if (prepared.isEmpty()) {
            return session
        }

        for (mutation in prepared) {
            if (
                mutation.type ==
                BulkSyncMutationType
                    .CREATE_PLAYLIST
            ) {
                session =
                    session.copy(
                        state =
                            BulkSyncSessionState
                                .PAUSED_INTERRUPTED,
                        updatedAt =
                            System.currentTimeMillis(),
                        lastError =
                            "Невідомо, чи завершилося створення плейлиста. " +
                                "Безпечний автоматичний retry неможливий без persisted remote ID."
                    )
                save(
                    session,
                    onProgress
                )
                return session
            }

            val remoteId =
                mutation.remotePlaylistId
                    ?: continue

            val rowIndex =
                session.plan
                    .indexOfFirst {
                        it.localPlaylistId ==
                            mutation.localPlaylistId
                    }

            if (rowIndex < 0) {
                return pauseUnknownPrepared(
                    session =
                        session,
                    message =
                        "Не знайдено локальний рядок для PREPARED insert.",
                    onProgress =
                        onProgress
                )
            }

            val row =
                session.plan[
                    rowIndex
                ]

            val createdBySession =
                session.mutationLedger
                    .any {
                        it.type ==
                            BulkSyncMutationType
                                .CREATE_PLAYLIST &&
                            it.localPlaylistId ==
                                row.localPlaylistId &&
                            it.remotePlaylistId ==
                                remoteId &&
                            it.status ==
                                BulkSyncMutationStatus
                                    .APPLIED
                    }

            if (!createdBySession) {
                return pauseUnknownPrepared(
                    session =
                        session,
                    message =
                        "PREPARED insert стосується не session-created playlist; " +
                            "автоматичне припущення заборонено.",
                    onProgress =
                        onProgress
                )
            }

            val preparedTrackIndex =
                mutation.trackIndex
                    ?: return pauseUnknownPrepared(
                        session =
                            session,
                        message =
                            "У PREPARED insert відсутній trackIndex.",
                        onProgress =
                            onProgress
                    )

            val rowTrackIndex =
                row.tracks.indexOfFirst {
                    it.trackIndex ==
                        preparedTrackIndex &&
                        it.videoId ==
                            mutation.videoId
                }

            if (rowTrackIndex < 0) {
                return pauseUnknownPrepared(
                    session =
                        session,
                    message =
                        "PREPARED insert не збігається з durable session plan.",
                    onProgress =
                        onProgress
                )
            }

            var readUnits = 0

            val remote =
                api.listPlaylistSnapshotItems(
                    accessToken =
                        accessToken,
                    playlistId =
                        remoteId
                ) {
                    readUnits +=
                        QuotaTracker
                            .SIMPLE_LIST_COST
                }

            if (readUnits > 0) {
                quotaTracker
                    .recordGeneralUnits(
                        readUnits
                    )
            }

            val expectedRemoteIds =
                BulkSyncExecutionPolicy
                    .expectedRemoteVideoIdsThroughPrepared(
                        session =
                            session,
                        row =
                            row,
                        preparedRowTrackIndex =
                            rowTrackIndex
                    )

            if (expectedRemoteIds.isEmpty()) {
                return pauseUnknownPrepared(
                    session =
                        session,
                    message =
                        "Не вдалося побудувати очікуваний remote prefix для PREPARED insert.",
                    onProgress =
                        onProgress
                )
            }

            val resolution =
                BulkSyncExecutionPolicy
                    .resolvePreparedInsert(
                        expectedVideoIds =
                            expectedRemoteIds,
                        preparedIndex =
                            expectedRemoteIds.lastIndex,
                        remoteVideoIds =
                            remote.items.mapNotNull {
                                it.videoId
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                            }
                    )

            session =
                when (resolution) {
                    is BulkSyncPreparedInsertResolution
                        .Applied -> {
                        val item =
                            remote.items.getOrNull(
                                resolution
                                    .remoteItemIndex
                            )

                        val reconciled =
                            session.replaceMutation(
                                mutation.copy(
                                    createdPlaylistItemId =
                                        item?.playlistItemId,
                                    status =
                                        BulkSyncMutationStatus
                                            .APPLIED,
                                    updatedAt =
                                        System.currentTimeMillis()
                                )
                            ).copy(
                                state =
                                    BulkSyncSessionState
                                        .PAUSED_INTERRUPTED,
                                updatedAt =
                                    System.currentTimeMillis(),
                                lastError =
                                    "Перерваний insert підтверджено read-only snapshot. " +
                                        "Натисніть «Продовжити», щоб перейти до наступної mutation."
                            )

                        if (
                            rowHasNoRemainingMutation(
                                reconciled,
                                rowIndex
                            )
                        ) {
                            reconciled.replaceRow(
                                index =
                                    rowIndex,
                                row =
                                    reconciled.plan[
                                        rowIndex
                                    ].copy(
                                        state =
                                            if (
                                                rowHasTerminalFailures(
                                                    reconciled,
                                                    rowIndex
                                                )
                                            ) {
                                                BulkSyncSessionRowState
                                                    .PARTIAL_FAILED
                                            } else {
                                                BulkSyncSessionRowState
                                                    .COMPLETED
                                            },
                                        lastError =
                                            null
                                    )
                            )
                        } else {
                            reconciled
                        }
                    }

                    BulkSyncPreparedInsertResolution
                        .NotApplied ->
                        session.replaceMutation(
                            mutation.copy(
                                status =
                                    BulkSyncMutationStatus
                                        .FAILED,
                                updatedAt =
                                    System.currentTimeMillis()
                            )
                        ).copy(
                            state =
                                BulkSyncSessionState
                                    .PAUSED_INTERRUPTED,
                            updatedAt =
                                System.currentTimeMillis(),
                            lastError =
                                "Read-only snapshot підтвердив, що перерваний insert не застосовано. " +
                                    "Натисніть «Продовжити» для безпечного retry."
                        )

                    BulkSyncPreparedInsertResolution
                        .Unknown ->
                        return pauseUnknownPrepared(
                            session =
                                session,
                            message =
                                "Remote snapshot не дозволяє однозначно визначити результат PREPARED insert. " +
                                    "Retry заблоковано, щоб не створити дублікат.",
                            onProgress =
                                onProgress
                        )
                }

            save(
                session,
                onProgress
            )
        }

        return session
    }

    private fun pauseUnknownPrepared(
        session: BulkSyncSession,
        message: String,
        onProgress: (BulkSyncSession) -> Unit
    ): BulkSyncSession {
        val paused =
            session.copy(
                state =
                    BulkSyncSessionState
                        .PAUSED_INTERRUPTED,
                updatedAt =
                    System.currentTimeMillis(),
                lastError =
                    message
            )

        save(
            paused,
            onProgress
        )
        return paused
    }

    private fun createPlaylist(
        accessToken: String,
        session: BulkSyncSession,
        next: BulkSyncNextMutation.CreatePlaylist,
        onProgress: (BulkSyncSession) -> Unit
    ): BulkSyncSession {
        val row =
            session.plan[
                next.rowIndex
            ]

        var working =
            session
                .replaceRow(
                    index =
                        next.rowIndex,
                    row =
                        row.copy(
                            state =
                                BulkSyncSessionRowState
                                    .CREATING,
                            lastError =
                                null
                        )
                )
                .replaceMutation(
                    BulkSyncMutation(
                        operationId =
                            next.operationId,
                        type =
                            BulkSyncMutationType
                                .CREATE_PLAYLIST,
                        localPlaylistId =
                            row.localPlaylistId,
                        remotePlaylistId =
                            null,
                        videoId =
                            null,
                        trackIndex =
                            null,
                        status =
                            BulkSyncMutationStatus
                                .PREPARED,
                        updatedAt =
                            System.currentTimeMillis()
                    )
                )
                .copy(
                    currentPlanIndex =
                        next.rowIndex,
                    updatedAt =
                        System.currentTimeMillis()
                )

        save(
            working,
            onProgress
        )

        quotaTracker.recordGeneralUnits(
            QuotaTracker.PLAYLIST_CREATE_COST
        )

        return try {
            val playlistId =
                api.createPlaylist(
                    accessToken =
                        accessToken,
                    title =
                        row.playlistName,
                    privacyStatus =
                        row.privacyStatus
                )

            val now =
                System.currentTimeMillis()

            working =
                working
                    .replaceMutation(
                        BulkSyncMutation(
                            operationId =
                                next.operationId,
                            type =
                                BulkSyncMutationType
                                    .CREATE_PLAYLIST,
                            localPlaylistId =
                                row.localPlaylistId,
                            remotePlaylistId =
                                playlistId,
                            videoId =
                                null,
                            trackIndex =
                                null,
                            status =
                                BulkSyncMutationStatus
                                    .APPLIED,
                            updatedAt =
                                now
                        )
                    )
                    .replaceRow(
                        index =
                            next.rowIndex,
                        row =
                            row.copy(
                                state =
                                    BulkSyncSessionRowState
                                        .INSERTING,
                                remotePlaylistId =
                                    playlistId,
                                lastError =
                                    null
                            )
                    )
                    .copy(
                        updatedAt =
                            now
                    )

            save(
                working,
                onProgress
            )

            persistRemoteLink(
                localPlaylistId =
                    row.localPlaylistId,
                playlistId =
                    playlistId,
                playlistTitle =
                    row.playlistName
            )

            working
        } catch (error: Throwable) {
            handleFailure(
                session =
                    working,
                rowIndex =
                    next.rowIndex,
                operationId =
                    next.operationId,
                action =
                    WritePauseAction
                        .CREATE_PLAYLIST,
                error =
                    error,
                onProgress =
                    onProgress
            )
        }
    }

    private fun insertItem(
        accessToken: String,
        session: BulkSyncSession,
        next: BulkSyncNextMutation.InsertPlaylistItem,
        onProgress: (BulkSyncSession) -> Unit
    ): BulkSyncSession {
        val row =
            session.plan[
                next.rowIndex
            ]

        var working =
            session
                .replaceRow(
                    index =
                        next.rowIndex,
                    row =
                        row.copy(
                            state =
                                BulkSyncSessionRowState
                                    .INSERTING,
                            remotePlaylistId =
                                next.remotePlaylistId,
                            lastError =
                                null
                        )
                )
                .replaceMutation(
                    BulkSyncMutation(
                        operationId =
                            next.operationId,
                        type =
                            BulkSyncMutationType
                                .INSERT_PLAYLIST_ITEM,
                        localPlaylistId =
                            row.localPlaylistId,
                        remotePlaylistId =
                            next.remotePlaylistId,
                        videoId =
                            next.videoId,
                        trackIndex =
                            row.tracks[
                                next.trackIndex
                            ].trackIndex,
                        status =
                            BulkSyncMutationStatus
                                .PREPARED,
                        updatedAt =
                            System.currentTimeMillis()
                    )
                )
                .copy(
                    currentPlanIndex =
                        next.rowIndex,
                    updatedAt =
                        System.currentTimeMillis()
                )

        save(
            working,
            onProgress
        )

        qaInsertFault
            ?.invoke()
            ?.let {
                error ->
                return handleFailure(
                    session =
                        working,
                    rowIndex =
                        next.rowIndex,
                    operationId =
                        next.operationId,
                    action =
                        WritePauseAction
                            .ADD_TRACK,
                    error =
                        error,
                    onProgress =
                        onProgress
                )
            }

        quotaTracker.recordGeneralUnits(
            QuotaTracker
                .PLAYLIST_ITEM_INSERT_COST
        )

        return try {
            val createdPlaylistItemId =
                api.addVideo(
                    accessToken =
                        accessToken,
                    playlistId =
                        next.remotePlaylistId,
                    videoId =
                        next.videoId
                )

            val now =
                System.currentTimeMillis()

            working =
                working
                    .replaceMutation(
                        BulkSyncMutation(
                            operationId =
                                next.operationId,
                            type =
                                BulkSyncMutationType
                                    .INSERT_PLAYLIST_ITEM,
                            localPlaylistId =
                                row.localPlaylistId,
                            remotePlaylistId =
                                next.remotePlaylistId,
                            videoId =
                                next.videoId,
                            trackIndex =
                                row.tracks[
                                    next.trackIndex
                                ].trackIndex,
                            createdPlaylistItemId =
                                createdPlaylistItemId,
                            status =
                                BulkSyncMutationStatus
                                    .APPLIED,
                            updatedAt =
                                now
                        )
                    )
                    .copy(
                        updatedAt =
                            now
                    )

            if (
                BulkSyncExecutionPolicy
                    .nextMutation(
                        working
                    ) == null ||
                rowHasNoRemainingMutation(
                    working,
                    next.rowIndex
                )
            ) {
                working =
                    working.replaceRow(
                        index =
                            next.rowIndex,
                        row =
                            working.plan[
                                next.rowIndex
                            ].copy(
                                state =
                                    if (
                                        rowHasTerminalFailures(
                                            working,
                                            next.rowIndex
                                        )
                                    ) {
                                        BulkSyncSessionRowState
                                            .PARTIAL_FAILED
                                    } else {
                                        BulkSyncSessionRowState
                                            .COMPLETED
                                    },
                                lastError =
                                    null
                            )
                    )
            }

            save(
                working,
                onProgress
            )

            working
        } catch (error: Throwable) {
            handleFailure(
                session =
                    working,
                rowIndex =
                    next.rowIndex,
                operationId =
                    next.operationId,
                action =
                    WritePauseAction
                        .ADD_TRACK,
                error =
                    error,
                onProgress =
                    onProgress
            )
        }
    }

    private fun rowHasNoRemainingMutation(
        session: BulkSyncSession,
        rowIndex: Int
    ): Boolean {
        val row =
            session.plan[
                rowIndex
            ]

        return row.tracks.all {
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
                    it.status in
                        setOf(
                            BulkSyncMutationStatus
                                .APPLIED,
                            BulkSyncMutationStatus
                                .TERMINAL_FAILED
                        )
            }
        }
    }

    private fun handleFailure(
        session: BulkSyncSession,
        rowIndex: Int,
        operationId: String,
        action: WritePauseAction,
        error: Throwable,
        onProgress: (BulkSyncSession) -> Unit
    ): BulkSyncSession {
        val apiError =
            error as?
                YouTubeApiException

        if (apiError == null) {
            val uncertain =
                session.copy(
                    state =
                        BulkSyncSessionState
                            .PAUSED_INTERRUPTED,
                    updatedAt =
                        System.currentTimeMillis(),
                    lastError =
                        "Немає підтвердженого HTTP результату останнього write. " +
                            "Mutation лишено PREPARED, повтор заблоковано: " +
                            safeError(error)
                )

            save(
                uncertain,
                onProgress
            )
            return uncertain
        }

        var failed =
            session.replaceMutationStatus(
                operationId =
                    operationId,
                status =
                    BulkSyncMutationStatus
                        .FAILED
            )

        val state: BulkSyncSessionState
        val message: String

        when {
            apiError.httpCode == 401 -> {
                state =
                    BulkSyncSessionState
                        .PAUSED_AUTH
                message =
                    "Google/YTM authorization потрібна знову. " +
                        "Підключіться на Home і продовжіть сесію вручну."
            }

            apiError.limitKind ==
                YouTubeLimitKind.DAILY_QUOTA -> {
                quotaTracker.recordQuotaError(
                    apiError.message
                )
                state =
                    BulkSyncSessionState
                        .PAUSED_WRITE_QUOTA
                message =
                    WritePausePolicy.userMessage(
                        kind =
                            YouTubeLimitKind
                                .DAILY_QUOTA,
                        action =
                            action
                    )
            }

            apiError.limitKind != null -> {
                state =
                    BulkSyncSessionState
                        .PAUSED_RATE_LIMIT
                message =
                    WritePausePolicy.userMessage(
                        kind =
                            requireNotNull(
                                apiError.limitKind
                            ),
                        action =
                            action
                    )
            }

            else -> {
                message =
                    safeError(apiError)

                if (
                    action ==
                    WritePauseAction.ADD_TRACK
                ) {
                    val currentMutation =
                        session.mutationLedger
                            .lastOrNull {
                                it.operationId ==
                                    operationId
                            }

                    failed =
                        if (currentMutation != null) {
                            session.replaceMutation(
                                currentMutation.copy(
                                    status =
                                        BulkSyncMutationStatus
                                            .TERMINAL_FAILED,
                                    error =
                                        message,
                                    updatedAt =
                                        System.currentTimeMillis()
                                )
                            )
                        } else {
                            failed
                        }

                    failed =
                        failed.replaceRow(
                            index =
                                rowIndex,
                            row =
                                failed.plan[
                                    rowIndex
                                ].copy(
                                    state =
                                        BulkSyncSessionRowState
                                            .INSERTING,
                                    lastError =
                                        message
                                )
                        )

                    state =
                        BulkSyncSessionState.RUNNING
                } else {
                    state =
                        BulkSyncSessionState
                            .PARTIAL_FAILED

                    failed =
                        failed.replaceRow(
                            index =
                                rowIndex,
                            row =
                                failed.plan[
                                    rowIndex
                                ].copy(
                                    state =
                                        BulkSyncSessionRowState
                                            .FAILED,
                                    lastError =
                                        message
                                )
                        )
                }
            }
        }

        failed =
            failed.copy(
                state =
                    state,
                updatedAt =
                    System.currentTimeMillis(),
                retryNotBeforeEpochMs =
                    if (state == BulkSyncSessionState.PAUSED_RATE_LIMIT) {
                        BulkWriteRetryGuard.retryNotBefore(
                            retryAfterHeader = apiError.retryAfterHeader,
                            nowEpochMs = System.currentTimeMillis()
                        )
                    } else {
                        null
                    },
                lastError =
                    if (state == BulkSyncSessionState.RUNNING) {
                        null
                    } else if (
                        state == BulkSyncSessionState.PAUSED_RATE_LIMIT ||
                        state == BulkSyncSessionState.PAUSED_WRITE_QUOTA
                    ) {
                        val reason = apiError.reason
                            ?: apiError.detailReasons.firstOrNull()
                            ?: apiError.status
                            ?: "не вказана"
                        message + "\nДіагностика YouTube: HTTP " +
                            apiError.httpCode + " • причина: " + reason
                    } else {
                        message
                    }
            )

        save(
            failed,
            onProgress
        )
        return failed
    }

    private fun rowHasTerminalFailures(
        session: BulkSyncSession,
        rowIndex: Int
    ): Boolean {
        val row =
            session.plan[
                rowIndex
            ]

        return row.tracks.any {
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
                            .TERMINAL_FAILED
            }
        }
    }

    private fun completeRows(
        session: BulkSyncSession
    ): BulkSyncSession {
        var result = session

        session.plan.forEachIndexed {
                index,
                row ->
            if (
                row.state in
                setOf(
                    BulkSyncSessionRowState.READY,
                    BulkSyncSessionRowState.READY_APPEND,
                    BulkSyncSessionRowState.CREATING,
                    BulkSyncSessionRowState.INSERTING
                ) &&
                rowHasNoRemainingMutation(
                    result,
                    index
                ) &&
                !row.remotePlaylistId
                    .isNullOrBlank()
            ) {
                result =
                    result.replaceRow(
                        index =
                            index,
                        row =
                            row.copy(
                                state =
                                    if (
                                        rowHasTerminalFailures(
                                            result,
                                            index
                                        )
                                    ) {
                                        BulkSyncSessionRowState
                                            .PARTIAL_FAILED
                                    } else {
                                        BulkSyncSessionRowState
                                            .COMPLETED
                                    },
                                lastError =
                                    null
                            )
                    )
            }
        }

        return result
    }

    private fun persistRemoteLink(
        localPlaylistId: String,
        playlistId: String,
        playlistTitle: String
    ) {
        val current =
            currentPlaylistStore.load()

        val snapshot =
            restorableStore.get(
                localPlaylistId
            )
                ?: return

        val updated =
            snapshot.copy(
                updatedAt =
                    System.currentTimeMillis(),
                destinationPlaylistId =
                    playlistId,
                destinationPlaylistTitle =
                    playlistTitle
            )

        restorableStore.upsert(
            updated
        )

        if (
            current?.localPlaylistId ==
            localPlaylistId
        ) {
            currentPlaylistStore.save(
                playlist =
                    updated.playlist,
                sourceLabel =
                    updated.sourceLabel,
                destinationPlaylistId =
                    playlistId,
                destinationPlaylistTitle =
                    playlistTitle,
                localPlaylistId =
                    localPlaylistId,
                sourceHistoryId =
                    updated.sourceHistoryId
            )
        }
    }

    private fun save(
        session: BulkSyncSession,
        onProgress: (BulkSyncSession) -> Unit
    ) {
        sessionStore.upsert(
            session
        )
        onProgress(session)
    }

    private fun safeError(
        error: Throwable
    ): String =
        error.message
            ?.trim()
            ?.take(300)
            ?.takeIf {
                it.isNotBlank()
            }
            ?: error.javaClass
                .simpleName

    private fun BulkSyncSession.replaceRow(
        index: Int,
        row: BulkSyncSessionRow
    ): BulkSyncSession {
        val rows =
            plan.toMutableList()

        rows[index] = row

        return copy(
            plan = rows
        )
    }

    private fun BulkSyncSession.replaceMutation(
        mutation: BulkSyncMutation
    ): BulkSyncSession {
        val entries =
            mutationLedger
                .filterNot {
                    it.operationId ==
                        mutation.operationId
                }
                .toMutableList()

        entries += mutation

        return copy(
            mutationLedger = entries
        )
    }

    private fun BulkSyncSession.replaceMutationStatus(
        operationId: String,
        status: BulkSyncMutationStatus
    ): BulkSyncSession {
        val entries =
            mutationLedger.map {
                if (
                    it.operationId ==
                    operationId
                ) {
                    it.copy(
                        status =
                            status,
                        updatedAt =
                            System.currentTimeMillis()
                    )
                } else {
                    it
                }
            }

        return copy(
            mutationLedger = entries
        )
    }
}
