package com.saney.ytmimporter.write

/**
 * Batch 19 (faric 2026-11-05 23:34, «роздратований»): a song added to an
 * existing playlist went to the end (№50) instead of its own place (№30) —
 * «щоб порядкові номера відповідали їх позиції».
 *
 * YouTube `playlistItems.insert` takes `snippet.position` (0-based) at the
 * same cost. This policy works out, for every song that is going to be
 * written, where it belongs: right after the nearest earlier song of the list
 * that is already in the YouTube playlist (or was just added before it), or
 * at the very top when no earlier song is there. Inserts are simulated one by
 * one in list order, so each position is valid at the moment it is written.
 *
 * A position equal to the end of the playlist is returned as -1 («just
 * append», no `position` sent): a plain append never depends on the
 * playlist's sort mode.
 */
object PlaylistInsertPositionPolicy {
    const val APPEND = -1

    /**
     * @param order video IDs of the selected songs in list order (null = no video)
     * @param write which of them are going to be written
     * @param existing video IDs of the YouTube playlist, in its order
     * @return for every selected song: the position to insert at, or [APPEND]
     *   (also for songs that are not written)
     */
    fun positions(
        order: List<String?>,
        write: List<Boolean>,
        existing: List<String>
    ): IntArray {
        require(order.size == write.size) { "order and write must have the same size" }
        val current = existing.toMutableList()
        val result = IntArray(order.size) { APPEND }
        for (i in order.indices) {
            val id = order[i]?.trim().orEmpty()
            if (!write[i] || id.isEmpty()) continue
            var position = 0
            for (j in i - 1 downTo 0) {
                val anchor = order[j]?.trim().orEmpty()
                if (anchor.isEmpty()) continue
                val at = current.indexOf(anchor)
                if (at >= 0) {
                    position = at + 1
                    break
                }
            }
            result[i] = if (position >= current.size) APPEND else position
            current.add(position.coerceAtMost(current.size), id)
        }
        return result
    }

    /** YouTube reasons meaning «this playlist does not accept a position». */
    val positionRefusedReasons: Set<String> = setOf(
        "manualSortRequired",
        "invalidPlaylistItemPosition"
    )
}
