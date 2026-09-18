package com.saney.ytmimporter.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeltaChainHead(
    val folderName: String,
    val baseSessionName: String,
    val scopeMode: String,
    val exportedAt: Long
)

data class DeltaChainPlaylist(
    val playlistId: String,
    val title: String,
    val privacyStatus: String,
    val sourceItemCount: Long,
    val playlist: ImportedPlaylist?,
    val contentFingerprint: String,
    val sourceFolderName: String
)

data class DeltaChainPlan(
    val rootFolderName: String,
    val baseFolderName: String,
    val headFolderName: String,
    val chainFolders: List<String>,
    val scopeMode: String,
    val scopePlaylistIds: Set<String>,
    val playlists: List<DeltaChainPlaylist>,
    val missingEvents: Int
) {
    val chainLength: Int
        get() =
            chainFolders.size

    val playlistCount: Int
        get() =
            playlists.size

    val projectCount: Int
        get() =
            playlists.count {
                it.playlist != null
            }

    val emptyCount: Int
        get() =
            playlists.count {
                it.playlist == null
            }
}

data class DeltaChainMaterializeResult(
    val folderName: String,
    val manifestFile: String,
    val chainLength: Int,
    val playlistCount: Int,
    val exportedProjects: Int,
    val emptyPlaylists: Int
)

object AccountLibraryDeltaChainRestorer {
    private const val FORMAT =
        "ytm-importer-account-library-export"

    private const val MAX_SCHEMA_VERSION =
        3

    private const val BACKUP_MODE_INCREMENTAL =
        "INCREMENTAL_DELTA"

    private const val BACKUP_MODE_CONSOLIDATED =
        "CONSOLIDATED_FULL"

    fun discoverHeads(
        resolver: ContentResolver,
        treeUri: Uri
    ): List<DeltaChainHead> {
        val sessions =
            scanSessions(
                resolver = resolver,
                treeUri = treeUri
            )

        val referenced =
            sessions
                .values
                .filter {
                    it.isIncremental
                }
                .mapNotNull {
                    it.baseSessionName
                }
                .toSet()

        val heads =
            sessions
                .values
                .filter {
                    it.isIncremental &&
                        it.folderName !in referenced
                }
                .sortedWith(
                    compareByDescending<ChainSession> {
                        it.exportedAt
                    }.thenByDescending {
                        it.folderName
                    }
                )
                .map {
                    DeltaChainHead(
                        folderName =
                            it.folderName,
                        baseSessionName =
                            requireNotNull(
                                it.baseSessionName
                            ),
                        scopeMode =
                            it.scopeMode,
                        exportedAt =
                            it.exportedAt
                    )
                }

        require(heads.isNotEmpty()) {
            if (
                sessions.values.any {
                    it.isIncremental
                }
            ) {
                "Не знайдено head для delta-chain. Можливий цикл або пошкоджений chain."
            } else {
                "У вибраній папці немає incremental delta-chain."
            }
        }

        return heads
    }

    fun resolvePlan(
        resolver: ContentResolver,
        treeUri: Uri,
        headFolderName: String
    ): DeltaChainPlan {
        val rootFolderName =
            queryTreeDisplayName(
                resolver = resolver,
                treeUri = treeUri
            )

        val sessions =
            scanSessions(
                resolver = resolver,
                treeUri = treeUri
            )

        val head =
            sessions[headFolderName]
                ?: throw IllegalArgumentException(
                    "Chain head не знайдено: $headFolderName"
                )

        require(head.isIncremental) {
            "Вибрана папка не є incremental delta head: $headFolderName"
        }

        val chainReverse =
            mutableListOf<ChainSession>()

        val visited =
            linkedSetOf<String>()

        var current =
            head

        while (true) {
            require(
                visited.add(
                    current.folderName
                )
            ) {
                "Delta-chain містить цикл біля ${current.folderName}"
            }

            chainReverse +=
                current

            if (!current.isIncremental) {
                break
            }

            val baseName =
                current.baseSessionName
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: throw IllegalArgumentException(
                        "Delta ${current.folderName} не містить baseSessionName"
                    )

            current =
                sessions[baseName]
                    ?: throw IllegalArgumentException(
                        "Не знайдено baseline-папку `$baseName` для ${current.folderName}. " +
                            "Виберіть спільну батьківську папку, де лежить увесь chain."
                    )
        }

        val chain =
            chainReverse
                .asReversed()

        val base =
            chain.first()

        require(!base.isIncremental) {
            "Delta-chain не завершується повним baseline."
        }

        val scopeMode =
            base.scopeMode

        require(
            scopeMode in
                setOf(
                    "ALL",
                    "SELECTED"
                )
        ) {
            "Непідтримуваний scope baseline: $scopeMode"
        }

        val baseScopeIds =
            if (scopeMode == "SELECTED") {
                base.scopePlaylistIds
                    .ifEmpty {
                        base.records
                            .map {
                                it.playlistId
                            }
                            .filter {
                                it.isNotBlank()
                            }
                            .toSet()
                    }
            } else {
                emptySet()
            }

        if (scopeMode == "SELECTED") {
            require(
                baseScopeIds.isNotEmpty()
            ) {
                "SELECTED baseline не містить playlistId scope."
            }
        }

        chain
            .drop(1)
            .forEach {
                    delta ->

                require(
                    delta.scopeMode ==
                        scopeMode
                ) {
                    "Scope змінився всередині chain: ${delta.folderName}"
                }

                if (
                    scopeMode == "SELECTED" &&
                    delta.scopePlaylistIds
                        .isNotEmpty()
                ) {
                    require(
                        delta.scopePlaylistIds ==
                            baseScopeIds
                    ) {
                        "SELECTED scopePlaylistIds не збігаються у ${delta.folderName}"
                    }
                }
            }

        val state =
            linkedMapOf<
                String,
                DeltaChainPlaylist
            >()

        applyFullBaseline(
            resolver = resolver,
            session = base,
            state = state
        )

        var missingEvents = 0

        chain
            .drop(1)
            .forEach {
                    delta ->

                missingEvents +=
                    applyDelta(
                        resolver = resolver,
                        session = delta,
                        state = state
                    )
            }

        return DeltaChainPlan(
            rootFolderName =
                rootFolderName,
            baseFolderName =
                base.folderName,
            headFolderName =
                head.folderName,
            chainFolders =
                chain.map {
                    it.folderName
                },
            scopeMode =
                scopeMode,
            scopePlaylistIds =
                baseScopeIds,
            playlists =
                state.values
                    .toList(),
            missingEvents =
                missingEvents
        )
    }

    fun materialize(
        resolver: ContentResolver,
        treeUri: Uri,
        appVersion: String,
        plan: DeltaChainPlan
    ): DeltaChainMaterializeResult {
        val session =
            createConsolidatedSessionFolder(
                resolver = resolver,
                treeUri = treeUri
            )

        val records =
            mutableListOf<
                ConsolidatedManifestRecord
            >()

        plan.playlists.forEach {
                state ->

            val playlist =
                state.playlist

            val fileName =
                if (
                    playlist != null &&
                    playlist.tracks
                        .isNotEmpty()
                ) {
                    AccountLibraryExporter
                        .writePlaylistProject(
                            resolver =
                                resolver,
                            session =
                                session,
                            playlistInfo =
                                YouTubePlaylistInfo(
                                    id =
                                        state.playlistId,
                                    title =
                                        state.title,
                                    privacyStatus =
                                        state.privacyStatus,
                                    itemCount =
                                        state.sourceItemCount
                                ),
                            playlist =
                                playlist,
                            appVersion =
                                appVersion
                        )
                } else {
                    null
                }

            records +=
                ConsolidatedManifestRecord(
                    playlistId =
                        state.playlistId,
                    title =
                        state.title,
                    privacyStatus =
                        state.privacyStatus,
                    sourceItemCount =
                        state.sourceItemCount,
                    exportedTrackCount =
                        playlist
                            ?.tracks
                            ?.size
                            ?: 0,
                    status =
                        if (fileName == null) {
                            "SKIPPED_EMPTY"
                        } else {
                            "EXPORTED"
                        },
                    fileName =
                        fileName
                )
        }

        val manifestFile =
            writeConsolidatedManifest(
                resolver = resolver,
                session = session,
                appVersion = appVersion,
                plan = plan,
                records = records
            )

        return DeltaChainMaterializeResult(
            folderName =
                session.folderName,
            manifestFile =
                manifestFile,
            chainLength =
                plan.chainLength,
            playlistCount =
                records.size,
            exportedProjects =
                records.count {
                    it.status ==
                        "EXPORTED"
                },
            emptyPlaylists =
                records.count {
                    it.status ==
                        "SKIPPED_EMPTY"
                }
        )
    }

    private fun applyFullBaseline(
        resolver: ContentResolver,
        session: ChainSession,
        state:
            LinkedHashMap<
                String,
                DeltaChainPlaylist
            >
    ) {
        session.records.forEach {
                record ->

            require(
                record.playlistId
                    .isNotBlank()
            ) {
                "Baseline ${session.folderName} містить запис без playlistId."
            }

            when (record.status) {
                "EXPORTED" -> {
                    val project =
                        loadProject(
                            resolver = resolver,
                            session = session,
                            record = record
                        )

                    state[record.playlistId] =
                        stateFromProject(
                            session = session,
                            record = record,
                            project = project
                        )
                }

                "SKIPPED_EMPTY" -> {
                    require(
                        record.sourceItemCount ==
                            0L
                    ) {
                        "SKIPPED_EMPTY має ненульовий sourceItemCount: ${record.title}"
                    }

                    state[record.playlistId] =
                        emptyState(
                            session = session,
                            record = record
                        )
                }

                "FAILED" ->
                    throw IllegalArgumentException(
                        "Baseline ${session.folderName} містить FAILED запис `${record.title}`. " +
                            "Точна консолідація неможлива."
                    )

                else ->
                    throw IllegalArgumentException(
                        "Непідтримуваний baseline status `${record.status}` у ${session.folderName}"
                    )
            }
        }
    }

    private fun applyDelta(
        resolver: ContentResolver,
        session: ChainSession,
        state:
            LinkedHashMap<
                String,
                DeltaChainPlaylist
            >
    ): Int {
        var missingEvents = 0

        session.records.forEach {
                record ->

            require(
                record.playlistId
                    .isNotBlank()
            ) {
                "Delta ${session.folderName} містить запис без playlistId."
            }

            when (record.status) {
                "NEW" -> {
                    require(
                        record.playlistId !in
                            state
                    ) {
                        "NEW playlist вже існує у попередньому state: ${record.title}"
                    }

                    state[record.playlistId] =
                        resolveChangedState(
                            resolver = resolver,
                            session = session,
                            record = record
                        )
                }

                "UPDATED" -> {
                    require(
                        record.playlistId in
                            state
                    ) {
                        "UPDATED playlist відсутній у попередньому state: ${record.title}"
                    }

                    state[record.playlistId] =
                        resolveChangedState(
                            resolver = resolver,
                            session = session,
                            record = record
                        )
                }

                "UNCHANGED" -> {
                    val previous =
                        state[record.playlistId]
                            ?: throw IllegalArgumentException(
                                "UNCHANGED playlist відсутній у попередньому state: ${record.title}"
                            )

                    validateUnchanged(
                        session = session,
                        record = record,
                        previous = previous
                    )
                }

                "MISSING" -> {
                    require(
                        record.playlistId in
                            state
                    ) {
                        "MISSING playlist відсутній у попередньому state: ${record.title}"
                    }

                    state.remove(
                        record.playlistId
                    )

                    missingEvents += 1
                }

                "FAILED" ->
                    throw IllegalArgumentException(
                        "Delta ${session.folderName} містить FAILED запис `${record.title}`. " +
                            "Точний актуальний стан невідомий, тому консолідацію зупинено."
                    )

                else ->
                    throw IllegalArgumentException(
                        "Непідтримуваний delta status `${record.status}` у ${session.folderName}"
                    )
            }
        }

        return missingEvents
    }

    private fun resolveChangedState(
        resolver: ContentResolver,
        session: ChainSession,
        record: ChainRecord
    ): DeltaChainPlaylist {
        val fileName =
            record.fileName

        if (
            !fileName.isNullOrBlank()
        ) {
            val project =
                loadProject(
                    resolver = resolver,
                    session = session,
                    record = record
                )

            return stateFromProject(
                session = session,
                record = record,
                project = project
            )
        }

        require(
            record.sourceItemCount ==
                0L &&
                record.exportedTrackCount ==
                    0
        ) {
            "${record.status} `${record.title}` не має YTM Project файлу, але playlist не порожній."
        }

        return emptyState(
            session = session,
            record = record
        )
    }

    private fun validateUnchanged(
        session: ChainSession,
        record: ChainRecord,
        previous: DeltaChainPlaylist
    ) {
        require(
            record.title ==
                previous.title
        ) {
            "UNCHANGED title відрізняється у ${session.folderName}: ${record.title}"
        }

        require(
            record.privacyStatus.equals(
                previous.privacyStatus,
                ignoreCase = true
            )
        ) {
            "UNCHANGED privacyStatus відрізняється у ${session.folderName}: ${record.title}"
        }

        require(
            record.sourceItemCount ==
                previous.sourceItemCount
        ) {
            "UNCHANGED sourceItemCount відрізняється у ${session.folderName}: ${record.title}"
        }

        val fingerprint =
            record.contentFingerprint

        if (
            !fingerprint.isNullOrBlank()
        ) {
            require(
                fingerprint ==
                    previous.contentFingerprint
            ) {
                "UNCHANGED fingerprint не збігається у ${session.folderName}: ${record.title}"
            }
        }
    }

    private fun stateFromProject(
        session: ChainSession,
        record: ChainRecord,
        project: PlaylistProjectImport
    ): DeltaChainPlaylist {
        val fingerprint =
            AccountLibraryIncrementalBackup
                .fingerprint(
                    project.playlist
                )

        val expectedFingerprint =
            record.contentFingerprint

        if (
            !expectedFingerprint.isNullOrBlank()
        ) {
            require(
                expectedFingerprint ==
                    fingerprint
            ) {
                "contentFingerprint не збігається з YTM Project: ${record.title}"
            }
        }

        return DeltaChainPlaylist(
            playlistId =
                record.playlistId,
            title =
                record.title,
            privacyStatus =
                record.privacyStatus,
            sourceItemCount =
                record.sourceItemCount,
            playlist =
                project.playlist,
            contentFingerprint =
                fingerprint,
            sourceFolderName =
                session.folderName
        )
    }

    private fun emptyState(
        session: ChainSession,
        record: ChainRecord
    ): DeltaChainPlaylist {
        val empty =
            ImportedPlaylist(
                name =
                    record.title,
                tracks =
                    mutableListOf()
            )

        return DeltaChainPlaylist(
            playlistId =
                record.playlistId,
            title =
                record.title,
            privacyStatus =
                record.privacyStatus,
            sourceItemCount =
                0L,
            playlist =
                null,
            contentFingerprint =
                AccountLibraryIncrementalBackup
                    .fingerprint(
                        empty
                    ),
            sourceFolderName =
                session.folderName
        )
    }

    private fun loadProject(
        resolver: ContentResolver,
        session: ChainSession,
        record: ChainRecord
    ): PlaylistProjectImport {
        val fileName =
            record.fileName
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw IllegalArgumentException(
                    "${record.status} запис не має fileName: ${record.title}"
                )

        val document =
            session.documents[fileName]
                ?: session.documents
                    .entries
                    .firstOrNull {
                        it.key.equals(
                            fileName,
                            ignoreCase = true
                        )
                    }
                    ?.value
                ?: throw IllegalArgumentException(
                    "YTM Project файл не знайдено у ${session.folderName}: $fileName"
                )

        val raw =
            readUtf8Text(
                resolver = resolver,
                uri = document
            )

        require(
            PlaylistProjectCodec
                .isProject(
                    raw
                )
        ) {
            "Файл $fileName не є YTM Project"
        }

        val project =
            PlaylistProjectCodec
                .importProject(
                    raw
                )

        if (
            !project.sourcePlaylistId
                .isNullOrBlank()
        ) {
            require(
                project.sourcePlaylistId ==
                    record.playlistId
            ) {
                "playlistId manifest/project не збігається: ${record.title}"
            }
        }

        if (
            !project.sourcePrivacyStatus
                .isNullOrBlank() &&
            record.privacyStatus
                .isNotBlank()
        ) {
            require(
                project.sourcePrivacyStatus
                    .equals(
                        record.privacyStatus,
                        ignoreCase = true
                    )
            ) {
                "privacyStatus manifest/project не збігається: ${record.title}"
            }
        }

        return project
    }

    private fun scanSessions(
        resolver: ContentResolver,
        treeUri: Uri
    ): Map<String, ChainSession> {
        val rootDocumentId =
            DocumentsContract
                .getTreeDocumentId(
                    treeUri
                )

        val rootChildren =
            listChildren(
                resolver = resolver,
                treeUri = treeUri,
                parentDocumentId =
                    rootDocumentId
            )

        val sessions =
            linkedMapOf<
                String,
                ChainSession
            >()

        rootChildren
            .filter {
                it.mimeType ==
                    DocumentsContract
                        .Document
                        .MIME_TYPE_DIR
            }
            .forEach {
                    folder ->

                val children =
                    listChildren(
                        resolver = resolver,
                        treeUri = treeUri,
                        parentDocumentId =
                            folder.documentId
                    )

                val manifest =
                    children.firstOrNull {
                        it.displayName.equals(
                            "manifest.json",
                            ignoreCase = true
                        )
                    } ?: return@forEach

                val root =
                    runCatching {
                        JSONObject(
                            readUtf8Text(
                                resolver = resolver,
                                uri = manifest.uri
                            )
                        )
                    }.getOrElse {
                        throw IllegalArgumentException(
                            "Не вдалося прочитати manifest у ${folder.displayName}: ${it.message}"
                        )
                    }

                if (
                    root.optString(
                        "format"
                    ) != FORMAT
                ) {
                    return@forEach
                }

                val parsed =
                    parseSession(
                        folderName =
                            folder.displayName,
                        folderDocumentId =
                            folder.documentId,
                        root =
                            root,
                        children =
                            children
                    )

                require(
                    parsed.folderName !in
                        sessions
                ) {
                    "У chain-root є дубльована session-папка: ${parsed.folderName}"
                }

                sessions[
                    parsed.folderName
                ] =
                    parsed
            }

        require(
            sessions.isNotEmpty()
        ) {
            "У вибраній папці немає YTM Importer backup sessions. " +
                "Виберіть спільну батьківську папку, що містить Account-Export і Account-Sync."
        }

        return sessions
    }

    private fun parseSession(
        folderName: String,
        folderDocumentId: String,
        root: JSONObject,
        children: List<DocumentEntry>
    ): ChainSession {
        val schemaVersion =
            root.optInt(
                "schemaVersion",
                -1
            )

        require(
            schemaVersion in
                1..MAX_SCHEMA_VERSION
        ) {
            "Непідтримуваний schemaVersion $schemaVersion у $folderName"
        }

        val backupMode =
            if (
                schemaVersion >=
                    3
            ) {
                root
                    .optString(
                        "backupMode",
                        "FULL"
                    )
                    .trim()
                    .uppercase()
                    .ifBlank {
                        "FULL"
                    }
            } else {
                "FULL"
            }

        val isIncremental =
            backupMode ==
                BACKUP_MODE_INCREMENTAL

        val scopeMode =
            if (isIncremental) {
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
            } else {
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
            }

        require(
            scopeMode in
                setOf(
                    "ALL",
                    "SELECTED"
                )
        ) {
            "Непідтримуваний scope `$scopeMode` у $folderName"
        }

        val items =
            root.optJSONArray(
                "playlists"
            ) ?: throw IllegalArgumentException(
                "У manifest $folderName немає playlists"
            )

        val declaredPlaylistCount =
            root.optInt(
                "playlistCount",
                items.length()
            )

        require(
            declaredPlaylistCount ==
                items.length()
        ) {
            "playlistCount не збігається у $folderName"
        }

        val records =
            mutableListOf<
                ChainRecord
            >()

        for (
            index in
            0 until items.length()
        ) {
            val item =
                items.optJSONObject(index)
                    ?: continue

            records +=
                ChainRecord(
                    playlistId =
                        item
                            .optString(
                                "playlistId"
                            )
                            .trim(),
                    title =
                        item
                            .optString(
                                "title",
                                "YTM Playlist"
                            )
                            .trim()
                            .ifBlank {
                                "YTM Playlist"
                            },
                    privacyStatus =
                        item
                            .optString(
                                "privacyStatus",
                                "private"
                            )
                            .trim()
                            .ifBlank {
                                "private"
                            },
                    sourceItemCount =
                        item.optLong(
                            "sourceItemCount",
                            0L
                        ),
                    exportedTrackCount =
                        item.optInt(
                            "exportedTrackCount",
                            0
                        ),
                    status =
                        item
                            .optString(
                                "status"
                            )
                            .trim()
                            .uppercase(),
                    fileName =
                        nullableString(
                            item,
                            "fileName"
                        ),
                    contentFingerprint =
                        nullableString(
                            item,
                            "contentFingerprint"
                        )
                )
        }

        val documents =
            children
                .filter {
                    it.mimeType !=
                        DocumentsContract
                            .Document
                            .MIME_TYPE_DIR &&
                        !it.displayName
                            .equals(
                                "manifest.json",
                                ignoreCase = true
                            )
                }
                .associate {
                    it.displayName to
                        it.uri
                }

        val scopePlaylistIds =
            if (
                schemaVersion >=
                    3 &&
                scopeMode ==
                    "SELECTED"
            ) {
                stringSet(
                    root.optJSONArray(
                        "scopePlaylistIds"
                    )
                )
            } else if (
                scopeMode ==
                    "SELECTED"
            ) {
                records
                    .map {
                        it.playlistId
                    }
                    .filter {
                        it.isNotBlank()
                    }
                    .toSet()
            } else {
                emptySet()
            }

        return ChainSession(
            folderName =
                folderName,
            folderDocumentId =
                folderDocumentId,
            schemaVersion =
                schemaVersion,
            backupMode =
                backupMode,
            scopeMode =
                scopeMode,
            scopePlaylistIds =
                scopePlaylistIds,
            baseSessionName =
                if (isIncremental) {
                    nullableString(
                        root,
                        "baseSessionName"
                    )
                } else {
                    null
                },
            exportedAt =
                root.optLong(
                    "exportedAt",
                    0L
                ),
            records =
                records,
            documents =
                documents
        )
    }

    private fun createConsolidatedSessionFolder(
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
            AccountBackupNaming
                .consolidatedFolderName()

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
                "Не вдалося створити consolidated backup папку"
            )

        return AccountLibraryExporter
            .ExportSession(
                folderUri =
                    folderUri,
                folderName =
                    folderName
            )
    }

    private fun writeConsolidatedManifest(
        resolver: ContentResolver,
        session:
            AccountLibraryExporter.ExportSession,
        appVersion: String,
        plan: DeltaChainPlan,
        records:
            List<
                ConsolidatedManifestRecord
            >
    ): String {
        val items =
            JSONArray()

        records.forEach {
                record ->

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
                        record.exportedTrackCount
                    )
                    .put(
                        "playlistItemsRequests",
                        0
                    )
                    .put(
                        "status",
                        record.status
                    )
                    .put(
                        "fileName",
                        record.fileName
                            ?: JSONObject.NULL
                    )
                    .put(
                        "error",
                        JSONObject.NULL
                    )
            )
        }

        val exported =
            records.count {
                it.status ==
                    "EXPORTED"
            }

        val skipped =
            records.count {
                it.status ==
                    "SKIPPED_EMPTY"
            }

        val root =
            JSONObject()
                .put(
                    "format",
                    FORMAT
                )
                .put(
                    "schemaVersion",
                    3
                )
                .put(
                    "appVersion",
                    appVersion
                )
                .put(
                    "selectionMode",
                    plan.scopeMode
                )
                .put(
                    "backupMode",
                    BACKUP_MODE_CONSOLIDATED
                )
                .put(
                    "syncScopeMode",
                    plan.scopeMode
                )
                .put(
                    "scopePlaylistIds",
                    JSONArray().also {
                            array ->

                        plan.scopePlaylistIds
                            .sorted()
                            .forEach {
                                array.put(it)
                            }
                    }
                )
                .put(
                    "materializedFromChain",
                    true
                )
                .put(
                    "chainBaseSessionName",
                    plan.baseFolderName
                )
                .put(
                    "chainHeadSessionName",
                    plan.headFolderName
                )
                .put(
                    "chainLength",
                    plan.chainLength
                )
                .put(
                    "sourceSessions",
                    JSONArray().also {
                            array ->

                        plan.chainFolders
                            .forEach {
                                array.put(it)
                            }
                    }
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
                    "exportedProjects",
                    exported
                )
                .put(
                    "skippedPlaylists",
                    skipped
                )
                .put(
                    "failedPlaylists",
                    0
                )
                .put(
                    "playlistItemsRequests",
                    0
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

    private fun listChildren(
        resolver: ContentResolver,
        treeUri: Uri,
        parentDocumentId: String
    ): List<DocumentEntry> {
        val childrenUri =
            DocumentsContract
                .buildChildDocumentsUriUsingTree(
                    treeUri,
                    parentDocumentId
                )

        val projection =
            arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DOCUMENT_ID,
                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME,
                DocumentsContract
                    .Document
                    .COLUMN_MIME_TYPE
            )

        val result =
            mutableListOf<
                DocumentEntry
            >()

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
                    cursor
                        .getColumnIndexOrThrow(
                            DocumentsContract
                                .Document
                                .COLUMN_DOCUMENT_ID
                        )

                val nameColumn =
                    cursor
                        .getColumnIndexOrThrow(
                            DocumentsContract
                                .Document
                                .COLUMN_DISPLAY_NAME
                        )

                val mimeColumn =
                    cursor
                        .getColumnIndexOrThrow(
                            DocumentsContract
                                .Document
                                .COLUMN_MIME_TYPE
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

                    val mimeType =
                        cursor
                            .getString(
                                mimeColumn
                            )
                            ?: ""

                    result +=
                        DocumentEntry(
                            documentId =
                                documentId,
                            displayName =
                                displayName,
                            mimeType =
                                mimeType,
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

    private fun queryTreeDisplayName(
        resolver: ContentResolver,
        treeUri: Uri
    ): String {
        val documentId =
            DocumentsContract
                .getTreeDocumentId(
                    treeUri
                )

        val documentUri =
            DocumentsContract
                .buildDocumentUriUsingTree(
                    treeUri,
                    documentId
                )

        resolver
            .query(
                documentUri,
                arrayOf(
                    DocumentsContract
                        .Document
                        .COLUMN_DISPLAY_NAME
                ),
                null,
                null,
                null
            )
            ?.use {
                    cursor ->

                if (
                    cursor.moveToFirst()
                ) {
                    val index =
                        cursor
                            .getColumnIndex(
                                DocumentsContract
                                    .Document
                                    .COLUMN_DISPLAY_NAME
                            )

                    if (index >= 0) {
                        return cursor
                            .getString(index)
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Backup chain"
                    }
                }
            }

        return "Backup chain"
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
            resolver
                .openOutputStream(
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
            resolver
                .openInputStream(
                    uri
                ) ?: throw IllegalArgumentException(
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
            .optString(
                key
            )
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
        val documentId: String,
        val displayName: String,
        val mimeType: String,
        val uri: Uri
    )

    private data class ChainSession(
        val folderName: String,
        val folderDocumentId: String,
        val schemaVersion: Int,
        val backupMode: String,
        val scopeMode: String,
        val scopePlaylistIds: Set<String>,
        val baseSessionName: String?,
        val exportedAt: Long,
        val records: List<ChainRecord>,
        val documents: Map<String, Uri>
    ) {
        val isIncremental: Boolean
            get() =
                backupMode ==
                    BACKUP_MODE_INCREMENTAL
    }

    private data class ChainRecord(
        val playlistId: String,
        val title: String,
        val privacyStatus: String,
        val sourceItemCount: Long,
        val exportedTrackCount: Int,
        val status: String,
        val fileName: String?,
        val contentFingerprint: String?
    )

    private data class ConsolidatedManifestRecord(
        val playlistId: String,
        val title: String,
        val privacyStatus: String,
        val sourceItemCount: Long,
        val exportedTrackCount: Int,
        val status: String,
        val fileName: String?
    )
}
