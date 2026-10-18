package com.saney.ytmimporter.library

/**
 * Permanent «група №N» numbers (owner 2026-10-18 04:44: «номер постійний…
 * за ним буде прив'язана проблема до тих пор, поки вона не рішиться»).
 *
 * Before, [PlaylistLibraryDuplicatePolicy.find] numbered groups anew on every
 * screen by the alphabet of all group names, so a deleted copy, a new
 * playlist or YouTube still loading after a pause moved the numbers and the
 * owner could not find «група №30» again.
 *
 * A group is recognised by a key: the alphabetically smallest normalized
 * title among its playlists. A known key keeps its number. New keys get the
 * next number never used before, in alphabetical order, so a number that
 * once meant one album is never given to another. The very first time
 * (empty memory) the current alphabetical numbers are kept as they are.
 */
object GroupNumberPolicy {
    data class Memory(
        val numbers: Map<String, Int> = emptyMap(),
        /** Next number never used before. */
        val next: Int = 1
    )

    data class Result(
        val hints: Map<String, PlaylistLibraryDuplicatePolicy.Hint>,
        val memory: Memory
    )

    const val MAX_KEYS = 3000

    /** Key of every group (by its current ordinal number) of [hints]. */
    fun groupKeys(
        candidates: List<PlaylistLibraryDuplicatePolicy.Candidate>,
        hints: Map<String, PlaylistLibraryDuplicatePolicy.Hint>
    ): Map<Int, String> = candidates
        .mapNotNull { c -> hints[c.identity]?.let { it.groupNumber to c } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, members) ->
            members.map { PlaylistLibraryDuplicatePolicy.normalizedTitle(it.title) }
                .filter { it.isNotBlank() }
                .minOrNull()
                ?: members.map { it.identity }.minOrNull().orEmpty()
        }

    fun renumber(
        candidates: List<PlaylistLibraryDuplicatePolicy.Candidate>,
        hints: Map<String, PlaylistLibraryDuplicatePolicy.Hint>,
        memory: Memory
    ): Result {
        if (hints.isEmpty()) return Result(hints, memory)
        val keys = groupKeys(candidates, hints)
        val numbers = LinkedHashMap(memory.numbers)
        var next = memory.next.coerceAtLeast((numbers.values.maxOrNull() ?: 0) + 1)
        val firstTime = numbers.isEmpty()
        val mapping = HashMap<Int, Int>()
        // Known groups first, then new ones in alphabetical order.
        keys.entries.sortedBy { it.value }.forEach { (ordinal, key) ->
            val known = numbers[key]
            if (known != null) mapping[ordinal] = known
        }
        keys.entries.filter { it.key !in mapping }.sortedBy { it.value }.forEach { (ordinal, key) ->
            val number = if (firstTime) ordinal else next
            numbers[key] = number
            mapping[ordinal] = number
            next = maxOf(next, number + 1)
        }
        // Two different keys must never share a number on one screen.
        val used = HashSet<Int>()
        keys.entries.sortedBy { it.value }.forEach { (ordinal, key) ->
            val number = mapping.getValue(ordinal)
            if (!used.add(number)) {
                mapping[ordinal] = next
                numbers[key] = next
                used += next
                next += 1
            }
        }
        val trimmed = if (numbers.size <= MAX_KEYS) numbers else
            numbers.entries.sortedByDescending { it.value }.take(MAX_KEYS)
                .associate { it.key to it.value }
        val renumbered = hints.mapValues { (_, hint) ->
            hint.copy(groupNumber = mapping[hint.groupNumber] ?: hint.groupNumber)
        }
        return Result(renumbered, Memory(trimmed, next))
    }

    /** Line format: first line `next`, then `number<TAB>key`. */
    fun encode(memory: Memory): String =
        (listOf(memory.next.toString()) +
            memory.numbers.entries.map { "${it.value}\t${it.key.replace('\n', ' ').replace('\t', ' ')}" })
            .joinToString("\n")

    fun decode(text: String?): Memory {
        if (text.isNullOrBlank()) return Memory()
        val lines = text.lines()
        val next = lines.first().trim().toIntOrNull() ?: return Memory()
        val numbers = LinkedHashMap<String, Int>()
        lines.drop(1).forEach { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@forEach
            val number = line.substring(0, tab).toIntOrNull() ?: return@forEach
            val key = line.substring(tab + 1)
            if (number > 0 && key.isNotEmpty()) numbers[key] = number
        }
        return Memory(numbers, maxOf(next, (numbers.values.maxOrNull() ?: 0) + 1))
    }
}
