package com.saney.ytmimporter.ui

import android.app.Dialog
import android.os.Bundle

/**
 * Persists only the semantic identity of an open window plus a small,
 * reconstructible payload. Callbacks stay owned by the Activity.
 *
 * Restoring this state must only rebuild presentation. It must never replay
 * network, file, destructive, authorization, or navigation side effects.
 */
class RestorableWindowState(
    savedInstanceState: Bundle?,
    private val statePrefix: String
) {
    private var activeKey: String? =
        savedInstanceState
            ?.getString(
                "$statePrefix.key"
            )

    private var activeArgs: Bundle? =
        savedInstanceState
            ?.getBundle(
                "$statePrefix.args"
            )
            ?.let(::Bundle)

    val key: String?
        get() = activeKey

    fun args(): Bundle =
        activeArgs
            ?.let(::Bundle)
            ?: Bundle()

    fun show(
        key: String,
        args: Bundle = Bundle(),
        onDismiss: (() -> Unit)? = null,
        createDialog: () -> Dialog
    ): Dialog {
        activeKey = key
        activeArgs = Bundle(args)

        return createDialog()
            .also { dialog ->
                dialog.setOnDismissListener {
                    if (activeKey == key) {
                        clear()
                    }
                    onDismiss?.invoke()
                }
            }
    }

    fun save(
        outState: Bundle
    ) {
        activeKey
            ?.let { key ->
                outState.putString(
                    "$statePrefix.key",
                    key
                )
            }

        activeArgs
            ?.let { args ->
                outState.putBundle(
                    "$statePrefix.args",
                    Bundle(args)
                )
            }
    }

    fun clear() {
        activeKey = null
        activeArgs = null
    }
}
