package com.saney.ytmimporter.search

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingOperation
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.PendingJobStore
import java.util.UUID

class SearchRecoveryCoordinator(
    private val pendingJobStore:
        PendingJobStore
) {
    data class AccountContext(
        val googleEmail: String?,
        val youtubeChannelId: String?,
        val youtubeChannelTitle: String?
    )

    fun pause(
        sourceLabel: String,
        playlist: ImportedPlaylist,
        destinationPlaylistId: String?,
        preserveExistingExact: Boolean,
        requestedJobId: String?,
        account: AccountContext,
        lastError: String?
    ): PendingJob {
        val now =
            System.currentTimeMillis()

        val recoveryKey =
            SearchRecoveryPolicy
                .workspaceKey(
                    sourceLabel =
                        sourceLabel,
                    playlist =
                        playlist
                )

        val existing =
            requestedJobId
                ?.let(
                    pendingJobStore::get
                )
                ?: pendingJobStore
                    .findSearchByRecoveryKey(
                        recoveryKey
                    )

        val job =
            PendingJob(
                id =
                    existing?.id
                        ?: UUID.randomUUID()
                            .toString(),
                createdAt =
                    existing?.createdAt
                        ?: now,
                updatedAt =
                    now,
                sourceLabel =
                    sourceLabel,
                playlistName =
                    playlist.name,
                playlistId =
                    destinationPlaylistId,
                privacyStatus =
                    "search",
                destination =
                    PendingDestination
                        .EXISTING_PLAYLIST,
                googleEmail =
                    account.googleEmail,
                youtubeChannelId =
                    account.youtubeChannelId,
                youtubeChannelTitle =
                    account.youtubeChannelTitle,
                totalCount =
                    playlist.tracks.size,
                addedCount =
                    0,
                failedCount =
                    playlist.tracks
                        .count {
                            it.status ==
                                TrackStatus.FAILED
                        },
                remainingTracks =
                    emptyList(),
                lastError =
                    lastError,
                operation =
                    PendingOperation.SEARCH,
                recoveryKey =
                    recoveryKey,
                preserveExistingExact =
                    preserveExistingExact,
                searchSnapshot =
                    SearchRecoveryPolicy
                        .snapshot(
                            playlist
                        )
            )

        pendingJobStore.upsert(
            job
        )

        return job
    }

    fun restore(
        job: PendingJob
    ): ImportedPlaylist? =
        if (
            job.operation ==
                PendingOperation.SEARCH
        ) {
            job.searchSnapshot
                ?.let(
                    SearchRecoveryPolicy::restore
                )
        } else {
            null
        }

    fun waitingCount(
        job: PendingJob
    ): Int =
        job.searchSnapshot
            ?.let(
                SearchRecoveryPolicy::waitingCount
            )
            ?: 0

    fun completeIfResolved(
        sourceLabel: String,
        playlist: ImportedPlaylist,
        requestedJobId: String?
    ) {
        if (
            playlist.tracks.any {
                it.status ==
                    TrackStatus.WAITING_QUOTA
            }
        ) {
            return
        }

        val byId =
            requestedJobId
                ?.let(
                    pendingJobStore::get
                )

        val job =
            if (
                byId?.operation ==
                    PendingOperation.SEARCH
            ) {
                byId
            } else {
                pendingJobStore
                    .findSearchByRecoveryKey(
                        SearchRecoveryPolicy
                            .workspaceKey(
                                sourceLabel =
                                    sourceLabel,
                                playlist =
                                    playlist
                            )
                    )
            }

        job?.let {
            pendingJobStore.remove(
                it.id
            )
        }
    }
}
