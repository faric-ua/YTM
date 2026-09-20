package com.saney.ytmimporter.auth

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
