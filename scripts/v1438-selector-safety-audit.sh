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
grep -Fq 'Mode.SINGLE' "$SELECTOR"   || fail "single-select mode missing"
grep -Fq 'Mode.MULTI' "$SELECTOR"   || fail "multi-select mode missing"
grep -Fq 'ScrollView(this)' "$SELECTOR"   || fail "selector scroll middle missing"
grep -Fq 'label = "Далі"' "$SELECTOR"   || fail "fixed multi-select Next action missing"
grep -Fq 'label = "Скасувати"' "$SELECTOR"   || fail "selector Cancel action missing"
grep -Fq 'text = "?"' "$SELECTOR"   || fail "selector help action missing"
grep -Fq 'STATE_SELECTED_JSON' "$SELECTOR"   || fail "multi-select saved-instance state missing"

SINGLE_COUNT="$(grep -F -c 'ListSelectorActivity.singleIntent(' "$IMPORT" || true)"
[ "$SINGLE_COUNT" -ge 3 ]   || fail "expected at least three full-screen single-select Import flows, found $SINGLE_COUNT"

grep -Fq 'ListSelectorActivity.multiIntent(' "$IMPORT"   || fail "selective export full-screen multi-select missing"

if grep -Fq '.setMultiChoiceItems(' "$IMPORT"; then
  fail "legacy selective-export multi-choice dialog still remains in Import"
fi

for request in   ytmPlaylistSelectorRequestCode   selectiveExportSelectorRequestCode   deltaChainHeadSelectorRequestCode   manifestProjectSelectorRequestCode
do
  grep -Fq "$request" "$IMPORT"     || fail "selector result bridge missing: $request"
done

grep -Fq 'decodeSelectorPlaylistValues' "$IMPORT"   || fail "selective export result decode missing"
grep -Fq 'decodeDeltaHeadSelection' "$IMPORT"   || fail "delta head result decode missing"
grep -Fq 'decodeManifestEntry' "$IMPORT"   || fail "manifest result decode missing"

grep -Fq '"Додати папку…"' "$STORAGE"   || fail "short save-folder action missing"
grep -Fq '"Зберегти як…"' "$STORAGE"   || fail "short system-save action missing"

grep -Fq '"Вибрати файл"' "$DATA"   || fail "short Restore picker label missing"
grep -Fq '"Готово"' "$DATA"   || fail "Restore/rollback completion label missing"
grep -Fq 'label = "Видалити знімок"' "$DATA"   || fail "separate snapshot-delete action missing"
grep -Fq 'confirmDeleteSafetySnapshot()' "$DATA"   || fail "snapshot-delete confirmation bridge missing"
grep -Fq '"Так, видалити"' "$DATA"   || fail "snapshot explicit destructive confirmation missing"
grep -Fq '"Так, відкотити"' "$DATA"   || fail "rollback explicit confirmation missing"

if grep -Fq '"Видалити snapshot"' "$DATA"; then
  fail "direct snapshot deletion still remains in DataActivity"
fi

grep -Fq '"Так, видалити"' "$HISTORY"   || fail "History explicit delete confirmation missing"
grep -Fq '"Так, очистити"' "$HISTORY"   || fail "History explicit clear confirmation missing"
grep -Fq '"Так, видалити"' "$PENDING"   || fail "Queue explicit delete confirmation missing"
grep -Fq '"Так, очистити"' "$SERVICE"   || fail "SearchCache explicit clear confirmation missing"

for token in '"видал"' '"очист"' '"відкот"'
do
  grep -Fq "$token" "$UI"     || fail "UiChrome danger-label token missing: $token"
done

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

grep -Fq 'Neon Dark Home color reference unchanged' docs/v.1.4.38/REGRESSION_CHECKLIST.md   || fail "Neon Dark color-lock regression guard missing"

echo "PASS:"
echo "- v1.4.38 / code 72"
echo "- reusable full-screen single/multi selector"
echo "- four Import long-list selector families migrated"
echo "- selective export no longer uses modal multi-choice"
echo "- short mobile save/restore action labels"
echo "- snapshot deletion separated from rollback success"
echo "- explicit destructive confirmations"
echo "- danger styling recognizes longer destructive labels"
echo "- no broad filesystem permission"
echo "- Neon Dark color lock preserved"
