#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
RELAY="app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt"
STD="docs/v.1.4.47/navigation/TEST_DIAGRAM_STANDARD.md"
DOC="docs/v.1.4.47/qa/R3_WRITE_PROGRESS_R8.md"

for f in "$MAIN" "$RELAY" "$STD" "$DOC"; do
  test -f "$f" || fail "missing R8 file: $f"
done

python - "$MAIN" "$RELAY" "$STD" "$DOC" <<'PY'
from pathlib import Path
import sys
main, relay, standard, doc = [Path(p).read_text(encoding="utf-8") for p in sys.argv[1:]]

w0=main.index("private fun executeWriteJob(")
write=main[w0:main.index("private fun showQuotaPausedDialog(",w0)]
for n in ["stateSourceTracks = tracks","workflowRelay.updateWriteTracks(","workflowRelay.updateWriteProgress("]:
    if n not in write: raise SystemExit(f"FAIL: Main R8 bridge missing: {n}")

for n in [
    "private var writeDisplayTracks: List<Track>",
    "private val writeStateByIndex",
    "private var activeWriteIndex",
    "stateSourceTracks: List<Track> = tracks",
    "private fun syncWriteStates(",
    "private fun resolveWriteIndex(",
    "private fun sameTrack(",
    "private fun blendColor(",
    "palette.successFill",
    "palette.duplicate",
    "palette.dangerFill",
    '"Оброблено: ${progress.processedTracks}/${progress.totalTracks} • "',
    '"Додано: ${progress.job.addedCount} • "',
]:
    if n not in relay: raise SystemExit(f"FAIL: R8 relay contract missing: {n}")

for n in [
    "`✓ Додано` — зелений",
    "`≋ Дублікат • пропущено` — синій",
    "`× Помилка` — червоний",
    "## 10. Легенда / умовні позначення",
]:
    if n not in standard: raise SystemExit(f"FAIL: diagram contract missing: {n}")

for n in ["05 FAIL","blue-tinted fill","diagram legend is mandatory"]:
    if n not in doc: raise SystemExit(f"FAIL: R8 QA evidence missing: {n}")

lines=len(main.splitlines())
if lines >= 4100: raise SystemExit(f"FAIL: MainActivity successor budget exceeded: {lines}")
print(f"PASS: MainActivity {lines} lines")
PY

echo "PASS:"
echo "- write subset is authoritative row-state source"
echo "- overlay owns explicit row-state snapshot"
echo "- added=green, duplicate=blue, failure=red"
echo "- aggregate status is split by semantic outcome"
echo "- diagram legend is mandatory"
