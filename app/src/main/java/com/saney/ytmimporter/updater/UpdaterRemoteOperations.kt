package com.saney.ytmimporter.updater

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

/**
 * Process-local owner for user-initiated updater remote work.
 *
 * ServiceActivity renders this state but does not own Check/Download/Verify.
 * Activity recreation therefore reconnects to the same logical operation
 * instead of interpreting recreation as another user command.
 */
object UpdaterRemoteOperations {
    enum class Phase {
        IDLE,
        CHECKING,
        UP_TO_DATE,
        UPDATE_AVAILABLE,
        DOWNLOADING,
        VERIFYING,
        READY_TO_INSTALL,
        ERROR
    }

    data class State(
        val phase: Phase = Phase.IDLE,
        val message: String = "",
        val remoteVersionName: String? = null,
        val remoteVersionCode: Int? = null,
        val manifest: UpdateManifest? = null,
        val bytesDownloaded: Long = 0L,
        val totalBytes: Long = -1L,
        val downloadedFilePath: String? = null,
        val errorTitle: String = "Не вдалося перевірити",
        val downloadRetryAvailable: Boolean = false
    ) {
        val running: Boolean
            get() =
                phase == Phase.CHECKING ||
                    phase == Phase.DOWNLOADING ||
                    phase == Phase.VERIFYING
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
                                manifest = manifest,
                                errorTitle =
                                    "Оновлення недоступне"
                            )
                        )
                }
            }.onFailure {
                error ->
                publish(
                    State(
                        phase = Phase.ERROR,
                        message = checkUserMessage(error),
                        errorTitle =
                            "Не вдалося перевірити"
                    )
                )
            }
        }

        return true
    }

    @Synchronized
    fun startDownload(
        context: Context
    ): Boolean {
        val snapshot = state

        if (snapshot.running) {
            return false
        }

        val allowed =
            snapshot.phase == Phase.UPDATE_AVAILABLE ||
                (
                    snapshot.phase == Phase.ERROR &&
                        snapshot.downloadRetryAvailable
                    )

        if (!allowed) {
            return false
        }

        val manifest =
            snapshot.manifest
                ?: return false

        publish(
            State(
                phase = Phase.DOWNLOADING,
                message = "Починаю завантаження APK…",
                remoteVersionName = manifest.versionName,
                remoteVersionCode = manifest.versionCode,
                manifest = manifest
            )
        )

        val appContext =
            context.applicationContext

        executor.execute {
            val result =
                runCatching {
                    downloadAndVerify(
                        appContext,
                        manifest
                    )
                }

            result.onSuccess {
                file ->
                publish(
                    State(
                        phase = Phase.READY_TO_INSTALL,
                        message =
                            "SHA-256 збігається з офіційним маніфестом. " +
                                "APK перевірено й готово до наступного етапу встановлення.",
                        remoteVersionName = manifest.versionName,
                        remoteVersionCode = manifest.versionCode,
                        manifest = manifest,
                        bytesDownloaded = file.length(),
                        totalBytes = file.length(),
                        downloadedFilePath =
                            file.absolutePath
                    )
                )
            }.onFailure {
                error ->
                publish(
                    downloadErrorState(
                        manifest,
                        error
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

    private fun downloadAndVerify(
        context: Context,
        manifest: UpdateManifest
    ): File {
        val updatesDir =
            File(
                context.filesDir,
                UPDATES_DIR_NAME
            )

        if (!updatesDir.exists() &&
            !updatesDir.mkdirs()
        ) {
            throw IOException(
                "Не вдалося створити каталог оновлень"
            )
        }

        val partFile =
            File(
                updatesDir,
                manifest.apkAsset + PART_SUFFIX
            )

        val finalFile =
            File(
                updatesDir,
                manifest.apkAsset
            )

        if (partFile.exists() &&
            !partFile.delete()
        ) {
            throw IOException(
                "Не вдалося очистити попереднє незавершене завантаження"
            )
        }

        try {
            downloadToPart(
                manifest,
                partFile
            )

            publish(
                State(
                    phase = Phase.VERIFYING,
                    message =
                        "Обчислюю та перевіряю SHA-256 завантаженого APK…",
                    remoteVersionName = manifest.versionName,
                    remoteVersionCode = manifest.versionCode,
                    manifest = manifest,
                    bytesDownloaded = partFile.length(),
                    totalBytes = partFile.length()
                )
            )

            if (!UpdaterDownloadPolicy.verifySha256(
                    partFile,
                    manifest.sha256
                )
            ) {
                throw ShaMismatchException()
            }

            Files.move(
                partFile.toPath(),
                finalFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )

            return finalFile
        } catch (error: Throwable) {
            partFile.delete()
            throw error
        }
    }

    private fun downloadToPart(
        manifest: UpdateManifest,
        partFile: File
    ) {
        val connection =
            (URL(
                UpdaterDownloadPolicy.assetUrl(
                    manifest
                )
            ).openConnection()
                as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty(
                    "Accept",
                    "application/vnd.android.package-archive"
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
                    "APK GitHub Release повернув HTTP $responseCode"
                )
            }

            val totalBytes =
                connection.contentLengthLong

            if (
                totalBytes >
                UpdaterDownloadPolicy.MAX_APK_BYTES
            ) {
                throw IOException(
                    "APK перевищує допустимий розмір"
                )
            }

            BufferedInputStream(
                connection.inputStream
            ).use {
                input ->
                FileOutputStream(partFile).use {
                    output ->
                    val buffer =
                        ByteArray(32 * 1024)
                    var downloaded = 0L
                    var lastPublished = 0L

                    while (true) {
                        val count =
                            input.read(buffer)

                        if (count < 0) break
                        if (count == 0) continue

                        output.write(
                            buffer,
                            0,
                            count
                        )

                        downloaded += count

                        if (
                            downloaded >
                            UpdaterDownloadPolicy.MAX_APK_BYTES
                        ) {
                            throw IOException(
                                "APK перевищує допустимий розмір"
                            )
                        }

                        val shouldPublish =
                            downloaded - lastPublished >=
                                PROGRESS_STEP_BYTES ||
                                (
                                    totalBytes > 0L &&
                                        downloaded >= totalBytes
                                    )

                        if (shouldPublish) {
                            lastPublished = downloaded
                            publishDownloadProgress(
                                manifest,
                                downloaded,
                                totalBytes
                            )
                        }
                    }

                    output.flush()

                    if (downloaded <= 0L) {
                        throw IOException(
                            "Отримано порожній APK"
                        )
                    }

                    publishDownloadProgress(
                        manifest,
                        downloaded,
                        totalBytes
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun publishDownloadProgress(
        manifest: UpdateManifest,
        downloaded: Long,
        totalBytes: Long
    ) {
        publish(
            State(
                phase = Phase.DOWNLOADING,
                message =
                    progressMessage(
                        downloaded,
                        totalBytes
                    ),
                remoteVersionName = manifest.versionName,
                remoteVersionCode = manifest.versionCode,
                manifest = manifest,
                bytesDownloaded = downloaded,
                totalBytes = totalBytes
            )
        )
    }

    private fun progressMessage(
        downloaded: Long,
        totalBytes: Long
    ): String =
        if (totalBytes > 0L) {
            val percent =
                (
                    downloaded * 100L /
                        totalBytes
                    ).coerceIn(0L, 100L)
            "Завантажено: $percent%."
        } else {
            val kib = downloaded / 1024L
            "Завантажено: $kib КБ."
        }

    private fun checkUserMessage(
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

    private fun downloadErrorState(
        manifest: UpdateManifest,
        error: Throwable
    ): State =
        when (error) {
            is ShaMismatchException ->
                State(
                    phase = Phase.ERROR,
                    message =
                        "Завантажений APK не відповідає SHA-256 з офіційного маніфесту. " +
                            "Файл відхилено й не буде використано.",
                    remoteVersionName = manifest.versionName,
                    remoteVersionCode = manifest.versionCode,
                    manifest = manifest,
                    errorTitle =
                        "Перевірка SHA-256 не пройдена",
                    downloadRetryAvailable = true
                )

            is IOException ->
                State(
                    phase = Phase.ERROR,
                    message =
                        "Не вдалося завантажити APK. " +
                            "Перевірте підключення до інтернету та спробуйте ще раз.",
                    remoteVersionName = manifest.versionName,
                    remoteVersionCode = manifest.versionCode,
                    manifest = manifest,
                    errorTitle =
                        "Не вдалося завантажити",
                    downloadRetryAvailable = true
                )

            else ->
                State(
                    phase = Phase.ERROR,
                    message =
                        "Не вдалося завершити завантаження через невідому помилку.",
                    remoteVersionName = manifest.versionName,
                    remoteVersionCode = manifest.versionCode,
                    manifest = manifest,
                    errorTitle =
                        "Не вдалося завантажити",
                    downloadRetryAvailable = true
                )
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

    private class ShaMismatchException :
        IOException("SHA-256 mismatch")

    const val MANIFEST_URL =
        "https://github.com/faric-ua/YTM/releases/latest/download/" +
            "YTM-Importer-update.json"

    private const val MAX_MANIFEST_CHARS =
        128 * 1024

    private const val UPDATES_DIR_NAME =
        "updates"

    private const val PART_SUFFIX =
        ".part"

    private const val PROGRESS_STEP_BYTES =
        128L * 1024L
}
