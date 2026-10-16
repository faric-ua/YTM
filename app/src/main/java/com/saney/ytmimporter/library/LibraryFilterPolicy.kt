package com.saney.ytmimporter.library

/**
 * Pure rules for the Playlist Library filter window and Google restore.
 *
 * Owner decision 2026-10-16 00:43 (phone recording): YouTube privacy options
 * (public / private / unlisted) make sense only when the source is
 * "Тільки YouTube". With "Всі" they silently hid some YouTube cards while
 * phone and History cards stayed, which was confusing.
 */
object LibraryFilterPolicy {
    enum class Source { ALL, PHONE, YOUTUBE }

    enum class Privacy { ALL, PUBLIC, PRIVATE, UNLISTED }

    fun source(showAll: Boolean, onlineTab: Boolean): Source = when {
        showAll -> Source.ALL
        onlineTab -> Source.YOUTUBE
        else -> Source.PHONE
    }

    /** The privacy section is offered only for the YouTube-only source. */
    fun privacyAvailable(source: Source): Boolean = source == Source.YOUTUBE

    /**
     * Privacy that stays in effect after the source changed. Leaving
     * YouTube-only resets it, so no hidden filter keeps working unseen.
     */
    fun privacyAfterSourceChange(newSource: Source, current: Privacy): Privacy =
        if (privacyAvailable(newSource)) current else Privacy.ALL

    /** Privacy actually applied to YouTube cards (defensive guard). */
    fun effectivePrivacy(source: Source, selected: Privacy): Privacy =
        if (privacyAvailable(source)) selected else Privacy.ALL

    /**
     * BUG-052: should the Library try a silent Google restore now?
     * Only when YouTube is shown, there is no token, the device was
     * authorized before, nothing is running, and this screen visit has not
     * already been told that Google needs the user (no prompt loops).
     */
    fun shouldRestoreGoogle(
        showsRemote: Boolean,
        hasToken: Boolean,
        hadPriorAuthorization: Boolean,
        restoring: Boolean,
        userActionRequired: Boolean
    ): Boolean = showsRemote && !hasToken && hadPriorAuthorization &&
        !restoring && !userActionRequired

    /*
     * «Нові» sort (BACKLOG «Remote date-sort wording», batch 3 2026-10-17):
     * before, YouTube cards had no date at all — they fell to the end in
     * name order while the label said «Нові (локально)». Now YouTube cards
     * use the playlist creation date on YouTube (snippet.publishedAt — it
     * is already in the playlists.list answer, no extra quota) and the label
     * says honestly which date is used for which source.
     */

    /** Short sort part of the pinned scope line. */
    fun recentScopeLabel(source: Source): String = when (source) {
        Source.PHONE -> "Нові (збережено на телефоні)"
        Source.YOUTUBE -> "Нові (створено на YouTube)"
        Source.ALL -> "Нові (телефон — збережено, YouTube — створено)"
    }

    /** Hint under the «Нові спочатку» option in the filter window. */
    fun recentOptionHint(source: Source): String = when (source) {
        Source.PHONE -> "За датою збереження на телефоні"
        Source.YOUTUBE -> "За датою створення плейліста на YouTube"
        Source.ALL -> "Телефон — за датою збереження, YouTube — за датою " +
            "створення на YouTube"
    }

    /**
     * YouTube `publishedAt` (RFC 3339, e.g. 2021-03-04T12:34:56Z or with
     * fractions / offset) → epoch millis; 0 when absent or unreadable,
     * so such a card goes to the end instead of breaking the list.
     */
    fun remoteDateMillis(publishedAt: String?): Long {
        val text = publishedAt?.trim().orEmpty()
        if (text.isEmpty()) return 0L
        return runCatching { java.time.OffsetDateTime.parse(text).toInstant().toEpochMilli() }
            .getOrDefault(0L)
            .coerceAtLeast(0L)
    }
}
