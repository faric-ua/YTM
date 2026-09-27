package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.bulk.BulkSyncBaselineItem
import com.saney.ytmimporter.bulk.BulkSyncBaselinePlaylist
import com.saney.ytmimporter.bulk.BulkSyncMutation
import com.saney.ytmimporter.bulk.BulkSyncMutationStatus
import com.saney.ytmimporter.bulk.BulkSyncMutationType
import com.saney.ytmimporter.bulk.BulkSyncPlanState
import com.saney.ytmimporter.bulk.BulkSyncRemoteBaseline
import com.saney.ytmimporter.bulk.BulkSyncSession
import com.saney.ytmimporter.bulk.BulkSyncSessionRow
import com.saney.ytmimporter.bulk.BulkSyncSessionRowState
import com.saney.ytmimporter.bulk.BulkSyncSessionState
import com.saney.ytmimporter.bulk.BulkSyncSessionTrack
import org.json.JSONArray
import org.json.JSONObject

class BulkSyncSessionStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun getAll(): List<BulkSyncSession> {
        val raw =
            prefs.getString(
                KEY_SESSIONS,
                "[]"
            ).orEmpty()

        return runCatching {
            val array = JSONArray(raw)

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    val root =
                        array.optJSONObject(index)
                            ?: continue

                    sessionFromJson(root)
                        ?.let(::add)
                }
            }.sortedByDescending {
                it.updatedAt
            }
        }.getOrDefault(
            emptyList()
        )
    }

    @Synchronized
    fun get(
        sessionId: String
    ): BulkSyncSession? =
        getAll().firstOrNull {
            it.sessionId == sessionId
        }

    @Synchronized
    fun active(): BulkSyncSession? {
        val id =
            prefs.getString(
                KEY_ACTIVE_SESSION_ID,
                null
            )
                ?: return null

        return get(id)
    }

    @Synchronized
    fun latest(): BulkSyncSession? =
        getAll().firstOrNull()

    @Synchronized
    fun upsert(
        session: BulkSyncSession,
        makeActive: Boolean = true
    ) {
        val items =
            getAll().toMutableList()

        val index =
            items.indexOfFirst {
                it.sessionId ==
                    session.sessionId
            }

        if (index >= 0) {
            items[index] = session
        } else {
            items += session
        }

        val array = JSONArray()

        items.sortedByDescending {
            it.updatedAt
        }.forEach {
                item ->
            array.put(
                sessionToJson(item)
            )
        }

        val editor =
            prefs.edit()
                .putString(
                    KEY_SESSIONS,
                    array.toString()
                )

        if (makeActive) {
            editor.putString(
                KEY_ACTIVE_SESSION_ID,
                session.sessionId
            )
        }

        check(editor.commit()) {
            "Не вдалося зберегти Bulk-сесію"
        }
    }

    @Synchronized
    fun setActive(
        sessionId: String
    ) {
        check(
            get(sessionId) != null
        ) {
            "Bulk-сесію не знайдено"
        }

        check(
            prefs.edit()
                .putString(
                    KEY_ACTIVE_SESSION_ID,
                    sessionId
                )
                .commit()
        ) {
            "Не вдалося вибрати Bulk-сесію"
        }
    }

    private fun sessionToJson(
        session: BulkSyncSession
    ): JSONObject =
        JSONObject()
            .put(
                "schemaVersion",
                SCHEMA_VERSION
            )
            .put(
                "sessionId",
                session.sessionId
            )
            .put(
                "createdAt",
                session.createdAt
            )
            .put(
                "updatedAt",
                session.updatedAt
            )
            .put(
                "state",
                session.state.name
            )
            .putNullable(
                "googleEmail",
                session.googleEmail
            )
            .putNullable(
                "youtubeChannelId",
                session.youtubeChannelId
            )
            .putNullable(
                "youtubeChannelTitle",
                session.youtubeChannelTitle
            )
            .put(
                "checkpointId",
                session.checkpointId
            )
            .put(
                "remoteBaseline",
                baselineToJson(
                    session.remoteBaseline
                )
            )
            .put(
                "plan",
                JSONArray().also {
                        array ->
                    session.plan.forEach {
                            array.put(
                                rowToJson(it)
                            )
                        }
                }
            )
            .put(
                "currentPlanIndex",
                session.currentPlanIndex
            )
            .put(
                "mutationLedger",
                JSONArray().also {
                        array ->
                    session.mutationLedger
                        .forEach {
                            array.put(
                                mutationToJson(it)
                            )
                        }
                }
            )
            .putNullable(
                "lastError",
                session.lastError
            )

    private fun sessionFromJson(
        root: JSONObject
    ): BulkSyncSession? {
        val id =
            root.optString(
                "sessionId"
            )

        if (id.isBlank()) {
            return null
        }

        val plan =
            mutableListOf<BulkSyncSessionRow>()

        root.optJSONArray(
            "plan"
        )?.let {
                array ->
            for (
                index in
                0 until array.length()
            ) {
                array.optJSONObject(index)
                    ?.let(::rowFromJson)
                    ?.let(plan::add)
            }
        }

        val ledger =
            mutableListOf<BulkSyncMutation>()

        root.optJSONArray(
            "mutationLedger"
        )?.let {
                array ->
            for (
                index in
                0 until array.length()
            ) {
                array.optJSONObject(index)
                    ?.let(::mutationFromJson)
                    ?.let(ledger::add)
            }
        }

        val state =
            enumValue(
                root.optString("state"),
                BulkSyncSessionState.READY
            )

        return BulkSyncSession(
            sessionId = id,
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
            state = state,
            googleEmail =
                root.optNullableString(
                    "googleEmail"
                ),
            youtubeChannelId =
                root.optNullableString(
                    "youtubeChannelId"
                ),
            youtubeChannelTitle =
                root.optNullableString(
                    "youtubeChannelTitle"
                ),
            checkpointId =
                root.optString(
                    "checkpointId"
                ),
            remoteBaseline =
                root.optJSONObject(
                    "remoteBaseline"
                )?.let(
                    ::baselineFromJson
                )
                    ?: BulkSyncRemoteBaseline(
                        capturedAt = 0L,
                        googleEmail = null,
                        youtubeChannelId = null,
                        youtubeChannelTitle = null,
                        playlists =
                            emptyList()
                    ),
            plan = plan,
            currentPlanIndex =
                root.optInt(
                    "currentPlanIndex",
                    0
                ),
            mutationLedger = ledger,
            lastError =
                root.optNullableString(
                    "lastError"
                )
        )
    }

    private fun rowToJson(
        row: BulkSyncSessionRow
    ): JSONObject =
        JSONObject()
            .put(
                "localPlaylistId",
                row.localPlaylistId
            )
            .put(
                "playlistName",
                row.playlistName
            )
            .put(
                "sourceLabel",
                row.sourceLabel
            )
            .put(
                "originalPlanState",
                row.originalPlanState.name
            )
            .put(
                "state",
                row.state.name
            )
            .put(
                "privacyStatus",
                row.privacyStatus
            )
            .putNullable(
                "remotePlaylistId",
                row.remotePlaylistId
            )
            .put(
                "tracks",
                JSONArray().also {
                        array ->
                    row.tracks.forEach {
                            track ->
                        array.put(
                            JSONObject()
                                .put(
                                    "trackIndex",
                                    track.trackIndex
                                )
                                .putNullable(
                                    "historyIndex",
                                    track.historyIndex
                                )
                                .put(
                                    "videoId",
                                    track.videoId
                                )
                                .put(
                                    "originalTitle",
                                    track.originalTitle
                                )
                                .put(
                                    "originalArtist",
                                    track.originalArtist
                                )
                        )
                    }
                }
            )
            .putNullable(
                "lastError",
                row.lastError
            )

    private fun rowFromJson(
        root: JSONObject
    ): BulkSyncSessionRow? {
        val localId =
            root.optString(
                "localPlaylistId"
            )

        if (localId.isBlank()) {
            return null
        }

        val tracks =
            mutableListOf<BulkSyncSessionTrack>()

        root.optJSONArray(
            "tracks"
        )?.let {
                array ->
            for (
                index in
                0 until array.length()
            ) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                val videoId =
                    item.optString(
                        "videoId"
                    )

                if (videoId.isBlank()) {
                    continue
                }

                tracks +=
                    BulkSyncSessionTrack(
                        trackIndex =
                            item.optInt(
                                "trackIndex",
                                index
                            ),
                        historyIndex =
                            item.optNullableInt(
                                "historyIndex"
                            ),
                        videoId = videoId,
                        originalTitle =
                            item.optString(
                                "originalTitle"
                            ),
                        originalArtist =
                            item.optString(
                                "originalArtist"
                            )
                    )
            }
        }

        return BulkSyncSessionRow(
            localPlaylistId = localId,
            playlistName =
                root.optString(
                    "playlistName"
                ),
            sourceLabel =
                root.optString(
                    "sourceLabel"
                ),
            originalPlanState =
                enumValue(
                    root.optString(
                        "originalPlanState"
                    ),
                    BulkSyncPlanState.BLOCKED
                ),
            state =
                enumValue(
                    root.optString(
                        "state"
                    ),
                    BulkSyncSessionRowState.BLOCKED
                ),
            privacyStatus =
                root.optString(
                    "privacyStatus",
                    "private"
                ),
            remotePlaylistId =
                root.optNullableString(
                    "remotePlaylistId"
                ),
            tracks = tracks,
            lastError =
                root.optNullableString(
                    "lastError"
                )
        )
    }

    private fun mutationToJson(
        mutation: BulkSyncMutation
    ): JSONObject =
        JSONObject()
            .put(
                "operationId",
                mutation.operationId
            )
            .put(
                "type",
                mutation.type.name
            )
            .put(
                "localPlaylistId",
                mutation.localPlaylistId
            )
            .putNullable(
                "remotePlaylistId",
                mutation.remotePlaylistId
            )
            .putNullable(
                "videoId",
                mutation.videoId
            )
            .putNullable(
                "trackIndex",
                mutation.trackIndex
            )
            .putNullable(
                "createdPlaylistItemId",
                mutation.createdPlaylistItemId
            )
            .put(
                "status",
                mutation.status.name
            )
            .putNullable(
                "error",
                mutation.error
            )
            .put(
                "updatedAt",
                mutation.updatedAt
            )

    private fun mutationFromJson(
        root: JSONObject
    ): BulkSyncMutation? {
        val operationId =
            root.optString(
                "operationId"
            )

        if (operationId.isBlank()) {
            return null
        }

        return BulkSyncMutation(
            operationId = operationId,
            type =
                enumValue(
                    root.optString(
                        "type"
                    ),
                    BulkSyncMutationType
                        .INSERT_PLAYLIST_ITEM
                ),
            localPlaylistId =
                root.optString(
                    "localPlaylistId"
                ),
            remotePlaylistId =
                root.optNullableString(
                    "remotePlaylistId"
                ),
            videoId =
                root.optNullableString(
                    "videoId"
                ),
            trackIndex =
                root.optNullableInt(
                    "trackIndex"
                ),
            createdPlaylistItemId =
                root.optNullableString(
                    "createdPlaylistItemId"
                ),
            status =
                enumValue(
                    root.optString(
                        "status"
                    ),
                    BulkSyncMutationStatus
                        .PREPARED
                ),
            error =
                root.optNullableString(
                    "error"
                ),
            updatedAt =
                root.optLong(
                    "updatedAt",
                    0L
                )
        )
    }

    private fun baselineToJson(
        baseline: BulkSyncRemoteBaseline
    ): JSONObject =
        JSONObject()
            .put(
                "capturedAt",
                baseline.capturedAt
            )
            .putNullable(
                "googleEmail",
                baseline.googleEmail
            )
            .putNullable(
                "youtubeChannelId",
                baseline.youtubeChannelId
            )
            .putNullable(
                "youtubeChannelTitle",
                baseline.youtubeChannelTitle
            )
            .put(
                "playlists",
                JSONArray().also {
                        array ->
                    baseline.playlists
                        .forEach {
                            playlist ->
                            array.put(
                                JSONObject()
                                    .put(
                                        "playlistId",
                                        playlist.playlistId
                                    )
                                    .put(
                                        "title",
                                        playlist.title
                                    )
                                    .put(
                                        "privacyStatus",
                                        playlist.privacyStatus
                                    )
                                    .put(
                                        "items",
                                        JSONArray().also {
                                                items ->
                                            playlist.items
                                                .forEach {
                                                    item ->
                                                    items.put(
                                                        JSONObject()
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
                                                    )
                                                }
                                        }
                                    )
                            )
                        }
                }
            )

    private fun baselineFromJson(
        root: JSONObject
    ): BulkSyncRemoteBaseline {
        val playlists =
            mutableListOf<BulkSyncBaselinePlaylist>()

        root.optJSONArray(
            "playlists"
        )?.let {
                array ->
            for (
                index in
                0 until array.length()
            ) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                val playlistId =
                    item.optString(
                        "playlistId"
                    )

                if (playlistId.isBlank()) {
                    continue
                }

                val baselineItems =
                    mutableListOf<BulkSyncBaselineItem>()

                item.optJSONArray(
                    "items"
                )?.let {
                        items ->
                    for (
                        itemIndex in
                        0 until items.length()
                    ) {
                        val baselineItem =
                            items.optJSONObject(
                                itemIndex
                            )
                                ?: continue

                        baselineItems +=
                            BulkSyncBaselineItem(
                                playlistItemId =
                                    baselineItem
                                        .optNullableString(
                                            "playlistItemId"
                                        ),
                                sourcePosition =
                                    baselineItem
                                        .optNullableInt(
                                            "sourcePosition"
                                        ),
                                videoId =
                                    baselineItem
                                        .optNullableString(
                                            "videoId"
                                        )
                            )
                    }
                }

                playlists +=
                    BulkSyncBaselinePlaylist(
                        playlistId =
                            playlistId,
                        title =
                            item.optString(
                                "title"
                            ),
                        privacyStatus =
                            item.optString(
                                "privacyStatus"
                            ),
                        items =
                            baselineItems
                    )
            }
        }

        return BulkSyncRemoteBaseline(
            capturedAt =
                root.optLong(
                    "capturedAt",
                    0L
                ),
            googleEmail =
                root.optNullableString(
                    "googleEmail"
                ),
            youtubeChannelId =
                root.optNullableString(
                    "youtubeChannelId"
                ),
            youtubeChannelTitle =
                root.optNullableString(
                    "youtubeChannelTitle"
                ),
            playlists = playlists
        )
    }

    private fun <T : Enum<T>> enumValue(
        raw: String,
        fallback: T
    ): T =
        runCatching {
            java.lang.Enum.valueOf(
                fallback.declaringJavaClass,
                raw
            )
        }.getOrDefault(
            fallback
        )

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
        if (!has(key) || isNull(key)) {
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
        if (!has(key) || isNull(key)) {
            null
        } else {
            optInt(key)
        }

    companion object {
        const val PREFS_NAME =
            "bulk_sync_session_v1"

        private const val KEY_SESSIONS =
            "sessions"

        private const val KEY_ACTIVE_SESSION_ID =
            "active_session_id"

        private const val SCHEMA_VERSION =
            1
    }
}
