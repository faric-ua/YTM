package com.saney.ytmimporter.urlsnapshot

import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException

class UrlSnapshotResolver(
    private val playlistReader:
        UrlSnapshotConcretePlaylistReader,
    private val quotaRecorder:
        UrlSnapshotQuotaRecorder,
    private val mixReader:
        UrlSnapshotMixReader? = null
) {
    /**
     * Batch 17: the one explicit attempt to read a Mix (button «Спробувати
     * прочитати Mix»). [resolve] itself still never reads a Mix. The result is
     * a snapshot of what YouTube gave at this moment — never called complete.
     */
    fun resolveMixAttempt(
        accessToken: String,
        source: UrlSnapshotSource
    ): UrlSnapshotResolutionResult {
        require(source.kind == UrlSnapshotSourceKind.DYNAMIC_MIX)
        val refused = UrlSnapshotResolutionResult.Unsupported(
            source = source,
            reason = UrlSnapshotUnsupportedReason.MIX_NOT_GIVEN_BY_YOUTUBE
        )
        val reader = mixReader ?: return refused
        val read = try {
            reader.read(
                accessToken = accessToken,
                playlistId = source.playlistId,
                onListRequest = { quotaRecorder.recordSimpleListRequest() }
            )
        } catch (error: YouTubeApiException) {
            if (UrlSnapshotMixPolicy.youTubeRefused(
                    error.httpCode, error.reason, error.isQuotaError)
            ) return refused
            throw error
        }
        val items = UrlSnapshotResolutionPolicy.resolveItems(read.items)
        if (items.none { it.availability == UrlSnapshotAvailability.AVAILABLE }) return refused
        return UrlSnapshotResolutionResult.Resolved(
            source = source,
            items = items,
            requestCount = read.requestCount,
            playlistTitle = UrlSnapshotMixPolicy.title(items)
        )
    }

    fun resolve(
        accessToken: String,
        source: UrlSnapshotSource
    ): UrlSnapshotResolutionResult {
        if (
            source.kind ==
            UrlSnapshotSourceKind.DYNAMIC_MIX
        ) {
            return UrlSnapshotResolutionResult.Unsupported(
                source = source,
                reason =
                    UrlSnapshotUnsupportedReason
                        .DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER
            )
        }

        val read =
            playlistReader.read(
                accessToken = accessToken,
                playlistId = source.playlistId,
                onListRequest = {
                    quotaRecorder
                        .recordSimpleListRequest()
                }
            )

        return UrlSnapshotResolutionResult.Resolved(
            source = source,
            items =
                UrlSnapshotResolutionPolicy
                    .resolveItems(
                        read.items
                    ),
            requestCount =
                read.requestCount,
            playlistTitle =
                read.playlistTitle
        )
    }

    companion object {
        fun production(
            api: YouTubeApi,
            quotaTracker: QuotaTracker
        ): UrlSnapshotResolver =
            UrlSnapshotResolver(
                playlistReader =
                    YouTubeApiSnapshotReader(
                        api
                    ),
                quotaRecorder =
                    QuotaTrackerSnapshotQuotaRecorder(
                        quotaTracker
                    ),
                mixReader =
                    UrlSnapshotMixReader { accessToken, playlistId, onListRequest ->
                        val result = api.listPlaylistSnapshotItems(
                            accessToken = accessToken,
                            playlistId = playlistId,
                            onListRequest = onListRequest,
                            pageLimit = UrlSnapshotMixPolicy.PAGE_LIMIT
                        )
                        UrlSnapshotPlaylistRead(
                            items = result.items.map { item ->
                                UrlSnapshotRawItem(
                                    playlistItemId = item.playlistItemId,
                                    sourcePosition = item.sourcePosition,
                                    videoId = item.videoId,
                                    title = item.title,
                                    channelTitle = item.channelTitle,
                                    privacyStatus = item.privacyStatus
                                )
                            },
                            requestCount = result.requestCount
                        )
                    }
            )
    }
}

private class YouTubeApiSnapshotReader(
    private val api: YouTubeApi
) : UrlSnapshotConcretePlaylistReader {
    override fun read(
        accessToken: String,
        playlistId: String,
        onListRequest: () -> Unit
    ): UrlSnapshotPlaylistRead {
        val playlistTitle =
            api.getPlaylistSnapshotTitle(
                accessToken = accessToken,
                playlistId = playlistId,
                onListRequest = onListRequest
            )

        val result =
            api.listPlaylistSnapshotItems(
                accessToken = accessToken,
                playlistId = playlistId,
                onListRequest =
                    onListRequest
            )

        return UrlSnapshotPlaylistRead(
            items =
                result.items.map {
                    item ->
                    UrlSnapshotRawItem(
                        playlistItemId =
                            item.playlistItemId,
                        sourcePosition =
                            item.sourcePosition,
                        videoId =
                            item.videoId,
                        title =
                            item.title,
                        channelTitle =
                            item.channelTitle,
                        privacyStatus =
                            item.privacyStatus
                    )
                },
            requestCount =
                result.requestCount + 1,
            playlistTitle =
                playlistTitle
        )
    }
}

private class QuotaTrackerSnapshotQuotaRecorder(
    private val quotaTracker: QuotaTracker
) : UrlSnapshotQuotaRecorder {
    override fun recordSimpleListRequest() {
        quotaTracker.recordGeneralUnits(
            QuotaTracker.SIMPLE_LIST_COST
        )
    }
}
