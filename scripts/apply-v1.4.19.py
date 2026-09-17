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
            f"ANCHOR:\n{old[:500]}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {rel}")

replace_once(
    "app/build.gradle.kts",
    '        versionCode = 52\n        versionName = "1.4.18"',
    '        versionCode = 53\n        versionName = "1.4.19"',
)

project_method = r'''    fun exportAccountPlaylist(
        playlist: ImportedPlaylist,
        sourcePlaylistId: String,
        privacyStatus: String,
        appVersion: String
    ): String {
        val tracks = JSONArray()

        playlist.tracks
            .forEachIndexed { index, track ->
                tracks.put(
                    workingTrackToJson(
                        index = index,
                        track = track
                    )
                )
            }

        val playlistJson =
            JSONObject()
                .put("name", playlist.name)
                .put(
                    "sourcePlaylistId",
                    sourcePlaylistId
                )
                .put(
                    "privacyStatus",
                    privacyStatus
                )
                .put(
                    "sourceDestination",
                    JSONObject.NULL
                )
                .put(
                    "tracks",
                    tracks
                )

        return JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("appVersion", appVersion)
            .put("exportedAt", System.currentTimeMillis())
            .put("sourceHistoryId", JSONObject.NULL)
            .put("sourceLabel", "YouTube/YTM account")
            .put("scope", "account-playlist-export")
            .put(
                "note",
                "Read-only local export of a playlist from the connected YouTube/YTM account."
            )
            .put("playlist", playlistJson)
            .toString(2)
    }

'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt",
    "    fun exportHistoryEntry(\n",
    project_method + "    fun exportHistoryEntry(\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    "import android.provider.OpenableColumns\n",
    "import android.provider.DocumentsContract\n"
    "import android.provider.OpenableColumns\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    "import com.saney.ytmimporter.storage.CurrentPlaylistStore\n",
    "import com.saney.ytmimporter.storage.AccountLibraryExporter\n"
    "import com.saney.ytmimporter.storage.CurrentPlaylistStore\n",
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''    private val fileRequestCode =
        2301
''',
    '''    private val fileRequestCode =
        2301

    private val exportFolderRequestCode =
        2302
''',
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''        if (
            requestCode == fileRequestCode &&
            resultCode == RESULT_OK
        ) {
            data
                ?.data
                ?.let(::loadFile)
        }
''',
    '''        if (resultCode != RESULT_OK) {
            return
        }

        when (requestCode) {
            fileRequestCode ->
                data
                    ?.data
                    ?.let(::loadFile)

            exportFolderRequestCode ->
                data
                    ?.data
                    ?.let(
                        ::exportAllYtmPlaylistsToFolder
                    )
        }
''',
)

button_anchor = '''                addView(
                    actionButton(
                        label =
                            "Вибрати плейлист з YTM",
                        primary = true
                    ) {
                        importFromYtmAccount()
                    }
                )
'''

button_new = button_anchor + '''
                addView(
                    actionButton(
                        label =
                            "Експортувати всі плейлисти в папку",
                        primary = false
                    ) {
                        chooseYtmExportFolder()
                    }
                )
'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    button_anchor,
    button_new,
)

bulk_methods = r'''    private fun chooseYtmExportFolder() {
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

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT_TREE
            ).apply {
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            exportFolderRequestCode
        )
    }

    private fun exportAllYtmPlaylistsToFolder(
        treeUri: Uri
    ) {
        val token =
            AuthSessionStore
                .current()
                .accessToken

        if (token.isNullOrBlank()) {
            toast(
                "Авторизація Google/YTM недоступна. Підключіть акаунт ще раз."
            )
            return
        }

        runCatching {
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
        }

        toast(
            "Готую read-only експорт плейлистів…"
        )

        executor.execute {
            val result =
                runCatching {
                    val playlists =
                        api.listMyPlaylists(token)

                    if (playlists.isEmpty()) {
                        error(
                            "У підключеному акаунті немає доступних плейлистів."
                        )
                    }

                    runOnUiThread {
                        if (
                            !isFinishing &&
                            !isDestroyed
                        ) {
                            toast(
                                "Знайдено ${playlists.size} плейлистів. Експортую…"
                            )
                        }
                    }

                    val session =
                        AccountLibraryExporter
                            .createSessionFolder(
                                resolver =
                                    contentResolver,
                                treeUri =
                                    treeUri
                            )

                    val records =
                        mutableListOf<
                            AccountLibraryExporter.ExportRecord
                        >()

                    playlists.forEach {
                            playlistInfo ->

                        if (
                            playlistInfo.itemCount <= 0
                        ) {
                            records +=
                                AccountLibraryExporter.ExportRecord(
                                    playlistId =
                                        playlistInfo.id,
                                    title =
                                        playlistInfo.title,
                                    privacyStatus =
                                        playlistInfo.privacyStatus,
                                    sourceItemCount =
                                        playlistInfo.itemCount,
                                    exportedTrackCount = 0,
                                    playlistItemsRequests = 0,
                                    status =
                                        "SKIPPED_EMPTY",
                                    fileName = null
                                )

                            return@forEach
                        }

                        runCatching {
                            api.listPlaylistTracks(
                                accessToken =
                                    token,
                                playlistId =
                                    playlistInfo.id
                            )
                        }.onSuccess {
                                loaded ->

                            if (
                                loaded.tracks.isEmpty()
                            ) {
                                records +=
                                    AccountLibraryExporter.ExportRecord(
                                        playlistId =
                                            playlistInfo.id,
                                        title =
                                            playlistInfo.title,
                                        privacyStatus =
                                            playlistInfo.privacyStatus,
                                        sourceItemCount =
                                            playlistInfo.itemCount,
                                        exportedTrackCount = 0,
                                        playlistItemsRequests =
                                            loaded.requestCount,
                                        status =
                                            "SKIPPED_NO_ACCESSIBLE_TRACKS",
                                        fileName = null
                                    )
                            } else {
                                val imported =
                                    ImportedPlaylist(
                                        name =
                                            playlistInfo.title,
                                        tracks =
                                            loaded.tracks
                                                .toMutableList()
                                    )

                                val fileName =
                                    AccountLibraryExporter
                                        .writePlaylistProject(
                                            resolver =
                                                contentResolver,
                                            session =
                                                session,
                                            playlistInfo =
                                                playlistInfo,
                                            playlist =
                                                imported,
                                            appVersion =
                                                BuildConfig.VERSION_NAME
                                        )

                                records +=
                                    AccountLibraryExporter.ExportRecord(
                                        playlistId =
                                            playlistInfo.id,
                                        title =
                                            playlistInfo.title,
                                        privacyStatus =
                                            playlistInfo.privacyStatus,
                                        sourceItemCount =
                                            playlistInfo.itemCount,
                                        exportedTrackCount =
                                            imported.tracks.size,
                                        playlistItemsRequests =
                                            loaded.requestCount,
                                        status =
                                            "EXPORTED",
                                        fileName =
                                            fileName
                                    )
                            }
                        }.onFailure {
                                error ->

                            records +=
                                AccountLibraryExporter.ExportRecord(
                                    playlistId =
                                        playlistInfo.id,
                                    title =
                                        playlistInfo.title,
                                    privacyStatus =
                                        playlistInfo.privacyStatus,
                                    sourceItemCount =
                                        playlistInfo.itemCount,
                                    exportedTrackCount = 0,
                                    playlistItemsRequests = 0,
                                    status =
                                        "FAILED",
                                    fileName = null,
                                    error =
                                        error.message
                                            ?: error
                                                .javaClass
                                                .simpleName
                                )
                        }
                    }

                    val manifestFile =
                        AccountLibraryExporter
                            .writeManifest(
                                resolver =
                                    contentResolver,
                                session =
                                    session,
                                appVersion =
                                    BuildConfig.VERSION_NAME,
                                records =
                                    records
                            )

                    BulkExportResult(
                        folderName =
                            session.folderName,
                        manifestFile =
                            manifestFile,
                        playlistCount =
                            records.size,
                        exportedProjects =
                            records.count {
                                it.status ==
                                    "EXPORTED"
                            },
                        skippedPlaylists =
                            records.count {
                                it.status
                                    .startsWith(
                                        "SKIPPED"
                                    )
                            },
                        failedPlaylists =
                            records.count {
                                it.status ==
                                    "FAILED"
                            },
                        playlistItemsRequests =
                            records.sumOf {
                                it.playlistItemsRequests
                            }
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
                        summary ->

                    UiChrome.showMessageDialog(
                        activity = this,
                        title =
                            "Експорт завершено",
                        message =
                            "Плейлистів акаунта: ${summary.playlistCount}\n" +
                                "Збережено YTM Project: ${summary.exportedProjects}\n" +
                                "Пропущено: ${summary.skippedPlaylists}\n" +
                                "Помилок: ${summary.failedPlaylists}\n" +
                                "playlistItems.list: ${summary.playlistItemsRequests} request(s)\n\n" +
                                "Папка: ${summary.folderName}\n" +
                                "Індекс: ${summary.manifestFile}",
                        actions =
                            listOf(
                                UiChrome.DialogAction(
                                    label =
                                        "Закрити",
                                    tone =
                                        UiChrome.ActionTone.ACCENT,
                                    onClick = {}
                                )
                            )
                    )
                }.onFailure { error ->
                    UiChrome.showMessageDialog(
                        activity = this,
                        title =
                            "Експорт не завершено",
                        message =
                            error.message
                                ?: "Невідома помилка експорту",
                        actions =
                            listOf(
                                UiChrome.DialogAction(
                                    label =
                                        "Закрити",
                                    tone =
                                        UiChrome.ActionTone.ACCENT,
                                    onClick = {}
                                )
                            )
                    )
                }
            }
        }
    }

    private data class BulkExportResult(
        val folderName: String,
        val manifestFile: String,
        val playlistCount: Int,
        val exportedProjects: Int,
        val skippedPlaylists: Int,
        val failedPlaylists: Int,
        val playlistItemsRequests: Int
    )

'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''    /**
     * Deliberately accepts any file type because some Android file providers
''',
    bulk_methods + '''    /**
     * Deliberately accepts any file type because some Android file providers
''',
)

replace_once(
    "scripts/release-preflight.sh",
    'check_file "docs/v.1.4.18/RELEASE.md"',
    'check_file "docs/v.1.4.19/RELEASE.md"',
)

replace_once(
    "scripts/release-preflight.sh",
    'check_file "docs/v.1.4.18/REGRESSION_CHECKLIST.md"',
    'check_file "docs/v.1.4.19/REGRESSION_CHECKLIST.md"',
)

replace_once(
    "scripts/release-preflight.sh",
    'check_file "scripts/v1418-account-library-import-audit.sh"\n',
    'check_file "scripts/v1418-account-library-import-audit.sh"\n'
    'check_file "scripts/v1419-account-library-export-audit.sh"\n',
)

replace_once(
    "scripts/release-preflight.sh",
    "bash scripts/v1418-account-library-import-audit.sh\n",
    "bash scripts/v1419-account-library-export-audit.sh\n",
)

replace_once(
    "scripts/release-preflight.sh",
    "grep -q 'versionCode = 52' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 52\"",
    "grep -q 'versionCode = 53' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 53\"",
)

replace_once(
    "scripts/release-preflight.sh",
    "grep -q 'versionName = \"1.4.18\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.18\"'",
    "grep -q 'versionName = \"1.4.19\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.19\"'",
)

qa = read("scripts/qa-plan-audit.sh")
status_guard = '''grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' "$STATUS" \\
  || fail "v1.4.18 G01 phone-test PASS status missing"'''
status_guard_new = status_guard + '''
grep -Fq '| v1.4.19 | **NOT TESTED YET** |' "$STATUS" \\
  || fail "v1.4.19 must start NOT TESTED YET"'''
if status_guard_new not in qa:
    if status_guard not in qa:
        raise SystemExit(
            "STOP: qa-plan-audit v1.4.18 status guard missing"
        )
    write(
        "scripts/qa-plan-audit.sh",
        qa.replace(status_guard, status_guard_new, 1)
    )
    print("PATCH: scripts/qa-plan-audit.sh")

preflight = read("scripts/release-preflight.sh")
release_status_guard = '''grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.18 G01 phone-test PASS status missing"'''
release_status_new = release_status_guard + '''
grep -Fq '| v1.4.19 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.19 must start NOT TESTED YET"'''
if release_status_new not in preflight:
    if release_status_guard not in preflight:
        raise SystemExit(
            "STOP: release-preflight v1.4.18 status guard missing"
        )
    write(
        "scripts/release-preflight.sh",
        preflight.replace(
            release_status_guard,
            release_status_new,
            1
        )
    )
    print("PATCH: scripts/release-preflight.sh (v1.4.19 status)")

replace_once(
    "RELEASE_TEST_STATUS.md",
    '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** | Account playlist picker/import, exact-videoId Review path, and YTM Project save/reopen passed on phone. Other regressions remain untested. |',
    '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** | Account playlist picker/import, exact-videoId Review path, and YTM Project save/reopen passed on phone. Other regressions remain untested. |\n'
    '| v1.4.19 | **NOT TESTED YET** | Read-only bulk export of all account playlists to a chosen device folder. |',
)

replace_once(
    "PROJECT_STATUS.txt",
    "Version: 1.4.18\nVersion code: 52",
    "Version: 1.4.19\nVersion code: 53",
)

status = read("PROJECT_STATUS.txt")
if "v1.4.19 NOT TESTED YET" not in status:
    anchor = "v1.4.18 PARTIALLY PHONE-TESTED — G01 PASS\n"
    if anchor not in status:
        raise SystemExit(
            "STOP: PROJECT_STATUS v1.4.18 anchor missing"
        )
    status = status.replace(
        anchor,
        anchor + "v1.4.19 NOT TESTED YET\n",
        1
    )

old_focus = '''v1.4.18 G01 focus:
- read-only list of playlists from connected YouTube/YTM account
- select one playlist
- load playlist items in account order with exact videoId
- open the account playlist as current local workspace
- preserve exact selections and avoid unnecessary search.list
- phone-test local YTM Project save/reopen after account import
'''
new_focus = '''v1.4.19 focus:
- choose a device folder with Android's folder picker
- read all connected-account playlists without modifying YouTube/YTM
- export one YTM Project per non-empty accessible playlist
- preserve source playlist id, privacy and exact videoId values
- write manifest.json with success/skip/failure status per playlist
- keep empty/inaccessible playlists documented in the manifest
'''
if new_focus not in status:
    if old_focus not in status:
        raise SystemExit(
            "STOP: PROJECT_STATUS v1.4.18 focus anchor missing"
        )
    status = status.replace(old_focus, new_focus, 1)

write("PROJECT_STATUS.txt", status)
print("PATCH: PROJECT_STATUS.txt")

backlog = read("BACKLOG.md")
backlog = backlog.replace(
    "## Current\nv1.4.18 — YTM account library import/export, G01 one-playlist import",
    "## Current\nv1.4.19 — export all connected-account playlists to a device folder",
    1
)
backlog = backlog.replace(
    "## v1.4.18\n",
    "## v1.4.18\n",
    1
)
backlog = backlog.replace(
    "- [ ] GitHub build\n- [x] phone test: account playlist list/picker",
    "- [x] GitHub build\n- [x] phone test: account playlist list/picker",
    1
)
backlog = backlog.replace(
    "- [ ] next wave: export all account playlists to a chosen folder",
    "- [x] next wave moved to v1.4.19: export all account playlists to a chosen folder",
    1
)

v1419_section = '''## v1.4.19
- [x] choose export destination with Android folder picker
- [x] create timestamped export session folder
- [x] list all playlists from connected account
- [x] export one YTM Project per non-empty accessible playlist
- [x] preserve source playlist id/privacy/exact videoId
- [x] write manifest.json with per-playlist status
- [x] skip empty/no-accessible-track playlists but record them in manifest
- [x] static audit + phone-test plan
- [ ] GitHub build
- [ ] phone test: choose export folder
- [ ] phone test: export small account library
- [ ] verify project-file count and manifest
- [ ] reopen one exported YTM Project
- [ ] verify source playlists remain unchanged

'''
if "## v1.4.19\n" not in backlog:
    marker = "## Next\n"
    if marker not in backlog:
        raise SystemExit("STOP: BACKLOG Next anchor missing")
    backlog = backlog.replace(
        marker,
        v1419_section + marker,
        1
    )

backlog = backlog.replace(
    '''## Next
After G01 phone verification:
continue YTM account library work — verify local-project reuse first,
then add read-only export of all account playlists to a chosen device folder.
''',
    '''## Next
Phone-test v1.4.19 bulk account export, then decide whether to add:
- selective multi-playlist export;
- import of a bulk-export manifest;
- incremental/sync-style account backup.
''',
    1
)

write("BACKLOG.md", backlog)
print("PATCH: BACKLOG.md")

changelog_entry = '''## v1.4.19
- Added read-only bulk export of connected-account playlists to a user-selected device folder.
- Android folder picker creates a timestamped export session folder.
- Each non-empty accessible playlist is saved as a YTM Project with exact videoId values.
- Account exports preserve source playlist id and privacy metadata.
- `manifest.json` records every account playlist, export status, file name, source/exported counts and request count.
- Empty or no-accessible-track playlists are skipped as project files but remain documented in the manifest.
- No remote playlist write API is used by the bulk export flow.
- versionCode 53 / versionName 1.4.19.
- v1.4.19 = NOT PHONE-TESTED YET.

'''

replace_once(
    "CHANGELOG.md",
    "# Журнал змін (Changelog)\n\n",
    "# Журнал змін (Changelog)\n\n" + changelog_entry,
)

plan = read("docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md")
old_pending = '''Still pending:
- phone verification;
- save/reopen verification as YTM Project;
- export all account playlists to a chosen folder.
'''
new_pending = '''G01 phone verification:
- account picker/import PASS;
- exact-videoId Review path PASS;
- YTM Project save/reopen PASS.

## v1.4.19 — bulk account export
Implemented scope:
- choose a device folder;
- create a timestamped export session directory;
- export one YTM Project per non-empty accessible account playlist;
- preserve source playlist id/privacy and exact videoId values;
- write `manifest.json` with success/skip/failure data;
- keep the source account read-only.

Phone verification is still required.
'''
if new_pending not in plan:
    if old_pending not in plan:
        raise SystemExit(
            "STOP: account-library plan pending anchor missing"
        )
    plan = plan.replace(old_pending, new_pending, 1)
    write(
        "docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md",
        plan
    )
    print(
        "PATCH: docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md"
    )

print()
print("PASS: v1.4.19 bulk account-export patch applied")
