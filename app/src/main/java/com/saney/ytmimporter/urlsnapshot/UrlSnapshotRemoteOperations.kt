package com.saney.ytmimporter.urlsnapshot

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.util.ErrorMessages
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

/**
 * Process-local owner for explicit URL snapshot resolution.
 *
 * Activity recreation only reattaches to this state. It never interprets
 * recreation as a new resolve command.
 */
object UrlSnapshotRemoteOperations {
    enum class Phase {
        IDLE,
        RESOLVING,
        RESOLVED,
        UNSUPPORTED,
        ERROR
    }

    data class State(
        val phase: Phase = Phase.IDLE,
        val inputUrl: String = "",
        val message: String = "",
        val source: UrlSnapshotSource? = null,
        val resolved:
            UrlSnapshotResolutionResult.Resolved? =
            null,
        val authorizationInvalidated:
            Boolean = false
    ) {
        val running: Boolean
            get() =
                phase == Phase.RESOLVING
    }

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    private val executor =
        Executors
            .newSingleThreadExecutor()

    private val listeners =
        CopyOnWriteArraySet<
            (State) -> Unit
        >()

    @Volatile
    private var state =
        State()

    fun current(): State =
        state

    fun addListener(
        listener: (State) -> Unit
    ) {
        listeners += listener

        val snapshot =
            state

        mainHandler.post {
            if (
                listener in
                    listeners
            ) {
                listener(
                    snapshot
                )
            }
        }
    }

    fun removeListener(
        listener: (State) -> Unit
    ) {
        listeners -=
            listener
    }

    @Synchronized
    fun startResolve(
        context: Context,
        rawUrl: String
    ): Boolean {
        if (state.running) {
            return false
        }

        val input =
            rawUrl.trim()

        val parsed =
            UrlSnapshotSourceParser
                .parse(input)

        if (
            parsed is
                UrlSnapshotParseResult
                    .Unsupported
        ) {
            publish(
                State(
                    phase =
                        Phase.ERROR,
                    inputUrl =
                        input,
                    message =
                        parseErrorMessage(
                            parsed
                        )
                )
            )

            return false
        }

        val source =
            (
                parsed as
                    UrlSnapshotParseResult
                        .Supported
                )
                .source

        val token =
            AuthSessionStore
                .current()
                .accessToken
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            source.kind ==
                UrlSnapshotSourceKind
                    .CONCRETE_PLAYLIST &&
            token == null
        ) {
            publish(
                State(
                    phase =
                        Phase.ERROR,
                    inputUrl =
                        input,
                    source =
                        source,
                    message =
                        "Спочатку підключіть Google / YTM на головному екрані."
                )
            )

            return false
        }

        publish(
            State(
                phase =
                    Phase.RESOLVING,
                inputUrl =
                    input,
                source =
                    source,
                message =
                    if (
                        source.kind ==
                        UrlSnapshotSourceKind
                            .DYNAMIC_MIX
                    ) {
                        "Перевіряю, чи можна надійно прочитати цей Mix…"
                    } else {
                        "Читаю плейлист через YouTube Data API…"
                    }
            )
        )

        val appContext =
            context.applicationContext

        executor.execute {
            val quotaTracker =
                QuotaTracker(
                    appContext
                )

            val resolver =
                UrlSnapshotResolver
                    .production(
                        api =
                            YouTubeApi(
                                accessTokenRecovery =
                                    GoogleAccessTokenRecovery(
                                        appContext
                                    )
                            ),
                        quotaTracker =
                            quotaTracker
                    )

            val result =
                runCatching {
                    resolver.resolve(
                        accessToken =
                            token.orEmpty(),
                        source =
                            source
                    )
                }

            result.onSuccess {
                    resolution ->

                when (resolution) {
                    is UrlSnapshotResolutionResult
                        .Resolved -> {
                        publish(
                            State(
                                phase =
                                    Phase.RESOLVED,
                                inputUrl =
                                    input,
                                source =
                                    source,
                                resolved =
                                    resolution,
                                message =
                                    resolvedMessage(
                                        resolution
                                    )
                            )
                        )
                    }

                    is UrlSnapshotResolutionResult
                        .Unsupported -> {
                        publish(
                            State(
                                phase =
                                    Phase.UNSUPPORTED,
                                inputUrl =
                                    input,
                                source =
                                    source,
                                message =
                                    unsupportedMessage(
                                        resolution
                                    )
                            )
                        )
                    }
                }
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

                val quotaFailure =
                    isQuotaFailure(
                        error
                    )

                if (quotaFailure) {
                    quotaTracker
                        .recordQuotaError(
                            error.message
                                ?: "YouTube quota error"
                        )
                }

                publish(
                    State(
                        phase =
                            Phase.ERROR,
                        inputUrl =
                            input,
                        source =
                            source,
                        message =
                            if (authFailure) {
                                "Авторизацію Google / YTM потрібно відновити."
                            } else {
                                ErrorMessages
                                    .userMessage(
                                        error,
                                        "Не вдалося прочитати URL snapshot"
                                    )
                            },
                        authorizationInvalidated =
                            authFailure
                    )
                )
            }
        }

        return true
    }

    @Synchronized
    fun clearTerminal() {
        if (state.running) {
            return
        }

        state =
            State(
                inputUrl =
                    state.inputUrl
            )

        publish(
            state
        )
    }

    private fun parseErrorMessage(
        result:
            UrlSnapshotParseResult
                .Unsupported
    ): String =
        when (result.error) {
            UrlSnapshotParseError
                .BLANK_INPUT ->
                "Вставте URL плейлиста YouTube або YouTube Music."

            UrlSnapshotParseError
                .MALFORMED_URL ->
                "Не вдалося розібрати URL."

            UrlSnapshotParseError
                .UNSUPPORTED_SCHEME ->
                "Підтримуються лише http/https URL YouTube."

            UrlSnapshotParseError
                .UNSUPPORTED_HOST ->
                "Це не підтримуваний домен YouTube / YouTube Music."

            UrlSnapshotParseError
                .UNSUPPORTED_PATH ->
                "Ця форма YouTube URL не підтримується для snapshot-імпорту."

            UrlSnapshotParseError
                .MISSING_PLAYLIST_ID ->
                "У URL немає параметра list з ідентифікатором плейлиста."

            UrlSnapshotParseError
                .AMBIGUOUS_PLAYLIST_ID ->
                "URL містить кілька різних list-параметрів."

            UrlSnapshotParseError
                .INVALID_PLAYLIST_ID ->
                "Ідентифікатор плейлиста в URL має непідтримуваний формат."
        }

    private fun resolvedMessage(
        result:
            UrlSnapshotResolutionResult
                .Resolved
    ): String {
        val total =
            result.items.size

        val unavailable =
            result.unavailableCount

        return buildString {
            append(
                "Отримано $total елементів"
            )

            if (unavailable > 0) {
                append(
                    " • недоступних: $unavailable"
                )
            }

            append(
                " • API-запитів: ${result.requestCount}"
            )
        }
    }

    private fun unsupportedMessage(
        result:
            UrlSnapshotResolutionResult
                .Unsupported
    ): String =
        when (result.reason) {
            UrlSnapshotUnsupportedReason
                .DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER ->
                "Це динамічний YouTube/YTM Mix. " +
                    "Поточна офіційна інтеграція не може надійно " +
                    "перелічити його як повний snapshot. " +
                    "Застосунок не використовує прихований scraper, " +
                    "не вгадує треки й не підміняє Mix пошуком."
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

    private fun isQuotaFailure(
        error: Throwable
    ): Boolean {
        var current:
            Throwable? =
            error

        while (current != null) {
            if (
                current is
                    YouTubeApiException &&
                current.isQuotaError
            ) {
                return true
            }

            current =
                current.cause
        }

        return false
    }

    private fun publish(
        next: State
    ) {
        state =
            next

        mainHandler.post {
            listeners.forEach {
                listener ->
                listener(
                    next
                )
            }
        }
    }
}
