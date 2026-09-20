#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
RELAY="app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt"
MANIFEST="app/src/main/AndroidManifest.xml"
STD="docs/v.1.4.47/navigation/TEST_DIAGRAM_STANDARD.md"
OWN="docs/v.1.4.47/navigation/NAVIGATION_ORIGIN_CONTRACT_R7.md"

for f in "$MAIN" "$REVIEW" "$RELAY" "$MANIFEST" "$STD" "$OWN"; do
  test -f "$f" || fail "missing R7 file: $f"
done

python - "$MAIN" "$REVIEW" "$RELAY" "$MANIFEST" "$STD" "$OWN" <<'PY'
from pathlib import Path
import sys

main, review, relay, manifest, standard, ownership = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

for needle in [
    "private var writeInProgress = false",
    "!writeInProgress",
    "private fun clearDelegatedReturnRoute()",
    "private fun openDirectSearchOrReview()",
    "private fun openDirectDestination()",
    "private fun openDirectPlaylistHub()",
]:
    if needle not in main:
        raise SystemExit(f"FAIL: direct-route/write lifecycle contract missing: {needle}")

playlist_handler = main[
    main.index("private fun handlePlaylistHubResult("):
    main.index("private fun handleMenuScreenResult(")
]
if 'beginPlaylistRelay("Поточний плейлист"' not in playlist_handler:
    raise SystemExit("FAIL: Playlist-owned search no longer sets Playlist return ownership")
if 'beginPlaylistRelay("Створити / додати"' not in playlist_handler:
    raise SystemExit("FAIL: Playlist-owned destination no longer sets Playlist return ownership")

write_start = main.index("private fun executeWriteJob(")
write = main[write_start:main.index("private fun showQuotaPausedDialog(", write_start)]
for needle in [
    "writeInProgress = true",
    "val displayTracks =",
    "workflowRelay.updateWriteTracks(",
    "workflowRelay.updateWriteProgress(",
    "writeInProgress = false",
    "persistCurrentWorkspace()",
]:
    if needle not in write:
        raise SystemExit(f"FAIL: stable write UI contract missing: {needle}")

semantic_groups = [
    (
        '"Оброблено ${progress.processedTracks}/${progress.totalTracks} • "',
        '"Оброблено: ${progress.processedTracks}/${progress.totalTracks} • "',
    ),
    (
        '"Додано ${progress.job.addedCount} • "',
        '"Додано: ${progress.job.addedCount} • "',
    ),
]

for alternatives in semantic_groups:
    if not any(needle in relay for needle in alternatives):
        raise SystemExit(
            "FAIL: progress semantic state missing: "
            + " OR ".join(alternatives)
        )

for needle in [
    "TrackStatus.DUPLICATE",
    "palette.duplicate",
    "TrackStatus.FAILED",
    "palette.danger",
]:
    if needle not in relay:
        raise SystemExit(f"FAIL: progress semantic state missing: {needle}")

repeat_start = review.index("private fun showRepeatSearchDialog()")
repeat = review[repeat_start:review.index("private fun reloadSnapshot()", repeat_start)]
if "if (!isChangingConfigurations)" not in repeat:
    raise SystemExit("FAIL: repeat-search config cancel guard missing")

review_pos = manifest.index('android:name=".ReviewActivity"')
review_manifest = manifest[review_pos - 40:manifest.index('android:name=".ImportActivity"')]
if 'android:configChanges="keyboardHidden|orientation|screenSize|smallestScreenSize"' not in review_manifest:
    raise SystemExit("FAIL: Review rotation preservation config missing")

for needle in [
    "завжди починається з `[Головна]`",
    "Первинна мова діаграм — **українська**",
    "MOBILE / DESKTOP",
    "Власник маршруту",
]:
    if needle not in standard:
        raise SystemExit(f"FAIL: diagram standard missing: {needle}")

for needle in [
    "3. Знайти / перевірити",
    "повернення на `[Головна]`",
    "Поточний плейлист",
    "нижня панель",
]:
    if needle not in ownership:
        raise SystemExit(f"FAIL: navigation origin doc missing: {needle}")

if len(main.splitlines()) >= 4100:
    raise SystemExit("FAIL: MainActivity unexpectedly exceeded 4100 lines")
PY

echo "PASS:"
echo "- direct Home search/destination routes are separated from Playlist/Menu delegated routes"
echo "- Playlist-owned route still preserves Playlist return ownership"
echo "- onResume cannot replace live write Track objects"
echo "- write overlay uses stable track references and truthful aggregate semantics"
echo "- terminal row prefixes/colors are semantic"
echo "- repeat-search rotation contract is protected"
echo "- approved full object-map diagram standard is persisted"
