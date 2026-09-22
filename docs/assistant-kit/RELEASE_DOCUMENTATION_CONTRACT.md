
# YTM Importer — Release Documentation Contract

This is the current mandatory release-package policy.

## Core package

Every new release folder must contain:

- `RELEASE.md`;
- `REGRESSION_CHECKLIST.md`;
- `qa/PHONE_TEST.md`;
- `qa/BUG_REGISTER.md`;
- `qa/EVIDENCE_MANIFEST.md`;
- `diagrams/README.md`;
- at least one current flow/test diagram when the release changes user-visible
  flow or system behavior.

## Phone-tested package

If phone QA is executed, record:

- exact APK version/build/source commit;
- exact GitHub Actions run where known;
- `qa/TEST_RUN_YYYY-MM-DD.md`;
- `qa/PHONE_TEST_REPORT_YYYY-MM-DD.md`;
- PASS/FAIL/BLOCKED/SKIP without broadening tested scope.

## Evidence

Do not invent missing screenshot/video evidence.

If an observation existed only in the development conversation and the binary
was never committed to Git, record that fact explicitly.

## System changes

A release touching any of the following must update the relevant reusable
contract/audit when the invariant itself changes:

- rotation / Activity recreation;
- navigation parent ownership;
- Back / Cancel;
- dialogs / Help / confirmation / result windows;
- progress ownership;
- remote-operation lifetime;
- form drafts / IME;
- SAF/system picker ownership;
- updater;
- theme/skin lifecycle.

## Release gate

`scripts/release-documentation-audit.sh` verifies the release package.

A signed build may exist before phone QA.

A final PHONE-QA checkpoint must additionally have executed test-run/report
evidence.

## Historical gaps

Do not rewrite old release history to look cleaner.

Missing historical documentation is tracked in:

`docs/documentation/DOCUMENTATION_GAP_AUDIT_2026-09-22.md`
