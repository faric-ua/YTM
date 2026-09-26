package com.saney.ytmimporter.youtube

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeLimitPolicyTest {
    @Test
    fun quotaExceeded403IsDailyQuota() {
        assertEquals(
            YouTubeLimitKind.DAILY_QUOTA,
            YouTubeLimitPolicy.classify(
                httpCode = 403,
                reason = "quotaExceeded",
                status = null,
                detailReasons = emptyList(),
                message = "Quota exceeded"
            )
        )
    }

    @Test
    fun rateLimitReasonIsRateLimit() {
        assertEquals(
            YouTubeLimitKind.RATE_LIMIT,
            YouTubeLimitPolicy.classify(
                httpCode = 429,
                reason = "rateLimitExceeded",
                status = "RESOURCE_EXHAUSTED",
                detailReasons = emptyList(),
                message = "Too many requests"
            )
        )
    }

    @Test
    fun structuredRateLimitReasonWinsOverGeneric429() {
        assertEquals(
            YouTubeLimitKind.RATE_LIMIT,
            YouTubeLimitPolicy.classify(
                httpCode = 429,
                reason = null,
                status = "RESOURCE_EXHAUSTED",
                detailReasons = listOf("RATE_LIMIT_EXCEEDED"),
                message = "Resource has been exhausted"
            )
        )
    }

    @Test
    fun structuredResourceQuotaIsResourceLimit() {
        assertEquals(
            YouTubeLimitKind.RESOURCE_LIMIT,
            YouTubeLimitPolicy.classify(
                httpCode = 429,
                reason = null,
                status = "RESOURCE_EXHAUSTED",
                detailReasons = listOf("RESOURCE_QUOTA_EXCEEDED"),
                message = "Resource has been exhausted"
            )
        )
    }

    @Test
    fun generic429CheckQuotaIsNotMisclassifiedAsDailyQuota() {
        assertEquals(
            YouTubeLimitKind.UNKNOWN_429,
            YouTubeLimitPolicy.classify(
                httpCode = 429,
                reason = null,
                status = "RESOURCE_EXHAUSTED",
                detailReasons = emptyList(),
                message = "Resource has been exhausted (e.g. check quota)"
            )
        )
    }

    @Test
    fun unrelated403IsNotALimit() {
        assertNull(
            YouTubeLimitPolicy.classify(
                httpCode = 403,
                reason = "playlistForbidden",
                status = null,
                detailReasons = emptyList(),
                message = "Forbidden"
            )
        )
    }
}
