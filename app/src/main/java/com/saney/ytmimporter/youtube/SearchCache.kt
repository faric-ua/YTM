package com.saney.ytmimporter.youtube

import android.content.Context
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Persistent local cache for YouTube search results.
 *
 * Cache entries do not expire automatically. A cache hit never calls YouTube
 * search.list, so known search knowledge remains reusable across app restarts
 * and ordinary APK updates. Full Backup/Restore includes this preference group.
 *
 * Android still removes private app storage after uninstall / Clear data unless
 * the user restores a Full Backup.
 */
data class SearchCacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val expiredEntries: Int,
    val malformedEntries: Int,
    val approximateBytes: Long,
    val oldestCachedAt: Long?,
    val newestCachedAt: Long?
)

class SearchCache(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Returns cached candidates, including an empty list for a cached
     * "nothing found" result. Returns null only when the entry is absent or
     * malformed. Valid entries never expire automatically.
     */
    fun get(track: Track): List<SearchCandidate>? {
        val key = keyFor(track)
        val raw = prefs.getString(key, null) ?: return null

        return runCatching {
            val root = JSONObject(raw)
            val cachedAt = root.optLong("cachedAt", 0L)

            require(cachedAt > 0L) {
                "Search cache entry has no valid timestamp"
            }

            val items = root.optJSONArray("items") ?: JSONArray()
            val result = mutableListOf<SearchCandidate>()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val videoId = item.optString("videoId")
                val title = item.optString("title")
                val channel = item.optString("channelTitle")

                if (videoId.isBlank() || title.isBlank()) continue

                // Recalculate with the CURRENT scorer so scoring improvements
                // can reuse the permanently stored candidate set.
                val score = MatchScorer.score(
                    artist = track.originalArtist,
                    title = track.originalTitle,
                    candidateTitle = title,
                    channel = channel
                )

                result += SearchCandidate(
                    videoId = videoId,
                    title = title,
                    channelTitle = channel,
                    score = score
                )
            }

            result.sortedByDescending { it.score }
        }.getOrNull()
    }

    fun put(track: Track, candidates: List<SearchCandidate>) {
        val items = JSONArray()

        candidates
            .distinctBy { it.videoId }
            .take(MAX_CANDIDATES)
            .forEach { candidate ->
                items.put(
                    JSONObject()
                        .put("videoId", candidate.videoId)
                        .put("title", candidate.title)
                        .put("channelTitle", candidate.channelTitle)
                )
            }

        val root = JSONObject()
            .put("cachedAt", System.currentTimeMillis())
            .put("items", items)

        check(
            prefs.edit()
                .putString(keyFor(track), root.toString())
                .commit()
        ) {
            "Не вдалося зберегти SearchCache"
        }
    }

    fun stats(): SearchCacheStats {
        var valid = 0
        var malformed = 0
        var approximateBytes = 0L
        var oldest: Long? = null
        var newest: Long? = null

        prefs.all.forEach { (_, value) ->
            val raw = value as? String

            if (raw == null) {
                malformed += 1
                return@forEach
            }

            approximateBytes += raw.toByteArray(Charsets.UTF_8).size.toLong()

            runCatching {
                val root = JSONObject(raw)
                val cachedAt = root.optLong("cachedAt", 0L)
                require(cachedAt > 0L)
                root.optJSONArray("items") ?: JSONArray()

                oldest = oldest?.let { minOf(it, cachedAt) } ?: cachedAt
                newest = newest?.let { maxOf(it, cachedAt) } ?: cachedAt
                valid += 1
            }.onFailure {
                malformed += 1
            }
        }

        return SearchCacheStats(
            totalEntries = prefs.all.size,
            validEntries = valid,
            expiredEntries = 0,
            malformedEntries = malformed,
            approximateBytes = approximateBytes,
            oldestCachedAt = oldest,
            newestCachedAt = newest
        )
    }

    /**
     * Backward-compatible maintenance hook. Search knowledge no longer expires;
     * this only removes structurally malformed entries when explicitly invoked.
     */
    fun clearExpired(): Int {
        val keysToRemove = mutableListOf<String>()

        prefs.all.forEach { (key, value) ->
            val raw = value as? String
            val malformed =
                raw == null ||
                    runCatching {
                        JSONObject(raw).optLong("cachedAt", 0L) <= 0L
                    }.getOrDefault(true)

            if (malformed) {
                keysToRemove += key
            }
        }

        if (keysToRemove.isNotEmpty()) {
            val editor = prefs.edit()
            keysToRemove.forEach(editor::remove)
            check(editor.commit()) {
                "Не вдалося очистити пошкоджені записи SearchCache"
            }
        }

        return keysToRemove.size
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun keyFor(track: Track): String {
        val normalizedArtist = MatchScorer.normalize(track.originalArtist)
        val normalizedTitle = MatchScorer.normalize(track.originalTitle)
        val rawKey = "$CACHE_SCHEMA|$normalizedArtist|$normalizedTitle"

        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(rawKey.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "youtube_search_cache"
        private const val CACHE_SCHEMA = "search-v2"
        private const val MAX_CANDIDATES = 10
    }
}
