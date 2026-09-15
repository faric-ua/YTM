package com.saney.ytmimporter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.parser.PlaylistParser
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import com.saney.ytmimporter.storage.PlaylistProjectImport

class ImportActivity : Activity() {
    private val fileRequestCode =
        2301

    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private lateinit var playlistNameInput:
        EditText

    private lateinit var tracksInput:
        EditText

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        currentPlaylistStore =
            CurrentPlaylistStore(this)

        buildUi()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == fileRequestCode &&
            resultCode == RESULT_OK
        ) {
            data
                ?.data
                ?.let(::loadFile)
        }
    }

    private fun buildUi() {
        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    BACKGROUND
                )
            }

        root.addView(topBar())

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

        currentPlaylistStore
            .load()
            ?.let { current ->
                content.addView(
                    sectionTitle(
                        "Поточний робочий список"
                    )
                )

                content.addView(
                    card().apply {
                        addView(
                            TextView(
                                this@ImportActivity
                            ).apply {
                                text =
                                    current.playlist.name
                                textSize = 16f
                                setTextColor(
                                    Color.WHITE
                                )
                                setTypeface(
                                    typeface,
                                    Typeface.BOLD
                                )
                            }
                        )

                        addView(
                            infoText(
                                "${current.playlist.tracks.size} треків • " +
                                    current.sourceLabel +
                                    "\nАвтовідновлення зберігає тільки останній робочий список. " +
                                    "Для кількох списків використовуйте YTM Project."
                            )
                        )

                        addView(
                            actionButton(
                                label =
                                    "Очистити поточний список",
                                primary = false
                            ) {
                                confirmClearWorkspace(
                                    current.playlist.name
                                )
                            }
                        )
                    }
                )
            }

        content.addView(
            sectionTitle("Імпорт із файлу")
        )

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@ImportActivity
                    ).apply {
                        text =
                            "CSV, TXT або YTM Project"
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    infoText(
                        "Android file picker приймає будь-який MIME type, " +
                            "бо деякі providers неправильно позначають CSV."
                    )
                )

                addView(
                    actionButton(
                        label = "Вибрати файл",
                        primary = true
                    ) {
                        chooseFile()
                    }
                )
            }
        )

        content.addView(
            sectionTitle("Вставити текст")
        )

        content.addView(
            card().apply {
                playlistNameInput =
                    EditText(
                        this@ImportActivity
                    ).apply {
                        hint =
                            "Назва плейлиста (необов'язково)"
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
                            dp(12),
                            dp(8),
                            dp(12),
                            dp(8)
                        )
                        background =
                            roundedBackground(
                                color =
                                    Color.rgb(
                                        31,
                                        33,
                                        39
                                    ),
                                radiusDp = 10,
                                strokeColor =
                                    BORDER
                            )
                    }

                addView(
                    playlistNameInput,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(48)
                    )
                )

                tracksInput =
                    EditText(
                        this@ImportActivity
                    ).apply {
                        hint =
                            "Solarstone & JES - Like a Waterfall\n" +
                                "Sultan & Tone Depth - Moments\n" +
                                "Ahmet Ertenu - Why"
                        minLines = 10
                        gravity =
                            Gravity.TOP or
                                Gravity.START
                        inputType =
                            InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        textSize = 14f
                        setTextColor(Color.WHITE)
                        setHintTextColor(
                            Color.rgb(
                                110,
                                113,
                                120
                            )
                        )
                        setPadding(
                            dp(12),
                            dp(10),
                            dp(12),
                            dp(10)
                        )
                        background =
                            roundedBackground(
                                color =
                                    Color.rgb(
                                        31,
                                        33,
                                        39
                                    ),
                                radiusDp = 10,
                                strokeColor =
                                    BORDER
                            )
                    }

                addView(
                    tracksInput,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(8)
                    }
                )

                addView(
                    infoText(
                        "Один трек на рядок: Artist - Track. " +
                            "Підтримуються -, – та —, а також 1. / 2)."
                    )
                )

                addView(
                    actionButton(
                        label =
                            "Імпортувати текст",
                        primary = true
                    ) {
                        importText()
                    }
                )
            }
        )

        content.addView(
            sectionTitle("Що буде далі")
        )

        content.addView(
            TextView(this).apply {
                text =
                    "Після імпорту ви повернетеся на головний екран. " +
                        "Крок 3 «Знайти / перевірити» запускає пошук, " +
                        "а потім відкриває окремий Review screen."
                textSize = 13f
                setTextColor(MUTED)
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
            }
        )

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

    /**
     * Deliberately accepts any file type because some Android file providers
     * expose CSV files with unexpected MIME types.
     */
    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, fileRequestCode)
    }

    private fun loadFile(
        uri: Uri
    ) {
        try {
            contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
        } catch (_: Exception) {
        }

        val fileName =
            queryFileName(uri)
                ?: "playlist.csv"

        val text =
            runCatching {
                contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    ?: error(
                        "Не вдалося прочитати файл"
                    )
            }.getOrElse { error ->
                toast(
                    error.message
                        ?: "Не вдалося прочитати файл"
                )
                return
            }

        if (
            PlaylistProjectCodec
                .isProject(text)
        ) {
            runCatching {
                PlaylistProjectCodec
                    .importProject(text)
            }.onSuccess { project ->
                finishProjectImport(
                    project = project,
                    fileName = fileName
                )
            }.onFailure { error ->
                toast(
                    error.message
                        ?: "Не вдалося завантажити YTM Project"
                )
            }

            return
        }

        runCatching {
            PlaylistParser.parse(
                fileName,
                text
            )
        }.onSuccess { imported ->
            finishImport(
                imported = imported,
                sourceLabel =
                    "Файл ($fileName)",
                message =
                    "Файл імпортовано: " +
                        "${imported.tracks.size} треків."
            )
        }.onFailure { error ->
            toast(
                error.message
                    ?: "Помилка імпорту"
            )
        }
    }

    private fun importText() {
        val raw =
            tracksInput
                .text
                .toString()

        if (raw.isBlank()) {
            tracksInput.error =
                "Вставте хоча б один трек"
            return
        }

        runCatching {
            PlaylistParser.parse(
                "Вставлений список.txt",
                raw
            ).also { imported ->
                playlistNameInput
                    .text
                    .toString()
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        imported.name = it
                    }
            }
        }.onSuccess { imported ->
            finishImport(
                imported = imported,
                sourceLabel =
                    "Текст",
                message =
                    "Текст імпортовано: " +
                        "${imported.tracks.size} треків."
            )
        }.onFailure { error ->
            tracksInput.error =
                error.message
                    ?: "Не вдалося розібрати список"
        }
    }

    private fun finishProjectImport(
        project: PlaylistProjectImport,
        fileName: String
    ) {
        val scopeNote =
            if (
                project.sourceDestination ==
                    PendingDestination
                        .EXISTING_PLAYLIST
            ) {
                " Це import batch, а не повна копія " +
                    "старого існуючого плейлиста."
            } else {
                ""
            }

        finishImport(
            imported = project.playlist,
            sourceLabel =
                "YTM Project ($fileName)",
            message =
                "YTM Project: " +
                    "${project.playlist.tracks.size} треків. " +
                    "Точних videoId: " +
                    "${project.exactSelectionCount}. " +
                    "Без videoId: " +
                    "${project.unresolvedCount}." +
                    scopeNote
        )
    }

    private fun finishImport(
        imported: ImportedPlaylist,
        sourceLabel: String,
        message: String
    ) {
        imported.tracks
            .forEachIndexed {
                    index,
                    track ->

                track.historyIndex =
                    index
            }

        currentPlaylistStore.save(
            playlist = imported,
            sourceLabel = sourceLabel
        )

        setResult(
            RESULT_OK,
            Intent()
                .putExtra(
                    EXTRA_IMPORT_MESSAGE,
                    message
                )
        )

        finish()
    }

    private fun confirmClearWorkspace(
        playlistName: String
    ) {
        AlertDialog.Builder(this)
            .setTitle(
                "Очистити поточний список?"
            )
            .setMessage(
                "Автозбережений робочий список «$playlistName» буде видалено з пристрою.\n\n" +
                    "YTM Project-файли та плейлисти в YouTube/YTM не змінюються."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Очистити"
            ) { _, _ ->
                currentPlaylistStore.clear()

                setResult(
                    RESULT_OK,
                    Intent()
                        .putExtra(
                            EXTRA_CLEAR_WORKSPACE,
                            true
                        )
                )

                finish()
            }
            .show()
    }

    private fun queryFileName(
        uri: Uri
    ): String? {
        contentResolver
            .query(
                uri,
                null,
                null,
                null,
                null
            )
            ?.use { cursor ->
                val index =
                    cursor.getColumnIndex(
                        OpenableColumns
                            .DISPLAY_NAME
                    )

                if (
                    index >= 0 &&
                    cursor.moveToFirst()
                ) {
                    return cursor
                        .getString(index)
                }
            }

        return uri.lastPathSegment
    }

    private fun topBar():
        LinearLayout =
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
                    this@ImportActivity
                ).apply {
                    text = "‹"
                    isAllCaps = false
                    textSize = 26f
                    setTextColor(Color.WHITE)
                    background =
                        roundedBackground(
                            color = SURFACE,
                            radiusDp = 12,
                            strokeColor = BORDER
                        )
                    setOnClickListener {
                        finish()
                    }
                },
                LinearLayout.LayoutParams(
                    dp(46),
                    dp(46)
                )
            )

            addView(
                TextView(
                    this@ImportActivity
                ).apply {
                    text = "Імпорт"
                    textSize = 20f
                    setTextColor(Color.WHITE)
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
                    bottomMargin = dp(8)
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
                dp(10),
                0,
                dp(6)
            )
        }

    private fun infoText(
        text: String
    ): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 12.5f
            setTextColor(MUTED)
            setPadding(
                0,
                dp(7),
                0,
                dp(10)
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
            setTextColor(Color.WHITE)
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
                )
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
                dp(radiusDp).toFloat()
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
                Toast.LENGTH_LONG
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

    companion object {
        const val EXTRA_IMPORT_MESSAGE =
            "import_message"

        const val EXTRA_CLEAR_WORKSPACE =
            "clear_current_workspace"

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
