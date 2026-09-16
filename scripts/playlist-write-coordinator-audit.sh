#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
COORD="app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt"

test -f "$COORD" || fail "PlaylistWriteCoordinator.kt missing"
grep -q 'class PlaylistWriteCoordinator' "$COORD" || fail "class missing"
grep -q 'fun buildPendingJob(' "$COORD" || fail "PendingJob factory missing"
grep -q 'fun execute(' "$COORD" || fail "execute missing"
grep -q 'api.createPlaylist(' "$COORD" || fail "create API not moved"
grep -q 'api.addVideo(' "$COORD" || fail "add API not moved"
grep -q 'pendingJobStore.upsert' "$COORD" || fail "upsert missing"
grep -q 'pendingJobStore.remove' "$COORD" || fail "remove missing"
grep -q 'HistoryStatus.PENDING_QUOTA' "$COORD" || fail "quota history missing"
grep -q 'TrackStatus.PENDING' "$COORD" || fail "pending track state missing"

if grep -q '^import android\\.' "$COORD"; then
  fail "coordinator must not import Android UI"
fi
if grep -q 'api.createPlaylist(' "$MAIN"; then
  fail "MainActivity still creates playlist directly"
fi
if grep -q 'api.addVideo(' "$MAIN"; then
  fail "MainActivity still inserts playlist item directly"
fi
if grep -q 'private fun buildPendingJob' "$MAIN"; then
  fail "MainActivity still owns PendingJob factory"
fi
if grep -q 'pendingTrackToTrack' "$MAIN"; then
  fail "MainActivity still owns PendingTrack conversion"
fi

grep -q 'playlistWriteCoordinator.execute(' "$MAIN" || fail "execute delegation missing"
grep -q 'playlistWriteCoordinator.buildPendingJob(' "$MAIN" || fail "job delegation missing"

echo "PASS:"
echo "- write loop extracted"
echo "- PendingJob lifecycle extracted"
echo "- quota pause/completion outcomes extracted"
echo "- MainActivity remains UI/auth/history bridge"
