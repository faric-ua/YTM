package com.saney.ytmimporter.library

/**
 * Only canonical YouTube video IDs are accepted, never arbitrary web URLs.
 * The page is Google's unmodified, full YouTube iframe player; no stream
 * extraction, audio-only playback or ad/control hiding.
 */
object YouTubeEmbedPolicy {
    private val videoIdFormat = Regex("^[A-Za-z0-9_-]{11}$")

    fun isValidVideoId(id: String?): Boolean =
        id != null && videoIdFormat.matches(id)

    fun embedUrl(id: String): String {
        require(isValidVideoId(id)) { "Invalid YouTube video ID" }
        return "https://www.youtube.com/embed/$id?playsinline=1&autoplay=0&controls=1"
    }

    fun watchUrl(id: String): String {
        require(isValidVideoId(id)) { "Invalid YouTube video ID" }
        return "https://www.youtube.com/watch?v=$id"
    }

    // Official YouTube player policy requires app identity as HTTPS Referer.
    fun referer(packageId: String): String {
        require(Regex("^[a-zA-Z0-9_.]+$").matches(packageId))
        return "https://$packageId/"
    }
}
