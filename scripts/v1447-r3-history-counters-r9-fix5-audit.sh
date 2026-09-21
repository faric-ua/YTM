#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
SEM="app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt"
DOC="docs/v.1.4.47/qa/R3_HISTORY_COUNTERS_R9_FIX5.md"
BUGS="docs/v.1.4.47/qa/BUG_REGISTER.md"

for f in "$MAIN" "$SEM" "$DOC" "$BUGS"; do
  test -f "$f" || fail "missing R9 FIX5 file: $f"
done

python - "$MAIN" "$SEM" "$DOC" "$BUGS" <<'PY'
from pathlib import Path
import sys

main, sem, doc, bugs = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

for n in [
    "stateSourceTracks = tracks",
    "stateSourceTracks: List<Track>",
    "stateSourceTracks.forEachIndexed",
    "writeTargetCount =\n                    job.totalCount",
    "addedCount =\n                    job.addedCount",
    "failedCount =\n                    job.failedCount",
]:
    if n not in main:
        raise SystemExit(f"FAIL: History counter authority missing: {n}")

sync0 = main.index("private fun syncHistoryFromJob(")
sync1 = main.index("private fun historyTrackFromTrack(", sync0)
sync = main[sync0:sync1]

if "merged.count {\n                        it.status == TrackStatus.ADDED.name" in sync:
    raise SystemExit("FAIL: History still derives addedCount from merged display state")

if "merged.count {\n                        it.status == TrackStatus.FAILED.name" in sync:
    raise SystemExit("FAIL: History still derives failedCount from merged display state")

for n in [
    "private fun effectiveRemoteAddedCount(",
    "PendingDestination.NEW_PLAYLIST",
    "HistoryStatus.COMPLETED",
    "entry.addedCount == 0",
    "entry.writeTargetCount > 0",
    "entry.failedCount == 0",
    "entry.pendingCount == 0",
    "entry.writeTargetCount",
]:
    if n not in sem:
        raise SystemExit(f"FAIL: legacy 0/N recovery guard missing: {n}")

if '"${effectiveRemoteAddedCount(entry)}/${entry.writeTargetCount}"' not in sem:
    raise SystemExit("FAIL: HistoryResultSemantics does not use effective added count")

for n in [
    "BUG-026",
    "stale",
    "PendingJob.addedCount",
]:
    if n not in bugs:
        raise SystemExit(f"FAIL: BUG-026 register evidence missing: {n}")

for n in [
    "History Counters R9 FIX5",
    "writeTargetCount = job.totalCount",
    "existing-playlist entries are never inferred",
]:
    if n not in doc:
        raise SystemExit(f"FAIL: FIX5 doc contract missing: {n}")

print("PASS:")
print("- History write counters come from PendingJob authority")
print("- coordinator track subset overlays History track details")
print("- legacy completed NEW_PLAYLIST 0/N can render N/N safely")
print("- existing-playlist duplicate history is not guessed")
PY
