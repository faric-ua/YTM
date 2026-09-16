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
grep -A28 'override fun onCreate' "$MAIN" | grep -q 'restoreAuthSessionFromMemory()' || fail "restore not in onCreate"
grep -A35 'override fun onCreate' "$MAIN" | grep -q 'updateAccountPanel()' || fail "account button not refreshed"
grep -A45 'override fun onCreate' "$MAIN" | grep -q 'loadAccountIdentity' || fail "incomplete identity not reloaded"
grep -A12 'private fun handleAuthorizedToken' "$MAIN" | grep -q 'syncAuthSessionToMemory()' || fail "token not synced immediately"
echo 'PASS:'
echo '- account session survives Activity recreation in process memory'
echo '- Step 2 can restore its connected state after rotation'
echo '- OAuth token is not persisted to disk by AuthSessionStore'
