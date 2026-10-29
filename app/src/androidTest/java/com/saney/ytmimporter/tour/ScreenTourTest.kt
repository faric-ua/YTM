package com.saney.ytmimporter.tour

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saney.ytmimporter.DataActivity
import com.saney.ytmimporter.EditPlaylistActivity
import com.saney.ytmimporter.HistoryActivity
import com.saney.ytmimporter.ImportActivity
import com.saney.ytmimporter.MainActivity
import com.saney.ytmimporter.MenuActivity
import com.saney.ytmimporter.PendingActivity
import com.saney.ytmimporter.PlaylistActivity
import com.saney.ytmimporter.PlaylistLibraryActivity
import com.saney.ytmimporter.QuotaActivity
import com.saney.ytmimporter.RecoveryCenterActivity
import com.saney.ytmimporter.ReviewActivity
import com.saney.ytmimporter.ServiceActivity
import com.saney.ytmimporter.UrlSnapshotActivity
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingDestination
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.CurrentPlaylistStore
import com.saney.ytmimporter.storage.HistoryStore
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Screenshots for the app presentation video (faric 2026-10-29: «зроби
 * відео-показ про наш проєкт»). Runs only with the instrumentation argument
 * screenTour=1 (workflow «UI screens»), never in Validate. Demo data only;
 * nothing is written to YouTube.
 */
@RunWith(AndroidJUnit4::class)
class ScreenTourTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    /** Runs as the shell user: files in /data/local/tmp survive the app uninstall after tests. */
    private fun sh(cmd: String) {
        val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(cmd)
        android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.readBytes() }
    }

    private fun shot(name: String) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(3500)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        sh("screencap -p /data/local/tmp/tour/$name.png")
        Log.i("ScreenTour", "shot $name")
    }

    private inline fun <reified A : Activity> screen(name: String, intent: Intent? = null) {
        runCatching {
            val i = intent ?: Intent(context, A::class.java)
            ActivityScenario.launch<A>(i).use { shot(name) }
        }.onFailure { Log.e("ScreenTour", "FAILED $name", it) }
    }

    private fun t(artist: String, title: String, id: String?, status: TrackStatus,
                  candidates: List<SearchCandidate> = emptyList()) =
        Track(title, artist, selectedVideoId = id,
            selectedTitle = id?.let { "$artist - $title" }, selectedChannel = id?.let { "$artist" },
            status = status, candidates = candidates)

    private fun demoTracks() = mutableListOf(
        t("Rick Astley", "Never Gonna Give You Up", "dQw4w9WgXcQ", TrackStatus.MATCHED),
        t("Queen", "Bohemian Rhapsody", "fJ9rUzIMcZQ", TrackStatus.MATCHED),
        t("Mark Ronson ft. Bruno Mars", "Uptown Funk", "OPf0YbXqDm0", TrackStatus.MATCHED),
        t("Nirvana", "Smells Like Teen Spirit", "hTWKbfoikeg", TrackStatus.MATCHED),
        t("Guns N' Roses", "Sweet Child O' Mine", "1w7OgIMMRc4", TrackStatus.MATCHED),
        t("Ed Sheeran", "Shape of You", "JGwWNGJdvx8", TrackStatus.REVIEW, listOf(
            SearchCandidate("JGwWNGJdvx8", "Ed Sheeran - Shape of You (Official Music Video)", "Ed Sheeran", 0.71),
            SearchCandidate("_dK2tDK9grQ", "Ed Sheeran - Shape Of You [Official Lyric Video]", "Ed Sheeran", 0.66))),
        t("Adele", "Hello", "YQHsXMglC9A", TrackStatus.REVIEW, listOf(
            SearchCandidate("YQHsXMglC9A", "Adele - Hello (Official Music Video)", "Adele", 0.68))),
        t("Luis Fonsi", "Despacito", "kJQP7kiw5Fk", TrackStatus.MATCHED),
        t("PSY", "Gangnam Style", "9bZkp7q19f0", TrackStatus.MATCHED),
        t("Демо-гурт", "Пісня, якої нема на YouTube", null, TrackStatus.MISSING)
    )

    private fun historyEntry(id: String, name: String, created: Long, ids: List<String?>) = HistoryEntry(
        id = id, createdAt = created, updatedAt = created + 60_000,
        status = HistoryStatus.COMPLETED, sourceLabel = "Файл demo.csv",
        playlistName = name, playlistId = null, privacyStatus = "private",
        destination = PendingDestination.NEW_PLAYLIST,
        googleEmail = null, youtubeChannelId = null, youtubeChannelTitle = null,
        totalImportedCount = ids.size, writeTargetCount = ids.size, addedCount = ids.count { it != null },
        failedCount = 0, pendingCount = 0, skippedCount = 0,
        duplicateCount = 0, missingCount = ids.count { it == null }, lastError = null,
        tracks = ids.mapIndexed { i, v -> HistoryTrack(
            index = i, originalTitle = "Пісня ${i + 1}", originalArtist = "Демо",
            videoId = v, selectedTitle = v?.let { "Пісня ${i + 1}" }, selectedChannel = null,
            status = if (v != null) "ADDED" else "MISSING", manuallySelected = false, error = null) }
    )

    @Test fun tour() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("screenTour") == "1")
        sh("rm -rf /data/local/tmp/tour")
        sh("mkdir -p /data/local/tmp/tour")
        val ids = demoTracks().map { it.selectedVideoId }
        val store = CurrentPlaylistStore(context)
        // Library fixtures: a few playlists, two of them copies of each other.
        store.save(ImportedPlaylist("Демо · Рок-класика", mutableListOf(
            t("Queen", "Bohemian Rhapsody", "fJ9rUzIMcZQ", TrackStatus.ADDED),
            t("Nirvana", "Smells Like Teen Spirit", "hTWKbfoikeg", TrackStatus.ADDED),
            t("Guns N' Roses", "Sweet Child O' Mine", "1w7OgIMMRc4", TrackStatus.ADDED))),
            "Файл rock.csv", localPlaylistId = "tour-rock-1")
        store.save(ImportedPlaylist("Демо · Рок-класика", mutableListOf(
            t("Queen", "Bohemian Rhapsody", "fJ9rUzIMcZQ", TrackStatus.ADDED),
            t("Nirvana", "Smells Like Teen Spirit", "hTWKbfoikeg", TrackStatus.ADDED),
            t("Guns N' Roses", "Sweet Child O' Mine", "1w7OgIMMRc4", TrackStatus.ADDED))),
            "Файл rock-copy.csv", localPlaylistId = "tour-rock-2")
        store.save(ImportedPlaylist("Демо · Поп 2010-х", mutableListOf(
            t("Ed Sheeran", "Shape of You", "JGwWNGJdvx8", TrackStatus.ADDED),
            t("Adele", "Hello", "YQHsXMglC9A", TrackStatus.ADDED),
            t("Mark Ronson ft. Bruno Mars", "Uptown Funk", "OPf0YbXqDm0", TrackStatus.ADDED))),
            "Файл pop.csv", localPlaylistId = "tour-pop")
        val history = HistoryStore(context)
        history.upsert(historyEntry("tour-h1", "Демо · Хіти назавжди", 1_758_900_000_000, ids))
        history.upsert(historyEntry("tour-h2", "Демо · Рок-класика", 1_758_000_000_000,
            listOf("fJ9rUzIMcZQ", "hTWKbfoikeg", "1w7OgIMMRc4")))
        // The current playlist on Home.
        store.save(ImportedPlaylist("Демо · Хіти назавжди", demoTracks()), "Файл demo.csv",
            localPlaylistId = "tour-current")

        context.getSharedPreferences("ui_prefs_v1", Context.MODE_PRIVATE).edit()
            .remove("welcome_v1_1_seen").commit()
        screen<MainActivity>("01_welcome")
        context.getSharedPreferences("ui_prefs_v1", Context.MODE_PRIVATE).edit()
            .putBoolean("welcome_v1_1_seen", true).commit()
        screen<MainActivity>("02_home")
        screen<MenuActivity>("03_menu")
        screen<ImportActivity>("04_import")
        screen<PlaylistActivity>("05_playlist")
        screen<ReviewActivity>("06_review")
        screen<EditPlaylistActivity>("07_edit")
        screen<HistoryActivity>("08_history")
        screen<PendingActivity>("09_queue")
        screen<QuotaActivity>("10_quota")
        screen<PlaylistLibraryActivity>("11_library")
        screen<UrlSnapshotActivity>("12_url")
        screen<DataActivity>("13_data")
        screen<RecoveryCenterActivity>("14_recovery")
        screen<ServiceActivity>("15_about", Intent(context, ServiceActivity::class.java)
            .putExtra(ServiceActivity.EXTRA_START_PAGE, ServiceActivity.START_PAGE_ABOUT))
        screen<ServiceActivity>("16_service")
    }
}
