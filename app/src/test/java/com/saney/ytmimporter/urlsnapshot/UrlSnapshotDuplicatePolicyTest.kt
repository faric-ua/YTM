package com.saney.ytmimporter.urlsnapshot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSnapshotDuplicatePolicyTest {
    @Test
    fun exactVideoIdDuplicates_areDetectedByOccurrenceAndFirstIndex() {
        val analysis =
            UrlSnapshotDuplicatePolicy
                .analyzeVideoIds(
                    listOf(
                        "A",
                        "B",
                        "A",
                        null,
                        "B",
                        "A"
                    )
                )

        assertEquals(6, analysis.totalCount)
        assertEquals(5, analysis.exactIdCount)
        assertEquals(2, analysis.uniqueExactIdCount)
        assertEquals(3, analysis.duplicateOccurrences)
        assertEquals(0, analysis.firstOccurrenceByDuplicateIndex[2])
        assertEquals(1, analysis.firstOccurrenceByDuplicateIndex[4])
        assertEquals(0, analysis.firstOccurrenceByDuplicateIndex[5])
    }

    @Test
    fun missingIds_areNeverCollapsedAsDuplicates() {
        val analysis =
            UrlSnapshotDuplicatePolicy
                .analyzeVideoIds(
                    listOf(
                        null,
                        "",
                        "   ",
                        null
                    )
                )

        assertEquals(0, analysis.exactIdCount)
        assertEquals(0, analysis.duplicateOccurrences)
        assertTrue(analysis.duplicateIndexes.isEmpty())
    }

    @Test
    fun dedupe_keepsFirstExactOccurrenceAndAllMissingIdRowsInOrder() {
        val items =
            listOf(
                item(0, "A"),
                item(1, null),
                item(2, "A"),
                item(3, "B"),
                item(4, null),
                item(5, "B")
            )

        val result =
            UrlSnapshotDuplicatePolicy
                .withoutRepeatedExactVideoIds(
                    items
                )

        assertEquals(
            listOf(0, 1, 3, 4),
            result.map { it.index }
        )
        assertEquals(
            listOf("A", null, "B", null),
            result.map { it.videoId }
        )
    }

    @Test
    fun videoIdsRemainCaseSensitive() {
        val analysis =
            UrlSnapshotDuplicatePolicy
                .analyzeVideoIds(
                    listOf(
                        "AbCd",
                        "abcd"
                    )
                )

        assertEquals(0, analysis.duplicateOccurrences)
        assertFalse(1 in analysis.duplicateIndexes)
    }

    private fun item(
        index: Int,
        videoId: String?
    ): UrlSnapshotResolvedItem =
        UrlSnapshotResolvedItem(
            index = index,
            playlistItemId = "item-$index",
            sourcePosition = index,
            videoId = videoId,
            title = "Track $index",
            channelTitle = "Channel",
            availability =
                if (videoId == null) {
                    UrlSnapshotAvailability.UNAVAILABLE
                } else {
                    UrlSnapshotAvailability.AVAILABLE
                },
            unavailableReason =
                if (videoId == null) {
                    UrlSnapshotUnavailableReason.MISSING_VIDEO_ID
                } else {
                    null
                }
        )
}
