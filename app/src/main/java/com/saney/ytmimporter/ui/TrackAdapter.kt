package com.saney.ytmimporter.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import kotlin.math.roundToInt

class TrackAdapter(
    private val context: Context,
    private val tracks: MutableList<Track>
) : BaseAdapter() {

    override fun getCount(): Int = tracks.size
    override fun getItem(position: Int): Track = tracks[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: Holder
        val view: LinearLayout
        if (convertView == null) {
            view = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(10), dp(14), dp(10))
                minimumHeight = dp(72)
                background =
                    GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = dp(12).toFloat()
                        setColor(
                            Color.rgb(
                                25,
                                27,
                                32
                            )
                        )
                        setStroke(
                            dp(1),
                            Color.rgb(
                                44,
                                47,
                                54
                            )
                        )
                    }
            }
            val top = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val title = TextView(context).apply {
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                maxLines = 2
            }
            val status = TextView(context).apply {
                textSize = 13f
                gravity = Gravity.END
                setPadding(dp(8), 0, 0, 0)
            }
            top.addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            top.addView(status, LinearLayout.LayoutParams(dp(100), ViewGroup.LayoutParams.WRAP_CONTENT))
            val sub = TextView(context).apply {
                setTextColor(Color.rgb(170, 172, 178))
                textSize = 14f
                maxLines = 2
            }
            view.addView(top)
            view.addView(sub)
            holder = Holder(title, sub, status)
            view.tag = holder
        } else {
            view = convertView as LinearLayout
            holder = view.tag as Holder
        }

        val track = getItem(position)

        val manualSelection =
            track.manuallySelected &&
                !track.selectedVideoId.isNullOrBlank() &&
                !track.selectedTitle.isNullOrBlank()

        holder.title.text =
            "${position + 1}. ${track.originalArtist} — " +
                track.originalTitle

        holder.sub.text =
            when {
                manualSelection ->
                    buildString {
                        append("Ручний вибір: ")
                        append(track.selectedTitle)

                        if (!track.selectedChannel.isNullOrBlank()) {
                            append(" • ")
                            append(track.selectedChannel)
                        }
                    }

                !track.selectedTitle.isNullOrBlank() ->
                    buildString {
                        append("Знайдено: ")
                        append(track.selectedTitle)

                        if (!track.selectedChannel.isNullOrBlank()) {
                            append(" • ")
                            append(track.selectedChannel)
                        }
                    }

                !track.error.isNullOrBlank() ->
                    track.error

                else ->
                    "Натисніть трек, щоб перевірити/вибрати результат"
            }

        holder.status.text = statusLabel(track)
        holder.status.setTextColor(statusColor(track.status))

        view.layoutParams =
            android.widget.AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        return view
    }

    private fun statusLabel(track: Track): String = when (track.status) {
        TrackStatus.NEW -> "○ новий"
        TrackStatus.SEARCHING -> "… пошук"
        TrackStatus.MATCHED ->
            if (track.manuallySelected) {
                "✓ вибрано"
            } else {
                "✓ знайдено"
            }
        TrackStatus.REVIEW -> "! перевірити"
        TrackStatus.MISSING -> "× немає"
        TrackStatus.SKIPPED -> "— пропуск"
        TrackStatus.DUPLICATE -> "⧉ дублікат"
        TrackStatus.PENDING -> "⏳ черга"
        TrackStatus.ADDED -> "✓ додано"
        TrackStatus.FAILED -> "× помилка"
    }

    private fun statusColor(status: TrackStatus): Int = when (status) {
        TrackStatus.MATCHED, TrackStatus.ADDED -> Color.rgb(63, 196, 109)
        TrackStatus.REVIEW, TrackStatus.PENDING -> Color.rgb(255, 193, 7)
        TrackStatus.DUPLICATE -> Color.rgb(120, 170, 255)
        TrackStatus.MISSING, TrackStatus.FAILED -> Color.rgb(255, 92, 92)
        else -> Color.rgb(170, 172, 178)
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).roundToInt()

    private data class Holder(val title: TextView, val sub: TextView, val status: TextView)
}
