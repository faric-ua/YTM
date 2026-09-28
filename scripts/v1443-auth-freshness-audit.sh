#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
WRITE="app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt"
RELEASE="docs/v.1.4.43/RELEASE.md"
PHONE="docs/v.1.4.43/qa/PHONE_TEST.md"

for f in "$GRADLE" "$MAIN" "$WRITE" "$RELEASE" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.43 file: $f"
done

grep -Fq 'versionCode = 82' "$GRADLE"   || fail "versionCode 82 missing"
grep -Fq 'versionName = "1.4.43"' "$GRADLE"   || fail "versionName 1.4.43 missing"

if grep -Fq 'if (!forceAccountPicker && !accessToken.isNullOrBlank())' "$MAIN"; then
  fail "stale-token authorize fast path still present"
fi

grep -Fq 'Перевіряю авторизацію Google/YTM…' "$MAIN"   || fail "pre-action authorization refresh status missing"
grep -Fq 'preserveKnownIdentity' "$MAIN"   || fail "silent refresh identity-preservation path missing"
grep -Fq 'clearAuthorizationForRefreshFailure' "$MAIN"   || fail "refresh-failure auth clearing path missing"
grep -Fq 'handleAuthorizedToken(' "$MAIN"   || fail "authorized-token handler missing"

grep -Fq 'data class AuthorizationInvalidated' "$WRITE"   || fail "write auth-invalidated outcome missing"
grep -Fq 'isAuthorizationError' "$WRITE"   || fail "write auth-error classifier missing"
grep -Fq 'httpCode == 401' "$WRITE"   || fail "write HTTP 401 detection missing"
grep -Fq 'Очікує повторної авторизації Google/YTM' "$WRITE"   || fail "pending-track auth retry state missing"

grep -Fq 'WriteOutcome.AuthorizationInvalidated' "$MAIN"   || fail "MainActivity write auth-invalidated handling missing"
grep -Fq 'invalidateAuthorizationIfNeeded(' "$MAIN"   || fail "shared auth invalidation fallback missing"
grep -Fq 'Незавершене завдання збережено в «Черзі».' "$MAIN"   || fail "write auth interruption queue copy missing"

echo "PASS:"
echo "- v1.4.43 / code 82"
echo "- cached token is no longer trusted as a fresh-auth fast path"
echo "- Google AuthorizationClient refresh/check precedes remote authorize() actions"
echo "- silent refresh can preserve known account/channel identity"
echo "- refresh failure clears misleading ready state"
echo "- write-time HTTP 401 stops write and preserves retryable pending work"
echo "- MainActivity propagates write auth invalidation into shared auth state"
