package com.saney.ytmimporter.model

enum class HistoryStatus {
    RUNNING,
    COMPLETED,
    PARTIAL,
    PENDING_QUOTA,
    FAILED
}

data class HistoryTrack(
    val index: Int,
    val originalTitle: String,
    val originalArtist: String,
    val videoId: String?,
    val selectedTitle: String?,
    val selectedChannel: String?,
    val status: String,
    val manuallySelected: Boolean,
    val error: String?
)

data class HistoryEntry(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val status: HistoryStatus,
    val sourceLabel: String,
    val playlistName: String,
    val playlistId: String?,
    val privacyStatus: String,
    val destination: PendingDestination,
    val googleEmail: String?,
    val youtubeChannelId: String?,
    val youtubeChannelTitle: String?,
    val totalImportedCount: Int,
    val writeTargetCount: Int,
    val addedCount: Int,
    val failedCount: Int,
    val pendingCount: Int,
    val skippedCount: Int,
    val missingCount: Int,
    val lastError: String?,
    val tracks: List<HistoryTrack>
)
