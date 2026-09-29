package com.saney.ytmimporter.bulk

enum class BulkSyncSessionState {
    PREVIEW,
    READY,
    RUNNING,
    PAUSED_SEARCH_QUOTA,
    PAUSED_WRITE_QUOTA,
    PAUSED_RATE_LIMIT,
    PAUSED_AUTH,
    PAUSED_INTERRUPTED,
    COMPLETED,
    ROLLING_BACK,
    ROLLBACK_PAUSED,
    ROLLED_BACK,
    PARTIAL_FAILED
}

enum class BulkSyncSessionRowState {
    READY,
    READY_APPEND,
    CREATING,
    INSERTING,
    COMPLETED,
    COMPLETED_NOOP,
    DEFERRED_LINKED,
    NEEDS_SEARCH,
    PENDING,
    BLOCKED,
    PARTIAL_FAILED,
    FAILED
}

enum class BulkSyncMutationType {
    CREATE_PLAYLIST,
    INSERT_PLAYLIST_ITEM
}

enum class BulkSyncMutationStatus {
    PREPARED,
    APPLIED,
    FAILED,
    TERMINAL_FAILED,
    ROLLED_BACK
}

data class BulkSyncSessionTrack(
    val trackIndex: Int,
    val historyIndex: Int?,
    val videoId: String,
    val originalTitle: String,
    val originalArtist: String
)

data class BulkSyncSessionRow(
    val localPlaylistId: String,
    val playlistName: String,
    val sourceLabel: String,
    val originalPlanState: BulkSyncPlanState,
    val state: BulkSyncSessionRowState,
    val privacyStatus: String = "private",
    val remotePlaylistId: String? = null,
    val tracks: List<BulkSyncSessionTrack> = emptyList(),
    val lastError: String? = null
)

data class BulkSyncBaselineItem(
    val playlistItemId: String?,
    val sourcePosition: Int?,
    val videoId: String?
)

data class BulkSyncBaselinePlaylist(
    val playlistId: String,
    val title: String,
    val privacyStatus: String,
    val items: List<BulkSyncBaselineItem>
)

data class BulkSyncRemoteBaseline(
    val capturedAt: Long,
    val googleEmail: String?,
    val youtubeChannelId: String?,
    val youtubeChannelTitle: String?,
    val playlists: List<BulkSyncBaselinePlaylist>
)

data class BulkSyncMutation(
    val operationId: String,
    val type: BulkSyncMutationType,
    val localPlaylistId: String,
    val remotePlaylistId: String?,
    val videoId: String?,
    val trackIndex: Int?,
    val createdPlaylistItemId: String? = null,
    val status: BulkSyncMutationStatus,
    val error: String? = null,
    val updatedAt: Long
)

data class BulkSyncSession(
    val sessionId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val state: BulkSyncSessionState,
    val googleEmail: String?,
    val youtubeChannelId: String?,
    val youtubeChannelTitle: String?,
    val checkpointId: String,
    val remoteBaseline: BulkSyncRemoteBaseline,
    val plan: List<BulkSyncSessionRow>,
    val currentPlanIndex: Int,
    val mutationLedger: List<BulkSyncMutation>,
    val lastError: String? = null
) {
    val isTerminal: Boolean
        get() {
            if (
                state in
                setOf(
                    BulkSyncSessionState.COMPLETED,
                    BulkSyncSessionState.ROLLED_BACK
                )
            ) {
                return true
            }

            if (
                state !=
                BulkSyncSessionState.PARTIAL_FAILED
            ) {
                return false
            }

            val hasUncertainPrepared =
                mutationLedger.any {
                    it.status ==
                        BulkSyncMutationStatus.PREPARED
                }

            val hasRecoverableLegacyInsertFailure =
                mutationLedger.any {
                    it.type ==
                        BulkSyncMutationType
                            .INSERT_PLAYLIST_ITEM &&
                    it.status ==
                        BulkSyncMutationStatus.FAILED
                }

            return !hasUncertainPrepared &&
                !hasRecoverableLegacyInsertFailure
        }
}
