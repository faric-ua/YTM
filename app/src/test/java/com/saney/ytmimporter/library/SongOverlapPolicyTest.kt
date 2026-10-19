package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistLibraryDuplicateGroupPolicy.Song
import com.saney.ytmimporter.library.SongOverlapPolicy.Entry
import com.saney.ytmimporter.library.SongOverlapPolicy.Level
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongOverlapPolicyTest {
    private fun vid(n: Int) = "vid" + n.toString().padStart(8, '0')
    private fun videos(vararg n: Int) = n.map { Song(vid(it), "Song $it") }
    private fun names(vararg t: String) = t.map { Song(null, it) }

    @Test
    fun renamedCopyWithSameVideosIsFound() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "Trance Vol.15", videos(1, 2, 3, 4)),
            Entry("youtube:b", "Моя збірка", videos(4, 3, 2, 1))
        ))
        assertEquals(1, r.matches.size)
        assertEquals(Level.SAME_VIDEOS, r.matches[0].level)
        assertEquals(4, r.matches[0].common)
    }

    @Test
    fun sameNamesWithoutVideosIsNotCalledSameVideos() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("history:a", "A", names("Prodigy — Firestarter", "Prodigy — Breathe", "Prodigy — Poison")),
            Entry("phone:b", "B", names("prodigy firestarter", "PRODIGY - Breathe!", "Prodigy  Poison"))
        ))
        assertEquals(Level.SAME_SONGS, r.matches.single().level)
    }

    @Test
    fun partialOverlap18of19() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "A", videos(*(1..19).toList().toIntArray())),
            Entry("youtube:b", "B", videos(*((1..18) + (100..106)).toList().toIntArray()))
        ))
        val m = r.matches.single()
        assertEquals(Level.PARTIAL, m.level)
        assertEquals(18, m.common)
        assertTrue(SongOverlapPolicy.verdict(m).contains("18"))
    }

    @Test
    fun fewCommonSongsAreIgnored() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "A", videos(1, 2, 3, 4, 5, 6, 7, 8)),
            Entry("phone:b", "B", videos(1, 2, 20, 21, 22, 23, 24, 25))
        ))
        assertTrue(r.matches.isEmpty())
    }

    @Test
    fun lessThanHalfOfSmallerIsIgnored() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "A", videos(*(1..10).toList().toIntArray())),
            Entry("phone:b", "B", videos(1, 2, 3, 4, 30, 31, 32, 33, 34, 35))
        ))
        assertTrue(r.matches.isEmpty())
    }

    @Test
    fun repeatsCountOnce() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "A", videos(1, 1, 1, 2, 3)),
            Entry("phone:b", "B", videos(1, 2, 3, 4, 5))
        ))
        assertEquals(3, r.matches.single().common)
    }

    @Test
    fun pairsInsideOneGroupAreNotRepeated() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "Same", videos(1, 2, 3), groupNumber = 30),
            Entry("youtube:b", "Same", videos(1, 2, 3), groupNumber = 30)
        ))
        assertTrue(r.matches.isEmpty())
    }

    @Test
    fun groupCopiesCollapseIntoOneLine() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "What Evil Lurks", videos(1, 2, 3, 4), groupNumber = 30),
            Entry("history:a2", "What Evil Lurks", videos(1, 2, 3, 4), groupNumber = 30),
            Entry("youtube:a3", "What Evil Lurks", videos(1, 2, 3, 4), groupNumber = 30),
            Entry("youtube:x", "Rave 1991", videos(1, 2, 3, 4, 9))
        ))
        val m = r.matches.single()
        assertEquals(30, listOf(m.first.groupNumber, m.second.groupNumber).filterNotNull().single())
        assertEquals(4, m.common)
    }

    @Test
    fun unknownSongsAreListedNotCompared() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("phone:a", "A", videos(1, 2, 3)),
            Entry("youtube:b", "B", null)
        ))
        assertEquals(listOf("youtube:b"), r.unknown)
        assertEquals(1, r.compared)
        assertTrue(r.matches.isEmpty())
    }

    @Test
    fun songKeyPrefersVideoAndSkipsBlank() {
        assertEquals("v:${vid(1)}", SongOverlapPolicy.songKey(Song(vid(1), "x")))
        assertEquals("t:beyonce halo remix", SongOverlapPolicy.songKey(Song("bad", "Beyoncé — Halo (Remix)")))
        assertNull(SongOverlapPolicy.songKey(Song(null, " — ")))
    }

    @Test
    fun strongestFirst() {
        val r = SongOverlapPolicy.find(listOf(
            Entry("p:1", "A", videos(1, 2, 3, 4, 5, 6)),
            Entry("p:2", "B", videos(1, 2, 3, 4, 50, 51)),
            Entry("p:3", "C", videos(10, 11, 12)),
            Entry("p:4", "D", videos(12, 11, 10))
        ))
        assertEquals(Level.SAME_VIDEOS, r.matches.first().level)
        assertEquals(Level.PARTIAL, r.matches.last().level)
    }

    @Test
    fun containmentAllInside() {
        val small = videos(1, 2, 3)
        val big = videos(9, 3, 2, 1, 8)
        assertEquals(3 to 3, SongOverlapPolicy.containment(small, big))
    }

    @Test
    fun containmentPartlyInside() {
        assertEquals(2 to 4, SongOverlapPolicy.containment(videos(1, 2, 50, 51), videos(1, 2, 3)))
    }

    @Test
    fun containmentRepeatsNeedEnoughCopies() {
        assertEquals(2 to 3, SongOverlapPolicy.containment(videos(1, 1, 2), videos(1, 2, 3)))
    }

    @Test
    fun matchedByAndInsideLabels() {
        val byVideo = SongOverlapPolicy.find(listOf(
            Entry("p:a", "A", videos(1, 2, 3)),
            Entry("p:b", "B", videos(1, 2, 3, 4, 5))
        )).matches.single()
        assertEquals("за відео", SongOverlapPolicy.matchedByLabel(byVideo))
        assertEquals("усі 3 є в 5", SongOverlapPolicy.insideLabel(byVideo))
        val byName = SongOverlapPolicy.find(listOf(
            Entry("p:a", "A", names("x one", "x two", "x three", "x four")),
            Entry("p:b", "B", names("X One", "X Two", "X Three", "Y"))
        )).matches.single()
        assertEquals("за назвами", SongOverlapPolicy.matchedByLabel(byName))
        assertNull(SongOverlapPolicy.insideLabel(byName))
    }

    /** Batch 10: an album and its Expanded edition share songs — not a copy. */
    @Test
    fun differentEditionsAreNamed() {
        val m = SongOverlapPolicy.Match(
            SongOverlapPolicy.Side("a", 10, 12), SongOverlapPolicy.Side("b", null, 25),
            Level.PARTIAL, 12)
        assertEquals("інше видання (Expanded) — не копія",
            SongOverlapPolicy.editionNote(m, "Music for the Jilted Generation",
                "Music for the Jilted Generation (Expanded)"))
        assertEquals(setOf("Live"), SongOverlapPolicy.editionMarks("World's on Fire (Live)"))
        assertEquals(setOf("Remaster", "Deluxe"),
            SongOverlapPolicy.editionMarks("Album – Remastered / DELUXE"))
        // Same release words, or the same songs → no note.
        assertNull(SongOverlapPolicy.editionNote(m, "A (Live)", "B live"))
        assertNull(SongOverlapPolicy.editionNote(m.copy(level = Level.SAME_VIDEOS), "A", "A (Expanded)"))
        assertNull(SongOverlapPolicy.editionNote(m, "Official Singles", "Invaders Must Die"))
    }

    /** Batch 10: group №31 — a live album sharing VIDEOS with studio songs. */
    @Test
    fun liveAlbumWithStudioVideosIsFlagged() {
        val m = SongOverlapPolicy.Match(
            SongOverlapPolicy.Side("a", 19, 20), SongOverlapPolicy.Side("b", 31, 14),
            Level.PARTIAL, 10, commonByVideo = 7)
        assertTrue(SongOverlapPolicy.liveVideoNote(m, "Official Singles", "World's on Fire (Live)")!!
            .startsWith("у live і студійному ті самі відео: 7"))
        // Matched only by names → a live song may well be another recording: no flag.
        assertNull(SongOverlapPolicy.liveVideoNote(m.copy(commonByVideo = 0),
            "Official Singles", "World's on Fire (Live)"))
        assertNull(SongOverlapPolicy.liveVideoNote(m, "Live A", "Live B"))
        assertNull(SongOverlapPolicy.liveVideoNote(m, "A", "B"))
    }
}
