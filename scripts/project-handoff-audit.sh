#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

for f in \
  START_HERE_ASSISTANT.md \
  README.md \
  YTM_ASSISTANT_WORKFLOW.md \
  TERMUX_COMMANDS.md \
  PROJECT_STATUS.txt \
  BACKLOG.md \
  RELEASE_TEST_STATUS.md \
  qa/BUG_REGISTER.md \
  docs/ASSISTANT_TOOL_MAP.md \
  docs/WORKFLOW_LESSONS.md \
  docs/BUILD_ARTIFACT_CONVENTION.md \
  docs/HANDOFF_HARDENING_2026-09-17.md
do
  test -f "$f" || fail "missing handoff file: $f"
done

grep -Fq 'Repository: `faric-ua/YTM`' START_HERE_ASSISTANT.md \
  || fail "START_HERE repository identity missing"
grep -Fq 'versionName: **1.4.29**' START_HERE_ASSISTANT.md \
  || fail "START_HERE current version missing"
grep -Fq 'versionCode: **63**' START_HERE_ASSISTANT.md \
  || fail "START_HERE current versionCode missing"
grep -Fq 'BUG-005 / Q-005' START_HERE_ASSISTANT.md \
  || fail "START_HERE BUG-005 identity missing"
grep -Fq 'CLOSED — PHONE RETEST PASS v1.4.27' START_HERE_ASSISTANT.md \
  || fail "START_HERE BUG-005 closed state missing"
grep -Fq 'START_HERE_ASSISTANT.md' README.md \
  || fail "README does not point new assistants to START_HERE"
if grep -Fq '# v1.4.20 phone-QA closeout' README.md; then
  fail "stale v1.4.20 closeout README returned"
fi

grep -Fq 'Version: 1.1' YTM_ASSISTANT_WORKFLOW.md \
  || fail "assistant workflow version 1.1 missing"
grep -Fq 'Stable phone-side build artifact convention' YTM_ASSISTANT_WORKFLOW.md \
  || fail "artifact convention missing from workflow policy"
grep -Fq 'New assistant handoff' YTM_ASSISTANT_WORKFLOW.md \
  || fail "new assistant handoff section missing"

grep -Fq 'Do **not** use `git add -A` as the routine default.' TERMUX_COMMANDS.md \
  || fail "Termux guide still lacks exact-staging rule"
grep -Fq 'git diff --cached --diff-filter=D --name-status' TERMUX_COMMANDS.md \
  || fail "Termux staged deletion guard missing"
grep -Fq '/storage/emulated/0/Download/YTM-vX.Y.Z-build/' TERMUX_COMMANDS.md \
  || fail "Termux stable build folder missing"

grep -Fq 'GitHub repository access' docs/ASSISTANT_TOOL_MAP.md \
  || fail "tool map GitHub section missing"
grep -Fq 'Android phone + Termux' docs/ASSISTANT_TOOL_MAP.md \
  || fail "tool map Termux section missing"
grep -Fq 'GitHub Actions' docs/ASSISTANT_TOOL_MAP.md \
  || fail "tool map Actions section missing"

for lesson in \
  'Package-root overwrite' \
  'Mutable historical audit coupling' \
  'Literal `\n` in multiline anchors' \
  'Python cache artifacts' \
  'Broad false-positive audit — v1.4.27 R1/R2/R3' \
  'Leftover staging guard caught a missing intended file' \
  'APK placement inconsistency'
do
  grep -Fq "$lesson" docs/WORKFLOW_LESSONS.md \
    || fail "workflow lesson missing: $lesson"
done

grep -Fq 'Historical structural audits must not pin the current app version' docs/WORKFLOW_LESSONS.md \
  || fail "v1.4.28 historical-audit lesson missing"

grep -Fq '/storage/emulated/0/Download/YTM-vX.Y.Z-build/' \
  docs/BUILD_ARTIFACT_CONVENTION.md \
  || fail "build artifact standard path missing"

grep -Fq 'Version: 1.4.29' PROJECT_STATUS.txt \
  || fail "PROJECT_STATUS version drift"
grep -Fq 'Version code: 63' PROJECT_STATUS.txt \
  || fail "PROJECT_STATUS versionCode drift"
grep -Fq 'BUG-005/Q-005 CLOSED — PHONE RETEST PASS v1.4.27' PROJECT_STATUS.txt \
  || fail "PROJECT_STATUS BUG-005 state drift"

grep -Fq 'Project handoff / documentation hardening — COMPLETE' BACKLOG.md \
  || fail "BACKLOG handoff completion missing"

echo "PASS:"
echo "- canonical START_HERE entry point"
echo "- README project entry page"
echo "- current version/bug handoff state"
echo "- tool map"
echo "- workflow lessons"
echo "- exact-path staging rules"
echo "- stable Android build-artifact folder"
echo "- project handoff documentation hardening"
