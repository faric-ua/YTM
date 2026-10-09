package com.saney.ytmimporter.recovery

import com.saney.ytmimporter.bulk.BulkSyncMutation
import com.saney.ytmimporter.bulk.BulkSyncMutationStatus
import com.saney.ytmimporter.bulk.BulkSyncMutationType
import com.saney.ytmimporter.bulk.BulkSyncPlanState
import com.saney.ytmimporter.bulk.BulkSyncRemoteBaseline
import com.saney.ytmimporter.bulk.BulkSyncSession
import com.saney.ytmimporter.bulk.BulkSyncSessionRow
import com.saney.ytmimporter.bulk.BulkSyncSessionRowState
import com.saney.ytmimporter.bulk.BulkSyncSessionState
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingOperation
import com.saney.ytmimporter.model.PendingPauseReason
import com.saney.ytmimporter.model.PendingTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCenterPolicyTest {
    @Test
    fun pendingRateLimitJobIsActionable() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions = emptyList(),
                pendingJobs =
                    listOf(
                        pendingJob(
                            id = "pending-1",
                            pauseReason =
                                PendingPauseReason.RATE_LIMIT
                        )
                    ),
                historyEntries = emptyList()
            )

        assertEquals(
            1,
            snapshot.actionableCount
        )
        assertEquals(
            RecoverySource.PENDING,
            snapshot.actionableItems.single().source
        )
        assertTrue(
            snapshot.actionableItems
                .single()
                .stateLabel
                .contains("ліміт запитів")
        )
    }

    @Test
    fun completedBulkSessionDoesNotNeedAttention() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions =
                    listOf(
                        bulkSession(
                            state =
                                BulkSyncSessionState.COMPLETED
                        )
                    ),
                pendingJobs = emptyList(),
                historyEntries = emptyList()
            )

        assertTrue(snapshot.items.isEmpty())
    }

    @Test
    fun interruptedRunningBulkSessionIsActionable() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions =
                    listOf(
                        bulkSession(
                            state =
                                BulkSyncSessionState.RUNNING
                        )
                    ),
                pendingJobs = emptyList(),
                historyEntries = emptyList()
            )

        val item =
            snapshot.actionableItems.single()

        assertEquals(
            RecoveryRoute.BULK_SESSION,
            item.route
        )
        assertEquals(
            "bulk-1",
            item.routeId
        )
    }

    @Test
    fun pausedCreationBatchRemainsActionableNotFailure() {
        val snapshot = RecoveryCenterPolicy.build(
            bulkSessions = listOf(
                bulkSession(state = BulkSyncSessionState.PAUSED_CREATE_BATCH)
            ),
            pendingJobs = emptyList(),
            historyEntries = emptyList()
        )

        assertEquals(1, snapshot.actionableCount)
        assertEquals(RecoveryRoute.BULK_SESSION, snapshot.actionableItems.single().route)
        assertTrue(snapshot.actionableItems.single().stateLabel.contains("Пакет створення"))
    }

    @Test
    fun rollbackPausedShowsExactAppliedRemainingCount() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions =
                    listOf(
                        bulkSession(
                            state =
                                BulkSyncSessionState.ROLLBACK_PAUSED,
                            mutationStatuses =
                                listOf(
                                    BulkSyncMutationStatus.ROLLED_BACK,
                                    BulkSyncMutationStatus.APPLIED
                                )
                        )
                    ),
                pendingJobs = emptyList(),
                historyEntries = emptyList()
            )

        val item =
            snapshot.actionableItems.single()

        assertEquals(
            "Залишилось відкотити дій: 1",
            item.remainingLabel
        )
        assertTrue(
            item.happenedLabel
                .contains("відкочено: 1")
        )
    }

    @Test
    fun terminalPartialBulkIsWarningNotActionable() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions =
                    listOf(
                        bulkSession(
                            state =
                                BulkSyncSessionState.PARTIAL_FAILED,
                            mutationStatuses =
                                listOf(
                                    BulkSyncMutationStatus.TERMINAL_FAILED
                                )
                        )
                    ),
                pendingJobs = emptyList(),
                historyEntries = emptyList()
            )

        assertEquals(
            0,
            snapshot.actionableCount
        )
        assertEquals(
            1,
            snapshot.warningItems.size
        )
    }

    @Test
    fun pendingJobSuppressesSameStableHistoryIdentity() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions = emptyList(),
                pendingJobs =
                    listOf(
                        pendingJob(
                            id = "pending-1",
                            localPlaylistId = "local-1"
                        )
                    ),
                historyEntries =
                    listOf(
                        historyEntry(
                            id = "history-1",
                            status =
                                HistoryStatus.PENDING_LIMIT,
                            localPlaylistId = "local-1"
                        )
                    )
            )

        assertEquals(
            1,
            snapshot.items.size
        )
        assertEquals(
            RecoverySource.PENDING,
            snapshot.items.single().source
        )
    }

    @Test
    fun sameTitleWithoutStableIdentityNeverDeduplicates() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions = emptyList(),
                pendingJobs =
                    listOf(
                        pendingJob(
                            id = "pending-1",
                            localPlaylistId = null,
                            playlistId = null
                        )
                    ),
                historyEntries =
                    listOf(
                        historyEntry(
                            id = "history-1",
                            status =
                                HistoryStatus.PENDING_LIMIT,
                            localPlaylistId = null,
                            playlistId = null
                        )
                    )
            )

        assertEquals(
            2,
            snapshot.actionableCount
        )
    }

    @Test
    fun completedHistoryWithFailureIsWarningOnly() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions = emptyList(),
                pendingJobs = emptyList(),
                historyEntries =
                    listOf(
                        historyEntry(
                            id = "history-1",
                            status =
                                HistoryStatus.COMPLETED,
                            failedCount = 1
                        )
                    )
            )

        assertEquals(
            0,
            snapshot.actionableCount
        )
        assertEquals(
            1,
            snapshot.warningItems.size
        )
    }

    @Test
    fun normalCompletedHistoryIsExcluded() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions = emptyList(),
                pendingJobs = emptyList(),
                historyEntries =
                    listOf(
                        historyEntry(
                            id = "history-1",
                            status =
                                HistoryStatus.COMPLETED
                        )
                    )
            )

        assertTrue(snapshot.items.isEmpty())
    }

    @Test
    fun actionableItemsSortBeforeWarningsThenByNewest() {
        val snapshot =
            RecoveryCenterPolicy.build(
                bulkSessions = emptyList(),
                pendingJobs =
                    listOf(
                        pendingJob(
                            id = "old-action",
                            updatedAt = 10L
                        )
                    ),
                historyEntries =
                    listOf(
                        historyEntry(
                            id = "new-warning",
                            status =
                                HistoryStatus.COMPLETED,
                            failedCount = 1,
                            updatedAt = 100L
                        ),
                        historyEntry(
                            id = "new-action",
                            status =
                                HistoryStatus.PENDING_QUOTA,
                            updatedAt = 20L
                        )
                    )
            )

        assertEquals(
            listOf(
                "history:new-action",
                "pending:old-action",
                "history:new-warning"
            ),
            snapshot.items.map { it.key }
        )
    }

    private fun pendingJob(
        id: String,
        updatedAt: Long = 20L,
        localPlaylistId: String? = "local-1",
        playlistId: String? = "remote-1",
        pauseReason: PendingPauseReason? =
            PendingPauseReason.DAILY_QUOTA
    ): PendingJob =
        PendingJob(
            id = id,
            createdAt = 1L,
            updatedAt = updatedAt,
            sourceLabel = "File",
            playlistName = "Playlist",
            playlistId = playlistId,
            privacyStatus = "private",
            destination =
                PendingDestination.EXISTING_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            totalCount = 3,
            addedCount = 0,
            failedCount = 0,
            remainingTracks =
                listOf(
                    PendingTrack(
                        originalTitle = "Track",
                        originalArtist = "Artist",
                        videoId = "AAAAAAAAAAA",
                        selectedTitle = "Track",
                        selectedChannel = "Channel",
                        historyIndex = 0
                    )
                ),
            lastError = "Paused",
            pauseReason = pauseReason,
            operation = PendingOperation.WRITE,
            destinationPlaylistTitle = "Playlist",
            localPlaylistId = localPlaylistId
        )

    private fun historyEntry(
        id: String,
        status: HistoryStatus,
        localPlaylistId: String? = "local-history",
        playlistId: String? = "remote-history",
        failedCount: Int = 0,
        updatedAt: Long = 30L
    ): HistoryEntry =
        HistoryEntry(
            id = id,
            createdAt = 1L,
            updatedAt = updatedAt,
            status = status,
            sourceLabel = "File",
            playlistName = "Playlist",
            playlistId = playlistId,
            privacyStatus = "private",
            destination =
                PendingDestination.EXISTING_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            totalImportedCount = 3,
            writeTargetCount = 3,
            addedCount = 0,
            failedCount = failedCount,
            pendingCount =
                if (
                    status in
                    setOf(
                        HistoryStatus.RUNNING,
                        HistoryStatus.PENDING_QUOTA,
                        HistoryStatus.PENDING_LIMIT
                    )
                ) {
                    1
                } else {
                    0
                },
            skippedCount = 0,
            duplicateCount = 0,
            missingCount = 0,
            lastError =
                if (failedCount > 0) {
                    "Failure"
                } else {
                    null
                },
            tracks = emptyList(),
            localPlaylistId = localPlaylistId
        )

    private fun bulkSession(
        state: BulkSyncSessionState,
        mutationStatuses:
            List<BulkSyncMutationStatus> =
                emptyList()
    ): BulkSyncSession =
        BulkSyncSession(
            sessionId = "bulk-1",
            createdAt = 1L,
            updatedAt = 40L,
            state = state,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            checkpointId = "checkpoint-1",
            remoteBaseline =
                BulkSyncRemoteBaseline(
                    capturedAt = 1L,
                    googleEmail = null,
                    youtubeChannelId = null,
                    youtubeChannelTitle = null,
                    playlists = emptyList()
                ),
            plan =
                listOf(
                    BulkSyncSessionRow(
                        localPlaylistId = "bulk-local-1",
                        playlistName = "Bulk Playlist",
                        sourceLabel = "File",
                        originalPlanState =
                            BulkSyncPlanState.NEW,
                        state =
                            if (
                                state ==
                                BulkSyncSessionState.COMPLETED
                            ) {
                                BulkSyncSessionRowState.COMPLETED
                            } else {
                                BulkSyncSessionRowState.INSERTING
                            }
                    )
                ),
            currentPlanIndex = 0,
            mutationLedger =
                mutationStatuses
                    .mapIndexed {
                            index,
                            status ->
                        BulkSyncMutation(
                            operationId =
                                "op-" + index,
                            type =
                                BulkSyncMutationType
                                    .INSERT_PLAYLIST_ITEM,
                            localPlaylistId =
                                "bulk-local-1",
                            remotePlaylistId =
                                "remote-1",
                            videoId =
                                "AAAAAAAAAAA",
                            trackIndex = index,
                            createdPlaylistItemId =
                                "item-" + index,
                            status = status,
                            error =
                                if (
                                    status ==
                                    BulkSyncMutationStatus
                                        .TERMINAL_FAILED
                                ) {
                                    "Failed"
                                } else {
                                    null
                                },
                            updatedAt =
                                10L + index
                        )
                    },
            lastError =
                if (
                    state ==
                    BulkSyncSessionState.COMPLETED
                ) {
                    null
                } else {
                    "Needs attention"
                }
        )
}
