package com.saney.ytmimporter.destination

import android.content.Intent
import com.saney.ytmimporter.DestinationActivity
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.YouTubePlaylistInfo

object DestinationForwardedWritePlan {
    data class Plan(
        val target:
            YouTubePlaylistInfo,
        val tracksToWrite:
            List<Track>,
        val tracksToSkip:
            List<Track>
    )

    fun from(
        data: Intent,
        selected: List<Track>
    ): Plan? {
        if (
            !data.hasExtra(
                DestinationActivity
                    .EXTRA_LOCAL_SKIP_POSITIONS
            )
        ) {
            return null
        }

        val id =
            data.getStringExtra(
                DestinationActivity.EXTRA_TARGET_ID
            )
                ?.trim()
                .orEmpty()

        if (id.isBlank()) {
            return null
        }

        val target =
            YouTubePlaylistInfo(
                id = id,
                title =
                    data.getStringExtra(
                        DestinationActivity
                            .EXTRA_TARGET_TITLE
                    ) ?: "Плейлист",
                privacyStatus =
                    data.getStringExtra(
                        DestinationActivity
                            .EXTRA_TARGET_PRIVACY
                    ) ?: "private",
                itemCount =
                    data.getLongExtra(
                        DestinationActivity
                            .EXTRA_TARGET_COUNT,
                        0L
                    )
            )

        val mode =
            data.getStringExtra(
                DestinationActivity
                    .EXTRA_DUPLICATE_MODE
            )
                ?: DestinationActivity
                    .DUPLICATE_MODE_SKIP

        val skippedPositions =
            data.getIntArrayExtra(
                DestinationActivity
                    .EXTRA_LOCAL_SKIP_POSITIONS
            )
                .orEmpty()
                .toSet()

        val tracksToSkip =
            if (
                mode ==
                DestinationActivity
                    .DUPLICATE_MODE_SKIP
            ) {
                selected
                    .filterIndexed {
                        index,
                        _ ->
                        index in
                            skippedPositions
                    }
            } else {
                emptyList()
            }

        val tracksToWrite =
            if (
                mode ==
                DestinationActivity
                    .DUPLICATE_MODE_SKIP
            ) {
                selected
                    .filterIndexed {
                        index,
                        _ ->
                        index !in
                            skippedPositions
                    }
            } else {
                selected
            }

        return Plan(
            target = target,
            tracksToWrite =
                tracksToWrite,
            tracksToSkip =
                tracksToSkip
        )
    }
}
