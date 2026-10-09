package com.saney.ytmimporter.bulk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkCreateBatchPolicyTest {
    @Test
    fun newSession_canPauseBeforeNextCreateButNotBeforeInsert() {
        val base = session(3)
        val threeApplied = base.copy(
            mutationLedger = (1..3).map { appliedCreate(it) }
        )

        assertTrue(
            BulkCreateBatchPolicy.shouldPauseBeforeCreate(
                threeApplied, 0, BulkSyncNextMutation.CreatePlaylist(3, "next")
            )
        )
        assertFalse(
            BulkCreateBatchPolicy.shouldPauseBeforeCreate(
                threeApplied, 0,
                BulkSyncNextMutation.InsertPlaylistItem(
                    2, 0, "insert", "remote-2", "abcdefghijk"
                )
            )
        )
    }

    @Test
    fun nextManualRun_getsItsOwnCreateBudget() {
        val base = session(3).copy(
            mutationLedger = (1..5).map { appliedCreate(it) }
        )
        assertFalse(
            BulkCreateBatchPolicy.shouldPauseBeforeCreate(
                base, 3, BulkSyncNextMutation.CreatePlaylist(5, "next")
            )
        )
        assertTrue(
            BulkCreateBatchPolicy.shouldPauseBeforeCreate(
                base, 2, BulkSyncNextMutation.CreatePlaylist(5, "next")
            )
        )
    }

    @Test
    fun legacyNullBudget_doesNotChangeOldSession() {
        val s = session(null).copy(
            mutationLedger = (1..8).map { appliedCreate(it) }
        )
        assertFalse(
            BulkCreateBatchPolicy.shouldPauseBeforeCreate(
                s, 0, BulkSyncNextMutation.CreatePlaylist(8, "next")
            )
        )
    }

    @Test
    fun noNextCreate_neverTriggersExtraPause() {
        val s = session(1).copy(
            mutationLedger = listOf(appliedCreate(1))
        )
        assertFalse(
            BulkCreateBatchPolicy.shouldPauseBeforeCreate(s, 0, null)
        )
    }

    @Test
    fun choices_haveSafeDefault() {
        assertEquals(setOf(1, 3, 5), BulkCreateBatchPolicy.SIZES)
        assertEquals(3, BulkCreateBatchPolicy.DEFAULT_MAX_CREATES)
    }

    private fun appliedCreate(index: Int): BulkSyncMutation =
        BulkSyncMutation(
            operationId = "create-$index",
            type = BulkSyncMutationType.CREATE_PLAYLIST,
            localPlaylistId = "local-$index",
            remotePlaylistId = "remote-$index",
            videoId = null,
            trackIndex = null,
            status = BulkSyncMutationStatus.APPLIED,
            updatedAt = index.toLong()
        )

    private fun session(batchSize: Int?): BulkSyncSession =
        BulkSyncSession(
            sessionId = "s",
            createdAt = 1L,
            updatedAt = 1L,
            state = BulkSyncSessionState.READY,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            checkpointId = "checkpoint",
            remoteBaseline = BulkSyncRemoteBaseline(
                capturedAt = 1L,
                googleEmail = null,
                youtubeChannelId = null,
                youtubeChannelTitle = null,
                playlists = emptyList()
            ),
            plan = emptyList(),
            currentPlanIndex = 0,
            mutationLedger = emptyList(),
            maxCreatesPerRun = batchSize
        )
}
