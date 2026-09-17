#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path

class PatchError(RuntimeError):
    pass

OPS = [('replace',
  'app/build.gradle.kts',
  '        versionCode = 59\n        versionName = "1.4.25"',
  '        versionCode = 60\n        versionName = "1.4.26"',
  'release version'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  'import java.util.concurrent.Executors\n',
  'import java.util.concurrent.Executors\nimport org.json.JSONArray\nimport org.json.JSONObject\n',
  'ImportActivity JSON imports'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '    private val exportFolderRequestCode =\n        2302\n\n    private val executor =\n',
  '    private val exportFolderRequestCode =\n'
  '        2302\n'
  '\n'
  '    private val selectiveExportFolderRequestCode =\n'
  '        2303\n'
  '\n'
  '    private var pendingSelectiveExport:\n'
  '        List<YouTubePlaylistInfo> =\n'
  '        emptyList()\n'
  '\n'
  '    private val executor =\n',
  'selective export request/state fields'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '        currentPlaylistStore =\n            CurrentPlaylistStore(this)\n\n        buildUi()\n',
  '        currentPlaylistStore =\n'
  '            CurrentPlaylistStore(this)\n'
  '\n'
  '        pendingSelectiveExport =\n'
  '            decodeSelectiveExportState(\n'
  '                savedInstanceState\n'
  '                    ?.getString(\n'
  '                        STATE_SELECTIVE_EXPORT\n'
  '                    )\n'
  '            )\n'
  '\n'
  '        buildUi()\n',
  'restore selective export state'),
 ('insert',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '    override fun onDestroy() {\n',
  '    override fun onSaveInstanceState(\n'
  '        outState: Bundle\n'
  '    ) {\n'
  '        outState.putString(\n'
  '            STATE_SELECTIVE_EXPORT,\n'
  '            encodeSelectiveExportState(\n'
  '                pendingSelectiveExport\n'
  '            )\n'
  '        )\n'
  '\n'
  '        super.onSaveInstanceState(\n'
  '            outState\n'
  '        )\n'
  '    }\n'
  '\n',
  'override fun onSaveInstanceState(',
  'save selective export state'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '            exportFolderRequestCode ->\n'
  '                data\n'
  '                    ?.data\n'
  '                    ?.let(\n'
  '                        ::exportAllYtmPlaylistsToFolder\n'
  '                    )\n',
  '            exportFolderRequestCode ->\n'
  '                data\n'
  '                    ?.data\n'
  '                    ?.let(\n'
  '                        ::exportAllYtmPlaylistsToFolder\n'
  '                    )\n'
  '\n'
  '            selectiveExportFolderRequestCode ->\n'
  '                data\n'
  '                    ?.data\n'
  '                    ?.let(\n'
  '                        ::exportSelectedYtmPlaylistsToFolder\n'
  '                    )\n',
  'selective folder result route'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '                addView(\n'
  '                    actionButton(\n'
  '                        label =\n'
  '                            "Експортувати всі плейлисти в папку",\n'
  '                        primary = false,\n'
  '                        topMarginDp = 10\n'
  '                    ) {\n'
  '                        chooseYtmExportFolder()\n'
  '                    }\n'
  '                )\n',
  '                addView(\n'
  '                    actionButton(\n'
  '                        label =\n'
  '                            "Вибрати плейлисти для експорту",\n'
  '                        primary = false,\n'
  '                        topMarginDp = 10\n'
  '                    ) {\n'
  '                        chooseSelectiveYtmExport()\n'
  '                    }\n'
  '                )\n'
  '\n'
  '                addView(\n'
  '                    actionButton(\n'
  '                        label =\n'
  '                            "Експортувати всі плейлисти в папку",\n'
  '                        primary = false,\n'
  '                        topMarginDp = 8\n'
  '                    ) {\n'
  '                        chooseYtmExportFolder()\n'
  '                    }\n'
  '                )\n',
  'selective export action button'),
 ('insert',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '    private fun chooseYtmExportFolder() {\n',
  '    private fun chooseSelectiveYtmExport() {\n'
  '        val token =\n'
  '            AuthSessionStore\n'
  '                .current()\n'
  '                .accessToken\n'
  '\n'
  '        if (token.isNullOrBlank()) {\n'
  '            toast(\n'
  '                "Спочатку підключіть Google/YTM у кроці 2 на головному екрані."\n'
  '            )\n'
  '            return\n'
  '        }\n'
  '\n'
  '        toast(\n'
  '            "Завантажую плейлисти для вибору…"\n'
  '        )\n'
  '\n'
  '        executor.execute {\n'
  '            val result =\n'
  '                runCatching {\n'
  '                    api.listMyPlaylists(token)\n'
  '                }\n'
  '\n'
  '            runOnUiThread {\n'
  '                if (\n'
  '                    isFinishing ||\n'
  '                    isDestroyed\n'
  '                ) {\n'
  '                    return@runOnUiThread\n'
  '                }\n'
  '\n'
  '                result.onSuccess {\n'
  '                        playlists ->\n'
  '\n'
  '                    if (playlists.isEmpty()) {\n'
  '                        toast(\n'
  '                            "У підключеному акаунті немає доступних плейлистів."\n'
  '                        )\n'
  '                        return@onSuccess\n'
  '                    }\n'
  '\n'
  '                    showSelectiveYtmExportPicker(\n'
  '                        playlists\n'
  '                    )\n'
  '                }.onFailure { error ->\n'
  '                    toast(\n'
  '                        error.message\n'
  '                            ?: "Не вдалося завантажити список плейлистів"\n'
  '                    )\n'
  '                }\n'
  '            }\n'
  '        }\n'
  '    }\n'
  '\n'
  '    private fun showSelectiveYtmExportPicker(\n'
  '        playlists: List<YouTubePlaylistInfo>\n'
  '    ) {\n'
  '        val previousSelection =\n'
  '            pendingSelectiveExport\n'
  '\n'
  '        val selectedIds =\n'
  '            previousSelection\n'
  '                .mapTo(\n'
  '                    linkedSetOf()\n'
  '                ) {\n'
  '                    it.id\n'
  '                }\n'
  '\n'
  '        val labels =\n'
  '            playlists\n'
  '                .map { playlist ->\n'
  '                    buildString {\n'
  '                        append(playlist.title)\n'
  '                        append("\\n")\n'
  '                        append(playlist.itemCount)\n'
  '                        append(" треків • ")\n'
  '                        append(\n'
  '                            when (\n'
  '                                playlist.privacyStatus\n'
  '                            ) {\n'
  '                                "public" ->\n'
  '                                    "публічний"\n'
  '\n'
  '                                "unlisted" ->\n'
  '                                    "за посиланням"\n'
  '\n'
  '                                else ->\n'
  '                                    "приватний"\n'
  '                            }\n'
  '                        )\n'
  '                    }\n'
  '                }\n'
  '                .toTypedArray()\n'
  '\n'
  '        val checked =\n'
  '            BooleanArray(playlists.size) {\n'
  '                    index ->\n'
  '\n'
  '                playlists[index].id in\n'
  '                    selectedIds\n'
  '            }\n'
  '\n'
  '        UiChrome.alertBuilder(this)\n'
  '            .setTitle(\n'
  '                "Вибрати плейлисти для експорту"\n'
  '            )\n'
  '            .setMultiChoiceItems(\n'
  '                labels,\n'
  '                checked\n'
  '            ) {\n'
  '                    _,\n'
  '                    which,\n'
  '                    isChecked ->\n'
  '\n'
  '                checked[which] =\n'
  '                    isChecked\n'
  '\n'
  '                val id =\n'
  '                    playlists[which].id\n'
  '\n'
  '                if (isChecked) {\n'
  '                    selectedIds += id\n'
  '                } else {\n'
  '                    selectedIds -= id\n'
  '                }\n'
  '\n'
  '                pendingSelectiveExport =\n'
  '                    playlists.filter {\n'
  '                        it.id in selectedIds\n'
  '                    }\n'
  '            }\n'
  '            .setNegativeButton(\n'
  '                "Скасувати"\n'
  '            ) {\n'
  '                    _,\n'
  '                    _ ->\n'
  '\n'
  '                pendingSelectiveExport =\n'
  '                    previousSelection\n'
  '            }\n'
  '            .setPositiveButton(\n'
  '                "Далі"\n'
  '            ) {\n'
  '                    _,\n'
  '                    _ ->\n'
  '\n'
  '                val selected =\n'
  '                    playlists\n'
  '                        .filterIndexed {\n'
  '                                index,\n'
  '                                _ ->\n'
  '\n'
  '                            checked[index]\n'
  '                        }\n'
  '\n'
  '                if (selected.isEmpty()) {\n'
  '                    pendingSelectiveExport =\n'
  '                        previousSelection\n'
  '\n'
  '                    toast(\n'
  '                        "Виберіть хоча б один плейлист."\n'
  '                    )\n'
  '                } else {\n'
  '                    pendingSelectiveExport =\n'
  '                        selected\n'
  '\n'
  '                    chooseSelectiveYtmExportFolder()\n'
  '                }\n'
  '            }\n'
  '            .show()\n'
  '    }\n'
  '\n'
  '    private fun chooseSelectiveYtmExportFolder() {\n'
  '        if (pendingSelectiveExport.isEmpty()) {\n'
  '            toast(\n'
  '                "Спочатку виберіть плейлисти для експорту."\n'
  '            )\n'
  '            return\n'
  '        }\n'
  '\n'
  '        val token =\n'
  '            AuthSessionStore\n'
  '                .current()\n'
  '                .accessToken\n'
  '\n'
  '        if (token.isNullOrBlank()) {\n'
  '            toast(\n'
  '                "Авторизація Google/YTM недоступна. Підключіть акаунт ще раз."\n'
  '            )\n'
  '            return\n'
  '        }\n'
  '\n'
  '        val intent =\n'
  '            Intent(\n'
  '                Intent.ACTION_OPEN_DOCUMENT_TREE\n'
  '            ).apply {\n'
  '                addFlags(\n'
  '                    Intent.FLAG_GRANT_READ_URI_PERMISSION or\n'
  '                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or\n'
  '                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or\n'
  '                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION\n'
  '                )\n'
  '            }\n'
  '\n'
  '        startActivityForResult(\n'
  '            intent,\n'
  '            selectiveExportFolderRequestCode\n'
  '        )\n'
  '    }\n'
  '\n'
  '    private fun encodeSelectiveExportState(\n'
  '        playlists: List<YouTubePlaylistInfo>\n'
  '    ): String =\n'
  '        JSONArray().apply {\n'
  '            playlists.forEach {\n'
  '                    playlist ->\n'
  '\n'
  '                put(\n'
  '                    JSONObject()\n'
  '                        .put(\n'
  '                            "id",\n'
  '                            playlist.id\n'
  '                        )\n'
  '                        .put(\n'
  '                            "title",\n'
  '                            playlist.title\n'
  '                        )\n'
  '                        .put(\n'
  '                            "privacyStatus",\n'
  '                            playlist.privacyStatus\n'
  '                        )\n'
  '                        .put(\n'
  '                            "itemCount",\n'
  '                            playlist.itemCount\n'
  '                        )\n'
  '                )\n'
  '            }\n'
  '        }.toString()\n'
  '\n'
  '    private fun decodeSelectiveExportState(\n'
  '        raw: String?\n'
  '    ): List<YouTubePlaylistInfo> {\n'
  '        if (raw.isNullOrBlank()) {\n'
  '            return emptyList()\n'
  '        }\n'
  '\n'
  '        return runCatching {\n'
  '            val array =\n'
  '                JSONArray(raw)\n'
  '\n'
  '            buildList {\n'
  '                for (\n'
  '                    index in\n'
  '                    0 until array.length()\n'
  '                ) {\n'
  '                    val item =\n'
  '                        array.getJSONObject(index)\n'
  '\n'
  '                    add(\n'
  '                        YouTubePlaylistInfo(\n'
  '                            id =\n'
  '                                item.getString("id"),\n'
  '                            title =\n'
  '                                item.getString("title"),\n'
  '                            privacyStatus =\n'
  '                                item.getString(\n'
  '                                    "privacyStatus"\n'
  '                                ),\n'
  '                            itemCount =\n'
  '                                item.getLong(\n'
  '                                    "itemCount"\n'
  '                                )\n'
  '                        )\n'
  '                    )\n'
  '                }\n'
  '            }\n'
  '        }.getOrDefault(\n'
  '            emptyList()\n'
  '        )\n'
  '    }\n'
  '\n',
  'private fun chooseSelectiveYtmExport()',
  'selective export picker/state methods'),
 ('range',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '    private fun exportAllYtmPlaylistsToFolder(\n',
  '    private data class BulkExportResult(\n',
  '    private fun exportAllYtmPlaylistsToFolder(\n'
  '        treeUri: Uri\n'
  '    ) {\n'
  '        val token =\n'
  '            AuthSessionStore\n'
  '                .current()\n'
  '                .accessToken\n'
  '\n'
  '        if (token.isNullOrBlank()) {\n'
  '            toast(\n'
  '                "Авторизація Google/YTM недоступна. Підключіть акаунт ще раз."\n'
  '            )\n'
  '            return\n'
  '        }\n'
  '\n'
  '        persistExportFolderPermission(\n'
  '            treeUri\n'
  '        )\n'
  '\n'
  '        toast(\n'
  '            "Готую read-only експорт плейлистів…"\n'
  '        )\n'
  '\n'
  '        executor.execute {\n'
  '            val result =\n'
  '                runCatching {\n'
  '                    val playlists =\n'
  '                        api.listMyPlaylists(token)\n'
  '\n'
  '                    if (playlists.isEmpty()) {\n'
  '                        error(\n'
  '                            "У підключеному акаунті немає доступних плейлистів."\n'
  '                        )\n'
  '                    }\n'
  '\n'
  '                    runOnUiThread {\n'
  '                        if (\n'
  '                            !isFinishing &&\n'
  '                            !isDestroyed\n'
  '                        ) {\n'
  '                            toast(\n'
  '                                "Знайдено ${playlists.size} плейлистів. Експортую…"\n'
  '                            )\n'
  '                        }\n'
  '                    }\n'
  '\n'
  '                    exportAccountPlaylistsToFolder(\n'
  '                        token = token,\n'
  '                        treeUri = treeUri,\n'
  '                        playlists = playlists,\n'
  '                        selectionMode = "ALL"\n'
  '                    )\n'
  '                }\n'
  '\n'
  '            showBulkExportResult(\n'
  '                result\n'
  '            )\n'
  '        }\n'
  '    }\n'
  '\n'
  '    private fun exportSelectedYtmPlaylistsToFolder(\n'
  '        treeUri: Uri\n'
  '    ) {\n'
  '        val token =\n'
  '            AuthSessionStore\n'
  '                .current()\n'
  '                .accessToken\n'
  '\n'
  '        if (token.isNullOrBlank()) {\n'
  '            toast(\n'
  '                "Авторизація Google/YTM недоступна. Підключіть акаунт ще раз."\n'
  '            )\n'
  '            return\n'
  '        }\n'
  '\n'
  '        val selected =\n'
  '            pendingSelectiveExport\n'
  '\n'
  '        if (selected.isEmpty()) {\n'
  '            toast(\n'
  '                "Вибрані плейлисти не відновлено. Повторіть вибір."\n'
  '            )\n'
  '            return\n'
  '        }\n'
  '\n'
  '        persistExportFolderPermission(\n'
  '            treeUri\n'
  '        )\n'
  '\n'
  '        toast(\n'
  '            "Експортую вибрані плейлисти: ${selected.size}…"\n'
  '        )\n'
  '\n'
  '        executor.execute {\n'
  '            val result =\n'
  '                runCatching {\n'
  '                    exportAccountPlaylistsToFolder(\n'
  '                        token = token,\n'
  '                        treeUri = treeUri,\n'
  '                        playlists = selected,\n'
  '                        selectionMode = "SELECTED"\n'
  '                    )\n'
  '                }\n'
  '\n'
  '            showBulkExportResult(\n'
  '                result\n'
  '            )\n'
  '        }\n'
  '    }\n'
  '\n'
  '    private fun persistExportFolderPermission(\n'
  '        treeUri: Uri\n'
  '    ) {\n'
  '        runCatching {\n'
  '            contentResolver\n'
  '                .takePersistableUriPermission(\n'
  '                    treeUri,\n'
  '                    Intent.FLAG_GRANT_READ_URI_PERMISSION or\n'
  '                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION\n'
  '                )\n'
  '        }\n'
  '    }\n'
  '\n'
  '    private fun exportAccountPlaylistsToFolder(\n'
  '        token: String,\n'
  '        treeUri: Uri,\n'
  '        playlists: List<YouTubePlaylistInfo>,\n'
  '        selectionMode: String\n'
  '    ): BulkExportResult {\n'
  '        val session =\n'
  '            AccountLibraryExporter\n'
  '                .createSessionFolder(\n'
  '                    resolver =\n'
  '                        contentResolver,\n'
  '                    treeUri =\n'
  '                        treeUri\n'
  '                )\n'
  '\n'
  '        val records =\n'
  '            mutableListOf<\n'
  '                AccountLibraryExporter.ExportRecord\n'
  '            >()\n'
  '\n'
  '        playlists.forEach {\n'
  '                playlistInfo ->\n'
  '\n'
  '            if (\n'
  '                playlistInfo.itemCount <= 0\n'
  '            ) {\n'
  '                records +=\n'
  '                    AccountLibraryExporter.ExportRecord(\n'
  '                        playlistId =\n'
  '                            playlistInfo.id,\n'
  '                        title =\n'
  '                            playlistInfo.title,\n'
  '                        privacyStatus =\n'
  '                            playlistInfo.privacyStatus,\n'
  '                        sourceItemCount =\n'
  '                            playlistInfo.itemCount,\n'
  '                        exportedTrackCount = 0,\n'
  '                        playlistItemsRequests = 0,\n'
  '                        status =\n'
  '                            "SKIPPED_EMPTY",\n'
  '                        fileName = null\n'
  '                    )\n'
  '\n'
  '                return@forEach\n'
  '            }\n'
  '\n'
  '            runCatching {\n'
  '                api.listPlaylistTracks(\n'
  '                    accessToken =\n'
  '                        token,\n'
  '                    playlistId =\n'
  '                        playlistInfo.id\n'
  '                )\n'
  '            }.onSuccess {\n'
  '                    loaded ->\n'
  '\n'
  '                if (\n'
  '                    loaded.tracks.isEmpty()\n'
  '                ) {\n'
  '                    records +=\n'
  '                        AccountLibraryExporter.ExportRecord(\n'
  '                            playlistId =\n'
  '                                playlistInfo.id,\n'
  '                            title =\n'
  '                                playlistInfo.title,\n'
  '                            privacyStatus =\n'
  '                                playlistInfo.privacyStatus,\n'
  '                            sourceItemCount =\n'
  '                                playlistInfo.itemCount,\n'
  '                            exportedTrackCount = 0,\n'
  '                            playlistItemsRequests =\n'
  '                                loaded.requestCount,\n'
  '                            status =\n'
  '                                "SKIPPED_NO_ACCESSIBLE_TRACKS",\n'
  '                            fileName = null\n'
  '                        )\n'
  '                } else {\n'
  '                    val imported =\n'
  '                        ImportedPlaylist(\n'
  '                            name =\n'
  '                                playlistInfo.title,\n'
  '                            tracks =\n'
  '                                loaded.tracks\n'
  '                                    .toMutableList()\n'
  '                        )\n'
  '\n'
  '                    val fileName =\n'
  '                        AccountLibraryExporter\n'
  '                            .writePlaylistProject(\n'
  '                                resolver =\n'
  '                                    contentResolver,\n'
  '                                session =\n'
  '                                    session,\n'
  '                                playlistInfo =\n'
  '                                    playlistInfo,\n'
  '                                playlist =\n'
  '                                    imported,\n'
  '                                appVersion =\n'
  '                                    BuildConfig.VERSION_NAME\n'
  '                            )\n'
  '\n'
  '                    records +=\n'
  '                        AccountLibraryExporter.ExportRecord(\n'
  '                            playlistId =\n'
  '                                playlistInfo.id,\n'
  '                            title =\n'
  '                                playlistInfo.title,\n'
  '                            privacyStatus =\n'
  '                                playlistInfo.privacyStatus,\n'
  '                            sourceItemCount =\n'
  '                                playlistInfo.itemCount,\n'
  '                            exportedTrackCount =\n'
  '                                imported.tracks.size,\n'
  '                            playlistItemsRequests =\n'
  '                                loaded.requestCount,\n'
  '                            status =\n'
  '                                "EXPORTED",\n'
  '                            fileName =\n'
  '                                fileName\n'
  '                        )\n'
  '                }\n'
  '            }.onFailure {\n'
  '                    error ->\n'
  '\n'
  '                records +=\n'
  '                    AccountLibraryExporter.ExportRecord(\n'
  '                        playlistId =\n'
  '                            playlistInfo.id,\n'
  '                        title =\n'
  '                            playlistInfo.title,\n'
  '                        privacyStatus =\n'
  '                            playlistInfo.privacyStatus,\n'
  '                        sourceItemCount =\n'
  '                            playlistInfo.itemCount,\n'
  '                        exportedTrackCount = 0,\n'
  '                        playlistItemsRequests = 0,\n'
  '                        status =\n'
  '                            "FAILED",\n'
  '                        fileName = null,\n'
  '                        error =\n'
  '                            error.message\n'
  '                                ?: error\n'
  '                                    .javaClass\n'
  '                                    .simpleName\n'
  '                    )\n'
  '            }\n'
  '        }\n'
  '\n'
  '        val manifestFile =\n'
  '            AccountLibraryExporter\n'
  '                .writeManifest(\n'
  '                    resolver =\n'
  '                        contentResolver,\n'
  '                    session =\n'
  '                        session,\n'
  '                    appVersion =\n'
  '                        BuildConfig.VERSION_NAME,\n'
  '                    records =\n'
  '                        records,\n'
  '                    selectionMode =\n'
  '                        selectionMode\n'
  '                )\n'
  '\n'
  '        return BulkExportResult(\n'
  '            selectionMode =\n'
  '                selectionMode,\n'
  '            folderName =\n'
  '                session.folderName,\n'
  '            manifestFile =\n'
  '                manifestFile,\n'
  '            playlistCount =\n'
  '                records.size,\n'
  '            exportedProjects =\n'
  '                records.count {\n'
  '                    it.status ==\n'
  '                        "EXPORTED"\n'
  '                },\n'
  '            skippedPlaylists =\n'
  '                records.count {\n'
  '                    it.status\n'
  '                        .startsWith(\n'
  '                            "SKIPPED"\n'
  '                        )\n'
  '                },\n'
  '            failedPlaylists =\n'
  '                records.count {\n'
  '                    it.status ==\n'
  '                        "FAILED"\n'
  '                },\n'
  '            playlistItemsRequests =\n'
  '                records.sumOf {\n'
  '                    it.playlistItemsRequests\n'
  '                }\n'
  '        )\n'
  '    }\n'
  '\n'
  '    private fun showBulkExportResult(\n'
  '        result: Result<BulkExportResult>\n'
  '    ) {\n'
  '        runOnUiThread {\n'
  '            if (\n'
  '                isFinishing ||\n'
  '                isDestroyed\n'
  '            ) {\n'
  '                return@runOnUiThread\n'
  '            }\n'
  '\n'
  '            result.onSuccess {\n'
  '                    summary ->\n'
  '\n'
  '                val scopeLabel =\n'
  '                    if (\n'
  '                        summary.selectionMode ==\n'
  '                            "SELECTED"\n'
  '                    ) {\n'
  '                        "Вибрано для експорту"\n'
  '                    } else {\n'
  '                        "Плейлистів акаунта"\n'
  '                    }\n'
  '\n'
  '                UiChrome.showMessageDialog(\n'
  '                    activity = this,\n'
  '                    title =\n'
  '                        "Експорт завершено",\n'
  '                    message =\n'
  '                        "$scopeLabel: ${summary.playlistCount}\\n" +\n'
  '                            "Збережено YTM Project: ${summary.exportedProjects}\\n" +\n'
  '                            "Пропущено: ${summary.skippedPlaylists}\\n" +\n'
  '                            "Помилок: ${summary.failedPlaylists}\\n" +\n'
  '                            "playlistItems.list: ${summary.playlistItemsRequests} request(s)\\n\\n" +\n'
  '                            "Папка: ${summary.folderName}\\n" +\n'
  '                            "Індекс: ${summary.manifestFile}",\n'
  '                    actions =\n'
  '                        listOf(\n'
  '                            UiChrome.DialogAction(\n'
  '                                label =\n'
  '                                    "Закрити",\n'
  '                                tone =\n'
  '                                    UiChrome.ActionTone.ACCENT,\n'
  '                                onClick = {}\n'
  '                            )\n'
  '                        )\n'
  '                )\n'
  '            }.onFailure { error ->\n'
  '                UiChrome.showMessageDialog(\n'
  '                    activity = this,\n'
  '                    title =\n'
  '                        "Експорт не завершено",\n'
  '                    message =\n'
  '                        error.message\n'
  '                            ?: "Невідома помилка експорту",\n'
  '                    actions =\n'
  '                        listOf(\n'
  '                            UiChrome.DialogAction(\n'
  '                                label =\n'
  '                                    "Закрити",\n'
  '                                tone =\n'
  '                                    UiChrome.ActionTone.ACCENT,\n'
  '                                onClick = {}\n'
  '                            )\n'
  '                        )\n'
  '                )\n'
  '            }\n'
  '        }\n'
  '    }\n'
  '\n',
  'private fun exportSelectedYtmPlaylistsToFolder(',
  'shared all/selective account export core'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '    private data class BulkExportResult(\n        val folderName: String,\n',
  '    private data class BulkExportResult(\n'
  '        val selectionMode: String,\n'
  '        val folderName: String,\n',
  'bulk export result selection mode'),
 ('insert',
  'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt',
  '        const val EXTRA_IMPORT_MESSAGE =\n',
  '        private const val STATE_SELECTIVE_EXPORT =\n            "selective_export_playlists"\n\n',
  'private const val STATE_SELECTIVE_EXPORT =',
  'selective export saved-state key'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt',
  '        appVersion: String,\n        records: List<ExportRecord>\n    ): String {\n',
  '        appVersion: String,\n'
  '        records: List<ExportRecord>,\n'
  '        selectionMode: String = "ALL"\n'
  '    ): String {\n',
  'manifest selectionMode parameter'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt',
  '                .put("schemaVersion", 1)\n                .put("appVersion", appVersion)\n',
  '                .put("schemaVersion", 2)\n'
  '                .put("appVersion", appVersion)\n'
  '                .put(\n'
  '                    "selectionMode",\n'
  '                    selectionMode\n'
  '                )\n',
  'manifest schema v2 selection mode'),
 ('replace',
  'RELEASE_TEST_STATUS.md',
  '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** | Blue '
  'Home/Review/Destination/History/Queue/Data and Neon Data/Service visual paths passed. Privacy radio, '
  'short hints and amber Security semantics confirmed. Import and non-empty Queue card were not separately '
  'tested. |',
  '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** | Blue '
  'Home/Review/Destination/History/Queue/Data and Neon Data/Service visual paths passed. Privacy radio, '
  'short hints and amber Security semantics confirmed. Import and non-empty Queue card were not separately '
  'tested. |\n'
  '| v1.4.26 | **NOT TESTED YET** | Selective Account Export: choose several connected-account playlists, '
  'export only the selection, manifest schema v2 with selectionMode. |',
  'release status v1.4.26'),
 ('replace',
  'PROJECT_STATUS.txt',
  'Version: 1.4.25\nVersion code: 59',
  'Version: 1.4.26\nVersion code: 60',
  'project version'),
 ('replace',
  'PROJECT_STATUS.txt',
  'v1.4.25 PARTIALLY PHONE-TESTED — ACCENT CARD TESTED PATHS PASS',
  'v1.4.25 PARTIALLY PHONE-TESTED — ACCENT CARD TESTED PATHS PASS\nv1.4.26 NOT TESTED YET',
  'project v1.4.26 test status'),
 ('replace',
  'BACKLOG.md',
  '## Current\nv1.4.25 — Accent Card System',
  '## Current\nv1.4.26 — Selective Account Export',
  'BACKLOG current release'),
 ('insert',
  'BACKLOG.md',
  '## Next\n',
  '## v1.4.26\n'
  '- [x] add selective connected-account playlist export\n'
  '- [x] keep existing single-import and export-all flows\n'
  '- [x] multi-select picker for account playlists\n'
  '- [x] prevent empty selection from opening folder picker\n'
  '- [x] preserve confirmed selection through saved-instance state\n'
  '- [x] process playlistItems only for selected playlists\n'
  '- [x] preserve exact videoId/source playlist id/privacy\n'
  '- [x] manifest schema v2 + `selectionMode`\n'
  '- [x] add static audit + phone-test plan\n'
  '- [x] add tutorial chapter `06_ACCOUNT_LIBRARY_EXPORT.md`\n'
  '- [ ] GitHub build\n'
  '- [ ] phone test: selective picker\n'
  '- [ ] phone test: exactly 2-playlist export\n'
  '- [ ] verify 2 projects + manifest\n'
  '- [ ] verify manifest `selectionMode = SELECTED`\n'
  '- [ ] reopen one exported project and verify exact videoId round trip\n'
  '\n',
  '## v1.4.26\n',
  'BACKLOG v1.4.26 section'),
 ('insert',
  'BACKLOG.md',
  '## Later bug-fix wave\n',
  '## Future product plan — localization + exclusive skin\n'
  '- [ ] Localization Wave: move user-facing strings to Android resources\n'
  '- [ ] Ukrainian (`uk`) language\n'
  '- [ ] Korean (`ko`) language\n'
  '- [ ] English (`en`) language\n'
  '- [ ] phone-test Korean text fit on primary screens/dialogs\n'
  '- [ ] Yerin Exclusive hidden skin\n'
  '- [ ] unlock Yerin skin by exact canonical public TikTok profile URL supplied later\n'
  '- [ ] do not store/guess the TikTok URL before it is explicitly provided\n'
  '- [ ] document that URL-only unlock is a hidden feature gate, not secure authentication\n'
  '- [ ] keep Yerin skin visual-only: no change to import/search/write semantics\n'
  '- [ ] tutorial chapter: internationalization\n'
  '- [ ] tutorial chapter: hidden feature/unlock mechanism\n'
  '\n',
  '## Future product plan — localization + exclusive skin',
  'future localization/Yerin skin plan'),
 ('insert',
  'CHANGELOG.md',
  '## v1.4.25\n',
  '## v1.4.26\n'
  '- Added read-only selective export of multiple connected-account playlists.\n'
  '- Added checkbox multi-select before Android folder selection.\n'
  '- Confirmed selection is preserved in Activity saved-instance state.\n'
  '- Selected export processes playlistItems only for the chosen playlists.\n'
  '- Existing export-all flow remains available.\n'
  '- Account export manifest schema advanced to v2 with `selectionMode = ALL | SELECTED`.\n'
  '- Added selective-export static audit, phone QA plan and tutorial chapter.\n'
  '- Added future product roadmap for Ukrainian/Korean/English localization and the hidden Yerin Exclusive '
  'skin.\n'
  '- versionCode 60 / versionName 1.4.26.\n'
  '- v1.4.26 = NOT PHONE-TESTED YET.\n'
  '\n',
  '## v1.4.26\n',
  'CHANGELOG v1.4.26'),
 ('insert',
  'scripts/qa-plan-audit.sh',
  'grep -Fq \'| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |\' "$BUG" \\\n',
  'grep -Fq \'| v1.4.26 | **NOT TESTED YET** |\' "$STATUS" \\\n'
  '  || fail "v1.4.26 must start NOT TESTED YET"\n',
  'v1.4.26 must start NOT TESTED YET',
  'qa-plan v1.4.26 status guard'),
 ('replace',
  'scripts/release-preflight.sh',
  'check_file "docs/v.1.4.25/RELEASE.md"',
  'check_file "docs/v.1.4.26/RELEASE.md"',
  'preflight v1.4.26 release doc'),
 ('replace',
  'scripts/release-preflight.sh',
  'check_file "docs/v.1.4.25/REGRESSION_CHECKLIST.md"',
  'check_file "docs/v.1.4.26/REGRESSION_CHECKLIST.md"',
  'preflight v1.4.26 checklist'),
 ('replace',
  'scripts/release-preflight.sh',
  'python scripts/v1425-apply-selftest.py\nbash scripts/v1425-accent-card-audit.sh',
  'python -B scripts/v1426-apply-selftest.py\nbash scripts/v1426-selective-export-audit.sh',
  'active v1.4.26 selftest/audit'),
 ('replace',
  'scripts/release-preflight.sh',
  'grep -q \'versionCode = 59\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 59"',
  'grep -q \'versionCode = 60\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 60"',
  'preflight versionCode 60'),
 ('replace',
  'scripts/release-preflight.sh',
  'grep -q \'versionName = "1.4.25"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.25"\'',
  'grep -q \'versionName = "1.4.26"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.26"\'',
  'preflight versionName 1.4.26')]
PROJECT_FOCUS = '\nv1.4.26 focus:\n- read-only selective account-library export\n- multi-select several connected-account playlists\n- export only confirmed selection\n- preserve exact videoId/source playlist id/privacy\n- manifest schema v2 with selectionMode ALL/SELECTED\n- selection survives Activity saved-instance restoration\n- existing single-import and export-all flows remain\n- no remote playlist write API in account export\n\nFuture product direction:\n- Ukrainian / Korean / English localization\n- hidden Yerin Exclusive visual skin\n- Yerin skin unlock uses exact canonical public TikTok profile URL supplied later\n- URL-only unlock is a feature gate, not secure authentication\n'
ROADMAP_OLD = '- `06_ANDROID_PROJECT_SETUP.md` — структура Android/Kotlin проєкту;\n- `07_IMPORT_AND_PROJECT_FORMAT.md` — імпорт та YTM Project;\n- `08_GOOGLE_YOUTUBE_AUTH.md` — авторизація, session state, recovery;\n- `09_YOUTUBE_API_AND_QUOTA.md` — API requests, quota, failure modes;\n- `10_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers;\n- `11_DESTINATION_AND_DUPLICATES.md` — existing playlist, duplicate scan, write plan;\n- `12_PENDING_QUEUE_AND_RECOVERY.md` — відкладені операції;\n- `13_BACKUP_AND_EXPORT.md` — local backup, account bulk export, manifest;\n- `14_THEME_SYSTEM.md` — palette, semantic colors, Accent Card System;\n- `15_GITHUB_ACTIONS_RELEASE.md` — signed APK, checksum, artifact;\n- `16_BUILD_A_FEATURE_FROM_ZERO.md` — повний практичний feature exercise;\n- `17_RECREATE_YTM_IMPORTER.md` — фінальний покроковий прохід від чистого repo.\n'
ROADMAP_NEW = '- `06_ACCOUNT_LIBRARY_EXPORT.md` — one / all / selective account export, exact videoId, manifest;\n- `07_ANDROID_PROJECT_SETUP.md` — структура Android/Kotlin проєкту;\n- `08_IMPORT_AND_PROJECT_FORMAT.md` — імпорт та YTM Project;\n- `09_GOOGLE_YOUTUBE_AUTH.md` — авторизація, session state, recovery;\n- `10_YOUTUBE_API_AND_QUOTA.md` — API requests, quota, failure modes;\n- `11_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers;\n- `12_DESTINATION_AND_DUPLICATES.md` — existing playlist, duplicate scan, write plan;\n- `13_PENDING_QUEUE_AND_RECOVERY.md` — відкладені операції;\n- `14_BACKUP_AND_EXPORT.md` — local backup, account bulk export, manifest;\n- `15_THEME_SYSTEM.md` — palette, semantic colors, Accent Card System;\n- `16_GITHUB_ACTIONS_RELEASE.md` — signed APK, checksum, artifact;\n- `17_BUILD_A_FEATURE_FROM_ZERO.md` — повний практичний feature exercise;\n- `18_RECREATE_YTM_IMPORTER.md` — фінальний покроковий прохід від чистого repo;\n- `19_LOCALIZATION_UK_KO_EN.md` — Android resources, Ukrainian/Korean/English UI;\n- `20_YERIN_EXCLUSIVE_SKIN.md` — hidden visual skin + URL feature-gate design.\n'

def _read(root: Path, rel: str) -> str:
    path = root / rel
    if not path.is_file():
        raise PatchError(f"missing file: {rel}")
    return path.read_text(encoding="utf-8")

def _write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")

def _replace(root: Path, rel: str, old: str, new: str, label: str, dry_run: bool) -> None:
    text = _read(root, rel)
    if new in text:
        print(f"SKIP already applied: {label}")
        return
    count = text.count(old)
    if count != 1:
        raise PatchError(
            f"{label}: expected exactly 1 old anchor; found {count}\n"
            f"FILE: {rel}\nANCHOR:\n{old[:1200]}"
        )
    if not dry_run:
        _write(root, rel, text.replace(old, new, 1))
    print(("CHECK" if dry_run else "PATCH") + f": {label}")

def _insert(root: Path, rel: str, marker: str, snippet: str, token: str, label: str, dry_run: bool) -> None:
    text = _read(root, rel)
    if token in text:
        print(f"SKIP already applied: {label}")
        return
    count = text.count(marker)
    if count != 1:
        raise PatchError(
            f"{label}: expected exactly 1 marker; found {count}\n"
            f"FILE: {rel}\nMARKER:\n{marker[:1200]}"
        )
    if not dry_run:
        _write(root, rel, text.replace(marker, snippet + marker, 1))
    print(("CHECK" if dry_run else "PATCH") + f": {label}")

def _range(root: Path, rel: str, start: str, end: str, replacement: str, token: str, label: str, dry_run: bool) -> None:
    text = _read(root, rel)
    if token in text:
        print(f"SKIP already applied: {label}")
        return
    if text.count(start) != 1:
        raise PatchError(f"{label}: range start count = {text.count(start)}")
    if text.count(end) != 1:
        raise PatchError(f"{label}: range end count = {text.count(end)}")
    start_idx = text.index(start)
    end_idx = text.index(end, start_idx)
    if end_idx <= start_idx:
        raise PatchError(f"{label}: invalid range order")
    if not dry_run:
        _write(root, rel, text[:start_idx] + replacement + text[end_idx:])
    print(("CHECK" if dry_run else "PATCH") + f": {label}")

def _custom(root: Path, dry_run: bool) -> None:
    # PROJECT_STATUS focus.
    rel = "PROJECT_STATUS.txt"
    text = _read(root, rel)
    if "v1.4.26 focus:" in text:
        print("SKIP already applied: PROJECT_STATUS v1.4.26 focus")
    else:
        marker = "\nKnown:\n"
        if text.count(marker) != 1:
            raise PatchError("PROJECT_STATUS Known marker missing/duplicated")
        if not dry_run:
            _write(root, rel, text.replace(marker, "\n" + PROJECT_FOCUS + "\n" + marker, 1))
        print(("CHECK" if dry_run else "PATCH") + ": PROJECT_STATUS v1.4.26 focus")

    _replace(root, "docs/tutorial/ROADMAP.md", ROADMAP_OLD, ROADMAP_NEW,
             "tutorial roadmap v1.4.26 + localization/skin", dry_run)

    # release-preflight requires new active scripts.
    rel = "scripts/release-preflight.sh"
    text = _read(root, rel)
    additions = (
        'check_file "scripts/v1426-apply-selftest.py"\n'
        'check_file "scripts/v1426-selective-export-audit.sh"\n'
    )
    if additions in text:
        print("SKIP already applied: preflight v1.4.26 script files")
    else:
        marker = 'check_file "scripts/v1425-apply-selftest.py"\n'
        if text.count(marker) != 1:
            raise PatchError("release-preflight v1.4.25 selftest marker missing/duplicated")
        if not dry_run:
            _write(root, rel, text.replace(marker, marker + additions, 1))
        print(("CHECK" if dry_run else "PATCH") + ": preflight v1.4.26 script files")

def run(root: Path, dry_run: bool = False) -> None:
    root = root.resolve()
    for op in OPS:
        kind = op[0]
        if kind == "replace":
            _, rel, old, new, label = op
            _replace(root, rel, old, new, label, dry_run)
        elif kind == "insert":
            _, rel, marker, snippet, token, label = op
            _insert(root, rel, marker, snippet, token, label, dry_run)
        elif kind == "range":
            _, rel, start, end, replacement, token, label = op
            _range(root, rel, start, end, replacement, token, label, dry_run)
        else:
            raise PatchError(f"unknown operation: {kind}")
    _custom(root, dry_run)

def validate_ops(ops=OPS) -> None:
    for op in ops:
        values = op[2:-1]
        for value in values:
            if not isinstance(value, str):
                continue
            if r"\n" in value and "\n" not in value:
                raise PatchError(f"literal \\n regression in operation: {op[-1]}")

if __name__ == "__main__":
    validate_ops()
    print(f"PASS: {len(OPS)} v1.4.26 operations validated")
