package com.saney.ytmimporter.library

/**
 * Remembers which videos a YouTube playlist had when its songs were read
 * (owner 2026-10-17 15:46: «так» — before, the result of «Перевірити пісні
 * YouTube» was lost when Android closed the app, and a checked «Копія» turned
 * back into «Схоже на копію»).
 *
 * Kept on the phone in app-private storage, per account: only video IDs, the
 * playlist's song count at that moment and the date. No titles, no token.
 * A remembered list is used only while the playlist still has the same number
 * of songs on YouTube; otherwise it is ignored (the playlist changed).
 *
 * Plain line format (no JSON, testable on the JVM):
 * `playlistId<TAB>itemCount<TAB>checkedAtMillis<TAB>idCount<TAB>id1,id2,…`
 * (an empty id = a song without a video; idCount keeps `[""]` and `[]` apart)
 */
object RemoteSongMemoryPolicy {
    data class Entry(
        val itemCount: Int,
        val videoIds: List<String>,
        val checkedAt: Long
    )

    const val MAX_PLAYLISTS = 500

    private val safeId = Regex("^[A-Za-z0-9_-]*$")

    fun encode(memory: Map<String, Entry>): String =
        memory.entries.joinToString("\n") { (id, entry) ->
            listOf(
                id,
                entry.itemCount.toString(),
                entry.checkedAt.toString(),
                entry.videoIds.size.toString(),
                entry.videoIds.joinToString(",")
            ).joinToString("\t")
        }

    /** Malformed lines are skipped, never thrown. */
    fun decode(text: String?): Map<String, Entry> {
        if (text.isNullOrBlank()) return emptyMap()
        val result = LinkedHashMap<String, Entry>()
        text.lineSequence().forEach { line ->
            val parts = line.split('\t')
            if (parts.size != 5) return@forEach
            val id = parts[0]
            val count = parts[1].toIntOrNull() ?: return@forEach
            val at = parts[2].toLongOrNull() ?: return@forEach
            if (id.isBlank() || !safeId.matches(id) || count < 0) return@forEach
            val size = parts[3].toIntOrNull() ?: return@forEach
            val ids = if (size == 0) emptyList() else parts[4].split(',')
            if (ids.size != size || !ids.all(safeId::matches)) return@forEach
            result[id] = Entry(count, ids, at)
        }
        return result
    }

    /**
     * Adds or replaces one playlist. Video IDs that cannot be stored safely
     * become empty (= «no video»). Keeps at most [MAX_PLAYLISTS], dropping the
     * oldest checks first.
     */
    fun remember(
        memory: Map<String, Entry>,
        playlistId: String,
        itemCount: Long,
        videoIds: List<String?>,
        now: Long
    ): Map<String, Entry> {
        if (playlistId.isBlank() || !safeId.matches(playlistId)) return memory
        val ids = videoIds.map { id -> id?.takeIf { safeId.matches(it) }.orEmpty() }
        val count = itemCount.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val updated = LinkedHashMap(memory)
        updated.remove(playlistId)
        updated[playlistId] = Entry(count, ids, now)
        if (updated.size <= MAX_PLAYLISTS) return updated
        return updated.entries
            .sortedByDescending { it.value.checkedAt }
            .take(MAX_PLAYLISTS)
            .associate { it.key to it.value }
    }

    /**
     * Remembered video IDs, only while the playlist still has the same number
     * of songs on YouTube as when it was checked.
     */
    fun usable(entry: Entry?, currentItemCount: Long): Entry? =
        entry?.takeIf { it.itemCount.toLong() == currentItemCount }
}
