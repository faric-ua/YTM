#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
PLAN="app/src/main/java/com/saney/ytmimporter/destination/DestinationForwardedWritePlan.kt"

for f in "$DEST" "$REVIEW" "$PLAN"; do
  test -f "$f" || fail "missing R4 FIX2 source: $f"
done

grep -Fq 'import android.widget.Toast' "$DEST" ||
  fail "Destination Toast import missing"
grep -Fq 'private fun toast(' "$DEST" ||
  fail "Destination toast helper missing"

python - "$DEST" "$REVIEW" "$PLAN" <<'PY_AUDIT'
from pathlib import Path
import sys

dest, review, plan = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

for name, text in [("Destination", dest), ("Review", review)]:
    start = text.index("private fun showRemoteProgress(")
    end = text.index("\n    private fun ", start + 10)
    block = text[start:end]

    if ".create()" in block:
        raise SystemExit(
            f"FAIL: {name} still calls unsupported StableAlertBuilder.create()"
        )
    if ".show()" not in block:
        raise SystemExit(
            f"FAIL: {name} remote progress dialog does not use StableAlertBuilder.show()"
        )
    if "setCancelable(" not in block:
        raise SystemExit(
            f"FAIL: {name} remote progress dialog lost non-cancelable contract"
        )

skip_start = plan.index("        val skippedPositions =")
skip_end = plan.index("        val tracksToSkip =", skip_start)
skip_block = plan[skip_start:skip_end]

if ".orEmpty()" in skip_block:
    raise SystemExit(
        "FAIL: forwarded primitive IntArray still uses unsupported nullable orEmpty()"
    )
if "?: IntArray(0)" not in skip_block:
    raise SystemExit(
        "FAIL: forwarded skip positions do not use explicit IntArray fallback"
    )

if "index in" not in plan or "index !in" not in plan:
    raise SystemExit(
        "FAIL: forwarded duplicate include/exclude contracts missing"
    )
PY_AUDIT

echo "PASS:"
echo "- Destination toast helper present"
echo "- StableAlertBuilder uses supported show() API in Destination/Review"
echo "- remote progress dialogs remain non-cancelable"
echo "- primitive skip positions use explicit IntArray fallback"
