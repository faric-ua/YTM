package com.saney.ytmimporter.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class QuotaMathTest {
    @Test
    fun generalUnitsExcludeSearchBucket() {
        assertEquals(
            5771,
            QuotaMath.generalUnits(
                nonSearchUnits = 5771
            )
        )

        assertEquals(
            4229,
            QuotaMath.generalRemaining(
                generalUnits = 5771,
                dailyLimit = 10000
            )
        )
    }

    @Test
    fun searchRemainingUsesOnlySearchBucket() {
        assertEquals(
            20,
            QuotaMath.searchRemaining(
                searchCalls = 80,
                searchDailyLimit = 100
            )
        )

        assertEquals(
            0,
            QuotaMath.searchRemaining(
                searchCalls = 100,
                searchDailyLimit = 100
            )
        )

        assertEquals(
            0,
            QuotaMath.searchRemaining(
                searchCalls = 120,
                searchDailyLimit = 100
            )
        )
    }
}
