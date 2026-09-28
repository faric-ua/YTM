# v1.4.54 — Current Working State / Assistant Handoff

Purpose: single source of truth for continuing this release after a chat reset, model handoff, app freeze, or context loss.

## Branch / candidate

- Branch: `feat/v1.4.54-history-bulk-sync`
- Installed signed phone candidate: `ced8a23ac2b172c88246d0b269c1892d56b0037c`
- Installed candidate Validate Android: run `36367212621` — PASS
- Installed signed APK: run `36369475053` — PASS
- Installation: in-place over existing app data
- BUG-045 shared action-layout fix: `1553d25ea9ebfedb3b23226f818d60acd72cf82f`
- persistent Bulk session creation-error diagnostic: `2ea18371f5fb6dd00742e5855374dc15a5770c3a`
- latest validation before this handoff refresh: run `36372305644` — PASS on `f61d30afc62682f9dde9c3ad05eb070c7200112b`; a newer validation is required after the diagnostic + handoff updates.

## Test status

- Test 1 — History Recovery: PASS
- Test 2 — local ↔ remote linkage: PASS
- Test 3 — Bulk preflight read-only: 3+ / PHONE PASS
- Test 4 — Bulk session + restart/reconcile: 4+ / PHONE PASS
- UX-037 — Bulk Preview Help fixed-footer + rotation + close: PASS; Bulk Session Help still pending before issue closure
- BUG-044 — finished PARTIAL_FAILED session blocks new session: old 18/19 session no longer reopened during retest, but fresh session creation then failed during checkpoint/baseline preparation; acceptance remains pending
- BUG-045 / #36 — narrow dialog action row wrapped `Створити сесію`: shared fix present, phone retest pending
- Test 5 — quota pause: ACTIVE / NOT COMPLETE
- Tests 6–9: NOT STARTED

## Exact current phone checkpoint

The installed signed APK is still the pre-BUG-045 candidate and remains installed in-place.

Phone evidence from the latest retest:
- one-shot Test 5 DAILY_QUOTA fault is still armed;
- no Bulk playlistItems.insert has fired;
- Bulk Preview Help already passed portrait + landscape + both rotations + close/no-op;
- the controlled preview still shows `NEW 1 · db ost`, 19 ready videoIds, 0 unresolved;
- after tapping `Створити Bulk-сесію`, the confirmation dialog exposed BUG-045: `Створити сесію` wrapped onto two lines instead of the peer action group stacking;
- after confirming, the flow entered `Створюю local checkpoint і свіжий read-only remote baseline…`;
- importantly, the old completed-partial `db ost 18/19` session did **not** reopen, so the original BUG-044 guard no longer blocked the path;
- session preparation then failed and returned to Preview with `Preview готовий. Сесію не створено.`;
- no fresh READY session screen was reached;
- no remote write started;
- the exact exception was not captured because this installed build exposed the reason only through a transient Toast.

Code follow-up now present on the branch:
- BUG-045 fix removes the unconditional 2–3-action horizontal fallback from shared `UiChrome`; width-first row/stack behavior is now authoritative;
- Bulk session creation failures now keep `safeError(error)` visible in the Preview status instead of relying only on the transient Toast.

Current phone screen:
- `Синхронізувати всі` Preview;
- `NEW · db ost`;
- 19 tracks / 19 ready videoIds / unresolved 0;
- `Створити Bulk-сесію` is enabled again;
- status says the session was not created.

Do NOT:
- tap `Створити Bulk-сесію` again on the currently installed old APK;
- clear app data;
- uninstall/reinstall;
- cancel the Test 5 fault;
- start unrelated Bulk writes.

## Current task

1. Validate and sign a new candidate containing BUG-045 + persistent creation-error diagnostics.
2. Install it **in-place**, preserving the armed Test 5 fault and all app data.
3. Retest the narrow confirmation layout.
4. Retry `Створити Bulk-сесію` exactly once.
5. Stop on the first evidence checkpoint:
   - if the fresh Bulk Session screen opens, capture it before `Почати синхронізацію`;
   - if preparation fails again, capture the now-persistent exact reason from Preview.
6. Only after a fresh READY session is confirmed, continue Test 5.

## Immediate next phone action

**No further action on the currently installed APK.**

After the new signed candidate is available:
- install it in-place;
- return to the same controlled Bulk Preview without clearing data;
- verify `db ost` still shows 19 ready tracks;
- tap `Створити Bulk-сесію`;
- verify the confirmation actions do not wrap incorrectly;
- confirm once;
- then STOP on either the new Bulk Session screen or the persistent creation-error text.

Do NOT press `Почати синхронізацію` until the fresh session screen is reviewed.

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
- #36 — BUG-045: Narrow dialog action rows wrap labels instead of stacking

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
