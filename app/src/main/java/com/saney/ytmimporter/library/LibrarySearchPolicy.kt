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

    /**
     * Batch 13 (faric 2026-10-20 22:58: «клавіатура закриває пів екрана, я
     * нічого не бачу»): while the keyboard is open for the search field in the
     * catalogue, the header, the button row and the description lines hide —
     * only the search field, the counters row and the tiles stay.
     */
    fun searchTyping(keyboardOpen: Boolean, searchFocused: Boolean, catalogue: Boolean): Boolean =
        keyboardOpen && searchFocused && catalogue

    /**
     * Keyboard open by the visible window frame (Android < 11): the screen
     * bottom is covered by more than 15 % of the window height — more than a
     * navigation bar, less than any keyboard.
     */
    fun keyboardOpenByFrame(windowHeight: Int, visibleBottom: Int): Boolean =
        windowHeight > 0 && windowHeight - visibleBottom > windowHeight * 15 / 100

    /**
     * Batch 18 (faric 2026-11-05 20:24: «в поле пошуку… хай випадає менюшко…
     * попередні записи… штук 5, щоб видно було, остальні прокручуватися…
     * постійно написую одне й те саме»). Search history kept on the phone.
     */
    const val HISTORY_MAX = 20

    /** Rows visible at once under the field; the rest scrolls. */
    const val HISTORY_VISIBLE = 5

    /** The query goes to the top, without a second copy (any case), at most [max]. */
    fun remember(history: List<String>, query: String, max: Int = HISTORY_MAX): List<String> {
        val q = normalize(query)
        if (q.isEmpty()) return history
        return (listOf(q) + history.filterNot { it.equals(q, ignoreCase = true) }).take(max)
    }

    /** What to offer under the field for the text typed so far. */
    fun suggestions(history: List<String>, typed: String): List<String> {
        val t = normalize(typed)
        if (t.isEmpty()) return history
        return history.filter {
            it.contains(t, ignoreCase = true) && !it.equals(t, ignoreCase = true)
        }
    }

    fun forget(history: List<String>, query: String): List<String> =
        history.filterNot { it.equals(query, ignoreCase = true) }

    fun encodeHistory(history: List<String>): String = history.joinToString("\n")

    fun decodeHistory(raw: String?): List<String> =
        raw.orEmpty().split('\n').map { it.trim() }.filter { it.isNotEmpty() }.take(HISTORY_MAX)
}
