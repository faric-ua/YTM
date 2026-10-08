package com.saney.ytmimporter.bulk

object BulkSyncSelectionPolicy {
    private val executableStates =
        setOf(
            BulkSyncPlanState.NEW,
            BulkSyncPlanState.LINKED
        )

    fun isExecutable(
        row: BulkSyncPlanRow
    ): Boolean =
        row.state in executableStates

    fun defaultIncludedIds(
        summary: BulkSyncPlanSummary
    ): Set<String> =
        summary.rows
            .filter(::isExecutable)
            .map {
                it.localPlaylistId
            }
            .toSet()

    /** A zero-create session: append only to verified linked YTM playlists. */
    fun linkedOnlyIncludedIds(
        summary: BulkSyncPlanSummary
    ): Set<String> =
        summary.rows
            .filter {
                it.state == BulkSyncPlanState.LINKED
            }
            .mapTo(linkedSetOf()) {
                it.localPlaylistId
            }

    fun sanitizeIncludedIds(
        summary: BulkSyncPlanSummary,
        includedIds: Set<String>
    ): Set<String> {
        val executableIds =
            defaultIncludedIds(summary)

        return includedIds
            .filterTo(
                linkedSetOf()
            ) {
                it in executableIds
            }
    }

    fun selectedSummary(
        summary: BulkSyncPlanSummary,
        includedIds: Set<String>
    ): BulkSyncPlanSummary {
        val sanitized =
            sanitizeIncludedIds(
                summary = summary,
                includedIds = includedIds
            )

        val rows =
            summary.rows.filter {
                row ->
                isExecutable(row) &&
                    row.localPlaylistId in sanitized
            }

        return BulkSyncPlanSummary(
            rows = rows,
            estimatedSearchCalls = 0,
            estimatedWriteUnits =
                rows.sumOf {
                    it.estimatedWriteUnits
                }
        )
    }

    fun selectedExecutableCount(
        summary: BulkSyncPlanSummary,
        includedIds: Set<String>
    ): Int =
        selectedSummary(
            summary = summary,
            includedIds = includedIds
        ).rows.count(::isExecutable)
}
