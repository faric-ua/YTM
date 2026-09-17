package com.saney.ytmimporter.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AccountBackupBaselineEntry(
    val playlistId: String,
    val title: String,
    val privacyStatus: String,
    val sourceItemCount: Long,
    val contentFingerprint: String?
)

data class AccountBackupBaseline(
    val folderName: String,
    val schemaVersion: Int,
    val appVersion: String,
    val exportedAt: Long,
    val scopeMode: String,
    val scopePlaylistIds: Set<String>,
    val entries: Map<String, AccountBackupBaselineEntry>
)

data class IncrementalBackupRecord(
    val playlistId: String,
    val title: String,
    val privacyStatus: String,
    val sourceItemCount: Long,
    val playlist: ImportedPlaylist?,
    val contentFingerprint: String?,
    val playlistItemsRequests: Int,
    val status: String,
    val error: String? = null
)

data class IncrementalBackupPlan(
    val baseline: AccountBackupBaseline,
    val records: List<IncrementalBackupRecord>,
    val playlistItemsRequests: Int
) {
    val currentPlaylistCount: Int
        get() =
            records.count {
                it.status !=
                    AccountLibraryIncrementalBackup.STATUS_MISSING
            }

    val newCount: Int
        get() =
            records.count {
                it.status ==
                    AccountLibraryIncrementalBackup.STATUS_NEW
            }

    val updatedCount: Int
        get() =
            records.count {
                it.status ==
                    AccountLibraryIncrementalBackup.STATUS_UPDATED
            }

    val unchangedCount: Int
        get() =
            records.count {
                it.status ==
                    AccountLibraryIncrementalBackup.STATUS_UNCHANGED
            }

    val missingCount: Int
        get() =
            records.count {
                it.status ==
                    AccountLibraryIncrementalBackup.STATUS_MISSING
            }

    val failedCount: Int
        get() =
            records.count {
                it.status ==
                    AccountLibraryIncrementalBackup.STATUS_FAILED
            }
}

data class IncrementalBackupWriteResult(
    val folderName: String,
    val manifestFile: String,
    val currentPlaylistCount: Int,
    val newCount: Int,
    val updatedCount: Int,
    val unchangedCount: Int,
    val missingCount: Int,
    val failedCount: Int,
    val writtenProjects: Int,
    val playlistItemsRequests: Int
)

object AccountLibraryIncrementalBackup {
    const val STATUS_NEW =
        "NEW"

    const val STATUS_UPDATED =
        "UPDATED"

    const val STATUS_UNCHANGED =
        "UNCHANGED"

    const val STATUS_MISSING =
        "MISSING"

    const val STATUS_FAILED =
        "FAILED"

    private const val FORMAT =
        "ytm-importer-account-library-export"

    private const val SYNC_SCHEMA_VERSION =
        3

    private const val BACKUP_MODE_INCREMENTAL =
        "INCREMENTAL_DELTA"

    fun readBaseline(
        resolver: ContentResolver,
        treeUri: Uri
    ): AccountBackupBaseline {
        val folderName =
            queryTreeDisplayName(
                resolver = resolver,
                treeUri = treeUri
            )

        val children =
            listDirectChildren(
                resolver = resolver,
                treeUri = treeUri
            )

        val manifestDocument =
            children.firstOrNull {
                it.displayName.equals(
                    "manifest.json",
                    ignoreCase = true
                )
            } ?: throw IllegalArgumentException(
                "У baseline-папці немає manifest.json"
            )

        val root =
            JSONObject(
                readUtf8Text(
                    resolver = resolver,
                    uri = manifestDocument.uri
                )
            )

        require(
            root.optString("format") == FORMAT
        ) {
            "Це не YTM Importer account backup manifest"
        }

        val schemaVersion =
            root.optInt(
                "schemaVersion",
                -1
            )

        require(
            schemaVersion in 1..SYNC_SCHEMA_VERSION
        ) {
            "Непідтримувана версія baseline manifest: $schemaVersion"
        }

        val scopeMode =
            when {
                schemaVersion >= 3 ->
                    root
                        .optString(
                            "syncScopeMode",
                            "ALL"
                        )
                        .trim()
                        .uppercase()
                        .ifBlank {
                            "ALL"
                        }

                schemaVersion >= 2 ->
                    root
                        .optString(
                            "selectionMode",
                            "ALL"
                        )
                        .trim()
                        .uppercase()
                        .ifBlank {
                            "ALL"
                        }

                else ->
                    "ALL"
            }

        require(
            scopeMode in
                setOf(
                    "ALL",
                    "SELECTED"
                )
        ) {
            "Непідтримуваний sync scope: $scopeMode"
        }

        val items =
            root.optJSONArray("playlists")
                ?: throw IllegalArgumentException(
                    "У baseline manifest немає playlists"
                )

        val documentsByName =
            children
                .filter {
                    !it.displayName.equals(
                        "manifest.json",
                        ignoreCase = true
                    )
                }
                .associateBy {
                    it.displayName
                }

        val allRecordIds =
            linkedSetOf<String>()

        val entries =
            linkedMapOf<
                String,
                AccountBackupBaselineEntry
            >()

        for (
            index in
            0 until items.length()
        ) {
            val record =
                items.optJSONObject(index)
                    ?: continue

            val playlistId =
                record
                    .optString("playlistId")
                    .trim()

            if (playlistId.isBlank()) {
                continue
            }

            allRecordIds += playlistId

            val status =
                record
                    .optString("status")
                    .trim()
                    .uppercase()

            if (status == STATUS_MISSING) {
                continue
            }

            val title =
                record
                    .optString(
                        "title",
                        "YTM Playlist"
                    )
                    .trim()
                    .ifBlank {
                        "YTM Playlist"
                    }

            val privacyStatus =
                record
                    .optString(
                        "privacyStatus",
                        "private"
                    )
                    .trim()
                    .ifBlank {
                        "private"
                    }

            val sourceItemCount =
                record.optLong(
                    "sourceItemCount",
                    0L
                )

            val manifestFingerprint =
                nullableString(
                    record,
                    "contentFingerprint"
                )

            val contentFingerprint =
                when {
                    !manifestFingerprint.isNullOrBlank() ->
                        manifestFingerprint

                    status == "SKIPPED_EMPTY" ->
                        fingerprintTracks(
                            emptyList()
                        )

                    status == "EXPORTED" -> {
                        val fileName =
                            nullableString(
                                record,
                                "fileName"
                            ) ?: throw IllegalArgumentException(
                                "Baseline EXPORTED запис не має fileName: $title"
                            )

                        val document =
                            documentsByName[fileName]
                                ?: children.firstOrNull {
                                    it.displayName.equals(
                                        fileName,
                                        ignoreCase = true
                                    )
                                }
                                ?: throw IllegalArgumentException(
                                    "Baseline project file не знайдено: $fileName"
                                )

                        val rawProject =
                            readUtf8Text(
                                resolver = resolver,
                                uri = document.uri
                            )

                        require(
                            PlaylistProjectCodec
                                .isProject(
                                    rawProject
                                )
                        ) {
                            "Baseline file не є YTM Project: $fileName"
                        }

                        val project =
                            PlaylistProjectCodec
                                .importProject(
                                    rawProject
                                )

                        if (
                            !project
                                .sourcePlaylistId
                                .isNullOrBlank()
                        ) {
                            require(
                                project.sourcePlaylistId ==
                                    playlistId
                            ) {
                                "playlistId baseline manifest/project не збігається: $title"
                            }
                        }

                        fingerprint(
                            project.playlist
                        )
                    }

                    else ->
                        null
                }

            if (
                status == "FAILED" &&
                contentFingerprint == null
            ) {
                continue
            }

            entries[playlistId] =
                AccountBackupBaselineEntry(
                    playlistId =
                        playlistId,
                    title =
                        title,
                    privacyStatus =
                        privacyStatus,
                    sourceItemCount =
                        sourceItemCount,
                    contentFingerprint =
                        contentFingerprint
                )
        }

        val scopePlaylistIds =
            if (scopeMode == "SELECTED") {
                if (schemaVersion >= 3) {
                    val fromManifest =
                        stringSet(
                            root.optJSONArray(
                                "scopePlaylistIds"
                            )
                        )

                    if (
                        fromManifest.isNotEmpty()
                    ) {
                        fromManifest
                    } else {
                        allRecordIds
                    }
                } else {
                    allRecordIds
                }
            } else {
                emptySet()
            }

        if (scopeMode == "SELECTED") {
            require(
                scopePlaylistIds.isNotEmpty()
            ) {
                "SELECTED baseline не містить playlistId scope"
            }
        }

        return AccountBackupBaseline(
            folderName =
                folderName,
            schemaVersion =
                schemaVersion,
            appVersion =
                root
                    .optString("appVersion")
                    .trim(),
            exportedAt =
                root.optLong(
                    "exportedAt",
                    0L
                ),
            scopeMode =
                scopeMode,
            scopePlaylistIds =
                scopePlaylistIds,
            entries =
                entries
        )
    }

    fun scopeCurrentPlaylists(
        baseline: AccountBackupBaseline,
        currentPlaylists: List<YouTubePlaylistInfo>
    ): List<YouTubePlaylistInfo> =
        if (
            baseline.scopeMode ==
                "SELECTED"
        ) {
            currentPlaylists.filter {
                it.id in
                    baseline.scopePlaylistIds
            }
        } else {
            currentPlaylists
        }

    fun estimatePlaylistItemsRequests(
        playlists: List<YouTubePlaylistInfo>
    ): Int =
        playlists.sumOf { playlist ->
            if (
                playlist.itemCount <= 0L
            ) {
                0
            } else {
                (
                    (playlist.itemCount + 49L) /
                        50L
                )
                    .coerceAtMost(
                        200L
                    )
                    .toInt()
            }
        }

    fun classify(
        baseline: AccountBackupBaseline,
        playlistInfo: YouTubePlaylistInfo,
        playlist: ImportedPlaylist,
        contentFingerprint: String,
        playlistItemsRequests: Int
    ): IncrementalBackupRecord {
        val previous =
            baseline.entries[
                playlistInfo.id
            ]

        val changed =
            previous == null ||
                previous.contentFingerprint == null ||
                previous.contentFingerprint !=
                    contentFingerprint ||
                previous.title !=
                    playlistInfo.title ||
                !previous.privacyStatus.equals(
                    playlistInfo.privacyStatus,
                    ignoreCase = true
                ) ||
                previous.sourceItemCount !=
                    playlistInfo.itemCount

        return IncrementalBackupRecord(
            playlistId =
                playlistInfo.id,
            title =
                playlistInfo.title,
            privacyStatus =
                playlistInfo.privacyStatus,
            sourceItemCount =
                playlistInfo.itemCount,
            playlist =
                playlist,
            contentFingerprint =
                contentFingerprint,
            playlistItemsRequests =
                playlistItemsRequests,
            status =
                when {
                    previous == null ->
                        STATUS_NEW

                    changed ->
                        STATUS_UPDATED

                    else ->
                        STATUS_UNCHANGED
                }
        )
    }

    fun failedRecord(
        baseline: AccountBackupBaseline,
        playlistInfo: YouTubePlaylistInfo,
        playlistItemsRequests: Int,
        error: Throwable
    ): IncrementalBackupRecord {
        val previous =
            baseline.entries[
                playlistInfo.id
            ]

        return IncrementalBackupRecord(
            playlistId =
                playlistInfo.id,
            title =
                playlistInfo.title,
            privacyStatus =
                playlistInfo.privacyStatus,
            sourceItemCount =
                playlistInfo.itemCount,
            playlist =
                null,
            contentFingerprint =
                previous
                    ?.contentFingerprint,
            playlistItemsRequests =
                playlistItemsRequests,
            status =
                STATUS_FAILED,
            error =
                error.message
                    ?: error
                        .javaClass
                        .simpleName
        )
    }

    fun missingRecords(
        baseline: AccountBackupBaseline,
        currentScopedIds: Set<String>
    ): List<IncrementalBackupRecord> =
        baseline.entries
            .values
            .filter {
                it.playlistId !in
                    currentScopedIds
            }
            .map {
                    previous ->

                IncrementalBackupRecord(
                    playlistId =
                        previous.playlistId,
                    title =
                        previous.title,
                    privacyStatus =
                        previous.privacyStatus,
                    sourceItemCount =
                        previous.sourceItemCount,
                    playlist =
                        null,
                    contentFingerprint =
                        previous
                            .contentFingerprint,
                    playlistItemsRequests =
                        0,
                    status =
                        STATUS_MISSING
                )
            }

    fun fingerprint(
        playlist: ImportedPlaylist
    ): String =
        fingerprintTracks(
            playlist.tracks
        )

    fun writeDelta(
        resolver: ContentResolver,
        treeUri: Uri,
        appVersion: String,
        plan: IncrementalBackupPlan
    ): IncrementalBackupWriteResult {
        val session =
            createSyncSessionFolder(
                resolver = resolver,
                treeUri = treeUri
            )

        val manifestRecords =
            mutableListOf<
                SyncManifestRecord
            >()

        plan.records.forEach {
                record ->

            val shouldWriteProject =
                record.status in
                    setOf(
                        STATUS_NEW,
                        STATUS_UPDATED
                    ) &&
                    record.playlist !=
                        null &&
                    record.playlist
                        .tracks
                        .isNotEmpty()

            val fileName =
                if (shouldWriteProject) {
                    AccountLibraryExporter
                        .writePlaylistProject(
                            resolver =
                                resolver,
                            session =
                                session,
                            playlistInfo =
                                YouTubePlaylistInfo(
                                    id =
                                        record.playlistId,
                                    title =
                                        record.title,
                                    privacyStatus =
                                        record
                                            .privacyStatus,
                                    itemCount =
                                        record
                                            .sourceItemCount
                                ),
                            playlist =
                                requireNotNull(
                                    record.playlist
                                ),
                            appVersion =
                                appVersion
                        )
                } else {
                    null
                }

            manifestRecords +=
                SyncManifestRecord(
                    record =
                        record,
                    fileName =
                        fileName
                )
        }

        val manifestFile =
            writeSyncManifest(
                resolver = resolver,
                session = session,
                appVersion = appVersion,
                plan = plan,
                records =
                    manifestRecords
            )

        return IncrementalBackupWriteResult(
            folderName =
                session.folderName,
            manifestFile =
                manifestFile,
            currentPlaylistCount =
                plan.currentPlaylistCount,
            newCount =
                plan.newCount,
            updatedCount =
                plan.updatedCount,
            unchangedCount =
                plan.unchangedCount,
            missingCount =
                plan.missingCount,
            failedCount =
                plan.failedCount,
            writtenProjects =
                manifestRecords.count {
                    !it.fileName
                        .isNullOrBlank()
                },
            playlistItemsRequests =
                plan.playlistItemsRequests
        )
    }

    private fun createSyncSessionFolder(
        resolver: ContentResolver,
        treeUri: Uri
    ): AccountLibraryExporter.ExportSession {
        val treeDocumentId =
            DocumentsContract
                .getTreeDocumentId(
                    treeUri
                )

        val rootDocumentUri =
            DocumentsContract
                .buildDocumentUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val folderName =
            "YTM-Importer-Account-Sync-" +
                SimpleDateFormat(
                    "yyyyMMdd-HHmmss",
                    Locale.US
                ).format(
                    Date()
                )

        val folderUri =
            DocumentsContract
                .createDocument(
                    resolver,
                    rootDocumentUri,
                    DocumentsContract
                        .Document
                        .MIME_TYPE_DIR,
                    folderName
                ) ?: error(
                "Не вдалося створити sync-папку"
            )

        return AccountLibraryExporter
            .ExportSession(
                folderUri =
                    folderUri,
                folderName =
                    folderName
            )
    }

    private fun writeSyncManifest(
        resolver: ContentResolver,
        session:
            AccountLibraryExporter.ExportSession,
        appVersion: String,
        plan: IncrementalBackupPlan,
        records: List<SyncManifestRecord>
    ): String {
        val items =
            JSONArray()

        records.forEach {
                item ->

            val record =
                item.record

            items.put(
                JSONObject()
                    .put(
                        "playlistId",
                        record.playlistId
                    )
                    .put(
                        "title",
                        record.title
                    )
                    .put(
                        "privacyStatus",
                        record.privacyStatus
                    )
                    .put(
                        "sourceItemCount",
                        record.sourceItemCount
                    )
                    .put(
                        "exportedTrackCount",
                        record.playlist
                            ?.tracks
                            ?.size
                            ?: 0
                    )
                    .put(
                        "playlistItemsRequests",
                        record.playlistItemsRequests
                    )
                    .put(
                        "status",
                        record.status
                    )
                    .put(
                        "contentFingerprint",
                        record.contentFingerprint
                            ?: JSONObject.NULL
                    )
                    .put(
                        "fileName",
                        item.fileName
                            ?: JSONObject.NULL
                    )
                    .put(
                        "error",
                        record.error
                            ?: JSONObject.NULL
                    )
            )
        }

        val root =
            JSONObject()
                .put(
                    "format",
                    FORMAT
                )
                .put(
                    "schemaVersion",
                    SYNC_SCHEMA_VERSION
                )
                .put(
                    "appVersion",
                    appVersion
                )
                .put(
                    "selectionMode",
                    "SYNC"
                )
                .put(
                    "backupMode",
                    BACKUP_MODE_INCREMENTAL
                )
                .put(
                    "syncScopeMode",
                    plan.baseline
                        .scopeMode
                )
                .put(
                    "scopePlaylistIds",
                    JSONArray().also {
                            array ->

                        plan.baseline
                            .scopePlaylistIds
                            .sorted()
                            .forEach {
                                array.put(it)
                            }
                    }
                )
                .put(
                    "baseSessionName",
                    plan.baseline
                        .folderName
                )
                .put(
                    "exportedAt",
                    System.currentTimeMillis()
                )
                .put(
                    "playlistCount",
                    records.size
                )
                .put(
                    "currentPlaylistCount",
                    plan.currentPlaylistCount
                )
                .put(
                    "newPlaylists",
                    plan.newCount
                )
                .put(
                    "updatedPlaylists",
                    plan.updatedCount
                )
                .put(
                    "unchangedPlaylists",
                    plan.unchangedCount
                )
                .put(
                    "missingPlaylists",
                    plan.missingCount
                )
                .put(
                    "failedPlaylists",
                    plan.failedCount
                )
                .put(
                    "exportedProjects",
                    records.count {
                        !it.fileName
                            .isNullOrBlank()
                    }
                )
                .put(
                    "playlistItemsRequests",
                    plan.playlistItemsRequests
                )
                .put(
                    "playlists",
                    items
                )

        val fileName =
            "manifest.json"

        writeTextDocument(
            resolver = resolver,
            parentUri = session.folderUri,
            displayName = fileName,
            text = root.toString(2)
        )

        return fileName
    }

    private fun fingerprintTracks(
        tracks: List<Track>
    ): String {
        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )

        digest.update(
            "ytm-account-backup-fingerprint-v1"
                .toByteArray(
                    Charsets.UTF_8
                )
        )

        tracks.forEachIndexed {
                index,
                track ->

            listOf(
                index.toString(),
                track.selectedVideoId
                    .orEmpty(),
                track.selectedTitle
                    .orEmpty(),
                track.selectedChannel
                    .orEmpty()
            ).forEach {
                    part ->

                digest.update(0.toByte())
                digest.update(
                    part.toByteArray(
                        Charsets.UTF_8
                    )
                )
            }
        }

        return digest
            .digest()
            .joinToString(
                separator = ""
            ) {
                "%02x".format(
                    it.toInt() and 0xff
                )
            }
    }

    private fun queryTreeDisplayName(
        resolver: ContentResolver,
        treeUri: Uri
    ): String {
        val treeDocumentId =
            DocumentsContract
                .getTreeDocumentId(
                    treeUri
                )

        val documentUri =
            DocumentsContract
                .buildDocumentUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val projection =
            arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME
            )

        resolver
            .query(
                documentUri,
                projection,
                null,
                null,
                null
            )
            ?.use {
                    cursor ->

                if (cursor.moveToFirst()) {
                    val column =
                        cursor
                            .getColumnIndex(
                                DocumentsContract
                                    .Document
                                    .COLUMN_DISPLAY_NAME
                            )

                    if (column >= 0) {
                        return cursor
                            .getString(column)
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "YTM backup"
                    }
                }
            }

        return "YTM backup"
    }

    private fun listDirectChildren(
        resolver: ContentResolver,
        treeUri: Uri
    ): List<DocumentEntry> {
        val treeDocumentId =
            DocumentsContract
                .getTreeDocumentId(
                    treeUri
                )

        val childrenUri =
            DocumentsContract
                .buildChildDocumentsUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val projection =
            arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DOCUMENT_ID,
                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME
            )

        val result =
            mutableListOf<DocumentEntry>()

        resolver
            .query(
                childrenUri,
                projection,
                null,
                null,
                null
            )
            ?.use {
                    cursor ->

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DOCUMENT_ID
                    )

                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DISPLAY_NAME
                    )

                while (
                    cursor.moveToNext()
                ) {
                    val documentId =
                        cursor
                            .getString(
                                idColumn
                            )
                            ?: continue

                    val displayName =
                        cursor
                            .getString(
                                nameColumn
                            )
                            ?: continue

                    result +=
                        DocumentEntry(
                            displayName =
                                displayName,
                            uri =
                                DocumentsContract
                                    .buildDocumentUriUsingTree(
                                        treeUri,
                                        documentId
                                    )
                        )
                }
            }

        return result
    }

    private fun writeTextDocument(
        resolver: ContentResolver,
        parentUri: Uri,
        displayName: String,
        text: String
    ) {
        val documentUri =
            DocumentsContract
                .createDocument(
                    resolver,
                    parentUri,
                    "application/json",
                    displayName
                ) ?: error(
                "Не вдалося створити файл $displayName"
            )

        val stream =
            resolver.openOutputStream(
                documentUri,
                "w"
            ) ?: error(
                "Не вдалося відкрити $displayName для запису"
            )

        stream
            .bufferedWriter(
                Charsets.UTF_8
            )
            .use {
                it.write(text)
            }
    }

    private fun readUtf8Text(
        resolver: ContentResolver,
        uri: Uri
    ): String {
        val input =
            resolver.openInputStream(uri)
                ?: throw IllegalArgumentException(
                    "Не вдалося відкрити файл"
                )

        return input
            .bufferedReader(
                Charsets.UTF_8
            )
            .use {
                it.readText()
            }
    }

    private fun nullableString(
        json: JSONObject,
        key: String
    ): String? {
        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json
            .optString(key)
            .trim()
            .takeIf {
                it.isNotBlank()
            }
    }

    private fun stringSet(
        array: JSONArray?
    ): Set<String> {
        if (array == null) {
            return emptySet()
        }

        val result =
            linkedSetOf<String>()

        for (
            index in
            0 until array.length()
        ) {
            array
                .optString(index)
                .trim()
                .takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    result += it
                }
        }

        return result
    }

    private data class DocumentEntry(
        val displayName: String,
        val uri: Uri
    )

    private data class SyncManifestRecord(
        val record: IncrementalBackupRecord,
        val fileName: String?
    )
}
