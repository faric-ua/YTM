package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.library.PlaylistDescriptionPolicy
import com.saney.ytmimporter.library.PlaylistDescriptionPolicy.Description
import com.saney.ytmimporter.library.PlaylistDescriptionPolicy.State
import org.json.JSONArray
import org.json.JSONObject

/**
 * Batch 21: the owner's playlist descriptions (strip of descriptions, the
 * active one, the «Опис» tick, how many playlists used each). Phone only;
 * nothing is ever deleted from the strip.
 */
class PlaylistDescriptionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    @Synchronized
    fun load(): State {
        val raw = prefs.getString(KEY, null)
        val state = runCatching { raw?.let(::decode) }.getOrNull() ?: State()
        val seeded = PlaylistDescriptionPolicy.seeded(state, System.currentTimeMillis())
        if (seeded != state) save(seeded)
        return seeded
    }

    @Synchronized
    fun save(state: State) {
        prefs.edit().putString(KEY, encode(state)).apply()
    }

    @Synchronized
    fun update(change: (State) -> State): State {
        val next = change(load())
        save(next)
        return next
    }

    /** The description for a new playlist and the id to count after success. */
    @Synchronized
    fun forNewPlaylist(): Pair<String, String?> {
        val state = load()
        return PlaylistDescriptionPolicy.forNewPlaylist(state) to PlaylistDescriptionPolicy.usedId(state)
    }

    @Synchronized
    fun markUsed(id: String?) {
        if (id != null) update { PlaylistDescriptionPolicy.markUsed(it, id) }
    }

    private fun encode(state: State): String = JSONObject()
        .put("active", state.activeId ?: JSONObject.NULL)
        .put("enabled", state.enabled)
        .put("items", JSONArray().apply {
            state.descriptions.forEach {
                put(JSONObject().put("id", it.id).put("text", it.text)
                    .put("createdAt", it.createdAt).put("uses", it.uses))
            }
        }).toString()

    private fun decode(raw: String): State {
        val json = JSONObject(raw)
        val items = json.optJSONArray("items") ?: JSONArray()
        return State(
            descriptions = (0 until items.length()).map { i ->
                val item = items.getJSONObject(i)
                Description(item.getString("id"), item.getString("text"),
                    item.optLong("createdAt"), item.optInt("uses"))
            },
            activeId = json.optString("active").takeIf { it.isNotBlank() && it != "null" },
            enabled = json.optBoolean("enabled", true)
        )
    }

    private companion object {
        const val PREFS_NAME = "playlist_descriptions"
        const val KEY = "state"
    }
}
