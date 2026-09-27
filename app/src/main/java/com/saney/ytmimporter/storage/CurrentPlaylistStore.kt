package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class CurrentPlaylistSnapshot(
    val playlist: ImportedPlaylist,
    val sourceLabel: String,
    val updatedAt: Long,
    val destinationPlaylistId: String? = null,
    val destinationPlaylistTitle: String? = null,
    val localPlaylistId: String,
    val sourceHistoryId: String? = null
)

class CurrentPlaylistStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private val restorableStore =
        RestorablePlaylistStore(
            context
        )

    @Synchronized
    fun save(
        playlist: ImportedPlaylist,
        sourceLabel: String,
        destinationPlaylistId: String? = null,
        destinationPlaylistTitle: String? = null,
        localPlaylistId: String? = null,
        sourceHistoryId: String? = null
    ): String {
        val previousRoot =
            currentRoot()

        val resolvedLocalPlaylistId =
            localPlaylistId
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: previousRoot
                    ?.optNullableString(
                        "localPlaylistId"
                    )
                ?: UUID.randomUUID()
                    .toString()

        val resolvedSourceHistoryId =
            sourceHistoryId
                ?: previousRoot
                    ?.optNullableString(
                        "sourceHistoryId"
                    )

        val now =
            System.currentTimeMillis()

        val root =
            JSONObject()
                .put(
                    "schemaVersion",
                    SCHEMA_VERSION
                )
                .put(
                    "localPlaylistId",
                    resolvedLocalPlaylistId
                )
                .putNullable(
                    "sourceHistoryId",
                    resolvedSourceHistoryId
                )
                .put(
                    "sourceLabel",
                    sourceLabel
                )
                .put(
                    "updatedAt",
                    now
                )
                .putNullable(
                    "destinationPlaylistId",
                    destinationPlaylistId
                )
                .putNullable(
                    "destinationPlaylistTitle",
                    destinationPlaylistTitle
                )
                .put(
                    "playlist",
                    playlistToJson(
                        playlist
                    )
                )

        prefs.edit()
            .putString(
                KEY_CURRENT,
                root.toString()
            )
            .apply()

        val previousRestorable =
            restorableStore.get(
                resolvedLocalPlaylistId
            )

        restorableStore.upsert(
            RestorablePlaylistSnapshot(
                localPlaylistId =
                    resolvedLocalPlaylistId,
                sourceHistoryId =
                    resolvedSourceHistoryId
                        ?: previousRestorable
                            ?.sourceHistoryId,
                playlist =
                    playlist,
                sourceLabel =
                    sourceLabel,
                createdAt =
                    previousRestorable
                        ?.createdAt
                        ?: now,
                updatedAt =
                    now,
                destinationPlaylistId =
                    destinationPlaylistId,
                destinationPlaylistTitle =
                    destinationPlaylistTitle
            )
        )

        return resolvedLocalPlaylistId
    }

    @Synchronized
    fun load(): CurrentPlaylistSnapshot? {
        val raw =
            prefs.getString(
                KEY_CURRENT,
                null
            )
                ?: return null

        return runCatching {
            val root =
                JSONObject(
                    raw
                )

            val schemaVersion =
                root.optInt(
                    "schemaVersion",
                    -1
                )

            require(
                schemaVersion in 1..SCHEMA_VERSION
            ) {
                "Unsupported current-playlist schema"
            }

            val resolvedLocalPlaylistId =
                root.optNullableString(
                    "localPlaylistId"
                )
                    ?: UUID.randomUUID()
                        .toString()

            val snapshot =
                CurrentPlaylistSnapshot(
                    playlist =
                        playlistFromJson(
                            root.getJSONObject(
                                "playlist"
                            )
                        ),
                    sourceLabel =
                        root.optString(
                            "sourceLabel",
                            "Невідоме джерело"
                        ),
                    updatedAt =
                        root.optLong(
                            "updatedAt",
                            0L
                        ),
                    destinationPlaylistId =
                        root.optNullableString(
                            "destinationPlaylistId"
                        ),
                    destinationPlaylistTitle =
                        root.optNullableString(
                            "destinationPlaylistTitle"
                        ),
                    localPlaylistId =
                        resolvedLocalPlaylistId,
                    sourceHistoryId =
                        root.optNullableString(
                            "sourceHistoryId"
                        )
                )

            if (
                schemaVersion <
                    SCHEMA_VERSION ||
                root.optNullableString(
                    "localPlaylistId"
                ) == null
            ) {
                val migratedRoot =
                    JSONObject(
                        raw
                    )
                        .put(
                            "schemaVersion",
                            SCHEMA_VERSION
                        )
                        .put(
                            "localPlaylistId",
                            resolvedLocalPlaylistId
                        )

                if (
                    !migratedRoot.has(
                        "sourceHistoryId"
                    )
                ) {
                    migratedRoot.put(
                        "sourceHistoryId",
                        JSONObject.NULL
                    )
                }

                prefs.edit()
                    .putString(
                        KEY_CURRENT,
                        migratedRoot
                            .toString()
                    )
                    .apply()
            }

            val existing =
                restorableStore.get(
                    resolvedLocalPlaylistId
                )

            restorableStore.upsert(
                RestorablePlaylistSnapshot(
                    localPlaylistId =
                        resolvedLocalPlaylistId,
                    sourceHistoryId =
                        snapshot.sourceHistoryId
                            ?: existing
                                ?.sourceHistoryId,
                    playlist =
                        snapshot.playlist,
                    sourceLabel =
                        snapshot.sourceLabel,
                    createdAt =
                        existing
                            ?.createdAt
                            ?: snapshot
                                .updatedAt,
                    updatedAt =
                        snapshot.updatedAt,
                    destinationPlaylistId =
                        snapshot
                            .destinationPlaylistId,
                    destinationPlaylistTitle =
                        snapshot
                            .destinationPlaylistTitle
                )
            )

            snapshot
        }.getOrNull()
    }

    @Synchronized
    fun clear() {
        prefs.edit()
            .remove(
                KEY_CURRENT
            )
            .apply()
    }

    private fun currentRoot():
        JSONObject? {
        val raw =
            prefs.getString(
                KEY_CURRENT,
                null
            )
                ?: return null

        return runCatching {
            JSONObject(
                raw
            )
        }.getOrNull()
    }

    private fun playlistToJson(
        playlist: ImportedPlaylist
    ): JSONObject =
        JSONObject()
            .put(
                "name",
                playlist.name
            )
            .put(
                "tracks",
                JSONArray().also {
                        array ->
                    playlist.tracks
                        .forEach {
                                track ->
                            array.put(
                                trackToJson(
                                    track
                                )
                            )
                        }
                }
            )

    private fun playlistFromJson(
        root: JSONObject
    ): ImportedPlaylist {
        val tracks =
            mutableListOf<Track>()

        val array =
            root.optJSONArray(
                "tracks"
            )
                ?: JSONArray()

        for (
            index in
            0 until array.length()
        ) {
            val item =
                array.optJSONObject(
                    index
                )
                    ?: continue

            tracks +=
                trackFromJson(
                    item
                )
        }

        return ImportedPlaylist(
            name =
                root.optString(
                    "name",
                    "YTM Import"
                ),
            tracks = tracks
        )
    }

    private fun trackToJson(
        track: Track
    ): JSONObject =
        JSONObject()
            .put(
                "originalTitle",
                track.originalTitle
            )
            .put(
                "originalArtist",
                track.originalArtist
            )
            .putNullable(
                "selectedVideoId",
                track.selectedVideoId
            )
            .putNullable(
                "selectedTitle",
                track.selectedTitle
            )
            .putNullable(
                "selectedChannel",
                track.selectedChannel
            )
            .put(
                "status",
                track.status.name
            )
            .put(
                "manuallySelected",
                track.manuallySelected
            )
            .putNullable(
                "error",
                track.error
            )
            .put(
                "durableExactSelection",
                track.durableExactSelection
            )
            .putNullable(
                "historyIndex",
                track.historyIndex
            )
            .put(
                "candidates",
                JSONArray().also {
                        array ->
                    track.candidates
                        .forEach {
                                candidate ->
                            array.put(
                                JSONObject()
                                    .put(
                                        "videoId",
                                        candidate.videoId
                                    )
                                    .put(
                                        "title",
                                        candidate.title
                                    )
                                    .put(
                                        "channelTitle",
                                        candidate.channelTitle
                                    )
                                    .put(
                                        "score",
                                        candidate.score
                                    )
                            )
                        }
                }
            )

    private fun trackFromJson(
        root: JSONObject
    ): Track {
        val candidates =
            mutableListOf<SearchCandidate>()

        val array =
            root.optJSONArray(
                "candidates"
            )
                ?: JSONArray()

        for (
            index in
            0 until array.length()
        ) {
            val item =
                array.optJSONObject(
                    index
                )
                    ?: continue

            val videoId =
                item.optString(
                    "videoId"
                )

            if (
                videoId.isBlank()
            ) {
                continue
            }

            candidates +=
                SearchCandidate(
                    videoId =
                        videoId,
                    title =
                        item.optString(
                            "title"
                        ),
                    channelTitle =
                        item.optString(
                            "channelTitle"
                        ),
                    score =
                        item.optDouble(
                            "score",
                            0.0
                        )
                )
        }

        val status =
            runCatching {
                TrackStatus.valueOf(
                    root.optString(
                        "status",
                        TrackStatus.NEW.name
                    )
                )
            }.getOrDefault(
                TrackStatus.NEW
            )

        return Track(
            originalTitle =
                root.optString(
                    "originalTitle"
                ),
            originalArtist =
                root.optString(
                    "originalArtist"
                ),
            selectedVideoId =
                root.optNullableString(
                    "selectedVideoId"
                ),
            selectedTitle =
                root.optNullableString(
                    "selectedTitle"
                ),
            selectedChannel =
                root.optNullableString(
                    "selectedChannel"
                ),
            status = status,
            candidates =
                candidates,
            manuallySelected =
                root.optBoolean(
                    "manuallySelected",
                    false
                ),
            error =
                root.optNullableString(
                    "error"
                ),
            durableExactSelection =
                root.optBoolean(
                    "durableExactSelection",
                    false
                ),
            historyIndex =
                root.optNullableInt(
                    "historyIndex"
                )
        )
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ): JSONObject {
        if (
            value == null
        ) {
            put(
                key,
                JSONObject.NULL
            )
        } else {
            put(
                key,
                value
            )
        }

        return this
    }

    private fun JSONObject.optNullableString(
        key: String
    ): String? =
        if (
            !has(key) ||
            isNull(key)
        ) {
            null
        } else {
            optString(
                key
            )
                .takeIf {
                    it.isNotBlank()
                }
        }

    private fun JSONObject.optNullableInt(
        key: String
    ): Int? =
        if (
            !has(key) ||
            isNull(key)
        ) {
            null
        } else {
            optInt(
                key
            )
        }

    companion object {
        private const val PREFS_NAME =
            "current_playlist_v1"

        private const val KEY_CURRENT =
            "current_playlist_json"

        private const val SCHEMA_VERSION =
            4
    }
}
