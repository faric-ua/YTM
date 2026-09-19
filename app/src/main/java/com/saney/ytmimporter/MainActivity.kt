package com.saney.ytmimporter

import android.app.Activity
import android.content.ClipData
import android.content.res.ColorStateList
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.model.GoogleAccountInfo
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.PendingJob
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.model.YouTubeChannelInfo
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.PendingJobStore
import com.saney.ytmimporter.storage.QuotaTracker
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.HomeDashboardChrome
import com.saney.ytmimporter.ui.TrackAdapter
import com.saney.ytmimporter.ui.UiChrome
import com.saney.ytmimporter.util.ErrorMessages
import com.saney.ytmimporter.destination.DestinationCoordinator
import com.saney.ytmimporter.search.SearchCoordinator
import com.saney.ytmimporter.write.PlaylistWriteCoordinator
import com.saney.ytmimporter.youtube.SearchCache
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val pendingQueueRequestCode = 1201
    private val importScreenRequestCode = 1301
    private val reviewScreenRequestCode = 1302
    private val destinationScreenRequestCode = 1401
    private val menuScreenRequestCode = 1501
    private val quotaScreenRequestCode = 1502
    private val playlistScreenRequestCode = 1601
    private val authRequestCode = 9001
    private val executor = Executors.newSingleThreadExecutor()
    private val api = YouTubeApi()

    private lateinit var searchCoordinator: SearchCoordinator
    private lateinit var destinationCoordinator: DestinationCoordinator
    private lateinit var playlistWriteCoordinator: PlaylistWriteCoordinator
    private lateinit var quotaTracker: QuotaTracker
    private lateinit var pendingJobStore: PendingJobStore
    private lateinit var historyStore: HistoryStore
    private lateinit var currentPlaylistStore: CurrentPlaylistStore
    private lateinit var persistentAuthStateStore: PersistentAuthStateStore

    private var playlist: ImportedPlaylist? = null
    private var accessToken: String? = null
    private var googleAccountInfo: GoogleAccountInfo? = null
    private var youtubeChannelInfo: YouTubeChannelInfo? = null
    private var createdPlaylistId: String? = null
    private var pendingAfterAuth: (() -> Unit)? = null
    private var currentImportSourceLabel: String = "Невідоме джерело"
    private var restoringPriorAuthorization: Boolean = false

    private lateinit var accountSummaryText: TextView
    private lateinit var statusText: TextView
    private lateinit var summaryText: TextView
    private lateinit var importButton: Button
    private lateinit var accountButton: Button
    private lateinit var searchButton: Button
    private lateinit var createButton: Button
    private lateinit var quotaButton: Button
    private lateinit var pendingButton: Button
    private lateinit var progress: ProgressBar
    private var accountDialogOpen = false
    private var returnToPlaylistHubAfterDelegatedAction =
        false

    private val uiPrefs by lazy {
        getSharedPreferences("ui_prefs_v1", MODE_PRIVATE)
    }
    private lateinit var adapter: TrackAdapter
    private val visibleTracks = mutableListOf<Track>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)
        val searchCache =
            SearchCache(this)
        quotaTracker =
            QuotaTracker(this)
        destinationCoordinator =
            DestinationCoordinator(
                api = api,
                quotaTracker = quotaTracker
            )
        searchCoordinator =
            SearchCoordinator(
                api = api,
                searchCache = searchCache,
                quotaTracker = quotaTracker
            )
        pendingJobStore = PendingJobStore(this)
        playlistWriteCoordinator =
            PlaylistWriteCoordinator(
                api = api,
                pendingJobStore = pendingJobStore,
                quotaTracker = quotaTracker
            )
        historyStore = HistoryStore(this)
        currentPlaylistStore = CurrentPlaylistStore(this)
        persistentAuthStateStore =
            PersistentAuthStateStore(this)

        restoreAuthSessionFromMemory()

        restoringPriorAuthorization =
            accessToken.isNullOrBlank() &&
                persistentAuthStateStore
                    .hadSuccessfulAuthorization()

        accountDialogOpen =
            savedInstanceState?.getBoolean(
                STATE_ACCOUNT_DIALOG_OPEN,
                false
            ) == true

        returnToPlaylistHubAfterDelegatedAction =
            savedInstanceState
                ?.getBoolean(
                    STATE_RETURN_TO_PLAYLIST_HUB,
                    false
                )
                ?: false

        buildUi()
        updateAccountPanel()
        restoreCurrentWorkspaceOnLaunch()

        val restoredToken =
            accessToken

        if (
            !restoredToken.isNullOrBlank() &&
            (
                googleAccountInfo == null ||
                    youtubeChannelInfo == null
            )
        ) {
            loadAccountIdentity(
                token = restoredToken,
                after = null
            )
        } else if (restoringPriorAuthorization) {
            restorePriorAuthorizationSilently()
        }

        if (savedInstanceState == null) {
            window.decorView.post {
                maybeShowWelcome()
            }
        } else if (accountDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showAccountDialog()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_ACCOUNT_DIALOG_OPEN,
            accountDialogOpen
        )
        outState.putBoolean(
            STATE_RETURN_TO_PLAYLIST_HUB,
            returnToPlaylistHubAfterDelegatedAction
        )
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        syncAuthorizationInvalidationFromMemory()

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
        val palette = AppThemeManager.palette(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(palette.background)
        }

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
                overScrollMode =
                    View.OVER_SCROLL_IF_CONTENT_SCROLLS
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
                setPadding(
                    0,
                    0,
                    0,
                    dp(8)
                )
            }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(8))
        }

        val logoView =
            ImageView(this).apply {
                setImageResource(
                    R.drawable.ic_ytm_music
                )
                imageTintList =
                    ColorStateList.valueOf(
                        Color.WHITE
                    )
                setPadding(
                    dp(9),
                    dp(9),
                    dp(9),
                    dp(9)
                )
                background =
                    AppThemeManager
                        .accentCircleDrawable(
                            this@MainActivity
                        )
            }

        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        titleBlock.addView(
            TextView(this).apply {
                text = "YTM Importer"
                textSize = 21.5f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }
        )

        titleBlock.addView(
            TextView(this).apply {
                text = "Імпорт трекліста → YouTube Music"
                textSize = 12.5f
                setTextColor(palette.muted)
                setPadding(0, dp(2), 0, 0)
            }
        )

        header.addView(
            logoView,
            LinearLayout.LayoutParams(
                dp(42),
                dp(42)
            ).apply {
                marginEnd =
                    dp(9)
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
                    AppThemeManager.surfaceDrawable(
                        context = this@MainActivity,
                        fill = palette.surfaceAlt,
                        radiusDp = 18,
                        accentStroke = false
                    )
            }
        )

        content.addView(header)

        val flowCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background =
                AppThemeManager.surfaceDrawable(
                    context = this@MainActivity,
                    fill = palette.surface,
                    radiusDp = 16,
                    accentStroke = true
                )
        }

        flowCard.addView(
            TextView(this).apply {
                text = "4 кроки до плейлиста"
                textSize = 12f
                setTextColor(palette.muted)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(2), 0, 0, dp(8))
            }
        )

        importButton =
            HomeDashboardChrome
                .workflowButton(
                    activity = this,
                    label = "1. Імпорт",
                    primary = false
                ) {
                    openImportScreen()
                }

        accountButton =
            HomeDashboardChrome
                .workflowButton(
                    activity = this,
                    label = "2. Google / YTM",
                    primary = false
                ) {
                    showAccountDialog()
                }

        searchButton =
            HomeDashboardChrome
                .workflowButton(
                    activity = this,
                    label = "3. Знайти / перевірити",
                    primary = true
                ) {
                    searchOrReview()
                }.apply {
                isEnabled = false
                alpha = 0.55f
            }

        createButton =
            HomeDashboardChrome
                .workflowButton(
                    activity = this,
                    label = "4. Створити / додати",
                    primary = true
                ) {
                    createPlaylist()
                }.apply {
                isEnabled = false
                alpha = 0.55f
            }

        flowCard.addView(
            HomeDashboardChrome
                .equalButtonsRow(
                    activity = this,
                    first = importButton,
                    second = accountButton
                )
        )

        flowCard.addView(
            HomeDashboardChrome
                .equalButtonsRow(
                    activity = this,
                    first = searchButton,
                    second = createButton
                ).apply {
                setPadding(0, dp(8), 0, 0)
            }
        )

        content.addView(
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
            isBaselineAligned = false
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, dp(10), dp(8))
        }

        val historyButton =
            HomeDashboardChrome
                .compactButton(
                    activity = this,
                    label = "Історія"
                ) {
                    showHistory()
                }

        pendingButton =
            HomeDashboardChrome
                .compactButton(
                    activity = this,
                    label = "Черга"
                ) {
                    showPendingJobs()
                }

        quotaButton =
            HomeDashboardChrome
                .compactButton(
                    activity = this,
                    label = "Квота"
                ) {
                    openQuotaScreen()
                }

        val moreButton =
            HomeDashboardChrome
                .compactButton(
                    activity = this,
                    label = "Меню"
                ) {
                    openMenuScreen()
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
                        marginStart = dp(5)
                    }
                }
            )
        }

        content.addView(utilityRow)

        /*
         * UX-019 Phase 2:
         * keep account identity and live status in one compact interactive
         * account card without changing workflow semantics.
         */
        val accountCard =
            UiChrome.interactiveSummaryCard(
                activity = this,
                title =
                    "Google/YTM не підключено",
                subtitle =
                    "Натисніть, щоб переглянути інформацію акаунта.",
                fill =
                    palette.surfaceAlt,
                accentOverride =
                    palette.accent,
                onClick = {
                    showAccountDialog()
                }
            )

        accountSummaryText =
            accountCard.title
        statusText =
            requireNotNull(
                accountCard.subtitle
            )

        content.addView(
            accountCard.root,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    dp(12),
                    0,
                    dp(12),
                    dp(8)
                )
            }
        )

        val workspaceCard =
            UiChrome.interactiveSummaryCard(
                activity = this,
                eyebrow =
                    "Поточний плейлист",
                title =
                    "Плейлист ще не імпортовано",
                subtitle =
                    "Натисніть для керування →",
                fill =
                    palette.surface,
                subtitleAccent = true,
                onClick = {
                    openPlaylistHub()
                }
            )

        summaryText =
            workspaceCard.title

        content.addView(
            workspaceCard.root,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    dp(12),
                    0,
                    dp(12),
                    dp(6)
                )
            }
        )

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

        content.addView(
            progress,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(4)
            ).apply {
                marginStart = dp(12)
                marginEnd = dp(12)
            }
        )

        /*
         * UX-019 Phase 2:
         * Home is a dashboard. Track rows live behind PlaylistActivity /
         * ReviewActivity instead of extending the Home screen.
         *
         * Keep the adapter initialized as an internal compatibility bridge
         * while search/write callbacks still notify it; it is no longer
         * attached to a Home ListView.
         */
        adapter =
            TrackAdapter(
                this,
                visibleTracks
            )

        val quickSection =
            HomeDashboardChrome
                .sectionCard(
                    activity = this,
                    title = "Швидкі дії"
                )

        val quickRow =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
                isBaselineAligned = false
            }

        quickRow.addView(
            HomeDashboardChrome
                .quickActionButton(
                    activity = this,
                    label =
                        "Імпортувати файл",
                    icon =
                        R.drawable.ic_ytm_download
                ) {
                openImportScreen()
            },
            LinearLayout.LayoutParams(
                0,
                dp(58),
                1f
            )
        )

        quickRow.addView(
            HomeDashboardChrome
                .quickActionButton(
                    activity = this,
                    label =
                        "Експорт плейлистів",
                    icon =
                        R.drawable.ic_ytm_playlist_add
                ) {
                openImportScreen()
            },
            LinearLayout.LayoutParams(
                0,
                dp(58),
                1f
            ).apply {
                marginStart =
                    dp(8)
            }
        )

        quickSection.addView(quickRow)

        content.addView(
            quickSection,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    dp(12),
                    dp(2),
                    dp(12),
                    dp(6)
                )
            }
        )

        scroll.addView(
            content,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
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
            HomeDashboardChrome
                .bottomNavigation(
                    activity = this,
                    onSearch = {
                        searchOrReview()
                    },
                    onPlaylist = {
                        openPlaylistHub()
                    },
                    onService = {
                        showServiceTools()
                    }
                ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    dp(8),
                    0,
                    dp(8),
                    dp(8)
                )
            }
        )

        setContentView(root)
        UiChrome.applyScreenInsets(this, root)

        updateQuotaPanel()
        updatePendingButton()
        updatePrimaryActions()
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
                    restoringPriorAuthorization ->
                        StepState.ATTENTION

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
        val palette =
            AppThemeManager.palette(this)

        val accentColor =
            when (state) {
                StepState.READY ->
                    palette.success

                StepState.ATTENTION ->
                    palette.warning

                StepState.REQUIRED ->
                    palette.accent
            }

        val fillColor =
            when (state) {
                StepState.READY,
                StepState.ATTENTION ->
                    palette.surfaceAlt

                StepState.REQUIRED ->
                    if (enabled) {
                        palette.accentFill
                    } else {
                        palette.surfaceAlt
                    }
            }

        button.background =
            AppThemeManager.stateButtonDrawable(
                context = this,
                fillColor = fillColor,
                radiusDp = 12,
                accentOverride =
                    accentColor
            )

        button.compoundDrawableTintList =
            ColorStateList.valueOf(
                accentColor
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
                currentImportSourceLabel,
            destinationPlaylistId =
                createdPlaylistId
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
        createdPlaylistId =
            snapshot.destinationPlaylistId

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
        createdPlaylistId =
            snapshot.destinationPlaylistId

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
                currentImportSourceLabel,
            destinationPlaylistId =
                createdPlaylistId
        )
    }

    private fun clearCurrentWorkspaceInMemory() {
        playlist = null
        currentImportSourceLabel =
            "Невідоме джерело"
        createdPlaylistId = null

        visibleTracks.clear()
        adapter.notifyDataSetChanged()

        summaryText.text =
            "Плейлист ще не імпортовано"

        updatePrimaryActions()
    }

    /**
     * Deliberately accepts any file type because some Android file providers
     * expose CSV files with unexpected MIME types.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            resultCode != RESULT_OK ||
            data == null
        ) {
            if (
                returnToPlaylistHubAfterDelegatedAction &&
                requestCode in
                    setOf(
                        reviewScreenRequestCode,
                        destinationScreenRequestCode
                    )
            ) {
                reopenPlaylistHubAfterDelegatedAction()
            }
            return
        }

        when (requestCode) {
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
                handleReviewScreenResult(data)
            }

            playlistScreenRequestCode -> {
                handlePlaylistHubResult(data)
            }

            destinationScreenRequestCode -> {
                handleDestinationResult(data)
            }

            menuScreenRequestCode -> {
                handleMenuScreenResult(data)
            }

            quotaScreenRequestCode -> {
                if (
                    data.getStringExtra(
                        QuotaActivity.EXTRA_ACTION
                    ) ==
                        QuotaActivity.ACTION_OPEN_QUEUE
                ) {
                    showPendingJobs()
                }
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

    private fun openQuotaScreen() {
        startActivityForResult(
            Intent(
                this,
                QuotaActivity::class.java
            ),
            quotaScreenRequestCode
        )
    }

    private fun openMenuScreen() {
        startActivityForResult(
            Intent(
                this,
                MenuActivity::class.java
            ),
            menuScreenRequestCode
        )
    }

    private fun openPlaylistHub() {
        startActivityForResult(
            Intent(
                this,
                PlaylistActivity::class.java
            ),
            playlistScreenRequestCode
        )
    }

    private fun reopenPlaylistHubAfterDelegatedAction() {
        if (
            !returnToPlaylistHubAfterDelegatedAction
        ) {
            return
        }

        returnToPlaylistHubAfterDelegatedAction =
            false

        window.decorView.post {
            if (!isFinishing && !isDestroyed) {
                openPlaylistHub()
            }
        }
    }

    private fun handleReviewScreenResult(
        data: Intent
    ) {
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
                openReviewAfter = true,
                preserveExistingExact = true
            )
            return
        }

        if (
            data.getBooleanExtra(
                ReviewActivity.EXTRA_OPEN_DESTINATION,
                false
            )
        ) {
            createPlaylist()
            return
        }

        handleManualVideoResult(data)
    }

    private fun handleManualVideoResult(
        data: Intent
    ) {
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
            videoId.isNullOrBlank() ||
            historyIndex == Int.MIN_VALUE
        ) {
            return
        }

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

    private fun handlePlaylistHubResult(
        data: Intent
    ) {
        reloadCurrentWorkspace(
            force = true
        )

        when (
            data.getStringExtra(
                PlaylistActivity.EXTRA_ACTION
            )
        ) {
            PlaylistActivity.ACTION_SEARCH -> {
                returnToPlaylistHubAfterDelegatedAction =
                    true
                searchOrReview()
            }

            PlaylistActivity.ACTION_REPEAT_SEARCH -> {
                returnToPlaylistHubAfterDelegatedAction =
                    true
                searchAll(
                    openReviewAfter = true,
                    preserveExistingExact = true
                )
            }

            PlaylistActivity.ACTION_CREATE -> {
                returnToPlaylistHubAfterDelegatedAction =
                    true
                createPlaylist()
            }

            PlaylistActivity.ACTION_REPLACEMENTS ->
                showReplacementLog()

            PlaylistActivity.ACTION_OPEN_YTM ->
                openInYtm()

            PlaylistActivity.ACTION_COPY_LINK ->
                copyPlaylistLink()

            PlaylistActivity.ACTION_MANUAL_VIDEO ->
                handleManualVideoResult(data)
        }
    }

    private fun handleMenuScreenResult(
        data: Intent
    ) {
        when (
            data.getStringExtra(
                MenuActivity.EXTRA_ACTION
            )
        ) {
            MenuActivity.ACTION_THEME ->
                showThemePicker()

            MenuActivity.ACTION_PROJECT ->
                openReviewScreen()

            MenuActivity.ACTION_REPLACEMENTS ->
                showReplacementLog()

            MenuActivity.ACTION_OPEN_YTM ->
                openInYtm()

            MenuActivity.ACTION_DATA ->
                startActivity(
                    Intent(
                        this,
                        DataActivity::class.java
                    )
                )

            MenuActivity.ACTION_SERVICE ->
                showServiceTools()
        }
    }

    private fun showAccountDialog() {
        if (accessToken.isNullOrBlank()) {
            accountDialogOpen = false
            authorize()
            return
        }

        accountDialogOpen = true

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

        val dialog =
            UiChrome.alertBuilder(this)
                .setTitle("Акаунт")
                .setMessage(
                    "Статус: Підключено\n\n" +
                        "Google профіль:\n$googleText\n\n" +
                        "YouTube / YouTube Music:\n$youtubeText\n\n" +
                        "Доступ: пошук, створення та робота з плейлистами " +
                        "через YouTube API.\n\n" +
                        "OAuth token у цьому вікні не показується і на диск не зберігається."
                )
                .setNegativeButton("Закрити", null)
                .setPositiveButton("Змінити") { _, _ ->
                    authorize(after = null, forceAccountPicker = true)
                }
                .show()

        dialog.setOnDismissListener {
            accountDialogOpen = false
        }
    }

    private fun restoreAuthSessionFromMemory() {
        val snapshot = AuthSessionStore.current()
        accessToken = snapshot.accessToken
        googleAccountInfo = snapshot.googleAccountInfo
        youtubeChannelInfo = snapshot.youtubeChannelInfo
    }

    private fun syncAuthorizationInvalidationFromMemory() {
        val shared =
            AuthSessionStore.current()

        if (
            !accessToken.isNullOrBlank() &&
            shared.accessToken.isNullOrBlank()
        ) {
            accessToken = null
            googleAccountInfo = null
            youtubeChannelInfo = null
            restoringPriorAuthorization = false
            pendingAfterAuth = null

            updateAccountPanel()
            status(
                "Авторизація Google/YTM завершилась. " +
                    "Натисніть «2. Google / YTM» і підключіть акаунт знову."
            )
        }
    }

    private fun syncAuthSessionToMemory() {
        AuthSessionStore.update(
            accessToken = accessToken,
            googleAccountInfo = googleAccountInfo,
            youtubeChannelInfo = youtubeChannelInfo
        )
    }

    private fun buildAuthorizationRequest(
        forceAccountPicker: Boolean
    ): AuthorizationRequest {
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
            builder.setPrompt(
                AuthorizationRequest.Prompt.SELECT_ACCOUNT
            )
        }

        return builder.build()
    }

    private fun restorePriorAuthorizationSilently() {
        if (
            !persistentAuthStateStore
                .hadSuccessfulAuthorization() ||
            !accessToken.isNullOrBlank()
        ) {
            restoringPriorAuthorization = false
            updateAccountPanel()
            return
        }

        restoringPriorAuthorization = true
        updateAccountPanel()
        status(
            "Відновлюю Google/YTM сесію без повторного входу…"
        )

        Identity.getAuthorizationClient(this)
            .authorize(
                buildAuthorizationRequest(
                    forceAccountPicker = false
                )
            )
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    restoringPriorAuthorization = false
                    updateAccountPanel()
                    status(
                        "Google/YTM потребує підтвердження. " +
                            "Натисніть «2. Google / YTM»."
                    )
                    return@addOnSuccessListener
                }

                val token =
                    result.accessToken

                if (token.isNullOrBlank()) {
                    restoringPriorAuthorization = false
                    updateAccountPanel()
                    status(
                        "Не вдалося автоматично відновити Google/YTM. " +
                            "Натисніть крок 2."
                    )
                    return@addOnSuccessListener
                }

                restoringPriorAuthorization = false
                handleAuthorizedToken(token)
            }
            .addOnFailureListener {
                restoringPriorAuthorization = false
                updateAccountPanel()
                status(
                    "Не вдалося автоматично відновити Google/YTM. " +
                        "Натисніть крок 2."
                )
            }
    }

    private fun authorize(
        forceAccountPicker: Boolean = false,
        after: (() -> Unit)? = null
    ) {
        val preserveKnownIdentity =
            !forceAccountPicker &&
                googleAccountInfo != null &&
                youtubeChannelInfo != null

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
                "Перевіряю авторизацію Google/YTM…"
            }
        )

        Identity.getAuthorizationClient(this)
            .authorize(
                buildAuthorizationRequest(
                    forceAccountPicker =
                        forceAccountPicker
                )
            )
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
                        toast(
                            ErrorMessages.userMessage(
                                e,
                                "Не вдалося відкрити Google"
                            )
                        )
                    }
                } else {
                    val token = result.accessToken

                    if (token.isNullOrBlank()) {
                        pendingAfterAuth = null
                        clearAuthorizationForRefreshFailure(
                            "Google не повернув актуальний access token."
                        )
                    } else {
                        handleAuthorizedToken(
                            token = token,
                            preserveKnownIdentity =
                                preserveKnownIdentity
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                pendingAfterAuth = null
                clearAuthorizationForRefreshFailure(
                    ErrorMessages.userMessage(
                        e,
                        "Не вдалося оновити авторизацію Google/YTM"
                    )
                )
            }
    }

    private fun clearAuthorizationForRefreshFailure(
        message: String
    ) {
        accessToken = null
        googleAccountInfo = null
        youtubeChannelInfo = null
        restoringPriorAuthorization = false
        AuthSessionStore.clear()
        persistentAuthStateStore.clear()
        updateAccountPanel()
        status(
            "$message Натисніть «2. Google / YTM» і підключіть акаунт знову."
        )
        toast("Авторизацію Google/YTM потрібно відновити")
    }

    private fun handleAuthorizedToken(
        token: String,
        preserveKnownIdentity: Boolean = false
    ) {
        restoringPriorAuthorization = false
        accessToken = token

        val identityReady =
            preserveKnownIdentity &&
                googleAccountInfo != null &&
                youtubeChannelInfo != null

        if (!identityReady) {
            googleAccountInfo = null
            youtubeChannelInfo = null
        }

        persistentAuthStateStore
            .markSuccessfulAuthorization()
        syncAuthSessionToMemory()
        updateAccountPanel()

        val action = pendingAfterAuth
        pendingAfterAuth = null

        if (identityReady) {
            status("Авторизацію Google/YTM оновлено.")
            action?.invoke()
        } else {
            status(
                "Google підключено. Завантажую дані акаунта і YouTube каналу…"
            )
            loadAccountIdentity(
                token,
                action
            )
        }
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

            val authFailure =
                listOfNotNull(
                    googleResult.exceptionOrNull(),
                    channelResult.exceptionOrNull()
                ).firstOrNull {
                    isAuthorizationFailure(it)
                }

            runOnUiThread {
                if (authFailure != null) {
                    invalidateAuthorizationIfNeeded(
                        authFailure
                    )
                    return@runOnUiThread
                }

                googleAccountInfo = googleResult.getOrNull()
                youtubeChannelInfo = channelResult.getOrNull()
                syncAuthSessionToMemory()
                updateAccountPanel()
                updateQuotaPanel()

                val recoveredTracks =
                    recoverPersistedAuthorizationFailures()

                val channel = youtubeChannelInfo
                status(
                    if (channel != null) {
                        if (recoveredTracks > 0) {
                            "Акаунт готовий • відновлено треків після auth-помилки: $recoveredTracks"
                        } else {
                            "Акаунт готовий до роботи."
                        }
                    } else {
                        "Google підключено, але YouTube канал не вдалося визначити."
                    }
                )

                after?.invoke()
            }
        }
    }

    private fun recoverPersistedAuthorizationFailures(): Int {
        val current =
            playlist
                ?: return 0

        var recovered = 0

        current.tracks.forEach { track ->
            val legacyAuthFailure =
                track.status == TrackStatus.FAILED &&
                    track.error
                        .orEmpty()
                        .contains(
                            "Авторизація Google більше не дійсна",
                            ignoreCase = true
                        )

            if (!legacyAuthFailure) {
                return@forEach
            }

            track.status =
                when {
                    track.selectedVideoId.isNullOrBlank() ->
                        TrackStatus.NEW

                    track.candidates.isEmpty() ->
                        TrackStatus.MATCHED

                    else ->
                        TrackStatus.REVIEW
                }
            track.error = null
            recovered += 1
        }

        if (recovered > 0) {
            updateSummary()
        }

        return recovered
    }

    private fun updateAccountPanel() {
        if (!::accountButton.isInitialized) return

        accountButton.text =
            when {
                restoringPriorAuthorization ->
                    "2. Google / YTM …"

                accessToken.isNullOrBlank() ->
                    "2. Google / YTM"

                youtubeChannelInfo != null ->
                    "2. Google / YTM ✓"

                else ->
                    "2. Google / YTM …"
            }

        if (::accountSummaryText.isInitialized) {
            accountSummaryText.text =
                when {
                    restoringPriorAuthorization ->
                        "Відновлення Google/YTM…"

                    accessToken.isNullOrBlank() ->
                        "Google/YTM не підключено"

                    youtubeChannelInfo != null ->
                        "Підключено YouTube/YTM: " +
                            youtubeChannelInfo?.title.orEmpty()

                    else ->
                        "Google підключено • YouTube/YTM уточнюється…"
                }
        }

        updatePrimaryActions()
    }

    private fun isAuthorizationFailure(
        error: Throwable
    ): Boolean {
        var current: Throwable? = error

        while (current != null) {
            if (
                current is YouTubeApiException &&
                current.httpCode == 401
            ) {
                return true
            }
            current = current.cause
        }

        return false
    }

    private fun invalidateAuthorizationIfNeeded(
        error: Throwable
    ): Boolean {
        if (!isAuthorizationFailure(error)) return false

        accessToken = null
        googleAccountInfo = null
        youtubeChannelInfo = null
        restoringPriorAuthorization = false
        pendingAfterAuth = null
        AuthSessionStore.clear()
        persistentAuthStateStore.clear()

        updateAccountPanel()
        status(
            "Авторизація Google/YTM більше не дійсна. " +
                "Натисніть «2. Google / YTM» і увійдіть знову."
        )
        toast("Сесію Google/YTM потрібно відновити")
        return true
    }

    private fun searchAll(
        openReviewAfter: Boolean = false,
        preserveExistingExact: Boolean = true
    ) {
        val p =
            playlist
                ?: return toast(
                    "Спочатку імпортуйте список треків"
                )

        val plan =
            searchCoordinator.plan(
                playlist = p,
                preserveExistingExact =
                    preserveExistingExact
            )

        val warning =
            if (
                plan.apiNeeded >
                plan.quota.searchRemaining
            ) {
                "\n\n⚠ Локальна оцінка показує, що search quota " +
                    "(квоти пошуку) може не вистачити."
            } else {
                ""
            }

        var searchStarted =
            false

        val searchPlanDialog =
            UiChrome.alertBuilder(this)
            .setTitle(
                "План пошуку (Search plan)"
            )
            .setMessage(
                "Треків у списку: ${plan.totalTracks}\n" +
                    "Пошук потрібен для: ${plan.tracksToSearch}\n" +
                    "Вже є в кеші: ${plan.cachedCount}\n" +
                    "Потрібно нових search.list: ${plan.apiNeeded}\n\n" +
                    "Локально використано сьогодні: " +
                    "${plan.quota.searchCalls}/" +
                    "${QuotaTracker.SEARCH_DAILY_LIMIT}\n" +
                    "Локальна оцінка залишку: " +
                    "${plan.quota.searchRemaining}" +
                    warning +
                    "\n\nЦе не точний залишок Google Cloud. " +
                    "Інші пристрої або клієнти того самого API project " +
                    "(проєкту API) можуть теж витрачати квоту."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Почати"
            ) { _, _ ->
                searchStarted =
                    true

                startSearch(
                    p = p,
                    openReviewAfter =
                        openReviewAfter,
                    preserveExistingExact =
                        preserveExistingExact
                )
            }
            .show()

        if (
            returnToPlaylistHubAfterDelegatedAction
        ) {
            searchPlanDialog.setOnDismissListener {
                window.decorView.post {
                    if (
                        !searchStarted &&
                        returnToPlaylistHubAfterDelegatedAction &&
                        !isFinishing &&
                        !isDestroyed
                    ) {
                        reopenPlaylistHubAfterDelegatedAction()
                    }
                }
            }
        }
    }

    private fun startSearch(
        p: ImportedPlaylist,
        openReviewAfter: Boolean = false,
        preserveExistingExact: Boolean = false
    ) {
        authorize {
            val token =
                accessToken
                    ?: return@authorize

            progress.visibility =
                View.VISIBLE
            progress.max =
                p.tracks.size
            progress.progress =
                0

            status(
                "Пошук 0/${p.tracks.size} • кеш 0 • API 0"
            )

            executor.execute {
                val result =
                    searchCoordinator.run(
                        accessToken = token,
                        playlist = p,
                        preserveExistingExact =
                            preserveExistingExact,
                        onTrackStateChanged = { index ->
                            runOnUiThread {
                                refreshRow(index)
                            }
                        },
                        onProgress = { searchProgress ->
                            runOnUiThread {
                                progress.progress =
                                    searchProgress.processed

                                status(
                                    when (
                                        searchProgress.preservedSelection
                                    ) {
                                        SearchCoordinator.PreservedSelection.MANUAL ->
                                            "Пошук ${searchProgress.processed}/" +
                                                "${searchProgress.total} • " +
                                                "ручний вибір збережено"

                                        SearchCoordinator.PreservedSelection.PROJECT_EXACT ->
                                            "Пошук ${searchProgress.processed}/" +
                                                "${searchProgress.total} • " +
                                                "точний videoId з Project збережено"

                                        null ->
                                            "Пошук ${searchProgress.processed}/" +
                                                "${searchProgress.total} • " +
                                                "кеш ${searchProgress.cacheHits} • " +
                                                "API ${searchProgress.apiSearches}"
                                    }
                                )

                                adapter.notifyDataSetChanged()
                                updateSummary()
                                updateQuotaPanel()
                            }
                        },
                        onQuotaBlocked = {
                            runOnUiThread {
                                toast(
                                    "Закінчилась квота пошуку YouTube API. " +
                                        "Треки, які вже є в кеші, програма ще обробить."
                                )
                            }
                        },
                        onAuthorizationInvalidated = { error ->
                            runOnUiThread {
                                invalidateAuthorizationIfNeeded(error)
                                updateSummary()
                            }
                        }
                    )

                runOnUiThread {
                    progress.visibility =
                        View.GONE

                    status(
                        when {
                            result.authorizationInvalidated ->
                                "Пошук зупинено: Google/YTM потребує повторного входу. " +
                                    "Поточний список збережено; після входу запустіть пошук ще раз."

                            result.quotaBlocked ->
                                "Готово: з кешу ${result.cacheHits}, " +
                                    "API-запитів ${result.apiSearches}. " +
                                    "Квота закінчилась; некешовані треки " +
                                    "залишились без пошуку."

                            else ->
                                "Пошук завершено: з кешу ${result.cacheHits}, " +
                                    "нових API-пошуків ${result.apiSearches}. " +
                                    "Жовті треки краще перевірити натисканням."
                        }
                    )

                    updateSummary()
                    updateQuotaPanel()

                    if (
                        openReviewAfter &&
                        !result.authorizationInvalidated
                    ) {
                        openReviewScreen()
                    }
                }
            }
        }
    }

    private fun createPlaylist() {
        val p =
            playlist
                ?: return toast(
                    "Спочатку імпортуйте список треків"
                )

        val selected =
            destinationCoordinator.currentTracksForDestination(p)

        if (selected.isEmpty()) {
            return toast(
                "Спочатку знайдіть або виберіть треки"
            )
        }

        destinationCoordinator.reset()

        openDestinationStart(
            p = p,
            selected = selected
        )
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
            destinationCoordinator.currentTracksForDestination(p)

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
                val cachedPlaylists =
                    destinationCoordinator.cachedPlaylists()

                if (cachedPlaylists.isEmpty()) {
                    loadExistingPlaylistsForDestination(
                        p = p,
                        selected = selected
                    )
                } else {
                    openDestinationExistingList(
                        p = p,
                        selected = selected,
                        playlists = cachedPlaylists
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
                    destinationCoordinator.selectExistingTarget(
                        id = id,
                        title =
                            data.getStringExtra(
                                DestinationActivity.EXTRA_TARGET_TITLE
                            ),
                        privacyStatus =
                            data.getStringExtra(
                                DestinationActivity.EXTRA_TARGET_PRIVACY
                            ),
                        itemCount =
                            data.getLongExtra(
                                DestinationActivity.EXTRA_TARGET_COUNT,
                                0L
                            )
                    )
                        ?: return toast(
                            "Не вдалося визначити вибраний плейлист"
                        )

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
                val result =
                    runCatching {
                        destinationCoordinator.loadExistingPlaylists(
                            accessToken = token
                        )
                    }

                runOnUiThread {
                    progress.isIndeterminate = false
                    progress.visibility = View.GONE
                    updateQuotaPanel()

                    result.onSuccess { playlists ->
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
                        if (invalidateAuthorizationIfNeeded(error)) {
                            return@onFailure
                        }

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
                        destinationCoordinator.scanDuplicates(
                            accessToken = token,
                            selected = selected,
                            target = target
                        )
                    }

                runOnUiThread {
                    progress.isIndeterminate = false
                    progress.visibility = View.GONE
                    updateQuotaPanel()

                    result.onSuccess { scan ->
                        openDestinationExistingConfirm(
                            p = p,
                            selected = selected,
                            target = target,
                            analysis = scan.analysis,
                            scanRequestCount = scan.requestCount
                        )
                    }.onFailure { error ->
                        if (invalidateAuthorizationIfNeeded(error)) {
                            return@onFailure
                        }

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
        analysis: DestinationCoordinator.DuplicateAnalysis,
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
        val mode =
            when (duplicateMode) {
                DestinationActivity.DUPLICATE_MODE_NO_SCAN ->
                    DestinationCoordinator.DuplicateMode.NO_SCAN

                DestinationActivity.DUPLICATE_MODE_ALL ->
                    DestinationCoordinator.DuplicateMode.ADD_ALL

                else ->
                    DestinationCoordinator.DuplicateMode.SKIP
            }

        val plan =
            runCatching {
                destinationCoordinator.buildExistingWritePlan(
                    selected = selected,
                    mode = mode
                )
            }.getOrElse { error ->
                return toast(
                    error.message
                        ?: "Не вдалося підготувати запис у вибраний плейлист"
                )
            }

        actuallyAppendToExisting(
            p = p,
            selected = plan.tracksToWrite,
            target = plan.target,
            duplicateTracksToSkip = plan.tracksToSkip
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
                playlistWriteCoordinator.buildPendingJob(
                    sourceLabel = currentImportSourceLabel,
                    playlistName = target.title,
                    playlistId = target.id,
                    privacyStatus = target.privacyStatus,
                    destination = PendingDestination.EXISTING_PLAYLIST,
                    tracks = selected,
                    account = currentWriteAccountContext()
                )

            pendingJobStore.upsert(job)
            updatePendingButton()

            createdPlaylistId = target.id
            persistCurrentWorkspace()

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
                playlistWriteCoordinator.buildPendingJob(
                    sourceLabel = currentImportSourceLabel,
                    playlistName = p.name,
                    playlistId = null,
                    privacyStatus = privacyStatus,
                    destination = PendingDestination.NEW_PLAYLIST,
                    tracks = selected,
                    account = currentWriteAccountContext()
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

    private fun currentWriteAccountContext():
        PlaylistWriteCoordinator.AccountContext =
        PlaylistWriteCoordinator.AccountContext(
            googleEmail = googleAccountInfo?.email,
            youtubeChannelId = youtubeChannelInfo?.id,
            youtubeChannelTitle = youtubeChannelInfo?.title
        )


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
            val outcome =
                playlistWriteCoordinator.execute(
                    token = token,
                    initialJob = initialJob,
                    tracks = tracks,
                    onHistoryState = { job, historyStatus ->
                        syncHistoryFromJob(
                            job,
                            historyStatus
                        )
                    },
                    onPlaylistIdAvailable = { playlistId ->
                        createdPlaylistId = playlistId
                        persistCurrentWorkspace()
                    },
                    onProgress = { writeProgress ->
                        runOnUiThread {
                            progress.progress =
                                writeProgress.progressValue

                            status(
                                if (writeProgress.playlistCreated) {
                                    "Плейлист створено. Додаю треки…"
                                } else {
                                    "Додаю " +
                                        "${writeProgress.processedTracks}/" +
                                        "${writeProgress.totalTracks}… " +
                                        "залишилось " +
                                        "${writeProgress.job.remainingTracks.size}"
                                }
                            )

                            adapter.notifyDataSetChanged()
                            updateSummary()
                            updateQuotaPanel()
                            updatePendingButton()
                        }
                    }
                )

            runOnUiThread {
                progress.visibility = View.GONE
                adapter.notifyDataSetChanged()
                updateSummary()
                updateQuotaPanel()
                updatePendingButton()

                when (outcome) {
                    is PlaylistWriteCoordinator.WriteOutcome.Completed -> {
                        createdPlaylistId = outcome.playlistId
                        persistCurrentWorkspace()

                        status(
                            "Готово. ${outcome.job.playlistName}: " +
                                "додано ${outcome.job.addedCount}, " +
                                "помилок ${outcome.job.failedCount}."
                        )

                        showPlaylistResult(
                            playlistName = outcome.job.playlistName,
                            addedCount = outcome.job.addedCount,
                            failedCount = outcome.job.failedCount,
                            privacyStatus = outcome.job.privacyStatus,
                            operationLabel = operationLabel
                        )
                    }

                    is PlaylistWriteCoordinator.WriteOutcome.PausedForQuota -> {
                        showQuotaPausedDialog(outcome.job)
                    }

                    is PlaylistWriteCoordinator.WriteOutcome.AuthorizationInvalidated -> {
                        if (
                            invalidateAuthorizationIfNeeded(
                                outcome.error
                            )
                        ) {
                            updatePendingButton()
                            status(
                                "Авторизацію Google/YTM потрібно відновити. " +
                                    "Незавершене завдання збережено в «Черзі»."
                            )
                        }
                    }

                    is PlaylistWriteCoordinator.WriteOutcome.Failed -> {
                        toast(outcome.userMessage)
                    }
                }
            }
        }
    }

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
                    .setPositiveButton("Змінити") { _, _ ->
                        authorize(forceAccountPicker = true) {
                            resumePendingJob(job)
                        }
                    }
                    .show()

                return@authorize
            }

            val tracks =
                job.remainingTracks.map(playlistWriteCoordinator::trackFromPending)

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

    private fun showThemePicker() {
        val active = AppThemeManager.currentStyle(this)
        UiChrome.showMenuDialog(
            activity = this,
            title = "Тема оформлення",
            subtitle = "Один інтерфейс — три палітри. Тема зберігається на пристрої.",
            actions = AppThemeManager.ThemeStyle.values().map { style ->
                UiChrome.MenuAction(
                    label = (if (style == active) "✓ " else "") + style.marker + "  " + style.label,
                    onClick = {
                        if (style != active) {
                            AppThemeManager.setStyle(this, style)
                            recreate()
                        }
                    }
                )
            }
        )
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

    private fun showHistory() {
        startActivity(
            Intent(
                this,
                HistoryActivity::class.java
            )
        )
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
        val url =
            playlistUrl()
                ?: return

        val duplicateCount =
            playlist
                ?.tracks
                .orEmpty()
                .count {
                    it.status ==
                        TrackStatus.DUPLICATE
                }

        val details =
            buildString {
                append("Додано: $addedCount")

                if (failedCount > 0) {
                    append(
                        " • Не додано: $failedCount"
                    )
                }

                if (duplicateCount > 0) {
                    append(
                        " • Дублікати: $duplicateCount"
                    )
                }

                append(
                    " • ${privacyLabel(privacyStatus)}"
                )
                append(
                    " • $operationLabel"
                )

                youtubeChannelInfo
                    ?.title
                    ?.let {
                        append(
                            "\nYouTube/YTM: $it"
                        )
                    }

                append(
                    "\n\n$url"
                )
            }

        UiChrome.showMessageDialog(
            activity = this,
            title = "✓ $playlistName",
            subtitle =
                "Операцію завершено",
            message = details,
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Відкрити в YTM"
                    ) {
                        openInYtm()
                        reopenPlaylistHubAfterDelegatedAction()
                    },
                    UiChrome.DialogAction(
                        label = "Копіювати посилання"
                    ) {
                        copyPlaylistLink()
                        reopenPlaylistHubAfterDelegatedAction()
                    },
                    UiChrome.DialogAction(
                        label = "Закрити",
                        tone =
                            UiChrome.ActionTone.ACCENT
                    ) {
                        reopenPlaylistHubAfterDelegatedAction()
                    }
                ),
            actionLayout =
                UiChrome.DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE
        )
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
                ?: return toast("Створіть / виберіть плейлист")

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
        val url = playlistUrl() ?: return toast("Створіть / виберіть плейлист")

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

        private const val STATE_ACCOUNT_DIALOG_OPEN =
            "state_account_dialog_open"

        private const val STATE_RETURN_TO_PLAYLIST_HUB =
            "state_return_to_playlist_hub"

        private const val YOUTUBE_SCOPE =
            "https://www.googleapis.com/auth/youtube.force-ssl"

        private const val USERINFO_EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email"

        private const val USERINFO_PROFILE_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile"
    }
}
