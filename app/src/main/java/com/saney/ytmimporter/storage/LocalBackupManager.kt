package com.saney.ytmimporter.storage

import android.content.Context
import android.content.SharedPreferences
import com.saney.ytmimporter.BuildConfig
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(
    val schemaVersion: Int,
    val appVersion: String,
    val exportedAt: Long,
    val preferenceGroups: Int,
    val valueCount: Int,
    val integrityProtected: Boolean,
    val integrityVerified: Boolean
)

data class RestoreSummary(
    val preferenceGroups: Int,
    val restoredValues: Int,
    val safetySnapshotCreated: Boolean
)

class LocalBackupManager(
    private val context: Context
) {
    fun createBackupJson(): String {
        val groups = buildPreferencesJson()
        val checksum = sha256(groups.toString())
        val totalValues = countValues(groups)

        return JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("appVersion", BuildConfig.VERSION_NAME)
            .put("exportedAt", System.currentTimeMillis())
            .put("containsSensitiveData", true)
            .put(
                "note",
                "Backup may contain playlist history, Google email, " +
                    "YouTube channel IDs and cached search results. " +
                    "It does NOT contain OAuth access tokens, passwords " +
                    "or signing keys."
            )
            .put("preferences", groups)
            .put("valueCount", totalValues)
            .put("integrityAlgorithm", "SHA-256")
            .put("preferencesSha256", checksum)
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

        validateGroups(groups)

        val actualValueCount = countValues(groups)
        val declaredValueCount = root.optInt("valueCount", actualValueCount)

        require(declaredValueCount == actualValueCount) {
            "Backup пошкоджено: кількість значень не збігається"
        }

        val expectedChecksum =
            root.optString("preferencesSha256")
                .trim()

        val integrityProtected =
            schema >= 2 && expectedChecksum.isNotBlank()

        val integrityVerified =
            if (integrityProtected) {
                val actualChecksum = sha256(groups.toString())

                require(
                    actualChecksum.equals(
                        expectedChecksum,
                        ignoreCase = true
                    )
                ) {
                    "Backup пошкоджено: SHA-256 integrity check не пройдено"
                }

                true
            } else {
                false
            }

        var groupCount = 0
        PREFS_NAMES.forEach { prefsName ->
            if (groups.optJSONObject(prefsName) != null) {
                groupCount += 1
            }
        }

        return BackupSummary(
            schemaVersion = schema,
            appVersion = root.optString("appVersion", "невідомо"),
            exportedAt = root.optLong("exportedAt", 0L),
            preferenceGroups = groupCount,
            valueCount = actualValueCount,
            integrityProtected = integrityProtected,
            integrityVerified = integrityVerified
        )
    }

    fun restoreBackupJson(raw: String): RestoreSummary {
        val summary = inspectBackup(raw)
        val safetySnapshot = createBackupJson()

        saveSafetySnapshot(safetySnapshot)

        return try {
            val restoredValues = applyBackupJson(raw)

            RestoreSummary(
                preferenceGroups = summary.preferenceGroups,
                restoredValues = restoredValues,
                safetySnapshotCreated = true
            )
        } catch (error: Throwable) {
            runCatching {
                applyBackupJson(safetySnapshot)
            }

            throw IllegalStateException(
                "Restore не завершився. Поточні локальні дані " +
                    "автоматично повернуто зі safety snapshot. " +
                    (error.message ?: "Невідома помилка"),
                error
            )
        }
    }

    fun hasSafetySnapshot(): Boolean =
        safetyPrefs().contains(KEY_SAFETY_BACKUP)

    fun inspectSafetySnapshot(): BackupSummary? {
        val raw =
            safetyPrefs().getString(
                KEY_SAFETY_BACKUP,
                null
            ) ?: return null

        return inspectBackup(raw)
    }

    fun restoreSafetySnapshot(): RestoreSummary {
        val raw =
            safetyPrefs().getString(
                KEY_SAFETY_BACKUP,
                null
            ) ?: throw IllegalStateException(
                "Safety snapshot ще не створено"
            )

        val summary = inspectBackup(raw)
        val restoredValues = applyBackupJson(raw)

        return RestoreSummary(
            preferenceGroups = summary.preferenceGroups,
            restoredValues = restoredValues,
            safetySnapshotCreated = false
        )
    }

    fun clearSafetySnapshot() {
        safetyPrefs()
            .edit()
            .remove(KEY_SAFETY_BACKUP)
            .apply()
    }

    private fun buildPreferencesJson(): JSONObject {
        val groups = JSONObject()

        PREFS_NAMES.forEach { prefsName ->
            val prefs =
                context.getSharedPreferences(
                    prefsName,
                    Context.MODE_PRIVATE
                )

            val values = JSONObject()

            prefs.all
                .toSortedMap()
                .forEach { (key, value) ->
                    values.put(
                        key,
                        serializeValue(value)
                    )
                }

            groups.put(prefsName, values)
        }

        return groups
    }

    private fun validateGroups(groups: JSONObject) {
        PREFS_NAMES.forEach { prefsName ->
            val values = groups.optJSONObject(prefsName)
                ?: return@forEach

            val keys = values.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val encoded = values.optJSONObject(key)
                    ?: throw IllegalArgumentException(
                        "Backup пошкоджено: ключ $prefsName/$key " +
                            "має невірний формат"
                    )

                val type = encoded.optString("type")

                require(type in SUPPORTED_TYPES) {
                    "Backup пошкоджено: невідомий тип '$type' " +
                        "для $prefsName/$key"
                }

                require(encoded.has("value")) {
                    "Backup пошкоджено: немає value для $prefsName/$key"
                }
            }
        }
    }

    private fun countValues(groups: JSONObject): Int {
        var total = 0

        PREFS_NAMES.forEach { prefsName ->
            total +=
                groups.optJSONObject(prefsName)
                    ?.length()
                    ?: 0
        }

        return total
    }

    private fun applyBackupJson(raw: String): Int {
        inspectBackup(raw)

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
                val encoded = values.getJSONObject(key)

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

        return restoredValues
    }

    private fun saveSafetySnapshot(raw: String) {
        check(
            safetyPrefs()
                .edit()
                .putString(
                    KEY_SAFETY_BACKUP,
                    raw
                )
                .commit()
        ) {
            "Не вдалося створити safety snapshot перед Restore"
        }
    }

    private fun safetyPrefs(): SharedPreferences =
        context.getSharedPreferences(
            SAFETY_PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(
                value.toByteArray(
                    Charsets.UTF_8
                )
            )
            .joinToString("") { byte ->
                "%02x".format(byte)
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
        private const val FORMAT = "ytm-importer-local-backup"
        private const val SCHEMA_VERSION = 2

        private const val SAFETY_PREFS_NAME =
            "restore_safety_snapshot_v1"

        private const val KEY_SAFETY_BACKUP =
            "last_pre_restore_backup_json"

        private val SUPPORTED_TYPES =
            setOf(
                "null",
                "string",
                "int",
                "long",
                "boolean",
                "float",
                "stringSet"
            )

        private val PREFS_NAMES =
            listOf(
                "history_store_v1",
                "pending_jobs_v1",
                "quota_tracker_v1",
                "youtube_search_cache",
                "current_playlist_v1"
            )
    }
}
