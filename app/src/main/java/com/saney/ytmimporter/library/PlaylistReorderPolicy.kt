package com.saney.ytmimporter.library

/**
 * Batch 20 (faric 2026-11-05 23:34 + 2026-11-06 03:06: «як перевірити
 * порядкові номера треків… згідно з оригіналом у тих плейлістах, які вже є
 * на Ютубі»): the YouTube playlist is put into the order of a list on the
 * phone (the imported file, a saved copy, an archive).
 *
 * Only songs that are not in their place are moved, as few as possible: the
 * songs that already follow the list's order (the longest such run) stay;
 * every other one goes right after the song that comes before it in the list.
 * Songs on YouTube that are not in the list (and repeats) stay where they are;
 * songs of the list missing on YouTube are not added here.
 *
 * YouTube `playlistItems.update` with `snippet.position` = the song's final
 * 0-based index, 50 quota units per move.
 */
object PlaylistReorderPolicy {
    data class Item(val itemId: String, val videoId: String, val title: String)

    data class Move(
        val item: Item,
        /** 1-based number on YouTube before anything was moved. */
        val fromNumber: Int,
        /** 1-based number after all moves. */
        val toNumber: Int,
        /** 0-based position to send with this move, valid at its turn. */
        val position: Int
    )

    data class Plan(
        val moves: List<Move>,
        /** Songs of the list that are not on YouTube (not added by this). */
        val missingOnYouTube: Int,
        /** Songs on YouTube that are not in the list, or repeats (stay). */
        val notInList: Int,
        val youTubeCount: Int
    ) {
        val inOrder: Boolean get() = moves.isEmpty()
    }

    const val UNITS_PER_MOVE = 50

    fun plan(reference: List<String?>, youtube: List<Item>): Plan {
        val rank = HashMap<String, Int>()
        reference.forEachIndexed { index, raw ->
            val id = raw?.trim().orEmpty()
            if (id.isNotEmpty() && id !in rank) rank[id] = index
        }
        // Each list song counts once on YouTube (its first copy).
        val seen = HashSet<String>()
        val ranked = ArrayList<Int>()          // indices in youtube that take part
        youtube.forEachIndexed { index, item ->
            val r = rank[item.videoId]
            if (r != null && seen.add(item.videoId)) ranked += index
        }
        val keep = longestIncreasing(ranked.map { rank.getValue(youtube[it].videoId) })
            .map { ranked[it] }.toHashSet()
        val toMove = ranked.filter { it !in keep }
            .sortedBy { rank.getValue(youtube[it].videoId) }

        // Simulate the moves in list order.
        val current = youtube.indices.toMutableList()      // youtube indices in current order
        val settled = HashSet<Int>(keep)
        val positions = HashMap<Int, Int>()
        toMove.forEach { index ->
            current.remove(index)
            val myRank = rank.getValue(youtube[index].videoId)
            // After the nearest earlier song of the list that is already in place.
            var at = -1
            var best = -1
            current.forEachIndexed { pos, other ->
                if (other in settled) {
                    val r = rank.getValue(youtube[other].videoId)
                    if (r < myRank && r > best) { best = r; at = pos }
                }
            }
            val position = if (at >= 0) at + 1 else {
                // Nothing earlier: before the first song of the list that is in place.
                current.indexOfFirst { it in settled }.takeIf { it >= 0 } ?: 0
            }
            current.add(position, index)
            positions[index] = position
            settled += index
        }
        val finalNumber = HashMap<Int, Int>()
        current.forEachIndexed { pos, index -> finalNumber[index] = pos + 1 }
        val moves = toMove.map { index ->
            Move(youtube[index], index + 1, finalNumber.getValue(index), positions.getValue(index))
        }
        val onYouTube = youtube.map { it.videoId }.toHashSet()
        return Plan(
            moves = moves,
            missingOnYouTube = rank.keys.count { it !in onYouTube },
            notInList = youtube.size - ranked.size,
            youTubeCount = youtube.size
        )
    }

    /** Indices (into [values]) of one longest strictly increasing subsequence. */
    internal fun longestIncreasing(values: List<Int>): List<Int> {
        if (values.isEmpty()) return emptyList()
        val tails = ArrayList<Int>()            // index of the tail of each length
        val parent = IntArray(values.size) { -1 }
        values.forEachIndexed { i, v ->
            var lo = 0
            var hi = tails.size
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (values[tails[mid]] < v) lo = mid + 1 else hi = mid
            }
            if (lo > 0) parent[i] = tails[lo - 1]
            if (lo == tails.size) tails += i else tails[lo] = i
        }
        val result = ArrayList<Int>()
        var k = tails.last()
        while (k >= 0) { result += k; k = parent[k] }
        return result.reversed()
    }

    /** YouTube reasons meaning «this playlist does not accept positions» (not sorted by hand). */
    val positionRefusedReasons: Set<String> = setOf("manualSortRequired", "invalidPlaylistItemPosition")

    /** «№50 → №30» */
    fun moveLine(move: Move): String = "№${move.fromNumber} → №${move.toNumber}"
}
