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
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.RestorablePlaylistStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Real library Activity; local fixture only, no remote writes or credentials. */
@RunWith(AndroidJUnit4::class)
class PlaylistLibraryActivityTest {
    @Test fun exactIdRemovePreservesSameTitlePeerAndGuardsCurrent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val current = CurrentPlaylistStore(context)
        val saved = RestorablePlaylistStore(context)
        val first = "del-first-${UUID.randomUUID()}"
        val second = "del-second-${UUID.randomUUID()}"
        val title = "Same title different identities"
        current.save(ImportedPlaylist(title, mutableListOf()), "Test",
            localPlaylistId = first)
        current.save(ImportedPlaylist(title, mutableListOf()), "Test",
            localPlaylistId = second)
        assertFalse(current.clearIfMatches(first))
        assertTrue(saved.remove(first))
        assertNull(saved.get(first))
        assertNotNull(saved.get(second))
        assertEquals(second, current.load()?.localPlaylistId)
        assertTrue(current.clearIfMatches(second))
        assertNull(current.load())
        assertTrue(saved.remove(second))
        assertFalse(saved.remove(second))
    }

    @Test fun onlyLocalDetailOffersDeleteButton() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = "Local delete QA ${UUID.randomUUID()}"
        val localId = "delete-ui-${UUID.randomUUID()}"
        CurrentPlaylistStore(context).save(
            ImportedPlaylist(title, mutableListOf(Track("Song", "Artist"))),
            "Local UI test", localPlaylistId = localId
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val root = activity.window.decorView
                assertFalse(root.collectButtons().any {
                    it.text.toString() == "Видалити локальну копію"
                })
                val label = root.findLabel(title) ?: error("Local card not found")
                var card: View? = label
                while (card != null && !card.isClickable) card = card.parent as? View
                assertNotNull(card)
                card!!.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.collectButtons().any {
                    it.text.toString() == "Видалити локальну копію"
                })
                assertNotNull(RestorablePlaylistStore(activity).get(localId))
            }
        }
    }

    /** Batch 14: «Видалити і копію, і архів…» removes both only after the second confirmation. */
    @android.annotation.TargetApi(29)
    @Test fun copyAndItsArchiveAreDeletedOnlyAfterSecondConfirmation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val suffix = UUID.randomUUID().toString().take(6)
        val title = "Копія з архівом $suffix"
        val localId = "copy-archive-$suffix"
        val archiveId = "archive-$suffix"
        CurrentPlaylistStore(context).save(
            ImportedPlaylist(title, mutableListOf(Track("Song", "Artist", selectedVideoId = "cparchive01"))),
            "Тест копії з архівом", localPlaylistId = localId)
        val history = HistoryStore(context)
        history.upsert(HistoryEntry(
            id = archiveId, createdAt = 1L, updatedAt = 2L,
            status = HistoryStatus.COMPLETED, sourceLabel = "File",
            playlistName = title, playlistId = null, privacyStatus = "private",
            destination = PendingDestination.NEW_PLAYLIST,
            googleEmail = null, youtubeChannelId = null, youtubeChannelTitle = null,
            totalImportedCount = 1, writeTargetCount = 1, addedCount = 1,
            failedCount = 0, pendingCount = 0, skippedCount = 0,
            duplicateCount = 0, missingCount = 0, lastError = null,
            tracks = listOf(HistoryTrack(
                index = 0, originalTitle = "Song", originalArtist = "Artist",
                videoId = "cparchive01", selectedTitle = "Song", selectedChannel = null,
                status = "ADDED", manuallySelected = false, error = null)),
            localPlaylistId = localId))
        try {
            ActivityScenario.launch<PlaylistLibraryActivity>(
                Intent(context, PlaylistLibraryActivity::class.java)
            ).use { scene ->
                idle()
                scene.onActivity { activity ->
                    val label = activity.window.decorView.findLabel(title) ?: error("Card missing")
                    var card: View? = label
                    while (card != null && !card.isClickable) card = card.parent as? View
                    card!!.performClick()
                }
                idle()
                scene.onActivity { activity ->
                    activity.window.decorView.collectButtons()
                        .first { it.isShown && it.text.toString() == "Видалити локальну копію" }
                        .performClick()
                }
                idle()
                scene.onActivity { clickInWindows("Видалити і копію, і архів…") }
                idle()
                scene.onActivity { activity ->
                    // Nothing is deleted yet — only the second window is open.
                    assertNotNull(RestorablePlaylistStore(activity).get(localId))
                    assertNotNull(HistoryStore(activity).get(archiveId))
                    clickInWindows("Видалити обидва")
                }
                idle()
                scene.onActivity { activity ->
                    assertNull(RestorablePlaylistStore(activity).get(localId))
                    assertNull(HistoryStore(activity).get(archiveId))
                }
            }
        } finally {
            history.remove(archiveId)
            RestorablePlaylistStore(context).remove(localId)
            CurrentPlaylistStore(context).clearIfMatches(localId)
        }
    }

    @android.annotation.TargetApi(29)
    private fun clickInWindows(label: String) {
        val target = android.view.inspector.WindowInspector.getGlobalWindowViews()
            .filter { it.isShown }
            .firstNotNullOfOrNull { it.findLabel(label) } ?: error("«$label» not shown")
        var view: View? = target
        while (view != null && !view.isClickable) view = view.parent as? View
        (view ?: target).performClick()
    }

    @Test fun historyDetailsOfferOnlyHistoryDeletionAndKeepEntryUntilConfirmation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = HistoryStore(context)
        val id = "history-delete-${UUID.randomUUID()}"
        val title = "Архів для видалення ${UUID.randomUUID()}"
        val tracks = listOf(HistoryTrack(
            index = 0, originalTitle = "Song", originalArtist = "Artist",
            videoId = null, selectedTitle = null, selectedChannel = null,
            status = "MISSING", manuallySelected = false, error = null
        ))
        store.upsert(HistoryEntry(
            id = id, createdAt = 1L, updatedAt = 2L,
            status = HistoryStatus.COMPLETED, sourceLabel = "File",
            playlistName = title, playlistId = null, privacyStatus = "private",
            destination = PendingDestination.NEW_PLAYLIST,
            googleEmail = null, youtubeChannelId = null, youtubeChannelTitle = null,
            totalImportedCount = 1, writeTargetCount = 0, addedCount = 0,
            failedCount = 0, pendingCount = 0, skippedCount = 0,
            duplicateCount = 0, missingCount = 1, lastError = null, tracks = tracks
        ))
        try {
            ActivityScenario.launch<PlaylistLibraryActivity>(
                Intent(context, PlaylistLibraryActivity::class.java)
            ).use { scene ->
                idle()
                scene.onActivity { activity ->
                    val label = activity.window.decorView.findLabel(title)
                        ?: error("History card not found")
                    var card: View? = label
                    while (card != null && !card.isClickable) card = card.parent as? View
                    assertNotNull(card)
                    card!!.performClick()
                }
                idle()
                scene.onActivity { activity ->
                    val names = activity.window.decorView.collectButtons()
                        .filter { it.isShown }.map { it.text.toString() }
                    assertTrue(names.contains("Видалити запис History"))
                    assertFalse(names.contains("Видалити локальну копію"))
                    assertFalse(names.contains("Видалити з YouTube Music"))
                    assertNotNull(HistoryStore(activity).get(id))
                }
                scene.recreate()
                idle()
                scene.onActivity { activity ->
                    assertTrue(activity.window.decorView.collectButtons()
                        .any { it.text.toString() == "Видалити запис History" })
                    assertNotNull(HistoryStore(activity).get(id))
                }
            }
        } finally {
            store.remove(id)
        }
    }

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
                val root = activity.window.decorView
                assertTrue(root.collectActions().none {
                    it is ImageButton && it.isShown &&
                        it.contentDescription in listOf("На телефоні", "YouTube")
                })
                assertTrue(root.collectActions().any {
                    it is ImageButton && it.isShown &&
                        it.contentDescription == "Відкрити поточний плейліст"
                })
                assertTrue(root.collectActions().any {
                    it is ImageButton && it.isShown &&
                        it.contentDescription == "Довідка бібліотеки плейлістів"
                })
                assertTrue(root.collectButtons().none {
                    it.isShown && it.text.toString().contains("До всіх плейлістів")
                })
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
                val root = activity.window.decorView
                val image = root.findImageDescription("Прев’ю: Трек з прев’ю")
                    ?: error("Portrait artwork missing")
                assertTrue(image.alpha in 0.39f..0.51f)
                val overlay = image.parent as? FrameLayout
                    ?: error("Portrait artwork must fill an overlay frame")
                val play = root.collectButtons().firstOrNull {
                    it.contentDescription == "Відтворити Трек з прев’ю у застосунку"
                } ?: error("Portrait overlay play missing")
                assertTrue(play.parent === overlay)
                assertTrue(overlay.hasText("1."))
                assertTrue(root.hasText("Трек з прев’ю"))
            }
        }
    }

    @Test fun landscapeDetailsKeepRowsAndHideCatalogueSourceActions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "Деталі landscape ${UUID.randomUUID()}"
        CurrentPlaylistStore(context).save(
            ImportedPlaylist(name, mutableListOf(
                Track("Ландшафтний трек", "Виконавець", selectedVideoId = "abcdefghijk")
            )), "Локальний тест",
            localPlaylistId = "landscape-details-${UUID.randomUUID()}"
        )
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            scene.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            idle()
            scene.onActivity { activity ->
                val label = activity.window.decorView.findLabel(name)
                    ?: error("Card missing")
                var item: View? = label
                while (item != null && !item.isClickable) item = item.parent as? View
                assertNotNull(item)
                item!!.performClick()
            }
            idle()
            scene.onActivity { activity ->
                val root = activity.window.decorView
                assertTrue(root.hasText("Ландшафтний трек"))
                assertTrue(root.collectActions().none {
                    it is ImageButton && it.isShown &&
                        it.contentDescription in listOf("На телефоні", "YouTube",
                            "Знайти плейліст", "Список одним стовпчиком")
                })
                assertTrue(root.collectActions().any {
                    it is ImageButton && it.isShown &&
                        it.contentDescription == "Довідка бібліотеки плейлістів"
                })
                val image = root.findImageDescription("Прев’ю: Ландшафтний трек")
                    ?: error("Landscape cover missing")
                assertTrue(image.parent is LinearLayout)
                assertTrue(root.collectButtons().any {
                    it.contentDescription == "Відтворити Ландшафтний трек у застосунку"
                })
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            idle()
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
                // Details keep a second Help control in the hidden header.
                // A catalogue test must select the actually visible one.
                val help = activity.window.decorView.collectActions().first {
                    it.isShown && it.contentDescription == "Довідка бібліотеки плейлістів"
                }
                help.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertTrue(activity.window.decorView.hasText("Всі •"))
                // Dialog Help owns a separate Window, not Activity.decorView.
                // Help opens a descriptive modal, never starts any playlist flow.
            }
        }
    }

    /** Batch 8: «Знайти дублікати за піснями» is on the pinned panel and only shows a window. */
    @Test fun songDuplicatesButtonOpensReadOnlyWindow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val suffix = UUID.randomUUID().toString().take(6)
        fun tracks(vararg ids: String) = ids.map {
            Track("Пісня $it", "Виконавець", selectedVideoId = it)
        }.toMutableList()
        val store = CurrentPlaylistStore(context)
        store.save(ImportedPlaylist("Перша назва $suffix",
            tracks("aaaaaaaaaa1", "aaaaaaaaaa2", "aaaaaaaaaa3", "aaaaaaaaaa4")),
            "Перевірка дублікатів", localPlaylistId = "dupA-$suffix")
        store.save(ImportedPlaylist("Зовсім інша $suffix",
            tracks("aaaaaaaaaa1", "aaaaaaaaaa2", "aaaaaaaaaa3", "aaaaaaaaaa9")),
            "Перевірка дублікатів", localPlaylistId = "dupB-$suffix")
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val button = activity.window.decorView.collectActions().first {
                    it is ImageButton && it.isShown &&
                        it.contentDescription == "Знайти дублікати за піснями"
                }
                assertNotNull((button as ImageButton).drawable)
                button.performClick()
            }
            idle()
            scene.onActivity { activity ->
                assertFalse(activity.isFinishing)
                assertTrue(activity.window.decorView.hasText("Перша назва $suffix"))
                assertTrue(activity.window.decorView.hasText("Зовсім інша $suffix"))
            }
            scene.recreate()
            idle()
            scene.onActivity { activity -> assertFalse(activity.isFinishing) }
        }
    }

    /** Batch 10: after the screen is rebuilt the catalogue is at the same card. */
    @Test fun catalogueKeepsItsPlaceAfterRecreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val suffix = UUID.randomUUID().toString().take(6)
        val store = CurrentPlaylistStore(context)
        val names = (1..24).map { "Місце $suffix №%02d".format(it) }
        names.forEachIndexed { i, name ->
            store.save(ImportedPlaylist(name, mutableListOf(Track("Пісня $i", "Гурт"))),
                "Тест місця", localPlaylistId = "place-$suffix-$i")
        }
        // «Нові» first: the 6th one saved is deep in the list.
        val target = names[5]
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val root = activity.window.decorView
                val scroller = root.findCatalogueScroller() ?: error("No catalogue scroll")
                val label = root.findLabel(target) ?: error("Card missing")
                scroller.scrollTo(0, label.topInside(scroller.getChildAt(0)))
                assertTrue(scroller.scrollY > 0)
            }
            idle()
            scene.recreate()
            var shown = false
            var tries = 0
            while (!shown && tries++ < 20) {
                idle()
                scene.onActivity { activity ->
                    val root = activity.window.decorView
                    val scroller = root.findCatalogueScroller()
                    val label = root.findLabel(target)
                    if (scroller != null && label != null && scroller.scrollY > 0) {
                        val onScreen = label.topInside(scroller.getChildAt(0)) - scroller.scrollY
                        shown = onScreen >= -label.height && onScreen < scroller.height
                    }
                }
                if (!shown) android.os.SystemClock.sleep(100)
            }
            assertTrue("Catalogue did not come back to the same card", shown)
        }
    }

    /** Batch 12: «Група №…» comes back on the same row after the screen is rebuilt. */
    @android.annotation.TargetApi(29)
    @Test fun groupWindowKeepsItsPlaceAfterRecreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val suffix = UUID.randomUUID().toString().take(6)
        val title = "Група місця $suffix"
        val store = CurrentPlaylistStore(context)
        repeat(14) { i ->
            store.save(ImportedPlaylist(title, mutableListOf(
                Track("Пісня 1", "Гурт", selectedVideoId = "grpplace001"),
                Track("Пісня 2", "Гурт", selectedVideoId = "grpplace002"))),
                "Тест вікна групи", localPlaylistId = "grp-$suffix-$i")
        }
        ActivityScenario.launch<PlaylistLibraryActivity>(
            Intent(context, PlaylistLibraryActivity::class.java)
        ).use { scene ->
            idle()
            scene.onActivity { activity ->
                val root = activity.window.decorView
                val label = root.findLabel(title) ?: error("Card missing")
                var card: View? = label
                while (card != null && !(card.isClickable && card.tag is String)) {
                    card = card.parent as? View
                }
                val hint = (card as ViewGroup).findClickableDescription("Схожі плейлісти, група")
                    ?: error("Group hint missing")
                hint.performClick()
            }
            idle()
            var before = 0
            scene.onActivity {
                val scroll = groupWindowScroller(title) ?: error("Group window missing")
                scroll.scrollTo(0, scroll.getChildAt(0).height)
                before = scroll.scrollY
                assertTrue(before > 0)
            }
            idle()
            scene.recreate()
            var after = 0
            var tries = 0
            while (after == 0 && tries++ < 20) {
                idle()
                scene.onActivity { after = groupWindowScroller(title)?.scrollY ?: 0 }
                if (after == 0) android.os.SystemClock.sleep(100)
            }
            assertTrue("Group window came back at the top ($after, was $before)",
                after > before / 2)
        }
    }

    @android.annotation.TargetApi(29)
    private fun groupWindowScroller(title: String): android.widget.ScrollView? =
        android.view.inspector.WindowInspector.getGlobalWindowViews()
            .filter { it.isShown && it.hasText("— схожі плейлісти") && it.hasText(title) }
            .firstNotNullOfOrNull { it.findCatalogueScroller() }

    private fun ViewGroup.findClickableDescription(prefix: String): View? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isClickable && child.contentDescription?.startsWith(prefix) == true) return child
            if (child is ViewGroup) child.findClickableDescription(prefix)?.let { return it }
        }
        return null
    }

    private fun View.findCatalogueScroller(): android.widget.ScrollView? {
        val found = mutableListOf<android.widget.ScrollView>()
        fun walk(view: View) {
            if (view is android.widget.ScrollView && view.childCount == 1) found += view
            if (view is ViewGroup) for (i in 0 until view.childCount) walk(view.getChildAt(i))
        }
        walk(this)
        return found.maxByOrNull { it.getChildAt(0).height }
    }

    private fun View.topInside(ancestor: View): Int {
        var y = 0
        var view: View? = this
        while (view != null && view !== ancestor) {
            y += view.top
            view = view.parent as? View
        }
        return y
    }

    /** Batch 11: the kept YouTube list is shown at once, before Google is back. */
    @Test fun keptYouTubeListIsShownWithoutConnection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val suffix = UUID.randomUUID().toString().take(6)
        val title = "Збережений YouTube $suffix"
        val store = com.saney.ytmimporter.storage.RemotePlaylistListStore(context)
        val auth = com.saney.ytmimporter.auth.PersistentAuthStateStore(context)
        store.save("UCtest$suffix", listOf(com.saney.ytmimporter.model.YouTubePlaylistInfo(
            "PLkept$suffix", title, "private", 12)))
        auth.markSuccessfulAuthorization()
        try {
            ActivityScenario.launch<PlaylistLibraryActivity>(
                Intent(context, PlaylistLibraryActivity::class.java)
            ).use { scene ->
                idle()
                scene.onActivity { activity ->
                    val root = activity.window.decorView
                    assertTrue("kept YouTube card missing", root.hasText(title))
                }
            }
        } finally {
            store.clear()
            auth.clear()
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
