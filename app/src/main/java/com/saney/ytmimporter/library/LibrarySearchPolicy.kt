package com.saney.ytmimporter.library

/**
 * Library batch 7 (owner 2026-10-18 14:21: «момент пошуку дуже сильно
 * тормозить»). Search was always local; the screen was slow because every
 * typed letter re-read all stores, re-ran the duplicate analysis and rebuilt
 * every card on the main thread. Pure rules kept here so they are tested
 * without Android.
 */
object LibrarySearchPolicy {
    /** Redraw once the owner stops typing, not on every letter. */
    const val TYPING_PAUSE_MS = 300L

    /** Voice/keyboard often leaves a space at the end; it must not hide results. */
    fun normalize(query: String): String = query.trim()

    /** Empty query shows everything; otherwise title or subtitle, any case. */
    fun matches(title: String, subtitle: String, query: String): Boolean {
        val q = normalize(query)
        if (q.isEmpty()) return true
        return title.contains(q, ignoreCase = true) ||
            subtitle.contains(q, ignoreCase = true)
    }

    /**
     * A search redraw may reuse the stores/duplicate analysis of the last full
     * redraw only when the YouTube side is still the same object and the
     * Google connection did not appear or disappear in between.
     */
    fun canReuse(
        searchOnly: Boolean,
        hasCache: Boolean,
        sameRemoteList: Boolean,
        sameConnection: Boolean
    ): Boolean = searchOnly && hasCache && sameRemoteList && sameConnection

    private const val MIN_COVER_CACHE = 8 * 1024 * 1024
    private const val MAX_COVER_CACHE = 48 * 1024 * 1024

    /**
     * Decoded covers kept in memory: 1/8 of the app heap, 8–48 MB. The old
     * cache held 24 pictures, so with ~135 playlists most covers were decoded
     * from disk again on every redraw. A YouTube cover (320×180) is ~230 KB.
     */
    fun coverCacheBytes(maxHeapBytes: Long): Int =
        (maxHeapBytes / 8).coerceIn(MIN_COVER_CACHE.toLong(), MAX_COVER_CACHE.toLong()).toInt()
}
