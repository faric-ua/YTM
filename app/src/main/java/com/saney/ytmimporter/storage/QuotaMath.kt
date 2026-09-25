package com.saney.ytmimporter.storage

import kotlin.math.max

object QuotaMath {
    fun generalUnits(
        nonSearchUnits: Int
    ): Int =
        max(
            0,
            nonSearchUnits
        )

    fun generalRemaining(
        generalUnits: Int,
        dailyLimit: Int
    ): Int =
        max(
            0,
            dailyLimit -
                max(
                    0,
                    generalUnits
                )
        )

    fun searchRemaining(
        searchCalls: Int,
        searchDailyLimit: Int
    ): Int =
        max(
            0,
            searchDailyLimit -
                max(
                    0,
                    searchCalls
                )
        )
}
