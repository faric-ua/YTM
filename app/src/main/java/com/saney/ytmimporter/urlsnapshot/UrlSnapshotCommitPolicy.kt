package com.saney.ytmimporter.urlsnapshot

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus

data class UrlSnapshotCommitPlan(
    val playlist: ImportedPlaylist,
    val sourceLabel: String,
    val availableCount: Int,
    val unavailableCount: Int
)

object UrlSnapshotCommitPolicy {
    fun buildPlan(
        resolved:
            UrlSnapshotResolutionResult.Resolved
    ): UrlSnapshotCommitPlan {
        val tracks =
            resolved.items.mapIndexed {
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

        return UrlSnapshotCommitPlan(
            playlist =
                ImportedPlaylist(
                    name =
                        snapshotName(
                            resolved.source
                                .playlistId
                        ),
                    tracks =
                        tracks
                ),
            sourceLabel =
                "URL snapshot (" +
                    resolved.source
                        .playlistId +
                    ")",
            availableCount =
                availableCount,
            unavailableCount =
                unavailableCount
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
