package com.saney.ytmimporter.storage

import org.json.JSONArray
import org.json.JSONObject

enum class YtmArtifactType {
    FULL_LOCAL_BACKUP,
    HISTORY_BACKUP,
    PLAYLIST_PROJECT,
    PENDING_DIAGNOSTICS,
    ACCOUNT_LIBRARY_MANIFEST,
    UNKNOWN
}

data class YtmArtifactInspection(
    val type: YtmArtifactType,
    val schemaVersion: Int? = null,
    val appVersion: String? = null,
    val exportedAt: Long? = null,
    val itemCount: Int? = null,
    val title: String? = null,
    val backupMode: String? = null
)

enum class YtmArtifactScope {
    FULL_LOCAL_RESTORE,
    HISTORY_RESTORE,
    PLAYLIST_PROJECT
}

object YtmArtifactScopePolicy {
    fun accepts(
        scope: YtmArtifactScope,
        type: YtmArtifactType
    ): Boolean =
        when (scope) {
            YtmArtifactScope.FULL_LOCAL_RESTORE ->
                type == YtmArtifactType.FULL_LOCAL_BACKUP

            YtmArtifactScope.HISTORY_RESTORE ->
                type == YtmArtifactType.HISTORY_BACKUP

            YtmArtifactScope.PLAYLIST_PROJECT ->
                type == YtmArtifactType.PLAYLIST_PROJECT
        }
}

object YtmArtifactClassifier {
    private const val FULL_BACKUP_FORMAT =
        "ytm-importer-local-backup"

    private const val PLAYLIST_PROJECT_FORMAT =
        "ytm-importer-playlist-project"

    private const val ACCOUNT_LIBRARY_FORMAT =
        "ytm-importer-account-library-export"

    fun inspect(
        raw: String
    ): YtmArtifactInspection {
        val text =
            raw.trim()

        if (text.isEmpty()) {
            return unknown()
        }

        return when (
            text.firstOrNull()
        ) {
            '{' ->
                runCatching {
                    inspectObject(
                        JSONObject(text)
                    )
                }.getOrElse {
                    unknown()
                }

            '[' ->
                runCatching {
                    inspectArray(
                        JSONArray(text)
                    )
                }.getOrElse {
                    unknown()
                }

            else ->
                unknown()
        }
    }

    private fun inspectObject(
        root: JSONObject
    ): YtmArtifactInspection {
        val format =
            root.optString(
                "format"
            )

        return when (format) {
            FULL_BACKUP_FORMAT ->
                if (
                    root.optJSONObject(
                        "preferences"
                    ) != null
                ) {
                    YtmArtifactInspection(
                        type =
                            YtmArtifactType
                                .FULL_LOCAL_BACKUP,
                        schemaVersion =
                            optionalPositiveInt(
                                root,
                                "schemaVersion"
                            ),
                        appVersion =
                            optionalString(
                                root,
                                "appVersion"
                            ),
                        exportedAt =
                            optionalPositiveLong(
                                root,
                                "exportedAt"
                            ),
                        itemCount =
                            optionalNonNegativeInt(
                                root,
                                "valueCount"
                            )
                    )
                } else {
                    unknown()
                }

            PLAYLIST_PROJECT_FORMAT ->
                inspectPlaylistProject(
                    root
                )

            ACCOUNT_LIBRARY_FORMAT ->
                inspectAccountManifest(
                    root
                )

            else ->
                unknown()
        }
    }

    private fun inspectPlaylistProject(
        root: JSONObject
    ): YtmArtifactInspection {
        val playlist =
            root.optJSONObject(
                "playlist"
            ) ?: return unknown()

        val tracks =
            playlist.optJSONArray(
                "tracks"
            ) ?: return unknown()

        return YtmArtifactInspection(
            type =
                YtmArtifactType
                    .PLAYLIST_PROJECT,
            schemaVersion =
                optionalPositiveInt(
                    root,
                    "schemaVersion"
                ),
            appVersion =
                optionalString(
                    root,
                    "appVersion"
                ),
            exportedAt =
                optionalPositiveLong(
                    root,
                    "exportedAt"
                ),
            itemCount =
                tracks.length(),
            title =
                optionalString(
                    playlist,
                    "name"
                )
        )
    }

    private fun inspectAccountManifest(
        root: JSONObject
    ): YtmArtifactInspection {
        val playlists =
            root.optJSONArray(
                "playlists"
            ) ?: return unknown()

        return YtmArtifactInspection(
            type =
                YtmArtifactType
                    .ACCOUNT_LIBRARY_MANIFEST,
            schemaVersion =
                optionalPositiveInt(
                    root,
                    "schemaVersion"
                ),
            appVersion =
                optionalString(
                    root,
                    "appVersion"
                ),
            exportedAt =
                optionalPositiveLong(
                    root,
                    "exportedAt"
                ),
            itemCount =
                optionalNonNegativeInt(
                    root,
                    "playlistCount"
                ) ?: playlists.length(),
            backupMode =
                optionalString(
                    root,
                    "backupMode"
                )
        )
    }

    private fun inspectArray(
        array: JSONArray
    ): YtmArtifactInspection {
        if (array.length() == 0) {
            return unknown()
        }

        val objects =
            mutableListOf<JSONObject>()

        for (
            index in
            0 until array.length()
        ) {
            val item =
                array.optJSONObject(
                    index
                ) ?: return unknown()

            objects +=
                item
        }

        val allHistory =
            objects.all(
                ::isHistoryEntry
            )

        val allPending =
            objects.all(
                ::isPendingEntry
            )

        if (
            allHistory ==
                allPending
        ) {
            return unknown()
        }

        val latestUpdated =
            objects
                .mapNotNull {
                    optionalPositiveLong(
                        it,
                        "updatedAt"
                    ) ?: optionalPositiveLong(
                        it,
                        "createdAt"
                    )
                }
                .maxOrNull()

        val firstTitle =
            objects
                .asSequence()
                .mapNotNull {
                    optionalString(
                        it,
                        "playlistName"
                    )
                }
                .firstOrNull()

        return YtmArtifactInspection(
            type =
                if (allHistory) {
                    YtmArtifactType
                        .HISTORY_BACKUP
                } else {
                    YtmArtifactType
                        .PENDING_DIAGNOSTICS
                },
            exportedAt =
                latestUpdated,
            itemCount =
                objects.size,
            title =
                firstTitle
        )
    }

    private fun isHistoryEntry(
        item: JSONObject
    ): Boolean =
        hasNonBlankString(
            item,
            "id"
        ) &&
            hasNonBlankString(
                item,
                "status"
            ) &&
            hasNonBlankString(
                item,
                "destination"
            ) &&
            item.optJSONArray(
                "tracks"
            ) != null

    private fun isPendingEntry(
        item: JSONObject
    ): Boolean =
        hasNonBlankString(
            item,
            "id"
        ) &&
            hasNonBlankString(
                item,
                "operation"
            ) &&
            hasNonBlankString(
                item,
                "destination"
            ) &&
            item.optJSONArray(
                "remainingTracks"
            ) != null

    private fun hasNonBlankString(
        json: JSONObject,
        key: String
    ): Boolean =
        optionalString(
            json,
            key
        ) != null

    private fun optionalString(
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
                it.isNotEmpty()
            }
    }

    private fun optionalPositiveInt(
        json: JSONObject,
        key: String
    ): Int? {
        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json
            .optInt(
                key,
                -1
            )
            .takeIf {
                it > 0
            }
    }

    private fun optionalNonNegativeInt(
        json: JSONObject,
        key: String
    ): Int? {
        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json
            .optInt(
                key,
                -1
            )
            .takeIf {
                it >= 0
            }
    }

    private fun optionalPositiveLong(
        json: JSONObject,
        key: String
    ): Long? {
        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json
            .optLong(
                key,
                -1L
            )
            .takeIf {
                it > 0L
            }
    }

    private fun unknown():
        YtmArtifactInspection =
        YtmArtifactInspection(
            type =
                YtmArtifactType.UNKNOWN
        )
}
