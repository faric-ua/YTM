# YTM Importer — CURRENT HANDOFF

Last updated: **2026-09-29**

## v1.4.54 disposition

Release: **v1.4.54 / versionCode 97**  
Branch: `feat/v1.4.54-history-bulk-sync`

Main targeted phone QA:
**Tests 1–9 = PHONE PASS.**

Immutable accepted functional source:

`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Accepted signed run for the final recovery/compatibility chain:

`36579457780`

Evidence:
- `docs/v.1.4.54/qa/TEST_RUN_2026-09-29.md`
- `docs/v.1.4.54/qa/PHONE_TEST_REPORT_2026-09-29.md`
- `docs/v.1.4.54/qa/STABILIZATION_CHECKPOINT.md`
- `docs/v.1.4.54/qa/PHONE_TEST.md`

## Publication decision

v1.4.54 is **functionally closed / phone accepted**, but is intentionally not
published as the public stable binary.

Reason:
the exact 9/9 accepted source contains temporary deterministic Test 5/Test 8 QA
fault controls used to prove pause/interrupted-rollback recovery.

Do not modify/rebuild this accepted source merely to manufacture a stable tag.

Public-release hardening continues on:

`feat/v1.4.55-ux-hardening`

v1.4.55 uses v1.4.54 source `e553c6d...` as its immutable functional reference
and compile-gates those QA controls behind `BuildConfig.DEBUG`.

## Resume rule

Do not resume Tests 1–9. They are complete.

For current development switch to the v1.4.55 branch and read:
1. `ACTIVE_PLAN.md`
2. `CURRENT_HANDOFF.md` on v1.4.55
3. `docs/v.1.4.55/CURRENT_STATE.md`
4. mandatory context files.

Historical Test 1–9 detail remains under `docs/v.1.4.54/`.
