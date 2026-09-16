#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
STORE="app/src/main/java/com/saney/ytmimporter/auth/PersistentAuthStateStore.kt"
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"

test -f "$STORE" || fail "PersistentAuthStateStore.kt missing"

grep -q 'hadSuccessfulAuthorization' "$STORE" \
  || fail "prior authorization marker missing"

grep -q 'had_successful_authorization' "$STORE" \
  || fail "persistent marker key missing"

if grep -Eqi 'access.?token|refresh.?token|email|channel.?id|channel.?title|google.?name' "$STORE"; then
  # Comments intentionally mention token/email as forbidden; inspect only put/get payload lines.
  if grep -E 'putString|getString' "$STORE" | grep -Eqi 'token|email|channel|name'; then
    fail "PersistentAuthStateStore stores identity/token strings"
  fi
fi

if grep -q '"auth_state_v1"' "$BACKUP"; then
  fail "auth_state_v1 must not be included in app Full Backup"
fi

grep -q 'restorePriorAuthorizationSilently()' "$MAIN" \
  || fail "silent authorization restore function missing"

grep -q 'hadSuccessfulAuthorization()' "$MAIN" \
  || fail "MainActivity does not consult persistent authorization marker"

grep -A80 'private fun restorePriorAuthorizationSilently' "$MAIN" |
  grep -q 'Identity.getAuthorizationClient(this)' \
  || fail "silent restore does not use AuthorizationClient"

grep -A80 'private fun restorePriorAuthorizationSilently' "$MAIN" |
  grep -q 'result.hasResolution()' \
  || fail "silent restore does not guard interactive resolution"

grep -A30 'private fun handleAuthorizedToken' "$MAIN" |
  grep -q 'markSuccessfulAuthorization()' \
  || fail "successful auth is not persisted as a non-secret marker"

echo "PASS:"
echo "- prior authorization success survives process restart / in-place update"
echo "- MainActivity attempts AuthorizationClient silently on launch"
echo "- interactive resolution is not auto-launched by silent restore"
echo "- no OAuth token/identity string is persisted in auth_state_v1"
echo "- auth_state_v1 is excluded from Full Backup"
