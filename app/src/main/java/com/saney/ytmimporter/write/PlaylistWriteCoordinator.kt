package com.saney.ytmimporter.write

import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingPauseReason
import com.saney.ytmimporter.model.PendingTrack
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.util.ErrorMessages
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import com.saney.ytmimporter.youtube.YouTubeLimitKind
import java.util.UUID

class PlaylistWriteCoordinator(
    private val api: YouTubeApi,
    private val pendingJobStore: PendingJobStore,
    private val quotaTracker: QuotaTracker
) {
    data class AccountContext(
        val googleEmail: String?,
        val youtubeChannelId: String?,
        val youtubeChannelTitle: String?
    )

    data class WriteProgress(
        val job: PendingJob,
        val processedTracks: Int,
        val totalTracks: Int,
        val createOffset: Int,
        val playlistCreated: Boolean = false
    ) {
        val progressValue: Int
            get() = processedTracks + createOffset
    }

    sealed class WriteOutcome {
        data class Completed(
            val job: PendingJob,
            val playlistId: String
        ) : WriteOutcome()

        data class PausedForQuota(
            val job: PendingJob,
            val userMessage: String
        ) : WriteOutcome()

        data class PausedForLimit(
            val job: PendingJob,
            val pauseReason: PendingPauseReason,
            val userMessage: String
        ) : WriteOutcome()

        data class Failed(
            val job: PendingJob,
            val userMessage: String
        ) : WriteOutcome()

        data class AuthorizationInvalidated(
            val job: PendingJob,
            val error: Throwable
        ) : WriteOutcome()
    }

    fun buildPendingJob(
        sourceLabel: String,
        playlistName: String,
        playlistId: String?,
        privacyStatus: String,
        destination: PendingDestination,
        tracks: List<Track>,
        account: AccountContext
    ): PendingJob {
        val now = System.currentTimeMillis()

        return PendingJob(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            sourceLabel = sourceLabel,
            playlistName = playlistName,
            playlistId = playlistId,
            privacyStatus = privacyStatus,
            destination = destination,
            googleEmail = account.googleEmail,
            youtubeChannelId = account.youtubeChannelId,
            youtubeChannelTitle = account.youtubeChannelTitle,
            totalCount = tracks.size,
            addedCount = 0,
            failedCount = 0,
            remainingTracks = tracks.mapNotNull(::trackToPendingTrack),
            lastError = null
        )
    }

    fun trackFromPending(item: PendingTrack): Track =
        Track(
            originalTitle = item.originalTitle,
            originalArtist = item.originalArtist,
            selectedVideoId = item.videoId,
            selectedTitle = item.selectedTitle,
            selectedChannel = item.selectedChannel,
            status = TrackStatus.PENDING,
            manuallySelected = false,
            historyIndex = item.historyIndex.takeIf { it >= 0 }
        )

    fun execute(
        token: String,
        initialJob: PendingJob,
        tracks: List<Track>,
        onHistoryState: (PendingJob, HistoryStatus) -> Unit,
        onTrackStart: (Track, Int, Int) -> Unit = { _, _, _ -> },
        onProgress: (WriteProgress) -> Unit = {},
        onPlaylistIdAvailable: (String) -> Unit = {}
    ): WriteOutcome {
        tracks.forEach { track ->
            track.status = TrackStatus.PENDING
            track.error = null
        }

        var job = initialJob
        onHistoryState(job, HistoryStatus.RUNNING)

        var playlistId = job.playlistId

        if (playlistId.isNullOrBlank()) {
            quotaTracker.recordGeneralUnits(QuotaTracker.PLAYLIST_CREATE_COST)

            try {
                playlistId =
                    api.createPlaylist(
                        token,
                        job.playlistName,
                        job.privacyStatus
                    )

                onPlaylistIdAvailable(playlistId)

                job =
                    job.copy(
                        playlistId = playlistId,
                        updatedAt = System.currentTimeMillis(),
                        lastError = null,
                        pauseReason = null
                    )

                pendingJobStore.upsert(job)
                onHistoryState(job, HistoryStatus.RUNNING)

                onProgress(
                    WriteProgress(
                        job = job,
                        processedTracks = 0,
                        totalTracks = tracks.size,
                        createOffset = 1,
                        playlistCreated = true
                    )
                )
            } catch (error: Exception) {
                if (isAuthorizationError(error)) {
                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            remainingTracks =
                                tracks.mapNotNull(::trackToPendingTrack),
                            lastError = error.message
                        )

                    pendingJobStore.upsert(job)

                    tracks.forEach { pendingTrack ->
                        pendingTrack.status = TrackStatus.PENDING
                        pendingTrack.error =
                            "Очікує повторної авторизації Google/YTM"
                    }

                    onHistoryState(job, HistoryStatus.FAILED)

                    return WriteOutcome.AuthorizationInvalidated(
                        job = job,
                        error = error
                    )
                }

                if (isQuotaError(error)) {
                    quotaTracker.recordQuotaError(
                        error.message ?: "Quota exceeded while creating playlist"
                    )

                    val userMessage =
                        WritePausePolicy.userMessage(
                            kind = YouTubeLimitKind.DAILY_QUOTA,
                            action = WritePauseAction.CREATE_PLAYLIST
                        )

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            lastError = userMessage,
                            pauseReason =
                                PendingPauseReason.DAILY_QUOTA
                        )

                    pendingJobStore.upsert(job)

                    tracks.forEach { track ->
                        track.status = TrackStatus.PENDING
                        track.error = userMessage
                    }

                    onHistoryState(job, HistoryStatus.PENDING_QUOTA)
                    return WriteOutcome.PausedForQuota(
                        job = job,
                        userMessage = userMessage
                    )
                }

                retryableWriteLimitKind(error)
                    ?.let { limitKind ->
                        val pauseReason =
                            WritePausePolicy.pendingReason(
                                limitKind
                            )

                        val userMessage =
                            WritePausePolicy.userMessage(
                                kind = limitKind,
                                action =
                                    WritePauseAction.CREATE_PLAYLIST
                            )

                        job =
                            job.copy(
                                updatedAt =
                                    System.currentTimeMillis(),
                                lastError =
                                    userMessage,
                                pauseReason =
                                    pauseReason,
                                remainingTracks =
                                    tracks.mapNotNull(
                                        ::trackToPendingTrack
                                    )
                            )

                        pendingJobStore.upsert(job)

                        tracks.forEach { track ->
                            track.status =
                                TrackStatus.PENDING
                            track.error =
                                userMessage
                        }

                        onHistoryState(
                            job,
                            HistoryStatus.PENDING_LIMIT
                        )

                        return WriteOutcome.PausedForLimit(
                            job = job,
                            pauseReason = pauseReason,
                            userMessage = userMessage
                        )
                    }

                val friendlyError =
                    ErrorMessages.userMessage(
                        error,
                        "Не вдалося створити плейлист"
                    )

                job =
                    job.copy(
                        updatedAt = System.currentTimeMillis(),
                        lastError = friendlyError
                    )

                onHistoryState(job, HistoryStatus.FAILED)
                pendingJobStore.remove(job.id)

                return WriteOutcome.Failed(
                    job = job,
                    userMessage = friendlyError
                )
            }
        } else {
            onPlaylistIdAvailable(playlistId)
        }

        val createOffset =
            if (job.destination == PendingDestination.NEW_PLAYLIST) 1 else 0

        for ((index, track) in tracks.withIndex()) {
            onTrackStart(
                track,
                index,
                tracks.size
            )

            val videoId = track.selectedVideoId

            if (videoId.isNullOrBlank()) {
                track.status = TrackStatus.FAILED
                track.error = "Немає videoId для додавання"

                job =
                    job.copy(
                        updatedAt = System.currentTimeMillis(),
                        failedCount = job.failedCount + 1,
                        remainingTracks = job.remainingTracks.drop(1),
                        lastError = track.error
                    )

                pendingJobStore.upsert(job)
                onHistoryState(job, HistoryStatus.RUNNING)

                onProgress(
                    WriteProgress(
                        job = job,
                        processedTracks = index + 1,
                        totalTracks = tracks.size,
                        createOffset = createOffset
                    )
                )
                continue
            }

            quotaTracker.recordGeneralUnits(
                QuotaTracker.PLAYLIST_ITEM_INSERT_COST
            )

            try {
                api.addVideo(
                    token,
                    playlistId!!,
                    videoId
                )

                track.status = TrackStatus.ADDED
                track.error = null

                job =
                    job.copy(
                        updatedAt = System.currentTimeMillis(),
                        addedCount = job.addedCount + 1,
                        remainingTracks = job.remainingTracks.drop(1),
                        lastError = null,
                        pauseReason = null
                    )

                pendingJobStore.upsert(job)
                onHistoryState(job, HistoryStatus.RUNNING)
            } catch (error: Exception) {
                if (isAuthorizationError(error)) {
                    val remaining =
                        tracks
                            .drop(index)
                            .mapNotNull(::trackToPendingTrack)

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            remainingTracks = remaining,
                            lastError = error.message
                        )

                    pendingJobStore.upsert(job)

                    tracks.drop(index).forEach { pendingTrack ->
                        pendingTrack.status = TrackStatus.PENDING
                        pendingTrack.error =
                            "Очікує повторної авторизації Google/YTM"
                    }

                    onHistoryState(job, HistoryStatus.FAILED)

                    return WriteOutcome.AuthorizationInvalidated(
                        job = job,
                        error = error
                    )
                }

                if (isQuotaError(error)) {
                    quotaTracker.recordQuotaError(
                        error.message ?: "Quota exceeded while adding track"
                    )

                    val remaining =
                        tracks
                            .drop(index)
                            .mapNotNull(::trackToPendingTrack)

                    val userMessage =
                        WritePausePolicy.userMessage(
                            kind = YouTubeLimitKind.DAILY_QUOTA,
                            action = WritePauseAction.ADD_TRACK
                        )

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            remainingTracks = remaining,
                            lastError = userMessage,
                            pauseReason =
                                PendingPauseReason.DAILY_QUOTA
                        )

                    pendingJobStore.upsert(job)

                    tracks.drop(index).forEach { pendingTrack ->
                        pendingTrack.status = TrackStatus.PENDING
                        pendingTrack.error = userMessage
                    }

                    onHistoryState(job, HistoryStatus.PENDING_QUOTA)
                    return WriteOutcome.PausedForQuota(
                        job = job,
                        userMessage = userMessage
                    )
                }

                retryableWriteLimitKind(error)
                    ?.let { limitKind ->
                        val remaining =
                            tracks
                                .drop(index)
                                .mapNotNull(
                                    ::trackToPendingTrack
                                )

                        val pauseReason =
                            WritePausePolicy.pendingReason(
                                limitKind
                            )

                        val userMessage =
                            WritePausePolicy.userMessage(
                                kind = limitKind,
                                action =
                                    WritePauseAction.ADD_TRACK
                            )

                        job =
                            job.copy(
                                updatedAt =
                                    System.currentTimeMillis(),
                                remainingTracks =
                                    remaining,
                                lastError =
                                    userMessage,
                                pauseReason =
                                    pauseReason
                            )

                        pendingJobStore.upsert(job)

                        tracks.drop(index)
                            .forEach {
                                pendingTrack ->
                                pendingTrack.status =
                                    TrackStatus.PENDING
                                pendingTrack.error =
                                    userMessage
                            }

                        onHistoryState(
                            job,
                            HistoryStatus.PENDING_LIMIT
                        )

                        return WriteOutcome.PausedForLimit(
                            job = job,
                            pauseReason = pauseReason,
                            userMessage = userMessage
                        )
                    }

                val friendlyError =
                    ErrorMessages.userMessage(
                        error,
                        "Не вдалося додати трек"
                    )

                track.status = TrackStatus.FAILED
                track.error = friendlyError

                job =
                    job.copy(
                        updatedAt = System.currentTimeMillis(),
                        failedCount = job.failedCount + 1,
                        remainingTracks = job.remainingTracks.drop(1),
                        lastError = friendlyError
                    )

                pendingJobStore.upsert(job)
                onHistoryState(job, HistoryStatus.RUNNING)
            }

            onProgress(
                WriteProgress(
                    job = job,
                    processedTracks = index + 1,
                    totalTracks = tracks.size,
                    createOffset = createOffset
                )
            )
        }

        onHistoryState(
            job,
            if (job.failedCount > 0) {
                HistoryStatus.PARTIAL
            } else {
                HistoryStatus.COMPLETED
            }
        )

        pendingJobStore.remove(job.id)

        return WriteOutcome.Completed(
            job = job,
            playlistId = playlistId!!
        )
    }

    private fun trackToPendingTrack(track: Track): PendingTrack? {
        val videoId = track.selectedVideoId ?: return null

        return PendingTrack(
            originalTitle = track.originalTitle,
            originalArtist = track.originalArtist,
            videoId = videoId,
            selectedTitle = track.selectedTitle,
            selectedChannel = track.selectedChannel,
            historyIndex = track.historyIndex ?: -1
        )
    }

    private fun isAuthorizationError(
        error: Throwable
    ): Boolean =
        (error as? YouTubeApiException)?.httpCode == 401

    private fun isQuotaError(
        error: Throwable
    ): Boolean =
        (error as? YouTubeApiException)
            ?.isQuotaError == true

    private fun retryableWriteLimitKind(
        error: Throwable
    ): YouTubeLimitKind? =
        (error as? YouTubeApiException)
            ?.limitKind
            ?.takeIf {
                it !=
                    YouTubeLimitKind.DAILY_QUOTA
            }
}
