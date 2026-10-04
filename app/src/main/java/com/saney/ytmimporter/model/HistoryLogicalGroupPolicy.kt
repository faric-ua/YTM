package com.saney.ytmimporter.model

import java.util.Locale

data class HistoryLogicalGroup(
    val key: String,
    val entries: List<HistoryEntry>
) {
    init {
        require(entries.isNotEmpty())
    }

    val latest: HistoryEntry
        get() = entries.first()

    val operationCount: Int
        get() = entries.size

    val latestYtmEntry: HistoryEntry?
        get() =
            entries.firstOrNull {
                !it.playlistId
                    .isNullOrBlank()
            }

    val hasYtm: Boolean
        get() =
            latestYtmEntry != null

    val linkageState: PlaylistLinkageState
        get() {
            val latestState =
                PlaylistLinkagePolicy
                    .history(latest)

            return when (latestState) {
                PlaylistLinkageState
                    .PENDING_SEARCH,
                PlaylistLinkageState
                    .PENDING_WRITE ->
                    latestState

                PlaylistLinkageState
                    .LOCAL_ONLY,
                PlaylistLinkageState
                    .LINKED_YTM ->
                    if (hasYtm) {
                        PlaylistLinkageState
                            .LINKED_YTM
                    } else {
                        PlaylistLinkageState
                            .LOCAL_ONLY
                    }
            }
        }

    val providerBadge: String?
        get() =
            latestYtmEntry
                ?.let { entry ->
                    val target =
                        entry.writeTargetCount

                    if (target > 0) {
                        "YTM " +
                            entry.addedCount +
                            "/" +
                            target
                    } else {
                        "YTM"
                    }
                }
}

object HistoryLogicalGroupPolicy {
    fun build(
        entries: List<HistoryEntry>
    ): List<HistoryLogicalGroup> =
        entries
            .sortedByDescending {
                it.updatedAt
            }
            .groupBy { entry ->
                entry.localPlaylistId
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        "local:$it"
                    }
                    ?: "operation:${entry.id}"
            }
            .map { (key, grouped) ->
                HistoryLogicalGroup(
                    key = key,
                    entries =
                        grouped.sortedByDescending {
                            it.updatedAt
                        }
                )
            }
            .sortedByDescending {
                it.latest.updatedAt
            }

    fun apply(
        groups: List<HistoryLogicalGroup>,
        query: String,
        semanticFilter:
            HistorySemanticFilter
    ): List<HistoryLogicalGroup> {
        val normalizedQuery =
            query
                .trim()
                .lowercase(
                    Locale.ROOT
                )

        return groups.filter { group ->
            matchesSearch(
                group = group,
                normalizedQuery =
                    normalizedQuery
            ) &&
                matchesSemantic(
                    group = group,
                    filter =
                        semanticFilter
                )
        }
    }

    fun matchesSemantic(
        group: HistoryLogicalGroup,
        filter: HistorySemanticFilter
    ): Boolean =
        when (filter) {
            HistorySemanticFilter.ALL ->
                true

            HistorySemanticFilter.LOCAL_ONLY ->
                group.linkageState ==
                    PlaylistLinkageState
                        .LOCAL_ONLY

            HistorySemanticFilter.LINKED_YTM ->
                group.linkageState ==
                    PlaylistLinkageState
                        .LINKED_YTM

            HistorySemanticFilter.PENDING_SEARCH ->
                group.linkageState ==
                    PlaylistLinkageState
                        .PENDING_SEARCH

            HistorySemanticFilter.PENDING_WRITE ->
                group.linkageState ==
                    PlaylistLinkageState
                        .PENDING_WRITE

            HistorySemanticFilter
                .PAUSED_OR_ERROR ->
                group.latest.status in
                    setOf(
                        HistoryStatus
                            .PENDING_QUOTA,
                        HistoryStatus
                            .PENDING_LIMIT,
                        HistoryStatus
                            .FAILED
                    ) ||
                    group.latest.failedCount > 0 ||
                    !group.latest.lastError
                        .isNullOrBlank()
        }

    private fun matchesSearch(
        group: HistoryLogicalGroup,
        normalizedQuery: String
    ): Boolean {
        if (normalizedQuery.isBlank()) {
            return true
        }

        return group.entries.any { entry ->
            sequenceOf(
                entry.playlistName,
                entry.sourceLabel,
                entry.youtubeChannelTitle
                    .orEmpty()
            ).any { value ->
                value
                    .lowercase(
                        Locale.ROOT
                    )
                    .contains(
                        normalizedQuery
                    )
            }
        }
    }
}
