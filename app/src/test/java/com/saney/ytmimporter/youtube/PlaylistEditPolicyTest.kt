package com.saney.ytmimporter.youtube

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistEditPolicyTest {
    @Test
    fun normalizeTitle_trimsAndCapsAt150Characters() {
        val raw =
            "  " +
                "x".repeat(
                    170
                ) +
                "  "

        val result =
            PlaylistEditPolicy
                .normalizeTitle(
                    raw
                )

        assertEquals(
            150,
            result.length
        )

        assertEquals(
            "x".repeat(
                150
            ),
            result
        )
    }

    @Test
    fun normalizePrivacy_preservesSupportedValues() {
        listOf(
            "public",
            "unlisted",
            "private"
        ).forEach {
            value ->
            assertEquals(
                value,
                PlaylistEditPolicy
                    .normalizePrivacy(
                        value
                    )
            )
        }
    }

    @Test
    fun normalizePrivacy_invalidValueFallsBackToPrivate() {
        assertEquals(
            "private",
            PlaylistEditPolicy
                .normalizePrivacy(
                    "something-else"
                )
        )

        assertEquals(
            "public",
            PlaylistEditPolicy
                .normalizePrivacy(
                    "  PUBLIC  "
                )
        )
    }

    @Test(
        expected =
            IllegalArgumentException::class
    )
    fun buildSpec_rejectsBlankTitle() {
        PlaylistEditPolicy
            .buildSpec(
                rawTitle =
                    "   ",
                rawPrivacyStatus =
                    "public",
                metadata =
                    PlaylistUpdateMetadata()
            )
    }

    @Test
    fun buildSpec_preservesExistingMetadata() {
        val spec =
            PlaylistEditPolicy
                .buildSpec(
                    rawTitle =
                        "  New title  ",
                    rawPrivacyStatus =
                        "unlisted",
                    metadata =
                        PlaylistUpdateMetadata(
                            description =
                                "Existing description",
                            defaultLanguage =
                                "uk",
                            tags =
                                listOf(
                                    "classic",
                                    "house"
                                ),
                            podcastStatus =
                                "enabled"
                        )
                )

        assertEquals(
            "New title",
            spec.title
        )

        assertEquals(
            "unlisted",
            spec.privacyStatus
        )

        assertEquals(
            "Existing description",
            spec.description
        )

        assertEquals(
            "uk",
            spec.defaultLanguage
        )

        assertEquals(
            listOf(
                "classic",
                "house"
            ),
            spec.tags
        )

        assertEquals(
            "enabled",
            spec.podcastStatus
        )
    }

    @Test
    fun buildSpec_omitsBlankOptionalMetadata() {
        val spec =
            PlaylistEditPolicy
                .buildSpec(
                    rawTitle =
                        "Playlist",
                    rawPrivacyStatus =
                        "private",
                    metadata =
                        PlaylistUpdateMetadata(
                            defaultLanguage =
                                "   ",
                            podcastStatus =
                                ""
                        )
                )

        assertNull(
            spec.defaultLanguage
        )

        assertNull(
            spec.podcastStatus
        )
    }
}
