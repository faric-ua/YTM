#!/usr/bin/env bash
set -euo pipefail

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
fail(){ echo "FAIL: $1" >&2; exit 1; }

test -f "$MAIN" || fail "MainActivity missing"

if grep -Fq 'workflowRelayActive' "$MAIN"; then
  fail "stale workflowRelayActive reference remains"
fi

grep -Fq 'private fun clearAuthorizationForRefreshFailure(' "$MAIN" \
  || fail "auth refresh failure handler missing"

python - "$MAIN" <<'PY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")
start = text.index("    private fun clearAuthorizationForRefreshFailure(")
end = text.index("    private fun handleAuthorizedToken(", start)
block = text[start:end]

if "reopenDelegatedParentAfterAction()" not in block:
    raise SystemExit("FAIL: auth refresh failure no longer returns delegated parent")

if "workflowRelayActive" in block:
    raise SystemExit("FAIL: removed relay field is still referenced")
PY

LINES="$(wc -l < "$MAIN" | tr -d ' ')"
[ "$LINES" -lt 4000 ] || fail "MainActivity exceeds historical cleanup ceiling: $LINES"

echo "PASS:"
echo "- stale workflowRelayActive compile reference removed"
echo "- delegated parent recovery preserved"
echo "- MainActivity remains below 4000-line cleanup ceiling ($LINES)"
