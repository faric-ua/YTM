# YTM Importer — Release Documentation Contract

This is the mandatory release-package policy.

## Release start

Create the documentation package **before the first application-code change**:

```bash
python -B scripts/create-release-docs.py \
  --version X.Y.Z \
  --code N \
  --feature "Feature name" \
  --branch "branch-name"
```

Every release begins with:

- `RELEASE_META.json`;
- `RELEASE.md`;
- `REGRESSION_CHECKLIST.md`;
- `qa/PHONE_TEST.md`;
- `qa/BUG_REGISTER.md`;
- `qa/EVIDENCE_MANIFEST.md`;
- `diagrams/README.md`.

A user-visible/system-flow release must have at least one real Mermaid flow/test
diagram before final closeout.

## Release metadata

`RELEASE_META.json` is the machine-readable identity for the release.

Schema 1 records:

- versionName;
- versionCode;
- phase: planned / development / final;
- feature;
- branch;
- exact tested app-source SHA;
- signed Actions run;
- QA status;
- release tag;
- optional checkpoint tag;
- phone-test date.

Do not put secrets/tokens in release metadata.

## Phone-tested package

If phone QA is executed, add:

- exact APK version/build/source commit;
- exact GitHub Actions run;
- `qa/TEST_RUN_YYYY-MM-DD.md`;
- `qa/PHONE_TEST_REPORT_YYYY-MM-DD.md`;
- PASS/FAIL/BLOCKED/SKIP without broadening tested scope.

## Evidence

Do not invent missing screenshot/video evidence.

If evidence existed only in a development conversation and was not committed,
record that limitation explicitly.

## System changes

A release touching any of the following must update relevant reusable
contracts/audits when the invariant itself changes:

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

## Release gates

Core package:

```bash
bash scripts/release-documentation-audit.sh X.Y.Z
```

Final closeout:

```bash
bash scripts/release-close-audit.sh X.Y.Z
```

Final closeout additionally requires:

- metadata phase `final`;
- exact app source SHA;
- signed Actions run;
- release tag pointing to the tested app source;
- current app version/code matching metadata;
- executed test run/report;
- stabilization checkpoint;
- `CHANGELOG.md`;
- `BACKLOG.md`;
- `RELEASE_TEST_STATUS.md`;
- `CURRENT_HANDOFF.md`;
- `PROJECT_STATUS.txt`;
- current historical documentation matrix.

## Historical matrix

Generate:

```bash
python -B scripts/generate-release-documentation-matrix.py
```

Check:

```bash
python -B scripts/generate-release-documentation-matrix.py --check
```

The matrix reports old missing artifacts as `RETRO GAP`. It must never fabricate
historical evidence.

## Historical gaps

Detailed historical context remains in:

`docs/documentation/DOCUMENTATION_GAP_AUDIT_2026-09-22.md`
