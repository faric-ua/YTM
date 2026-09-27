#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncPreflightPolicy.kt"
PREVIEW="app/src/main/java/com/saney/ytmimporter/BulkSyncPreviewActivity.kt"
MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
MANIFEST="app/src/main/AndroidManifest.xml"
TEST="app/src/test/java/com/saney/ytmimporter/bulk/BulkSyncPreflightPolicyTest.kt"

for file in "$POLICY" "$PREVIEW" "$MENU" "$MANIFEST" "$TEST"; do
  [ -f "$file" ] || fail "Missing Bulk Sync foundation file: $file"
done

for state in NEW LINKED ALREADY_SYNCED NEEDS_SEARCH PENDING BLOCKED; do
  grep -Fq "$state" "$POLICY" || fail "Missing Bulk Sync state: $state"
done

grep -Fq 'Синхронізувати всі' "$MENU"   || fail "Bulk Sync menu entry missing"

grep -Fq 'android:name=".BulkSyncPreviewActivity"' "$MANIFEST"   || fail "Bulk Sync preview activity is not registered"

grep -Fq 'listMyPlaylists' "$PREVIEW"   || fail "Bulk preview remote inventory read missing"

grep -Fq 'listPlaylistSnapshotItems' "$PREVIEW"   || fail "Bulk preview linked-playlist snapshot read missing"

grep -Fq 'putSerializable' "$PREVIEW"   || fail "Bulk preview plan is not saved across recreation"

grep -Fq 'estimatedSearchCalls' "$PREVIEW"   || fail "Search estimate missing from Bulk preview"

grep -Fq 'estimatedWriteUnits' "$PREVIEW"   || fail "Non-Search estimate missing from Bulk preview"

grep -Fq 'isEnabled = false' "$PREVIEW"   || fail "Bulk execution gate must remain disabled in Test 3 foundation"

for forbidden in   'createPlaylist('   'insertPlaylistItem('   'deletePlaylist('   'deletePlaylistItem('; do
  if grep -Fq "$forbidden" "$PREVIEW"; then
    fail "Remote mutation leaked into read-only Bulk preview: $forbidden"
  fi
done

grep -Fq 'searchAndOtherApiEstimates_staySeparate' "$TEST"   || fail "Bulk quota-separation JVM coverage missing"

echo "PASS: v1.4.54 Bulk Sync Test 3 read-only foundation"
