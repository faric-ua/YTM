package com.saney.ytmimporter.library

/**
 * Library batch 10 (faric 2026-10-19 21:05: after a long time in other apps
 * the Library came back «not where I left it»). Android had closed the app in
 * the background; the screen was rebuilt from saved state, but:
 * - the catalogue always started from the top (the scroll view had no id and
 *   only the position from the moment a playlist was opened was saved);
 * - the «Група №N» window silently did not come back when the group has a
 *   YouTube playlist, because the YouTube list was not read again yet.
 *
 * Pure rules, so they are checked by JVM tests.
 */
object LibraryRestorePolicy {
    /**
     * The catalogue place is remembered as «this card, this far down it»,
     * not as pixels: after a restore YouTube cards arrive later and, with
     * «Нові», are mixed between the phone cards, so pixels point elsewhere.
     */
    data class Anchor(val identity: String, val offset: Int)

    /** A card of the catalogue: top and bottom inside the scrolled content. */
    data class CardBox(val identity: String, val top: Int, val bottom: Int)

    /**
     * The first card still (partly) on screen at [scrollY]. Null at the very
     * top of the list — the list then simply starts from the top again.
     */
    fun anchorAt(cards: List<CardBox>, scrollY: Int): Anchor? {
        if (scrollY <= 0) return null
        val card = cards.filter { it.bottom > scrollY }
            .minWithOrNull(compareBy<CardBox> { it.top }) ?: return null
        return Anchor(card.identity, scrollY - card.top)
    }

    /** Where to scroll so the anchor card is in the same screen place; null = card not shown. */
    fun scrollFor(anchor: Anchor, cards: List<CardBox>): Int? =
        cards.firstOrNull { it.identity == anchor.identity }
            ?.let { maxOf(0, it.top + anchor.offset) }

    /**
     * YouTube cards will still arrive in this visit: Google is being restored
     * silently or the list of YouTube playlists is being read.
     */
    fun youTubeStillComing(
        showsRemote: Boolean,
        restoringGoogle: Boolean,
        loadingList: Boolean
    ): Boolean = showsRemote && (restoringGoogle || loadingList)

    /**
     * Keep putting the catalogue back at the anchor after each redraw until
     * the YouTube part is there — unless the owner has scrolled himself in
     * the meantime (then his own place wins).
     */
    fun keepAnchor(stillComing: Boolean, userMoved: Boolean): Boolean =
        stillComing && !userMoved

    /** The owner scrolled away from where the anchor put the list. */
    fun userMoved(currentY: Int, lastAppliedY: Int?, slop: Int): Boolean =
        lastAppliedY != null && kotlin.math.abs(currentY - lastAppliedY) > slop

    /**
     * A restored window that needs the YouTube list («Група №N», «Чим
     * відрізняється», «Дублікати за піснями», removal from YouTube) waits
     * for the list instead of silently not opening.
     */
    fun waitForYouTube(needsYouTube: Boolean, stillComing: Boolean): Boolean =
        needsYouTube && stillComing
}
