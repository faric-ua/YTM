package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.library.LibrarySearchPolicy

/** Batch 18: the Library search history (last queries), on the phone only. */
class LibrarySearchHistoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    @Synchronized
    fun load(): List<String> = LibrarySearchPolicy.decodeHistory(prefs.getString(KEY, null))

    @Synchronized
    fun save(history: List<String>) {
        prefs.edit().putString(KEY, LibrarySearchPolicy.encodeHistory(history)).apply()
    }

    private companion object {
        const val PREFS_NAME = "library_search_history"
        const val KEY = "queries"
    }
}
