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
    val sourceCount: Int,
    val savedCount: Int,
    val exactCount: Int,
    val unavailableCount: Int,
    val duplicateOccurrences: Int,
    val duplicateSkippedCount: Int,
    val historyEntryId: String
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
            UrlSnapshotResolutionResult.Resolved,
        duplicateMode:
            UrlSnapshotDuplicateMode =
            UrlSnapshotDuplicateMode.KEEP_ALL
    ): UrlSnapshotCommitReceipt {
        val plan =
            UrlSnapshotCommitPolicy
                .buildPlan(
                    resolved = resolved,
                    duplicateMode =
                        duplicateMode
                )

        currentPlaylistStore.save(
            playlist =
                plan.playlist,
            sourceLabel =
                plan.sourceLabel
        )

        val historyEntry =
            historyEntry(
                plan
            )

        historyStore.upsert(
            historyEntry
        )

        val savedCount =
            plan.playlist
                .tracks
                .size

        return UrlSnapshotCommitReceipt(
            message =
                buildString {
                    append(
                        "URL snapshot збережено: "
                    )
                    append(
                        "джерело ${plan.sourceCount} • "
                    )
                    append(
                        "збережено $savedCount"
                    )

                    if (
                        plan.duplicateOccurrences > 0
                    ) {
                        if (
                            plan.duplicateSkippedCount > 0
                        ) {
                            append(
                                " • повторів пропущено: ${plan.duplicateSkippedCount}"
                            )
                        } else {
                            append(
                                " • повторів збережено: ${plan.duplicateOccurrences}"
                            )
                        }
                    }

                    append(
                        " • точних videoId: ${plan.availableCount}"
                    )

                    if (
                        plan.unavailableCount > 0
                    ) {
                        append(
                            " • недоступних: ${plan.unavailableCount}"
                        )
                    }

                    append(".")
                },
            sourceCount =
                plan.sourceCount,
            savedCount =
                savedCount,
            exactCount =
                plan.availableCount,
            unavailableCount =
                plan.unavailableCount,
            duplicateOccurrences =
                plan.duplicateOccurrences,
            duplicateSkippedCount =
                plan.duplicateSkippedCount,
            historyEntryId =
                historyEntry.id
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
                plan.duplicateSkippedCount,
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
