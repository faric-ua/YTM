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
}
