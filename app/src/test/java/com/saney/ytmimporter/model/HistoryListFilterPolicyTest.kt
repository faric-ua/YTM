package com.saney.ytmimporter.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryListFilterPolicyTest {
    @Test
    fun searchAndSemanticFilterCombine() {
        val local =
            entry(
                id = "local",
                playlistName = "House Dance",
                playlistId = null,
                tracks =
                    listOf(
                        track(
                            status =
                                TrackStatus.MATCHED,
                            videoId = "video"
                        )
                    )
            )
        val linked =
            entry(
                id = "linked",
                playlistName = "House Dance Live",
                playlistId = "PL123"
            )
        val other =
            entry(
                id = "other",
                playlistName = "Prodigy",
                playlistId = null
            )

        val result =
            HistoryListFilterPolicy.apply(
                entries =
                    listOf(
                        local,
                        linked,
                        other
                    ),
                query = "house",
                semanticFilter =
                    HistorySemanticFilter
                        .LINKED_YTM
            )

        assertEquals(
            listOf("linked"),
            result.map { it.id }
        )
    }

    @Test
    fun allWithBlankQueryReturnsCompleteList() {
        val entries =
            listOf(
                entry(
                    id = "one"
                ),
                entry(
                    id = "two"
                )
            )

        assertEquals(
            entries,
            HistoryListFilterPolicy
                .apply(
                    entries = entries,
                    query = "",
                    semanticFilter =
                        HistorySemanticFilter
                            .ALL
                )
        )
    }

    @Test
    fun unresolvedHistoryTrackMatchesPendingSearch() {
        val value =
            entry(
                id = "search",
                tracks =
                    listOf(
                        track(
                            status =
                                TrackStatus
                                    .WAITING_QUOTA,
                            videoId = null
                        )
                    )
            )

        assertTrue(
            HistoryListFilterPolicy
                .matchesSemantic(
                    entry = value,
                    filter =
                        HistorySemanticFilter
                            .PENDING_SEARCH
                )
        )
    }

    @Test
    fun pendingWriteAndPauseAreIndependentUsefulViews() {
        val value =
            entry(
                id = "write",
                status =
                    HistoryStatus
                        .PENDING_LIMIT,
                playlistId = "PL123",
                pendingCount = 1,
                lastError =
                    "Забагато запитів"
            )

        assertTrue(
            HistoryListFilterPolicy
                .matchesSemantic(
                    entry = value,
                    filter =
                        HistorySemanticFilter
                            .PENDING_WRITE
                )
        )

        assertTrue(
            HistoryListFilterPolicy
                .matchesSemantic(
                    entry = value,
                    filter =
                        HistorySemanticFilter
                            .PAUSED_OR_ERROR
                )
        )
    }

    private fun entry(
        id: String,
        playlistName: String = "Playlist",
        status: HistoryStatus =
            HistoryStatus.COMPLETED,
        playlistId: String? = null,
        pendingCount: Int = 0,
        lastError: String? = null,
        tracks: List<HistoryTrack> =
            emptyList()
    ): HistoryEntry =
        HistoryEntry(
            id = id,
            createdAt = 1L,
            updatedAt = 1L,
            status = status,
            sourceLabel = "fixture",
            playlistName =
                playlistName,
            playlistId =
                playlistId,
            privacyStatus =
                "private",
            destination =
                PendingDestination
                    .NEW_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle =
                null,
            totalImportedCount =
                tracks.size,
            writeTargetCount =
                tracks.size,
            addedCount = 0,
            failedCount = 0,
            pendingCount =
                pendingCount,
            skippedCount = 0,
            duplicateCount = 0,
            missingCount = 0,
            lastError = lastError,
            tracks = tracks
        )

    private fun track(
        status: TrackStatus,
        videoId: String?
    ): HistoryTrack =
        HistoryTrack(
            index = 0,
            originalTitle = "Track",
            originalArtist = "Artist",
            videoId = videoId,
            selectedTitle = null,
            selectedChannel = null,
            status = status.name,
            manuallySelected = false,
            error = null
        )
}
