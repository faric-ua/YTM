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
            session.state !=
            BulkSyncSessionState.RUNNING
        ) {
            return session
        }

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

    fun hasUncertainPreparedMutation(
        session: BulkSyncSession
    ): Boolean =
        session.mutationLedger.any {
            it.status ==
                BulkSyncMutationStatus.PREPARED
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

                val applied =
                    session.mutationLedger
                        .any {
                            it.operationId ==
                                operationId &&
                                it.status ==
                                    BulkSyncMutationStatus
                                        .APPLIED
                        }

                if (!applied) {
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
