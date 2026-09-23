package com.saney.ytmimporter.urlsnapshot

import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.youtube.YouTubeApi

class UrlSnapshotResolver(
    private val playlistReader:
        UrlSnapshotConcretePlaylistReader,
    private val quotaRecorder:
        UrlSnapshotQuotaRecorder
) {
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
                    )
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
