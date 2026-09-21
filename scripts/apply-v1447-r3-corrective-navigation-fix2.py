#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path

ROOT = Path.cwd()
TARGET = ROOT / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
OPS = [
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Workflow relay helper import",
        "old": "import com.saney.ytmimporter.ui.HomeDashboardChrome\nimport com.saney.ytmimporter.ui.TrackAdapter\n",
        "new": "import com.saney.ytmimporter.ui.HomeDashboardChrome\nimport com.saney.ytmimporter.ui.TrackAdapter\nimport com.saney.ytmimporter.ui.WorkflowRelayOverlay\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main delegated-parent and relay fields",
        "old": "    private var returnToPlaylistHubAfterDelegatedAction =\n        false\n\n    private var returnToMenuAfterDelegatedAction =\n        false\n\n    private var workflowRelayActive =\n        false\n\n    private var workflowRelayTitle =\n        \"\"\n\n    private var workflowRelayMessage =\n        \"\"\n\n    private var workflowRelayOverlay:\n        View? = null\n\n    private var workflowRelayStatusText:\n        TextView? = null\n\n    private val uiPrefs by lazy {\n",
        "new": "    private var returnToPlaylistHubAfterDelegatedAction = false\n    private var returnToMenuAfterDelegatedAction = false\n    private lateinit var workflowRelay: WorkflowRelayOverlay\n\n    private val uiPrefs by lazy {\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main relay state restore",
        "old": "        returnToPlaylistHubAfterDelegatedAction =\n            savedInstanceState\n                ?.getBoolean(\n                    STATE_RETURN_TO_PLAYLIST_HUB,\n                    false\n                )\n                ?: false\n\n        returnToMenuAfterDelegatedAction =\n            savedInstanceState\n                ?.getBoolean(\n                    STATE_RETURN_TO_MENU,\n                    false\n                )\n                ?: false\n\n        workflowRelayActive =\n            savedInstanceState\n                ?.getBoolean(\n                    STATE_WORKFLOW_RELAY_ACTIVE,\n                    false\n                )\n                ?: false\n\n        workflowRelayTitle =\n            savedInstanceState\n                ?.getString(\n                    STATE_WORKFLOW_RELAY_TITLE\n                )\n                .orEmpty()\n\n        workflowRelayMessage =\n            savedInstanceState\n                ?.getString(\n                    STATE_WORKFLOW_RELAY_MESSAGE\n                )\n                .orEmpty()\n\n        buildUi()\n\n        if (workflowRelayActive) {\n            window.decorView.post {\n                if (!isFinishing && !isDestroyed) {\n                    showWorkflowRelayOverlay(\n                        title =\n                            workflowRelayTitle\n                                .ifBlank {\n                                    \"Виконую дію\"\n                                },\n                        message =\n                            workflowRelayMessage\n                                .ifBlank {\n                                    \"Переходимо до наступного екрана…\"\n                                }\n                    )\n                }\n            }\n        }\n\n        updateAccountPanel()\n",
        "new": "        returnToPlaylistHubAfterDelegatedAction =\n            savedInstanceState?.getBoolean(STATE_RETURN_TO_PLAYLIST_HUB, false) ?: false\n        returnToMenuAfterDelegatedAction =\n            savedInstanceState?.getBoolean(STATE_RETURN_TO_MENU, false) ?: false\n\n        buildUi()\n        workflowRelay = WorkflowRelayOverlay(this, savedInstanceState)\n\n        updateAccountPanel()\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main relay save state",
        "old": "        outState.putBoolean(\n            STATE_RETURN_TO_PLAYLIST_HUB,\n            returnToPlaylistHubAfterDelegatedAction\n        )\n        outState.putBoolean(\n            STATE_RETURN_TO_MENU,\n            returnToMenuAfterDelegatedAction\n        )\n        outState.putBoolean(\n            STATE_WORKFLOW_RELAY_ACTIVE,\n            workflowRelayActive\n        )\n        outState.putString(\n            STATE_WORKFLOW_RELAY_TITLE,\n            workflowRelayTitle\n        )\n        outState.putString(\n            STATE_WORKFLOW_RELAY_MESSAGE,\n            workflowRelayMessage\n        )\n        super.onSaveInstanceState(outState)\n",
        "new": "        outState.putBoolean(STATE_RETURN_TO_PLAYLIST_HUB, returnToPlaylistHubAfterDelegatedAction)\n        outState.putBoolean(STATE_RETURN_TO_MENU, returnToMenuAfterDelegatedAction)\n        workflowRelay.save(outState)\n        super.onSaveInstanceState(outState)\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main relay destroy detach",
        "old": "    override fun onDestroy() {\n        workflowRelayOverlay = null\n        workflowRelayStatusText = null\n        executor.shutdownNow()\n        super.onDestroy()\n    }\n",
        "new": "    override fun onDestroy() {\n        if (::workflowRelay.isInitialized) workflowRelay.detach()\n        executor.shutdownNow()\n        super.onDestroy()\n    }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main cancelled delegated result routing",
        "old": "        if (\n            resultCode != RESULT_OK ||\n            data == null\n        ) {\n            when {\n                returnToPlaylistHubAfterDelegatedAction &&\n                    requestCode in\n                        setOf(\n                            reviewScreenRequestCode,\n                            destinationScreenRequestCode\n                        ) -> {\n                    showWorkflowRelayOverlay(\n                        title =\n                            \"Поточний плейлист\",\n                        message =\n                            \"Повертаюся до плейлиста…\"\n                    )\n                    reopenPlaylistHubAfterDelegatedAction()\n                }\n\n                returnToMenuAfterDelegatedAction &&\n                    requestCode in\n                        setOf(\n                            reviewScreenRequestCode,\n                            destinationScreenRequestCode\n                        ) -> {\n                    showWorkflowRelayOverlay(\n                        title = \"Меню\",\n                        message =\n                            \"Повертаюся до меню…\"\n                    )\n                    reopenMenuAfterDelegatedAction()\n                }\n\n                requestCode ==\n                    playlistScreenRequestCode -> {\n                    returnToPlaylistHubAfterDelegatedAction =\n                        false\n                    hideWorkflowRelayOverlay()\n                }\n\n                requestCode ==\n                    menuScreenRequestCode -> {\n                    returnToMenuAfterDelegatedAction =\n                        false\n                    hideWorkflowRelayOverlay()\n                }\n\n                requestCode in\n                    setOf(\n                        reviewScreenRequestCode,\n                        destinationScreenRequestCode\n                    ) ->\n                    hideWorkflowRelayOverlay()\n            }\n\n            return\n        }\n",
        "new": "        if (resultCode != RESULT_OK || data == null) {\n            when {\n                returnToPlaylistHubAfterDelegatedAction &&\n                    requestCode in setOf(reviewScreenRequestCode, destinationScreenRequestCode) -> {\n                    showWorkflowRelayOverlay(\"Поточний плейлист\", \"Повертаюся до плейлиста…\")\n                    reopenPlaylistHubAfterDelegatedAction()\n                }\n                returnToMenuAfterDelegatedAction &&\n                    requestCode in setOf(reviewScreenRequestCode, destinationScreenRequestCode) -> {\n                    showWorkflowRelayOverlay(\"Меню\", \"Повертаюся до меню…\")\n                    reopenMenuAfterDelegatedAction()\n                }\n                requestCode == playlistScreenRequestCode -> {\n                    returnToPlaylistHubAfterDelegatedAction = false\n                    hideWorkflowRelayOverlay()\n                }\n                requestCode == menuScreenRequestCode -> {\n                    returnToMenuAfterDelegatedAction = false\n                    hideWorkflowRelayOverlay()\n                }\n                requestCode in setOf(reviewScreenRequestCode, destinationScreenRequestCode) ->\n                    hideWorkflowRelayOverlay()\n            }\n            return\n        }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main relay helper methods",
        "old": "    private fun showWorkflowRelayOverlay(\n        title: String,\n        message: String\n    ) {\n        workflowRelayActive = true\n        workflowRelayTitle = title\n        workflowRelayMessage = message\n\n        workflowRelayOverlay\n            ?.let { overlay ->\n                (overlay.parent as? ViewGroup)\n                    ?.removeView(overlay)\n            }\n\n        val palette =\n            AppThemeManager.palette(this)\n\n        val root =\n            LinearLayout(this).apply {\n                orientation =\n                    LinearLayout.VERTICAL\n                gravity =\n                    Gravity.CENTER\n                isClickable = true\n                isFocusable = true\n                setBackgroundColor(\n                    palette.background\n                )\n                setPadding(\n                    dp(28),\n                    dp(48),\n                    dp(28),\n                    dp(48)\n                )\n\n                addView(\n                    UiChrome.emphasizedTitle(\n                        activity =\n                            this@MainActivity,\n                        label = title,\n                        textSizeSp = 22f\n                    )\n                )\n\n                val relayStatus =\n                    TextView(\n                        this@MainActivity\n                    ).apply {\n                        text = message\n                        textSize = 14f\n                        gravity =\n                            Gravity.CENTER\n                        setTextColor(\n                            palette.muted\n                        )\n                        setPadding(\n                            0,\n                            dp(16),\n                            0,\n                            dp(18)\n                        )\n                    }\n\n                workflowRelayStatusText =\n                    relayStatus\n\n                addView(\n                    relayStatus,\n                    LinearLayout.LayoutParams(\n                        ViewGroup.LayoutParams.MATCH_PARENT,\n                        ViewGroup.LayoutParams.WRAP_CONTENT\n                    )\n                )\n\n                addView(\n                    ProgressBar(\n                        this@MainActivity\n                    ).apply {\n                        isIndeterminate = true\n                    },\n                    LinearLayout.LayoutParams(\n                        dp(48),\n                        dp(48)\n                    )\n                )\n            }\n\n        addContentView(\n            root,\n            ViewGroup.LayoutParams(\n                ViewGroup.LayoutParams.MATCH_PARENT,\n                ViewGroup.LayoutParams.MATCH_PARENT\n            )\n        )\n\n        workflowRelayOverlay = root\n    }\n\n    private fun hideWorkflowRelayOverlay() {\n        workflowRelayActive = false\n        workflowRelayTitle = \"\"\n        workflowRelayMessage = \"\"\n        workflowRelayStatusText = null\n\n        workflowRelayOverlay\n            ?.let { overlay ->\n                (overlay.parent as? ViewGroup)\n                    ?.removeView(overlay)\n            }\n\n        workflowRelayOverlay = null\n    }\n\n    private fun reopenPlaylistHubAfterDelegatedAction() {\n        if (\n            !returnToPlaylistHubAfterDelegatedAction\n        ) {\n            return\n        }\n\n        returnToPlaylistHubAfterDelegatedAction =\n            false\n\n        window.decorView.post {\n            if (!isFinishing && !isDestroyed) {\n                openPlaylistHub()\n            }\n        }\n    }\n\n    private fun reopenMenuAfterDelegatedAction() {\n        if (\n            !returnToMenuAfterDelegatedAction\n        ) {\n            return\n        }\n\n        returnToMenuAfterDelegatedAction =\n            false\n\n        window.decorView.post {\n            if (!isFinishing && !isDestroyed) {\n                openMenuScreen()\n            }\n        }\n    }\n\n    private fun reopenDelegatedParentAfterAction() {\n        when {\n            returnToPlaylistHubAfterDelegatedAction ->\n                reopenPlaylistHubAfterDelegatedAction()\n\n            returnToMenuAfterDelegatedAction ->\n                reopenMenuAfterDelegatedAction()\n\n            else ->\n                hideWorkflowRelayOverlay()\n        }\n    }\n",
        "new": "    private fun showWorkflowRelayOverlay(title: String, message: String) =\n        workflowRelay.show(title, message)\n\n    private fun hideWorkflowRelayOverlay() = workflowRelay.hide()\n\n    private fun beginPlaylistRelay(title: String, message: String) {\n        returnToMenuAfterDelegatedAction = false\n        returnToPlaylistHubAfterDelegatedAction = true\n        showWorkflowRelayOverlay(title, message)\n    }\n\n    private fun beginMenuRelay(title: String, message: String) {\n        returnToPlaylistHubAfterDelegatedAction = false\n        returnToMenuAfterDelegatedAction = true\n        showWorkflowRelayOverlay(title, message)\n    }\n\n    private fun reopenPlaylistHubAfterDelegatedAction() {\n        if (!returnToPlaylistHubAfterDelegatedAction) return\n        returnToPlaylistHubAfterDelegatedAction = false\n        window.decorView.post { if (!isFinishing && !isDestroyed) openPlaylistHub() }\n    }\n\n    private fun reopenMenuAfterDelegatedAction() {\n        if (!returnToMenuAfterDelegatedAction) return\n        returnToMenuAfterDelegatedAction = false\n        window.decorView.post { if (!isFinishing && !isDestroyed) openMenuScreen() }\n    }\n\n    private fun reopenDelegatedParentAfterAction() {\n        when {\n            returnToPlaylistHubAfterDelegatedAction -> reopenPlaylistHubAfterDelegatedAction()\n            returnToMenuAfterDelegatedAction -> reopenMenuAfterDelegatedAction()\n            else -> hideWorkflowRelayOverlay()\n        }\n    }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Playlist Hub delegated routing",
        "old": "            PlaylistActivity.ACTION_SEARCH -> {\n                returnToMenuAfterDelegatedAction =\n                    false\n                returnToPlaylistHubAfterDelegatedAction =\n                    true\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Поточний плейлист\",\n                    message =\n                        \"Відкриваю пошук / перевірку…\"\n                )\n                searchOrReview()\n            }\n\n            PlaylistActivity.ACTION_REPEAT_SEARCH -> {\n                returnToMenuAfterDelegatedAction =\n                    false\n                returnToPlaylistHubAfterDelegatedAction =\n                    true\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Поточний плейлист\",\n                    message =\n                        \"Повторюю пошук потрібних треків…\"\n                )\n                searchAll(\n                    openReviewAfter = true,\n                    preserveExistingExact = true\n                )\n            }\n\n            PlaylistActivity.ACTION_CREATE -> {\n                returnToMenuAfterDelegatedAction =\n                    false\n                returnToPlaylistHubAfterDelegatedAction =\n                    true\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Створити / додати\",\n                    message =\n                        \"Відкриваю вибір цільового плейлиста…\"\n                )\n                createPlaylist()\n            }\n",
        "new": "            PlaylistActivity.ACTION_SEARCH -> {\n                beginPlaylistRelay(\"Поточний плейлист\", \"Відкриваю пошук / перевірку…\")\n                searchOrReview()\n            }\n\n            PlaylistActivity.ACTION_REPEAT_SEARCH -> {\n                beginPlaylistRelay(\"Поточний плейлист\", \"Повторюю пошук потрібних треків…\")\n                searchAll(openReviewAfter = true, preserveExistingExact = true)\n            }\n\n            PlaylistActivity.ACTION_CREATE -> {\n                beginPlaylistRelay(\"Створити / додати\", \"Відкриваю вибір цільового плейлиста…\")\n                createPlaylist()\n            }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Playlist manual delegated routing",
        "old": "            PlaylistActivity.ACTION_MANUAL_VIDEO -> {\n                returnToMenuAfterDelegatedAction =\n                    false\n                returnToPlaylistHubAfterDelegatedAction =\n                    true\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Перевірка треків\",\n                    message =\n                        \"Отримую дані ручної заміни…\"\n                )\n                handleManualVideoResult(data)\n            }\n",
        "new": "            PlaylistActivity.ACTION_MANUAL_VIDEO -> {\n                beginPlaylistRelay(\"Перевірка треків\", \"Отримую дані ручної заміни…\")\n                handleManualVideoResult(data)\n            }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Menu Project delegated routing",
        "old": "            MenuActivity.ACTION_PROJECT -> {\n                returnToPlaylistHubAfterDelegatedAction =\n                    false\n                returnToMenuAfterDelegatedAction =\n                    true\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Поточний проєкт\",\n                    message =\n                        \"Відкриваю перевірку проєкту…\"\n                )\n                openReviewScreen()\n            }\n",
        "new": "            MenuActivity.ACTION_PROJECT -> {\n                beginMenuRelay(\"Поточний проєкт\", \"Відкриваю перевірку проєкту…\")\n                openReviewScreen()\n            }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Destination create relay overlay",
        "old": "            DestinationActivity.ACTION_CREATE_NEW -> {\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Створити / додати\",\n                    message =\n                        \"Створюю плейлист і готую запис…\"\n                )\n\n                val privacy =\n",
        "new": "            DestinationActivity.ACTION_CREATE_NEW -> {\n                showWorkflowRelayOverlay(\"Створити / додати\", \"Створюю плейлист і готую запис…\")\n                val privacy =\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Destination load-existing relay overlay",
        "old": "            DestinationActivity.ACTION_LOAD_EXISTING -> {\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Існуючий плейлист\",\n                    message =\n                        \"Завантажую ваші плейлисти…\"\n                )\n                loadExistingPlaylistsForDestination(\n",
        "new": "            DestinationActivity.ACTION_LOAD_EXISTING -> {\n                showWorkflowRelayOverlay(\"Існуючий плейлист\", \"Завантажую ваші плейлисти…\")\n                loadExistingPlaylistsForDestination(\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Destination back-start relay overlay",
        "old": "            DestinationActivity.ACTION_BACK_TO_START -> {\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Створити / додати\",\n                    message =\n                        \"Повертаюся до вибору способу…\"\n                )\n                openDestinationStart(\n",
        "new": "            DestinationActivity.ACTION_BACK_TO_START -> {\n                showWorkflowRelayOverlay(\"Створити / додати\", \"Повертаюся до вибору способу…\")\n                openDestinationStart(\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Destination back-list relay overlay",
        "old": "            DestinationActivity.ACTION_BACK_TO_EXISTING_LIST -> {\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Існуючий плейлист\",\n                    message =\n                        \"Повертаюся до списку плейлистів…\"\n                )\n\n                val cachedPlaylists =\n",
        "new": "            DestinationActivity.ACTION_BACK_TO_EXISTING_LIST -> {\n                showWorkflowRelayOverlay(\"Існуючий плейлист\", \"Повертаюся до списку плейлистів…\")\n                val cachedPlaylists =\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Destination selected target relay overlay",
        "old": "            DestinationActivity.ACTION_SELECT_EXISTING -> {\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Перевірка перед додаванням\",\n                    message =\n                        \"Перевіряю дублікати у вибраному плейлисті…\"\n                )\n\n                val id =\n",
        "new": "            DestinationActivity.ACTION_SELECT_EXISTING -> {\n                showWorkflowRelayOverlay(\"Перевірка перед додаванням\", \"Перевіряю дублікати у вибраному плейлисті…\")\n                val id =\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Destination confirm relay overlay",
        "old": "            DestinationActivity.ACTION_CONFIRM_EXISTING -> {\n                showWorkflowRelayOverlay(\n                    title =\n                        \"Створити / додати\",\n                    message =\n                        \"Готую додавання треків…\"\n                )\n                finishExistingDestination(\n",
        "new": "            DestinationActivity.ACTION_CONFIRM_EXISTING -> {\n                showWorkflowRelayOverlay(\"Створити / додати\", \"Готую додавання треків…\")\n                finishExistingDestination(\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Status mirrors workflow relay",
        "old": "    private fun status(message: String) {\n        statusText.text = message\n\n        if (workflowRelayActive) {\n            workflowRelayMessage =\n                message\n            workflowRelayStatusText\n                ?.text =\n                message\n        }\n    }\n",
        "new": "    private fun status(message: String) {\n        statusText.text = message\n        if (::workflowRelay.isInitialized) workflowRelay.update(message)\n    }\n"
    },
    {
        "file": "app/src/main/java/com/saney/ytmimporter/MainActivity.kt",
        "name": "Main relay state constants",
        "old": "        private const val STATE_RETURN_TO_PLAYLIST_HUB =\n            \"state_return_to_playlist_hub\"\n\n        private const val STATE_RETURN_TO_MENU =\n            \"state_return_to_menu\"\n\n        private const val STATE_WORKFLOW_RELAY_ACTIVE =\n            \"state_workflow_relay_active\"\n\n        private const val STATE_WORKFLOW_RELAY_TITLE =\n            \"state_workflow_relay_title\"\n\n        private const val STATE_WORKFLOW_RELAY_MESSAGE =\n            \"state_workflow_relay_message\"\n\n        private const val YOUTUBE_SCOPE =\n",
        "new": "        private const val STATE_RETURN_TO_PLAYLIST_HUB =\n            \"state_return_to_playlist_hub\"\n        private const val STATE_RETURN_TO_MENU = \"state_return_to_menu\"\n\n        private const val YOUTUBE_SCOPE =\n"
    }
]
NEW_FILES = {
    "app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt": 'package com.saney.ytmimporter.ui\n\nimport android.app.Activity\nimport android.os.Bundle\nimport android.view.Gravity\nimport android.view.View\nimport android.view.ViewGroup\nimport android.widget.LinearLayout\nimport android.widget.ProgressBar\nimport android.widget.TextView\nimport kotlin.math.roundToInt\n\nclass WorkflowRelayOverlay(\n    private val activity: Activity,\n    savedInstanceState: Bundle?\n) {\n    private var active = savedInstanceState?.getBoolean(KEY_ACTIVE, false) ?: false\n    private var title = savedInstanceState?.getString(KEY_TITLE).orEmpty()\n    private var message = savedInstanceState?.getString(KEY_MESSAGE).orEmpty()\n    private var overlay: View? = null\n    private var statusText: TextView? = null\n\n    init {\n        if (active) {\n            activity.window.decorView.post {\n                if (!activity.isFinishing && !activity.isDestroyed) {\n                    show(\n                        title.ifBlank { "Виконую дію" },\n                        message.ifBlank { "Переходимо до наступного екрана…" }\n                    )\n                }\n            }\n        }\n    }\n\n    fun save(outState: Bundle) {\n        outState.putBoolean(KEY_ACTIVE, active)\n        outState.putString(KEY_TITLE, title)\n        outState.putString(KEY_MESSAGE, message)\n    }\n\n    fun show(title: String, message: String) {\n        active = true\n        this.title = title\n        this.message = message\n        removeOverlay()\n\n        val palette = AppThemeManager.palette(activity)\n        val root = LinearLayout(activity).apply {\n            orientation = LinearLayout.VERTICAL\n            gravity = Gravity.CENTER\n            isClickable = true\n            isFocusable = true\n            setBackgroundColor(palette.background)\n            setPadding(dp(28), dp(48), dp(28), dp(48))\n        }\n\n        root.addView(\n            UiChrome.emphasizedTitle(\n                activity = activity,\n                label = title,\n                textSizeSp = 22f\n            )\n        )\n\n        val relayStatus = TextView(activity).apply {\n            text = message\n            textSize = 14f\n            gravity = Gravity.CENTER\n            setTextColor(palette.muted)\n            setPadding(0, dp(16), 0, dp(18))\n        }\n        statusText = relayStatus\n\n        root.addView(\n            relayStatus,\n            LinearLayout.LayoutParams(\n                ViewGroup.LayoutParams.MATCH_PARENT,\n                ViewGroup.LayoutParams.WRAP_CONTENT\n            )\n        )\n        root.addView(\n            ProgressBar(activity).apply { isIndeterminate = true },\n            LinearLayout.LayoutParams(dp(48), dp(48))\n        )\n\n        activity.addContentView(\n            root,\n            ViewGroup.LayoutParams(\n                ViewGroup.LayoutParams.MATCH_PARENT,\n                ViewGroup.LayoutParams.MATCH_PARENT\n            )\n        )\n        overlay = root\n    }\n\n    fun hide() {\n        active = false\n        title = ""\n        message = ""\n        removeOverlay()\n    }\n\n    fun update(message: String) {\n        if (!active) return\n        this.message = message\n        statusText?.text = message\n    }\n\n    fun detach() {\n        overlay = null\n        statusText = null\n    }\n\n    private fun removeOverlay() {\n        overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }\n        overlay = null\n        statusText = null\n    }\n\n    private fun dp(value: Int): Int =\n        (value * activity.resources.displayMetrics.density).roundToInt()\n\n    private companion object {\n        const val KEY_ACTIVE = "workflow_relay_active"\n        const val KEY_TITLE = "workflow_relay_title"\n        const val KEY_MESSAGE = "workflow_relay_message"\n    }\n}\n',
    "docs/v.1.4.47/qa/R3_CORRECTIVE_NAVIGATION_FIX2.md": '# v1.4.47-R3 corrective navigation FIX2\n\nThe first corrective navigation patch passed its dedicated audit but failed the historical\n`mainactivity-cleanup-audit.sh` structural guard because `MainActivity.kt` grew to 4312 lines.\n\nFIX2 does **not** weaken the `< 4000` guard. It extracts workflow-relay rendering/state into\n`ui/WorkflowRelayOverlay.kt` and compacts the MainActivity bridge calls. The navigation\ncontract from BUG-025 remains unchanged, while MainActivity returns below the historical\ncleanup ceiling.\n\nNo version bump. Re-run the corrective audit and complete release preflight before staging.\n',
}

def patch_one(op, do_apply):
    text = TARGET.read_text(encoding="utf-8")
    old, new = op["old"], op["new"]
    old_count, new_count = text.count(old), text.count(new)
    if new_count == 1:
        print(f"SKIP: already compacted: {op['name']}")
        return
    if old_count == 1 and new_count == 0:
        print(f"READY: {op['name']}")
        if do_apply:
            TARGET.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
            print(f"APPLIED: {op['name']}")
        return
    raise SystemExit(f"FAIL: FIX2 anchor mismatch for {op['name']}: old={old_count}, new={new_count}")

def ensure_file(rel, content, do_apply):
    path = ROOT / rel
    if path.exists():
        if path.read_text(encoding="utf-8") == content:
            print(f"SKIP: already present: {rel}")
            return
        raise SystemExit(f"FAIL: existing FIX2 file differs: {rel}")
    print(f"READY: create {rel}")
    if do_apply:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="\n")
        print(f"CREATED: {rel}")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if not TARGET.exists():
        raise SystemExit(f"FAIL: missing {TARGET}")
    for op in OPS:
        patch_one(op, not args.check)
    for rel, content in NEW_FILES.items():
        ensure_file(rel, content, not args.check)
    if not args.check:
        lines = len(TARGET.read_text(encoding="utf-8").splitlines())
        print(f"MainActivity lines after FIX2: {lines}")
        if lines >= 4000:
            raise SystemExit(f"FAIL: MainActivity remains >= 4000 lines: {lines}")
    print("PASS: corrective navigation FIX2 ready/already applied" if args.check else "PASS: corrective navigation FIX2 applied")

if __name__ == "__main__":
    main()
