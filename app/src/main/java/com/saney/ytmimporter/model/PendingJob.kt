package com.saney.ytmimporter.model

enum class PendingDestination {
    NEW_PLAYLIST,
    EXISTING_PLAYLIST
}

data class PendingTrack(
    val originalTitle: String,
    val originalArtist: String,
    val videoId: String,
    val selectedTitle: String?,
    val selectedChannel: String?
)

data class PendingJob(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val playlistName: String,
    val playlistId: String?,
    val privacyStatus: String,
    val destination: PendingDestination,
    val googleEmail: String?,
    val youtubeChannelId: String?,
    val youtubeChannelTitle: String?,
    val totalCount: Int,
    val addedCount: Int,
    val failedCount: Int,
    val remainingTracks: List<PendingTrack>,
    val lastError: String?
)
