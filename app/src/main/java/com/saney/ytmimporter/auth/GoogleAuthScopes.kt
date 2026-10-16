package com.saney.ytmimporter.auth

import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.common.api.Scope

/**
 * The single set of Google scopes the app asks for, shared by silent
 * restore and HTTP-401 recovery so both request exactly the same grant.
 * (MainActivity still keeps its own copy for the interactive flow.)
 */
object GoogleAuthScopes {
    const val YOUTUBE =
        "https://www.googleapis.com/auth/youtube.force-ssl"
    const val USERINFO_EMAIL =
        "https://www.googleapis.com/auth/userinfo.email"
    const val USERINFO_PROFILE =
        "https://www.googleapis.com/auth/userinfo.profile"

    /** Silent request: no account picker, no prompt. */
    fun silentRequest(): AuthorizationRequest =
        AuthorizationRequest
            .builder()
            .setRequestedScopes(
                listOf(
                    Scope(YOUTUBE),
                    Scope(USERINFO_EMAIL),
                    Scope(USERINFO_PROFILE)
                )
            )
            .build()
}
