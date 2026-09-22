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
                    "Manifest JSON is malformed",
                    error
                )
            }

        val schema = requiredInt(json, "schema")
        require(schema == SUPPORTED_SCHEMA) {
            "Unsupported manifest schema: $schema"
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
            "versionCode must be positive"
        }
        require(minSdk > 0) {
            "minSdk must be positive"
        }
        require(tag == "v$versionName") {
            "tag does not match versionName"
        }

        val expectedApk =
            "YTM-Importer-v${versionName}-release.apk"
        require(apkAsset == expectedApk) {
            "Unexpected APK asset: $apkAsset"
        }
        require(SHA256_REGEX.matches(sha256)) {
            "sha256 must contain 64 hexadecimal characters"
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
            "localVersionCode must be positive"
        }
        require(deviceSdk > 0) {
            "deviceSdk must be positive"
        }

        if (manifest.versionCode < localVersionCode) {
            return UpdateDecision.Rejected(
                "Stable manifest is older than the installed build"
            )
        }

        if (manifest.versionCode == localVersionCode) {
            return UpdateDecision.UpToDate
        }

        if (manifest.minSdk > deviceSdk) {
            return UpdateDecision.Rejected(
                "Update requires Android API ${manifest.minSdk}, " +
                    "device API is $deviceSdk"
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
            "Missing or invalid string field: $key"
        }
        return value
    }

    private fun requiredInt(
        json: JSONObject,
        key: String
    ): Int {
        val value = json.opt(key)
        require(value is Number) {
            "Missing or invalid integer field: $key"
        }

        val asLong = value.toLong()
        require(value.toDouble() == asLong.toDouble()) {
            "Field must be an integer: $key"
        }
        require(
            asLong >= Int.MIN_VALUE.toLong() &&
                asLong <= Int.MAX_VALUE.toLong()
        ) {
            "Integer field out of range: $key"
        }
        return asLong.toInt()
    }

    private val SHA256_REGEX =
        Regex("^[0-9a-f]{64}$")
}
