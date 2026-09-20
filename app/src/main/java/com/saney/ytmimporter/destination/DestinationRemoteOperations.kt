package com.saney.ytmimporter.destination

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

/**
 * Process-local owner for Destination read operations.
 *
 * Playlist listing and duplicate scanning stay attached to DestinationActivity
 * instead of finishing it and exposing Main/Home as a transit screen.
 */
object DestinationRemoteOperations {
    enum class Kind {
        LOAD_PLAYLISTS,
        SCAN_DUPLICATES
    }

    data class ScanPayload(
        val target: YouTubePlaylistInfo,
        val alreadyCount: Int,
        val repeatedCount: Int,
        val newCount: Int,
        val requestCount: Int,
        val skipPositions: IntArray
    )

    data class State(
        val kind: Kind? = null,
        val running: Boolean = false,
        val message: String = "",
        val terminalSerial: Long = 0L,
        val playlists:
            List<YouTubePlaylistInfo>? = null,
        val scan:
            ScanPayload? = null,
        val target:
            YouTubePlaylistInfo? = null,
        val errorMessage:
            String? = null,
        val authorizationInvalidated:
            Boolean = false
    )

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val executor =
        Executors.newSingleThreadExecutor()

    private val listeners =
        CopyOnWriteArraySet<(State) -> Unit>()

    @Volatile
    private var state =
        State()

    private var serialCounter =
        0L

    fun addListener(
        listener: (State) -> Unit
    ) {
        listeners += listener
        val snapshot = state

        mainHandler.post {
            if (listener in listeners) {
                listener(snapshot)
            }
        }
    }

    fun removeListener(
        listener: (State) -> Unit
    ) {
        listeners -= listener
    }

    @Synchronized
    fun startLoad(
        context: Context
    ): Boolean {
        if (state.running) {
            return false
        }

        val appContext =
            context.applicationContext

        val token =
            currentToken()
                ?: return terminalError(
                    kind =
                        Kind.LOAD_PLAYLISTS,
                    message =
                        "Підключіть Google / YTM на головному екрані",
                    authorizationInvalidated =
                        true
                )

        publish(
            State(
                kind =
                    Kind.LOAD_PLAYLISTS,
                running =
                    true,
                message =
                    "Завантажую ваші плейлисти…"
            )
        )

        executor.execute {
            val coordinator =
                coordinator(
                    appContext
                )

            runCatching {
                coordinator
                    .loadExistingPlaylists(
                        accessToken =
                            token
                    )
            }.onSuccess {
                playlists ->
                terminalPlaylists(
                    playlists
                )
            }.onFailure {
                error ->
                val authFailure =
                    isAuthorizationFailure(
                        error
                    )

                if (authFailure) {
                    invalidateAuthorization(
                        appContext
                    )
                }

                terminalError(
                    kind =
                        Kind.LOAD_PLAYLISTS,
                    message =
                        if (authFailure) {
                            "Авторизацію Google / YTM потрібно відновити."
                        } else {
                            error.message
                                ?: "Не вдалося завантажити плейлисти"
                        },
                    authorizationInvalidated =
                        authFailure
                )
            }
        }

        return true
    }

    @Synchronized
    fun startScan(
        context: Context,
        target:
            YouTubePlaylistInfo
    ): Boolean {
        if (state.running) {
            return false
        }

        val appContext =
            context.applicationContext

        val snapshot =
            CurrentPlaylistStore(
                appContext
            ).load()
                ?: return terminalError(
                    kind =
                        Kind.SCAN_DUPLICATES,
                    message =
                        "Поточний плейлист уже недоступний",
                    target =
                        target
                )

        val coordinator =
            coordinator(
                appContext
            )

        val selected =
            coordinator
                .currentTracksForDestination(
                    snapshot.playlist
                )

        if (selected.isEmpty()) {
            return terminalError(
                kind =
                    Kind.SCAN_DUPLICATES,
                message =
                    "Немає треків для запису",
                target =
                    target
            )
        }

        val token =
            currentToken()
                ?: return terminalError(
                    kind =
                        Kind.SCAN_DUPLICATES,
                    message =
                        "Підключіть Google / YTM на головному екрані",
                    target =
                        target,
                    authorizationInvalidated =
                        true
                )

        publish(
            State(
                kind =
                    Kind.SCAN_DUPLICATES,
                running =
                    true,
                message =
                    "Перевіряю дублікати у «${target.title}»…",
                target =
                    target
            )
        )

        executor.execute {
            runCatching {
                coordinator
                    .scanDuplicates(
                        accessToken =
                            token,
                        selected =
                            selected,
                        target =
                            target
                    )
            }.onSuccess {
                result ->
                val skip =
                    result.analysis
                        .tracksToSkip

                val skipPositions =
                    selected
                        .indices
                        .filter {
                            index ->
                            skip.any {
                                duplicate ->
                                duplicate ===
                                    selected[index]
                            }
                        }
                        .toIntArray()

                terminalScan(
                    ScanPayload(
                        target =
                            result.target,
                        alreadyCount =
                            result.analysis
                                .alreadyInPlaylist
                                .size,
                        repeatedCount =
                            result.analysis
                                .repeatedInImport
                                .size,
                        newCount =
                            result.analysis
                                .tracksToAdd
                                .size,
                        requestCount =
                            result.requestCount,
                        skipPositions =
                            skipPositions
                    )
                )
            }.onFailure {
                error ->
                val authFailure =
                    isAuthorizationFailure(
                        error
                    )

                if (authFailure) {
                    invalidateAuthorization(
                        appContext
                    )
                }

                terminalError(
                    kind =
                        Kind.SCAN_DUPLICATES,
                    message =
                        if (authFailure) {
                            "Авторизацію Google / YTM потрібно відновити."
                        } else {
                            error.message
                                ?: "Не вдалося перевірити дублікати"
                        },
                    target =
                        target,
                    authorizationInvalidated =
                        authFailure
                )
            }
        }

        return true
    }

    @Synchronized
    fun acknowledgeTerminal(
        serial: Long
    ) {
        if (
            !state.running &&
            state.terminalSerial ==
                serial
        ) {
            state =
                State()
        }
    }

    private fun currentToken():
        String? =
        AuthSessionStore
            .current()
            .accessToken
            ?.takeIf {
                it.isNotBlank()
            }

    private fun coordinator(
        context: Context
    ): DestinationCoordinator =
        DestinationCoordinator(
            api =
                YouTubeApi(
                    accessTokenRecovery =
                        GoogleAccessTokenRecovery(
                            context
                        )
                ),
            quotaTracker =
                QuotaTracker(context)
        )

    private fun invalidateAuthorization(
        context: Context
    ) {
        AuthSessionStore.clear()
        PersistentAuthStateStore(
            context
        ).clear()
    }

    private fun isAuthorizationFailure(
        error: Throwable
    ): Boolean {
        var current:
            Throwable? =
            error

        while (current != null) {
            if (
                current is
                    YouTubeApiException &&
                current.httpCode ==
                    401
            ) {
                return true
            }

            current =
                current.cause
        }

        return false
    }

    @Synchronized
    private fun terminalPlaylists(
        playlists:
            List<YouTubePlaylistInfo>
    ) {
        serialCounter += 1

        publish(
            State(
                kind =
                    Kind.LOAD_PLAYLISTS,
                terminalSerial =
                    serialCounter,
                playlists =
                    playlists
            )
        )
    }

    @Synchronized
    private fun terminalScan(
        scan:
            ScanPayload
    ) {
        serialCounter += 1

        publish(
            State(
                kind =
                    Kind.SCAN_DUPLICATES,
                terminalSerial =
                    serialCounter,
                scan =
                    scan,
                target =
                    scan.target
            )
        )
    }

    @Synchronized
    private fun terminalError(
        kind: Kind,
        message: String,
        target:
            YouTubePlaylistInfo? = null,
        authorizationInvalidated:
            Boolean = false
    ): Boolean {
        serialCounter += 1

        publish(
            State(
                kind =
                    kind,
                terminalSerial =
                    serialCounter,
                target =
                    target,
                errorMessage =
                    message,
                authorizationInvalidated =
                    authorizationInvalidated
            )
        )

        return false
    }

    private fun publish(
        next: State
    ) {
        state = next

        mainHandler.post {
            listeners.forEach {
                listener ->
                listener(next)
            }
        }
    }
}
