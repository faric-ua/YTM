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

object AccountLibraryExporter {
    data class ExportRecord(
        val playlistId: String,
        val title: String,
        val privacyStatus: String,
        val sourceItemCount: Long,
        val exportedTrackCount: Int,
        val playlistItemsRequests: Int,
        val status: String,
        val fileName: String?,
        val error: String? = null
    )

    data class ExportSession(
        val folderUri: Uri,
        val folderName: String
    )

    fun createSessionFolder(
        resolver: ContentResolver,
        treeUri: Uri
    ): ExportSession {
        val treeDocumentId =
            DocumentsContract.getTreeDocumentId(treeUri)

        val rootDocumentUri =
            DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                treeDocumentId
            )

        val folderName =
            "YTM-Importer-Account-Export-" +
                SimpleDateFormat(
                    "yyyyMMdd-HHmmss",
                    Locale.US
                ).format(Date())

        val folderUri =
            DocumentsContract.createDocument(
                resolver,
                rootDocumentUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                folderName
            ) ?: error(
                "Не вдалося створити папку експорту"
            )

        return ExportSession(
            folderUri = folderUri,
            folderName = folderName
        )
    }

    fun writePlaylistProject(
        resolver: ContentResolver,
        session: ExportSession,
        playlistInfo: YouTubePlaylistInfo,
        playlist: ImportedPlaylist,
        appVersion: String
    ): String {
        val fileName =
            projectFileName(
                title = playlistInfo.title,
                playlistId = playlistInfo.id
            )

        val raw =
            PlaylistProjectCodec.exportAccountPlaylist(
                playlist = playlist,
                sourcePlaylistId = playlistInfo.id,
                privacyStatus = playlistInfo.privacyStatus,
                appVersion = appVersion
            )

        writeTextDocument(
            resolver = resolver,
            parentUri = session.folderUri,
            mimeType = "application/json",
            displayName = fileName,
            text = raw
        )

        return fileName
    }

    fun writeManifest(
        resolver: ContentResolver,
        session: ExportSession,
        appVersion: String,
        records: List<ExportRecord>
    ): String {
        val items = JSONArray()

        records.forEach { record ->
            items.put(
                JSONObject()
                    .put("playlistId", record.playlistId)
                    .put("title", record.title)
                    .put("privacyStatus", record.privacyStatus)
                    .put("sourceItemCount", record.sourceItemCount)
                    .put("exportedTrackCount", record.exportedTrackCount)
                    .put("playlistItemsRequests", record.playlistItemsRequests)
                    .put("status", record.status)
                    .put(
                        "fileName",
                        record.fileName ?: JSONObject.NULL
                    )
                    .put(
                        "error",
                        record.error ?: JSONObject.NULL
                    )
            )
        }

        val exported =
            records.count {
                it.status == "EXPORTED"
            }

        val skipped =
            records.count {
                it.status.startsWith("SKIPPED")
            }

        val failed =
            records.count {
                it.status == "FAILED"
            }

        val root =
            JSONObject()
                .put(
                    "format",
                    "ytm-importer-account-library-export"
                )
                .put("schemaVersion", 1)
                .put("appVersion", appVersion)
                .put("exportedAt", System.currentTimeMillis())
                .put("playlistCount", records.size)
                .put("exportedProjects", exported)
                .put("skippedPlaylists", skipped)
                .put("failedPlaylists", failed)
                .put(
                    "playlistItemsRequests",
                    records.sumOf {
                        it.playlistItemsRequests
                    }
                )
                .put("playlists", items)

        val fileName = "manifest.json"

        writeTextDocument(
            resolver = resolver,
            parentUri = session.folderUri,
            mimeType = "application/json",
            displayName = fileName,
            text = root.toString(2)
        )

        return fileName
    }

    private fun projectFileName(
        title: String,
        playlistId: String
    ): String {
        val forbidden =
            setOf(
                '\\',
                '/',
                ':',
                '*',
                '?',
                '"',
                '<',
                '>',
                '|'
            )

        val cleaned =
            buildString {
                title.forEach { char ->
                    append(
                        if (
                            char.code < 32 ||
                            char in forbidden
                        ) {
                            '_'
                        } else {
                            char
                        }
                    )
                }
            }
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim()
                .trim('.')
                .take(80)
                .ifBlank {
                    "playlist"
                }

        val suffix =
            playlistId
                .take(10)
                .ifBlank {
                    "unknown"
                }

        return "${cleaned}_${suffix}.ytm-project.json"
    }

    private fun writeTextDocument(
        resolver: ContentResolver,
        parentUri: Uri,
        mimeType: String,
        displayName: String,
        text: String
    ) {
        val documentUri =
            DocumentsContract.createDocument(
                resolver,
                parentUri,
                mimeType,
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
            .bufferedWriter(Charsets.UTF_8)
            .use {
                it.write(text)
            }
    }
}
