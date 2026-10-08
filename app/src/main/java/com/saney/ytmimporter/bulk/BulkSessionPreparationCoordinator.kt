package com.saney.ytmimporter.bulk

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.storage.BulkSyncCheckpointStore
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import com.saney.ytmimporter.storage.LocalBackupManager
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.Executors

/**
 * Single-flight, process-retained preparation owner for the Bulk preview UI.
 * The worker holds only application context, never an Activity or Dialog.
 * Rotation detaches the observer but does NOT restart a checkpoint/baseline.
 * This work reads YouTube and persists a READY session; it never writes to YTM.
 */
object BulkSessionPreparationCoordinator {
    enum class Step(val label: String) {
        CHECKPOINT("Зберігаю контрольну точку"),
        BASELINE("Перевіряю плейлисти в YouTube Music"),
        SESSION("Готую сесію")
    }

    sealed class Status {
        data object Idle : Status()
        data class Preparing(val step: Step) : Status()
        data class Ready(val sessionId: String) : Status()
        data class Failed(val message: String) : Status()
    }

    private val worker =
        Executors.newSingleThreadExecutor()
    private val main =
        Handler(Looper.getMainLooper())

    private var status: Status = Status.Idle
    private var listener: ((Status) -> Unit)? = null
    private var navigationPending = false

    @Synchronized
    fun current(): Status = status

    @Synchronized
    fun observe(callback: (Status) -> Unit) {
        listener = callback
        main.post {
            if (isCurrentObserver(callback)) {
                callback(current())
            }
        }
    }

    @Synchronized
    fun detach(callback: (Status) -> Unit) {
        if (listener === callback) listener = null
    }

    @Synchronized
    private fun isCurrentObserver(callback: (Status) -> Unit): Boolean =
        listener === callback

    @Synchronized
    fun consumeReadyNavigation(sessionId: String): Boolean {
        val ready = status as? Status.Ready ?: return false
        if (!navigationPending || ready.sessionId != sessionId) return false
        navigationPending = false
        return true
    }

    @Synchronized
    fun clearFailure() {
        if (status is Status.Failed) {
            status = Status.Idle
        }
    }

    fun start(
        context: Context,
        summary: BulkSyncPlanSummary,
        accessToken: String,
        auth: AuthSessionStore.Snapshot
    ): Boolean {
        synchronized(this) {
            if (status is Status.Preparing) return false
            status = Status.Preparing(Step.CHECKPOINT)
            navigationPending = false
        }
        publish(Status.Preparing(Step.CHECKPOINT))

        val appContext = context.applicationContext
        worker.execute {
            try {
                val checkpointJson =
                    LocalBackupManager(appContext)
                        .createBulkSyncCheckpointJson()
                val checkpoint =
                    BulkSyncCheckpointStore(appContext)
                        .save(checkpointJson)

                transition(Status.Preparing(Step.BASELINE))
                val baseline =
                    captureBaseline(
                        context = appContext,
                        accessToken = accessToken,
                        auth = auth
                    )
                if (baseline.second > 0) {
                    QuotaTracker(appContext)
                        .recordGeneralUnits(baseline.second)
                }

                transition(Status.Preparing(Step.SESSION))
                val session =
                    BulkSyncSessionFactory.create(
                        summary = summary,
                        snapshots =
                            RestorablePlaylistStore(appContext).getAll(),
                        checkpointId = checkpoint.checkpointId,
                        baseline = baseline.first
                    )
                BulkSyncSessionStore(appContext).upsert(session)
                synchronized(this) {
                    navigationPending = true
                }
                transition(Status.Ready(session.sessionId))
            } catch (error: Throwable) {
                transition(
                    Status.Failed(
                        error.message
                            ?.trim()
                            ?.take(180)
                            ?.takeIf(String::isNotBlank)
                            ?: error.javaClass.simpleName
                    )
                )
            }
        }
        return true
    }

    private fun transition(next: Status) {
        synchronized(this) {
            status = next
        }
        publish(next)
    }

    private fun publish(snapshot: Status) {
        main.post {
            val callback = synchronized(this) { listener }
            callback?.invoke(snapshot)
        }
    }

    private fun captureBaseline(
        context: Context,
        accessToken: String,
        auth: AuthSessionStore.Snapshot
    ): Pair<BulkSyncRemoteBaseline, Int> {
        val api = YouTubeApi(
            accessTokenRecovery =
                GoogleAccessTokenRecovery(context)
        )
        var readUnits = 0
        val owned = api.listMyPlaylists(accessToken = accessToken) {
            readUnits += QuotaTracker.SIMPLE_LIST_COST
        }
        val playlists = owned.map { info ->
            val snapshot = api.listPlaylistSnapshotItems(
                accessToken = accessToken,
                playlistId = info.id
            ) {
                readUnits += QuotaTracker.SIMPLE_LIST_COST
            }
            BulkSyncBaselinePlaylist(
                playlistId = info.id,
                title = info.title,
                privacyStatus = info.privacyStatus,
                items = snapshot.items.map { item ->
                    BulkSyncBaselineItem(
                        playlistItemId = item.playlistItemId,
                        sourcePosition = item.sourcePosition,
                        videoId = item.videoId
                    )
                }
            )
        }

        return Pair(
            BulkSyncRemoteBaseline(
                capturedAt = System.currentTimeMillis(),
                googleEmail = auth.googleAccountInfo?.email,
                youtubeChannelId = auth.youtubeChannelInfo?.id,
                youtubeChannelTitle = auth.youtubeChannelInfo?.title,
                playlists = playlists
            ),
            readUnits
        )
    }
}
