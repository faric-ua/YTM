package com.saney.ytmimporter.bulk

import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import com.saney.ytmimporter.youtube.YouTubeLimitKind

class BulkSyncRollbackExecutor(
    private val api: YouTubeApi,
    private val sessionStore: BulkSyncSessionStore,
    private val restorableStore: RestorablePlaylistStore,
    private val currentPlaylistStore: CurrentPlaylistStore,
    private val quotaTracker: QuotaTracker
) {
    fun rollback(
        accessToken: String,
        sessionId: String,
        onProgress: (BulkSyncSession) -> Unit = {}
    ): BulkSyncSession {
        var session =
            sessionStore.get(sessionId)
                ?: throw IllegalStateException(
                    "Bulk-сесію не знайдено"
                )

        val exactnessError =
            BulkSyncRollbackPolicy
                .exactnessError(session)

        if (exactnessError != null) {
            val blocked =
                session.copy(
                    state =
                        BulkSyncSessionState
                            .ROLLBACK_PAUSED,
                    updatedAt =
                        System.currentTimeMillis(),
                    lastError =
                        exactnessError
                )

            save(
                blocked,
                onProgress
            )
            return blocked
        }

        val canStart =
            BulkSyncRollbackPolicy
                .canStartRollback(session)

        val canResume =
            BulkSyncRollbackPolicy
                .canResumeRollback(session)

        if (!canStart && !canResume) {
            return session
        }

        session =
            session.copy(
                state =
                    BulkSyncSessionState
                        .ROLLING_BACK,
                updatedAt =
                    System.currentTimeMillis(),
                lastError =
                    null
            )

        save(
            session,
            onProgress
        )

        while (true) {
            val mutation =
                BulkSyncRollbackPolicy
                    .nextAppliedMutation(
                        session
                    )

            if (mutation == null) {
                session =
                    session.copy(
                        state =
                            BulkSyncSessionState
                                .ROLLED_BACK,
                        currentPlanIndex =
                            session.plan.size,
                        updatedAt =
                            System.currentTimeMillis(),
                        lastError =
                            null
                    )

                save(
                    session,
                    onProgress
                )
                return session
            }

            val outcome =
                rollbackMutation(
                    accessToken =
                        accessToken,
                    session =
                        session,
                    mutation =
                        mutation,
                    onProgress =
                        onProgress
                )

            session =
                outcome.session

            if (!outcome.continueRollback) {
                return session
            }
        }
    }

    private fun rollbackMutation(
        accessToken: String,
        session: BulkSyncSession,
        mutation: BulkSyncMutation,
        onProgress: (BulkSyncSession) -> Unit
    ): RollbackOutcome {
        return try {
            when (mutation.type) {
                BulkSyncMutationType
                    .INSERT_PLAYLIST_ITEM -> {
                    val itemId =
                        requireNotNull(
                            mutation.createdPlaylistItemId
                        ) {
                            "Немає playlistItemId для exact rollback."
                        }

                    quotaTracker.recordGeneralUnits(
                        QuotaTracker
                            .PLAYLIST_ITEM_DELETE_COST
                    )

                    api.deletePlaylistItem(
                        accessToken =
                            accessToken,
                        playlistItemId =
                            itemId
                    )
                }

                BulkSyncMutationType
                    .CREATE_PLAYLIST -> {
                    val playlistId =
                        requireNotNull(
                            mutation.remotePlaylistId
                        ) {
                            "Немає playlistId для exact rollback."
                        }

                    quotaTracker.recordGeneralUnits(
                        QuotaTracker
                            .PLAYLIST_DELETE_COST
                    )

                    api.deletePlaylist(
                        accessToken =
                            accessToken,
                        playlistId =
                            playlistId
                    )
                }
            }

            appliedRollback(
                session =
                    session,
                mutation =
                    mutation,
                onProgress =
                    onProgress
            )
        } catch (error: Throwable) {
            val apiError =
                error as? YouTubeApiException

            if (
                apiError?.httpCode == 404
            ) {
                // DELETE is idempotent for our recovery contract:
                // if the exact ledger-owned object is already absent after
                // an interrupted rollback, treat it as rolled back.
                return appliedRollback(
                    session =
                        session,
                    mutation =
                        mutation,
                    onProgress =
                        onProgress
                )
            }

            val paused =
                session.copy(
                    state =
                        BulkSyncSessionState
                            .ROLLBACK_PAUSED,
                    updatedAt =
                        System.currentTimeMillis(),
                    lastError =
                        rollbackErrorMessage(
                            error
                        )
                )

            save(
                paused,
                onProgress
            )

            RollbackOutcome(
                session =
                    paused,
                continueRollback =
                    false
            )
        }
    }

    private fun appliedRollback(
        session: BulkSyncSession,
        mutation: BulkSyncMutation,
        onProgress: (BulkSyncSession) -> Unit
    ): RollbackOutcome {
        val now =
            System.currentTimeMillis()

        val updatedLedger =
            session.mutationLedger
                .map {
                    existing ->
                    if (
                        existing.operationId ==
                        mutation.operationId
                    ) {
                        existing.copy(
                            status =
                                BulkSyncMutationStatus
                                    .ROLLED_BACK,
                            error =
                                null,
                            updatedAt =
                                now
                        )
                    } else {
                        existing
                    }
                }

        val updated =
            session.copy(
                mutationLedger =
                    updatedLedger,
                updatedAt =
                    now,
                lastError =
                    null
            )

        save(
            updated,
            onProgress
        )

        if (
            mutation.type ==
            BulkSyncMutationType.CREATE_PLAYLIST
        ) {
            val remoteId =
                mutation.remotePlaylistId

            if (!remoteId.isNullOrBlank()) {
                clearRemoteLinkIfOwned(
                    localPlaylistId =
                        mutation.localPlaylistId,
                    remotePlaylistId =
                        remoteId
                )
            }
        }

        return RollbackOutcome(
            session =
                updated,
            continueRollback =
                true
        )
    }

    private fun clearRemoteLinkIfOwned(
        localPlaylistId: String,
        remotePlaylistId: String
    ) {
        val snapshot =
            restorableStore.get(
                localPlaylistId
            )

        if (
            snapshot?.destinationPlaylistId ==
            remotePlaylistId
        ) {
            restorableStore.upsert(
                snapshot.copy(
                    updatedAt =
                        System.currentTimeMillis(),
                    destinationPlaylistId =
                        null,
                    destinationPlaylistTitle =
                        null
                )
            )
        }

        val current =
            currentPlaylistStore.load()

        if (
            current?.localPlaylistId ==
            localPlaylistId &&
            current.destinationPlaylistId ==
            remotePlaylistId
        ) {
            currentPlaylistStore.save(
                playlist =
                    current.playlist,
                sourceLabel =
                    current.sourceLabel,
                destinationPlaylistId =
                    null,
                destinationPlaylistTitle =
                    null,
                localPlaylistId =
                    current.localPlaylistId,
                sourceHistoryId =
                    current.sourceHistoryId
            )
        }
    }

    private fun rollbackErrorMessage(
        error: Throwable
    ): String {
        val apiError =
            error as? YouTubeApiException

        if (apiError == null) {
            return "Відкат зупинено без підтвердженого HTTP результату. " +
                "Автоматичне продовження вимкнено. " +
                safeError(error)
        }

        if (apiError.httpCode == 401) {
            return "Для продовження відкату потрібна Google/YTM авторизація. " +
                "Підключіться на Home і поверніться до цієї Bulk-сесії."
        }

        return when (apiError.limitKind) {
            YouTubeLimitKind.DAILY_QUOTA ->
                "Відкат поставлено на паузу через підтверджене вичерпання добової квоти. " +
                    "Продовжіть відкат вручну після відновлення квоти."

            YouTubeLimitKind.RATE_LIMIT ->
                "Відкат поставлено на паузу через тимчасове обмеження частоти write-запитів. " +
                    "Зачекайте і продовжіть відкат вручну."

            YouTubeLimitKind.RESOURCE_LIMIT ->
                "Відкат поставлено на паузу через ресурсний ліміт Google/YouTube. " +
                    "Продовжіть його вручну пізніше."

            YouTubeLimitKind.UNKNOWN_429 ->
                "Відкат поставлено на паузу через HTTP 429 невідомого типу. " +
                    "Автоматичний retry вимкнено; продовжіть вручну пізніше."

            null ->
                "Відкат поставлено на паузу: " +
                    safeError(apiError)
        }
    }

    private fun save(
        session: BulkSyncSession,
        onProgress: (BulkSyncSession) -> Unit
    ) {
        sessionStore.upsert(
            session
        )
        onProgress(session)
    }

    private fun safeError(
        error: Throwable
    ): String =
        error.message
            ?.trim()
            ?.take(300)
            ?.takeIf {
                it.isNotBlank()
            }
            ?: error.javaClass.simpleName

    private data class RollbackOutcome(
        val session: BulkSyncSession,
        val continueRollback: Boolean
    )
}
