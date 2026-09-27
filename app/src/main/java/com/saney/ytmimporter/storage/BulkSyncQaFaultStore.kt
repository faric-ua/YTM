package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.bulk.BulkSyncQaFaultKind

/**
 * Temporary one-shot phone-QA switch for v1.4.54 Test 5.
 *
 * It never changes QuotaTracker counters by itself and is consumed before
 * one Bulk playlistItems.insert attempt. Remove this QA surface before the
 * final release closeout.
 */
class BulkSyncQaFaultStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun arm(
        kind: BulkSyncQaFaultKind
    ) {
        check(
            prefs.edit()
                .putString(
                    KEY_NEXT_INSERT_FAULT,
                    kind.name
                )
                .commit()
        ) {
            "Не вдалося увімкнути QA fault"
        }
    }

    @Synchronized
    fun peek():
        BulkSyncQaFaultKind? =
        prefs.getString(
            KEY_NEXT_INSERT_FAULT,
            null
        )?.let {
            raw ->
            runCatching {
                BulkSyncQaFaultKind
                    .valueOf(raw)
            }.getOrNull()
        }

    @Synchronized
    fun consume():
        BulkSyncQaFaultKind? {
        val kind =
            peek()
                ?: return null

        check(
            prefs.edit()
                .remove(
                    KEY_NEXT_INSERT_FAULT
                )
                .commit()
        ) {
            "Не вдалося вимкнути QA fault"
        }

        return kind
    }

    @Synchronized
    fun clear() {
        check(
            prefs.edit()
                .remove(
                    KEY_NEXT_INSERT_FAULT
                )
                .commit()
        ) {
            "Не вдалося очистити QA fault"
        }
    }

    companion object {
        const val PREFS_NAME =
            "bulk_sync_qa_fault_v1"

        private const val KEY_NEXT_INSERT_FAULT =
            "next_insert_fault"
    }
}
