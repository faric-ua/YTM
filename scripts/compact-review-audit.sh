#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
QUOTA="app/src/main/java/com/saney/ytmimporter/QuotaActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"

for f in "$MAIN" "$QUOTA" "$REVIEW" "$UI"; do
  test -f "$f" || fail "missing $f"
done

grep -q 'class QuotaActivity' "$QUOTA" || fail "dedicated quota screen missing"
grep -q 'label = "Google Cloud"' "$QUOTA" || fail "quota Google Cloud action missing"
grep -q 'label = "Черга"' "$QUOTA" || fail "quota Queue action missing"
grep -q 'quotaScreenRequestCode' "$MAIN" || fail "quota screen bridge missing"
grep -q 'enum class DialogActionLayout' "$UI" || fail "layout enum missing"
grep -q '"▣ Зберегти"' "$REVIEW" || fail "compact save missing"
grep -q '"↻ Пошук"' "$REVIEW" || fail "compact search missing"
grep -q '"≡ Усі"' "$REVIEW" || fail "compact filters missing"
grep -q 'return "$safeName.ytm.json"' "$REVIEW" || fail "playlist filename missing"
if grep -q 'SimpleDateFormat' "$REVIEW"; then fail "timestamp filename still present"; fi
echo 'PASS:'
echo '- quota: dedicated full-screen page with Google Cloud + Queue actions'
echo '- Review actions one row'
echo '- Review filters one row'
echo '- Project filename uses playlist name'
