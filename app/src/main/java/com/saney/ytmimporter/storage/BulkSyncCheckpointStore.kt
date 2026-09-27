package com.saney.ytmimporter.storage

import android.content.Context
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class BulkSyncCheckpoint(
    val checkpointId: String,
    val createdAt: Long,
    val backupJson: String
)

class BulkSyncCheckpointStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun save(
        backupJson: String
    ): BulkSyncCheckpoint {
        val checkpoint =
            BulkSyncCheckpoint(
                checkpointId =
                    UUID.randomUUID().toString(),
                createdAt =
                    System.currentTimeMillis(),
                backupJson =
                    backupJson
            )

        val items =
            getAll().toMutableList()

        items += checkpoint

        val kept =
            items.sortedByDescending {
                it.createdAt
            }.take(MAX_CHECKPOINTS)

        writeAll(kept)
        return checkpoint
    }

    @Synchronized
    fun get(
        checkpointId: String
    ): BulkSyncCheckpoint? =
        getAll().firstOrNull {
            it.checkpointId ==
                checkpointId
        }

    @Synchronized
    fun getAll(): List<BulkSyncCheckpoint> {
        val raw =
            prefs.getString(
                KEY_CHECKPOINTS,
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

                    val id =
                        root.optString(
                            "checkpointId"
                        )

                    if (id.isBlank()) {
                        continue
                    }

                    add(
                        BulkSyncCheckpoint(
                            checkpointId = id,
                            createdAt =
                                root.optLong(
                                    "createdAt",
                                    0L
                                ),
                            backupJson =
                                root.optString(
                                    "backupJson"
                                )
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )
    }

    private fun writeAll(
        checkpoints: List<BulkSyncCheckpoint>
    ) {
        val array = JSONArray()

        checkpoints.forEach {
                checkpoint ->
            array.put(
                JSONObject()
                    .put(
                        "checkpointId",
                        checkpoint.checkpointId
                    )
                    .put(
                        "createdAt",
                        checkpoint.createdAt
                    )
                    .put(
                        "backupJson",
                        checkpoint.backupJson
                    )
            )
        }

        check(
            prefs.edit()
                .putString(
                    KEY_CHECKPOINTS,
                    array.toString()
                )
                .commit()
        ) {
            "Не вдалося зберегти Bulk checkpoint"
        }
    }

    companion object {
        const val PREFS_NAME =
            "bulk_sync_checkpoint_v1"

        private const val KEY_CHECKPOINTS =
            "checkpoints"

        private const val MAX_CHECKPOINTS =
            10
    }
}
