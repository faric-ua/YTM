#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
RELEASE="docs/v.1.4.32/RELEASE.md"
CHECKLIST="docs/v.1.4.32/REGRESSION_CHECKLIST.md"
PHONE="docs/v.1.4.32/qa/PHONE_TEST.md"
BUG="docs/v.1.4.32/qa/BUG_REGISTER.md"
for f in "$RELEASE" "$CHECKLIST" "$PHONE" "$BUG"; do
  test -f "$f" || fail "missing immutable v1.4.32 snapshot: $f"
done
grep -Fq 'versionName: **1.4.32**' "$RELEASE" || fail "v1.4.32 versionName snapshot missing"
grep -Fq 'versionCode: **66**' "$RELEASE" || fail "v1.4.32 versionCode snapshot missing"
grep -Fq 'Dialog + setContentView' "$RELEASE" || fail "v1.4.32 Dialog architecture snapshot missing"
grep -Fq 'configure Window before show' "$RELEASE" || fail "v1.4.32 pre-show architecture snapshot missing"
grep -Fq 'BUG-002 / Q-002 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.32' "$BUG" || fail "v1.4.32 BUG snapshot missing"
echo "PASS:"
echo "- immutable v1.4.32 release snapshot present"
echo "- current UiChrome may evolve without breaking historical audit"
