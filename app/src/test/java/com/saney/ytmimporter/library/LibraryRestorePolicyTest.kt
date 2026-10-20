package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.LibraryRestorePolicy.Anchor
import com.saney.ytmimporter.library.LibraryRestorePolicy.CardBox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Batch 10: the Library comes back where faric left it after Android closed the app. */
class LibraryRestorePolicyTest {
    // Grid: two tiles per row, rows 300 px high.
    private val before = listOf(
        CardBox("phone:a", 100, 400), CardBox("phone:b", 100, 400),
        CardBox("history:c", 410, 710), CardBox("youtube:d", 410, 710),
        CardBox("phone:e", 720, 1020)
    )

    @Test fun anchorIsTheFirstCardStillOnScreen() {
        assertEquals(Anchor("history:c", 90), LibraryRestorePolicy.anchorAt(before, 500))
        // In the gap between rows: the next row, a little above it.
        assertEquals(Anchor("phone:e", -5), LibraryRestorePolicy.anchorAt(before, 715))
        // The very top of the list is not anchored: it simply starts from the top.
        assertNull(LibraryRestorePolicy.anchorAt(before, 0))
        assertNull(LibraryRestorePolicy.anchorAt(emptyList(), 300))
    }

    @Test fun sameCardAfterYouTubeCardsArrivedAbove() {
        val anchor = LibraryRestorePolicy.anchorAt(before, 500)!!
        // After the restore, YouTube cards were mixed in above «history:c».
        val after = listOf(
            CardBox("youtube:x", 100, 400), CardBox("phone:a", 100, 400),
            CardBox("phone:b", 410, 710), CardBox("youtube:y", 410, 710),
            CardBox("history:c", 720, 1020)
        )
        assertEquals(810, LibraryRestorePolicy.scrollFor(anchor, after))
        assertNull(LibraryRestorePolicy.scrollFor(Anchor("gone", 10), after))
        assertEquals(0, LibraryRestorePolicy.scrollFor(Anchor("youtube:x", -500), after))
    }

    @Test fun anchorIsKeptOnlyWhileYouTubeIsComingAndOwnerDidNotScroll() {
        assertTrue(LibraryRestorePolicy.youTubeStillComing(true, restoringGoogle = true, loadingList = false))
        assertTrue(LibraryRestorePolicy.youTubeStillComing(true, false, loadingList = true))
        assertFalse(LibraryRestorePolicy.youTubeStillComing(false, true, true))
        assertFalse(LibraryRestorePolicy.youTubeStillComing(true, false, false))
        assertTrue(LibraryRestorePolicy.keepAnchor(stillComing = true, userMoved = false))
        assertFalse(LibraryRestorePolicy.keepAnchor(stillComing = true, userMoved = true))
        assertFalse(LibraryRestorePolicy.keepAnchor(stillComing = false, userMoved = false))
        assertFalse(LibraryRestorePolicy.userMoved(810, null, 24))
        assertFalse(LibraryRestorePolicy.userMoved(820, 810, 24))
        assertTrue(LibraryRestorePolicy.userMoved(400, 810, 24))
    }

    @Test fun groupWindowWaitsForTheYouTubeList() {
        assertTrue(LibraryRestorePolicy.waitForYouTube(needsYouTube = true, stillComing = true))
        assertFalse(LibraryRestorePolicy.waitForYouTube(needsYouTube = true, stillComing = false))
        assertFalse(LibraryRestorePolicy.waitForYouTube(needsYouTube = false, stillComing = true))
    }

    /** Batch 12: a playlist page waits until its songs make it long enough. */
    @Test fun detailPlaceIsReachedOnlyWhenTheListIsLongEnough() {
        assertFalse(LibraryRestorePolicy.detailReached(appliedY = 0, wantedY = 1200, slop = 24))
        assertFalse(LibraryRestorePolicy.detailReached(600, 1200, 24))
        assertTrue(LibraryRestorePolicy.detailReached(1190, 1200, 24))
        assertTrue(LibraryRestorePolicy.detailReached(1200, 1200, 24))
    }
}
