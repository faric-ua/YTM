package com.saney.ytmimporter.recovery

import android.content.Context
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.PendingJobStore

class RecoveryCenterSource(
    context: Context
) {
    private val appContext =
        context.applicationContext

    fun snapshot(): RecoverySnapshot =
        RecoveryCenterPolicy.build(
            bulkSessions =
                BulkSyncSessionStore(
                    appContext
                ).getAll(),
            pendingJobs =
                PendingJobStore(
                    appContext
                ).getAll(),
            historyEntries =
                HistoryStore(
                    appContext
                ).getAll()
        )
}
