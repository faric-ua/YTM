package com.saney.ytmimporter.storage

import android.content.Context
import com.saney.ytmimporter.bulk.BulkSyncQaFaultKind

/**
 * Temporary one-shot phone-QA switches for v1.4.54.
 *
 * Test 5: simulates a daily-quota failure before one Bulk insert.
 * Test 8: interrupts rollback immediately after one successfully persisted
 * reverse mutation, leaving the durable session in ROLLING_BACK so cold-open
 * recovery can be verified deterministically.
 *
 * Remove these QA surfaces before the final release closeout.
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

    @Synchronized
    fun armRollbackInterruptAfterOne() {
        check(
            prefs.edit()
                .putBoolean(
                    KEY_ROLLBACK_INTERRUPT_AFTER_ONE,
                    true
                )
                .commit()
        ) {
            "Не вдалося увімкнути Test 8 rollback interrupt"
        }
    }

    @Synchronized
    fun isRollbackInterruptAfterOneArmed():
        Boolean =
        prefs.getBoolean(
            KEY_ROLLBACK_INTERRUPT_AFTER_ONE,
            false
        )

    @Synchronized
    fun consumeRollbackInterruptAfterOne():
        Boolean {
        if (
            !isRollbackInterruptAfterOneArmed()
        ) {
            return false
        }

        check(
            prefs.edit()
                .remove(
                    KEY_ROLLBACK_INTERRUPT_AFTER_ONE
                )
                .commit()
        ) {
            "Не вдалося вимкнути Test 8 rollback interrupt"
        }

        return true
    }

    @Synchronized
    fun clearRollbackInterruptAfterOne() {
        check(
            prefs.edit()
                .remove(
                    KEY_ROLLBACK_INTERRUPT_AFTER_ONE
                )
                .commit()
        ) {
            "Не вдалося очистити Test 8 rollback interrupt"
        }
    }

    companion object {
        const val PREFS_NAME =
            "bulk_sync_qa_fault_v1"

        private const val KEY_NEXT_INSERT_FAULT =
            "next_insert_fault"

        private const val KEY_ROLLBACK_INTERRUPT_AFTER_ONE =
            "rollback_interrupt_after_one"
    }
}
