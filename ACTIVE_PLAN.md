# YTM Importer — ACTIVE PLAN

Updated: 2026-09-28

Purpose: live crash-recovery checklist. A new session reads `CURRENT_HANDOFF.md` first, then this file, and resumes from the first unchecked item.

## Bootstrap of the new planning rule

- [x] Add the mandatory active-plan rule to the repository workflow.
- [x] Add `ACTIVE_PLAN.md` to the new-assistant reading order.
- [ ] Before the next YTM functional change, reconcile `CURRENT_HANDOFF.md` with live GitHub branches/PRs, latest signed build and phone-installed version.
- [ ] Replace this bootstrap section with the exact active release/task checklist.
- [ ] Continue from that checklist and mark each verified step immediately.

## Rule

After every successful project-progress step:
1. mark only the verified checkbox complete;
2. update QA/findings when evidence changes;
3. update `CURRENT_HANDOFF.md` when the resume point changes;
4. keep the next action as the first unchecked item.

Do not use chat memory as the only record of progress.
