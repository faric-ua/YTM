package com.saney.ytmimporter.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ImageButton
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
                assertTrue(activity.window.decorView.hasText("Всі •"))
                assertTrue(activity.window.decorView.collectActions()
                    .any { it.contentDescription == "На телефоні" && it is ImageButton })
                assertTrue(activity.window.decorView.collectActions()
                    .any { it.contentDescription == "YouTube" && it is ImageButton })
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

    @Test fun catalogueCountsAreCenteredAndLinkedYtmUsesThemedIcon() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "Плейліст зі зв'язком YTM ${UUID.randomUUID()}"
        CurrentPlaylistStore(context).save(
            ImportedPlaylist(name, mutableListOf(
                Track("Пісня", "Виконавець", selectedVideoId = "abcdefghijk")
            )),
            "Перевірка іконок",
            destinationPlaylistId = "PLfixture123",
            localPlaylistId = "link-${UUID.randomUUID()}"
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val root = activity.window.decorView
                assertTrue(root.hasText(name))
                assertTrue(root.hasImageDescription("Дані плейліста збережені на телефоні"))
                assertTrue(root.hasImageDescription("Плейліст пов’язаний з YouTube Music"))
                val counter = root.findImageDescription("На телефоні")
                    ?: error("Counter's phone icon missing")
                val counts = counter.parent as? LinearLayout
                    ?: error("Counter group missing")
                val params = counts.layoutParams as? FrameLayout.LayoutParams
                    ?: error("Counts are not in a centered frame")
                assertEquals(Gravity.CENTER, params.gravity)
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

    @Test fun localTrackRowHasPreviewAndPlaybackAction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "Бібліотека прев’ю ${UUID.randomUUID()}"
        CurrentPlaylistStore(context).save(
            playlist = ImportedPlaylist(name, mutableListOf(
                Track("Трек з прев’ю", "Виконавець", selectedVideoId = "abcdefghijk")
            )),
            sourceLabel = "Локальна перевірка прев’ю",
            localPlaylistId = "preview-${UUID.randomUUID()}"
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val label = activity.window.decorView.findLabel(name)
                    ?: error("Playlist card missing")
                var card: View? = label
                while (card != null && !card.isClickable) {
                    card = card.parent as? View
                }
                assertNotNull(card)
                card!!.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Трек з прев’ю"))
                assertTrue(activity.window.decorView.hasPreview("Прев’ю: Трек з прев’ю"))
                assertTrue(activity.window.decorView.collectButtons()
                    .any { it.contentDescription == "Відтворити Трек з прев’ю у застосунку" })
            }
        }
    }

    @Test fun landscapeLibraryUsesCompactHeaderAndSearchOnDemand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        CurrentPlaylistStore(context).save(
            ImportedPlaylist("Контроль повороту", mutableListOf()),
            "Локальна перевірка",
            localPlaylistId = "landscape-${UUID.randomUUID()}"
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            scene.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            idle()
            scene.onActivity { activity ->
                val controls = activity.window.decorView.collectActions()
                for (name in listOf(
                    "На телефоні", "YouTube", "Знайти плейліст",
                    "Відкрити поточний плейліст", "Список одним стовпчиком",
                    "Довідка бібліотеки плейлістів"
                )) {
                    assertTrue("Missing vector icon: $name", controls.any {
                        it is ImageButton && it.contentDescription == name &&
                            it.drawable != null && it.isShown
                    })
                }
                assertTrue(controls.none {
                    it is Button && it.text.toString() == "Поточний плейліст →" &&
                        it.isShown
                })
                controls.first {
                    it.contentDescription == "Знайти плейліст"
                }.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasVisibleSearch())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            idle()
        }
    }

    @Test fun portraitSourceIconsToggleAllAndPhoneAndKeepStateAcrossRecreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "Фільтр телефону ${UUID.randomUUID()}"
        CurrentPlaylistStore(context).save(
            ImportedPlaylist(name, mutableListOf(Track("Пісня", "Виконавець"))),
            "Перевірка фільтрів", localPlaylistId = "filter-${UUID.randomUUID()}"
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Всі •"))
                assertTrue(activity.window.decorView.hasText(name))
                val phone = activity.window.decorView.collectActions()
                    .first { it is ImageButton && it.contentDescription == "На телефоні" }
                phone.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("На телефоні •"))
                assertTrue(activity.window.decorView.hasText(name))
                val phone = activity.window.decorView.collectActions()
                    .first { it is ImageButton && it.contentDescription == "На телефоні" }
                assertTrue(phone.isSelected)
                phone.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Всі •"))
                assertTrue(activity.window.decorView.collectActions()
                    .none { it is Button && it.isShown &&
                        it.text.toString() == "Поточний плейліст →" })
            }
            scene.recreate()
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Всі •"))
                assertTrue(activity.window.decorView.hasText(name))
                val help = activity.window.decorView.collectActions()
                    .first { it.contentDescription == "Довідка бібліотеки плейлістів" }
                assertTrue(help.isShown)
                help.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Всі •"))
                // Help opens a descriptive modal, never starts any playlist flow.
            }
        }
    }

    @Test fun onlineTabWithoutConsentOrTokenDoesNotStartRemoteRead() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val onlineButton = activity.window.decorView
                    .collectActions().first {
                        it is ImageButton && it.contentDescription == "YouTube"
                    }
                onlineButton.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("YouTube не підключено"))
                // Missing account: no automatic Google authorization prompt.
                assertTrue(activity.window.decorView.collectButtons()
                    .none { it.text.contains("Створити") })
            }
        }
    }

    private fun View.findImageDescription(target: String): ImageView? {
        if (this is ImageView && this !is ImageButton && contentDescription == target) return this
        if (this is ViewGroup) for (i in 0 until childCount) {
            val result = getChildAt(i).findImageDescription(target)
            if (result != null) return result
        }
        return null
    }

    private fun View.hasImageDescription(target: String): Boolean =
        findImageDescription(target) != null

    private fun View.findLabel(target: String): TextView? {
        if (this is TextView && text?.toString() == target) return this
        if (this is ViewGroup) for (i in 0 until childCount) {
            val found = getChildAt(i).findLabel(target)
            if (found != null) return found
        }
        return null
    }

    private fun View.hasVisibleSearch(): Boolean {
        if (this is android.widget.EditText &&
            hint?.toString() == "Знайти плейліст" && isShown) return true
        if (this is ViewGroup) for (i in 0 until childCount) {
            if (getChildAt(i).hasVisibleSearch()) return true
        }
        return false
    }

    private fun View.hasPreview(target: String): Boolean {
        if (this is android.widget.ImageView && contentDescription == target) return true
        if (this is ViewGroup) for (i in 0 until childCount) {
            if (getChildAt(i).hasPreview(target)) return true
        }
        return false
    }

    private fun View.hasText(target: String): Boolean {
        if (this is TextView && text?.contains(target) == true) return true
        if (this is ViewGroup) for (i in 0 until childCount)
            if (getChildAt(i).hasText(target)) return true
        return false
    }

    private fun View.collectActions(): List<View> = buildList {
        if (this@collectActions is Button || this@collectActions is ImageButton)
            add(this@collectActions)
        if (this@collectActions is ViewGroup)
            for (i in 0 until this@collectActions.childCount)
                addAll(this@collectActions.getChildAt(i).collectActions())
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
