package com.saney.ytmimporter.library

import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot

/**
 * Unified local catalogue. Never silently copies media or restores History:
 * older History-only entries are visible, clearly labeled as archive, and
 * require an explicit user tap before anything becomes the current playlist.
 */
enum class LocalLibrarySource { SAVED, CURRENT, HISTORY }

data class LocalLibraryItem(
    val identity: String,
    val localPlaylistId: String?,
    val historyEntryId: String?,
    val title: String,
    val sourceLabel: String,
    val trackCount: Int,
    val sampleVideoId: String?,
    val destinationPlaylistId: String?,
    val destinationTitle: String?,
    val updatedAt: Long,
    val source: LocalLibrarySource,
    val isCurrent: Boolean
)

object PlaylistLibraryPolicy {
    // Only official 11-character video IDs may request YouTube artwork.
    // Skip invalid/missing IDs rather than making a failed thumbnail request
    // and leaving an archive with an unintended generic placeholder.
    private val validVideoId = Regex("^[A-Za-z0-9_-]{11}$")

    fun build(
        saved: List<RestorablePlaylistSnapshot>,
        current: CurrentPlaylistSnapshot?,
        history: List<HistoryEntry>
    ): List<LocalLibraryItem> {
        val result = mutableListOf<LocalLibraryItem>()
        val savedIds = mutableSetOf<String>()

        saved.forEach { snapshot ->
            if (!savedIds.add(snapshot.localPlaylistId)) return@forEach
            val active = current?.takeIf {
                it.localPlaylistId == snapshot.localPlaylistId
            }
            val playing = active != null
            val playlist = active?.playlist ?: snapshot.playlist
            result += LocalLibraryItem(
                identity = "saved:" + snapshot.localPlaylistId,
                localPlaylistId = snapshot.localPlaylistId,
                historyEntryId = snapshot.sourceHistoryId,
                title = playlist.name,
                sourceLabel = active?.sourceLabel ?: snapshot.sourceLabel,
                trackCount = playlist.tracks.size,
                sampleVideoId = playlist.tracks.firstNotNullOfOrNull {
                    it.selectedVideoId?.takeIf(validVideoId::matches)
                },
                destinationPlaylistId =
                    active?.destinationPlaylistId ?: snapshot.destinationPlaylistId,
                destinationTitle =
                    active?.destinationPlaylistTitle ?: snapshot.destinationPlaylistTitle,
                updatedAt = active?.updatedAt ?: snapshot.updatedAt,
                source = if (playing) LocalLibrarySource.CURRENT else LocalLibrarySource.SAVED,
                isCurrent = playing
            )
        }

        if (current != null && current.localPlaylistId !in savedIds) {
            result += LocalLibraryItem(
                identity = "saved:" + current.localPlaylistId,
                localPlaylistId = current.localPlaylistId,
                historyEntryId = current.sourceHistoryId,
                title = current.playlist.name,
                sourceLabel = current.sourceLabel,
                trackCount = current.playlist.tracks.size,
                sampleVideoId = current.playlist.tracks.firstNotNullOfOrNull {
                    it.selectedVideoId?.takeIf(validVideoId::matches)
                },
                destinationPlaylistId = current.destinationPlaylistId,
                destinationTitle = current.destinationPlaylistTitle,
                updatedAt = current.updatedAt,
                source = LocalLibrarySource.CURRENT,
                isCurrent = true
            )
            savedIds += current.localPlaylistId
        }

        // Match only durable LOCAL IDs. Orphan History rows are independent
        // snapshots, even if their titles, sources or YouTube IDs coincide.
        // Align with HistoryLogicalGroupPolicy's operation-scoped fallback.
        val historicalKeys = mutableSetOf<String>()
        history.sortedByDescending { it.updatedAt }.forEach { entry ->
            if (entry.tracks.isEmpty()) return@forEach
            val id = entry.localPlaylistId?.takeIf(String::isNotBlank)
            if (id != null && id in savedIds) return@forEach
            val key = if (id != null) "local:$id" else "operation:${entry.id}"
            if (!historicalKeys.add(key)) return@forEach
            result += LocalLibraryItem(
                identity = "history:" + entry.id,
                localPlaylistId = id,
                historyEntryId = entry.id,
                title = entry.playlistName,
                sourceLabel = entry.sourceLabel,
                trackCount = entry.tracks.size,
                sampleVideoId = entry.tracks.firstNotNullOfOrNull {
                    it.videoId?.takeIf(validVideoId::matches)
                },
                destinationPlaylistId = entry.playlistId,
                destinationTitle = entry.playlistName.takeIf { entry.playlistId != null },
                updatedAt = entry.updatedAt,
                source = LocalLibrarySource.HISTORY,
                isCurrent = false
            )
        }

        return result.sortedWith(
            compareByDescending<LocalLibraryItem> { it.isCurrent }
                .thenByDescending { it.updatedAt }
                .thenBy { it.title.lowercase() }
        )
    }
}
