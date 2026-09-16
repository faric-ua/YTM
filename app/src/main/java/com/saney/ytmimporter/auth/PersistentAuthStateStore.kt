package com.saney.ytmimporter.auth

import android.content.Context

/**
 * Persists only the fact that Google/YTM authorization succeeded before.
 *
 * IMPORTANT:
 * - NO OAuth access token is stored here;
 * - NO refresh token is stored here;
 * - NO Google email/name/channel data is stored here.
 *
 * The marker survives ordinary in-place APK updates because Android keeps
 * application data. On the next process start MainActivity asks Google
 * AuthorizationClient for a fresh access token without forcing a prompt.
 */
class PersistentAuthStateStore(
    context: Context
) {
    private val prefs =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun hadSuccessfulAuthorization(): Boolean =
        prefs.getBoolean(
            KEY_HAD_SUCCESSFUL_AUTHORIZATION,
            false
        )

    fun markSuccessfulAuthorization() {
        prefs.edit()
            .putBoolean(
                KEY_HAD_SUCCESSFUL_AUTHORIZATION,
                true
            )
            .apply()
    }

    fun clear() {
        prefs.edit()
            .clear()
            .apply()
    }

    companion object {
        const val PREFS_NAME =
            "auth_state_v1"

        private const val KEY_HAD_SUCCESSFUL_AUTHORIZATION =
            "had_successful_authorization"
    }
}
