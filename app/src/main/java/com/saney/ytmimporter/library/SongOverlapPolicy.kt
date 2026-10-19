package com.saney.ytmimporter.library

import java.text.Normalizer
import java.util.Locale

/**
 * Library batch 8 — «Знайти дублікати» by the songs INSIDE playlists
 * (owner request 2026-10-12, taken 2026-10-18 19:31). The «Група №N» finder
 * matches names (or exactly the same videos); a renamed copy, or a copy with
 * a few songs swapped, stays invisible there. This finds such pairs.
 *
 * Read-only: never selects, merges or deletes anything. Uses only songs the
 * app already has (phone, History, YouTube read earlier / remembered).
 */
object SongOverlapPolicy {
    data class Entry(
        val identity: String,
        val title: String,
        /** null = songs not known (e.g. a YouTube playlist not checked yet). */
        val songs: List<PlaylistLibraryDuplicateGroupPolicy.Song>?,
        /** Existing «Група №N», if the playlist is in one. */
        val groupNumber: Int? = null
    )

    enum class Level {
        /** Same songs, every one matched by its YouTube video. */
        SAME_VIDEOS,
        /** Same songs, some matched only by the song name (not proof). */
        SAME_SONGS,
        /** Many common songs, but not the same list. */
        PARTIAL
    }

    /** One side of a pair: a single playlist, or a whole «Група №N». */
    data class Side(
        /** Playlist shown for this side (a member when it is a group). */
        val identity: String,
        val groupNumber: Int?,
        val size: Int
    )

    data class Match(
        val first: Side,
        val second: Side,
        val level: Level,
        val common: Int,
        /** How many of [common] matched by the YouTube video (the rest by name). */
        val commonByVideo: Int = common
    )

    /** Batch 9: how the common songs matched. */
    enum class MatchedBy { VIDEO, NAME, BOTH }

    fun matchedBy(match: Match): MatchedBy = when (match.commonByVideo) {
        match.common -> MatchedBy.VIDEO
        0 -> MatchedBy.NAME
        else -> MatchedBy.BOTH
    }

    fun matchedByLabel(match: Match): String = when (matchedBy(match)) {
        MatchedBy.VIDEO -> "за відео"
        MatchedBy.NAME -> "за назвами"
        MatchedBy.BOTH -> "за відео і назвами"
    }

    /** «усі 320 є в 813» when one side is fully inside the other, else null. */
    fun insideLabel(match: Match): String? = when {
        match.first.size == match.second.size -> null
        match.common >= match.first.size -> "усі ${match.first.size} є в ${match.second.size}"
        match.common >= match.second.size -> "усі ${match.second.size} є в ${match.first.size}"
        else -> null
    }

    /**
     * Batch 9 (owner 2026-10-19 01:29: «320 чи 866?»): how many songs of
     * [target] are also in [reference]. Repeats count once per copy.
     * Returns (common, songs of target that could be compared).
     */
    fun containment(
        target: List<PlaylistLibraryDuplicateGroupPolicy.Song>,
        reference: List<PlaylistLibraryDuplicateGroupPolicy.Song>
    ): Pair<Int, Int> {
        val mine = target.mapNotNull(::songKey)
        val theirs = reference.mapNotNull(::songKey).groupingBy { it }.eachCount().toMutableMap()
        var common = 0
        for (key in mine) {
            val left = theirs[key] ?: 0
            if (left > 0) {
                common++
                theirs[key] = left - 1
            }
        }
        return common to mine.size
    }

    data class Result(
        val matches: List<Match>,
        /** Playlists whose songs are unknown, so they could not be compared. */
        val unknown: List<String>,
        val compared: Int
    )

    const val MIN_COMMON = 3
    const val MAX_SHOWN = 60

    private val videoId = Regex("^[A-Za-z0-9_-]{11}$")
    private val marks = Regex("\\p{M}+")
    private val separators = Regex("[^\\p{L}\\p{N}]+")

    /** Case, accents and punctuation do not matter; words (remix, live…) stay. */
    fun normalizeSong(title: String): String =
        Normalizer.normalize(title, Normalizer.Form.NFKD)
            .replace(marks, "")
            .lowercase(Locale.ROOT)
            .replace(separators, " ")
            .trim()

    /** A song is its YouTube video when known, otherwise its name. */
    fun songKey(song: PlaylistLibraryDuplicateGroupPolicy.Song): String? {
        song.videoId?.takeIf(videoId::matches)?.let { return "v:$it" }
        return normalizeSong(song.title).takeIf { it.isNotEmpty() }?.let { "t:$it" }
    }

    private class Prepared(
        val entry: Entry,
        val keys: Map<String, Int>,
        val size: Int,
        val allVideos: Boolean
    )

    fun find(entries: List<Entry>, minCommon: Int = MIN_COMMON): Result {
        val unknown = mutableListOf<String>()
        val prepared = entries.mapNotNull { entry ->
            val songs = entry.songs
            if (songs == null) {
                unknown += entry.identity
                return@mapNotNull null
            }
            val keyList = songs.mapNotNull(::songKey)
            if (keyList.isEmpty()) return@mapNotNull null
            Prepared(
                entry,
                keyList.groupingBy { it }.eachCount(),
                keyList.size,
                keyList.size == songs.size && keyList.all { it.startsWith("v:") }
            )
        }
        // Index «song → playlists» so only playlists sharing songs are compared.
        val index = HashMap<String, MutableList<Int>>()
        prepared.forEachIndexed { i, p -> p.keys.keys.forEach { index.getOrPut(it) { mutableListOf() } += i } }
        val pairCommon = HashMap<Long, Int>()
        for (holders in index.values) {
            if (holders.size < 2) continue
            for (x in holders.indices) for (y in x + 1 until holders.size) {
                val a = holders[x]
                val b = holders[y]
                val key = a.toLong() * 1_000_003L + b
                pairCommon[key] = (pairCommon[key] ?: 0) + 1
            }
        }
        val best = LinkedHashMap<String, Match>()
        for (pairKey in pairCommon.keys) {
            val a = prepared[(pairKey / 1_000_003L).toInt()]
            val b = prepared[(pairKey % 1_000_003L).toInt()]
            val ga = a.entry.groupNumber
            val gb = b.entry.groupNumber
            // Already shown together by «Група №N».
            if (ga != null && ga == gb) continue
            val common = a.keys.entries.sumOf { (k, n) -> minOf(n, b.keys[k] ?: 0) }
            val byVideo = a.keys.entries.filter { it.key.startsWith("v:") }
                .sumOf { (k, n) -> minOf(n, b.keys[k] ?: 0) }
            val same = a.keys == b.keys
            val smaller = minOf(a.size, b.size)
            val enough = common >= minCommon && common * 2 >= smaller
            if (!same && !enough) continue
            if (same && common < 2) continue
            val level = when {
                same && a.allVideos && b.allVideos -> Level.SAME_VIDEOS
                same -> Level.SAME_SONGS
                else -> Level.PARTIAL
            }
            fun side(p: Prepared) = Side(p.entry.identity, p.entry.groupNumber, p.size)
            val (first, second) = listOf(side(a), side(b)).sortedBy { unitKey(it) }
            val match = Match(first, second, level, common, byVideo)
            // Several copies of one group against the same playlist → one line.
            val unit = unitKey(first) + "|" + unitKey(second)
            val old = best[unit]
            if (old == null || rank(match) > rank(old)) best[unit] = match
        }
        val ordered = best.values.sortedWith(
            compareBy<Match> { it.level.ordinal }
                .thenByDescending { it.common }
                .thenBy { unitKey(it.first) }
        ).take(MAX_SHOWN)
        return Result(ordered, unknown, prepared.size)
    }

    private fun unitKey(side: Side): String =
        side.groupNumber?.let { "g:%06d".format(it) } ?: "p:${side.identity}"

    private fun rank(m: Match): Long =
        (Level.values().size - m.level.ordinal).toLong() * 1_000_000L + m.common

    /** Short plain line for a result row. */
    fun verdict(match: Match): String = when (match.level) {
        Level.SAME_VIDEOS -> "однакові пісні: ${match.common} — ті самі відео YouTube"
        Level.SAME_SONGS -> "однакові пісні за назвами: ${match.common} — відео не всі перевірені"
        Level.PARTIAL -> "спільних пісень: ${match.common} • тут ${match.first.size}, там ${match.second.size}"
    }
}
