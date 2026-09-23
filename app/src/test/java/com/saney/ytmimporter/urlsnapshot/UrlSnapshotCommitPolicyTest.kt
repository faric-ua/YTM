package com.saney.ytmimporter.urlsnapshot

import com.saney.ytmimporter.model.TrackStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSnapshotCommitPolicyTest {
    @Test
    fun exactTracks_keepOrderDuplicatesAndCanonicalSelections() {
        val resolved =
            resolved(
                listOf(
                    available(
                        index = 0,
                        videoId = "AAAAAAAAAAA",
                        title = "First"
                    ),
                    available(
                        index = 1,
                        videoId = "BBBBBBBBBBB",
                        title = "Second"
                    ),
                    available(
                        index = 2,
                        videoId = "AAAAAAAAAAA",
                        title = "First duplicate"
                    )
                )
            )

        val plan =
            UrlSnapshotCommitPolicy
                .buildPlan(
                    resolved
                )

        assertEquals(
            listOf(
                "AAAAAAAAAAA",
                "BBBBBBBBBBB",
                "AAAAAAAAAAA"
            ),
            plan.playlist
                .tracks
                .map {
                    it.selectedVideoId
                }
        )

        assertEquals(
            listOf(
                0,
                1,
                2
            ),
            plan.playlist
                .tracks
                .map {
                    it.historyIndex
                }
        )

        assertTrue(
            plan.playlist
                .tracks
                .all {
                    it.status ==
                        TrackStatus.MATCHED &&
                        it.candidates.isEmpty()
                }
        )

        assertEquals(
            3,
            plan.availableCount
        )

        assertEquals(
            0,
            plan.unavailableCount
        )
    }

    @Test
    fun unavailableRows_remainInSnapshotAndKeepExactIdWhenExposed() {
        val resolved =
            resolved(
                listOf(
                    unavailable(
                        index = 0,
                        videoId =
                            "PRIVATE0001",
                        title =
                            "[Private video]",
                        reason =
                            UrlSnapshotUnavailableReason
                                .PRIVATE_VIDEO
                    ),
                    unavailable(
                        index = 1,
                        videoId =
                            null,
                        title =
                            null,
                        reason =
                            UrlSnapshotUnavailableReason
                                .MISSING_VIDEO_ID
                    )
                )
            )

        val plan =
            UrlSnapshotCommitPolicy
                .buildPlan(
                    resolved
                )

        assertEquals(
            2,
            plan.playlist
                .tracks
                .size
        )

        val privateTrack =
            plan.playlist
                .tracks[0]

        assertEquals(
            TrackStatus.MISSING,
            privateTrack.status
        )

        assertEquals(
            "PRIVATE0001",
            privateTrack.selectedVideoId
        )

        assertTrue(
            privateTrack.error
                .orEmpty()
                .contains(
                    "приватне",
                    ignoreCase = true
                )
        )

        val missingId =
            plan.playlist
                .tracks[1]

        assertEquals(
            TrackStatus.MISSING,
            missingId.status
        )

        assertNull(
            missingId.selectedVideoId
        )

        assertTrue(
            missingId.error
                .orEmpty()
                .contains(
                    "videoId",
                    ignoreCase = true
                )
        )

        assertEquals(
            0,
            plan.availableCount
        )

        assertEquals(
            2,
            plan.unavailableCount
        )
    }

    @Test
    fun sourceLabelAndPlaylistNameAreStableLocalSnapshotIdentity() {
        val plan =
            UrlSnapshotCommitPolicy
                .buildPlan(
                    resolved(
                        listOf(
                            available(
                                index = 0,
                                videoId =
                                    "dQw4w9WgXcQ",
                                title =
                                    "Exact"
                            )
                        ),
                        playlistId =
                            "PL1234567890ABCDEFGHIJ"
                    )
                )

        assertEquals(
            "URL snapshot (PL1234567890ABCDEFGHIJ)",
            plan.sourceLabel
        )

        assertTrue(
            plan.playlist.name
                .startsWith(
                    "URL snapshot • PL1234567890"
                )
        )
    }

    @Test
    fun unavailableRowDoesNotBecomeSearchCandidateDuringCommitMapping() {
        val track =
            UrlSnapshotCommitPolicy
                .buildPlan(
                    resolved(
                        listOf(
                            unavailable(
                                index = 0,
                                videoId =
                                    "DELETED0001",
                                title =
                                    "[Deleted video]",
                                reason =
                                    UrlSnapshotUnavailableReason
                                        .DELETED_VIDEO
                            )
                        )
                    )
                )
                .playlist
                .tracks
                .single()

        assertTrue(
            track.candidates.isEmpty()
        )

        assertEquals(
            false,
            track.manuallySelected
        )
    }

    private fun resolved(
        items:
            List<UrlSnapshotResolvedItem>,
        playlistId: String =
            "PLcommit"
    ):
        UrlSnapshotResolutionResult.Resolved =
        UrlSnapshotResolutionResult.Resolved(
            source =
                UrlSnapshotSource(
                    originalUrl =
                        "https://www.youtube.com/playlist?list=$playlistId",
                    canonicalUrl =
                        "https://www.youtube.com/playlist?list=$playlistId",
                    playlistId =
                        playlistId,
                    kind =
                        UrlSnapshotSourceKind
                            .CONCRETE_PLAYLIST,
                    surface =
                        UrlSnapshotSurface.YOUTUBE,
                    contextVideoId =
                        null
                ),
            items =
                items,
            requestCount =
                1
        )

    private fun available(
        index: Int,
        videoId: String,
        title: String
    ): UrlSnapshotResolvedItem =
        UrlSnapshotResolvedItem(
            index =
                index,
            playlistItemId =
                "item-$index",
            sourcePosition =
                index,
            videoId =
                videoId,
            title =
                title,
            channelTitle =
                "Channel $index",
            availability =
                UrlSnapshotAvailability
                    .AVAILABLE,
            unavailableReason =
                null
        )

    private fun unavailable(
        index: Int,
        videoId: String?,
        title: String?,
        reason:
            UrlSnapshotUnavailableReason
    ): UrlSnapshotResolvedItem =
        UrlSnapshotResolvedItem(
            index =
                index,
            playlistItemId =
                "item-$index",
            sourcePosition =
                index,
            videoId =
                videoId,
            title =
                title,
            channelTitle =
                null,
            availability =
                UrlSnapshotAvailability
                    .UNAVAILABLE,
            unavailableReason =
                reason
        )
}
