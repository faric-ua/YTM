# YTM Importer — Test evidence policy

This file defines how phone-test evidence is stored from v1.4.16 onward.

## Rule

Every release that is tested on a phone must keep its actual test evidence inside that release snapshot:

`docs/v.X.Y.Z/qa/`

The current global test definitions remain under:

`qa/`

## Per-release evidence

When phone tests are run, add as applicable:
- `TEST_RUN_YYYY-MM-DD.md` — exact executed cases and PASS/FAIL/BLOCKED/SKIP;
- `PHONE_TEST_REPORT_YYYY-MM-DD.md` — narrative analysis and release conclusion;
- `UI_SCREENSHOT_ANALYSIS_YYYY-MM-DD.md` — screenshot/layout observations;
- release-specific bug notes in `BUG_REGISTER.md`;
- diagrams under `docs/v.X.Y.Z/diagrams/`.

## Screenshot binaries

Do not commit every screenshot by default.

Prefer text analysis because it is searchable, diffable, small and easy to preserve in Git history.

Commit selected compressed screenshots under `docs/v.X.Y.Z/qa/evidence/` only when an image is materially useful to reproduce or understand a layout bug, visual regression, error dialog or state-machine discrepancy.


## Screenshot privacy / redaction

Before a screenshot is committed to the public repository, redact or blur
personal information that is not required to understand the test:

- email addresses;
- personal account names when identifying;
- Channel IDs;
- tokens, keys and secrets;
- unrelated personal identifiers.

Keep the UI state or defect itself visible.

Store only the cleaned copy in:
`docs/v.X.Y.Z/qa/evidence/`

The original private screenshot does not need to be committed.

## Status discipline

Static audits and successful CI do not equal phone testing.

A release moves through:
- NOT TESTED YET
- PARTIALLY PHONE-TESTED
- PHONE-TESTED

or a failure/blocking variant when appropriate.

Never mark a case PASS if the available evidence only proves that the screen existed or that an action was offered.

## Test data

Prefer real, verifiable YouTube/YTM tracks for normal functional tests.

Synthetic/broken data is appropriate only for explicit negative/error cases such as malformed CSV, invalid URL, missing videoId, duplicate stress cases or damaged backup.

When synthetic data is used, label it clearly in the test report.

## Exact test input files

Files actually used during release testing belong in:

`docs/v.X.Y.Z/qa/test-data/`

This includes CSV/TXT/project fixtures needed to reproduce the run.
