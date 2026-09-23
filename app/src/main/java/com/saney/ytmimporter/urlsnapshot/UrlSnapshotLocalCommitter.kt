package com.saney.ytmimporter.urlsnapshot

import android.content.Context
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.HistoryStore
import java.util.UUID

data class UrlSnapshotCommitReceipt(
    val message: String,
    val totalCount: Int,
    val exactCount: Int,
    val unavailableCount: Int
)

class UrlSnapshotLocalCommitter(
    context: Context
) {
    private val appContext =
        context.applicationContext

    private val currentPlaylistStore =
        CurrentPlaylistStore(
            appContext
        )

    private val historyStore =
        HistoryStore(
            appContext
        )

    fun commit(
        resolved:
            UrlSnapshotResolutionResult.Resolved
    ): UrlSnapshotCommitReceipt {
        val plan =
            UrlSnapshotCommitPolicy
                .buildPlan(
                    resolved
                )

        currentPlaylistStore.save(
            playlist =
                plan.playlist,
            sourceLabel =
                plan.sourceLabel
        )

        historyStore.upsert(
            historyEntry(
                plan
            )
        )

        val total =
            plan.playlist
                .tracks
                .size

        return UrlSnapshotCommitReceipt(
            message =
                "URL snapshot імпортовано: " +
                    "$total елементів • " +
                    "точних videoId: ${plan.availableCount} • " +
                    "недоступних: ${plan.unavailableCount}.",
            totalCount =
                total,
            exactCount =
                plan.availableCount,
            unavailableCount =
                plan.unavailableCount
        )
    }

    private fun historyEntry(
        plan:
            UrlSnapshotCommitPlan
    ): HistoryEntry {
        val now =
            System.currentTimeMillis()

        return HistoryEntry(
            id =
                "local-import-" +
                    UUID.randomUUID()
                        .toString(),
            createdAt =
                now,
            updatedAt =
                now,
            status =
                HistoryStatus.COMPLETED,
            sourceLabel =
                plan.sourceLabel,
            playlistName =
                plan.playlist.name,
            playlistId =
                null,
            privacyStatus =
                "local",
            destination =
                PendingDestination
                    .NEW_PLAYLIST,
            googleEmail =
                null,
            youtubeChannelId =
                null,
            youtubeChannelTitle =
                null,
            totalImportedCount =
                plan.playlist
                    .tracks
                    .size,
            writeTargetCount =
                0,
            addedCount =
                0,
            failedCount =
                0,
            pendingCount =
                0,
            skippedCount =
                0,
            duplicateCount =
                0,
            missingCount =
                plan.unavailableCount,
            lastError =
                null,
            tracks =
                plan.playlist
                    .tracks
                    .mapIndexed {
                            fallbackIndex,
                            track ->

                        HistoryTrack(
                            index =
                                track.historyIndex
                                    ?: fallbackIndex,
                            originalTitle =
                                track.originalTitle,
                            originalArtist =
                                track.originalArtist,
                            videoId =
                                track.selectedVideoId,
                            selectedTitle =
                                track.selectedTitle,
                            selectedChannel =
                                track.selectedChannel,
                            status =
                                if (
                                    track.status ==
                                    TrackStatus.MISSING
                                ) {
                                    "MISSING"
                                } else {
                                    "IMPORTED"
                                },
                            manuallySelected =
                                track.manuallySelected,
                            error =
                                track.error
                        )
                    }
        )
    }
}
