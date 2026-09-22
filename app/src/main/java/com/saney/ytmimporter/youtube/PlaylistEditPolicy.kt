package com.saney.ytmimporter.youtube

import java.util.Locale

data class PlaylistUpdateMetadata(
    val description: String = "",
    val defaultLanguage: String? = null,
    val tags: List<String> = emptyList(),
    val podcastStatus: String? = null
)

data class PlaylistUpdateSpec(
    val title: String,
    val privacyStatus: String,
    val description: String,
    val defaultLanguage: String?,
    val tags: List<String>,
    val podcastStatus: String?
)

object PlaylistEditPolicy {
    const val MAX_TITLE_LENGTH =
        150

    fun normalizeTitle(
        raw: String
    ): String =
        raw
            .trim()
            .take(
                MAX_TITLE_LENGTH
            )

    fun normalizePrivacy(
        raw: String
    ): String {
        val normalized =
            raw
                .trim()
                .lowercase(
                    Locale.ROOT
                )

        return when (
            normalized
        ) {
            "public",
            "unlisted",
            "private" ->
                normalized

            else ->
                "private"
        }
    }

    fun buildSpec(
        rawTitle: String,
        rawPrivacyStatus: String,
        metadata:
            PlaylistUpdateMetadata
    ): PlaylistUpdateSpec {
        val title =
            normalizeTitle(
                rawTitle
            )

        require(
            title.isNotBlank()
        ) {
            "Назва плейлиста не може бути порожньою"
        }

        return PlaylistUpdateSpec(
            title =
                title,
            privacyStatus =
                normalizePrivacy(
                    rawPrivacyStatus
                ),
            description =
                metadata.description,
            defaultLanguage =
                normalizeOptional(
                    metadata.defaultLanguage
                ),
            tags =
                metadata.tags.toList(),
            podcastStatus =
                normalizeOptional(
                    metadata.podcastStatus
                )
        )
    }

    private fun normalizeOptional(
        value: String?
    ): String? =
        value
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
}
