package com.saney.ytmimporter.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saney.ytmimporter.YouTubePlayerActivity
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

/** Offline/credential-free Activity smoke: invalid ID must never load YouTube. */
@RunWith(AndroidJUnit4::class)
class YouTubePlayerActivityTest {
    @Test fun rejectsInvalidVideoIdWithoutOpeningNetworkPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, YouTubePlayerActivity::class.java)
            .putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, "bad-id")
            .putExtra(YouTubePlayerActivity.EXTRA_TITLE, "Контроль відтворення")
        ActivityScenario.launch<YouTubePlayerActivity>(intent).use { scene ->
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Контроль відтворення"))
                assertTrue(activity.window.decorView.hasText("Немає коректного YouTube videoId"))
            }
            scene.recreate()
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Немає коректного YouTube videoId"))
            }
        }
    }

    @Test fun orientationChangeKeepsSameActivityAndPlayerShell() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, YouTubePlayerActivity::class.java)
            .putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, "invalid")
            .putExtra(YouTubePlayerActivity.EXTRA_TITLE, "Поворот без втрати стану")
        ActivityScenario.launch<YouTubePlayerActivity>(intent).use { scene ->
            var originalActivity: YouTubePlayerActivity? = null
            scene.onActivity { activity ->
                originalActivity = activity
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            idle()
            scene.onActivity { activity ->
                assertSame("Landscape orientation recreated the player", originalActivity, activity)
                assertTrue(activity.window.decorView.hasText("Поворот без втрати стану"))
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            idle()
            scene.onActivity { activity ->
                assertSame("Portrait orientation recreated the player", originalActivity, activity)
                assertTrue(activity.window.decorView.hasText("Немає коректного YouTube videoId"))
            }
        }
    }

    private fun View.hasText(value: String): Boolean {
        if (this is TextView && text?.contains(value) == true) return true
        if (this is ViewGroup) for (i in 0 until childCount) {
            if (getChildAt(i).hasText(value)) return true
        }
        return false
    }

    private fun idle() = InstrumentationRegistry.getInstrumentation().waitForIdleSync()
}
