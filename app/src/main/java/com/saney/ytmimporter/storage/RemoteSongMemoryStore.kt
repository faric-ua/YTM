package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.library.RemoteSongMemoryPolicy

/**
 * App-private memory of checked YouTube playlist songs, one block per account
 * (YouTube channel id, or Google e-mail when the channel is unknown).
 * Only video IDs, song count and date — see [RemoteSongMemoryPolicy].
 * Never holds a token or song titles.
 */
class RemoteSongMemoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    @Synchronized
    fun load(owner: String): Map<String, RemoteSongMemoryPolicy.Entry> {
        if (owner.isBlank()) return emptyMap()
        return RemoteSongMemoryPolicy.decode(prefs.getString(key(owner), null))
    }

    @Synchronized
    fun remember(
        owner: String,
        playlistId: String,
        itemCount: Long,
        videoIds: List<String?>,
        now: Long = System.currentTimeMillis()
    ): Map<String, RemoteSongMemoryPolicy.Entry> {
        if (owner.isBlank()) return emptyMap()
        val updated = RemoteSongMemoryPolicy.remember(
            load(owner), playlistId, itemCount, videoIds, now
        )
        prefs.edit().putString(key(owner), RemoteSongMemoryPolicy.encode(updated)).apply()
        return updated
    }

    @Synchronized
    fun forget(owner: String, playlistId: String) {
        if (owner.isBlank()) return
        val current = load(owner)
        if (playlistId !in current) return
        prefs.edit().putString(
            key(owner), RemoteSongMemoryPolicy.encode(current - playlistId)
        ).apply()
    }

    private fun key(owner: String) = "owner:$owner"

    private companion object {
        const val PREFS_NAME = "library_remote_song_memory"
    }
}
