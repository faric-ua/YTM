package com.saney.ytmimporter.destination

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException

/**
 * Owns destination-playlist and duplicate domain orchestration.
 *
 * This class intentionally contains no Android Activity/View code.
 * MainActivity remains responsible for authorization, screen navigation and
 * rendering progress/errors, while DestinationCoordinator owns:
 *
 * - choosing tracks eligible for destination write;
 * - caching the user's destination playlist list for one flow;
 * - resolving the selected existing playlist;
 * - reading an existing playlist and accounting scan quota;
 * - exact-videoId duplicate analysis;
 * - duplicate write-plan construction;
 * - temporary destination-flow state.
 */
class DestinationCoordinator(
    private val api: YouTubeApi,
    private val quotaTracker: QuotaTracker
) {
    data class DuplicateAnalysis(
        val alreadyInPlaylist: List<Track>,
        val repeatedInImport: List<Track>,
        val tracksToAdd: List<Track>
    ) {
        val tracksToSkip: List<Track>
            get() = alreadyInPlaylist + repeatedInImport

        val totalDuplicates: Int
            get() = tracksToSkip.size
    }

    data class DuplicateScanResult(
        val target: YouTubePlaylistInfo,
        val analysis: DuplicateAnalysis,
        val requestCount: Int
    )

    enum class DuplicateMode {
        SKIP,
        ADD_ALL,
        NO_SCAN
    }

    data class ExistingWritePlan(
        val target: YouTubePlaylistInfo,
        val tracksToWrite: List<Track>,
        val tracksToSkip: List<Track>,
        val alreadyInPlaylistCount: Int,
        val repeatedInImportCount: Int,
        val scanRequestCount: Int,
        val scanSucceeded: Boolean,
        val addDuplicatesAnyway: Boolean
    ) {
        val duplicatesFound: Int
            get() = alreadyInPlaylistCount + repeatedInImportCount

        val savedWriteUnits: Int
            get() = tracksToSkip.size * QuotaTracker.PLAYLIST_ITEM_INSERT_COST
    }

    private var cachedDestinationPlaylists: List<YouTubePlaylistInfo> = emptyList()
    private var pendingTarget: YouTubePlaylistInfo? = null
    private var pendingAnalysis: DuplicateAnalysis? = null
    private var pendingScanRequestCount: Int = 0

    fun reset() {
        cachedDestinationPlaylists = emptyList()
        clearPendingSelection()
    }

    fun currentTracksForDestination(
        playlist: ImportedPlaylist
    ): List<Track> =
        playlist.tracks.filter { track ->
            !track.selectedVideoId.isNullOrBlank() &&
                track.status != TrackStatus.SKIPPED &&
                track.status != TrackStatus.WAITING_QUOTA
        }

    fun cachedPlaylists(): List<YouTubePlaylistInfo> =
        cachedDestinationPlaylists

    fun loadExistingPlaylists(
        accessToken: String
    ): List<YouTubePlaylistInfo> {
        // Preserve the v1.4.15 accounting behavior: one local list cost is
        // recorded for the playlist-list operation.
        quotaTracker.recordGeneralUnits(
            QuotaTracker.SIMPLE_LIST_COST
        )

        val playlists =
            api.listMyPlaylists(accessToken)

        cachedDestinationPlaylists = playlists
        return playlists
    }

    fun selectExistingTarget(
        id: String,
        title: String?,
        privacyStatus: String?,
        itemCount: Long
    ): YouTubePlaylistInfo? {
        val normalizedId = id.trim()
        if (normalizedId.isBlank()) return null

        val target =
            cachedDestinationPlaylists
                .firstOrNull { it.id == normalizedId }
                ?: YouTubePlaylistInfo(
                    id = normalizedId,
                    title = title ?: "Плейлист",
                    privacyStatus = privacyStatus ?: "private",
                    itemCount = itemCount
                )

        pendingTarget = target
        pendingAnalysis = null
        pendingScanRequestCount = 0

        return target
    }

    fun scanDuplicates(
        accessToken: String,
        selected: List<Track>,
        target: YouTubePlaylistInfo
    ): DuplicateScanResult {
        pendingTarget = target

        try {
            val playlistContents =
                api.listPlaylistVideoIds(
                    accessToken = accessToken,
                    playlistId = target.id
                )

            quotaTracker.recordGeneralUnits(
                playlistContents.requestCount *
                    QuotaTracker.SIMPLE_LIST_COST
            )

            val analysis =
                analyzeDuplicates(
                    selected = selected,
                    existingVideoIds = playlistContents.videoIds
                )

            pendingAnalysis = analysis
            pendingScanRequestCount = playlistContents.requestCount

            return DuplicateScanResult(
                target = target,
                analysis = analysis,
                requestCount = playlistContents.requestCount
            )
        } catch (error: Throwable) {
            if (isQuotaError(error)) {
                quotaTracker.recordQuotaError(
                    error.message
                        ?: "Не вдалося перевірити дублікати через квоту"
                )
            }

            pendingAnalysis = null
            pendingScanRequestCount = 0
            throw error
        }
    }

    fun buildExistingWritePlan(
        selected: List<Track>,
        mode: DuplicateMode
    ): ExistingWritePlan {
        val target =
            pendingTarget
                ?: throw IllegalStateException(
                    "Цільовий плейлист уже недоступний. Виберіть його ще раз."
                )

        val analysis = pendingAnalysis

        val plan =
            when (mode) {
                DuplicateMode.NO_SCAN ->
                    ExistingWritePlan(
                        target = target,
                        tracksToWrite = selected,
                        tracksToSkip = emptyList(),
                        alreadyInPlaylistCount = 0,
                        repeatedInImportCount = 0,
                        scanRequestCount = 0,
                        scanSucceeded = false,
                        addDuplicatesAnyway = true
                    )

                DuplicateMode.ADD_ALL -> {
                    val currentAnalysis =
                        analysis
                            ?: throw IllegalStateException(
                                "Результат перевірки дублікатів уже недоступний. Спробуйте ще раз."
                            )

                    ExistingWritePlan(
                        target = target,
                        tracksToWrite = selected,
                        tracksToSkip = emptyList(),
                        alreadyInPlaylistCount = currentAnalysis.alreadyInPlaylist.size,
                        repeatedInImportCount = currentAnalysis.repeatedInImport.size,
                        scanRequestCount = pendingScanRequestCount,
                        scanSucceeded = true,
                        addDuplicatesAnyway = true
                    )
                }

                DuplicateMode.SKIP -> {
                    val currentAnalysis =
                        analysis
                            ?: throw IllegalStateException(
                                "Результат перевірки дублікатів уже недоступний. Спробуйте ще раз."
                            )

                    ExistingWritePlan(
                        target = target,
                        tracksToWrite = currentAnalysis.tracksToAdd,
                        tracksToSkip = currentAnalysis.tracksToSkip,
                        alreadyInPlaylistCount = currentAnalysis.alreadyInPlaylist.size,
                        repeatedInImportCount = currentAnalysis.repeatedInImport.size,
                        scanRequestCount = pendingScanRequestCount,
                        scanSucceeded = true,
                        addDuplicatesAnyway = false
                    )
                }
            }

        clearPendingSelection()
        return plan
    }

    private fun analyzeDuplicates(
        selected: List<Track>,
        existingVideoIds: Set<String>
    ): DuplicateAnalysis {
        val alreadyInPlaylist = mutableListOf<Track>()
        val repeatedInImport = mutableListOf<Track>()
        val tracksToAdd = mutableListOf<Track>()
        val seenIncoming = mutableSetOf<String>()

        selected.forEach { track ->
            val videoId =
                track.selectedVideoId
                    ?.trim()
                    .orEmpty()

            if (videoId.isBlank()) {
                tracksToAdd += track
                return@forEach
            }

            val isAlreadyInPlaylist =
                videoId in existingVideoIds

            val isRepeatedInImport =
                !seenIncoming.add(videoId)

            when {
                isAlreadyInPlaylist ->
                    alreadyInPlaylist += track

                isRepeatedInImport ->
                    repeatedInImport += track

                else ->
                    tracksToAdd += track
            }
        }

        return DuplicateAnalysis(
            alreadyInPlaylist = alreadyInPlaylist,
            repeatedInImport = repeatedInImport,
            tracksToAdd = tracksToAdd
        )
    }

    private fun clearPendingSelection() {
        pendingTarget = null
        pendingAnalysis = null
        pendingScanRequestCount = 0
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
}
