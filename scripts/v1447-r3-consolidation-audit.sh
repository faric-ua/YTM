#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
STATUS="RELEASE_TEST_STATUS.md"
START="START_HERE_ASSISTANT.md"
HANDOFF="CURRENT_HANDOFF.md"
PROJECT="PROJECT_STATUS.txt"
BACKLOG="BACKLOG.md"
BUGS="qa/BUG_REGISTER.md"
R3="docs/v.1.4.47/R3.md"
PHONE="docs/v.1.4.47/qa/PHONE_TEST_R3.md"
CHECKPOINT="docs/v.1.4.47/qa/R3_STABILIZATION_CHECKPOINT.md"

for f in "$GRADLE" "$STATUS" "$START" "$HANDOFF" "$PROJECT" "$BACKLOG" "$BUGS" "$R3" "$PHONE" "$CHECKPOINT"; do
  test -f "$f" || fail "missing R3 consolidation file: $f"
done

grep -Fq 'versionName: **1.4.47-R3**' "$R3" || fail "R3 release doc version missing"
grep -Fq 'versionCode: **90**' "$R3" || fail "R3 release doc versionCode missing"
grep -Fq 'app-source commit: `197da0c6afd7c1f41544e0d39b1dc17e2c7c156f`' "$CHECKPOINT" ||
  fail "R3 tested application source missing"

for wave in \
  docs/v.1.4.47/qa/R3_LIFECYCLE_WAVE1.md \
  docs/v.1.4.47/qa/R3_OAUTH_RETRY.md \
  docs/v.1.4.47/qa/R3_HISTORY_SEMANTICS.md
do
  test -f "$wave" || fail "missing R3 wave evidence: $wave"
done

grep -Fq 'dd7e8d5e984200b9c2cdca993bc8378a2db4198b' "$R3" || fail "Wave 1 commit evidence missing"
grep -Fq 'f53fc1d3ad8a02c67269c9f22df8e4cc8b2f2f14' "$R3" || fail "Wave 2 commit evidence missing"
grep -Fq 'e1fee8ed989acfd1209e53873910bf3f362e5ec0' "$R3" || fail "Wave 3 commit evidence missing"

grep -Fq '| v1.4.47-R3 | **PARTIALLY PHONE-TESTED — STABILIZATION CHECKPOINT PASS / BROADER R3 QA DEFERRED** |' "$STATUS" ||
  fail "R3 release status row missing"
grep -Fq '| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SUPERSEDED BY R3 BEFORE SIGNED BUILD/PHONE QA** |' "$STATUS" ||
  fail "R2 superseded status missing"

grep -Fq '## v1.4.47-R3 — Lifecycle + OAuth Recovery + History Semantics' "$BACKLOG" ||
  fail "BACKLOG historical R3 section missing"
grep -Fq 'checkpoint-v1.4.47-R3-phone-pass' "$START" ||
  fail "START_HERE stable R3 checkpoint reference missing"
grep -Fq 'tested R3 app checkpoint remains' "$HANDOFF" ||
  fail "CURRENT_HANDOFF R3 checkpoint history missing"

grep -F '| BUG-004 / Q-004 |' "$BUGS" |
  grep -Fq 'R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED' ||
  fail "BUG-004 R3 status missing"
grep -Fq '| BUG-021 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED |' "$BUGS" || fail "BUG-021 status missing"
grep -Fq '| BUG-022 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED |' "$BUGS" || fail "BUG-022 status missing"
grep -Fq '| BUG-023 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED |' "$BUGS" || fail "BUG-023 status missing"
grep -Fq '| BUG-027 | CLOSED — PHONE RETEST PASS v1.4.47-R3 |' "$BUGS" || fail "BUG-027 checkpoint PASS missing"
grep -Fq '| BUG-028 | CLOSED — PHONE RETEST PASS v1.4.47-R3 |' "$BUGS" || fail "BUG-028 checkpoint PASS missing"
grep -Fq "197da0c6afd7c1f41544e0d39b1dc17e2c7c156f" "$CHECKPOINT" || fail "tested source SHA missing"
grep -Fq "35667160072" "$CHECKPOINT" || fail "signed run evidence missing"

grep -Fq 'DEFERRED — NATURAL 401 NOT REPRODUCED' "$PHONE" || fail "natural-401 deferred rule missing"
grep -Fq 'BLOCKED — NO REPRESENTATIVE HISTORY RECORD' "$PHONE" || fail "History BLOCKED rule missing"

bash scripts/v1447-r3-lifecycle-wave1-audit.sh
bash scripts/v1447-r3-oauth-retry-audit.sh
bash scripts/v1447-r3-history-semantics-audit.sh

echo "PASS:"
echo "- v1.4.47-R3 / code 90 consolidation identity"
echo "- R3 lifecycle Wave 1 contract"
echo "- BUG-004 silent HTTP-401 token recovery + one retry"
echo "- BUG-021 operation-aware History semantics"
echo "- R2 UI work carried forward and marked superseded before signed QA"
echo "- current handoff/status/backlog/bug register point to R3"
echo "- unified R3 phone acceptance plan documented"
