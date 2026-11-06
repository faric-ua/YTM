package com.saney.ytmimporter.library

/**
 * Batch 21 (faric 2026-11-06 01:10–01:25, mockup `docs/mockups/create_description_mock.*`):
 * the description of a new YouTube playlist.
 *
 * - The first line is always the short signature «Створено через YTM Importer»
 *   (YouTube shows only the first line; the rest opens on a tap — faric 01:15:
 *   «не нав'язувати… хто проявить бажання — натисне й почитає»).
 * - Below it, after an empty line, the owner's own description — optional
 *   (a tick, on by default), chosen from a strip of all saved descriptions.
 *   Old descriptions are never deleted; the chosen one («✓ активний») goes
 *   into every new playlist until another one is chosen.
 * - Every description counts how many playlists were created with it
 *   («використано N разів», faric 01:25) — only successful creations.
 *
 * Pure Kotlin: the store keeps [State] in SharedPreferences.
 */
object PlaylistDescriptionPolicy {
    const val SIGNATURE = "Створено через YTM Importer"

    /** YouTube's limit for a playlist description. */
    const val MAX_LENGTH = 5000

    data class Description(
        val id: String,
        val text: String,
        val createdAt: Long,
        val uses: Int = 0
    )

    data class State(
        val descriptions: List<Description> = emptyList(),
        val activeId: String? = null,
        /** The «Опис (необов'язково)» tick. */
        val enabled: Boolean = true
    ) {
        val active: Description? get() = descriptions.firstOrNull { it.id == activeId }
    }

    /** The first description, offered until the owner writes his own. */
    const val FIRST_TEXT =
        "Застосунок для Android, безкоштовний: перетворює списки пісень (файл CSV або TXT, " +
            "текст, посилання на плейліст, резервна копія) на плейлісти YouTube Music; " +
            "кожну пісню можна перевірити перед записом. Не пов'язаний з YouTube / Google. " +
            "Докладніше: https://github.com/faric-ua/YTM"

    fun seeded(state: State, now: Long): State =
        if (state.descriptions.isNotEmpty()) state
        else state.copy(descriptions = listOf(Description("d1", FIRST_TEXT, now)), activeId = "d1")

    /** YouTube does not accept «<» and «>» in a description. */
    fun clean(text: String): String =
        text.replace("<", "‹").replace(">", "›").replace("\r\n", "\n").trim()

    /** The text sent to YouTube for a new playlist. */
    fun forNewPlaylist(state: State): String {
        val own = state.active?.text?.let(::clean)?.takeIf { state.enabled && it.isNotEmpty() }
            ?: return SIGNATURE
        return (SIGNATURE + "\n\n" + own).take(MAX_LENGTH)
    }

    /** Which description the new playlist used (for the counter), or null. */
    fun usedId(state: State): String? =
        state.active?.takeIf { state.enabled && clean(it.text).isNotEmpty() }?.id

    fun add(state: State, text: String, now: Long): State {
        val cleaned = clean(text)
        if (cleaned.isEmpty()) return state
        val id = "d" + ((state.descriptions.mapNotNull { it.id.drop(1).toIntOrNull() }.maxOrNull() ?: 0) + 1)
        return state.copy(descriptions = state.descriptions + Description(id, cleaned, now),
            activeId = id, enabled = true)
    }

    fun edit(state: State, id: String, text: String): State {
        val cleaned = clean(text)
        if (cleaned.isEmpty()) return state
        return state.copy(descriptions = state.descriptions.map {
            if (it.id == id) it.copy(text = cleaned) else it
        })
    }

    fun activate(state: State, id: String): State =
        if (state.descriptions.any { it.id == id }) state.copy(activeId = id, enabled = true) else state

    fun markUsed(state: State, id: String?): State =
        if (id == null) state else state.copy(descriptions = state.descriptions.map {
            if (it.id == id) it.copy(uses = it.uses + 1) else it
        })

    /** «№3» — the number of a description in the strip. */
    fun number(state: State, id: String): Int = state.descriptions.indexOfFirst { it.id == id } + 1

    /** «використано 3 рази» */
    fun usesLine(uses: Int): String {
        val mod10 = uses % 10
        val mod100 = uses % 100
        val word = when {
            mod10 == 1 && mod100 != 11 -> "раз"
            mod10 in 2..4 && mod100 !in 12..14 -> "рази"
            else -> "разів"
        }
        return if (uses == 0) "ще не використано" else "використано $uses $word"
    }

    /** First line of a description, for the folded view. */
    fun firstLine(text: String, max: Int = 90): String {
        val line = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        return if (line.length <= max) line else line.take(max - 1).trimEnd() + "…"
    }
}
