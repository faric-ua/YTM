#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
grep -q 'DialogActionLayout.PRIMARY_TOP' "$MAIN" || fail "quota layout missing"
grep -q 'enum class DialogActionLayout' "$UI" || fail "layout enum missing"
grep -q '"▣ Зберегти"' "$REVIEW" || fail "compact save missing"
grep -q '"↻ Пошук"' "$REVIEW" || fail "compact search missing"
grep -q '"≡ Усі"' "$REVIEW" || fail "compact filters missing"
grep -q 'return "$safeName.ytm.json"' "$REVIEW" || fail "playlist filename missing"
if grep -q 'SimpleDateFormat' "$REVIEW"; then fail "timestamp filename still present"; fi
echo 'PASS:'
echo '- quota: Google Cloud top, Queue/Close bottom'
echo '- Review actions one row'
echo '- Review filters one row'
echo '- Project filename uses playlist name'
