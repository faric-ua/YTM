package com.saney.ytmimporter.bulk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkSyncSelectionPolicyTest {
    @Test
    fun defaultSelection_includesAllExecutableRowsOnly() {
        val summary =
            summary(
                row(
                    id = "new",
                    state = BulkSyncPlanState.NEW,
                    writeUnits = 150
                ),
                row(
                    id = "linked",
                    state = BulkSyncPlanState.LINKED,
                    writeUnits = 100
                ),
                row(
                    id = "noop",
                    state = BulkSyncPlanState.ALREADY_SYNCED
                )
            )

        assertEquals(
            setOf(
                "new",
                "linked"
            ),
            BulkSyncSelectionPolicy
                .defaultIncludedIds(summary)
        )
    }

    @Test
    fun selectedSummary_keepsOnlyIncludedExecutableRows() {
        val summary =
            summary(
                row(
                    id = "new",
                    state = BulkSyncPlanState.NEW,
                    writeUnits = 100
                ),
                row(
                    id = "linked-keep",
                    state = BulkSyncPlanState.LINKED,
                    writeUnits = 100
                ),
                row(
                    id = "linked-drop",
                    state = BulkSyncPlanState.LINKED,
                    writeUnits = 50
                ),
                row(
                    id = "pending",
                    state = BulkSyncPlanState.PENDING
                )
            )

        val selected =
            BulkSyncSelectionPolicy.selectedSummary(
                summary = summary,
                includedIds =
                    setOf(
                        "linked-keep"
                    )
            )

        assertEquals(
            listOf(
                "linked-keep"
            ),
            selected.rows.map {
                it.localPlaylistId
            }
        )
        assertEquals(
            0,
            selected.estimatedSearchCalls
        )
        assertEquals(
            100,
            selected.estimatedWriteUnits
        )
        assertEquals(
            1,
            BulkSyncSelectionPolicy
                .selectedExecutableCount(
                    summary = summary,
                    includedIds =
                        setOf(
                            "linked-keep"
                        )
                )
        )
    }

    @Test
    fun linkedOnlySelection_excludesNewPlaylistCreations() {
        val summary = summary(
            row(id = "create", state = BulkSyncPlanState.NEW, writeUnits = 150),
            row(id = "append", state = BulkSyncPlanState.LINKED, writeUnits = 100),
            row(id = "already", state = BulkSyncPlanState.ALREADY_SYNCED),
            row(id = "blocked", state = BulkSyncPlanState.BLOCKED)
        )

        val selected = BulkSyncSelectionPolicy.linkedOnlyIncludedIds(summary)
        assertEquals(setOf("append"), selected)
        assertEquals(0, BulkSyncSelectionPolicy.selectedSummary(summary, selected).count(BulkSyncPlanState.NEW))
        assertEquals(100, BulkSyncSelectionPolicy.selectedSummary(summary, selected).estimatedWriteUnits)
    }

    @Test
    fun linkedOnlySelection_withoutLinkedRows_isEmpty() {
        val summary = summary(
            row(id = "create", state = BulkSyncPlanState.NEW, writeUnits = 150),
            row(id = "already", state = BulkSyncPlanState.ALREADY_SYNCED)
        )
        assertTrue(BulkSyncSelectionPolicy.linkedOnlyIncludedIds(summary).isEmpty())
    }

    @Test
    fun staleIncludedIds_areIgnored() {
        val summary =
            summary(
                row(
                    id = "linked",
                    state = BulkSyncPlanState.LINKED,
                    writeUnits = 50
                )
            )

        val sanitized =
            BulkSyncSelectionPolicy
                .sanitizeIncludedIds(
                    summary = summary,
                    includedIds =
                        setOf(
                            "missing"
                        )
                )

        assertTrue(
            sanitized.isEmpty()
        )
        assertFalse(
            "missing" in sanitized
        )
    }

    private fun summary(
        vararg rows: BulkSyncPlanRow
    ): BulkSyncPlanSummary =
        BulkSyncPlanSummary(
            rows = rows.toList(),
            estimatedSearchCalls =
                rows.sumOf {
                    it.estimatedSearchCalls
                },
            estimatedWriteUnits =
                rows.sumOf {
                    it.estimatedWriteUnits
                }
        )

    private fun row(
        id: String,
        state: BulkSyncPlanState,
        writeUnits: Int = 0
    ): BulkSyncPlanRow =
        BulkSyncPlanRow(
            localPlaylistId = id,
            playlistName = id,
            state = state,
            trackCount = 1,
            selectedCount = 1,
            unresolvedCount = 0,
            estimatedSearchCalls = 0,
            cacheHits = 0,
            destinationPlaylistId =
                if (
                    state ==
                    BulkSyncPlanState.LINKED
                ) {
                    "remote-$id"
                } else {
                    null
                },
            plannedCreate =
                state ==
                    BulkSyncPlanState.NEW,
            plannedInsertCount =
                if (
                    state in
                    setOf(
                        BulkSyncPlanState.NEW,
                        BulkSyncPlanState.LINKED
                    )
                ) {
                    1
                } else {
                    0
                },
            estimatedWriteUnits =
                writeUnits,
            reason = "test"
        )
}
