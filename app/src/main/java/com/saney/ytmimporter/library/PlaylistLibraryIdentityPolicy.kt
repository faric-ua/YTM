package com.saney.ytmimporter.library

import com.saney.ytmimporter.model.YouTubePlaylistInfo

/** Read-only exact-ID reference against the active account's fetched catalogue.
 * Same playlistId is NOT proof of equal tracks, account ownership or sync.
 * Never infer identity from title, cover, track count or a stale account list.
 */
object PlaylistLibraryIdentityPolicy {
    fun exactRemoteMatches(
        local: List<LocalLibraryItem>,
        activeAccountRemote: List<YouTubePlaylistInfo>
    ): Map<String, List<String>> {
        val ids = activeAccountRemote.map { it.id }.filter(String::isNotBlank).toSet()
        if (ids.isEmpty()) return emptyMap()
        return local.mapNotNull { item ->
            val id = item.destinationPlaylistId?.takeIf {
                it.isNotBlank() && it in ids
            } ?: return@mapNotNull null
            id to item.identity
        }.groupBy({ it.first }, { it.second })
    }

    /** A phone copy or History record that points to a YouTube playlist. */
    data class LinkedLocal(
        val identity: String,
        val playlistId: String?,
        /** YouTube channel the link was made with, when known (History). */
        val channelId: String? = null
    )

    /**
     * «Safe link check with YouTube», step 1 (read-only, owner question
     * 2026-10-16 03:45, «так» 2026-10-17 20:49): which linked phone / History
     * entries point to a playlist that is NOT in the account's YouTube list.
     * Marks nothing unless the list was loaded completely, and skips entries
     * known to belong to another channel. Never deletes or unlinks anything.
     */
    fun missingOnYouTube(
        linked: List<LinkedLocal>,
        remoteIds: Set<String>,
        catalogueComplete: Boolean,
        currentChannelId: String?
    ): Set<String> {
        if (!catalogueComplete) return emptySet()
        return linked.filter { item ->
            val id = item.playlistId?.takeIf { it.isNotBlank() } ?: return@filter false
            val sameAccount = item.channelId.isNullOrBlank() ||
                currentChannelId.isNullOrBlank() || item.channelId == currentChannelId
            sameAccount && id !in remoteIds
        }.map { it.identity }.toSet()
    }

    /** YouTubeApi.listMyPlaylists stops after 20 pages of 50. */
    const val CATALOGUE_PAGE_LIMIT_ITEMS = 1000

    fun catalogueComplete(loaded: List<YouTubePlaylistInfo>?): Boolean =
        loaded != null && loaded.size < CATALOGUE_PAGE_LIMIT_ITEMS
}
