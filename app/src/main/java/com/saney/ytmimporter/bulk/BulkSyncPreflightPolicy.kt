package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingOperation
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.search.SearchRecoveryPolicy
import com.saney.ytmimporter.storage.QuotaTracker
import java.io.Serializable

enum class BulkSyncPlanState {
    NEW,
    LINKED,
    ALREADY_SYNCED,
    NEEDS_SEARCH,
    PENDING,
    BLOCKED
}

data class BulkSyncLocalPlaylist(
    val localPlaylistId: String,
    val playlistName: String,
    val sourceLabel: String,
    val tracks: List<Track>,
    val destinationPlaylistId: String?,
    val estimatedSearchCalls: Int,
    val cacheHits: Int
)

data class BulkSyncRemoteSnapshot(
    val inventoryAvailable: Boolean,
    val ownedPlaylistIds: Set<String>,
    val orderedVideoIdsByPlaylistId: Map<String, List<String>>,
    val errorsByPlaylistId: Map<String, String> = emptyMap()
)

data class BulkSyncPlanRow(
    val localPlaylistId: String,
    val playlistName: String,
    val state: BulkSyncPlanState,
    val trackCount: Int,
    val selectedCount: Int,
    val unresolvedCount: Int,
    val estimatedSearchCalls: Int,
    val cacheHits: Int,
    val destinationPlaylistId: String?,
    val plannedCreate: Boolean,
    val plannedInsertCount: Int,
    val estimatedWriteUnits: Int,
    val reason: String
) : Serializable

data class BulkSyncPlanSummary(
    val rows: List<BulkSyncPlanRow>,
    val estimatedSearchCalls: Int,
    val estimatedWriteUnits: Int
) : Serializable {
    fun count(state: BulkSyncPlanState): Int =
        rows.count { it.state == state }
}

object BulkSyncPreflightPolicy {
    fun build(
        localPlaylists: List<BulkSyncLocalPlaylist>,
        pendingJobs: List<PendingJob>,
        connected: Boolean,
        remote: BulkSyncRemoteSnapshot
    ): BulkSyncPlanSummary {
        val rows =
            localPlaylists.map { local ->
                classify(
                    local = local,
                    pendingJobs = pendingJobs,
                    connected = connected,
                    remote = remote
                )
            }

        return BulkSyncPlanSummary(
            rows = rows,
            estimatedSearchCalls =
                rows
                    .filter {
                        it.state == BulkSyncPlanState.NEEDS_SEARCH
                    }
                    .sumOf { it.estimatedSearchCalls },
            estimatedWriteUnits =
                rows.sumOf { it.estimatedWriteUnits }
        )
    }

    private fun classify(
        local: BulkSyncLocalPlaylist,
        pendingJobs: List<PendingJob>,
        connected: Boolean,
        remote: BulkSyncRemoteSnapshot
    ): BulkSyncPlanRow {
        val activeTracks =
            local.tracks.filter {
                it.status != TrackStatus.SKIPPED
            }

        val selectedIds =
            activeTracks.mapNotNull {
                it.selectedVideoId
                    ?.takeIf(String::isNotBlank)
            }

        val unresolved =
            activeTracks.count(::requiresReviewOrSearch)

        val pendingOwner =
            pendingOwner(
                local = local,
                pendingJobs = pendingJobs
            )

        if (pendingOwner != null) {
            return row(
                local = local,
                state = BulkSyncPlanState.PENDING,
                selectedCount = selectedIds.size,
                unresolvedCount = unresolved,
                reason =
                    if (pendingOwner.operation == PendingOperation.SEARCH) {
                        "Існуюче SEARCH-завдання вже володіє цією лінією."
                    } else {
                        "Існуюче WRITE-завдання вже володіє цією лінією."
                    }
            )
        }

        if (local.localPlaylistId.isBlank() || activeTracks.isEmpty()) {
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = selectedIds.size,
                unresolvedCount = unresolved,
                reason =
                    "Недостатньо локальних даних для безпечної синхронізації."
            )
        }

        if (activeTracks.any { it.status == TrackStatus.PENDING }) {
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = selectedIds.size,
                unresolvedCount = unresolved,
                reason =
                    "Є PENDING-треки без однозначного власника в Черзі."
            )
        }

        if (unresolved > 0) {
            return row(
                local = local,
                state = BulkSyncPlanState.NEEDS_SEARCH,
                selectedCount = selectedIds.size,
                unresolvedCount = unresolved,
                estimatedSearchCalls = local.estimatedSearchCalls,
                cacheHits = local.cacheHits,
                reason =
                    "Спочатку потрібен Search або ручна перевірка."
            )
        }

        if (selectedIds.isEmpty()) {
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = 0,
                unresolvedCount = 0,
                reason =
                    "Немає вибраних videoId для безпечного запису."
            )
        }

        if (!connected) {
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                reason =
                    "Підключіть Google / YTM перед синхронізацією."
            )
        }

        val remoteId =
            local.destinationPlaylistId
                ?.takeIf { it.isNotBlank() }

        if (remoteId == null) {
            val inserts = selectedIds.size

            return row(
                local = local,
                state = BulkSyncPlanState.NEW,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                plannedCreate = true,
                plannedInsertCount = inserts,
                estimatedWriteUnits =
                    QuotaTracker.PLAYLIST_CREATE_COST +
                        inserts * QuotaTracker.PLAYLIST_ITEM_INSERT_COST,
                reason =
                    "Локальний плейлист без persisted YTM ID."
            )
        }

        if (!remote.inventoryAvailable) {
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                reason =
                    "Не вдалося підтвердити remote inventory для поточного акаунта."
            )
        }

        remote.errorsByPlaylistId[remoteId]?.let { message ->
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                reason = message
            )
        }

        if (remoteId !in remote.ownedPlaylistIds) {
            return row(
                local = local,
                state = BulkSyncPlanState.BLOCKED,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                reason =
                    "Persisted YTM ID не належить поточному акаунту/каналу або недоступний."
            )
        }

        val remoteIds =
            remote.orderedVideoIdsByPlaylistId[remoteId]
                ?: emptyList()

        val missing =
            missingOccurrences(
                localIds = selectedIds,
                remoteIds = remoteIds
            )

        return if (missing.isEmpty()) {
            row(
                local = local,
                state = BulkSyncPlanState.ALREADY_SYNCED,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                reason =
                    "Remote вже містить усі вибрані videoId occurrences."
            )
        } else {
            row(
                local = local,
                state = BulkSyncPlanState.LINKED,
                selectedCount = selectedIds.size,
                unresolvedCount = 0,
                plannedInsertCount = missing.size,
                estimatedWriteUnits =
                    missing.size * QuotaTracker.PLAYLIST_ITEM_INSERT_COST,
                reason =
                    "Persisted YTM ID підтверджено; потрібне лише add-only доповнення."
            )
        }
    }

    private fun pendingOwner(
        local: BulkSyncLocalPlaylist,
        pendingJobs: List<PendingJob>
    ): PendingJob? {
        pendingJobs.firstOrNull {
            !it.localPlaylistId.isNullOrBlank() &&
                it.localPlaylistId == local.localPlaylistId
        }?.let { return it }

        val recoveryKey =
            SearchRecoveryPolicy.workspaceKey(
                sourceLabel = local.sourceLabel,
                playlist =
                    ImportedPlaylist(
                        name = local.playlistName,
                        tracks = local.tracks.toMutableList()
                    )
            )

        pendingJobs.firstOrNull {
            it.operation == PendingOperation.SEARCH &&
                it.recoveryKey == recoveryKey
        }?.let { return it }

        val remoteId =
            local.destinationPlaylistId
                ?.takeIf { it.isNotBlank() }

        if (remoteId != null) {
            pendingJobs.firstOrNull {
                it.operation == PendingOperation.WRITE &&
                    it.playlistId == remoteId
            }?.let { return it }
        }

        return null
    }

    private fun requiresReviewOrSearch(track: Track): Boolean {
        if (track.status == TrackStatus.SKIPPED) {
            return false
        }

        if (
            track.status in
                setOf(
                    TrackStatus.NEW,
                    TrackStatus.SEARCHING,
                    TrackStatus.REVIEW,
                    TrackStatus.MISSING,
                    TrackStatus.WAITING_QUOTA,
                    TrackStatus.FAILED
                )
        ) {
            return true
        }

        return track.selectedVideoId.isNullOrBlank()
    }

    internal fun missingOccurrences(
        localIds: List<String>,
        remoteIds: List<String>
    ): List<String> {
        val available =
            remoteIds
                .groupingBy { it }
                .eachCount()
                .toMutableMap()

        val missing = mutableListOf<String>()

        localIds.forEach { videoId ->
            val count = available[videoId] ?: 0

            if (count > 0) {
                available[videoId] = count - 1
            } else {
                missing += videoId
            }
        }

        return missing
    }

    private fun row(
        local: BulkSyncLocalPlaylist,
        state: BulkSyncPlanState,
        selectedCount: Int,
        unresolvedCount: Int,
        estimatedSearchCalls: Int = 0,
        cacheHits: Int = 0,
        plannedCreate: Boolean = false,
        plannedInsertCount: Int = 0,
        estimatedWriteUnits: Int = 0,
        reason: String
    ): BulkSyncPlanRow =
        BulkSyncPlanRow(
            localPlaylistId = local.localPlaylistId,
            playlistName = local.playlistName,
            state = state,
            trackCount = local.tracks.size,
            selectedCount = selectedCount,
            unresolvedCount = unresolvedCount,
            estimatedSearchCalls = estimatedSearchCalls,
            cacheHits = cacheHits,
            destinationPlaylistId = local.destinationPlaylistId,
            plannedCreate = plannedCreate,
            plannedInsertCount = plannedInsertCount,
            estimatedWriteUnits = estimatedWriteUnits,
            reason = reason
        )
}
