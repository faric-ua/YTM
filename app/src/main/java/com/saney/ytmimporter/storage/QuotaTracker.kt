package com.saney.ytmimporter.storage

import android.content.Context
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

data class QuotaSnapshot(
    val dayKey: String,
    val searchCalls: Int,
    val searchRemaining: Int,
    val generalUnits: Int,
    val generalRemaining: Int,
    val cacheHits: Int,
    val lastQuotaError: String?
)

class QuotaTracker(context: Context) {
    private val prefs =
        context.getSharedPreferences("quota_tracker_v1", Context.MODE_PRIVATE)

    fun snapshot(): QuotaSnapshot {
        ensureCurrentDay()

        val searchCalls = prefs.getInt(KEY_SEARCH_CALLS, 0)
        val generalUnits = prefs.getInt(KEY_GENERAL_UNITS, 0)

        return QuotaSnapshot(
            dayKey = prefs.getString(KEY_DAY, currentDayKey()).orEmpty(),
            searchCalls = searchCalls,
            searchRemaining = max(0, SEARCH_DAILY_LIMIT - searchCalls),
            generalUnits = generalUnits,
            generalRemaining = max(0, GENERAL_DAILY_LIMIT - generalUnits),
            cacheHits = prefs.getInt(KEY_CACHE_HITS, 0),
            lastQuotaError = prefs.getString(KEY_LAST_QUOTA_ERROR, null)
        )
    }

    fun recordSearchCall() {
        ensureCurrentDay()
        prefs.edit()
            .putInt(KEY_SEARCH_CALLS, prefs.getInt(KEY_SEARCH_CALLS, 0) + 1)
            .apply()
    }

    fun recordCacheHit() {
        ensureCurrentDay()
        prefs.edit()
            .putInt(KEY_CACHE_HITS, prefs.getInt(KEY_CACHE_HITS, 0) + 1)
            .apply()
    }

    fun recordGeneralUnits(units: Int) {
        if (units <= 0) return
        ensureCurrentDay()
        prefs.edit()
            .putInt(KEY_GENERAL_UNITS, prefs.getInt(KEY_GENERAL_UNITS, 0) + units)
            .apply()
    }

    fun recordQuotaError(message: String) {
        ensureCurrentDay()
        prefs.edit()
            .putString(KEY_LAST_QUOTA_ERROR, message.take(1000))
            .apply()
    }

    private fun ensureCurrentDay() {
        val current = currentDayKey()
        val stored = prefs.getString(KEY_DAY, null)

        if (stored == current) return

        prefs.edit()
            .putString(KEY_DAY, current)
            .putInt(KEY_SEARCH_CALLS, 0)
            .putInt(KEY_GENERAL_UNITS, 0)
            .putInt(KEY_CACHE_HITS, 0)
            .remove(KEY_LAST_QUOTA_ERROR)
            .apply()
    }

    private fun currentDayKey(): String =
        LocalDate.now(ZoneId.of("America/Los_Angeles")).toString()

    companion object {
        const val SEARCH_DAILY_LIMIT = 100
        const val GENERAL_DAILY_LIMIT = 10_000

        const val PLAYLIST_CREATE_COST = 50
        const val PLAYLIST_DELETE_COST = 50
        const val PLAYLIST_ITEM_INSERT_COST = 50
        const val SIMPLE_LIST_COST = 1

        private const val KEY_DAY = "day"
        private const val KEY_SEARCH_CALLS = "search_calls"
        private const val KEY_GENERAL_UNITS = "general_units"
        private const val KEY_CACHE_HITS = "cache_hits"
        private const val KEY_LAST_QUOTA_ERROR = "last_quota_error"
    }
}
