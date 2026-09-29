# v1.4.54 — Current Working State / Assistant Handoff

Purpose: single source of truth for continuing this release after a chat reset,
model handoff, app freeze, or context loss.

Last updated: 2026-09-28

## Release / branch

- Release: `v1.4.54` / versionCode `97`
- Branch: `feat/v1.4.54-history-bulk-sync`
- Stable baseline: v1.4.53 exact accepted source
  `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`
- Development phase:
  **TESTS 1–5 PHONE PASS — WAVE 4 ROLLBACK VALIDATED / SIGNED PHONE QA NEXT**

## Accepted phone work

- Wave 0 write-limit regression smoke: PASS
- UX-030 local ↔ YTM linkage visibility: PASS
- Test 1 — History Recovery: PASS
- Test 2 — local ↔ remote linkage preservation: PASS
- Test 3 — Bulk read-only preflight: `3+` / PASS
- Test 4 — durable Bulk session / force-close / PREPARED reconciliation /
  terminal-per-track continuation: `4+` / PASS
- Bulk Preview Help:
  portrait + landscape + both rotations + close/no-op: PASS

Do not rerun Tests 1–4 just because the branch has advanced.

## Exact phone / repository checkpoint

Phone:
- installed signed source: `83d1cec92482841fd660df90a92137a77cbf8c29`;
- signed run: `36450498230` — PASS;
- Tests 1–5: PHONE PASS;
- Test 5 result: `5+`;
- preserved terminal session: `db ost` at `18/19`, with one known terminal unavailable/deleted source item;
- one-shot DAILY_QUOTA QA fault is consumed; do not re-arm it for this session;
- preserve this terminal session and its remote destination for Test 6.

Wave 4 repository candidate:
- exact validated HEAD: `11c4671f687d27b6ac0ea663861b92d26ed2c67a`;
- Validate Android: `36501830491` — PASS;
- release preflight: PASS;
- Wave 4 exact rollback audit: PASS through release preflight;
- JVM tests: PASS;
- unsigned release assemble: PASS.

Wave 4 implemented scope:
- exact `playlistItems.delete` by persisted `playlistItemId`;
- exact session-created playlist delete by persisted `playlistId`;
- rollback uses only APPLIED mutations from the same session;
- insert mutations are reverted before session-created playlist deletes;
- HTTP 404 on exact delete is idempotently accepted for interrupted rollback recovery;
- durable `ROLLING_BACK / ROLLBACK_PAUSED / ROLLED_BACK`;
- cold reopen never auto-resumes rollback;
- explicit `Продовжити відкат`;
- lifecycle-safe `Відкотити цю синхронізацію?` confirmation;
- LINKED add-only rows are now executable from exact persisted remote identity and missing-occurrence diff;
- shared adaptive full-screen footer buttons now inherit active Skin tones and disabled-state colors.

Next phone gate:
1. signed build from the exact final validated Wave 4 HEAD;
2. in-place install, no data clear;
3. reopen the preserved Test 5 session;
4. tap `Відкотити цю синхронізацію`;
5. review the confirmation and rotate portrait → landscape → portrait;
6. **cancel first**; do not execute remote rollback until confirmation counts/ownership copy are reviewed.


## BUG-046 status — issue #38 — CLOSED / PHONE PASS

Original phone finding on signed source `d34f37a46d115b288d64a609e2aead266345be53`:
- `Створити Bulk-сесію?` was open in portrait;
- portrait → landscape recreated Bulk Preview but the confirmation disappeared;
- the underlying Preview remained read-only;
- no confirmation action executed;
- no Bulk session creation or remote write started;
- Test 5 DAILY_QUOTA fault therefore remains armed.

Root cause:
Bulk Preview Help had explicit recreation state, but create-session / active-session
confirmations bypassed the existing `RestorableModalController`.

Branch fix:
- Bulk Preview confirmations now use `RestorableModalController`;
- semantic modal id is saved/restored through `STATE_PREVIEW_MODAL`;
- restore happens after content/plan restoration;
- explicit buttons clear modal state before executing their action;
- system recreation restores the same modal without executing an action;
- `scripts/ui-window-contract-audit.sh` now requires this lifecycle contract.

Final phone retest on signed source `83d1cec92482841fd660df90a92137a77cbf8c29`
(signed run `36450498230`) passed in one continuous video:
portrait open → landscape → portrait preserved the same confirmation; only layout
reflowed; no action executed; explicit `Скасувати` returned to the same Preview.

Result: **BUG-046 PASS / CLOSED**.

## BUG-044 status — issue #34 — CLOSED / PHONE PASS

Original problem:
a finished `PARTIAL_FAILED` `db ost 18/19` session blocked creation of a fresh
Bulk session.

Phone retest already proved:
- the old 18/19 session no longer reopened;
- the flow advanced into fresh local-checkpoint + remote-baseline preparation.

That fresh preparation then failed on an older candidate and returned to Preview
before a new READY session screen opened.

A persistent error diagnostic is now present on the branch, so the next controlled
fresh-session retry will either:
- open a new 19-track READY session, or
- leave the exact preparation error visible on Preview.

Final phone retest reached a fresh Bulk Session in `Готово до запуску` with the NEW `db ost` row at `0/19`, checkpoint + remote baseline present, linked rows still deferred, and no auto-write. The old completed-partial 18/19 session did not reopen.

Result: **BUG-044 PASS / CLOSED.**

## BUG-045 status — issue #36

Original symptom:
narrow portrait confirmation forced two actions into one row and wrapped
`Створити сесію`.

Root cause:
the shared dialog renderer correctly rejected a horizontal row by width, then an
action-count-only fallback forced 2–3 actions back into a row.

Status:
- original forced-row fallback removed;
- portrait symptom passed on installed source `552c387...`;
- project-wide shared-window acceptance is now owned by UX-038 rather than by
  another sequence of one-window retests.

## UX-033 skin-aware footer finding — issue #26

Phone evidence in the active Blue skin:
- Bulk Session reached READY correctly, but `Почати синхронізацію` / `Закрити`
  rendered as default gray Android buttons instead of Blue skin actions.
- source inspection confirms Bulk Preview/Session build raw `Button(this)` controls;
- `UiChrome.addAdaptiveActionButtons(...)` currently handles only row/stack sizing,
  not skin/tone styling.

Decision:
do **not** patch this in the middle of the preserved Test 5 session. Track it under
UX-033 as shared action styling work: canonical full-screen footer buttons must be
skin-aware for primary/secondary/danger/disabled states while preserving adaptive
layout.

## UX-037 status — issue #35

- Bulk Preview Help lifecycle/fixed-footer phone QA: PASS
- Bulk Session Help: pending
- remaining Help-shell acceptance is folded into the UX-038 representative matrix

## UX-038 status — issue #37

A project-wide static audit was started because the documented UI rules were
correct but runtime/shared-component enforcement had drifted.

Canonical audit document:
`docs/v.1.4.54/UI_WINDOW_AUDIT_2026-09-28.md`

Canonical rules:
- `docs/design/UI_WINDOW_QA_CONTRACT.md`
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`

New static gate:
- `scripts/ui-window-contract-audit.sh`
- wired into `scripts/release-preflight.sh`
- included in the portable/system audit inventory

Systemic corrections already on the branch:
- all action-bearing shared dialogs converge on one
  **fixed header → scrollable content → fixed footer** shell;
- menu/message/Help/record/result/content/multi-choice/danger-confirm and
  `StableAlertBuilder` paths inherit that shell;
- dialog row/stack choice accounts for actual label width;
- action count alone cannot force a row;
- explicit semantic layouts resolve before AUTO;
- shared dialog footer labels are one line and are not auto-shrunk merely to keep
  a row;
- ListSelector, Bulk Preview and Bulk Session footers now use the same adaptive
  row/stack renderer already used by Storage/Recent-file/URL Snapshot flows;
- historical audits that encoded obsolete implementation details were updated;
- the audit catalog/portable inventory now includes the new UI-window gate.

Static validation progressed until the only remaining failure was generated
inventory (`FILE_MANIFEST.txt`) becoming stale after the broad cleanup.
The manifest must be refreshed **last**, after the checkpoint docs stop changing,
then exact-HEAD Validate Android must pass.

## Tomorrow's stop/resume contract

Do **not** resume Test 5 first.

Resume in this order:

1. Read this file and `CURRENT_HANDOFF.md`.
2. Check current branch HEAD and the latest exact-HEAD Validate Android run.
3. If the final manifest-refresh HEAD is not PASS, fix static/preflight only;
   do not touch the phone.
4. Once exact-HEAD Validate is PASS, build a signed APK from that exact HEAD.
5. Install it **in-place** over source `552c387...`; preserve app data and the
   armed Test 5 fault.
6. Run the **single consolidated representative window matrix** from
   `UI_WINDOW_AUDIT_2026-09-28.md` instead of testing/fixing windows one by one.
7. If the matrix passes, retry `Створити Bulk-сесію` exactly once.
8. STOP on the first dynamic checkpoint:
   - fresh `db ost` 19-track READY Bulk Session, **before**
     `Почати синхронізацію`; or
   - persistent exact preparation error on Preview.
9. Only after that checkpoint is reviewed may Test 5 start its first controlled
   insert.

If the representative matrix exposes another **shared** UI defect:
stop feature QA, fix the shared layer, rerun the static gate once, install the
replacement candidate in-place, and resume the same checkpoint. Do not create a
new per-window patch/test loop.

## Representative UX-038 phone matrix

One compact pass on one signed candidate:

1. short 2-action confirmation — portrait + landscape;
2. long Help — portrait + landscape + both rotations;
3. long content or multi-choice dialog — scroll content while actions remain fixed;
4. record/result dialog with 3 actions;
5. ListSelector footer — portrait + landscape;
6. Bulk Preview footer — portrait + landscape;
7. Bulk Session Help + footer on the preserved Test 5 path;
8. Neon plus one alternate-theme sanity check.

For each sample verify:
- fixed header/content/footer structure where applicable;
- footer actions never scroll away;
- no label is wrapped because a row was forced;
- row ↔ stack reflow never executes an action;
- Back/Cancel/Close returns to the correct owner;
- rotation restores semantic state and never starts remote work.

## Test 5 initial quota-pause checkpoint — PASS

Phone evidence on signed source `83d1cec92482841fd660df90a92137a77cbf8c29`:
- explicit `Почати синхронізацію` was tapped once;
- session entered `Пауза — write quota`;
- user-facing copy reports daily quota exhaustion and that unfinished add-track work was saved;
- NEW `db ost` remains at `0/19`;
- `Продовжити` is available;
- no further action was taken after the pause screenshot.

This is the expected first Test 5 checkpoint: the one-shot DAILY_QUOTA QA hook fired before the first real Bulk insert.

Next acceptance sequence:
1. rotate paused screen portrait → landscape → portrait;
2. verify pause state persists and no auto-resume occurs;
3. force-close/reopen app;
4. reopen current Bulk session and verify the same paused state still exists with no auto-resume;
5. only then explicitly press `Продовжити`.

UX-037 Bulk Preview + Bulk Session Help phone acceptance is complete; issue #35 is closed.

## Test 5 cold-reopen checkpoint — PASS

Phone video confirms the YTM Importer task was removed from Samsung Recents, the app
was relaunched from the launcher, and the preserved Bulk session was reopened from
the app UI.

Observed after relaunch:
- session is still `Пауза — write quota`;
- NEW `db ost` is still `0/19`;
- `Продовжити` is available;
- no automatic continuation occurred.

This completes the cold-reopen / no-auto-resume requirement for Test 5.

Samsung note:
the fixed icon row shown at the bottom of Recents is Samsung's quick-launch/app
shortcut area, not evidence that those apps are still present as task cards. The
previous phone video was interpreted too conservatively; the new video makes the
task-removal sequence unambiguous.

Next Test 5 step:
explicitly press `Продовжити` once and capture the resulting state. The one-shot
DAILY_QUOTA hook has already auto-consumed, so execution should continue through
the normal path without duplicating the interrupted insert.

## Test 5 after the UI gate

Expected controlled flow once a fresh READY session exists:

1. explicit `Почати синхронізацію`;
2. remote playlist create may succeed;
3. first Bulk insert is intercepted before the real remote insert by the one-shot
   DAILY_QUOTA QA hook;
4. session enters `PAUSED_WRITE_QUOTA`;
5. created playlist/YTM ID stays durable;
6. interrupted insert stays retryable, not terminal/duplicated;
7. rotate;
8. force-close/reopen;
9. no auto-resume;
10. explicit `Продовжити` resumes safely after the one-shot fault has
    auto-consumed.

One known deleted source video may still produce terminal 404 later, so final
completion may be 18/19; that does not invalidate Test 5 if quota
pause/restart/resume semantics pass.

## Test 5 final result — 5+ / PHONE PASS

Phone evidence on signed source `83d1cec92482841fd660df90a92137a77cbf8c29`:

1. one-shot DAILY_QUOTA fault intercepted the first Bulk insert and produced
   `Пауза — write quota` at `db ost 0/19`;
2. rotation preserved the paused state and never auto-resumed;
3. task removal + cold relaunch preserved the same paused state and never
   auto-resumed;
4. one explicit `Продовжити` resumed the durable session;
5. execution advanced normally after the consumed one-shot fault;
6. transient RUNNING evidence showed 1 in-flight PREPARED mutation while confirmed
   inserts advanced;
7. final session state became `Частково завершено з помилкою` with:
   - created playlists: 1;
   - added tracks: 18;
   - not added: 1;
   - NEW `db ost`: `18/19`.

The final 18/19 partial result is the known terminal unavailable/deleted source item
already accepted in Test 4 and does not invalidate Test 5.

Result: **5+ / PHONE PASS**.

Important interpretation:
the temporary QA fault path is proven durable and explicitly resumable; the fault
was consumed and must not be re-armed for this session.

Next implementation gate:
Tests 6–8 require Wave 4 exact rollback. The current branch has rollback states and
ledger identifiers, but no user-facing rollback engine/action yet. Do not attempt
Test 6 on phone until Wave 4 is implemented and validated.

## Wave 4 exact rollback implementation — STATIC/JVM PASS

Issue: #39.

The current candidate implements the exact rollback contract required for Tests 6–8.
The code is validated but not yet phone accepted. Do not infer rollback PASS from
static/JVM evidence; first signed phone Test 6 must inspect the exact rollback plan
and then prove remote ownership behavior.

UX-033 / #26:
the shared adaptive full-screen footer helper is now skin-aware rather than leaving
raw Android gray buttons. Phone retest remains pending on the same signed Wave 4 candidate.

## Open issues at this checkpoint

- #34 — BUG-044: finished PARTIAL_FAILED session blocked new session — CLOSED / PASS
- #35 — UX-037: Bulk Preview/Session lifecycle-safe Help — CLOSED / PASS
- #36 — BUG-045: narrow dialog action row wrapped label — CLOSED / PASS
- #37 — UX-038: project-wide window/footer contract consolidation
- #38 — BUG-046: Bulk create-session confirmation disappears on rotation — CLOSED / PASS

## Recovery order in a new chat

1. Read `CURRENT_HANDOFF.md`.
2. Read this file.
3. Read `docs/v.1.4.54/UI_WINDOW_AUDIT_2026-09-28.md`.
4. Read `docs/v.1.4.54/qa/PHONE_TEST.md`.
5. Check issues #34–#37.
6. Confirm branch HEAD and latest exact-HEAD Validate/Signed runs.
7. Continue from **Tomorrow's stop/resume contract** above.

Do not restart Test 5 from scratch unless phone state is actually proven lost.
