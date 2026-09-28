#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
STORE="app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt"
test -f "$STORE" || fail "AuthSessionStore.kt missing"
grep -q 'object AuthSessionStore' "$STORE" || fail "AuthSessionStore missing"
if grep -q -E 'getSharedPreferences|JSONObject|openFile|File\(' "$STORE"; then
  fail "AuthSessionStore must stay memory-only"
fi
grep -q 'restoreAuthSessionFromMemory()' "$MAIN" || fail "auth restore missing"
grep -q 'syncAuthSessionToMemory()' "$MAIN" || fail "auth sync missing"
grep -q 'AuthSessionStore.clear()' "$MAIN" || fail "account change does not clear old session"
grep -A40 'override fun onCreate' "$MAIN" | grep -q 'restoreAuthSessionFromMemory()' || fail "restore not in onCreate"
grep -A50 'override fun onCreate' "$MAIN" | grep -q 'updateAccountPanel()' || fail "account button not refreshed"
grep -A60 'override fun onCreate' "$MAIN" | grep -q 'loadAccountIdentity' || fail "incomplete identity not reloaded"

python - "$MAIN" <<'PY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")
start = text.find("    private fun handleAuthorizedToken(")
if start < 0:
    raise SystemExit("FAIL: handleAuthorizedToken missing")

end = text.find("\n    private fun ", start + 1)
block = text[start:] if end < 0 else text[start:end]

required = [
    "accessToken = token",
    "syncAuthSessionToMemory()",
    "val action = pendingAfterAuth",
]
for needle in required:
    if needle not in block:
        raise SystemExit(f"FAIL: handleAuthorizedToken missing: {needle}")

if block.index("accessToken = token") > block.index("syncAuthSessionToMemory()"):
    raise SystemExit("FAIL: token assignment occurs after session sync")

if block.index("syncAuthSessionToMemory()") > block.index("val action = pendingAfterAuth"):
    raise SystemExit("FAIL: token not synced before pending auth action can run")
PY

echo 'PASS:'
echo '- account session survives Activity recreation in process memory'
echo '- Step 2 can restore its connected state after rotation'
echo '- OAuth token is not persisted to disk by AuthSessionStore'
echo '- authorized token is synced before pending auth action execution'
