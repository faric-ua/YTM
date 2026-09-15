package com.saney.ytmimporter.model

data class SearchCandidate(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val score: Double
)

enum class TrackStatus {
    NEW,
    SEARCHING,
    MATCHED,
    REVIEW,
    MISSING,
    SKIPPED,
    PENDING,
    ADDED,
    FAILED
}

data class Track(
    val originalTitle: String,
    val originalArtist: String,
    var selectedVideoId: String? = null,
    var selectedTitle: String? = null,
    var selectedChannel: String? = null,
    var status: TrackStatus = TrackStatus.NEW,
    var candidates: List<SearchCandidate> = emptyList(),
    var manuallySelected: Boolean = false,
    var error: String? = null
) {
    val query: String
        get() = listOf(originalArtist, originalTitle)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}

data class ImportedPlaylist(
    var name: String,
    val tracks: MutableList<Track>
)
