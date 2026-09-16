package com.saney.ytmimporter

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.model.GoogleAccountInfo
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.PendingTrack
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.model.YouTubeChannelInfo
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.parser.PlaylistParser
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.LocalBackupManager
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import com.saney.ytmimporter.storage.PlaylistProjectImport
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.TrackAdapter
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.util.ErrorMessages
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private data class DuplicateAnalysis(
        val alreadyInPlaylist: List<Track>,
        val repeatedInImport: List<Track>,
        val tracksToAdd: List<Track>
    ) {
        val tracksToSkip: List<Track>
            get() = alreadyInPlaylist + repeatedInImport

        val totalDuplicates: Int
            get() = tracksToSkip.size
    }

    private data class DuplicateWritePlan(
        val tracksToWrite: List<Track>,
        val tracksToSkip: List<Track>,
        val alreadyInPlaylistCount: Int,
        val repeatedInImportCount: Int,
        val scanRequestCount: Int,
        val scanSucceeded: Boolean,
        val addDuplicatesAnyway: Boolean
    ) {
        val duplicatesFound: Int
            get() = alreadyInPlaylistCount + repeatedInImportCount

        val savedWriteUnits: Int
            get() =
                tracksToSkip.size *
                    QuotaTracker.PLAYLIST_ITEM_INSERT_COST
    }

    private val fileRequestCode = 1001
    private val saveExportRequestCode = 1101
    private val restoreBackupRequestCode = 1102
    private val pendingQueueRequestCode = 1201
    private val importScreenRequestCode = 1301
    private val reviewScreenRequestCode = 1302
    private val destinationScreenRequestCode = 1401
    private val authRequestCode = 9001
    private val executor = Executors.newSingleThreadExecutor()
    private val api = YouTubeApi()

    private lateinit var searchCache: SearchCache
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var pendingJobStore: PendingJobStore
    private lateinit var historyStore: HistoryStore
    private lateinit var localBackupManager: LocalBackupManager
    private lateinit var currentPlaylistStore: CurrentPlaylistStore

    private var playlist: ImportedPlaylist? = null
    private var accessToken: String? = null
    private var googleAccountInfo: GoogleAccountInfo? = null
    private var youtubeChannelInfo: YouTubeChannelInfo? = null
    private var createdPlaylistId: String? = null
    private var pendingAfterAuth: (() -> Unit)? = null
    private var currentImportSourceLabel: String = "Невідоме джерело"
    private var pendingExportContent: String? = null
    private var pendingExportSuccessMessage: String? = null
    private var destinationPlaylists: List<YouTubePlaylistInfo> = emptyList()
    private var pendingDestinationTarget: YouTubePlaylistInfo? = null
    private var pendingDestinationAnalysis: DuplicateAnalysis? = null
    private var pendingDestinationScanRequestCount: Int = 0

    private lateinit var statusText: TextView
    private lateinit var summaryText: TextView
    private lateinit var importButton: Button
    private lateinit var accountButton: Button
    private lateinit var searchButton: Button
    private lateinit var createButton: Button
    private lateinit var quotaButton: Button
    private lateinit var pendingButton: Button
    private lateinit var progress: ProgressBar

    private val uiPrefs by lazy {
        getSharedPreferences("ui_prefs_v1", MODE_PRIVATE)
    }
    private lateinit var resultPanel: LinearLayout
    private lateinit var resultTitleText: TextView
    private lateinit var resultDetailsText: TextView
    private lateinit var resultLinkText: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: TrackAdapter
    private val visibleTracks = mutableListOf<Track>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchCache = SearchCache(this)
        quotaTracker = QuotaTracker(this)
        pendingJobStore = PendingJobStore(this)
        historyStore = HistoryStore(this)
        localBackupManager = LocalBackupManager(this)
        currentPlaylistStore = CurrentPlaylistStore(this)

        restoreAuthSessionFromMemory()
        buildUi()
        updateAccountPanel()
        restoreCurrentWorkspaceOnLaunch()

        val restoredToken = accessToken
        if (
            !restoredToken.isNullOrBlank() &&
            (googleAccountInfo == null || youtubeChannelInfo == null)
        ) {
            loadAccountIdentity(
                token = restoredToken,
                after = null
            )
        }

        if (savedInstanceState == null) {
            window.decorView.post {
                maybeShowWelcome()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            reloadCurrentWorkspace()
        }

        if (::pendingButton.isInitialized) {
            updatePendingButton()
        }

        if (::quotaButton.isInitialized) {
            updateQuotaPanel()
        }

        updatePrimaryActions()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 16, 19))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(10))
        }

        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        titleBlock.addView(
            TextView(this).apply {
                text = "YTM Importer"
                textSize = 23f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }
        )

        titleBlock.addView(
            TextView(this).apply {
                text = "Імпорт трекліста → YouTube Music"
                textSize = 12.5f
                setTextColor(Color.rgb(165, 167, 173))
                setPadding(0, dp(2), 0, 0)
            }
        )

        header.addView(
            titleBlock,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        header.addView(
            TextView(this).apply {
                text = "v${BuildConfig.VERSION_NAME}"
                textSize = 11.5f
                setTextColor(Color.rgb(220, 222, 228))
                gravity = android.view.Gravity.CENTER
                setPadding(dp(10), dp(6), dp(10), dp(6))
                background =
                    roundedBackground(
                        color = Color.rgb(31, 33, 39),
                        radiusDp = 18,
                        strokeColor = Color.rgb(55, 58, 66)
                    )
            }
        )

        root.addView(header)

        val flowCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background =
                roundedBackground(
                    color = Color.rgb(25, 27, 32),
                    radiusDp = 16,
                    strokeColor = Color.rgb(48, 51, 59)
                )
        }

        flowCard.addView(
            TextView(this).apply {
                text = "4 кроки до плейлиста"
                textSize = 12f
                setTextColor(Color.rgb(165, 167, 173))
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(2), 0, 0, dp(8))
            }
        )

        importButton =
            button("1. Імпорт") {
                openImportScreen()
            }

        accountButton =
            button("2. Google / YTM") {
                showAccountDialog()
            }

        searchButton =
            primaryButton("3. Знайти / перевірити") {
                searchOrReview()
            }.apply {
                isEnabled = false
                alpha = 0.55f
            }

        createButton =
            primaryButton("4. Створити / додати") {
                createPlaylist()
            }.apply {
                isEnabled = false
                alpha = 0.55f
            }

        flowCard.addView(
            equalButtonsRow(
                importButton,
                accountButton
            )
        )

        flowCard.addView(
            equalButtonsRow(
                searchButton,
                createButton
            ).apply {
                setPadding(0, dp(8), 0, 0)
            }
        )

        root.addView(
            flowCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(12), 0, dp(12), dp(8))
            }
        )

        val utilityRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), 0, dp(12), dp(8))
        }

        val historyButton =
            compactButton("Історія") {
                showHistory()
            }

        pendingButton =
            compactButton("Черга") {
                showPendingJobs()
            }

        quotaButton =
            compactButton("Квота") {
                showQuotaDialog()
            }

        val moreButton =
            compactButton("Ще") {
                showMoreActions()
            }

        listOf(
            historyButton,
            pendingButton,
            quotaButton,
            moreButton
        ).forEachIndexed { index, item ->
            utilityRow.addView(
                item,
                LinearLayout.LayoutParams(
                    0,
                    dp(50),
                    1f
                ).apply {
                    if (index > 0) {
                        marginStart = dp(6)
                    }
                }
            )
        }

        root.addView(utilityRow)

        summaryText = TextView(this).apply {
            setPadding(dp(16), dp(5), dp(16), 0)
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            text = "Плейлист ще не імпортовано"
        }
        root.addView(summaryText)

        statusText = TextView(this).apply {
            setPadding(dp(16), dp(3), dp(16), dp(7))
            setTextColor(Color.rgb(165, 167, 173))
            textSize = 13f
            text = "Почніть з «1. Імпорт»."
        }
        root.addView(statusText)

        progress =
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                max = 100
                progress = 0
                visibility = View.GONE
            }

        root.addView(
            progress,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(4)
            )
        )

        resultPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background =
                roundedBackground(
                    color = Color.rgb(27, 29, 34),
                    radiusDp = 14,
                    strokeColor = Color.rgb(52, 55, 63)
                )
        }

        resultTitleText = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        resultPanel.addView(resultTitleText)

        resultDetailsText = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(190, 192, 198))
            setPadding(0, dp(5), 0, dp(6))
        }
        resultPanel.addView(resultDetailsText)

        resultLinkText = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.rgb(140, 185, 255))
            setTextIsSelectable(true)
            setPadding(0, 0, 0, dp(10))
        }
        resultPanel.addView(resultLinkText)

        val resultActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val openResultButton =
            button("Відкрити в YTM") {
                openInYtm()
            }

        val copyResultButton =
            button("Копіювати посилання") {
                copyPlaylistLink()
            }

        resultActions.addView(
            openResultButton,
            LinearLayout.LayoutParams(
                0,
                dp(42),
                1f
            )
        )

        resultActions.addView(
            copyResultButton,
            LinearLayout.LayoutParams(
                0,
                dp(42),
                1f
            ).apply {
                marginStart = dp(8)
            }
        )

        resultPanel.addView(resultActions)

        root.addView(
            resultPanel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(12), dp(4), dp(12), dp(8))
            }
        )

        listView = ListView(this).apply {
            divider = null
            dividerHeight = dp(1)
            setBackgroundColor(Color.rgb(15, 16, 19))
            clipToPadding = false
            setPadding(dp(8), 0, dp(8), dp(12))
        }

        adapter =
            TrackAdapter(
                this,
                visibleTracks
            )

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            openReviewScreen(
                visibleTracks[position]
                    .historyIndex
            )
        }

        root.addView(
            listView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)

        updateQuotaPanel()
        updatePendingButton()
        updatePrimaryActions()
    }

    private fun button(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(14), dp(10), dp(14), dp(10))
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 15
            )
            background =
                roundedBackground(
                    color = Color.rgb(34, 36, 42),
                    radiusDp = 12,
                    strokeColor = Color.rgb(58, 61, 70)
                )
            setOnClickListener {
                action()
            }
        }

    private fun primaryButton(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(14), dp(10), dp(14), dp(10))
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 15
            )
            background =
                roundedBackground(
                    color = Color.rgb(196, 0, 42),
                    radiusDp = 12
                )
            setOnClickListener {
                action()
            }
        }

    private fun compactButton(
        label: String,
        action: () -> Unit
    ): Button =
        button(
            label = label,
            action = action
        ).apply {
            textSize = 12f
            setPadding(dp(9), dp(7), dp(9), dp(7))
            UiChrome.autoSizeButton(
                this,
                minSp = 10,
                maxSp = 13
            )
        }

    private fun equalButtonsRow(
        first: Button,
        second: Button
    ): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL

            addView(
                first,
                LinearLayout.LayoutParams(
                    0,
                    dp(70),
                    1f
                )
            )

            addView(
                second,
                LinearLayout.LayoutParams(
                    0,
                    dp(70),
                    1f
                ).apply {
                    marginStart = dp(8)
                }
            )
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(color)

            if (strokeColor != null) {
                setStroke(
                    dp(1),
                    strokeColor
                )
            }
        }

    private fun updatePrimaryActions() {
        if (!::importButton.isInitialized ||
            !::accountButton.isInitialized ||
            !::searchButton.isInitialized ||
            !::createButton.isInitialized
        ) {
            return
        }

        val current = playlist
        val hasPlaylist =
            current != null &&
                current.tracks.isNotEmpty()

        val hasSelectedVideo =
            current
                ?.tracks
                .orEmpty()
                .any {
                    !it.selectedVideoId.isNullOrBlank() &&
                        it.status != TrackStatus.SKIPPED
                }

        val needsSearch =
            current
                ?.tracks
                .orEmpty()
                .any {
                    it.status in
                        setOf(
                            TrackStatus.NEW,
                            TrackStatus.SEARCHING
                        )
                }

        val needsAttention =
            current
                ?.tracks
                .orEmpty()
                .any {
                    it.status in
                        setOf(
                            TrackStatus.REVIEW,
                            TrackStatus.MISSING,
                            TrackStatus.FAILED
                        )
                }

        applyStepState(
            button = importButton,
            state =
                if (hasPlaylist) {
                    StepState.READY
                } else {
                    StepState.REQUIRED
                }
        )

        applyStepState(
            button = accountButton,
            state =
                when {
                    accessToken.isNullOrBlank() ->
                        StepState.REQUIRED

                    youtubeChannelInfo != null ->
                        StepState.READY

                    else ->
                        StepState.ATTENTION
                }
        )

        searchButton.isEnabled = hasPlaylist
        applyStepState(
            button = searchButton,
            state =
                when {
                    !hasPlaylist || needsSearch ->
                        StepState.REQUIRED

                    needsAttention ->
                        StepState.ATTENTION

                    else ->
                        StepState.READY
                },
            enabled = hasPlaylist
        )

        createButton.isEnabled = hasSelectedVideo
        applyStepState(
            button = createButton,
            state =
                when {
                    !hasSelectedVideo ->
                        StepState.REQUIRED

                    needsSearch || needsAttention ->
                        StepState.ATTENTION

                    else ->
                        StepState.READY
                },
            enabled = hasSelectedVideo
        )
    }

    private fun applyStepState(
        button: Button,
        state: StepState,
        enabled: Boolean = true
    ) {
        val color =
            when (state) {
                StepState.READY ->
                    Color.rgb(31, 122, 77)

                StepState.ATTENTION ->
                    Color.rgb(157, 105, 15)

                StepState.REQUIRED ->
                    Color.rgb(176, 0, 32)
            }

        button.background =
            roundedBackground(
                color = color,
                radiusDp = 12
            )

        button.alpha =
            if (enabled) {
                1f
            } else {
                0.72f
            }
    }

    private enum class StepState {
        READY,
        ATTENTION,
        REQUIRED
    }

    private fun openImportScreen() {
        startActivityForResult(
            Intent(
                this,
                ImportActivity::class.java
            ),
            importScreenRequestCode
        )
    }

    private fun openReviewScreen(
        focusHistoryIndex: Int? = null
    ) {
        val current =
            playlist
                ?: return toast(
                    "Спочатку імпортуйте список треків"
                )

        currentPlaylistStore.save(
            playlist = current,
            sourceLabel =
                currentImportSourceLabel
        )

        val intent =
            Intent(
                this,
                ReviewActivity::class.java
            )

        focusHistoryIndex
            ?.let {
                intent.putExtra(
                    ReviewActivity
                        .EXTRA_FOCUS_HISTORY_INDEX,
                    it
                )
            }

        startActivityForResult(
            intent,
            reviewScreenRequestCode
        )
    }

    private fun searchOrReview() {
        val current =
            playlist
                ?: return toast(
                    "Спочатку імпортуйте список треків"
                )

        val needsInitialSearch =
            current.tracks.any { track ->
                track.status ==
                    TrackStatus.NEW &&
                    track.selectedVideoId
                        .isNullOrBlank()
            }

        if (needsInitialSearch) {
            searchAll(
                openReviewAfter = true,
                preserveExistingExact = true
            )
        } else {
            openReviewScreen()
        }
    }

    private fun restoreCurrentWorkspaceOnLaunch() {
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return

        playlist =
            snapshot.playlist
        currentImportSourceLabel =
            snapshot.sourceLabel

        visibleTracks.clear()
        visibleTracks.addAll(
            snapshot.playlist.tracks
        )

        adapter.notifyDataSetChanged()
        updateSummary()

        status(
            "Відновлено робочий список: " +
                "${snapshot.playlist.name} • " +
                "${snapshot.playlist.tracks.size} треків."
        )
    }

    private fun reloadCurrentWorkspace(
        force: Boolean = false
    ) {
        val snapshot =
            currentPlaylistStore
                .load()
                ?: return

        val current =
            playlist

        if (
            !force &&
            current != null &&
            current.tracks.any {
                it.status ==
                    TrackStatus.SEARCHING
            }
        ) {
            return
        }

        playlist =
            snapshot.playlist
        currentImportSourceLabel =
            snapshot.sourceLabel

        visibleTracks.clear()
        visibleTracks.addAll(
            snapshot.playlist.tracks
        )

        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun persistCurrentWorkspace() {
        val current =
            playlist
                ?: return

        currentPlaylistStore.save(
            playlist = current,
            sourceLabel =
                currentImportSourceLabel
        )
    }

    private fun clearCurrentWorkspaceInMemory() {
        playlist = null
        currentImportSourceLabel =
            "Невідоме джерело"

        visibleTracks.clear()
        adapter.notifyDataSetChanged()

        summaryText.text =
            "Плейлист ще не імпортовано"

        updatePrimaryActions()
    }

    private fun showImportMenu() {
        UiChrome.showMenuDialog(
            activity = this,
            title = "Імпорт трекліста",
            subtitle = "Оберіть джерело для нового списку.",
            negativeLabel = "Скасувати",
            actions = listOf(
                UiChrome.MenuAction(
                    "Файл — CSV / TXT / YTM Project"
                ) {
                    chooseFile()
                },
                UiChrome.MenuAction(
                    "Вставити текст — Artist - Track"
                ) {
                    showPasteTrackListDialog()
                }
            )
        )
    }

    private fun showMoreActions() {
        UiChrome.showMenuDialog(
            activity = this,
            title = "Ще",
            subtitle = "Додаткові дії та сервісні інструменти.",
            actions = listOf(
                UiChrome.MenuAction(
                    "Поточний проект — review / save / share"
                ) {
                    openReviewScreen()
                },
                UiChrome.MenuAction(
                    "Заміни — перевірити ручні заміни"
                ) {
                    showReplacementLog()
                },
                UiChrome.MenuAction(
                    "Відкрити останній плейлист у YTM"
                ) {
                    openInYtm()
                },
                UiChrome.MenuAction(
                    "Дані — export / backup / restore"
                ) {
                    startActivity(
                        Intent(
                            this,
                            DataActivity::class.java
                        )
                    )
                },
                UiChrome.MenuAction(
                    "Сервіс — допомога / diagnostics / cache"
                ) {
                    showServiceTools()
                }
            )
        )
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

    @Deprecated("Deprecated in Android API but kept for a minimal dependency-free Activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return

        when (requestCode) {
            fileRequestCode ->
                data.data?.let(::loadFile)

            saveExportRequestCode ->
                data.data?.let(::writePendingExport)

            restoreBackupRequestCode ->
                data.data?.let(::prepareRestoreBackup)

            pendingQueueRequestCode -> {
                updatePendingButton()

                val jobId =
                    data.getStringExtra(
                        PendingActivity.EXTRA_RESUME_JOB_ID
                    )

                if (!jobId.isNullOrBlank()) {
                    val job =
                        pendingJobStore.get(
                            jobId
                        )

                    if (job == null) {
                        toast(
                            "Завдання вже відсутнє в черзі"
                        )
                    } else {
                        resumePendingJob(job)
                    }
                }
            }

            importScreenRequestCode -> {
                if (
                    data.getBooleanExtra(
                        ImportActivity.EXTRA_CLEAR_WORKSPACE,
                        false
                    )
                ) {
                    clearCurrentWorkspaceInMemory()
                    status(
                        "Поточний робочий список очищено. " +
                            "Виберіть новий файл або YTM Project."
                    )
                    return
                }

                reloadCurrentWorkspace(
                    force = true
                )

                val message =
                    data.getStringExtra(
                        ImportActivity.EXTRA_IMPORT_MESSAGE
                    )

                if (!message.isNullOrBlank()) {
                    status(
                        message +
                            " Крок 3: «Знайти / перевірити»."
                    )
                }
            }

            reviewScreenRequestCode -> {
                reloadCurrentWorkspace(
                    force = true
                )

                if (
                    data.getBooleanExtra(
                        ReviewActivity.EXTRA_REPEAT_SEARCH,
                        false
                    )
                ) {
                    searchAll(
                        openReviewAfter = true
                    )
                    return
                }

                val videoId =
                    data.getStringExtra(
                        ReviewActivity.EXTRA_MANUAL_VIDEO_ID
                    )

                val historyIndex =
                    data.getIntExtra(
                        ReviewActivity.EXTRA_MANUAL_HISTORY_INDEX,
                        Int.MIN_VALUE
                    )

                if (
                    !videoId.isNullOrBlank() &&
                    historyIndex != Int.MIN_VALUE
                ) {
                    val track =
                        playlist
                            ?.tracks
                            ?.firstOrNull {
                                it.historyIndex ==
                                    historyIndex
                            }

                    if (track == null) {
                        toast(
                            "Не вдалося знайти трек для ручної заміни"
                        )
                    } else {
                        applyManualUrl(
                            track = track,
                            videoId = videoId,
                            reopenReviewHistoryIndex =
                                historyIndex
                        )
                    }
                }
            }

            destinationScreenRequestCode -> {
                handleDestinationResult(data)
            }

            authRequestCode -> {
                try {
                    val result =
                        Identity.getAuthorizationClient(this)
                            .getAuthorizationResultFromIntent(data)
                    val token = result.accessToken
                    if (token.isNullOrBlank()) {
                        toast("Google не повернув access token")
                    } else {
                        handleAuthorizedToken(token)
                    }
                } catch (e: ApiException) {
                    toast("Авторизація не вдалася: ${e.statusCode}")
                }
            }
        }
    }

    private fun loadFile(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }

        val fileName =
            queryFileName(uri)
                ?: "playlist.csv"

        val text =
            contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: return toast(
                    "Не вдалося прочитати файл"
                )

        if (PlaylistProjectCodec.isProject(text)) {
            runCatching {
                PlaylistProjectCodec.importProject(text)
            }.onSuccess { project ->
                applyImportedProject(
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
        }.onSuccess {
            applyImportedPlaylist(
                imported = it,
                sourceLabel = "Файл ($fileName)"
            )
        }.onFailure {
            toast(
                it.message
                    ?: "Помилка імпорту"
            )
        }
    }

    private fun showPasteTrackListDialog() {
        val playlistNameInput = EditText(this).apply {
            hint = "Назва плейлиста (необов’язково)"
            setSingleLine(true)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val tracksInput = EditText(this).apply {
            hint =
                "Solarstone & JES - Like a Waterfall\n" +
                    "Sultan & Tone Depth - Moments\n" +
                    "Ahmet Ertenu - Why"
            minLines = 9
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(4), dp(18), 0)
            addView(
                playlistNameInput,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                tracksInput,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            )
        }

        val dialog = UiChrome.alertBuilder(this)
            .setTitle("Вставити список треків")
            .setMessage(
                "Один трек на рядок: Artist - Track. " +
                    "Підтримуються також – та — і нумерація 1. / 2)."
            )
            .setView(container)
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Імпортувати", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val rawText = tracksInput.text.toString()
                if (rawText.isBlank()) {
                    tracksInput.error = "Вставте хоча б один трек"
                    return@setOnClickListener
                }

                runCatching {
                    PlaylistParser.parse("Вставлений список.txt", rawText).also { imported ->
                        playlistNameInput.text.toString().trim()
                            .takeIf { it.isNotBlank() }
                            ?.let { imported.name = it }
                    }
                }.onSuccess { imported ->
                    applyImportedPlaylist(
                        imported = imported,
                        sourceLabel = "Текст"
                    )
                    dialog.dismiss()
                }.onFailure { error ->
                    tracksInput.error = error.message ?: "Не вдалося розібрати список"
                }
            }
        }

        dialog.show()
    }

    private fun applyImportedPlaylist(
        imported: ImportedPlaylist,
        sourceLabel: String
    ) {
        imported.tracks.forEachIndexed { index, track ->
            track.historyIndex = index
        }

        playlist = imported
        currentImportSourceLabel = sourceLabel
        createdPlaylistId = null
        resultPanel.visibility = View.GONE
        visibleTracks.clear()
        visibleTracks.addAll(imported.tracks)
        adapter.notifyDataSetChanged()
        updateSummary()
        status(
            "$sourceLabel імпортовано: ${imported.tracks.size} треків. " +
                "Крок 3: натисніть «Знайти / перевірити». Відомі треки будуть взяті з кешу."
        )
    }


    private fun applyImportedProject(
        project: PlaylistProjectImport,
        fileName: String
    ) {
        applyImportedPlaylist(
            imported = project.playlist,
            sourceLabel = "YTM Project ($fileName)"
        )

        val scopeNote =
            if (
                project.sourceDestination ==
                    PendingDestination.EXISTING_PLAYLIST
            ) {
                " Це збережений import batch (пакет імпорту), " +
                    "а не повна копія старого існуючого плейлиста."
            } else {
                ""
            }

        status(
            "YTM Project завантажено: " +
                "${project.playlist.tracks.size} треків. " +
                "Точних YouTube videoId відновлено: " +
                "${project.exactSelectionCount}. " +
                "Без videoId: ${project.unresolvedCount}." +
                scopeNote +
                " Якщо всі потрібні videoId вже є, можна одразу " +
                "натиснути «4. Створити / додати» без нового пошуку."
        )
    }

    private fun queryFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) return cursor.getString(index)
        }
        return uri.lastPathSegment
    }

    private fun showAccountDialog() {
        if (accessToken.isNullOrBlank()) {
            authorize()
            return
        }

        val googleText =
            googleAccountInfo?.let {
                listOf(it.name, it.email)
                    .filter { value -> value.isNotBlank() }
                    .joinToString(" • ")
            }.orEmpty().ifBlank { "Google підключено, профіль не завантажено" }

        val youtubeText =
            youtubeChannelInfo?.let {
                "${it.title}\nChannel ID (ID каналу): ${it.id}"
            } ?: "YouTube/YTM канал не визначено"

        UiChrome.alertBuilder(this)
            .setTitle("Акаунт")
            .setMessage(
                "Google:\n$googleText\n\n" +
                    "YouTube / YouTube Music:\n$youtubeText\n\n" +
                    "Плейлисти записуються саме в цей YouTube/YTM профіль."
            )
            .setNegativeButton("Закрити", null)
            .setPositiveButton("Змінити акаунт") { _, _ ->
                authorize(after = null, forceAccountPicker = true)
            }
            .show()
    }

    private fun restoreAuthSessionFromMemory() {
        val snapshot = AuthSessionStore.current()
        accessToken = snapshot.accessToken
        googleAccountInfo = snapshot.googleAccountInfo
        youtubeChannelInfo = snapshot.youtubeChannelInfo
    }

    private fun syncAuthSessionToMemory() {
        AuthSessionStore.update(
            accessToken = accessToken,
            googleAccountInfo = googleAccountInfo,
            youtubeChannelInfo = youtubeChannelInfo
        )
    }

    private fun authorize(
        forceAccountPicker: Boolean = false,
        after: (() -> Unit)? = null
    ) {
        if (!forceAccountPicker && !accessToken.isNullOrBlank()) {
            if (googleAccountInfo == null || youtubeChannelInfo == null) {
                loadAccountIdentity(accessToken!!, after)
            } else {
                after?.invoke()
            }
            return
        }

        if (forceAccountPicker) {
            accessToken = null
            googleAccountInfo = null
            youtubeChannelInfo = null
            AuthSessionStore.clear()
            updateAccountPanel()
        }

        pendingAfterAuth = after
        status(
            if (forceAccountPicker) {
                "Виберіть Google акаунт…"
            } else {
                "Відкриваю доступ Google…"
            }
        )

        val builder =
            AuthorizationRequest.builder()
                .setRequestedScopes(
                    listOf(
                        Scope(YOUTUBE_SCOPE),
                        Scope(USERINFO_EMAIL_SCOPE),
                        Scope(USERINFO_PROFILE_SCOPE)
                    )
                )

        if (forceAccountPicker) {
            builder.setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
        }

        Identity.getAuthorizationClient(this)
            .authorize(builder.build())
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent!!.intentSender,
                            authRequestCode,
                            null,
                            0,
                            0,
                            0
                        )
                    } catch (e: Exception) {
                        pendingAfterAuth = null
                        toast(ErrorMessages.userMessage(e, "Не вдалося відкрити Google"))
                    }
                } else {
                    val token = result.accessToken
                    if (token.isNullOrBlank()) {
                        pendingAfterAuth = null
                        toast("Google не повернув access token")
                    } else {
                        handleAuthorizedToken(token)
                    }
                }
            }
            .addOnFailureListener { e ->
                pendingAfterAuth = null
                toast(ErrorMessages.userMessage(e, "Авторизація Google не вдалася"))
            }
    }

    private fun handleAuthorizedToken(token: String) {
        accessToken = token
        googleAccountInfo = null
        youtubeChannelInfo = null
        syncAuthSessionToMemory()
        updateAccountPanel()
        status("Google підключено. Завантажую дані акаунта і YouTube каналу…")

        val action = pendingAfterAuth
        pendingAfterAuth = null

        loadAccountIdentity(token, action)
    }

    private fun loadAccountIdentity(
        token: String,
        after: (() -> Unit)?
    ) {
        executor.execute {
            val googleResult =
                runCatching { api.getGoogleAccountInfo(token) }

            quotaTracker.recordGeneralUnits(QuotaTracker.SIMPLE_LIST_COST)
            val channelResult =
                runCatching { api.getMyYouTubeChannel(token) }

            runOnUiThread {
                googleAccountInfo = googleResult.getOrNull()
                youtubeChannelInfo = channelResult.getOrNull()
                syncAuthSessionToMemory()
                updateAccountPanel()
                updateQuotaPanel()

                val channel = youtubeChannelInfo
                status(
                    if (channel != null) {
                        "Підключено YouTube/YTM: ${channel.title}"
                    } else {
                        "Google підключено, але YouTube канал не вдалося визначити."
                    }
                )

                after?.invoke()
            }
        }
    }

    private fun updateAccountPanel() {
        if (!::accountButton.isInitialized) return

        accountButton.text =
            when {
                accessToken.isNullOrBlank() ->
                    "2. Google / YTM"

                youtubeChannelInfo != null ->
                    "2. Google / YTM ✓"

                else ->
                    "2. Google / YTM …"
            }

        updatePrimaryActions()
    }

    private fun searchAll(
        openReviewAfter: Boolean = false,
        preserveExistingExact: Boolean = false
    ) {
        val p = playlist ?: return toast("Спочатку імпортуйте список треків")

        val tracksToSearch =
            p.tracks.filter { track ->
                val manualExact =
                    track.manuallySelected &&
                        !track.selectedVideoId.isNullOrBlank()

                if (manualExact) {
                    false
                } else if (preserveExistingExact) {
                    track.selectedVideoId.isNullOrBlank() ||
                        track.status != TrackStatus.MATCHED ||
                        track.candidates.isNotEmpty()
                } else {
                    true
                }
            }

        val cachedCount =
            tracksToSearch.count {
                searchCache.get(it) != null
            }

        val apiNeeded =
            tracksToSearch.size -
                cachedCount
        val quota = quotaTracker.snapshot()

        val warning =
            if (apiNeeded > quota.searchRemaining) {
                "\n\n⚠ Локальна оцінка показує, що search quota " +
                    "(квоти пошуку) може не вистачити."
            } else {
                ""
            }

        UiChrome.alertBuilder(this)
            .setTitle("План пошуку (Search plan)")
            .setMessage(
                "Треків у списку: ${p.tracks.size}\n" +
                    "Пошук потрібен для: ${tracksToSearch.size}\n" +
                    "Вже є в кеші: $cachedCount\n" +
                    "Потрібно нових search.list: $apiNeeded\n\n" +
                    "Локально використано сьогодні: ${quota.searchCalls}/" +
                    "${QuotaTracker.SEARCH_DAILY_LIMIT}\n" +
                    "Локальна оцінка залишку: ${quota.searchRemaining}" +
                    warning +
                    "\n\nЦе не точний залишок Google Cloud. " +
                    "Інші пристрої або клієнти того самого API project " +
                    "(проєкту API) можуть теж витрачати квоту."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Почати") { _, _ ->
                startSearch(
                    p = p,
                    openReviewAfter =
                        openReviewAfter,
                    preserveExistingExact =
                        preserveExistingExact
                )
            }
            .show()
    }

    private fun startSearch(
        p: ImportedPlaylist,
        openReviewAfter: Boolean = false,
        preserveExistingExact: Boolean = false
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.max = p.tracks.size
            progress.progress = 0
            status("Пошук 0/${p.tracks.size} • кеш 0 • API 0")

            executor.execute {
                var cacheHits = 0
                var apiSearches = 0
                var quotaBlocked = false
                var quotaToastShown = false

                for ((index, track) in p.tracks.withIndex()) {
                    if (Thread.currentThread().isInterrupted) break

                    val keepManualSelection =
                        track.manuallySelected &&
                            !track.selectedVideoId.isNullOrBlank()

                    val keepExactSelection =
                        preserveExistingExact &&
                            !track.selectedVideoId.isNullOrBlank() &&
                            track.status == TrackStatus.MATCHED &&
                            track.candidates.isEmpty()

                    if (
                        keepManualSelection ||
                        keepExactSelection
                    ) {
                        runOnUiThread {
                            progress.progress = index + 1
                            status(
                                "Пошук ${index + 1}/${p.tracks.size} • " +
                                    if (keepManualSelection) {
                                        "ручний вибір збережено"
                                    } else {
                                        "точний videoId з Project збережено"
                                    }
                            )
                            adapter.notifyDataSetChanged()
                            updateSummary()
                            updateQuotaPanel()
                        }
                        continue
                    }

                    track.status = TrackStatus.SEARCHING
                    track.error = null
                    refreshRow(index)

                    try {
                        val cachedCandidates = searchCache.get(track)

                        val candidates =
                            if (cachedCandidates != null) {
                                cacheHits += 1
                                quotaTracker.recordCacheHit()
                                cachedCandidates
                            } else if (quotaBlocked) {
                                track.status = TrackStatus.FAILED
                                track.error =
                                    "Немає в кеші, а квота YouTube search API вже закінчилась"
                                emptyList()
                            } else {
                                apiSearches += 1
                                quotaTracker.recordSearchCall()
                                val freshCandidates = api.search(token, track)
                                searchCache.put(track, freshCandidates)
                                freshCandidates
                            }

                        if (track.status != TrackStatus.FAILED) {
                            applySearchCandidates(track, candidates)
                        }
                    } catch (e: Exception) {
                        track.status = TrackStatus.FAILED
                        track.error =
                            ErrorMessages.userMessage(
                                e,
                                "Не вдалося виконати пошук"
                            )

                        if (isQuotaError(e)) {
                            quotaBlocked = true
                            quotaTracker.recordQuotaError(
                                e.message ?: "Search quota exceeded"
                            )

                            if (!quotaToastShown) {
                                quotaToastShown = true
                                runOnUiThread {
                                    toast(
                                        "Закінчилась квота пошуку YouTube API. " +
                                            "Треки, які вже є в кеші, програма ще обробить."
                                    )
                                }
                            }
                        }
                    }

                    runOnUiThread {
                        progress.progress = index + 1
                        status(
                            "Пошук ${index + 1}/${p.tracks.size} • " +
                                "кеш $cacheHits • API $apiSearches"
                        )
                        adapter.notifyDataSetChanged()
                        updateSummary()
                        updateQuotaPanel()
                    }
                }

                runOnUiThread {
                    progress.visibility = View.GONE

                    status(
                        if (quotaBlocked) {
                            "Готово: з кешу $cacheHits, API-запитів $apiSearches. " +
                                "Квота закінчилась; некешовані треки залишились без пошуку."
                        } else {
                            "Пошук завершено: з кешу $cacheHits, " +
                                "нових API-пошуків $apiSearches. " +
                                "Жовті треки краще перевірити натисканням."
                        }
                    )

                    updateSummary()
                    updateQuotaPanel()

                    if (openReviewAfter) {
                        openReviewScreen()
                    }
                }
            }
        }
    }

    private fun applySearchCandidates(
        track: Track,
        candidates: List<SearchCandidate>
    ) {
        track.candidates = candidates

        if (
            track.manuallySelected &&
            !track.selectedVideoId.isNullOrBlank()
        ) {
            track.status = TrackStatus.MATCHED
            track.error = null
            return
        }

        val best = candidates.firstOrNull()
        if (best == null) {
            track.status = TrackStatus.MISSING
            track.selectedVideoId = null
            track.selectedTitle = null
            track.selectedChannel = null
            return
        }

        applyCandidate(track, best, manual = false)

        track.status =
            when {
                best.score >= 0.72 -> TrackStatus.MATCHED
                else -> TrackStatus.REVIEW
            }
    }

    private fun createPlaylist() {
        val p =
            playlist
                ?: return toast(
                    "Спочатку імпортуйте список треків"
                )

        val selected =
            currentTracksForDestination(p)

        if (selected.isEmpty()) {
            return toast(
                "Спочатку знайдіть або виберіть треки"
            )
        }

        destinationPlaylists = emptyList()
        pendingDestinationTarget = null
        pendingDestinationAnalysis = null
        pendingDestinationScanRequestCount = 0

        openDestinationStart(
            p = p,
            selected = selected
        )
    }

    private fun currentTracksForDestination(
        p: ImportedPlaylist
    ): List<Track> =
        p.tracks.filter { track ->
            !track.selectedVideoId.isNullOrBlank() &&
                track.status != TrackStatus.SKIPPED
        }

    private fun openDestinationStart(
        p: ImportedPlaylist,
        selected: List<Track>
    ) {
        val intent =
            destinationBaseIntent(
                p = p,
                selected = selected
            ).apply {
                putExtra(
                    DestinationActivity.EXTRA_MODE,
                    DestinationActivity.MODE_START
                )
                putExtra(
                    DestinationActivity.EXTRA_NEW_QUOTA_PLAN,
                    quotaPlanForWrite(
                        trackCount = selected.size,
                        createPlaylist = true
                    )
                )
            }

        startActivityForResult(
            intent,
            destinationScreenRequestCode
        )
    }

    private fun destinationBaseIntent(
        p: ImportedPlaylist,
        selected: List<Track>
    ): Intent {
        val questionable =
            selected.count {
                it.status == TrackStatus.REVIEW
            }

        val googleLabel =
            googleAccountInfo
                ?.email
                ?.takeIf { it.isNotBlank() }
                ?: "буде перевірено перед записом"

        val channelLabel =
            youtubeChannelInfo
                ?.title
                ?.takeIf { it.isNotBlank() }
                ?: "буде перевірено перед записом"

        return Intent(
            this,
            DestinationActivity::class.java
        ).apply {
            putExtra(
                DestinationActivity.EXTRA_PLAYLIST_NAME,
                p.name
            )
            putExtra(
                DestinationActivity.EXTRA_IMPORTED_COUNT,
                p.tracks.size
            )
            putExtra(
                DestinationActivity.EXTRA_SELECTED_COUNT,
                selected.size
            )
            putExtra(
                DestinationActivity.EXTRA_QUESTIONABLE_COUNT,
                questionable
            )
            putExtra(
                DestinationActivity.EXTRA_GOOGLE_LABEL,
                googleLabel
            )
            putExtra(
                DestinationActivity.EXTRA_CHANNEL_LABEL,
                channelLabel
            )
        }
    }

    private fun handleDestinationResult(
        data: Intent
    ) {
        val p =
            playlist
                ?: return toast(
                    "Поточний список уже недоступний"
                )

        val selected =
            currentTracksForDestination(p)

        if (selected.isEmpty()) {
            return toast(
                "Немає треків для запису"
            )
        }

        when (
            data.getStringExtra(
                DestinationActivity.EXTRA_ACTION
            )
        ) {
            DestinationActivity.ACTION_CREATE_NEW -> {
                val privacy =
                    data.getStringExtra(
                        DestinationActivity.EXTRA_PRIVACY
                    ) ?: "private"

                actuallyCreatePlaylist(
                    p = p,
                    selected = selected,
                    privacyStatus = privacy
                )
            }

            DestinationActivity.ACTION_LOAD_EXISTING -> {
                loadExistingPlaylistsForDestination(
                    p = p,
                    selected = selected
                )
            }

            DestinationActivity.ACTION_BACK_TO_START -> {
                openDestinationStart(
                    p = p,
                    selected = selected
                )
            }

            DestinationActivity.ACTION_BACK_TO_EXISTING_LIST -> {
                if (destinationPlaylists.isEmpty()) {
                    loadExistingPlaylistsForDestination(
                        p = p,
                        selected = selected
                    )
                } else {
                    openDestinationExistingList(
                        p = p,
                        selected = selected,
                        playlists = destinationPlaylists
                    )
                }
            }

            DestinationActivity.ACTION_SELECT_EXISTING -> {
                val id =
                    data.getStringExtra(
                        DestinationActivity.EXTRA_TARGET_ID
                    ).orEmpty()

                if (id.isBlank()) {
                    return toast(
                        "Не вдалося визначити вибраний плейлист"
                    )
                }

                val target =
                    destinationPlaylists
                        .firstOrNull {
                            it.id == id
                        }
                        ?: YouTubePlaylistInfo(
                            id = id,
                            title =
                                data.getStringExtra(
                                    DestinationActivity.EXTRA_TARGET_TITLE
                                ) ?: "Плейлист",
                            privacyStatus =
                                data.getStringExtra(
                                    DestinationActivity.EXTRA_TARGET_PRIVACY
                                ) ?: "private",
                            itemCount =
                                data.getLongExtra(
                                    DestinationActivity.EXTRA_TARGET_COUNT,
                                    0L
                                )
                        )

                pendingDestinationTarget = target

                checkDuplicatesForDestination(
                    p = p,
                    selected = selected,
                    target = target
                )
            }

            DestinationActivity.ACTION_CONFIRM_EXISTING -> {
                finishExistingDestination(
                    p = p,
                    selected = selected,
                    duplicateMode =
                        data.getStringExtra(
                            DestinationActivity.EXTRA_DUPLICATE_MODE
                        ) ?: DestinationActivity.DUPLICATE_MODE_SKIP
                )
            }
        }
    }

    private fun loadExistingPlaylistsForDestination(
        p: ImportedPlaylist,
        selected: List<Track>
    ) {
        authorize {
            val token =
                accessToken
                    ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.isIndeterminate = true
            status(
                "Завантажую ваші існуючі плейлисти…"
            )

            executor.execute {
                quotaTracker.recordGeneralUnits(
                    QuotaTracker.SIMPLE_LIST_COST
                )

                val result =
                    runCatching {
                        api.listMyPlaylists(token)
                    }

                runOnUiThread {
                    progress.isIndeterminate = false
                    progress.visibility = View.GONE
                    updateQuotaPanel()

                    result.onSuccess { playlists ->
                        destinationPlaylists = playlists

                        if (playlists.isEmpty()) {
                            toast(
                                "У цьому YouTube/YTM профілі немає доступних плейлистів."
                            )
                        } else {
                            openDestinationExistingList(
                                p = p,
                                selected = selected,
                                playlists = playlists
                            )
                        }
                    }.onFailure { error ->
                        toast(
                            ErrorMessages.userMessage(
                                error,
                                "Не вдалося завантажити плейлисти"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun openDestinationExistingList(
        p: ImportedPlaylist,
        selected: List<Track>,
        playlists: List<YouTubePlaylistInfo>
    ) {
        val intent =
            destinationBaseIntent(
                p = p,
                selected = selected
            ).apply {
                putExtra(
                    DestinationActivity.EXTRA_MODE,
                    DestinationActivity.MODE_EXISTING_LIST
                )
                putStringArrayListExtra(
                    DestinationActivity.EXTRA_EXISTING_IDS,
                    ArrayList(
                        playlists.map { it.id }
                    )
                )
                putStringArrayListExtra(
                    DestinationActivity.EXTRA_EXISTING_TITLES,
                    ArrayList(
                        playlists.map { it.title }
                    )
                )
                putStringArrayListExtra(
                    DestinationActivity.EXTRA_EXISTING_PRIVACY,
                    ArrayList(
                        playlists.map { it.privacyStatus }
                    )
                )
                putExtra(
                    DestinationActivity.EXTRA_EXISTING_COUNTS,
                    playlists
                        .map { it.itemCount }
                        .toLongArray()
                )
            }

        startActivityForResult(
            intent,
            destinationScreenRequestCode
        )
    }

    private fun checkDuplicatesForDestination(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo
    ) {
        authorize {
            val token =
                accessToken
                    ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.isIndeterminate = true
            status(
                "Перевіряю дублікати у «${target.title}»…"
            )

            executor.execute {
                val result =
                    runCatching {
                        api.listPlaylistVideoIds(
                            accessToken = token,
                            playlistId = target.id
                        )
                    }

                runOnUiThread {
                    progress.isIndeterminate = false
                    progress.visibility = View.GONE

                    result.onSuccess { playlistContents ->
                        quotaTracker.recordGeneralUnits(
                            playlistContents.requestCount *
                                QuotaTracker.SIMPLE_LIST_COST
                        )
                        updateQuotaPanel()

                        val analysis =
                            analyzeDuplicates(
                                selected = selected,
                                existingVideoIds =
                                    playlistContents.videoIds
                            )

                        pendingDestinationTarget = target
                        pendingDestinationAnalysis = analysis
                        pendingDestinationScanRequestCount =
                            playlistContents.requestCount

                        openDestinationExistingConfirm(
                            p = p,
                            selected = selected,
                            target = target,
                            analysis = analysis,
                            scanRequestCount =
                                playlistContents.requestCount
                        )
                    }.onFailure { error ->
                        if (isQuotaError(error)) {
                            quotaTracker.recordQuotaError(
                                error.message
                                    ?: "Не вдалося перевірити дублікати через квоту"
                            )
                            updateQuotaPanel()
                        }

                        pendingDestinationTarget = target
                        pendingDestinationAnalysis = null
                        pendingDestinationScanRequestCount = 0

                        openDestinationScanFailed(
                            p = p,
                            selected = selected,
                            target = target,
                            error = error
                        )
                    }
                }
            }
        }
    }

    private fun openDestinationExistingConfirm(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo,
        analysis: DuplicateAnalysis,
        scanRequestCount: Int
    ) {
        val intent =
            destinationBaseIntent(
                p = p,
                selected = selected
            ).apply {
                putExtra(
                    DestinationActivity.EXTRA_MODE,
                    DestinationActivity.MODE_EXISTING_CONFIRM
                )
                putDestinationTargetExtras(target)
                putExtra(
                    DestinationActivity.EXTRA_ALREADY_COUNT,
                    analysis.alreadyInPlaylist.size
                )
                putExtra(
                    DestinationActivity.EXTRA_REPEATED_COUNT,
                    analysis.repeatedInImport.size
                )
                putExtra(
                    DestinationActivity.EXTRA_NEW_COUNT,
                    analysis.tracksToAdd.size
                )
                putExtra(
                    DestinationActivity.EXTRA_SCAN_REQUESTS,
                    scanRequestCount
                )
                putExtra(
                    DestinationActivity.EXTRA_QUOTA_SKIP,
                    quotaPlanForWrite(
                        trackCount = analysis.tracksToAdd.size,
                        createPlaylist = false
                    )
                )
                putExtra(
                    DestinationActivity.EXTRA_QUOTA_ALL,
                    quotaPlanForWrite(
                        trackCount = selected.size,
                        createPlaylist = false
                    )
                )
            }

        startActivityForResult(
            intent,
            destinationScreenRequestCode
        )
    }

    private fun openDestinationScanFailed(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo,
        error: Throwable
    ) {
        val intent =
            destinationBaseIntent(
                p = p,
                selected = selected
            ).apply {
                putExtra(
                    DestinationActivity.EXTRA_MODE,
                    DestinationActivity.MODE_EXISTING_SCAN_FAILED
                )
                putDestinationTargetExtras(target)
                putExtra(
                    DestinationActivity.EXTRA_SCAN_ERROR,
                    ErrorMessages.userMessage(
                        error,
                        "Не вдалося прочитати вміст плейлиста"
                    ) +
                        "\n\nТехнічно: " +
                        ErrorMessages.technicalDetails(error)
                )
                putExtra(
                    DestinationActivity.EXTRA_QUOTA_ALL,
                    quotaPlanForWrite(
                        trackCount = selected.size,
                        createPlaylist = false
                    )
                )
            }

        startActivityForResult(
            intent,
            destinationScreenRequestCode
        )
    }

    private fun Intent.putDestinationTargetExtras(
        target: YouTubePlaylistInfo
    ) {
        putExtra(
            DestinationActivity.EXTRA_TARGET_ID,
            target.id
        )
        putExtra(
            DestinationActivity.EXTRA_TARGET_TITLE,
            target.title
        )
        putExtra(
            DestinationActivity.EXTRA_TARGET_PRIVACY,
            target.privacyStatus
        )
        putExtra(
            DestinationActivity.EXTRA_TARGET_COUNT,
            target.itemCount
        )
    }

    private fun finishExistingDestination(
        p: ImportedPlaylist,
        selected: List<Track>,
        duplicateMode: String
    ) {
        val target =
            pendingDestinationTarget
                ?: return toast(
                    "Цільовий плейлист уже недоступний. Виберіть його ще раз."
                )

        val analysis =
            pendingDestinationAnalysis

        val plan =
            when (duplicateMode) {
                DestinationActivity.DUPLICATE_MODE_NO_SCAN ->
                    DuplicateWritePlan(
                        tracksToWrite = selected,
                        tracksToSkip = emptyList(),
                        alreadyInPlaylistCount = 0,
                        repeatedInImportCount = 0,
                        scanRequestCount = 0,
                        scanSucceeded = false,
                        addDuplicatesAnyway = true
                    )

                DestinationActivity.DUPLICATE_MODE_ALL -> {
                    val currentAnalysis =
                        analysis
                            ?: return toast(
                                "Результат перевірки дублікатів уже недоступний. Спробуйте ще раз."
                            )

                    DuplicateWritePlan(
                        tracksToWrite = selected,
                        tracksToSkip = emptyList(),
                        alreadyInPlaylistCount =
                            currentAnalysis.alreadyInPlaylist.size,
                        repeatedInImportCount =
                            currentAnalysis.repeatedInImport.size,
                        scanRequestCount =
                            pendingDestinationScanRequestCount,
                        scanSucceeded = true,
                        addDuplicatesAnyway = true
                    )
                }

                else -> {
                    val currentAnalysis =
                        analysis
                            ?: return toast(
                                "Результат перевірки дублікатів уже недоступний. Спробуйте ще раз."
                            )

                    DuplicateWritePlan(
                        tracksToWrite =
                            currentAnalysis.tracksToAdd,
                        tracksToSkip =
                            currentAnalysis.tracksToSkip,
                        alreadyInPlaylistCount =
                            currentAnalysis.alreadyInPlaylist.size,
                        repeatedInImportCount =
                            currentAnalysis.repeatedInImport.size,
                        scanRequestCount =
                            pendingDestinationScanRequestCount,
                        scanSucceeded = true,
                        addDuplicatesAnyway = false
                    )
                }
            }

        pendingDestinationTarget = null
        pendingDestinationAnalysis = null
        pendingDestinationScanRequestCount = 0

        actuallyAppendToExisting(
            p = p,
            selected = plan.tracksToWrite,
            target = target,
            duplicateTracksToSkip =
                plan.tracksToSkip
        )
    }






    private fun analyzeDuplicates(
        selected: List<Track>,
        existingVideoIds: Set<String>
    ): DuplicateAnalysis {
        val alreadyInPlaylist = mutableListOf<Track>()
        val repeatedInImport = mutableListOf<Track>()
        val tracksToAdd = mutableListOf<Track>()
        val seenIncoming = mutableSetOf<String>()

        selected.forEach { track ->
            val videoId =
                track.selectedVideoId
                    ?.trim()
                    .orEmpty()

            if (videoId.isBlank()) {
                tracksToAdd += track
                return@forEach
            }

            val isAlreadyInPlaylist =
                videoId in existingVideoIds

            val isRepeatedInImport =
                !seenIncoming.add(videoId)

            when {
                isAlreadyInPlaylist ->
                    alreadyInPlaylist += track

                isRepeatedInImport ->
                    repeatedInImport += track

                else ->
                    tracksToAdd += track
            }
        }

        return DuplicateAnalysis(
            alreadyInPlaylist = alreadyInPlaylist,
            repeatedInImport = repeatedInImport,
            tracksToAdd = tracksToAdd
        )
    }




    private fun actuallyAppendToExisting(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo,
        duplicateTracksToSkip: List<Track>
    ) {
        duplicateTracksToSkip.forEach { track ->
            track.status = TrackStatus.DUPLICATE
            track.error =
                "Дублікат: цей YouTube videoId уже є у вибраному " +
                    "плейлисті або повторюється в поточному імпорті. " +
                    "Write-запит пропущено."
        }

        adapter.notifyDataSetChanged()
        updateSummary()

        authorize {
            val token = accessToken ?: return@authorize

            val job =
                buildPendingJob(
                    playlistName = target.title,
                    playlistId = target.id,
                    privacyStatus = target.privacyStatus,
                    destination = PendingDestination.EXISTING_PLAYLIST,
                    tracks = selected
                )

            pendingJobStore.upsert(job)
            updatePendingButton()

            createdPlaylistId = target.id
            prepareWriteUi(
                total = selected.size,
                message =
                    if (selected.isEmpty()) {
                        "Усі треки — дублікати. API write не потрібен."
                    } else {
                        "Додаю треки до «${target.title}»…"
                    }
            )

            executeWriteJob(
                initialJob = job,
                tracks = selected,
                token = token,
                operationLabel = "оновлено існуючий"
            )
        }
    }




    private fun actuallyCreatePlaylist(
        p: ImportedPlaylist,
        selected: List<Track>,
        privacyStatus: String
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            val job =
                buildPendingJob(
                    playlistName = p.name,
                    playlistId = null,
                    privacyStatus = privacyStatus,
                    destination = PendingDestination.NEW_PLAYLIST,
                    tracks = selected
                )

            pendingJobStore.upsert(job)
            updatePendingButton()

            createdPlaylistId = null
            prepareWriteUi(
                total = selected.size + 1,
                message = "Створюю плейлист…"
            )

            executeWriteJob(
                initialJob = job,
                tracks = selected,
                token = token,
                operationLabel = "створено новий"
            )
        }
    }

    private fun prepareWriteUi(
        total: Int,
        message: String
    ) {
        progress.visibility = View.VISIBLE
        progress.isIndeterminate = false
        progress.max = total.coerceAtLeast(1)
        progress.progress = 0
        status(message)
    }

    private fun executeWriteJob(
        initialJob: PendingJob,
        tracks: List<Track>,
        token: String,
        operationLabel: String
    ) {
        executor.execute {
            var job = initialJob
            syncHistoryFromJob(job, HistoryStatus.RUNNING)
            var playlistId = job.playlistId

            if (playlistId.isNullOrBlank()) {
                quotaTracker.recordGeneralUnits(QuotaTracker.PLAYLIST_CREATE_COST)

                try {
                    playlistId =
                        api.createPlaylist(
                            token,
                            job.playlistName,
                            job.privacyStatus
                        )

                    createdPlaylistId = playlistId

                    job =
                        job.copy(
                            playlistId = playlistId,
                            updatedAt = System.currentTimeMillis(),
                            lastError = null
                        )

                    pendingJobStore.upsert(job)
                    syncHistoryFromJob(job, HistoryStatus.RUNNING)

                    runOnUiThread {
                        progress.progress = 1
                        updateQuotaPanel()
                        updatePendingButton()
                    }
                } catch (e: Exception) {
                    if (isQuotaError(e)) {
                        quotaTracker.recordQuotaError(
                            e.message ?: "Quota exceeded while creating playlist"
                        )

                        job =
                            job.copy(
                                updatedAt = System.currentTimeMillis(),
                                lastError = e.message
                            )

                        pendingJobStore.upsert(job)
                        tracks.forEach { pendingTrack ->
                            pendingTrack.status = TrackStatus.PENDING
                            pendingTrack.error =
                                "Очікує продовження: " +
                                    (e.message ?: "закінчилась квота API")
                        }
                        syncHistoryFromJob(job, HistoryStatus.PENDING_QUOTA)

                        runOnUiThread {
                            progress.visibility = View.GONE
                            adapter.notifyDataSetChanged()
                            updateSummary()
                            updateQuotaPanel()
                            updatePendingButton()
                            showQuotaPausedDialog(job)
                        }
                    } else {
                        val friendlyError =
                            ErrorMessages.userMessage(
                                e,
                                "Не вдалося створити плейлист"
                            )

                        job =
                            job.copy(
                                updatedAt = System.currentTimeMillis(),
                                lastError = friendlyError
                            )
                        syncHistoryFromJob(job, HistoryStatus.FAILED)
                        pendingJobStore.remove(job.id)

                        runOnUiThread {
                            progress.visibility = View.GONE
                            updatePendingButton()
                            toast(friendlyError)
                        }
                    }

                    return@execute
                }
            } else {
                createdPlaylistId = playlistId
            }

            val createOffset =
                if (job.destination == PendingDestination.NEW_PLAYLIST) 1 else 0

            for ((index, track) in tracks.withIndex()) {
                val videoId = track.selectedVideoId

                if (videoId.isNullOrBlank()) {
                    track.status = TrackStatus.FAILED
                    track.error = "Немає videoId для додавання"

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            failedCount = job.failedCount + 1,
                            remainingTracks = job.remainingTracks.drop(1),
                            lastError = track.error
                        )

                    pendingJobStore.upsert(job)
                    syncHistoryFromJob(job, HistoryStatus.RUNNING)

                    runOnUiThread {
                        progress.progress = index + 1 + createOffset
                        adapter.notifyDataSetChanged()
                        updateSummary()
                        updatePendingButton()
                    }

                    continue
                }

                quotaTracker.recordGeneralUnits(
                    QuotaTracker.PLAYLIST_ITEM_INSERT_COST
                )

                try {
                    api.addVideo(token, playlistId!!, videoId)

                    track.status = TrackStatus.ADDED
                    track.error = null

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            addedCount = job.addedCount + 1,
                            remainingTracks = job.remainingTracks.drop(1),
                            lastError = null
                        )

                    pendingJobStore.upsert(job)
                    syncHistoryFromJob(job, HistoryStatus.RUNNING)
                } catch (e: Exception) {
                    if (isQuotaError(e)) {
                        quotaTracker.recordQuotaError(
                            e.message ?: "Quota exceeded while adding track"
                        )

                        val remaining =
                            tracks.drop(index).mapNotNull(::trackToPendingTrack)

                        job =
                            job.copy(
                                updatedAt = System.currentTimeMillis(),
                                remainingTracks = remaining,
                                lastError = e.message
                            )

                        pendingJobStore.upsert(job)

                        tracks.drop(index).forEach { pendingTrack ->
                            pendingTrack.status = TrackStatus.PENDING
                            pendingTrack.error =
                                "Очікує продовження: " +
                                    (e.message ?: "закінчилась квота API")
                        }
                        syncHistoryFromJob(job, HistoryStatus.PENDING_QUOTA)

                        runOnUiThread {
                            progress.visibility = View.GONE
                            adapter.notifyDataSetChanged()
                            updateSummary()
                            updateQuotaPanel()
                            updatePendingButton()
                            showQuotaPausedDialog(job)
                        }

                        return@execute
                    }

                    val friendlyError =
                        ErrorMessages.userMessage(
                            e,
                            "Не вдалося додати трек"
                        )

                    track.status = TrackStatus.FAILED
                    track.error = friendlyError

                    job =
                        job.copy(
                            updatedAt = System.currentTimeMillis(),
                            failedCount = job.failedCount + 1,
                            remainingTracks = job.remainingTracks.drop(1),
                            lastError = friendlyError
                        )

                    pendingJobStore.upsert(job)
                    syncHistoryFromJob(job, HistoryStatus.RUNNING)
                }

                runOnUiThread {
                    progress.progress = index + 1 + createOffset
                    status(
                        "Додаю ${index + 1}/${tracks.size}… " +
                            "залишилось ${job.remainingTracks.size}"
                    )
                    adapter.notifyDataSetChanged()
                    updateSummary()
                    updateQuotaPanel()
                    updatePendingButton()
                }
            }

            syncHistoryFromJob(
                job,
                if (job.failedCount > 0) {
                    HistoryStatus.PARTIAL
                } else {
                    HistoryStatus.COMPLETED
                }
            )
            pendingJobStore.remove(job.id)

            runOnUiThread {
                progress.visibility = View.GONE
                updatePendingButton()
                updateQuotaPanel()
                updateSummary()

                val completedAdded = job.addedCount
                val completedFailed = job.failedCount

                status(
                    "Готово. ${job.playlistName}: " +
                        "додано $completedAdded, помилок $completedFailed."
                )

                showPlaylistResult(
                    playlistName = job.playlistName,
                    addedCount = completedAdded,
                    failedCount = completedFailed,
                    privacyStatus = job.privacyStatus,
                    operationLabel = operationLabel
                )
            }
        }
    }

    private fun buildPendingJob(
        playlistName: String,
        playlistId: String?,
        privacyStatus: String,
        destination: PendingDestination,
        tracks: List<Track>
    ): PendingJob {
        val now = System.currentTimeMillis()

        return PendingJob(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            sourceLabel = currentImportSourceLabel,
            playlistName = playlistName,
            playlistId = playlistId,
            privacyStatus = privacyStatus,
            destination = destination,
            googleEmail = googleAccountInfo?.email,
            youtubeChannelId = youtubeChannelInfo?.id,
            youtubeChannelTitle = youtubeChannelInfo?.title,
            totalCount = tracks.size,
            addedCount = 0,
            failedCount = 0,
            remainingTracks = tracks.mapNotNull(::trackToPendingTrack),
            lastError = null
        )
    }

    private fun trackToPendingTrack(track: Track): PendingTrack? {
        val videoId = track.selectedVideoId ?: return null

        return PendingTrack(
            originalTitle = track.originalTitle,
            originalArtist = track.originalArtist,
            videoId = videoId,
            selectedTitle = track.selectedTitle,
            selectedChannel = track.selectedChannel,
            historyIndex = track.historyIndex ?: -1
        )
    }

    private fun pendingTrackToTrack(item: PendingTrack): Track =
        Track(
            originalTitle = item.originalTitle,
            originalArtist = item.originalArtist,
            selectedVideoId = item.videoId,
            selectedTitle = item.selectedTitle,
            selectedChannel = item.selectedChannel,
            status = TrackStatus.PENDING,
            manuallySelected = false,
            historyIndex = item.historyIndex.takeIf { it >= 0 }
        )


    private fun showQuotaPausedDialog(job: PendingJob) {
        val playlistInfo =
            if (job.playlistId.isNullOrBlank()) {
                "Плейлист ще не створений."
            } else {
                "Playlist ID (ID плейлиста): ${job.playlistId}"
            }

        UiChrome.alertBuilder(this)
            .setTitle("Операцію призупинено")
            .setMessage(
                "YouTube API повідомив про вичерпання квоти.\n\n" +
                    "Вже додано: ${job.addedCount}/${job.totalCount}\n" +
                    "Помилок: ${job.failedCount}\n" +
                    "У черзі: ${job.remainingTracks.size}\n" +
                    "$playlistInfo\n\n" +
                    "Невиконані треки збережені локально. " +
                    "Відкрийте «Черга» і натисніть «Продовжити», " +
                    "коли квота відновиться."
            )
            .setNegativeButton("Закрити", null)
            .setPositiveButton("Відкрити чергу") { _, _ ->
                showPendingJobs()
            }
            .show()
    }

    private fun showPendingJobs() {
        startActivityForResult(
            Intent(
                this,
                PendingActivity::class.java
            ),
            pendingQueueRequestCode
        )
    }

    private fun showPendingJobDetails(job: PendingJob) {
        val destination =
            when (job.destination) {
                PendingDestination.NEW_PLAYLIST ->
                    "новий плейлист"
                PendingDestination.EXISTING_PLAYLIST ->
                    "існуючий плейлист"
            }

        val playlistId =
            job.playlistId ?: "ще не створений"

        UiChrome.showMessageDialog(
            activity = this,
            title = job.playlistName,
            message =
                "Тип: $destination\n" +
                    "Додано: ${job.addedCount}/${job.totalCount}\n" +
                    "Помилок: ${job.failedCount}\n" +
                    "Очікує: ${job.remainingTracks.size}\n\n" +
                    "Google: ${job.googleEmail ?: "не збережено"}\n" +
                    "YouTube/YTM: ${job.youtubeChannelTitle ?: "не збережено"}\n" +
                    "Channel ID: ${job.youtubeChannelId ?: "—"}\n" +
                    "Playlist ID: $playlistId\n\n" +
                    "Остання помилка:\n${job.lastError ?: "—"}",
            actions = listOf(
                UiChrome.DialogAction("Продовжити") {
                    resumePendingJob(job)
                },
                UiChrome.DialogAction(
                    label = "Видалити",
                    tone = UiChrome.ActionTone.DANGER
                ) {
                    confirmDeletePendingJob(job)
                },
                UiChrome.DialogAction(
                    label = "Закрити",
                    tone = UiChrome.ActionTone.ACCENT
                ) {}
            )
        )
    }

    private fun confirmDeletePendingJob(job: PendingJob) {
        UiChrome.alertBuilder(this)
            .setTitle("Видалити із черги?")
            .setMessage(
                "Локальний запис «${job.playlistName}» буде видалений. " +
                    "Вже додані в YouTube/YTM треки не видаляються."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Видалити") { _, _ ->
                pendingJobStore.remove(job.id)
                updatePendingButton()
                toast("Запис видалено з черги")
            }
            .show()
    }

    private fun resumePendingJob(job: PendingJob) {
        authorize {
            val token = accessToken ?: return@authorize

            val currentChannel = youtubeChannelInfo?.id
            val currentEmail = googleAccountInfo?.email

            val channelMismatch =
                !job.youtubeChannelId.isNullOrBlank() &&
                    !currentChannel.isNullOrBlank() &&
                    job.youtubeChannelId != currentChannel

            val emailMismatch =
                !job.googleEmail.isNullOrBlank() &&
                    !currentEmail.isNullOrBlank() &&
                    !job.googleEmail.equals(currentEmail, ignoreCase = true)

            if (channelMismatch || emailMismatch) {
                UiChrome.alertBuilder(this)
                    .setTitle("Потрібен інший акаунт")
                    .setMessage(
                        "Це завдання було створено для:\n" +
                            "Google: ${job.googleEmail ?: "—"}\n" +
                            "YouTube/YTM: ${job.youtubeChannelTitle ?: "—"}\n" +
                            "Channel ID: ${job.youtubeChannelId ?: "—"}\n\n" +
                            "Зараз підключений інший акаунт або канал."
                    )
                    .setNegativeButton("Скасувати", null)
                    .setPositiveButton("Змінити акаунт") { _, _ ->
                        authorize(forceAccountPicker = true) {
                            resumePendingJob(job)
                        }
                    }
                    .show()

                return@authorize
            }

            val tracks =
                job.remainingTracks.map(::pendingTrackToTrack)

            playlist =
                ImportedPlaylist(
                    name = job.playlistName,
                    tracks = tracks.toMutableList()
                )
            currentImportSourceLabel = job.sourceLabel

            visibleTracks.clear()
            visibleTracks.addAll(tracks)
            adapter.notifyDataSetChanged()
            createdPlaylistId = job.playlistId
            updateSummary()

            prepareWriteUi(
                total =
                    tracks.size +
                        if (
                            job.destination == PendingDestination.NEW_PLAYLIST &&
                            job.playlistId.isNullOrBlank()
                        ) 1 else 0,
                message = "Продовжую «${job.playlistName}»…"
            )

            executeWriteJob(
                initialJob = job.copy(
                    updatedAt = System.currentTimeMillis(),
                    lastError = null
                ),
                tracks = tracks,
                token = token,
                operationLabel = "продовжено з черги"
            )
        }
    }

    private fun quotaPlanForWrite(
        trackCount: Int,
        createPlaylist: Boolean
    ): String {
        val required =
            trackCount * QuotaTracker.PLAYLIST_ITEM_INSERT_COST +
                if (createPlaylist) QuotaTracker.PLAYLIST_CREATE_COST else 0

        val quota = quotaTracker.snapshot()

        val warning =
            if (required > quota.generalRemaining) {
                "\n⚠ Локальна оцінка показує, що квоти може не вистачити. " +
                    "Якщо Google поверне quotaExceeded, залишок автоматично " +
                    "піде в Чергу (Pending Queue)."
            } else {
                ""
            }

        return "Квота API (оцінка):\n" +
            "Потрібно приблизно: $required units (одиниць)\n" +
            "Локально залишилось приблизно: ${quota.generalRemaining}/" +
            "${QuotaTracker.GENERAL_DAILY_LIMIT}" +
            warning
    }

    private fun showQuotaDialog() {
        val quota = quotaTracker.snapshot()
        val jobs = pendingJobStore.getAll()

        val message =
            "Search Queries (пошук):\n" +
                "${quota.searchCalls}/${QuotaTracker.SEARCH_DAILY_LIMIT} використано\n" +
                "≈ ${quota.searchRemaining} залишилось\n\n" +
                "General quota (загальна квота):\n" +
                "${quota.generalUnits}/${QuotaTracker.GENERAL_DAILY_LIMIT} використано\n" +
                "≈ ${quota.generalRemaining} залишилось\n\n" +
                "Попадань у кеш сьогодні: ${quota.cacheHits}\n" +
                "Недороблених завдань: ${jobs.size}\n" +
                "День квоти Google: ${quota.dayKey} (Pacific Time)\n\n" +
                "Це локальна оцінка тільки для операцій, які цей застосунок " +
                "зафіксував на цьому телефоні. Точний стан знаходиться в " +
                "Google Cloud Console." +
                if (!quota.lastQuotaError.isNullOrBlank()) {
                    "\n\nОстання quota error (помилка квоти):\n" +
                        quota.lastQuotaError
                } else {
                    ""
                }

        UiChrome.showMessageDialog(
            activity = this,
            title = "Квота API (локальна оцінка)",
            message = message,
            actions = listOf(
                UiChrome.DialogAction("Google Cloud") {
                    openGoogleCloudQuota()
                },
                UiChrome.DialogAction("Черга") {
                    showPendingJobs()
                },
                UiChrome.DialogAction(
                    label = "Закрити",
                    tone = UiChrome.ActionTone.ACCENT
                ) {}
            ),
            actionLayout = UiChrome.DialogActionLayout.PRIMARY_TOP
        )
    }

    private fun updateQuotaPanel() {
        if (!::quotaButton.isInitialized) return

        val quota = quotaTracker.snapshot()

        quotaButton.text =
            if (quota.lastQuotaError.isNullOrBlank()) {
                "Квота"
            } else {
                "Квота ⚠"
            }
    }

    private fun updatePendingButton() {
        if (!::pendingButton.isInitialized) return

        val count = pendingJobStore.getAll().size
        pendingButton.text =
            if (count > 0) {
                "Черга ($count)"
            } else {
                "Черга"
            }
    }

    private fun isQuotaError(error: Throwable): Boolean =
        (error as? YouTubeApiException)?.isQuotaError == true ||
            error.message.orEmpty().contains("quota", ignoreCase = true) ||
            error.message.orEmpty().contains("daily limit", ignoreCase = true)



    private fun maybeShowWelcome() {
        if (
            uiPrefs.getBoolean(
                KEY_WELCOME_SEEN,
                false
            )
        ) {
            return
        }

        showQuickStartDialog(
            firstRun = true
        )
    }

    private fun showQuickStartDialog(
        firstRun: Boolean = false
    ) {
        val actions =
            mutableListOf(
                UiChrome.DialogAction("Почати") {
                    markWelcomeSeen()
                },
                UiChrome.DialogAction("Приватність") {
                    if (firstRun) {
                        markWelcomeSeen()
                    }
                    showPrivacyDialog()
                }
            )

        actions +=
            UiChrome.DialogAction(
                label =
                    if (firstRun) {
                        "Не зараз"
                    } else {
                        "Закрити"
                    },
                tone = UiChrome.ActionTone.ACCENT
            ) {}

        UiChrome.showMessageDialog(
            activity = this,
            title = "Вітаємо в YTM Importer",
            message =
                "Створити плейлист можна у 4 кроки:\n\n" +
                    "1. Імпортуйте CSV/TXT/YTM Project або вставте текст.\n" +
                    "2. Підключіть Google / YouTube Music.\n" +
                    "3. Знайдіть треки та перевірте сумнівні результати.\n" +
                    "4. Створіть новий плейлист або додайте треки " +
                    "до існуючого.\n\n" +
                    "Порада: жовті треки краще переглянути вручну. " +
                    "SearchCache зменшує повторні API-пошуки.\n\n" +
                    "YTM Importer не має власного сервера, реклами " +
                    "або вбудованої аналітики.",
            actions = actions
        )
    }

    private fun markWelcomeSeen() {
        uiPrefs
            .edit()
            .putBoolean(
                KEY_WELCOME_SEEN,
                true
            )
            .apply()
    }

    private fun showPrivacyDialog() {
        UiChrome.alertBuilder(this)
            .setTitle("Приватність")
            .setMessage(
                "YTM Importer працює без власного сервера.\n\n" +
                    "Застосунок використовує Google OAuth та YouTube Data API " +
                    "лише для дій, які ви запускаєте: читання інформації " +
                    "про акаунт/канал, пошук, створення плейлистів і " +
                    "додавання треків.\n\n" +
                    "Локально на телефоні можуть зберігатися History, " +
                    "Pending Queue, SearchCache та локальна оцінка quota.\n\n" +
                    "OAuth access token не входить у backup, YTM Project " +
                    "або Diagnostics. Diagnostics маскує email та Channel ID.\n\n" +
                    "Full Backup може містити персональні метадані, наприклад " +
                    "email, Channel ID, назви плейлистів та History. " +
                    "Зберігайте та надсилайте backup лише туди, де йому довіряєте.\n\n" +
                    "Android Share не завантажує файли на сервер YTM Importer: " +
                    "після вибору іншого застосунку подальша передача залежить " +
                    "від нього.\n\n" +
                    "YTM Importer — незалежний інструмент і не є офіційним " +
                    "застосунком Google або YouTube."
            )
            .setNegativeButton("Закрити", null)
            .setPositiveButton("Швидкий старт") { _, _ ->
                showQuickStartDialog()
            }
            .show()
    }

    private fun showServiceTools() {
        startActivity(
            Intent(
                this,
                ServiceActivity::class.java
            ).apply {
                putExtra(
                    ServiceActivity.EXTRA_GOOGLE_CONNECTED,
                    !accessToken.isNullOrBlank()
                )
                putExtra(
                    ServiceActivity.EXTRA_GOOGLE_EMAIL,
                    maskedEmail(googleAccountInfo?.email)
                )
                putExtra(
                    ServiceActivity.EXTRA_CHANNEL_TITLE,
                    youtubeChannelInfo?.title
                )
                putExtra(
                    ServiceActivity.EXTRA_CHANNEL_ID,
                    maskedIdentifier(youtubeChannelInfo?.id)
                )
            }
        )
    }

    private fun showAboutDialog() {
        val targetSdk =
            applicationInfo.targetSdkVersion

        val message =
            "Версія: ${BuildConfig.VERSION_NAME} " +
                "(${BuildConfig.VERSION_CODE})\n" +
                "Android target SDK: $targetSdk\n\n" +
                "YTM Importer допомагає перетворити трекліст " +
                "у плейлист YouTube / YouTube Music.\n\n" +
                "Можливості:\n" +
                "• CSV / TXT / прямий текст / YTM Project\n" +
                "• автоматичний пошук + SearchCache\n" +
                "• ручна перевірка та заміна треків\n" +
                "• новий або існуючий плейлист\n" +
                "• перевірка дублікатів\n" +
                "• History + повторно завантажувані YTM Project\n" +
                "• Queue / Resume при quota problems\n" +
                "• Backup / Restore / Rollback\n\n" +
                "Без реклами, власного сервера та вбудованої аналітики.\n\n" +
                "Незалежний інструмент. Не є офіційним застосунком " +
                "Google або YouTube."

        UiChrome.showMessageDialog(
            activity = this,
            title = "YTM Importer",
            message = message,
            actions = listOf(
                UiChrome.DialogAction("Швидкий старт") {
                    showQuickStartDialog()
                },
                UiChrome.DialogAction("Приватність") {
                    showPrivacyDialog()
                },
                UiChrome.DialogAction(
                    label = "Закрити",
                    tone = UiChrome.ActionTone.ACCENT
                ) {}
            )
        )
    }

    private fun showRegressionChecklist() {
        UiChrome.alertBuilder(this)
            .setTitle("Regression checklist")
            .setMessage(
                "Короткий список:\n\n" +
                    "1. Імпорт CSV/TXT/текст\n" +
                    "2. Google account + YTM channel\n" +
                    "3. Пошук + кеш + ручний кандидат\n" +
                    "4. Новий playlist + privacy\n" +
                    "5. Existing playlist + duplicates\n" +
                    "6. Quota / Pending Queue / Resume\n" +
                    "7. History\n" +
                    "8. Export / Backup / Restore\n" +
                    "9. Diagnostics / Share / SearchCache\n" +
                    "10. Оновлення APK поверх попередньої версії\n\n" +
                    "Повний checklist є у docs/v.1.3.1/REGRESSION_CHECKLIST.md."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDiagnostics() {
        UiChrome.showMessageDialog(
            activity = this,
            title = "Діагностика YTM Importer",
            message = buildDiagnosticsText(),
            actions = listOf(
                UiChrome.DialogAction("Зберегти TXT") {
                    saveDiagnostics()
                },
                UiChrome.DialogAction("Поділитися") {
                    shareDiagnostics()
                },
                UiChrome.DialogAction(
                    label = "Закрити",
                    tone = UiChrome.ActionTone.ACCENT
                ) {}
            )
        )
    }

    private fun saveDiagnostics() {
        createDocumentForExport(
            fileName =
                "YTM_Diagnostics_${exportTimestamp()}.txt",
            mimeType = "text/plain",
            content = buildDiagnosticsText(),
            successMessage = "Diagnostics TXT збережено"
        )
    }

    private fun shareDiagnostics() {
        shareTextFile(
            fileName =
                "YTM_Diagnostics_${exportTimestamp()}.txt",
            mimeType = "text/plain",
            content = buildDiagnosticsText(),
            chooserTitle = "Поділитися YTM Diagnostics"
        )
    }

    private fun buildDiagnosticsText(): String {
        val quota = quotaTracker.snapshot()
        val cache = searchCache.stats()
        val history = historyStore.getAll()
        val pending = pendingJobStore.getAll()
        val tracks = playlist?.tracks.orEmpty()

        return buildString {
            append("YTM Importer — Diagnostics\n")
            append("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
            append("Package: $packageName\n")
            append(
                "Android: ${Build.VERSION.RELEASE} " +
                    "(SDK ${Build.VERSION.SDK_INT})\n"
            )
            append(
                "Device: ${Build.MANUFACTURER} ${Build.MODEL}\n"
            )
            append(
                "Generated: " +
                    formatHistoryDate(
                        System.currentTimeMillis()
                    ) +
                    "\n\n"
            )

            append("ACCOUNT\n")
            append(
                "Google: " +
                    if (accessToken.isNullOrBlank()) {
                        "not connected"
                    } else {
                        "connected"
                    } +
                    "\n"
            )
            append(
                "Google email: " +
                    maskedEmail(
                        googleAccountInfo?.email
                    ) +
                    "\n"
            )
            append(
                "YouTube/YTM channel: " +
                    (
                        youtubeChannelInfo?.title
                            ?: "not loaded"
                    ) +
                    "\n"
            )
            append(
                "Channel ID: " +
                    maskedIdentifier(
                        youtubeChannelInfo?.id
                    ) +
                    "\n\n"
            )

            append("CURRENT IMPORT\n")
            append(
                "Playlist: " +
                    (
                        playlist?.name
                            ?: "none"
                    ) +
                    "\n"
            )
            append("Tracks: ${tracks.size}\n")
            TrackStatus.entries.forEach { status ->
                val count =
                    tracks.count {
                        it.status == status
                    }

                if (count > 0) {
                    append(
                        "${status.name}: $count\n"
                    )
                }
            }
            append("\n")

            append("QUOTA — local estimate\n")
            append(
                "Day: ${quota.dayKey} Pacific Time\n"
            )
            append(
                "Search: ${quota.searchCalls}/" +
                    "${QuotaTracker.SEARCH_DAILY_LIMIT} " +
                    "(remaining ≈${quota.searchRemaining})\n"
            )
            append(
                "General: ${quota.generalUnits}/" +
                    "${QuotaTracker.GENERAL_DAILY_LIMIT} " +
                    "(remaining ≈${quota.generalRemaining})\n"
            )
            append("Cache hits today: ${quota.cacheHits}\n")

            if (!quota.lastQuotaError.isNullOrBlank()) {
                append(
                    "Last quota error: " +
                        quota.lastQuotaError +
                        "\n"
                )
            }

            append("\nSEARCH CACHE\n")
            append(
                "Total entries: ${cache.totalEntries}\n"
            )
            append(
                "Valid: ${cache.validEntries}\n"
            )
            append(
                "Expired: ${cache.expiredEntries}\n"
            )
            append(
                "Malformed: ${cache.malformedEntries}\n"
            )
            append(
                "Approx size: " +
                    formatBytes(
                        cache.approximateBytes
                    ) +
                    "\n"
            )
            append(
                "Oldest: " +
                    formatNullableDate(
                        cache.oldestCachedAt
                    ) +
                    "\n"
            )
            append(
                "Newest: " +
                    formatNullableDate(
                        cache.newestCachedAt
                    ) +
                    "\n\n"
            )

            append("LOCAL DATA\n")
            append("History entries: ${history.size}\n")
            append("Pending jobs: ${pending.size}\n")
            append(
                "History JSON size: " +
                    formatBytes(
                        historyStore
                            .exportJson()
                            .toByteArray(
                                Charsets.UTF_8
                            )
                            .size
                            .toLong()
                    ) +
                    "\n"
            )
            append(
                "Pending JSON size: " +
                    formatBytes(
                        pendingJobStore
                            .exportJson()
                            .toByteArray(
                                Charsets.UTF_8
                            )
                            .size
                            .toLong()
                    ) +
                    "\n\n"
            )

            append("PRIVACY\n")
            append(
                "Diagnostics does not contain OAuth access token, " +
                    "Google password or signing keys.\n"
            )
            append(
                "Email and Channel ID are masked."
            )
        }
    }

    private fun showSearchCacheTools() {
        val stats = searchCache.stats()

        val message =
            "Усього записів: ${stats.totalEntries}\n" +
                "Активних: ${stats.validEntries}\n" +
                "Прострочених: ${stats.expiredEntries}\n" +
                "Пошкоджених: ${stats.malformedEntries}\n" +
                "Приблизний розмір: " +
                formatBytes(
                    stats.approximateBytes
                ) +
                "\n\nНайстаріший: " +
                formatNullableDate(
                    stats.oldestCachedAt
                ) +
                "\nНайновіший: " +
                formatNullableDate(
                    stats.newestCachedAt
                ) +
                "\n\nОчищення кешу не видаляє History, " +
                "Чергу або YouTube/YTM плейлисти. " +
                "Після очищення повторний пошук знову " +
                "витрачатиме search quota."

        UiChrome.showMessageDialog(
            activity = this,
            title = "SearchCache",
            message = message,
            actions = listOf(
                UiChrome.DialogAction("Прострочені") {
                    val removed =
                        searchCache.clearExpired()

                    toast(
                        "Видалено записів SearchCache: $removed"
                    )
                },
                UiChrome.DialogAction(
                    label = "Очистити весь",
                    tone = UiChrome.ActionTone.DANGER
                ) {
                    confirmClearSearchCache()
                },
                UiChrome.DialogAction(
                    label = "Закрити",
                    tone = UiChrome.ActionTone.ACCENT
                ) {}
            )
        )
    }

    private fun confirmClearSearchCache() {
        UiChrome.alertBuilder(this)
            .setTitle("Очистити весь SearchCache?")
            .setMessage(
                "Усі кешовані результати пошуку буде видалено.\n\n" +
                    "History і плейлисти не зміняться, але " +
                    "наступний пошук цих треків знову звернеться " +
                    "до YouTube API."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Очистити") { _, _ ->
                val before =
                    searchCache.stats().totalEntries

                searchCache.clear()

                toast(
                    "SearchCache очищено: $before записів"
                )
            }
            .show()
    }

    private fun openGoogleCloudQuota() {
        val url =
            "https://console.cloud.google.com/apis/api/" +
                "youtube.googleapis.com/quotas"

        runCatching {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }.onFailure {
            toast(
                "Не вдалося відкрити Google Cloud Console"
            )
        }
    }

    private fun formatBytes(bytes: Long): String =
        when {
            bytes < 1024L ->
                "$bytes B"

            bytes < 1024L * 1024L ->
                String.format(
                    Locale.US,
                    "%.1f KB",
                    bytes / 1024.0
                )

            else ->
                String.format(
                    Locale.US,
                    "%.2f MB",
                    bytes / (1024.0 * 1024.0)
                )
        }

    private fun formatNullableDate(
        timestamp: Long?
    ): String =
        if (timestamp == null || timestamp <= 0L) {
            "—"
        } else {
            formatHistoryDate(timestamp)
        }

    private fun maskedEmail(
        email: String?
    ): String {
        if (email.isNullOrBlank()) return "—"

        val at = email.indexOf('@')

        if (at <= 0) {
            return maskedIdentifier(email)
        }

        val local = email.substring(0, at)
        val domain = email.substring(at)

        return when {
            local.length <= 1 ->
                "*$domain"

            local.length == 2 ->
                "${local.first()}*$domain"

            else ->
                "${local.take(2)}***$domain"
        }
    }

    private fun maskedIdentifier(
        value: String?
    ): String {
        if (value.isNullOrBlank()) return "—"
        if (value.length <= 8) return "***"

        return value.take(4) +
            "…" +
            value.takeLast(4)
    }

    private fun syncHistoryFromJob(
        job: PendingJob,
        historyStatus: HistoryStatus
    ) {
        val existing = historyStore.get(job.id)
        val liveTracks = playlist?.tracks.orEmpty()

        val merged =
            if (existing == null) {
                liveTracks
                    .mapIndexed { index, track ->
                        historyTrackFromTrack(
                            track = track,
                            fallbackIndex = index
                        )
                    }
                    .sortedBy { it.index }
                    .toMutableList()
            } else {
                existing.tracks
                    .sortedBy { it.index }
                    .toMutableList()
                    .also { stored ->
                        liveTracks.forEachIndexed { fallbackIndex, track ->
                            val historyTrack =
                                historyTrackFromTrack(
                                    track = track,
                                    fallbackIndex = fallbackIndex
                                )

                            val targetIndex =
                                stored.indexOfFirst {
                                    it.index == historyTrack.index
                                }

                            if (targetIndex >= 0) {
                                stored[targetIndex] = historyTrack
                            } else {
                                stored += historyTrack
                            }
                        }
                    }
                    .sortedBy { it.index }
                    .toMutableList()
            }

        val now = System.currentTimeMillis()

        val entry =
            HistoryEntry(
                id = job.id,
                createdAt = existing?.createdAt ?: job.createdAt,
                updatedAt = now,
                status = historyStatus,
                sourceLabel = job.sourceLabel,
                playlistName = job.playlistName,
                playlistId = job.playlistId,
                privacyStatus = job.privacyStatus,
                destination = job.destination,
                googleEmail = job.googleEmail,
                youtubeChannelId = job.youtubeChannelId,
                youtubeChannelTitle = job.youtubeChannelTitle,
                totalImportedCount =
                    existing?.totalImportedCount
                        ?: merged.size
                        .coerceAtLeast(job.totalCount),
                writeTargetCount =
                    existing?.writeTargetCount
                        ?: job.totalCount,
                addedCount =
                    merged.count {
                        it.status == TrackStatus.ADDED.name
                    },
                failedCount =
                    merged.count {
                        it.status == TrackStatus.FAILED.name
                    },
                pendingCount =
                    merged.count {
                        it.status == TrackStatus.PENDING.name
                    },
                skippedCount =
                    merged.count {
                        it.status == TrackStatus.SKIPPED.name
                    },
                duplicateCount =
                    merged.count {
                        it.status == TrackStatus.DUPLICATE.name
                    },
                missingCount =
                    merged.count {
                        it.status == TrackStatus.MISSING.name
                    },
                lastError = job.lastError,
                tracks = merged
            )

        historyStore.upsert(entry)
    }

    private fun historyTrackFromTrack(
        track: Track,
        fallbackIndex: Int
    ): HistoryTrack =
        HistoryTrack(
            index = track.historyIndex ?: fallbackIndex,
            originalTitle = track.originalTitle,
            originalArtist = track.originalArtist,
            videoId = track.selectedVideoId,
            selectedTitle = track.selectedTitle,
            selectedChannel = track.selectedChannel,
            status = track.status.name,
            manuallySelected = track.manuallySelected,
            error = track.error
        )

    private fun showDataTools() {
        val historyCount = historyStore.getAll().size
        val pendingCount = pendingJobStore.getAll().size

        val intro = TextView(this).apply {
            text =
                "History: $historyCount записів\n" +
                    "Черга: $pendingCount завдань\n\n" +
                    "Зберегти = записати файл у вибрану папку.\n" +
                    "Поділитися = відкрити стандартне Android Share " +
                    "(меню поширення).\n\n" +
                    "Для повного Restore використовується тільки " +
                    "«Повний backup», а не History JSON.\n" +
                    "Перед Restore v1.0.0 автоматично створює safety snapshot " +
                    "(точку відкату) поточних локальних даних."
            textSize = 14f
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        val labels =
            arrayOf(
                "Історія → TXT\n" +
                    "Зберегти читабельний звіт",

                "Історія → JSON\n" +
                    "Зберегти технічний експорт History",

                "Черга → JSON\n" +
                    "Зберегти технічний експорт Pending Queue",

                "Повний backup → JSON\n" +
                    "Зберегти History + Черга + Quota + SearchCache",

                "Restore повного backup\n" +
                    "Відновити локальні дані з YTM_Backup_*.json",

                "Поділитися History TXT\n" +
                    "Надіслати читабельний звіт через Android Share",

                "Поділитися повним backup\n" +
                    "Надіслати backup JSON — містить приватні метадані",

                "Відкотити останній Restore\n" +
                    if (localBackupManager.hasSafetySnapshot()) {
                        "Safety snapshot доступний"
                    } else {
                        "Safety snapshot ще не створено"
                    }
            )

        val list = ListView(this).apply {
            dividerHeight = 1
            adapter =
                ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_list_item_1,
                    labels
                )
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            addView(
                intro,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                list,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(470)
                )
            )
        }

        val dialog =
            UiChrome.alertBuilder(this)
                .setTitle("Дані — Export / Backup / Share")
                .setView(container)
                .setNegativeButton("Закрити", null)
                .create()

        list.setOnItemClickListener { _, _, position, _ ->
            dialog.dismiss()

            when (position) {
                0 -> exportHistoryTxt()
                1 -> exportHistoryJson()
                2 -> exportPendingJson()
                3 -> createFullBackup()
                4 -> chooseBackupForRestore()
                5 -> shareHistoryTxt()
                6 -> confirmShareFullBackup()
                7 -> confirmRestoreSafetySnapshot()
            }
        }

        dialog.show()
    }

    private fun exportHistoryTxt() {
        val text =
            buildHistoryExportTxt()
                ?: return toast(
                    "Історія порожня — експортувати нічого"
                )

        createDocumentForExport(
            fileName = "YTM_History_${exportTimestamp()}.txt",
            mimeType = "text/plain",
            content = text,
            successMessage = "History TXT збережено"
        )
    }

    private fun buildHistoryExportTxt(): String? {
        val entries = historyStore.getAll()

        if (entries.isEmpty()) return null

        return buildString {
            append("YTM Importer — History export\n")
            append("Версія застосунку: ${BuildConfig.VERSION_NAME}\n")
            append(
                "Експортовано: " +
                    formatHistoryDate(
                        System.currentTimeMillis()
                    ) +
                    "\n"
            )
            append("Записів: ${entries.size}\n\n")

            entries.forEachIndexed { index, entry ->
                append(
                    "==================================================\n"
                )
                append("${index + 1}. ${entry.playlistName}\n")
                append(
                    "==================================================\n"
                )
                append(buildHistorySummary(entry))

                val problems =
                    buildHistoryProblemLog(entry)

                if (!problems.isNullOrBlank()) {
                    append("\n\n")
                    append(problems)
                }

                if (index != entries.lastIndex) {
                    append("\n\n")
                }
            }
        }
    }

    private fun exportHistoryJson() {
        val entries = historyStore.getAll()

        if (entries.isEmpty()) {
            return toast("Історія порожня — експортувати нічого")
        }

        createDocumentForExport(
            fileName = "YTM_History_${exportTimestamp()}.json",
            mimeType = "application/json",
            content = historyStore.exportJson(),
            successMessage = "History JSON збережено"
        )
    }

    private fun exportPendingJson() {
        val jobs = pendingJobStore.getAll()

        if (jobs.isEmpty()) {
            return toast("Черга порожня — експортувати нічого")
        }

        createDocumentForExport(
            fileName = "YTM_Pending_${exportTimestamp()}.json",
            mimeType = "application/json",
            content = pendingJobStore.exportJson(),
            successMessage = "Pending Queue JSON збережено"
        )
    }

    private fun createFullBackup() {
        val content =
            runCatching {
                localBackupManager.createBackupJson()
            }.getOrElse { error ->
                toast(
                    error.message
                        ?: "Не вдалося створити backup"
                )
                return
            }

        UiChrome.alertBuilder(this)
            .setTitle("Створити повний backup?")
            .setMessage(
                "У JSON буде збережено:\n" +
                    "• History\n" +
                    "• Pending Queue (Черга)\n" +
                    "• локальні quota counters\n" +
                    "• SearchCache\n\n" +
                    "Backup може містити Google email, " +
                    "YouTube Channel ID і назви плейлистів.\n\n" +
                    "OAuth access token, паролі та signing keys " +
                    "НЕ зберігаються.\n\n" +
                    "v1.0.0 також має SHA-256 integrity check, щоб " +
                    "пошкоджений backup не відновлювався мовчки."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Зберегти") { _, _ ->
                createDocumentForExport(
                    fileName =
                        "YTM_Backup_${exportTimestamp()}.json",
                    mimeType = "application/json",
                    content = content,
                    successMessage = "Повний backup збережено"
                )
            }
            .show()
    }

    private fun shareHistoryTxt() {
        val content =
            buildHistoryExportTxt()
                ?: return toast(
                    "Історія порожня — ділитися нічим"
                )

        shareTextFile(
            fileName = "YTM_History_${exportTimestamp()}.txt",
            mimeType = "text/plain",
            content = content,
            chooserTitle = "Поділитися History"
        )
    }

    private fun confirmShareFullBackup() {
        UiChrome.alertBuilder(this)
            .setTitle("Поділитися повним backup?")
            .setMessage(
                "Backup може містити:\n" +
                    "• Google email\n" +
                    "• YouTube Channel ID\n" +
                    "• назви плейлистів\n" +
                    "• History / Queue / SearchCache\n\n" +
                    "OAuth token, паролі та signing keys " +
                    "у файл не входять.\n\n" +
                    "Надсилайте backup тільки туди, де ви готові " +
                    "розкрити ці локальні дані."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Поділитися") { _, _ ->
                val content =
                    runCatching {
                        localBackupManager.createBackupJson()
                    }.getOrElse { error ->
                        toast(
                            error.message
                                ?: "Не вдалося створити backup"
                        )
                        return@setPositiveButton
                    }

                shareTextFile(
                    fileName =
                        "YTM_Backup_${exportTimestamp()}.json",
                    mimeType = "application/json",
                    content = content,
                    chooserTitle = "Поділитися YTM backup"
                )
            }
            .show()
    }

    private fun shareTextFile(
        fileName: String,
        mimeType: String,
        content: String,
        chooserTitle: String
    ) {
        runCatching {
            val directory =
                File(
                    cacheDir,
                    "shared_exports"
                ).apply {
                    mkdirs()
                }

            directory
                .listFiles()
                ?.filter {
                    System.currentTimeMillis() -
                        it.lastModified() >
                        24L * 60L * 60L * 1000L
                }
                ?.forEach(File::delete)

            val file =
                File(
                    directory,
                    fileName
                ).apply {
                    writeText(
                        content,
                        Charsets.UTF_8
                    )
                }

            val uri =
                FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    file
                )

            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    clipData =
                        ClipData.newRawUri(
                            fileName,
                            uri
                        )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    chooserTitle
                )
            )
        }.onFailure { error ->
            toast(
                "Не вдалося поділитися файлом: " +
                    (error.message ?: "невідома помилка")
            )
        }
    }

    private fun createDocumentForExport(
        fileName: String,
        mimeType: String,
        content: String,
        successMessage: String
    ) {
        pendingExportContent = content
        pendingExportSuccessMessage = successMessage

        val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

        runCatching {
            startActivityForResult(
                intent,
                saveExportRequestCode
            )
        }.onFailure { error ->
            pendingExportContent = null
            pendingExportSuccessMessage = null
            toast(
                "Не вдалося відкрити вибір файлу: " +
                    (error.message ?: "невідома помилка")
            )
        }
    }

    private fun writePendingExport(uri: Uri) {
        val content =
            pendingExportContent
                ?: return toast(
                    "Немає підготовлених даних для експорту"
                )

        val success =
            pendingExportSuccessMessage
                ?: "Файл збережено"

        runCatching {
            contentResolver.openOutputStream(
                uri,
                "w"
            )?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                writer.write(content)
            } ?: throw IllegalStateException(
                "Android не відкрив файл для запису"
            )
        }.onSuccess {
            toast(success)
        }.onFailure { error ->
            toast(
                "Помилка запису файлу: " +
                    (error.message ?: "невідома помилка")
            )
        }

        pendingExportContent = null
        pendingExportSuccessMessage = null
    }

    private fun chooseBackupForRestore() {
        UiChrome.alertBuilder(this)
            .setTitle("Відновити backup?")
            .setMessage(
                "Restore (відновлення) замінить локальні дані " +
                    "цих розділів даними з backup:\n\n" +
                    "• History\n" +
                    "• Черга\n" +
                    "• локальна квота\n" +
                    "• SearchCache\n\n" +
                    "YouTube/YTM плейлисти в інтернеті " +
                    "не змінюються."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Вибрати backup") { _, _ ->
                val intent =
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                    }

                startActivityForResult(
                    intent,
                    restoreBackupRequestCode
                )
            }
            .show()
    }

    private fun prepareRestoreBackup(uri: Uri) {
        val raw =
            runCatching {
                contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?: throw IllegalStateException(
                        "Не вдалося прочитати backup"
                    )
            }.getOrElse { error ->
                toast(
                    "Помилка читання backup: " +
                        (error.message ?: "невідома помилка")
                )
                return
            }

        val summary =
            runCatching {
                localBackupManager.inspectBackup(raw)
            }.getOrElse { error ->
                toast(
                    "Backup не підходить: " +
                        (error.message ?: "невідома помилка")
                )
                return
            }

        val exportedDate =
            if (summary.exportedAt > 0L) {
                formatHistoryDate(summary.exportedAt)
            } else {
                "невідомо"
            }

        UiChrome.alertBuilder(this)
            .setTitle("Підтвердити Restore")
            .setMessage(
                "Backup YTM Importer\n\n" +
                    "Версія backup schema: ${summary.schemaVersion}\n" +
                    "Версія застосунку при створенні: ${summary.appVersion}\n" +
                    "Дата backup: $exportedDate\n" +
                    "Груп даних: ${summary.preferenceGroups}\n" +
                    "Значень: ${summary.valueCount}\n" +
                    "Integrity: " +
                    if (summary.integrityProtected) {
                        if (summary.integrityVerified) {
                            "SHA-256 ✓\n\n"
                        } else {
                            "SHA-256 ?\n\n"
                        }
                    } else {
                        "legacy backup без checksum\n\n"
                    } +
                    "Перед Restore буде автоматично створено safety snapshot " +
                    "поточних History / Queue / Quota / Cache.\n\n" +
                    "Поточні локальні дані потім будуть замінені."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Відновити") { _, _ ->
                restoreBackupNow(raw)
            }
            .show()
    }

    private fun restoreBackupNow(raw: String) {
        val result =
            runCatching {
                localBackupManager.restoreBackupJson(raw)
            }.getOrElse { error ->
                toast(
                    "Restore не виконано: " +
                        (error.message ?: "невідома помилка")
                )
                return
            }

        updatePendingButton()
        updateQuotaPanel()

        UiChrome.alertBuilder(this)
            .setTitle("Backup відновлено")
            .setMessage(
                "Груп даних: ${result.preferenceGroups}\n" +
                    "Відновлено значень: ${result.restoredValues}\n\n" +
                    "History, Черга, локальна квота та SearchCache " +
                    "вже доступні без перевстановлення застосунку.\n\n" +
                    if (result.safetySnapshotCreated) {
                        "Safety snapshot стану ДО Restore збережено. " +
                            "Його можна використати через «Дані → " +
                            "Відкотити останній Restore»."
                    } else {
                        ""
                    }
            )
            .setNeutralButton("Відкотити") { _, _ ->
                confirmRestoreSafetySnapshot()
            }
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmRestoreSafetySnapshot() {
        val summary =
            runCatching {
                localBackupManager.inspectSafetySnapshot()
            }.getOrElse { error ->
                toast(
                    "Safety snapshot пошкоджено: " +
                        (error.message ?: "невідома помилка")
                )
                return
            }

        if (summary == null) {
            return toast(
                "Safety snapshot ще не створено. Він з'явиться " +
                    "автоматично перед першим Restore."
            )
        }

        val date =
            if (summary.exportedAt > 0L) {
                formatHistoryDate(summary.exportedAt)
            } else {
                "невідомо"
            }

        UiChrome.alertBuilder(this)
            .setTitle("Відкотити останній Restore?")
            .setMessage(
                "Буде відновлено локальний стан, який був ДО " +
                    "останнього Restore.\n\n" +
                    "Дата safety snapshot: $date\n" +
                    "Версія: ${summary.appVersion}\n" +
                    "Груп: ${summary.preferenceGroups}\n" +
                    "Значень: ${summary.valueCount}\n\n" +
                    "YouTube/YTM плейлисти в інтернеті не змінюються."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Відкотити") { _, _ ->
                restoreSafetySnapshotNow()
            }
            .show()
    }

    private fun restoreSafetySnapshotNow() {
        val result =
            runCatching {
                localBackupManager.restoreSafetySnapshot()
            }.getOrElse { error ->
                toast(
                    "Відкат не виконано: " +
                        (error.message ?: "невідома помилка")
                )
                return
            }

        updatePendingButton()
        updateQuotaPanel()

        UiChrome.alertBuilder(this)
            .setTitle("Відкат виконано")
            .setMessage(
                "Локальний стан ДО останнього Restore повернуто.\n\n" +
                    "Груп даних: ${result.preferenceGroups}\n" +
                    "Відновлено значень: ${result.restoredValues}."
            )
            .setNegativeButton("Видалити snapshot") { _, _ ->
                localBackupManager.clearSafetySnapshot()
                toast("Safety snapshot видалено")
            }
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportTimestamp(): String =
        SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(Date())


    private fun showHistory() {
        startActivity(
            Intent(
                this,
                HistoryActivity::class.java
            )
        )
    }

    private fun showHistoryEntry(entry: HistoryEntry) {
        val status = effectiveHistoryStatus(entry)
        val destination =
            when (entry.destination) {
                PendingDestination.NEW_PLAYLIST ->
                    "новий плейлист"
                PendingDestination.EXISTING_PLAYLIST ->
                    "існуючий плейлист"
            }

        val problems =
            entry.tracks.count(::isHistoryProblemTrack)

        val preview =
            entry.tracks
                .filter(::isHistoryProblemTrack)
                .take(6)
                .joinToString("\n") {
                    "• ${it.originalArtist} — ${it.originalTitle}: " +
                        historyReplacementLabel(it)
                }
                .takeIf { it.isNotBlank() }

        UiChrome.showMessageDialog(
            activity = this,
            title =
                "${historyStatusIcon(status)} ${entry.playlistName}",
            message =
                buildString {
                    append("Статус: ${historyStatusLabel(status)}\n")
                    append("Дата: ${formatHistoryDate(entry.updatedAt)}\n")
                    append("Джерело: ${entry.sourceLabel}\n")
                    append("Тип: $destination\n")
                    append("Приватність: ${privacyLabel(entry.privacyStatus)}\n\n")

                    append("Імпортовано: ${entry.totalImportedCount}\n")
                    append("Для запису: ${entry.writeTargetCount}\n")
                    append("Додано: ${entry.addedCount}\n")
                    append("Помилок: ${entry.failedCount}\n")
                    append("Очікує: ${entry.pendingCount}\n")
                    append("Пропущено вручну: ${entry.skippedCount}\n")
                    append("Дублікатів пропущено: ${entry.duplicateCount}\n")
                    append("Не знайдено: ${entry.missingCount}\n")
                    append("Проблемних/замінених: $problems\n\n")

                    append("Google: ${entry.googleEmail ?: "—"}\n")
                    append("YouTube/YTM: ${entry.youtubeChannelTitle ?: "—"}\n")
                    append("Channel ID: ${entry.youtubeChannelId ?: "—"}\n")
                    append("Playlist ID: ${entry.playlistId ?: "—"}")

                    if (!entry.lastError.isNullOrBlank()) {
                        append("\n\nОстання помилка:\n${entry.lastError}")
                    }

                    if (!preview.isNullOrBlank()) {
                        append("\n\nПроблемні треки:\n")
                        append(preview)

                        if (problems > 6) {
                            append("\n…ще ${problems - 6}")
                        }
                    }
                },
            actions = listOf(
                UiChrome.DialogAction("Дії") {
                    showHistoryActions(entry)
                },
                UiChrome.DialogAction(
                    if (entry.playlistId.isNullOrBlank()) {
                        "Черга"
                    } else {
                        "YTM"
                    }
                ) {
                    if (entry.playlistId.isNullOrBlank()) {
                        showPendingJobs()
                    } else {
                        openPlaylistIdInYtm(entry.playlistId)
                    }
                },
                UiChrome.DialogAction(
                    label = "Назад",
                    tone = UiChrome.ActionTone.ACCENT
                ) {
                    showHistory()
                }
            )
        )
    }

    private fun showHistoryActions(entry: HistoryEntry) {
        val actions =
            mutableListOf<UiChrome.MenuAction>()

        if (!entry.playlistId.isNullOrBlank()) {
            actions +=
                UiChrome.MenuAction(
                    "Копіювати посилання на плейлист"
                ) {
                    entry.playlistId
                        ?.let { id ->
                            copyText(
                                label = "YTM playlist",
                                text = playlistUrl(id),
                                successMessage = "Посилання скопійовано"
                            )
                        }
                    showHistoryEntry(entry)
                }
        }

        actions +=
            UiChrome.MenuAction(
                "Зберегти YTM Project"
            ) {
                confirmHistoryProjectScope(
                    entry = entry,
                    actionLabel = "Зберегти"
                ) {
                    saveHistoryProject(entry)
                }
            }

        actions +=
            UiChrome.MenuAction(
                "Поділитися YTM Project"
            ) {
                confirmHistoryProjectScope(
                    entry = entry,
                    actionLabel = "Поділитися"
                ) {
                    shareHistoryProject(entry)
                }
            }

        actions +=
            UiChrome.MenuAction(
                "Копіювати підсумок"
            ) {
                copyText(
                    label = "YTM Importer history summary",
                    text = buildHistorySummary(entry),
                    successMessage = "Підсумок історії скопійовано"
                )
                showHistoryEntry(entry)
            }

        actions +=
            UiChrome.MenuAction(
                "Копіювати журнал проблем"
            ) {
                val text =
                    buildHistoryProblemLog(entry)

                if (text == null) {
                    toast(
                        "У цьому записі немає проблемних або замінених треків"
                    )
                } else {
                    copyText(
                        label = "YTM Importer history problems",
                        text = text,
                        successMessage = "Журнал проблем скопійовано"
                    )
                }

                showHistoryEntry(entry)
            }

        actions +=
            UiChrome.MenuAction(
                "Видалити запис з історії"
            ) {
                confirmDeleteHistoryEntry(entry)
            }

        UiChrome.showMenuDialog(
            activity = this,
            title = "Дії з історією",
            subtitle = "Project, копіювання та локальне керування записом.",
            negativeLabel = "Назад",
            actions = actions,
            onNegative = {
                showHistoryEntry(entry)
            }
        )
    }

    private fun confirmHistoryProjectScope(
        entry: HistoryEntry,
        actionLabel: String,
        after: () -> Unit
    ) {
        if (
            entry.destination ==
                PendingDestination.NEW_PLAYLIST
        ) {
            after()
            return
        }

        UiChrome.alertBuilder(this)
            .setTitle("$actionLabel YTM Project?")
            .setMessage(
                "Цей запис History був додаванням до вже існуючого " +
                    "плейлиста.\n\n" +
                    "Тому проект міститиме тільки треки цієї конкретної " +
                    "операції імпорту, а НЕ повний вміст існуючого " +
                    "плейлиста в YouTube/YTM.\n\n" +
                    "Для повторного використання цього import batch " +
                    "(пакета імпорту) це правильний формат."
            )
            .setNegativeButton("Скасувати") { _, _ ->
                showHistoryEntry(entry)
            }
            .setPositiveButton(actionLabel) { _, _ ->
                after()
            }
            .show()
    }

    private fun saveHistoryProject(
        entry: HistoryEntry
    ) {
        val content =
            PlaylistProjectCodec.exportHistoryEntry(
                entry = entry,
                appVersion = BuildConfig.VERSION_NAME
            )

        createDocumentForExport(
            fileName =
                historyProjectFileName(entry),
            mimeType =
                "application/json",
            content = content,
            successMessage =
                "YTM Project збережено"
        )
    }

    private fun shareHistoryProject(
        entry: HistoryEntry
    ) {
        val content =
            PlaylistProjectCodec.exportHistoryEntry(
                entry = entry,
                appVersion = BuildConfig.VERSION_NAME
            )

        shareTextFile(
            fileName =
                historyProjectFileName(entry),
            mimeType =
                "application/json",
            content = content,
            chooserTitle =
                "Поділитися YTM Project"
        )
    }

    private fun historyProjectFileName(
        entry: HistoryEntry
    ): String {
        val safeName =
            entry.playlistName
                .replace(
                    Regex("[^\\p{L}\\p{N}._-]+"),
                    "_"
                )
                .trim('_')
                .take(60)
                .ifBlank {
                    "playlist"
                }

        return "YTM_Project_${safeName}_" +
            "${exportTimestamp()}.ytm.json"
    }

    private fun buildHistorySummary(entry: HistoryEntry): String =
        buildString {
            val status = effectiveHistoryStatus(entry)

            append("YTM Importer — історія\n")
            append("Плейлист: ${entry.playlistName}\n")
            append("Статус: ${historyStatusLabel(status)}\n")
            append("Дата: ${formatHistoryDate(entry.updatedAt)}\n")
            append("Джерело: ${entry.sourceLabel}\n")
            append("Додано: ${entry.addedCount}/${entry.writeTargetCount}\n")
            append("Помилок: ${entry.failedCount}\n")
            append("Очікує: ${entry.pendingCount}\n")
            append("Пропущено вручну: ${entry.skippedCount}\n")
            append("Дублікатів пропущено: ${entry.duplicateCount}\n")
            append("Не знайдено: ${entry.missingCount}\n")

            if (!entry.playlistId.isNullOrBlank()) {
                append("YTM: ${playlistUrl(entry.playlistId)}\n")
            }

            if (!entry.lastError.isNullOrBlank()) {
                append("Остання помилка: ${entry.lastError}\n")
            }
        }.trimEnd()

    private fun buildHistoryProblemLog(entry: HistoryEntry): String? {
        val tracks = entry.tracks.filter(::isHistoryProblemTrack)
        if (tracks.isEmpty()) return null

        return buildString {
            append("YTM Importer — історичний журнал проблем\n")
            append("Плейлист: ${entry.playlistName}\n\n")

            tracks.forEachIndexed { index, track ->
                append(index + 1)
                append(". ")
                append(track.originalArtist)
                append(" — ")
                append(track.originalTitle)
                append(" → ")
                append(historyReplacementLabel(track))

                if (!track.selectedChannel.isNullOrBlank()) {
                    append("\n   Канал: ${track.selectedChannel}")
                }

                if (!track.videoId.isNullOrBlank()) {
                    append(
                        "\n   YTM: https://music.youtube.com/watch?v=${track.videoId}"
                    )
                }

                if (!track.error.isNullOrBlank()) {
                    append("\n   Помилка: ${track.error}")
                }

                if (index != tracks.lastIndex) {
                    append("\n\n")
                }
            }
        }
    }

    private fun isHistoryProblemTrack(track: HistoryTrack): Boolean =
        track.manuallySelected ||
            track.status == TrackStatus.SKIPPED.name ||
            track.status == TrackStatus.DUPLICATE.name ||
            track.status == TrackStatus.MISSING.name ||
            track.status == TrackStatus.PENDING.name ||
            track.status == TrackStatus.FAILED.name

    private fun historyReplacementLabel(track: HistoryTrack): String =
        when {
            track.status == TrackStatus.SKIPPED.name ->
                "[пропущено]"

            track.status == TrackStatus.DUPLICATE.name ->
                "[дублікат — уже є у плейлисті]"

            track.status == TrackStatus.MISSING.name ->
                "[не знайдено]"

            track.status == TrackStatus.PENDING.name ->
                "[очікує в черзі]"

            track.status == TrackStatus.FAILED.name &&
                track.selectedTitle.isNullOrBlank() ->
                "[помилка]"

            track.selectedTitle == "Ручне посилання" ->
                "[ручне YouTube/YTM посилання]"

            !track.selectedTitle.isNullOrBlank() ->
                track.selectedTitle

            else ->
                "[без заміни]"
        }

    private fun confirmDeleteHistoryEntry(entry: HistoryEntry) {
        UiChrome.alertBuilder(this)
            .setTitle("Видалити запис історії?")
            .setMessage(
                "Буде видалено тільки локальний запис «${entry.playlistName}». " +
                    "Плейлист у YouTube/YTM не зміниться."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Видалити") { _, _ ->
                historyStore.remove(entry.id)
                toast("Запис видалено з історії")

                if (historyStore.getAll().isNotEmpty()) {
                    showHistory()
                }
            }
            .show()
    }

    private fun confirmClearHistory() {
        UiChrome.alertBuilder(this)
            .setTitle("Очистити всю історію?")
            .setMessage(
                "Будуть видалені тільки локальні записи історії. " +
                    "Плейлисти YouTube/YTM і Черга не видаляються."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Очистити") { _, _ ->
                historyStore.clear()
                toast("Історію очищено")
            }
            .show()
    }

    private fun effectiveHistoryStatus(entry: HistoryEntry): HistoryStatus {
        if (
            entry.status == HistoryStatus.RUNNING &&
            pendingJobStore.getAll().any { it.id == entry.id }
        ) {
            return HistoryStatus.PARTIAL
        }

        return entry.status
    }

    private fun historyStatusLabel(status: HistoryStatus): String =
        when (status) {
            HistoryStatus.RUNNING ->
                "виконується"

            HistoryStatus.COMPLETED ->
                "завершено"

            HistoryStatus.PARTIAL ->
                "частково / очікує продовження"

            HistoryStatus.PENDING_QUOTA ->
                "очікує квоти"

            HistoryStatus.FAILED ->
                "помилка"
        }

    private fun historyStatusIcon(status: HistoryStatus): String =
        when (status) {
            HistoryStatus.RUNNING -> "▶"
            HistoryStatus.COMPLETED -> "✓"
            HistoryStatus.PARTIAL -> "◐"
            HistoryStatus.PENDING_QUOTA -> "⏳"
            HistoryStatus.FAILED -> "×"
        }

    private fun formatHistoryDate(timestamp: Long): String =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(timestamp))


    private fun showTrackDialog(track: Track) {
        val candidates = track.candidates
        val actions =
            mutableListOf<UiChrome.MenuAction>()

        candidates.forEachIndexed { index, candidate ->
            val selectedMark =
                if (candidate.videoId == track.selectedVideoId) {
                    "✓ "
                } else {
                    ""
                }

            actions +=
                UiChrome.MenuAction(
                    "$selectedMark${index + 1}. " +
                        "${(candidate.score * 100).roundToInt()}%  " +
                        "${candidate.title} • ${candidate.channelTitle}"
                ) {
                    showCandidateDialog(
                        track,
                        candidate
                    )
                }
        }

        actions +=
            UiChrome.MenuAction(
                "🔗 Вставити YouTube / YouTube Music URL"
            ) {
                showPasteUrlDialog(track)
            }

        actions +=
            UiChrome.MenuAction(
                "⏭ Пропустити цей трек"
            ) {
                track.status = TrackStatus.SKIPPED
                track.selectedVideoId = null
                track.selectedTitle = null
                track.selectedChannel = null
                track.manuallySelected = true
                adapter.notifyDataSetChanged()
                updateSummary()
                status(
                    "Пропущено: ${track.originalArtist} — ${track.originalTitle}"
                )
            }

        UiChrome.showMenuDialog(
            activity = this,
            title = "${track.originalArtist} — ${track.originalTitle}",
            subtitle =
                if (candidates.isEmpty()) {
                    "Кандидатів немає"
                } else {
                    "Кандидати: ${candidates.size}"
                },
            actions = actions
        )
    }

    private fun showCandidateDialog(
        track: Track,
        candidate: SearchCandidate
    ) {
        val scorePercent = (candidate.score * 100).roundToInt()
        val isCurrent = candidate.videoId == track.selectedVideoId

        UiChrome.showMessageDialog(
            activity = this,
            title = candidate.title,
            message =
                buildString {
                    append("Канал: ${candidate.channelTitle}\n")
                    append("Збіг: $scorePercent%")
                    if (isCurrent) {
                        append("\n\n✓ Зараз вибрано для цього треку")
                    }
                },
            actions = listOf(
                UiChrome.DialogAction(
                    if (isCurrent) "Залишити" else "Використати"
                ) {
                    applyCandidate(track, candidate, manual = true)
                    track.status = TrackStatus.MATCHED
                    adapter.notifyDataSetChanged()
                    updateSummary()

                    status(
                        "Вибрано вручну: ${track.originalArtist} — " +
                            "${track.originalTitle} → ${candidate.title}"
                    )
                },
                UiChrome.DialogAction("YTM") {
                    openCandidateInYtm(candidate.videoId)
                },
                UiChrome.DialogAction(
                    label = "Назад",
                    tone = UiChrome.ActionTone.ACCENT
                ) {
                    showTrackDialog(track)
                }
            )
        )
    }

    private fun openCandidateInYtm(videoId: String) {
        val uri = Uri.parse("https://music.youtube.com/watch?v=$videoId")

        val ytmIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.youtube.music")
        }

        val openedInYtm =
            runCatching {
                startActivity(ytmIntent)
                true
            }.getOrDefault(false)

        if (openedInYtm) return

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            toast("Не вдалося відкрити кандидат у YouTube Music")
        }
    }

    private fun showPasteUrlDialog(track: Track) {
        val input = EditText(this).apply {
            hint = "https://music.youtube.com/watch?v=…"
            setSingleLine(true)
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        UiChrome.alertBuilder(this)
            .setTitle("Вставити посилання")
            .setMessage(
                "Програма спробує отримати з YouTube реальну назву " +
                    "та канал цього відео. Оригінальна назва треку " +
                    "залишиться в історії як джерело заміни."
            )
            .setView(input)
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Використати") { _, _ ->
                val videoId = extractVideoId(input.text.toString())

                if (videoId == null) {
                    toast("Не бачу YouTube video ID у посиланні")
                } else {
                    applyManualUrl(
                        track = track,
                        videoId = videoId
                    )
                }
            }
            .show()
    }

    private fun applyManualUrl(
        track: Track,
        videoId: String,
        reopenReviewHistoryIndex: Int? = null
    ) {
        authorize {
            val token = accessToken ?: return@authorize

            status(
                "Отримую назву ручної заміни для " +
                    "${track.originalArtist} — ${track.originalTitle}…"
            )

            executor.execute {
                quotaTracker.recordGeneralUnits(
                    QuotaTracker.SIMPLE_LIST_COST
                )

                val result =
                    runCatching {
                        api.getVideoInfo(
                            accessToken = token,
                            videoId = videoId
                        )
                    }

                runOnUiThread {
                    updateQuotaPanel()

                    result.onSuccess { videoInfo ->
                        if (videoInfo != null) {
                            val targetTrack =
                                resolveCurrentTrack(
                                    fallbackTrack = track,
                                    historyIndex =
                                        reopenReviewHistoryIndex
                                            ?: track.historyIndex
                                )

                            applyCandidate(
                                track = targetTrack,
                                candidate = videoInfo,
                                manual = true
                            )
                            targetTrack.status = TrackStatus.MATCHED
                            targetTrack.error = null

                            adapter.notifyDataSetChanged()
                            updateSummary()

                            status(
                                "Ручна заміна: " +
                                    "${targetTrack.originalArtist} — " +
                                    "${targetTrack.originalTitle} → " +
                                    videoInfo.title
                            )

                            (
                                reopenReviewHistoryIndex
                                    ?: targetTrack.historyIndex
                            )?.let {
                                openReviewScreen(it)
                            }
                        } else {
                            applyManualUrlFallback(
                                track = track,
                                videoId = videoId,
                                reason =
                                    "YouTube не повернув назву цього відео",
                                reopenReviewHistoryIndex =
                                    reopenReviewHistoryIndex
                            )
                        }
                    }.onFailure { error ->
                        applyManualUrlFallback(
                            track = track,
                            videoId = videoId,
                            reason =
                                ErrorMessages.userMessage(
                                    error,
                                    "Не вдалося отримати назву відео"
                                ),
                            reopenReviewHistoryIndex =
                                reopenReviewHistoryIndex
                        )
                    }
                }
            }
        }
    }

    private fun applyManualUrlFallback(
        track: Track,
        videoId: String,
        reason: String,
        reopenReviewHistoryIndex: Int? = null
    ) {
        val targetTrack =
            resolveCurrentTrack(
                fallbackTrack = track,
                historyIndex =
                    reopenReviewHistoryIndex
                        ?: track.historyIndex
            )

        targetTrack.selectedVideoId = videoId
        targetTrack.selectedTitle = "YouTube video $videoId"
        targetTrack.selectedChannel = "метадані не завантажено"
        targetTrack.status = TrackStatus.MATCHED
        targetTrack.manuallySelected = true
        targetTrack.error = null

        adapter.notifyDataSetChanged()
        updateSummary()

        status(
            "Посилання збережено, але назву не вдалося отримати: $reason"
        )

        toast(
            "Посилання використано. Назву можна перевірити у YTM."
        )

        (
            reopenReviewHistoryIndex
                ?: targetTrack.historyIndex
        )?.let {
            openReviewScreen(it)
        }
    }

    private fun resolveCurrentTrack(
        fallbackTrack: Track,
        historyIndex: Int?
    ): Track {
        val index =
            historyIndex
                ?: fallbackTrack.historyIndex

        return playlist
            ?.tracks
            ?.firstOrNull { candidate ->
                candidate.historyIndex == index
            }
            ?: fallbackTrack
    }

    private fun extractVideoId(value: String): String? {
        val text = value.trim()

        val regexes =
            listOf(
                Regex("[?&]v=([A-Za-z0-9_-]{11})"),
                Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
                Regex("youtube\\.com/shorts/([A-Za-z0-9_-]{11})"),
                Regex("youtube\\.com/live/([A-Za-z0-9_-]{11})"),
                Regex("^([A-Za-z0-9_-]{11})$")
            )

        return regexes.firstNotNullOfOrNull {
            it.find(text)?.groupValues?.getOrNull(1)
        }
    }

    private fun applyCandidate(
        track: Track,
        candidate: SearchCandidate,
        manual: Boolean
    ) {
        track.selectedVideoId = candidate.videoId
        track.selectedTitle = candidate.title
        track.selectedChannel = candidate.channelTitle
        track.manuallySelected = manual
        track.error = null
    }

    private fun showPlaylistResult(
        playlistName: String,
        addedCount: Int,
        failedCount: Int,
        privacyStatus: String,
        operationLabel: String
    ) {
        val url = playlistUrl() ?: return

        resultTitleText.text = "✓ $playlistName"
        resultDetailsText.text =
            buildString {
                append("Додано: $addedCount")
                if (failedCount > 0) append(" • Не додано: $failedCount")

                val duplicateCount =
                    playlist?.tracks?.count {
                        it.status == TrackStatus.DUPLICATE
                    } ?: 0

                if (duplicateCount > 0) {
                    append(" • Дублікати: $duplicateCount")
                }

                append(" • ${privacyLabel(privacyStatus)}")
                append(" • $operationLabel")
                youtubeChannelInfo?.title?.let {
                    append("\nYouTube/YTM: $it")
                }
            }
        resultLinkText.text = url
        resultPanel.visibility = View.VISIBLE
    }

    private fun playlistUrl(): String? {
        val id = createdPlaylistId ?: return null
        return playlistUrl(id)
    }

    private fun playlistUrl(playlistId: String): String =
        "https://music.youtube.com/playlist?list=$playlistId"

    private fun openInYtm() {
        val id =
            createdPlaylistId
                ?: return toast("Спочатку створіть або виберіть плейлист")

        openPlaylistIdInYtm(id)
    }

    private fun openPlaylistIdInYtm(playlistId: String) {
        val uri = Uri.parse(playlistUrl(playlistId))

        val ytmIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.youtube.music")
        }

        val openedInYtm =
            runCatching {
                startActivity(ytmIntent)
                true
            }.getOrDefault(false)

        if (openedInYtm) return

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            toast("Не вдалося відкрити YouTube Music")
        }
    }

    private fun copyPlaylistLink() {
        val url = playlistUrl() ?: return toast("Спочатку створіть або виберіть плейлист")

        val clipboard =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText("YTM playlist", url)
        )

        toast("Посилання на плейлист скопійовано")
    }

    private fun showReplacementLog() {
        val p = playlist ?: return toast("Немає імпортованого плейлиста")

        val problemTracks =
            p.tracks.filter { track ->
                track.manuallySelected ||
                    track.status == TrackStatus.SKIPPED ||
                    track.status == TrackStatus.DUPLICATE ||
                    track.status == TrackStatus.MISSING ||
                    track.status == TrackStatus.PENDING ||
                    track.status == TrackStatus.FAILED
            }

        if (problemTracks.isEmpty()) {
            return toast("Замін, пропусків або проблемних треків поки немає")
        }

        val shortText = buildShortReplacementText(problemTracks)
        val fullText = buildFullReplacementText(problemTracks)

        UiChrome.showRecordDialog(
            activity = this,
            title = "Заміни / проблемні треки: ${problemTracks.size}",
            subtitle = "Кожна позиція показана окремою плиткою.",
            records =
                problemTracks.mapIndexed { index, track ->
                    UiChrome.DialogRecord(
                        title =
                            "${index + 1}. " +
                                "${track.originalArtist} — ${track.originalTitle}",
                        detail =
                            replacementRecordLabel(track),
                        tone =
                            if (track.manuallySelected) {
                                UiChrome.ActionTone.ACCENT
                            } else {
                                UiChrome.ActionTone.NORMAL
                            }
                    )
                },
            actions = listOf(
                UiChrome.DialogAction("TikTok список") {
                    copyText(
                        label = "YTM Importer TikTok replacements",
                        text = shortText,
                        successMessage = "Короткий список для TikTok скопійовано"
                    )
                },
                UiChrome.DialogAction("Повний текст") {
                    copyText(
                        label = "YTM Importer replacement log",
                        text = fullText,
                        successMessage = "Повний журнал скопійовано"
                    )
                },
                UiChrome.DialogAction(
                    label = "Закрити",
                    tone = UiChrome.ActionTone.ACCENT
                ) {}
            ),
            actionLayout =
                UiChrome.DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE
        )
    }

    private fun replacementRecordLabel(track: Track): String =
        when {
            track.manuallySelected &&
                !track.selectedTitle.isNullOrBlank() ->
                buildString {
                    append("Ручний вибір: ")
                    append(track.selectedTitle)
                    if (!track.selectedChannel.isNullOrBlank()) {
                        append(" • ")
                        append(track.selectedChannel)
                    }
                }

            track.status == TrackStatus.SKIPPED ->
                "Пропущено"

            track.status == TrackStatus.DUPLICATE ->
                "Дублікат у цільовому плейлисті"

            track.status == TrackStatus.MISSING ->
                "Не знайдено"

            track.status == TrackStatus.PENDING ->
                "Очікує в Pending Queue"

            track.status == TrackStatus.FAILED ->
                track.error
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "Помилка: $it" }
                    ?: "Помилка"

            else ->
                replacementLabel(track)
        }

    private fun buildShortReplacementText(tracks: List<Track>): String =
        buildString {
            append("Заміни / недоступні треки:\n")

            tracks.forEachIndexed { index, track ->
                append(index + 1)
                append(". ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append(" → ")
                append(replacementLabel(track))

                if (index != tracks.lastIndex) {
                    append('\n')
                }
            }
        }

    private fun buildFullReplacementText(tracks: List<Track>): String =
        buildString {
            append("YTM Importer — журнал замін\n\n")

            tracks.forEachIndexed { index, track ->
                append(index + 1)
                append(". Оригінал: ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append('\n')

                append("   Результат: ")
                append(replacementLabel(track))
                append('\n')

                if (!track.selectedChannel.isNullOrBlank()) {
                    append("   Канал: ")
                    append(track.selectedChannel)
                    append('\n')
                }

                if (!track.selectedVideoId.isNullOrBlank()) {
                    append("   YTM: https://music.youtube.com/watch?v=")
                    append(track.selectedVideoId)
                    append('\n')
                }

                if (!track.error.isNullOrBlank()) {
                    append("   Помилка: ")
                    append(track.error)
                    append('\n')
                }

                if (index != tracks.lastIndex) {
                    append('\n')
                }
            }
        }

    private fun replacementLabel(track: Track): String =
        when {
            track.status == TrackStatus.SKIPPED -> "[пропущено]"
            track.status == TrackStatus.DUPLICATE ->
                "[дублікат — write-запит пропущено]"
            track.status == TrackStatus.MISSING -> "[не знайдено]"
            track.status == TrackStatus.PENDING -> "[очікує в черзі]"
            track.status == TrackStatus.FAILED &&
                track.selectedTitle.isNullOrBlank() -> "[помилка]"
            track.selectedTitle == "Ручне посилання" ->
                "[ручне YouTube/YTM посилання]"
            !track.selectedTitle.isNullOrBlank() ->
                track.selectedTitle!!
            else ->
                "[не знайдено]"
        }

    private fun copyText(
        label: String,
        text: String,
        successMessage: String
    ) {
        val clipboard =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(label, text)
        )

        toast(successMessage)
    }

    private fun privacyLabel(value: String): String =
        when (value) {
            "public" -> "публічний"
            "unlisted" -> "за посиланням"
            else -> "приватний"
        }

    private fun updateSummary() {
        val p = playlist ?: return

        val matched =
            p.tracks.count {
                it.status in setOf(TrackStatus.MATCHED, TrackStatus.ADDED)
            }

        val review =
            p.tracks.count {
                it.status == TrackStatus.REVIEW
            }

        val duplicates =
            p.tracks.count {
                it.status == TrackStatus.DUPLICATE
            }

        val pending =
            p.tracks.count {
                it.status == TrackStatus.PENDING
            }

        val missing =
            p.tracks.count {
                it.status in setOf(TrackStatus.MISSING, TrackStatus.FAILED)
            }

        summaryText.text =
            "${p.name} • ${p.tracks.size} треків • " +
                "✓ $matched  ! $review  ⧉ $duplicates  " +
                "⏳ $pending  × $missing"

        persistCurrentWorkspace()
        updatePrimaryActions()
    }

    private fun refreshRow(index: Int) {
        runOnUiThread {
            if (index in visibleTracks.indices) {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun status(message: String) {
        statusText.text = message
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        status(message)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    companion object {
        private const val KEY_WELCOME_SEEN =
            "welcome_v1_1_seen"

        private const val YOUTUBE_SCOPE =
            "https://www.googleapis.com/auth/youtube.force-ssl"

        private const val USERINFO_EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email"

        private const val USERINFO_PROFILE_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile"
    }
}
