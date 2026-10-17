package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.PlaylistLibraryDuplicatePolicy.Candidate

/**
 * Explains one «Схожі» group in plain words (owner 2026-10-16 21:57: even
 * after reading «?» it was not clear which playlists are full copies and which
 * are not). Read-only: never selects, merges or deletes anything.
 *
 * Members are laid out by number of songs (bigger first). Inside one count:
 * - SAME: the full song lists are known and equal to at least one other
 *   member — a real copy;
 * - DIFFERENT: the songs are known, but no other member with a known list has
 *   the same songs;
 * - UNCHECKED: the songs are not known completely (YouTube playlist not opened
 *   yet, or some track has no video), so nothing can be proven.
 */
object PlaylistLibraryDuplicateGroupPolicy {
    enum class Mark { SAME, DIFFERENT, UNCHECKED }

    data class Row(
        val identity: String,
        val mark: Mark,
        /** How many OTHER members have exactly the same songs. */
        val sameWith: Int
    )

    data class Band(val trackCount: Int, val rows: List<Row>)

    data class Overview(
        val bands: List<Band>,
        val exactCopies: Int,
        val different: Int,
        val unchecked: Int
    ) {
        val size: Int get() = bands.sumOf { it.rows.size }
    }

    fun members(
        groupNumber: Int,
        candidates: List<Candidate>,
        hints: Map<String, PlaylistLibraryDuplicatePolicy.Hint>
    ): List<Candidate> = candidates.filter { hints[it.identity]?.groupNumber == groupNumber }

    fun overview(members: List<Candidate>): Overview {
        val bands = members
            .groupBy { it.trackCount }
            .toSortedMap(compareByDescending { it })
            .map { (count, list) ->
                val prints = list.associate { it.identity to
                    PlaylistLibraryDuplicatePolicy.songFingerprint(it) }
                val sameCount = prints.values.filterNotNull()
                    .groupingBy { it }.eachCount()
                val rows = list.map { item ->
                    val print = prints[item.identity]
                    val others = print?.let { (sameCount[it] ?: 1) - 1 } ?: 0
                    val mark = when {
                        print == null -> Mark.UNCHECKED
                        others > 0 -> Mark.SAME
                        // Known songs, but nobody to compare with in this count.
                        prints.values.count { it != null } < 2 -> Mark.UNCHECKED
                        else -> Mark.DIFFERENT
                    }
                    Row(item.identity, mark, others)
                }.sortedWith(compareBy<Row> { it.mark.ordinal }
                    .thenByDescending { it.sameWith })
                Band(count, rows)
            }
        val rows = bands.flatMap { it.rows }
        return Overview(
            bands = bands,
            exactCopies = rows.count { it.mark == Mark.SAME },
            different = rows.count { it.mark == Mark.DIFFERENT },
            unchecked = rows.count { it.mark == Mark.UNCHECKED }
        )
    }

    /** One line for the top of the group window. */
    fun summary(overview: Overview): String {
        val parts = mutableListOf<String>()
        if (overview.exactCopies > 0) parts += "однакові пісні: ${overview.exactCopies}"
        if (overview.different > 0) parts += "пісні різні: ${overview.different}"
        if (overview.unchecked > 0) parts += "не перевірено: ${overview.unchecked}"
        if (overview.bands.size > 1) {
            parts += "кількість пісень різна: " +
                overview.bands.joinToString(" / ") { it.trackCount.toString() }
        }
        // Owner 2026-10-17 05:27: «7» read as songs. Say «по 4 пісні» when
        // all have the same number of songs.
        val head = "${overview.size} ${playlistsWord(overview.size)}" +
            (overview.bands.singleOrNull()?.let { " по ${it.trackCount} ${songsWord(it.trackCount)}" }
                ?: "")
        return head + " • " + parts.joinToString(" • ")
    }

    /**
     * The member with the earliest date (phone: first saved, History: import,
     * YouTube: created on YouTube) — shown as «найстаріший». The app cannot
     * know an «original»; this is only the honest fact closest to it.
     * Null when fewer than two members have a date or the earliest is shared.
     */
    fun oldest(dates: Map<String, Long>): String? {
        val known = dates.filterValues { it > 0L }
        if (known.size < 2) return null
        val min = known.values.minOrNull() ?: return null
        val first = known.filterValues { it == min }.keys
        return first.singleOrNull()
    }

    /**
     * Extra lines in the «remove this copy» confirmation (batch 5, owner
     * 2026-10-17 20:49: remove copies one at a time, only by hand). Plain
     * facts, never a block: removing a ✓ copy loses nothing (identical copies
     * stay), removing a ≠ / ? copy may lose a song list that exists nowhere
     * else in the group.
     */
    fun removalWarnings(target: String, overview: Overview, oldest: String?): List<String> {
        val row = overview.bands.flatMap { it.rows }.firstOrNull { it.identity == target }
            ?: return emptyList()
        val lines = mutableListOf<String>()
        when (row.mark) {
            Mark.SAME -> lines += "Пісні не загубляться: лишиться ще ${row.sameWith} " +
                "${playlistsWord(row.sameWith)} з тими самими піснями."
            Mark.DIFFERENT -> lines += "Увага: такого складу пісень більше ніде в групі нема — " +
                "після видалення його не лишиться (див. «чим відрізняється»)."
            Mark.UNCHECKED -> lines += "Увага: пісні цього плейліста не перевірені — " +
                "невідомо, чи є такий самий склад деінде."
        }
        if (target == oldest) lines += "Це найстаріший плейліст групи."
        return lines
    }

    data class Song(val videoId: String?, val title: String)

    data class Difference(
        /** Songs (with a video) in this playlist that the reference lacks. */
        val onlyHere: List<Song>,
        /** Songs of the reference that this playlist lacks. */
        val missingHere: List<Song>,
        /** Songs of this playlist with no YouTube video at all. */
        val withoutVideo: List<Song>
    ) {
        val isEmpty: Boolean
            get() = onlyHere.isEmpty() && missingHere.isEmpty() && withoutVideo.isEmpty()
    }

    private val videoIdPattern = Regex("^[A-Za-z0-9_-]{11}$")

    private fun validId(id: String?): String? = id?.takeIf(videoIdPattern::matches)

    /**
     * What a «≠» or «?» playlist is compared with: among the OTHER members
     * with the same number of songs and a complete known song list, the list
     * shared by most of them (ties — the first in [band] order).
     */
    fun referenceFor(target: String, band: List<Candidate>): String? {
        val others = band.filter { it.identity != target }
            .mapNotNull { c -> PlaylistLibraryDuplicatePolicy.songFingerprint(c)?.let { c to it } }
        if (others.isEmpty()) return null
        val counts = others.groupingBy { it.second }.eachCount()
        val best = counts.values.maxOrNull() ?: return null
        return others.first { counts[it.second] == best }.first.identity
    }

    /** Song-by-song difference by video, repeats count, order ignored. */
    fun difference(target: List<Song>, reference: List<Song>?): Difference {
        val withoutVideo = target.filter { validId(it.videoId) == null }
        if (reference == null) return Difference(emptyList(), emptyList(), withoutVideo)
        val left = reference.filter { validId(it.videoId) != null }.toMutableList()
        val onlyHere = mutableListOf<Song>()
        target.filter { validId(it.videoId) != null }.forEach { song ->
            val i = left.indexOfFirst { it.videoId == song.videoId }
            if (i >= 0) left.removeAt(i) else onlyHere += song
        }
        return Difference(onlyHere, left, withoutVideo)
    }

    /**
     * Short plain verdict shown on a catalogue card (owner 2026-10-17 00:25:
     * the long wording was cut on a 2-line grid tile and the tap hint
     * «група №N ›» disappeared). The full wording is [verdictExplained],
     * shown in the group window and in «?».
     */
    fun cardVerdict(evidence: PlaylistLibraryDuplicatePolicy.Evidence): String = when (evidence) {
        PlaylistLibraryDuplicatePolicy.Evidence.SAME_TRACKS -> "Копія"
        PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE_COUNT -> "Схоже на копію"
        PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE -> "Інша версія"
    }

    /** What a short card verdict means, in full words. */
    fun verdictExplained(evidence: PlaylistLibraryDuplicatePolicy.Evidence): String =
        when (evidence) {
            PlaylistLibraryDuplicatePolicy.Evidence.SAME_TRACKS -> "Копія — ті самі пісні"
            PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE_COUNT ->
                "Схоже на копію — назва і кількість пісень ті самі, пісні ще не порівняні"
            PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE ->
                "Інша версія — та сама назва, інша кількість пісень"
        }

    /** Tap hint under the verdict: «група №8 (4) ›». */
    fun groupTapHint(hint: PlaylistLibraryDuplicatePolicy.Hint): String =
        "група №${hint.groupNumber} (${hint.groupSize}) ›"

    /**
     * Card line. On a 2-line grid tile the verdict and the tap hint are two
     * separate lines, so the hint is never cut off; in the list — one line.
     */
    fun cardLine(hint: PlaylistLibraryDuplicatePolicy.Hint, tile: Boolean): String =
        cardVerdict(hint.evidence) + (if (tile) "\n" else " • ") + groupTapHint(hint)

    /**
     * YouTube API quota for reading the songs of playlists with these song
     * counts: playlistItems.list costs 1 unit per page of 50, at least 1.
     */
    fun songsCheckQuota(trackCounts: List<Int>): Int =
        trackCounts.sumOf { count -> maxOf(1, (count + 49) / 50) }

    fun songsWord(n: Int): String = plural(n, "пісня", "пісні", "пісень")

    fun playlistsWord(n: Int): String = plural(n, "плейліст", "плейлісти", "плейлістів")

    private fun plural(n: Int, one: String, few: String, many: String): String {
        val mod100 = n % 100
        val mod10 = n % 10
        return when {
            mod100 in 11..14 -> many
            mod10 == 1 -> one
            mod10 in 2..4 -> few
            else -> many
        }
    }
}
