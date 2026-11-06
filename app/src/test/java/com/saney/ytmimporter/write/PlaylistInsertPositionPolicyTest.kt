package com.saney.ytmimporter.write

import com.saney.ytmimporter.write.PlaylistInsertPositionPolicy.APPEND
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/** Batch 19: a song added to an existing playlist goes to its own place. */
class PlaylistInsertPositionPolicyTest {
    private fun ids(n: Int) = (1..n).map { "v$it" }

    /** faric 05.11 23:34: №30 of 50 was missing; it must go to position 29, not the end. */
    @Test fun missingSongGoesToItsNumber() {
        val order = ids(50)
        val existing = order - "v30"
        val write = order.map { it == "v30" }
        val result = PlaylistInsertPositionPolicy.positions(order, write, existing)
        assertArrayEquals(IntArray(50) { if (it == 29) 29 else APPEND }, result)
    }

    @Test fun lastSongIsAPlainAppend() {
        val order = ids(5)
        val result = PlaylistInsertPositionPolicy.positions(order, order.map { it == "v5" }, ids(4))
        assertArrayEquals(intArrayOf(APPEND, APPEND, APPEND, APPEND, APPEND), result)
    }

    @Test fun firstSongGoesToTheTop() {
        val order = ids(3)
        val result = PlaylistInsertPositionPolicy.positions(order, order.map { it == "v1" }, listOf("v2", "v3"))
        assertArrayEquals(intArrayOf(0, APPEND, APPEND), result)
    }

    /** Several missing in a row: each after the one written just before it. */
    @Test fun runOfMissingSongsKeepsTheOrder() {
        val order = ids(6)
        val write = order.map { it in setOf("v2", "v3", "v4") }
        val result = PlaylistInsertPositionPolicy.positions(order, write, listOf("v1", "v5", "v6"))
        assertArrayEquals(intArrayOf(APPEND, 1, 2, 3, APPEND, APPEND), result)
    }

    /** New playlist (nothing there yet): everything is a plain append. */
    @Test fun emptyPlaylistIsAllAppends() {
        val order = ids(4)
        val result = PlaylistInsertPositionPolicy.positions(order, order.map { true }, emptyList())
        assertArrayEquals(IntArray(4) { APPEND }, result)
    }

    /** Songs on YouTube that are not in the list stay where they are. */
    @Test fun foreignSongsOnYouTubeAreRespected() {
        val order = listOf("a", "b", "c")
        val existing = listOf("a", "x", "y", "c")
        val result = PlaylistInsertPositionPolicy.positions(order, order.map { it == "b" }, existing)
        assertArrayEquals(intArrayOf(APPEND, 1, APPEND), result)
    }

    @Test fun songsWithoutVideoAreNotAnchorsNorWritten() {
        val order = listOf("v1", null, "v3", "v4")
        val write = listOf(false, true, true, false)
        val result = PlaylistInsertPositionPolicy.positions(order, write, listOf("v1", "v4"))
        assertArrayEquals(intArrayOf(APPEND, APPEND, 1, APPEND), result)
    }

    @Test fun refusedReasonsAreYouTubeCodes() {
        org.junit.Assert.assertTrue("manualSortRequired" in PlaylistInsertPositionPolicy.positionRefusedReasons)
    }
}
