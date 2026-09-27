# v1.4.54 — Targeted Phone QA

Do not execute this suite until v1.4.54 has a signed candidate built from the exact
validated source.

## Wave 0 — BUG-039 write-limit smoke

Do **not** intentionally spam playlist creation to force HTTP 429.

A. Normal regression path:
1. Install the signed v1.4.54 candidate over real v1.4.53 data.
2. Verify existing Search/WRITE Queue records remain readable.
3. Create one controlled small playlist or resume one existing WRITE job.
4. Verify a normal successful write still completes.

PASS:
- no regression in ordinary create/add;
- Queue compatibility remains intact;
- no false pause appears on successful requests.

B. Natural-limit path, only if Google returns a real 429/limit during ordinary QA:
1. Capture the displayed reason and Queue state.
2. Close/reopen or rotate.
3. Reopen the WRITE job.

PASS:
- unfinished work remains in Queue;
- explicit daily quota is called daily quota only when the server reason supports it;
- rate/resource/unknown 429 copy does not claim a daily reset;
- frequent-write/rate-limit copy tells the user to wait and resume manually;
- no automatic retry occurs after rotation/restart;
- the Queue retains the same pause reason.

JVM policy tests are the deterministic classification acceptance for synthetic
rate/resource/generic-429 variants; phone QA must not manufacture API abuse.

Result notation: `W0+` / `W0-`; natural-limit evidence can be added separately.

### Wave 0 phone result — 2026-09-26: W0+

Exact candidate:
- source: `9daa9027981551539d2fdfa76d08faa4620aeee6`;
- signed build run: `36261268460`;
- installed in place over real v1.4.53 data without clearing app data.

Observed:
- app launched as v1.4.54 with Google/YTM account state intact;
- current workspace `The Prodigy - What Evil Lurks (1991)` remained present with 4 resolved tracks;
- no Search or YTM write auto-started after launch;
- Queue opened normally and was empty; no phantom SEARCH/WRITE recovery job appeared;
- one controlled new private playlist `YTM v1.4.54 W0 test` was created through the ordinary write path;
- all 4 selected tracks were added successfully;
- final result: added 4, errors 0;
- no false quota/rate-limit pause appeared.

Result:
- **W0+ / PHONE PASS for the normal regression path**.
- Natural 429/limit behavior remains evidence-on-occurrence only; do not manufacture rate limits.
- Next development scope: UX-030 explicit local↔YTM linkage visibility, then History Recovery.

## UX-030 — explicit local ↔ YTM linkage gate

Run this gate before starting History Recovery. Do not start Bulk Sync Tests 3+ yet.

Exact static/JVM/full-preflight evidence before this phone gate:
- app/code source `f00ee7a4b6f0ab6fc4533d9b7c7610046972181a`;
- Validate Android run `36267305742`: **PASS**;
- `PlaylistLinkagePolicy` is covered by JVM tests for local-only, linked, pending Search and pending Write precedence;
- linkage identity uses persisted playlistId only; playlist title is display metadata, never identity.

A. Linked current workspace:
1. Open the current workspace that was written to a known YTM playlist.
2. Inspect Home `Поточний плейлист`.
3. Open Playlist Hub.

PASS:
- Home says `Пов'язано з YTM`;
- Playlist Hub says `Пов'язано з YTM` and shows the exact persisted `YTM ID`;
- when a persisted target title exists it is display-only next to the linkage status.

B. Local-only workspace:
1. Open/import a workspace that has never been written to YTM.
2. Inspect Home and Playlist Hub.

PASS:
- both say `Лише локально`;
- matching/similar playlist titles must not create a remote link.

C. History visibility:
1. Open History.
2. Inspect one completed linked write entry and one local-import entry.
3. Open each detail.

PASS:
- linked write entry says `Пов'язано з YTM` and exposes its persisted playlistId where available;
- local-import entry says `Лише локально`;
- list/detail wording agrees.

D. Pending states, only when naturally available:
- existing SEARCH recovery work must show `Очікує Search`;
- existing WRITE pending work must show `Очікує запис у YTM`;
- do not manufacture quota/rate-limit failures to create these states.

Rotation/restart smoke:
- linkage wording restores from persisted state;
- no Search or YTM write auto-starts.

Result notation: `UX030+` / `UX030-`.

After `UX030+`, begin History Recovery Tests 1–2. Bulk Sync Tests 3+ stay blocked until the History Recovery contract is implemented.


### UX-030 phone result — 2026-09-27: UX030+

Exact installed candidate:
- source: `90d470ddbe6d7158613f7a069d8f6cd3596804dc`;
- Validate Android run: `36272701416` — PASS;
- signed build run: `36276086140` — PASS.

Observed on real phone:
- linked workspace `The Prodigy - What Evil Lurks (1991)` showed `Пов'язано з YTM` on Home and Playlist Hub;
- Playlist Hub exposed the persisted YTM playlist ID;
- linked History list/detail agreed and detail exposed the persisted YTM ID with `Додано в YTM 4/4`;
- rotation/restart preserved the linked wording and did not auto-start Search or WRITE;
- fresh local import `The Prodigy - Promotional Singles / Official Promo Tracks (1994)` naturally showed `Очікує Search` before search;
- its Search plan showed 12 tracks, 11 cache hits and 1 new `search.list` request;
- after search completed, Home and Playlist Hub showed `Лише локально`;
- the corresponding local-import History detail also showed `Лише локально`, type `Локальний імпорт`, result `Імпортовано 12 треків`, and no YTM ID;
- no title-only linkage was invented;
- pending WRITE state was not manufactured because no natural pending WRITE was available.

Result:
- **UX030+ / PHONE PASS**.
- UX-030 is closed. History Recovery is the next implementation scope.
- Tests 1–2 below are acceptance criteria for that not-yet-implemented feature; do not execute them until the restore action exists in the app.
- Bulk Sync Tests 3+ remain blocked until the History Recovery contract is implemented.


## Test 1 — Search work survives workspace replacement

**Implementation gate:** NOT YET EXECUTABLE. The current History UI does not yet expose `Відновити як поточний плейлист`; implement History Recovery first, then run this test.


1. Import playlist A with at least 4 uncached tracks.
2. Search until all tracks resolve.
3. Record Search counter.
4. Import unrelated playlist B.
5. Open History entry for A.
6. Tap `Відновити як поточний плейлист`.
7. Verify A returns with the same selected results/order.
8. Open Search plan for A.

PASS:
- no already-resolved track requires a new search.list call;
- Search counter does not increase merely by restore;
- no Search auto-start;
- no YTM write auto-start.

Result notation: `1+` / `1-`.

## Test 2 — Local ↔ remote linkage

1. Use a workspace with a known persisted remote playlistId.
2. Replace current workspace.
3. Restore the linked workspace from History.
4. Inspect Home/detail state.

PASS:
- UI says linked-to-YTM;
- exact playlistId lineage is preserved;
- no title-based lookup creates/replaces linkage.

Result: `2+` / `2-`.

## Test 3 — Bulk preflight is read-only

1. Prepare at least:
   - one NEW local playlist;
   - one LINKED playlist;
   - one NEEDS_SEARCH playlist;
   - one playlist owned by an existing Pending job.
2. Tap `Синхронізувати всі`.
3. Rotate on preview.
4. Cancel preview.

PASS:
- classifications are correct;
- estimated Search/non-Search usage is shown separately;
- no remote playlist/item count changed;
- rotation does not start work;
- Cancel is a no-op.

Result: `3+` / `3-`.

### Test 3 phone result — 2026-09-27: 3+

Exact accepted candidate:
- source: `e0362e122d612cd8fa18e361b2cff302df283b3b`;
- Validate Android run: `36332906386` — PASS;
- signed build run: `36333728905` — PASS.

Observed on real phone:
- Bulk preview remained read-only; the execution button stayed disabled;
- classifications were verified for NEW, NEEDS_SEARCH, ALREADY_SYNCED and LINKED;
- LINKED was reproduced safely by changing one local selected videoId while preserving the persisted YTM playlist ID; preview planned only one add-only insert and did not create a new playlist;
- a natural legacy WRITE Queue job for `The Prodigy - Baby's Got A Temper (2002)` initially exposed BUG-040 because the old job lacked `localPlaylistId`;
- BUG-040 was fixed without title-only matching: the legacy fallback requires exact pending-track identity and unique local ownership;
- retest showed `PENDING 1`, `BLOCKED 0`, and the queued playlist was correctly classified as PENDING;
- Search usage and non-Search/write estimates were shown separately;
- rotation restored the completed preview without rebuilding/starting work;
- Cancel returned to Menu and remained a no-op;
- remote verification in YouTube Music confirmed the linked test playlist still had 4 tracks and the locally changed selected videoId was not written remotely.

Result:
- **3+ / PHONE PASS**.
- BUG-040 is closed.
- Tests 4+ remain blocked until the durable Bulk execution/session/rollback implementation exists.

## Test 4 — Bulk session + restart

**Implementation gate — READY FOR SIGNED PHONE QA.**

Validated Wave 3 foundation before phone QA:
- app source: `28a44a0e5e22f12f3337cd78fc572c258f47eb6e`;
- Validate Android run: `36337826120` — PASS;
- session creation persists a local Full Backup checkpoint and a fresh account-wide read-only remote baseline before any write;
- preview still performs no remote mutation; creating the durable session does not auto-start execution;
- Wave 3 executes only NEW rows; LINKED is explicitly deferred to the append-safe wave;
- session progress and mutation ledger are durable;
- restart/cold-open never auto-resumes a RUNNING session;
- each remote mutation is persisted as PREPARED before request and APPLIED after confirmed success;
- successful playlistItems.insert stores the created playlistItemId;
- interrupted PREPARED inserts into a session-created playlist are reconciled by exact ordered remote prefix on explicit Resume; ambiguity blocks blind retry;
- an uncertain PREPARED CREATE remains blocked rather than guessing by playlist title.

**Phone safety note:** every NEW row in the confirmed Wave 3 plan is executable. Before pressing `Почати синхронізацію`, verify the exact NEW playlist set and do not proceed with unintended playlists.

1. Confirm a controlled bulk plan.
2. Allow at least one mutation.
3. Force-close app.
4. Reopen and inspect bulk-session screen.

PASS:
- session resumes UI from durable progress;
- already-successful mutation is not duplicated;
- continuation requires explicit user tap when paused.

Result: `4+` / `4-`.

### Test 4 phone evidence — 2026-09-27

Two phone runs were executed.

#### Run A — normal durable execution

- durable session created with 14 plan rows and an account-wide baseline of 61 playlists;
- before Start: 0 playlists created, 0 tracks added;
- two NEW rows were materialized as `Готовий новий плейлист • 0/12`;
- LINKED remained deferred, NEEDS_SEARCH remained skipped, PENDING remained Queue-owned;
- after explicit Start, confirmed counters advanced while one transient PREPARED mutation represented the in-flight write;
- session completed successfully: 2 playlists created, 24 tracks added;
- final state: `Завершено`.

Result for normal Wave 3 NEW execution: **PASS**.

#### Run B — controlled force-close / restart

Controlled playlist:
- `db ost`;
- 19 tracks;
- 19 exact ready videoIds;
- one and only one NEW row in preview;
- planned work: create 1 + insert 19.

Observed from two screen recordings:
- durable session started only after explicit Start;
- remote playlist creation completed;
- insert counters advanced while one mutation remained PREPARED/in-flight;
- app was removed from Recents during active write execution;
- app was reopened manually;
- Home remained stable with no automatic Bulk resume;
- current playlist retained the new remote linkage;
- opening `Поточна Bulk-сесія` restored:
  - `Пауза — попередній запуск перервано`;
  - created playlists: 1;
  - confirmed added tracks: 3;
  - PREPARED without confirmation: 1.

This proves:
- cold reopen does not auto-resume: **PASS**;
- durable progress survives process death: **PASS**;
- PREPARED mutation survives process death: **PASS**.

Initial failure:
- `Продовжити` was disabled even though the remaining PREPARED mutation was an insert into a playlist durably created by the same session;
- recorded as **BUG-042 / issue #31**.

#### Run B continuation — BUG-042 retest

The BUG-042 signed candidate was installed **in-place** over the interrupted-session phone data.

Observed:
- Home retained `db ost`, its remote linkage and the existing Queue state;
- no Bulk operation auto-resumed after app launch;
- the same interrupted session reopened with 1 playlist created, 3 confirmed inserts and PREPARED 1;
- `Продовжити` was enabled;
- one explicit Resume entered reconciliation and execution advanced beyond the pre-crash boundary.

Result:
- **BUG-042 PASS / CLOSED**.

During this continuation, a separate terminal item error appeared:
- one insert returned `HTTP 404 — Video not found`;
- old Bulk behavior stopped the whole session at 6/19;
- recorded as **BUG-043 / issue #32**.

#### Run B continuation — BUG-043 retest + video review

Fix source:
- `3e9967f7897f8d1d4409e93666413af7a12115a3`;
- Validate Android run `36355526661` — PASS;
- signed APK run `36356575193` — PASS.

The fixed APK was again installed **in-place**, preserving the same 6/19 durable session.

Observed on device and confirmed in the uploaded 37.8 s recording:
- cold launch did not auto-resume;
- the old partial failure was migrated to `Пауза — попередній запуск перервано`;
- progress stayed 6/19;
- terminal failure stayed durable as `Не додано треків: 1`;
- row detail identified `YouTube — Deleted video`;
- reason stayed `HTTP 404 — Video not found`;
- explicit Resume continued the same session;
- visible confirmed progress advanced through 7/19, 10/19, 13/19, 15/19, 17/19 and 18/19;
- `Створено плейлистів: 1` remained unchanged;
- final row state: `Завершено частково • 18/19`;
- final session state: `Частково завершено з помилкою`;
- terminal failure remained visible and Resume became disabled after completion.

Result:
- **BUG-043 PASS / CLOSED**;
- terminal per-track failure no longer blocks remaining independent writes;
- failed terminal mutation is not retried;
- durable partial result remains inspectable.

### Test 4 final remote verification

The destination was opened in the regular YouTube app after the completed recovery flow.

Observed:
- playlist title: `db ost`;
- privacy: private;
- attribution: `Створено через YTM Importer`;
- remote item count: **18 videos**.

Combined with the recorded durable-session evidence:
- `Створено плейлистів: 1` remained unchanged during both recovery continuations;
- the same persisted YTM playlist identity was retained across in-place updates and resumes;
- final session result was 18/19 with exactly one terminal `Deleted video` failure;
- no second create operation occurred during reconciliation/resume.

This is consistent with exactly 18 successful remote inserts and one unavailable source item.

### Test 4 final result

**4+ / PHONE PASS.**

Accepted behaviors:
- explicit Start only;
- durable progress survives force-close;
- cold reopen never auto-resumes;
- recoverable PREPARED insert can be explicitly reconciled and resumed;
- no blind retry of ambiguous PREPARED work;
- terminal per-track 404 does not stop unrelated remaining inserts;
- terminal failure identity/reason stays durable and visible;
- recovery preserves the same destination playlist;
- final remote destination contains 18 videos for the 18 successful inserts.

BUG-042 and BUG-043 are both closed.


## Test 5 — Quota pause

1. Run a session that reaches Search or write quota/rate-limit.
2. Capture session state.
3. Restart/rotate.

PASS:
- session is PAUSED with the correct reason;
- completed playlist work remains completed;
- remaining work stays pending;
- no automatic resume.

Result: `5+` / `5-`.

## Test 6 — Rollback newly created playlist

1. Let bulk sync create one controlled test playlist and add tracks.
2. Stop before unrelated operations.
3. Choose `Відкотити цю синхронізацію`.
4. Confirm.

PASS:
- only session-created playlist is removed;
- unrelated remote playlists remain;
- local checkpoint/result remains inspectable.

Result: `6+` / `6-`.

## Test 7 — Rollback additions to existing playlist

Use a dedicated test playlist with known pre-existing items.

1. Bulk sync adds at least two missing items.
2. Record created playlistItem ids in diagnostics/session detail.
3. Roll back the session.

PASS:
- exactly the inserted playlistItem ids are deleted;
- pre-existing items remain;
- playlist itself remains.

Result: `7+` / `7-`.

## Test 8 — interrupted rollback

1. Start rollback under a controlled condition that prevents completion.
2. Restart app.
3. Open the session.
4. Resume rollback explicitly later.

PASS:
- already-reverted mutations are not repeated incorrectly;
- remaining reverse mutations stay durable;
- UI never reports full rollback before completion.

Result: `8+` / `8-`.

## Test 9 — legacy compatibility

1. Install over real v1.4.53 data.
2. Verify old History, current workspace, Search/Write Pending Queue and backup.
3. Restore one legacy History entry lacking a modern snapshot.

PASS:
- legacy data remains readable;
- legacy restore produces a safe local workspace;
- no title-only YTM linkage is invented.

Result: `9+` / `9-`.
