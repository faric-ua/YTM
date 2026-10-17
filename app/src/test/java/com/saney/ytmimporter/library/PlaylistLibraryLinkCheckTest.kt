package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistLibraryIdentityPolicy.LinkedLocal
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistLibraryLinkCheckTest {
    private val linked = listOf(
        LinkedLocal("phone:gone", "PLgone"),
        LinkedLocal("phone:here", "PLhere"),
        LinkedLocal("phone:free", null),
        LinkedLocal("history:other", "PLother", channelId = "UCother"),
        LinkedLocal("history:mine", "PLmine", channelId = "UCme")
    )

    @Test fun marksOnlyLinksMissingFromACompleteListOfTheSameAccount() {
        assertEquals(setOf("phone:gone", "history:mine"),
            PlaylistLibraryIdentityPolicy.missingOnYouTube(
                linked, setOf("PLhere"), catalogueComplete = true, currentChannelId = "UCme"))
    }

    @Test fun partialListMarksNothing() {
        assertTrue(PlaylistLibraryIdentityPolicy.missingOnYouTube(
            linked, setOf("PLhere"), catalogueComplete = false, currentChannelId = "UCme").isEmpty())
    }

    @Test fun catalogueCompleteness() {
        assertFalse(PlaylistLibraryIdentityPolicy.catalogueComplete(null))
        assertTrue(PlaylistLibraryIdentityPolicy.catalogueComplete(emptyList()))
        val full = List(PlaylistLibraryIdentityPolicy.CATALOGUE_PAGE_LIMIT_ITEMS) {
            YouTubePlaylistInfo("PL$it", "t", "private", 1)
        }
        assertFalse("hit the 20-page cap: may be cut", PlaylistLibraryIdentityPolicy.catalogueComplete(full))
    }
}
