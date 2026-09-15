package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import org.json.JSONArray
import org.json.JSONObject

data class CurrentPlaylistSnapshot(
    val playlist: ImportedPlaylist,
    val sourceLabel: String,
    val updatedAt: Long
)

class CurrentPlaylistStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun save(
        playlist: ImportedPlaylist,
        sourceLabel: String
    ) {
        val root =
            JSONObject()
                .put("schemaVersion", SCHEMA_VERSION)
                .put("sourceLabel", sourceLabel)
                .put("updatedAt", System.currentTimeMillis())
                .put("playlist", playlistToJson(playlist))

        prefs
            .edit()
            .putString(KEY_CURRENT, root.toString())
            .apply()
    }

    @Synchronized
    fun load(): CurrentPlaylistSnapshot? {
        val raw =
            prefs.getString(KEY_CURRENT, null)
                ?: return null

        return runCatching {
            val root = JSONObject(raw)

            require(
                root.optInt(
                    "schemaVersion",
                    -1
                ) in 1..SCHEMA_VERSION
            ) {
                "Unsupported current-playlist schema"
            }

            CurrentPlaylistSnapshot(
                playlist =
                    playlistFromJson(
                        root.getJSONObject("playlist")
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
                    )
            )
        }.getOrNull()
    }

    @Synchronized
    fun clear() {
        prefs
            .edit()
            .remove(KEY_CURRENT)
            .apply()
    }

    private fun playlistToJson(
        playlist: ImportedPlaylist
    ): JSONObject =
        JSONObject()
            .put("name", playlist.name)
            .put(
                "tracks",
                JSONArray().also { array ->
                    playlist.tracks
                        .forEach { track ->
                            array.put(
                                trackToJson(track)
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
            root.optJSONArray("tracks")
                ?: JSONArray()

        for (index in 0 until array.length()) {
            val item =
                array.optJSONObject(index)
                    ?: continue

            tracks += trackFromJson(item)
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
            .put("status", track.status.name)
            .put(
                "manuallySelected",
                track.manuallySelected
            )
            .putNullable("error", track.error)
            .putNullable(
                "historyIndex",
                track.historyIndex
            )
            .put(
                "candidates",
                JSONArray().also { array ->
                    track.candidates
                        .forEach { candidate ->
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
            root.optJSONArray("candidates")
                ?: JSONArray()

        for (index in 0 until array.length()) {
            val item =
                array.optJSONObject(index)
                    ?: continue

            val videoId =
                item.optString("videoId")

            if (videoId.isBlank()) {
                continue
            }

            candidates +=
                SearchCandidate(
                    videoId = videoId,
                    title =
                        item.optString("title"),
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
            candidates = candidates,
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
        if (value == null) {
            put(key, JSONObject.NULL)
        } else {
            put(key, value)
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
            "current_playlist_v1"

        private const val KEY_CURRENT =
            "current_playlist_json"

        private const val SCHEMA_VERSION =
            1
    }
}
