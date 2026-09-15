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
 * A cache hit does NOT call YouTube Data API, so repeated imports/searches
 * of the same track do not spend another search request.
 *
 * The cache survives app restarts and normal APK updates.
 * It is removed if the app is uninstalled or its storage is cleared.
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
     * @return cached candidates, including an empty list for a cached "nothing found" result.
     * Returns null only when there is no valid cache entry.
     */
    fun get(track: Track): List<SearchCandidate>? {
        val key = keyFor(track)
        val raw = prefs.getString(key, null) ?: return null

        return runCatching {
            val root = JSONObject(raw)
            val cachedAt = root.optLong("cachedAt", 0L)

            if (cachedAt <= 0L || System.currentTimeMillis() - cachedAt > MAX_AGE_MS) {
                prefs.edit().remove(key).apply()
                return null
            }

            val items = root.optJSONArray("items") ?: JSONArray()
            val result = mutableListOf<SearchCandidate>()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val videoId = item.optString("videoId")
                val title = item.optString("title")
                val channel = item.optString("channelTitle")

                if (videoId.isBlank() || title.isBlank()) continue

                // Recalculate the score with the CURRENT scorer.
                // This means a future MatchScorer improvement can reuse cached candidates.
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
        }.getOrElse {
            prefs.edit().remove(key).apply()
            null
        }
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

        prefs.edit()
            .putString(keyFor(track), root.toString())
            .apply()
    }

    fun stats(): SearchCacheStats {
        var valid = 0
        var expired = 0
        var malformed = 0
        var approximateBytes = 0L
        var oldest: Long? = null
        var newest: Long? = null
        val now = System.currentTimeMillis()

        prefs.all.forEach { (_, value) ->
            val raw = value as? String

            if (raw == null) {
                malformed += 1
                return@forEach
            }

            approximateBytes +=
                raw.toByteArray(Charsets.UTF_8).size.toLong()

            runCatching {
                val root = JSONObject(raw)
                val cachedAt = root.optLong("cachedAt", 0L)

                if (cachedAt <= 0L) {
                    malformed += 1
                    return@runCatching
                }

                oldest =
                    oldest?.let { minOf(it, cachedAt) }
                        ?: cachedAt

                newest =
                    newest?.let { maxOf(it, cachedAt) }
                        ?: cachedAt

                if (now - cachedAt > MAX_AGE_MS) {
                    expired += 1
                } else {
                    valid += 1
                }
            }.onFailure {
                malformed += 1
            }
        }

        return SearchCacheStats(
            totalEntries = prefs.all.size,
            validEntries = valid,
            expiredEntries = expired,
            malformedEntries = malformed,
            approximateBytes = approximateBytes,
            oldestCachedAt = oldest,
            newestCachedAt = newest
        )
    }

    fun clearExpired(): Int {
        val now = System.currentTimeMillis()
        val keysToRemove = mutableListOf<String>()

        prefs.all.forEach { (key, value) ->
            val raw = value as? String

            val remove =
                if (raw == null) {
                    true
                } else {
                    runCatching {
                        val cachedAt =
                            JSONObject(raw).optLong("cachedAt", 0L)

                        cachedAt <= 0L ||
                            now - cachedAt > MAX_AGE_MS
                    }.getOrDefault(true)
                }

            if (remove) {
                keysToRemove += key
            }
        }

        if (keysToRemove.isNotEmpty()) {
            val editor = prefs.edit()
            keysToRemove.forEach(editor::remove)
            editor.apply()
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

        // Music search results are fairly stable; 30 days gives strong quota savings
        // while still allowing results to refresh eventually.
        private const val MAX_AGE_MS = 30L * 24L * 60L * 60L * 1000L
    }
}
