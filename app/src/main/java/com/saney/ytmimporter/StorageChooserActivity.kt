package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.saney.ytmimporter.storage.SafTreeAccess
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

class StorageChooserActivity : Activity() {
    private lateinit var access: SafTreeAccess.Access
    private lateinit var mode: Mode
    private var titleText: String = "Вибір папки"
    private var suggestedFileName: String? = null
    private var mimeType: String = "text/plain"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        access =
            runCatching {
                SafTreeAccess.Access.valueOf(
                    intent.getStringExtra(EXTRA_ACCESS)
                        ?: SafTreeAccess.Access.READ.name
                )
            }.getOrDefault(
                SafTreeAccess.Access.READ
            )

        mode =
            runCatching {
                Mode.valueOf(
                    intent.getStringExtra(EXTRA_MODE)
                        ?: Mode.TREE.name
                )
            }.getOrDefault(
                Mode.TREE
            )

        titleText =
            intent.getStringExtra(EXTRA_TITLE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: if (mode == Mode.SAVE) {
                    "Куди зберегти файл?"
                } else {
                    "Вибір папки"
                }

        suggestedFileName =
            intent.getStringExtra(
                EXTRA_SUGGESTED_FILE_NAME
            )

        mimeType =
            intent.getStringExtra(EXTRA_MIME_TYPE)
                ?.takeIf { it.isNotBlank() }
                ?: "text/plain"

        render()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }

    private fun render() {
        val palette =
            AppThemeManager.palette(this)

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

        val roots =
            SafTreeAccess.persistedRoots(
                context = this,
                access = access
            )

        root.addView(
            TextView(this).apply {
                text =
                    if (roots.isEmpty()) {
                        "Збережених папок для цієї дії ще немає."
                    } else {
                        "Збережені папки: ${roots.size}"
                    }
                textSize = 12.5f
                setTextColor(
                    palette.muted
                )
                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    dp(8)
                )
            }
        )

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
                clipToPadding = false
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(12)
                )
            }

        if (roots.isEmpty()) {
            content.addView(
                TextView(this).apply {
                    text =
                        if (mode == Mode.SAVE) {
                            "Додайте папку для швидкого збереження " +
                                "або скористайтеся системним збереженням."
                        } else {
                            "Додайте папку, щоб Android запам’ятав дозвіл " +
                                "і надалі її можна було вибирати тут."
                        }
                    gravity =
                        Gravity.CENTER
                    textSize = 15f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(18),
                        dp(36),
                        dp(18),
                        dp(36)
                    )
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        } else {
            roots.forEachIndexed { index, rootItem ->
                content.addView(
                    rootButton(
                        label =
                            buildString {
                                append(rootItem.label)
                                append("\n")
                                append(
                                    if (
                                        rootItem.canWrite
                                    ) {
                                        "Дозволено читання і запис"
                                    } else {
                                        "Дозволено читання"
                                    }
                                )
                            }
                    ) {
                        finishWithTree(
                            rootItem.uri
                        )
                    },
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) {
                            topMargin =
                                dp(9)
                        }
                    }
                )
            }
        }

        scroll.addView(content)

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

        setContentView(root)
        UiChrome.applyScreenInsets(
            this,
            root
        )
    }

    private fun topBar():
        LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
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
                UiChrome.backButton(
                    activity =
                        this@StorageChooserActivity,
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
                TextView(
                    this@StorageChooserActivity
                ).apply {
                    text =
                        titleText.take(56)
                    textSize = 20f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                    setPadding(
                        dp(12),
                        0,
                        dp(8),
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

            addView(
                Button(
                    this@StorageChooserActivity
                ).apply {
                    text = "?"
                    isAllCaps = false
                    textSize = 20f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                    minWidth = 0
                    minimumWidth = 0
                    minHeight = 0
                    minimumHeight = 0
                    setPadding(
                        0,
                        0,
                        0,
                        0
                    )
                    background =
                        AppThemeManager
                            .neutralButtonDrawable(
                                this@StorageChooserActivity
                            )
                    setOnClickListener {
                        showHelp()
                    }
                },
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )
        }
    }

    private fun footer():
        LinearLayout {
        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    dp(12),
                    dp(10),
                    dp(12),
                    dp(10)
                )
            }

        root.addView(
            footerButton(
                label =
                    if (mode == Mode.SAVE) {
                        "Додати папку…"
                    } else {
                        "Додати іншу папку…"
                    },
                primary = true
            ) {
                openSystemTreePicker()
            }
        )

        if (mode == Mode.SAVE) {
            root.addView(
                footerButton(
                    label =
                        "Зберегти як…",
                    primary = false
                ) {
                    openSystemCreateDocument()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    topMargin =
                        dp(8)
                }
            )
        }

        root.addView(
            footerButton(
                label = "Скасувати",
                primary = false
            ) {
                finish()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin =
                    dp(8)
            }
        )

        return root
    }

    private fun rootButton(
        label: String,
        onClick: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            gravity =
                Gravity.START or
                    Gravity.CENTER_VERTICAL
            setTextColor(
                palette.text
            )
            setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
            )
            minimumHeight =
                dp(72)
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context =
                            this@StorageChooserActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )
            setOnClickListener {
                onClick()
            }
        }
    }

    private fun footerButton(
        label: String,
        primary: Boolean,
        onClick: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(this)

        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setTextColor(
                palette.text
            )
            background =
                if (primary) {
                    AppThemeManager
                        .accentButtonDrawable(
                            this@StorageChooserActivity
                        )
                } else {
                    AppThemeManager
                        .neutralButtonDrawable(
                            this@StorageChooserActivity
                        )
                }
            setOnClickListener {
                onClick()
            }
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                )
        }
    }

    private fun showHelp() {
        val accessDescription =
            if (
                access ==
                    SafTreeAccess.Access.READ_WRITE
            ) {
                "Для цієї дії потрібен дозвіл на читання і запис."
            } else {
                "Для цієї дії достатньо дозволу на читання."
            }

        UiChrome.showMessageDialog(
            activity = this,
            title = "Що це за список?",
            message =
                "Це папки, до яких ви раніше надали YTM Importer доступ " +
                    "через системний Android picker. Android зберігає ці SAF-дозволи, " +
                    "тому застосунок може повторно використовувати папку без нового " +
                    "переходу в системний файловий провідник.\n\n" +
                    accessDescription +
                    "\n\n«Читання» означає, що застосунок може відкрити дані. " +
                    "«Читання і запис» також дозволяє створювати файли в папці. " +
                    "Якщо потрібної папки немає, скористайтеся кнопкою внизу.",
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Зрозуміло",
                        tone =
                            UiChrome.ActionTone.ACCENT
                    ) {}
                )
        )
    }

    private fun openSystemTreePicker() {
        val flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION or
                if (
                    access ==
                        SafTreeAccess.Access.READ_WRITE
                ) {
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                } else {
                    0
                }

        startActivityForResult(
            Intent(
                Intent.ACTION_OPEN_DOCUMENT_TREE
            ).apply {
                addFlags(flags)
            },
            REQUEST_SYSTEM_TREE
        )
    }

    private fun openSystemCreateDocument() {
        startActivityForResult(
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type =
                    mimeType
                putExtra(
                    Intent.EXTRA_TITLE,
                    suggestedFileName
                        ?: "YTM_Export.txt"
                )
            },
            REQUEST_SYSTEM_DOCUMENT
        )
    }

    @Deprecated("Deprecated in Java")
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
            resultCode != RESULT_OK ||
            data == null
        ) {
            return
        }

        when (requestCode) {
            REQUEST_SYSTEM_TREE -> {
                val uri =
                    data.data
                        ?: return

                runCatching {
                    SafTreeAccess.persist(
                        context = this,
                        treeUri = uri,
                        access = access
                    )
                }

                finishWithTree(uri)
            }

            REQUEST_SYSTEM_DOCUMENT -> {
                val uri =
                    data.data
                        ?: return

                setResult(
                    RESULT_OK,
                    Intent()
                        .setData(uri)
                        .putExtra(
                            EXTRA_RESULT_KIND,
                            RESULT_DOCUMENT
                        )
                )
                finish()
            }
        }
    }

    private fun finishWithTree(
        uri: Uri
    ) {
        setResult(
            RESULT_OK,
            Intent()
                .setData(uri)
                .putExtra(
                    EXTRA_RESULT_KIND,
                    RESULT_TREE
                )
        )
        finish()
    }

    private fun dp(value: Int): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
        ).toInt()

    private enum class Mode {
        TREE,
        SAVE
    }

    companion object {
        const val EXTRA_TITLE =
            "storage_chooser_title"
        const val EXTRA_ACCESS =
            "storage_chooser_access"
        const val EXTRA_MODE =
            "storage_chooser_mode"
        const val EXTRA_SUGGESTED_FILE_NAME =
            "storage_chooser_suggested_file_name"
        const val EXTRA_MIME_TYPE =
            "storage_chooser_mime_type"
        const val EXTRA_RESULT_KIND =
            "storage_chooser_result_kind"

        const val RESULT_TREE =
            "TREE"
        const val RESULT_DOCUMENT =
            "DOCUMENT"

        private const val REQUEST_SYSTEM_TREE =
            8701
        private const val REQUEST_SYSTEM_DOCUMENT =
            8702

        fun treeIntent(
            activity: Activity,
            title: String,
            access: SafTreeAccess.Access
        ): Intent =
            Intent(
                activity,
                StorageChooserActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_TITLE,
                    title
                )
                putExtra(
                    EXTRA_ACCESS,
                    access.name
                )
                putExtra(
                    EXTRA_MODE,
                    Mode.TREE.name
                )
            }

        fun saveIntent(
            activity: Activity,
            title: String,
            suggestedFileName: String,
            mimeType: String
        ): Intent =
            Intent(
                activity,
                StorageChooserActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_TITLE,
                    title
                )
                putExtra(
                    EXTRA_ACCESS,
                    SafTreeAccess.Access.READ_WRITE.name
                )
                putExtra(
                    EXTRA_MODE,
                    Mode.SAVE.name
                )
                putExtra(
                    EXTRA_SUGGESTED_FILE_NAME,
                    suggestedFileName
                )
                putExtra(
                    EXTRA_MIME_TYPE,
                    mimeType
                )
            }
    }
}
