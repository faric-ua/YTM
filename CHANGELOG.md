# Журнал змін (Changelog)

## v1.4.20
- Fixed clipped text on long Import action buttons by replacing fixed height with WRAP_CONTENT + minimum height.
- Added controlled autosizing and comfortable vertical padding for Import action buttons.
- Added 10dp spacing between `Вибрати плейлист з YTM` and `Експортувати всі плейлисти в папку`.
- Recorded v1.4.19 bulk-export phone test: 21/21 projects exported, manifest matched, round-trip project reopen passed.
- versionCode 54 / versionName 1.4.20.
- v1.4.20 phone UI smoke PASS: account action labels/spacing verified, bulk-export folder picker opens; BUG-003 in-place update recovery retest PASS.

## v1.4.19
- Added read-only bulk export of connected-account playlists to a user-selected device folder.
- Android folder picker creates a timestamped export session folder.
- Each non-empty accessible playlist is saved as a YTM Project with exact videoId values.
- Account exports preserve source playlist id and privacy metadata.
- `manifest.json` records every account playlist, export status, file name, source/exported counts and request count.
- Empty or no-accessible-track playlists are skipped as project files but remain documented in the manifest.
- No remote playlist write API is used by the bulk export flow.
- versionCode 53 / versionName 1.4.19.
- v1.4.19 phone test PASS for tested bulk-export path: 21/21 projects exported, manifest counts matched, and one exported project reopened with exact videoId preserved.

## v1.4.18
- G01: added read-only import of one playlist from the connected YouTube/YTM account.
- Import screen can list account playlists, select one and load ordered playlist items.
- Imported account tracks keep exact YouTube videoId and open as the current local workspace.
- Exact imported selections are marked MATCHED, so Step 3 can open Review without search.list.
- Playlist source is read-only; no account playlist is modified during import.
- Added v1.4.18 G01 static audit, release docs and phone-test plan.
- v1.4.17 FAST_FLOW existing-target duplicate/rotation path recorded as phone-tested PASS; unrelated auth cases remain open/retest.
- versionCode 52 / versionName 1.4.18.
- v1.4.18 G01 phone test PASS: account picker/import, 3/3 exact videoId Review path, and YTM Project save/reopen verified on 2026-09-17.

## v1.4.17
- Step 2 auth-ready state is invalidated after YouTube API HTTP 401.
- Added direct `Далі → Створити / додати` action from Review to Destination.
- Playlist result modal uses clearer vertical action hierarchy.
- GitHub Actions APK/artifact names now follow `versionName`.
- Added public QA screenshot redaction/blur policy.
- Added `TERMUX_COMMANDS.md` with the phone/Termux development workflow.
- Added design plan for importing/exporting playlists from the connected YTM account.
- BUG-003 and BUG-004 require real-phone retest.
- versionCode 51 / versionName 1.4.17.

## v1.4.16
- Cleanup Wave 5: extracted destination playlist / duplicate orchestration into `DestinationCoordinator`.
- DestinationCoordinator now owns eligible-track selection, destination playlist caching/selection,
  existing-playlist scan quota accounting, exact-videoId duplicate analysis and duplicate write planning.
- MainActivity remains the auth/UI/executor bridge and hands the final write plan to PlaylistWriteCoordinator.
- Added `scripts/destination-coordinator-audit.sh`.
- Added immutable `docs/v.1.4.16/` release documentation, diagrams and QA snapshot.
- BUG-003/Q-003 auth recovery failure remains deferred and is not claimed fixed.
- v1.4.16 = NOT TESTED YET.
- versionCode 50 / versionName 1.4.16.

## v1.4.15
- Recorded v1.4.14 auth recovery phone-test failure as BUG-003 / Q-003.
- v1.4.14 status = PARTIALLY PHONE-TESTED — FAIL.
- Added `qa/BUG_REGISTER.md`.
- Started immutable per-release QA snapshots under `docs/v.X.Y.Z/qa/`.
- Extracted playlist create/append write loop into PlaylistWriteCoordinator.
- Moved PendingJob construction/conversion and write quota lifecycle out of MainActivity.
- MainActivity: 3685 → 3478 lines.
- v1.4.15 = NOT TESTED YET.
- versionCode 49 / versionName 1.4.15.

## v1.4.14
- Added silent Google/YTM authorization recovery after process restart and in-place APK update.
- Added non-secret `PersistentAuthStateStore` marker; OAuth token remains memory-only.
- Silent restore never auto-launches a required Google resolution; Step 2 asks the user when interaction is needed.
- Removed the large inline playlist result frame from Home.
- Create/append completion now uses a modal result with Open / Copy / Close.
- Added global QA system under `qa/`.
- Added `auth-persistence-audit.sh`, `result-modal-audit.sh`, `qa-plan-audit.sh`.
- v1.4.12 remains NOT TESTED.
- v1.4.13 marked PARTIALLY PHONE-TESTED.
- v1.4.14 starts NOT TESTED YET.
- Q-002 remains DEFERRED.
- versionCode 48 / versionName 1.4.14.

## v1.4.13
- Marked v1.4.12 explicitly **NOT TESTED** in `RELEASE_TEST_STATUS.md`.
- Added mutable release test-status register without rewriting immutable old release docs.
- Cleanup Wave 3: extracted track-search domain into `SearchCoordinator`.
- SearchCoordinator now owns planning, cache/API selection, quota accounting,
  quota-stop behavior and automatic candidate/state application.
- MainActivity now keeps only search UI/auth/executor/result callbacks.
- Removed direct `api.search`, SearchCache get/put and search quota accounting from MainActivity.
- Added `scripts/search-coordinator-audit.sh`.
- MainActivity reduced from 3689 to 3620 lines.
- Q-002 remains DEFERRED BY USER.
- versionCode 47 / versionName 1.4.13.
- v1.4.13 itself is NOT TESTED YET.

## v1.4.12
- Cleanup Wave 2: removed obsolete duplicate UI flows from MainActivity.
- MainActivity reduced from 5668 to 3689 lines
  (1979 lines / 34.9% smaller).
- Removed legacy Main import/file/text/project UI; ImportActivity remains owner.
- Removed legacy Main candidate/manual-review dialogs; ReviewActivity remains owner.
- Removed legacy Main Pending details; PendingActivity remains owner.
- Removed legacy Main History details/actions; HistoryActivity remains owner.
- Removed legacy Main Data/export/backup/restore flow; DataActivity remains owner.
- Removed legacy Main Service/Diagnostics/SearchCache popup flow; ServiceActivity remains owner.
- Removed obsolete Main request codes/export state/LocalBackupManager dependency.
- Added `scripts/mainactivity-cleanup-audit.sh`.
- Preserved permissive `ACTION_OPEN_DOCUMENT` file picker in ImportActivity.
- Added Q-002 as DEFERRED by user; dialog motion is not a release blocker.
- versionCode 46 / versionName 1.4.12.

## v1.4.11
- Analyzed v1.4.10 phone video frame-by-frame.
- Identified remaining dialog motion as AlertDialog Window animation.
- Disabled WindowManager animations for UiChrome custom dialogs.
- Kept TOP anchor, safe insets, hidden provisional content and scrolling.
- Added `scripts/dialog-animation-audit.sh`.
- versionCode 45 / versionName 1.4.11.

## v1.4.10
- Fixed Step 2 vertical displacement after screen rotation.
- Disabled baseline alignment in Step 1/2 and Step 3/4 button rows.
- Hardened other horizontal action rows against auto-size baseline shifts.
- Removed height-dependent vertical centering from custom dialogs.
- All UiChrome custom dialogs are now TOP anchored.
- Preserved safe system-bar/cutout/bottom insets and scrolling.
- Added `scripts/rotation-layout-audit.sh`.
- Updated `scripts/dialog-bounds-audit.sh`.
- versionCode 44 / versionName 1.4.10.

## v1.4.9
- Fixed Google/YTM Step 2 state loss after screen rotation.
- Added process-memory-only AuthSessionStore.
- OAuth token remains non-persistent.
- Reloads incomplete account identity after recreation.
- Fixed visible custom-dialog center-to-top snap.
- Added configuration-state audit and strengthened dialog-bounds audit.
- versionCode 43 / versionName 1.4.9.

## v1.4.8
- Fixed top clipping in tall custom dialogs.
- Quota and Problem Tracks now start inside the visible safe viewport.
- Added system-bar and display-cutout handling to all UiChrome custom dialogs.
- Short custom dialogs remain vertically centered.
- Tall custom dialogs start at the safe top and scroll normally.
- Added `scripts/dialog-bounds-audit.sh`.
- versionCode 42 / versionName 1.4.8.

## v1.4.7
- Problem tracks are rendered as individual cards.
- TikTok / Full text actions are stacked vertically with flat Close.
- Service submenus now stay inside ServiceActivity.
- Back from Service detail returns to Service home, not Main.
- Quick Start / Privacy / Diagnostics / SearchCache / About are structured pages.
- Diagnostics TXT save/share moved into ServiceActivity.
- Added reusable UiChrome record-dialog template.
- versionCode 41 / versionName 1.4.7.

## v1.4.6
- Action hierarchy + dedicated Service screen.
