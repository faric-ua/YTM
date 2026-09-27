package com.saney.ytmimporter.bulk

sealed class BulkSyncPreparedInsertResolution {
    data class Applied(
        val remoteItemIndex: Int
    ) : BulkSyncPreparedInsertResolution()

    data object NotApplied :
        BulkSyncPreparedInsertResolution()

    data object Unknown :
        BulkSyncPreparedInsertResolution()
}

sealed class BulkSyncNextMutation {
    data class CreatePlaylist(
        val rowIndex: Int,
        val operationId: String
    ) : BulkSyncNextMutation()

    data class InsertPlaylistItem(
        val rowIndex: Int,
        val trackIndex: Int,
        val operationId: String,
        val remotePlaylistId: String,
        val videoId: String
    ) : BulkSyncNextMutation()
}

object BulkSyncExecutionPolicy {
    fun normalizeAfterColdOpen(
        session: BulkSyncSession
    ): BulkSyncSession {
        if (
            session.state ==
            BulkSyncSessionState.RUNNING
        ) {
            return session.copy(
                state =
                    BulkSyncSessionState
                        .PAUSED_INTERRUPTED,
                updatedAt =
                    System.currentTimeMillis(),
                lastError =
                    "Попередній запуск був перерваний. " +
                        "Автоматичне продовження вимкнено; " +
                        "перевірте стан і натисніть «Продовжити»."
            )
        }

        if (
            session.state ==
            BulkSyncSessionState.PARTIAL_FAILED
        ) {
            val legacyFailedInsert =
                session.mutationLedger
                    .lastOrNull {
                        it.type ==
                            BulkSyncMutationType
                                .INSERT_PLAYLIST_ITEM &&
                            it.status ==
                                BulkSyncMutationStatus
                                    .FAILED
                    }

            if (legacyFailedInsert != null) {
                val rowIndex =
                    session.plan.indexOfFirst {
                        it.localPlaylistId ==
                            legacyFailedInsert
                                .localPlaylistId
                    }

                if (rowIndex >= 0) {
                    val rows =
                        session.plan
                            .toMutableList()

                    val row =
                        rows[rowIndex]

                    val reason =
                        legacyFailedInsert.error
                            ?: row.lastError
                            ?: session.lastError
                            ?: "Трек не вдалося додати"

                    rows[rowIndex] =
                        row.copy(
                            state =
                                BulkSyncSessionRowState
                                    .INSERTING,
                            lastError =
                                reason
                        )

                    val ledger =
                        session.mutationLedger
                            .map {
                                mutation ->
                                if (
                                    mutation.operationId ==
                                    legacyFailedInsert
                                        .operationId
                                ) {
                                    mutation.copy(
                                        status =
                                            BulkSyncMutationStatus
                                                .TERMINAL_FAILED,
                                        error =
                                            reason,
                                        updatedAt =
                                            System.currentTimeMillis()
                                    )
                                } else {
                                    mutation
                                }
                            }

                    return session.copy(
                        state =
                            BulkSyncSessionState
                                .PAUSED_INTERRUPTED,
                        plan =
                            rows,
                        mutationLedger =
                            ledger,
                        updatedAt =
                            System.currentTimeMillis(),
                        lastError =
                            "Один трек не вдалося додати. " +
                                "Решту можна безпечно продовжити вручну."
                    )
                }
            }
        }

        return session
    }

    fun hasUncertainPreparedMutation(
        session: BulkSyncSession
    ): Boolean =
        session.mutationLedger.any {
            it.status ==
                BulkSyncMutationStatus.PREPARED
        }

    fun canExplicitlyResume(
        session: BulkSyncSession
    ): Boolean {
        if (
            session.state !in
            setOf(
                BulkSyncSessionState.READY,
                BulkSyncSessionState
                    .PAUSED_WRITE_QUOTA,
                BulkSyncSessionState
                    .PAUSED_RATE_LIMIT,
                BulkSyncSessionState
                    .PAUSED_AUTH,
                BulkSyncSessionState
                    .PAUSED_INTERRUPTED
            )
        ) {
            return false
        }

        val prepared =
            session.mutationLedger
                .filter {
                    it.status ==
                        BulkSyncMutationStatus
                            .PREPARED
                }

        if (prepared.isEmpty()) {
            return true
        }

        return prepared.all {
                mutation ->
            if (
                mutation.type !=
                BulkSyncMutationType
                    .INSERT_PLAYLIST_ITEM
            ) {
                return@all false
            }

            val remoteId =
                mutation.remotePlaylistId
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: return@all false

            session.mutationLedger.any {
                applied ->
                applied.type ==
                    BulkSyncMutationType
                        .CREATE_PLAYLIST &&
                    applied.localPlaylistId ==
                        mutation.localPlaylistId &&
                    applied.remotePlaylistId ==
                        remoteId &&
                    applied.status ==
                        BulkSyncMutationStatus
                            .APPLIED
            }
        }
    }

    fun expectedRemoteVideoIdsThroughPrepared(
        session: BulkSyncSession,
        row: BulkSyncSessionRow,
        preparedRowTrackIndex: Int
    ): List<String> {
        if (
            preparedRowTrackIndex !in
            row.tracks.indices
        ) {
            return emptyList()
        }

        return row.tracks
            .take(
                preparedRowTrackIndex + 1
            )
            .filterNot {
                track ->
                val operationId =
                    insertOperationId(
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
                    mutation ->
                    mutation.operationId ==
                        operationId &&
                        mutation.status ==
                            BulkSyncMutationStatus
                                .TERMINAL_FAILED
                }
            }
            .map {
                it.videoId
            }
    }

    fun resolvePreparedInsert(
        expectedVideoIds: List<String>,
        preparedIndex: Int,
        remoteVideoIds: List<String>
    ): BulkSyncPreparedInsertResolution {
        if (
            preparedIndex !in
            expectedVideoIds.indices
        ) {
            return BulkSyncPreparedInsertResolution
                .Unknown
        }

        val before =
            expectedVideoIds.take(
                preparedIndex
            )

        if (
            remoteVideoIds.size <
            before.size ||
            remoteVideoIds.take(
                before.size
            ) != before
        ) {
            return BulkSyncPreparedInsertResolution
                .Unknown
        }

        if (
            remoteVideoIds.size ==
            before.size
        ) {
            return BulkSyncPreparedInsertResolution
                .NotApplied
        }

        return if (
            remoteVideoIds.take(
                preparedIndex + 1
            ) ==
            expectedVideoIds.take(
                preparedIndex + 1
            )
        ) {
            BulkSyncPreparedInsertResolution
                .Applied(
                    remoteItemIndex =
                        preparedIndex
                )
        } else {
            BulkSyncPreparedInsertResolution
                .Unknown
        }
    }

    fun nextMutation(
        session: BulkSyncSession
    ): BulkSyncNextMutation? {
        session.plan.forEachIndexed {
                rowIndex,
                row ->
            if (
                row.state !in
                setOf(
                    BulkSyncSessionRowState.READY,
                    BulkSyncSessionRowState.CREATING,
                    BulkSyncSessionRowState.INSERTING
                )
            ) {
                return@forEachIndexed
            }

            val createId =
                createOperationId(
                    session.sessionId,
                    row.localPlaylistId
                )

            val appliedCreate =
                session.mutationLedger
                    .lastOrNull {
                        it.operationId ==
                            createId &&
                            it.status ==
                                BulkSyncMutationStatus
                                    .APPLIED
                    }

            val remoteId =
                row.remotePlaylistId
                    ?: appliedCreate
                        ?.remotePlaylistId

            if (remoteId.isNullOrBlank()) {
                return BulkSyncNextMutation
                    .CreatePlaylist(
                        rowIndex =
                            rowIndex,
                        operationId =
                            createId
                    )
            }

            row.tracks.forEachIndexed {
                    trackIndex,
                    track ->
                val operationId =
                    insertOperationId(
                        sessionId =
                            session.sessionId,
                        localPlaylistId =
                            row.localPlaylistId,
                        trackIndex =
                            track.trackIndex,
                        videoId =
                            track.videoId
                    )

                val resolved =
                    session.mutationLedger
                        .any {
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

                if (!resolved) {
                    return BulkSyncNextMutation
                        .InsertPlaylistItem(
                            rowIndex =
                                rowIndex,
                            trackIndex =
                                trackIndex,
                            operationId =
                                operationId,
                            remotePlaylistId =
                                remoteId,
                            videoId =
                                track.videoId
                        )
                }
            }
        }

        return null
    }

    fun createOperationId(
        sessionId: String,
        localPlaylistId: String
    ): String =
        "create:" +
            sessionId +
            ":" +
            localPlaylistId

    fun insertOperationId(
        sessionId: String,
        localPlaylistId: String,
        trackIndex: Int,
        videoId: String
    ): String =
        "insert:" +
            sessionId +
            ":" +
            localPlaylistId +
            ":" +
            trackIndex +
            ":" +
            videoId
}
