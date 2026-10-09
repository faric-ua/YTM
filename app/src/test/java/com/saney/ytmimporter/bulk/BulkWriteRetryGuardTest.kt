package com.saney.ytmimporter.bulk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkWriteRetryGuardTest {
    private val now = 1_000_000L
    private val minWait = BulkWriteRetryGuard.LOCAL_MINIMUM_WAIT_MS

    @Test
    fun noRetryAfter_usesConservativeLocalFloor() {
        assertEquals(
            now + minWait,
            BulkWriteRetryGuard.retryNotBefore(null, now)
        )
    }

    @Test
    fun serverDelayLongerThanLocalFloor_isRespected() {
        assertEquals(
            now + 3_600_000L,
            BulkWriteRetryGuard.retryNotBefore("3600", now)
        )
    }

    @Test
    fun shorterServerDelay_doesNotDefeatLocalFloor() {
        assertEquals(
            now + minWait,
            BulkWriteRetryGuard.retryNotBefore("30", now)
        )
    }

    @Test
    fun httpDate_isAccepted() {
        val date = "Wed, 21 Oct 2015 07:28:00 GMT"
        val start = 1_445_410_000_000L
        assertEquals(
            1_445_412_480_000L,
            BulkWriteRetryGuard.parseRetryAfter(date, start)
        )
    }

    @Test
    fun malformedRetryAfter_doesNotPermitImmediateRetry() {
        assertEquals(
            now + minWait,
            BulkWriteRetryGuard.retryNotBefore("nonsense", now)
        )
    }

    @Test
    fun hugeSeconds_saturatesInsteadOfWrapping() {
        assertEquals(
            Long.MAX_VALUE,
            BulkWriteRetryGuard.retryNotBefore(
                Long.MAX_VALUE.toString(),
                now
            )
        )
    }

    @Test
    fun gateBlocksOnlyPausedRateLimitBeforeDeadline() {
        val base = minimalSession()
        val cooldown = base.copy(
            state = BulkSyncSessionState.PAUSED_RATE_LIMIT,
            retryNotBeforeEpochMs = now + minWait
        )
        assertTrue(BulkWriteRetryGuard.isWaiting(cooldown, now))
        assertFalse(
            BulkWriteRetryGuard.isWaiting(
                cooldown, now + minWait
            )
        )
        assertFalse(
            BulkWriteRetryGuard.isWaiting(
                cooldown.copy(state = BulkSyncSessionState.READY),
                now
            )
        )
        assertFalse(
            BulkSyncExecutionPolicy.canExplicitlyResume(cooldown, now)
        )
        assertTrue(
            BulkSyncExecutionPolicy.canExplicitlyResume(
                cooldown, now + minWait
            )
        )
    }

    private fun minimalSession(): BulkSyncSession =
        BulkSyncSession(
            sessionId = "test-session",
            createdAt = now,
            updatedAt = now,
            state = BulkSyncSessionState.READY,
            googleEmail = null,
            youtubeChannelId = null,
            youtubeChannelTitle = null,
            checkpointId = "checkpoint",
            remoteBaseline = BulkSyncRemoteBaseline(
                capturedAt = now,
                googleEmail = null,
                youtubeChannelId = null,
                youtubeChannelTitle = null,
                playlists = emptyList()
            ),
            plan = emptyList(),
            currentPlanIndex = 0,
            mutationLedger = emptyList()
        )
}
