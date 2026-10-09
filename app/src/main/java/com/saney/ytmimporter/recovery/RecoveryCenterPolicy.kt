package com.saney.ytmimporter.recovery

import com.saney.ytmimporter.bulk.BulkSyncMutationStatus
import com.saney.ytmimporter.bulk.BulkSyncSession
import com.saney.ytmimporter.bulk.BulkSyncSessionRowState
import com.saney.ytmimporter.bulk.BulkSyncSessionState
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingOperation
import com.saney.ytmimporter.model.PendingPauseReason

enum class RecoveryClassification {
    ACTION_REQUIRED,
    WARNING
}

enum class RecoverySource {
    BULK,
    PENDING,
    HISTORY
}

enum class RecoveryRoute {
    BULK_SESSION,
    PENDING_QUEUE,
    HISTORY_DETAIL
}

data class RecoveryItem(
    val key: String,
    val source: RecoverySource,
    val classification: RecoveryClassification,
    val title: String,
    val stateLabel: String,
    val updatedAt: Long,
    val happenedLabel: String,
    val remainingLabel: String?,
    val reason: String?,
    val route: RecoveryRoute,
    val routeId: String
)

data class RecoverySnapshot(
    val items: List<RecoveryItem>
) {
    val actionableItems: List<RecoveryItem>
        get() =
            items.filter {
                it.classification ==
                    RecoveryClassification.ACTION_REQUIRED
            }

    val warningItems: List<RecoveryItem>
        get() =
            items.filter {
                it.classification ==
                    RecoveryClassification.WARNING
            }

    val actionableCount: Int
        get() = actionableItems.size
}

object RecoveryCenterPolicy {
    fun build(
        bulkSessions: List<BulkSyncSession>,
        pendingJobs: List<PendingJob>,
        historyEntries: List<HistoryEntry>
    ): RecoverySnapshot {
        val items =
            mutableListOf<RecoveryItem>()

        bulkSessions
            .mapNotNull(::bulkItem)
            .let(items::addAll)

        pendingJobs
            .map(::pendingItem)
            .let(items::addAll)

        val pendingLocalIds =
            pendingJobs
                .mapNotNull {
                    it.localPlaylistId
                        ?.takeIf(String::isNotBlank)
                }
                .toSet()

        val pendingRemoteIds =
            pendingJobs
                .mapNotNull {
                    it.playlistId
                        ?.takeIf(String::isNotBlank)
                }
                .toSet()

        historyEntries
            .filterNot {
                isCoveredByPending(
                    entry = it,
                    pendingLocalIds = pendingLocalIds,
                    pendingRemoteIds = pendingRemoteIds
                )
            }
            .mapNotNull(::historyItem)
            .let(items::addAll)

        return RecoverySnapshot(
            items =
                items.sortedWith(
                    compareByDescending<RecoveryItem> {
                        it.classification ==
                            RecoveryClassification.ACTION_REQUIRED
                    }.thenByDescending {
                        it.updatedAt
                    }
                )
        )
    }

    private fun bulkItem(
        session: BulkSyncSession
    ): RecoveryItem? {
        val classification =
            when (session.state) {
                BulkSyncSessionState.RUNNING,
                BulkSyncSessionState.PAUSED_SEARCH_QUOTA,
                BulkSyncSessionState.PAUSED_WRITE_QUOTA,
                BulkSyncSessionState.PAUSED_RATE_LIMIT,
                BulkSyncSessionState.PAUSED_CREATE_BATCH,
                BulkSyncSessionState.PAUSED_AUTH,
                BulkSyncSessionState.PAUSED_INTERRUPTED,
                BulkSyncSessionState.ROLLING_BACK,
                BulkSyncSessionState.ROLLBACK_PAUSED ->
                    RecoveryClassification.ACTION_REQUIRED

                BulkSyncSessionState.PARTIAL_FAILED ->
                    if (session.isTerminal) {
                        RecoveryClassification.WARNING
                    } else {
                        RecoveryClassification.ACTION_REQUIRED
                    }

                BulkSyncSessionState.PREVIEW,
                BulkSyncSessionState.READY,
                BulkSyncSessionState.COMPLETED,
                BulkSyncSessionState.ROLLED_BACK ->
                    return null
            }

        val applied =
            session.mutationLedger.count {
                it.status ==
                    BulkSyncMutationStatus.APPLIED
            }

        val rolledBack =
            session.mutationLedger.count {
                it.status ==
                    BulkSyncMutationStatus.ROLLED_BACK
            }

        val prepared =
            session.mutationLedger.count {
                it.status ==
                    BulkSyncMutationStatus.PREPARED
            }

        val remainingRows =
            session.plan.count {
                it.state !in
                    setOf(
                        BulkSyncSessionRowState.COMPLETED,
                        BulkSyncSessionRowState.COMPLETED_NOOP,
                        BulkSyncSessionRowState.DEFERRED_LINKED
                    )
            }

        val rollbackState =
            session.state in
                setOf(
                    BulkSyncSessionState.ROLLING_BACK,
                    BulkSyncSessionState.ROLLBACK_PAUSED
                )

        val title =
            session.plan
                .singleOrNull()
                ?.playlistName
                ?.takeIf(String::isNotBlank)
                ?: "Синхронізація всіх"

        val happened =
            buildString {
                append("Застосовано дій у YTM: ")
                append(applied)

                if (rolledBack > 0) {
                    append(" • відкочено: ")
                    append(rolledBack)
                }

                if (prepared > 0) {
                    append(" • потребує звірки: ")
                    append(prepared)
                }
            }

        val remaining =
            if (rollbackState) {
                "Залишилось відкотити дій: " +
                    applied
            } else {
                "Незавершених плейлистів: " +
                    remainingRows
            }

        return RecoveryItem(
            key = "bulk:" + session.sessionId,
            source = RecoverySource.BULK,
            classification = classification,
            title = title,
            stateLabel =
                bulkStateLabel(session.state),
            updatedAt = session.updatedAt,
            happenedLabel = happened,
            remainingLabel = remaining,
            reason = session.lastError,
            route = RecoveryRoute.BULK_SESSION,
            routeId = session.sessionId
        )
    }

    private fun pendingItem(
        job: PendingJob
    ): RecoveryItem =
        RecoveryItem(
            key = "pending:" + job.id,
            source = RecoverySource.PENDING,
            classification =
                RecoveryClassification.ACTION_REQUIRED,
            title = job.playlistName,
            stateLabel =
                pendingStateLabel(job),
            updatedAt = job.updatedAt,
            happenedLabel =
                if (
                    job.operation ==
                    PendingOperation.WRITE
                ) {
                    "Додано в YTM: " +
                        job.addedCount +
                        "/" +
                        job.totalCount
                } else {
                    "Пошук збережено для продовження"
                },
            remainingLabel =
                "Залишилось треків: " +
                    job.remainingTracks.size,
            reason = job.lastError,
            route = RecoveryRoute.PENDING_QUEUE,
            routeId = job.id
        )

    private fun historyItem(
        entry: HistoryEntry
    ): RecoveryItem? {
        val classification =
            when (entry.status) {
                HistoryStatus.RUNNING,
                HistoryStatus.PENDING_QUOTA,
                HistoryStatus.PENDING_LIMIT ->
                    RecoveryClassification.ACTION_REQUIRED

                HistoryStatus.PARTIAL,
                HistoryStatus.FAILED ->
                    if (entry.pendingCount > 0) {
                        RecoveryClassification.ACTION_REQUIRED
                    } else {
                        RecoveryClassification.WARNING
                    }

                HistoryStatus.COMPLETED ->
                    if (
                        entry.failedCount > 0 ||
                        !entry.lastError.isNullOrBlank()
                    ) {
                        RecoveryClassification.WARNING
                    } else {
                        return null
                    }
            }

        return RecoveryItem(
            key = "history:" + entry.id,
            source = RecoverySource.HISTORY,
            classification = classification,
            title = entry.playlistName,
            stateLabel =
                historyStateLabel(entry.status),
            updatedAt = entry.updatedAt,
            happenedLabel =
                if (entry.addedCount > 0) {
                    "Додано в YTM: " +
                        entry.addedCount +
                        "/" +
                        entry.writeTargetCount
                } else {
                    "Збережено в History"
                },
            remainingLabel =
                entry.pendingCount
                    .takeIf { it > 0 }
                    ?.let {
                        "Очікує треків: " + it
                    },
            reason = entry.lastError,
            route = RecoveryRoute.HISTORY_DETAIL,
            routeId = entry.id
        )
    }

    private fun isCoveredByPending(
        entry: HistoryEntry,
        pendingLocalIds: Set<String>,
        pendingRemoteIds: Set<String>
    ): Boolean {
        val localId =
            entry.localPlaylistId
                ?.takeIf(String::isNotBlank)

        if (
            localId != null &&
            localId in pendingLocalIds
        ) {
            return true
        }

        val remoteId =
            entry.playlistId
                ?.takeIf(String::isNotBlank)

        return remoteId != null &&
            remoteId in pendingRemoteIds
    }

    private fun bulkStateLabel(
        state: BulkSyncSessionState
    ): String =
        when (state) {
            BulkSyncSessionState.RUNNING ->
                "Запуск був перерваний"

            BulkSyncSessionState.PAUSED_SEARCH_QUOTA ->
                "Пошук призупинено через квоту"

            BulkSyncSessionState.PAUSED_WRITE_QUOTA ->
                "Запис призупинено через квоту"

            BulkSyncSessionState.PAUSED_RATE_LIMIT ->
                "Призупинено через ліміт запитів"

            BulkSyncSessionState.PAUSED_CREATE_BATCH ->
                "Пакет створення завершено; продовжити вручну"

            BulkSyncSessionState.PAUSED_AUTH ->
                "Потрібна Google/YTM авторизація"

            BulkSyncSessionState.PAUSED_INTERRUPTED ->
                "Операцію перервано"

            BulkSyncSessionState.ROLLING_BACK ->
                "Відкат був перерваний"

            BulkSyncSessionState.ROLLBACK_PAUSED ->
                "Відкат призупинено"

            BulkSyncSessionState.PARTIAL_FAILED ->
                "Завершено частково"

            BulkSyncSessionState.PREVIEW,
            BulkSyncSessionState.READY,
            BulkSyncSessionState.COMPLETED,
            BulkSyncSessionState.ROLLED_BACK ->
                "Завершено"
        }

    private fun pendingStateLabel(
        job: PendingJob
    ): String {
        val operation =
            if (
                job.operation ==
                PendingOperation.SEARCH
            ) {
                "Пошук"
            } else {
                "Запис у YTM"
            }

        val reason =
            when (job.pauseReason) {
                PendingPauseReason.SEARCH_QUOTA ->
                    "квота Search"

                PendingPauseReason.DAILY_QUOTA ->
                    "денна квота"

                PendingPauseReason.RATE_LIMIT ->
                    "ліміт запитів"

                PendingPauseReason.RESOURCE_LIMIT ->
                    "ліміт ресурсу"

                PendingPauseReason.UNKNOWN_API_LIMIT ->
                    "обмеження API"

                null ->
                    "очікує продовження"
            }

        return operation + " • " + reason
    }

    private fun historyStateLabel(
        status: HistoryStatus
    ): String =
        when (status) {
            HistoryStatus.RUNNING ->
                "Операцію було перервано"

            HistoryStatus.PENDING_QUOTA ->
                "Очікує через квоту"

            HistoryStatus.PENDING_LIMIT ->
                "Очікує через ліміт"

            HistoryStatus.PARTIAL ->
                "Завершено частково"

            HistoryStatus.FAILED ->
                "Завершено з помилкою"

            HistoryStatus.COMPLETED ->
                "Завершено з попередженням"
        }
}
