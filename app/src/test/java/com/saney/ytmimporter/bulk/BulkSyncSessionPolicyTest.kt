package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkSyncSessionPolicyTest {
    @Test
    fun factory_executesOnlyNewRows() {
        val summary =
            BulkSyncPlanSummary(
                rows =
                    listOf(
                        planRow(
                            id = "new",
                            state =
                                BulkSyncPlanState.NEW,
                            create = true,
                            inserts = 2
                        ),
                        planRow(
                            id = "linked",
                            state =
                                BulkSyncPlanState.LINKED,
                            remoteId =
                                "remote-1",
                            inserts = 1
                        ),
                        planRow(
                            id = "done",
                            state =
                                BulkSyncPlanState
                                    .ALREADY_SYNCED,
                            remoteId =
                                "remote-2"
                        )
                    ),
                estimatedSearchCalls = 0,
                estimatedWriteUnits = 200
            )

        val session =
            BulkSyncSessionFactory.create(
                summary = summary,
                snapshots =
                    listOf(
                        snapshot(
                            id = "new",
                            videos =
                                listOf(
                                    "AAAAAAAAAAA",
                                    "BBBBBBBBBBB"
                                )
                        ),
                        snapshot(
                            id = "linked",
                            videos =
                                listOf(
                                    "CCCCCCCCCCC"
                                ),
                            remoteId =
                                "remote-1"
                        ),
                        snapshot(
                            id = "done",
                            videos =
                                listOf(
                                    "DDDDDDDDDDD"
                                ),
                            remoteId =
                                "remote-2"
                        )
                    ),
                checkpointId =
                    "checkpoint-1",
                baseline =
                    baseline()
            )

        assertEquals(
            BulkSyncSessionRowState.READY,
            session.plan[0].state
        )
        assertEquals(
            2,
            session.plan[0].tracks.size
        )
        assertEquals(
            BulkSyncSessionRowState
                .DEFERRED_LINKED,
            session.plan[1].state
        )
        assertEquals(
            BulkSyncSessionRowState
                .COMPLETED_NOOP,
            session.plan[2].state
        )
    }

    @Test
    fun appliedMutations_areNotScheduledAgain() {
        val base =
            sessionWithNewRow()

        val create =
            BulkSyncExecutionPolicy
                .nextMutation(base)
                as BulkSyncNextMutation
                    .CreatePlaylist

        val afterCreate =
            base.copy(
                plan =
                    listOf(
                        base.plan.single()
                            .copy(
                                state =
                                    BulkSyncSessionRowState
                                        .INSERTING,
                                remotePlaylistId =
                                    "remote-new"
                            )
                    ),
                mutationLedger =
                    listOf(
                        BulkSyncMutation(
                            operationId =
                                create.operationId,
                            type =
                                BulkSyncMutationType
                                    .CREATE_PLAYLIST,
                            localPlaylistId =
                                "new",
                            remotePlaylistId =
                                "remote-new",
                            videoId = null,
                            trackIndex = null,
                            status =
                                BulkSyncMutationStatus
                                    .APPLIED,
                            updatedAt = 1L
                        )
                    )
            )

        val firstInsert =
            BulkSyncExecutionPolicy
                .nextMutation(
                    afterCreate
                )
                as BulkSyncNextMutation
                    .InsertPlaylistItem

        assertEquals(
            "AAAAAAAAAAA",
            firstInsert.videoId
        )

        val afterFirstInsert =
            afterCreate.copy(
                mutationLedger =
                    afterCreate.mutationLedger +
                        BulkSyncMutation(
                            operationId =
                                firstInsert.operationId,
                            type =
                                BulkSyncMutationType
                                    .INSERT_PLAYLIST_ITEM,
                            localPlaylistId =
                                "new",
                            remotePlaylistId =
                                "remote-new",
                            videoId =
                                firstInsert.videoId,
                            trackIndex = 0,
                            status =
                                BulkSyncMutationStatus
                                    .APPLIED,
                            updatedAt = 2L
                        )
            )

        val secondInsert =
            BulkSyncExecutionPolicy
                .nextMutation(
                    afterFirstInsert
                )
                as BulkSyncNextMutation
                    .InsertPlaylistItem

        assertEquals(
            "BBBBBBBBBBB",
            secondInsert.videoId
        )

        val afterAll =
            afterFirstInsert.copy(
                mutationLedger =
                    afterFirstInsert
                        .mutationLedger +
                        BulkSyncMutation(
                            operationId =
                                secondInsert
                                    .operationId,
                            type =
                                BulkSyncMutationType
                                    .INSERT_PLAYLIST_ITEM,
                            localPlaylistId =
                                "new",
                            remotePlaylistId =
                                "remote-new",
                            videoId =
                                secondInsert.videoId,
                            trackIndex = 1,
                            status =
                                BulkSyncMutationStatus
                                    .APPLIED,
                            updatedAt = 3L
                        )
            )

        assertNull(
            BulkSyncExecutionPolicy
                .nextMutation(
                    afterAll
                )
        )
    }

    @Test
    fun coldOpen_neverAutoResumesRunningSession() {
        val running =
            sessionWithNewRow()
                .copy(
                    state =
                        BulkSyncSessionState
                            .RUNNING
                )

        val restored =
            BulkSyncExecutionPolicy
                .normalizeAfterColdOpen(
                    running
                )

        assertEquals(
            BulkSyncSessionState
                .PAUSED_INTERRUPTED,
            restored.state
        )
        assertTrue(
            restored.lastError
                ?.contains(
                    "Автоматичне продовження"
                ) == true
        )
    }

    @Test
    fun preparedInsert_exactRemotePrefix_isApplied() {
        val resolution =
            BulkSyncExecutionPolicy
                .resolvePreparedInsert(
                    expectedVideoIds =
                        listOf(
                            "AAAAAAAAAAA",
                            "BBBBBBBBBBB",
                            "CCCCCCCCCCC"
                        ),
                    preparedIndex = 1,
                    remoteVideoIds =
                        listOf(
                            "AAAAAAAAAAA",
                            "BBBBBBBBBBB"
                        )
                )

        assertTrue(
            resolution is
                BulkSyncPreparedInsertResolution
                    .Applied
        )
    }

    @Test
    fun preparedInsert_missingAtExpectedPosition_isNotApplied() {
        val resolution =
            BulkSyncExecutionPolicy
                .resolvePreparedInsert(
                    expectedVideoIds =
                        listOf(
                            "AAAAAAAAAAA",
                            "BBBBBBBBBBB"
                        ),
                    preparedIndex = 1,
                    remoteVideoIds =
                        listOf(
                            "AAAAAAAAAAA"
                        )
                )

        assertTrue(
            resolution ===
                BulkSyncPreparedInsertResolution
                    .NotApplied
        )
    }

    @Test
    fun preparedInsert_remotePrefixMismatch_isUnknown() {
        val resolution =
            BulkSyncExecutionPolicy
                .resolvePreparedInsert(
                    expectedVideoIds =
                        listOf(
                            "AAAAAAAAAAA",
                            "BBBBBBBBBBB"
                        ),
                    preparedIndex = 1,
                    remoteVideoIds =
                        listOf(
                            "ZZZZZZZZZZZ",
                            "BBBBBBBBBBB"
                        )
                )

        assertTrue(
            resolution ===
                BulkSyncPreparedInsertResolution
                    .Unknown
        )
    }

    @Test
    fun preparedMutation_blocksBlindRetry() {
        val session =
            sessionWithNewRow()
                .copy(
                    mutationLedger =
                        listOf(
                            BulkSyncMutation(
                                operationId =
                                    "op-1",
                                type =
                                    BulkSyncMutationType
                                        .CREATE_PLAYLIST,
                                localPlaylistId =
                                    "new",
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
                                    1L
                            )
                        )
                )

        assertTrue(
            BulkSyncExecutionPolicy
                .hasUncertainPreparedMutation(
                    session
                )
        )
    }

    private fun sessionWithNewRow():
        BulkSyncSession {
        val summary =
            BulkSyncPlanSummary(
                rows =
                    listOf(
                        planRow(
                            id = "new",
                            state =
                                BulkSyncPlanState.NEW,
                            create = true,
                            inserts = 2
                        )
                    ),
                estimatedSearchCalls = 0,
                estimatedWriteUnits = 150
            )

        return BulkSyncSessionFactory
            .create(
                summary = summary,
                snapshots =
                    listOf(
                        snapshot(
                            id = "new",
                            videos =
                                listOf(
                                    "AAAAAAAAAAA",
                                    "BBBBBBBBBBB"
                                )
                        )
                    ),
                checkpointId =
                    "checkpoint-1",
                baseline =
                    baseline()
            )
    }

    private fun planRow(
        id: String,
        state: BulkSyncPlanState,
        remoteId: String? = null,
        create: Boolean = false,
        inserts: Int = 0
    ): BulkSyncPlanRow =
        BulkSyncPlanRow(
            localPlaylistId = id,
            playlistName = id,
            state = state,
            trackCount =
                if (inserts > 0) inserts else 1,
            selectedCount =
                if (inserts > 0) inserts else 1,
            unresolvedCount = 0,
            estimatedSearchCalls = 0,
            cacheHits = 0,
            destinationPlaylistId =
                remoteId,
            plannedCreate =
                create,
            plannedInsertCount =
                inserts,
            estimatedWriteUnits =
                0,
            reason =
                "test"
        )

    private fun snapshot(
        id: String,
        videos: List<String>,
        remoteId: String? = null
    ): RestorablePlaylistSnapshot =
        RestorablePlaylistSnapshot(
            localPlaylistId = id,
            sourceHistoryId = null,
            playlist =
                ImportedPlaylist(
                    name = id,
                    tracks =
                        videos.mapIndexed {
                                index,
                                videoId ->
                            Track(
                                originalTitle =
                                    "Track " +
                                        index,
                                originalArtist =
                                    "Artist",
                                selectedVideoId =
                                    videoId,
                                selectedTitle =
                                    videoId,
                                selectedChannel =
                                    "Channel",
                                status =
                                    TrackStatus.MATCHED,
                                historyIndex =
                                    index
                            )
                        }.toMutableList()
                ),
            sourceLabel =
                id + ".csv",
            createdAt = 1L,
            updatedAt = 1L,
            destinationPlaylistId =
                remoteId,
            destinationPlaylistTitle =
                if (remoteId == null) {
                    null
                } else {
                    id
                }
        )

    private fun baseline():
        BulkSyncRemoteBaseline =
        BulkSyncRemoteBaseline(
            capturedAt = 1L,
            googleEmail =
                "test@example.com",
            youtubeChannelId =
                "channel",
            youtubeChannelTitle =
                "Channel",
            playlists =
                emptyList()
        )
}
