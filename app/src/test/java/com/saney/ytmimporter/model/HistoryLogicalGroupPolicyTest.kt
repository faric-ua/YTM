package com.saney.ytmimporter.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryLogicalGroupPolicyTest {
    @Test
    fun sameLocalIdentityBecomesOneLogicalPlaylist() {
        val local =
            entry(
                id = "local",
                updatedAt = 10L,
                localPlaylistId = "local-1",
                playlistId = null,
                addedCount = 0,
                writeTargetCount = 0
            )
        val ytm =
            entry(
                id = "ytm",
                updatedAt = 20L,
                localPlaylistId = "local-1",
                playlistId = "PL123",
                addedCount = 4,
                writeTargetCount = 4
            )

        val groups =
            HistoryLogicalGroupPolicy
                .build(
                    listOf(
                        local,
                        ytm
                    )
                )

        assertEquals(1, groups.size)
        assertEquals(2, groups.single().operationCount)
        assertEquals("ytm", groups.single().latest.id)
        assertTrue(groups.single().hasYtm)
        assertEquals(
            "YTM 4/4",
            groups.single().providerBadge
        )
    }

    @Test
    fun sameTitleDifferentLocalIdentityNeverMerges() {
        val groups =
            HistoryLogicalGroupPolicy
                .build(
                    listOf(
                        entry(
                            id = "a",
                            playlistName = "Same",
                            localPlaylistId = "local-a"
                        ),
                        entry(
                            id = "b",
                            playlistName = "Same",
                            localPlaylistId = "local-b"
                        )
                    )
                )

        assertEquals(2, groups.size)
    }

    @Test
    fun missingLocalIdentityStaysOperationScoped() {
        val groups =
            HistoryLogicalGroupPolicy
                .build(
                    listOf(
                        entry(
                            id = "a",
                            playlistName = "Same",
                            localPlaylistId = null,
                            playlistId = "PL123"
                        ),
                        entry(
                            id = "b",
                            playlistName = "Same",
                            localPlaylistId = null,
                            playlistId = "PL123"
                        )
                    )
                )

        assertEquals(2, groups.size)
    }

    @Test
    fun linkedProviderPresenceSurvivesLaterLocalOperation() {
        val groups =
            HistoryLogicalGroupPolicy
                .build(
                    listOf(
                        entry(
                            id = "ytm",
                            updatedAt = 10L,
                            localPlaylistId = "local-1",
                            playlistId = "PL123",
                            addedCount = 4,
                            writeTargetCount = 4
                        ),
                        entry(
                            id = "local-newer",
                            updatedAt = 20L,
                            localPlaylistId = "local-1",
                            playlistId = null
                        )
                    )
                )

        val group = groups.single()

        assertEquals(
            PlaylistLinkageState.LINKED_YTM,
            group.linkageState
        )
        assertEquals(
            "YTM 4/4",
            group.providerBadge
        )
    }

    @Test
    fun pendingCurrentOperationOverridesProviderPresence() {
        val groups =
            HistoryLogicalGroupPolicy
                .build(
                    listOf(
                        entry(
                            id = "linked",
                            updatedAt = 10L,
                            localPlaylistId = "local-1",
                            playlistId = "PL123"
                        ),
                        entry(
                            id = "pending",
                            updatedAt = 20L,
                            localPlaylistId = "local-1",
                            playlistId = "PL123",
                            status =
                                HistoryStatus
                                    .PENDING_LIMIT,
                            pendingCount = 2
                        )
                    )
                )

        assertEquals(
            PlaylistLinkageState.PENDING_WRITE,
            groups.single().linkageState
        )
    }

    @Test
    fun groupedSearchCanMatchOlderOperationButFilterUsesCurrentState() {
        val group =
            HistoryLogicalGroupPolicy
                .build(
                    listOf(
                        entry(
                            id = "old",
                            updatedAt = 10L,
                            playlistName = "Old Prodigy",
                            localPlaylistId = "local-1",
                            playlistId = null
                        ),
                        entry(
                            id = "new",
                            updatedAt = 20L,
                            playlistName = "Renamed",
                            localPlaylistId = "local-1",
                            playlistId = "PL123"
                        )
                    )
                )
                .single()

        val result =
            HistoryLogicalGroupPolicy
                .apply(
                    groups = listOf(group),
                    query = "prodigy",
                    semanticFilter =
                        HistorySemanticFilter
                            .LINKED_YTM
                )

        assertEquals(1, result.size)

        assertFalse(
            HistoryLogicalGroupPolicy
                .matchesSemantic(
                    group = group,
                    filter =
                        HistorySemanticFilter
                            .LOCAL_ONLY
                )
        )
    }

    private fun entry(
        id: String,
        updatedAt: Long = 1L,
        playlistName: String = "Playlist",
        localPlaylistId: String? = null,
        playlistId: String? = null,
        status: HistoryStatus =
            HistoryStatus.COMPLETED,
        pendingCount: Int = 0,
        addedCount: Int = 0,
        writeTargetCount: Int = 0
    ): HistoryEntry =
        HistoryEntry(
            id = id,
            createdAt = updatedAt,
            updatedAt = updatedAt,
            status = status,
            sourceLabel = "fixture",
            playlistName = playlistName,
            playlistId = playlistId,
            privacyStatus = "private",
            destination =
                PendingDestination
                    .NEW_PLAYLIST,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            totalImportedCount = 0,
            writeTargetCount =
                writeTargetCount,
            addedCount =
                addedCount,
            failedCount = 0,
            pendingCount =
                pendingCount,
            skippedCount = 0,
            duplicateCount = 0,
            missingCount = 0,
            lastError = null,
            tracks = emptyList(),
            localPlaylistId =
                localPlaylistId
        )
}
