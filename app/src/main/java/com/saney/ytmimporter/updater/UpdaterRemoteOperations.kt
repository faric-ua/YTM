package com.saney.ytmimporter.updater

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

/**
 * Process-local owner for the user-initiated updater check.
 *
 * ServiceActivity renders this state but does not own the HTTP operation.
 * Activity recreation therefore reconnects to the same logical check instead
 * of interpreting recreation as another Check command.
 */
object UpdaterRemoteOperations {
    enum class Phase {
        IDLE,
        CHECKING,
        UP_TO_DATE,
        UPDATE_AVAILABLE,
        ERROR
    }

    data class State(
        val phase: Phase = Phase.IDLE,
        val message: String = "",
        val remoteVersionName: String? = null,
        val remoteVersionCode: Int? = null,
        val manifest: UpdateManifest? = null
    ) {
        val running: Boolean
            get() = phase == Phase.CHECKING
    }

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val executor =
        Executors.newSingleThreadExecutor()

    private val listeners =
        CopyOnWriteArraySet<(State) -> Unit>()

    @Volatile
    private var state = State()

    fun current(): State = state

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
    fun startCheck(
        localVersionCode: Int,
        deviceSdk: Int
    ): Boolean {
        if (state.running) {
            return false
        }

        publish(
            State(
                phase = Phase.CHECKING,
                message = "Перевіряю офіційний GitHub Release…"
            )
        )

        executor.execute {
            val result =
                runCatching {
                    fetchManifest()
                }

            result.onSuccess {
                manifest ->
                when (
                    val decision =
                        UpdaterManifestPolicy.decide(
                            manifest = manifest,
                            localVersionCode = localVersionCode,
                            deviceSdk = deviceSdk
                        )
                ) {
                    UpdateDecision.UpToDate ->
                        publish(
                            State(
                                phase = Phase.UP_TO_DATE,
                                message =
                                    "Встановлена версія відповідає поточній стабільній версії.",
                                remoteVersionName =
                                    manifest.versionName,
                                remoteVersionCode =
                                    manifest.versionCode,
                                manifest = manifest
                            )
                        )

                    UpdateDecision.InstalledBuildNewer ->
                        publish(
                            State(
                                phase = Phase.UP_TO_DATE,
                                message =
                                    "Встановлена версія новіша за поточну стабільну версію.",
                                remoteVersionName =
                                    manifest.versionName,
                                remoteVersionCode =
                                    manifest.versionCode,
                                manifest = manifest
                            )
                        )

                    is UpdateDecision.UpdateAvailable ->
                        publish(
                            State(
                                phase = Phase.UPDATE_AVAILABLE,
                                message =
                                    "Доступна новіша стабільна версія.",
                                remoteVersionName =
                                    decision.manifest.versionName,
                                remoteVersionCode =
                                    decision.manifest.versionCode,
                                manifest =
                                    decision.manifest
                            )
                        )

                    is UpdateDecision.Rejected ->
                        publish(
                            State(
                                phase = Phase.ERROR,
                                message =
                                    decision.reason,
                                remoteVersionName =
                                    manifest.versionName,
                                remoteVersionCode =
                                    manifest.versionCode,
                                manifest = manifest
                            )
                        )
                }
            }.onFailure {
                error ->
                publish(
                    State(
                        phase = Phase.ERROR,
                        message = userMessage(error)
                    )
                )
            }
        }

        return true
    }

    private fun fetchManifest(): UpdateManifest {
        val connection =
            (URL(MANIFEST_URL).openConnection()
                as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 20_000
                instanceFollowRedirects = true
                setRequestProperty(
                    "Accept",
                    "application/json"
                )
                setRequestProperty(
                    "User-Agent",
                    "YTM-Importer-Updater"
                )
            }

        try {
            val responseCode =
                connection.responseCode

            if (responseCode !in 200..299) {
                throw IOException(
                    "Маніфест GitHub Release повернув HTTP $responseCode"
                )
            }

            val body =
                BufferedReader(
                    InputStreamReader(
                        connection.inputStream,
                        Charsets.UTF_8
                    )
                ).use {
                    reader ->
                    reader.readText()
                }

            if (body.length > MAX_MANIFEST_CHARS) {
                throw IOException(
                    "Маніфест оновлення має неочікувано великий розмір"
                )
            }

            return UpdaterManifestPolicy.parse(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun userMessage(
        error: Throwable
    ): String =
        when (error) {
            is IOException ->
                "Не вдалося отримати маніфест оновлення. " +
                    "Перевірте підключення до інтернету та спробуйте ще раз."

            is IllegalArgumentException ->
                "Маніфест оновлення відхилено: " +
                    (error.message ?: "некоректний формат")

            else ->
                "Не вдалося перевірити оновлення через невідому помилку."
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

    const val MANIFEST_URL =
        "https://github.com/faric-ua/YTM/releases/latest/download/" +
            "YTM-Importer-update.json"

    private const val MAX_MANIFEST_CHARS =
        128 * 1024
}
