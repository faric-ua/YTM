package com.saney.ytmimporter.storage

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YtmArtifactClassificationCacheTest {
    @After
    fun tearDown() {
        YtmArtifactClassificationCache
            .clearForTests()
    }

    @Test
    fun sameFileIdentityReusesClassification() {
        YtmArtifactClassificationCache
            .put(
                uri = "content://backup/1",
                lastModified = 100L,
                size = 200L,
                type =
                    YtmArtifactType
                        .FULL_LOCAL_BACKUP
            )

        assertEquals(
            YtmArtifactType
                .FULL_LOCAL_BACKUP,
            YtmArtifactClassificationCache
                .get(
                    uri = "content://backup/1",
                    lastModified = 100L,
                    size = 200L
                )
        )
    }

    @Test
    fun inspectionMetadataSurvivesRotationCache() {
        val inspection =
            YtmArtifactInspection(
                type =
                    YtmArtifactType
                        .PLAYLIST_PROJECT,
                schemaVersion = 3,
                appVersion = "1.4.55",
                itemCount = 13,
                title = "The Prodigy"
            )

        YtmArtifactClassificationCache
            .putInspection(
                uri = "content://project/1",
                lastModified = 100L,
                size = 500L,
                inspection = inspection
            )

        assertEquals(
            inspection,
            YtmArtifactClassificationCache
                .getInspection(
                    uri = "content://project/1",
                    lastModified = 100L,
                    size = 500L
                )
        )
        assertEquals(
            YtmArtifactType
                .PLAYLIST_PROJECT,
            YtmArtifactClassificationCache
                .get(
                    uri = "content://project/1",
                    lastModified = 100L,
                    size = 500L
                )
        )
    }

    @Test
    fun changedMetadataInvalidatesOldIdentity() {
        YtmArtifactClassificationCache
            .put(
                uri = "content://backup/1",
                lastModified = 100L,
                size = 200L,
                type =
                    YtmArtifactType
                        .FULL_LOCAL_BACKUP
            )

        assertNull(
            YtmArtifactClassificationCache
                .get(
                    uri = "content://backup/1",
                    lastModified = 101L,
                    size = 200L
                )
        )

        assertNull(
            YtmArtifactClassificationCache
                .get(
                    uri = "content://backup/1",
                    lastModified = 100L,
                    size = 201L
                )
        )
    }

    @Test
    fun newVersionReplacesOldIdentityForSameUri() {
        YtmArtifactClassificationCache
            .put(
                uri = "content://backup/1",
                lastModified = 100L,
                size = 200L,
                type =
                    YtmArtifactType
                        .FULL_LOCAL_BACKUP
            )

        YtmArtifactClassificationCache
            .put(
                uri = "content://backup/1",
                lastModified = 101L,
                size = 210L,
                type =
                    YtmArtifactType
                        .HISTORY_BACKUP
            )

        assertNull(
            YtmArtifactClassificationCache
                .get(
                    uri = "content://backup/1",
                    lastModified = 100L,
                    size = 200L
                )
        )

        assertEquals(
            YtmArtifactType.HISTORY_BACKUP,
            YtmArtifactClassificationCache
                .get(
                    uri = "content://backup/1",
                    lastModified = 101L,
                    size = 210L
                )
        )
    }

    @Test
    fun cacheIsBounded() {
        for (
            index in
            0 until 400
        ) {
            YtmArtifactClassificationCache
                .put(
                    uri =
                        "content://backup/$index",
                    lastModified =
                        index.toLong(),
                    size =
                        index.toLong(),
                    type =
                        YtmArtifactType.UNKNOWN
                )
        }

        assertTrue(
            YtmArtifactClassificationCache
                .sizeForTests() <=
                256
        )
    }
}
