package com.saney.ytmimporter.auth

import com.saney.ytmimporter.model.GoogleAccountInfo
import com.saney.ytmimporter.model.YouTubeChannelInfo

/**
 * Process-memory-only Google/YTM authorization state.
 *
 * OAuth access token is deliberately NOT written to SharedPreferences,
 * backups, projects, workspace, files, or SavedInstanceState.
 * It only survives configuration recreation while the app process lives.
 */
object AuthSessionStore {
    data class Snapshot(
        val accessToken: String? = null,
        val googleAccountInfo: GoogleAccountInfo? = null,
        val youtubeChannelInfo: YouTubeChannelInfo? = null
    )

    @Volatile
    private var snapshot = Snapshot()

    fun current(): Snapshot = snapshot

    @Synchronized
    fun update(
        accessToken: String?,
        googleAccountInfo: GoogleAccountInfo?,
        youtubeChannelInfo: YouTubeChannelInfo?
    ) {
        snapshot = Snapshot(
            accessToken = accessToken,
            googleAccountInfo = googleAccountInfo,
            youtubeChannelInfo = youtubeChannelInfo
        )
    }

    @Synchronized
    fun clear() {
        snapshot = Snapshot()
    }
}
