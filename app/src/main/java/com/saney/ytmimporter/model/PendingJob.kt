package com.saney.ytmimporter.model

enum class PendingDestination {
    NEW_PLAYLIST,
    EXISTING_PLAYLIST
}

enum class PendingOperation {
    WRITE,
    SEARCH
}

data class PendingTrack(
    val originalTitle: String,
    val originalArtist: String,
    val videoId: String,
    val selectedTitle: String?,
    val selectedChannel: String?,
    val historyIndex: Int = -1
)

data class PendingSearchCandidate(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val score: Double
)

data class PendingSearchTrack(
    val originalTitle: String,
    val originalArtist: String,
    val selectedVideoId: String?,
    val selectedTitle: String?,
    val selectedChannel: String?,
    val status: String,
    val manuallySelected: Boolean,
    val error: String?,
    val historyIndex: Int?,
    val candidates: List<PendingSearchCandidate>
)

data class PendingSearchSnapshot(
    val playlistName: String,
    val tracks: List<PendingSearchTrack>
)

data class PendingJob(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val sourceLabel: String,
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
    val lastError: String?,
    val operation: PendingOperation = PendingOperation.WRITE,
    val recoveryKey: String? = null,
    val preserveExistingExact: Boolean = false,
    val searchSnapshot: PendingSearchSnapshot? = null
)
