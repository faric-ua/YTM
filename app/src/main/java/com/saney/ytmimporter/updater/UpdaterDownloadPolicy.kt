package com.saney.ytmimporter.updater

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale

object UpdaterDownloadPolicy {
    const val MAX_APK_BYTES =
        250L * 1024L * 1024L

    fun assetUrl(
        manifest: UpdateManifest
    ): String =
        "https://github.com/faric-ua/YTM/releases/download/" +
            "${manifest.tag}/${manifest.apkAsset}"

    fun sha256(
        file: File
    ): String {
        val digest =
            MessageDigest.getInstance("SHA-256")

        FileInputStream(file).use {
            input ->
            val buffer =
                ByteArray(32 * 1024)

            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) {
                    digest.update(buffer, 0, count)
                }
            }
        }

        return digest.digest().joinToString("") {
            byte ->
            "%02x".format(
                Locale.ROOT,
                byte.toInt() and 0xff
            )
        }
    }

    fun verifySha256(
        file: File,
        expectedSha256: String
    ): Boolean {
        val normalized =
            expectedSha256.lowercase(Locale.ROOT)

        require(
            SHA256_REGEX.matches(normalized)
        ) {
            "SHA-256 має містити 64 шістнадцяткові символи"
        }

        return sha256(file) == normalized
    }

    private val SHA256_REGEX =
        Regex("^[0-9a-f]{64}$")
}
