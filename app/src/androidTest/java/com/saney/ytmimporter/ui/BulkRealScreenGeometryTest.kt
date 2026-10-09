package com.saney.ytmimporter.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saney.ytmimporter.BulkSyncPreviewActivity
import com.saney.ytmimporter.BulkSyncSessionActivity
import com.saney.ytmimporter.bulk.BulkSyncPlanRow
import com.saney.ytmimporter.bulk.BulkSyncPlanState
import com.saney.ytmimporter.bulk.BulkSyncPlanSummary
import com.saney.ytmimporter.bulk.BulkSyncRemoteBaseline
import com.saney.ytmimporter.bulk.BulkSyncSession
import com.saney.ytmimporter.bulk.BulkSyncSessionRow
import com.saney.ytmimporter.bulk.BulkSyncSessionRowState
import com.saney.ytmimporter.bulk.BulkSyncSessionState
import com.saney.ytmimporter.storage.BulkSyncSessionStore
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests REAL production Bulk Activity view trees, not only UiChrome test host.
 *
 * Preview uses a DEBUG-only inert Serializable plan, not loadPreview().
 * Session uses a synthetic, local READY record in the isolated emulator.
 * No buttons are clicked; no Google credentials, network or YTM mutations.
 */
@RunWith(AndroidJUnit4::class)
class BulkRealScreenGeometryTest {
    @Test
    fun actualPreview_showsBothQuickSelectionLabelsInItsNestedCard() {
        previewScenario().use { scenario ->
            idle()
            scenario.onActivity { activity ->
                val actions = actionButtons(
                    activity, "Усі готові", "Лише доповнити"
                )
                assertActionsReadable(activity, actions)
                assertTrue("No plan rows rendered", findButtons(activity).size >= 4)
            }
        }
    }

    @Test
    fun actualPreview_recreationPreservesReadOnlyPlanAndActions() {
        previewScenario().use { scenario ->
            idle()
            scenario.recreate()
            idle()
            scenario.onActivity { activity ->
                assertActionsReadable(
                    activity,
                    actionButtons(activity, "Усі готові", "Лише доповнити")
                )
                assertTrue("Read-only plan lost", activity.window.decorView.findText(
                    "План готовий"
                ))
            }
        }
    }

    @Test
    fun actualSession_readyActionsStayFullyVisibleAboveLongPlan() {
        val sessionId = makeReadySession()
        sessionScenario(sessionId).use { scenario ->
            idle()
            scenario.onActivity { activity ->
                assertActionsReadable(
                    activity,
                    actionButtons(activity, "Почати синхронізацію", "Закрити")
                )
                assertTrue(
                    "Lost the create batch controls",
                    activity.window.decorView.findText("Змінити розмір пакета")
                )
                assertEquals(
                    "Fixture session unexpectedly mutated",
                    0,
                    BulkSyncSessionStore(activity).get(sessionId)
                        ?.mutationLedger?.size
                )
            }
        }
    }

    @Test
    fun actualSession_recreationRetainsFooterWithoutAnyWrite() {
        val sessionId = makeReadySession()
        sessionScenario(sessionId).use { scenario ->
            idle()
            scenario.recreate()
            idle()
            scenario.onActivity { activity ->
                assertActionsReadable(
                    activity,
                    actionButtons(activity, "Почати синхронізацію", "Закрити")
                )
                val stored = BulkSyncSessionStore(activity).get(sessionId)
                assertEquals(BulkSyncSessionState.READY, stored?.state)
                assertEquals(0, stored?.mutationLedger?.size)
            }
        }
    }

    private fun previewScenario(): ActivityScenario<BulkSyncPreviewActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val plan = BulkSyncPlanSummary(
            rows = listOf(
                previewRow("preview-new", "Новий тестовий плейліст", BulkSyncPlanState.NEW),
                previewRow("preview-linked", "Пов'язаний тестовий плейліст", BulkSyncPlanState.LINKED)
            ),
            estimatedSearchCalls = 0,
            estimatedWriteUnits = 300
        )
        val intent = Intent(context, BulkSyncPreviewActivity::class.java)
            .putExtra(BulkSyncPreviewActivity.EXTRA_DEBUG_READ_ONLY_PLAN, plan)
        return ActivityScenario.launch(intent)
    }

    private fun previewRow(
        id: String, name: String, state: BulkSyncPlanState
    ) = BulkSyncPlanRow(
        localPlaylistId = id,
        playlistName = name,
        state = state,
        trackCount = 3,
        selectedCount = 3,
        unresolvedCount = 0,
        estimatedSearchCalls = 0,
        cacheHits = 0,
        destinationPlaylistId = if (state == BulkSyncPlanState.LINKED) "remote-test" else null,
        plannedCreate = state == BulkSyncPlanState.NEW,
        plannedInsertCount = 3,
        estimatedWriteUnits = 150,
        reason = "Локальний синтетичний план без зовнішнього запиту"
    )

    private fun makeReadySession(): String {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = "ui-geometry-" + UUID.randomUUID()
        val now = System.currentTimeMillis()
        val rows = (0 until 12).map { i ->
            BulkSyncSessionRow(
                localPlaylistId = "$id-$i",
                playlistName = "Тестовий плейліст $i — довга назва для перевірки скролу",
                sourceLabel = "Локальний тест, без YouTube",
                originalPlanState = if (i == 0) BulkSyncPlanState.NEW else BulkSyncPlanState.LINKED,
                state = if (i == 0) BulkSyncSessionRowState.READY
                        else BulkSyncSessionRowState.READY_APPEND,
                remotePlaylistId = if (i == 0) null else "remote-test-$i"
            )
        }
        BulkSyncSessionStore(context).upsert(
            BulkSyncSession(
                sessionId = id,
                createdAt = now,
                updatedAt = now,
                state = BulkSyncSessionState.READY,
                googleEmail = null,
                youtubeChannelId = null,
                youtubeChannelTitle = null,
                checkpointId = "ui-test-only-checkpoint",
                remoteBaseline = BulkSyncRemoteBaseline(
                    capturedAt = now,
                    googleEmail = null,
                    youtubeChannelId = null,
                    youtubeChannelTitle = null,
                    playlists = emptyList()
                ),
                plan = rows,
                currentPlanIndex = 0,
                mutationLedger = emptyList(),
                maxCreatesPerRun = 3
            ),
            makeActive = false
        )
        return id
    }

    private fun sessionScenario(id: String): ActivityScenario<BulkSyncSessionActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ActivityScenario.launch(
            Intent(context, BulkSyncSessionActivity::class.java)
                .putExtra(BulkSyncSessionActivity.EXTRA_SESSION_ID, id)
        )
    }

    private fun actionButtons(
        activity: Activity, first: String, second: String
    ): List<Button> {
        val buttons = findButtons(activity)
        return listOf(first, second).map { name ->
            buttons.firstOrNull { it.text.toString() == name }
                ?: error("Actual Activity is missing action: $name")
        }
    }

    private fun findButtons(activity: Activity) =
        activity.window.decorView.collectButtons()

    private fun View.collectButtons(): List<Button> =
        buildList {
            if (this@collectButtons is Button) add(this@collectButtons)
            if (this@collectButtons is ViewGroup) {
                for (i in 0 until this@collectButtons.childCount) {
                    addAll(this@collectButtons.getChildAt(i).collectButtons())
                }
            }
        }

    private fun View.findText(value: String): Boolean {
        if (this is android.widget.TextView && text.contains(value)) return true
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                if (getChildAt(i).findText(value)) return true
            }
        }
        return false
    }

    private fun assertActionsReadable(activity: Activity, actions: List<Button>) {
        val root = Rect()
        assertTrue(activity.window.decorView.getGlobalVisibleRect(root))
        for (button in actions) {
            val visible = Rect()
            assertTrue("Button hidden: ${button.text}", button.getGlobalVisibleRect(visible))
            assertTrue("Button outside viewport: ${button.text}", root.contains(visible))
            assertTrue("Clipped horizontally: ${button.text}", visible.width() >= button.width - 2)
            assertTrue("Clipped vertically: ${button.text}", visible.height() >= button.height - 2)
            val layout = button.layout
            assertTrue("No text layout: ${button.text}", layout != null)
            val last = layout.lineCount - 1
            assertEquals(
                "Truncated label: ${button.text}",
                button.text.length,
                layout.getLineEnd(last)
            )
            val available = button.width -
                button.compoundPaddingLeft - button.compoundPaddingRight
            for (line in 0 until layout.lineCount) {
                assertEquals("Ellipsized: ${button.text}", 0, layout.getEllipsisCount(line))
                assertTrue("Label exceeds button: ${button.text}",
                    layout.getLineWidth(line) <= available + 2f)
            }
        }
    }

    private fun idle() =
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
}
