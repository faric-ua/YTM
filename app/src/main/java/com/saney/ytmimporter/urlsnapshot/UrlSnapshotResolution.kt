package com.saney.ytmimporter.urlsnapshot

enum class UrlSnapshotAvailability {
    AVAILABLE,
    UNAVAILABLE
}

enum class UrlSnapshotUnavailableReason {
    PRIVATE_VIDEO,
    DELETED_VIDEO,
    MISSING_VIDEO_ID
}

enum class UrlSnapshotUnsupportedReason {
    DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER,
    /** Batch 17: the one explicit Mix attempt was made, YouTube gave no songs. */
    MIX_NOT_GIVEN_BY_YOUTUBE
}

data class UrlSnapshotRawItem(
    val playlistItemId: String?,
    val sourcePosition: Int?,
    val videoId: String?,
    val title: String?,
    val channelTitle: String?,
    val privacyStatus: String?
)

data class UrlSnapshotResolvedItem(
    val index: Int,
    val playlistItemId: String?,
    val sourcePosition: Int?,
    val videoId: String?,
    val title: String?,
    val channelTitle: String?,
    val availability: UrlSnapshotAvailability,
    val unavailableReason: UrlSnapshotUnavailableReason?
)

data class UrlSnapshotPlaylistRead(
    val items: List<UrlSnapshotRawItem>,
    val requestCount: Int,
    val playlistTitle: String? = null
)

fun interface UrlSnapshotConcretePlaylistReader {
    fun read(
        accessToken: String,
        playlistId: String,
        onListRequest: () -> Unit
    ): UrlSnapshotPlaylistRead
}

/**
 * Batch 17 (faric 2026-11-05 05:45: «чому ми не можемо мікси взяти… може
 * запрос пошлемо, і він нам пришле пачку»): reads the first pages of a Mix
 * through the official YouTube Data API — the same request as for a playlist.
 */
fun interface UrlSnapshotMixReader {
    fun read(
        accessToken: String,
        playlistId: String,
        onListRequest: () -> Unit
    ): UrlSnapshotPlaylistRead
}

/** Batch 17: pure rules of the one explicit Mix attempt. */
object UrlSnapshotMixPolicy {
    /** At most 2 pages = up to 100 songs, 2 quota units. */
    const val PAGE_LIMIT = 2

    /** YouTube said «no such playlist / not readable» — not a sign-in or quota problem. */
    fun youTubeRefused(httpCode: Int, reason: String?, quota: Boolean): Boolean =
        !quota && when (httpCode) {
            404, 400 -> true
            403 -> reason?.contains("NotAccessible", ignoreCase = true) == true ||
                reason?.contains("playlistForbidden", ignoreCase = true) == true
            else -> false
        }

    /** «Mix — <first song>», as YouTube names it; without songs — «YouTube Mix». */
    fun title(items: List<UrlSnapshotResolvedItem>): String =
        items.firstOrNull {
            it.availability == UrlSnapshotAvailability.AVAILABLE && !it.title.isNullOrBlank()
        }?.title?.let { "Mix — $it" } ?: "YouTube Mix"
}

fun interface UrlSnapshotQuotaRecorder {
    fun recordSimpleListRequest()
}

sealed class UrlSnapshotResolutionResult {
    data class Resolved(
        val source: UrlSnapshotSource,
        val items: List<UrlSnapshotResolvedItem>,
        val requestCount: Int,
        val playlistTitle: String? = null
    ) : UrlSnapshotResolutionResult() {
        val unavailableCount: Int
            get() =
                items.count {
                    it.availability ==
                        UrlSnapshotAvailability.UNAVAILABLE
                }
    }

    data class Unsupported(
        val source: UrlSnapshotSource,
        val reason: UrlSnapshotUnsupportedReason
    ) : UrlSnapshotResolutionResult()
}

object UrlSnapshotResolutionPolicy {
    fun resolveItems(
        rawItems: List<UrlSnapshotRawItem>
    ): List<UrlSnapshotResolvedItem> =
        rawItems.mapIndexed {
                index,
                raw ->
            resolveItem(
                index = index,
                raw = raw
            )
        }

    private fun resolveItem(
        index: Int,
        raw: UrlSnapshotRawItem
    ): UrlSnapshotResolvedItem {
        val videoId =
            raw.videoId
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val title =
            raw.title
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val channel =
            raw.channelTitle
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val privacy =
            raw.privacyStatus
                ?.trim()
                ?.lowercase()
                .orEmpty()

        val reason =
            when {
                title.equals(
                    "[Deleted video]",
                    ignoreCase = true
                ) ->
                    UrlSnapshotUnavailableReason
                        .DELETED_VIDEO

                title.equals(
                    "[Private video]",
                    ignoreCase = true
                ) ||
                    privacy == "private" ->
                    UrlSnapshotUnavailableReason
                        .PRIVATE_VIDEO

                videoId == null ->
                    UrlSnapshotUnavailableReason
                        .MISSING_VIDEO_ID

                else ->
                    null
            }

        return UrlSnapshotResolvedItem(
            index = index,
            playlistItemId =
                raw.playlistItemId
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    },
            sourcePosition =
                raw.sourcePosition,
            videoId = videoId,
            title = title,
            channelTitle = channel,
            availability =
                if (reason == null) {
                    UrlSnapshotAvailability.AVAILABLE
                } else {
                    UrlSnapshotAvailability.UNAVAILABLE
                },
            unavailableReason = reason
        )
    }
}
