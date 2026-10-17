package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistLibraryDuplicateGroupPolicy.Mark
import com.saney.ytmimporter.library.PlaylistLibraryDuplicatePolicy.Candidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistLibraryDuplicateGroupPolicyTest {
    private val a = "abcdefghijk"
    private val b = "12345678901"
    private val c = "ZYXWVutsrqp"

    private fun marks(o: PlaylistLibraryDuplicateGroupPolicy.Overview) =
        o.bands.flatMap { it.rows }.associate { it.identity to it.mark }

    @Test fun bandsGoFromMoreSongsToFewer() {
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("phone", "db ost", 2, listOf(a, b)),
            Candidate("yt", "db ost", 3, null),
            Candidate("history", "db ost", 2, listOf(b, a))
        ))
        assertEquals(listOf(3, 2), o.bands.map { it.trackCount })
        assertEquals(Mark.SAME, marks(o)["phone"])
        assertEquals(Mark.SAME, marks(o)["history"])
        assertEquals(Mark.UNCHECKED, marks(o)["yt"])
        assertEquals(1, o.bands[1].rows.first().sameWith)
    }

    @Test fun knownButDifferentSongsWithSameCount() {
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("one", "Mix", 2, listOf(a, b)),
            Candidate("two", "Mix", 2, listOf(a, c))
        ))
        assertEquals(Mark.DIFFERENT, marks(o)["one"])
        assertEquals(Mark.DIFFERENT, marks(o)["two"])
        assertEquals(2, o.different)
    }

    @Test fun aloneKnownListIsUncheckedNotDifferent() {
        // Nothing to compare with: do not claim the songs differ.
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("phone", "Mix", 2, listOf(a, b)),
            Candidate("yt", "Mix", 2, null)
        ))
        assertEquals(Mark.UNCHECKED, marks(o)["phone"])
        assertEquals(Mark.UNCHECKED, marks(o)["yt"])
    }

    @Test fun incompleteListIsUnchecked() {
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("one", "Mix", 2, listOf(a, "")),
            Candidate("two", "Mix", 2, listOf(a, b)),
            Candidate("three", "Mix", 2, listOf(b, a))
        ))
        assertEquals(Mark.UNCHECKED, marks(o)["one"])
        assertEquals(2, o.exactCopies)
        assertEquals(1, o.unchecked)
    }

    @Test fun summaryNamesOnlyWhatApplies() {
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("p", "db ost", 19, null),
            Candidate("y", "db ost", 18, null)
        ))
        assertEquals("2 плейлісти • не перевірено: 2 • кількість пісень різна: 19 / 18",
            PlaylistLibraryDuplicateGroupPolicy.summary(o))
    }

    @Test fun membersFollowGroupNumber() {
        val items = listOf(
            Candidate("a1", "Alpha album", 3), Candidate("a2", "Alpha album", 3),
            Candidate("b1", "Beta album", 4), Candidate("b2", "Beta album", 4)
        )
        val hints = PlaylistLibraryDuplicatePolicy.find(items)
        val group = hints.getValue("b1").groupNumber
        assertEquals(setOf("b1", "b2"), PlaylistLibraryDuplicateGroupPolicy
            .members(group, items, hints).map { it.identity }.toSet())
    }

    @Test fun ukrainianPlurals() {
        assertEquals("1 плейліст", "1 " + PlaylistLibraryDuplicateGroupPolicy.playlistsWord(1))
        assertEquals("плейлісти", PlaylistLibraryDuplicateGroupPolicy.playlistsWord(3))
        assertEquals("плейлістів", PlaylistLibraryDuplicateGroupPolicy.playlistsWord(12))
        assertEquals("пісня", PlaylistLibraryDuplicateGroupPolicy.songsWord(21))
        assertEquals("пісень", PlaylistLibraryDuplicateGroupPolicy.songsWord(19))
    }

    @Test fun cardVerdictIsShortAndTileKeepsGroupHintOnOwnLine() {
        // Phone 2026-10-17 00:25: the long verdict was cut on 2-line tiles and
        // «група №N ›» disappeared. Short verdict; on a tile — its own line.
        val hint = PlaylistLibraryDuplicatePolicy.Hint(
            12, 4, PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE_COUNT)
        assertEquals("Схоже на копію\nгрупа №12 (4) ›",
            PlaylistLibraryDuplicateGroupPolicy.cardLine(hint, tile = true))
        assertEquals("Схоже на копію • група №12 (4) ›",
            PlaylistLibraryDuplicateGroupPolicy.cardLine(hint, tile = false))
        PlaylistLibraryDuplicatePolicy.Evidence.values().forEach { evidence ->
            val verdict = PlaylistLibraryDuplicateGroupPolicy.cardVerdict(evidence)
            // Fits one line of a half-width tile at 11.5sp.
            assertTrue("$evidence: $verdict", verdict.length <= 16)
            assertTrue(PlaylistLibraryDuplicateGroupPolicy.verdictExplained(evidence)
                .startsWith(verdict + " — "))
        }
    }

    @Test fun songsCheckQuotaIsOneUnitPerFiftySongsAtLeastOne() {
        assertEquals(0, PlaylistLibraryDuplicateGroupPolicy.songsCheckQuota(emptyList()))
        assertEquals(1, PlaylistLibraryDuplicateGroupPolicy.songsCheckQuota(listOf(0)))
        assertEquals(1, PlaylistLibraryDuplicateGroupPolicy.songsCheckQuota(listOf(50)))
        assertEquals(2, PlaylistLibraryDuplicateGroupPolicy.songsCheckQuota(listOf(51)))
        assertEquals(1 + 1 + 3, PlaylistLibraryDuplicateGroupPolicy.songsCheckQuota(
            listOf(3, 25, 101)))
    }

    @Test fun loadedYouTubeSongsTurnUncheckedIntoSame() {
        // What «Перевірити пісні YouTube» achieves: the same playlist before
        // (no songs known) and after (songs read) the check.
        val before = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("phone", "Mix", 2, listOf(a, b)),
            Candidate("youtube:PL1", "Mix", 2, null)
        ))
        assertEquals(2, before.unchecked)
        val after = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("phone", "Mix", 2, listOf(a, b)),
            Candidate("youtube:PL1", "Mix", 2, listOf(b, a))
        ))
        assertEquals(2, after.exactCopies)
        assertEquals(0, after.unchecked)
    }

    @Test fun oldestNeedsAUniqueEarliestDate() {
        assertEquals("phone", PlaylistLibraryDuplicateGroupPolicy.oldest(
            mapOf("phone" to 100L, "yt" to 200L, "history" to 0L)))
        assertNull("only one dated", PlaylistLibraryDuplicateGroupPolicy.oldest(
            mapOf("phone" to 100L, "yt" to 0L)))
        assertNull("shared earliest", PlaylistLibraryDuplicateGroupPolicy.oldest(
            mapOf("a" to 100L, "b" to 100L, "c" to 300L)))
    }

    @Test fun referenceIsTheMostCommonOtherList() {
        val band = listOf(
            Candidate("odd", "X", 2, listOf(a, c)),
            Candidate("y1", "X", 2, listOf(a, b)),
            Candidate("y2", "X", 2, listOf(b, a)),
            Candidate("q", "X", 2, null)
        )
        assertEquals("y1", PlaylistLibraryDuplicateGroupPolicy.referenceFor("odd", band))
        // Tie (each other list once): the first in the band.
        assertEquals("odd", PlaylistLibraryDuplicateGroupPolicy.referenceFor("y1", band))
        assertNull(PlaylistLibraryDuplicateGroupPolicy.referenceFor("odd",
            listOf(Candidate("odd", "X", 2, listOf(a, c)), Candidate("q", "X", 2, null))))
    }

    @Test fun differenceSongBySong() {
        val S = PlaylistLibraryDuplicateGroupPolicy::Song
        val target = listOf(S(a, "A"), S(c, "C"), S(null, "No video"), S(a, "A again"))
        val reference = listOf(S(a, "A"), S(b, "B"), S(a, "A2"), S(b, "B2"))
        val d = PlaylistLibraryDuplicateGroupPolicy.difference(target, reference)
        assertEquals(listOf("C"), d.onlyHere.map { it.title })
        assertEquals(listOf("B", "B2"), d.missingHere.map { it.title })
        assertEquals(listOf("No video"), d.withoutVideo.map { it.title })
        val none = PlaylistLibraryDuplicateGroupPolicy.difference(listOf(S(a, "A")), null)
        assertTrue(none.isEmpty)
        assertTrue(PlaylistLibraryDuplicateGroupPolicy.difference(
            listOf(S(a, "1"), S(b, "2")), listOf(S(b, "x"), S(a, "y"))).isEmpty)
    }

    @Test fun summarySaysSongsPerPlaylistForOneBand() {
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("p", "Mix", 4, null), Candidate("y", "Mix", 4, null)))
        assertTrue(PlaylistLibraryDuplicateGroupPolicy.summary(o)
            .startsWith("2 плейлісти по 4 пісні • "))
    }

    @Test fun removalWarningsSayWhetherSongsAreLost() {
        val o = PlaylistLibraryDuplicateGroupPolicy.overview(listOf(
            Candidate("a", "X", 2, listOf(a, b)),
            Candidate("b", "X", 2, listOf(b, a)),
            Candidate("odd", "X", 2, listOf(a, c)),
            Candidate("q", "X", 3, null)
        ))
        val same = PlaylistLibraryDuplicateGroupPolicy.removalWarnings("a", o, oldest = "a")
        assertTrue(same[0].startsWith("Пісні не загубляться: лишиться ще 1 плейліст"))
        assertEquals("Це найстаріший плейліст групи.", same[1])
        assertTrue(PlaylistLibraryDuplicateGroupPolicy.removalWarnings("odd", o, null)
            .single().startsWith("Увага: такого складу"))
        assertTrue(PlaylistLibraryDuplicateGroupPolicy.removalWarnings("q", o, null)
            .single().contains("не перевірені"))
        assertTrue(PlaylistLibraryDuplicateGroupPolicy.removalWarnings("none", o, null).isEmpty())
    }
}
