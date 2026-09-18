#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
GRADLE="app/build.gradle.kts"
MANIFEST="app/src/main/AndroidManifest.xml"
SELECTOR="$SRC/ListSelectorActivity.kt"
IMPORT="$SRC/ImportActivity.kt"
DATA="$SRC/DataActivity.kt"
HISTORY="$SRC/HistoryActivity.kt"
PENDING="$SRC/PendingActivity.kt"
SERVICE="$SRC/ServiceActivity.kt"
STORAGE="$SRC/StorageChooserActivity.kt"
UI="$SRC/ui/UiChrome.kt"

for f in   "$GRADLE"   "$MANIFEST"   "$SELECTOR"   "$IMPORT"   "$DATA"   "$HISTORY"   "$PENDING"   "$SERVICE"   "$STORAGE"   "$UI"   docs/v.1.4.38/RELEASE.md   docs/v.1.4.38/UX_AUDIT.md   docs/v.1.4.38/REGRESSION_CHECKLIST.md   docs/v.1.4.38/qa/PHONE_TEST.md   docs/v.1.4.38/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.38 file: $f"
done

grep -Fq 'versionCode = 72' "$GRADLE" || fail "versionCode 72 missing"
grep -Fq 'versionName = "1.4.38"' "$GRADLE" || fail "versionName 1.4.38 missing"

grep -Fq 'android:name=".ListSelectorActivity"' "$MANIFEST"   || fail "ListSelectorActivity missing from manifest"

grep -Fq 'class ListSelectorActivity' "$SELECTOR"   || fail "ListSelectorActivity missing"
grep -Fq 'ScrollView(this)' "$SELECTOR"   || fail "selector scroll middle missing"
grep -Fq 'label = "Далі"' "$SELECTOR"   || fail "selector fixed Next action missing"
grep -Fq 'label = "Скасувати"' "$SELECTOR"   || fail "selector fixed Cancel action missing"
grep -Fq 'text = "?"' "$SELECTOR"   || fail "selector help button missing"
grep -Fq 'STATE_SELECTED_IDS' "$SELECTOR"   || fail "selector recreation state missing"
grep -Fq 'EXTRA_RESULT_IDS' "$SELECTOR"   || fail "selector result contract missing"

SINGLE_COUNT="$(grep -F 'ListSelectorActivity.singleIntent' "$IMPORT" | wc -l | tr -d ' ')"
[ "$SINGLE_COUNT" -eq 3 ]   || fail "expected 3 full-screen single selectors in Import, found $SINGLE_COUNT"

MULTI_COUNT="$(grep -F 'ListSelectorActivity.multiIntent' "$IMPORT" | wc -l | tr -d ' ')"
[ "$MULTI_COUNT" -eq 1 ]   || fail "expected 1 full-screen multi selector in Import, found $MULTI_COUNT"

if grep -Fq 'UiChrome.showMenuDialog' "$IMPORT"; then
  fail "legacy long Import menu dialog remains"
fi

if grep -Fq 'setMultiChoiceItems' "$IMPORT"; then
  fail "legacy Import multi-choice dialog remains"
fi

for code in   ytmPlaylistSelectorRequestCode   selectiveExportSelectorRequestCode   deltaChainHeadSelectorRequestCode   manifestProjectSelectorRequestCode
do
  grep -Fq "$code" "$IMPORT"     || fail "selector result bridge missing: $code"
done

grep -Fq 'encodeDeltaHeadsState' "$IMPORT"   || fail "delta selector recreation state missing"
grep -Fq 'encodeManifestState' "$IMPORT"   || fail "manifest selector recreation state missing"

grep -Fq 'fun showDestructiveConfirmDialog' "$UI"   || fail "shared destructive confirmation helper missing"

for f in "$IMPORT" "$DATA" "$HISTORY" "$PENDING" "$SERVICE"
do
  grep -Fq 'UiChrome.showDestructiveConfirmDialog' "$f"     || fail "destructive confirmation helper not used: $f"
done

grep -Fq '"Так, видалити"' "$HISTORY"   || fail "History explicit delete confirmation missing"
grep -Fq '"Так, очистити все"' "$HISTORY"   || fail "History bulk clear confirmation missing"
grep -Fq '"Так, видалити"' "$PENDING"   || fail "Queue explicit delete confirmation missing"
grep -Fq 'confirmDeleteExpiredSearchCache' "$SERVICE"   || fail "expired SearchCache confirmation missing"
grep -Fq '"Так, очистити"' "$SERVICE"   || fail "SearchCache explicit clear confirmation missing"
grep -Fq 'confirmDeleteSafetySnapshot' "$DATA"   || fail "safety snapshot deletion confirmation missing"
grep -Fq '"Видалити знімок"' "$DATA"   || fail "separate Data snapshot delete action missing"

if grep -Fq '"Видалити snapshot"' "$DATA"; then
  fail "old direct snapshot-delete copy remains"
fi

grep -Fq 'buttonLabel = "Вибрати файл"' "$DATA"   || fail "short Restore file action missing"
grep -Fq '"Готово"' "$DATA"   || fail "Restore/rollback success copy not shortened"
grep -Fq 'label = "Відкотити"' "$DATA"   || fail "short rollback action missing"
grep -Fq '"Додати папку…"' "$STORAGE"   || fail "short add-folder footer copy missing"
grep -Fq '"Зберегти як…"' "$STORAGE"   || fail "short system-save footer copy missing"
grep -Fq 'maxLines = 1' "$STORAGE"   || fail "storage footer single-line guard missing"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

grep -Fq 'Neon Dark Home colors unchanged' docs/v.1.4.38/REGRESSION_CHECKLIST.md   || fail "Neon Dark regression guard missing"

echo "PASS:"
echo "- v1.4.38 / code 72"
echo "- four dynamic Import selector families are full-screen"
echo "- multi-select footer is fixed and recreation-safe"
echo "- no legacy Import long-list menu/multi-choice dialog remains"
echo "- explicit destructive confirmation helper is used across destructive local actions"
echo "- safety snapshot deletion is separated from rollback-success acknowledgement"
echo "- Restore/storage mobile action copy shortened"
echo "- no broad filesystem permission"
echo "- Neon Dark color lock preserved"
