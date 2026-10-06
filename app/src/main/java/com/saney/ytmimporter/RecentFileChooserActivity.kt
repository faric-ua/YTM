package com.saney.ytmimporter

import android.app.Activity
import android.app.Dialog
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
import com.saney.ytmimporter.storage.YtmArtifactClassificationCache
import com.saney.ytmimporter.storage.YtmArtifactClassifier
import com.saney.ytmimporter.storage.YtmArtifactScope
import com.saney.ytmimporter.storage.YtmArtifactScopePolicy
import com.saney.ytmimporter.storage.YtmArtifactType
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.ui.ScrollPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class RecentFileChooserActivity : Activity() {
    private var titleText =
        "Вибір файла"

    private var mimeType =
        "*/*"

    private var allowedExtensions:
        Set<String> =
        emptySet()

    private var artifactScope:
        YtmArtifactScope? =
        null

    private var visibleRecentFiles:
        List<SafRecentFileQuery.Entry> =
        emptyList()

    private var recentRoots:
        List<SafTreeAccess.Root> =
        emptyList()

    private var allFilesGranted =
        false

    private var scopedFilesLoading =
        false

    private var scopedLoadGeneration =
        0

    private val classifierExecutor =
        Executors.newSingleThreadExecutor()

    private var refreshAfterSettings =
        false

    private var helpDialogOpen =
        false

    private var helpDialog: Dialog? =
        null

    private lateinit var scrollView:
        ScrollView

    private val scrollPosition =
        ScrollPositionState(
            STATE_SCROLL_POSITION
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        scrollPosition.restore(
            savedInstanceState
        )

        helpDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_HELP_DIALOG_OPEN,
                    false
                )
                ?: false

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

        artifactScope =
            intent
                .getStringExtra(
                    EXTRA_ARTIFACT_SCOPE
                )
                ?.let { raw ->
                    runCatching {
                        YtmArtifactScope
                            .valueOf(raw)
                    }.getOrNull()
                }

        refreshRecentFiles()

        if (helpDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showHelp()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_HELP_DIALOG_OPEN,
            helpDialogOpen
        )

        scrollPosition.save(
            outState,
            if (::scrollView.isInitialized) {
                scrollView
            } else {
                null
            }
        )
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }
        super.onPause()
    }

    override fun onDestroy() {
        scopedLoadGeneration += 1
        classifierExecutor.shutdownNow()

        helpDialog
            ?.setOnDismissListener(null)
        helpDialog = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (refreshAfterSettings) {
            refreshAfterSettings = false
            refreshRecentFiles()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }

    private fun refreshRecentFiles() {
        if (::scrollView.isInitialized) {
            scrollPosition.capture(
                scrollView
            )
        }

        val roots =
            SafTreeAccess.persistedRoots(
                context = this,
                access =
                    SafTreeAccess.Access.READ
            )

        val granted =
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

        val sourceFiles =
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

        recentRoots =
            roots
        allFilesGranted =
            granted

        val scope =
            artifactScope

        if (scope == null) {
            scopedLoadGeneration += 1
            scopedFilesLoading =
                false
            visibleRecentFiles =
                sourceFiles
            render()
            return
        }

        val generation =
            scopedLoadGeneration + 1

        scopedLoadGeneration =
            generation

        val cached =
            sourceFiles.map {
                    entry ->
                entry to
                    cachedArtifactType(
                        entry
                    )
            }

        val cachedMatches =
            cached
                .filter {
                    (_, type) ->
                    type != null &&
                        YtmArtifactScopePolicy
                            .accepts(
                                scope = scope,
                                type = type
                            )
                }
                .map {
                    (entry, _) ->
                    entry
                }

        val uncached =
            cached
                .filter {
                    (_, type) ->
                    type == null
                }
                .map {
                    (entry, _) ->
                    entry
                }

        visibleRecentFiles =
            cachedMatches

        if (uncached.isEmpty()) {
            scopedFilesLoading =
                false
            render()
            return
        }

        scopedFilesLoading =
            true
        render()

        classifierExecutor.execute {
            uncached.forEach {
                    entry ->
                inspectAndCacheArtifactType(
                    entry
                )
            }

            val filtered =
                sourceFiles.filter {
                        entry ->

                    val type =
                        cachedArtifactType(
                            entry
                        ) ?: YtmArtifactType.UNKNOWN

                    YtmArtifactScopePolicy
                        .accepts(
                            scope = scope,
                            type = type
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed ||
                    generation !=
                        scopedLoadGeneration
                ) {
                    return@runOnUiThread
                }

                visibleRecentFiles =
                    filtered
                scopedFilesLoading =
                    false
                render()
            }
        }
    }

    private fun cachedArtifactType(
        entry:
            SafRecentFileQuery.Entry
    ): YtmArtifactType? =
        YtmArtifactClassificationCache
            .get(
                uri =
                    entry.uri.toString(),
                lastModified =
                    entry.lastModified,
                size =
                    entry.size
            )

    private fun inspectAndCacheArtifactType(
        entry:
            SafRecentFileQuery.Entry
    ): YtmArtifactType {
        val raw =
            runCatching {
                contentResolver
                    .openInputStream(
                        entry.uri
                    )
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    ?: error(
                        "Файл недоступний для читання"
                    )
            }.getOrElse {
                return YtmArtifactType.UNKNOWN
            }

        if (
            Thread
                .currentThread()
                .isInterrupted
        ) {
            return YtmArtifactType.UNKNOWN
        }

        val type =
            YtmArtifactClassifier
                .inspect(raw)
                .type

        if (
            !Thread
                .currentThread()
                .isInterrupted
        ) {
            YtmArtifactClassificationCache
                .put(
                    uri =
                        entry.uri.toString(),
                    lastModified =
                        entry.lastModified,
                    size =
                        entry.size,
                    type =
                        type
                )
        }

        return type
    }

    private fun render() {
        val palette =
            AppThemeManager.palette(this)

        val roots =
            recentRoots

        val recentFiles =
            visibleRecentFiles

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
                        scopedFilesLoading ->
                            "Перевіряю типи JSON-файлів…"

                        AllFilesAccess.isRequired() &&
                            !allFilesGranted ->
                            "Надайте «Доступ до всіх файлів», щоб YTM Importer " +
                                "автоматично перевіряв Download • найсвіжіші зверху"

                        artifactScope != null ->
                            "Файли потрібного типу: ${recentFiles.size} • найсвіжіші зверху"

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

        scrollView =
            ScrollView(this).apply {
                isFillViewport = true
                clipToPadding = false
            }

        val scroll =
            scrollView

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
                            scopedFilesLoading ->
                                "Перевіряю вміст JSON-файлів…"

                            AllFilesAccess.isRequired() &&
                                !allFilesGranted ->
                                "Натисніть «Надати доступ до всіх файлів», " +
                                    "потім увімкніть доступ для YTM Importer у системних налаштуваннях."

                            roots.isEmpty() ->
                                "У Download немає файлів потрібного типу. " +
                                    "Можна додати окрему папку або відкрити системний вибір."

                            else ->
                                "У Download та доданих папках немає файлів потрібного типу. " +
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

        scrollPosition.restoreInto(
            scrollView
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
                    "Додати папку…",
                primary =
                    !AllFilesAccess.isRequired()
            ) {
                openSystemTreePicker()
            }

        buttons +=
            footerButton(
                label =
                    when {
                        artifactScope != null ->
                            "Інший файл…"

                        useCompactLandscapeLabels ->
                            "Системний вибір…"

                        else ->
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
        if (helpDialog?.isShowing == true) {
            return
        }

        helpDialogOpen = true

        val scopeNote =
            if (artifactScope != null) {
                "Основний список показує лише файли потрібного типу, " +
                    "визначені за вмістом JSON, а не за назвою файла.\n\n" +
                    "«Інший файл…» відкриває системний вибір Android для legacy " +
                    "або зовнішнього файла. Після вибору власник Restore/Import " +
                    "ще раз перевірить формат перед будь-якою зміною даних.\n\n"
            } else {
                ""
            }

        helpDialog =
            UiChrome.showMessageDialog(
                activity = this,
                title = "Останні файли",
                message =
                    scopeNote +
                    "На Android 11+ YTM Importer може напряму читати Download після того, " +
                        "як ви вручну увімкнете спеціальний системний дозвіл «Доступ до всіх файлів».\n\n" +
                        "Після цього файли з Download показуються автоматично й сортуються " +
                        "за часом останньої зміни: найсвіжіші зверху.\n\n" +
                        "«Додати папку…» лишається додатковим способом підключити іншу папку " +
                        "через системний вибір Android. Сам корінь Download Android не дозволяє " +
                        "підключати цим способом — для нього використовується окремий дозвіл " +
                        "«Доступ до всіх файлів».\n\n" +
                        "«Системний вибір файла…» залишає стандартний вибір Android як запасний варіант.\n\n" +
                        "Точний час створення доступний не у всіх файлових системах, " +
                        "тому список сортується за часом останньої зміни.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label = "Зрозуміло",
                            tone =
                                UiChrome.ActionTone.ACCENT
                        ) {}
                    )
            ).also { dialog ->
                dialog.setOnDismissListener {
                    helpDialogOpen = false
                    helpDialog = null
                }
            }
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

                refreshRecentFiles()
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

        const val EXTRA_ARTIFACT_SCOPE =
            "recent_file_chooser_artifact_scope"

        const val EXTRA_RESULT_KIND =
            "recent_file_chooser_result_kind"

        const val RESULT_DOCUMENT =
            "DOCUMENT"

        private const val STATE_HELP_DIALOG_OPEN =
            "recent_file_chooser_help_dialog_open"

        private const val STATE_SCROLL_POSITION =
            "recent_file_chooser_scroll_position"

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
