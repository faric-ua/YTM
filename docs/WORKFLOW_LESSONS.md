# YTM Importer — Workflow Lessons Learned

These are real project failures/near-failures. Keep them because they explain why the workflow has specific guards.

## 1. Package-root overwrite

A package once contained a top-level `README.md`. Copying the whole package root into the repository overwrote the repository README.

Guard:

- prefer `overlay/`;
- keep package-only helpers outside the overlay;
- explicitly test for package-root collision files.

## 2. Mutable historical audit coupling

A historical v1.4.26 self-test called a current mutable audit. Later, the current status legitimately evolved, so the historical self-test failed even though the historical release fixture was correct.

Guard:

- historical audits should validate immutable release snapshots;
- current mutable status belongs to current QA audits;
- do not make old release self-tests depend on future root status wording.

## 3. Literal `\n` in multiline anchors

Generated patch definitions can accidentally contain the two characters backslash+n instead of real line breaks.

Guard:

- reject literal `\n` in multiline patch anchors;
- test patch definitions before delivery.

## 4. Python cache artifacts

Running Python helpers can create `__pycache__` and `.pyc` inside the repository.

Guard:

- use `python -B`;
- set `PYTHONDONTWRITEBYTECODE=1` when useful;
- self-tests and final leftover checks reject cache artifacts.

## 5. CRLF / trailing whitespace / blank line at EOF

Generated Markdown/CSV/scripts can look fine but fail `git diff --check`.

Guard:

- generate LF-only text;
- CSV writers use `lineterminator="\n"`;
- reject trailing whitespace;
- normalize to exactly one LF at EOF;
- always run `git diff --check`.

## 6. Broad false-positive audit — v1.4.27 R1/R2/R3

The first BUG-005 audit searched the entire `MainActivity.kt` for:

`preserveExistingExact: Boolean = false`

That incorrectly treated a legitimate local `startSearch(... = false)` default as if the unsafe `searchAll()` default remained.

R2 fixed one audit but missed the second audit script, so the phone preflight failed again.

R3 corrected the remaining audit to check the exact unsafe `searchAll(...)` signature.

Guards:

- audits must target the exact invariant, not a broad substring;
- if two audits encode the same invariant, update/test both;
- self-test with a realistic fixture that includes legitimate look-alike code.

## 7. Leftover staging guard caught a missing intended file

During the v1.4.27 stage/commit flow, `scripts/v1426-selective-export-audit.sh` was intentionally changed by historical-audit hardening but omitted from the exact stage list.

The leftover check stopped the commit.

Guard:

- after staging exact files, require both unstaged and untracked sets to be empty for a release package;
- do not bypass this stop just because preflight passed.

## 8. APK placement inconsistency

One v1.4.27 command copied the APK directly into the root of Android `Download/`, while previous releases used versioned build folders.

This confused the user's established file-management pattern.

Guard:

Stable convention:

`/storage/emulated/0/Download/YTM-vX.Y.Z-build/`

with APK + SHA file inside.

## 9. A successful persistence test is not enough

v1.4.26 proved exact `videoId` survived account export → YTM Project → import, but downstream repeat-search still planned redundant quota work.

Guard:

For identifier-rich round trips, test:

1. persistence invariant;
2. next meaningful behavior using the persisted identifier.

This lesson became BUG-005 and tutorial chapter `11_SEARCH_AND_EXACT_VIDEO_ID.md`.

## 10. Safety checks are product infrastructure

`--check`, idempotence, deletion guards, leftover guards, immutable QA snapshots and evidence sanitization are not bureaucracy.

They are what makes the phone-based ChatGPT development loop repeatable and safe.
