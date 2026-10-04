package com.saney.ytmimporter.model

import java.util.Locale

enum class HistorySemanticFilter(
    val label: String
) {
    ALL("Усі"),
    LOCAL_ONLY("Лише локально"),
    LINKED_YTM("Пов’язано з YTM"),
    PENDING_SEARCH("Очікує Search"),
    PENDING_WRITE("Очікує запис у YTM"),
    PAUSED_OR_ERROR("Пауза або помилка");

    companion object {
        fun fromStoredName(
            value: String?
        ): HistorySemanticFilter =
            values()
                .firstOrNull {
                    it.name == value
                }
                ?: ALL
    }
}

object HistoryListFilterPolicy {
    fun apply(
        entries: List<HistoryEntry>,
        query: String,
        semanticFilter:
            HistorySemanticFilter
    ): List<HistoryEntry> {
        val normalizedQuery =
            query
                .trim()
                .lowercase(
                    Locale.ROOT
                )

        return entries.filter { entry ->
            matchesSearch(
                entry = entry,
                normalizedQuery =
                    normalizedQuery
            ) &&
                matchesSemantic(
                    entry = entry,
                    filter =
                        semanticFilter
                )
        }
    }

    fun matchesSemantic(
        entry: HistoryEntry,
        filter: HistorySemanticFilter
    ): Boolean =
        when (filter) {
            HistorySemanticFilter.ALL ->
                true

            HistorySemanticFilter.LOCAL_ONLY ->
                PlaylistLinkagePolicy
                    .history(entry) ==
                    PlaylistLinkageState
                        .LOCAL_ONLY

            HistorySemanticFilter.LINKED_YTM ->
                PlaylistLinkagePolicy
                    .history(entry) ==
                    PlaylistLinkageState
                        .LINKED_YTM

            HistorySemanticFilter.PENDING_SEARCH ->
                PlaylistLinkagePolicy
                    .history(entry) ==
                    PlaylistLinkageState
                        .PENDING_SEARCH

            HistorySemanticFilter.PENDING_WRITE ->
                PlaylistLinkagePolicy
                    .history(entry) ==
                    PlaylistLinkageState
                        .PENDING_WRITE

            HistorySemanticFilter
                .PAUSED_OR_ERROR ->
                entry.status in
                    setOf(
                        HistoryStatus
                            .PENDING_QUOTA,
                        HistoryStatus
                            .PENDING_LIMIT,
                        HistoryStatus
                            .FAILED
                    ) ||
                    entry.failedCount > 0 ||
                    !entry.lastError
                        .isNullOrBlank()
        }

    private fun matchesSearch(
        entry: HistoryEntry,
        normalizedQuery: String
    ): Boolean {
        if (normalizedQuery.isBlank()) {
            return true
        }

        return sequenceOf(
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
