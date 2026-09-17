# YTM Importer

YTM Importer is an Android/Kotlin project for importing track lists, preserving exact YouTube video identifiers when available, reviewing matches, and creating/adding tracks in YouTube / YouTube Music workflows.

The repository is intentionally more than source code: it preserves release history, real-phone QA, evidence, failed approaches, safety checks, build automation, and a tutorial intended to make the project reproducible.

## New ChatGPT node / project handoff

**Start here: [`START_HERE_ASSISTANT.md`](START_HERE_ASSISTANT.md)**

That file explains:

- what the project is and why it exists;
- current version and known issues;
- ChatGPT/user responsibilities;
- available tools;
- package/Git/QA safety rules;
- signed-APK workflow;
- documentation structure;
- the correct reading order for continuing development.

## Current release

- versionName: **1.4.30**
- versionCode: **64**
- focus: **Consolidated Delta-Chain Restore**
- status: **NOT PHONE-TESTED YET**
- BUG-005 / Q-005 remains **CLOSED — PHONE RETEST PASS v1.4.27**

For exact current status, see:

- `PROJECT_STATUS.txt`
- `BACKLOG.md`
- `RELEASE_TEST_STATUS.md`
- `qa/BUG_REGISTER.md`
- `OPEN_QUESTIONS.md`

## Workflow

The default project loop is:

`requirements → prepared change → package self-test → Termux apply/preflight → Git commit/push → signed GitHub Actions APK → real-phone QA → evidence/QA closeout → tutorial/history`

Stable collaboration rules live in `YTM_ASSISTANT_WORKFLOW.md`.

Reusable Termux/Git commands live in `TERMUX_COMMANDS.md`.

## Documentation

- `docs/v.*` — immutable historical release/QA evidence snapshots;
- `docs/tutorial/` — curated step-by-step learning path;
- `docs/ASSISTANT_TOOL_MAP.md` — tool/capability map;
- `docs/WORKFLOW_LESSONS.md` — mistakes and guards learned from them;
- `docs/BUILD_ARTIFACT_CONVENTION.md` — stable APK artifact layout.

Do not delete historical QA/evidence merely to simplify the repository.
