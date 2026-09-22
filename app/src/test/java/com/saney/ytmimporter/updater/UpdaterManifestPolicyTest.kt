package com.saney.ytmimporter.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class UpdaterManifestPolicyTest {
    @Test
    fun parsesValidManifest() {
        val manifest =
            UpdaterManifestPolicy.parse(
                manifestJson()
            )

        assertEquals(1, manifest.schema)
        assertEquals("1.4.48", manifest.versionName)
        assertEquals(91, manifest.versionCode)
        assertEquals("v1.4.48", manifest.tag)
        assertEquals(
            "YTM-Importer-v1.4.48-release.apk",
            manifest.apkAsset
        )
        assertEquals(26, manifest.minSdk)
    }

    @Test
    fun sameVersionIsUpToDate() {
        val decision =
            UpdaterManifestPolicy.decide(
                manifest =
                    UpdaterManifestPolicy.parse(
                        manifestJson()
                    ),
                localVersionCode = 91,
                deviceSdk = 36
            )

        assertTrue(
            decision is UpdateDecision.UpToDate
        )
    }

    @Test
    fun newerVersionIsAvailable() {
        val decision =
            UpdaterManifestPolicy.decide(
                manifest =
                    UpdaterManifestPolicy.parse(
                        manifestJson(
                            versionName = "1.4.49",
                            versionCode = 92
                        )
                    ),
                localVersionCode = 91,
                deviceSdk = 36
            )

        assertTrue(
            decision is UpdateDecision.UpdateAvailable
        )
    }

    @Test
    fun olderStableManifestIsRejected() {
        val decision =
            UpdaterManifestPolicy.decide(
                manifest =
                    UpdaterManifestPolicy.parse(
                        manifestJson(
                            versionName = "1.4.47",
                            versionCode = 90
                        )
                    ),
                localVersionCode = 91,
                deviceSdk = 36
            )

        assertTrue(
            decision is UpdateDecision.Rejected
        )
    }

    @Test
    fun unsupportedSchemaIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            UpdaterManifestPolicy.parse(
                manifestJson(schema = 2)
            )
        }
    }

    @Test
    fun malformedManifestIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            UpdaterManifestPolicy.parse("{not-json")
        }
    }

    @Test
    fun unexpectedApkAssetIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            UpdaterManifestPolicy.parse(
                manifestJson(
                    apkAsset = "unexpected.apk"
                )
            )
        }
    }

    @Test
    fun invalidShaIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            UpdaterManifestPolicy.parse(
                manifestJson(sha256 = "abc")
            )
        }
    }

    @Test
    fun unsupportedDeviceSdkIsRejected() {
        val decision =
            UpdaterManifestPolicy.decide(
                manifest =
                    UpdaterManifestPolicy.parse(
                        manifestJson(
                            versionName = "1.4.49",
                            versionCode = 92,
                            minSdk = 37
                        )
                    ),
                localVersionCode = 91,
                deviceSdk = 36
            )

        assertTrue(
            decision is UpdateDecision.Rejected
        )
    }

    private fun manifestJson(
        schema: Int = 1,
        versionName: String = "1.4.48",
        versionCode: Int = 91,
        apkAsset: String =
            "YTM-Importer-v${versionName}-release.apk",
        sha256: String = "a".repeat(64),
        minSdk: Int = 26
    ): String =
        """
        {
          "schema": $schema,
          "versionName": "$versionName",
          "versionCode": $versionCode,
          "tag": "v$versionName",
          "apkAsset": "$apkAsset",
          "sha256": "$sha256",
          "minSdk": $minSdk
        }
        """.trimIndent()
}
