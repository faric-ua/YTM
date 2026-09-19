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
import com.saney.ytmimporter.storage.AllFilesAccess
import com.saney.ytmimporter.storage.DirectDownloadFileQuery
import com.saney.ytmimporter.storage.SafRecentFileQuery
import com.saney.ytmimporter.storage.SafTreeAccess
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentFileChooserActivity : Activity() {
    private var titleText =
        "Вибір файла"

    private var mimeType =
        "*/*"

    private var allowedExtensions:
        Set<String> =
        emptySet()

    private var refreshAfterSettings =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        titleText =
            intent
                .getStringExtra(EXTRA_TITLE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "Вибір файла"

        mimeType =
            intent
                .getStringExtra(EXTRA_MIME_TYPE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "*/*"

        allowedExtensions =
            intent
                .getStringArrayExtra(
                    EXTRA_ALLOWED_EXTENSIONS
                )
                ?.asSequence()
                ?.map {
                    it
                        .trim()
                        .removePrefix(".")
                        .lowercase(Locale.ROOT)
                }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()

        render()
    }

    override fun onResume() {
        super.onResume()

        if (refreshAfterSettings) {
            refreshAfterSettings = false
            render()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }

    private fun render() {
        val palette =
            AppThemeManager.palette(this)

        val roots =
            SafTreeAccess.persistedRoots(
                context = this,
                access =
                    SafTreeAccess.Access.READ
            )

        val allFilesGranted =
            AllFilesAccess.isGranted()

        val downloadFiles =
            DirectDownloadFileQuery.list(
                context = this,
                allowedExtensions =
                    allowedExtensions
            )

        val safFiles =
            SafRecentFileQuery.list(
                context = this,
                roots = roots,
                allowedExtensions =
                    allowedExtensions
            )

        val recentFiles =
            (
                downloadFiles +
                    safFiles
            )
                .distinctBy {
                    it.uri.toString()
                }
                .sortedWith(
                    compareByDescending<
                        SafRecentFileQuery.Entry
                    > {
                        it.lastModified
                    }.thenBy {
                        it.name.lowercase(
                            Locale.ROOT
                        )
                    }
                )
                .take(200)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }

        root.addView(topBar())

        root.addView(
            TextView(this).apply {
                text =
                    when {
                        AllFilesAccess.isRequired() &&
                            !allFilesGranted ->
                            "Надайте «Доступ до всіх файлів», щоб YTM Importer " +
                                "автоматично показував Download • найсвіжіші зверху"

                        else ->
                            "Останні файли: ${recentFiles.size} • найсвіжіші зверху"
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

        val listContent =
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

        if (recentFiles.isEmpty()) {
            listContent.addView(
                TextView(this).apply {
                    text =
                        when {
                            AllFilesAccess.isRequired() &&
                                !allFilesGranted ->
                                "Натисніть «Надати доступ до всіх файлів», " +
                                    "потім увімкніть доступ для YTM Importer у системних налаштуваннях."

                            roots.isEmpty() ->
                                "У Download немає файлів потрібного типу. " +
                                    "Можна додати окрему SAF-папку або відкрити системний вибір."

                            else ->
                                "У Download та дозволених SAF-папках немає файлів потрібного типу. " +
                                    "Можна додати іншу папку або відкрити системний вибір."
                        }
                    gravity = Gravity.CENTER
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
            recentFiles.forEachIndexed {
                    index,
                    entry ->
                listContent.addView(
                    fileRow(entry),
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) {
                            topMargin =
                                dp(8)
                        }
                    }
                )
            }
        }

        scroll.addView(listContent)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(footer())

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
                        this@RecentFileChooserActivity,
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
                    activity = this@RecentFileChooserActivity,
                    label = titleText.take(56),
                    maxLines = 2
                ).apply {
                    setPadding(
                        dp(12),
                        0,
                        dp(8),
                        0
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                Button(
                    this@RecentFileChooserActivity
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
                    setPadding(0, 0, 0, 0)
                    background =
                        AppThemeManager
                            .neutralButtonDrawable(
                                this@RecentFileChooserActivity
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

    private fun fileRow(
        entry:
            SafRecentFileQuery.Entry
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(this)

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
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
                            this@RecentFileChooserActivity,
                        fill =
                            palette.surfaceAlt,
                        radiusDp = 12,
                        accentStroke = false
                    )
            isClickable = true
            isFocusable = true

            addView(
                TextView(
                    this@RecentFileChooserActivity
                ).apply {
                    text =
                        entry.name
                    textSize = 15f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.text
                    )
                    maxLines = 2
                }
            )

            addView(
                TextView(
                    this@RecentFileChooserActivity
                ).apply {
                    text =
                        fileSubtitle(entry)
                    textSize = 12f
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        0,
                        dp(4),
                        0,
                        0
                    )
                    maxLines = 2
                }
            )

            setOnClickListener {
                finishWithDocument(
                    entry.uri
                )
            }
        }
    }

    private fun fileSubtitle(
        entry:
            SafRecentFileQuery.Entry
    ): String {
        val modified =
            if (entry.lastModified > 0L) {
                DATE_FORMAT.format(
                    Date(
                        entry.lastModified
                    )
                )
            } else {
                "час невідомий"
            }

        val size =
            entry.size
                ?.takeIf { it >= 0L }
                ?.let {
                    " • ${formatSize(it)}"
                }
                .orEmpty()

        return buildString {
            append(modified)
            append(size)
            append(" • ")
            append(entry.rootLabel)
        }
    }

    private fun formatSize(
        bytes: Long
    ): String =
        when {
            bytes < 1024L ->
                "$bytes Б"

            bytes <
                1024L * 1024L ->
                String.format(
                    Locale.getDefault(),
                    "%.1f КБ",
                    bytes / 1024.0
                )

            else ->
                String.format(
                    Locale.getDefault(),
                    "%.1f МБ",
                    bytes /
                        (
                            1024.0 *
                                1024.0
                        )
                )
        }

    private fun footer():
        LinearLayout {
        val root =
            LinearLayout(this).apply {
                setPadding(
                    dp(12),
                    dp(10),
                    dp(12),
                    dp(10)
                )
            }

        val buttons =
            mutableListOf<Button>()

        val needsAllFilesGrant =
            AllFilesAccess.isRequired() &&
                !AllFilesAccess.isGranted()

        val actionCount =
            if (needsAllFilesGrant) {
                4
            } else {
                3
            }

        val useCompactLandscapeLabels =
            UiChrome.useHorizontalActionRow(
                context = this,
                actionCount = actionCount
            )

        if (needsAllFilesGrant) {
            buttons +=
                footerButton(
                    label =
                        "Надати доступ до всіх файлів",
                    primary = true
                ) {
                    explainAndRequestAllFilesAccess()
                }
        }

        buttons +=
            footerButton(
                label =
                    "Додати SAF-папку…",
                primary =
                    !AllFilesAccess.isRequired()
            ) {
                openSystemTreePicker()
            }

        buttons +=
            footerButton(
                label =
                    if (useCompactLandscapeLabels) {
                        "Системний вибір…"
                    } else {
                        "Системний вибір файла…"
                    },
                primary = false
            ) {
                openSystemDocumentPicker()
            }

        buttons +=
            footerButton(
                label = "Скасувати",
                primary = false
            ) {
                finish()
            }

        UiChrome.addAdaptiveActionButtons(
            activity = this,
            container = root,
            buttons = buttons,
            buttonHeightDp = 54
        )

        return root
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
                            this@RecentFileChooserActivity
                        )
                } else {
                    AppThemeManager
                        .neutralButtonDrawable(
                            this@RecentFileChooserActivity
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

    private fun footerParams():
        LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(54)
        ).apply {
            topMargin =
                dp(8)
        }

    private fun showHelp() {
        UiChrome.showMessageDialog(
            activity = this,
            title = "Останні файли",
            message =
                "На Android 11+ YTM Importer може напряму читати Download після того, " +
                    "як ви вручну увімкнете спеціальний системний дозвіл «Доступ до всіх файлів».\n\n" +
                    "Після цього файли з Download показуються автоматично й сортуються " +
                    "за часом останньої зміни: найсвіжіші зверху.\n\n" +
                    "«Додати SAF-папку…» лишається додатковим способом підключити іншу папку. " +
                    "Корінь Download Android через SAF не дозволяє — для нього використовується " +
                    "саме All files access.\n\n" +
                    "«Системний вибір файла…» залишає стандартний Android picker як запасний варіант.\n\n" +
                    "Справжній creation time доступний не у всіх файлових системах, " +
                    "тому сортування використовує last modified.",
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

    private fun explainAndRequestAllFilesAccess() {
        UiChrome.showMessageDialog(
            activity = this,
            title = "Доступ до Download",
            message =
                "Щоб автоматично показувати файли з Download і сортувати їх " +
                    "найсвіжіші зверху, YTM Importer просить спеціальний Android-доступ " +
                    "«Доступ до всіх файлів».\n\n" +
                    "Цей доступ ширший за звичайний вибір одного файла. " +
                    "Ви можете не вмикати його й користуватися «Системним вибором файла…».",
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label =
                            "Відкрити налаштування",
                        tone =
                            UiChrome.ActionTone.ACCENT,
                        onClick = {
                            refreshAfterSettings = true
                            runCatching {
                                startActivity(
                                    AllFilesAccess.settingsIntent(
                                        this
                                    )
                                )
                            }.onFailure {
                                refreshAfterSettings = false
                                UiChrome.showMessageDialog(
                                    activity = this,
                                    title = "Не вдалося відкрити налаштування",
                                    message =
                                        "Відкрийте системні Налаштування → Спеціальний доступ → " +
                                            "Доступ до всіх файлів → YTM Importer.",
                                    actions =
                                        listOf(
                                            UiChrome.DialogAction(
                                                label = "Закрити",
                                                tone =
                                                    UiChrome.ActionTone.NORMAL
                                            ) {}
                                        )
                                )
                            }
                        }
                    ),
                    UiChrome.DialogAction(
                        label = "Скасувати",
                        tone =
                            UiChrome.ActionTone.NORMAL
                    ) {}
                )
        )
    }

    private fun openSystemTreePicker() {
        val flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION

        startActivityForResult(
            Intent(
                Intent.ACTION_OPEN_DOCUMENT_TREE
            ).apply {
                addFlags(flags)
            },
            REQUEST_SYSTEM_TREE
        )
    }

    private fun openSystemDocumentPicker() {
        startActivityForResult(
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type = mimeType
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
                        access =
                            SafTreeAccess.Access.READ
                    )
                }

                render()
            }

            REQUEST_SYSTEM_DOCUMENT -> {
                val uri =
                    data.data
                        ?: return

                runCatching {
                    contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                }

                finishWithDocument(uri)
            }
        }
    }

    private fun finishWithDocument(
        uri: Uri
    ) {
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
        const val EXTRA_TITLE =
            "recent_file_chooser_title"

        const val EXTRA_MIME_TYPE =
            "recent_file_chooser_mime_type"

        const val EXTRA_ALLOWED_EXTENSIONS =
            "recent_file_chooser_allowed_extensions"

        const val EXTRA_RESULT_KIND =
            "recent_file_chooser_result_kind"

        const val RESULT_DOCUMENT =
            "DOCUMENT"

        private const val REQUEST_SYSTEM_TREE =
            8801

        private const val REQUEST_SYSTEM_DOCUMENT =
            8802

        private val DATE_FORMAT =
            SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
            )
    }
}
