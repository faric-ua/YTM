package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.RemotePlaylistListPolicy.Saved
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Batch 11 (faric 2026-10-20 «план Б»): the YouTube list kept on the phone. */
class RemotePlaylistListPolicyTest {
    private val list = listOf(
        YouTubePlaylistInfo("PLa", "mylist", "unlisted", 866,
            "https://i.ytimg.com/vi/x/hq.jpg", 1_758_000_000_000),
        YouTubePlaylistInfo("PLb", "Назва\tз табом\nі рядком % & =", "private", 0, null, 0)
    )

    @Test fun keptListComesBackTheSame() {
        val saved = Saved("UCchannel", 1_760_000_000_000, list)
        assertEquals(saved, RemotePlaylistListPolicy.decode(RemotePlaylistListPolicy.encode(saved)))
        val empty = Saved("me@example.com", 5, emptyList())
        assertEquals(empty, RemotePlaylistListPolicy.decode(RemotePlaylistListPolicy.encode(empty)))
    }

    @Test fun damagedTextNeverThrows() {
        assertNull(RemotePlaylistListPolicy.decode(null))
        assertNull(RemotePlaylistListPolicy.decode(""))
        assertNull(RemotePlaylistListPolicy.decode("v0\tx\t1"))
        assertNull(RemotePlaylistListPolicy.decode("v1\t\t1"))
        val text = RemotePlaylistListPolicy.encode(Saved("o", 1, list)) + "\nбитий рядок\n%ZZ\ta\tb\t1\t\t0"
        assertEquals(list, RemotePlaylistListPolicy.decode(text)!!.playlists)
    }

    @Test fun shownOnlyForTheSameAccountAndAfterSignIn() {
        val saved = Saved("UCme", 1, list)
        // Google still being restored: the last account's list is shown.
        assertTrue(RemotePlaylistListPolicy.usable(saved, true, hasToken = false, currentOwner = null))
        assertTrue(RemotePlaylistListPolicy.usable(saved, true, hasToken = true, currentOwner = "UCme"))
        assertFalse(RemotePlaylistListPolicy.usable(saved, true, hasToken = true, currentOwner = "UCother"))
        assertFalse(RemotePlaylistListPolicy.usable(saved, true, hasToken = true, currentOwner = ""))
        assertFalse(RemotePlaylistListPolicy.usable(saved, hadAuthorization = false, hasToken = false, currentOwner = null))
        assertFalse(RemotePlaylistListPolicy.usable(null, true, false, null))
    }

    @Test fun keptListIsReadAgainOnceConnected() {
        assertTrue(RemotePlaylistListPolicy.needsRefresh(true, hasList = true, fromMemory = true, loading = false))
        assertTrue(RemotePlaylistListPolicy.needsRefresh(true, hasList = false, fromMemory = false, loading = false))
        assertFalse(RemotePlaylistListPolicy.needsRefresh(true, hasList = true, fromMemory = false, loading = false))
        assertFalse(RemotePlaylistListPolicy.needsRefresh(true, true, true, loading = true))
        assertFalse(RemotePlaylistListPolicy.needsRefresh(false, false, false, false))
    }

    @Test fun deletedPlaylistLeavesTheKeptList() {
        val saved = Saved("o", 1, list)
        assertEquals(listOf("PLb"), RemotePlaylistListPolicy.without(saved, "PLa").playlists.map { it.id })
        assertEquals(saved, RemotePlaylistListPolicy.without(saved, "nope"))
    }

    @Test fun windowWaitsOnlyWithoutAnyList() {
        assertTrue(RemotePlaylistListPolicy.windowMustWait(true, hasList = false, stillComing = true))
        assertFalse(RemotePlaylistListPolicy.windowMustWait(true, hasList = true, stillComing = true))
        assertFalse(RemotePlaylistListPolicy.windowMustWait(true, false, stillComing = false))
        assertFalse(RemotePlaylistListPolicy.windowMustWait(false, false, true))
    }
}
