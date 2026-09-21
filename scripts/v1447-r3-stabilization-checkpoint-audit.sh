#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
QUOTA="app/src/main/java/com/saney/ytmimporter/storage/QuotaTracker.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
WRITE="app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt"
CHECKPOINT="docs/v.1.4.47/qa/R3_STABILIZATION_CHECKPOINT.md"

for f in "$DEST" "$API" "$QUOTA" "$MAIN" "$SERVICE" "$WRITE" "$CHECKPOINT"; do
  test -f "$f" || fail "missing stabilization file: $f"
done

for needle in \
  'pendingDeleteConfirmation' \
  'STATE_DELETE_CONFIRM_ID' \
  'showDeleteConfirmationDialog' \
  'existingPlaylistQuery' \
  'STATE_EXISTING_PLAYLIST_QUERY' \
  'setText(existingPlaylistQuery)'
do
  grep -Fq "$needle" "$DEST" ||
    fail "Destination lifecycle contract missing: $needle"
done

grep -Fq 'fun deletePlaylist(' "$API" ||
  fail "playlist delete API missing"

grep -Fq 'PLAYLIST_DELETE_COST = 50' "$QUOTA" ||
  fail "playlist delete quota cost missing"

grep -Fq 'ServiceActivity.START_PAGE_ABOUT' "$MAIN" ||
  fail "version badge About shortcut missing"

grep -Fq 'START_PAGE_ABOUT = "ABOUT"' "$SERVICE" ||
  fail "About start-page contract missing"

python - "$WRITE" <<'PY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text()
start = text.index("    fun execute(")
block = text[start:start + 3000]

for needle in [
    "tracks.forEach { track ->",
    "track.status = TrackStatus.PENDING",
    "track.error = null",
]:
    if needle not in block:
        raise SystemExit(
            "FAIL: write-state reset missing: " + needle
        )
PY

grep -Fq 'BUG-027' "$CHECKPOINT" ||
  fail "BUG-027 evidence missing"

grep -Fq 'BUG-028' "$CHECKPOINT" ||
  fail "BUG-028 evidence missing"

grep -Fq '197da0c6afd7c1f41544e0d39b1dc17e2c7c156f' "$CHECKPOINT" ||
  fail "tested source SHA missing"

grep -Fq '35667160072' "$CHECKPOINT" ||
  fail "signed run evidence missing"

echo "PASS:"
echo "- version badge -> About"
echo "- write-state reset contract"
echo "- playlist deletion contract"
echo "- delete-confirm rotation"
echo "- search/filter rotation"
echo "- signed checkpoint evidence"
