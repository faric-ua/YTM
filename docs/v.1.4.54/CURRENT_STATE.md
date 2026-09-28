# v1.4.54 — Current Working State / Assistant Handoff

Purpose: single source of truth for continuing this release after a chat reset, model handoff, app freeze, or context loss.

## Branch / candidate

- Branch: `feat/v1.4.54-history-bulk-sync`
- Functional candidate HEAD before this handoff document: `ced8a23ac2b172c88246d0b269c1892d56b0037c`
- Validate Android: run `36367212621` — PASS
- Signed APK: run `36369475053` — PASS
- Installation: in-place over existing app data

## Test status

- Test 1 — History Recovery: PASS
- Test 2 — local ↔ remote linkage: PASS
- Test 3 — Bulk preflight read-only: 3+ / PHONE PASS
- Test 4 — Bulk session + restart/reconcile: 4+ / PHONE PASS
- UX-037 — Bulk Help terminology + window behavior: PARTIAL, phone retest pending
- BUG-044 — finished PARTIAL_FAILED session blocks new session: code fix present, phone retest pending
- Test 5 — quota pause: ACTIVE / NOT COMPLETE
- Tests 6–9: NOT STARTED

## Exact current phone checkpoint

The latest signed APK is installed in-place.

The user opened:
`Квота API → QA — Test 5`

Phone evidence confirms:
- one-shot Test 5 fault is still armed;
- UI says the next Bulk insert will simulate daily quota exhaustion;
- no Test 5 Bulk insert has fired yet;
- therefore the one-shot fault must remain armed.

Do NOT:
- clear app data;
- uninstall/reinstall;
- cancel the Test 5 fault;
- start unrelated Bulk writes.

## Current task

Finish the work that was interrupted before Test 5:

1. Retest UX-037 on the new fixed-footer Help shell.
2. Retest BUG-044 by creating a fresh Bulk session while an old completed-partial session exists.
3. Complete Test 5 quota-pause flow.
4. Only after Test 5 passes, continue to Tests 6–9.

## Immediate next phone action

From the currently open Quota screen:

`Back → Home → Menu → Синхронізувати всі → wait for Bulk preview → ?`

Checkpoint:
- stop with Bulk Preview Help open;
- verify `Зрозуміло` is visible without scrolling;
- verify only Help content scrolls;
- send screenshot before continuing.

After that, phone QA should cover:
- portrait;
- landscape;
- portrait ↔ landscape restoration;
- closing Help returns to same Bulk preview;
- no write/session starts because Help was opened/restored.

Then:
- press `Створити Bulk-сесію`;
- verify a NEW Test 5 session is actually created (BUG-044 retest);
- stop before `Почати синхронізацію` for evidence.

## Test 5 expected flow after BUG-044 retest

Once the new controlled session exists:

1. explicit `Почати синхронізацію`;
2. remote playlist create may succeed;
3. first Bulk insert is intercepted by the one-shot QA fault before remote insert;
4. session must enter `PAUSED_WRITE_QUOTA`;
5. created playlist/YTM ID remains durable;
6. failed/interrupted insert remains retryable, not terminal/duplicated;
7. rotate;
8. force-close/reopen;
9. no auto-resume;
10. explicit `Продовжити` continues safely after the one-shot fault has auto-consumed.

Controlled source:
- local playlist: `db ost`
- 19 ready videoIds
- one known deleted video may later produce terminal 404, so final completion may be 18/19; that does not invalidate Test 5 if quota pause/restart/resume semantics pass.

## Open issues relevant to this checkpoint

- #34 — BUG-044: Finished PARTIAL_FAILED Bulk session blocks creation of a new session
- #35 — UX-037: lifecycle-safe Help windows for Bulk preview/session terminology

## Phone-QA interaction rule

Mechanical actions may be batched in one instruction only when no intermediate evidence or decision is required.

Stop exactly at:
- screenshot/evidence checkpoints;
- dynamic state transitions;
- any point where the next action depends on what the phone shows.

Do not jump ahead across those checkpoints.

## Recovery order in a new chat

1. Read this file first.
2. Read `docs/v.1.4.54/qa/PHONE_TEST.md`.
3. Check issues #34 and #35.
4. Confirm current branch HEAD and latest Validate/Signed runs.
5. Continue from the exact phone checkpoint above; do not restart Test 5 from scratch unless the state was actually lost.
