package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.PendingDestination
import org.json.JSONArray
import org.json.JSONObject

data class HistoryImportSummary(
    val sourceEntries: Int,
    val importEntries: Int,
    val trackCount: Int,
    val newestUpdatedAt: Long
)

class HistoryStore(context: Context) {
    private val prefs =
        context.getSharedPreferences("history_store_v1", Context.MODE_PRIVATE)

    @Synchronized
    fun getAll(): List<HistoryEntry> =
        readEntries().sortedByDescending { it.updatedAt }

    @Synchronized
    fun get(id: String): HistoryEntry? =
        readEntries().firstOrNull { it.id == id }

    @Synchronized
    fun upsert(entry: HistoryEntry) {
        val entries = readEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == entry.id }

        if (index >= 0) {
            entries[index] = entry
        } else {
            entries += entry
        }

        writeEntries(
            entries
                .sortedByDescending { it.updatedAt }
                .take(MAX_HISTORY_ENTRIES)
        )
    }

    @Synchronized
    fun remove(id: String) {
        writeEntries(readEntries().filterNot { it.id == id })
    }

    @Synchronized
    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    @Synchronized
    fun exportJson(): String {
        val raw = prefs.getString(KEY_HISTORY, "[]").orEmpty()
        return runCatching {
            JSONArray(raw).toString(2)
        }.getOrDefault("[]")
    }

    @Synchronized
    fun inspectImportJson(
        raw: String
    ): HistoryImportSummary {
        val entries =
            parseImportEntries(raw)

        require(entries.isNotEmpty()) {
            "History JSON порожній"
        }

        val uniqueIds =
            entries
                .map { it.id }
                .toSet()

        require(
            uniqueIds.size ==
                entries.size
        ) {
            "History JSON містить дублікати id"
        }

        val normalized =
            entries
                .sortedByDescending {
                    it.updatedAt
                }
                .take(
                    MAX_HISTORY_ENTRIES
                )

        return HistoryImportSummary(
            sourceEntries =
                entries.size,
            importEntries =
                normalized.size,
            trackCount =
                normalized.sumOf {
                    it.tracks.size
                },
            newestUpdatedAt =
                normalized
                    .maxOfOrNull {
                        it.updatedAt
                    }
                    ?: 0L
        )
    }

    @Synchronized
    fun normalizeImportJson(
        raw: String
    ): String {
        val parsed =
            parseImportEntries(raw)

        require(parsed.isNotEmpty()) {
            "History JSON порожній"
        }

        require(
            parsed
                .map { it.id }
                .toSet()
                .size ==
                parsed.size
        ) {
            "History JSON містить дублікати id"
        }

        val entries =
            parsed
                .sortedByDescending {
                    it.updatedAt
                }
                .take(
                    MAX_HISTORY_ENTRIES
                )

        val array =
            JSONArray()

        entries.forEach { entry ->
            array.put(
                entryToJson(entry)
            )
        }

        return array.toString()
    }

    private fun parseImportEntries(
        raw: String
    ): List<HistoryEntry> {
        val array =
            try {
                JSONArray(raw)
            } catch (
                error: Throwable
            ) {
                throw IllegalArgumentException(
                    "Очікується YTM_History_*.json — JSON-масив History",
                    error
                )
            }

        return buildList {
            for (
                i in 0 until array.length()
            ) {
                val item =
                    array.optJSONObject(i)
                        ?: throw IllegalArgumentException(
                            "History JSON: запис #${i + 1} не є об'єктом"
                        )

                validateImportObject(
                    item,
                    i
                )

                add(
                    entryFromJson(item)
                )
            }
        }
    }

    private fun validateImportObject(
        json: JSONObject,
        index: Int
    ) {
        val number =
            index + 1

        require(
            json.optString("id")
                .isNotBlank()
        ) {
            "History JSON: запис #$number не має id"
        }

        require(
            json.optString(
                "playlistName"
            ).isNotBlank()
        ) {
            "History JSON: запис #$number не має playlistName"
        }

        require(
            json.has("createdAt") &&
                json.has("updatedAt")
        ) {
            "History JSON: запис #$number не має timestamp"
        }

        val status =
            json.optString("status")

        require(
            runCatching {
                HistoryStatus.valueOf(
                    status
                )
            }.isSuccess
        ) {
            "History JSON: невідомий status у записі #$number"
        }

        val destination =
            json.optString(
                "destination"
            )

        require(
            runCatching {
                PendingDestination.valueOf(
                    destination
                )
            }.isSuccess
        ) {
            "History JSON: невідомий destination у записі #$number"
        }

        val tracks =
            json.optJSONArray(
                "tracks"
            )
                ?: throw IllegalArgumentException(
                    "History JSON: запис #$number не має tracks"
                )

        for (
            trackIndex in 0 until tracks.length()
        ) {
            val track =
                tracks.optJSONObject(
                    trackIndex
                )
                    ?: throw IllegalArgumentException(
                        "History JSON: track #${trackIndex + 1} у записі #$number не є об'єктом"
                    )

            require(
                track.has(
                    "originalTitle"
                ) &&
                    track.has(
                        "originalArtist"
                    ) &&
                    track.has(
                        "status"
                    )
            ) {
                "History JSON: track #${trackIndex + 1} у записі #$number має невірний формат"
            }
        }
    }

    private fun readEntries(): List<HistoryEntry> {
        val raw = prefs.getString(KEY_HISTORY, "[]").orEmpty()

        return runCatching {
            val array = JSONArray(raw)

            buildList {
                for (i in 0 until array.length()) {
                    add(entryFromJson(array.getJSONObject(i)))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun writeEntries(entries: List<HistoryEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(entryToJson(it)) }

        prefs.edit()
            .putString(KEY_HISTORY, array.toString())
            .apply()
    }

    private fun entryToJson(entry: HistoryEntry): JSONObject =
        JSONObject()
            .put("id", entry.id)
            .put("createdAt", entry.createdAt)
            .put("updatedAt", entry.updatedAt)
            .put("status", entry.status.name)
            .put("sourceLabel", entry.sourceLabel)
            .put("playlistName", entry.playlistName)
            .put("playlistId", entry.playlistId ?: JSONObject.NULL)
            .put("privacyStatus", entry.privacyStatus)
            .put("destination", entry.destination.name)
            .put("googleEmail", entry.googleEmail ?: JSONObject.NULL)
            .put("youtubeChannelId", entry.youtubeChannelId ?: JSONObject.NULL)
            .put("youtubeChannelTitle", entry.youtubeChannelTitle ?: JSONObject.NULL)
            .put("totalImportedCount", entry.totalImportedCount)
            .put("writeTargetCount", entry.writeTargetCount)
            .put("addedCount", entry.addedCount)
            .put("failedCount", entry.failedCount)
            .put("pendingCount", entry.pendingCount)
            .put("skippedCount", entry.skippedCount)
            .put("duplicateCount", entry.duplicateCount)
            .put("missingCount", entry.missingCount)
            .put("lastError", entry.lastError ?: JSONObject.NULL)
            .put(
                "tracks",
                JSONArray().also { array ->
                    entry.tracks.forEach { track ->
                        array.put(
                            JSONObject()
                                .put("index", track.index)
                                .put("originalTitle", track.originalTitle)
                                .put("originalArtist", track.originalArtist)
                                .put("videoId", track.videoId ?: JSONObject.NULL)
                                .put("selectedTitle", track.selectedTitle ?: JSONObject.NULL)
                                .put("selectedChannel", track.selectedChannel ?: JSONObject.NULL)
                                .put("status", track.status)
                                .put("manuallySelected", track.manuallySelected)
                                .put("error", track.error ?: JSONObject.NULL)
                        )
                    }
                }
            )

    private fun entryFromJson(json: JSONObject): HistoryEntry {
        val tracksJson = json.optJSONArray("tracks") ?: JSONArray()
        val tracks = mutableListOf<HistoryTrack>()

        for (i in 0 until tracksJson.length()) {
            val item = tracksJson.getJSONObject(i)

            tracks +=
                HistoryTrack(
                    index = item.optInt("index", i),
                    originalTitle = item.optString("originalTitle"),
                    originalArtist = item.optString("originalArtist"),
                    videoId = nullableString(item, "videoId"),
                    selectedTitle = nullableString(item, "selectedTitle"),
                    selectedChannel = nullableString(item, "selectedChannel"),
                    status = item.optString("status", "NEW"),
                    manuallySelected = item.optBoolean("manuallySelected", false),
                    error = nullableString(item, "error")
                )
        }

        return HistoryEntry(
            id = json.getString("id"),
            createdAt = json.optLong("createdAt"),
            updatedAt = json.optLong("updatedAt"),
            status =
                runCatching {
                    HistoryStatus.valueOf(
                        json.optString("status", HistoryStatus.FAILED.name)
                    )
                }.getOrDefault(HistoryStatus.FAILED),
            sourceLabel = json.optString("sourceLabel", "Невідоме джерело"),
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
            totalImportedCount = json.optInt("totalImportedCount", tracks.size),
            writeTargetCount = json.optInt("writeTargetCount", tracks.size),
            addedCount = json.optInt("addedCount", 0),
            failedCount = json.optInt("failedCount", 0),
            pendingCount = json.optInt("pendingCount", 0),
            skippedCount = json.optInt("skippedCount", 0),
            duplicateCount = json.optInt("duplicateCount", 0),
            missingCount = json.optInt("missingCount", 0),
            lastError = nullableString(json, "lastError"),
            tracks = tracks.sortedBy { it.index }
        )
    }

    private fun nullableString(json: JSONObject, key: String): String? {
        val value = json.opt(key)
        if (value == null || value == JSONObject.NULL) return null
        return value.toString().takeIf { it.isNotBlank() }
    }

    companion object {
        private const val KEY_HISTORY = "history"
        private const val MAX_HISTORY_ENTRIES = 100
    }
}
