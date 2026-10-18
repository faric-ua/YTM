package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.library.GroupNumberPolicy

/** Permanent «група №N» numbers on the phone — see [GroupNumberPolicy]. */
class GroupNumberStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    @Synchronized
    fun load(): GroupNumberPolicy.Memory =
        GroupNumberPolicy.decode(prefs.getString(KEY, null))

    @Synchronized
    fun save(memory: GroupNumberPolicy.Memory) {
        prefs.edit().putString(KEY, GroupNumberPolicy.encode(memory)).apply()
    }

    private companion object {
        const val PREFS_NAME = "library_group_numbers"
        const val KEY = "numbers"
    }
}
