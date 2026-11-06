package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistDescriptionPolicy.State
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Batch 21: signature first, own description below, strip of descriptions, counter. */
class PlaylistDescriptionPolicyTest {
    private val seeded = PlaylistDescriptionPolicy.seeded(State(), now = 1L)

    @Test fun signatureIsAlwaysTheFirstLine() {
        val text = PlaylistDescriptionPolicy.forNewPlaylist(seeded)
        assertTrue(text.startsWith(PlaylistDescriptionPolicy.SIGNATURE + "\n\n"))
        assertEquals(PlaylistDescriptionPolicy.SIGNATURE,
            PlaylistDescriptionPolicy.forNewPlaylist(seeded.copy(enabled = false)))
        assertEquals(PlaylistDescriptionPolicy.SIGNATURE, PlaylistDescriptionPolicy.forNewPlaylist(State()))
    }

    @Test fun firstDescriptionIsOfferedOnce() {
        assertEquals(1, seeded.descriptions.size)
        assertEquals("d1", seeded.activeId)
        assertEquals(seeded, PlaylistDescriptionPolicy.seeded(seeded, now = 99L))
        assertFalse("no dead help-page link yet",
            PlaylistDescriptionPolicy.FIRST_TEXT.contains("github.io"))
    }

    @Test fun newDescriptionBecomesActiveAndOldOnesStay() {
        val two = PlaylistDescriptionPolicy.add(seeded, "  Мій опис <тест>  ", now = 2L)
        assertEquals(2, two.descriptions.size)
        assertEquals("d2", two.activeId)
        assertEquals("Мій опис ‹тест›", two.active?.text)
        val back = PlaylistDescriptionPolicy.activate(two, "d1")
        assertEquals("d1", back.activeId)
        assertEquals(2, back.descriptions.size)
        assertEquals(two, PlaylistDescriptionPolicy.add(two, "   ", now = 3L))
    }

    @Test fun editKeepsTheCounter() {
        val used = PlaylistDescriptionPolicy.markUsed(seeded, "d1")
        val edited = PlaylistDescriptionPolicy.edit(used, "d1", "Новий текст")
        assertEquals("Новий текст", edited.active?.text)
        assertEquals(1, edited.active?.uses)
    }

    @Test fun counterOnlyForTheDescriptionReallyUsed() {
        assertEquals("d1", PlaylistDescriptionPolicy.usedId(seeded))
        assertNull(PlaylistDescriptionPolicy.usedId(seeded.copy(enabled = false)))
        assertEquals(seeded, PlaylistDescriptionPolicy.markUsed(seeded, null))
        val twice = PlaylistDescriptionPolicy.markUsed(PlaylistDescriptionPolicy.markUsed(seeded, "d1"), "d1")
        assertEquals(2, twice.active?.uses)
    }

    @Test fun usesWords() {
        assertEquals("ще не використано", PlaylistDescriptionPolicy.usesLine(0))
        assertEquals("використано 1 раз", PlaylistDescriptionPolicy.usesLine(1))
        assertEquals("використано 11 разів", PlaylistDescriptionPolicy.usesLine(11))
        assertEquals("використано 3 рази", PlaylistDescriptionPolicy.usesLine(3))
        assertEquals("використано 12 разів", PlaylistDescriptionPolicy.usesLine(12))
        assertEquals("використано 22 рази", PlaylistDescriptionPolicy.usesLine(22))
    }

    @Test fun longDescriptionIsCutToTheYouTubeLimit() {
        val long = PlaylistDescriptionPolicy.add(seeded, "я".repeat(6000), now = 5L)
        assertEquals(PlaylistDescriptionPolicy.MAX_LENGTH, PlaylistDescriptionPolicy.forNewPlaylist(long).length)
    }

    @Test fun firstLineForTheFoldedView() {
        assertEquals("Рядок один", PlaylistDescriptionPolicy.firstLine("\n Рядок один \nдва"))
        assertTrue(PlaylistDescriptionPolicy.firstLine("а".repeat(200)).endsWith("…"))
    }
}
