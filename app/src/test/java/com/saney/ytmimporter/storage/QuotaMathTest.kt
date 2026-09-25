package com.saney.ytmimporter.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class QuotaMathTest {
    @Test
    fun searchCallsContributeToTotalUnits() {
        val total =
            QuotaMath.totalUnits(
                searchCalls = 98,
                nonSearchUnits = 5771,
                searchListCost = 100
            )

        assertEquals(
            15571,
            total
        )

        assertEquals(
            0,
            QuotaMath.totalRemaining(
                totalUnits = total,
                dailyLimit = 10000
            )
        )
    }

    @Test
    fun searchRemainingIsLimitedByTotalUnitBudget() {
        assertEquals(
            2,
            QuotaMath.searchRemaining(
                searchCalls = 80,
                searchDailyLimit = 100,
                totalRemaining = 250,
                searchListCost = 100
            )
        )

        assertEquals(
            20,
            QuotaMath.searchRemaining(
                searchCalls = 80,
                searchDailyLimit = 100,
                totalRemaining = 5000,
                searchListCost = 100
            )
        )
    }
}
