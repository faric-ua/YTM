# YTM Importer — BUG REGISTER

| ID | Status | Severity | Description | Related tests |
|---|---|---:|---|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. | F-06 |
| BUG-002 / Q-002 | FIX IMPLEMENTED — FULL MODAL PHONE RETEST NEEDED v1.4.34 | P2 | v1.4.32 fixed the tested incremental preflight entrance, but quota and other modal windows remained inconsistent. v1.4.33 unified the runtime modal pipeline; v1.4.34 carries that implementation forward unchanged for phone QA. | M-02; v1.4.32 partial PASS → v1.4.33 unified fix → v1.4.34 retest |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update verified: Step 2 briefly gray, then automatically green. | A-03, D-03 |
| BUG-004 / Q-004 | R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED | P1 | R3 keeps prior Search/write invalidation safety, but first attempts silent Google token recovery after live HTTP 401 and retries the exact failed request once. Manual disconnected/red fallback remains only when silent recovery cannot succeed. | B-01; v1.4.30 repro → v1.4.41 invalidation repair → v1.4.47-R3 silent recovery/retry |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Ordinary repeat-search preserves canonical exact videoId tracks; real-phone search plan confirmed 0 redundant search.list for exact 3/3. | v1.4.26 repro → v1.4.27 PASS |
| BUG-006 / Q-006 | CLOSED — PHONE RETEST PASS v1.4.29 R2 | P2 | Incremental-delta boundary explanation was truncated as a Toast; R2 replaced it with a readable UiChrome dialog and phone retest passed. | v1.4.29 repro → v1.4.29 R2 PASS |
| BUG-007 / Q-007 | CLOSED — PHONE RETEST PASS v1.4.30 R2 | P3 | Timestamp-first folder naming is readable in portrait; R2 one-word `Створити` keeps both preview actions single-line and equal-height. | v1.4.30 repro → R1 naming PASS → R2 button PASS |
| BUG-008 / Q-008 | CLOSED — PHONE RETEST PASS v1.4.38 R1 | P2 | Restore confirmation now survives phone rotation without forcing the user to choose the backup file again. | v1.4.38 phone repro → R1 rotation-state fix → phone PASS |
| BUG-009 / Q-009 | PHONE PORTRAIT PASS v1.4.41 | P3 | Real-phone portrait confirms readable profile copy, single-line `Змінити` on the left and `Закрити` on the right. Rotation itself exposed separate BUG-011 (dialog disappears), so BUG-009 remains a portrait visual-fit PASS rather than absorbing the state-restoration defect. | v1.4.40 account-switch screenshot → v1.4.41 portrait PASS |
| BUG-010 / Q-010 | CLOSED — PHONE RETEST PASS v1.4.41 | P2 | Real-phone full Restore and subsequent `Відкотити` both preserved the live quota exactly: Search 0/100; total 505/10000; ≈9495 remaining. Full Restore applied 3 of 4 backup groups; `Відкотити` applied 4 of 5 safety-snapshot groups, confirming `quota_tracker_v1` was excluded from both paths. | v1.4.40 finding → v1.4.41 Restore + `Відкотити` PASS |
| BUG-011 / Q-011 | CLOSED — PHONE RETEST PASS v1.4.41-R1 | P3 | Real-phone R1 retest passed: Account modal remains/reappears through portrait ↔ landscape Activity recreation and preserves the expected action layout. | v1.4.41 repro → v1.4.41-R1 phone PASS |
| BUG-012 / Q-012 | CLOSED — PHONE RETEST PASS v1.4.42-R1 | P2 | All files access is recognized; direct Download newest-first list works; direct House Dance import succeeds; Restore JSON reaches confirmation; system-picker fallback/return passes. | v1.4.42 repro → v1.4.42-R1 phone PASS |
| BUG-013 / Q-013 | PARTIAL PHONE QA — STARTUP RECOVERY OBSERVED / STALE-TOKEN RETEST DEFERRED | P1 | v1.4.43 is installed and startup silent Google/YTM recovery was observed. R3 additionally handles a live HTTP 401 with silent replacement-token recovery + one retry, but the naturally aged-token phone acceptance case remains deferred until reproducible. | v1.4.42-R1 repro → v1.4.43 partial phone evidence → R3 recovery hardening |
| BUG-021 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED | P2 | History no longer uses universal `Додано X/Y` for clean non-write records; primary wording becomes `Додано в YTM`, `Імпортовано`, or `Відновлено` according to operation evidence. | v1.4.47-R3 History semantics |
| BUG-022 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED | P2 | Selector/Recent-file/Storage Help windows preserve open state through Activity recreation/rotation without side effects. | v1.4.47-R3 lifecycle Wave 1 |
| BUG-023 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED | P2 | `Поточний YTM Project` action modal preserves open state/context through rotation; Save/Share never auto-run. | v1.4.47-R3 lifecycle Wave 1 |
| BUG-027 | CLOSED — PHONE RETEST PASS v1.4.47-R3 | P2 | Existing-playlist delete confirmation survives Activity recreation/rotation and never starts deletion until the user explicitly confirms. | v1.4.47-R3 stabilization checkpoint |
| BUG-028 | CLOSED — PHONE RETEST PASS v1.4.47-R3 | P2 | Existing-playlist search query and active filter survive Activity recreation/rotation. | v1.4.47-R3 stabilization checkpoint |
| BUG-029 | CLOSED FOR TESTED SCOPE — PHONE RETEST PASS v1.4.49 R1 | P2 | Updater correctly detected installed 1.4.49/92 as newer than stable 1.4.48/91. R1 renders the relation as informational `Оновлень немає`, keeps updater prose Ukrainian, preserves the result through rotation and returns Back to About. | signed run 35730023317 / 4d30672c700d2fc2a32255465f555c0fd64acdc3 / result 1+ |
| BUG-030 | OPEN — NON-BLOCKING DISTRIBUTION/REPUTATION FINDING | P2 | Google Play Protect displayed `Шкідливий додаток заблоковано` while sideloading the exact v1.4.49 final RC. The user explicitly chose `Усе одно встановити`; installation and all final functional updater checks then passed. Treat as a Play Protect/reputation/false-positive follow-up, not evidence that the updater flow itself failed. Do not solve by globally disabling Play Protect. | v1.4.49 final RC / conversation screenshot / FINAL+ |
| BUG-031 | CLOSED — PHONE RETEST PASS v1.4.50 R1 | P2 | Changing Skin in Menu previously left most of Home on the old Skin. R1 moves Skin-change detection into AppThemeManager and keeps a one-line MainActivity resume guard; full Home Neon/Blue/Green refresh passed on phone. | run `35782627453` / source `81d5ebd988d08d3ddb80d78b73fd94e20280c980` / `R1-1+` |
| BUG-032 | CLOSED — PHONE RETEST PASS v1.4.50 R1 | P2 | History → Очистити confirmation previously disappeared on rotation. R1 restores it without auto-clearing History; rotation and Cancel/no-op paths passed on phone. | run `35782627453` / source `81d5ebd988d08d3ddb80d78b73fd94e20280c980` / `R1-2+`, `R1-3+` |
| BUG-033 | CLOSED — PHONE RETEST PASS v1.4.50 WAVE 3 | P2 | DataActivity modal windows previously shared UiChrome rendering without shared lifecycle ownership. Wave 3 routes all 11 Data modal states through RestorableModalController; ordinary, picker-gating, share and prepared History confirmation rotation paths passed on phone without automatic actions. | run `35796094108` / source `7e6fcb482387be92a7de54db0f4df5081d640495` / `W3-1+..W3-4+` |
| BUG-034 | CLOSED — PHONE RETEST PASS v1.4.50 WAVE 3 R2 | P2 | Result and rollback-confirm modals previously disappeared on rotation. R2 makes OnDismiss transient-only and moves semantic close to explicit button/OnCancel events; repeated result/rollback rotation and single-transition tests passed on phone. | run `35802968056` / source `66d06d6912d014efb3a98d317ed49355a5fa3078` / `W3R2-1+..W3R2-4+` |

## BUG-002 current evidence

Real-phone reconfirmation on v1.4.27 (2026-09-17):

- the custom dialog first appears offset from its final stable position;
- it then visibly shifts/settles into the final position;
- this is the same long-standing BUG-002 / Q-002 behavior, not a new v1.4.27 regression;
- previous fix attempts did not fully solve it on the real device;
- the user explicitly chose to defer it again and continue feature development.

Evidence:
`docs/issues/BUG-002/evidence/BUG002-dialog-entrance-motion-v1.4.27-2026-09-17.mp4`

v1.4.31 phone reconfirmation:
- the incremental backup preflight still visibly appeared and then shifted upward;
- the user explicitly asked to reopen the issue instead of deferring it further.

v1.4.32 implementation:
- UiChrome custom Menu/Message/Record dialogs use a dedicated `Dialog`, not an AlertDialog custom-view panel;
- the full-screen transparent Window is configured before `show()`;
- there is no post-show geometry correction;
- safe insets are applied while content is hidden;
- content becomes visible only at pre-draw after final inset padding is in place.

Current status:
**FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.32.**


v1.4.32 phone result: the incremental backup preflight is stable, while the quota modal and other modal windows still behave inconsistently.

v1.4.33 unifies 22 direct UiChrome modal call sites and 22 legacy `UiChrome.alertBuilder(...)` call sites through the same stable custom Dialog engine. Native `AlertDialog.Builder` runtime behavior is removed. The whole attached decor stays hidden until safe insets and repeated geometry measurements stabilize.

v1.4.34 carries the v1.4.33 modal engine forward unchanged while fixing a separate secondary-screen back-button alignment issue. BUG-002 remains gated on representative real-phone modal evidence.

Current status: **FIX IMPLEMENTED — FULL MODAL PHONE RETEST NEEDED v1.4.34.**

## BUG-003 reproduction

Preconditions:
- v1.4.13 authorized;
- install v1.4.14 over it without uninstall.

Steps:
1. Launch v1.4.14.
2. Do not press Step 2.
3. Wait for automatic session recovery.

Actual:
- Step 2 remains red.

Expected:
- previously granted account/session should recover automatically when Google permits it.

Retest result:
- successful in-place update phone test completed on v1.4.20;
- Step 2 briefly showed gray while silent recovery ran;
- Step 2 automatically returned to green without manual re-authorization;
- BUG-003 is closed for this recovery scenario.

## Per-release rule

Every release from v1.4.15 onward contains a snapshot of this register under:
`docs/v.X.Y.Z/qa/BUG_REGISTER.md`.

## BUG-004 reproduction

Actual:
- Step 2 can still display green `Google / YTM ✓`;
- API reports that Google authorization is no longer valid;
- user must authorize again.

Expected:
- HTTP 401 invalidates the in-memory ready state;
- Step 2 immediately stops showing green/ready;
- imported playlist and search selections remain intact.

v1.4.17 added HTTP 401 invalidation, but v1.4.30 phone QA reproduced the stale-ready state again.

v1.4.30 evidence:
- account API returned a real HTTP 401 invalid-authentication response;
- Home still showed green/checked `2. Google / YTM ✓` and connected status after the 401;
- the user had to reauthorize before backup API reads worked again;
- another 401 occurred later in the same delta-status QA wave.

Implemented in v1.4.31:
- ImportActivity recognizes YouTube API HTTP 401 and clears process-memory auth plus the prior-auth marker;
- account export/incremental per-playlist loops rethrow 401 instead of converting it into ordinary FAILED playlist records;
- MainActivity syncs a cleared shared session on resume so Step 2 cannot remain green after returning from Import;
- the local working playlist is not cleared;
- the 401 dialog offers a direct return to Step 2.

Status: **FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.41.**

## BUG-005 reproduction

Preconditions:
- reopen/import a YTM Project with exact videoId already present;
- phone evidence used `top 3`, exact videoId 3/3.

Steps:
1. Confirm Home/Review show all tracks ready.
2. Open manual `Пошук`.
3. Inspect the search plan.
4. Do not press `Почати`.

Actual:
- search required for all 3 tracks;
- 3 new `search.list` requests proposed.

Expected:
- exact-videoId tracks are excluded from search planning;
- 3/3 exact tracks should require 0 new search.list requests.

Impact:
- can waste limited search quota if the user explicitly starts the redundant search;
- no quota was wasted in the recorded reproduction because `Почати` was not pressed.

Implemented fix:
- v1.4.27 Exact-ID Search Guard.

Phone retest result (2026-09-17):
- signed v1.4.27 installed successfully;
- Home showed `top 3` with 3 tracks, 3 ready/exact, 0 missing/problem;
- Review showed 3/3 ready;
- ordinary repeat-search plan showed search required 0;
- ordinary repeat-search plan showed new `search.list` 0;
- BUG-005 / Q-005 closed for the tested path.


## BUG-008 reproduction

Preconditions:
- v1.4.38;
- open Data → Restore;
- select a valid `YTM_Backup_*.json`.

Steps:
1. Wait for the `Підтвердити Restore` dialog that shows schema/version/date/group information.
2. Rotate the phone before pressing Restore or Cancel.

Actual on v1.4.38:
- the confirmation disappears;
- the selected file context is lost;
- the user must reopen the picker and find the backup again.

Expected:
- pending Restore confirmation survives Activity recreation;
- the same already-validated backup information is shown again;
- no second system file selection is required.

Implemented in v1.4.38 R1:
- validated pending backup raw data is copied to app cache;
- only a small pending-state flag is stored in instance state;
- after rotation, DataActivity re-reads the cached pending backup and recreates the confirmation;
- explicit Cancel / Restore clears the temporary cached file;
- the backup itself is not placed into the Android saved-state Bundle.

Status: **CLOSED — PHONE RETEST PASS v1.4.38 R1.**


## BUG-004 v1.4.40 Search-path reproduction

Real-phone reproduction on 2026-09-19 using
`docs/test-data/collections/House_Dance_Hit_2000/House_Dance_Hit_2000_Vol1_YTM.txt`:

- TXT import loaded 9 tracks successfully;
- Home still displayed green/checked `2. Google / YTM ✓`;
- Search attempted 9 new API searches with 0 cache hits;
- all 9 tracks ended in error;
- Review showed 0 ready / 9 problems;
- affected tracks reported:
  `Авторизація Google більше не дійсна. Відкрийте «2. Акаунт» і увійдіть знову.`;
- the imported 9-track workspace remained present.

The user then re-entered the Google account flow and later fully restarted the app.
The House Dance workspace still appeared broken because its nine auth-failed tracks
had already been persisted as `TrackStatus.FAILED` with the old authorization error.

The user then restored the latest full local backup. That restore loaded a different
current playlist (`top 3`). Afterward:
- cached search/match state was usable;
- all three tracks were ready;
- YTM Importer successfully created a **new private YouTube/YTM playlist** and added
  all three tracks.

Important correction:
- `LocalBackupManager` does **not** back up `auth_state_v1`;
- it explicitly does not contain OAuth access tokens;
- therefore backup restore did not restore authorization;
- the successful remote playlist creation proves authorization was already usable
  again by that point.

Code-path inspection after the reproduction:
- `SearchCoordinator.run(...)` catches per-track exceptions and converts them to
  `TrackStatus.FAILED` plus a user-facing error;
- it has a quota callback but no equivalent authorization-invalid callback;
- therefore an auth failure can be consumed inside SearchCoordinator without clearing
  shared authorization state or forcing Main Step 2 out of green;
- `CurrentPlaylistStore` persists each track's `status` and `error`, so auth-failed
  rows survive process restart;
- successful re-authorization does not currently clear/reset those stale auth-failed
  track states automatically.

BUG-004 therefore has two connected repair targets:
1. propagate Search-path HTTP 401/auth invalidation to shared auth state immediately;
2. after successful re-authorization, recover or clearly reset only tracks that failed
   because of invalid authorization, without destroying the imported workspace.

The earlier v1.4.31 ImportActivity invalidation implementation must not be treated as
complete coverage.

### v1.4.41 BUG-004 implementation

- SearchCoordinator detects HTTP 401 separately from ordinary per-track errors.
- The first auth failure stops the search loop immediately.
- The currently searching track returns to `NEW` with no persisted auth error.
- Remaining tracks are not converted into duplicate auth failures.
- MainActivity receives `onAuthorizationInvalidated` and clears shared/persistent ready state through the existing centralized invalidation path.
- Review is not auto-opened after an auth-invalidated Search.
- After a successful re-login, old persisted v1.4.40 auth-failed rows are repaired back to a retryable/reviewable state.

Phone acceptance still requires a real/reproduced auth failure.

## BUG-009 — Google account-switch copy/action fit

Status: **PHONE PORTRAIT PASS v1.4.41.** Rotation continuity is tracked separately as BUG-011.

Observed on the real phone while changing the connected Google account:

- explanatory text and the account-switch action do not compose cleanly at the tested
  phone width;
- this is a UI/copy-fit issue, separate from BUG-004 authorization-state logic;
- functional account switching is not declared broken solely from this visual finding.

Keep BUG-009 separate so a future auth-state fix cannot accidentally close the UI issue.

### v1.4.41 BUG-009 implementation

- account action shortened from `Змінити акаунт` to `Змінити`;
- explanatory copy shortened to `Плейлисти створюватимуться в цьому YouTube/YTM профілі.`;
- shared horizontal modal policy keeps the action on the left and Close/Cancel on the right.

### v1.4.41 BUG-009 phone result

Portrait real-phone evidence confirms:
- explanatory copy is readable;
- `Змінити` fits on one line;
- `Змінити` is left;
- `Закрити` is right;
- no obvious height/fit problem remains in portrait.

Rotation was exercised and exposed a separate state-continuity defect: the Account modal disappears when the Activity is recreated. That issue is tracked as BUG-011 and does not invalidate the portrait layout PASS recorded here.


## BUG-010 — Restore rewinds local quota estimate

Status: **CLOSED — PHONE RETEST PASS v1.4.41.**

Observed during the House Dance recovery run:

- before the successful second Search, the user restored an older full local backup;
- the subsequent Search plan showed local search usage back at `0/100`;
- `LocalBackupManager.PREFS_NAMES` includes `quota_tracker_v1`;
- full Restore therefore replaces the current local quota tracker with the value from
  the backup snapshot.

This does not change Google Cloud's real quota. It only rewinds YTM Importer's local
estimate, which can make the UI overestimate remaining search capacity after restoring
an older backup.

Expected direction:
- backup/restore should preserve useful app state without pretending external API quota
  usage moved backward in time;
- decide whether `quota_tracker_v1` should be excluded from ordinary Restore, merged
  conservatively, or reset with an explicit warning;
- keep safety/rollback semantics documented before changing behavior.

Implemented policy in v1.4.41:

- `quota_tracker_v1` remains inside exported full backup JSON for diagnostics and backward compatibility;
- ordinary full Restore does not apply that preference group;
- safety-snapshot rollback also leaves the live quota tracker untouched;
- Data/Restore copy explicitly tells the user that the local quota estimate is preserved.

### v1.4.41 BUG-010 implementation

This prevents an old local backup from making the app claim that externally consumed API quota became available again.

### v1.4.41 BUG-010 phone result

Ordinary full Restore passed on the real phone.

Before Restore:
- Search `0/100`;
- total local quota `505/10000`;
- remaining units `≈9495`.

The selected legacy backup reported 4 data groups. Restore reported 3 restored groups
and 33 restored values. The confirmation/result copy both stated that local quota
estimate would remain current.

After Restore the quota screen still showed:
- Search `0/100`;
- total local quota `505/10000`;
- remaining units `≈9495`.

This is direct phone evidence that ordinary full Restore no longer rewinds
`quota_tracker_v1`.

Safety-snapshot rollback path has now also been exercised on phone:
- snapshot: 5 groups / 117 values;
- rollback applied: 4 groups / 113 values;
- confirmation/result both state local quota estimate remains current;
- local state before Restore was returned.

Final quota-screen readback after `Відкотити` confirmed the same values: Search `0/100`, total local quota `505/10000`, remaining units `≈9495`. BUG-010 is closed for both ordinary full Restore and return-to-pre-Restore state (`Відкотити`).

Do not close this from static reasoning alone; verify the chosen policy on phone.


## BUG-011 — Account modal disappears on rotation

Status: **CLOSED — PHONE RETEST PASS v1.4.41-R1.**

Path:
`Home → 2. Google / YTM → Account modal → rotate phone`

Real-phone result:
- Account modal opens correctly in portrait;
- rotate portrait → landscape;
- Activity recreates and the Account modal disappears;
- the underlying Home screen remains;
- account information itself is not reported lost; this is dialog/UI-state continuity.

Expected:
- if the Account modal was open before rotation, recreate/reopen the same modal after
  Activity recreation;
- preserve the same safe action order: `Змінити` left, `Закрити` right.

Code finding from installed v1.4.41:
- `MainActivity.showAccountDialog()` built/shows the modal directly;
- there was no saved-instance-state flag/pending modal marker;
- therefore normal Activity recreation dismissed it.

Follow-up implementation on the active branch:
- stores `accountDialogOpen` in `onSaveInstanceState`;
- restores the flag in `onCreate`;
- reposts `showAccountDialog()` after Activity recreation;
- resets the flag when the dialog is dismissed.

Keep BUG-011 separate from BUG-009:
- BUG-009 = portrait copy/button fit;
- BUG-011 = modal visibility/state across rotation.


### v1.4.41-R1 BUG-011 phone result

Path:
`Home → 2. Google / YTM → Account → rotate portrait ↔ landscape`

Result:
**PASS.**

The Account modal remains/reappears after rotation instead of disappearing.
BUG-011 is closed on v1.4.41-R1.


## BUG-012 — Recent-file selector cannot grant root Download

Status: **CLOSED — PHONE RETEST PASS v1.4.42-R1.**

Path:
`Home → 1. Імпорт → імпортувати файл → Додати папку… → Download`

Phone result:
- v1.4.42 RecentFileChooserActivity opens correctly;
- attempting to use root Download as the remembered SAF tree is blocked;
- Android app-permissions settings show no ordinary storage permission to grant.

Platform constraint:
- Android 11+ does not allow `ACTION_OPEN_DOCUMENT_TREE` to grant access to root
  `Download`;
- therefore this is not solved by asking for a normal runtime storage permission.

Product decision after the reproduction:
- the project owner explicitly chose Android All files access for the sideload build;
- v1.4.42-R1 declares `MANAGE_EXTERNAL_STORAGE`;
- the selector shows an in-app rationale before opening Android special-access settings;
- after the user enables access, Download is read directly and sorted by `lastModified` newest-first;
- direct files are returned through the app's non-exported FileProvider;
- SAF subfolders and `ACTION_OPEN_DOCUMENT` remain fallbacks;
- phone retest is required before BUG-012 can close.

Distribution note:
- Google Play treats MANAGE_EXTERNAL_STORAGE as restricted/high-risk; future Play
  distribution would require a separate policy/eligibility review.

### v1.4.42-R1 phone evidence

PASS:
- rationale dialog displayed;
- app recognized the All files access grant after return;
- grant action disappeared;
- direct Download list loaded 47 matching files;
- visible newest-first ordering passed: 14:50 entries above 14:47;
- standard Android system picker opened.

Important scope clarification:
- MANAGE_EXTERNAL_STORAGE itself is broad shared-storage access;
- YTM Importer narrows what it displays/uses in each selector;
- Import currently filters to TXT/CSV/JSON;
- Data/Restore filters to JSON.

Final v1.4.42-R1 phone acceptance:
- direct House Dance TXT import PASS: exact title `House Dance Hit 2000 Vol.1`, 9 tracks;
- Restore JSON selection PASS: valid backup reaches `Підтвердити Restore`;
- system-picker fallback and return PASS;
- BUG-012 closed.

Separate UX observation:
- landscape footer actions consume most vertical space;
- tracked as UX-021, not as BUG-012 functional failure.


## BUG-013 — Green auth state can outlive token validity

Status: **PARTIAL PHONE QA — STARTUP RECOVERY OBSERVED / STALE-TOKEN RETEST DEFERRED.**

Phone reproduction:
- Home showed green `2. Google / YTM ✓`;
- user entered `4. Створити / додати`;
- new private-playlist path was initially reachable;
- switching to `додати в існуючий playlist` triggered a real YouTube API request;
- app then reported authorization required;
- Step 2 changed from green to red.

Code-path interpretation:
- `authorize()` skips a fresh Google authorization call when `accessToken` is already non-blank and account/channel identity is cached;
- therefore Home can remain green even if that token has expired or was revoked;
- `loadExistingPlaylistsForDestination()` then performs the first live YouTube request;
- on HTTP 401, `invalidateAuthorizationIfNeeded()` correctly clears accessToken/account/channel state, clears persistent ready state, and updates Step 2 to red.

What passed:
- destination-side 401 invalidation is working as intended;
- the misleading green state is cleared immediately after the real 401.

What remains broken:
- token freshness is not proactively validated/refreshed before remote destination/write operations;
- the first remote action can therefore fail even while Home still shows green.

Repair direction:
- centralize an `ensureFreshAuthorization` path before remote YouTube operations;
- do not trust only `accessToken != null` plus cached identity;
- prefer a silent Google authorization refresh/validation before destination list/create/write actions;
- preserve current 401 invalidation as the fallback;
- keep BUG-004 Search-path real-401 retest separate: this phone evidence is destination-side, not SearchCoordinator.

### v1.4.43 implementation

- removed the cached-token fast path from `MainActivity.authorize()`;
- each remote action routed through `authorize()` now asks Google AuthorizationClient
  for current authorization first;
- silent success updates the access token and can preserve already-known account/channel
  identity;
- refresh failure or a missing token clears local/persistent ready state so Step 2
  cannot remain misleadingly green;
- PlaylistWriteCoordinator now stops on HTTP 401 with an explicit
  `AuthorizationInvalidated` outcome;
- remaining write tracks stay PENDING/retryable and the pending job is preserved;
- MainActivity invalidates shared auth and tells the user the unfinished job remains in
  `Черга`.

Real-phone startup silent recovery has been observed. The decisive aged/stale-token retest remains required before BUG-013 closes and is deferred until that state occurs naturally.
