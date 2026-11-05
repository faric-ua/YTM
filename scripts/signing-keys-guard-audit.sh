#!/data/data/com.termux/files/usr/bin/bash
# faric 2026-11-06: signing keys must never reach GitHub (YTM is public).
# The spare copy lives only on the phone (~/Ключі) — see tools/termux/ytm-keys.sh.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  BAD="$(git ls-files | grep -E -i '(\.jks|\.keystore|\.p12|\.age)$|(^|/)release-signing\.properties$|(^|/)паролі\.txt$|(^|/)Ключі/' || true)"
  [ -z "$BAD" ] || fail "signing material is tracked by git: $BAD"
fi

for pattern in '*.jks' '*.keystore' '*.p12' '*.age' 'release-signing.properties' 'Ключі/'; do
  grep -Fxq "$pattern" .gitignore || fail ".gitignore does not block: $pattern"
done

BOX=".github/workflows/key-backup.yml"
test -f "$BOX" || fail "missing $BOX"
grep -Fq 'on:' "$BOX" && grep -Fq 'workflow_dispatch:' "$BOX" || fail "key box must run only by hand"
if grep -Eq '^\s*(push|pull_request|pull_request_target|schedule|workflow_run):' "$BOX"; then
  fail "key box must not start by itself (only workflow_dispatch)"
fi
grep -Fq "| age -r \"\$RECIPIENT\" -o \"\$RUNNER_TEMP/ytm-key-box.age\"" "$BOX" ||
  fail "key box is not encrypted to the phone lock"
grep -Fq 'rm -rf "$BOX"' "$BOX" || fail "key box does not remove the open copy"
grep -Fq 'path: ${{ runner.temp }}/ytm-key-box.age' "$BOX" || fail "key box uploads something else than the locked box"
grep -Fq 'retention-days: 1' "$BOX" || fail "locked box must expire within a day"
if grep -E 'echo .*\$(STORE_PASSWORD|KEY_PASSWORD|KEYSTORE_B64)' "$BOX" | grep -v '^ *echo "Пароль' >/dev/null; then
  fail "key box may print a secret to the log"
fi

KEYS="tools/termux/ytm-keys.sh"
test -f "$KEYS" || fail "missing $KEYS"
grep -Fq 'KEYS_DIR="${FARIC_KEYS_DIR:-$HOME/Ключі}"' "$KEYS" || fail "phone key folder must stay outside the repo"
grep -Fq -- '-mhe=on' "$KEYS" || fail "key archive must hide file names (7z -mhe=on)"

echo "Signing keys guard audit: PASS"
