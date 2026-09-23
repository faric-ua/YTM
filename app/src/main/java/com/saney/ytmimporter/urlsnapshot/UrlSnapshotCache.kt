package com.saney.ytmimporter.urlsnapshot

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class UrlSnapshotCacheEntry(
    val resolved: UrlSnapshotResolutionResult.Resolved,
    val cachedAt: Long
)

data class UrlSnapshotCacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val malformedEntries: Int,
    val approximateBytes: Long,
    val oldestCachedAt: Long?,
    val newestCachedAt: Long?
)

/**
 * Persistent cache of concrete playlist snapshots.
 *
 * Entries do not expire automatically. Reading the same playlist can therefore
 * reuse the last explicit snapshot with zero new playlistItems.list requests.
 * A fresh remote read is only started through an explicit forceRemote action.
 */
class UrlSnapshotCache(
    context: Context
) {
    private val prefs =
        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

    @Synchronized
    fun get(
        source: UrlSnapshotSource
    ): UrlSnapshotCacheEntry? {
        if (
            source.kind !=
                UrlSnapshotSourceKind.CONCRETE_PLAYLIST
        ) {
            return null
        }

        val raw =
            prefs.getString(
                source.playlistId,
                null
            ) ?: return null

        return runCatching {
            decode(
                raw = raw,
                source = source
            )
        }.getOrNull()
    }

    @Synchronized
    fun put(
        resolved:
            UrlSnapshotResolutionResult.Resolved
    ): Long {
        require(
            resolved.source.kind ==
                UrlSnapshotSourceKind.CONCRETE_PLAYLIST
        ) {
            "Only concrete playlist snapshots are cacheable"
        }

        val cachedAt =
            System.currentTimeMillis()

        val raw =
            encode(
                resolved = resolved,
                cachedAt = cachedAt
            )

        check(
            prefs.edit()
                .putString(
                    resolved.source.playlistId,
                    raw
                )
                .commit()
        ) {
            "Не вдалося зберегти URL snapshot cache"
        }

        return cachedAt
    }

    fun stats(): UrlSnapshotCacheStats {
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

            approximateBytes +=
                raw.toByteArray(
                    Charsets.UTF_8
                ).size.toLong()

            runCatching {
                val root =
                    JSONObject(raw)

                require(
                    root.optInt(
                        "schemaVersion",
                        -1
                    ) == SCHEMA_VERSION
                )

                val cachedAt =
                    root.optLong(
                        "cachedAt",
                        0L
                    )

                require(
                    cachedAt > 0L
                )

                root.getJSONArray(
                    "items"
                )

                oldest =
                    oldest?.let {
                        minOf(
                            it,
                            cachedAt
                        )
                    } ?: cachedAt

                newest =
                    newest?.let {
                        maxOf(
                            it,
                            cachedAt
                        )
                    } ?: cachedAt

                valid += 1
            }.onFailure {
                malformed += 1
            }
        }

        return UrlSnapshotCacheStats(
            totalEntries =
                prefs.all.size,
            validEntries =
                valid,
            malformedEntries =
                malformed,
            approximateBytes =
                approximateBytes,
            oldestCachedAt =
                oldest,
            newestCachedAt =
                newest
        )
    }

    fun clear() {
        prefs.edit()
            .clear()
            .apply()
    }

    private fun encode(
        resolved:
            UrlSnapshotResolutionResult.Resolved,
        cachedAt: Long
    ): String =
        JSONObject()
            .put(
                "schemaVersion",
                SCHEMA_VERSION
            )
            .put(
                "cachedAt",
                cachedAt
            )
            .put(
                "requestCount",
                resolved.requestCount
            )
            .put(
                "items",
                JSONArray().also {
                        array ->
                    resolved.items.forEach {
                            item ->
                        array.put(
                            JSONObject()
                                .put(
                                    "index",
                                    item.index
                                )
                                .putNullable(
                                    "playlistItemId",
                                    item.playlistItemId
                                )
                                .putNullable(
                                    "sourcePosition",
                                    item.sourcePosition
                                )
                                .putNullable(
                                    "videoId",
                                    item.videoId
                                )
                                .putNullable(
                                    "title",
                                    item.title
                                )
                                .putNullable(
                                    "channelTitle",
                                    item.channelTitle
                                )
                                .put(
                                    "availability",
                                    item.availability.name
                                )
                                .putNullable(
                                    "unavailableReason",
                                    item.unavailableReason
                                        ?.name
                                )
                        )
                    }
                }
            )
            .toString()

    private fun decode(
        raw: String,
        source: UrlSnapshotSource
    ): UrlSnapshotCacheEntry {
        val root =
            JSONObject(raw)

        require(
            root.optInt(
                "schemaVersion",
                -1
            ) == SCHEMA_VERSION
        ) {
            "Unsupported URL snapshot cache schema"
        }

        val cachedAt =
            root.optLong(
                "cachedAt",
                0L
            )

        require(
            cachedAt > 0L
        ) {
            "URL snapshot cache timestamp missing"
        }

        val itemsJson =
            root.getJSONArray(
                "items"
            )

        val items =
            mutableListOf<
                UrlSnapshotResolvedItem
            >()

        for (
            position in
            0 until itemsJson.length()
        ) {
            val item =
                itemsJson
                    .getJSONObject(
                        position
                    )

            val availability =
                UrlSnapshotAvailability
                    .valueOf(
                        item.getString(
                            "availability"
                        )
                    )

            val reason =
                item.optNullableString(
                    "unavailableReason"
                )
                    ?.let(
                        UrlSnapshotUnavailableReason
                            ::valueOf
                    )

            items +=
                UrlSnapshotResolvedItem(
                    index =
                        item.optInt(
                            "index",
                            position
                        ),
                    playlistItemId =
                        item.optNullableString(
                            "playlistItemId"
                        ),
                    sourcePosition =
                        item.optNullableInt(
                            "sourcePosition"
                        ),
                    videoId =
                        item.optNullableString(
                            "videoId"
                        ),
                    title =
                        item.optNullableString(
                            "title"
                        ),
                    channelTitle =
                        item.optNullableString(
                            "channelTitle"
                        ),
                    availability =
                        availability,
                    unavailableReason =
                        reason
                )
        }

        return UrlSnapshotCacheEntry(
            resolved =
                UrlSnapshotResolutionResult
                    .Resolved(
                        source = source,
                        items = items,
                        requestCount =
                            root.optInt(
                                "requestCount",
                                0
                            )
                    ),
            cachedAt =
                cachedAt
        )
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ): JSONObject {
        if (value == null) {
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
        const val PREFS_NAME =
            "url_snapshot_cache_v1"

        private const val SCHEMA_VERSION =
            1
    }
}
