package com.saney.ytmimporter.urlsnapshot

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

enum class UrlSnapshotSurface {
    YOUTUBE,
    YOUTUBE_MUSIC
}

enum class UrlSnapshotSourceKind {
    CONCRETE_PLAYLIST,
    DYNAMIC_MIX
}

enum class UrlSnapshotParseError {
    BLANK_INPUT,
    MALFORMED_URL,
    UNSUPPORTED_SCHEME,
    UNSUPPORTED_HOST,
    UNSUPPORTED_PATH,
    MISSING_PLAYLIST_ID,
    AMBIGUOUS_PLAYLIST_ID,
    INVALID_PLAYLIST_ID
}

data class UrlSnapshotSource(
    val originalUrl: String,
    val canonicalUrl: String,
    val playlistId: String,
    val kind: UrlSnapshotSourceKind,
    val surface: UrlSnapshotSurface,
    val contextVideoId: String?
)

sealed class UrlSnapshotParseResult {
    data class Supported(
        val source: UrlSnapshotSource
    ) : UrlSnapshotParseResult()

    data class Unsupported(
        val error: UrlSnapshotParseError,
        val detail: String
    ) : UrlSnapshotParseResult()
}

object UrlSnapshotSourceParser {
    private val playlistIdPattern =
        Regex("^[A-Za-z0-9_-]{2,200}$")

    private val videoIdPattern =
        Regex("^[A-Za-z0-9_-]{11}$")

    private val youtubeHosts =
        setOf(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com"
        )

    private val musicHosts =
        setOf(
            "music.youtube.com"
        )

    private val shortHosts =
        setOf(
            "youtu.be",
            "www.youtu.be"
        )

    fun parse(
        rawValue: String
    ): UrlSnapshotParseResult {
        val original =
            rawValue.trim()

        if (original.isBlank()) {
            return unsupported(
                UrlSnapshotParseError.BLANK_INPUT,
                "URL is blank"
            )
        }

        val normalizedInput =
            normalizeMissingScheme(
                original
            )

        val uri =
            try {
                URI(normalizedInput)
            } catch (_: Exception) {
                return unsupported(
                    UrlSnapshotParseError.MALFORMED_URL,
                    "URL cannot be parsed"
                )
            }

        val scheme =
            uri.scheme
                ?.lowercase()
                .orEmpty()

        if (
            scheme != "https" &&
            scheme != "http"
        ) {
            return unsupported(
                UrlSnapshotParseError.UNSUPPORTED_SCHEME,
                "Only http/https YouTube URLs are accepted"
            )
        }

        val host =
            uri.host
                ?.lowercase()
                .orEmpty()

        val surface =
            when {
                host in musicHosts ->
                    UrlSnapshotSurface.YOUTUBE_MUSIC

                host in youtubeHosts ||
                    host in shortHosts ->
                    UrlSnapshotSurface.YOUTUBE

                else ->
                    return unsupported(
                        UrlSnapshotParseError.UNSUPPORTED_HOST,
                        "Host is not an accepted YouTube/YouTube Music host"
                    )
            }

        val path =
            normalizePath(
                uri.path
            )

        if (!isSupportedPath(host, path)) {
            return unsupported(
                UrlSnapshotParseError.UNSUPPORTED_PATH,
                "URL path is not a supported playlist-context form"
            )
        }

        val query =
            try {
                parseQuery(
                    uri.rawQuery
                )
            } catch (_: IllegalArgumentException) {
                return unsupported(
                    UrlSnapshotParseError.MALFORMED_URL,
                    "URL query cannot be decoded"
                )
            }

        val playlistIds =
            query["list"]
                .orEmpty()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()

        if (playlistIds.isEmpty()) {
            return unsupported(
                UrlSnapshotParseError.MISSING_PLAYLIST_ID,
                "Supported source URLs must contain exactly one list parameter"
            )
        }

        if (playlistIds.size != 1) {
            return unsupported(
                UrlSnapshotParseError.AMBIGUOUS_PLAYLIST_ID,
                "Multiple different list parameters are not accepted"
            )
        }

        val playlistId =
            playlistIds.single()

        if (!playlistIdPattern.matches(playlistId)) {
            return unsupported(
                UrlSnapshotParseError.INVALID_PLAYLIST_ID,
                "Playlist ID contains unsupported characters or length"
            )
        }

        val contextVideoId =
            extractContextVideoId(
                host = host,
                path = path,
                query = query
            )

        val kind =
            if (playlistId.startsWith("RD")) {
                UrlSnapshotSourceKind.DYNAMIC_MIX
            } else {
                UrlSnapshotSourceKind.CONCRETE_PLAYLIST
            }

        val canonicalHost =
            when (surface) {
                UrlSnapshotSurface.YOUTUBE ->
                    "www.youtube.com"

                UrlSnapshotSurface.YOUTUBE_MUSIC ->
                    "music.youtube.com"
            }

        return UrlSnapshotParseResult.Supported(
            UrlSnapshotSource(
                originalUrl = original,
                canonicalUrl =
                    "https://$canonicalHost/playlist?list=$playlistId",
                playlistId = playlistId,
                kind = kind,
                surface = surface,
                contextVideoId = contextVideoId
            )
        )
    }

    private fun normalizeMissingScheme(
        value: String
    ): String {
        if ("://" in value) {
            return value
        }

        val lower =
            value.lowercase()

        val recognizedPrefix =
            (youtubeHosts + musicHosts + shortHosts)
                .any { host ->
                    lower == host ||
                        lower.startsWith("$host/") ||
                        lower.startsWith("$host?")
                }

        return if (recognizedPrefix) {
            "https://$value"
        } else {
            value
        }
    }

    private fun normalizePath(
        rawPath: String?
    ): String {
        val path =
            rawPath
                .orEmpty()
                .ifBlank { "/" }

        if (path == "/") {
            return path
        }

        return path.trimEnd('/')
    }

    private fun isSupportedPath(
        host: String,
        path: String
    ): Boolean {
        if (host in shortHosts) {
            val video =
                path
                    .removePrefix("/")
                    .substringBefore('/')

            return videoIdPattern.matches(video)
        }

        if (host in musicHosts) {
            return path == "/playlist" ||
                path == "/watch"
        }

        if (host in youtubeHosts) {
            return path == "/playlist" ||
                path == "/watch" ||
                path == "/embed/videoseries" ||
                path.startsWith("/shorts/") ||
                path.startsWith("/live/")
        }

        return false
    }

    private fun parseQuery(
        rawQuery: String?
    ): Map<String, List<String>> {
        if (rawQuery.isNullOrBlank()) {
            return emptyMap()
        }

        val result =
            linkedMapOf<String, MutableList<String>>()

        rawQuery
            .split('&')
            .filter(String::isNotBlank)
            .forEach { part ->
                val pieces =
                    part.split(
                        '=',
                        limit = 2
                    )

                val key =
                    decode(
                        pieces[0]
                    )

                val value =
                    if (pieces.size == 2) {
                        decode(
                            pieces[1]
                        )
                    } else {
                        ""
                    }

                result
                    .getOrPut(key) {
                        mutableListOf()
                    }
                    .add(value)
            }

        return result
    }

    private fun decode(
        value: String
    ): String =
        URLDecoder.decode(
            value,
            StandardCharsets.UTF_8.name()
        )

    private fun extractContextVideoId(
        host: String,
        path: String,
        query: Map<String, List<String>>
    ): String? {
        val pathVideo =
            when {
                host in shortHosts ->
                    path
                        .removePrefix("/")
                        .substringBefore('/')

                path.startsWith("/shorts/") ->
                    path.removePrefix("/shorts/")
                        .substringBefore('/')

                path.startsWith("/live/") ->
                    path.removePrefix("/live/")
                        .substringBefore('/')

                else ->
                    null
            }
                ?.takeIf(
                    videoIdPattern::matches
                )

        if (pathVideo != null) {
            return pathVideo
        }

        return query["v"]
            .orEmpty()
            .map(String::trim)
            .firstOrNull(
                videoIdPattern::matches
            )
    }

    private fun unsupported(
        error: UrlSnapshotParseError,
        detail: String
    ): UrlSnapshotParseResult.Unsupported =
        UrlSnapshotParseResult.Unsupported(
            error = error,
            detail = detail
        )
}
