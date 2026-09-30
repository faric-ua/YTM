package com.saney.ytmimporter.ui

import android.os.Bundle
import android.view.View

/**
 * Owns selectable-text range state for one logical Activity surface at a time.
 *
 * Re-attaching the same surface captures the previous root and restores matching
 * selectable text into the rebuilt root. Switching to a different logical surface
 * drops the previous selection so a stale range cannot leak across pages/objects.
 */
class SelectableTextSurfaceState(
    private val stateKey: String
) {
    private var surfaceId: String? =
        null

    private var snapshot: Bundle =
        Bundle()

    private var attachedRoot: View? =
        null

    fun restore(
        savedInstanceState: Bundle?
    ) {
        val state =
            savedInstanceState
                ?.getBundle(
                    stateKey
                )

        surfaceId =
            state
                ?.getString(
                    KEY_SURFACE_ID
                )

        snapshot =
            state
                ?.getBundle(
                    KEY_SELECTION_STATE
                )
                ?.let(::Bundle)
                ?: Bundle()

        attachedRoot =
            null
    }

    fun attach(
        root: View,
        newSurfaceId: String
    ) {
        captureCurrent()

        if (
            surfaceId !=
                newSurfaceId
        ) {
            surfaceId =
                newSurfaceId
            snapshot =
                Bundle()
        }

        attachedRoot =
            root

        SelectableTextState.restore(
            root =
                root,
            state =
                snapshot
        )
    }

    fun save(
        outState: Bundle
    ) {
        captureCurrent()

        outState.putBundle(
            stateKey,
            Bundle().apply {
                putString(
                    KEY_SURFACE_ID,
                    surfaceId
                )
                putBundle(
                    KEY_SELECTION_STATE,
                    Bundle(
                        snapshot
                    )
                )
            }
        )
    }

    fun clear() {
        attachedRoot =
            null
        surfaceId =
            null
        snapshot =
            Bundle()
    }

    private fun captureCurrent() {
        val root =
            attachedRoot
                ?: return

        snapshot =
            SelectableTextState.capture(
                root
            )
    }

    companion object {
        private const val KEY_SURFACE_ID =
            "surface_id"

        private const val KEY_SELECTION_STATE =
            "selection_state"
    }
}
