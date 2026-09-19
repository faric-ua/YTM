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
UI="$SRC/ui/UiChrome.kt"
STORAGE="$SRC/StorageChooserActivity.kt"
RECENT="$SRC/RecentFileChooserActivity.kt"

for f in   TERMUX_ORGANIZE_DOWNLOAD_ZIPS.txt   "$GRADLE"   "$MANIFEST"   "$SELECTOR"   "$IMPORT"   "$DATA"   "$HISTORY"   "$PENDING"   "$SERVICE"   "$UI"   "$STORAGE"   "$RECENT"   docs/v.1.4.38/RELEASE.md   docs/v.1.4.38/UX_AUDIT.md   docs/v.1.4.38/REGRESSION_CHECKLIST.md   docs/v.1.4.38/qa/PHONE_TEST.md   docs/v.1.4.38/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.38 file: $f"
done

grep -Fq 'versionCode: **72**' docs/v.1.4.38/RELEASE.md || fail "v1.4.38 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.38**' docs/v.1.4.38/RELEASE.md || fail "v1.4.38 versionName snapshot missing"

grep -Fq 'android:name=".ListSelectorActivity"' "$MANIFEST"   || fail "ListSelectorActivity missing from manifest"
grep -Fq 'class ListSelectorActivity' "$SELECTOR"   || fail "ListSelectorActivity missing"
grep -Fq 'ScrollView(this)' "$SELECTOR"   || fail "selector scroll middle missing"
grep -Fq 'selectionSummary' "$SELECTOR"   || fail "selector selection summary missing"
grep -Fq 'footer()' "$SELECTOR"   || fail "selector fixed footer missing"
grep -Fq 'text = "?"' "$SELECTOR"   || fail "selector help button missing"
grep -Fq 'label = "Скасувати"' "$SELECTOR"   || fail "selector cancel footer missing"
grep -Fq 'Mode.SINGLE' "$SELECTOR"   || fail "single-select mode missing"
grep -Fq 'Mode.MULTI' "$SELECTOR"   || fail "multi-select mode missing"
grep -Fq 'private fun multiChoiceView(index: Int): LinearLayout' "$SELECTOR"   || fail "multi-select dedicated checkbox column missing"

SINGLE_CALLS="$(grep -F 'ListSelectorActivity.singleIntent(' "$IMPORT" | wc -l | tr -d ' ')"
MULTI_CALLS="$(grep -F 'ListSelectorActivity.multiIntent(' "$IMPORT" | wc -l | tr -d ' ')"
[ "$SINGLE_CALLS" -eq 3 ] || fail "expected three single-select flows, found $SINGLE_CALLS"
[ "$MULTI_CALLS" -eq 1 ] || fail "expected one multi-select flow, found $MULTI_CALLS"

for request in   ytmPlaylistSelectorRequestCode   selectiveExportSelectorRequestCode   deltaChainHeadSelectorRequestCode   manifestProjectSelectorRequestCode
do
  grep -Fq "$request" "$IMPORT" || fail "selector request route missing: $request"
done

if grep -Fq 'setMultiChoiceItems' "$IMPORT"; then
  fail "legacy Import multi-choice dialog remains"
fi
if grep -Fq 'UiChrome.showMenuDialog' "$IMPORT"; then
  fail "legacy Import long-list menu dialog remains"
fi

grep -Fq 'fun showDangerConfirmDialog' "$UI"   || fail "danger confirmation helper missing"

for f in "$IMPORT" "$HISTORY" "$PENDING" "$SERVICE" "$DATA"; do
  grep -Fq 'UiChrome.showDangerConfirmDialog(' "$f"     || fail "danger confirmation not used: $f"
done

grep -Fq 'confirmLabel =' "$HISTORY"   || fail "History explicit danger labels missing"
grep -Fq '"Так, видалити"' "$HISTORY"   || fail "History explicit delete copy missing"
grep -Fq '"Так, очистити"' "$HISTORY"   || fail "History bulk clear copy missing"
grep -Fq '"Так, видалити"' "$PENDING"   || fail "Pending explicit delete copy missing"
grep -Fq '"Так, очистити"' "$SERVICE"   || fail "SearchCache explicit clear copy missing"
grep -Fq 'confirmClearExpiredSearchCache' "$SERVICE"   || fail "expired SearchCache delete confirmation missing"

grep -Fq 'label = "Видалити знімок"' "$DATA"   || fail "separate snapshot delete action missing"
grep -Fq 'confirmDeleteSafetySnapshot()' "$DATA"   || fail "snapshot delete confirmation route missing"
grep -Fq 'STATE_RESTORE_CONFIRMATION_PENDING' "$DATA"   || fail "Restore rotation saved-state guard missing"
grep -Fq 'PENDING_RESTORE_CACHE_FILE' "$DATA"   || fail "pending Restore cache file guard missing"
grep -Fq 'restorePendingBackupConfirmation()' "$DATA"   || fail "Restore confirmation recreation path missing"
grep -Fq 'dialog.setOnCancelListener' "$DATA"   || fail "Restore pending-cache cancel cleanup missing"
if grep -A35 -F 'private fun restoreSafetySnapshotNow()' "$DATA" | grep -Fq 'setNegativeButton'; then
  fail "rollback-success dialog still exposes a side/destructive action"
fi

grep -Fq '"Вибрати файл"' "$DATA"   || fail "short Restore file label missing"
grep -Fq '"Готово"' "$DATA"   || fail "Restore/rollback done label missing"
grep -Fq 'label = "Відкотити Restore"' "$DATA"   || fail "short rollback label missing"
grep -Fq '"Додати папку…"' "$STORAGE"   || fail "short save-folder label missing"
grep -Fq '"Зберегти як…"' "$STORAGE"   || fail "short system-save label missing"

grep -Fq 'RecentFileChooserActivity::class.java' "$IMPORT" \
  || fail "Import generic open-document path missing"
grep -Fq 'RecentFileChooserActivity::class.java' "$DATA" \
  || fail "Data generic open-document path missing"
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$RECENT" \
  || fail "generic Android open-document fallback missing"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

grep -Fq 'Neon Dark Home colors unchanged' docs/v.1.4.38/REGRESSION_CHECKLIST.md   || fail "Neon Dark color-lock guard missing"

echo "PASS:"
echo "- immutable v1.4.38 / code 72 release snapshot"
echo "- four dynamic Import list families use ListSelectorActivity"
echo "- fixed header/summary/scroll/footer selector architecture"
echo "- legacy Import long-list menu/multi-choice dialogs removed"
echo "- destructive local mutations use explicit danger confirmation"
echo "- safety snapshot deletion separated from rollback success"
echo "- short mobile action copy present"
echo "- R1 checkbox centering + Restore rotation persistence guarded"
echo "- both generic open-document flows remain through the recent-file selector"
echo "- no broad filesystem permission"
echo "- Neon Dark color lock retained"
