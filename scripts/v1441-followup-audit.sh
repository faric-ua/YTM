#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
PARSER="app/src/main/java/com/saney/ytmimporter/parser/PlaylistParser.kt"

test -f "$MAIN" || fail "MainActivity missing"
test -f "$PARSER" || fail "PlaylistParser missing"

grep -Fq 'private var accountDialogOpen = false' "$MAIN" || fail "BUG-011 account modal state flag missing"
grep -Fq 'STATE_ACCOUNT_DIALOG_OPEN' "$MAIN" || fail "BUG-011 saved-state key missing"
grep -Fq 'override fun onSaveInstanceState(' "$MAIN" || fail "BUG-011 onSaveInstanceState missing"
grep -Fq 'outState.putBoolean(' "$MAIN" || fail "BUG-011 modal state is not saved"
grep -Fq 'else if (accountDialogOpen)' "$MAIN" || fail "BUG-011 modal restore branch missing"
grep -Fq 'dialog.setOnDismissListener' "$MAIN" || fail "BUG-011 modal dismiss reset missing"

grep -Fq '[-_]\\s*\\d+' "$PARSER" || fail "UX-017 dash/underscore duplicate suffix support missing"
grep -Fq '\\(\\s*\\d+\\s*\\)' "$PARSER" || fail "UX-017 parenthesized duplicate suffix support missing"
grep -Fq 'YTM(?:\\s+Importer)?' "$PARSER" || fail "UX-017 service marker cleanup missing"

echo "PASS:"
echo "- BUG-011 Account modal rotation state fix present"
echo "- UX-017 duplicate-download filename suffix cleanup present"
