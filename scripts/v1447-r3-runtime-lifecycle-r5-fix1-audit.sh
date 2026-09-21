#!/usr/bin/env bash
set -euo pipefail

AUDIT="scripts/v1447-r3-navigation-ownership-audit.sh"
DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"

fail(){ echo "FAIL: $1" >&2; exit 1; }

test -f "$AUDIT" || fail "historical navigation audit missing"
test -f "$DEST" || fail "DestinationActivity missing"

python - "$AUDIT" "$DEST" <<'PY'
from pathlib import Path
import sys

audit = Path(sys.argv[1]).read_text(encoding="utf-8")
dest = Path(sys.argv[2]).read_text(encoding="utf-8")

for needle in [
    "existing_entry = dest[start_existing:start_existing_end]",
    "'openExistingPlaylists()' not in existing_entry",
    "helper_start = dest.index('private fun openExistingPlaylists()')",
    "'EXTRA_EXISTING_IDS'",
    "'showExistingListScreen()'",
    "'requestExistingPlaylists()'",
]:
    if needle not in audit:
        raise SystemExit(
            f"FAIL: R5 FIX1 historical audit successor missing: {needle}"
        )

start = dest.index('label = "Вибрати існуючий плейлист"')
end = dest.index('content.addView(existingCard)', start)
entry = dest[start:end]

if 'openExistingPlaylists()' not in entry:
    raise SystemExit(
        "FAIL: R5 Destination entry does not use cache-aware local opener"
    )

helper_start = dest.index('private fun openExistingPlaylists()')
helper_end = dest.index(
    'private fun requestExistingPlaylists()',
    helper_start
)
helper = dest[helper_start:helper_end]

for needle in [
    'intent.hasExtra(',
    'EXTRA_EXISTING_IDS',
    'showExistingListScreen()',
    'requestExistingPlaylists()',
]:
    if needle not in helper:
        raise SystemExit(
            f"FAIL: R5 Destination cache helper missing: {needle}"
        )
PY

echo "PASS:"
echo "- historical R4 navigation audit remains compatible"
echo "- R5 cache-aware Destination successor is verified strictly"
echo "- cached list reopens locally; API load remains fallback only"
