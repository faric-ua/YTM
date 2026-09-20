#!/usr/bin/env bash
set -euo pipefail

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

test -f "$MAIN" || fail "MainActivity.kt missing"

MAIN_LINES="$(wc -l < "$MAIN" | tr -d ' ')"

echo "YTM Importer MainActivity audit"
echo "Lines: $MAIN_LINES"

for removed in \
  chooseDestination \
  chooseExistingPlaylist \
  showExistingPlaylistDialog \
  checkDuplicatesBeforeAppend \
  showDuplicateChoiceDialog \
  showDuplicateCheckFailureDialog \
  confirmAppendToExisting \
  choosePrivacyAndCreate \
  confirmCreateWithQuota
do
  if grep -q "private fun ${removed}" "$MAIN"; then
    fail "legacy destination function still present: ${removed}"
  fi
done

for required in \
  openDestinationStart \
  handleDestinationResult \
  loadExistingPlaylistsForDestination \
  checkDuplicatesForDestination \
  finishExistingDestination \
  actuallyAppendToExisting \
  actuallyCreatePlaylist
do
  grep -q "private fun ${required}" "$MAIN" \
    || fail "required DestinationActivity bridge/core missing: ${required}"
done

grep -q 'DestinationActivity::class.java' "$MAIN" \
  || fail "DestinationActivity navigation missing"

echo "PASS:"
echo "- legacy destination AlertDialog flow removed"
echo "- DestinationActivity bridge retained"
echo "- create/append write core retained"
