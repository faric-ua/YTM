package com.saney.ytmimporter.updater

import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


data class UpdateManifest(
    val schema: Int,
    val versionName: String,
    val versionCode: Int,
    val tag: String,
    val apkAsset: String,
    val sha256: String,
    val minSdk: Int
)

sealed class UpdateDecision {
    object UpToDate : UpdateDecision()

    object InstalledBuildNewer : UpdateDecision()

    data class UpdateAvailable(
        val manifest: UpdateManifest
    ) : UpdateDecision()

    data class Rejected(
        val reason: String
    ) : UpdateDecision()
}

object UpdaterManifestPolicy {
    const val SUPPORTED_SCHEMA = 1

    fun parse(raw: String): UpdateManifest {
        val json =
            try {
                JSONObject(raw)
            } catch (error: JSONException) {
                throw IllegalArgumentException(
                    "JSON маніфесту має некоректний формат",
                    error
                )
            }

        val schema = requiredInt(json, "schema")
        require(schema == SUPPORTED_SCHEMA) {
            "Непідтримувана схема маніфесту: $schema"
        }

        val versionName = requiredString(json, "versionName")
        val versionCode = requiredInt(json, "versionCode")
        val tag = requiredString(json, "tag")
        val apkAsset = requiredString(json, "apkAsset")
        val sha256 =
            requiredString(json, "sha256")
                .lowercase(Locale.ROOT)
        val minSdk = requiredInt(json, "minSdk")

        require(versionCode > 0) {
            "versionCode має бути додатним"
        }
        require(minSdk > 0) {
            "minSdk має бути додатним"
        }
        require(tag == "v$versionName") {
            "tag не відповідає versionName"
        }

        val expectedApk =
            "YTM-Importer-v${versionName}-release.apk"
        require(apkAsset == expectedApk) {
            "Неочікувана назва APK: $apkAsset"
        }
        require(SHA256_REGEX.matches(sha256)) {
            "SHA-256 має містити 64 шістнадцяткові символи"
        }

        return UpdateManifest(
            schema = schema,
            versionName = versionName,
            versionCode = versionCode,
            tag = tag,
            apkAsset = apkAsset,
            sha256 = sha256,
            minSdk = minSdk
        )
    }

    fun decide(
        manifest: UpdateManifest,
        localVersionCode: Int,
        deviceSdk: Int
    ): UpdateDecision {
        require(localVersionCode > 0) {
            "localVersionCode має бути додатним"
        }
        require(deviceSdk > 0) {
            "deviceSdk має бути додатним"
        }

        if (manifest.versionCode < localVersionCode) {
            return UpdateDecision.InstalledBuildNewer
        }

        if (manifest.versionCode == localVersionCode) {
            return UpdateDecision.UpToDate
        }

        if (manifest.minSdk > deviceSdk) {
            return UpdateDecision.Rejected(
                "Оновлення потребує Android API ${manifest.minSdk}, " +
                    "API пристрою: $deviceSdk"
            )
        }

        return UpdateDecision.UpdateAvailable(
            manifest
        )
    }

    private fun requiredString(
        json: JSONObject,
        key: String
    ): String {
        val value = json.opt(key)
        require(value is String && value.isNotBlank()) {
            "Відсутнє або некоректне текстове поле: $key"
        }
        return value
    }

    private fun requiredInt(
        json: JSONObject,
        key: String
    ): Int {
        val value = json.opt(key)
        require(value is Number) {
            "Відсутнє або некоректне ціле поле: $key"
        }

        val asLong = value.toLong()
        require(value.toDouble() == asLong.toDouble()) {
            "Поле має бути цілим числом: $key"
        }
        require(
            asLong >= Int.MIN_VALUE.toLong() &&
                asLong <= Int.MAX_VALUE.toLong()
        ) {
            "Ціле поле поза допустимим діапазоном: $key"
        }
        return asLong.toInt()
    }

    private val SHA256_REGEX =
        Regex("^[0-9a-f]{64}$")
}
