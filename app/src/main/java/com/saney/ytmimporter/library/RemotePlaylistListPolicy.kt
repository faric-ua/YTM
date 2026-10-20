package com.saney.ytmimporter.library

import com.saney.ytmimporter.model.YouTubePlaylistInfo
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Library batch 11 — the list of the account's YouTube playlists kept on the
 * phone (faric 2026-10-20 05:55, «план Б»: after a long pause the Library
 * showed the catalogue for ~3 s while Google was restored and the list read
 * again; «хай вони зберігаються і показуються миттєво… а що там в фоні
 * зробиться і потихенько поміняється — погоди не зіграє»).
 *
 * Kept: id, name, privacy, number of songs, cover link, creation date — per
 * account, only the last account. Never a token, never songs. The saved list
 * is shown at once and replaced by the fresh one read in the background.
 * Until then nothing is deleted from YouTube (the delete needs the fresh list).
 */
object RemotePlaylistListPolicy {
    data class Saved(
        /** YouTube channel id (or Google e-mail when the channel is unknown). */
        val owner: String,
        val savedAt: Long,
        val playlists: List<YouTubePlaylistInfo>
    )

    private const val HEADER = "v1"
    private const val UTF8 = "UTF-8"

    private fun enc(value: String): String = URLEncoder.encode(value, UTF8)
    private fun dec(value: String): String = URLDecoder.decode(value, UTF8)

    fun encode(saved: Saved): String = buildString {
        append(listOf(HEADER, enc(saved.owner), saved.savedAt.toString()).joinToString("\t"))
        saved.playlists.forEach { p ->
            append('\n')
            append(listOf(
                enc(p.id), enc(p.title), enc(p.privacyStatus), p.itemCount.toString(),
                enc(p.thumbnailUrl.orEmpty()), p.publishedAtMillis.toString()
            ).joinToString("\t"))
        }
    }

    /** Null for nothing saved or a damaged header; damaged lines are skipped. */
    fun decode(text: String?): Saved? {
        if (text.isNullOrBlank()) return null
        val lines = text.lines()
        val head = lines.first().split('\t')
        if (head.size != 3 || head[0] != HEADER) return null
        val owner = runCatching { dec(head[1]) }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val savedAt = head[2].toLongOrNull() ?: return null
        val playlists = lines.drop(1).mapNotNull { line ->
            val p = line.split('\t')
            if (p.size != 6) return@mapNotNull null
            runCatching {
                YouTubePlaylistInfo(
                    id = dec(p[0]).takeIf { it.isNotBlank() } ?: return@mapNotNull null,
                    title = dec(p[1]),
                    privacyStatus = dec(p[2]),
                    itemCount = p[3].toLongOrNull()?.takeIf { it >= 0 } ?: return@mapNotNull null,
                    thumbnailUrl = dec(p[4]).ifBlank { null },
                    publishedAtMillis = p[5].toLongOrNull() ?: 0L
                )
            }.getOrNull()
        }
        return Saved(owner, savedAt, playlists)
    }

    /**
     * May the saved list be shown now? Only after a successful Google sign-in
     * on this phone (signing out clears it); with a token — only for the same
     * account; without a token (Google still being restored) — the last one.
     */
    fun usable(
        saved: Saved?,
        hadAuthorization: Boolean,
        hasToken: Boolean,
        currentOwner: String?
    ): Boolean = saved != null && hadAuthorization &&
        (!hasToken || (!currentOwner.isNullOrBlank() && currentOwner == saved.owner))

    /** Read the fresh list now: connected, and the list is missing or only saved. */
    fun needsRefresh(
        hasToken: Boolean,
        hasList: Boolean,
        fromMemory: Boolean,
        loading: Boolean
    ): Boolean = hasToken && !loading && (!hasList || fromMemory)

    /** The saved list after a playlist was deleted from YouTube. */
    fun without(saved: Saved, playlistId: String): Saved =
        saved.copy(playlists = saved.playlists.filterNot { it.id == playlistId })

    /** Windows that need the YouTube list wait only when there is none at all. */
    fun windowMustWait(needsYouTube: Boolean, hasList: Boolean, stillComing: Boolean): Boolean =
        needsYouTube && !hasList && stillComing
}
