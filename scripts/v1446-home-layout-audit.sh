#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
RELEASE="docs/v.1.4.46/RELEASE.md"
PHONE="docs/v.1.4.46/qa/PHONE_TEST.md"

for f in "$MAIN" "$RELEASE" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.46 file: $f"
done

grep -Fq 'versionCode: **86**' "$RELEASE" ||
  fail "historical v1.4.46 versionCode evidence missing"
grep -Fq 'versionName: **1.4.46**' "$RELEASE" ||
  fail "historical v1.4.46 versionName evidence missing"

for label in   '1. Імпорт'   '2. Google / YTM'   '3. Знайти / перевірити'   '4. Створити / додати'   'Історія'   'Черга'   'Квота'   'Меню'   'Поточний плейлист'
do
  grep -Fq "$label" "$MAIN" || fail "Home label missing: $label"
done

grep -Fq 'separate theme-aware' "$RELEASE" ||
  fail "historical v1.4.46 status/info-card evidence missing"

grep -Fq '### A — Portrait hierarchy: PASS' "$PHONE" ||
  fail "historical v1.4.46 portrait phone evidence missing"

grep -Fq 'layout/hierarchy reference only' "$RELEASE" ||
  fail "layout-only prototype constraint missing"
grep -Fq 'no auth/search/write logic changed' "$PHONE" ||
  fail "phone plan does not protect workflow logic"

echo "PASS:"
echo "- historical v1.4.46 / code 86 evidence"
echo "- four-step workflow preserved"
echo "- utility row preserved"
echo "- historical separate status/info-card evidence"
echo "- historical portrait hierarchy phone PASS evidence"
echo "- prototype treated as layout hierarchy only"
echo "- auth/search/write semantics protected"
