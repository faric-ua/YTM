package com.saney.ytmimporter.storage

import kotlin.math.max
import kotlin.math.min

object QuotaMath {
    fun totalUnits(
        searchCalls: Int,
        nonSearchUnits: Int,
        searchListCost: Int
    ): Int =
        max(0, nonSearchUnits) +
            max(0, searchCalls) *
                max(0, searchListCost)

    fun totalRemaining(
        totalUnits: Int,
        dailyLimit: Int
    ): Int =
        max(
            0,
            dailyLimit - max(0, totalUnits)
        )

    fun searchRemaining(
        searchCalls: Int,
        searchDailyLimit: Int,
        totalRemaining: Int,
        searchListCost: Int
    ): Int {
        val byCalls =
            max(
                0,
                searchDailyLimit -
                    max(0, searchCalls)
            )

        val cost =
            max(1, searchListCost)

        val byUnits =
            max(0, totalRemaining) / cost

        return min(
            byCalls,
            byUnits
        )
    }
}
