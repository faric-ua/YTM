package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import org.json.JSONArray
import org.json.JSONObject

data class RestorablePlaylistSnapshot(
    val localPlaylistId: String,
    val sourceHistoryId: String?,
    val playlist: ImportedPlaylist,
    val sourceLabel: String,
    val createdAt: Long,
    val updatedAt: Long,
    val destinationPlaylistId: String?,
    val destinationPlaylistTitle: String?
)

class RestorablePlaylistStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun get(
        localPlaylistId: String
    ): RestorablePlaylistSnapshot? =
        readAll().firstOrNull {
            it.localPlaylistId == localPlaylistId
        }

    @Synchronized
    fun getAll(): List<RestorablePlaylistSnapshot> =
        readAll().sortedByDescending {
            it.updatedAt
        }

    @Synchronized
    fun upsert(
        snapshot: RestorablePlaylistSnapshot
    ) {
        val entries =
            readAll().toMutableList()

        val index =
            entries.indexOfFirst {
                it.localPlaylistId ==
                    snapshot.localPlaylistId
            }

        val normalized =
            if (index >= 0) {
                snapshot.copy(
                    createdAt =
                        entries[index]
                            .createdAt
                )
            } else {
                snapshot
            }

        if (index >= 0) {
            entries[index] =
                normalized
        } else {
            entries +=
                normalized
        }

        writeAll(
            entries.sortedByDescending {
                it.updatedAt
            }
        )
    }

    private fun readAll():
        List<RestorablePlaylistSnapshot> {
        val raw =
            prefs.getString(
                KEY_SNAPSHOTS,
                "[]"
            ).orEmpty()

        return runCatching {
            val array =
                JSONArray(raw)

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    val root =
                        array.optJSONObject(
                            index
                        )
                            ?: continue

                    val localPlaylistId =
                        root.optString(
                            "localPlaylistId"
                        )

                    if (
                        localPlaylistId
                            .isBlank()
                    ) {
                        continue
                    }

                    add(
                        RestorablePlaylistSnapshot(
                            localPlaylistId =
                                localPlaylistId,
                            sourceHistoryId =
                                root.optNullableString(
                                    "sourceHistoryId"
                                ),
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
                            createdAt =
                                root.optLong(
                                    "createdAt",
                                    0L
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
                                )
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )
    }

    private fun writeAll(
        snapshots:
            List<RestorablePlaylistSnapshot>
    ) {
        val array =
            JSONArray()

        snapshots.forEach {
                snapshot ->
            array.put(
                JSONObject()
                    .put(
                        "schemaVersion",
                        SCHEMA_VERSION
                    )
                    .put(
                        "localPlaylistId",
                        snapshot.localPlaylistId
                    )
                    .putNullable(
                        "sourceHistoryId",
                        snapshot.sourceHistoryId
                    )
                    .put(
                        "sourceLabel",
                        snapshot.sourceLabel
                    )
                    .put(
                        "createdAt",
                        snapshot.createdAt
                    )
                    .put(
                        "updatedAt",
                        snapshot.updatedAt
                    )
                    .putNullable(
                        "destinationPlaylistId",
                        snapshot
                            .destinationPlaylistId
                    )
                    .putNullable(
                        "destinationPlaylistTitle",
                        snapshot
                            .destinationPlaylistTitle
                    )
                    .put(
                        "playlist",
                        playlistToJson(
                            snapshot.playlist
                        )
                    )
            )
        }

        prefs.edit()
            .putString(
                KEY_SNAPSHOTS,
                array.toString()
            )
            .apply()
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
            optString(key)
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
            optInt(key)
        }

    companion object {
        private const val PREFS_NAME =
            "restorable_playlist_v1"

        private const val KEY_SNAPSHOTS =
            "restorable_playlist_snapshots"

        private const val SCHEMA_VERSION =
            1
    }
}
