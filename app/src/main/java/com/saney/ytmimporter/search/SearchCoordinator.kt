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
        val quotaBlocked: Boolean
    )

    fun plan(
        playlist: ImportedPlaylist,
        preserveExistingExact: Boolean
    ): SearchPlan {
        val tracksToSearch =
            playlist.tracks.filter { track ->
                shouldSearch(
                    track = track,
                    preserveExistingExact = preserveExistingExact
                )
            }

        val cachedCount =
            tracksToSearch.count { track ->
                searchCache.get(track) != null
            }

        return SearchPlan(
            totalTracks = playlist.tracks.size,
            tracksToSearch = tracksToSearch.size,
            cachedCount = cachedCount,
            apiNeeded = tracksToSearch.size - cachedCount,
            quota = quotaTracker.snapshot()
        )
    }

    fun run(
        accessToken: String,
        playlist: ImportedPlaylist,
        preserveExistingExact: Boolean,
        onTrackStateChanged: (index: Int) -> Unit = {},
        onProgress: (SearchProgress) -> Unit = {},
        onQuotaBlocked: () -> Unit = {}
    ): SearchResult {
        var cacheHits = 0
        var apiSearches = 0
        var quotaBlocked = false
        var quotaCallbackSent = false

        for ((index, track) in playlist.tracks.withIndex()) {
            if (Thread.currentThread().isInterrupted) {
                break
            }

            val preservedSelection =
                preservedSelection(
                    track = track,
                    preserveExistingExact = preserveExistingExact
                )

            if (preservedSelection != null) {
                onProgress(
                    SearchProgress(
                        processed = index + 1,
                        total = playlist.tracks.size,
                        cacheHits = cacheHits,
                        apiSearches = apiSearches,
                        preservedSelection = preservedSelection
                    )
                )
                continue
            }

            track.status = TrackStatus.SEARCHING
            track.error = null
            onTrackStateChanged(index)

            try {
                val cachedCandidates =
                    searchCache.get(track)

                val candidates =
                    if (cachedCandidates != null) {
                        cacheHits += 1
                        quotaTracker.recordCacheHit()
                        cachedCandidates
                    } else if (quotaBlocked) {
                        track.status =
                            TrackStatus.FAILED
                        track.error =
                            "Немає в кеші, а квота YouTube search API вже закінчилась"
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

                if (track.status != TrackStatus.FAILED) {
                    applySearchCandidates(
                        track = track,
                        candidates = candidates
                    )
                }
            } catch (error: Exception) {
                track.status =
                    TrackStatus.FAILED
                track.error =
                    ErrorMessages.userMessage(
                        error,
                        "Не вдалося виконати пошук"
                    )

                if (isQuotaError(error)) {
                    quotaBlocked = true
                    quotaTracker.recordQuotaError(
                        error.message
                            ?: "Search quota exceeded"
                    )

                    if (!quotaCallbackSent) {
                        quotaCallbackSent = true
                        onQuotaBlocked()
                    }
                }
            }

            onProgress(
                SearchProgress(
                    processed = index + 1,
                    total = playlist.tracks.size,
                    cacheHits = cacheHits,
                    apiSearches = apiSearches
                )
            )
        }

        return SearchResult(
            cacheHits = cacheHits,
            apiSearches = apiSearches,
            quotaBlocked = quotaBlocked
        )
    }

    private fun shouldSearch(
        track: Track,
        preserveExistingExact: Boolean
    ): Boolean {
        val manualExact =
            track.manuallySelected &&
                !track.selectedVideoId.isNullOrBlank()

        if (manualExact) {
            return false
        }

        return if (preserveExistingExact) {
            track.selectedVideoId.isNullOrBlank() ||
                track.status != TrackStatus.MATCHED ||
                track.candidates.isNotEmpty()
        } else {
            true
        }
    }

    private fun preservedSelection(
        track: Track,
        preserveExistingExact: Boolean
    ): PreservedSelection? {
        val keepManualSelection =
            track.manuallySelected &&
                !track.selectedVideoId.isNullOrBlank()

        if (keepManualSelection) {
            return PreservedSelection.MANUAL
        }

        val keepExactSelection =
            preserveExistingExact &&
                !track.selectedVideoId.isNullOrBlank() &&
                track.status == TrackStatus.MATCHED &&
                track.candidates.isEmpty()

        return if (keepExactSelection) {
            PreservedSelection.PROJECT_EXACT
        } else {
            null
        }
    }

    private fun applySearchCandidates(
        track: Track,
        candidates: List<SearchCandidate>
    ) {
        track.candidates =
            candidates

        if (
            track.manuallySelected &&
            !track.selectedVideoId.isNullOrBlank()
        ) {
            track.status =
                TrackStatus.MATCHED
            track.error = null
            return
        }

        val best =
            candidates.firstOrNull()

        if (best == null) {
            track.status =
                TrackStatus.MISSING
            track.selectedVideoId = null
            track.selectedTitle = null
            track.selectedChannel = null
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
        track.error = null

        track.status =
            if (best.score >= AUTO_MATCH_THRESHOLD) {
                TrackStatus.MATCHED
            } else {
                TrackStatus.REVIEW
            }
    }

    private fun isQuotaError(
        error: Throwable
    ): Boolean =
        (error as? YouTubeApiException)
            ?.isQuotaError == true ||
            error.message
                .orEmpty()
                .contains(
                    "quota",
                    ignoreCase = true
                ) ||
            error.message
                .orEmpty()
                .contains(
                    "daily limit",
                    ignoreCase = true
                )

    companion object {
        private const val AUTO_MATCH_THRESHOLD =
            0.72
    }
}
