package com.saney.ytmimporter.library

import com.saney.ytmimporter.model.YouTubePlaylistInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistLibraryIdentityPolicyTest {
    @Test fun exactIdLinksDifferentNamesButDoesNotMergeCards() {
        val l = listOf(local("a", "Local", "PLsame"))
        assertEquals(listOf("a"), PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            l, listOf(remote("PLsame", "Renamed")))["PLsame"])
        assertEquals(1, l.size)
    }
    @Test fun sameNameDifferentIdsCannotLink() {
        assertTrue(PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            listOf(local("a", "db ost", "PLfirst")),
            listOf(remote("PLother", "db ost"))).isEmpty())
    }
    @Test fun sameRemoteIdRetainsDistinctLocalEditions() {
        val l = listOf(local("a", "Album", "PLsame"),
            local("b", "Album: Expanded", "PLsame"))
        assertEquals(listOf("a", "b"), PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            l, listOf(remote("PLsame", "Album")))["PLsame"])
    }
    @Test fun missingCurrentAccountListNeverInfersCrossAccountLinks() {
        val l = listOf(local("a", "Album", "PLprevious"))
        assertTrue(PlaylistLibraryIdentityPolicy.exactRemoteMatches(l, emptyList()).isEmpty())
        assertTrue(PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            l, listOf(remote("PLcurrent", "Album"))).isEmpty())
    }
    @Test fun blankAndNearIdsNeverMatch() {
        assertTrue(PlaylistLibraryIdentityPolicy.exactRemoteMatches(
            listOf(local("a", "Title", " PLid"), local("b", "Title", " ")),
            listOf(remote("PLid", "Title"), remote(" ", "Title"))).isEmpty())
    }
    private fun local(key: String, title: String, remoteId: String?) =
        LocalLibraryItem(key, null, null, title, "File", 1, null,
            remoteId, null, 1L, LocalLibrarySource.HISTORY, false)
    private fun remote(id: String, title: String) =
        YouTubePlaylistInfo(id, title, "private", 1)
}
