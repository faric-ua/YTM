package com.saney.ytmimporter.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YtmArtifactClassifierTest {
    @Test
    fun fullLocalBackupUsesContentMarker() {
        val inspection =
            YtmArtifactClassifier.inspect(
                """
                {
                  "format": "ytm-importer-local-backup",
                  "schemaVersion": 2,
                  "appVersion": "1.4.55",
                  "exportedAt": 12345,
                  "valueCount": 19,
                  "preferences": {}
                }
                """.trimIndent()
            )

        assertEquals(
            YtmArtifactType.FULL_LOCAL_BACKUP,
            inspection.type
        )
        assertEquals(
            2,
            inspection.schemaVersion
        )
        assertEquals(
            19,
            inspection.itemCount
        )
    }

    @Test
    fun playlistProjectUsesProjectMarkerAndMetadata() {
        val inspection =
            YtmArtifactClassifier.inspect(
                """
                {
                  "format": "ytm-importer-playlist-project",
                  "schemaVersion": 3,
                  "appVersion": "1.4.55",
                  "exportedAt": 12345,
                  "playlist": {
                    "name": "The Prodigy",
                    "tracks": [{}, {}]
                  }
                }
                """.trimIndent()
            )

        assertEquals(
            YtmArtifactType.PLAYLIST_PROJECT,
            inspection.type
        )
        assertEquals(
            "The Prodigy",
            inspection.title
        )
        assertEquals(
            2,
            inspection.itemCount
        )
    }

    @Test
    fun accountManifestIsNotConfusedWithFullLocalBackup() {
        val inspection =
            YtmArtifactClassifier.inspect(
                """
                {
                  "format": "ytm-importer-account-library-export",
                  "schemaVersion": 3,
                  "appVersion": "1.4.55",
                  "backupMode": "INCREMENTAL_DELTA",
                  "playlistCount": 2,
                  "playlists": [{}, {}]
                }
                """.trimIndent()
            )

        assertEquals(
            YtmArtifactType.ACCOUNT_LIBRARY_MANIFEST,
            inspection.type
        )
        assertEquals(
            "INCREMENTAL_DELTA",
            inspection.backupMode
        )
        assertEquals(
            2,
            inspection.itemCount
        )
    }

    @Test
    fun historyArrayUsesHistoryStructure() {
        val inspection =
            YtmArtifactClassifier.inspect(
                """
                [
                  {
                    "id": "history-1",
                    "createdAt": 100,
                    "updatedAt": 200,
                    "status": "PARTIAL",
                    "destination": "NEW_PLAYLIST",
                    "playlistName": "Album",
                    "tracks": []
                  }
                ]
                """.trimIndent()
            )

        assertEquals(
            YtmArtifactType.HISTORY_BACKUP,
            inspection.type
        )
        assertEquals(
            1,
            inspection.itemCount
        )
        assertEquals(
            200L,
            inspection.exportedAt
        )
        assertEquals(
            "Album",
            inspection.title
        )
    }

    @Test
    fun pendingArrayUsesPendingStructure() {
        val inspection =
            YtmArtifactClassifier.inspect(
                """
                [
                  {
                    "id": "pending-1",
                    "createdAt": 100,
                    "updatedAt": 250,
                    "operation": "CREATE_PLAYLIST",
                    "destination": "NEW_PLAYLIST",
                    "playlistName": "Album",
                    "remainingTracks": []
                  }
                ]
                """.trimIndent()
            )

        assertEquals(
            YtmArtifactType.PENDING_DIAGNOSTICS,
            inspection.type
        )
        assertEquals(
            250L,
            inspection.exportedAt
        )
    }

    @Test
    fun emptyArrayFailsClosed() {
        val inspection =
            YtmArtifactClassifier.inspect(
                "[]"
            )

        assertEquals(
            YtmArtifactType.UNKNOWN,
            inspection.type
        )
        assertNull(
            inspection.itemCount
        )
    }

    @Test
    fun mixedArrayFailsClosed() {
        val inspection =
            YtmArtifactClassifier.inspect(
                """
                [
                  {
                    "id": "history-1",
                    "status": "COMPLETED",
                    "destination": "NEW_PLAYLIST",
                    "tracks": []
                  },
                  {
                    "id": "pending-1",
                    "operation": "CREATE_PLAYLIST",
                    "destination": "NEW_PLAYLIST",
                    "remainingTracks": []
                  }
                ]
                """.trimIndent()
            )

        assertEquals(
            YtmArtifactType.UNKNOWN,
            inspection.type
        )
    }

    @Test
    fun malformedOrIncompleteJsonFailsClosed() {
        assertEquals(
            YtmArtifactType.UNKNOWN,
            YtmArtifactClassifier
                .inspect(
                    "{not json"
                )
                .type
        )

        assertEquals(
            YtmArtifactType.UNKNOWN,
            YtmArtifactClassifier
                .inspect(
                    """
                    {
                      "format": "ytm-importer-local-backup",
                      "schemaVersion": 2
                    }
                    """.trimIndent()
                )
                .type
        )
    }

    @Test
    fun scopesAcceptOnlyTheirCanonicalType() {
        assertTrue(
            YtmArtifactScopePolicy.accepts(
                YtmArtifactScope.FULL_LOCAL_RESTORE,
                YtmArtifactType.FULL_LOCAL_BACKUP
            )
        )
        assertFalse(
            YtmArtifactScopePolicy.accepts(
                YtmArtifactScope.FULL_LOCAL_RESTORE,
                YtmArtifactType.HISTORY_BACKUP
            )
        )
        assertTrue(
            YtmArtifactScopePolicy.accepts(
                YtmArtifactScope.HISTORY_RESTORE,
                YtmArtifactType.HISTORY_BACKUP
            )
        )
        assertFalse(
            YtmArtifactScopePolicy.accepts(
                YtmArtifactScope.HISTORY_RESTORE,
                YtmArtifactType.PENDING_DIAGNOSTICS
            )
        )
        assertTrue(
            YtmArtifactScopePolicy.accepts(
                YtmArtifactScope.PLAYLIST_PROJECT,
                YtmArtifactType.PLAYLIST_PROJECT
            )
        )
        assertFalse(
            YtmArtifactScopePolicy.accepts(
                YtmArtifactScope.PLAYLIST_PROJECT,
                YtmArtifactType.ACCOUNT_LIBRARY_MANIFEST
            )
        )
    }
}
