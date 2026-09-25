package com.saney.ytmimporter.search

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchRecoveryPolicyTest {
    @Test
    fun snapshotRoundTrip_preservesSearchStateAndCandidates() {
        val playlist =
            ImportedPlaylist(
                name = "Quota test",
                tracks =
                    mutableListOf(
                        Track(
                            originalTitle = "Ready",
                            originalArtist = "Artist",
                            selectedVideoId = "AAAAAAAAAAA",
                            selectedTitle = "Ready result",
                            selectedChannel = "Channel",
                            status = TrackStatus.MATCHED,
                            candidates =
                                listOf(
                                    SearchCandidate(
                                        videoId = "AAAAAAAAAAA",
                                        title = "Ready result",
                                        channelTitle = "Channel",
                                        score = 0.95
                                    )
                                ),
                            historyIndex = 4
                        ),
                        Track(
                            originalTitle = "Waiting",
                            originalArtist = "Artist",
                            status = TrackStatus.WAITING_QUOTA,
                            error = "quota"
                        )
                    )
            )

        val snapshot =
            SearchRecoveryPolicy.snapshot(
                playlist
            )

        assertEquals(
            1,
            SearchRecoveryPolicy.waitingCount(
                snapshot
            )
        )

        val restored =
            SearchRecoveryPolicy.restore(
                snapshot
            )

        assertEquals(
            "Quota test",
            restored.name
        )
        assertEquals(
            TrackStatus.MATCHED,
            restored.tracks[0].status
        )
        assertEquals(
            "AAAAAAAAAAA",
            restored.tracks[0].selectedVideoId
        )
        assertEquals(
            4,
            restored.tracks[0].historyIndex
        )
        assertEquals(
            1,
            restored.tracks[0].candidates.size
        )
        assertEquals(
            TrackStatus.WAITING_QUOTA,
            restored.tracks[1].status
        )
        assertNull(
            restored.tracks[1].selectedVideoId
        )
    }

    @Test
    fun workspaceKey_isStableForSameOrderedSourceAndTracks() {
        val playlist =
            ImportedPlaylist(
                name = "Same",
                tracks =
                    mutableListOf(
                        Track("One", "A"),
                        Track("Two", "B")
                    )
            )

        assertEquals(
            SearchRecoveryPolicy.workspaceKey(
                "TXT",
                playlist
            ),
            SearchRecoveryPolicy.workspaceKey(
                "TXT",
                playlist
            )
        )

        assertNotEquals(
            SearchRecoveryPolicy.workspaceKey(
                "TXT",
                playlist
            ),
            SearchRecoveryPolicy.workspaceKey(
                "Other",
                playlist
            )
        )
    }
}
