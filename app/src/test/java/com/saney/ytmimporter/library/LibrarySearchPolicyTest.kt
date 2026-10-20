package com.saney.ytmimporter.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySearchPolicyTest {
    @Test
    fun emptyOrSpacesShowsEverything() {
        assertTrue(LibrarySearchPolicy.matches("What Evil Lurks", "4 треків", ""))
        assertTrue(LibrarySearchPolicy.matches("What Evil Lurks", "4 треків", "   "))
    }

    @Test
    fun trailingSpaceFromVoiceStillFinds() {
        assertTrue(LibrarySearchPolicy.matches("The Prodigy — What Evil Lurks", "", "Evil "))
        assertTrue(LibrarySearchPolicy.matches("The Prodigy — What Evil Lurks", "", " evil"))
    }

    @Test
    fun ignoresCaseIncludingCyrillic() {
        assertTrue(LibrarySearchPolicy.matches("WHAT EVIL LURKS", "", "evil"))
        assertTrue(LibrarySearchPolicy.matches("Мій плейліст", "", "МІЙ"))
    }

    @Test
    fun searchesSubtitleToo() {
        assertTrue(LibrarySearchPolicy.matches("Baby's Got A Temper", "3 треків • Архів History", "архів"))
        assertFalse(LibrarySearchPolicy.matches("Baby's Got A Temper", "3 треків • Поточний", "архів"))
    }

    @Test
    fun spaceInsideQueryIsKept() {
        assertTrue(LibrarySearchPolicy.matches("What Evil Lurks", "", "evil lu"))
        assertFalse(LibrarySearchPolicy.matches("EvilLurks", "", "evil lu"))
    }

    @Test
    fun reuseOnlyForSearchWithSameYouTubeSide() {
        assertTrue(LibrarySearchPolicy.canReuse(true, true, true, true))
        assertFalse(LibrarySearchPolicy.canReuse(false, true, true, true))
        assertFalse(LibrarySearchPolicy.canReuse(true, false, true, true))
        assertFalse(LibrarySearchPolicy.canReuse(true, true, false, true))
        assertFalse(LibrarySearchPolicy.canReuse(true, true, true, false))
    }

    @Test
    fun coverCacheIsEighthOfHeapWithinBounds() {
        val mb = 1024L * 1024L
        assertEquals((32 * mb).toInt(), LibrarySearchPolicy.coverCacheBytes(256 * mb))
        assertEquals((8 * mb).toInt(), LibrarySearchPolicy.coverCacheBytes(16 * mb))
        assertEquals((48 * mb).toInt(), LibrarySearchPolicy.coverCacheBytes(1024 * mb))
        // ≈ 140 YouTube covers (320×180×4 bytes) on a 256 MB heap.
        assertTrue(LibrarySearchPolicy.coverCacheBytes(256 * mb) / (320 * 180 * 4) >= 120)
    }

    /** Batch 13: the header hides only while typing a search in the catalogue. */
    @Test fun headerHidesOnlyWhileTypingASearch() {
        assertTrue(LibrarySearchPolicy.searchTyping(true, true, true))
        assertFalse(LibrarySearchPolicy.searchTyping(false, true, true))
        assertFalse(LibrarySearchPolicy.searchTyping(true, false, true))
        assertFalse(LibrarySearchPolicy.searchTyping(true, true, catalogue = false))
        // 2000 px window: a 130 px navigation bar is not a keyboard, 900 px is.
        assertFalse(LibrarySearchPolicy.keyboardOpenByFrame(2000, 1870))
        assertTrue(LibrarySearchPolicy.keyboardOpenByFrame(2000, 1100))
        assertFalse(LibrarySearchPolicy.keyboardOpenByFrame(0, 0))
    }
}
