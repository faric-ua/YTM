# YTM Importer

YTM Importer is an Android/Kotlin project for importing track lists, preserving exact YouTube video identifiers when available, reviewing matches, and creating/adding tracks in YouTube / YouTube Music workflows.

The repository is intentionally more than source code: it preserves release history, real-phone QA, evidence, failed approaches, safety checks, build automation, and a tutorial intended to make the project reproducible.

## New ChatGPT node / project handoff

**Start here: [`START_HERE_ASSISTANT.md`](START_HERE_ASSISTANT.md)**

For a chat crash or mid-release takeover, immediately continue with:

**[`CURRENT_HANDOFF.md`](CURRENT_HANDOFF.md)**

The two files together explain:

- what the project is and why it exists;
- the exact current branch / PR stack / signed-build / phone-QA resume point;
- current version and known issues;
- ChatGPT/user responsibilities;
- package/Git/QA safety rules;
- signed-APK workflow;
- the correct reading order for continuing development.

A new assistant should recover context from the repository and live GitHub state instead of asking the user to reconstruct the previous chat.

## Current release

- versionName: **1.4.42**
- versionCode: **80**
- focus: **newest-first in-app file selection / UX-008 Phase 2B**
- status: **NOT PHONE-TESTED YET — v1.4.42 implemented / QA needed**
- v1.4.39 History JSON: Cancel/rotation/invalid-file paths PASS; populated-History Restore + rollback remain inconclusive and need later retest
- v1.4.40 Release History tested path remains PASS
- BUG-009 portrait account-dialog fix is phone-PASS; BUG-004 real-401 and BUG-010 Restore quota-preservation still await targeted phone retest
- UX-018 has partial phone evidence via the account dialog; UX-017 fallback-title and additional modal-order checks remain pending
- BUG-005 / Q-005 remains **CLOSED — PHONE RETEST PASS v1.4.27**

For the exact active resume point, read `CURRENT_HANDOFF.md`.

For broader current status, see:

- `PROJECT_STATUS.txt`
- `BACKLOG.md`
- `RELEASE_TEST_STATUS.md`
- `qa/BUG_REGISTER.md`
- `OPEN_QUESTIONS.md`

## Workflow

The default project loop is:

`requirements → prepared change → package/self-test → Termux preflight → Git push → signed GitHub Actions APK → real-phone QA → evidence/status closeout → merge/next work`

Stable collaboration rules live in `YTM_ASSISTANT_WORKFLOW.md`.

Reusable Termux/Git commands live in `TERMUX_COMMANDS.md`.

## Documentation

- `CURRENT_HANDOFF.md` — mutable crash-recovery/current-session snapshot;
- `docs/v.*` — immutable historical release/QA evidence snapshots;
- `docs/tutorial/` — curated step-by-step learning path;
- `docs/ASSISTANT_TOOL_MAP.md` — tool/capability map;
- `docs/WORKFLOW_LESSONS.md` — mistakes and guards learned from them;
- `docs/BUILD_ARTIFACT_CONVENTION.md` — stable APK artifact layout.

Do not delete or rewrite historical QA/evidence merely to simplify the repository.

Current v1.4.42 work adds a YTM Importer **Recent file selector** before generic Android
open-file flows. Remembered SAF files are ordered by provider `lastModified` newest-first,
while Android's system picker remains available as an explicit fallback.
