#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
TRACK="app/src/main/java/com/saney/ytmimporter/model/Track.kt"
PENDING_MODEL="app/src/main/java/com/saney/ytmimporter/model/PendingJob.kt"
PENDING_STORE="app/src/main/java/com/saney/ytmimporter/storage/PendingJobStore.kt"
SEARCH="app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt"
RECOVERY="app/src/main/java/com/saney/ytmimporter/search/SearchRecoveryCoordinator.kt"
POLICY="app/src/main/java/com/saney/ytmimporter/search/SearchRecoveryPolicy.kt"
QUEUE="app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
DEST="app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"
QUOTA="app/src/main/java/com/saney/ytmimporter/storage/QuotaTracker.kt"
QUOTA_MATH="app/src/main/java/com/saney/ytmimporter/storage/QuotaMath.kt"

for path in   "$BUILD"   "$TRACK"   "$PENDING_MODEL"   "$PENDING_STORE"   "$SEARCH"   "$RECOVERY"   "$POLICY"   "$QUEUE"   "$MAIN"   "$DEST"   "$QUOTA"   "$QUOTA_MATH"   app/src/test/java/com/saney/ytmimporter/search/SearchRecoveryPolicyTest.kt   app/src/test/java/com/saney/ytmimporter/storage/QuotaMathTest.kt   docs/v.1.4.53/RELEASE.md   docs/v.1.4.53/REGRESSION_CHECKLIST.md   docs/v.1.4.53/qa/PHONE_TEST.md   docs/v.1.4.53/qa/BUG_REGISTER.md   docs/v.1.4.53/diagrams/QUOTA_RECOVERY_FLOW.md
do
  test -f "$path" || fail "missing v1.4.53 artifact: $path"
done

grep -Fq 'versionCode = 96' "$BUILD"   || fail "versionCode 96 missing"
grep -Fq 'versionName = "1.4.53"' "$BUILD"   || fail "versionName 1.4.53 missing"

grep -Fq 'WAITING_QUOTA' "$TRACK"   || fail "WAITING_QUOTA track state missing"
grep -Fq 'SEARCH' "$PENDING_MODEL"   || fail "SEARCH pending operation missing"
grep -Fq 'searchSnapshot' "$PENDING_MODEL"   || fail "PendingJob search snapshot missing"
grep -Fq 'PendingOperation.WRITE' "$PENDING_STORE"   || fail "legacy PendingJob WRITE default missing"
grep -Fq 'findSearchByRecoveryKey' "$PENDING_STORE"   || fail "search recovery lookup missing"
grep -Fq '"searchSnapshot"' "$PENDING_STORE"   || fail "search snapshot JSON persistence missing"

grep -Fq 'TrackStatus.WAITING_QUOTA' "$SEARCH"   || fail "SearchCoordinator does not emit WAITING_QUOTA"
grep -Fq 'resumeWaitingOnly' "$SEARCH"   || fail "SearchCoordinator waiting-only resume missing"
grep -Fq 'waitingQuotaCount' "$SEARCH"   || fail "SearchCoordinator waiting count missing"

grep -Fq 'class SearchRecoveryCoordinator' "$RECOVERY"   || fail "SearchRecoveryCoordinator missing"
grep -Fq 'fun pause(' "$RECOVERY"   || fail "Search recovery pause missing"
grep -Fq 'fun completeIfResolved(' "$RECOVERY"   || fail "Search recovery completion cleanup missing"
grep -Fq 'workspaceKey' "$POLICY"   || fail "deterministic search recovery key missing"

grep -Fq 'PendingOperation.SEARCH' "$QUEUE"   || fail "Queue does not distinguish SEARCH jobs"
grep -Fq 'Продовжити пошук' "$QUEUE"   || fail "Queue Search Resume action missing"
grep -Fq 'SearchRecoveryCoordinator' "$MAIN"   || fail "MainActivity search recovery bridge missing"
grep -Fq 'resumePendingSearchJob' "$MAIN"   || fail "MainActivity Search Resume route missing"
grep -Fq 'TrackStatus.WAITING_QUOTA' "$DEST"   || fail "destination write does not exclude WAITING_QUOTA"

grep -Fq 'SEARCH_DAILY_LIMIT = 100' "$QUOTA"   || fail "Search daily bucket missing"
grep -Fq 'QuotaMath.generalUnits' "$QUOTA"   || fail "general quota computation missing"
grep -Fq 'QuotaMath.searchRemaining' "$QUOTA"   || fail "Search quota computation missing"
if grep -Fq 'SEARCH_LIST_COST' "$QUOTA"; then
  fail "Search must not be charged into the general 10k-unit bucket"
fi

grep -Fq 'BUG-038' docs/v.1.4.53/qa/BUG_REGISTER.md   || fail "BUG-038 investigation not carried into release"
grep -Fq 'root cause not yet proven' docs/v.1.4.53/qa/BUG_REGISTER.md   || fail "BUG-038 uncertainty guard missing"

python -B - <<'PY'
from pathlib import Path

main = Path(
    "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
).read_text(encoding="utf-8")
queue = Path(
    "app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"
).read_text(encoding="utf-8")
dest = Path(
    "app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"
).read_text(encoding="utf-8")

if len(main.splitlines()) >= 4100:
    raise SystemExit("FAIL: v1.4.53 regressed MainActivity size budget")

if "startSearch(" not in main or "resumeWaitingOnly = true" not in main:
    raise SystemExit("FAIL: explicit waiting-only Search resume bridge missing")

if "PendingOperation.SEARCH" not in queue:
    raise SystemExit("FAIL: Queue Search operation rendering missing")

if (
    "track.status != TrackStatus.WAITING_QUOTA"
    not in dest
):
    raise SystemExit(
        "FAIL: WAITING_QUOTA track can leak into destination write"
    )
PY

echo "PASS:"
echo "- v1.4.53 identity"
echo "- durable Search recovery payload + Queue route"
echo "- waiting-quota Search semantics"
echo "- write PendingJob backward compatibility"
echo "- granular Search quota separated from general 10k-unit bucket"
echo "- WAITING_QUOTA blocked from destination write"
echo "- BUG-038 remains evidence-driven / no speculative closure"
echo "- v1.4.53 JVM policy tests + release docs present"
