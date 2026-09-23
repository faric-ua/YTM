
# YTM Importer — Assistant Context Index

This file is the canonical map of the knowledge required to work safely on
YTM Importer.

The goal is that a new ChatGPT session can recover the project from the
repository rather than depending on remembered chat context.

## Mandatory startup procedure

Before any code-changing session, release work, architectural change,
system/lifecycle change, or recovery after a lost chat:

1. read `START_HERE_ASSISTANT.md`;
2. read this file;
3. read every non-comment path, in order, from
   `docs/assistant-kit/CONTEXT_FILES.txt`;
4. verify the live Git branch / HEAD against GitHub;
5. inspect the exact source files and audits relevant to the requested task;
6. only then design or apply changes.

For a tiny factual question a full repository walk is not required. For actual
project modification, the context walk is mandatory.

## Source-of-truth precedence

When sources disagree, use this order:

1. actual real-phone observation for runtime UI/behavior;
2. signed GitHub Actions build evidence;
3. current Git/GitHub source;
4. current root status/policy documents;
5. current release documentation;
6. historical release snapshots;
7. old conversation recollection.

Chat memory must never silently override current repository truth.

## Context layers

### Current mutable state

- `CURRENT_HANDOFF.md`
- `PROJECT_STATUS.txt`
- `BACKLOG.md`
- `RELEASE_TEST_STATUS.md`
- `qa/BUG_REGISTER.md`
- `OPEN_QUESTIONS.md`

### Collaboration / safety

- `YTM_ASSISTANT_WORKFLOW.md`
- `TERMUX_COMMANDS.md`
- `docs/ASSISTANT_TOOL_MAP.md`
- `docs/WORKFLOW_LESSONS.md`
- `docs/BUILD_ARTIFACT_CONVENTION.md`

### System behavior contracts

- `docs/assistant-kit/portable/SYSTEM_BEHAVIOR_CONTRACT.md`
- `docs/v.1.4.47/navigation/NAVIGATION_ORIGIN_CONTRACT_R7.md`
- `docs/v.1.4.47/navigation/TEST_DIAGRAM_STANDARD.md`
- `docs/design/TILE_UI_CONTRACT.md`
- `docs/testing/AUTOMATED_TEST_STRATEGY.md`
- `docs/testing/YOUTUBE_URL_FIXTURES.md`

These rules cover Activity recreation, rotation, navigation ownership, modal
lifecycle, form drafts, remote-operation ownership, progress/result separation,
Back/Cancel semantics, SAF/system UI boundaries and destructive actions.

### QA / release documentation

- `qa/MASTER_TEST_PLAN.md`
- `qa/TEST_EVIDENCE_POLICY.md`
- `docs/assistant-kit/RELEASE_DOCUMENTATION_CONTRACT.md`
- `docs/assistant-kit/AUDIT_CATALOG.md`

### Planned architecture

- `docs/roadmap/UPDATER.md`
- `docs/roadmap/SKINS.md`

### Historical documentation coverage

- `docs/documentation/HISTORICAL_RELEASE_MATRIX.md`
- `docs/documentation/DOCUMENTATION_GAP_AUDIT_2026-09-22.md`

The matrix is generated from the repository and marks missing old artifacts as
retrospective gaps rather than invented evidence.

### Previous accepted release evidence

- `docs/v.1.4.48/RELEASE_META.json`
- `docs/v.1.4.48/RELEASE.md`
- `docs/v.1.4.48/REGRESSION_CHECKLIST.md`
- `docs/v.1.4.48/qa/STABILIZATION_CHECKPOINT.md`
- `docs/v.1.4.48/qa/PHONE_TEST.md`
- `docs/v.1.4.48/qa/TEST_RUN_2026-09-22.md`
- `docs/v.1.4.48/qa/PHONE_TEST_REPORT_2026-09-22.md`
- `docs/v.1.4.48/qa/EVIDENCE_MANIFEST.md`
- `docs/v.1.4.48/diagrams/README.md`

### Current accepted release evidence

- `docs/v.1.4.49/RELEASE_META.json`
- `docs/v.1.4.49/RELEASE.md`
- `docs/v.1.4.49/REGRESSION_CHECKLIST.md`
- `docs/v.1.4.49/qa/STABILIZATION_CHECKPOINT.md`
- `docs/v.1.4.49/qa/PHONE_TEST.md`
- `docs/v.1.4.49/qa/UPDATER_QA_CHANNEL.md`
- `docs/v.1.4.49/qa/TEST_RUN_2026-09-22.md`
- `docs/v.1.4.49/qa/PHONE_TEST_REPORT_2026-09-22.md`
- `docs/v.1.4.49/qa/BUG_REGISTER.md`
- `docs/v.1.4.49/qa/EVIDENCE_MANIFEST.md`
- `docs/v.1.4.49/diagrams/UPDATER_FLOW.md`

### Accepted stable release package

- `docs/v.1.4.50/RELEASE_META.json`
- `docs/v.1.4.50/RELEASE.md`
- `docs/v.1.4.50/REGRESSION_CHECKLIST.md`
- `docs/v.1.4.50/qa/STABILIZATION_CHECKPOINT.md`
- `docs/v.1.4.50/qa/PHONE_TEST.md`
- `docs/v.1.4.50/qa/BUG_REGISTER.md`
- `docs/v.1.4.50/qa/EVIDENCE_MANIFEST.md`
- `docs/v.1.4.50/diagrams/README.md`

### Active development release package

- `docs/v.1.4.51/RELEASE_META.json`
- `docs/v.1.4.51/RELEASE.md`
- `docs/v.1.4.51/URL_SNAPSHOT_CONTRACT.md`
- `docs/v.1.4.51/URL_SOURCE_MATRIX.md`
- `docs/v.1.4.51/RESOLVER_CONTRACT.md`
- `docs/v.1.4.51/PREVIEW_CONTRACT.md`
- `docs/v.1.4.51/COMMIT_CONTRACT.md`
- `docs/v.1.4.51/REGRESSION_CHECKLIST.md`
- `docs/v.1.4.51/qa/PHONE_TEST.md`
- `docs/v.1.4.51/qa/BUG_REGISTER.md`
- `docs/v.1.4.51/qa/EVIDENCE_MANIFEST.md`
- `docs/v.1.4.51/diagrams/README.md`
- `docs/v.1.4.51/diagrams/URL_SNAPSHOT_FLOW.md`

## Audit rule

Do not guess which old audit matters.

Read `docs/assistant-kit/AUDIT_CATALOG.md`.

When modifying behavior covered by an audit:

1. read the exact audit;
2. decide whether the old invariant still applies;
3. update the audit only when the product contract intentionally changed;
4. never weaken an audit merely to make a new implementation pass.

## Documentation drift rule

A completed release must not silently lose:

- release description;
- regression checklist;
- phone-test plan;
- bug register;
- evidence manifest;
- system/test diagrams;
- executed test run and report when phone QA was performed.

`scripts/release-documentation-audit.sh` enforces the package contract.
`scripts/release-close-audit.sh` prevents final closeout when metadata, evidence,
history/status files, diagrams or the tested-source tag are missing.

## Portable project skeleton

Reusable cross-project knowledge lives under:

`docs/assistant-kit/portable/`

The exporter is:

`python -B scripts/export-assistant-project-skeleton.py`

It creates one archive with:

1. a generic portable project skeleton;
2. current YTM-specific context as a worked reference;
3. a curated system/lifecycle/navigation audit subset.

The archive intentionally contains no OAuth tokens, signing keys, keystores or
private evidence.

## File manifest rule

`FILE_MANIFEST.txt` is generated, not hand-maintained.

Use:

`python -B scripts/generate-file-manifest.py`

Release preflight verifies that the manifest is current.

## Key principle

The repository must contain enough information for another assistant to
understand what the project is, how work is performed, what must never regress,
what is actually phone-tested, which audits protect which contracts, how
build/release works, and what comes next.

## Phone-side code handoff

- `docs/assistant-kit/YTM_CODE_HANDOFF_CONTRACT.md`

Substantial repository changes should normally be delivered as correctly
structured `YTM_*.zip` packages for the user's `ytm-code` runner.
