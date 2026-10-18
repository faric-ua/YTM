package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistLibraryDuplicatePolicy.Candidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupNumberPolicyTest {
    private fun group(name: String, vararg ids: String) =
        ids.map { Candidate(it, name, 3) }

    private val alpha = group("Alpha", "a1", "a2")
    private val beta = group("Beta", "b1", "b2")
    private val gamma = group("Gamma", "g1", "g2")

    private fun numbers(items: List<Candidate>, memory: GroupNumberPolicy.Memory):
        Pair<Map<String, Int>, GroupNumberPolicy.Memory> {
        val r = GroupNumberPolicy.renumber(items, PlaylistLibraryDuplicatePolicy.find(items), memory)
        return r.hints.mapValues { it.value.groupNumber } to r.memory
    }

    @Test fun firstTimeKeepsTodaysAlphabeticalNumbers() {
        val (n, _) = numbers(alpha + beta + gamma, GroupNumberPolicy.Memory())
        assertEquals(1, n["a1"]); assertEquals(2, n["b1"]); assertEquals(3, n["g1"])
    }

    @Test fun numberStaysWhenAnotherGroupDisappears() {
        // Owner 2026-10-18 03:11: after a delete «група №30» got another number.
        val (_, memory) = numbers(alpha + beta + gamma, GroupNumberPolicy.Memory())
        val (n, _) = numbers(beta + gamma, memory)
        assertEquals(2, n["b1"]); assertEquals(3, n["g1"])
    }

    @Test fun newGroupGetsANeverUsedNumber() {
        val (_, m1) = numbers(alpha + gamma, GroupNumberPolicy.Memory())
        // Beta appears later, alphabetically between them: does not push Gamma.
        val (n, m2) = numbers(alpha + beta + gamma, m1)
        assertEquals(1, n["a1"]); assertEquals(2, n["g1"]); assertEquals(3, n["b1"])
        // Alpha solved (gone), a new Aardvark group must not take №1.
        val (n2, _) = numbers(group("Aardvark", "x1", "x2") + beta + gamma, m2)
        assertEquals(4, n2["x1"]); assertEquals(3, n2["b1"])
    }

    @Test fun groupShrinkingKeepsItsNumber() {
        val big = group("What Evil Lurks", "p1", "p2", "y1", "y2")
        val (_, memory) = numbers(alpha + big, GroupNumberPolicy.Memory())
        val (n, _) = numbers(alpha + big.drop(1), memory)
        assertEquals(2, n["p2"])
    }

    @Test fun encodeDecodeRoundTrip() {
        val m = GroupNumberPolicy.Memory(mapOf("alpha" to 1, "what evil lurks 1991" to 30), 31)
        assertEquals(m, GroupNumberPolicy.decode(GroupNumberPolicy.encode(m)))
        assertEquals(GroupNumberPolicy.Memory(), GroupNumberPolicy.decode("junk"))
        assertEquals(GroupNumberPolicy.Memory(), GroupNumberPolicy.decode(null))
        val repaired = GroupNumberPolicy.decode("1\n5\tx\nbad line")
        assertEquals(6, repaired.next)
    }

    @Test fun neverTwoGroupsWithOneNumber() {
        val m = GroupNumberPolicy.Memory(mapOf("alpha" to 1, "beta" to 1), 2)
        val (n, _) = numbers(alpha + beta, m)
        assertNotEquals(n["a1"], n["b1"])
        assertTrue(n.values.all { it > 0 })
    }
}
