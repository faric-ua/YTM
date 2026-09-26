package com.saney.ytmimporter.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistLinkagePolicyTest {
    @Test
    fun resolvedWithoutRemoteIdIsLocalOnly() {
        assertEquals(
            PlaylistLinkageState.LOCAL_ONLY,
            PlaylistLinkagePolicy.current(
                tracks =
                    listOf(
                        Track(
                            originalTitle = "Track",
                            originalArtist = "Artist",
                            selectedVideoId = "video",
                            status = TrackStatus.MATCHED
                        )
                    ),
                destinationPlaylistId = null
            )
        )
    }

    @Test
    fun remoteIdMakesResolvedWorkspaceLinked() {
        assertEquals(
            PlaylistLinkageState.LINKED_YTM,
            PlaylistLinkagePolicy.current(
                tracks =
                    listOf(
                        Track(
                            originalTitle = "Track",
                            originalArtist = "Artist",
                            selectedVideoId = "video",
                            status = TrackStatus.ADDED
                        )
                    ),
                destinationPlaylistId = "PL123"
            )
        )
    }

    @Test
    fun pendingWriteOverridesExistingRemoteLink() {
        assertEquals(
            PlaylistLinkageState.PENDING_WRITE,
            PlaylistLinkagePolicy.current(
                tracks =
                    listOf(
                        Track(
                            originalTitle = "Track",
                            originalArtist = "Artist",
                            selectedVideoId = "video",
                            status = TrackStatus.PENDING
                        )
                    ),
                destinationPlaylistId = "PL123"
            )
        )
    }

    @Test
    fun unresolvedSearchOverridesExistingRemoteLink() {
        assertEquals(
            PlaylistLinkageState.PENDING_SEARCH,
            PlaylistLinkagePolicy.current(
                tracks =
                    listOf(
                        Track(
                            originalTitle = "Track",
                            originalArtist = "Artist",
                            status = TrackStatus.WAITING_QUOTA
                        )
                    ),
                destinationPlaylistId = "PL123"
            )
        )
    }

    @Test
    fun exactNewTrackDoesNotPretendSearchIsPending() {
        assertEquals(
            PlaylistLinkageState.LOCAL_ONLY,
            PlaylistLinkagePolicy.current(
                tracks =
                    listOf(
                        Track(
                            originalTitle = "Track",
                            originalArtist = "Artist",
                            selectedVideoId = "video",
                            status = TrackStatus.NEW
                        )
                    ),
                destinationPlaylistId = null
            )
        )
    }

    @Test
    fun pausedHistoryWriteOverridesRemoteLink() {
        val entry =
            historyEntry(
                status =
                    HistoryStatus.PENDING_LIMIT,
                playlistId = "PL123",
                pendingCount = 1
            )

        assertEquals(
            PlaylistLinkageState.PENDING_WRITE,
            PlaylistLinkagePolicy.history(entry)
        )
    }

    @Test
    fun finalPartialHistoryWithRemoteIdIsStillLinked() {
        assertEquals(
            PlaylistLinkageState.LINKED_YTM,
            PlaylistLinkagePolicy.history(
                historyEntry(
                    status =
                        HistoryStatus.PARTIAL,
                    playlistId = "PL123",
                    pendingCount = 0
                )
            )
        )
    }

    @Test
    fun completedHistoryWithRemoteIdIsLinked() {
        assertEquals(
            PlaylistLinkageState.LINKED_YTM,
            PlaylistLinkagePolicy.history(
                historyEntry(
                    status =
                        HistoryStatus.COMPLETED,
                    playlistId = "PL123",
                    pendingCount = 0
                )
            )
        )
    }

    private fun historyEntry(
        status: HistoryStatus,
        playlistId: String?,
        pendingCount: Int
    ): HistoryEntry =
        HistoryEntry(
            id = "history",
            createdAt = 1L,
            updatedAt = 1L,
            status = status,
            sourceLabel = "test",
            playlistName = "Playlist",
            playlistId = playlistId,
            privacyStatus = "private",
            destination =
                PendingDestination.NEW_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            totalImportedCount = 1,
            writeTargetCount = 1,
            addedCount = 0,
            failedCount = 0,
            pendingCount = pendingCount,
            skippedCount = 0,
            duplicateCount = 0,
            missingCount = 0,
            lastError = null,
            tracks = emptyList()
        )
}
