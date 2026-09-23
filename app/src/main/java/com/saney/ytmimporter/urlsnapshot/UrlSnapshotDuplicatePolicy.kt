package com.saney.ytmimporter.urlsnapshot

import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus

data class UrlSnapshotDuplicateAnalysis(
    val totalCount: Int,
    val exactIdCount: Int,
    val uniqueExactIdCount: Int,
    val duplicateOccurrences: Int,
    val firstOccurrenceByDuplicateIndex:
        Map<Int, Int>
) {
    val duplicateIndexes:
        Set<Int>
        get() =
            firstOccurrenceByDuplicateIndex
                .keys
}

object UrlSnapshotDuplicatePolicy {
    fun analyze(
        items:
            List<UrlSnapshotResolvedItem>
    ): UrlSnapshotDuplicateAnalysis =
        analyzeVideoIds(
            items.map {
                it.videoId
            }
        )

    fun analyzeVideoIds(
        videoIds: List<String?>
    ): UrlSnapshotDuplicateAnalysis {
        val firstIndexById =
            linkedMapOf<String, Int>()

        val duplicateFirstIndex =
            linkedMapOf<Int, Int>()

        var exactCount =
            0

        videoIds.forEachIndexed {
                index,
                rawId ->

            val videoId =
                rawId
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: return@forEachIndexed

            exactCount += 1

            val first =
                firstIndexById[videoId]

            if (first == null) {
                firstIndexById[videoId] =
                    index
            } else {
                duplicateFirstIndex[index] =
                    first
            }
        }

        return UrlSnapshotDuplicateAnalysis(
            totalCount =
                videoIds.size,
            exactIdCount =
                exactCount,
            uniqueExactIdCount =
                firstIndexById.size,
            duplicateOccurrences =
                duplicateFirstIndex.size,
            firstOccurrenceByDuplicateIndex =
                duplicateFirstIndex
        )
    }

    fun countWorkspaceDuplicates(
        tracks: List<Track>
    ): Int {
        val exactDuplicateIndexes =
            analyzeVideoIds(
                tracks.map {
                    it.selectedVideoId
                }
            )
                .duplicateIndexes

        val duplicateStatusIndexes =
            tracks
                .mapIndexedNotNull {
                        index,
                        track ->
                    index.takeIf {
                        track.status ==
                            TrackStatus.DUPLICATE
                    }
                }
                .toSet()

        return (
            exactDuplicateIndexes +
                duplicateStatusIndexes
            )
            .size
    }

    fun withoutRepeatedExactVideoIds(
        items:
            List<UrlSnapshotResolvedItem>
    ): List<UrlSnapshotResolvedItem> {
        val duplicateIndexes =
            analyze(items)
                .duplicateIndexes

        return items.filterIndexed {
                index,
                _ ->
            index !in duplicateIndexes
        }
    }
}
