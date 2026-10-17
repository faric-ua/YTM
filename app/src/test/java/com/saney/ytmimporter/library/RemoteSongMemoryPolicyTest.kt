package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.RemoteSongMemoryPolicy.Entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteSongMemoryPolicyTest {
    private val a = "abcdefghijk"
    private val b = "12345678901"

    @Test fun encodeDecodeRoundTrip() {
        val memory = mapOf(
            "PLone" to Entry(2, listOf(a, b), 1_000L),
            "PLtwo" to Entry(1, listOf(""), 2_000L),
            "PLempty" to Entry(0, emptyList(), 3_000L)
        )
        assertEquals(memory, RemoteSongMemoryPolicy.decode(RemoteSongMemoryPolicy.encode(memory)))
    }

    @Test fun brokenLinesAreSkippedNotThrown() {
        val text = "PLok\t1\t5\t1\t$a\nbroken line\nPLbad\tx\t5\t1\t$a\nPL\u0000\t1\t5\t1\t$a\nPLc\t1\t5\t2\tbad,id!\nPLd\t2\t5\t3\t$a,$a"
        assertEquals(setOf("PLok"), RemoteSongMemoryPolicy.decode(text).keys)
        assertTrue(RemoteSongMemoryPolicy.decode(null).isEmpty())
        assertTrue(RemoteSongMemoryPolicy.decode("").isEmpty())
    }

    @Test fun rememberReplacesAndCleansIds() {
        val first = RemoteSongMemoryPolicy.remember(emptyMap(), "PL1", 2, listOf(a, null), 10)
        assertEquals(listOf(a, ""), first.getValue("PL1").videoIds)
        val second = RemoteSongMemoryPolicy.remember(first, "PL1", 2, listOf(a, b), 20)
        assertEquals(Entry(2, listOf(a, b), 20), second.getValue("PL1"))
        assertEquals(first, RemoteSongMemoryPolicy.remember(first, "bad id!", 1, listOf(a), 30))
    }

    @Test fun keepsAtMostTheNewest() {
        var memory = emptyMap<String, Entry>()
        for (i in 0 until RemoteSongMemoryPolicy.MAX_PLAYLISTS + 5) {
            memory = RemoteSongMemoryPolicy.remember(memory, "PL$i", 1, listOf(a), i.toLong())
        }
        assertEquals(RemoteSongMemoryPolicy.MAX_PLAYLISTS, memory.size)
        assertTrue("PL0" !in memory)
        assertTrue("PL${RemoteSongMemoryPolicy.MAX_PLAYLISTS + 4}" in memory)
    }

    @Test fun usableOnlyWhileSongCountIsTheSame() {
        val entry = Entry(3, listOf(a, b, a), 1)
        assertEquals(entry, RemoteSongMemoryPolicy.usable(entry, 3))
        assertNull("playlist changed on YouTube", RemoteSongMemoryPolicy.usable(entry, 4))
        assertNull(RemoteSongMemoryPolicy.usable(null, 3))
    }
}
