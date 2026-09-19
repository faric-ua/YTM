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

- versionName: **1.4.41**
- versionCode: **77**
- focus: **auth/search recovery + restore/UI consistency**
- status: **NOT PHONE-TESTED YET — v1.4.41 implemented / QA needed**
- v1.4.39 History JSON: Cancel/rotation/invalid-file paths PASS; populated-History Restore + rollback remain inconclusive and need later retest
- v1.4.40 Release History tested path remains PASS
- BUG-004 / BUG-009 / BUG-010 fixes are implemented in v1.4.41 and await phone retest
- UX-017 / UX-018 are implemented in v1.4.41 and await phone retest
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