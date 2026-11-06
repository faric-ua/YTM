package com.saney.ytmimporter.urlsnapshot

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.auth.SilentGoogleSessionRestorer
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
            Boolean = false,
        val fromCache:
            Boolean = false,
        val cachedAt:
            Long? = null,
        /** Batch 17: the one explicit «Спробувати прочитати Mix» was made. */
        val mixAttempted:
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
        rawUrl: String,
        forceRemote: Boolean = false,
        /** Batch 17: the owner pressed «Спробувати прочитати Mix». */
        mixAttempt: Boolean = false
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

        val appContext =
            context.applicationContext

        if (
            !forceRemote &&
            source.kind ==
                UrlSnapshotSourceKind
                    .CONCRETE_PLAYLIST
        ) {
            val cached =
                UrlSnapshotCache(
                    appContext
                )
                    .get(
                        source
                    )

            if (cached != null) {
                publish(
                    State(
                        phase =
                            Phase.RESOLVED,
                        inputUrl =
                            input,
                        source =
                            source,
                        resolved =
                            cached.resolved,
                        message =
                            resolvedMessage(
                                result =
                                    cached.resolved,
                                fromCache =
                                    true
                            ),
                        fromCache =
                            true,
                        cachedAt =
                            cached.cachedAt
                    )
                )

                return true
            }
        }

        val token =
            AuthSessionStore
                .current()
                .accessToken
                ?.takeIf {
                    it.isNotBlank()
                }

        val readsMix =
            mixAttempt &&
                source.kind ==
                UrlSnapshotSourceKind
                    .DYNAMIC_MIX

        val needsToken =
            source.kind ==
                UrlSnapshotSourceKind
                    .CONCRETE_PLAYLIST || readsMix

        // Batch 17 (faric 2026-11-06 00:13: «Спочатку підключіть Google»
        // while Home showed «Підключено: faric»): the access token lives only
        // in memory and is gone after Android closed the app in the background.
        // Restore it silently, like the Library does (BUG-052), instead of
        // sending the owner to Home.
        if (
            needsToken &&
            token == null &&
            !PersistentAuthStateStore(appContext)
                .hadSuccessfulAuthorization()
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
                    if (readsMix) {
                        "Пробую прочитати Mix: до ${UrlSnapshotMixPolicy.PAGE_LIMIT} " +
                            "запитів до YouTube…"
                    } else if (
                        source.kind ==
                        UrlSnapshotSourceKind
                            .DYNAMIC_MIX
                    ) {
                        "Перевіряю, чи можна надійно прочитати цей Mix…"
                    } else if (forceRemote) {
                        "Оновлюю snapshot з YouTube Data API…"
                    } else {
                        "Читаю плейлист через YouTube Data API…"
                    }
            )
        )

        executor.execute {
            val quotaTracker =
                QuotaTracker(
                    appContext
                )

            val api =
                YouTubeApi(
                    accessTokenRecovery =
                        GoogleAccessTokenRecovery(
                            appContext
                        )
                )

            val liveToken =
                if (needsToken && token == null) {
                    val restored =
                        runCatching {
                            SilentGoogleSessionRestorer(appContext)
                                .restoreBlocking(api) {
                                    quotaTracker.recordGeneralUnits(
                                        QuotaTracker.SIMPLE_LIST_COST
                                    )
                                }
                        }.getOrDefault(
                            SilentGoogleSessionRestorer.Result.FAILED
                        )

                    val fresh =
                        AuthSessionStore
                            .current()
                            .accessToken
                            ?.takeIf { it.isNotBlank() }

                    if (fresh == null) {
                        publish(
                            State(
                                phase =
                                    Phase.ERROR,
                                inputUrl =
                                    input,
                                source =
                                    source,
                                message =
                                    restoreFailureMessage(restored)
                            )
                        )
                        return@execute
                    }
                    fresh
                } else {
                    token
                }

            val resolver =
                UrlSnapshotResolver
                    .production(
                        api =
                            api,
                        quotaTracker =
                            quotaTracker
                    )

            val result =
                runCatching {
                    if (readsMix) {
                        resolver.resolveMixAttempt(
                            accessToken =
                                liveToken.orEmpty(),
                            source =
                                source
                        )
                    } else {
                        resolver.resolve(
                            accessToken =
                                liveToken.orEmpty(),
                            source =
                                source
                        )
                    }
                }

            result.onSuccess {
                    resolution ->

                when (resolution) {
                    is UrlSnapshotResolutionResult
                        .Resolved -> {
                        // A Mix is different every time — never cached (batch 17).
                        val cachedAt =
                            if (readsMix) null else runCatching {
                                UrlSnapshotCache(
                                    appContext
                                )
                                    .put(
                                        resolution
                                    )
                            }.getOrNull()

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
                                        result =
                                            resolution,
                                        fromCache =
                                            false
                                    ),
                                fromCache =
                                    false,
                                cachedAt =
                                    cachedAt
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
                                    ),
                                mixAttempted =
                                    readsMix
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
    fun loadMissingPlaylistTitle(
        context: Context
    ): Boolean {
        if (state.running) return false
        val current = state
        if (!current.fromCache) return false
        val snapshotCachedAt =
            current.cachedAt
                ?: return false
        val resolved = current.resolved ?: return false
        if (!resolved.playlistTitle.isNullOrBlank()) return false
        val source = current.source ?: resolved.source
        if (source.kind != UrlSnapshotSourceKind.CONCRETE_PLAYLIST) return false
        val token = AuthSessionStore.current().accessToken?.takeIf { it.isNotBlank() }
        if (token == null) {
            publish(current.copy(phase = Phase.ERROR, message = "Спочатку підключіть Google / YTM на головному екрані."))
            return false
        }
        val appContext = context.applicationContext
        publish(current.copy(phase = Phase.RESOLVING, message = "Отримую назву плейлиста через YouTube Data API…"))
        executor.execute {
            val quotaTracker = QuotaTracker(appContext)
            val api = YouTubeApi(GoogleAccessTokenRecovery(appContext))
            val result = runCatching {
                api.getPlaylistSnapshotTitle(
                    accessToken = token,
                    playlistId = source.playlistId,
                    onListRequest = { quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST) }
                )
            }
            result.onSuccess { title ->
                val updated = resolved.copy(playlistTitle = title)
                UrlSnapshotCache(
                    appContext
                ).putPreservingCachedAt(
                    resolved =
                        updated,
                    cachedAt =
                        snapshotCachedAt
                )
                UrlSnapshotTitleBackfill(appContext).apply(source.playlistId, title)
                publish(State(
                    phase = Phase.RESOLVED,
                    inputUrl = current.inputUrl,
                    source = source,
                    resolved = updated,
                    message = resolvedMessage(updated, true, requestCountNow = 1),
                    fromCache = true,
                    cachedAt = snapshotCachedAt
                ))
            }.onFailure { error ->
                val authFailure = isAuthorizationFailure(error)
                if (authFailure) invalidateAuthorization(appContext)
                if (isQuotaFailure(error)) quotaTracker.recordQuotaError(error.message ?: "YouTube quota error")
                publish(State(
                    phase = Phase.ERROR,
                    inputUrl = current.inputUrl,
                    source = source,
                    message = if (authFailure) "Авторизацію Google / YTM потрібно відновити." else ErrorMessages.userMessage(error, "Не вдалося отримати назву плейлиста"),
                    authorizationInvalidated = authFailure
                ))
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
            UrlSnapshotResolutionResult.Resolved,
        fromCache: Boolean,
        requestCountNow: Int? = null
    ): String {
        val total =
            result.items.size

        val unavailable =
            result.unavailableCount

        val requestsNow =
            requestCountNow ?: if (fromCache) 0 else result.requestCount

        return buildString {
            if (fromCache) {
                append(
                    "Кешований snapshot: $total елементів"
                )
            } else {
                append(
                    "Отримано $total елементів"
                )
            }

            if (unavailable > 0) {
                append(
                    " • недоступних: $unavailable"
                )
            }

            if (fromCache) {
                append(
                    " • API-запитів зараз: $requestsNow"
                )
            } else {
                append(
                    " • API-запитів: $requestsNow"
                )
            }
        }
    }

    /** Batch 17: why the silent Google restore did not give a token. */
    private fun restoreFailureMessage(
        result: SilentGoogleSessionRestorer.Result
    ): String =
        when (result) {
            SilentGoogleSessionRestorer.Result.NEEDS_USER ->
                "Google просить підтвердити вхід. Відкрийте Головна → " +
                    "«2. Google / YTM» і підтвердьте, потім спробуйте ще раз."
            SilentGoogleSessionRestorer.Result.NEVER_AUTHORIZED ->
                "Спочатку підключіть Google / YTM на головному екрані."
            SilentGoogleSessionRestorer.Result.FAILED,
            SilentGoogleSessionRestorer.Result.RESTORED ->
                "Не вдалося відновити підключення Google (немає інтернету " +
                    "або Google не відповів). Спробуйте ще раз трохи пізніше."
        }

    private fun unsupportedMessage(
        result:
            UrlSnapshotResolutionResult
                .Unsupported
    ): String =
        when (result.reason) {
            UrlSnapshotUnsupportedReason
                .DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER ->
                // Batch 19 (faric 06.11 01:07): no «try to read Mix» button —
                // the official API does not give mixes (checked 06.11 00:28).
                "Це динамічний YouTube/YTM Mix: YouTube складає його сам " +
                    "і щоразу інакше, тому застосункам його не віддає. " +
                    "Що можна зробити: відкрийте Mix у YouTube Music, збережіть " +
                    "його як звичайний плейліст («⋮» → «Додати в плейліст») " +
                    "і вставте сюди посилання вже на цей плейліст."

            UrlSnapshotUnsupportedReason
                .MIX_NOT_GIVEN_BY_YOUTUBE ->
                "YouTube не віддав пісень цього Mix через офіційний доступ. " +
                    "Що можна зробити: відкрийте Mix у YouTube Music, збережіть " +
                    "його як звичайний плейліст («⋮» → «Додати в плейліст») " +
                    "і вставте сюди посилання вже на цей плейліст."
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
