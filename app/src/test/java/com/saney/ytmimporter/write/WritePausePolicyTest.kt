package com.saney.ytmimporter.write

import com.saney.ytmimporter.model.PendingPauseReason
import com.saney.ytmimporter.youtube.YouTubeLimitKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WritePausePolicyTest {
    @Test
    fun rateLimitMapsToDurableRatePause() {
        assertEquals(
            PendingPauseReason.RATE_LIMIT,
            WritePausePolicy.pendingReason(
                YouTubeLimitKind.RATE_LIMIT
            )
        )
    }

    @Test
    fun generic429CopyDoesNotClaimDailyQuota() {
        val message =
            WritePausePolicy.userMessage(
                YouTubeLimitKind.UNKNOWN_429,
                WritePauseAction.CREATE_PLAYLIST
            )

        assertTrue(
            message.contains("HTTP 429")
        )
        assertTrue(
            message.contains("Черзі")
        )
        assertTrue(
            message.contains("Зачекайте")
        )
        assertFalse(
            message.contains(
                "вичерпання добової квоти"
            )
        )
    }

    @Test
    fun rateLimitCopyWarnsAgainstRapidRetries() {
        val message =
            WritePausePolicy.userMessage(
                YouTubeLimitKind.RATE_LIMIT,
                WritePauseAction.CREATE_PLAYLIST
            )

        assertTrue(
            message.contains(
                "не запускайте",
                ignoreCase = true
            )
        )
        assertTrue(
            message.contains(
                "Продовжити"
            )
        )
    }
}
