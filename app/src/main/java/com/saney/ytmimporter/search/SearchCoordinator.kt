package com.saney.ytmimporter.search

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.QuotaSnapshot
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.util.ErrorMessages
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException

/**
 * Owns track-search domain orchestration.
 *
 * This class intentionally contains no Android View/Activity code.
 * MainActivity remains responsible for authorization and rendering progress,
 * while SearchCoordinator owns:
 *
 * - deciding which tracks need search;
 * - cache/API accounting;
 * - search quota accounting;
 * - automatic best-candidate application;
 * - quota-stop behavior;
 * - per-track search state transitions.
 */
class SearchCoordinator(
    private val api: YouTubeApi,
    private val searchCache: SearchCache,
    private val quotaTracker: QuotaTracker
) {
    data class SearchPlan(
        val totalTracks: Int,
        val tracksToSearch: Int,
        val cachedCount: Int,
        val apiNeeded: Int,
        val quota: QuotaSnapshot
    )

    enum class PreservedSelection {
        MANUAL,
        PROJECT_EXACT
    }

    data class SearchProgress(
        val processed: Int,
        val total: Int,
        val cacheHits: Int,
        val apiSearches: Int,
        val preservedSelection: PreservedSelection? = null
    )

    data class SearchResult(
        val cacheHits: Int,
        val apiSearches: Int,
        val quotaBlocked: Boolean,
        val authorizationInvalidated: Boolean,
        val waitingQuotaCount: Int
    )

    fun plan(
        playlist: ImportedPlaylist,
        preserveExistingExact: Boolean,
        resumeWaitingOnly: Boolean = false
    ): SearchPlan {
        val tracksToSearch =
            playlist.tracks.filter { track ->
                shouldSearch(
                    track = track,
                    preserveExistingExact =
                        preserveExistingExact,
                    resumeWaitingOnly =
                        resumeWaitingOnly
                )
            }

        val cachedCount =
            tracksToSearch.count { track ->
                searchCache.get(track) != null
            }

        return SearchPlan(
            totalTracks =
                playlist.tracks.size,
            tracksToSearch =
                tracksToSearch.size,
            cachedCount =
                cachedCount,
            apiNeeded =
                tracksToSearch.size -
                    cachedCount,
            quota =
                quotaTracker.snapshot()
        )
    }

    fun run(
        accessToken: String,
        playlist: ImportedPlaylist,
        preserveExistingExact: Boolean,
        resumeWaitingOnly: Boolean = false,
        onTrackStateChanged: (index: Int) -> Unit = {},
        onProgress: (SearchProgress) -> Unit = {},
        onQuotaBlocked: () -> Unit = {},
        onAuthorizationInvalidated: (Throwable) -> Unit = {}
    ): SearchResult {
        var cacheHits = 0
        var apiSearches = 0
        var quotaBlocked = false
        var quotaCallbackSent = false
        var authorizationInvalidated = false

        val indexesToProcess =
            playlist.tracks
                .indices
                .filter { index ->
                    shouldSearch(
                        track =
                            playlist.tracks[index],
                        preserveExistingExact =
                            preserveExistingExact,
                        resumeWaitingOnly =
                            resumeWaitingOnly
                    )
                }

        for (
            (
                progressIndex,
                trackIndex
            ) in indexesToProcess
                .withIndex()
        ) {
            if (
                Thread.currentThread()
                    .isInterrupted
            ) {
                break
            }

            val track =
                playlist.tracks[
                    trackIndex
                ]

            val preservedSelection =
                preservedSelection(
                    track = track,
                    preserveExistingExact =
                        preserveExistingExact
                )

            if (
                preservedSelection != null
            ) {
                onProgress(
                    SearchProgress(
                        processed =
                            progressIndex + 1,
                        total =
                            indexesToProcess.size,
                        cacheHits =
                            cacheHits,
                        apiSearches =
                            apiSearches,
                        preservedSelection =
                            preservedSelection
                    )
                )
                continue
            }

            track.status =
                TrackStatus.SEARCHING
            track.error = null

            onTrackStateChanged(
                trackIndex
            )

            try {
                val cachedCandidates =
                    searchCache.get(
                        track
                    )

                val candidates =
                    if (
                        cachedCandidates != null
                    ) {
                        cacheHits += 1
                        quotaTracker.recordCacheHit()
                        cachedCandidates
                    } else if (
                        quotaBlocked
                    ) {
                        track.status =
                            TrackStatus
                                .WAITING_QUOTA
                        track.error =
                            WAITING_QUOTA_MESSAGE
                        emptyList()
                    } else {
                        apiSearches += 1
                        quotaTracker.recordSearchCall()

                        val freshCandidates =
                            api.search(
                                accessToken,
                                track
                            )

                        searchCache.put(
                            track,
                            freshCandidates
                        )

                        freshCandidates
                    }

                if (
                    track.status !=
                    TrackStatus
                        .WAITING_QUOTA
                ) {
                    applySearchCandidates(
                        track = track,
                        candidates =
                            candidates
                    )
                }
            } catch (
                error: Exception
            ) {
                if (
                    isAuthorizationFailure(
                        error
                    )
                ) {
                    // A 401 is a session failure, not a track failure.
                    // Keep the workspace retryable and stop before producing
                    // the same misleading error for every remaining track.
                    track.status =
                        TrackStatus.NEW
                    track.error = null
                    authorizationInvalidated =
                        true

                    onTrackStateChanged(
                        trackIndex
                    )

                    onAuthorizationInvalidated(
                        error
                    )

                    onProgress(
                        SearchProgress(
                            processed =
                                progressIndex +
                                    1,
                            total =
                                indexesToProcess
                                    .size,
                            cacheHits =
                                cacheHits,
                            apiSearches =
                                apiSearches
                        )
                    )
                    break
                }

                if (
                    isQuotaError(
                        error
                    )
                ) {
                    track.status =
                        TrackStatus
                            .WAITING_QUOTA
                    track.error =
                        WAITING_QUOTA_MESSAGE
                    quotaBlocked =
                        true

                    quotaTracker
                        .recordQuotaError(
                            error.message
                                ?: "Search quota exceeded"
                        )

                    if (
                        !quotaCallbackSent
                    ) {
                        quotaCallbackSent =
                            true
                        onQuotaBlocked()
                    }
                } else {
                    track.status =
                        TrackStatus.FAILED
                    track.error =
                        ErrorMessages
                            .userMessage(
                                error,
                                "Не вдалося виконати пошук"
                            )
                }
            }

            onTrackStateChanged(
                trackIndex
            )

            onProgress(
                SearchProgress(
                    processed =
                        progressIndex + 1,
                    total =
                        indexesToProcess.size,
                    cacheHits =
                        cacheHits,
                    apiSearches =
                        apiSearches
                )
            )
        }

        return SearchResult(
            cacheHits =
                cacheHits,
            apiSearches =
                apiSearches,
            quotaBlocked =
                quotaBlocked,
            authorizationInvalidated =
                authorizationInvalidated,
            waitingQuotaCount =
                playlist.tracks.count {
                    it.status ==
                        TrackStatus
                            .WAITING_QUOTA
                }
        )
    }

    private fun shouldSearch(
        track: Track,
        preserveExistingExact: Boolean,
        resumeWaitingOnly: Boolean
    ): Boolean {
        if (
            resumeWaitingOnly
        ) {
            return track.status ==
                TrackStatus
                    .WAITING_QUOTA
        }

        val manualExact =
            track.manuallySelected &&
                !track.selectedVideoId
                    .isNullOrBlank()

        if (
            manualExact
        ) {
            return false
        }

        if (
            preserveExistingExact &&
            hasCanonicalExactSelection(
                track
            )
        ) {
            return false
        }

        return true
    }

    private fun preservedSelection(
        track: Track,
        preserveExistingExact: Boolean
    ): PreservedSelection? {
        val keepManualSelection =
            track.manuallySelected &&
                !track.selectedVideoId
                    .isNullOrBlank()

        if (
            keepManualSelection
        ) {
            return PreservedSelection
                .MANUAL
        }

        val keepExactSelection =
            preserveExistingExact &&
                hasCanonicalExactSelection(
                    track
                )

        return if (
            keepExactSelection
        ) {
            PreservedSelection
                .PROJECT_EXACT
        } else {
            null
        }
    }

    private fun hasCanonicalExactSelection(
        track: Track
    ): Boolean =
        !track.selectedVideoId
            .isNullOrBlank() &&
            track.status ==
                TrackStatus.MATCHED &&
            track.candidates
                .isEmpty()

    private fun applySearchCandidates(
        track: Track,
        candidates:
            List<SearchCandidate>
    ) {
        track.candidates =
            candidates

        if (
            track.manuallySelected &&
            !track.selectedVideoId
                .isNullOrBlank()
        ) {
            track.status =
                TrackStatus.MATCHED
            track.error = null
            return
        }

        val best =
            candidates.firstOrNull()

        if (
            best == null
        ) {
            track.status =
                TrackStatus.MISSING
            track.selectedVideoId =
                null
            track.selectedTitle =
                null
            track.selectedChannel =
                null
            track.error = null
            return
        }

        track.selectedVideoId =
            best.videoId
        track.selectedTitle =
            best.title
        track.selectedChannel =
            best.channelTitle
        track.manuallySelected =
            false
        track.error =
            null

        track.status =
            if (
                best.score >=
                AUTO_MATCH_THRESHOLD
            ) {
                TrackStatus.MATCHED
            } else {
                TrackStatus.REVIEW
            }
    }

    private fun isAuthorizationFailure(
        error: Throwable
    ): Boolean {
        var current:
            Throwable? =
            error

        while (
            current != null
        ) {
            if (
                current is
                    YouTubeApiException &&
                current.httpCode ==
                    401
            ) {
                return true
            }
            current =
                current.cause
        }

        return false
    }

    private fun isQuotaError(
        error: Throwable
    ): Boolean =
        (
            error as?
                YouTubeApiException
            )
            ?.isQuotaError ==
            true ||
            error.message
                .orEmpty()
                .contains(
                    "quota",
                    ignoreCase =
                        true
                ) ||
            error.message
                .orEmpty()
                .contains(
                    "daily limit",
                    ignoreCase =
                        true
                )

    companion object {
        private const val AUTO_MATCH_THRESHOLD =
            0.72

        const val WAITING_QUOTA_MESSAGE =
            "Очікує продовження: квота YouTube Search API закінчилась"
    }
}
