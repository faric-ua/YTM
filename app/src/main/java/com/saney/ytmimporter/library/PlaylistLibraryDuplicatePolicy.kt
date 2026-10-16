package com.saney.ytmimporter.library

import java.util.Locale

/**
 * Read-only candidate finder. Never merges, selects or deletes any playlist.
 * Different catalogue identities stay independent even when names match.
 * Full video lists are optional; never fetch videos from YouTube just to rank.
 */
object PlaylistLibraryDuplicatePolicy {
    data class Candidate(
        val identity: String,
        val title: String,
        val trackCount: Int,
        val videoIds: List<String>? = null
    )

    enum class Evidence {
        SAME_TRACKS, SAME_TITLE_COUNT, SAME_TITLE
    }

    data class Hint(
        val groupNumber: Int,
        val groupSize: Int,
        val evidence: Evidence
    )

    private val punctuation = Regex("[^\\p{L}\\p{N}]+")
    private val videoId = Regex("^[A-Za-z0-9_-]{11}$")

    fun normalizedTitle(title: String): String =
        title.lowercase(Locale.ROOT).replace(punctuation, " ").trim()

    /**
     * Sorted full list of video IDs, or null when the songs are not known
     * completely (not loaded, a track without a valid video, wrong count).
     * Order is ignored, repeats count.
     */
    fun songFingerprint(item: Candidate): List<String>? =
        item.videoIds
            ?.takeIf { it.size >= 2 && it.size == item.trackCount }
            ?.takeIf { list -> list.all(videoId::matches) }
            ?.sorted()

    fun find(items: List<Candidate>): Map<String, Hint> {
        if (items.size < 2) return emptyMap()
        val names = items.map { normalizedTitle(it.title) }
        val contents = items.map(::songFingerprint)
        val parent = IntArray(items.size) { it }
        fun root(i: Int): Int {
            var p = i
            while (parent[p] != p) p = parent[p]
            return p
        }
        fun join(a: Int, b: Int) {
            val x = root(a)
            val y = root(b)
            if (x != y) parent[y] = x
        }
        val matches = mutableMapOf<Pair<Int, Int>, Evidence>()
        for (a in items.indices) {
            for (b in a + 1 until items.size) {
                if (items[a].identity == items[b].identity) continue
                val equalVideos = contents[a] != null && contents[a] == contents[b]
                val equalName = names[a].length >= 4 && names[a] == names[b]
                val evidence = when {
                    equalVideos -> Evidence.SAME_TRACKS
                    equalName && items[a].trackCount == items[b].trackCount ->
                        Evidence.SAME_TITLE_COUNT
                    equalName -> Evidence.SAME_TITLE
                    else -> null
                } ?: continue
                matches[a to b] = evidence
                join(a, b)
            }
        }
        val groups = items.indices.groupBy(::root).values
            .filter { it.size > 1 }
            .sortedWith(compareBy<List<Int>> { names[it[0]] }
                .thenBy { items[it[0]].identity })
        val result = mutableMapOf<String, Hint>()
        groups.forEachIndexed { ordinal, group ->
            group.forEach { i ->
                val best = group.mapNotNull { j ->
                    if (i == j) null
                    else matches[minOf(i, j) to maxOf(i, j)]
                }.minByOrNull { it.ordinal } ?: Evidence.SAME_TITLE
                result[items[i].identity] = Hint(
                    groupNumber = ordinal + 1,
                    groupSize = group.size,
                    evidence = best
                )
            }
        }
        return result
    }
}
