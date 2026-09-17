package com.saney.ytmimporter.storage

import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import org.json.JSONArray
import org.json.JSONObject

data class PlaylistProjectImport(
    val playlist: ImportedPlaylist,
    val sourceHistoryId: String?,
    val sourcePlaylistId: String?,
    val sourceDestination: PendingDestination?,
    val exactSelectionCount: Int,
    val unresolvedCount: Int
)

object PlaylistProjectCodec {
    private const val FORMAT = "ytm-importer-playlist-project"
    private const val SCHEMA_VERSION = 2

    fun isProject(raw: String): Boolean =
        runCatching {
            JSONObject(raw).optString("format") == FORMAT
        }.getOrDefault(false)

    fun exportWorkingPlaylist(
        playlist: ImportedPlaylist,
        sourceLabel: String,
        appVersion: String
    ): String {
        val tracks = JSONArray()

        playlist.tracks
            .forEachIndexed { index, track ->
                tracks.put(
                    workingTrackToJson(
                        index = index,
                        track = track
                    )
                )
            }

        val playlistJson =
            JSONObject()
                .put("name", playlist.name)
                .put("sourcePlaylistId", JSONObject.NULL)
                .put("privacyStatus", JSONObject.NULL)
                .put("sourceDestination", JSONObject.NULL)
                .put("tracks", tracks)

        return JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("appVersion", appVersion)
            .put("exportedAt", System.currentTimeMillis())
            .put("sourceHistoryId", JSONObject.NULL)
            .put("sourceLabel", sourceLabel)
            .put("scope", "working-playlist")
            .put(
                "note",
                "Project was saved from the current working list before YouTube/YTM export."
            )
            .put("playlist", playlistJson)
            .toString(2)
    }

    fun exportAccountPlaylist(
        playlist: ImportedPlaylist,
        sourcePlaylistId: String,
        privacyStatus: String,
        appVersion: String
    ): String {
        val tracks = JSONArray()

        playlist.tracks
            .forEachIndexed { index, track ->
                tracks.put(
                    workingTrackToJson(
                        index = index,
                        track = track
                    )
                )
            }

        val playlistJson =
            JSONObject()
                .put("name", playlist.name)
                .put(
                    "sourcePlaylistId",
                    sourcePlaylistId
                )
                .put(
                    "privacyStatus",
                    privacyStatus
                )
                .put(
                    "sourceDestination",
                    JSONObject.NULL
                )
                .put(
                    "tracks",
                    tracks
                )

        return JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("appVersion", appVersion)
            .put("exportedAt", System.currentTimeMillis())
            .put("sourceHistoryId", JSONObject.NULL)
            .put("sourceLabel", "YouTube/YTM account")
            .put("scope", "account-playlist-export")
            .put(
                "note",
                "Read-only local export of a playlist from the connected YouTube/YTM account."
            )
            .put("playlist", playlistJson)
            .toString(2)
    }

    fun exportHistoryEntry(
        entry: HistoryEntry,
        appVersion: String
    ): String {
        val tracks = JSONArray()

        entry.tracks
            .sortedBy { it.index }
            .forEach { track ->
                tracks.put(
                    JSONObject()
                        .put("position", track.index)
                        .put("originalTitle", track.originalTitle)
                        .put("originalArtist", track.originalArtist)
                        .put(
                            "videoId",
                            track.videoId ?: JSONObject.NULL
                        )
                        .put(
                            "selectedTitle",
                            track.selectedTitle ?: JSONObject.NULL
                        )
                        .put(
                            "selectedChannel",
                            track.selectedChannel ?: JSONObject.NULL
                        )
                        .put(
                            "manuallySelected",
                            track.manuallySelected
                        )
                        .put("sourceStatus", track.status)
                        .put(
                            "sourceError",
                            track.error ?: JSONObject.NULL
                        )
                        .put("candidates", JSONArray())
                )
            }

        val playlist =
            JSONObject()
                .put("name", entry.playlistName)
                .put(
                    "sourcePlaylistId",
                    entry.playlistId ?: JSONObject.NULL
                )
                .put("privacyStatus", entry.privacyStatus)
                .put(
                    "sourceDestination",
                    entry.destination.name
                )
                .put("tracks", tracks)

        return JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("appVersion", appVersion)
            .put("exportedAt", System.currentTimeMillis())
            .put("sourceHistoryId", entry.id)
            .put(
                "scope",
                if (
                    entry.destination ==
                        PendingDestination.NEW_PLAYLIST
                ) {
                    "new-playlist-history"
                } else {
                    "existing-playlist-import-batch"
                }
            )
            .put(
                "note",
                if (
                    entry.destination ==
                        PendingDestination.NEW_PLAYLIST
                ) {
                    "Project contains the playlist import stored in History."
                } else {
                    "Project contains only this History import batch, " +
                        "not the complete pre-existing YouTube playlist."
                }
            )
            .put("playlist", playlist)
            .toString(2)
    }

    fun importProject(raw: String): PlaylistProjectImport {
        val root = JSONObject(raw)

        require(root.optString("format") == FORMAT) {
            "Це не YTM Playlist Project"
        }

        val schemaVersion =
            root.optInt(
                "schemaVersion",
                -1
            )

        require(schemaVersion in 1..SCHEMA_VERSION) {
            "Непідтримувана версія YTM Project: $schemaVersion"
        }

        val playlistJson =
            root.optJSONObject("playlist")
                ?: throw IllegalArgumentException(
                    "У YTM Project немає секції playlist"
                )

        val tracksJson =
            playlistJson.optJSONArray("tracks")
                ?: throw IllegalArgumentException(
                    "У YTM Project немає списку tracks"
                )

        val tracks = mutableListOf<Track>()
        var exactSelectionCount = 0
        var unresolvedCount = 0

        for (i in 0 until tracksJson.length()) {
            val item = tracksJson.getJSONObject(i)

            val originalTitle =
                item.optString("originalTitle").trim()

            val originalArtist =
                item.optString("originalArtist").trim()

            if (
                originalTitle.isBlank() ||
                originalArtist.isBlank()
            ) {
                continue
            }

            val videoId =
                nullableString(
                    item,
                    "videoId"
                )

            val selectedTitleRaw =
                nullableString(
                    item,
                    "selectedTitle"
                )

            val selectedTitle =
                when {
                    videoId.isNullOrBlank() ->
                        null

                    selectedTitleRaw.isNullOrBlank() ||
                        selectedTitleRaw == "Ручне посилання" ->
                        "YouTube video $videoId"

                    else ->
                        selectedTitleRaw
                }

            val selectedChannel =
                nullableString(
                    item,
                    "selectedChannel"
                )

            val hasExactSelection =
                !videoId.isNullOrBlank()

            if (hasExactSelection) {
                exactSelectionCount += 1
            } else {
                unresolvedCount += 1
            }

            val candidates =
                if (schemaVersion >= 2) {
                    parseCandidates(
                        item.optJSONArray("candidates")
                    )
                } else {
                    emptyList()
                }

            val importedStatus =
                if (schemaVersion >= 2) {
                    normalizeImportedStatus(
                        rawStatus =
                            item.optString(
                                "sourceStatus",
                                if (hasExactSelection) {
                                    TrackStatus.MATCHED.name
                                } else {
                                    TrackStatus.NEW.name
                                }
                            ),
                        hasExactSelection =
                            hasExactSelection
                    )
                } else if (hasExactSelection) {
                    TrackStatus.MATCHED
                } else {
                    TrackStatus.NEW
                }

            tracks +=
                Track(
                    originalTitle = originalTitle,
                    originalArtist = originalArtist,
                    selectedVideoId = videoId,
                    selectedTitle = selectedTitle,
                    selectedChannel = selectedChannel,
                    status = importedStatus,
                    candidates = candidates,
                    manuallySelected =
                        item.optBoolean(
                            "manuallySelected",
                            false
                        ),
                    error =
                        if (schemaVersion >= 2) {
                            nullableString(
                                item,
                                "sourceError"
                            )
                        } else {
                            null
                        },
                    historyIndex =
                        item.optInt(
                            "position",
                            i
                        )
                )
        }

        require(tracks.isNotEmpty()) {
            "YTM Project не містить коректних треків"
        }

        val destination =
            nullableString(
                playlistJson,
                "sourceDestination"
            )?.let { value ->
                runCatching {
                    PendingDestination.valueOf(value)
                }.getOrNull()
            }

        return PlaylistProjectImport(
            playlist =
                ImportedPlaylist(
                    name =
                        playlistJson
                            .optString(
                                "name",
                                "YTM Project"
                            )
                            .ifBlank {
                                "YTM Project"
                            },
                    tracks =
                        tracks
                            .sortedBy {
                                it.historyIndex ?: Int.MAX_VALUE
                            }
                            .toMutableList()
                ),
            sourceHistoryId =
                nullableString(
                    root,
                    "sourceHistoryId"
                ),
            sourcePlaylistId =
                nullableString(
                    playlistJson,
                    "sourcePlaylistId"
                ),
            sourceDestination = destination,
            exactSelectionCount = exactSelectionCount,
            unresolvedCount = unresolvedCount
        )
    }

    private fun workingTrackToJson(
        index: Int,
        track: Track
    ): JSONObject =
        JSONObject()
            .put(
                "position",
                track.historyIndex ?: index
            )
            .put("originalTitle", track.originalTitle)
            .put("originalArtist", track.originalArtist)
            .put(
                "videoId",
                track.selectedVideoId ?: JSONObject.NULL
            )
            .put(
                "selectedTitle",
                track.selectedTitle ?: JSONObject.NULL
            )
            .put(
                "selectedChannel",
                track.selectedChannel ?: JSONObject.NULL
            )
            .put(
                "manuallySelected",
                track.manuallySelected
            )
            .put("sourceStatus", track.status.name)
            .put(
                "sourceError",
                track.error ?: JSONObject.NULL
            )
            .put(
                "candidates",
                JSONArray().also { array ->
                    track.candidates.forEach { candidate ->
                        array.put(
                            JSONObject()
                                .put("videoId", candidate.videoId)
                                .put("title", candidate.title)
                                .put(
                                    "channelTitle",
                                    candidate.channelTitle
                                )
                                .put("score", candidate.score)
                        )
                    }
                }
            )

    private fun parseCandidates(
        array: JSONArray?
    ): List<SearchCandidate> {
        if (array == null) {
            return emptyList()
        }

        val candidates =
            mutableListOf<SearchCandidate>()

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

        return candidates
    }

    private fun normalizeImportedStatus(
        rawStatus: String,
        hasExactSelection: Boolean
    ): TrackStatus {
        val parsed =
            runCatching {
                TrackStatus.valueOf(rawStatus)
            }.getOrNull()

        return when (parsed) {
            TrackStatus.MATCHED ->
                TrackStatus.MATCHED

            TrackStatus.REVIEW ->
                TrackStatus.REVIEW

            TrackStatus.SKIPPED ->
                TrackStatus.SKIPPED

            TrackStatus.MISSING ->
                TrackStatus.MISSING

            TrackStatus.FAILED ->
                TrackStatus.FAILED

            TrackStatus.NEW ->
                TrackStatus.NEW

            TrackStatus.ADDED,
            TrackStatus.DUPLICATE,
            TrackStatus.PENDING ->
                if (hasExactSelection) {
                    TrackStatus.MATCHED
                } else {
                    TrackStatus.NEW
                }

            TrackStatus.SEARCHING,
            null ->
                if (hasExactSelection) {
                    TrackStatus.MATCHED
                } else {
                    TrackStatus.NEW
                }
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
}
