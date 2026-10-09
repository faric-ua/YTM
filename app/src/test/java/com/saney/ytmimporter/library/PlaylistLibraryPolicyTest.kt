package com.saney.ytmimporter.library

import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.storage.CurrentPlaylistSnapshot
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistLibraryPolicyTest {
    @Test fun currentAndSavedAreCombinedWithoutDuplicates() {
        val list = PlaylistLibraryPolicy.build(
            saved = listOf(saved("one", "Old", 10), saved("two", "Second", 11)),
            current = current("one", "Edited", 20),
            history = listOf(history("h1", "one", "Old", 30))
        )
        assertEquals(2, list.size)
        assertEquals("Edited", list.first().title)
        assertEquals(LocalLibrarySource.CURRENT, list.first().source)
        assertTrue(list.first().isCurrent)
        assertEquals("two", list.last().localPlaylistId)
    }

    @Test fun historyOnlyPlaylistsAppearButDontImpersonateSavedSnapshots() {
        val list = PlaylistLibraryPolicy.build(
            saved = emptyList(),
            current = null,
            history = listOf(
                history("h1", null, "Album", 11),
                history("h2", null, "Album", 20),
                history("h3", null, "Other", 8)
            )
        )
        assertEquals(2, list.size)
        assertEquals("history:h2", list[0].identity)
        assertEquals(LocalLibrarySource.HISTORY, list[0].source)
        assertFalse(list[0].isCurrent)
    }

    @Test fun historicalRemoteAndPersistedLocalKeepLinkageButNotDuplicated() {
        val item = PlaylistLibraryPolicy.build(
            saved = listOf(saved("L", "Local", 10, remoteId = "PL123")),
            current = null,
            history = listOf(history("h1", "L", "Local", 30, "PL123"))
        )
        assertEquals(1, item.size)
        assertEquals("PL123", item.single().destinationPlaylistId)
        assertEquals("abcdefghijk", item.single().sampleVideoId)
    }

    private fun playlist(name: String) = ImportedPlaylist(
        name, mutableListOf(
            Track("Track", "Artist", selectedVideoId = "abcdefghijk")
        )
    )

    private fun saved(id: String, name: String, now: Long, remoteId: String? = null) =
        RestorablePlaylistSnapshot(
            localPlaylistId = id,
            sourceHistoryId = null,
            playlist = playlist(name),
            sourceLabel = "File",
            createdAt = now,
            updatedAt = now,
            destinationPlaylistId = remoteId,
            destinationPlaylistTitle = null
        )

    private fun current(id: String, name: String, now: Long) =
        CurrentPlaylistSnapshot(
            playlist = playlist(name),
            sourceLabel = "File",
            updatedAt = now,
            localPlaylistId = id
        )

    private fun history(
        id: String, localId: String?, title: String,
        time: Long, remoteId: String? = null
    ) = HistoryEntry(
        id = id, createdAt = time, updatedAt = time,
        status = HistoryStatus.COMPLETED, sourceLabel = "File",
        playlistName = title, playlistId = remoteId, privacyStatus = "private",
        destination = PendingDestination.NEW_PLAYLIST,
        googleEmail = null, youtubeChannelId = null,
        youtubeChannelTitle = null,
        totalImportedCount = 1, writeTargetCount = 0, addedCount = 0,
        failedCount = 0, pendingCount = 0, skippedCount = 0,
        duplicateCount = 0, missingCount = 0, lastError = null,
        tracks = listOf(
            HistoryTrack(0, "Track", "Artist", "abcdefghijk",
                null, null, "MATCHED", false, null)
        ),
        localPlaylistId = localId
    )
}
