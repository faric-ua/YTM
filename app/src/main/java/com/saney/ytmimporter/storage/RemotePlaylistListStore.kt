package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.library.RemotePlaylistListPolicy
import com.saney.ytmimporter.model.YouTubePlaylistInfo

/**
 * Library batch 11 (faric 2026-10-20 «план Б»): app-private copy of the last
 * account's YouTube playlist list, so the Library shows it at once after
 * Android closed the app. Never a token or songs — see [RemotePlaylistListPolicy].
 */
class RemotePlaylistListStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    @Synchronized
    fun load(): RemotePlaylistListPolicy.Saved? =
        RemotePlaylistListPolicy.decode(prefs.getString(KEY, null))

    @Synchronized
    fun save(owner: String, playlists: List<YouTubePlaylistInfo>, now: Long = System.currentTimeMillis()) {
        if (owner.isBlank()) return
        prefs.edit().putString(KEY, RemotePlaylistListPolicy.encode(
            RemotePlaylistListPolicy.Saved(owner, now, playlists))).apply()
    }

    @Synchronized
    fun forget(playlistId: String) {
        val saved = load() ?: return
        if (saved.playlists.none { it.id == playlistId }) return
        prefs.edit().putString(KEY, RemotePlaylistListPolicy.encode(
            RemotePlaylistListPolicy.without(saved, playlistId))).apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private companion object {
        const val PREFS_NAME = "library_remote_playlist_list"
        const val KEY = "last_account"
    }
}
