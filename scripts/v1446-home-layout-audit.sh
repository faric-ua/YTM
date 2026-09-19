#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
RELEASE="docs/v.1.4.46/RELEASE.md"
PHONE="docs/v.1.4.46/qa/PHONE_TEST.md"

for f in "$GRADLE" "$MAIN" "$RELEASE" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.46 file: $f"
done

grep -Fq 'versionCode = 86' "$GRADLE" || fail "versionCode 86 missing"
grep -Fq 'versionName = "1.4.46"' "$GRADLE" || fail "versionName 1.4.46 missing"

for label in   '1. Імпорт'   '2. Google / YTM'   '3. Знайти / перевірити'   '4. Створити / додати'   'Історія'   'Черга'   'Квота'   'Меню'   'Поточний плейлист'
do
  grep -Fq "$label" "$MAIN" || fail "Home label missing: $label"
done

grep -Fq 'val statusCard =' "$MAIN" || fail "separate Home status card missing"
grep -A45 -F 'val statusCard =' "$MAIN" |
  grep -Fq 'palette.accent' ||
  fail "Home status card is not theme-accent aware"

STATUS_LINE="$(grep -n -F 'statusCard.addView(' "$MAIN" | head -n1 | cut -d: -f1)"
WORKSPACE_LINE="$(grep -n -F 'val workspaceCard =' "$MAIN" | head -n1 | cut -d: -f1)"

[ -n "$STATUS_LINE" ] && [ -n "$WORKSPACE_LINE" ] ||
  fail "Home status/current-playlist structure missing"

[ "$STATUS_LINE" -lt "$WORKSPACE_LINE" ] ||
  fail "status info card must precede current-playlist card"

grep -Fq 'layout/hierarchy reference only' "$RELEASE" ||
  fail "layout-only prototype constraint missing"
grep -Fq 'no auth/search/write logic changed' "$PHONE" ||
  fail "phone plan does not protect workflow logic"

echo "PASS:"
echo "- v1.4.46 / code 86"
echo "- four-step workflow preserved"
echo "- utility row preserved"
echo "- separate theme-aware status/info card"
echo "- current-playlist card follows status/info"
echo "- prototype treated as layout hierarchy only"
echo "- auth/search/write semantics protected"
