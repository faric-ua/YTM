package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingOperation
import com.saney.ytmimporter.model.PendingPauseReason
import com.saney.ytmimporter.model.PendingSearchCandidate
import com.saney.ytmimporter.model.PendingSearchSnapshot
import com.saney.ytmimporter.model.PendingSearchTrack
import com.saney.ytmimporter.model.PendingTrack
import org.json.JSONArray
import org.json.JSONObject

class PendingJobStore(context: Context) {
    private val prefs =
        context.getSharedPreferences("pending_jobs_v1", Context.MODE_PRIVATE)

    @Synchronized
    fun getAll(): List<PendingJob> =
        readJobs().sortedByDescending { it.updatedAt }

    @Synchronized
    fun get(jobId: String): PendingJob? =
        readJobs().firstOrNull { it.id == jobId }

    @Synchronized
    fun findSearchByRecoveryKey(
        recoveryKey: String
    ): PendingJob? =
        readJobs().firstOrNull {
            it.operation == PendingOperation.SEARCH &&
                it.recoveryKey == recoveryKey
        }

    @Synchronized
    fun upsert(job: PendingJob) {
        val jobs = readJobs().toMutableList()
        val index = jobs.indexOfFirst { it.id == job.id }

        if (index >= 0) {
            jobs[index] = job
        } else {
            jobs += job
        }

        writeJobs(jobs)
    }

    @Synchronized
    fun remove(jobId: String) {
        writeJobs(readJobs().filterNot { it.id == jobId })
    }

    @Synchronized
    fun exportJson(): String {
        val raw = prefs.getString(KEY_JOBS, "[]").orEmpty()
        return runCatching {
            JSONArray(raw).toString(2)
        }.getOrDefault("[]")
    }

    private fun readJobs(): List<PendingJob> {
        val raw = prefs.getString(KEY_JOBS, "[]").orEmpty()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    add(jobFromJson(array.getJSONObject(i)))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun writeJobs(jobs: List<PendingJob>) {
        val array = JSONArray()
        jobs.forEach { array.put(jobToJson(it)) }

        prefs.edit()
            .putString(KEY_JOBS, array.toString())
            .apply()
    }

    private fun jobToJson(job: PendingJob): JSONObject =
        JSONObject()
            .put("id", job.id)
            .put("createdAt", job.createdAt)
            .put("updatedAt", job.updatedAt)
            .put("sourceLabel", job.sourceLabel)
            .put("playlistName", job.playlistName)
            .put("playlistId", job.playlistId ?: JSONObject.NULL)
            .put("privacyStatus", job.privacyStatus)
            .put("destination", job.destination.name)
            .put("googleEmail", job.googleEmail ?: JSONObject.NULL)
            .put("youtubeChannelId", job.youtubeChannelId ?: JSONObject.NULL)
            .put("youtubeChannelTitle", job.youtubeChannelTitle ?: JSONObject.NULL)
            .put("totalCount", job.totalCount)
            .put("addedCount", job.addedCount)
            .put("failedCount", job.failedCount)
            .put(
                "remainingTracks",
                JSONArray().also { array ->
                    job.remainingTracks.forEach { track ->
                        array.put(
                            JSONObject()
                                .put("originalTitle", track.originalTitle)
                                .put("originalArtist", track.originalArtist)
                                .put("videoId", track.videoId)
                                .put("selectedTitle", track.selectedTitle ?: JSONObject.NULL)
                                .put("selectedChannel", track.selectedChannel ?: JSONObject.NULL)
                                .put("historyIndex", track.historyIndex)
                        )
                    }
                }
            )
            .put("lastError", job.lastError ?: JSONObject.NULL)
            .put(
                "pauseReason",
                job.pauseReason?.name ?: JSONObject.NULL
            )
            .put("operation", job.operation.name)
            .put("recoveryKey", job.recoveryKey ?: JSONObject.NULL)
            .put("preserveExistingExact", job.preserveExistingExact)
            .put(
                "searchSnapshot",
                job.searchSnapshot
                    ?.let(::searchSnapshotToJson)
                    ?: JSONObject.NULL
            )

    private fun jobFromJson(json: JSONObject): PendingJob {
        val tracksJson = json.optJSONArray("remainingTracks") ?: JSONArray()
        val tracks = mutableListOf<PendingTrack>()

        for (i in 0 until tracksJson.length()) {
            val item = tracksJson.getJSONObject(i)
            tracks += PendingTrack(
                originalTitle = item.optString("originalTitle"),
                originalArtist = item.optString("originalArtist"),
                videoId = item.optString("videoId"),
                selectedTitle = nullableString(item, "selectedTitle"),
                selectedChannel = nullableString(item, "selectedChannel"),
                historyIndex = item.optInt("historyIndex", i)
            )
        }

        return PendingJob(
            id = json.getString("id"),
            createdAt = json.optLong("createdAt"),
            updatedAt = json.optLong("updatedAt"),
            sourceLabel = json.optString("sourceLabel", "Черга з попередньої версії"),
            playlistName = json.optString("playlistName", "YTM Importer"),
            playlistId = nullableString(json, "playlistId"),
            privacyStatus = json.optString("privacyStatus", "private"),
            destination =
                runCatching {
                    PendingDestination.valueOf(
                        json.optString(
                            "destination",
                            PendingDestination.EXISTING_PLAYLIST.name
                        )
                    )
                }.getOrDefault(PendingDestination.EXISTING_PLAYLIST),
            googleEmail = nullableString(json, "googleEmail"),
            youtubeChannelId = nullableString(json, "youtubeChannelId"),
            youtubeChannelTitle = nullableString(json, "youtubeChannelTitle"),
            totalCount = json.optInt("totalCount", tracks.size),
            addedCount = json.optInt("addedCount", 0),
            failedCount = json.optInt("failedCount", 0),
            remainingTracks = tracks,
            lastError = nullableString(json, "lastError"),
            pauseReason =
                nullableString(
                    json,
                    "pauseReason"
                )?.let { value ->
                    runCatching {
                        PendingPauseReason.valueOf(
                            value
                        )
                    }.getOrNull()
                },
            operation =
                runCatching {
                    PendingOperation.valueOf(
                        json.optString(
                            "operation",
                            PendingOperation.WRITE.name
                        )
                    )
                }.getOrDefault(
                    PendingOperation.WRITE
                ),
            recoveryKey =
                nullableString(
                    json,
                    "recoveryKey"
                ),
            preserveExistingExact =
                json.optBoolean(
                    "preserveExistingExact",
                    false
                ),
            searchSnapshot =
                json.optJSONObject(
                    "searchSnapshot"
                )?.let(
                    ::searchSnapshotFromJson
                )
        )
    }

    private fun searchSnapshotToJson(
        snapshot: PendingSearchSnapshot
    ): JSONObject =
        JSONObject()
            .put(
                "playlistName",
                snapshot.playlistName
            )
            .put(
                "tracks",
                JSONArray().also { array ->
                    snapshot.tracks.forEach { track ->
                        array.put(
                            JSONObject()
                                .put(
                                    "originalTitle",
                                    track.originalTitle
                                )
                                .put(
                                    "originalArtist",
                                    track.originalArtist
                                )
                                .put(
                                    "selectedVideoId",
                                    track.selectedVideoId
                                        ?: JSONObject.NULL
                                )
                                .put(
                                    "selectedTitle",
                                    track.selectedTitle
                                        ?: JSONObject.NULL
                                )
                                .put(
                                    "selectedChannel",
                                    track.selectedChannel
                                        ?: JSONObject.NULL
                                )
                                .put(
                                    "status",
                                    track.status
                                )
                                .put(
                                    "manuallySelected",
                                    track.manuallySelected
                                )
                                .put(
                                    "error",
                                    track.error
                                        ?: JSONObject.NULL
                                )
                                .put(
                                    "historyIndex",
                                    track.historyIndex
                                        ?: JSONObject.NULL
                                )
                                .put(
                                    "candidates",
                                    JSONArray().also {
                                            candidates ->
                                        track.candidates.forEach {
                                                candidate ->
                                            candidates.put(
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
                        )
                    }
                }
            )

    private fun searchSnapshotFromJson(
        json: JSONObject
    ): PendingSearchSnapshot {
        val tracksJson =
            json.optJSONArray("tracks")
                ?: JSONArray()

        val tracks =
            mutableListOf<PendingSearchTrack>()

        for (
            index in 0 until
                tracksJson.length()
        ) {
            val item =
                tracksJson.optJSONObject(
                    index
                ) ?: continue

            val candidatesJson =
                item.optJSONArray(
                    "candidates"
                ) ?: JSONArray()

            val candidates =
                mutableListOf<
                    PendingSearchCandidate
                >()

            for (
                candidateIndex in 0 until
                    candidatesJson.length()
            ) {
                val candidate =
                    candidatesJson
                        .optJSONObject(
                            candidateIndex
                        ) ?: continue

                val videoId =
                    candidate
                        .optString(
                            "videoId"
                        )

                if (
                    videoId.isBlank()
                ) {
                    continue
                }

                candidates +=
                    PendingSearchCandidate(
                        videoId =
                            videoId,
                        title =
                            candidate
                                .optString(
                                    "title"
                                ),
                        channelTitle =
                            candidate
                                .optString(
                                    "channelTitle"
                                ),
                        score =
                            candidate
                                .optDouble(
                                    "score",
                                    0.0
                                )
                    )
            }

            tracks +=
                PendingSearchTrack(
                    originalTitle =
                        item.optString(
                            "originalTitle"
                        ),
                    originalArtist =
                        item.optString(
                            "originalArtist"
                        ),
                    selectedVideoId =
                        nullableString(
                            item,
                            "selectedVideoId"
                        ),
                    selectedTitle =
                        nullableString(
                            item,
                            "selectedTitle"
                        ),
                    selectedChannel =
                        nullableString(
                            item,
                            "selectedChannel"
                        ),
                    status =
                        item.optString(
                            "status",
                            "NEW"
                        ),
                    manuallySelected =
                        item.optBoolean(
                            "manuallySelected",
                            false
                        ),
                    error =
                        nullableString(
                            item,
                            "error"
                        ),
                    historyIndex =
                        nullableInt(
                            item,
                            "historyIndex"
                        ),
                    candidates =
                        candidates
                )
        }

        return PendingSearchSnapshot(
            playlistName =
                json.optString(
                    "playlistName",
                    "YTM Import"
                ),
            tracks =
                tracks
        )
    }

    private fun nullableString(
        json: JSONObject,
        key: String
    ): String? {
        val value = json.opt(key)
        if (value == null || value == JSONObject.NULL) return null
        return value.toString().takeIf { it.isNotBlank() }
    }

    private fun nullableInt(
        json: JSONObject,
        key: String
    ): Int? =
        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            null
        } else {
            json.optInt(key)
        }

    companion object {
        private const val KEY_JOBS = "jobs"
    }
}
