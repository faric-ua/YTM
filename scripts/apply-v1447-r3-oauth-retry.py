#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path
import sys

ROOT = Path.cwd()


def fail(message: str) -> "NoReturn":
    raise SystemExit(f"FAIL: {message}")


def check_text_file(path: Path, old: str, new: str, label: str, apply: bool) -> None:
    if not path.is_file():
        fail(f"missing file for {label}: {path}")

    text = path.read_text(encoding="utf-8")
    old_count = text.count(old)
    new_count = text.count(new)

    if new_count == 1:
        expected_old_count = 1 if old in new else 0
        if old_count == expected_old_count:
            print(f"SKIP: already applied: {label}")
            return

    if old_count == 1 and new_count == 0:
        print(f"READY: {label}")
        if apply:
            path.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
            print(f"APPLIED: {label}")
        return

    fail(
        f"anchor mismatch for {label}: old={old_count}, new={new_count}, file={path}"
    )


def check_new_file(path: Path, content: str, label: str, apply: bool) -> None:
    if path.exists():
        existing = path.read_text(encoding="utf-8")
        if existing == content:
            print(f"SKIP: already applied: {label}")
            return
        fail(f"existing file differs for {label}: {path}")

    print(f"READY: {label}")
    if apply:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="\n")
        print(f"APPLIED: {label}")


GOOGLE_RECOVERY = r'''package com.saney.ytmimporter.auth

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.saney.ytmimporter.youtube.YouTubeAccessTokenRecovery
import java.util.concurrent.TimeUnit

/**
 * Silent HTTP-401 recovery for the Android Google authorization flow.
 *
 * The Android client deliberately does not persist a raw refresh token.
 * Google AuthorizationClient owns the device-side grant/token cache. When a
 * YouTube/Google request rejects an access token with HTTP 401, we clear that
 * rejected token from the Google cache, request a replacement token for the
 * already-granted scopes, and let YouTubeApi retry the same HTTP request once.
 *
 * If Google reports that user interaction is required (hasResolution), or if
 * refresh fails, this class returns null. Existing activity-level 401 handling
 * then clears the connected state and requires explicit user authorization.
 */
class GoogleAccessTokenRecovery(
    context: Context
) : YouTubeAccessTokenRecovery {
    private val appContext =
        context.applicationContext

    override fun currentAccessToken(
        fallbackToken: String
    ): String =
        AuthSessionStore
            .current()
            .accessToken
            ?.takeIf { it.isNotBlank() }
            ?: fallbackToken

    @Synchronized
    override fun refreshAfterUnauthorized(
        rejectedToken: String
    ): String? {
        val before =
            AuthSessionStore.current()

        before.accessToken
            ?.takeIf {
                it.isNotBlank() &&
                    it != rejectedToken
            }
            ?.let {
                return it
            }

        val client =
            Identity.getAuthorizationClient(
                appContext
            )

        val clearRequest =
            ClearTokenRequest
                .builder()
                .setToken(rejectedToken)
                .build()

        val cleared =
            runCatching {
                Tasks.await(
                    client.clearToken(
                        clearRequest
                    ),
                    TOKEN_OPERATION_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
                )
            }.isSuccess

        if (!cleared) {
            return null
        }

        val result =
            runCatching {
                Tasks.await(
                    client.authorize(
                        buildAuthorizationRequest()
                    ),
                    TOKEN_OPERATION_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
                )
            }.getOrNull()
                ?: return null

        if (result.hasResolution()) {
            return null
        }

        val refreshedToken =
            result.accessToken
                ?.trim()
                ?.takeIf {
                    it.isNotBlank() &&
                        it != rejectedToken
                }
                ?: return null

        val latest =
            AuthSessionStore.current()

        AuthSessionStore.update(
            accessToken = refreshedToken,
            googleAccountInfo =
                latest.googleAccountInfo,
            youtubeChannelInfo =
                latest.youtubeChannelInfo
        )

        PersistentAuthStateStore(appContext)
            .markSuccessfulAuthorization()

        return refreshedToken
    }

    private fun buildAuthorizationRequest():
        AuthorizationRequest =
        AuthorizationRequest
            .builder()
            .setRequestedScopes(
                listOf(
                    Scope(YOUTUBE_SCOPE),
                    Scope(USERINFO_EMAIL_SCOPE),
                    Scope(USERINFO_PROFILE_SCOPE)
                )
            )
            .build()

    companion object {
        private const val TOKEN_OPERATION_TIMEOUT_SECONDS =
            30L

        private const val YOUTUBE_SCOPE =
            "https://www.googleapis.com/auth/youtube.force-ssl"

        private const val USERINFO_EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email"

        private const val USERINFO_PROFILE_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile"
    }
}
'''

AUTH_STORE_OLD = r'''    @Synchronized
    fun clear() {
        snapshot = Snapshot()
    }
'''

AUTH_STORE_NEW = r'''    @Synchronized
    fun updateIdentity(
        googleAccountInfo: GoogleAccountInfo?,
        youtubeChannelInfo: YouTubeChannelInfo?
    ) {
        snapshot =
            snapshot.copy(
                googleAccountInfo =
                    googleAccountInfo,
                youtubeChannelInfo =
                    youtubeChannelInfo
            )
    }

    @Synchronized
    fun clear() {
        snapshot = Snapshot()
    }
'''

YOUTUBE_CLASS_OLD = r'''class YouTubeApi {
    data class ApiResponse(val code: Int, val body: String)
'''

YOUTUBE_CLASS_NEW = r'''interface YouTubeAccessTokenRecovery {
    fun currentAccessToken(
        fallbackToken: String
    ): String

    fun refreshAfterUnauthorized(
        rejectedToken: String
    ): String?
}

class YouTubeApi(
    private val accessTokenRecovery:
        YouTubeAccessTokenRecovery? = null
) {
    data class ApiResponse(val code: Int, val body: String)
'''

YOUTUBE_REQUEST_OLD = r'''    private fun request(
        method: String,
        url: String,
        accessToken: String,
        body: String? = null
    ): ApiResponse {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 20_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use {
                it.write(body.toByteArray(Charsets.UTF_8))
            }
        }

        val code = connection.responseCode
        val input =
            if (code in 200..299) connection.inputStream
            else connection.errorStream

        val text = input?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
        }.orEmpty()

        connection.disconnect()
        return ApiResponse(code, text)
    }
'''

YOUTUBE_REQUEST_NEW = r'''    private fun request(
        method: String,
        url: String,
        accessToken: String,
        body: String? = null
    ): ApiResponse {
        val initialToken =
            accessTokenRecovery
                ?.currentAccessToken(
                    accessToken
                )
                ?.takeIf { it.isNotBlank() }
                ?: accessToken

        val firstResponse =
            requestOnce(
                method = method,
                url = url,
                accessToken = initialToken,
                body = body
            )

        if (
            firstResponse.code != 401 ||
            accessTokenRecovery == null
        ) {
            return firstResponse
        }

        val refreshedToken =
            accessTokenRecovery
                .refreshAfterUnauthorized(
                    initialToken
                )
                ?.takeIf {
                    it.isNotBlank() &&
                        it != initialToken
                }
                ?: return firstResponse

        return requestOnce(
            method = method,
            url = url,
            accessToken = refreshedToken,
            body = body
        )
    }

    private fun requestOnce(
        method: String,
        url: String,
        accessToken: String,
        body: String? = null
    ): ApiResponse {
        val connection =
            URL(url)
                .openConnection()
                as HttpURLConnection

        connection.requestMethod = method
        connection.connectTimeout = 20_000
        connection.readTimeout = 30_000
        connection.setRequestProperty(
            "Authorization",
            "Bearer $accessToken"
        )
        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/json; charset=utf-8"
            )
            connection.outputStream.use {
                it.write(
                    body.toByteArray(
                        Charsets.UTF_8
                    )
                )
            }
        }

        val code =
            connection.responseCode

        val input =
            if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

        val text =
            input
                ?.use { stream ->
                    BufferedReader(
                        InputStreamReader(
                            stream,
                            Charsets.UTF_8
                        )
                    ).readText()
                }
                .orEmpty()

        connection.disconnect()

        return ApiResponse(
            code = code,
            body = text
        )
    }
'''

MAIN_IMPORT_OLD = r'''import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.PersistentAuthStateStore
'''

MAIN_IMPORT_NEW = r'''import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
'''

MAIN_API_OLD = r'''    private val executor = Executors.newSingleThreadExecutor()
    private val api = YouTubeApi()
'''

MAIN_API_NEW = r'''    private val executor = Executors.newSingleThreadExecutor()

    private val api by lazy {
        YouTubeApi(
            accessTokenRecovery =
                GoogleAccessTokenRecovery(
                    this
                )
        )
    }
'''

MAIN_IDENTITY_OLD = r'''                googleAccountInfo = googleResult.getOrNull()
                youtubeChannelInfo = channelResult.getOrNull()
                syncAuthSessionToMemory()
                updateAccountPanel()
'''

MAIN_IDENTITY_NEW = r'''                googleAccountInfo = googleResult.getOrNull()
                youtubeChannelInfo = channelResult.getOrNull()

                AuthSessionStore.updateIdentity(
                    googleAccountInfo =
                        googleAccountInfo,
                    youtubeChannelInfo =
                        youtubeChannelInfo
                )

                accessToken =
                    AuthSessionStore
                        .current()
                        .accessToken
                        ?: accessToken

                updateAccountPanel()
'''

IMPORT_IMPORT_OLD = r'''import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.PersistentAuthStateStore
'''

IMPORT_IMPORT_NEW = r'''import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.GoogleAccessTokenRecovery
import com.saney.ytmimporter.auth.PersistentAuthStateStore
'''

IMPORT_API_OLD = r'''    private val api =
        YouTubeApi()
'''

IMPORT_API_NEW = r'''    private val api by lazy {
        YouTubeApi(
            accessTokenRecovery =
                GoogleAccessTokenRecovery(
                    this
                )
        )
    }
'''


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    apply = not args.check

    check_new_file(
        ROOT / "app/src/main/java/com/saney/ytmimporter/auth/GoogleAccessTokenRecovery.kt",
        GOOGLE_RECOVERY,
        "GoogleAccessTokenRecovery helper",
        apply,
    )

    patches = [
        (
            "app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt",
            AUTH_STORE_OLD,
            AUTH_STORE_NEW,
            "AuthSessionStore identity-only update",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt",
            YOUTUBE_CLASS_OLD,
            YOUTUBE_CLASS_NEW,
            "YouTubeApi token-recovery contract",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt",
            YOUTUBE_REQUEST_OLD,
            YOUTUBE_REQUEST_NEW,
            "YouTubeApi single HTTP-401 retry",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
            MAIN_IMPORT_OLD,
            MAIN_IMPORT_NEW,
            "MainActivity recovery import",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
            MAIN_API_OLD,
            MAIN_API_NEW,
            "MainActivity recovery-aware YouTubeApi",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
            MAIN_IDENTITY_OLD,
            MAIN_IDENTITY_NEW,
            "MainActivity preserve refreshed token during identity load",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
            IMPORT_IMPORT_OLD,
            IMPORT_IMPORT_NEW,
            "ImportActivity recovery import",
        ),
        (
            "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
            IMPORT_API_OLD,
            IMPORT_API_NEW,
            "ImportActivity recovery-aware YouTubeApi",
        ),
    ]

    for rel, old, new, label in patches:
        check_text_file(
            ROOT / rel,
            old,
            new,
            label,
            apply,
        )

    if args.check:
        print("PASS: R3 OAuth retry anchors are ready or already applied")
    else:
        print("PASS: R3 OAuth retry patch applied")


if __name__ == "__main__":
    main()
