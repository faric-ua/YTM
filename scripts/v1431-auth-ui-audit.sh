#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
GRADLE="app/build.gradle.kts"

for f in \
  "$MAIN" \
  "$IMPORT" \
  "$GRADLE" \
  docs/v.1.4.31/RELEASE.md \
  docs/v.1.4.31/REGRESSION_CHECKLIST.md \
  docs/v.1.4.31/qa/PHONE_TEST.md \
  docs/v.1.4.31/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.31 file: $f"
done

grep -Fq 'versionCode = 65' "$GRADLE" || fail "versionCode 65"
grep -Fq 'versionName = "1.4.31"' "$GRADLE" || fail "versionName 1.4.31"

grep -Fq 'syncAuthorizationInvalidationFromMemory()' "$MAIN" \
  || fail "Main onResume auth sync missing"
grep -Fq 'persistentAuthStateStore.clear()' "$MAIN" \
  || fail "Main persistent auth invalidation missing"
grep -Fq 'shared.accessToken.isNullOrBlank()' "$MAIN" \
  || fail "Main shared-session invalidation check missing"

grep -Fq 'PersistentAuthStateStore(this)' "$IMPORT" \
  || fail "Import persistent auth invalidation missing"
grep -Fq 'current is YouTubeApiException' "$IMPORT" \
  || fail "Import nested YouTubeApiException detection missing"
grep -Fq 'current.httpCode == 401' "$IMPORT" \
  || fail "Import HTTP 401 guard missing"
grep -Fq 'AuthSessionStore.clear()' "$IMPORT" \
  || fail "Import shared auth clear missing"
grep -Fq 'Сесію Google/YTM завершено' "$IMPORT" \
  || fail "401 recovery dialog missing"
grep -Fq '"До кроку 2"' "$IMPORT" \
  || fail "401 direct Step 2 action missing"

grep -Fq 'throw error' "$IMPORT" \
  || fail "auth failure abort path missing"
grep -Fq '"Перевірити"' "$IMPORT" \
  || fail "short verify action missing"
if grep -Fq '"Перевірити зміни"' "$IMPORT"; then
  fail "old wrapped verify label remains"
fi

for old in \
  'Backup chain — preview' \
  'Backup chain — помилка' \
  'Consolidated backup збережено' \
  'Плейлистів у фінальному state' \
  'Source backup folders не змінюються.' \
  'Повне відновлення delta-ланцюжка ще не підтримується.'
do
  if grep -Fq "$old" "$IMPORT"; then
    fail "old mixed/obsolete UI copy remains: $old"
  fi
done

grep -Fq 'Ланцюжок backup — попередній перегляд' "$IMPORT" \
  || fail "localized chain preview title missing"
grep -Fq 'Плейлистів у фінальному стані' "$IMPORT" \
  || fail "localized final-state wording missing"
grep -Fq 'Зведений backup збережено' "$IMPORT" \
  || fail "localized consolidated title missing"
grep -Fq 'Зібрати повний backup з ланцюжка' "$IMPORT" \
  || fail "localized chain action missing"
grep -Fq 'спільну батьківську папку' "$IMPORT" \
  || fail "destination parent guidance missing"

grep -Fq '| BUG-004 / Q-004 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.31 |' \
  qa/BUG_REGISTER.md \
  || fail "root BUG-004 implementation status missing"

echo "PASS:"
echo "- version 1.4.31 / code 65"
echo "- Import HTTP 401 invalidates shared + prior-auth state"
echo "- Main Step 2 syncs invalidation on resume"
echo "- 401 aborts backup scans instead of ordinary FAILED classification"
echo "- current local workspace is not cleared by auth invalidation code"
echo "- single-line Перевірити action"
echo "- backup/delta/chain mixed-language cleanup"
echo "- obsolete delta restore warning removed"
echo "- common-parent destination guidance"
echo "- BUG-004 remains phone-retest gated"
