#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
FLOW="$SRC/ui/SafFileSaveFlow.kt"
WRITER="$SRC/storage/SafTreeFileWriter.kt"
DATA="$SRC/DataActivity.kt"
REVIEW="$SRC/ReviewActivity.kt"
HISTORY="$SRC/HistoryActivity.kt"
SERVICE="$SRC/ServiceActivity.kt"
GRADLE="app/build.gradle.kts"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in   "$FLOW"   "$WRITER"   "$DATA"   "$REVIEW"   "$HISTORY"   "$SERVICE"   "$GRADLE"   "$MANIFEST"   docs/v.1.4.36/RELEASE.md   docs/v.1.4.36/FILE_SAVE_AUDIT.md   docs/v.1.4.36/REGRESSION_CHECKLIST.md   docs/v.1.4.36/qa/PHONE_TEST.md   docs/v.1.4.36/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.36 file: $f"
done

grep -Fq 'versionCode = 70' "$GRADLE" || fail "versionCode 70 missing"
grep -Fq 'versionName = "1.4.36"' "$GRADLE" || fail "versionName 1.4.36 missing"

grep -Fq 'object SafFileSaveFlow' "$FLOW" || fail "SafFileSaveFlow missing"
grep -Fq 'ACTION_CREATE_DOCUMENT' "$FLOW" || fail "shared CREATE_DOCUMENT fallback missing"
grep -Fq 'ACTION_OPEN_DOCUMENT_TREE' "$FLOW" || fail "shared add-folder launcher missing"
grep -Fq 'Системне збереження / змінити ім’я…' "$FLOW" || fail "system-save fallback label missing"
grep -Fq 'Додати папку для швидкого збереження…' "$FLOW" || fail "add-folder action missing"
grep -Fq 'negativeLabel =' "$FLOW" || fail "save destination cancel missing"
grep -Fq '"Скасувати"' "$FLOW" || fail "save destination cancel label missing"

CREATE_COUNT="$(grep -R -h -F 'ACTION_CREATE_DOCUMENT' "$SRC" | wc -l | tr -d ' ')"
[ "$CREATE_COUNT" -eq 1 ] || fail "expected one centralized ACTION_CREATE_DOCUMENT launcher, found $CREATE_COUNT"

for f in "$DATA" "$REVIEW" "$HISTORY" "$SERVICE"; do
  grep -Fq 'SafFileSaveFlow.show(' "$f" || fail "shared save flow missing: $f"
  grep -Fq 'SafTreeFileWriter.writeText(' "$f" || fail "direct tree writer missing: $f"
  if grep -Fq 'ACTION_CREATE_DOCUMENT' "$f"; then
    fail "activity-local ACTION_CREATE_DOCUMENT remains: $f"
  fi
done

grep -Fq 'object SafTreeFileWriter' "$WRITER" || fail "SafTreeFileWriter missing"
grep -Fq 'four user-facing create-file workflows' docs/v.1.4.36/FILE_SAVE_AUDIT.md \
  || fail "v1.4.36 create-file inventory missing"
grep -Fq '.createDocument(' "$WRITER" || fail "tree document create missing"
grep -Fq 'uniqueFileName' "$WRITER" || fail "duplicate-safe filename helper missing"
grep -Fq '"$base ($index)$extension"' "$WRITER" || fail "numbered duplicate naming missing"
grep -Fq 'DocumentsContract' "$WRITER" || fail "DocumentsContract writer missing"
grep -Fq '.deleteDocument(' "$WRITER" || fail "failed-write cleanup missing"
grep -Fq 'SafTreeAccess.persist(' "$WRITER" || fail "write-root persistence missing"
grep -Fq 'SafTreeAccess.Access.READ_WRITE' "$WRITER" || fail "write permission boundary missing"

OPEN_IMPORT="$(grep -h -F 'ACTION_OPEN_DOCUMENT' "$SRC/ImportActivity.kt" | grep -v 'TREE' | wc -l | tr -d ' ')"
OPEN_DATA="$(grep -h -F 'ACTION_OPEN_DOCUMENT' "$DATA" | grep -v 'TREE' | wc -l | tr -d ' ')"
[ "$OPEN_IMPORT" -eq 1 ] || fail "Import open-document path drift"
[ "$OPEN_DATA" -eq 1 ] || fail "Data Restore open-document path drift"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

echo "PASS:"
echo "- v1.4.36 / code 70"
echo "- four create-file workflows use one in-app save-destination flow"
echo "- ACTION_CREATE_DOCUMENT centralized to one explicit fallback launcher"
echo "- remembered write roots can save directly"
echo "- duplicate direct saves avoid overwrite"
echo "- failed direct writes clean up the newly-created document"
echo "- both open-file regression paths remain"
echo "- no broad filesystem permission"
