#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
PARSER="app/src/main/java/com/saney/ytmimporter/parser/PlaylistParser.kt"
R1="docs/v.1.4.41/R1.md"

for f in "$MAIN" "$PARSER" "$R1"; do
  test -f "$f" || fail "missing v1.4.41-R1 file: $f"
done

grep -Fq 'versionCode: **78**' "$R1" || fail "historical R1 versionCode evidence missing"
grep -Fq 'versionName: **1.4.41-R1**' "$R1" || fail "historical R1 versionName evidence missing"

grep -Fq 'private var accountDialogOpen = false' "$MAIN"   || fail "BUG-011 account dialog state flag missing"
grep -Fq 'STATE_ACCOUNT_DIALOG_OPEN' "$MAIN"   || fail "BUG-011 saved-state key missing"
grep -Fq 'override fun onSaveInstanceState(' "$MAIN"   || fail "BUG-011 onSaveInstanceState missing"
grep -Fq 'outState.putBoolean(' "$MAIN"   || fail "BUG-011 dialog state save missing"
grep -Fq 'else if (accountDialogOpen)' "$MAIN"   || fail "BUG-011 restore branch missing"
grep -Fq 'dialog.setOnDismissListener' "$MAIN"   || fail "BUG-011 dismiss-state reset missing"

grep -Fq 'YTM(?:\\s+Importer)?' "$PARSER"   || fail "UX-017 service marker cleanup missing"
grep -Fq '[-_]\\s*\\d+' "$PARSER"   || fail "UX-017 -1/_1 copy suffix cleanup missing"
grep -Fq '\\(\\s*\\d+\\s*\\)' "$PARSER"   || fail "UX-017 parenthesized copy suffix cleanup missing"

grep -Fq 'two targeted corrective phone findings only' "$R1"   || fail "R1 narrow corrective scope missing"

echo "PASS:"
echo "- historical v1.4.41-R1 / code 78 evidence"
echo "- BUG-011 Account modal rotation persistence implementation"
echo "- UX-017 duplicate-download filename suffix cleanup"
echo "- narrow R1 phone acceptance documented"
