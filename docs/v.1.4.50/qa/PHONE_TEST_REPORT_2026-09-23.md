# v1.4.50 — Phone Test Report — 2026-09-23

## Result

**TARGETED PHONE PASS — WAVE 2 SKIN PREVIEW**

Exact tested package:
- source `fdb2892c7b4fa0c858c55d5187a04ce296bde913`;
- signed run `35787308504`;
- version `1.4.50 (93)`.

Results:
- `W2-1+` — preview + Cancel no-op;
- `W2-2+` — Apply commits + Menu/Home refresh;
- `W2-3+` — preview rotation continuity + Back/Cancel no-op.

## Evidence

Repository screenshot:
`evidence/WAVE2_SKIN_PREVIEW_2026-09-23.jpg`

It confirms the preview UI renders the selected candidate's visual and semantic
tokens while the surrounding Menu remains visible.

Behavioral acceptance comes from real-phone execution of the three Wave 2
tests on the exact signed package.

## Preserved evidence

Wave 1/R1 targeted PASS remains accepted:
- source `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- run `35782627453`;
- `R1-1+ / R1-2+ / R1-3+`.

## Remaining v1.4.50 scope

Still pending:
- broader representative screen/modal/tile phone QA;
- remaining release checklist items;
- final release/tag/checkpoint closeout.

Do not broaden this targeted PASS into a full-app regression PASS.

## Wave 3 corrective closeout

**TARGETED PHONE PASS — RESTORABLE MODAL CORE**

Exact corrective package:
- source `7e6fcb482387be92a7de54db0f4df5081d640495`;
- signed run `35796094108`;
- result `W3-1+ / W3-2+ / W3-3+ / W3-4+`.

BUG-033 is CLOSED for the tested Data modal lifecycle paths.

The real prepared-file test used History JSON and confirmed that the semantic
confirmation survives recreation without file reselection or automatic import.

Stored screenshot:
`evidence/WAVE3_HISTORY_IMPORT_CONFIRM_2026-09-23.jpg`.

Representative UI status:
- prior `UI-1+ / UI-2+ / UI-3+` visual/readability/History modal smoke accepted;
- Data modal rotation defect corrected and retested;
- representative screen/modal/tile checklist item accepted.

Final v1.4.50 release acceptance is not yet claimed because the release-level
error-path and duplicate-operation checks are still open.

## Wave 3 R2 corrective closeout

**TARGETED PHONE PASS — DETERMINISTIC MODAL DISMISS**

Exact package:
- source `66d06d6912d014efb3a98d317ed49355a5fa3078`;
- signed run `35802968056`;
- result `W3R2-1+ / W3R2-2+ / W3R2-3+ / W3R2-4+`.

BUG-034 is CLOSED for the tested phone paths.

The R2 phone run proves that result/rollback semantic state survives repeated
recreation while explicit `Готово` remains a durable close and a single
rollback transition does not duplicate or auto-execute the domain action.

Final v1.4.50 release acceptance is not yet claimed because the general
`error path` and `no accidental duplicate operation` checks remain open.

## Final release QA closeout

**QA COMPLETE — ALL v1.4.50 RELEASE CHECKS PASSED**

Exact final tested app source:
`66d06d6912d014efb3a98d317ed49355a5fa3078`

Exact signed run:
`35802968056`

Final checks:
- `FINAL-A+`;
- `FINAL-B+`.

The invalid-backup error path was additionally reviewed from the stored phone
recording. It shows the wrong History JSON being rejected by the full-Restore
flow without opening `Підтвердити Restore` or starting Restore.

The no-duplicate-operation check passed on the same exact APK.

v1.4.50 is ready for the separate documentation/tag/checkpoint release
closeout. This QA checkpoint itself does not create release tags.
