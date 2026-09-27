package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.youtube.YouTubeApiException

enum class BulkSyncQaFaultKind {
    DAILY_QUOTA
}

object BulkSyncQaFaultPolicy {
    fun asException(
        kind: BulkSyncQaFaultKind
    ): YouTubeApiException =
        when (kind) {
            BulkSyncQaFaultKind.DAILY_QUOTA ->
                YouTubeApiException(
                    httpCode = 403,
                    reason = "quotaExceeded",
                    message =
                        "QA Test 5: simulated daily quota exceeded",
                    status = "RESOURCE_EXHAUSTED",
                    detailReasons =
                        listOf(
                            "quotaExceeded"
                        )
                )
        }
}
