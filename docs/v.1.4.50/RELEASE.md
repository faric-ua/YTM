
# YTM Importer v1.4.50 — Skin System

## Goal

Introduce a reusable Skin contract while preserving existing Neon/Blue/Green
appearance and behavior.

## Scope

- common `Skin` model for the three existing built-in styles;
- named `SkinPalette` visual tokens;
- separate `SemanticPalette` state roles;
- static audit preventing semantic roles from collapsing back into unlabelled palette fields;
- no intentional visual redesign in Wave 1.

## Architecture / behavior changes

- `AppThemeManager` resolves the selected style through a built-in Skin registry;
- UI visual tokens still come from `AppThemeManager.palette(...)`;
- state colors are explicitly accessed as `palette.semantic.<role>`;
- existing theme storage keys and exact Wave 1 RGB values remain unchanged.

See `SKIN_CONTRACT.md` and `diagrams/SKIN_ARCHITECTURE.md`.

## System/lifecycle impact

No new remote work, navigation or modal ownership is introduced in Wave 1.
Theme selection continues to use the existing persisted style key and existing Activity recreation behavior.

## Version

- versionName: `1.4.50`
- versionCode: `93`
- branch: `feat/v1.4.50-skin-system`


## Phone QA checkpoint

Initial signed Wave 1:
- run `35772192953`;
- source `c3939849516124cd66c6a72b04f1683f5c6e161c`;
- result `1- / 2+ / 3-`.

Corrective R1:
- exact tested app source `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- signed GitHub Actions run `35782627453`;
- phone result `R1-1+ / R1-2+ / R1-3+`.

R1 confirms full Home Skin refresh after Neon/Blue/Green changes and preserves
the History clear-confirmation through rotation with explicit Cancel/no-op safety.
This is a targeted Wave 1/R1 phone PASS, not a full-app regression or final
v1.4.50 release closeout.

## Status

**DEVELOPMENT — WAVE 3 R2 DETERMINISTIC MODAL-DISMISS CORE STATIC/FULL PREFLIGHT PASS / BUG-034 PHONE RETEST PENDING; R1 PHONE RETEST FAILED**

## Wave 2 implementation

Skin selection now has an explicit preview/commit boundary.

Candidate preview:
- reads the selected built-in Skin without writing preferences;
- shows surface/accent and representative semantic-state tokens;
- survives rotation by storing the candidate style key;
- treats Cancel/Back/dismiss as no-op.

Commit:
- only `Застосувати` calls `AppThemeManager.setStyle(...)`;
- Menu recreates after a real Skin change;
- Home continues to use the Wave 1 R1 resume guard when the user returns.

Wave 2 does not change the built-in RGB values or business semantics.

## Wave 2 phone checkpoint

Exact signed Wave 2 package:
- source `fdb2892c7b4fa0c858c55d5187a04ce296bde913`;
- GitHub Actions run `35787308504`;
- version `1.4.50 (93)`;
- real-phone result `W2-1+ / W2-2+ / W2-3+`.

Accepted behavior:
- preview does not commit the candidate Skin;
- Cancel leaves the previously active Skin selected;
- explicit Apply commits and refreshes Menu/Home;
- the same candidate preview survives portrait/landscape Activity recreation;
- Back/Cancel after recreation do not commit the candidate.

Visual evidence:
- `docs/v.1.4.50/qa/evidence/WAVE2_SKIN_PREVIEW_2026-09-23.jpg` shows the Skin preview with candidate visual/semantic tokens.

This remains a targeted Wave 2 phone PASS. The broader representative
screen/modal/tile QA item is still open and final v1.4.50 release acceptance is
not claimed.

## Wave 3 — shared restorable modal lifecycle

Broader Wave 2 UI QA found that History modals survived rotation while Data
modals such as `Зберегти повний backup?` disappeared.

The rendering layer was already shared through `UiChrome`; the missing piece
was shared semantic lifecycle ownership.

Wave 3 adds `RestorableModalController`:
- one durable semantic modal id + Bundle args;
- one save/restore/detach implementation;
- Activity-specific renderer and callbacks;
- no callback execution during recreation.

DataActivity is the first full migration and routes all 11 Data modal states
through the controller. This includes ordinary confirmations, prepared
Restore/History confirmations, destructive snapshot confirmation and result
modals.

History/Menu are intentionally not rewritten in the same corrective wave
because their current lifecycle paths already have accepted phone evidence.

## Wave 3 phone checkpoint

Exact signed Wave 3 package:
- source `7e6fcb482387be92a7de54db0f4df5081d640495`;
- GitHub Actions run `35796094108`;
- version `1.4.50 (93)`;
- real-phone result `W3-1+ / W3-2+ / W3-3+ / W3-4+`.

Accepted behavior:
- ordinary Data confirmations survive portrait/landscape recreation;
- rotation does not open save/file/share pickers automatically;
- Cancel remains a no-op and returns to the same Data parent screen;
- a prepared History JSON confirmation survives recreation without reselecting
  the file;
- prepared confirmation recreation does not execute History import;
- the shared `RestorableModalController` is now proven on the real phone for
  both ordinary and prepared Data modal states.

Visual evidence:
- `docs/v.1.4.50/qa/evidence/WAVE3_HISTORY_IMPORT_CONFIRM_2026-09-23.jpg` shows the prepared `Підтвердити History import` modal from
  the exact signed Wave 3 APK.

BUG-033 is closed for the tested Data modal lifecycle scope.

The earlier representative UI smoke `UI-1+ / UI-2+ / UI-3+` plus this Wave 3
corrective retest closes the representative screens/modals/tiles checklist
item. Final release acceptance is still not claimed: explicit error-path and
duplicate-operation release checks remain open.

## Wave 3 R1 — result-modal recreation hardening

Final release QA exposed a new result-modal lifecycle defect on the exact
signed Wave 3 build:
- source `7e6fcb482387be92a7de54db0f4df5081d640495`;
- run `35796094108`;
- `History відновлено` disappeared after Activity recreation/rotation.

The result modal was already represented as semantic
`DataModal.HISTORY_IMPORT_RESULT` with primitive Bundle args. The remaining
weakness was inside `RestorableModalController`: its dismiss listener relied
primarily on `Activity.isChangingConfigurations` at dismiss time.

R1 hardens the shared controller itself:
- Activity explicitly reports `onResume` / `onPause`;
- `save()` marks modal state as state-saved before recreation;
- dismiss clears durable semantic state only while the Activity is resumed,
  state is not saved, and configuration recreation is not in progress;
- paused/state-saved/system teardown cannot erase the semantic modal identity;
- user dismiss while resumed still clears state normally.

This applies to all Data result states, including:
- `HISTORY_IMPORT_RESULT`;
- `RESTORE_RESULT`;
- `ROLLBACK_RESULT`.

Failure evidence:
- `docs/v.1.4.50/qa/evidence/BUG034_HISTORY_RESULT_ROTATION_FAIL_2026-09-23.jpg`.

BUG-033 remains closed for its tested confirmation scope. BUG-034 tracks this
new result-modal recreation finding.

## Wave 3 R2 — deterministic semantic dismissal

Wave 3 R1 signed package:
- source `c25b9f2a6843caf8790e45467df5bf118d08656d`;
- run `35799736192`;
- phone result `W3R1-1- / W3R1-2- / W3R1-3-`.

Observed failures:
- `History відновлено` disappears on rotation;
- `Відкотити останній Restore?` disappears on rotation.

Evidence:
- `docs/v.1.4.50/qa/evidence/BUG034_HISTORY_RESULT_R1_FAIL_2026-09-23.jpg`;
- `docs/v.1.4.50/qa/evidence/BUG034_ROLLBACK_CONFIRM_R1_FAIL_2026-09-23.jpg`.

R1 proved that lifecycle timing flags are not a reliable distinction between a
real user close and Android window teardown.

R2 changes the shared contract:

- `OnDismissListener` owns only transient `Dialog` detachment and never clears
  semantic modal state;
- explicit modal buttons call `completeDataModalAction()` to close semantic
  state deterministically;
- Back/touch-outside uses `Dialog.OnCancelListener`, which the shared
  `RestorableModalController` owns;
- the controller forwards semantic cancel to DataActivity so prepared
  Restore/History caches are cleaned only on real user cancellation;
- system recreation may dismiss the old transient Dialog at any lifecycle
  timing without erasing `DataModal + Bundle args`;
- `UiChrome.showDangerConfirmDialog` now has a backwards-compatible explicit
  cancel-button callback.

This removes dependence on `onPause`, `onSaveInstanceState` timing and
`isChangingConfigurations` for semantic close decisions.
