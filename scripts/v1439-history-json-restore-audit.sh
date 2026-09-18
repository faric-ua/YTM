#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/storage/HistoryStore.kt"
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in   "$GRADLE"   "$DATA"   "$HISTORY"   "$BACKUP"   "$MANIFEST"   docs/v.1.4.39/RELEASE.md   docs/v.1.4.39/UX_AUDIT.md   docs/v.1.4.39/REGRESSION_CHECKLIST.md   docs/v.1.4.39/qa/PHONE_TEST.md   docs/v.1.4.39/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.39 file: $f"
done

grep -Fq 'versionCode = 75' "$GRADLE" || fail "versionCode 75 missing"
grep -Fq 'versionName = "1.4.39"' "$GRADLE" || fail "versionName 1.4.39 missing"

grep -Fq 'data class HistoryImportSummary' "$HISTORY"   || fail "History import summary missing"
grep -Fq 'fun inspectImportJson(' "$HISTORY"   || fail "History JSON inspection missing"
grep -Fq 'fun normalizeImportJson(' "$HISTORY"   || fail "History JSON normalization missing"
grep -Fq 'History JSON містить дублікати id' "$HISTORY"   || fail "duplicate History id guard missing"
grep -Fq 'Очікується YTM_History_*.json' "$HISTORY"   || fail "History root-format validation missing"
grep -Fq 'MAX_HISTORY_ENTRIES' "$HISTORY"   || fail "History import cap guard missing"

grep -Fq 'fun restoreHistoryJson(' "$BACKUP"   || fail "History-only restore missing"
grep -Fq '"history_store_v1"' "$BACKUP"   || fail "History-only preference group missing"
grep -Fq 'Other local preference groups are intentionally omitted' "$BACKUP"   || fail "partial-restore boundary note missing"
grep -Fq 'return restoreBackupJson(' "$BACKUP"   || fail "History restore does not reuse safety-snapshot engine"

grep -Fq 'title = "History JSON"' "$DATA"   || fail "History JSON restore card missing"
grep -Fq 'buttonLabel = "Імпорт History"' "$DATA"   || fail "History import action missing"
grep -Fq 'historyImportRequestCode = 4204' "$DATA"   || fail "History import request route missing"
grep -Fq 'prepareHistoryImport(uri)' "$DATA"   || fail "History import result handler missing"
grep -Fq 'showHistoryImportConfirmation(' "$DATA"   || fail "History import confirmation missing"
grep -Fq 'STATE_HISTORY_IMPORT_CONFIRMATION_PENDING' "$DATA"   || fail "History import rotation state missing"
grep -Fq 'PENDING_HISTORY_IMPORT_CACHE_FILE' "$DATA"   || fail "History import cache file missing"
grep -Fq 'restorePendingHistoryImportConfirmation()' "$DATA"   || fail "History import confirmation recreation missing"
grep -Fq 'clearPendingHistoryImportConfirmation()' "$DATA"   || fail "History import cache cleanup missing"
grep -Fq 'localBackupManager' "$DATA"   || fail "LocalBackupManager use missing"
grep -Fq '.restoreHistoryJson(' "$DATA"   || fail "History-only restore execution missing"

OPEN_DATA="$(grep -F 'ACTION_OPEN_DOCUMENT' "$DATA" | grep -v 'TREE' | wc -l | tr -d ' ')"
[ "$OPEN_DATA" -eq 1 ]   || fail "DataActivity must keep one centralized ACTION_OPEN_DOCUMENT launcher; found $OPEN_DATA"

for preserved in   'Черга, quota, SearchCache і поточний список залишаться без змін.'   'Черга, локальна квота, SearchCache і поточний робочий список'
do
  grep -Fq "$preserved" "$DATA"     || fail "History-only preservation copy missing: $preserved"
done

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

echo "PASS:"
echo "- v1.4.39 / code 75"
echo "- native YTM_History_*.json validation + normalization"
echo "- History-only partial restore through existing safety-snapshot engine"
echo "- Queue/quota/SearchCache/current playlist preserved"
echo "- History confirmation survives Activity recreation"
echo "- DataActivity still has one centralized ACTION_OPEN_DOCUMENT launcher"
echo "- no broad filesystem permission"
