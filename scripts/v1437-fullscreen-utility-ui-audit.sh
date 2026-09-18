#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
MANIFEST="app/src/main/AndroidManifest.xml"
STORAGE="$SRC/StorageChooserActivity.kt"
QUOTA="$SRC/QuotaActivity.kt"
MENU="$SRC/MenuActivity.kt"
MAIN="$SRC/MainActivity.kt"
IMPORT="$SRC/ImportActivity.kt"
SAVE_FLOW="$SRC/ui/SafFileSaveFlow.kt"

for f in \
  "$MANIFEST" \
  "$STORAGE" \
  "$QUOTA" \
  "$MENU" \
  "$MAIN" \
  "$IMPORT" \
  "$SAVE_FLOW" \
  docs/v.1.4.37/RELEASE.md \
  docs/v.1.4.37/UX_AUDIT.md \
  docs/v.1.4.37/REGRESSION_CHECKLIST.md \
  docs/v.1.4.37/qa/PHONE_TEST.md \
  docs/v.1.4.37/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.37 file: $f"
done

grep -Fq 'versionCode: **71**' docs/v.1.4.37/RELEASE.md   || fail "v1.4.37 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.37**' docs/v.1.4.37/RELEASE.md   || fail "v1.4.37 versionName snapshot missing"

for activity in StorageChooserActivity QuotaActivity MenuActivity
do
  grep -Fq "android:name=\".$activity\"" "$MANIFEST" \
    || fail "$activity missing from manifest"
done

grep -Fq 'class StorageChooserActivity' "$STORAGE" || fail "StorageChooserActivity missing"
grep -Fq 'ScrollView(this)' "$STORAGE" || fail "storage chooser scroll middle missing"
grep -Fq 'footer()' "$STORAGE" || fail "storage chooser fixed footer missing"
grep -Fq 'text = "?"' "$STORAGE" || fail "storage chooser help button missing"
grep -Fq 'showHelp()' "$STORAGE" || fail "storage chooser help action missing"
grep -Fq 'Додати іншу папку…' "$STORAGE" || fail "tree add-folder footer missing"
grep -Fq 'Додати папку…' "$STORAGE" || fail "save add-folder footer missing"
grep -Fq 'Зберегти як…' "$STORAGE" || fail "system save fallback missing"
grep -Fq 'label = "Скасувати"' "$STORAGE" || fail "storage chooser cancel footer missing"

TREE_COUNT="$(grep -R -h -F 'ACTION_OPEN_DOCUMENT_TREE' "$SRC" | wc -l | tr -d ' ')"
[ "$TREE_COUNT" -eq 1 ] || fail "expected one centralized ACTION_OPEN_DOCUMENT_TREE, found $TREE_COUNT"

CREATE_COUNT="$(grep -R -h -F 'ACTION_CREATE_DOCUMENT' "$SRC" | wc -l | tr -d ' ')"
[ "$CREATE_COUNT" -eq 1 ] || fail "expected one centralized ACTION_CREATE_DOCUMENT, found $CREATE_COUNT"

grep -Fq 'StorageChooserActivity.treeIntent(' "$IMPORT" \
  || fail "Import does not use full-screen tree chooser"
grep -Fq 'StorageChooserActivity.saveIntent(' "$SAVE_FLOW" \
  || fail "save flow does not use full-screen save chooser"

grep -Fq 'class QuotaActivity' "$QUOTA" || fail "QuotaActivity missing"
grep -Fq 'ACTION_OPEN_QUEUE' "$QUOTA" || fail "Quota queue result contract missing"
grep -Fq 'quotaScreenRequestCode' "$MAIN" || fail "Main quota result bridge missing"
grep -Fq 'showPendingJobs()' "$MAIN" || fail "Main Pending bridge missing"

grep -Fq 'class MenuActivity' "$MENU" || fail "MenuActivity missing"
grep -Fq 'text = "Меню"' "$MENU" || fail "Menu screen title missing"
grep -Fq 'compactButton("Меню")' "$MAIN" || fail "Home Ще → Меню rename missing"
grep -Fq 'menuScreenRequestCode' "$MAIN" || fail "Menu result bridge missing"
grep -Fq 'MenuActivity.ACTION_THEME' "$MAIN" || fail "Theme action bridge missing"
grep -Fq 'MenuActivity.ACTION_DATA' "$MAIN" || fail "Data action bridge missing"
grep -Fq 'MenuActivity.ACTION_SERVICE' "$MAIN" || fail "Service action bridge missing"

grep -Fq 'openQuotaScreen()' "$MAIN" || fail "Home Quota full-screen navigation missing"
grep -Fq 'openMenuScreen()' "$MAIN" || fail "Home Menu full-screen navigation missing"
if grep -Fq 'private fun showQuotaDialog()' "$MAIN"; then
  fail "superseded Home quota modal still present"
fi
if grep -Fq 'private fun showMoreActions()' "$MAIN"; then
  fail "superseded Home More modal still present"
fi

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

grep -Fq 'Neon Dark Home colors remain unchanged' docs/v.1.4.37/REGRESSION_CHECKLIST.md \
  || fail "Neon Dark color-lock regression guard missing"
grep -Fq 'fixed top bar' docs/v.1.4.37/RELEASE.md \
  || fail "fixed-header release contract missing"
grep -Fq 'fixed bottom controls' docs/v.1.4.37/RELEASE.md \
  || fail "fixed-footer release contract missing"

echo "PASS:"
echo "- immutable v1.4.37 release snapshot + current fullscreen architecture"
echo "- full-screen storage chooser registered"
echo "- remembered roots scroll independently from fixed controls"
echo "- help / Add / Cancel are explicit"
echo "- one centralized system tree picker"
echo "- one centralized system create-document picker"
echo "- Import + save flows route through StorageChooserActivity"
echo "- Quota is a dedicated full-screen page"
echo "- Ще renamed to Меню and Menu is a dedicated full-screen page"
echo "- Queue resume bridge remains owned by MainActivity"
echo "- no broad filesystem permission"
echo "- Neon Dark color lock preserved in QA contract"
