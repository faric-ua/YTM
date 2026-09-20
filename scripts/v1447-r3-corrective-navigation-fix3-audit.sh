#!/usr/bin/env bash
set -euo pipefail

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"

fail(){ echo "FAIL: $1" >&2; exit 1; }

test -f "$MAIN" || fail "MainActivity missing"

grep -Fq '!result.authorizationInvalidated' "$MAIN" \
  || fail "historical explicit Review auth-invalidated guard missing"

grep -Fq 'reopenDelegatedParentAfterAction()' "$MAIN" \
  || fail "delegated parent recovery missing after auth invalidation"

python - "$MAIN" <<'PY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")

guard = "!result.authorizationInvalidated"
guard_pos = text.find(guard)
if guard_pos < 0:
    raise SystemExit("FAIL: explicit negated auth guard missing")

# Verify the guard belongs to the Review auto-open decision, rather than merely
# existing somewhere else in MainActivity.
window = text[max(0, guard_pos - 500):guard_pos + 900]

if "openReviewAfter" not in window:
    raise SystemExit("FAIL: explicit negated guard is not tied to openReviewAfter")

if "openReviewScreen()" not in window:
    raise SystemExit("FAIL: Review auto-open call is not protected by the explicit guard")

# Independently verify that auth invalidation still recovers the delegated parent.
auth_pos = text.find("if (\n                        result.authorizationInvalidated")
if auth_pos < 0:
    raise SystemExit("FAIL: auth-invalidated branch missing")

auth_window = text[auth_pos:auth_pos + 700]
if "reopenDelegatedParentAfterAction()" not in auth_window:
    raise SystemExit("FAIL: delegated parent recovery missing in auth-invalidated branch")
PY

MAIN_LINES="$(wc -l < "$MAIN" | tr -d ' ')"
LIMIT=4000
if grep -Fq 'private var writeInProgress = false' "$MAIN"; then
  LIMIT=4100
fi
[ "$MAIN_LINES" -lt "$LIMIT" ] ||
  fail "MainActivity exceeds active cleanup ceiling: $MAIN_LINES (limit <$LIMIT)"

echo "PASS:"
echo "- explicit !result.authorizationInvalidated guard restored"
echo "- Review auto-open is protected by that guard"
echo "- delegated parent recovery remains intact"
echo "- MainActivity remains below active cleanup ceiling (<$LIMIT; current $MAIN_LINES)"
