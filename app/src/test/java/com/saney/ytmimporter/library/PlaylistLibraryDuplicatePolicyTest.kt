package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistLibraryDuplicatePolicy.Candidate
import com.saney.ytmimporter.library.PlaylistLibraryDuplicatePolicy.Evidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistLibraryDuplicatePolicyTest {
    private val a = "abcdefghijk"
    private val b = "12345678901"
    private val c = "ZYXWVutsrqp"

    @Test fun punctuationAndCaseFindTitleAndCountCandidates() {
        val hints = PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("local:a", "The Prodigy - Experience (2001)", 25),
            Candidate("youtube:b", "THE PRODIGY: EXPERIENCE 2001", 25)
        ))
        assertEquals(2, hints.size)
        assertEquals(Evidence.SAME_TITLE_COUNT, hints["local:a"]?.evidence)
        assertEquals(hints["local:a"]?.groupNumber, hints["youtube:b"]?.groupNumber)
    }

    @Test fun sameNameDifferentCountsAreOnlyPossibleRelations() {
        val hints = PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("local:19", "db ost", 19),
            Candidate("youtube:18", "db ost", 18)
        ))
        assertEquals(Evidence.SAME_TITLE, hints["local:19"]?.evidence)
        assertEquals(Evidence.SAME_TITLE, hints["youtube:18"]?.evidence)
    }

    @Test fun fullVideoFingerprintIgnoresOrderButPreservesMultiplicity() {
        val matches = PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("one", "Original", 3, listOf(a, b, b)),
            Candidate("two", "Renamed", 3, listOf(b, a, b)),
            Candidate("three", "Another", 3, listOf(a, a, b))
        ))
        assertEquals(2, matches.size)
        assertEquals(Evidence.SAME_TRACKS, matches["one"]?.evidence)
        assertFalse(matches.containsKey("three"))
    }

    @Test fun incompleteVideoMetadataIsNeverAnExactTrackMatch() {
        val hints = PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("x", "A collection", 3, listOf(a, b)),
            Candidate("y", "Different collection", 3, listOf(b, a)),
            Candidate("z", "Other", 2, listOf(a, "Deleted video"))
        ))
        assertTrue(hints.isEmpty())
    }

    @Test fun identicalRemoteIdIsNotProofOfDuplicateContents() {
        // The policy intentionally knows only catalogue identity, title,
        // count and complete track metadata, not a linked destination ID.
        val hints = PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("saved:one", "Album A", 12),
            Candidate("youtube:same-link", "Album B", 13)
        ))
        assertTrue(hints.isEmpty())
    }

    @Test fun independentDifferentEditionsAndSingleEntryStayUnflagged() {
        assertTrue(PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("a", "Experience (1992)", 12),
            Candidate("b", "Experience Expanded (2001)", 25)
        )).isEmpty())
        assertTrue(PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("only", "One", 1)
        )).isEmpty())
    }

    @Test fun stableGroupingKeepsThreeCopiesVisible() {
        val hints = PlaylistLibraryDuplicatePolicy.find(listOf(
            Candidate("local:a", "Album", 5),
            Candidate("history:b", "Album", 5),
            Candidate("youtube:c", "Album", 5)
        ))
        assertEquals(3, hints.size)
        assertEquals(setOf(3), hints.values.map { it.groupSize }.toSet())
        assertEquals(setOf(1), hints.values.map { it.groupNumber }.toSet())
    }
}
