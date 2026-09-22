package com.saney.ytmimporter.ui

import android.app.Activity
import android.app.Dialog
import android.os.Bundle

/**
 * Owns only the semantic lifecycle of one visible modal.
 *
 * The Dialog object is transient and belongs to the current Activity instance.
 * The durable state is the modal id + primitive Bundle arguments.
 *
 * Activity recreation restores the same modal state through the supplied
 * renderer. Restoration never executes a modal action.
 */
class RestorableModalController(
    private val activity: Activity,
    private val stateKey: String
) {
    private var openModalId: String? = null
    private var openModalArgs: Bundle = Bundle()
    private var dialog: Dialog? = null
    private var activityResumed = false
    private var stateSaved = false

    fun restore(
        savedInstanceState: Bundle?
    ) {
        val snapshot =
            savedInstanceState
                ?.getBundle(stateKey)

        openModalId =
            snapshot
                ?.getString(KEY_MODAL_ID)

        openModalArgs =
            snapshot
                ?.getBundle(KEY_MODAL_ARGS)
                ?.let(::Bundle)
                ?: Bundle()
    }

    fun save(
        outState: Bundle
    ) {
        stateSaved = true

        val modalId =
            openModalId
                ?: return

        outState.putBundle(
            stateKey,
            Bundle().apply {
                putString(
                    KEY_MODAL_ID,
                    modalId
                )
                putBundle(
                    KEY_MODAL_ARGS,
                    Bundle(
                        openModalArgs
                    )
                )
            }
        )
    }

    fun show(
        modalId: String,
        args: Bundle = Bundle(),
        renderer: (
            modalId: String,
            args: Bundle
        ) -> Dialog?
    ) {
        require(
            modalId.isNotBlank()
        )

        detachCurrent(
            dismiss = true
        )

        openModalId =
            modalId
        openModalArgs =
            Bundle(args)

        attach(
            renderer(
                modalId,
                Bundle(openModalArgs)
            )
        )
    }

    fun restoreAfterContentReady(
        renderer: (
            modalId: String,
            args: Bundle
        ) -> Dialog?
    ) {
        val modalId =
            openModalId
                ?: return

        activity
            .window
            .decorView
            .post {
                if (
                    !activity.isFinishing &&
                    !activity.isDestroyed &&
                    openModalId == modalId &&
                    dialog?.isShowing != true
                ) {
                    attach(
                        renderer(
                            modalId,
                            Bundle(
                                openModalArgs
                            )
                        )
                    )
                }
            }
    }

    fun clearState() {
        openModalId = null
        openModalArgs = Bundle()
    }

    fun onResume() {
        stateSaved = false
        activityResumed = true
    }

    fun onPause() {
        activityResumed = false
    }

    fun onDestroy() {
        detachCurrent(
            dismiss = false
        )
    }

    private fun attach(
        created: Dialog?
    ) {
        if (created == null) {
            clearState()
            return
        }

        dialog =
            created

        created.setOnDismissListener {
            if (
                dialog === created
            ) {
                dialog = null
            }

            if (
                activityResumed &&
                !stateSaved &&
                !activity
                    .isChangingConfigurations
            ) {
                clearState()
            }
        }
    }

    private fun detachCurrent(
        dismiss: Boolean
    ) {
        val current =
            dialog
                ?: return

        current.setOnDismissListener(
            null
        )

        if (
            dismiss &&
            current.isShowing
        ) {
            current.dismiss()
        }

        dialog = null
    }

    private companion object {
        const val KEY_MODAL_ID =
            "modal_id"

        const val KEY_MODAL_ARGS =
            "modal_args"
    }
}
