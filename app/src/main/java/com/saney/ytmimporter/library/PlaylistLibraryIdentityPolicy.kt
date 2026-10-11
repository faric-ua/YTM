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
}
