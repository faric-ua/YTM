package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot
import java.util.UUID

object BulkSyncSessionFactory {
    fun create(
        summary: BulkSyncPlanSummary,
        snapshots: List<RestorablePlaylistSnapshot>,
        checkpointId: String,
        baseline: BulkSyncRemoteBaseline
    ): BulkSyncSession {
        val byId =
            snapshots.associateBy {
                it.localPlaylistId
            }

        val rows =
            summary.rows.map {
                    planRow ->
                val snapshot =
                    byId[
                        planRow.localPlaylistId
                    ]

                val tracks =
                    if (
                        planRow.state ==
                        BulkSyncPlanState.NEW &&
                        snapshot != null
                    ) {
                        snapshot.playlist.tracks
                            .mapIndexedNotNull {
                                    index,
                                    track ->
                                if (
                                    track.status ==
                                    TrackStatus.SKIPPED
                                ) {
                                    null
                                } else {
                                    track.selectedVideoId
                                        ?.takeIf(
                                            String::isNotBlank
                                        )
                                        ?.let {
                                                videoId ->
                                            BulkSyncSessionTrack(
                                                trackIndex =
                                                    index,
                                                historyIndex =
                                                    track.historyIndex,
                                                videoId =
                                                    videoId,
                                                originalTitle =
                                                    track.originalTitle,
                                                originalArtist =
                                                    track.originalArtist
                                            )
                                        }
                                }
                            }
                    } else {
                        emptyList()
                    }

                BulkSyncSessionRow(
                    localPlaylistId =
                        planRow.localPlaylistId,
                    playlistName =
                        planRow.playlistName,
                    sourceLabel =
                        snapshot?.sourceLabel
                            ?: "Bulk Sync",
                    originalPlanState =
                        planRow.state,
                    state =
                        rowState(
                            planRow.state
                        ),
                    remotePlaylistId =
                        planRow.destinationPlaylistId,
                    tracks =
                        tracks
                )
            }

        val now =
            System.currentTimeMillis()

        return BulkSyncSession(
            sessionId =
                UUID.randomUUID()
                    .toString(),
            createdAt = now,
            updatedAt = now,
            state =
                BulkSyncSessionState.READY,
            googleEmail =
                baseline.googleEmail,
            youtubeChannelId =
                baseline.youtubeChannelId,
            youtubeChannelTitle =
                baseline.youtubeChannelTitle,
            checkpointId =
                checkpointId,
            remoteBaseline =
                baseline,
            plan =
                rows,
            currentPlanIndex =
                rows.indexOfFirst {
                    it.state ==
                        BulkSyncSessionRowState.READY
                }.takeIf {
                    it >= 0
                } ?: rows.size,
            mutationLedger =
                emptyList(),
            lastError =
                null
        )
    }

    private fun rowState(
        state: BulkSyncPlanState
    ): BulkSyncSessionRowState =
        when (state) {
            BulkSyncPlanState.NEW ->
                BulkSyncSessionRowState.READY

            BulkSyncPlanState.LINKED ->
                BulkSyncSessionRowState
                    .DEFERRED_LINKED

            BulkSyncPlanState.ALREADY_SYNCED ->
                BulkSyncSessionRowState
                    .COMPLETED_NOOP

            BulkSyncPlanState.NEEDS_SEARCH ->
                BulkSyncSessionRowState
                    .NEEDS_SEARCH

            BulkSyncPlanState.PENDING ->
                BulkSyncSessionRowState
                    .PENDING

            BulkSyncPlanState.BLOCKED ->
                BulkSyncSessionRowState
                    .BLOCKED
        }
}
