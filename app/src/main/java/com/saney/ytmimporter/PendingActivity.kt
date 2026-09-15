package com.saney.ytmimporter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingTrack
import com.saney.ytmimporter.storage.PendingJobStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PendingActivity : Activity() {
    private lateinit var pendingJobStore: PendingJobStore

    private var currentJobId: String? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        pendingJobStore =
            PendingJobStore(this)

        val restoredId =
            savedInstanceState
                ?.getString(
                    KEY_CURRENT_JOB_ID
                )

        if (!restoredId.isNullOrBlank()) {
            val job =
                pendingJobStore.get(
                    restoredId
                )

            if (job != null) {
                showDetailScreen(job)
                return
            }
        }

        showListScreen()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putString(
            KEY_CURRENT_JOB_ID,
            currentJobId
        )

        super.onSaveInstanceState(
            outState
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentJobId != null) {
            showListScreen()
        } else {
            super.onBackPressed()
        }
    }

    private fun showListScreen() {
        currentJobId = null

        val root = baseRoot()

        root.addView(
            topBar(
                title = "Черга",
                onBack = {
                    finish()
                }
            )
        )

        root.addView(
            TextView(this).apply {
                text =
                    "Невиконані треки, збережені після quota error " +
                        "або перерваного запису."
                textSize = 13f
                setTextColor(MUTED)
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(10)
                )
            }
        )

        val search =
            EditText(this).apply {
                hint =
                    "Пошук за плейлистом, джерелом або каналом"
                setSingleLine(true)
                textSize = 14f
                setTextColor(Color.WHITE)
                setHintTextColor(
                    Color.rgb(
                        120,
                        123,
                        130
                    )
                )
                setPadding(
                    dp(14),
                    0,
                    dp(14),
                    0
                )
                background =
                    roundedBackground(
                        color = SURFACE,
                        radiusDp = 12,
                        strokeColor = BORDER
                    )
            }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                setMargins(
                    dp(12),
                    0,
                    dp(12),
                    dp(10)
                )
            }
        )

        val countText =
            TextView(this).apply {
                textSize = 12f
                setTextColor(MUTED)
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(6)
                )
            }

        root.addView(countText)

        val jobs =
            pendingJobStore.getAll()

        if (jobs.isEmpty()) {
            countText.text =
                "Черга порожня"

            root.addView(
                TextView(this).apply {
                    text =
                        "Невиконаних завдань немає.\n\n" +
                            "Якщо YouTube API зупинить запис через квоту, " +
                            "залишок автоматично з'явиться тут."
                    gravity = Gravity.CENTER
                    textSize = 15f
                    setTextColor(MUTED)
                    setPadding(
                        dp(24),
                        dp(32),
                        dp(24),
                        dp(32)
                    )
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )

            setContentView(root)
            return
        }

        val list =
            ListView(this).apply {
                divider = null
                dividerHeight = dp(6)
                clipToPadding = false
                setPadding(
                    dp(10),
                    0,
                    dp(10),
                    dp(14)
                )
                setBackgroundColor(
                    BACKGROUND
                )
            }

        root.addView(
            list,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        val adapter =
            PendingListAdapter(jobs)

        list.adapter = adapter
        countText.text =
            "${jobs.size} завдань"

        list.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            adapter
                .getItem(position)
                ?.let(
                    ::showDetailScreen
                )
        }

        search.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val query =
                        s
                            ?.toString()
                            .orEmpty()
                            .trim()

                    adapter.filter(query)

                    countText.text =
                        if (query.isBlank()) {
                            "${adapter.count} завдань"
                        } else {
                            "Знайдено: ${adapter.count}"
                        }
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    private fun showDetailScreen(
        job: PendingJob
    ) {
        currentJobId = job.id

        val root = baseRoot()

        root.addView(
            topBar(
                title = job.playlistName,
                onBack = {
                    showListScreen()
                }
            )
        )

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(24)
                )
            }

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@PendingActivity
                    ).apply {
                        text =
                            "⏳ Очікує продовження"
                        textSize = 18f
                        setTextColor(
                            Color.rgb(
                                255,
                                195,
                                80
                            )
                        )
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    metaText(
                        "Оновлено: ${formatDate(job.updatedAt)}\n" +
                            "Створено: ${formatDate(job.createdAt)}\n" +
                            "Джерело: ${job.sourceLabel}\n" +
                            "Тип: ${destinationLabel(job.destination)}\n" +
                            "Приватність: ${privacyLabel(job.privacyStatus)}"
                    )
                )
            }
        )

        content.addView(
            sectionTitle("Прогрес")
        )

        content.addView(
            card().apply {
                addView(
                    statLine(
                        "Додано",
                        "${job.addedCount}/${job.totalCount}"
                    )
                )

                addView(
                    statLine(
                        "Очікує",
                        job.remainingTracks.size
                            .toString()
                    )
                )

                if (job.failedCount > 0) {
                    addView(
                        statLine(
                            "Помилки",
                            job.failedCount
                                .toString()
                        )
                    )
                }

                val completed =
                    (
                        job.addedCount +
                            job.failedCount
                    ).coerceAtMost(
                        job.totalCount
                    )

                val percent =
                    if (job.totalCount > 0) {
                        (
                            completed *
                                100 /
                                job.totalCount
                        )
                    } else {
                        0
                    }

                addView(
                    statLine(
                        "Виконано",
                        "$percent%"
                    )
                )
            }
        )

        content.addView(
            sectionTitle("Акаунт")
        )

        content.addView(
            card().apply {
                addView(
                    metaText(
                        "Google: ${maskEmail(job.googleEmail)}\n" +
                            "YouTube/YTM: " +
                            (
                                job.youtubeChannelTitle
                                    ?: "не збережено"
                            ) +
                            "\nChannel ID: " +
                            maskId(
                                job.youtubeChannelId
                            ) +
                            "\nPlaylist ID: " +
                            maskId(
                                job.playlistId
                            )
                    )
                )
            }
        )

        if (!job.lastError.isNullOrBlank()) {
            content.addView(
                sectionTitle(
                    "Остання помилка"
                )
            )

            content.addView(
                card().apply {
                    addView(
                        TextView(
                            this@PendingActivity
                        ).apply {
                            text = job.lastError
                            textSize = 13f
                            setTextColor(
                                Color.rgb(
                                    255,
                                    150,
                                    150
                                )
                            )
                            setTextIsSelectable(
                                true
                            )
                        }
                    )
                }
            )
        }

        content.addView(
            sectionTitle(
                "Треки в черзі"
            )
        )

        content.addView(
            card().apply {
                job.remainingTracks
                    .take(15)
                    .forEachIndexed {
                            index,
                            track ->

                        addView(
                            trackRow(
                                index = index,
                                track = track
                            )
                        )
                    }

                if (
                    job.remainingTracks.size >
                        15
                ) {
                    addView(
                        metaText(
                            "Ще " +
                                "${job.remainingTracks.size - 15} " +
                                "треків."
                        )
                    )
                }
            }
        )

        content.addView(
            sectionTitle(
                "Дії"
            )
        )

        val actions =
            card()

        actions.addView(
            actionButton(
                label =
                    "Продовжити",
                primary = true
            ) {
                requestResume(job)
            }
        )

        if (!job.playlistId.isNullOrBlank()) {
            actions.addView(
                actionButton(
                    label =
                        "Відкрити плейлист у YTM",
                    primary = false
                ) {
                    openPlaylistInYtm(
                        job.playlistId
                    )
                }
            )
        }

        actions.addView(
            actionButton(
                label =
                    "Видалити з черги",
                primary = false
            ) {
                confirmDelete(job)
            }
        )

        content.addView(actions)

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun requestResume(
        job: PendingJob
    ) {
        setResult(
            RESULT_OK,
            Intent().putExtra(
                EXTRA_RESUME_JOB_ID,
                job.id
            )
        )

        finish()
    }

    private fun confirmDelete(
        job: PendingJob
    ) {
        AlertDialog.Builder(this)
            .setTitle(
                "Видалити із черги?"
            )
            .setMessage(
                "Буде видалено тільки локальне завдання " +
                    "«${job.playlistName}».\n\n" +
                    "Треки, які вже були додані в YouTube/YTM, " +
                    "не видаляються."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Видалити"
            ) { _, _ ->
                pendingJobStore.remove(
                    job.id
                )

                toast(
                    "Завдання видалено з черги"
                )

                showListScreen()
            }
            .show()
    }

    private fun openPlaylistInYtm(
        playlistId: String
    ) {
        val uri =
            Uri.parse(
                "https://music.youtube.com/" +
                    "playlist?list=$playlistId"
            )

        val ytmIntent =
            Intent(
                Intent.ACTION_VIEW,
                uri
            ).apply {
                setPackage(
                    "com.google.android.apps.youtube.music"
                )
            }

        val opened =
            runCatching {
                startActivity(
                    ytmIntent
                )
                true
            }.getOrDefault(
                false
            )

        if (!opened) {
            runCatching {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        uri
                    )
                )
            }.onFailure {
                toast(
                    "Не вдалося відкрити плейлист"
                )
            }
        }
    }

    private fun trackRow(
        index: Int,
        track: PendingTrack
    ): TextView =
        TextView(this).apply {
            text =
                buildString {
                    append(
                        "${index + 1}. "
                    )
                    append(
                        track.originalArtist
                    )
                    append(" — ")
                    append(
                        track.originalTitle
                    )

                    if (
                        !track.selectedTitle
                            .isNullOrBlank()
                    ) {
                        append("\n→ ")
                        append(
                            track.selectedTitle
                        )
                    }

                    if (
                        !track.selectedChannel
                            .isNullOrBlank()
                    ) {
                        append(
                            " • " +
                                track.selectedChannel
                        )
                    }
                }

            textSize = 13f
            setTextColor(
                Color.WHITE
            )
            setPadding(
                0,
                if (index == 0) {
                    0
                } else {
                    dp(9)
                },
                0,
                dp(5)
            )
            setTextIsSelectable(
                true
            )
        }

    private fun destinationLabel(
        destination:
            PendingDestination
    ): String =
        when (destination) {
            PendingDestination.NEW_PLAYLIST ->
                "Новий плейлист"

            PendingDestination.EXISTING_PLAYLIST ->
                "Існуючий плейлист"
        }

    private fun privacyLabel(
        value: String
    ): String =
        when (value) {
            "public" ->
                "Публічний"

            "unlisted" ->
                "За посиланням"

            else ->
                "Приватний"
        }

    private fun formatDate(
        timestamp: Long
    ): String =
        if (timestamp > 0L) {
            SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
            ).format(
                Date(timestamp)
            )
        } else {
            "невідомо"
        }

    private fun maskEmail(
        email: String?
    ): String {
        val value =
            email
                ?.trim()
                .orEmpty()

        if (value.isBlank()) {
            return "не збережено"
        }

        val at =
            value.indexOf('@')

        if (at <= 1) {
            return "•••"
        }

        return value.take(1) +
            "•••" +
            value.substring(at)
    }

    private fun maskId(
        value: String?
    ): String {
        val raw =
            value
                ?.trim()
                .orEmpty()

        if (raw.isBlank()) {
            return "—"
        }

        if (raw.length <= 10) {
            return raw
        }

        return raw.take(5) +
            "…" +
            raw.takeLast(4)
    }

    private fun baseRoot():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setBackgroundColor(
                BACKGROUND
            )
        }

    private fun topBar(
        title: String,
        onBack: () -> Unit
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            addView(
                Button(
                    this@PendingActivity
                ).apply {
                    text = "‹"
                    isAllCaps = false
                    textSize = 26f
                    setTextColor(
                        Color.WHITE
                    )
                    setPadding(
                        0,
                        0,
                        0,
                        dp(2)
                    )
                    background =
                        roundedBackground(
                            color = SURFACE,
                            radiusDp = 12,
                            strokeColor = BORDER
                        )
                    setOnClickListener {
                        onBack()
                    }
                },
                LinearLayout.LayoutParams(
                    dp(46),
                    dp(46)
                )
            )

            addView(
                TextView(
                    this@PendingActivity
                ).apply {
                    text =
                        title.take(
                            50
                        )
                    textSize = 20f
                    setTextColor(
                        Color.WHITE
                    )
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setPadding(
                        dp(12),
                        0,
                        0,
                        0
                    )
                    maxLines = 2
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }

    private fun card():
        LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
            )
            background =
                roundedBackground(
                    color = SURFACE,
                    radiusDp = 14,
                    strokeColor = BORDER
                )
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(8)
                }
        }

    private fun sectionTitle(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(MUTED)
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setPadding(
                dp(4),
                dp(8),
                0,
                dp(6)
            )
        }

    private fun metaText(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(
                Color.rgb(
                    202,
                    204,
                    210
                )
            )
            setPadding(
                0,
                dp(8),
                0,
                0
            )
            setTextIsSelectable(
                true
            )
        }

    private fun statLine(
        label: String,
        value: String
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL
            gravity =
                Gravity.CENTER_VERTICAL
            setPadding(
                0,
                dp(3),
                0,
                dp(3)
            )

            addView(
                TextView(
                    this@PendingActivity
                ).apply {
                    text = label
                    textSize = 13.5f
                    setTextColor(
                        MUTED
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(
                    this@PendingActivity
                ).apply {
                    text = value
                    textSize = 14f
                    setTextColor(
                        Color.WHITE
                    )
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                }
            )
        }

    private fun actionButton(
        label: String,
        primary: Boolean,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(
                Color.WHITE
            )
            background =
                roundedBackground(
                    color =
                        if (primary) {
                            Color.rgb(
                                196,
                                0,
                                42
                            )
                        } else {
                            Color.rgb(
                                37,
                                39,
                                46
                            )
                        },
                    radiusDp = 11,
                    strokeColor =
                        if (primary) {
                            null
                        } else {
                            Color.rgb(
                                63,
                                66,
                                76
                            )
                        }
                )

            setOnClickListener {
                action()
            }

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(46)
                ).apply {
                    bottomMargin =
                        dp(7)
                }
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): GradientDrawable =
        GradientDrawable().apply {
            shape =
                GradientDrawable.RECTANGLE
            cornerRadius =
                dp(
                    radiusDp
                ).toFloat()
            setColor(color)

            if (strokeColor != null) {
                setStroke(
                    dp(1),
                    strokeColor
                )
            }
        }

    private fun toast(
        message: String
    ) {
        Toast
            .makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            )
            .show()
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    private inner class PendingListAdapter(
        jobs: List<PendingJob>
    ) : BaseAdapter() {
        private val allJobs =
            jobs.toList()

        private val visibleJobs =
            jobs.toMutableList()

        fun filter(
            query: String
        ) {
            visibleJobs.clear()

            if (query.isBlank()) {
                visibleJobs.addAll(
                    allJobs
                )
            } else {
                val normalized =
                    query.lowercase(
                        Locale.getDefault()
                    )

                visibleJobs.addAll(
                    allJobs.filter { job ->
                        job.playlistName
                            .lowercase(
                                Locale.getDefault()
                            )
                            .contains(
                                normalized
                            ) ||
                            job.sourceLabel
                                .lowercase(
                                    Locale.getDefault()
                                )
                                .contains(
                                    normalized
                                ) ||
                            job.youtubeChannelTitle
                                .orEmpty()
                                .lowercase(
                                    Locale.getDefault()
                                )
                                .contains(
                                    normalized
                                )
                    }
                )
            }

            notifyDataSetChanged()
        }

        override fun getCount():
            Int =
            visibleJobs.size

        override fun getItem(
            position: Int
        ): PendingJob? =
            visibleJobs.getOrNull(
                position
            )

        override fun getItemId(
            position: Int
        ): Long =
            position.toLong()

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val job =
                visibleJobs[position]

            val row =
                convertView as?
                    LinearLayout
                    ?: createRow()

            val title =
                row.getChildAt(0)
                    as TextView

            val meta =
                row.getChildAt(1)
                    as TextView

            title.text =
                "⏳ ${job.playlistName}"

            meta.text =
                buildString {
                    append(
                        formatDate(
                            job.updatedAt
                        )
                    )
                    append(
                        " • Очікує " +
                            job.remainingTracks.size
                    )
                    append("\n")
                    append(
                        "Додано " +
                            "${job.addedCount}/" +
                            "${job.totalCount}"
                    )

                    if (
                        job.failedCount > 0
                    ) {
                        append(
                            " • Помилок " +
                                job.failedCount
                        )
                    }
                }

            return row
        }

        private fun createRow():
            LinearLayout =
            LinearLayout(
                this@PendingActivity
            ).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(14),
                    dp(12),
                    dp(14),
                    dp(12)
                )
                background =
                    roundedBackground(
                        color = SURFACE,
                        radiusDp = 13,
                        strokeColor = BORDER
                    )

                layoutParams =
                    AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                addView(
                    TextView(
                        this@PendingActivity
                    ).apply {
                        textSize = 15.5f
                        setTextColor(
                            Color.rgb(
                                255,
                                195,
                                80
                            )
                        )
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                        maxLines = 2
                    }
                )

                addView(
                    TextView(
                        this@PendingActivity
                    ).apply {
                        textSize = 12.5f
                        setTextColor(
                            MUTED
                        )
                        setPadding(
                            0,
                            dp(5),
                            0,
                            0
                        )
                    }
                )
            }
    }

    companion object {
        const val EXTRA_RESUME_JOB_ID =
            "pending_resume_job_id"

        private const val KEY_CURRENT_JOB_ID =
            "current_pending_job_id"

        private val BACKGROUND =
            Color.rgb(
                15,
                16,
                19
            )

        private val SURFACE =
            Color.rgb(
                25,
                27,
                32
            )

        private val BORDER =
            Color.rgb(
                48,
                51,
                59
            )

        private val MUTED =
            Color.rgb(
                165,
                167,
                173
            )
    }
}
