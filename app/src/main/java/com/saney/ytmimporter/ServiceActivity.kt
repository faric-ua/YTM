package com.saney.ytmimporter
import com.saney.ytmimporter.ui.AppThemeManager

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.storage.SafTreeFileWriter
import com.saney.ytmimporter.ui.SafFileSaveFlow
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.youtube.SearchCache
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServiceActivity : Activity() {
    private lateinit var searchCache: SearchCache
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var historyStore: HistoryStore
    private lateinit var pendingJobStore: PendingJobStore
    private lateinit var currentPlaylistStore: CurrentPlaylistStore

    private var page: Page = Page.HOME
    private var pendingExportContent: String? = null
    private var pendingExportFileName: String? = null
    private var changelogScrollY: Int = 0

    private val saveDiagnosticsRequestCode = 6101
    private val saveDiagnosticsFolderRequestCode = 6102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        searchCache = SearchCache(this)
        quotaTracker = QuotaTracker(this)
        historyStore = HistoryStore(this)
        pendingJobStore = PendingJobStore(this)
        currentPlaylistStore = CurrentPlaylistStore(this)

        page =
            savedInstanceState
                ?.getString(KEY_PAGE)
                ?.let { raw ->
                    runCatching { Page.valueOf(raw) }.getOrNull()
                }
                ?: Page.HOME

        changelogScrollY =
            savedInstanceState
                ?.getInt(KEY_CHANGELOG_SCROLL_Y, 0)
                ?: 0

        buildUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_PAGE, page.name)
        outState.putInt(KEY_CHANGELOG_SCROLL_Y, changelogScrollY)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (::searchCache.isInitialized) {
            buildUi()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (page) {
            Page.HOME ->
                super.onBackPressed()

            Page.CHANGELOG -> {
                page = Page.ABOUT
                buildUi()
            }

            else -> {
                page = Page.HOME
                buildUi()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            return
        }

        when (requestCode) {
            saveDiagnosticsRequestCode -> {
                val uri = data?.data ?: return
                writeDiagnostics(uri)
            }

            saveDiagnosticsFolderRequestCode -> {
                val uri = data?.data ?: return

                if (
                    data.getStringExtra(
                        StorageChooserActivity.EXTRA_RESULT_KIND
                    ) ==
                        StorageChooserActivity.RESULT_DOCUMENT
                ) {
                    writeDiagnostics(uri)
                } else {
                    writeDiagnosticsToTree(uri)
                }
            }
        }
    }

    private fun buildUi() {
        AppThemeManager.applyWindow(this)

        when (page) {
            Page.HOME -> buildHome()
            Page.QUICK_START -> buildQuickStart()
            Page.PRIVACY -> buildPrivacy()
            Page.DIAGNOSTICS -> buildDiagnostics()
            Page.SEARCH_CACHE -> buildSearchCache()
            Page.ABOUT -> buildAbout()
            Page.CHANGELOG -> buildChangelog()
        }
    }

    private fun buildHome() {
        val root = screenRoot()
        root.addView(topBar("Сервіс"))

        val content = contentColumn()
        val cache = searchCache.stats()
        val quota = quotaTracker.snapshot()

        content.addView(
            infoCard(
                title = "Стан",
                body =
                    "Cache: ${cache.validEntries} активних записів\n" +
                        "Search quota: ${quota.searchCalls}/${QuotaTracker.SEARCH_DAILY_LIMIT}"
            )
        )

        content.addView(sectionTitle("Допомога"))
        content.addView(
            serviceCard(
                "Швидкий старт",
                "Як створити плейлист у 4 кроки"
            ) { open(Page.QUICK_START) }
        )
        content.addView(
            serviceCard(
                "Приватність",
                "Які дані використовуються та що зберігається локально"
            ) { open(Page.PRIVACY) }
        )

        content.addView(sectionTitle("Діагностика"))
        content.addView(
            serviceCard(
                "Діагностика",
                "Акаунт, імпорт, quota, cache та локальні дані"
            ) { open(Page.DIAGNOSTICS) }
        )

        content.addView(sectionTitle("API та локальні дані"))
        content.addView(
            serviceCard(
                "SearchCache",
                "Статистика, прострочені записи та очищення"
            ) { open(Page.SEARCH_CACHE) }
        )
        content.addView(
            serviceCard(
                "Google Cloud Console",
                "Відкрити квоту YouTube Data API у браузері"
            ) { openGoogleCloudQuota() }
        )

        content.addView(sectionTitle("Про застосунок"))
        content.addView(
            serviceCard(
                "Про YTM Importer",
                "Версія, можливості та принципи роботи"
            ) { open(Page.ABOUT) }
        )

        setScreen(root, content)
    }

    private fun buildQuickStart() {
        val root = screenRoot()
        root.addView(topBar("Швидкий старт"))
        val content = contentColumn()

        content.addView(
            infoCard(
                "4 кроки до плейлиста",
                "Кожен крок має окремий екран. Після перевірки треків можна повернутися до проекту пізніше."
            )
        )

        quickStep(content, "1", "Імпорт", "CSV / TXT / YTM Project або вставлений список Artist - Track.")
        quickStep(content, "2", "Google / YTM", "Підключіть Google OAuth і завантажте інформацію про YouTube channel.")
        quickStep(content, "3", "Знайти / перевірити", "SearchCache зменшує повторні API-пошуки. Сумнівні результати перевірте вручну.")
        quickStep(content, "4", "Створити / додати", "Створіть новий плейлист або додайте треки до існуючого з duplicate check.")

        content.addView(sectionTitle("Корисно"))
        content.addView(
            serviceCard(
                "Приватність",
                "Що зберігається локально і що не входить у backup"
            ) { open(Page.PRIVACY) }
        )

        setScreen(root, content)
    }

    private fun buildPrivacy() {
        val root = screenRoot()
        root.addView(topBar("Приватність"))
        val content = contentColumn()

        content.addView(
            infoCard(
                "Google OAuth та YouTube API",
                "Використовуються лише для дій, які запускає користувач: інформація про акаунт/канал, пошук, створення плейлистів і додавання треків."
            )
        )
        content.addView(
            infoCard(
                "Локальні дані",
                "На телефоні можуть зберігатися History, Pending Queue, SearchCache, поточний робочий список і локальна оцінка quota."
            )
        )
        content.addView(
            infoCard(
                "Не зберігається у проектах",
                "OAuth access token, Google password і signing keys не входять у YTM Project, Diagnostics або Full Backup."
            )
        )
        content.addView(
            infoCard(
                "Backup та Android Share",
                "Full Backup може містити email, Channel ID, назви плейлистів та History. Надсилайте backup лише туди, де довіряєте одержувачу. YTM Importer не має власного сервера."
            )
        )
        content.addView(
            infoCard(
                "Незалежний інструмент",
                "YTM Importer не є офіційним застосунком Google або YouTube."
            )
        )

        setScreen(root, content)
    }

    private fun buildDiagnostics() {
        val root = screenRoot()
        root.addView(topBar("Діагностика"))
        val content = contentColumn()

        val quota = quotaTracker.snapshot()
        val cache = searchCache.stats()
        val history = historyStore.getAll()
        val pending = pendingJobStore.getAll()
        val playlist = currentPlaylistStore.load()?.playlist
        val tracks = playlist?.tracks.orEmpty()

        content.addView(
            infoCard(
                "Акаунт",
                "Google: ${if (intent.getBooleanExtra(EXTRA_GOOGLE_CONNECTED, false)) "підключено" else "не підключено"}\n" +
                    "Email: ${intent.getStringExtra(EXTRA_GOOGLE_EMAIL) ?: "—"}\n" +
                    "YouTube/YTM: ${intent.getStringExtra(EXTRA_CHANNEL_TITLE) ?: "не завантажено"}\n" +
                    "Channel ID: ${intent.getStringExtra(EXTRA_CHANNEL_ID) ?: "—"}"
            )
        )

        content.addView(
            infoCard(
                "Поточний імпорт",
                buildString {
                    append("Playlist: ${playlist?.name ?: "немає"}\n")
                    append("Треків: ${tracks.size}")
                    TrackStatus.entries.forEach { status ->
                        val count = tracks.count { it.status == status }
                        if (count > 0) append("\n${status.name}: $count")
                    }
                }
            )
        )

        content.addView(
            infoCard(
                "Quota — локальна оцінка",
                "День Google: ${quota.dayKey} Pacific Time\n" +
                    "Search: ${quota.searchCalls}/${QuotaTracker.SEARCH_DAILY_LIMIT} • ≈${quota.searchRemaining} залишилось\n" +
                    "General: ${quota.generalUnits}/${QuotaTracker.GENERAL_DAILY_LIMIT} • ≈${quota.generalRemaining} залишилось\n" +
                    "Cache hits сьогодні: ${quota.cacheHits}"
            )
        )

        content.addView(
            infoCard(
                "SearchCache",
                "Усього: ${cache.totalEntries}\n" +
                    "Активних: ${cache.validEntries}\n" +
                    "Прострочених: ${cache.expiredEntries}\n" +
                    "Пошкоджених: ${cache.malformedEntries}\n" +
                    "Розмір: ${formatBytes(cache.approximateBytes)}\n" +
                    "Найстаріший: ${formatNullableDate(cache.oldestCachedAt)}\n" +
                    "Найновіший: ${formatNullableDate(cache.newestCachedAt)}"
            )
        )

        content.addView(
            infoCard(
                "Локальні дані",
                "History: ${history.size} записів\n" +
                    "Pending Queue: ${pending.size} завдань\n" +
                    "History JSON: ${formatBytes(historyStore.exportJson().toByteArray(Charsets.UTF_8).size.toLong())}\n" +
                    "Pending JSON: ${formatBytes(pendingJobStore.exportJson().toByteArray(Charsets.UTF_8).size.toLong())}"
            )
        )

        content.addView(
            infoCard(
                "Приватність Diagnostics",
                "Звіт не містить OAuth access token, Google password або signing keys. Email та Channel ID передаються вже замаскованими."
            )
        )

        content.addView(sectionTitle("Дії"))
        content.addView(
            fullActionButton("Зберегти Diagnostics TXT") {
                saveDiagnostics()
            }
        )
        content.addView(
            fullActionButton("Поділитися Diagnostics TXT") {
                shareDiagnostics()
            }
        )

        setScreen(root, content)
    }

    private fun buildSearchCache() {
        val root = screenRoot()
        root.addView(topBar("SearchCache"))
        val content = contentColumn()
        val stats = searchCache.stats()

        content.addView(
            infoCard(
                "Статистика",
                "Усього записів: ${stats.totalEntries}\n" +
                    "Активних: ${stats.validEntries}\n" +
                    "Прострочених: ${stats.expiredEntries}\n" +
                    "Пошкоджених: ${stats.malformedEntries}\n" +
                    "Приблизний розмір: ${formatBytes(stats.approximateBytes)}\n" +
                    "Найстаріший: ${formatNullableDate(stats.oldestCachedAt)}\n" +
                    "Найновіший: ${formatNullableDate(stats.newestCachedAt)}"
            )
        )
        content.addView(
            infoCard(
                "Що станеться після очищення",
                "History, Pending Queue та YouTube/YTM плейлисти не видаляються. Повторний пошук очищених треків знову витрачатиме search quota."
            )
        )

        content.addView(sectionTitle("Дії"))
        content.addView(
            fullActionButton(
                label = "Видалити прострочені записи",
                danger = true
            ) {
                confirmClearExpiredSearchCache(
                    stats.expiredEntries
                )
            }
        )
        content.addView(
            fullActionButton(
                label = "Очистити весь SearchCache",
                danger = true
            ) {
                confirmClearSearchCache()
            }
        )

        setScreen(root, content)
    }

    private fun buildAbout() {
        val root = screenRoot()
        root.addView(topBar("Про YTM Importer"))
        val content = contentColumn()

        content.addView(
            infoCard(
                "Версія",
                "YTM Importer ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                    "Android target SDK: ${applicationInfo.targetSdkVersion}"
            )
        )
        content.addView(
            infoCard(
                "Що робить застосунок",
                "Перетворює трекліст у плейлист YouTube / YouTube Music і дозволяє вручну перевірити кожен результат перед записом."
            )
        )
        content.addView(
            infoCard(
                "Імпорт та пошук",
                "• CSV / TXT / прямий текст / YTM Project\n" +
                    "• автоматичний пошук + SearchCache\n" +
                    "• ручна перевірка та заміна треків"
            )
        )
        content.addView(
            infoCard(
                "Плейлисти та відновлення",
                "• новий або існуючий плейлист\n" +
                    "• перевірка дублікатів\n" +
                    "• History + YTM Project\n" +
                    "• Queue / Resume при quota problems\n" +
                    "• Backup / Restore / Rollback"
            )
        )
        content.addView(
            infoCard(
                "Принципи",
                "Без реклами, власного сервера та вбудованої аналітики. Незалежний інструмент, не є офіційним застосунком Google або YouTube."
            )
        )

        content.addView(sectionTitle("Дізнатися більше"))
        content.addView(
            serviceCard(
                "Швидкий старт",
                "4 кроки до готового плейлиста"
            ) { open(Page.QUICK_START) }
        )
        content.addView(
            serviceCard(
                "Приватність",
                "Локальні дані, OAuth і backup"
            ) { open(Page.PRIVACY) }
        )
        content.addView(
            serviceCard(
                "Історія змін",
                "Що змінювалося у кожному релізі"
            ) { open(Page.CHANGELOG) }
        )

        setScreen(root, content)
    }

    private fun buildChangelog() {
        val root =
            screenRoot()

        root.addView(
            topBar(
                "Історія змін"
            )
        )

        val content =
            contentColumn()

        content.addView(
            infoCard(
                "YTM Importer ${BuildConfig.VERSION_NAME}",
                "Найновіші релізи показані першими. " +
                    "Історія змін вбудовується у застосунок з CHANGELOG.md під час build."
            )
        )

        val releases =
            loadReleaseHistory()

        if (releases.isEmpty()) {
            content.addView(
                infoCard(
                    "Немає даних",
                    "Не вдалося прочитати вбудовану історію змін."
                )
            )
        } else {
            releases.forEach { release ->
                content.addView(
                    infoCard(
                        title =
                            release.title,
                        body =
                            release.body
                    )
                )
            }
        }

        setScreen(
            root,
            content
        )
    }

    private fun loadReleaseHistory():
        List<ReleaseNote> {
        val raw =
            runCatching {
                assets
                    .open(
                        CHANGELOG_ASSET
                    )
                    .bufferedReader(
                        Charsets.UTF_8
                    )
                    .use {
                        it.readText()
                    }
            }.getOrElse {
                return emptyList()
            }

        val releases =
            mutableListOf<ReleaseNote>()

        var title:
            String? =
            null

        val body =
            mutableListOf<String>()

        fun flush() {
            val releaseTitle =
                title
                    ?: return

            val releaseBody =
                body
                    .joinToString(
                        "\n"
                    )
                    .trim()
                    .ifBlank {
                        "Без окремого опису."
                    }

            releases +=
                ReleaseNote(
                    title =
                        releaseTitle,
                    body =
                        releaseBody
                )

            body.clear()
        }

        raw
            .lineSequence()
            .forEach { sourceLine ->
                val line =
                    sourceLine
                        .trimEnd()

                when {
                    line.startsWith(
                        "## "
                    ) -> {
                        flush()

                        title =
                            line
                                .removePrefix(
                                    "## "
                                )
                                .trim()
                    }

                    title == null -> {
                        // Ignore the document H1 before
                        // the first release section.
                    }

                    line.startsWith(
                        "- "
                    ) -> {
                        body +=
                            "• " +
                                cleanReleaseMarkdown(
                                    line.removePrefix(
                                        "- "
                                    )
                                )
                    }

                    line.isBlank() -> {
                        if (
                            body.isNotEmpty() &&
                                body.last()
                                    .isNotBlank()
                        ) {
                            body += ""
                        }
                    }

                    else -> {
                        body +=
                            cleanReleaseMarkdown(
                                line
                            )
                    }
                }
            }

        flush()

        return releases
    }

    private fun cleanReleaseMarkdown(
        value: String
    ): String =
        value
            .replace(
                "`",
                ""
            )
            .replace(
                "**",
                ""
            )
            .trim()


    private fun open(value: Page) {
        if (value == Page.CHANGELOG && page != Page.CHANGELOG) {
            changelogScrollY = 0
        }

        page = value
        buildUi()
    }

    private fun screenRoot(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppThemeManager.palette(this@ServiceActivity).background)
        }

    private fun contentColumn(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }

    private fun setScreen(
        root: LinearLayout,
        content: LinearLayout
    ) {
        val scroll = ScrollView(this).apply {
            isFillViewport = true

            if (page == Page.CHANGELOG) {
                setOnScrollChangeListener { _, _, scrollY, _, _ ->
                    changelogScrollY = scrollY
                }
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
        setContentView(root)
        UiChrome.applyScreenInsets(this, root)

        if (page == Page.CHANGELOG && changelogScrollY > 0) {
            scroll.post {
                scroll.scrollTo(0, changelogScrollY)
            }
        }
    }

    private fun topBar(title: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(10))

            addView(
                UiChrome.backButton(
                    activity = this@ServiceActivity,
                    onClick = { onBackPressed() }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                UiChrome.emphasizedTitle(
                    activity = this@ServiceActivity,
                    label = title,
                    textSizeSp = 21f
                ).apply {
                    setPadding(dp(14), 0, dp(4), 0)
                },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
        }

    private fun quickStep(
        parent: LinearLayout,
        number: String,
        title: String,
        body: String
    ) {
        parent.addView(
            infoCard(
                title = "$number. $title",
                body = body
            )
        )
    }

    private fun infoCard(
        title: String,
        body: String
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedBackground(SURFACE, 16, BORDER)
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }

            addView(
                TextView(this@ServiceActivity).apply {
                    text = title
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    setLineSpacing(0f, 1.04f)
                }
            )
            addView(
                TextView(this@ServiceActivity).apply {
                    text = body
                    textSize = 13.5f
                    setTextColor(MUTED)
                    setPadding(0, dp(7), 0, 0)
                    setLineSpacing(0f, 1.12f)
                    setTextIsSelectable(true)
                }
            )
        }

    private fun sectionTitle(value: String): TextView =
        TextView(this).apply {
            text = value
            textSize = 13f
            setTextColor(MUTED)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(4), dp(14), 0, dp(7))
        }

    private fun serviceCard(
        title: String,
        subtitle: String,
        action: () -> Unit
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(13), dp(12), dp(13))
            background = roundedBackground(ROW_SURFACE, 14, BORDER)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }

            val textColumn = LinearLayout(this@ServiceActivity).apply {
                orientation = LinearLayout.VERTICAL
            }
            textColumn.addView(
                TextView(this@ServiceActivity).apply {
                    text = title
                    textSize = 15.5f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    maxLines = 2
                }
            )
            textColumn.addView(
                TextView(this@ServiceActivity).apply {
                    text = subtitle
                    textSize = 12.5f
                    setTextColor(MUTED)
                    setPadding(0, dp(4), 0, 0)
                    maxLines = 3
                }
            )
            addView(
                textColumn,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
            addView(
                TextView(this@ServiceActivity).apply {
                    text = "›"
                    textSize = 25f
                    setTextColor(MUTED)
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(dp(28), ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }

    private fun fullActionButton(
        label: String,
        danger: Boolean = false,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            minHeight = dp(56)
            minimumHeight = dp(56)
            maxLines = 2
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setTextColor(
                if (danger) Color.rgb(255, 100, 115) else Color.WHITE
            )
            background = roundedBackground(ROW_SURFACE, 13, BORDER)
            UiChrome.autoSizeButton(this, 11, 15)
            setOnClickListener { action() }
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
        }

    private fun confirmClearExpiredSearchCache(
        expiredCount: Int
    ) {
        if (expiredCount <= 0) {
            toast(
                "Прострочених записів немає"
            )
            return
        }

        UiChrome.showDangerConfirmDialog(
            activity = this,
            title =
                "Видалити прострочені записи?",
            message =
                "Буде видалено $expiredCount прострочених записів SearchCache.\n\n" +
                    "History, Pending Queue та плейлисти YouTube/YTM не змінюються. " +
                    "Якщо ці треки знадобляться знову, пошук повторно витрачатиме quota.",
            confirmLabel =
                "Так, видалити"
        ) {
            val removed =
                searchCache.clearExpired()

            toast(
                "Видалено записів SearchCache: $removed"
            )

            buildUi()
        }
    }

    private fun confirmClearSearchCache() {
        UiChrome.showDangerConfirmDialog(
            activity = this,
            title =
                "Очистити весь SearchCache?",
            message =
                "Усі кешовані результати пошуку буде видалено.\n\n" +
                    "History і плейлисти не зміняться, але наступний пошук " +
                    "цих треків знову звернеться до YouTube API.",
            confirmLabel =
                "Так, очистити"
        ) {
            val before =
                searchCache
                    .stats()
                    .totalEntries

            searchCache.clear()

            toast(
                "SearchCache очищено: $before записів"
            )

            buildUi()
        }
    }

    private fun saveDiagnostics() {
        val fileName =
            "YTM_Diagnostics_${exportTimestamp()}.txt"

        pendingExportContent =
            buildDiagnosticsText()
        pendingExportFileName =
            fileName

        runCatching {
            SafFileSaveFlow.show(
                activity = this,
                title =
                    "Куди зберегти Diagnostics?",
                suggestedFileName =
                    fileName,
                mimeType =
                    "text/plain",
                requestCode =
                    saveDiagnosticsFolderRequestCode
            )
        }.onFailure { error ->
            clearPendingDiagnostics()

            toast(
                "Не вдалося відкрити вибір збереження: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }
    }

    private fun writeDiagnosticsToTree(
        treeUri: Uri
    ) {
        val content =
            pendingExportContent
                ?: return

        val fileName =
            pendingExportFileName
                ?: return

        runCatching {
            SafTreeFileWriter.writeText(
                context = this,
                treeUri = treeUri,
                preferredFileName =
                    fileName,
                mimeType =
                    "text/plain",
                content = content
            )
        }.onSuccess { result ->
            toast(
                "Diagnostics TXT збережено: " +
                    result.fileName
            )
        }.onFailure { error ->
            toast(
                "Помилка запису: " +
                    (
                        error.message
                            ?: "невідома помилка"
                    )
            )
        }

        clearPendingDiagnostics()
    }

    private fun writeDiagnostics(uri: Uri) {
        val content = pendingExportContent ?: return
        runCatching {
            contentResolver.openOutputStream(uri, "w")
                ?.bufferedWriter(Charsets.UTF_8)
                ?.use { it.write(content) }
                ?: error("Android не відкрив файл для запису")
        }.onSuccess {
            toast("Diagnostics TXT збережено")
        }.onFailure { error ->
            toast("Помилка запису: ${error.message ?: "невідома помилка"}")
        }
        clearPendingDiagnostics()
    }

    private fun clearPendingDiagnostics() {
        pendingExportContent = null
        pendingExportFileName = null
    }

    private fun shareDiagnostics() {
        val fileName = "YTM_Diagnostics_${exportTimestamp()}.txt"
        runCatching {
            val directory = File(cacheDir, "shared_exports").apply { mkdirs() }
            val file = File(directory, fileName).apply {
                writeText(buildDiagnosticsText(), Charsets.UTF_8)
            }
            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri(fileName, uri)
            }
            startActivity(
                Intent.createChooser(
                    intent,
                    "Поділитися YTM Diagnostics"
                )
            )
        }.onFailure { error ->
            toast("Не вдалося поділитися: ${error.message ?: "невідома помилка"}")
        }
    }

    private fun buildDiagnosticsText(): String {
        val quota = quotaTracker.snapshot()
        val cache = searchCache.stats()
        val history = historyStore.getAll()
        val pending = pendingJobStore.getAll()
        val playlist = currentPlaylistStore.load()?.playlist
        val tracks = playlist?.tracks.orEmpty()

        return buildString {
            append("YTM Importer — Diagnostics\n")
            append("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
            append("Package: $packageName\n")
            append("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Generated: ${formatDate(System.currentTimeMillis())}\n\n")

            append("ACCOUNT\n")
            append("Google: ${if (intent.getBooleanExtra(EXTRA_GOOGLE_CONNECTED, false)) "connected" else "not connected"}\n")
            append("Google email: ${intent.getStringExtra(EXTRA_GOOGLE_EMAIL) ?: "—"}\n")
            append("YouTube/YTM channel: ${intent.getStringExtra(EXTRA_CHANNEL_TITLE) ?: "not loaded"}\n")
            append("Channel ID: ${intent.getStringExtra(EXTRA_CHANNEL_ID) ?: "—"}\n\n")

            append("CURRENT IMPORT\n")
            append("Playlist: ${playlist?.name ?: "none"}\n")
            append("Tracks: ${tracks.size}\n")
            TrackStatus.entries.forEach { status ->
                val count = tracks.count { it.status == status }
                if (count > 0) append("${status.name}: $count\n")
            }
            append("\n")

            append("QUOTA — local estimate\n")
            append("Day: ${quota.dayKey} Pacific Time\n")
            append("Search: ${quota.searchCalls}/${QuotaTracker.SEARCH_DAILY_LIMIT} (remaining ≈${quota.searchRemaining})\n")
            append("General: ${quota.generalUnits}/${QuotaTracker.GENERAL_DAILY_LIMIT} (remaining ≈${quota.generalRemaining})\n")
            append("Cache hits today: ${quota.cacheHits}\n\n")

            append("SEARCH CACHE\n")
            append("Total entries: ${cache.totalEntries}\n")
            append("Valid: ${cache.validEntries}\n")
            append("Expired: ${cache.expiredEntries}\n")
            append("Malformed: ${cache.malformedEntries}\n")
            append("Approx size: ${formatBytes(cache.approximateBytes)}\n")
            append("Oldest: ${formatNullableDate(cache.oldestCachedAt)}\n")
            append("Newest: ${formatNullableDate(cache.newestCachedAt)}\n\n")

            append("LOCAL DATA\n")
            append("History entries: ${history.size}\n")
            append("Pending jobs: ${pending.size}\n")
            append("History JSON size: ${formatBytes(historyStore.exportJson().toByteArray(Charsets.UTF_8).size.toLong())}\n")
            append("Pending JSON size: ${formatBytes(pendingJobStore.exportJson().toByteArray(Charsets.UTF_8).size.toLong())}\n\n")

            append("PRIVACY\n")
            append("Diagnostics does not contain OAuth access token, Google password or signing keys.\n")
            append("Email and Channel ID are masked.")
        }
    }

    private fun openGoogleCloudQuota() {
        val url =
            "https://console.cloud.google.com/apis/api/" +
                "youtube.googleapis.com/quotas"
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            toast("Не вдалося відкрити Google Cloud Console")
        }
    }

    private fun exportTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(timestamp))

    private fun formatNullableDate(timestamp: Long?): String =
        if (timestamp == null || timestamp <= 0L) "—" else formatDate(timestamp)

    private fun formatBytes(bytes: Long): String =
        when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): android.graphics.drawable.Drawable {
        val palette =
            AppThemeManager.palette(this)

        val mappedFill =
            when (color) {
                Color.rgb(15, 16, 19) ->
                    palette.background

                Color.rgb(25, 27, 32) ->
                    palette.surface

                Color.rgb(31, 33, 39),
                Color.rgb(37, 39, 46) ->
                    palette.surfaceAlt

                Color.rgb(196, 0, 42) ->
                    palette.accentFill

                Color.rgb(39, 25, 27),
                Color.rgb(31, 29, 24) ->
                    palette.surface

                else ->
                    color
            }

        val accentOverride =
            when (strokeColor) {
                Color.rgb(95, 48, 52) ->
                    palette.danger

                Color.rgb(83, 68, 37) ->
                    palette.warning

                else ->
                    null
            }

        val useAccentStroke =
            radiusDp >= 14 ||
                color == Color.rgb(196, 0, 42) ||
                accentOverride != null

        return AppThemeManager.surfaceDrawable(
            context = this,
            fill = mappedFill,
            radiusDp = radiusDp,
            accentStroke = useAccentStroke,
            accentOverride = accentOverride
        )
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private data class ReleaseNote(
        val title: String,
        val body: String
    )

    private enum class Page {
        HOME,
        QUICK_START,
        PRIVACY,
        DIAGNOSTICS,
        SEARCH_CACHE,
        ABOUT,
        CHANGELOG
    }

    companion object {
        private const val CHANGELOG_ASSET =
            "CHANGELOG.md"

        const val EXTRA_GOOGLE_CONNECTED = "service_google_connected"
        const val EXTRA_GOOGLE_EMAIL = "service_google_email"
        const val EXTRA_CHANNEL_TITLE = "service_channel_title"
        const val EXTRA_CHANNEL_ID = "service_channel_id"

        private const val KEY_PAGE = "service_page"
        private const val KEY_CHANGELOG_SCROLL_Y =
            "service_changelog_scroll_y"

        private val BACKGROUND = Color.rgb(15, 16, 19)
        private val SURFACE = Color.rgb(25, 27, 32)
        private val ROW_SURFACE = Color.rgb(31, 33, 39)
        private val BORDER = Color.rgb(48, 51, 59)
        private val MUTED = Color.rgb(165, 167, 173)
    }
}
