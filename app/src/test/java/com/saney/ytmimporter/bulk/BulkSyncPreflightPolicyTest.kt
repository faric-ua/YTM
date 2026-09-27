package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingOperation
import com.saney.ytmimporter.model.PendingTrack
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.search.SearchRecoveryPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkSyncPreflightPolicyTest {
    @Test
    fun localReadyWithoutRemoteId_isNew() {
        val row =
            plan(
                local(
                    id = "local-1",
                    remoteId = null,
                    tracks =
                        listOf(
                            ready("AAAAAAAAAAA"),
                            ready("BBBBBBBBBBB")
                        )
                )
            ).rows.single()

        assertEquals(BulkSyncPlanState.NEW, row.state)
        assertTrue(row.plannedCreate)
        assertEquals(2, row.plannedInsertCount)
        assertEquals(150, row.estimatedWriteUnits)
    }

    @Test
    fun linkedMissingOccurrences_isLinkedAndKeepsDuplicates() {
        val row =
            plan(
                local(
                    id = "local-1",
                    remoteId = "remote-1",
                    tracks =
                        listOf(
                            ready("AAAAAAAAAAA"),
                            ready("AAAAAAAAAAA"),
                            ready("BBBBBBBBBBB")
                        )
                ),
                remote =
                    remote(
                        "remote-1" to
                            listOf("AAAAAAAAAAA")
                    )
            ).rows.single()

        assertEquals(BulkSyncPlanState.LINKED, row.state)
        assertEquals(2, row.plannedInsertCount)
        assertEquals(100, row.estimatedWriteUnits)
    }

    @Test
    fun linkedContainingAllOccurrences_isAlreadySynced() {
        val row =
            plan(
                local(
                    id = "local-1",
                    remoteId = "remote-1",
                    tracks =
                        listOf(
                            ready("AAAAAAAAAAA"),
                            ready("AAAAAAAAAAA")
                        )
                ),
                remote =
                    remote(
                        "remote-1" to
                            listOf(
                                "AAAAAAAAAAA",
                                "AAAAAAAAAAA",
                                "CCCCCCCCCCC"
                            )
                    )
            ).rows.single()

        assertEquals(BulkSyncPlanState.ALREADY_SYNCED, row.state)
    }

    @Test
    fun unresolvedTrack_isNeedsSearchWithSeparateEstimate() {
        val summary =
            plan(
                local(
                    id = "local-1",
                    remoteId = null,
                    tracks =
                        listOf(
                            ready("AAAAAAAAAAA"),
                            Track(
                                originalTitle = "Missing",
                                originalArtist = "Artist",
                                status = TrackStatus.NEW
                            )
                        ),
                    estimatedSearchCalls = 1
                )
            )

        val row = summary.rows.single()

        assertEquals(BulkSyncPlanState.NEEDS_SEARCH, row.state)
        assertEquals(1, row.estimatedSearchCalls)
        assertEquals(0, row.estimatedWriteUnits)
        assertEquals(1, summary.estimatedSearchCalls)
    }

    @Test
    fun searchAndOtherApiEstimates_staySeparate() {
        val summary =
            BulkSyncPreflightPolicy.build(
                localPlaylists =
                    listOf(
                        local(
                            id = "needs-search",
                            remoteId = null,
                            tracks =
                                listOf(
                                    Track(
                                        originalTitle = "Missing",
                                        originalArtist = "Artist",
                                        status = TrackStatus.NEW
                                    )
                                ),
                            estimatedSearchCalls = 1
                        ),
                        local(
                            id = "ready-new",
                            remoteId = null,
                            tracks =
                                listOf(
                                    ready("AAAAAAAAAAA")
                                )
                        )
                    ),
                pendingJobs = emptyList(),
                connected = true,
                remote =
                    BulkSyncRemoteSnapshot(
                        inventoryAvailable = true,
                        ownedPlaylistIds = emptySet(),
                        orderedVideoIdsByPlaylistId = emptyMap()
                    )
            )

        assertEquals(
            1,
            summary.estimatedSearchCalls
        )
        assertEquals(
            100,
            summary.estimatedWriteUnits
        )
        assertEquals(
            0,
            summary.rows
                .first {
                    it.localPlaylistId ==
                        "needs-search"
                }
                .estimatedWriteUnits
        )
        assertEquals(
            0,
            summary.rows
                .first {
                    it.localPlaylistId ==
                        "ready-new"
                }
                .estimatedSearchCalls
        )
    }

    @Test
    fun pendingLocalPlaylistId_hasPriority() {
        val summary =
            plan(
                local(
                    id = "local-1",
                    remoteId = null,
                    tracks = listOf(ready("AAAAAAAAAAA"))
                ),
                pending =
                    listOf(
                        pendingJob(
                            operation = PendingOperation.WRITE,
                            localPlaylistId = "local-1"
                        )
                    )
            )

        assertEquals(
            BulkSyncPlanState.PENDING,
            summary.rows.single().state
        )
    }

    @Test
    fun legacyWriteWithoutIds_matchesExactPendingTrackIdentity() {
        val pendingTrack =
            Track(
                originalTitle = "One",
                originalArtist = "Artist",
                selectedVideoId = "AAAAAAAAAAA",
                selectedTitle = "One",
                selectedChannel = "Channel",
                status = TrackStatus.PENDING,
                historyIndex = 7
            )

        val summary =
            plan(
                local(
                    id = "local-legacy",
                    remoteId = null,
                    tracks = listOf(pendingTrack)
                ),
                pending =
                    listOf(
                        pendingJob(
                            operation = PendingOperation.WRITE,
                            localPlaylistId = null,
                            remainingTracks =
                                listOf(
                                    PendingTrack(
                                        originalTitle = "One",
                                        originalArtist = "Artist",
                                        videoId = "AAAAAAAAAAA",
                                        selectedTitle = "One",
                                        selectedChannel = "Channel",
                                        historyIndex = 7
                                    )
                                )
                        )
                    )
            )

        assertEquals(
            BulkSyncPlanState.PENDING,
            summary.rows.single().state
        )
    }

    @Test
    fun legacyWriteWithoutIds_doesNotMatchByPlaylistTitleOnly() {
        val summary =
            plan(
                local(
                    id = "local-legacy",
                    remoteId = null,
                    tracks =
                        listOf(
                            Track(
                                originalTitle = "One",
                                originalArtist = "Artist",
                                selectedVideoId = "AAAAAAAAAAA",
                                selectedTitle = "One",
                                selectedChannel = "Channel",
                                status = TrackStatus.PENDING,
                                historyIndex = 7
                            )
                        )
                ),
                pending =
                    listOf(
                        pendingJob(
                            operation = PendingOperation.WRITE,
                            localPlaylistId = null,
                            remainingTracks =
                                listOf(
                                    PendingTrack(
                                        originalTitle = "Different",
                                        originalArtist = "Artist",
                                        videoId = "BBBBBBBBBBB",
                                        selectedTitle = "Different",
                                        selectedChannel = "Channel",
                                        historyIndex = 7
                                    )
                                )
                        )
                    )
            )

        assertEquals(
            BulkSyncPlanState.BLOCKED,
            summary.rows.single().state
        )
    }

    @Test
    fun legacyWriteWithoutIds_requiresUniqueLocalOwner() {
        val first =
            local(
                id = "local-1",
                remoteId = null,
                tracks =
                    listOf(
                        Track(
                            originalTitle = "One",
                            originalArtist = "Artist",
                            selectedVideoId = "AAAAAAAAAAA",
                            selectedTitle = "One",
                            selectedChannel = "Channel",
                            status = TrackStatus.PENDING,
                            historyIndex = 7
                        )
                    )
            )

        val second =
            first.copy(
                localPlaylistId = "local-2"
            )

        val summary =
            BulkSyncPreflightPolicy.build(
                localPlaylists =
                    listOf(
                        first,
                        second
                    ),
                pendingJobs =
                    listOf(
                        pendingJob(
                            operation = PendingOperation.WRITE,
                            localPlaylistId = null,
                            remainingTracks =
                                listOf(
                                    PendingTrack(
                                        originalTitle = "One",
                                        originalArtist = "Artist",
                                        videoId = "AAAAAAAAAAA",
                                        selectedTitle = "One",
                                        selectedChannel = "Channel",
                                        historyIndex = 7
                                    )
                                )
                        )
                    ),
                connected = true,
                remote =
                    BulkSyncRemoteSnapshot(
                        inventoryAvailable = true,
                        ownedPlaylistIds = emptySet(),
                        orderedVideoIdsByPlaylistId = emptyMap()
                    )
            )

        assertTrue(
            summary.rows.all {
                it.state == BulkSyncPlanState.BLOCKED
            }
        )
    }

    @Test
    fun legacySearchRecoveryKey_matchesWithoutTitleGuessing() {
        val local =
            local(
                id = "local-1",
                remoteId = null,
                tracks =
                    listOf(
                        Track(
                            originalTitle = "One",
                            originalArtist = "Artist",
                            status = TrackStatus.WAITING_QUOTA
                        )
                    ),
                estimatedSearchCalls = 1
            )

        val key =
            SearchRecoveryPolicy.workspaceKey(
                sourceLabel = local.sourceLabel,
                playlist =
                    ImportedPlaylist(
                        name = local.playlistName,
                        tracks = local.tracks.toMutableList()
                    )
            )

        val summary =
            plan(
                local,
                pending =
                    listOf(
                        pendingJob(
                            operation = PendingOperation.SEARCH,
                            localPlaylistId = null,
                            recoveryKey = key
                        )
                    )
            )

        assertEquals(
            BulkSyncPlanState.PENDING,
            summary.rows.single().state
        )
    }

    @Test
    fun persistedRemoteIdNotOwnedByCurrentAccount_isBlocked() {
        val summary =
            plan(
                local(
                    id = "local-1",
                    remoteId = "remote-foreign",
                    tracks = listOf(ready("AAAAAAAAAAA"))
                ),
                remote =
                    BulkSyncRemoteSnapshot(
                        inventoryAvailable = true,
                        ownedPlaylistIds = setOf("remote-other"),
                        orderedVideoIdsByPlaylistId = emptyMap()
                    )
            )

        assertEquals(
            BulkSyncPlanState.BLOCKED,
            summary.rows.single().state
        )
    }

    @Test
    fun equalTitleNeverCreatesRemoteLinkage() {
        val summary =
            plan(
                local(
                    id = "local-1",
                    remoteId = null,
                    tracks = listOf(ready("AAAAAAAAAAA")),
                    name = "Same title"
                ),
                remote =
                    remote(
                        "remote-1" to
                            listOf("AAAAAAAAAAA")
                    )
            )

        assertEquals(
            BulkSyncPlanState.NEW,
            summary.rows.single().state
        )
    }

    private fun plan(
        local: BulkSyncLocalPlaylist,
        pending: List<PendingJob> = emptyList(),
        remote: BulkSyncRemoteSnapshot =
            BulkSyncRemoteSnapshot(
                inventoryAvailable = true,
                ownedPlaylistIds = emptySet(),
                orderedVideoIdsByPlaylistId = emptyMap()
            )
    ): BulkSyncPlanSummary =
        BulkSyncPreflightPolicy.build(
            localPlaylists = listOf(local),
            pendingJobs = pending,
            connected = true,
            remote = remote
        )

    private fun local(
        id: String,
        remoteId: String?,
        tracks: List<Track>,
        estimatedSearchCalls: Int = 0,
        cacheHits: Int = 0,
        name: String = "Playlist"
    ): BulkSyncLocalPlaylist =
        BulkSyncLocalPlaylist(
            localPlaylistId = id,
            playlistName = name,
            sourceLabel = "file.csv",
            tracks = tracks,
            destinationPlaylistId = remoteId,
            estimatedSearchCalls = estimatedSearchCalls,
            cacheHits = cacheHits
        )

    private fun ready(videoId: String): Track =
        Track(
            originalTitle = videoId,
            originalArtist = "Artist",
            selectedVideoId = videoId,
            selectedTitle = videoId,
            selectedChannel = "Channel",
            status = TrackStatus.MATCHED,
            durableExactSelection = true
        )

    private fun remote(
        vararg entries: Pair<String, List<String>>
    ): BulkSyncRemoteSnapshot {
        val map = entries.toMap()

        return BulkSyncRemoteSnapshot(
            inventoryAvailable = true,
            ownedPlaylistIds = map.keys,
            orderedVideoIdsByPlaylistId = map
        )
    }

    private fun pendingJob(
        operation: PendingOperation,
        localPlaylistId: String?,
        recoveryKey: String? = null,
        remainingTracks: List<PendingTrack> = emptyList()
    ): PendingJob =
        PendingJob(
            id = "job-1",
            createdAt = 1L,
            updatedAt = 1L,
            sourceLabel = "file.csv",
            playlistName = "Playlist",
            playlistId = null,
            privacyStatus = "private",
            destination = PendingDestination.NEW_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            totalCount = 1,
            addedCount = 0,
            failedCount = 0,
            remainingTracks = remainingTracks,
            lastError = null,
            operation = operation,
            recoveryKey = recoveryKey,
            localPlaylistId = localPlaylistId
        )
}
