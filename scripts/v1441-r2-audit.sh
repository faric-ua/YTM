#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

PARSER="app/src/main/java/com/saney/ytmimporter/parser/PlaylistParser.kt"
R2="docs/v.1.4.41/R2.md"

for f in "$PARSER" "$R2"; do
  test -f "$f" || fail "missing v1.4.41-R2 file: $f"
done

grep -Fq 'versionCode: **79**' "$R2" || fail "historical R2 versionCode evidence missing"
grep -Fq 'versionName: **1.4.41-R2**' "$R2" || fail "historical R2 versionName evidence missing"

grep -Fq 'YTM(?:\\s+Importer)?' "$PARSER"   || fail "service marker cleanup missing"
grep -Fq '))*\\s*$' "$PARSER"   || fail "stacked duplicate suffix repetition missing"
grep -Fq '[-_]\\s*\\d+' "$PARSER"   || fail "dash/underscore duplicate token missing"
grep -Fq '\\(\\s*\\d+\\s*\\)' "$PARSER"   || fail "parenthesized duplicate token missing"

grep -Fq 'stacked duplicate-download filename suffix only' "$R2"   || fail "R2 narrow scope missing"

echo "PASS:"
echo "- historical v1.4.41-R2 / code 79 evidence"
echo "- repeated duplicate-download suffix stripping present"
echo "- R2 narrow phone acceptance documented"
