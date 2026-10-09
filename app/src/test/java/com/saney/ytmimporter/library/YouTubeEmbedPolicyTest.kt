package com.saney.ytmimporter.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeEmbedPolicyTest {
    @Test fun embedRequiresCanonicalVideoId() {
        assertTrue(YouTubeEmbedPolicy.isValidVideoId("abcdefghijk"))
        assertFalse(YouTubeEmbedPolicy.isValidVideoId(""))
        assertFalse(YouTubeEmbedPolicy.isValidVideoId("https://youtube.com/watch?v=abcdefghijk"))
        assertFalse(YouTubeEmbedPolicy.isValidVideoId("bad?x=12345"))
        assertEquals(
            "https://www.youtube.com/embed/abcdefghijk?playsinline=1&autoplay=0&controls=1",
            YouTubeEmbedPolicy.embedUrl("abcdefghijk")
        )
    }

    @Test fun embedKeepsControlsAndDisablesAutoplay() {
        val url = YouTubeEmbedPolicy.embedUrl("abcdefghijk")
        assertTrue(url.contains("controls=1"))
        assertTrue(url.contains("autoplay=0"))
        assertEquals("https://com.saney.ytmimporter/",
            YouTubeEmbedPolicy.referer("com.saney.ytmimporter"))
    }

    @Test fun watchFallbackUsesTrustedYoutube() {
        assertEquals(
            "https://www.youtube.com/watch?v=abcdefghijk",
            YouTubeEmbedPolicy.watchUrl("abcdefghijk")
        )
    }
}
