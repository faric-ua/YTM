package com.saney.ytmimporter.model

object LocalPlaylistEditPolicy {
    fun normalizeName(
        raw: String
    ): String? =
        raw
            .trim()
            .takeIf {
                it.isNotBlank()
            }
}
