package com.saney.ytmimporter.auth

import android.content.Context
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.tasks.Tasks
import com.saney.ytmimporter.youtube.YouTubeApi
import java.util.concurrent.TimeUnit

/**
 * Silent Google/YTM session restore for screens other than Home.
 *
 * Why it exists (BUG-052, phone 2026-10-16): the OAuth access token lives only
 * in process memory ([AuthSessionStore]). When Android kills the app in the
 * background and later restores a deeper screen (Playlist Library) directly,
 * that screen starts with no token. Only Home knew how to re-authorize
 * silently, so Library showed YouTube as "—" until the user went to Home.
 *
 * This class does the same silent authorization as Home, off the main thread:
 * no account picker, no prompt, nothing written to disk except the existing
 * "had a successful authorization" flag. If Google needs the user (consent,
 * account choice), it returns [Result.NEEDS_USER] and the screen tells the
 * user to use Home step 2.
 */
class SilentGoogleSessionRestorer(
    context: Context
) {
    enum class Result {
        /** Token (and identity, when available) are in [AuthSessionStore]. */
        RESTORED,
        /** Never authorized on this device — nothing to restore. */
        NEVER_AUTHORIZED,
        /** Google wants user interaction; only Home step 2 can do that. */
        NEEDS_USER,
        /** Network / Google Play services failure. Safe to retry later. */
        FAILED
    }

    private val appContext = context.applicationContext

    /** Blocking. Call from a worker thread only. */
    fun restoreBlocking(
        api: YouTubeApi,
        onIdentityRequest: () -> Unit = {}
    ): Result {
        AuthSessionStore.current().accessToken
            ?.takeIf { it.isNotBlank() }
            ?.let { return Result.RESTORED }

        if (!PersistentAuthStateStore(appContext).hadSuccessfulAuthorization()) {
            return Result.NEVER_AUTHORIZED
        }

        val authorization = runCatching {
            Tasks.await(
                Identity.getAuthorizationClient(appContext)
                    .authorize(GoogleAuthScopes.silentRequest()),
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
        }.getOrNull() ?: return Result.FAILED

        if (authorization.hasResolution()) return Result.NEEDS_USER

        val token = authorization.accessToken
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return Result.FAILED

        // Home may have restored the session meanwhile; keep its identity.
        val meanwhile = AuthSessionStore.current()
        if (!meanwhile.accessToken.isNullOrBlank() &&
            meanwhile.googleAccountInfo != null &&
            meanwhile.youtubeChannelInfo != null
        ) {
            return Result.RESTORED
        }

        // Identity keeps per-account isolation of the YouTube catalogue.
        val google = runCatching { api.getGoogleAccountInfo(token) }.getOrNull()
        onIdentityRequest()
        val channel = runCatching { api.getMyYouTubeChannel(token) }.getOrNull()

        AuthSessionStore.update(
            accessToken = token,
            googleAccountInfo = google,
            youtubeChannelInfo = channel
        )
        PersistentAuthStateStore(appContext).markSuccessfulAuthorization()
        return Result.RESTORED
    }

    private companion object {
        const val TIMEOUT_SECONDS = 30L
    }
}
