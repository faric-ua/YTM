package com.saney.ytmimporter.ui

import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Actual on-device Android View measurement/painting assertions.
 *
 * These tests never open Bulk Sync, read an account or contact YouTube:
 * they render the exact shared UiChrome renderer inside isolated synthetic
 * Preview and Session containers.
 */
@RunWith(AndroidJUnit4::class)
class AdaptiveActionsGeometryTest {
    @Test
    fun previewQuickActions_showCompleteCaptionsInsidePaddedCard() {
        ActivityScenario.launch(UiGeometryTestActivity::class.java).use { scene ->
            scene.onActivity {
                it.showPreviewCard(widthDp = 320, fontSp = 15f)
            }
            awaitLayout()
            scene.onActivity { activity ->
                assertEquals(LinearLayout.VERTICAL, activity.actionPanel.orientation)
                assertEquals("Усі готові", activity.actions[0].text.toString())
                assertEquals("Лише доповнити", activity.actions[1].text.toString())
                assertButtonsReadableAndVisible(activity)
                assertEquals(0, activity.actionsInvoked)
            }
        }
    }

    @Test
    fun narrowContainerOverridesOptimisticScreenWidthEstimate() {
        ActivityScenario.launch(UiGeometryTestActivity::class.java).use { scene ->
            scene.onActivity {
                it.showPreviewCard(
                    widthDp = 320,
                    fontSp = 19f,
                    firstCaption = "Усі готові",
                    secondCaption = "Лише доповнити"
                )
            }
            awaitLayout()
            scene.onActivity { activity ->
                assertEquals(LinearLayout.VERTICAL, activity.actionPanel.orientation)
                assertButtonsReadableAndVisible(activity)
            }
        }
    }

    @Test
    fun equalWidthButtonsCanShareRowWhenBothLabelsActuallyFit() {
        ActivityScenario.launch(UiGeometryTestActivity::class.java).use { scene ->
            scene.onActivity {
                it.showPreviewCard(
                    widthDp = 340,
                    fontSp = 15f,
                    firstCaption = "Усі",
                    secondCaption = "Лише",
                    minButtonWidthDp = 110
                )
            }
            awaitLayout()
            scene.onActivity { activity ->
                assertEquals(LinearLayout.HORIZONTAL, activity.actionPanel.orientation)
                assertButtonsReadableAndVisible(activity)
            }
        }
    }

    @Test
    fun sessionFooter_staysVisibleAndKeepsCompleteLongActionAtLargeTextSize() {
        ActivityScenario.launch(UiGeometryTestActivity::class.java).use { scene ->
            scene.onActivity {
                it.showSessionFooter(widthDp = 360, fontSp = 22f)
            }
            awaitLayout()
            scene.onActivity { activity ->
                assertEquals(LinearLayout.VERTICAL, activity.actionPanel.orientation)
                assertEquals(
                    "Почати синхронізацію",
                    activity.actions.first().text.toString()
                )
                assertButtonsReadableAndVisible(activity)
                val rootVisible = Rect()
                val footerVisible = Rect()
                assertTrue(activity.rootPanel.getGlobalVisibleRect(rootVisible))
                assertTrue(activity.actionPanel.getGlobalVisibleRect(footerVisible))
                assertTrue("Footer extends beyond root bounds", rootVisible.contains(footerVisible))
                assertTrue("Scrollable body should have measurable height", activity.contentScroll.height > 0)
                assertEquals(0, activity.actionsInvoked)
            }
        }
    }

    @Test
    fun footerRemainsFixedWhenLongSessionContentScrolls() {
        ActivityScenario.launch(UiGeometryTestActivity::class.java).use { scene ->
            var topBefore = 0
            scene.onActivity { activity ->
                activity.showSessionFooter(widthDp = 320, fontSp = 15f)
            }
            awaitLayout()
            scene.onActivity { activity ->
                val rect = Rect()
                activity.actionPanel.getGlobalVisibleRect(rect)
                topBefore = rect.top
                activity.contentScroll.fullScroll(View.FOCUS_DOWN)
            }
            awaitLayout()
            scene.onActivity { activity ->
                val rect = Rect()
                activity.actionPanel.getGlobalVisibleRect(rect)
                assertEquals("Footer moved with content scroll", topBefore, rect.top)
                assertButtonsReadableAndVisible(activity)
                assertEquals(0, activity.actionsInvoked)
            }
        }
    }

    @Test
    fun rotationRebuildsSafeActionLayoutWithoutClickingAnything() {
        ActivityScenario.launch(UiGeometryTestActivity::class.java).use { scene ->
            scene.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            awaitLayout()
            scene.onActivity { activity ->
                assertButtonsReadableAndVisible(activity)
                assertEquals(0, activity.actionsInvoked)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            awaitLayout()
            scene.onActivity { activity ->
                assertButtonsReadableAndVisible(activity)
                assertEquals(0, activity.actionsInvoked)
            }
        }
    }

    private fun awaitLayout() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun assertButtonsReadableAndVisible(activity: UiGeometryTestActivity) {
        val parentRect = Rect()
        assertTrue(activity.rootPanel.getGlobalVisibleRect(parentRect))

        activity.actions.forEach { button ->
            assertTrue("Missing action: ${button.text}", button.isShown)
            assertTrue("Action not measured: ${button.text}", button.width > 0 && button.height > 0)

            val rect = Rect()
            assertTrue("Invisible action: ${button.text}", button.getGlobalVisibleRect(rect))
            assertTrue("Action outside safe root: ${button.text}", parentRect.contains(rect))
            assertTrue("Horizontally clipped: ${button.text}", rect.width() >= button.width - 2)
            assertTrue("Vertically clipped: ${button.text}", rect.height() >= button.height - 2)

            assertFullCaption(button)
        }
    }

    private fun assertFullCaption(button: Button) {
        val layout = button.layout
        assertTrue("No text layout for ${button.text}", layout != null)
        val lastLine = layout.lineCount - 1
        assertTrue("Missing line for ${button.text}", lastLine >= 0)
        assertEquals(
            "Caption truncated: ${button.text}",
            button.text.length,
            layout.getLineEnd(lastLine)
        )
        val availableTextPx = button.width -
            button.compoundPaddingLeft - button.compoundPaddingRight
        for (i in 0 until layout.lineCount) {
            assertEquals("Ellipsized: ${button.text}", 0, layout.getEllipsisCount(i))
            assertTrue(
                "Caption rendered wider than action: ${button.text}",
                layout.getLineWidth(i) <= availableTextPx + 2f
            )
        }
    }
}
