package com.saney.ytmimporter.ui

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.write.PlaylistWriteCoordinator
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
    private var writeRows: LinearLayout? = null
    private var writeScroll: ScrollView? = null

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

    fun showWriteProgress(
        playlistName: String,
        tracks: List<Track>,
        message: String
    ) {
        active = true
        title = "Створити / додати"
        this.message = message
        removeOverlay()

        val palette = AppThemeManager.palette(activity)
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            isClickable = true
            isFocusable = true
            setBackgroundColor(palette.background)
            setPadding(dp(18), dp(34), dp(18), dp(24))
        }

        root.addView(
            UiChrome.emphasizedTitle(
                activity = activity,
                label = "Створити / додати",
                textSizeSp = 22f
            )
        )

        root.addView(
            TextView(activity).apply {
                text = playlistName
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(palette.text)
                setPadding(0, dp(10), 0, dp(4))
            }
        )

        val relayStatus = TextView(activity).apply {
            text = message
            textSize = 13.5f
            setTextColor(palette.muted)
            setPadding(0, 0, 0, dp(10))
        }
        statusText = relayStatus
        root.addView(relayStatus)

        val scroll = ScrollView(activity).apply {
            isFillViewport = true
        }
        writeScroll = scroll

        val rows = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(10))
        }
        writeRows = rows
        scroll.addView(rows)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        updateWriteTracks(
            tracks = tracks,
            activeTrack = null
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

    fun updateWriteTracks(
        tracks: List<Track>,
        activeTrack: Track?
    ) {
        val rows =
            writeRows
                ?: return
        val palette =
            AppThemeManager.palette(activity)

        rows.removeAllViews()
        var activeRow: View? = null

        tracks.forEachIndexed { index, track ->
            val isActive =
                track === activeTrack

            val state =
                when {
                    isActive ->
                        "● Додаю…"

                    track.status ==
                        TrackStatus.ADDED ->
                        "✓ Додано"

                    track.status ==
                        TrackStatus.DUPLICATE ->
                        "≋ Дублікат • пропущено"

                    track.status ==
                        TrackStatus.FAILED ->
                        "× Помилка"

                    track.status ==
                        TrackStatus.SKIPPED ->
                        "— Пропущено"

                    track.status ==
                        TrackStatus.PENDING ->
                        "… Очікує"

                    else ->
                        "… Очікує"
                }

            val stateColor =
                when {
                    isActive ->
                        palette.accent

                    track.status ==
                        TrackStatus.ADDED ->
                        palette.success

                    track.status ==
                        TrackStatus.DUPLICATE ->
                        palette.duplicate

                    track.status ==
                        TrackStatus.FAILED ->
                        palette.danger

                    else ->
                        palette.muted
                }

            val row =
                LinearLayout(activity).apply {
                    orientation =
                        LinearLayout.VERTICAL
                    setPadding(
                        dp(12),
                        dp(9),
                        dp(12),
                        dp(9)
                    )
                    background =
                        AppThemeManager.surfaceDrawable(
                            context = activity,
                            fill = palette.surface,
                            radiusDp = 12,
                            accentStroke = isActive
                        )
                }

            row.addView(
                TextView(activity).apply {
                    val prefix =
                        when {
                            isActive ->
                                "● "

                            track.status ==
                                TrackStatus.ADDED ->
                                "✓ "

                            track.status ==
                                TrackStatus.DUPLICATE ->
                                "≋ "

                            track.status ==
                                TrackStatus.FAILED ->
                                "× "

                            track.status ==
                                TrackStatus.SKIPPED ->
                                "— "

                            else ->
                                ""
                        }

                    text =
                        prefix +
                            "${index + 1}. " +
                            track.originalArtist +
                            " — " +
                            track.originalTitle
                    textSize = 13.5f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        when {
                            isActive ->
                                palette.accent

                            track.status ==
                                TrackStatus.ADDED ->
                                palette.success

                            track.status ==
                                TrackStatus.DUPLICATE ->
                                palette.duplicate

                            track.status ==
                                TrackStatus.FAILED ->
                                palette.danger

                            track.status ==
                                TrackStatus.SKIPPED ->
                                palette.muted

                            else ->
                                palette.text
                        }
                    )
                }
            )

            row.addView(
                TextView(activity).apply {
                    text = state
                    textSize = 12.5f
                    setTextColor(
                        stateColor
                    )
                    setPadding(
                        0,
                        dp(4),
                        0,
                        0
                    )
                }
            )

            rows.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin =
                            dp(6)
                    }
                }
            )

            if (isActive) {
                activeRow = row
            }
        }

        activeRow?.let { row ->
            writeScroll?.post {
                writeScroll?.smoothScrollTo(
                    0,
                    (row.top - dp(12))
                        .coerceAtLeast(0)
                )
            }
        }
    }

    fun updateWriteProgress(
        progress:
            PlaylistWriteCoordinator.WriteProgress,
        tracks: List<Track>
    ) {
        val skippedDuplicates =
            tracks.count {
                it.status ==
                    TrackStatus.DUPLICATE
            }

        val nextMessage =
            if (
                progress.playlistCreated
            ) {
                "Плейлист створено. Додаю треки…"
            } else {
                "Оброблено ${progress.processedTracks}/${progress.totalTracks} • " +
                    "залишилось ${progress.job.remainingTracks.size}\n" +
                    "Додано ${progress.job.addedCount} • " +
                    "дублікатів пропущено $skippedDuplicates • " +
                    "помилок ${progress.job.failedCount}"
            }

        update(nextMessage)
        updateWriteTracks(
            tracks = tracks,
            activeTrack = null
        )
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
        writeRows = null
        writeScroll = null
    }

    private fun removeOverlay() {
        overlay?.let {
            (it.parent as? ViewGroup)
                ?.removeView(it)
        }
        overlay = null
        statusText = null
        writeRows = null
        writeScroll = null
    }

    private fun dp(value: Int): Int =
        (
            value *
                activity.resources
                    .displayMetrics
                    .density
        ).roundToInt()

    private companion object {
        const val KEY_ACTIVE =
            "workflow_relay_active"
        const val KEY_TITLE =
            "workflow_relay_title"
        const val KEY_MESSAGE =
            "workflow_relay_message"
    }
}
