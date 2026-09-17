package com.saney.ytmimporter.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import org.json.JSONArray
import org.json.JSONObject

data class AccountLibraryManifestEntry(
    val playlistId: String,
    val title: String,
    val privacyStatus: String,
    val sourceItemCount: Long,
    val exportedTrackCount: Int,
    val playlistItemsRequests: Int,
    val fileName: String,
    val projectUri: Uri
)

data class AccountLibraryManifestImport(
    val schemaVersion: Int,
    val appVersion: String,
    val selectionMode: String,
    val exportedAt: Long,
    val playlistCount: Int,
    val exportedProjects: Int,
    val skippedPlaylists: Int,
    val failedPlaylists: Int,
    val missingProjectFiles: Int,
    val entries: List<AccountLibraryManifestEntry>
)

class IncrementalDeltaManifestException(
    message: String
) : IllegalArgumentException(message)

object AccountLibraryManifestImporter {
    private const val FORMAT =
        "ytm-importer-account-library-export"

    private const val MAX_SCHEMA_VERSION =
        3

    fun readManifest(
        resolver: ContentResolver,
        treeUri: Uri
    ): AccountLibraryManifestImport {
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
                "У вибраній папці немає manifest.json"
            )

        val raw =
            readUtf8Text(
                resolver = resolver,
                uri = manifestDocument.uri
            )

        val root = JSONObject(raw)

        require(
            root.optString("format") == FORMAT
        ) {
            "Це не YTM Importer account-export manifest"
        }

        val schemaVersion =
            root.optInt(
                "schemaVersion",
                -1
            )

        require(
            schemaVersion in 1..MAX_SCHEMA_VERSION
        ) {
            "Непідтримувана версія manifest: $schemaVersion"
        }

        val backupMode =
            if (schemaVersion >= 3) {
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

        if (
            backupMode ==
                "INCREMENTAL_DELTA"
        ) {
            throw IncrementalDeltaManifestException(
                "Це incremental delta backup."
            )
        }

        val items =
            root.optJSONArray("playlists")
                ?: throw IllegalArgumentException(
                    "У manifest немає списку playlists"
                )

        val declaredPlaylistCount =
            root.optInt(
                "playlistCount",
                items.length()
            )

        require(
            declaredPlaylistCount == items.length()
        ) {
            "manifest пошкоджено: playlistCount не збігається зі списком playlists"
        }

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

        val exportedRecords =
            exportedRecords(items)

        val declaredExported =
            root.optInt(
                "exportedProjects",
                exportedRecords.size
            )

        require(
            declaredExported == exportedRecords.size
        ) {
            "manifest пошкоджено: exportedProjects не збігається з EXPORTED записами"
        }

        var missingProjectFiles = 0

        val entries =
            exportedRecords.mapNotNull {
                    record ->

                val fileName =
                    nullableString(
                        record,
                        "fileName"
                    )

                if (fileName == null) {
                    missingProjectFiles += 1
                    return@mapNotNull null
                }

                val document =
                    documentsByName[fileName]
                        ?: children.firstOrNull {
                            it.displayName.equals(
                                fileName,
                                ignoreCase = true
                            )
                        }

                if (document == null) {
                    missingProjectFiles += 1
                    return@mapNotNull null
                }

                AccountLibraryManifestEntry(
                    playlistId =
                        record
                            .optString("playlistId")
                            .trim(),
                    title =
                        record
                            .optString(
                                "title",
                                "YTM Playlist"
                            )
                            .trim()
                            .ifBlank {
                                "YTM Playlist"
                            },
                    privacyStatus =
                        record
                            .optString(
                                "privacyStatus",
                                "unknown"
                            )
                            .trim()
                            .ifBlank {
                                "unknown"
                            },
                    sourceItemCount =
                        record.optLong(
                            "sourceItemCount",
                            0L
                        ),
                    exportedTrackCount =
                        record.optInt(
                            "exportedTrackCount",
                            0
                        ),
                    playlistItemsRequests =
                        record.optInt(
                            "playlistItemsRequests",
                            0
                        ),
                    fileName = fileName,
                    projectUri = document.uri
                )
            }

        require(entries.isNotEmpty()) {
            if (exportedRecords.isEmpty()) {
                "У manifest немає експортованих YTM Projects"
            } else {
                "Файли YTM Project з manifest не знайдені у вибраній папці"
            }
        }

        val selectionMode =
            if (schemaVersion >= 2) {
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
            } else {
                "ALL"
            }

        require(
            selectionMode in
                setOf(
                    "ALL",
                    "SELECTED"
                )
        ) {
            "Непідтримуваний selectionMode: $selectionMode"
        }

        return AccountLibraryManifestImport(
            schemaVersion = schemaVersion,
            appVersion =
                root
                    .optString("appVersion")
                    .trim(),
            selectionMode =
                selectionMode,
            exportedAt =
                root.optLong(
                    "exportedAt",
                    0L
                ),
            playlistCount =
                declaredPlaylistCount,
            exportedProjects =
                declaredExported,
            skippedPlaylists =
                root.optInt(
                    "skippedPlaylists",
                    0
                ),
            failedPlaylists =
                root.optInt(
                    "failedPlaylists",
                    0
                ),
            missingProjectFiles =
                missingProjectFiles,
            entries = entries
        )
    }

    fun loadProject(
        resolver: ContentResolver,
        entry: AccountLibraryManifestEntry
    ): PlaylistProjectImport {
        val raw =
            readUtf8Text(
                resolver = resolver,
                uri = entry.projectUri
            )

        require(
            PlaylistProjectCodec.isProject(raw)
        ) {
            "Файл ${entry.fileName} не є YTM Project"
        }

        val project =
            PlaylistProjectCodec.importProject(raw)

        if (
            entry.playlistId.isNotBlank() &&
            !project.sourcePlaylistId.isNullOrBlank()
        ) {
            require(
                entry.playlistId ==
                    project.sourcePlaylistId
            ) {
                "playlistId у manifest не збігається з YTM Project"
            }
        }

        if (
            entry.privacyStatus.isNotBlank() &&
            entry.privacyStatus != "unknown" &&
            !project.sourcePrivacyStatus.isNullOrBlank()
        ) {
            require(
                entry.privacyStatus.equals(
                    project.sourcePrivacyStatus,
                    ignoreCase = true
                )
            ) {
                "privacyStatus у manifest не збігається з YTM Project"
            }
        }

        return project
    }

    private fun exportedRecords(
        items: JSONArray
    ): List<JSONObject> {
        val records =
            mutableListOf<JSONObject>()

        for (index in 0 until items.length()) {
            val record =
                items.optJSONObject(index)
                    ?: continue

            if (
                record
                    .optString("status")
                    .trim()
                    .uppercase() == "EXPORTED"
            ) {
                records += record
            }
        }

        return records
    }

    private fun listDirectChildren(
        resolver: ContentResolver,
        treeUri: Uri
    ): List<DocumentEntry> {
        val treeDocumentId =
            DocumentsContract.getTreeDocumentId(
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
                    .COLUMN_DISPLAY_NAME,
                DocumentsContract
                    .Document
                    .COLUMN_MIME_TYPE
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
            ?.use { cursor ->
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

                val mimeColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_MIME_TYPE
                    )

                while (cursor.moveToNext()) {
                    val documentId =
                        cursor.getString(idColumn)
                            ?: continue

                    val displayName =
                        cursor.getString(nameColumn)
                            ?: continue

                    val mimeType =
                        cursor.getString(mimeColumn)
                            ?: ""

                    val documentUri =
                        DocumentsContract
                            .buildDocumentUriUsingTree(
                                treeUri,
                                documentId
                            )

                    result +=
                        DocumentEntry(
                            displayName =
                                displayName,
                            mimeType =
                                mimeType,
                            uri =
                                documentUri
                        )
                }
            }

        return result
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
            .bufferedReader(Charsets.UTF_8)
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

    private data class DocumentEntry(
        val displayName: String,
        val mimeType: String,
        val uri: Uri
    )
}
