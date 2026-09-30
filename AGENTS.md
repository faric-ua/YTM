# YTM Importer — assistant tripwire

> **STOP. READ THIS BEFORE DOING PROJECT WORK.**

This file is intentionally tiny and stable. It exists so a fresh assistant/session
hits the recovery workflow immediately instead of reconstructing the project from
chat memory.

## Mandatory startup

1. Open `ACTIVE_PLAN.md` **first**.
2. Find its first unchecked actionable checkbox and read `NEXT ACTION`.
3. Then read `CURRENT_HANDOFF.md`, `START_HERE_ASSISTANT.md`,
   `ASSISTANT_CONTEXT_INDEX.md`, and the ordered context files referenced there.
4. Verify the live GitHub branch/HEAD/Actions state before relying on old SHAs,
   build numbers, PR state, or chat recollection.
5. Before substantial multi-step work, send the user a short **friction note**:
   what is currently uncertain, stale, risky, conflicting, or blocking. If nothing
   is blocked, say that plainly and name the main thing being verified. Do not hide
   the point where work is getting stuck.
6. Before implementing that task, make sure `ACTIVE_PLAN.md` has a **CURRENT TASK**
   section with the concrete ordered checklist. The plan must exist in the repository
   before the implementation it governs.
7. After every **verified** progress step, immediately:
   - mark exactly that item `[x]`;
   - attach evidence/commit/run where useful;
   - keep the next real action as the first unchecked item;
   - update `NEXT ACTION`;
   - update `CURRENT_HANDOFF.md` when the resume point materially changes.
8. Never mark phone behavior PASS from code inspection or a build. Phone behavior
   requires phone evidence.
9. If repository documents disagree, reconcile them before implementation.

## Crash / context-loss rule

After a chat crash, model replacement, context loss, or long pause:

**Do not continue from memory. Start again at `ACTIVE_PLAN.md`.**

`ACTIVE_PLAN.md` is mutable execution state.
`CURRENT_HANDOFF.md` is the concise current-state snapshot.
Historical evidence stays in the versioned QA/release files.

Do not turn this file into another status document. Its job is only to make the
assistant trip over the correct recovery path every time.
