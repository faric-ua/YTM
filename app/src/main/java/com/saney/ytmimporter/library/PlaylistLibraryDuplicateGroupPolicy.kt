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
        return "${overview.size} ${playlistsWord(overview.size)} • " + parts.joinToString(" • ")
    }

    /** Short plain verdict shown on a catalogue card. */
    fun cardVerdict(evidence: PlaylistLibraryDuplicatePolicy.Evidence): String = when (evidence) {
        PlaylistLibraryDuplicatePolicy.Evidence.SAME_TRACKS -> "Копія: ті самі пісні"
        PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE_COUNT ->
            "Схоже на копію: назва і кількість ті самі"
        PlaylistLibraryDuplicatePolicy.Evidence.SAME_TITLE ->
            "Інша версія: та сама назва, інша кількість пісень"
    }

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
