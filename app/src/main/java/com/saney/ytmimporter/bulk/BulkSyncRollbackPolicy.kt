package com.saney.ytmimporter.bulk

object BulkSyncRollbackPolicy {
    fun normalizeAfterColdOpen(
        session: BulkSyncSession
    ): BulkSyncSession {
        if (
            session.state ==
            BulkSyncSessionState.ROLLING_BACK
        ) {
            return session.copy(
                state =
                    BulkSyncSessionState
                        .ROLLBACK_PAUSED,
                updatedAt =
                    System.currentTimeMillis(),
                lastError =
                    "Попередній відкат був перерваний. " +
                        "Автоматичне продовження вимкнено; " +
                        "перевірте стан і натисніть «Продовжити відкат»."
            )
        }

        return session
    }

    fun exactnessError(
        session: BulkSyncSession
    ): String? {
        session.mutationLedger
            .filter {
                it.status ==
                    BulkSyncMutationStatus.APPLIED
            }
            .forEach {
                mutation ->
                when (mutation.type) {
                    BulkSyncMutationType
                        .CREATE_PLAYLIST -> {
                        if (
                            mutation.remotePlaylistId
                                .isNullOrBlank()
                        ) {
                            return "CREATE mutation не має persisted playlistId; exact rollback заборонено."
                        }
                    }

                    BulkSyncMutationType
                        .INSERT_PLAYLIST_ITEM -> {
                        if (
                            mutation.remotePlaylistId
                                .isNullOrBlank()
                        ) {
                            return "INSERT mutation не має persisted playlistId; exact rollback заборонено."
                        }

                        if (
                            mutation.createdPlaylistItemId
                                .isNullOrBlank()
                        ) {
                            return "INSERT mutation не має persisted playlistItemId; exact rollback заборонено."
                        }
                    }
                }
            }

        return null
    }

    fun canStartRollback(
        session: BulkSyncSession
    ): Boolean {
        if (
            session.state !in
            setOf(
                BulkSyncSessionState.COMPLETED,
                BulkSyncSessionState.PARTIAL_FAILED
            )
        ) {
            return false
        }

        if (
            exactnessError(session) != null
        ) {
            return false
        }

        return session.mutationLedger.any {
            it.status ==
                BulkSyncMutationStatus.APPLIED
        }
    }

    fun canResumeRollback(
        session: BulkSyncSession
    ): Boolean =
        session.state ==
            BulkSyncSessionState.ROLLBACK_PAUSED &&
            exactnessError(session) == null &&
            nextAppliedMutation(session) != null

    fun nextAppliedMutation(
        session: BulkSyncSession
    ): BulkSyncMutation? {
        val applied =
            session.mutationLedger
                .filter {
                    it.status ==
                        BulkSyncMutationStatus.APPLIED
                }

        return applied
            .filter {
                it.type ==
                    BulkSyncMutationType
                        .INSERT_PLAYLIST_ITEM
            }
            .maxByOrNull {
                it.updatedAt
            }
            ?: applied
                .filter {
                    it.type ==
                        BulkSyncMutationType
                            .CREATE_PLAYLIST
                }
                .maxByOrNull {
                    it.updatedAt
                }
    }

    fun rolledBackCount(
        session: BulkSyncSession
    ): Int =
        session.mutationLedger.count {
            it.status ==
                BulkSyncMutationStatus.ROLLED_BACK
        }

    fun remainingAppliedCount(
        session: BulkSyncSession
    ): Int =
        session.mutationLedger.count {
            it.status ==
                BulkSyncMutationStatus.APPLIED
        }
}
