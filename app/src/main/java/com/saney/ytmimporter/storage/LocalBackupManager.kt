package com.saney.ytmimporter.storage

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(
    val schemaVersion: Int,
    val appVersion: String,
    val exportedAt: Long,
    val preferenceGroups: Int,
    val valueCount: Int
)

data class RestoreSummary(
    val preferenceGroups: Int,
    val restoredValues: Int
)

class LocalBackupManager(
    private val context: Context
) {
    fun createBackupJson(): String {
        val groups = JSONObject()
        var totalValues = 0

        PREFS_NAMES.forEach { prefsName ->
            val prefs =
                context.getSharedPreferences(
                    prefsName,
                    Context.MODE_PRIVATE
                )

            val values = JSONObject()

            prefs.all.forEach { (key, value) ->
                values.put(
                    key,
                    serializeValue(value)
                )
                totalValues += 1
            }

            groups.put(prefsName, values)
        }

        return JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("appVersion", APP_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put(
                "containsSensitiveData",
                true
            )
            .put(
                "note",
                "Backup may contain playlist history, Google email, " +
                    "YouTube channel IDs and cached search results. " +
                    "It does NOT contain OAuth access tokens, passwords " +
                    "or signing keys."
            )
            .put("preferences", groups)
            .put("valueCount", totalValues)
            .toString(2)
    }

    fun inspectBackup(raw: String): BackupSummary {
        val root = JSONObject(raw)

        require(root.optString("format") == FORMAT) {
            "Це не backup YTM Importer"
        }

        val schema = root.optInt("schemaVersion", -1)

        require(schema in 1..SCHEMA_VERSION) {
            "Непідтримувана версія backup: $schema"
        }

        val groups =
            root.optJSONObject("preferences")
                ?: throw IllegalArgumentException(
                    "У backup немає секції preferences"
                )

        var valueCount = 0
        var groupCount = 0

        PREFS_NAMES.forEach { prefsName ->
            val values = groups.optJSONObject(prefsName)

            if (values != null) {
                groupCount += 1
                valueCount += values.length()
            }
        }

        return BackupSummary(
            schemaVersion = schema,
            appVersion = root.optString("appVersion", "невідомо"),
            exportedAt = root.optLong("exportedAt", 0L),
            preferenceGroups = groupCount,
            valueCount = valueCount
        )
    }

    fun restoreBackupJson(raw: String): RestoreSummary {
        val summary = inspectBackup(raw)
        val root = JSONObject(raw)
        val groups = root.getJSONObject("preferences")

        var restoredValues = 0

        PREFS_NAMES.forEach { prefsName ->
            val values = groups.optJSONObject(prefsName)
                ?: return@forEach

            val prefs =
                context.getSharedPreferences(
                    prefsName,
                    Context.MODE_PRIVATE
                )

            val editor = prefs.edit().clear()
            val keys = values.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val encoded = values.optJSONObject(key) ?: continue

                restoreValue(
                    editor = editor,
                    key = key,
                    encoded = encoded
                )

                restoredValues += 1
            }

            check(editor.commit()) {
                "Не вдалося записати $prefsName"
            }
        }

        return RestoreSummary(
            preferenceGroups = summary.preferenceGroups,
            restoredValues = restoredValues
        )
    }

    private fun serializeValue(value: Any?): JSONObject {
        val root = JSONObject()

        when (value) {
            null -> {
                root.put("type", "null")
                root.put("value", JSONObject.NULL)
            }

            is String -> {
                root.put("type", "string")
                root.put("value", value)
            }

            is Int -> {
                root.put("type", "int")
                root.put("value", value)
            }

            is Long -> {
                root.put("type", "long")
                root.put("value", value)
            }

            is Boolean -> {
                root.put("type", "boolean")
                root.put("value", value)
            }

            is Float -> {
                root.put("type", "float")
                root.put("value", value.toDouble())
            }

            is Set<*> -> {
                root.put("type", "stringSet")
                root.put(
                    "value",
                    JSONArray().also { array ->
                        value
                            .filterIsInstance<String>()
                            .sorted()
                            .forEach(array::put)
                    }
                )
            }

            else -> {
                root.put("type", "string")
                root.put("value", value.toString())
            }
        }

        return root
    }

    private fun restoreValue(
        editor: SharedPreferences.Editor,
        key: String,
        encoded: JSONObject
    ) {
        when (encoded.optString("type")) {
            "null" ->
                editor.remove(key)

            "string" ->
                editor.putString(
                    key,
                    encoded.optString("value")
                )

            "int" ->
                editor.putInt(
                    key,
                    encoded.optInt("value")
                )

            "long" ->
                editor.putLong(
                    key,
                    encoded.optLong("value")
                )

            "boolean" ->
                editor.putBoolean(
                    key,
                    encoded.optBoolean("value")
                )

            "float" ->
                editor.putFloat(
                    key,
                    encoded.optDouble("value").toFloat()
                )

            "stringSet" -> {
                val array =
                    encoded.optJSONArray("value") ?: JSONArray()

                val set = linkedSetOf<String>()

                for (i in 0 until array.length()) {
                    val item = array.optString(i)

                    if (item.isNotBlank()) {
                        set += item
                    }
                }

                editor.putStringSet(key, set)
            }

            else ->
                throw IllegalArgumentException(
                    "Невідомий тип значення backup для ключа $key"
                )
        }
    }

    companion object {
        const val APP_VERSION = "0.14.0"

        private const val FORMAT = "ytm-importer-local-backup"
        private const val SCHEMA_VERSION = 1

        private val PREFS_NAMES =
            listOf(
                "history_store_v1",
                "pending_jobs_v1",
                "quota_tracker_v1",
                "youtube_search_cache"
            )
    }
}
