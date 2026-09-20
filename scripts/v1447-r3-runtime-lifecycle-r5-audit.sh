#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
WRITE="app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt"
RELAY="app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in "$MAIN" "$REVIEW" "$DEST" "$WRITE" "$RELAY" "$MANIFEST"; do
  test -f "$f" || fail "missing R5 runtime file: $f"
done

python - "$MAIN" "$REVIEW" "$DEST" "$WRITE" "$RELAY" "$MANIFEST" <<'PY'
from pathlib import Path
import sys

main, review, dest, write, relay, manifest = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

# Home-origin Review must keep Review as the real parent of Destination.
start = review.index('label = "Далі → Створити / додати"')
end = review.index('list.setOnItemClickListener', start)
block = review[start:end]
if 'openDestinationFromReview()' not in block:
    raise SystemExit("FAIL: Review does not own Destination locally")
if 'EXTRA_OPEN_DESTINATION' in block or 'finish()' in block:
    raise SystemExit("FAIL: Review still relays Destination through Main")

# Repeat confirmation must not clear its lifecycle flag during rotation dismissal.
repeat_start = review.index('private fun showRepeatSearchDialog()')
repeat_end = review.index('private fun reloadSnapshot()', repeat_start)
repeat = review[repeat_start:repeat_end]
if '!isChangingConfigurations' not in repeat:
    raise SystemExit("FAIL: repeat-search dismiss is not configuration-aware")

# Existing playlist list must be cache-aware on internal re-entry/back.
if 'private fun openExistingPlaylists()' not in dest:
    raise SystemExit("FAIL: Destination cached-list opener missing")
cache_start = dest.index('private fun openExistingPlaylists()')
cache_end = dest.index('private fun requestExistingPlaylists()', cache_start)
cache = dest[cache_start:cache_end]
for needle in ['intent.hasExtra(', 'EXTRA_EXISTING_IDS', 'showExistingListScreen()']:
    if needle not in cache:
        raise SystemExit(f"FAIL: Destination cache contract missing: {needle}")

back_start = dest.index('private fun backToExistingList()')
back_end = dest.index('private fun finishExistingConfirm(', back_start)
if 'openExistingPlaylists()' not in dest[back_start:back_end]:
    raise SystemExit("FAIL: Destination Back does not reuse cached list")

# Main handles orientation in-place so its active write executor/UI is not destroyed.
main_decl = manifest.index('android:name=".MainActivity"')
main_end = manifest.index('>', main_decl)
main_tag = manifest[main_decl:main_end]
for token in ['configChanges=', 'orientation', 'screenSize', 'smallestScreenSize']:
    if token not in main_tag:
        raise SystemExit(f"FAIL: Main rotation ownership missing: {token}")

# Coordinator exposes active-track start before each write.
sig = write.index('fun execute(')
loop = write.index('for ((index, track) in tracks.withIndex())', sig)
if 'onTrackStart:' not in write[sig:loop]:
    raise SystemExit("FAIL: write track-start callback missing")
loop_end = write.index('val videoId = track.selectedVideoId', loop)
if 'onTrackStart(' not in write[loop:loop_end]:
    raise SystemExit("FAIL: active track is not announced before write")

# Main connects write callbacks to detailed relay.
execute_start = main.index('private fun executeWriteJob(')
execute_end = main.index('private fun showQuotaPausedDialog(', execute_start)
execute = main[execute_start:execute_end]
for needle in [
    'workflowRelay.showWriteProgress(',
    'onTrackStart =',
    'workflowRelay.updateWriteTracks(',
    'workflowRelay.updateWriteProgress(',
]:
    if needle not in execute:
        raise SystemExit(f"FAIL: Main detailed write UI bridge missing: {needle}")

# Overlay shows playlist title + full per-track states.
for needle in [
    'fun showWriteProgress(',
    'fun updateWriteTracks(',
    '"✓ Додано"',
    '"≋ Дублікат • пропущено"',
    '"● Додаю…"',
]:
    if needle not in relay:
        raise SystemExit(f"FAIL: write overlay contract missing: {needle}")

lines = len(main.splitlines())
if lines >= 4000:
    raise SystemExit(f"FAIL: MainActivity cleanup regression: {lines} lines")
PY

echo "PASS:"
echo "- Home-origin Review owns Destination Back stack"
echo "- repeat-search confirmation keeps state through rotation"
echo "- existing-playlist list reuses already-loaded data on internal re-entry"
echo "- Main write session is not destroyed by orientation changes"
echo "- write progress shows playlist + per-track active/added/duplicate states"
echo "- MainActivity remains below 4000 lines"
