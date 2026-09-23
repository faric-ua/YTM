package com.saney.ytmimporter.urlsnapshot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSnapshotResolverTest {
    @Test
    fun dynamicMix_returnsUnsupportedWithoutReadOrQuota() {
        var reads = 0
        var quota = 0

        val resolver =
            UrlSnapshotResolver(
                playlistReader =
                    UrlSnapshotConcretePlaylistReader {
                            _,
                            _,
                            _ ->
                        reads += 1
                        UrlSnapshotPlaylistRead(
                            items = emptyList(),
                            requestCount = 0
                        )
                    },
                quotaRecorder =
                    UrlSnapshotQuotaRecorder {
                        quota += 1
                    }
            )

        val result =
            resolver.resolve(
                accessToken = "token",
                source =
                    source(
                        playlistId =
                            "RDREDRRxBLCgTn4p2e5sfWmEpQ",
                        kind =
                            UrlSnapshotSourceKind
                                .DYNAMIC_MIX
                    )
            )

        assertTrue(
            result is
                UrlSnapshotResolutionResult.Unsupported
        )
        assertEquals(
            0,
            reads
        )
        assertEquals(
            0,
            quota
        )
    }

    @Test
    fun concretePlaylist_preservesOrderAndDuplicateOccurrences() {
        val raw =
            listOf(
                raw(0, "AAAAAAAAAAA", "First"),
                raw(1, "BBBBBBBBBBB", "Second"),
                raw(2, "AAAAAAAAAAA", "First again")
            )

        val resolver =
            resolverReturning(
                rawItems = raw,
                requestCount = 2
            )

        val result =
            resolver.resolve(
                accessToken = "token",
                source =
                    source(
                        playlistId =
                            "PLconcrete"
                    )
            ) as
                UrlSnapshotResolutionResult.Resolved

        assertEquals(
            listOf(
                "AAAAAAAAAAA",
                "BBBBBBBBBBB",
                "AAAAAAAAAAA"
            ),
            result.items.map {
                it.videoId
            }
        )

        assertEquals(
            listOf(
                0,
                1,
                2
            ),
            result.items.map {
                it.sourcePosition
            }
        )

        assertEquals(
            3,
            result.items.size
        )

        assertEquals(
            2,
            result.requestCount
        )
    }

    @Test
    fun inaccessibleItemsRemainExplicitAndKeepExactVideoIdWhenPresent() {
        val result =
            resolverReturning(
                rawItems =
                    listOf(
                        raw(
                            position = 0,
                            videoId =
                                "PRIVATE0001",
                            title =
                                "[Private video]",
                            privacyStatus =
                                "private"
                        ),
                        raw(
                            position = 1,
                            videoId =
                                "DELETED0001",
                            title =
                                "[Deleted video]"
                        ),
                        raw(
                            position = 2,
                            videoId = null,
                            title = null
                        )
                    )
            )
                .resolve(
                    accessToken = "token",
                    source =
                        source(
                            playlistId =
                                "PLunavailable"
                        )
                ) as
                    UrlSnapshotResolutionResult.Resolved

        assertEquals(
            3,
            result.unavailableCount
        )

        assertEquals(
            "PRIVATE0001",
            result.items[0].videoId
        )

        assertEquals(
            UrlSnapshotUnavailableReason
                .PRIVATE_VIDEO,
            result.items[0]
                .unavailableReason
        )

        assertEquals(
            "DELETED0001",
            result.items[1].videoId
        )

        assertEquals(
            UrlSnapshotUnavailableReason
                .DELETED_VIDEO,
            result.items[1]
                .unavailableReason
        )

        assertNull(
            result.items[2].videoId
        )

        assertEquals(
            UrlSnapshotUnavailableReason
                .MISSING_VIDEO_ID,
            result.items[2]
                .unavailableReason
        )
    }

    @Test
    fun oneQuotaUnitIsRecordedForEveryReaderRequestCallback() {
        var quota = 0

        val resolver =
            UrlSnapshotResolver(
                playlistReader =
                    UrlSnapshotConcretePlaylistReader {
                            _,
                            _,
                            onListRequest ->
                        repeat(3) {
                            onListRequest()
                        }

                        UrlSnapshotPlaylistRead(
                            items = emptyList(),
                            requestCount = 3
                        )
                    },
                quotaRecorder =
                    UrlSnapshotQuotaRecorder {
                        quota += 1
                    }
            )

        val result =
            resolver.resolve(
                accessToken = "token",
                source =
                    source(
                        playlistId =
                            "PLquota"
                    )
            ) as
                UrlSnapshotResolutionResult.Resolved

        assertEquals(
            3,
            quota
        )

        assertEquals(
            3,
            result.requestCount
        )
    }

    @Test
    fun quotaIsAlreadyRecordedWhenRemoteReadFails() {
        var quota = 0

        val resolver =
            UrlSnapshotResolver(
                playlistReader =
                    UrlSnapshotConcretePlaylistReader {
                            _,
                            _,
                            onListRequest ->
                        onListRequest()
                        onListRequest()

                        throw IllegalStateException(
                            "network failed"
                        )
                    },
                quotaRecorder =
                    UrlSnapshotQuotaRecorder {
                        quota += 1
                    }
            )

        var thrown = false

        try {
            resolver.resolve(
                accessToken = "token",
                source =
                    source(
                        playlistId =
                            "PLfailure"
                    )
            )
        } catch (_: IllegalStateException) {
            thrown = true
        }

        assertTrue(
            thrown
        )

        assertEquals(
            2,
            quota
        )
    }

    @Test
    fun availableItemKeepsExactIdentityAndMetadata() {
        val result =
            resolverReturning(
                rawItems =
                    listOf(
                        raw(
                            position = 7,
                            videoId =
                                "dQw4w9WgXcQ",
                            title =
                                "Exact title",
                            channel =
                                "Exact channel",
                            privacyStatus =
                                "public"
                        )
                    )
            )
                .resolve(
                    accessToken = "token",
                    source =
                        source(
                            playlistId =
                                "PLexact"
                        )
                ) as
                    UrlSnapshotResolutionResult.Resolved

        val item =
            result.items.single()

        assertEquals(
            UrlSnapshotAvailability.AVAILABLE,
            item.availability
        )

        assertNull(
            item.unavailableReason
        )

        assertEquals(
            "dQw4w9WgXcQ",
            item.videoId
        )

        assertEquals(
            "Exact title",
            item.title
        )

        assertEquals(
            "Exact channel",
            item.channelTitle
        )

        assertEquals(
            7,
            item.sourcePosition
        )
    }

    @Test
    fun concretePlaylist_carriesRemotePlaylistTitle() {
        val result = resolverReturning(
            rawItems = emptyList(),
            requestCount = 2,
            playlistTitle = "Remote playlist title"
        ).resolve("token", source("PLtitle")) as UrlSnapshotResolutionResult.Resolved
        assertEquals("Remote playlist title", result.playlistTitle)
        assertEquals(2, result.requestCount)
    }

    private fun resolverReturning(
        rawItems: List<UrlSnapshotRawItem>,
        requestCount: Int = 1,
        playlistTitle: String? = null
    ): UrlSnapshotResolver =
        UrlSnapshotResolver(
            playlistReader =
                UrlSnapshotConcretePlaylistReader {
                        _,
                        _,
                        onListRequest ->
                    repeat(
                        requestCount
                    ) {
                        onListRequest()
                    }

                    UrlSnapshotPlaylistRead(
                        items = rawItems,
                        requestCount =
                            requestCount,
                        playlistTitle = playlistTitle
                    )
                },
            quotaRecorder =
                UrlSnapshotQuotaRecorder {}
        )

    private fun source(
        playlistId: String,
        kind:
            UrlSnapshotSourceKind =
            UrlSnapshotSourceKind
                .CONCRETE_PLAYLIST
    ): UrlSnapshotSource =
        UrlSnapshotSource(
            originalUrl =
                "https://www.youtube.com/playlist?list=$playlistId",
            canonicalUrl =
                "https://www.youtube.com/playlist?list=$playlistId",
            playlistId =
                playlistId,
            kind =
                kind,
            surface =
                UrlSnapshotSurface.YOUTUBE,
            contextVideoId =
                null
        )

    private fun raw(
        position: Int,
        videoId: String?,
        title: String?,
        channel: String? = "Channel",
        privacyStatus: String? = "public"
    ): UrlSnapshotRawItem =
        UrlSnapshotRawItem(
            playlistItemId =
                "item-$position",
            sourcePosition =
                position,
            videoId =
                videoId,
            title =
                title,
            channelTitle =
                channel,
            privacyStatus =
                privacyStatus
        )
}
