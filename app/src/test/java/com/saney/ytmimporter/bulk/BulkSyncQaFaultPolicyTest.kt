package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.youtube.YouTubeLimitKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkSyncQaFaultPolicyTest {
    @Test
    fun dailyQuotaFault_isClassifiedAsDailyQuota() {
        val error =
            BulkSyncQaFaultPolicy
                .asException(
                    BulkSyncQaFaultKind
                        .DAILY_QUOTA
                )

        assertEquals(
            403,
            error.httpCode
        )
        assertEquals(
            YouTubeLimitKind.DAILY_QUOTA,
            error.limitKind
        )
        assertTrue(
            error.message.contains(
                "QA Test 5"
            )
        )
    }
}
