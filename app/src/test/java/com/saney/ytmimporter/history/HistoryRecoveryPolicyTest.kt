package com.saney.ytmimporter.history

import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRecoveryPolicyTest {
    @Test
    fun durableSnapshot_winsAndPreservesSearchState() {
        val entry =
            historyEntry(
                localPlaylistId = "local-1",
                playlistId = "remote-1"
            )

        val durable =
            RestorablePlaylistSnapshot(
                localPlaylistId = "local-1",
                sourceHistoryId = entry.id,
                playlist =
                    ImportedPlaylist(
                        name = "Recovered",
                        tracks =
                            mutableListOf(
                                Track(
                                    originalTitle = "Track",
                                    originalArtist = "Artist",
                                    selectedVideoId = "AAAAAAAAAAA",
                                    selectedTitle = "Selected",
                                    selectedChannel = "Channel",
                                    status = TrackStatus.MATCHED,
                                    candidates =
                                        listOf(
                                            SearchCandidate(
                                                videoId = "AAAAAAAAAAA",
                                                title = "Selected",
                                                channelTitle = "Channel",
                                                score = 0.9
                                            )
                                        )
                                )
                            )
                    ),
                sourceLabel = "File",
                createdAt = 1L,
                updatedAt = 2L,
                destinationPlaylistId = "remote-2",
                destinationPlaylistTitle = "Remote title"
            )

        val plan =
            HistoryRecoveryPolicy.plan(
                entry = entry,
                durableSnapshot = durable,
                generatedLocalPlaylistId = "generated"
            )

        assertTrue(plan.usedDurableSnapshot)
        assertEquals("local-1", plan.localPlaylistId)
        assertEquals("remote-2", plan.destinationPlaylistId)
        assertEquals(1, plan.playlist.tracks[0].candidates.size)
        assertEquals(
            TrackStatus.MATCHED,
            plan.playlist.tracks[0].status
        )
        assertTrue(
            plan.playlist.tracks[0]
                .durableExactSelection
        )
    }

    @Test
    fun legacyHistory_fallsBackWithoutRemoteSideEffects() {
        val entry =
            historyEntry(
                localPlaylistId = null,
                playlistId = null,
                tracks =
                    listOf(
                        HistoryTrack(
                            index = 0,
                            originalTitle = "Known",
                            originalArtist = "Artist",
                            videoId = "BBBBBBBBBBB",
                            selectedTitle = "Known result",
                            selectedChannel = "Channel",
                            status = "ADDED",
                            manuallySelected = false,
                            error = null
                        ),
                        HistoryTrack(
                            index = 1,
                            originalTitle = "Unknown",
                            originalArtist = "Artist",
                            videoId = null,
                            selectedTitle = null,
                            selectedChannel = null,
                            status = "MISSING",
                            manuallySelected = false,
                            error = "old error"
                        )
                    )
            )

        val plan =
            HistoryRecoveryPolicy.plan(
                entry = entry,
                durableSnapshot = null,
                generatedLocalPlaylistId = "generated"
            )

        assertFalse(plan.usedDurableSnapshot)
        assertEquals("generated", plan.localPlaylistId)
        assertNull(plan.destinationPlaylistId)
        assertEquals(TrackStatus.MATCHED, plan.playlist.tracks[0].status)
        assertTrue(plan.playlist.tracks[0].durableExactSelection)
        assertEquals(TrackStatus.NEW, plan.playlist.tracks[1].status)
        assertFalse(plan.playlist.tracks[1].durableExactSelection)
        assertNull(plan.playlist.tracks[1].error)
    }

    private fun historyEntry(
        localPlaylistId: String?,
        playlistId: String?,
        tracks: List<HistoryTrack> =
            listOf(
                HistoryTrack(
                    index = 0,
                    originalTitle = "Track",
                    originalArtist = "Artist",
                    videoId = "AAAAAAAAAAA",
                    selectedTitle = "Selected",
                    selectedChannel = "Channel",
                    status = "MATCHED",
                    manuallySelected = false,
                    error = null
                )
            )
    ): HistoryEntry =
        HistoryEntry(
            id = "history-1",
            createdAt = 1L,
            updatedAt = 2L,
            status = HistoryStatus.COMPLETED,
            sourceLabel = "File",
            playlistName = "Playlist",
            playlistId = playlistId,
            privacyStatus = "private",
            destination = PendingDestination.NEW_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            totalImportedCount = tracks.size,
            writeTargetCount = tracks.size,
            addedCount = 0,
            failedCount = 0,
            pendingCount = 0,
            skippedCount = 0,
            duplicateCount = 0,
            missingCount = 0,
            lastError = null,
            tracks = tracks,
            localPlaylistId = localPlaylistId
        )
}
