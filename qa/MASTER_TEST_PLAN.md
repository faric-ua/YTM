# YTM Importer — MASTER TEST PLAN

Version of test plan: 1.0  
Applies to: YTM Importer v1.4.x and later unless a release-specific checklist overrides a case.

This is the **global manual QA source of truth**. It is intentionally broader than
a single release checklist. Use it when doing a full regression before sharing the
APK with other people.

## 0. How to record a test run

For every case mark exactly one:

- `[PASS]` — actual behavior matches expected behavior.
- `[FAIL]` — behavior differs; add screenshot/video and notes.
- `[BLOCKED]` — cannot execute because another issue prevents it.
- `[SKIP]` — intentionally not executed; state why.

Recommended note format:

```text
Device:
Android:
App version:
Install type: clean / update over previous
Google account:
Network:
Date/time:
Case:
Result:
Notes:
Screenshot/video:
```

A release is **not PHONE TESTED** until its required cases are actually executed on
a real phone. Static audits and GitHub compilation do not count as phone testing.

---

# A. INSTALL / UPDATE / DATA PRESERVATION

## A-01 — Clean install
Priority: P0

Preconditions:
- app is not installed.

Steps:
1. Install signed APK.
2. Launch app.
3. Observe first screen.

Expected:
- app starts without crash;
- version badge is correct;
- no stale workspace/history appears;
- Step 1 requires import;
- Step 2 requires Google/YTM authorization;
- Step 3/4 are unavailable or marked required as appropriate.

## A-02 — In-place update
Priority: P0

Preconditions:
- previous signed version is installed;
- History, Queue, SearchCache, quota counters and current workspace contain data.

Steps:
1. Install newer signed APK **without uninstall**.
2. Launch app.
3. Open History, Queue, Data, Service.
4. Inspect current workspace.

Expected:
- application data is preserved;
- History remains;
- Queue remains;
- current workspace remains;
- SearchCache/quota data remain;
- app does not behave like a clean install.

## A-03 — Google/YTM session after update
Priority: P0

Preconditions:
- Google/YTM was authorized before update;
- update is installed over existing app data.

Steps:
1. Launch updated app.
2. Do not press Step 2.
3. Wait several seconds.

Expected:
- Step 2 first shows restoring state if needed;
- app attempts silent authorization;
- if Google still has a saved eligible account and grants, Step 2 becomes green automatically;
- no account picker/consent dialog appears in the normal successful restore case;
- if Google requires interaction, app asks user to press Step 2 rather than opening consent unexpectedly.

## A-04 — Uninstall/reinstall
Priority: P1

Steps:
1. Uninstall app.
2. Reinstall.
3. Launch.

Expected:
- local app data is gone;
- authorization may need to be established again;
- this is treated as a new installation, not an update.

---

# B. HOME SCREEN / PRIMARY ACTIONS

## B-01 — Four-step state machine
Priority: P0

Steps:
1. Launch with no import.
2. Import a playlist.
3. Authorize Google/YTM.
4. Search/review tracks.
5. Prepare destination.

Expected:
- button colors/states correspond to actual readiness;
- green = ready/completed;
- amber = attention/in progress;
- red = required/not ready;
- Step 2 does not become red merely because the Activity rotated or app was updated in-place.

## B-02 — Utility buttons
Priority: P1

Test:
- History;
- Queue;
- Quota;
- More.

Expected:
- every button opens the intended screen/dialog;
- returning does not reset workspace;
- labels remain readable in portrait and landscape.

## B-03 — Rotation
Priority: P1

Steps:
1. Rotate portrait → landscape → portrait on Home.
2. Repeat after import.
3. Repeat after account authorization.
4. Repeat after search.

Expected:
- no crash;
- account state stays logically correct;
- imported list stays;
- counts/status remain correct;
- no duplicate operation starts.

---

# C. IMPORT

## C-01 — TXT file
Priority: P0

Steps:
1. Step 1 → choose file.
2. Select TXT tracklist.
3. Confirm import.

Expected:
- Android ACTION_OPEN_DOCUMENT picker opens;
- picker remains permissive (`*/*`);
- playlist name/source are parsed;
- track count matches file;
- workspace is saved.

## C-02 — CSV file
Priority: P0

Expected:
- CSV rows parse correctly;
- artist/title are not swapped;
- blank/invalid rows are handled without crash.

## C-03 — Pasted text
Priority: P0

Steps:
1. Import → direct/pasted text.
2. Paste multi-line tracklist.
3. Set/confirm playlist name.

Expected:
- lines become tracks;
- playlist title is preserved;
- imported batch appears on Home.

## C-04 — YTM Project import
Priority: P0

Expected:
- project format/schema is recognized;
- exact resolved video IDs are restored;
- manual selections/provenance are preserved;
- already resolved Project tracks are not unnecessarily searched when preserve-exact mode applies.

## C-05 — Invalid/empty file
Priority: P1

Expected:
- readable error;
- no crash;
- previous workspace is not silently destroyed.

## C-06 — Cancel file picker
Priority: P2

Expected:
- app returns to Import screen;
- nothing is imported;
- existing workspace remains unchanged.

---

# D. GOOGLE / YTM AUTHORIZATION

## D-01 — First authorization
Priority: P0

Steps:
1. Fresh install.
2. Press Step 2.
3. Select Google account if asked.
4. Grant requested access.

Expected:
- access token is received;
- Google profile loads;
- YouTube channel loads;
- Step 2 becomes green;
- account dialog shows correct account/channel.

## D-02 — Silent recovery after normal app restart
Priority: P0

Steps:
1. Authorize once.
2. Force-close app.
3. Reopen.

Expected:
- app restores access without forcing account selection when Google can satisfy the prior grant silently;
- Step 2 returns to green after recovery.

## D-03 — Silent recovery after in-place update
Priority: P0

Expected:
- same as A-03;
- user does not repeat account selection just because APK version changed.

## D-04 — Google requires resolution
Priority: P1

Simulate by revoking grant or otherwise invalidating authorization.

Expected:
- automatic startup attempt does **not** force an unexpected popup;
- Step 2 indicates attention/required;
- pressing Step 2 launches proper Google flow.

## D-05 — Change account
Priority: P0

Steps:
1. Open Account.
2. Choose Change account.
3. Select another account.

Expected:
- new token/identity replace old session;
- YouTube channel is refreshed;
- writes go to the newly selected profile;
- Queue resume account guard still prevents writing a job created for a different account.

## D-06 — Token privacy
Priority: P0

Expected:
- OAuth access token is memory-only;
- access token is not in Full Backup;
- not in YTM Project;
- not in workspace;
- `auth_state_v1` stores only a boolean successful-authorization marker.

---

# E. SEARCH

## E-01 — Search plan
Priority: P0

Expected plan fields:
- total tracks;
- tracks needing search;
- cache hits;
- API searches needed;
- local quota estimate.

## E-02 — Initial search
Priority: P0

Expected:
- progress increments;
- SearchCoordinator uses SearchCache first;
- uncached tracks call YouTube search;
- candidates are scored;
- each track ends MATCHED / REVIEW / MISSING / FAILED as appropriate.

## E-03 — Cache repeat search
Priority: P0

Steps:
1. Search list once.
2. Search same list again.

Expected:
- cache count rises;
- cached tracks avoid new `search.list`;
- local cache-hit counter rises;
- result quality remains based on current scoring.

## E-04 — Manual selection preservation
Priority: P0

Steps:
1. Manually choose URL/candidate for a track.
2. Repeat search.

Expected:
- manual video ID is not overwritten;
- UI indicates manual selection was preserved.

## E-05 — Exact Project ID preservation
Priority: P0

Expected:
- exact resolved Project tracks remain unchanged when preserve-exact is requested.

## E-06 — Weak candidate
Priority: P1

Expected:
- low-score best candidate goes to REVIEW, not silently MATCHED.

## E-07 — No candidates
Priority: P1

Expected:
- track becomes MISSING;
- user can review/skip/manual replace.

## E-08 — Search quota exhausted
Priority: P0

Expected:
- quota error is recorded;
- one user warning is shown;
- cached remaining tracks may continue;
- uncached remaining tracks fail with readable quota reason;
- app does not loop API calls after quota is known exhausted.

---

# F. REVIEW / MANUAL CORRECTION

## F-01 — Review screen opens
Priority: P0

Expected:
- counts match current playlist;
- every track card represents current status.

## F-02 — Filters
Priority: P1

Test:
- All;
- Review;
- Ready;
- Problems.

Expected:
- correct subset;
- no track state is changed by filtering.

## F-03 — Candidate list
Priority: P0

Expected:
- candidate title/channel/video correspond to candidate;
- opening a candidate opens that exact video;
- selecting a candidate updates the current track.

## F-04 — Manual URL
Priority: P0

Steps:
1. Paste valid YouTube/YTM video URL.
2. Confirm.

Expected:
- video ID extracted;
- actual video metadata loaded;
- track becomes manual selection;
- repeat search does not replace it;
- opening review opens the manual video, not an old cached automatic candidate.

## F-05 — Invalid URL
Priority: P1

Expected:
- readable error;
- current valid selection remains intact.

## F-06 — Save YTM Project before export
Priority: P0

Expected:
- project can be saved while list is only in Review/current workspace;
- filename field defaults to current playlist name;
- exact/manual selections are serialized.

## F-07 — Share YTM Project
Priority: P1

Expected:
- Android share sheet opens;
- file contains project data only, not OAuth token/password/signing keys.

## F-08 — Repeat search
Priority: P1

Expected:
- returns through current search flow;
- current manual selections are protected.

---

# G. DESTINATION / CREATE / APPEND

## G-01 — New private playlist
Priority: P0

Expected:
- playlist created;
- privacy is private;
- selected tracks added;
- History entry updated.

## G-02 — New unlisted playlist
Priority: P1

Expected:
- correct privacy.

## G-03 — New public playlist
Priority: P1

Expected:
- correct privacy.

## G-04 — Existing playlist list
Priority: P0

Expected:
- user's playlists load;
- intended target can be selected.

## G-05 — Existing playlist duplicate scan
Priority: P0

Expected:
- existing video IDs detected;
- repeated IDs in the imported batch detected;
- user sees duplicate summary before write.

## G-06 — Skip duplicates
Priority: P0

Expected:
- duplicates are not inserted;
- saved write-quota estimate is correct;
- skipped/duplicate statuses are recorded.

## G-07 — Add duplicates anyway
Priority: P1

Expected:
- explicit user choice is honored.

## G-08 — Create/append with missing/problem tracks
Priority: P1

Expected:
- app clearly shows what will be written/skipped;
- no accidental null/invalid video IDs are inserted.

---

# H. WRITE / RESULT

## H-01 — Successful result modal
Priority: P0

After create/append completes:

Expected:
- result is shown as a **modal dialog**, not an inline frame on Home;
- title shows playlist name;
- details show added/failed/duplicate/privacy/operation info;
- YTM URL is visible/selectable;
- actions include `Відкрити в YTM`, `Копіювати`, `Закрити`.

## H-02 — Open in YTM
Priority: P0

Expected:
- YouTube Music app is preferred if available;
- correct playlist opens;
- browser fallback works when needed.

## H-03 — Copy link
Priority: P1

Expected:
- clipboard receives exact playlist URL;
- confirmation toast/status appears if implemented.

## H-04 — Close result
Priority: P0

Expected:
- modal closes;
- Home is usable;
- no large result frame remains in the tracklist;
- created playlist ID remains available to "open last playlist" actions where supported.

## H-05 — Partial write failure
Priority: P0

Expected:
- added/failed counts are accurate;
- remaining work can become Pending Queue where applicable;
- History status is PARTIAL/PENDING rather than falsely COMPLETED.

---

# I. PENDING QUEUE / RESUME

## I-01 — Queue creation on quota interruption
Priority: P0

Expected:
- remaining tracks saved;
- job includes destination/account/playlist context.

## I-02 — Queue list/detail
Priority: P1

Expected:
- job counts/status/details correct.

## I-03 — Resume same account
Priority: P0

Expected:
- resumes from remaining tracks only;
- does not re-add completed tracks;
- History syncs.

## I-04 — Resume different account
Priority: P0

Expected:
- blocked with clear account mismatch message;
- no write is attempted to wrong account.

## I-05 — Delete queue job
Priority: P1

Expected:
- local job removed only;
- remote playlist unchanged.

---

# J. HISTORY

## J-01 — Completed entry
Priority: P0

Expected:
- playlist name/date/status/counts correct;
- remote playlist ID/link present if available.

## J-02 — Failed/partial/pending entry
Priority: P1

Expected:
- status and counts reflect actual operation.

## J-03 — Search/filter History
Priority: P2

Expected:
- matches title/source/channel fields as designed.

## J-04 — History details/actions
Priority: P1

Test:
- copy summary;
- problem log;
- save/share YTM Project;
- delete local record.

Expected:
- local actions do not alter remote playlist unless explicitly designed.

## J-05 — Clear History
Priority: P1

Expected:
- confirmation is shown;
- History only is cleared;
- Queue and remote playlists are not changed.

---

# K. DATA / BACKUP / RESTORE

## K-01 — History TXT
Priority: P1

Expected:
- readable text export.

## K-02 — History JSON
Priority: P1

Expected:
- machine-readable export.

## K-03 — Pending JSON
Priority: P1

Expected:
- queue export matches current queue.

## K-04 — Full Backup
Priority: P0

Expected:
- schema/version metadata correct;
- SHA-256 integrity field present;
- supported local groups included;
- OAuth token/password/signing keys excluded.

## K-05 — Share Full Backup
Priority: P1

Expected:
- privacy warning appears before sharing.

## K-06 — Restore valid backup
Priority: P0

Expected:
- preview appears;
- integrity validates;
- safety snapshot is created first;
- local data restore succeeds.

## K-07 — Restore invalid/tampered backup
Priority: P0

Expected:
- rejected before destructive apply;
- current local data remains intact.

## K-08 — Rollback last Restore
Priority: P0

Expected:
- safety snapshot restores previous local state.

---

# L. SERVICE / DIAGNOSTICS / CACHE / ABOUT

## L-01 — Quick Start
Priority: P2

Expected:
- instructions match current four-step UI.

## L-02 — Privacy
Priority: P1

Expected:
- describes actual local/cloud data handling.

## L-03 — Diagnostics
Priority: P1

Expected:
- app version;
- account masked where appropriate;
- current import;
- quota;
- cache;
- local data;
- no OAuth access token.

## L-04 — Save Diagnostics TXT
Priority: P1

Expected:
- file is created;
- safe to share according to documented masking policy.

## L-05 — Share Diagnostics TXT
Priority: P1

Expected:
- share sheet works.

## L-06 — SearchCache stats
Priority: P1

Expected:
- total/valid/expired/malformed/size/date information is plausible.

## L-07 — Clear expired cache
Priority: P1

Expected:
- only expired items removed.

## L-08 — Clear entire cache
Priority: P1

Expected:
- confirmation;
- History/Queue/remote playlists remain untouched;
- future searches spend quota again.

## L-09 — About
Priority: P2

Expected:
- current app version;
- feature description matches current app;
- independence from Google/YouTube is clear.

---

# M. UI / UX / DEVICE BEHAVIOR

## M-01 — System insets
Priority: P1

Expected:
- no primary screen content under status bar/cutout/navigation gestures.

## M-02 — Long dialogs
Priority: P1

Expected:
- content scrolls;
- first/last actions reachable;
- no title/button is clipped.

Note:
- Q-002 custom-dialog entrance motion is currently DEFERRED and is not a release
  blocker unless reopened explicitly.

## M-03 — Long labels / narrow width
Priority: P1

Expected:
- button text remains readable;
- no broken single-letter wrapping;
- equal buttons remain equal height/alignment.

## M-04 — Orientation
Priority: P1

Expected:
- no account loss;
- no duplicated write/search;
- screens recover usable state.

## M-05 — Back navigation
Priority: P1

Expected:
- nested screens return to expected parent;
- Service submenu closes back to Service screen, not unexpectedly to Home.

---

# N. PRIVACY / SECURITY

## N-01 — No secrets in project archive
Priority: P0

Expected:
- no `.jks`;
- no `release-signing.properties`;
- no passwords;
- no OAuth token.

## N-02 — Backup privacy
Priority: P0

Expected:
- OAuth token excluded;
- signing keys excluded;
- warning states that backup may contain Google email/channel IDs/history/queue/workspace/cache.

## N-03 — Auth persistence design
Priority: P0

Expected:
- `auth_state_v1` contains only prior-success boolean;
- access token remains process memory only;
- ordinary update can silently re-request a token from Google authorization state.

---

# O. FAILURE / RECOVERY

## O-01 — No network during search
Priority: P1

Expected:
- readable failure;
- app remains usable;
- retry possible.

## O-02 — No network during write
Priority: P0

Expected:
- accurate partial/failure state;
- remaining tracks are not falsely marked ADDED;
- queue/retry path remains possible.

## O-03 — Google API error
Priority: P1

Expected:
- user-safe message;
- technical details available in diagnostics where appropriate.

## O-04 — App process killed with workspace saved
Priority: P1

Expected:
- current workspace restores;
- account authorization is silently recovered when possible;
- no duplicate remote write starts automatically.

---

# P. STRESS / RELEASE CANDIDATE

## P-01 — 3-track smoke test
Priority: P0

Use before every development release:
1. Import 3 tracks.
2. Search.
3. Manually replace one.
4. Save Project.
5. Create private playlist.
6. Open/copy/close result modal.

## P-02 — 50-track Clubland regression
Priority: P0 before public release

Expected:
- 50 tracks import;
- cache/search progress remains responsive;
- review usable;
- Project save works before export;
- create/append does not lose track state;
- History/Queue/result remain consistent.

## P-03 — Repeated operations
Priority: P1

Repeat:
- import/search/review/create;
- close/reopen app;
- rotate;
- return through History/Queue/Data/Service.

Expected:
- no state corruption;
- no duplicated remote write;
- no increasingly broken UI.

---

# Q. RELEASE SIGN-OFF

Before marking a release `PHONE TESTED`, record:

```text
[ ] GitHub signed APK build PASS
[ ] Installed on real phone
[ ] P0 cases PASS
[ ] Release-specific regression PASS
[ ] No unreviewed FAIL/BLOCKED P0
[ ] RELEASE_TEST_STATUS.md updated
```

For a public/shareable build additionally run:
- P-02 50-track stress;
- backup/restore/rollback;
- account update/restart recovery;
- new + existing playlist paths;
- duplicate handling;
- Queue resume;
- History export/project actions;
- Diagnostics/privacy review.
