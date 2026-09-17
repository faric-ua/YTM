# YTM Importer — Project Handoff Hardening — 2026-09-17

This documentation-only hardening follows the v1.4.27 BUG-005 QA closeout.

## Goal

Make the repository self-explanatory enough that a new ChatGPT node can recover the working method without relying on hidden conversation history.

## Added

- `START_HERE_ASSISTANT.md` — canonical new-node entry point;
- `docs/ASSISTANT_TOOL_MAP.md` — tool/environment selection map;
- `docs/WORKFLOW_LESSONS.md` — real workflow failures and their guards;
- `docs/BUILD_ARTIFACT_CONVENTION.md` — stable APK + checksum folder convention;
- `scripts/project-handoff-audit.sh` — static guard for handoff documentation.

## Reworked

- `README.md` becomes a real project entry page;
- `TERMUX_COMMANDS.md` now matches exact-path staging policy and the stable release-folder convention;
- `YTM_ASSISTANT_WORKFLOW.md` gains handoff/source-of-truth/tool/artifact rules;
- `BACKLOG.md` records the handoff-hardening step as complete and moves the next product work forward;
- `scripts/release-preflight.sh` runs the handoff audit.

## No application behavior change

This change does not modify Android production code, `versionCode`, or `versionName`.

Current app remains v1.4.27 / code 61.
