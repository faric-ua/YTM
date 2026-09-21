#!/usr/bin/env bash
set -euo pipefail

AUDIT="scripts/v1447-r1-audit.sh"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"

fail(){ echo "FAIL: $1" >&2; exit 1; }

test -f "$AUDIT" || fail "historical R1 audit missing"
test -f "$MAIN" || fail "MainActivity missing"

grep -Fq 'Швидкі дії файл/плейлист' "$AUDIT" ||
  fail "R1 audit does not recognize R4 successor title"
grep -Fq '"Імпорт"' "$AUDIT" ||
  fail "R1 audit does not verify R4 Import label"
grep -Fq '"Експорт"' "$AUDIT" ||
  fail "R1 audit does not verify R4 Export label"
grep -Fq 'dp(48)' "$AUDIT" ||
  fail "R1 audit does not verify R4 compact height"

grep -Fq 'title = "Швидкі дії файл/плейлист"' "$MAIN" ||
  fail "R4 Home title missing"
grep -Fq '"Імпорт"' "$MAIN" ||
  fail "R4 Home Import label missing"
grep -Fq '"Експорт"' "$MAIN" ||
  fail "R4 Home Export label missing"

echo "PASS:"
echo "- historical R1 audit remains strict for the old contract"
echo "- documented R4 successor is accepted only with title + labels + 48dp"
