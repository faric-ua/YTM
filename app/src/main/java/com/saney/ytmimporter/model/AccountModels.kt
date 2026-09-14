package com.saney.ytmimporter.model

data class GoogleAccountInfo(
    val name: String,
    val email: String
)

data class YouTubeChannelInfo(
    val id: String,
    val title: String
)

data class YouTubePlaylistInfo(
    val id: String,
    val title: String,
    val privacyStatus: String,
    val itemCount: Long
)
