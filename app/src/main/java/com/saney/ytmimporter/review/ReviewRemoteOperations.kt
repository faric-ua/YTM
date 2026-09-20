package com.saney.ytmimporter.review

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.search.SearchCoordinator
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

/**
 * Process-local owner for remote Review operations.
 *
 * The operation itself survives Activity recreation. ReviewActivity only
 * renders the current state, so rotation does not force a navigation relay
 * through Main/Home.
 */
object ReviewRemoteOperations {
    enum class Kind {
        SEARCH,
        MANUAL_URL
    }

    data class State(
        val kind: Kind? = null,
        val running: Boolean = false,
        val message: String = "",
        val terminalSerial: Long = 0L,
        val terminalMessage: String? = null,
        val focusHistoryIndex: Int? = null,
        val authorizationInvalidated: Boolean = false
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

    fun current(): State =
        state

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

    fun planSearch(
        context: Context,
        preserveExistingExact: Boolean
    ): SearchCoordinator.SearchPlan? {
        val appContext =
            context.applicationContext

        val playlist =
            CurrentPlaylistStore(appContext)
                .load()
                ?.playlist
                ?: return null

        return searchCoordinator(appContext)
            .plan(
                playlist = playlist,
                preserveExistingExact =
                    preserveExistingExact
            )
    }

    @Synchronized
    fun startSearch(
        context: Context,
        preserveExistingExact: Boolean
    ): Boolean {
        if (state.running) {
            return false
        }

        val appContext =
            context.applicationContext

        val store =
            CurrentPlaylistStore(appContext)

        val snapshot =
            store.load()
                ?: return publishTerminal(
                    kind = Kind.SEARCH,
                    message =
                        "Поточний плейлист уже недоступний"
                )

        val token =
            AuthSessionStore
                .current()
                .accessToken
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return publishTerminal(
                    kind = Kind.SEARCH,
                    message =
                        "Підключіть Google / YTM на головному екрані"
                )

        publish(
            State(
                kind = Kind.SEARCH,
                running = true,
                message =
                    "Починаю пошук…"
            )
        )

        executor.execute {
            val coordinator =
                searchCoordinator(
                    appContext
                )

            val result =
                runCatching {
                    coordinator.run(
                        accessToken = token,
                        playlist =
                            snapshot.playlist,
                        preserveExistingExact =
                            preserveExistingExact,
                        onProgress = {
                            progress ->
                            publish(
                                state.copy(
                                    kind =
                                        Kind.SEARCH,
                                    running =
                                        true,
                                    message =
                                        "Пошук " +
                                            "${progress.processed}/" +
                                            "${progress.total} • " +
                                            "кеш ${progress.cacheHits} • " +
                                            "API ${progress.apiSearches}"
                                )
                            )
                        },
                        onQuotaBlocked = {
                            publish(
                                state.copy(
                                    kind =
                                        Kind.SEARCH,
                                    running =
                                        true,
                                    message =
                                        "Квота пошуку вичерпана; " +
                                            "завершую кешовані результати…"
                                )
                            )
                        }
                    )
                }

            result.onSuccess {
                searchResult ->
                store.save(
                    playlist =
                        snapshot.playlist,
                    sourceLabel =
                        snapshot.sourceLabel,
                    destinationPlaylistId =
                        snapshot.destinationPlaylistId
                )

                if (
                    searchResult
                        .authorizationInvalidated
                ) {
                    invalidateAuthorization(
                        appContext
                    )
                }

                val message =
                    when {
                        searchResult
                            .authorizationInvalidated ->
                            "Пошук зупинено: " +
                                "потрібно знову підключити Google / YTM."

                        searchResult
                            .quotaBlocked ->
                            "Пошук завершено частково: " +
                                "квота search.list закінчилась."

                        else ->
                            "Пошук завершено: " +
                                "кеш ${searchResult.cacheHits}, " +
                                "API ${searchResult.apiSearches}."
                    }

                publishTerminal(
                    kind = Kind.SEARCH,
                    message = message,
                    authorizationInvalidated =
                        searchResult
                            .authorizationInvalidated
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

                publishTerminal(
                    kind = Kind.SEARCH,
                    message =
                        if (authFailure) {
                            "Авторизацію Google / YTM потрібно відновити."
                        } else {
                            error.message
                                ?: "Не вдалося виконати пошук"
                        },
                    authorizationInvalidated =
                        authFailure
                )
            }
        }

        return true
    }

    @Synchronized
    fun startManualLookup(
        context: Context,
        historyIndex: Int,
        videoId: String
    ): Boolean {
        if (state.running) {
            return false
        }

        val appContext =
            context.applicationContext

        val store =
            CurrentPlaylistStore(
                appContext
            )

        val snapshot =
            store.load()
                ?: return publishTerminal(
                    kind =
                        Kind.MANUAL_URL,
                    message =
                        "Поточний плейлист уже недоступний",
                    focusHistoryIndex =
                        historyIndex
                )

        val track =
            snapshot
                .playlist
                .tracks
                .firstOrNull {
                    it.historyIndex ==
                        historyIndex
                }
                ?: return publishTerminal(
                    kind =
                        Kind.MANUAL_URL,
                    message =
                        "Не вдалося знайти трек для ручної заміни",
                    focusHistoryIndex =
                        historyIndex
                )

        val token =
            AuthSessionStore
                .current()
                .accessToken
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return publishTerminal(
                    kind =
                        Kind.MANUAL_URL,
                    message =
                        "Підключіть Google / YTM на головному екрані",
                    focusHistoryIndex =
                        historyIndex
                )

        publish(
            State(
                kind =
                    Kind.MANUAL_URL,
                running =
                    true,
                message =
                    "Отримую назву та канал через YouTube API…",
                focusHistoryIndex =
                    historyIndex
            )
        )

        executor.execute {
            val api =
                YouTubeApi(
                    accessTokenRecovery =
                        GoogleAccessTokenRecovery(
                            appContext
                        )
                )

            val result =
                runCatching {
                    api.getVideoInfo(
                        accessToken =
                            token,
                        videoId =
                            videoId
                    )
                }

            result.onSuccess {
                videoInfo ->
                if (videoInfo != null) {
                    track.selectedVideoId =
                        videoId
                    track.selectedTitle =
                        videoInfo.title
                    track.selectedChannel =
                        videoInfo.channelTitle
                } else {
                    applyManualFallback(
                        track = track,
                        videoId = videoId
                    )
                }

                track.status =
                    TrackStatus.MATCHED
                track.manuallySelected =
                    true
                track.error =
                    null

                store.save(
                    playlist =
                        snapshot.playlist,
                    sourceLabel =
                        snapshot.sourceLabel,
                    destinationPlaylistId =
                        snapshot.destinationPlaylistId
                )

                publishTerminal(
                    kind =
                        Kind.MANUAL_URL,
                    message =
                        if (videoInfo != null) {
                            "Ручну заміну збережено: ${videoInfo.title}"
                        } else {
                            "Посилання збережено; YouTube не повернув назву."
                        },
                    focusHistoryIndex =
                        historyIndex
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

                    publishTerminal(
                        kind =
                            Kind.MANUAL_URL,
                        message =
                            "Авторизацію Google / YTM потрібно відновити.",
                        focusHistoryIndex =
                            historyIndex,
                        authorizationInvalidated =
                            true
                    )
                } else {
                    applyManualFallback(
                        track = track,
                        videoId = videoId
                    )

                    store.save(
                        playlist =
                            snapshot.playlist,
                        sourceLabel =
                            snapshot.sourceLabel,
                        destinationPlaylistId =
                            snapshot.destinationPlaylistId
                    )

                    publishTerminal(
                        kind =
                            Kind.MANUAL_URL,
                        message =
                            "Посилання збережено без метаданих: " +
                                (
                                    error.message
                                        ?: "невідома помилка"
                                ),
                        focusHistoryIndex =
                            historyIndex
                    )
                }
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
                serial &&
            state.terminalMessage !=
                null
        ) {
            state =
                state.copy(
                    terminalMessage =
                        null,
                    focusHistoryIndex =
                        null
                )
        }
    }

    private fun searchCoordinator(
        context: Context
    ): SearchCoordinator =
        SearchCoordinator(
            api =
                YouTubeApi(
                    accessTokenRecovery =
                        GoogleAccessTokenRecovery(
                            context
                        )
                ),
            searchCache =
                SearchCache(context),
            quotaTracker =
                QuotaTracker(context)
        )

    private fun applyManualFallback(
        track:
            com.saney.ytmimporter.model.Track,
        videoId: String
    ) {
        track.selectedVideoId =
            videoId
        track.selectedTitle =
            "YouTube video $videoId"
        track.selectedChannel =
            "метадані не завантажено"
        track.status =
            TrackStatus.MATCHED
        track.manuallySelected =
            true
        track.error =
            null
    }

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
    private fun publishTerminal(
        kind: Kind,
        message: String,
        focusHistoryIndex:
            Int? = null,
        authorizationInvalidated:
            Boolean = false
    ): Boolean {
        serialCounter += 1

        publish(
            State(
                kind = kind,
                running = false,
                message = message,
                terminalSerial =
                    serialCounter,
                terminalMessage =
                    message,
                focusHistoryIndex =
                    focusHistoryIndex,
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
