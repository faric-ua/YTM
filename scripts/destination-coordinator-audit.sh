#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
COORD="app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"

[ -f "$MAIN" ] || fail "MainActivity missing"
[ -f "$COORD" ] || fail "DestinationCoordinator missing"

grep -q 'class DestinationCoordinator' "$COORD" \
  || fail "DestinationCoordinator class missing"
grep -q 'fun currentTracksForDestination' "$COORD" \
  || fail "eligible-track selection not extracted"
grep -q 'fun loadExistingPlaylists' "$COORD" \
  || fail "playlist loading not extracted"
grep -q 'fun selectExistingTarget' "$COORD" \
  || fail "target selection not extracted"
grep -q 'fun scanDuplicates' "$COORD" \
  || fail "duplicate scan not extracted"
grep -q 'fun buildExistingWritePlan' "$COORD" \
  || fail "duplicate write plan not extracted"
grep -q 'api.listPlaylistVideoIds' "$COORD" \
  || fail "playlist duplicate API orchestration missing"
grep -q 'QuotaTracker.SIMPLE_LIST_COST' "$COORD" \
  || fail "destination scan quota accounting missing"

grep -q 'private lateinit var destinationCoordinator: DestinationCoordinator' "$MAIN" \
  || fail "MainActivity coordinator field missing"
grep -q 'destinationCoordinator.scanDuplicates' "$MAIN" \
  || fail "MainActivity does not delegate duplicate scan"
grep -q 'destinationCoordinator.buildExistingWritePlan' "$MAIN" \
  || fail "MainActivity does not delegate duplicate write planning"

if grep -q 'private data class DuplicateAnalysis' "$MAIN"; then
  fail "DuplicateAnalysis still owned by MainActivity"
fi
if grep -q 'private data class DuplicateWritePlan' "$MAIN"; then
  fail "DuplicateWritePlan still owned by MainActivity"
fi
if grep -q 'private var destinationPlaylists' "$MAIN"; then
  fail "destination playlist cache still owned by MainActivity"
fi
if grep -q 'private var pendingDestinationAnalysis' "$MAIN"; then
  fail "pending duplicate analysis still owned by MainActivity"
fi
if grep -q 'api.listPlaylistVideoIds' "$MAIN"; then
  fail "MainActivity still calls duplicate scan API directly"
fi
if grep -q 'api.listMyPlaylists' "$MAIN"; then
  fail "MainActivity still calls destination playlist API directly"
fi

echo "PASS:"
echo "- DestinationCoordinator owns destination domain state"
echo "- playlist loading delegated"
echo "- duplicate scan/quota accounting delegated"
echo "- duplicate analysis/write planning delegated"
echo "- MainActivity remains auth/UI/executor bridge"
