package com.saney.ytmimporter

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.model.LocalPlaylistEditPolicy
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.LocalPlaylistRenameResult
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome
import kotlin.math.roundToInt

class EditPlaylistActivity :
    Activity() {

    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private lateinit var nameInput:
        EditText

    private lateinit var clearButton:
        ImageButton

    private lateinit var validationText:
        TextView

    private lateinit var saveButton:
        Button

    private var targetLocalPlaylistId:
        String =
        ""

    private var originalName:
        String =
        ""

    private var draftName:
        String =
        ""

    private var validationVisible:
        Boolean =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        AppThemeManager
            .applyWindow(
                this
            )

        currentPlaylistStore =
            CurrentPlaylistStore(
                this
            )

        val snapshot =
            currentPlaylistStore
                .load()
                ?: return finishMissingPlaylist()

        targetLocalPlaylistId =
            savedInstanceState
                ?.getString(
                    STATE_TARGET_LOCAL_PLAYLIST_ID
                )
                ?: snapshot.localPlaylistId

        originalName =
            savedInstanceState
                ?.getString(
                    STATE_ORIGINAL_NAME
                )
                ?: snapshot.playlist.name

        draftName =
            savedInstanceState
                ?.getString(
                    STATE_DRAFT_NAME
                )
                ?: originalName

        validationVisible =
            savedInstanceState
                ?.getBoolean(
                    STATE_VALIDATION_VISIBLE,
                    false
                )
                ?: false

        if (
            targetLocalPlaylistId !=
            snapshot.localPlaylistId
        ) {
            toast(
                "Поточний плейлист змінився. " +
                    "Відкрийте «Редагувати» ще раз."
            )
            finish()
            return
        }

        buildUi(
            destinationPlaylistTitle =
                snapshot.destinationPlaylistTitle,
            destinationPlaylistId =
                snapshot.destinationPlaylistId
        )
    }

    override fun onResume() {
        super.onResume()

        val snapshot =
            currentPlaylistStore
                .load()

        if (
            snapshot == null ||
            snapshot.localPlaylistId !=
                targetLocalPlaylistId
        ) {
            toast(
                "Поточний плейлист змінився. " +
                    "Відкрийте «Редагувати» ще раз."
            )
            finish()
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        captureDraft()

        outState.putString(
            STATE_TARGET_LOCAL_PLAYLIST_ID,
            targetLocalPlaylistId
        )
        outState.putString(
            STATE_ORIGINAL_NAME,
            originalName
        )
        outState.putString(
            STATE_DRAFT_NAME,
            draftName
        )
        outState.putBoolean(
            STATE_VALIDATION_VISIBLE,
            validationVisible
        )

        super.onSaveInstanceState(
            outState
        )
    }

    @Deprecated(
        "Deprecated in Java"
    )
    override fun onBackPressed() {
        finish()
    }

    private fun buildUi(
        destinationPlaylistTitle: String?,
        destinationPlaylistId: String?
    ) {
        val palette =
            AppThemeManager
                .palette(
                    this
                )

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }

        root.addView(
            topBar()
        )

        val scroll =
            ScrollView(this).apply {
                isFillViewport =
                    true
                overScrollMode =
                    View.OVER_SCROLL_IF_CONTENT_SCROLLS
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(16),
                    dp(6),
                    dp(16),
                    dp(24)
                )
            }

        content.addView(
            localOnlyBadge()
        )

        content.addView(
            TextView(this).apply {
                text =
                    "Змінюється тільки назва в YTM Importer."
                textSize =
                    14f
                setTextColor(
                    palette.muted
                )
                setPadding(
                    dp(2),
                    dp(10),
                    dp(2),
                    dp(18)
                )
            }
        )

        content.addView(
            TextView(this).apply {
                text =
                    "Локальна назва"
                textSize =
                    13f
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
                setTextColor(
                    palette.muted
                )
                setPadding(
                    dp(2),
                    0,
                    dp(2),
                    dp(7)
                )
            }
        )

        nameInput =
            EditText(this).apply {
                setText(
                    draftName
                )
                textSize =
                    17f
                setTextColor(
                    palette.text
                )
                setHintTextColor(
                    palette.muted
                )
                hint =
                    "Назва плейлиста"
                gravity =
                    Gravity.TOP or
                        Gravity.START
                inputType =
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions =
                    EditorInfo.IME_ACTION_DONE
                setSingleLine(
                    false
                )
                setHorizontallyScrolling(
                    false
                )
                minLines =
                    2
                maxLines =
                    4
                setPadding(
                    dp(14),
                    dp(12),
                    dp(54),
                    dp(12)
                )
                background =
                    AppThemeManager
                        .surfaceDrawable(
                            context =
                                this@EditPlaylistActivity,
                            fill =
                                palette.surfaceAlt,
                            radiusDp =
                                12,
                            accentStroke =
                                true
                        )
                setSelection(
                    text.length
                )
            }

        val inputContainer =
            FrameLayout(this).apply {
                addView(
                    nameInput,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        clearButton =
            ImageButton(this).apply {
                contentDescription =
                    "Очистити назву"
                setImageResource(
                    R.drawable.ic_ytm_clear
                )
                imageTintList =
                    ColorStateList.valueOf(
                        palette.muted
                    )
                setBackgroundColor(
                    Color.TRANSPARENT
                )
                minimumWidth =
                    dp(48)
                minimumHeight =
                    dp(48)
                setPadding(
                    dp(10),
                    dp(10),
                    dp(10),
                    dp(10)
                )
                setOnClickListener {
                    nameInput.setText("")
                    nameInput.requestFocus()
                }
            }

        inputContainer.addView(
            clearButton,
            FrameLayout.LayoutParams(
                dp(48),
                dp(48),
                Gravity.END or
                    Gravity.CENTER_VERTICAL
            ).apply {
                marginEnd =
                    dp(4)
            }
        )

        content.addView(
            inputContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        validationText =
            TextView(this).apply {
                text =
                    "Назва не може бути порожньою."
                textSize =
                    12.5f
                setTextColor(
                    palette.semantic.danger
                )
                setPadding(
                    dp(3),
                    dp(6),
                    dp(3),
                    0
                )
                visibility =
                    if (
                        validationVisible
                    ) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }

        content.addView(
            validationText
        )

        if (
            !destinationPlaylistTitle
                .isNullOrBlank() ||
            !destinationPlaylistId
                .isNullOrBlank()
        ) {
            content.addView(
                linkedYtmCard(
                    destinationPlaylistTitle =
                        destinationPlaylistTitle,
                    destinationPlaylistId =
                        destinationPlaylistId
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(18)
                }
            )
        } else {
            content.addView(
                TextView(this).apply {
                    text =
                        "YouTube Music не змінюється."
                    textSize =
                        13f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(2),
                        dp(16),
                        dp(2),
                        0
                    )
                }
            )
        }

        scroll.addView(
            content
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            footer()
        )

        setContentView(
            root
        )

        UiChrome
            .applyScreenInsets(
                activity =
                    this,
                root =
                    root,
                extraTopDp =
                    4,
                extraBottomDp =
                    8,
                includeIme =
                    true
            )

        nameInput.addTextChangedListener(
            object :
                TextWatcher {
                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No-op.
                }

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    // No-op.
                }

                override fun afterTextChanged(
                    text: Editable?
                ) {
                    draftName =
                        text
                            ?.toString()
                            .orEmpty()

                    if (
                        draftName
                            .isNotBlank()
                    ) {
                        validationVisible =
                            false
                    }

                    refreshEditorState()
                }
            }
        )

        nameInput.setOnEditorActionListener {
                _,
                actionId,
                _ ->
            if (
                actionId ==
                    EditorInfo.IME_ACTION_DONE &&
                saveButton.isEnabled
            ) {
                save()
                true
            } else {
                false
            }
        }

        refreshEditorState()
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
                dp(12),
                dp(8)
            )

            addView(
                UiChrome.backButton(
                    activity =
                        this@EditPlaylistActivity,
                    onClick = {
                        finish()
                    }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                UiChrome.emphasizedTitle(
                    activity =
                        this@EditPlaylistActivity,
                    label =
                        "Редагувати плейлист",
                    textSizeSp =
                        21f,
                    maxLines =
                        2
                ).apply {
                    setPadding(
                        dp(14),
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

    private fun localOnlyBadge():
        TextView =
        TextView(this).apply {
            val palette =
                AppThemeManager
                    .palette(
                        this@EditPlaylistActivity
                    )

            text =
                "Лише локально"
            textSize =
                13f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                palette.accent
            )
            setPadding(
                dp(12),
                dp(7),
                dp(12),
                dp(7)
            )
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@EditPlaylistActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp =
                            999,
                        accentStroke =
                            true
                    )
        }

    private fun linkedYtmCard(
        destinationPlaylistTitle: String?,
        destinationPlaylistId: String?
    ): LinearLayout {
        val palette =
            AppThemeManager
                .palette(
                    this
                )

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
            )
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@EditPlaylistActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp =
                            12,
                        accentStroke =
                            false
                    )

            addView(
                TextView(
                    this@EditPlaylistActivity
                ).apply {
                    text =
                        "Назва в YouTube Music"
                    textSize =
                        12.5f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.muted
                    )
                }
            )

            if (
                !destinationPlaylistTitle
                    .isNullOrBlank()
            ) {
                addView(
                    TextView(
                        this@EditPlaylistActivity
                    ).apply {
                        text =
                            destinationPlaylistTitle
                        textSize =
                            15f
                        setTextColor(
                            palette.text
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

            if (
                !destinationPlaylistId
                    .isNullOrBlank()
            ) {
                addView(
                    TextView(
                        this@EditPlaylistActivity
                    ).apply {
                        text =
                            "YTM ID: " +
                                destinationPlaylistId
                        textSize =
                            12f
                        setTextColor(
                            palette.muted
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
    }

    private fun footer():
        LinearLayout =
        LinearLayout(this).apply {
            val palette =
                AppThemeManager
                    .palette(
                        this@EditPlaylistActivity
                    )

            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(8)
            )
            setBackgroundColor(
                palette.background
            )

            saveButton =
                Button(
                    this@EditPlaylistActivity
                ).apply {
                    text =
                        "Зберегти"
                    isAllCaps =
                        false
                    textSize =
                        16f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        Color.WHITE
                    )
                    minHeight =
                        0
                    minimumHeight =
                        dp(54)
                    background =
                        AppThemeManager
                            .accentButtonDrawable(
                                context =
                                    this@EditPlaylistActivity,
                                radiusDp =
                                    12
                            )
                    setOnClickListener {
                        save()
                    }
                }

            addView(
                saveButton,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                )
            )
        }

    private fun refreshEditorState() {
        if (
            !::nameInput.isInitialized ||
            !::saveButton.isInitialized
        ) {
            return
        }

        val normalized =
            LocalPlaylistEditPolicy
                .normalizeName(
                    draftName
                )

        val valid =
            normalized !=
                null

        val dirty =
            valid &&
                normalized !=
                    originalName

        clearButton.visibility =
            if (
                draftName.isEmpty()
            ) {
                View.GONE
            } else {
                View.VISIBLE
            }

        if (
            validationVisible &&
            valid
        ) {
            validationVisible =
                false
        }

        validationText.visibility =
            if (
                validationVisible
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        saveButton.isEnabled =
            dirty
        saveButton.alpha =
            if (
                dirty
            ) {
                1f
            } else {
                0.45f
            }
    }

    private fun save() {
        captureDraft()

        val normalized =
            LocalPlaylistEditPolicy
                .normalizeName(
                    draftName
                )

        if (
            normalized == null
        ) {
            validationVisible =
                true
            refreshEditorState()
            nameInput.requestFocus()
            return
        }

        when (
            currentPlaylistStore
                .renameCurrentPlaylist(
                    expectedLocalPlaylistId =
                        targetLocalPlaylistId,
                    rawName =
                        draftName
                )
        ) {
            LocalPlaylistRenameResult.RENAMED -> {
                setResult(
                    RESULT_OK
                )
                toast(
                    "Локальну назву збережено"
                )
                finish()
            }

            LocalPlaylistRenameResult.UNCHANGED -> {
                finish()
            }

            LocalPlaylistRenameResult.INVALID_NAME -> {
                validationVisible =
                    true
                refreshEditorState()
                nameInput.requestFocus()
            }

            LocalPlaylistRenameResult.NOT_FOUND -> {
                toast(
                    "Немає активного плейлиста"
                )
                finish()
            }

            LocalPlaylistRenameResult.TARGET_CHANGED -> {
                toast(
                    "Поточний плейлист змінився. " +
                        "Відкрийте «Редагувати» ще раз."
                )
                finish()
            }
        }
    }

    private fun captureDraft() {
        if (
            ::nameInput.isInitialized
        ) {
            draftName =
                nameInput.text
                    ?.toString()
                    .orEmpty()
        }
    }

    private fun finishMissingPlaylist() {
        toast(
            "Немає активного плейлиста"
        )
        finish()
    }

    private fun toast(
        message: String
    ) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).roundToInt()

    companion object {
        private const val STATE_TARGET_LOCAL_PLAYLIST_ID =
            "edit_playlist_target_local_playlist_id"

        private const val STATE_ORIGINAL_NAME =
            "edit_playlist_original_name"

        private const val STATE_DRAFT_NAME =
            "edit_playlist_draft_name"

        private const val STATE_VALIDATION_VISIBLE =
            "edit_playlist_validation_visible"
    }
}
