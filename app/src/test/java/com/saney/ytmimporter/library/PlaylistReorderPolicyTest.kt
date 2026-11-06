package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistReorderPolicy.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Batch 20: «Вирівняти порядок» — as few moves as possible, each valid at its turn. */
class PlaylistReorderPolicyTest {
    private fun items(vararg ids: String) = ids.map { Item("item-$it", it, "Пісня $it") }

    /** Replays the moves the way YouTube does (remove, then insert at position). */
    private fun apply(youtube: List<Item>, plan: PlaylistReorderPolicy.Plan): List<String> {
        val list = youtube.toMutableList()
        plan.moves.forEach { move ->
            list.remove(move.item)
            list.add(move.position, move.item)
        }
        return list.map { it.videoId }
    }

    /** faric 05.11: №30 «Invaders Must Die» ended up 50th. One move: №50 → №30. */
    @Test fun bestOfTheBestCase() {
        val reference = (1..50).map { "v$it" }
        val youtube = items(*((1..29).map { "v$it" } + (31..50).map { "v$it" } + "v30").toTypedArray())
        val plan = PlaylistReorderPolicy.plan(reference, youtube)
        assertEquals(1, plan.moves.size)
        val move = plan.moves.single()
        assertEquals("v30", move.item.videoId)
        assertEquals("№50 → №30", PlaylistReorderPolicy.moveLine(move))
        assertEquals(29, move.position)
        assertEquals(reference, apply(youtube, plan))
    }

    @Test fun sameOrderNeedsNothing() {
        val plan = PlaylistReorderPolicy.plan(listOf("a", "b", "c"), items("a", "b", "c"))
        assertTrue(plan.inOrder)
        assertEquals(0, plan.missingOnYouTube)
    }

    @Test fun reversedOrderIsFixedAndValid() {
        val reference = listOf("a", "b", "c", "d", "e")
        val youtube = items("e", "d", "c", "b", "a")
        val plan = PlaylistReorderPolicy.plan(reference, youtube)
        assertEquals(4, plan.moves.size)
        assertEquals(reference, apply(youtube, plan))
    }

    @Test fun firstSongMovedToTheTop() {
        val reference = listOf("a", "b", "c")
        val youtube = items("b", "c", "a")
        val plan = PlaylistReorderPolicy.plan(reference, youtube)
        assertEquals(1, plan.moves.size)
        assertEquals(0, plan.moves.single().position)
        assertEquals("№3 → №1", PlaylistReorderPolicy.moveLine(plan.moves.single()))
        assertEquals(reference, apply(youtube, plan))
    }

    /** Songs not in the list (and repeats) stay; missing ones are only counted. */
    @Test fun foreignAndMissingSongs() {
        val reference = listOf("a", "b", "c", "d")
        val youtube = items("x", "c", "a", "y", "a")   // b, d missing; x, y, 2nd a foreign
        val plan = PlaylistReorderPolicy.plan(reference, youtube)
        assertEquals(2, plan.missingOnYouTube)
        assertEquals(3, plan.notInList)
        assertEquals(1, plan.moves.size)
        val result = apply(youtube, plan)
        assertTrue("a before c: $result", result.indexOf("a") < result.indexOf("c"))
        assertEquals("x stays first", "x", result.first())
        assertEquals(5, result.size)
    }

    @Test fun manyRandomOrdersEndInListOrder() {
        val random = java.util.Random(19)
        repeat(200) {
            val n = 1 + random.nextInt(40)
            val reference = (1..n).map { "s$it" }
            val youtube = items(*reference.shuffled(random).toTypedArray())
            val plan = PlaylistReorderPolicy.plan(reference, youtube)
            assertEquals(reference, apply(youtube, plan))
            // Never more moves than songs out of the longest run in place.
            assertTrue(plan.moves.size < n || n == 0)
        }
    }

    @Test fun longestIncreasingKeepsTheRun() {
        assertEquals(listOf(0, 1, 3), PlaylistReorderPolicy.longestIncreasing(listOf(1, 2, 0, 3)))
        assertEquals(emptyList<Int>(), PlaylistReorderPolicy.longestIncreasing(emptyList()))
    }
}
