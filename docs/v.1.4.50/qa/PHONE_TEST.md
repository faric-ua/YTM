# v1.4.50 — Phone Test

## Wave 1 — Skin contract smoke

Precondition:

- install the signed v1.4.50 / code 93 APK over the accepted v1.4.49 build;
- preserve existing local/account state;
- do not clear app data.

### Test 1 — built-in skins

For each built-in skin:

1. open the existing theme/skin selector;
2. select Neon Dark, Blue Dark and Green Dark in turn;
3. confirm the selected style survives the existing recreation/navigation path;
4. inspect Home, one representative full-screen utility page, one tile/list and
   one modal.

Expected:

- no crash;
- no missing controls;
- no navigation change;
- no unexpected geometry change;
- colors remain consistent with the pre-v1.4.50 built-in style;
- text remains readable.

Result format: `1+` / `1-`

### Test 2 — semantic state roles

Inspect representative UI that exposes:

- success/ready;
- warning/attention;
- danger/error/destructive;
- duplicate.

Expected:

- each role remains distinguishable;
- the role meaning is unchanged across Neon/Blue/Green;
- no semantic state silently becomes an ordinary accent state.

Result format: `2+` / `2-`

### Test 3 — recreation smoke

While a representative modal/form is open:

1. rotate portrait → landscape → portrait;
2. confirm the existing lifecycle contract still holds.

Expected:

- no remote operation restarts;
- no destructive action auto-runs;
- existing draft/modal semantics remain consistent with the common lifecycle
  contract.

Result format: `3+` / `3-`

## Initial signed Wave 1 result

Signed workflow:

- run: `35772192953`
- source: `c3939849516124cd66c6a72b04f1683f5c6e161c`
- installed version: `1.4.50 (93)`

Initial signed Wave 1 phone result: `1- / 2+ / 3-`

Observed:

- `1-`: after changing Skin in Menu, Home retained the previous Neon visual chrome;
  only the four workflow/state buttons refreshed because `updatePrimaryActions()`
  re-read the new palette while the rest of MainActivity's existing views were not
  rebuilt.
- `2+`: semantic success/warning/danger/duplicate roles remained distinguishable.
- `3-`: `History → Очистити` destructive confirmation disappeared after rotation.
  History was not automatically cleared.

Corrective R1 targets:

- BUG-031: AppThemeManager records the Skin applied to each Activity window; MainActivity
  uses a one-line resume guard to recreate when that applied Skin differs from the
  persisted Skin, rebuilding the whole Home without regrowing MainActivity.
- BUG-032: History stores the clear-confirm open flag and restores the confirmation
  after recreation without executing `historyStore.clear()` automatically.

### R1 retest

- `R1-1+`: Neon → Blue → Green; after returning from Menu the whole Home chrome
  uses the active Skin, not only the four workflow buttons.
- `R1-2+`: `History → Очистити`; rotate portrait → landscape → portrait; the
  confirmation remains/reappears and History is unchanged until explicit confirm.
- `R1-3+`: press Cancel after rotation; History remains unchanged and user stays on
  History.

## R1 signed result

Exact corrective package:
- source: `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- signed run: `35782627453`;
- installed version: `1.4.50 (93)`.

Real-phone result:
- `R1-1+` — Neon → Blue → Green refreshes the whole Home, not only workflow buttons;
- `R1-2+` — `History → Очистити` confirmation survives portrait → landscape → portrait and History does not auto-clear;
- `R1-3+` — Cancel after rotation closes only the confirmation, keeps the user on History and leaves History unchanged.

Combined Wave 1 evidence:
- initial semantic-state test remains `2+` from signed run `35772192953`;
- corrective R1 scope is `R1-1+ / R1-2+ / R1-3+`.

Scope note:
- this closes BUG-031 and BUG-032 for their tested phone paths;
- this does not claim a full-app regression PASS;
- no R1 screenshot/video was committed; the R1 result is conversation-reported phone evidence.

## Wave 2 — Skin preview / selection

Use the exact signed Wave 2 APK produced from the Wave 2 source commit.

### W2-1 — preview does not commit

1. note the currently active Skin and its `✓` marker;
2. open `Меню → Тема`;
3. tap a different Skin;
4. inspect the candidate preview;
5. press `Скасувати`;
6. reopen `Тема`.

Expected:
- preview shows candidate background/surface/accent and semantic role samples;
- before Apply, persisted/active Skin does not change;
- after Cancel, the old Skin still has the `✓` marker;
- Menu/Home remain on the old Skin.

Result: `W2-1+` / `W2-1-`

### W2-2 — explicit Apply commits

1. open preview for a different Skin;
2. press `Застосувати`;
3. confirm Menu recreates into the selected Skin;
4. reopen `Тема` and confirm the new Skin has `✓`;
5. return to Home.

Expected:
- only Apply changes the active Skin;
- Menu uses the new Skin after recreation;
- Home fully refreshes to the same Skin through the accepted R1 resume guard;
- no navigation or business behavior changes.

Result: `W2-2+` / `W2-2-`

### W2-3 — preview rotation continuity

1. open preview for a Skin that is not active;
2. rotate portrait → landscape → portrait before Apply;
3. verify the same candidate preview remains/reappears;
4. press system Back or `Скасувати`;
5. reopen `Тема`.

Expected:
- the same candidate Skin preview survives Activity recreation;
- rotation never commits the candidate;
- Back/Cancel leaves the previously active Skin selected;
- no remote operation or destructive action starts.

Result: `W2-3+` / `W2-3-`

## Wave 2 signed result

Exact package:
- source `fdb2892c7b4fa0c858c55d5187a04ce296bde913`;
- signed run `35787308504`;
- installed version `1.4.50 (93)`.

Real-phone result:
- `W2-1+` — candidate preview opens and Cancel preserves the previously active Skin;
- `W2-2+` — explicit Apply commits the Skin, Menu recreates, and Home refreshes to the same Skin;
- `W2-3+` — the same candidate preview survives portrait → landscape → portrait; Back/Cancel does not commit it.

Screenshot evidence:
- `evidence/WAVE2_SKIN_PREVIEW_2026-09-23.jpg`.

The screenshot confirms the candidate-preview presentation and readable visual/
semantic token samples. The interaction/rotation PASS is real-phone
conversation-reported evidence from the same signed build.

Scope:
- Wave 2 preview/selection lifecycle is accepted for the tested paths;
- Wave 1 R1 evidence remains accepted;
- broader representative screen/modal/tile QA remains pending;
- this is not final v1.4.50 release acceptance.

## Wave 3 — Data restorable modal core

Use the exact signed Wave 3 APK. Install over the current v1.4.50 build without
clearing app data.

### W3-1 — Full backup confirmation

1. Open `Меню → Дані та резервні копії`.
2. Tap `Зберегти backup`.
3. While `Зберегти повний backup?` is open, rotate portrait → landscape → portrait.
4. Press `Скасувати`.

Expected:
- the same confirmation remains/reappears after each recreation;
- no save picker opens during rotation;
- Cancel closes only the modal;
- no backup operation starts automatically.

Result: `W3-1+` / `W3-1-`

### W3-2 — Pre-picker Restore / History confirmations

For both `Вибрати backup` and `Імпорт History`:
1. open the first confirmation;
2. rotate before pressing `Вибрати файл`;
3. press `Скасувати`.

Expected:
- the same confirmation restores;
- Android/YTM file picker never opens from rotation;
- Cancel returns to the same Data screen.

Result: `W3-2+` / `W3-2-`

### W3-3 — Share full backup confirmation

1. Open the Data `Поділитися → Повний backup` confirmation.
2. Rotate portrait → landscape → portrait.
3. Press `Скасувати`.

Expected:
- the same confirmation restores;
- Android share UI never starts from recreation;
- Cancel leaves local data unchanged.

Result: `W3-3+` / `W3-3-`

### W3-4 — prepared-file confirmation (optional fixture)

If a known-good backup/History JSON fixture is already available:
1. reach `Підтвердити Restore` or `Підтвердити History import`;
2. rotate before confirming;
3. verify the same prepared confirmation restores;
4. Cancel.

Expected:
- selected validated file does not need to be selected again;
- rotation never performs Restore/import;
- Cancel clears the pending confirmation safely.

Result: `W3-4+` / `W3-4-` / `W3-4 SKIP`

## Wave 3 signed result

Exact package:
- source `7e6fcb482387be92a7de54db0f4df5081d640495`;
- signed run `35796094108`;
- installed version `1.4.50 (93)`.

Real-phone result:
- `W3-1+` — full-backup confirmation survives rotation; no picker/backup starts automatically; Cancel is a no-op;
- `W3-2+` — Restore and History pre-picker confirmations survive rotation; file picker does not auto-open;
- `W3-3+` — full-backup share confirmation survives rotation; share UI does not auto-open;
- `W3-4+` — a real prepared History JSON confirmation survives recreation without file reselection and without auto-import.

Screenshot evidence:
- `evidence/WAVE3_HISTORY_IMPORT_CONFIRM_2026-09-23.jpg`.

The screenshot confirms the prepared History-import confirmation on the exact
Wave 3 build, including the parsed History counts and preserved scope copy.
Behavioral rotation/no-op acceptance comes from the real-phone W3 test.

Result:
- BUG-033 closed for tested Data modal lifecycle paths;
- representative screens/modals/tiles smoke is now complete when combined with
  the prior `UI-1+ / UI-2+ / UI-3+` pass;
- explicit release-level error-path and duplicate-operation checks remain open.

## Wave 3 R1 — Result modal lifecycle

Use the exact signed Wave 3 R1 APK.

### W3R1-1 — History result rotation

1. Import a valid History JSON and explicitly confirm Restore.
2. Wait for `History відновлено`.
3. Rotate portrait → landscape → portrait without pressing a result action.

Expected:
- the same `History відновлено` result remains/reappears;
- restored entry/track counts remain the same;
- History import does not execute a second time.

Result: `W3R1-1+` / `W3R1-1-`

### W3R1-2 — Done clears result state

1. From the restored result after at least one rotation, press `Готово`.
2. Rotate the Data screen again.

Expected:
- the result closes once;
- `History відновлено` does not resurrect after the explicit user dismissal.

Result: `W3R1-2+` / `W3R1-2-`

### W3R1-3 — rollback transition is single-shot

1. Repeat a History import if needed to reach `History відновлено`.
2. Rotate the result once.
3. Press `Відкотити` exactly once.

Expected:
- exactly one `Відкотити останній Restore?` confirmation opens;
- no second rollback confirmation appears;
- rollback itself does not start until explicitly confirmed.

Result: `W3R1-3+` / `W3R1-3-`

## Wave 3 R1 signed failure

Exact R1 package:
- source `c25b9f2a6843caf8790e45467df5bf118d08656d`;
- signed run `35799736192`.

Phone result:
- `W3R1-1-`;
- `W3R1-2-`;
- `W3R1-3-`.

Observed:
- `History відновлено` disappears after rotation;
- `Відкотити останній Restore?` disappears after rotation.

Screenshots:
- `evidence/BUG034_HISTORY_RESULT_R1_FAIL_2026-09-23.jpg`;
- `evidence/BUG034_ROLLBACK_CONFIRM_R1_FAIL_2026-09-23.jpg`.

R1 is not accepted as a BUG-034 fix.

## Wave 3 R2 — Deterministic modal dismissal

Use the exact signed Wave 3 R2 APK.

### W3R2-1 — result survives repeated recreation

1. Import valid History JSON and confirm.
2. Wait for `History відновлено`.
3. Rotate portrait → landscape → portrait twice.

Expected:
- the same result remains/reappears after every rotation;
- counts remain unchanged;
- import does not execute again.

Result: `W3R2-1+` / `W3R2-1-`

### W3R2-2 — explicit Done is durable close

1. From a restored `History відновлено` result, press `Готово`.
2. Rotate Data screen twice.

Expected:
- result stays closed;
- no result resurrection.

Result: `W3R2-2+` / `W3R2-2-`

### W3R2-3 — rollback confirmation survives recreation

1. Reach `History відновлено`.
2. Press `Відкотити` once.
3. While `Відкотити останній Restore?` is open, rotate twice.

Expected:
- the same rollback confirmation remains/reappears;
- rollback has not executed.

Result: `W3R2-3+` / `W3R2-3-`

### W3R2-4 — rollback transition is single-shot

1. From `History відновлено`, press `Відкотити` exactly once.
2. Do not confirm rollback.

Expected:
- exactly one rollback confirmation opens;
- no duplicate confirmation;
- no rollback occurs before the explicit confirmation button.

Result: `W3R2-4+` / `W3R2-4-`

## Wave 3 R2 signed result

Exact package:
- source `66d06d6912d014efb3a98d317ed49355a5fa3078`;
- signed run `35802968056`;
- installed version `1.4.50 (93)`.

Real-phone result:
- `W3R2-1+` — `History відновлено` survives repeated rotation;
- `W3R2-2+` — `Готово` closes the result and it does not resurrect;
- `W3R2-3+` — `Відкотити останній Restore?` survives repeated rotation;
- `W3R2-4+` — one rollback tap opens one confirmation and no rollback runs
  before explicit confirmation.

Stored screenshots:
- `evidence/WAVE3_R2_HISTORY_RESULT_PORTRAIT_2026-09-23.jpg`;
- `evidence/WAVE3_R2_HISTORY_RESULT_LANDSCAPE_2026-09-23.jpg`;
- `evidence/WAVE3_R2_ROLLBACK_CONFIRM_LANDSCAPE_2026-09-23.jpg`;
- `evidence/WAVE3_R2_ROLLBACK_CONFIRM_PORTRAIT_2026-09-23.jpg`.

BUG-034 is closed for the tested phone paths.
