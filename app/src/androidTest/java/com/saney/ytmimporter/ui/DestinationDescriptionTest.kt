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
import com.saney.ytmimporter.DestinationActivity
import com.saney.ytmimporter.library.PlaylistDescriptionPolicy
import com.saney.ytmimporter.storage.PlaylistDescriptionStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Batch 21: «Створити / додати» — signature, «Опис (необов'язково)», the strip
 * of descriptions (tap = active) and the «?» window. Nothing goes to YouTube.
 */
@RunWith(AndroidJUnit4::class)
class DestinationDescriptionTest {
    private fun idle() = InstrumentationRegistry.getInstrumentation().waitForIdleSync()

    private fun View.texts(): List<String> = buildList {
        if (this@texts is TextView) add(text.toString())
        if (this@texts is ViewGroup) for (i in 0 until childCount) addAll(getChildAt(i).texts())
    }

    private fun View.byDescription(value: String): View? {
        if (contentDescription?.toString() == value) return this
        if (this is ViewGroup) for (i in 0 until childCount) getChildAt(i).byDescription(value)?.let { return it }
        return null
    }

    private fun View.byTag(value: String): View? {
        if (tag == value) return this
        if (this is ViewGroup) for (i in 0 until childCount) getChildAt(i).byTag(value)?.let { return it }
        return null
    }

    @Test fun signatureStripAndHelp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = PlaylistDescriptionStore(context)
        val before = store.load()
        store.save(PlaylistDescriptionPolicy.add(
            PlaylistDescriptionPolicy.seeded(PlaylistDescriptionPolicy.State(), 1L), "Другий опис", 2L))
        try {
            ActivityScenario.launch<DestinationActivity>(
                Intent(context, DestinationActivity::class.java)
            ).use { scene ->
                idle()
                scene.onActivity { activity ->
                    val root = activity.window.decorView
                    val texts = root.texts()
                    assertTrue("signature missing", PlaylistDescriptionPolicy.SIGNATURE in texts)
                    assertTrue("tick missing", "Опис (необов'язково)" in texts)
                    assertTrue("folded preview missing", texts.any { it.startsWith("Зараз: «Другий опис") })
                    root.byDescription("Опис плейліста — показати мої описи")!!.performClick()
                }
                idle()
                scene.onActivity { activity ->
                    val root = activity.window.decorView
                    val first = root.byTag("description:d1") ?: error("strip has no d1")
                    assertTrue(root.byTag("description:d2") != null)
                    first.performClick()
                }
                idle()
                assertEquals("d1", store.load().activeId)
                scene.onActivity { activity ->
                    activity.window.decorView.byDescription("Довідка «Створити / додати»")!!.performClick()
                }
                idle()
                // The «?» window survives a rotation (restorable modal).
                scene.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
                idle()
                Thread.sleep(1500)
                idle()
                scene.onActivity { activity ->
                    assertTrue(activity.isChangingConfigurations.not())
                }
            }
        } finally {
            store.save(before)
        }
    }
}
