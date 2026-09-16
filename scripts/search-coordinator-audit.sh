#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
COORD="app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt"

test -f "$COORD" || fail "SearchCoordinator.kt missing"

grep -q 'class SearchCoordinator' "$COORD" \
  || fail "SearchCoordinator class missing"

grep -q 'fun plan(' "$COORD" \
  || fail "SearchCoordinator.plan missing"

grep -q 'fun run(' "$COORD" \
  || fail "SearchCoordinator.run missing"

grep -q 'api.search(' "$COORD" \
  || fail "YouTube search call not owned by SearchCoordinator"

grep -q 'searchCache.get(' "$COORD" \
  || fail "SearchCoordinator cache read missing"

grep -q 'searchCache.put(' "$COORD" \
  || fail "SearchCoordinator cache write missing"

grep -q 'quotaTracker.recordSearchCall()' "$COORD" \
  || fail "SearchCoordinator quota accounting missing"

grep -q 'quotaTracker.recordCacheHit()' "$COORD" \
  || fail "SearchCoordinator cache-hit accounting missing"

grep -q 'AUTO_MATCH_THRESHOLD' "$COORD" \
  || fail "automatic match threshold missing"

if grep -q '^import android\\.' "$COORD"; then
  fail "SearchCoordinator must not import Android UI classes"
fi

if grep -q 'api.search(' "$MAIN"; then
  fail "MainActivity still calls YouTube search API directly"
fi

if grep -q 'searchCache\\.get\\|searchCache\\.put' "$MAIN"; then
  fail "MainActivity still owns SearchCache domain flow"
fi

if grep -q 'recordSearchCall()' "$MAIN"; then
  fail "MainActivity still owns search quota accounting"
fi

if grep -q 'private fun applySearchCandidates' "$MAIN"; then
  fail "MainActivity still owns automatic search candidate selection"
fi

grep -q 'searchCoordinator.plan(' "$MAIN" \
  || fail "MainActivity does not delegate search planning"

grep -q 'searchCoordinator.run(' "$MAIN" \
  || fail "MainActivity does not delegate search execution"

grep -q 'SearchCoordinator.PreservedSelection.MANUAL' "$MAIN" \
  || fail "MainActivity progress bridge does not preserve manual selection message"

grep -q 'SearchCoordinator.PreservedSelection.PROJECT_EXACT' "$MAIN" \
  || fail "MainActivity progress bridge does not preserve project-exact message"

echo "PASS:"
echo "- SearchCoordinator owns planning/cache/API/quota/search-state domain logic"
echo "- MainActivity owns only authorization + UI progress bridge"
echo "- manual and Project exact selections remain protected"
echo "- coordinator contains no Android UI imports"
