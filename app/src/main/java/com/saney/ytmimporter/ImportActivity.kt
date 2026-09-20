package com.saney.ytmimporter
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.RestorableWindowState
import com.saney.ytmimporter.ui.UiChrome

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
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
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.saney.ytmimporter.auth.AuthSessionStore
import com.saney.ytmimporter.auth.PersistentAuthStateStore
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import com.saney.ytmimporter.parser.PlaylistParser
import com.saney.ytmimporter.storage.AccountBackupBaseline
import com.saney.ytmimporter.storage.AccountLibraryDeltaChainRestorer
import com.saney.ytmimporter.storage.AccountLibraryExporter
import com.saney.ytmimporter.storage.AccountLibraryIncrementalBackup
import com.saney.ytmimporter.storage.AccountLibraryManifestEntry
import com.saney.ytmimporter.storage.AccountLibraryManifestImport
import com.saney.ytmimporter.storage.AccountLibraryManifestImporter
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.DeltaChainHead
import com.saney.ytmimporter.storage.DeltaChainMaterializeResult
import com.saney.ytmimporter.storage.DeltaChainPlan
import com.saney.ytmimporter.storage.IncrementalBackupPlan
import com.saney.ytmimporter.storage.IncrementalBackupRecord
import com.saney.ytmimporter.storage.IncrementalBackupWriteResult
import com.saney.ytmimporter.storage.IncrementalDeltaManifestException
import com.saney.ytmimporter.storage.PlaylistProjectCodec
import com.saney.ytmimporter.storage.PlaylistProjectImport
import com.saney.ytmimporter.storage.SafTreeAccess
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject

class ImportActivity : Activity() {
    private val fileRequestCode =
        2301

    private val exportFolderRequestCode =
        2302

    private val selectiveExportFolderRequestCode =
        2303

    private val manifestImportFolderRequestCode =
        2404

    private val incrementalBackupBaseRequestCode =
        2405

    private val incrementalBackupTargetRequestCode =
        2406

    private val deltaChainRootRequestCode =
        2407

    private val deltaChainTargetRequestCode =
        2408

    private val ytmPlaylistSelectorRequestCode =
        2501

    private val selectiveExportSelectorRequestCode =
        2502

    private val deltaChainHeadSelectorRequestCode =
        2503

    private val manifestProjectSelectorRequestCode =
        2504

    private val freshAuthRequestCode =
        2601

    private var pendingDeltaChainPlan:
        DeltaChainPlan? =
        null

    private var pendingIncrementalBackupPlan:
        IncrementalBackupPlan? =
        null

    private var pendingIncrementalBackupPreflight:
        IncrementalBackupPreflight? =
        null

    private var pendingSelectiveExport:
        List<YouTubePlaylistInfo> =
        emptyList()

    private var clearWorkspaceDialogOpen =
        false

    private var clearWorkspaceDialog:
        Dialog? =
        null

    private var pendingFreshAuthAction:
        FreshAuthAction? =
        null

    private var pendingFreshAuthPayload:
        String? =
        null

    private var awaitingFreshAuthResolution =
        false

    private var freshAuthCheckInFlight =
        false

    private val executor =
        Executors.newSingleThreadExecutor()

    private val api =
        YouTubeApi()

    private lateinit var currentPlaylistStore:
        CurrentPlaylistStore

    private lateinit var playlistNameInput:
        EditText

    private lateinit var tracksInput:
        EditText

    private lateinit var windowState:
        RestorableWindowState

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        AppThemeManager.applyWindow(this)
        windowState =
            RestorableWindowState(
                savedInstanceState,
                STATE_WINDOW
            )

        currentPlaylistStore =
            CurrentPlaylistStore(this)

        val retainedState =
            lastNonConfigurationInstance
                as? ImportNonConfigState

        pendingIncrementalBackupPlan =
            retainedState
                ?.incrementalPlan
        pendingIncrementalBackupPreflight =
            retainedState
                ?.incrementalPreflight
        pendingDeltaChainPlan =
            retainedState
                ?.deltaChainPlan

        pendingSelectiveExport =
            decodeSelectiveExportState(
                savedInstanceState
                    ?.getString(
                        STATE_SELECTIVE_EXPORT
                    )
            )

        pendingFreshAuthAction =
            savedInstanceState
                ?.getString(
                    STATE_PENDING_FRESH_AUTH_ACTION
                )
                ?.let { raw ->
                    runCatching {
                        FreshAuthAction.valueOf(raw)
                    }.getOrNull()
                }

        pendingFreshAuthPayload =
            savedInstanceState
                ?.getString(
                    STATE_PENDING_FRESH_AUTH_PAYLOAD
                )

        awaitingFreshAuthResolution =
            savedInstanceState
                ?.getBoolean(
                    STATE_AWAITING_FRESH_AUTH_RESOLUTION,
                    false
                )
                ?: false

        clearWorkspaceDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_CLEAR_WORKSPACE_DIALOG_OPEN,
                    false
                )
                ?: false

        buildUi()

        if (
            savedInstanceState == null &&
            intent.getStringExtra(
                EXTRA_START_ACTION
            ) == ACTION_SELECTIVE_EXPORT
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    chooseSelectiveYtmExport()
                }
            }
        }

        if (clearWorkspaceDialogOpen) {
            window.decorView.post {
                val current =
                    currentPlaylistStore
                        .load()

                if (
                    current != null &&
                    !isFinishing &&
                    !isDestroyed
                ) {
                    confirmClearWorkspace(
                        current.playlist.name
                    )
                } else {
                    clearWorkspaceDialogOpen =
                        false
                }
            }
        }

        restoreWindowIfNeeded()

        if (
            pendingFreshAuthAction != null &&
            !awaitingFreshAuthResolution
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    requestPendingFreshAuthorization()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putString(
            STATE_SELECTIVE_EXPORT,
            encodeSelectiveExportState(
                pendingSelectiveExport
            )
        )

        outState.putBoolean(
            STATE_CLEAR_WORKSPACE_DIALOG_OPEN,
            clearWorkspaceDialogOpen
        )

        pendingFreshAuthAction
            ?.let { action ->
                outState.putString(
                    STATE_PENDING_FRESH_AUTH_ACTION,
                    action.name
                )
            }

        pendingFreshAuthPayload
            ?.let { payload ->
                outState.putString(
                    STATE_PENDING_FRESH_AUTH_PAYLOAD,
                    payload
                )
            }

        outState.putBoolean(
            STATE_AWAITING_FRESH_AUTH_RESOLUTION,
            awaitingFreshAuthResolution
        )

        windowState.save(outState)

        super.onSaveInstanceState(
            outState
        )
    }

    override fun onRetainNonConfigurationInstance():
        Any? =
        ImportNonConfigState(
            incrementalPlan =
                pendingIncrementalBackupPlan,
            incrementalPreflight =
                pendingIncrementalBackupPreflight,
            deltaChainPlan =
                pendingDeltaChainPlan
        )

    override fun onDestroy() {
        clearWorkspaceDialog
            ?.setOnDismissListener(
                null
            )
        clearWorkspaceDialog =
            null

        executor.shutdownNow()
        super.onDestroy()
    }

    private fun restoreWindowIfNeeded() {
        if (windowState.key == null) {
            return
        }

        window.decorView.post {
            if (isFinishing || isDestroyed) {
                return@post
            }

            when (windowState.key) {
                WINDOW_AUTH_INVALIDATED ->
                    showAuthorizationInvalidatedNotice()

                WINDOW_STATUS_MESSAGE ->
                    showStoredStatusMessage()

                WINDOW_INCREMENTAL_SCAN_CONFIRM -> {
                    val preflight =
                        pendingIncrementalBackupPreflight

                    if (preflight == null) {
                        windowState.clear()
                    } else {
                        showIncrementalBackupScanConfirmation(
                            preflight
                        )
                    }
                }

                WINDOW_INCREMENTAL_PREVIEW -> {
                    val plan =
                        pendingIncrementalBackupPlan

                    if (plan == null) {
                        windowState.clear()
                    } else {
                        showIncrementalBackupPreview(
                            plan
                        )
                    }
                }

                WINDOW_DELTA_CHAIN_PREVIEW -> {
                    val plan =
                        pendingDeltaChainPlan

                    if (plan == null) {
                        windowState.clear()
                    } else {
                        showDeltaChainPreview(
                            plan
                        )
                    }
                }
            }
        }
    }

    private fun showStatusMessage(
        title: String,
        message: String
    ) {
        windowState.show(
            key = WINDOW_STATUS_MESSAGE,
            args =
                Bundle().apply {
                    putString(
                        ARG_STATUS_TITLE,
                        title
                    )
                    putString(
                        ARG_STATUS_MESSAGE,
                        message
                    )
                }
        ) {
            UiChrome.showMessageDialog(
                activity = this,
                title = title,
                message = message,
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label = "Закрити",
                            tone =
                                UiChrome.ActionTone.ACCENT,
                            onClick = {}
                        )
                    )
            )
        }
    }

    private fun showStoredStatusMessage() {
        val args =
            windowState.args()

        val title =
            args.getString(
                ARG_STATUS_TITLE
            )
        val message =
            args.getString(
                ARG_STATUS_MESSAGE
            )

        if (
            title.isNullOrBlank() ||
            message.isNullOrBlank()
        ) {
            windowState.clear()
            return
        }

        showStatusMessage(
            title = title,
            message = message
        )
    }

    private fun isAuthorizationFailure(
        error: Throwable
    ): Boolean {
        var current: Throwable? =
            error

        while (current != null) {
            if (
                current is YouTubeApiException &&
                current.httpCode == 401
            ) {
                return true
            }

            current =
                current.cause
        }

        return false
    }

    private fun invalidateAuthorizationIfNeeded(
        error: Throwable
    ): Boolean {
        if (!isAuthorizationFailure(error)) {
            return false
        }

        AuthSessionStore.clear()
        PersistentAuthStateStore(this)
            .clear()

        showAuthorizationInvalidatedNotice()

        return true
    }

    private fun showAuthorizationInvalidatedNotice() {
        windowState.show(
            WINDOW_AUTH_INVALIDATED
        ) {
            UiChrome.showMessageDialog(
                activity = this,
                title =
                    "Сесію Google/YTM завершено",
                message =
                    "Google відхилив поточну авторизацію (HTTP 401).\n\n" +
                        "Стан підключення скинуто. Локальний робочий список не видалено.\n\n" +
                        "Поверніться до кроку 2 і підключіть Google/YTM знову.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label =
                                "До кроку 2",
                            tone =
                                UiChrome.ActionTone.ACCENT,
                            onClick = {
                                finish()
                            }
                        )
                    )
            )
        }
    }

    private fun requestFreshAuthorization(
        action: FreshAuthAction,
        payload: String? = null
    ) {
        if (
            freshAuthCheckInFlight ||
            awaitingFreshAuthResolution
        ) {
            toast(
                "Перевірка авторизації Google/YTM вже виконується."
            )
            return
        }

        pendingFreshAuthAction = action
        pendingFreshAuthPayload = payload
        requestPendingFreshAuthorization()
    }

    private fun requestPendingFreshAuthorization() {
        if (
            pendingFreshAuthAction == null ||
            freshAuthCheckInFlight ||
            awaitingFreshAuthResolution
        ) {
            return
        }

        freshAuthCheckInFlight = true

        Identity.getAuthorizationClient(this)
            .authorize(
                buildFreshAuthorizationRequest()
            )
            .addOnSuccessListener { result ->
                freshAuthCheckInFlight = false

                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@addOnSuccessListener
                }

                if (result.hasResolution()) {
                    val pendingIntent =
                        result.pendingIntent

                    if (pendingIntent == null) {
                        failFreshAuthorization(
                            "Google не повернув вікно підтвердження авторизації."
                        )
                        return@addOnSuccessListener
                    }

                    awaitingFreshAuthResolution =
                        true

                    try {
                        startIntentSenderForResult(
                            pendingIntent.intentSender,
                            freshAuthRequestCode,
                            null,
                            0,
                            0,
                            0
                        )
                    } catch (error: Exception) {
                        awaitingFreshAuthResolution =
                            false
                        failFreshAuthorization(
                            error.message
                                ?: "Не вдалося відкрити Google авторизацію."
                        )
                    }

                    return@addOnSuccessListener
                }

                val token =
                    result.accessToken

                if (token.isNullOrBlank()) {
                    failFreshAuthorization(
                        "Google не повернув актуальний access token."
                    )
                    return@addOnSuccessListener
                }

                applyFreshAuthorizationAndResume(
                    token
                )
            }
            .addOnFailureListener { error ->
                freshAuthCheckInFlight = false

                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@addOnFailureListener
                }

                failFreshAuthorization(
                    error.message
                        ?: "Не вдалося оновити авторизацію Google/YTM."
                )
            }
    }

    private fun buildFreshAuthorizationRequest():
        AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope(YOUTUBE_SCOPE),
                    Scope(USERINFO_EMAIL_SCOPE),
                    Scope(USERINFO_PROFILE_SCOPE)
                )
            )
            .build()

    private fun handleFreshAuthorizationResult(
        data: Intent?
    ) {
        awaitingFreshAuthResolution = false

        if (data == null) {
            failFreshAuthorization(
                "Google не повернув результат авторизації."
            )
            return
        }

        try {
            val result =
                Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(
                        data
                    )

            val token =
                result.accessToken

            if (token.isNullOrBlank()) {
                failFreshAuthorization(
                    "Google не повернув access token."
                )
                return
            }

            applyFreshAuthorizationAndResume(
                token
            )
        } catch (error: ApiException) {
            failFreshAuthorization(
                "Авторизація Google/YTM не вдалася: " +
                    error.statusCode +
                    "."
            )
        } catch (error: Exception) {
            failFreshAuthorization(
                error.message
                    ?: "Не вдалося завершити авторизацію Google/YTM."
            )
        }
    }

    private fun applyFreshAuthorizationAndResume(
        token: String
    ) {
        val current =
            AuthSessionStore.current()

        AuthSessionStore.update(
            accessToken = token,
            googleAccountInfo =
                current.googleAccountInfo,
            youtubeChannelInfo =
                current.youtubeChannelInfo
        )

        PersistentAuthStateStore(this)
            .markSuccessfulAuthorization()

        val action =
            pendingFreshAuthAction

        if (action == null) {
            clearPendingFreshAuthorization()
            return
        }

        val payload =
            pendingFreshAuthPayload

        clearPendingFreshAuthorization()

        when (action) {
            FreshAuthAction.IMPORT_PLAYLIST_LIST ->
                loadYtmPlaylistList(token)

            FreshAuthAction.SELECTIVE_EXPORT_LIST ->
                loadSelectiveYtmExportList(token)

            FreshAuthAction.LOAD_PLAYLIST -> {
                val playlist =
                    payload
                        ?.let(
                            ::decodePlaylistSelection
                        )

                if (playlist == null) {
                    toast(
                        "Не вдалося відновити вибраний плейлист."
                    )
                } else {
                    loadYtmPlaylist(
                        token = token,
                        playlistInfo = playlist
                    )
                }
            }

            FreshAuthAction.EXPORT_ALL -> {
                val treeUri =
                    payload
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let(Uri::parse)

                if (treeUri == null) {
                    toast(
                        "Не вдалося відновити папку експорту."
                    )
                } else {
                    exportAllYtmPlaylistsToFolderAuthorized(
                        token = token,
                        treeUri = treeUri
                    )
                }
            }

            FreshAuthAction.EXPORT_SELECTED -> {
                val treeUri =
                    payload
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let(Uri::parse)

                if (treeUri == null) {
                    toast(
                        "Не вдалося відновити папку експорту."
                    )
                } else {
                    exportSelectedYtmPlaylistsToFolderAuthorized(
                        token = token,
                        treeUri = treeUri
                    )
                }
            }

            FreshAuthAction.PREPARE_INCREMENTAL -> {
                val baseTreeUri =
                    payload
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let(Uri::parse)

                if (baseTreeUri == null) {
                    toast(
                        "Не вдалося відновити папку baseline backup."
                    )
                } else {
                    prepareIncrementalBackupAuthorized(
                        token = token,
                        baseTreeUri = baseTreeUri
                    )
                }
            }
        }
    }

    private fun failFreshAuthorization(
        message: String
    ) {
        clearPendingFreshAuthorization()
        toast(
            "$message Локальний робочий список не змінено."
        )
    }

    private fun clearPendingFreshAuthorization() {
        pendingFreshAuthAction = null
        pendingFreshAuthPayload = null
        awaitingFreshAuthResolution = false
        freshAuthCheckInFlight = false
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
            requestCode ==
                freshAuthRequestCode
        ) {
            freshAuthCheckInFlight = false
            awaitingFreshAuthResolution = false

            if (resultCode == RESULT_OK) {
                handleFreshAuthorizationResult(
                    data
                )
            } else {
                clearPendingFreshAuthorization()
                toast(
                    "Авторизацію Google/YTM скасовано."
                )
            }

            return
        }

        if (resultCode != RESULT_OK) {
            return
        }

        when (requestCode) {
            fileRequestCode ->
                data
                    ?.data
                    ?.let(::loadFile)

            exportFolderRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::exportAllYtmPlaylistsToFolder
                    )

            selectiveExportFolderRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::exportSelectedYtmPlaylistsToFolder
                    )

            manifestImportFolderRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::openAccountBackupFolder
                    )

            incrementalBackupBaseRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::prepareIncrementalBackup
                    )

            incrementalBackupTargetRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::writeIncrementalBackup
                    )

            deltaChainRootRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::prepareDeltaChainRoot
                    )

            deltaChainTargetRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::materializeDeltaChain
                    )

            ytmPlaylistSelectorRequestCode ->
                handleYtmPlaylistSelection(data)

            selectiveExportSelectorRequestCode ->
                handleSelectiveExportSelection(data)

            deltaChainHeadSelectorRequestCode ->
                handleDeltaChainHeadSelection(data)

            manifestProjectSelectorRequestCode ->
                handleManifestProjectSelection(data)
        }
    }

    private fun buildUi() {
        val palette = AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
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
            sectionTitle(
                "Імпорт із YouTube/YTM"
            )
        )

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@ImportActivity
                    ).apply {
                        text =
                            "Плейлист з підключеного акаунта"
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
                        "Read-only імпорт: застосунок лише читає список плейлистів " +
                            "та їх треки. Плейлист у YouTube/YTM не змінюється. " +
                            "Треки відкриваються локально вже з точними videoId."
                    )
                )

                addView(
                    actionButton(
                        label =
                            "Вибрати плейлист з YTM",
                        primary = true
                    ) {
                        importFromYtmAccount()
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Вибрати плейлисти для експорту",
                        primary = false,
                        topMarginDp = 10
                    ) {
                        chooseSelectiveYtmExport()
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Експортувати всі плейлисти в папку",
                        primary = false,
                        topMarginDp = 8
                    ) {
                        chooseYtmExportFolder()
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Відкрити backup / manifest.json",
                        primary = false,
                        topMarginDp = 8
                    ) {
                        chooseAccountBackupFolder()
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Оновити backup (incremental)",
                        primary = false,
                        topMarginDp = 8
                    ) {
                        chooseIncrementalBackupBase()
                    }
                )

                addView(
                    actionButton(
                        label =
                            "Зібрати повний backup з ланцюжка",
                        primary = false,
                        topMarginDp = 8
                    ) {
                        chooseDeltaChainRoot()
                    }
                )
            }
        )

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
        UiChrome.applyScreenInsets(this, root)
    }

    private fun importFromYtmAccount() {
        requestFreshAuthorization(
            FreshAuthAction.IMPORT_PLAYLIST_LIST
        )
    }

    private fun loadYtmPlaylistList(
        token: String
    ) {
        toast(
            "Завантажую плейлисти YouTube/YTM…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listMyPlaylists(token)
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        playlists ->

                    if (playlists.isEmpty()) {
                        toast(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                        return@onSuccess
                    }

                    showYtmPlaylistPicker(
                        playlists
                    )
                }.onFailure { error ->
                    if (
                        invalidateAuthorizationIfNeeded(
                            error
                        )
                    ) {
                        return@onFailure
                    }

                    toast(
                        error.message
                            ?: "Не вдалося завантажити список плейлистів"
                    )
                }
            }
        }
    }

    private fun showYtmPlaylistPicker(
        playlists: List<YouTubePlaylistInfo>
    ) {
        startActivityForResult(
            ListSelectorActivity.singleIntent(
                activity = this,
                title =
                    "Вибрати плейлист YouTube/YTM",
                subtitle =
                    "Read-only імпорт: виберіть один плейлист.",
                labels =
                    playlists.map(
                        ::playlistSelectorLabel
                    ),
                values =
                    playlists.map(
                        ::encodePlaylistSelection
                    ),
                helpTitle =
                    "Що буде імпортовано?",
                helpMessage =
                    "Цей список показує плейлисти підключеного Google/YTM акаунта.\n\n" +
                        "YTM Importer лише читає вибраний плейлист і завантажує його треки " +
                        "у локальний робочий список із точними videoId. " +
                        "Плейлист у YouTube/YTM не змінюється.",
                confirmLabel =
                    "Вибрати"
            ),
            ytmPlaylistSelectorRequestCode
        )
    }

    private fun loadYtmPlaylist(
        token: String,
        playlistInfo: YouTubePlaylistInfo
    ) {
        toast(
            "Завантажую «${playlistInfo.title}»…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listPlaylistTracks(
                        accessToken = token,
                        playlistId =
                            playlistInfo.id
                    )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        loaded ->

                    if (loaded.tracks.isEmpty()) {
                        toast(
                            "Плейлист «${playlistInfo.title}» порожній або не містить доступних відео."
                        )
                        return@onSuccess
                    }

                    val imported =
                        ImportedPlaylist(
                            name =
                                playlistInfo.title,
                            tracks =
                                loaded.tracks
                                    .toMutableList()
                        )

                    finishImport(
                        imported = imported,
                        sourceLabel =
                            "YouTube/YTM (${playlistInfo.title})",
                        message =
                            "YTM playlist імпортовано: " +
                                "${imported.tracks.size} треків • " +
                                "точних videoId: ${imported.tracks.size} • " +
                                "playlistItems.list: ${loaded.requestCount} request(s)."
                    )
                }.onFailure { error ->
                    if (
                        invalidateAuthorizationIfNeeded(
                            error
                        )
                    ) {
                        return@onFailure
                    }

                    toast(
                        error.message
                            ?: "Не вдалося завантажити плейлист"
                    )
                }
            }
        }
    }

    private fun chooseSelectiveYtmExport() {
        requestFreshAuthorization(
            FreshAuthAction.SELECTIVE_EXPORT_LIST
        )
    }

    private fun loadSelectiveYtmExportList(
        token: String
    ) {
        toast(
            "Завантажую плейлисти для вибору…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listMyPlaylists(token)
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        playlists ->

                    if (playlists.isEmpty()) {
                        toast(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                        return@onSuccess
                    }

                    showSelectiveYtmExportPicker(
                        playlists
                    )
                }.onFailure { error ->
                    if (
                        invalidateAuthorizationIfNeeded(
                            error
                        )
                    ) {
                        return@onFailure
                    }

                    toast(
                        error.message
                            ?: "Не вдалося завантажити список плейлистів"
                    )
                }
            }
        }
    }

    private fun showSelectiveYtmExportPicker(
        playlists: List<YouTubePlaylistInfo>
    ) {
        val values =
            playlists.map(
                ::encodePlaylistSelection
            )

        val selectedIds =
            pendingSelectiveExport
                .mapTo(
                    linkedSetOf()
                ) {
                    it.id
                }

        val initialValues =
            playlists
                .filter {
                    it.id in selectedIds
                }
                .map(
                    ::encodePlaylistSelection
                )

        startActivityForResult(
            ListSelectorActivity.multiIntent(
                activity = this,
                title =
                    "Плейлисти для експорту",
                subtitle =
                    "Виберіть один або кілька плейлистів.",
                labels =
                    playlists.map(
                        ::playlistSelectorLabel
                    ),
                values = values,
                selectedValues =
                    initialValues,
                helpTitle =
                    "Що буде експортовано?",
                helpMessage =
                    "Вибрані плейлисти будуть прочитані з підключеного акаунта " +
                        "і збережені локально як YTM Project-файли разом із manifest.json.\n\n" +
                        "Експорт не змінює плейлисти в YouTube/YTM.",
                confirmLabel =
                    "Далі"
            ),
            selectiveExportSelectorRequestCode
        )
    }

    private fun chooseSelectiveYtmExportFolder() {
        if (pendingSelectiveExport.isEmpty()) {
            toast(
                "Спочатку виберіть плейлисти для експорту."
            )
            return
        }

        chooseSafTree(
            title =
                "Папка для вибраного експорту",
            access =
                SafTreeAccess.Access.READ_WRITE,
            requestCode =
                selectiveExportFolderRequestCode
        )
    }

    private fun chooseSafTree(
        title: String,
        access: SafTreeAccess.Access,
        requestCode: Int
    ) {
        startActivityForResult(
            StorageChooserActivity.treeIntent(
                activity = this,
                title = title,
                access = access
            ),
            requestCode
        )
    }

    private fun selectorValues(
        data: Intent?
    ): List<String> =
        data
            ?.getStringArrayListExtra(
                ListSelectorActivity.EXTRA_SELECTED_VALUES
            )
            ?.toList()
            .orEmpty()

    private fun handleYtmPlaylistSelection(
        data: Intent?
    ) {
        val playlist =
            selectorValues(data)
                .firstOrNull()
                ?.let(
                    ::decodePlaylistSelection
                )
                ?: return

        requestFreshAuthorization(
            action =
                FreshAuthAction.LOAD_PLAYLIST,
            payload =
                encodePlaylistSelection(
                    playlist
                )
        )
    }

    private fun handleSelectiveExportSelection(
        data: Intent?
    ) {
        val selected =
            selectorValues(data)
                .mapNotNull(
                    ::decodePlaylistSelection
                )

        if (selected.isEmpty()) {
            toast(
                "Виберіть хоча б один плейлист."
            )
            return
        }

        pendingSelectiveExport =
            selected

        chooseSelectiveYtmExportFolder()
    }

    private fun handleDeltaChainHeadSelection(
        data: Intent?
    ) {
        val decoded =
            selectorValues(data)
                .firstOrNull()
                ?.let(
                    ::decodeDeltaHeadSelection
                )
                ?: return

        resolveDeltaChain(
            treeUri = decoded.first,
            head = decoded.second
        )
    }

    private fun handleManifestProjectSelection(
        data: Intent?
    ) {
        val entry =
            selectorValues(data)
                .firstOrNull()
                ?.let(
                    ::decodeManifestEntrySelection
                )
                ?: return

        loadAccountBackupProject(entry)
    }

    private fun playlistSelectorLabel(
        playlist: YouTubePlaylistInfo
    ): String =
        buildString {
            append(playlist.title)
            append("\n")
            append(playlist.itemCount)
            append(" треків • ")
            append(
                when (
                    playlist.privacyStatus
                ) {
                    "public" ->
                        "публічний"

                    "unlisted" ->
                        "за посиланням"

                    else ->
                        "приватний"
                }
            )
        }

    private fun encodePlaylistSelection(
        playlist: YouTubePlaylistInfo
    ): String =
        JSONObject()
            .put(
                "id",
                playlist.id
            )
            .put(
                "title",
                playlist.title
            )
            .put(
                "privacyStatus",
                playlist.privacyStatus
            )
            .put(
                "itemCount",
                playlist.itemCount
            )
            .toString()

    private fun decodePlaylistSelection(
        raw: String
    ): YouTubePlaylistInfo? =
        runCatching {
            val item =
                JSONObject(raw)

            YouTubePlaylistInfo(
                id =
                    item.getString("id"),
                title =
                    item.getString("title"),
                privacyStatus =
                    item.getString(
                        "privacyStatus"
                    ),
                itemCount =
                    item.getLong(
                        "itemCount"
                    )
            )
        }.getOrNull()

    private fun encodeDeltaHeadSelection(
        treeUri: Uri,
        head: DeltaChainHead
    ): String =
        JSONObject()
            .put(
                "treeUri",
                treeUri.toString()
            )
            .put(
                "folderName",
                head.folderName
            )
            .put(
                "baseSessionName",
                head.baseSessionName
            )
            .put(
                "scopeMode",
                head.scopeMode
            )
            .put(
                "exportedAt",
                head.exportedAt
            )
            .toString()

    private fun decodeDeltaHeadSelection(
        raw: String
    ): Pair<Uri, DeltaChainHead>? =
        runCatching {
            val item =
                JSONObject(raw)

            Uri.parse(
                item.getString(
                    "treeUri"
                )
            ) to
                DeltaChainHead(
                    folderName =
                        item.getString(
                            "folderName"
                        ),
                    baseSessionName =
                        item.getString(
                            "baseSessionName"
                        ),
                    scopeMode =
                        item.getString(
                            "scopeMode"
                        ),
                    exportedAt =
                        item.getLong(
                            "exportedAt"
                        )
                )
        }.getOrNull()

    private fun encodeManifestEntrySelection(
        entry: AccountLibraryManifestEntry
    ): String =
        JSONObject()
            .put(
                "playlistId",
                entry.playlistId
            )
            .put(
                "title",
                entry.title
            )
            .put(
                "privacyStatus",
                entry.privacyStatus
            )
            .put(
                "sourceItemCount",
                entry.sourceItemCount
            )
            .put(
                "exportedTrackCount",
                entry.exportedTrackCount
            )
            .put(
                "playlistItemsRequests",
                entry.playlistItemsRequests
            )
            .put(
                "fileName",
                entry.fileName
            )
            .put(
                "projectUri",
                entry.projectUri.toString()
            )
            .toString()

    private fun decodeManifestEntrySelection(
        raw: String
    ): AccountLibraryManifestEntry? =
        runCatching {
            val item =
                JSONObject(raw)

            AccountLibraryManifestEntry(
                playlistId =
                    item.getString(
                        "playlistId"
                    ),
                title =
                    item.getString(
                        "title"
                    ),
                privacyStatus =
                    item.getString(
                        "privacyStatus"
                    ),
                sourceItemCount =
                    item.getLong(
                        "sourceItemCount"
                    ),
                exportedTrackCount =
                    item.getInt(
                        "exportedTrackCount"
                    ),
                playlistItemsRequests =
                    item.getInt(
                        "playlistItemsRequests"
                    ),
                fileName =
                    item.getString(
                        "fileName"
                    ),
                projectUri =
                    Uri.parse(
                        item.getString(
                            "projectUri"
                        )
                    )
            )
        }.getOrNull()

    private fun encodeSelectiveExportState(
        playlists: List<YouTubePlaylistInfo>
    ): String =
        JSONArray().apply {
            playlists.forEach {
                    playlist ->

                put(
                    JSONObject()
                        .put(
                            "id",
                            playlist.id
                        )
                        .put(
                            "title",
                            playlist.title
                        )
                        .put(
                            "privacyStatus",
                            playlist.privacyStatus
                        )
                        .put(
                            "itemCount",
                            playlist.itemCount
                        )
                )
            }
        }.toString()

    private fun decodeSelectiveExportState(
        raw: String?
    ): List<YouTubePlaylistInfo> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }

        return runCatching {
            val array =
                JSONArray(raw)

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    val item =
                        array.getJSONObject(index)

                    add(
                        YouTubePlaylistInfo(
                            id =
                                item.getString("id"),
                            title =
                                item.getString("title"),
                            privacyStatus =
                                item.getString(
                                    "privacyStatus"
                                ),
                            itemCount =
                                item.getLong(
                                    "itemCount"
                                )
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )
    }

    private fun chooseYtmExportFolder() {
        chooseSafTree(
            title =
                "Папка для експорту",
            access =
                SafTreeAccess.Access.READ_WRITE,
            requestCode =
                exportFolderRequestCode
        )
    }

    private fun exportAllYtmPlaylistsToFolder(
        treeUri: Uri
    ) {
        requestFreshAuthorization(
            action =
                FreshAuthAction.EXPORT_ALL,
            payload =
                treeUri.toString()
        )
    }

    private fun exportAllYtmPlaylistsToFolderAuthorized(
        token: String,
        treeUri: Uri
    ) {
        persistExportFolderPermission(
            treeUri
        )

        toast(
            "Готую read-only експорт плейлистів…"
        )

        executor.execute {
            val result =
                runCatching {
                    val playlists =
                        api.listMyPlaylists(token)

                    if (playlists.isEmpty()) {
                        error(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                    }

                    runOnUiThread {
                        if (
                            !isFinishing &&
                            !isDestroyed
                        ) {
                            toast(
                                "Знайдено ${playlists.size} плейлистів. Експортую…"
                            )
                        }
                    }

                    exportAccountPlaylistsToFolder(
                        token = token,
                        treeUri = treeUri,
                        playlists = playlists,
                        selectionMode = "ALL"
                    )
                }

            showBulkExportResult(
                result
            )
        }
    }

    private fun exportSelectedYtmPlaylistsToFolder(
        treeUri: Uri
    ) {
        requestFreshAuthorization(
            action =
                FreshAuthAction.EXPORT_SELECTED,
            payload =
                treeUri.toString()
        )
    }

    private fun exportSelectedYtmPlaylistsToFolderAuthorized(
        token: String,
        treeUri: Uri
    ) {
        val selected =
            pendingSelectiveExport

        if (selected.isEmpty()) {
            toast(
                "Вибрані плейлисти не відновлено. Повторіть вибір."
            )
            return
        }

        persistExportFolderPermission(
            treeUri
        )

        toast(
            "Експортую вибрані плейлисти: ${selected.size}…"
        )

        executor.execute {
            val result =
                runCatching {
                    exportAccountPlaylistsToFolder(
                        token = token,
                        treeUri = treeUri,
                        playlists = selected,
                        selectionMode = "SELECTED"
                    )
                }

            showBulkExportResult(
                result
            )
        }
    }

    private fun persistExportFolderPermission(
        treeUri: Uri
    ) {
        runCatching {
            SafTreeAccess.persist(
                context = this,
                treeUri = treeUri,
                access =
                    SafTreeAccess.Access.READ_WRITE
            )
        }
    }

    private fun exportAccountPlaylistsToFolder(
        token: String,
        treeUri: Uri,
        playlists: List<YouTubePlaylistInfo>,
        selectionMode: String
    ): BulkExportResult {
        val session =
            AccountLibraryExporter
                .createSessionFolder(
                    resolver =
                        contentResolver,
                    treeUri =
                        treeUri
                )

        val records =
            mutableListOf<
                AccountLibraryExporter.ExportRecord
            >()

        playlists.forEach {
                playlistInfo ->

            if (
                playlistInfo.itemCount <= 0
            ) {
                records +=
                    AccountLibraryExporter.ExportRecord(
                        playlistId =
                            playlistInfo.id,
                        title =
                            playlistInfo.title,
                        privacyStatus =
                            playlistInfo.privacyStatus,
                        sourceItemCount =
                            playlistInfo.itemCount,
                        exportedTrackCount = 0,
                        playlistItemsRequests = 0,
                        status =
                            "SKIPPED_EMPTY",
                        fileName = null
                    )

                return@forEach
            }

            runCatching {
                api.listPlaylistTracks(
                    accessToken =
                        token,
                    playlistId =
                        playlistInfo.id
                )
            }.onSuccess {
                    loaded ->

                if (
                    loaded.tracks.isEmpty()
                ) {
                    records +=
                        AccountLibraryExporter.ExportRecord(
                            playlistId =
                                playlistInfo.id,
                            title =
                                playlistInfo.title,
                            privacyStatus =
                                playlistInfo.privacyStatus,
                            sourceItemCount =
                                playlistInfo.itemCount,
                            exportedTrackCount = 0,
                            playlistItemsRequests =
                                loaded.requestCount,
                            status =
                                "SKIPPED_NO_ACCESSIBLE_TRACKS",
                            fileName = null
                        )
                } else {
                    val imported =
                        ImportedPlaylist(
                            name =
                                playlistInfo.title,
                            tracks =
                                loaded.tracks
                                    .toMutableList()
                        )

                    val fileName =
                        AccountLibraryExporter
                            .writePlaylistProject(
                                resolver =
                                    contentResolver,
                                session =
                                    session,
                                playlistInfo =
                                    playlistInfo,
                                playlist =
                                    imported,
                                appVersion =
                                    BuildConfig.VERSION_NAME
                            )

                    records +=
                        AccountLibraryExporter.ExportRecord(
                            playlistId =
                                playlistInfo.id,
                            title =
                                playlistInfo.title,
                            privacyStatus =
                                playlistInfo.privacyStatus,
                            sourceItemCount =
                                playlistInfo.itemCount,
                            exportedTrackCount =
                                imported.tracks.size,
                            playlistItemsRequests =
                                loaded.requestCount,
                            status =
                                "EXPORTED",
                            fileName =
                                fileName
                        )
                }
            }.onFailure {
                    error ->

                if (
                    isAuthorizationFailure(
                        error
                    )
                ) {
                    throw error
                }

                records +=
                    AccountLibraryExporter.ExportRecord(
                        playlistId =
                            playlistInfo.id,
                        title =
                            playlistInfo.title,
                        privacyStatus =
                            playlistInfo.privacyStatus,
                        sourceItemCount =
                            playlistInfo.itemCount,
                        exportedTrackCount = 0,
                        playlistItemsRequests = 0,
                        status =
                            "FAILED",
                        fileName = null,
                        error =
                            error.message
                                ?: error
                                    .javaClass
                                    .simpleName
                    )
            }
        }

        val manifestFile =
            AccountLibraryExporter
                .writeManifest(
                    resolver =
                        contentResolver,
                    session =
                        session,
                    appVersion =
                        BuildConfig.VERSION_NAME,
                    records =
                        records,
                    selectionMode =
                        selectionMode
                )

        return BulkExportResult(
            selectionMode =
                selectionMode,
            folderName =
                session.folderName,
            manifestFile =
                manifestFile,
            playlistCount =
                records.size,
            exportedProjects =
                records.count {
                    it.status ==
                        "EXPORTED"
                },
            skippedPlaylists =
                records.count {
                    it.status
                        .startsWith(
                            "SKIPPED"
                        )
                },
            failedPlaylists =
                records.count {
                    it.status ==
                        "FAILED"
                },
            playlistItemsRequests =
                records.sumOf {
                    it.playlistItemsRequests
                }
        )
    }

    private fun showBulkExportResult(
        result: Result<BulkExportResult>
    ) {
        runOnUiThread {
            if (
                isFinishing ||
                isDestroyed
            ) {
                return@runOnUiThread
            }

            result.onSuccess {
                    summary ->

                val scopeLabel =
                    if (
                        summary.selectionMode ==
                            "SELECTED"
                    ) {
                        "Вибрано для експорту"
                    } else {
                        "Плейлистів акаунта"
                    }

                showStatusMessage(
                    title =
                        "Експорт завершено",
                    message =
                        "$scopeLabel: ${summary.playlistCount}\n" +
                            "Збережено YTM Project: ${summary.exportedProjects}\n" +
                            "Пропущено: ${summary.skippedPlaylists}\n" +
                            "Помилок: ${summary.failedPlaylists}\n" +
                            "playlistItems.list: ${summary.playlistItemsRequests} request(s)\n\n" +
                            "Папка: ${summary.folderName}\n" +
                            "Індекс: ${summary.manifestFile}"
                )
            }.onFailure { error ->
                if (
                    invalidateAuthorizationIfNeeded(
                        error
                    )
                ) {
                    return@onFailure
                }

                showStatusMessage(
                    title =
                        "Експорт не завершено",
                    message =
                        error.message
                            ?: "Невідома помилка експорту"
                )
            }
        }
    }

    private data class BulkExportResult(
        val selectionMode: String,
        val folderName: String,
        val manifestFile: String,
        val playlistCount: Int,
        val exportedProjects: Int,
        val skippedPlaylists: Int,
        val failedPlaylists: Int,
        val playlistItemsRequests: Int
    )



    private fun chooseIncrementalBackupBase() {
        chooseSafTree(
            title =
                "Основа incremental backup",
            access =
                SafTreeAccess.Access.READ,
            requestCode =
                incrementalBackupBaseRequestCode
        )
    }

    private fun prepareIncrementalBackup(
        baseTreeUri: Uri
    ) {
        requestFreshAuthorization(
            action =
                FreshAuthAction.PREPARE_INCREMENTAL,
            payload =
                baseTreeUri.toString()
        )
    }

    private fun prepareIncrementalBackupAuthorized(
        token: String,
        baseTreeUri: Uri
    ) {
        pendingIncrementalBackupPlan =
            null

        runCatching {
            SafTreeAccess.persist(
                context = this,
                treeUri = baseTreeUri,
                access =
                    SafTreeAccess.Access.READ
            )
        }

        toast(
            "Читаю baseline backup та список плейлистів…"
        )

        executor.execute {
            val result =
                runCatching {
                    val baseline =
                        AccountLibraryIncrementalBackup
                            .readBaseline(
                                resolver =
                                    contentResolver,
                                treeUri =
                                    baseTreeUri
                            )

                    val accountPlaylists =
                        api.listMyPlaylists(
                            token
                        )

                    if (
                        accountPlaylists.isEmpty()
                    ) {
                        error(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                    }

                    val scopedPlaylists =
                        AccountLibraryIncrementalBackup
                            .scopeCurrentPlaylists(
                                baseline =
                                    baseline,
                                currentPlaylists =
                                    accountPlaylists
                            )

                    IncrementalBackupPreflight(
                        baseline =
                            baseline,
                        playlists =
                            scopedPlaylists,
                        estimatedPlaylistItemsRequests =
                            AccountLibraryIncrementalBackup
                                .estimatePlaylistItemsRequests(
                                    scopedPlaylists
                                )
                    )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        preflight ->

                    showIncrementalBackupScanConfirmation(
                        preflight
                    )
                }.onFailure {
                        error ->

                    if (
                        invalidateAuthorizationIfNeeded(
                            error
                        )
                    ) {
                        return@onFailure
                    }

                    toast(
                        error.message
                            ?: "Не вдалося підготувати incremental backup"
                    )
                }
            }
        }
    }

    private fun showIncrementalBackupScanConfirmation(
        preflight: IncrementalBackupPreflight
    ) {
        pendingIncrementalBackupPreflight =
            preflight

        val scopeText =
            if (
                preflight.baseline
                    .scopeMode ==
                    "SELECTED"
            ) {
                "SELECTED (${preflight.baseline.scopePlaylistIds.size})"
            } else {
                "ALL"
            }

        windowState.show(
            WINDOW_INCREMENTAL_SCAN_CONFIRM
        ) {
            UiChrome.showMessageDialog(
                activity = this,
                title =
                    "Інкрементальний backup — перевірка",
                message =
                    "Основа: ${preflight.baseline.folderName}\n" +
                        "Режим: $scopeText\n" +
                        "Поточних плейлистів у режимі: ${preflight.playlists.size}\n" +
                        "Оцінка playlistItems.list: ${preflight.estimatedPlaylistItemsRequests} request(s)\n\n" +
                        "Щоб надійно знайти зміни навіть при тій самій кількості треків, " +
                        "застосунок прочитає вміст кожного непорожнього плейлиста у scope.\n\n" +
                        "search.list: 0 • write API: 0",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label =
                                "Перевірити",
                            tone =
                                UiChrome.ActionTone.ACCENT,
                            onClick = {
                                val token =
                                    AuthSessionStore
                                        .current()
                                        .accessToken

                                if (token.isNullOrBlank()) {
                                    toast(
                                        "Авторизація Google/YTM недоступна. Підключіть акаунт ще раз."
                                    )
                                } else {
                                    pendingIncrementalBackupPreflight =
                                        null

                                    scanIncrementalBackup(
                                        token = token,
                                        preflight =
                                            preflight
                                    )
                                }
                            }
                        ),
                        UiChrome.DialogAction(
                            label =
                                "Скасувати",
                            tone =
                                UiChrome.ActionTone.NORMAL,
                            onClick = {
                                pendingIncrementalBackupPreflight =
                                    null
                            }
                        )
                    )
            )
        }
    }

    private fun scanIncrementalBackup(
        token: String,
        preflight: IncrementalBackupPreflight
    ) {
        toast(
            "Сканую плейлисти для incremental backup…"
        )

        executor.execute {
            val result =
                runCatching {
                    val records =
                        mutableListOf<
                            IncrementalBackupRecord
                        >()

                    var requests = 0

                    preflight.playlists.forEach {
                            playlistInfo ->

                        if (
                            playlistInfo.itemCount <=
                                0L
                        ) {
                            val empty =
                                ImportedPlaylist(
                                    name =
                                        playlistInfo.title,
                                    tracks =
                                        mutableListOf()
                                )

                            records +=
                                AccountLibraryIncrementalBackup
                                    .classify(
                                        baseline =
                                            preflight.baseline,
                                        playlistInfo =
                                            playlistInfo,
                                        playlist =
                                            empty,
                                        contentFingerprint =
                                            AccountLibraryIncrementalBackup
                                                .fingerprint(
                                                    empty
                                                ),
                                        playlistItemsRequests =
                                            0
                                    )

                            return@forEach
                        }

                        runCatching {
                            api.listPlaylistTracks(
                                accessToken =
                                    token,
                                playlistId =
                                    playlistInfo.id
                            )
                        }.onSuccess {
                                loaded ->

                            requests +=
                                loaded.requestCount

                            if (
                                loaded.tracks.isEmpty()
                            ) {
                                records +=
                                    AccountLibraryIncrementalBackup
                                        .failedRecord(
                                            baseline =
                                                preflight.baseline,
                                            playlistInfo =
                                                playlistInfo,
                                            playlistItemsRequests =
                                                loaded.requestCount,
                                            error =
                                                IllegalStateException(
                                                    "Плейлист не повернув доступних videoId"
                                                )
                                        )
                            } else {
                                val imported =
                                    ImportedPlaylist(
                                        name =
                                            playlistInfo.title,
                                        tracks =
                                            loaded.tracks
                                                .toMutableList()
                                    )

                                records +=
                                    AccountLibraryIncrementalBackup
                                        .classify(
                                            baseline =
                                                preflight.baseline,
                                            playlistInfo =
                                                playlistInfo,
                                            playlist =
                                                imported,
                                            contentFingerprint =
                                                AccountLibraryIncrementalBackup
                                                    .fingerprint(
                                                        imported
                                                    ),
                                            playlistItemsRequests =
                                                loaded.requestCount
                                        )
                            }
                        }.onFailure {
                                error ->

                            if (
                                isAuthorizationFailure(
                                    error
                                )
                            ) {
                                throw error
                            }

                            requests += 1

                            records +=
                                AccountLibraryIncrementalBackup
                                    .failedRecord(
                                        baseline =
                                            preflight.baseline,
                                        playlistInfo =
                                            playlistInfo,
                                        playlistItemsRequests =
                                            1,
                                        error =
                                            error
                                    )
                        }
                    }

                    val currentScopedIds =
                        preflight.playlists
                            .mapTo(
                                linkedSetOf()
                            ) {
                                it.id
                            }

                    records +=
                        AccountLibraryIncrementalBackup
                            .missingRecords(
                                baseline =
                                    preflight.baseline,
                                currentScopedIds =
                                    currentScopedIds
                            )

                    IncrementalBackupPlan(
                        baseline =
                            preflight.baseline,
                        records =
                            records,
                        playlistItemsRequests =
                            requests
                    )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        plan ->

                    pendingIncrementalBackupPlan =
                        plan

                    showIncrementalBackupPreview(
                        plan
                    )
                }.onFailure {
                        error ->

                    pendingIncrementalBackupPlan =
                        null

                    if (
                        invalidateAuthorizationIfNeeded(
                            error
                        )
                    ) {
                        return@onFailure
                    }

                    toast(
                        error.message
                            ?: "Не вдалося просканувати backup"
                    )
                }
            }
        }
    }

    private fun showIncrementalBackupPreview(
        plan: IncrementalBackupPlan
    ) {
        pendingIncrementalBackupPlan =
            plan

        windowState.show(
            WINDOW_INCREMENTAL_PREVIEW
        ) {
            UiChrome.showMessageDialog(
                activity = this,
                title =
                    "Інкрементальний backup — попередній перегляд",
                message =
                    "Нові: ${plan.newCount}\n" +
                        "Змінені: ${plan.updatedCount}\n" +
                        "Без змін: ${plan.unchangedCount}\n" +
                        "Зникли / недоступні в режимі: ${plan.missingCount}\n" +
                        "Помилки читання: ${plan.failedCount}\n" +
                        "playlistItems.list: ${plan.playlistItemsRequests} request(s)\n\n" +
                        "Буде створено нову delta-папку. " +
                        "Старий backup не змінюється і не видаляється.\n\n" +
                        "Для збереження виберіть спільну батьківську папку, " +
                        "а не саму папку baseline або delta.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label =
                                "Зберегти delta",
                            tone =
                                UiChrome.ActionTone.ACCENT,
                            onClick = {
                                chooseIncrementalBackupTarget()
                            }
                        ),
                        UiChrome.DialogAction(
                            label =
                                "Скасувати",
                            tone =
                                UiChrome.ActionTone.NORMAL,
                            onClick = {
                                pendingIncrementalBackupPlan =
                                    null
                            }
                        )
                    )
            )
        }
    }

    private fun chooseIncrementalBackupTarget() {
        if (
            pendingIncrementalBackupPlan ==
                null
        ) {
            toast(
                "План інкрементального backup втрачено. Повторіть перевірку."
            )
            return
        }

        chooseSafTree(
            title =
                "Куди зберегти incremental backup",
            access =
                SafTreeAccess.Access.READ_WRITE,
            requestCode =
                incrementalBackupTargetRequestCode
        )
    }

    private fun writeIncrementalBackup(
        treeUri: Uri
    ) {
        val plan =
            pendingIncrementalBackupPlan

        if (plan == null) {
            toast(
                "План інкрементального backup втрачено. Повторіть перевірку."
            )
            return
        }

        persistExportFolderPermission(
            treeUri
        )

        toast(
            "Записую інкрементальну delta локально…"
        )

        executor.execute {
            val result =
                runCatching {
                    AccountLibraryIncrementalBackup
                        .writeDelta(
                            resolver =
                                contentResolver,
                            treeUri =
                                treeUri,
                            appVersion =
                                BuildConfig.VERSION_NAME,
                            plan =
                                plan
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        summary ->

                    pendingIncrementalBackupPlan =
                        null

                    showIncrementalBackupResult(
                        summary
                    )
                }.onFailure {
                        error ->

                    toast(
                        error.message
                            ?: "Не вдалося записати інкрементальний backup"
                    )
                }
            }
        }
    }

    private fun showIncrementalBackupResult(
        summary: IncrementalBackupWriteResult
    ) {
        showStatusMessage(
            title =
                "Інкрементальний backup збережено",
            message =
                "Плейлистів у поточному режимі: ${summary.currentPlaylistCount}\n" +
                    "Нові: ${summary.newCount}\n" +
                    "Змінені: ${summary.updatedCount}\n" +
                    "Без змін: ${summary.unchangedCount}\n" +
                    "Зникли: ${summary.missingCount}\n" +
                    "Помилки: ${summary.failedCount}\n" +
                    "Нових YTM Project файлів: ${summary.writtenProjects}\n" +
                    "playlistItems.list: ${summary.playlistItemsRequests} request(s)\n\n" +
                    "Папка delta: ${summary.folderName}\n" +
                    "Індекс: ${summary.manifestFile}\n\n" +
                    "Старий backup не змінено."
        )
    }

    private data class IncrementalBackupPreflight(
        val baseline: AccountBackupBaseline,
        val playlists:
            List<YouTubePlaylistInfo>,
        val estimatedPlaylistItemsRequests: Int
    )


    private fun chooseDeltaChainRoot() {
        chooseSafTree(
            title =
                "Папка з backup-ланцюжком",
            access =
                SafTreeAccess.Access.READ,
            requestCode =
                deltaChainRootRequestCode
        )
    }

    private fun prepareDeltaChainRoot(
        treeUri: Uri
    ) {
        pendingDeltaChainPlan =
            null

        runCatching {
            SafTreeAccess.persist(
                context = this,
                treeUri = treeUri,
                access =
                    SafTreeAccess.Access.READ
            )
        }

        toast(
            "Сканую локальні backup-сесії…"
        )

        executor.execute {
            val result =
                runCatching {
                    AccountLibraryDeltaChainRestorer
                        .discoverHeads(
                            resolver =
                                contentResolver,
                            treeUri =
                                treeUri
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        heads ->

                    showDeltaChainHeadPicker(
                        treeUri = treeUri,
                        heads = heads
                    )
                }.onFailure {
                        error ->

                    toast(
                        error.message
                            ?: "Не вдалося знайти ланцюжок backup"
                    )
                }
            }
        }
    }

    private fun showDeltaChainHeadPicker(
        treeUri: Uri,
        heads: List<DeltaChainHead>
    ) {
        if (
            heads.size ==
                1
        ) {
            resolveDeltaChain(
                treeUri = treeUri,
                head =
                    heads.first()
            )
            return
        }

        startActivityForResult(
            ListSelectorActivity.singleIntent(
                activity = this,
                title =
                    "Кінцева delta-сесія",
                subtitle =
                    "Знайдено ${heads.size} незалежних кінцевих сесій.",
                labels =
                    heads.map { head ->
                        "${head.folderName}\n${head.scopeMode}"
                    },
                values =
                    heads.map { head ->
                        encodeDeltaHeadSelection(
                            treeUri = treeUri,
                            head = head
                        )
                    },
                helpTitle =
                    "Що означає цей список?",
                helpMessage =
                    "Кожен пункт — кінцева delta-сесія окремого ланцюжка backup.\n\n" +
                        "Виберіть той head, до стану якого потрібно зібрати новий повний backup. " +
                        "Операція локальна і не змінює YouTube/YTM.",
                confirmLabel =
                    "Вибрати"
            ),
            deltaChainHeadSelectorRequestCode
        )
    }

    private fun resolveDeltaChain(
        treeUri: Uri,
        head: DeltaChainHead
    ) {
        toast(
            "Відновлюю логічний стан із ланцюжка backup…"
        )

        executor.execute {
            val result =
                runCatching {
                    AccountLibraryDeltaChainRestorer
                        .resolvePlan(
                            resolver =
                                contentResolver,
                            treeUri =
                                treeUri,
                            headFolderName =
                                head.folderName
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        plan ->

                    pendingDeltaChainPlan =
                        plan

                    showDeltaChainPreview(
                        plan
                    )
                }.onFailure {
                        error ->

                    pendingDeltaChainPlan =
                        null

                    showStatusMessage(
                        title =
                            "Ланцюжок backup — помилка",
                        message =
                            error.message
                                ?: "Не вдалося відновити ланцюжок"
                    )
                }
            }
        }
    }

    private fun showDeltaChainPreview(
        plan: DeltaChainPlan
    ) {
        pendingDeltaChainPlan =
            plan

        val scopeText =
            if (
                plan.scopeMode ==
                    "SELECTED"
            ) {
                "SELECTED (${plan.scopePlaylistIds.size})"
            } else {
                "ALL"
            }

        windowState.show(
            WINDOW_DELTA_CHAIN_PREVIEW
        ) {
            UiChrome.showMessageDialog(
                activity = this,
                title =
                    "Ланцюжок backup — попередній перегляд",
                message =
                    "Основа: ${plan.baseFolderName}\n" +
                        "Кінцева сесія: ${plan.headFolderName}\n" +
                        "Ланок у ланцюжку: ${plan.chainLength}\n" +
                        "Режим: $scopeText\n\n" +
                        "Плейлистів у фінальному стані: ${plan.playlistCount}\n" +
                        "Джерел YTM Project: ${plan.projectCount}\n" +
                        "Порожніх плейлистів: ${plan.emptyCount}\n" +
                        "Застосовано подій MISSING: ${plan.missingEvents}\n\n" +
                        "Локально: YouTube API = 0.\n" +
                        "Вихідні папки backup не змінюються.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label =
                                "Створити",
                            tone =
                                UiChrome.ActionTone.ACCENT,
                            onClick = {
                                chooseDeltaChainTarget()
                            }
                        ),
                        UiChrome.DialogAction(
                            label =
                                "Скасувати",
                            tone =
                                UiChrome.ActionTone.NORMAL,
                            onClick = {
                                pendingDeltaChainPlan =
                                    null
                            }
                        )
                    )
            )
        }
    }

    private fun chooseDeltaChainTarget() {
        if (
            pendingDeltaChainPlan ==
                null
        ) {
            toast(
                "План ланцюжка backup втрачено. Повторіть сканування."
            )
            return
        }

        chooseSafTree(
            title =
                "Куди зберегти повний backup",
            access =
                SafTreeAccess.Access.READ_WRITE,
            requestCode =
                deltaChainTargetRequestCode
        )
    }

    private fun materializeDeltaChain(
        treeUri: Uri
    ) {
        val plan =
            pendingDeltaChainPlan

        if (plan == null) {
            toast(
                "План ланцюжка backup втрачено. Повторіть сканування."
            )
            return
        }

        persistExportFolderPermission(
            treeUri
        )

        toast(
            "Створюю зведений backup локально…"
        )

        executor.execute {
            val result =
                runCatching {
                    AccountLibraryDeltaChainRestorer
                        .materialize(
                            resolver =
                                contentResolver,
                            treeUri =
                                treeUri,
                            appVersion =
                                BuildConfig.VERSION_NAME,
                            plan =
                                plan
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        summary ->

                    pendingDeltaChainPlan =
                        null

                    showDeltaChainResult(
                        summary
                    )
                }.onFailure {
                        error ->

                    showStatusMessage(
                        title =
                            "Зведений backup — помилка",
                        message =
                            error.message
                                ?: "Не вдалося створити зведений backup із ланцюжка"
                    )
                }
            }
        }
    }

    private fun showDeltaChainResult(
        summary: DeltaChainMaterializeResult
    ) {
        showStatusMessage(
            title =
                "Зведений backup збережено",
            message =
                "Ланок у вихідному ланцюжку: ${summary.chainLength}\n" +
                    "Фінальних плейлистів: ${summary.playlistCount}\n" +
                    "YTM Project файлів: ${summary.exportedProjects}\n" +
                    "Порожніх плейлистів: ${summary.emptyPlaylists}\n\n" +
                    "Папка: ${summary.folderName}\n" +
                    "Індекс: ${summary.manifestFile}\n\n" +
                    "Це самодостатній повний backup. " +
                    "Його можна відкрити через «Відкрити backup / manifest.json» " +
                    "або використати як основу для наступного інкрементального backup."
        )
    }

    private fun chooseAccountBackupFolder() {
        chooseSafTree(
            title =
                "Відкрити backup",
            access =
                SafTreeAccess.Access.READ,
            requestCode =
                manifestImportFolderRequestCode
        )
    }

    private fun openAccountBackupFolder(
        treeUri: Uri
    ) {
        runCatching {
            SafTreeAccess.persist(
                context = this,
                treeUri = treeUri,
                access =
                    SafTreeAccess.Access.READ
            )
        }

        toast(
            "Читаю manifest.json локально…"
        )

        executor.execute {
            val result =
                runCatching {
                    AccountLibraryManifestImporter
                        .readManifest(
                            resolver =
                                contentResolver,
                            treeUri =
                                treeUri
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        manifest ->

                    showAccountBackupPicker(
                        manifest
                    )
                }.onFailure { error ->
                    when (error) {
                        is IncrementalDeltaManifestException ->
                            showIncrementalDeltaBoundary()

                        else ->
                            toast(
                                error.message
                                    ?: "Не вдалося прочитати backup manifest"
                            )
                    }
                }
            }
        }
    }

    private fun showIncrementalDeltaBoundary() {
        showStatusMessage(
            title =
                "Інкрементальний delta backup",
            message =
                "Це delta backup, а не повний export.\n\n" +
                    "Щоб отримати повний стан, скористайтеся " +
                    "«Зібрати повний backup з ланцюжка» і виберіть " +
                    "спільну батьківську папку.\n\n" +
                    "Для наступного інкрементального backup виберіть цю папку через " +
                    "«Оновити backup (incremental)»."
        )
    }

    private fun showAccountBackupPicker(
        manifest:
            AccountLibraryManifestImport
    ) {
        val missingNote =
            if (
                manifest.missingProjectFiles > 0
            ) {
                " • відсутніх файлів: " +
                    manifest.missingProjectFiles
            } else {
                ""
            }

        val subtitle =
            "manifest v${manifest.schemaVersion} • " +
                "${manifest.selectionMode} • " +
                "доступно ${manifest.entries.size}/" +
                "${manifest.exportedProjects}" +
                missingNote

        startActivityForResult(
            ListSelectorActivity.singleIntent(
                activity = this,
                title =
                    "Backup / manifest.json",
                subtitle = subtitle,
                labels =
                    manifest.entries.map { entry ->
                        entry.title +
                            "\n" +
                            entry.exportedTrackCount +
                            " треків • " +
                            manifestPrivacyLabel(
                                entry.privacyStatus
                            )
                    },
                values =
                    manifest.entries.map(
                        ::encodeManifestEntrySelection
                    ),
                helpTitle =
                    "Що це за список?",
                helpMessage =
                    "Це YTM Project-файли, знайдені через manifest.json у вибраному backup.\n\n" +
                        "Виберіть один плейлист, щоб відкрити його як локальний робочий список. " +
                        "YouTube API для цього не використовується.",
                confirmLabel =
                    "Відкрити"
            ),
            manifestProjectSelectorRequestCode
        )
    }

    private fun loadAccountBackupProject(
        entry: AccountLibraryManifestEntry
    ) {
        toast(
            "Відкриваю «${entry.title}»…"
        )

        executor.execute {
            val result =
                runCatching {
                    AccountLibraryManifestImporter
                        .loadProject(
                            resolver =
                                contentResolver,
                            entry =
                                entry
                        )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        project ->

                    finishProjectImport(
                        project = project,
                        fileName =
                            entry.fileName
                    )
                }.onFailure { error ->
                    toast(
                        error.message
                            ?: "Не вдалося відкрити YTM Project із backup"
                    )
                }
            }
        }
    }

    private fun manifestPrivacyLabel(
        privacyStatus: String
    ): String =
        when (
            privacyStatus
                .lowercase()
        ) {
            "public" ->
                "публічний"

            "unlisted" ->
                "за посиланням"

            "private" ->
                "приватний"

            else ->
                "privacy: $privacyStatus"
        }

    /**
     * Opens the YTM Importer recent-file selector first.
     *
     * The system picker remains available from the selector as a fallback.
     * Its MIME filter stays permissive because some Android providers expose
     * CSV/TXT/JSON files with unexpected MIME types.
     */
    private fun chooseFile() {
        startActivityForResult(
            Intent(
                this,
                RecentFileChooserActivity::class.java
            ).apply {
                putExtra(
                    RecentFileChooserActivity.EXTRA_TITLE,
                    "Імпорт файла"
                )
                putExtra(
                    RecentFileChooserActivity.EXTRA_MIME_TYPE,
                    "*/*"
                )
                putExtra(
                    RecentFileChooserActivity.EXTRA_ALLOWED_EXTENSIONS,
                    arrayOf(
                        "txt",
                        "csv",
                        "json"
                    )
                )
            },
            fileRequestCode
        )
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
        clearWorkspaceDialogOpen =
            true

        clearWorkspaceDialog =
            UiChrome.showDangerConfirmDialog(
                activity = this,
                title =
                    "Очистити поточний список?",
                message =
                    "Буде видалено тільки автозбережений локальний робочий список " +
                        "«$playlistName».\n\n" +
                        "YTM Project-файли та плейлисти в YouTube/YTM не змінюються.",
                confirmLabel =
                    "Так, очистити"
            ) {
                clearWorkspaceDialogOpen =
                    false

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
            }.also { dialog ->
                dialog.setOnDismissListener {
                    clearWorkspaceDialogOpen =
                        false
                    clearWorkspaceDialog =
                        null
                }
            }
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
                UiChrome.backButton(
                    activity = this@ImportActivity,
                    onClick = { finish() }
                ),
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            addView(
                UiChrome.emphasizedTitle(
                    activity = this@ImportActivity,
                    label = "Імпорт"
                ).apply {
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
                AppThemeManager.surfaceDrawable(
                    context = this@ImportActivity,
                    fill = AppThemeManager.palette(this@ImportActivity).surface,
                    radiusDp = 14,
                    accentStroke = true
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
        topMarginDp: Int = 0,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            minimumHeight = dp(58)
            minHeight = dp(58)
            setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(10)
            )
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 14
            )
            background =
                if (primary) {
                    AppThemeManager.accentButtonDrawable(this@ImportActivity, 11)
                } else {
                    AppThemeManager.neutralButtonDrawable(this@ImportActivity, 11)
                }
            setOnClickListener {
                action()
            }
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(topMarginDp)
                }
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

    private data class ImportNonConfigState(
        val incrementalPlan:
            IncrementalBackupPlan?,
        val incrementalPreflight:
            IncrementalBackupPreflight?,
        val deltaChainPlan:
            DeltaChainPlan?
    )

    private enum class FreshAuthAction {
        IMPORT_PLAYLIST_LIST,
        SELECTIVE_EXPORT_LIST,
        LOAD_PLAYLIST,
        EXPORT_ALL,
        EXPORT_SELECTED,
        PREPARE_INCREMENTAL
    }

    companion object {
        private const val STATE_SELECTIVE_EXPORT =
            "selective_export_playlists"

        private const val STATE_CLEAR_WORKSPACE_DIALOG_OPEN =
            "clear_workspace_dialog_open"
        private const val STATE_PENDING_FRESH_AUTH_ACTION =
            "pending_fresh_auth_action"
        private const val STATE_PENDING_FRESH_AUTH_PAYLOAD =
            "pending_fresh_auth_payload"
        private const val STATE_AWAITING_FRESH_AUTH_RESOLUTION =
            "awaiting_fresh_auth_resolution"
        private const val STATE_WINDOW =
            "import_window"
        private const val WINDOW_AUTH_INVALIDATED =
            "auth_invalidated"
        private const val WINDOW_STATUS_MESSAGE =
            "status_message"
        private const val WINDOW_INCREMENTAL_SCAN_CONFIRM =
            "incremental_scan_confirm"
        private const val WINDOW_INCREMENTAL_PREVIEW =
            "incremental_preview"
        private const val WINDOW_DELTA_CHAIN_PREVIEW =
            "delta_chain_preview"

        private const val ARG_STATUS_TITLE =
            "status_title"
        private const val ARG_STATUS_MESSAGE =
            "status_message"

        const val EXTRA_IMPORT_MESSAGE =
            "import_message"

        const val EXTRA_CLEAR_WORKSPACE =
            "clear_current_workspace"

        const val EXTRA_START_ACTION =
            "import_start_action"

        const val ACTION_SELECTIVE_EXPORT =
            "selective_export"

        private const val YOUTUBE_SCOPE =
            "https://www.googleapis.com/auth/youtube.force-ssl"
        private const val USERINFO_EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email"
        private const val USERINFO_PROFILE_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile"

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
