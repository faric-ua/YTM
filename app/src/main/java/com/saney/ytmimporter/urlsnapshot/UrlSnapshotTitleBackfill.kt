package com.saney.ytmimporter.urlsnapshot

import android.content.Context
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.HistoryStore

data class UrlSnapshotTitleBackfillResult(
    val currentPlaylistUpdated: Boolean,
    val historyEntriesUpdated: Int
)

class UrlSnapshotTitleBackfill(
    context: Context
) {
    private val currentPlaylistStore =
        CurrentPlaylistStore(context.applicationContext)

    private val historyStore =
        HistoryStore(context.applicationContext)

    fun apply(
        playlistId: String,
        playlistTitle: String
    ): UrlSnapshotTitleBackfillResult {
        val normalizedTitle =
            playlistTitle.trim().takeIf { it.isNotBlank() }
                ?: return UrlSnapshotTitleBackfillResult(false, 0)

        val sourcePrefix =
            "URL snapshot ($playlistId)"

        var currentUpdated = false

        currentPlaylistStore.load()?.let { snapshot ->
            if (
                snapshot.sourceLabel.startsWith(sourcePrefix) &&
                isTechnicalFallbackName(snapshot.playlist.name, playlistId)
            ) {
                snapshot.playlist.name = normalizedTitle
                currentPlaylistStore.save(
                    playlist = snapshot.playlist,
                    sourceLabel = snapshot.sourceLabel,
                    destinationPlaylistId = snapshot.destinationPlaylistId
                )
                currentUpdated = true
            }
        }

        var historyUpdated = 0

        historyStore.getAll()
            .filter { entry ->
                entry.sourceLabel.startsWith(sourcePrefix) &&
                    isTechnicalFallbackName(entry.playlistName, playlistId)
            }
            .forEach { entry ->
                historyStore.upsert(
                    entry.copy(
                        playlistName = normalizedTitle
                    )
                )
                historyUpdated += 1
            }

        return UrlSnapshotTitleBackfillResult(
            currentPlaylistUpdated = currentUpdated,
            historyEntriesUpdated = historyUpdated
        )
    }

    private fun isTechnicalFallbackName(
        name: String,
        playlistId: String
    ): Boolean {
        val shortId = playlistId.take(18)
        return name == "URL snapshot" ||
            name == "URL snapshot • $shortId"
    }
}
