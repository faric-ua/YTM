package com.saney.ytmimporter.urlsnapshot

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus

enum class UrlSnapshotDuplicateMode {
    KEEP_ALL,
    DROP_REPEATED_EXACT_VIDEO_IDS
}

data class UrlSnapshotCommitPlan(
    val playlist: ImportedPlaylist,
    val sourceLabel: String,
    val sourceCount: Int,
    val availableCount: Int,
    val unavailableCount: Int,
    val duplicateOccurrences: Int,
    val duplicateSkippedCount: Int
)

object UrlSnapshotCommitPolicy {
    fun buildPlan(
        resolved:
            UrlSnapshotResolutionResult.Resolved,
        duplicateMode:
            UrlSnapshotDuplicateMode =
            UrlSnapshotDuplicateMode.KEEP_ALL
    ): UrlSnapshotCommitPlan {
        val duplicateAnalysis =
            UrlSnapshotDuplicatePolicy
                .analyze(
                    resolved.items
                )

        val selectedItems =
            when (duplicateMode) {
                UrlSnapshotDuplicateMode
                    .KEEP_ALL ->
                    resolved.items

                UrlSnapshotDuplicateMode
                    .DROP_REPEATED_EXACT_VIDEO_IDS ->
                    UrlSnapshotDuplicatePolicy
                        .withoutRepeatedExactVideoIds(
                            resolved.items
                        )
            }

        val tracks =
            selectedItems.mapIndexed {
                    fallbackIndex,
                    item ->

                val index =
                    item.index
                        .takeIf {
                            it >= 0
                        }
                        ?: fallbackIndex

                toTrack(
                    item = item,
                    historyIndex = index
                )
            }
                .toMutableList()

        val availableCount =
            tracks.count {
                it.status ==
                    TrackStatus.MATCHED
            }

        val unavailableCount =
            tracks.count {
                it.status ==
                    TrackStatus.MISSING
            }

        val duplicateSkippedCount =
            resolved.items.size -
                selectedItems.size

        return UrlSnapshotCommitPlan(
            playlist =
                ImportedPlaylist(
                    name =
                        resolved.playlistTitle
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                            ?: snapshotName(
                                resolved.source.playlistId
                            ),
                    tracks =
                        tracks
                ),
            sourceLabel =
                buildString {
                    append(
                        "URL snapshot ("
                    )
                    append(
                        resolved.source
                            .playlistId
                    )
                    append(")")

                    if (
                        duplicateMode ==
                        UrlSnapshotDuplicateMode
                            .DROP_REPEATED_EXACT_VIDEO_IDS
                    ) {
                        append(
                            " • без повторів"
                        )
                    }
                },
            sourceCount =
                resolved.items.size,
            availableCount =
                availableCount,
            unavailableCount =
                unavailableCount,
            duplicateOccurrences =
                duplicateAnalysis
                    .duplicateOccurrences,
            duplicateSkippedCount =
                duplicateSkippedCount
        )
    }

    private fun toTrack(
        item: UrlSnapshotResolvedItem,
        historyIndex: Int
    ): Track {
        val videoId =
            item.videoId
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val title =
            item.title
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: videoId
                    ?.let {
                        "YouTube video $it"
                    }
                ?: "Недоступний елемент #${historyIndex + 1}"

        val channel =
            item.channelTitle
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "YouTube"

        val available =
            item.availability ==
                UrlSnapshotAvailability
                    .AVAILABLE &&
                videoId != null

        return Track(
            originalTitle =
                title,
            originalArtist =
                channel,
            selectedVideoId =
                videoId,
            selectedTitle =
                if (videoId != null) {
                    title
                } else {
                    null
                },
            selectedChannel =
                if (videoId != null) {
                    channel
                } else {
                    null
                },
            status =
                if (available) {
                    TrackStatus.MATCHED
                } else {
                    TrackStatus.MISSING
                },
            candidates =
                emptyList(),
            manuallySelected =
                false,
            error =
                if (available) {
                    null
                } else {
                    unavailableMessage(
                        item.unavailableReason
                    )
                },
            historyIndex =
                historyIndex
        )
    }

    private fun unavailableMessage(
        reason:
            UrlSnapshotUnavailableReason?
    ): String =
        when (reason) {
            UrlSnapshotUnavailableReason
                .PRIVATE_VIDEO ->
                "Source snapshot: приватне відео"

            UrlSnapshotUnavailableReason
                .DELETED_VIDEO ->
                "Source snapshot: видалене відео"

            UrlSnapshotUnavailableReason
                .MISSING_VIDEO_ID ->
                "Source snapshot: YouTube не повернув точний videoId"

            null ->
                "Source snapshot: елемент недоступний"
        }

    private fun snapshotName(
        playlistId: String
    ): String {
        val shortId =
            playlistId
                .take(18)

        return if (
            shortId.isBlank()
        ) {
            "URL snapshot"
        } else {
            "URL snapshot • $shortId"
        }
    }
}
