package com.saney.ytmimporter.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.saney.ytmimporter.HistoryActivity

class HomeHistoryDetailLink(
    private val activity: Activity,
    private val statusText: TextView,
    container: LinearLayout,
    savedInstanceState: Bundle?
) {
    private var message = ""

    private var historyEntryId: String? = null

    private val restoredMessage =
        savedInstanceState
            ?.getString(STATE_MESSAGE)
            .orEmpty()

    private val restoredHistoryEntryId =
        savedInstanceState
            ?.getString(STATE_HISTORY_ENTRY_ID)
            ?.takeIf {
                it.isNotBlank()
            }

    private val detailText =
        TextView(activity).apply {
            text = "Деталі в Історії →"
            textSize = 12f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                AppThemeManager
                    .palette(activity)
                    .accent
            )
            setPadding(
                0,
                dp(5),
                0,
                0
            )
            visibility = View.GONE
            isClickable = true
            isFocusable = true
        }

    init {
        container.addView(detailText)
    }

    fun restoreSavedState() {
        if (restoredMessage.isBlank()) {
            return
        }

        show(
            restoredMessage,
            restoredHistoryEntryId
        )
    }

    fun show(
        message: String,
        historyEntryId: String? = null
    ) {
        this.message =
            message

        this.historyEntryId =
            historyEntryId
                ?.takeIf {
                    it.isNotBlank()
                }

        render()
    }

    fun save(
        outState: Bundle
    ) {
        outState.putString(
            STATE_MESSAGE,
            message
        )

        outState.putString(
            STATE_HISTORY_ENTRY_ID,
            historyEntryId
        )
    }

    private fun render() {
        statusText.text =
            message

        statusText.maxLines =
            1

        statusText.ellipsize =
            android.text.TextUtils
                .TruncateAt.END

        statusText.setTextColor(
            AppThemeManager
                .palette(activity)
                .muted
        )

        val target =
            historyEntryId

        detailText.visibility =
            if (target == null) {
                View.GONE
            } else {
                View.VISIBLE
            }

        detailText.setOnClickListener(
            if (target == null) {
                null
            } else {
                View.OnClickListener {
                    activity.startActivity(
                        Intent(
                            activity,
                            HistoryActivity::class.java
                        ).putExtra(
                            HistoryActivity
                                .EXTRA_OPEN_ENTRY_ID,
                            target
                        )
                    )
                }
            }
        )
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                activity.resources
                    .displayMetrics
                    .density
            )
            .toInt()

    companion object {
        private const val STATE_MESSAGE =
            "home_history_status_message"

        private const val STATE_HISTORY_ENTRY_ID =
            "home_history_status_entry_id"
    }
}
