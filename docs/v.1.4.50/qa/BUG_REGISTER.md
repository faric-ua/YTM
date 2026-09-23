
# v1.4.50 — Bug Register

Record only findings belonging to this release.

Do not silently close historical/global bugs without evidence.

## BUG-031 — Home only partially refreshes after Skin change

Status: **CLOSED — PHONE RETEST PASS v1.4.50 R1**

Initial signed evidence:

- run `35772192953`;
- source `c3939849516124cd66c6a72b04f1683f5c6e161c`;
- phone result `1-`.

Observed:

- selecting another Skin in Menu recreates MenuActivity;
- MainActivity underneath keeps its already-built old-Skin views;
- when Home resumes, `updatePrimaryActions()` re-reads the palette and repaints the
  four workflow buttons only;
- the rest of Home retains the previous visual Skin until MainActivity is rebuilt.

R1 fix:

- AppThemeManager records the Skin applied by `applyWindow()` on the Activity intent;
- MainActivity uses `recreateIfSkinChanged()` before ordinary resume updates;
- the recreated MainActivity builds the whole Home from the active Skin while staying
  within the historical R7 line-budget contract.

## BUG-032 — History clear confirmation disappears on rotation

Status: **CLOSED — PHONE RETEST PASS v1.4.50 R1**

Initial signed evidence:

- run `35772192953`;
- source `c3939849516124cd66c6a72b04f1683f5c6e161c`;
- phone result `3-`.

Observed:

- open `History → Очистити`;
- rotate the phone;
- the destructive confirmation disappears.

Safety observation:

- History was not automatically cleared.

R1 fix:

- HistoryActivity stores whether the clear confirmation is open;
- after Activity recreation the list screen is rebuilt and the same confirmation is
  reopened;
- `historyStore.clear()` remains only inside the explicit confirm callback;
- dismiss/cancel clears only the dialog-open state.

## R1 closeout evidence

Exact tested corrective APK:
- source `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- signed run `35782627453`;
- result `R1-1+ / R1-2+ / R1-3+`.

BUG-031 closeout:
- full Home chrome follows Neon/Blue/Green after returning from Menu;
- the earlier partial-refresh behavior is no longer reproduced.

BUG-032 closeout:
- History clear confirmation remains/reappears through rotation;
- no destructive action executes during restoration;
- Cancel leaves History unchanged and returns to the same History screen.

Both bugs are closed only for these tested paths.

## BUG-033 — Data modal lifecycle fragmentation

Status: **CLOSED — PHONE RETEST PASS v1.4.50 WAVE 3**

Phone finding on signed Wave 2:
- source `fdb2892c7b4fa0c858c55d5187a04ce296bde913`;
- run `35787308504`;
- broader UI smoke: `UI-1+ / UI-2+ / UI-3+` for visual/readability scope;
- History action/Help modal rotation remained stable;
- Data modal windows such as `Зберегти повний backup?` disappeared on rotation.

Root cause:
- `UiChrome` unified rendering, but modal lifecycle ownership remained per-call-site;
- History had explicit saved semantic dialog state;
- most Data modals were direct `UiChrome.alertBuilder(...).show()` calls with no
  saved modal identity;
- two prepared Restore/History confirmations had separate boolean/cache logic,
  creating a third lifecycle pattern inside the same Activity.

Wave 3 fix:
- add reusable `RestorableModalController`;
- DataActivity uses one semantic `DataModal` type for all 11 modal states;
- controller persists modal id + primitive result args;
- prepared Restore/History confirmations keep validated source JSON in their
  existing local cache and restore the semantic confirmation;
- result modals also restore with primitive summary args;
- rotation restores UI only and never invokes positive/destructive callbacks;
- old Activity listeners are detached in `onDestroy()`.

Scope:
- Wave 3 migrates DataActivity first because it has real failing phone evidence;
- already phone-passing History/Menu paths remain unchanged in this corrective
  wave and may migrate to the same controller after the core proves stable.

## BUG-033 closeout evidence

Exact tested corrective APK:
- source `7e6fcb482387be92a7de54db0f4df5081d640495`;
- signed run `35796094108`;
- result `W3-1+ / W3-2+ / W3-3+ / W3-4+`.

Phone proof covers:
- ordinary Data confirmations;
- Restore/History pre-picker confirmations;
- share confirmation;
- real prepared History JSON confirmation after file selection.

Safety proof:
- rotation never launched picker/share/domain actions automatically;
- prepared History confirmation restored without repeating file selection;
- Cancel stayed non-destructive and returned to the Data parent screen.

Stored visual evidence:
- `qa/evidence/WAVE3_HISTORY_IMPORT_CONFIRM_2026-09-23.jpg`.

BUG-033 is closed for these tested paths.

## BUG-034 — Result modal disappears on Activity recreation

Status: **R1 PHONE RETEST FAILED / R2 FIX IMPLEMENTED — STATIC/FULL PREFLIGHT PASS / PHONE RETEST NEEDED**

Finding:
- exact signed source `7e6fcb482387be92a7de54db0f4df5081d640495`;
- run `35796094108`;
- after successful History JSON import, result modal `History відновлено`
  disappears on rotation.

Evidence:
- `docs/v.1.4.50/qa/evidence/BUG034_HISTORY_RESULT_ROTATION_FAIL_2026-09-23.jpg`.

Scope distinction:
- BUG-033 remains closed for ordinary/prepared confirmation windows verified by
  W3-1..W3-4;
- BUG-034 is specifically about a post-operation result modal.

Root cause:
- result state already existed as `HISTORY_IMPORT_RESULT` with primitive Bundle
  args;
- shared controller could clear semantic state from an `OnDismissListener`
  during Activity teardown before the recreation-safe condition was reliable.

R1 fix:
- controller tracks resumed vs paused Activity state;
- `save()` marks state-saved before snapshot persistence;
- dismiss only clears semantic state during a normal resumed user dismissal;
- paused/state-saved/configuration teardown preserves the state for recreation.

The fix is generic for History-import, full-Restore and rollback result modals.

### BUG-034 R1 phone evidence / R2 correction

R1 exact package:
- source `c25b9f2a6843caf8790e45467df5bf118d08656d`;
- run `35799736192`;
- result `W3R1-1- / W3R1-2- / W3R1-3-`.

Failure screenshots:
- `qa/evidence/BUG034_HISTORY_RESULT_R1_FAIL_2026-09-23.jpg`;
- `qa/evidence/BUG034_ROLLBACK_CONFIRM_R1_FAIL_2026-09-23.jpg`.

R1 root-cause correction:
the controller must not infer semantic user dismissal from `Dialog.onDismiss`
plus Activity lifecycle timing. Android can tear down a Dialog during
recreation at a timing that makes such inference unreliable.

R2 invariant:
- OnDismiss = transient window detached only;
- button callback = explicit semantic close/transition;
- OnCancel = explicit Back/touch-outside semantic cancellation;
- system recreation = never a semantic close.

This is implemented once in the shared controller/Data modal host so all Data
modal states follow the same contract.
