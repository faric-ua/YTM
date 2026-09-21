package com.saney.ytmimporter.ui

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private var writeDisplayTracks: List<Track> = emptyList()
    private val writeStateByIndex = mutableMapOf<Int, TrackStatus>()
    private var activeWriteIndex: Int? = null
    private var writeDoneButton: Button? = null

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

        writeDisplayTracks = tracks.toList()
        writeStateByIndex.clear()
        tracks.forEachIndexed { index, track ->
            writeStateByIndex[index] = track.status
        }
        activeWriteIndex = null

        renderWriteTracks()

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
        activeTrack: Track?,
        stateSourceTracks: List<Track> = tracks
    ) {
        ensureWriteDisplayTracks(tracks)
        syncWriteStates(stateSourceTracks)

        activeWriteIndex =
            activeTrack
                ?.let(::resolveWriteIndex)

        renderWriteTracks()
    }

    private fun renderWriteTracks() {
        val rows =
            writeRows
                ?: return
        val palette =
            AppThemeManager.palette(activity)

        rows.removeAllViews()
        var activeRow: View? = null

        writeDisplayTracks.forEachIndexed { index, track ->
            val effectiveStatus =
                writeStateByIndex[index]
                    ?: track.status

            val isActive =
                activeWriteIndex == index

            val state =
                when {
                    isActive ->
                        "● Додаю…"

                    effectiveStatus ==
                        TrackStatus.ADDED ->
                        "✓ Додано"

                    effectiveStatus ==
                        TrackStatus.DUPLICATE ->
                        "≋ Дублікат • пропущено"

                    effectiveStatus ==
                        TrackStatus.FAILED ->
                        "× Помилка"

                    effectiveStatus ==
                        TrackStatus.SKIPPED ->
                        "— Пропущено"

                    else ->
                        "… Очікує"
                }

            val semanticIcon =
                when {
                    isActive ->
                        "●"

                    effectiveStatus ==
                        TrackStatus.ADDED ->
                        "✓"

                    effectiveStatus ==
                        TrackStatus.DUPLICATE ->
                        "≋"

                    effectiveStatus ==
                        TrackStatus.FAILED ->
                        "×"

                    effectiveStatus ==
                        TrackStatus.SKIPPED ->
                        "—"

                    else ->
                        "○"
                }

            val semanticIconColor =
                when {
                    isActive ->
                        palette.accent

                    effectiveStatus ==
                        TrackStatus.ADDED ->
                        palette.success

                    effectiveStatus ==
                        TrackStatus.DUPLICATE ->
                        palette.duplicate

                    effectiveStatus ==
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
                            accentStroke = false
                        )
                }

            row.addView(
                LinearLayout(activity).apply {
                    orientation =
                        LinearLayout.HORIZONTAL

                    addView(
                        TextView(activity).apply {
                            text =
                                "$semanticIcon "
                            textSize = 13.5f
                            setTypeface(
                                typeface,
                                Typeface.BOLD
                            )
                            setTextColor(
                                semanticIconColor
                            )
                        }
                    )

                    addView(
                        TextView(activity).apply {
                            text =
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
                                palette.text
                            )
                        }
                    )
                }
            )

            row.addView(
                TextView(activity).apply {
                    text =
                        when {
                            isActive ->
                                "Додаю…"

                            effectiveStatus ==
                                TrackStatus.ADDED ->
                                "Додано"

                            effectiveStatus ==
                                TrackStatus.DUPLICATE ->
                                "Дублікат • пропущено"

                            effectiveStatus ==
                                TrackStatus.FAILED ->
                                track.error
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { "Помилка: $it" }
                                    ?: "Помилка"

                            effectiveStatus ==
                                TrackStatus.SKIPPED ->
                                "Пропущено"

                            else ->
                                "Очікує"
                        }
                    textSize = 12.5f
                    setTextColor(
                        palette.muted
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
        tracks: List<Track>,
        stateSourceTracks: List<Track> = tracks
    ) {
        ensureWriteDisplayTracks(tracks)
        syncWriteStates(stateSourceTracks)
        activeWriteIndex = null

        val skippedDuplicates =
            writeStateByIndex
                .values
                .count {
                    it ==
                        TrackStatus.DUPLICATE
                }

        val prefix =
            if (progress.playlistCreated) {
                "Плейлист створено. "
            } else {
                ""
            }

        val nextMessage =
            prefix +
                "Оброблено: ${progress.processedTracks}/${progress.totalTracks} • " +
                "залишилось: ${progress.job.remainingTracks.size}\n" +
                "Додано: ${progress.job.addedCount} • " +
                "дублікатів пропущено: $skippedDuplicates • " +
                "помилок: ${progress.job.failedCount}"

        update(nextMessage)
        renderWriteTracks()
    }

    fun showWriteCompletedAction(
        onDone: () -> Unit
    ) {
        activeWriteIndex = null
        renderWriteTracks()

        val root =
            overlay as? LinearLayout
                ?: return

        writeDoneButton
            ?.let { existing ->
                (existing.parent as? ViewGroup)
                    ?.removeView(existing)
            }

        val palette =
            AppThemeManager.palette(activity)

        val button =
            Button(activity).apply {
                text = "Готово"
                isAllCaps = false
                textSize = 15f
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
                setTextColor(
                    palette.text
                )
                background =
                    AppThemeManager
                        .accentButtonDrawable(
                            activity
                        )
                setOnClickListener {
                    onDone()
                }
            }

        root.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(10)
            }
        )

        writeDoneButton = button
    }

    private fun ensureWriteDisplayTracks(
        tracks: List<Track>
    ) {
        if (
            writeDisplayTracks.size == tracks.size &&
            writeDisplayTracks.indices.all { index ->
                sameTrack(
                    writeDisplayTracks[index],
                    tracks[index]
                )
            }
        ) {
            return
        }

        writeDisplayTracks = tracks.toList()
        writeStateByIndex.clear()
        tracks.forEachIndexed { index, track ->
            writeStateByIndex[index] =
                track.status
        }
        activeWriteIndex = null
    }

    private fun syncWriteStates(
        sourceTracks: List<Track>
    ) {
        sourceTracks.forEach { track ->
            resolveWriteIndex(track)
                ?.let { index ->
                    writeStateByIndex[index] =
                        track.status
                }
        }
    }

    private fun resolveWriteIndex(
        track: Track
    ): Int? {
        val byIdentity =
            writeDisplayTracks
                .indexOfFirst {
                    it === track
                }

        if (byIdentity >= 0) {
            return byIdentity
        }

        track.historyIndex
            ?.let { historyIndex ->
                val byHistory =
                    writeDisplayTracks
                        .indexOfFirst {
                            it.historyIndex ==
                                historyIndex
                        }

                if (byHistory >= 0) {
                    return byHistory
                }
            }

        val byValue =
            writeDisplayTracks
                .indexOfFirst {
                    sameTrack(
                        it,
                        track
                    )
                }

        return byValue
            .takeIf {
                it >= 0
            }
    }

    private fun sameTrack(
        first: Track,
        second: Track
    ): Boolean =
        (
            first.historyIndex != null &&
                first.historyIndex ==
                    second.historyIndex
        ) ||
            (
                first.originalArtist ==
                    second.originalArtist &&
                    first.originalTitle ==
                        second.originalTitle &&
                    first.selectedVideoId ==
                        second.selectedVideoId
            )

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
        writeDisplayTracks = emptyList()
        writeStateByIndex.clear()
        activeWriteIndex = null
        writeDoneButton = null
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
        writeDisplayTracks = emptyList()
        writeStateByIndex.clear()
        activeWriteIndex = null
        writeDoneButton = null
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
