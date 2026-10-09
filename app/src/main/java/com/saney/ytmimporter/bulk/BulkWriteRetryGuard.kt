package com.saney.ytmimporter.bulk

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Local safety floor after YouTube rejects a write for rate/resource pressure.
 * This is NOT YouTube's published daily playlist-creation allowance and does
 * not guarantee that YouTube will accept a request after the deadline.
 *
 * No work is scheduled here: an explicit user action is always required.
 */
object BulkWriteRetryGuard {
    const val LOCAL_MINIMUM_WAIT_MS: Long = 15L * 60L * 1_000L

    fun retryNotBefore(
        retryAfterHeader: String?,
        nowEpochMs: Long
    ): Long {
        val localFloor = safeAdd(
            nowEpochMs,
            LOCAL_MINIMUM_WAIT_MS
        )
        val serverDeadline = parseRetryAfter(
            retryAfterHeader,
            nowEpochMs
        ) ?: localFloor

        return maxOf(localFloor, serverDeadline)
    }

    fun isWaiting(
        session: BulkSyncSession,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Boolean =
        session.state == BulkSyncSessionState.PAUSED_RATE_LIMIT &&
            (session.retryNotBeforeEpochMs ?: 0L) > nowEpochMs

    /**
     * Retry-After permits either delta-seconds or an RFC 1123 HTTP date.
     * Never interpret invalid header text as a safe immediate retry.
     */
    fun parseRetryAfter(
        rawHeader: String?,
        nowEpochMs: Long
    ): Long? {
        val value = rawHeader?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: return null

        value.toLongOrNull()?.let { seconds ->
            if (seconds < 0L) return null
            val duration =
                if (seconds > Long.MAX_VALUE / 1_000L) {
                    Long.MAX_VALUE
                } else {
                    seconds * 1_000L
                }
            return safeAdd(nowEpochMs, duration)
        }

        return runCatching {
            ZonedDateTime.parse(
                value,
                DateTimeFormatter.RFC_1123_DATE_TIME
            ).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun safeAdd(start: Long, duration: Long): Long =
        if (duration > 0L && start > Long.MAX_VALUE - duration) {
            Long.MAX_VALUE
        } else {
            start + duration
        }
}
