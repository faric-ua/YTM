package com.saney.ytmimporter.tour

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saney.ytmimporter.ImportActivity
import com.saney.ytmimporter.PlaylistLibraryActivity
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Diagnostic only (faric 2026-11-05 23:38 / 11-06 03:58: help windows should
 * take the whole available area). Screens of the Import «?» window in both
 * orientations; runs only with screenTour=1 (workflow «UI screens»).
 */
@RunWith(AndroidJUnit4::class)
class DialogTourTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    private fun sh(cmd: String) {
        val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(cmd)
        android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.readBytes() }
    }

    private fun shot(name: String) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(3000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        sh("screencap -p /data/local/tmp/tour/$name.png")
        Log.i("ScreenTour", "shot $name")
    }

    private fun View.find(desc: String): View? {
        if (contentDescription?.toString() == desc) return this
        if (this is ViewGroup) for (i in 0 until childCount) getChildAt(i).find(desc)?.let { return it }
        return null
    }

    @Test fun importHelpBothOrientations() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("screenTour") == "1")
        sh("mkdir -p /data/local/tmp/tour")
        ActivityScenario.launch<ImportActivity>(Intent(context, ImportActivity::class.java)).use { scene ->
            shot("01_import_portrait")
            scene.onActivity { it.window.decorView.find("Довідка імпорту")?.performClick() }
            shot("02_import_help_portrait")
            scene.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            SystemClock.sleep(2500)
            shot("03_import_help_landscape")
        }
    }

    @Test fun libraryHelpBothOrientations() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("screenTour") == "1")
        sh("mkdir -p /data/local/tmp/tour")
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)).use { scene ->
            scene.onActivity { it.window.decorView.find("Довідка бібліотеки плейлістів")?.performClick() }
            shot("04_library_help_portrait")
            scene.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            SystemClock.sleep(2500)
            shot("05_library_help_landscape")
        }
    }
}
