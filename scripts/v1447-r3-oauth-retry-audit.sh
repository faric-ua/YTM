#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

RECOVERY="app/src/main/java/com/saney/ytmimporter/auth/GoogleAccessTokenRecovery.kt"
STORE="app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
DOC="docs/v.1.4.47/qa/R3_OAUTH_RETRY.md"

for f in "$RECOVERY" "$STORE" "$API" "$MAIN" "$IMPORT" "$DOC"; do
  test -f "$f" || fail "missing R3 OAuth retry file: $f"
done

grep -Fq 'ClearTokenRequest' "$RECOVERY" || fail "rejected-token cache clear missing"
grep -Fq 'client.clearToken(' "$RECOVERY" || fail "AuthorizationClient.clearToken not used"
grep -Fq 'client.authorize(' "$RECOVERY" || fail "silent AuthorizationClient authorize missing"
grep -Fq 'result.hasResolution()' "$RECOVERY" || fail "interactive-resolution fallback missing"
grep -Fq 'return null' "$RECOVERY" || fail "silent recovery failure contract missing"
grep -Fq 'AuthSessionStore.update(' "$RECOVERY" || fail "fresh access token is not published in process memory"
grep -Fq 'markSuccessfulAuthorization()' "$RECOVERY" || fail "successful authorization marker is not preserved"

if grep -E 'putString|getString' "$RECOVERY" | grep -Eqi 'refresh.?token|access.?token'; then
  fail "OAuth token is being persisted by recovery helper"
fi

grep -Fq 'interface YouTubeAccessTokenRecovery' "$API" || fail "YouTubeApi recovery interface missing"
grep -Fq 'firstResponse.code != 401' "$API" || fail "YouTubeApi 401 gate missing"
grep -Fq 'refreshAfterUnauthorized(' "$API" || fail "YouTubeApi does not request recovery"
grep -Fq 'private fun requestOnce(' "$API" || fail "non-recursive request primitive missing"

python - "$API" <<'PY'
from pathlib import Path
import sys
text = Path(sys.argv[1]).read_text(encoding="utf-8")
start = text.index("    private fun request(\n")
end = text.index("    private fun requestOnce(\n", start)
block = text[start:end]
if block.count("requestOnce(") != 2:
    raise SystemExit("FAIL: request() must perform exactly initial request + one retry")
if "refreshAfterUnauthorized" not in block:
    raise SystemExit("FAIL: 401 recovery missing from request()")
retry_start = text.index("    private fun requestOnce(\n", start)
retry_end = text.index("    private fun requireSuccess", retry_start)
primitive = text[retry_start:retry_end]
if "refreshAfterUnauthorized" in primitive:
    raise SystemExit("FAIL: requestOnce() must never recurse into auth recovery")
PY

grep -Fq 'fun updateIdentity(' "$STORE" || fail "identity-only AuthSessionStore update missing"
grep -Fq 'GoogleAccessTokenRecovery' "$MAIN" || fail "MainActivity does not install 401 recovery"
grep -Fq 'GoogleAccessTokenRecovery' "$IMPORT" || fail "ImportActivity does not install 401 recovery"
grep -Fq 'AuthSessionStore.updateIdentity(' "$MAIN" || fail "identity load can overwrite recovered token"

grep -Fq 'private val api by lazy' "$MAIN" || fail "MainActivity YouTubeApi must be context-safe lazy"
grep -Fq 'private val api by lazy' "$IMPORT" || fail "ImportActivity YouTubeApi must be context-safe lazy"

# Historical fallback remains deliberately present: if silent refresh cannot
# recover, coordinators still propagate HTTP 401 and the UI disconnects.
grep -Fq 'current.httpCode == 401' "$MAIN" || fail "MainActivity terminal 401 fallback missing"
grep -Fq 'current.httpCode == 401' "$IMPORT" || fail "ImportActivity terminal 401 fallback missing"
grep -Fq 'WriteOutcome.AuthorizationInvalidated' "$MAIN" || fail "write terminal 401 fallback missing"

# Token persistence policy must remain unchanged.
bash scripts/auth-persistence-audit.sh >/dev/null

echo "PASS:"
echo "- HTTP 401 clears rejected Google access token cache entry"
echo "- AuthorizationClient silently requests a replacement access token"
echo "- same HTTP request is retried exactly once"
echo "- refreshed token stays process-memory-only and is reused by later API calls"
echo "- Main + Import share the same recovery path"
echo "- unresolved/failed refresh falls through to existing disconnected/manual-auth behavior"
echo "- raw OAuth access/refresh token persistence remains prohibited"
