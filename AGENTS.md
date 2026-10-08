# YTM Importer — assistant tripwire

> **STOP. READ THIS BEFORE DOING PROJECT WORK.**

This file is intentionally tiny and stable. It exists so a fresh assistant/session
hits the recovery workflow immediately instead of reconstructing the project from
chat memory.

## Mandatory startup

1. Open `RESUME_HERE.md` **first**. This is the canonical one-minute crash-resume pointer.
2. Verify the live branch HEAD and latest Actions state against `RESUME_HERE.md`. If GitHub is newer, update `RESUME_HERE.md` before substantial work.
3. Open `ACTIVE_PLAN.md`, find its first unchecked actionable checkbox, and confirm it matches `RESUME_HERE.md` → `NEXT ACTION`.
4. Then read `CURRENT_HANDOFF.md`, `START_HERE_ASSISTANT.md`,
   `ASSISTANT_CONTEXT_INDEX.md`, and the ordered context files referenced there.
5. Verify the live GitHub branch/HEAD/Actions state before relying on old SHAs,
   build numbers, PR state, or chat recollection.
6. Before substantial multi-step work, send the user a short **friction note**:
   what is currently uncertain, stale, risky, conflicting, or blocking. If nothing
   is blocked, say that plainly and name the main thing being verified. Do not hide
   the point where work is getting stuck.
7. Before implementing that task, make sure `ACTIVE_PLAN.md` has a **CURRENT TASK**
   section with the concrete ordered checklist. The plan must exist in the repository
   before the implementation it governs.
8. After every **verified** progress step, immediately:
   - mark exactly that item `[x]`;
   - attach evidence/commit/run where useful;
   - keep the next real action as the first unchecked item;
   - update `NEXT ACTION`;
   - update `RESUME_HERE.md` whenever the real stop/resume point changes;\n   - update `CURRENT_HANDOFF.md` when the resume point materially changes.
9. Never mark phone behavior PASS from code inspection or a build. Phone behavior
   requires phone evidence.
10. If repository documents disagree, reconcile them before implementation.
11. When giving the user phone QA or Termux instructions, use the exact visible
   labels and the full tap/navigation path from the live app/menu. Verify the live
   source when wording is uncertain; internal flow names must not replace user-visible
   instructions.

## Mandatory reply layout (user-approved, 2026-10-08)

Before replying to the user about project work, signed APK, Termux or PHONE QA,
read `docs/assistant-kit/USER_RESPONSE_TEMPLATE.md`. Keep the user-facing
order: **short issue/fix heading → useful results → short exact Termux menu steps
at the END → 1–3 concise new APK tests as the LAST paragraph**.
Do not substitute repetitive CI narration or re-test already accepted items.
For long YTM work, keep exactly three visible parts with completion markers.

## Crash / context-loss rule

After a chat crash, model replacement, context loss, or long pause:

**Do not continue from memory. Start at `RESUME_HERE.md`, verify live GitHub, then read `ACTIVE_PLAN.md`.**

`RESUME_HERE.md` is the concise canonical stop/resume pointer.
`ACTIVE_PLAN.md` is mutable execution state.
`CURRENT_HANDOFF.md` is the concise current-state snapshot.
Historical evidence stays in the versioned QA/release files.

Do not turn this file into another status document. Its job is only to make the
assistant trip over the correct recovery path every time.
