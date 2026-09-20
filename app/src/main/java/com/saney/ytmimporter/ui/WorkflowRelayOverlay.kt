package com.saney.ytmimporter.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.math.roundToInt

class WorkflowRelayOverlay(
    private val activity: Activity,
    savedInstanceState: Bundle?
) {
    private var active = savedInstanceState?.getBoolean(KEY_ACTIVE, false) ?: false
    private var title = savedInstanceState?.getString(KEY_TITLE).orEmpty()
    private var message = savedInstanceState?.getString(KEY_MESSAGE).orEmpty()
    private var overlay: View? = null
    private var statusText: TextView? = null

    init {
        if (active) {
            activity.window.decorView.post {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    show(
                        title.ifBlank { "Виконую дію" },
                        message.ifBlank { "Переходимо до наступного екрана…" }
                    )
                }
            }
        }
    }

    fun save(outState: Bundle) {
        outState.putBoolean(KEY_ACTIVE, active)
        outState.putString(KEY_TITLE, title)
        outState.putString(KEY_MESSAGE, message)
    }

    fun show(title: String, message: String) {
        active = true
        this.title = title
        this.message = message
        removeOverlay()

        val palette = AppThemeManager.palette(activity)
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setBackgroundColor(palette.background)
            setPadding(dp(28), dp(48), dp(28), dp(48))
        }

        root.addView(
            UiChrome.emphasizedTitle(
                activity = activity,
                label = title,
                textSizeSp = 22f
            )
        )

        val relayStatus = TextView(activity).apply {
            text = message
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(palette.muted)
            setPadding(0, dp(16), 0, dp(18))
        }
        statusText = relayStatus

        root.addView(
            relayStatus,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            ProgressBar(activity).apply { isIndeterminate = true },
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        activity.addContentView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        overlay = root
    }

    fun hide() {
        active = false
        title = ""
        message = ""
        removeOverlay()
    }

    fun update(message: String) {
        if (!active) return
        this.message = message
        statusText?.text = message
    }

    fun detach() {
        overlay = null
        statusText = null
    }

    private fun removeOverlay() {
        overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
        overlay = null
        statusText = null
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).roundToInt()

    private companion object {
        const val KEY_ACTIVE = "workflow_relay_active"
        const val KEY_TITLE = "workflow_relay_title"
        const val KEY_MESSAGE = "workflow_relay_message"
    }
}
