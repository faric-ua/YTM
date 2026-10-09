package com.saney.ytmimporter.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saney.ytmimporter.PlaylistLibraryActivity
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Real library Activity; local fixture only, no remote writes or credentials. */
@RunWith(AndroidJUnit4::class)
class PlaylistLibraryActivityTest {
    @Test fun realLibraryShowsLocalPlaylistAndKeepsItAfterRecreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = "ui-library-" + UUID.randomUUID()
        val name = "Контрольна бібліотека ${id.takeLast(6)}"
        CurrentPlaylistStore(context).save(
            playlist = ImportedPlaylist(name, mutableListOf(
                Track("Перша пісня", "Виконавець", selectedVideoId = "abcdefghijk")
            )),
            sourceLabel = "Автотест локального каталогу",
            localPlaylistId = id
        )

        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText(name))
                assertTrue(activity.window.decorView.hasText("На телефоні"))
                assertTrue(activity.window.decorView.hasText("YouTube"))
                assertNotNull(RestorablePlaylistStore(activity).get(id))
            }
            scene.recreate()
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText(name))
            }
        }
    }

    @Test fun localCatalogueOpensRealTrackDetailsAfterRecreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "Бібліотека — деталі ${UUID.randomUUID()}"
        CurrentPlaylistStore(context).save(
            playlist = ImportedPlaylist(name, mutableListOf(
                Track("Тестова пісня", "Гурт")
            )),
            sourceLabel = "Локальний тест",
            localPlaylistId = "details-${UUID.randomUUID()}"
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val label = activity.window.decorView.findLabel(name)
                    ?: error("Playlist card missing")
                var target: View? = label
                while (target != null && !target.isClickable) {
                    target = target.parent as? View
                }
                assertNotNull("Playlist card not clickable", target)
                target!!.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Тестова пісня"))
                assertTrue(activity.window.decorView.hasText("Відкрити поточний плейліст"))
            }
            scene.recreate()
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Тестова пісня"))
                assertTrue(activity.window.decorView.hasText(name))
            }
        }
    }

    @Test fun switchingLocalIdDoesNotInheritPreviousHistoryIdentity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = CurrentPlaylistStore(context)
        store.save(ImportedPlaylist("One", mutableListOf()),
            "test", localPlaylistId = "old-${UUID.randomUUID()}",
            sourceHistoryId = "old-history")
        store.save(ImportedPlaylist("Two", mutableListOf()),
            "test", localPlaylistId = "new-${UUID.randomUUID()}")
        assertEquals(null, store.load()?.sourceHistoryId)
    }

    @Test fun onlineTabWithoutConsentOrTokenDoesNotStartRemoteRead() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val onlineButton = activity.window.decorView
                    .collectButtons().first { it.text.toString() == "YouTube" }
                onlineButton.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("тільки читання"))
                // Missing account: no automatic Google authorization prompt.
                assertTrue(activity.window.decorView.collectButtons()
                    .none { it.text.contains("Створити") })
            }
        }
    }

    private fun View.findLabel(target: String): TextView? {
        if (this is TextView && text?.toString() == target) return this
        if (this is ViewGroup) for (i in 0 until childCount) {
            val found = getChildAt(i).findLabel(target)
            if (found != null) return found
        }
        return null
    }

    private fun View.hasText(target: String): Boolean {
        if (this is TextView && text?.contains(target) == true) return true
        if (this is ViewGroup) for (i in 0 until childCount)
            if (getChildAt(i).hasText(target)) return true
        return false
    }

    private fun View.collectButtons(): List<Button> = buildList {
        if (this@collectButtons is Button) add(this@collectButtons)
        if (this@collectButtons is ViewGroup)
            for (i in 0 until this@collectButtons.childCount)
                addAll(this@collectButtons.getChildAt(i).collectButtons())
    }

    private fun idle() =
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
}
