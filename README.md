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

- versionName: **1.4.44**
- versionCode: **83**
- focus: **UX-021 adaptive landscape action layout**
- status: **NOT PHONE-TESTED YET — v1.4.44 UX-021 implemented**
- v1.4.39 History JSON: Cancel/rotation/invalid-file paths PASS; populated-History Restore + rollback remain inconclusive and need later retest
- v1.4.40 Release History tested path remains PASS
- BUG-009 portrait account-dialog fix is phone-PASS; BUG-010 and BUG-012 are closed; v1.4.43 startup silent auth recovery is phone-observed, while the aged-token BUG-013 acceptance case and BUG-004 Search-specific 401 retest remain pending
- UX-017 is closed on v1.4.41-R2; UX-018 has representative phone PASS but not exhaustive modal coverage
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
