package com.saney.ytmimporter.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UpdaterDownloadPolicyTest {
    @Test
    fun assetUrlUsesExactTagAndAsset() {
        val manifest = manifest()

        assertEquals(
            "https://github.com/faric-ua/YTM/releases/download/" +
                "v1.4.50/YTM-Importer-v1.4.50-release.apk",
            UpdaterDownloadPolicy.assetUrl(manifest)
        )
    }

    @Test
    fun sha256MatchesKnownPayload() {
        val file = tempFile("abc")

        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223" +
                "b00361a396177a9cb410ff61f20015ad",
            UpdaterDownloadPolicy.sha256(file)
        )
    }

    @Test
    fun verifySha256AcceptsMatchingFile() {
        val file = tempFile("abc")

        assertTrue(
            UpdaterDownloadPolicy.verifySha256(
                file,
                "ba7816bf8f01cfea414140de5dae2223" +
                    "b00361a396177a9cb410ff61f20015ad"
            )
        )
    }

    @Test
    fun verifySha256RejectsMismatch() {
        val file = tempFile("abc")

        assertFalse(
            UpdaterDownloadPolicy.verifySha256(
                file,
                "0".repeat(64)
            )
        )
    }

    @Test
    fun verifySha256RejectsInvalidExpectedHash() {
        val file = tempFile("abc")

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            UpdaterDownloadPolicy.verifySha256(
                file,
                "abc"
            )
        }
    }

    private fun tempFile(
        content: String
    ): File =
        File.createTempFile(
            "ytm-updater-",
            ".apk"
        ).apply {
            writeText(content)
            deleteOnExit()
        }

    private fun manifest(): UpdateManifest =
        UpdateManifest(
            schema = 1,
            versionName = "1.4.50",
            versionCode = 93,
            tag = "v1.4.50",
            apkAsset =
                "YTM-Importer-v1.4.50-release.apk",
            sha256 = "a".repeat(64),
            minSdk = 26
        )
}
