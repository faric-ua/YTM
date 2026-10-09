package com.saney.ytmimporter.bulk

/**
 * Local, user-adjustable safety batch. This is NOT YouTube's unpublished
 * channel daily playlist-creation limit. Never auto-start the next batch.
 */
object BulkCreateBatchPolicy {
    val SIZES: Set<Int> = setOf(1, 3, 5)
    const val DEFAULT_MAX_CREATES: Int = 3

    fun appliedCreates(session: BulkSyncSession): Int =
        session.mutationLedger.count {
            it.type == BulkSyncMutationType.CREATE_PLAYLIST &&
                it.status == BulkSyncMutationStatus.APPLIED
        }

    fun shouldPauseBeforeCreate(
        session: BulkSyncSession,
        createdAtManualStart: Int,
        next: BulkSyncNextMutation?
    ): Boolean {
        val batchSize = session.maxCreatesPerRun
            ?.takeIf { it in SIZES }
            ?: return false

        // Complete the current playlist's track insertions first.
        if (next !is BulkSyncNextMutation.CreatePlaylist) return false
        return appliedCreates(session) - createdAtManualStart >= batchSize
    }

    fun remainingCreates(session: BulkSyncSession): Int =
        session.plan.count { row ->
            row.originalPlanState == BulkSyncPlanState.NEW &&
                row.remotePlaylistId.isNullOrBlank() &&
                row.state != BulkSyncSessionRowState.FAILED
        }
}
