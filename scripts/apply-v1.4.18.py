#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

def write(rel, text):
    (ROOT / rel).write_text(text, encoding="utf-8")

def replace_once(rel, old, new):
    text = read(rel)
    if new in text:
        print(f"SKIP already applied: {rel}")
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(
            f"STOP: expected exactly 1 anchor in {rel}, found {count}\n"
            f"ANCHOR:\n{old[:400]}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {rel}")

def replace_all(rel, old, new, minimum=1):
    text = read(rel)
    if new in text and old not in text:
        print(f"SKIP already applied: {rel}")
        return
    count = text.count(old)
    if count < minimum:
        raise SystemExit(
            f"STOP: expected at least {minimum} anchor(s) in {rel}, found {count}\n"
            f"ANCHOR:\n{old[:400]}"
        )
    write(rel, text.replace(old, new))
    print(f"PATCH: {rel} ({count} replacement(s))")


# Version
replace_once(
    "app/build.gradle.kts",
    '        versionCode = 51\n        versionName = "1.4.17"',
    '        versionCode = 52\n        versionName = "1.4.18"',
)

# YouTubeApi imports/result
replace_once(
    "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt",
    "import com.saney.ytmimporter.model.Track\n",
    "import com.saney.ytmimporter.model.Track\n"
    "import com.saney.ytmimporter.model.TrackStatus\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt",
    '''    data class PlaylistVideoIdsResult(
        val videoIds: Set<String>,
        val requestCount: Int
    )
''',
    '''    data class PlaylistVideoIdsResult(
        val videoIds: Set<String>,
        val requestCount: Int
    )

    data class PlaylistTracksResult(
        val tracks: List<Track>,
        val requestCount: Int
    )
''',
)

playlist_tracks_method = r'''    fun listPlaylistTracks(
        accessToken: String,
        playlistId: String
    ): PlaylistTracksResult {
        val tracks = mutableListOf<Track>()
        var pageToken: String? = null
        var requestCount = 0
        var pages = 0

        do {
            var url =
                "https://www.googleapis.com/youtube/v3/playlistItems" +
                    "?part=snippet,contentDetails" +
                    "&maxResults=50" +
                    "&playlistId=" +
                    URLEncoder.encode(
                        playlistId,
                        Charsets.UTF_8.name()
                    )

            if (!pageToken.isNullOrBlank()) {
                url += "&pageToken=" +
                    URLEncoder.encode(
                        pageToken,
                        Charsets.UTF_8.name()
                    )
            }

            val response =
                request(
                    "GET",
                    url,
                    accessToken
                )

            requestCount += 1

            requireSuccess(
                response,
                "Завантаження треків плейлиста"
            )

            val json = JSONObject(response.body)
            val items = json.optJSONArray("items")

            if (items != null) {
                for (i in 0 until items.length()) {
                    val item =
                        items.getJSONObject(i)

                    val snippet =
                        item.optJSONObject("snippet")

                    val contentDetails =
                        item.optJSONObject(
                            "contentDetails"
                        )

                    val contentVideoId =
                        contentDetails
                            ?.optString("videoId")
                            .orEmpty()
                            .trim()

                    val resourceVideoId =
                        snippet
                            ?.optJSONObject(
                                "resourceId"
                            )
                            ?.optString("videoId")
                            .orEmpty()
                            .trim()

                    val videoId =
                        contentVideoId
                            .ifBlank {
                                resourceVideoId
                            }

                    if (videoId.isBlank()) {
                        continue
                    }

                    val title =
                        decodeEntities(
                            snippet
                                ?.optString("title")
                                .orEmpty()
                        )
                            .trim()
                            .ifBlank {
                                "YouTube video $videoId"
                            }

                    val channel =
                        decodeEntities(
                            snippet
                                ?.optString(
                                    "videoOwnerChannelTitle"
                                )
                                .orEmpty()
                        )
                            .trim()
                            .ifBlank {
                                "YouTube"
                            }

                    tracks +=
                        Track(
                            originalTitle = title,
                            originalArtist = channel,
                            selectedVideoId = videoId,
                            selectedTitle = title,
                            selectedChannel = channel,
                            status = TrackStatus.MATCHED,
                            candidates = emptyList(),
                            manuallySelected = false,
                            error = null,
                            historyIndex = tracks.size
                        )
                }
            }

            pageToken =
                json.optString("nextPageToken")
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }

            pages += 1
        } while (
            pageToken != null &&
            pages < 200
        )

        return PlaylistTracksResult(
            tracks = tracks,
            requestCount = requestCount
        )
    }

'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt",
    "    fun listPlaylistVideoIds(\n",
    playlist_tracks_method + "    fun listPlaylistVideoIds(\n",
)

# ImportActivity imports/fields
replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    "import com.saney.ytmimporter.model.ImportedPlaylist\n",
    "import com.saney.ytmimporter.auth.AuthSessionStore\n"
    "import com.saney.ytmimporter.model.ImportedPlaylist\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    "import com.saney.ytmimporter.model.PendingDestination\n",
    "import com.saney.ytmimporter.model.PendingDestination\n"
    "import com.saney.ytmimporter.model.YouTubePlaylistInfo\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    "import com.saney.ytmimporter.storage.PlaylistProjectImport\n",
    "import com.saney.ytmimporter.storage.PlaylistProjectImport\n"
    "import com.saney.ytmimporter.youtube.YouTubeApi\n"
    "import java.util.concurrent.Executors\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''class ImportActivity : Activity() {
    private val fileRequestCode =
        2301
''',
    '''class ImportActivity : Activity() {
    private val fileRequestCode =
        2301

    private val executor =
        Executors.newSingleThreadExecutor()

    private val api =
        YouTubeApi()
''',
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''    override fun onActivityResult(
        requestCode: Int,
''',
    '''    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    override fun onActivityResult(
        requestCode: Int,
''',
)

account_section = r'''        content.addView(
            sectionTitle(
                "Імпорт із YouTube/YTM"
            )
        )

        content.addView(
            card().apply {
                addView(
                    TextView(
                        this@ImportActivity
                    ).apply {
                        text =
                            "Плейлист з підключеного акаунта"
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        setTypeface(
                            typeface,
                            Typeface.BOLD
                        )
                    }
                )

                addView(
                    infoText(
                        "Read-only імпорт: застосунок лише читає список плейлистів " +
                            "та їх треки. Плейлист у YouTube/YTM не змінюється. " +
                            "Треки відкриваються локально вже з точними videoId."
                    )
                )

                addView(
                    actionButton(
                        label =
                            "Вибрати плейлист з YTM",
                        primary = true
                    ) {
                        importFromYtmAccount()
                    }
                )
            }
        )

'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''        content.addView(
            sectionTitle("Імпорт із файлу")
        )
''',
    account_section + '''        content.addView(
            sectionTitle("Імпорт із файлу")
        )
''',
)

account_methods = r'''    private fun importFromYtmAccount() {
        val token =
            AuthSessionStore
                .current()
                .accessToken

        if (token.isNullOrBlank()) {
            toast(
                "Спочатку підключіть Google/YTM у кроці 2 на головному екрані."
            )
            return
        }

        toast(
            "Завантажую плейлисти YouTube/YTM…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listMyPlaylists(token)
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        playlists ->

                    if (playlists.isEmpty()) {
                        toast(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                        return@onSuccess
                    }

                    showYtmPlaylistPicker(
                        token = token,
                        playlists = playlists
                    )
                }.onFailure { error ->
                    toast(
                        error.message
                            ?: "Не вдалося завантажити список плейлистів"
                    )
                }
            }
        }
    }

    private fun showYtmPlaylistPicker(
        token: String,
        playlists: List<YouTubePlaylistInfo>
    ) {
        val labels =
            playlists.map { playlist ->
                buildString {
                    append(playlist.title)
                    append("\n")
                    append(playlist.itemCount)
                    append(" треків • ")
                    append(
                        when (
                            playlist.privacyStatus
                        ) {
                            "public" ->
                                "публічний"

                            "unlisted" ->
                                "за посиланням"

                            else ->
                                "приватний"
                        }
                    )
                }
            }

        UiChrome.showMenuDialog(
            activity = this,
            title =
                "Вибрати плейлист YouTube/YTM",
            subtitle =
                "Read-only: виберіть плейлист для локального імпорту.",
            actions =
                playlists.mapIndexed {
                        index,
                        playlist ->

                    UiChrome.MenuAction(
                        label = labels[index],
                        onClick = {
                            loadYtmPlaylist(
                                token = token,
                                playlistInfo =
                                    playlist
                            )
                        }
                    )
                },
            negativeLabel = "Скасувати"
        )
    }

    private fun loadYtmPlaylist(
        token: String,
        playlistInfo: YouTubePlaylistInfo
    ) {
        toast(
            "Завантажую «${playlistInfo.title}»…"
        )

        executor.execute {
            val result =
                runCatching {
                    api.listPlaylistTracks(
                        accessToken = token,
                        playlistId =
                            playlistInfo.id
                    )
                }

            runOnUiThread {
                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                result.onSuccess {
                        loaded ->

                    if (loaded.tracks.isEmpty()) {
                        toast(
                            "Плейлист «${playlistInfo.title}» порожній або не містить доступних відео."
                        )
                        return@onSuccess
                    }

                    val imported =
                        ImportedPlaylist(
                            name =
                                playlistInfo.title,
                            tracks =
                                loaded.tracks
                                    .toMutableList()
                        )

                    finishImport(
                        imported = imported,
                        sourceLabel =
                            "YouTube/YTM (${playlistInfo.title})",
                        message =
                            "YTM playlist імпортовано: " +
                                "${imported.tracks.size} треків • " +
                                "точних videoId: ${imported.tracks.size} • " +
                                "playlistItems.list: ${loaded.requestCount} request(s)."
                    )
                }.onFailure { error ->
                    toast(
                        error.message
                            ?: "Не вдалося завантажити плейлист"
                    )
                }
            }
        }
    }

'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''    /**
     * Deliberately accepts any file type because some Android file providers
''',
    account_methods + '''    /**
     * Deliberately accepts any file type because some Android file providers
''',
)

# release-preflight current version + audit
replace_all(
    "scripts/release-preflight.sh",
    'check_file "docs/v.1.4.17/RELEASE.md"',
    'check_file "docs/v.1.4.18/RELEASE.md"',
)
replace_all(
    "scripts/release-preflight.sh",
    'check_file "docs/v.1.4.17/REGRESSION_CHECKLIST.md"',
    'check_file "docs/v.1.4.18/REGRESSION_CHECKLIST.md"',
)
replace_all(
    "scripts/release-preflight.sh",
    "bash scripts/v1417-auth-flow-audit.sh",
    "bash scripts/v1418-account-library-import-audit.sh",
)
replace_all(
    "scripts/release-preflight.sh",
    "grep -q 'versionCode = 51' app/build.gradle.kts",
    "grep -q 'versionCode = 52' app/build.gradle.kts",
)
replace_all(
    "scripts/release-preflight.sh",
    'grep -q \'versionName = "1.4.17"\' app/build.gradle.kts',
    'grep -q \'versionName = "1.4.18"\' app/build.gradle.kts',
)
replace_all(
    "scripts/release-preflight.sh",
    '|| fail "Expected versionCode = 51"',
    '|| fail "Expected versionCode = 52"',
)
replace_all(
    "scripts/release-preflight.sh",
    '|| fail \'Expected versionName = "1.4.17"\'',
    '|| fail \'Expected versionName = "1.4.18"\'',
)

preflight = read("scripts/release-preflight.sh")
needle = 'check_file "scripts/v1417-auth-flow-audit.sh"\n'
addition = (
    'check_file "scripts/v1417-auth-flow-audit.sh"\n'
    'check_file "scripts/v1418-account-library-import-audit.sh"\n'
)
if 'check_file "scripts/v1418-account-library-import-audit.sh"' not in preflight:
    if needle not in preflight:
        raise SystemExit("STOP: release-preflight audit anchor missing")
    preflight = preflight.replace(needle, addition, 1)

old_status_guard = '''grep -Fq '| v1.4.17 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.17 must start NOT TESTED YET"'''
new_status_guard = '''grep -Fq '| v1.4.17 | **PARTIALLY PHONE-TESTED — PASS FOR TESTED PATH** |' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.17 tested-path status missing"\ngrep -Fq '| v1.4.18 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.18 must start NOT TESTED YET"'''
if new_status_guard not in preflight:
    if old_status_guard not in preflight:
        raise SystemExit("STOP: release-preflight release-status guard missing")
    preflight = preflight.replace(
        old_status_guard,
        new_status_guard,
        1,
    )

write("scripts/release-preflight.sh", preflight)
print("PATCH: scripts/release-preflight.sh (G01 audit/status guards)")

# qa-plan-audit follows the mutable release status register.
qa_audit = read("scripts/qa-plan-audit.sh")
qa_old = '''grep -Fq '| v1.4.17 | **NOT TESTED YET** |' "$STATUS" \\\n  || fail "v1.4.17 must start NOT TESTED YET"'''
qa_new = '''grep -Fq '| v1.4.17 | **PARTIALLY PHONE-TESTED — PASS FOR TESTED PATH** |' "$STATUS" \\\n  || fail "v1.4.17 tested-path status missing"\ngrep -Fq '| v1.4.18 | **NOT TESTED YET** |' "$STATUS" \\\n  || fail "v1.4.18 must start NOT TESTED YET"'''
if qa_new not in qa_audit:
    if qa_old not in qa_audit:
        raise SystemExit("STOP: qa-plan-audit release-status guard missing")
    qa_audit = qa_audit.replace(
        qa_old,
        qa_new,
        1,
    )
    write("scripts/qa-plan-audit.sh", qa_audit)
    print("PATCH: scripts/qa-plan-audit.sh")

# changelog
changelog_entry = '''## v1.4.18
- G01: added read-only import of one playlist from the connected YouTube/YTM account.
- Import screen can list account playlists, select one and load ordered playlist items.
- Imported account tracks keep exact YouTube videoId and open as the current local workspace.
- Exact imported selections are marked MATCHED, so Step 3 can open Review without search.list.
- Playlist source is read-only; no account playlist is modified during import.
- Added v1.4.18 G01 static audit, release docs and phone-test plan.
- v1.4.17 FAST_FLOW existing-target duplicate/rotation path recorded as phone-tested PASS; unrelated auth cases remain open/retest.
- versionCode 52 / versionName 1.4.18.
- v1.4.18 G01 = NOT PHONE-TESTED YET.

'''
replace_once(
    "CHANGELOG.md",
    "# Журнал змін (Changelog)\n\n",
    "# Журнал змін (Changelog)\n\n" + changelog_entry,
)

# backlog
replace_once(
    "BACKLOG.md",
    '''## Current
v1.4.17 — Auth-state + Review→Destination UX
''',
    '''## Current
v1.4.18 — YTM account library import/export, G01 one-playlist import
''',
)

v1418_backlog = '''## v1.4.18
- [x] G01 list playlists from connected YouTube/YTM account
- [x] G01 select one account playlist
- [x] G01 load ordered playlist items with exact videoId
- [x] G01 open imported account playlist as current local workspace
- [x] G01 preserve exact selections so search.list is not required
- [x] G01 static audit + phone-test plan
- [ ] GitHub build
- [ ] phone test: account playlist list/picker
- [ ] phone test: import one playlist
- [ ] phone test: Step 3 opens Review without search.list
- [ ] phone test: save imported workspace as YTM Project
- [ ] phone test: reopen saved YTM Project and preserve exact videoId
- [ ] next wave: export all account playlists to a chosen folder

'''

replace_once(
    "BACKLOG.md",
    "## Next\nPreferred v1.4.18 candidate:\n",
    v1418_backlog + "## Next\nAfter G01 phone verification:\n",
)
replace_once(
    "BACKLOG.md",
    '''YTM account library import/export — load one playlist into a local working project,
save/export it for reuse, and later support exporting all account playlists to a chosen folder.
''',
    '''continue YTM account library work — verify local-project reuse first,
then add read-only export of all account playlists to a chosen device folder.
''',
)

# project status
status = read("PROJECT_STATUS.txt")
status = status.replace("Version: 1.4.17", "Version: 1.4.18", 1)
status = status.replace("Version code: 51", "Version code: 52", 1)
if "v1.4.18 NOT TESTED YET" not in status:
    status = status.replace(
        "v1.4.17 NOT TESTED YET\n",
        "v1.4.17 PARTIALLY PHONE-TESTED — FAST_FLOW EXISTING-TARGET PASS\n"
        "v1.4.18 NOT TESTED YET\n",
        1,
    )

old_focus = '''v1.4.17 focus:
- invalidate stale auth-ready state after HTTP 401
- direct Review -> Destination navigation
- cleaner playlist result actions
- dynamic APK/artifact naming
- TERMUX_COMMANDS.md
- plan YTM account playlist import/export
'''
new_focus = '''v1.4.18 G01 focus:
- read-only list of playlists from connected YouTube/YTM account
- select one playlist
- load playlist items in account order with exact videoId
- open the account playlist as current local workspace
- preserve exact selections and avoid unnecessary search.list
- phone-test local YTM Project save/reopen after account import
'''
if new_focus not in status:
    if old_focus not in status:
        raise SystemExit("STOP: PROJECT_STATUS focus anchor missing")
    status = status.replace(old_focus, new_focus, 1)
write("PROJECT_STATUS.txt", status)
print("PATCH: PROJECT_STATUS.txt")

# release test status
replace_once(
    "RELEASE_TEST_STATUS.md",
    '''| v1.4.17 | **NOT TESTED YET** | Auth-state + Review→Destination UX; BUG-003/004 require phone retest. |
''',
    '''| v1.4.17 | **PARTIALLY PHONE-TESTED — PASS FOR TESTED PATH** | FAST_FLOW existing-target duplicate flow, rotation state preservation and Added: 0 result passed. BUG-003/004 are not closed by this run. |
| v1.4.18 | **NOT TESTED YET** | G01 account playlist → current local workspace; requires GitHub build + phone test. |
''',
)

# design plan
plan = read("docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md")
marker = "Suggested implementation wave: v1.4.18.\n"
plan_add = '''Suggested implementation wave: v1.4.18.

## v1.4.18 G01
Implemented scope:
- list playlists from the connected account;
- select one playlist;
- load ordered playlist items with exact videoId;
- open it as the current local workspace;
- keep the operation read-only against the source account.

Still pending:
- phone verification;
- save/reopen verification as YTM Project;
- export all account playlists to a chosen folder.
'''
if plan_add not in plan:
    if marker not in plan:
        raise SystemExit("STOP: design-plan anchor missing")
    plan = plan.replace(marker, plan_add, 1)
    write("docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md", plan)
    print("PATCH: docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md")

print()
print("PASS: v1.4.18 G01 source/document patches applied")
