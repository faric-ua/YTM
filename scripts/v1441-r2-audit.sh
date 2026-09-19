#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
PARSER="app/src/main/java/com/saney/ytmimporter/parser/PlaylistParser.kt"
R2="docs/v.1.4.41/R2.md"

for f in "$GRADLE" "$PARSER" "$R2"; do
  test -f "$f" || fail "missing v1.4.41-R2 file: $f"
done

grep -Fq 'versionCode = 79' "$GRADLE" || fail "R2 versionCode 79 missing"
grep -Fq 'versionName = "1.4.41-R2"' "$GRADLE" || fail "R2 versionName missing"

grep -Fq 'YTM(?:\\s+Importer)?' "$PARSER"   || fail "service marker cleanup missing"
grep -Fq '))*\\s*$' "$PARSER"   || fail "stacked duplicate suffix repetition missing"
grep -Fq '[-_]\\s*\\d+' "$PARSER"   || fail "dash/underscore duplicate token missing"
grep -Fq '\\(\\s*\\d+\\s*\\)' "$PARSER"   || fail "parenthesized duplicate token missing"

grep -Fq 'stacked duplicate-download filename suffix only' "$R2"   || fail "R2 narrow scope missing"

echo "PASS:"
echo "- v1.4.41-R2 / code 79"
echo "- repeated duplicate-download suffix stripping present"
echo "- R2 narrow phone acceptance documented"
