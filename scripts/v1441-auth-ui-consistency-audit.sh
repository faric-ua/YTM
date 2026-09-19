#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
SEARCH="app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt"
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
PARSER="app/src/main/java/com/saney/ytmimporter/parser/PlaylistParser.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in "$MAIN" "$SEARCH" "$BACKUP" "$DATA" "$PARSER" "$UI" "$MANIFEST" docs/v.1.4.41/RELEASE.md docs/v.1.4.41/UX_AUDIT.md docs/v.1.4.41/REGRESSION_CHECKLIST.md docs/v.1.4.41/qa/PHONE_TEST.md docs/v.1.4.41/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.41 file: $f"
done

grep -Fq 'versionCode: **77**' docs/v.1.4.41/RELEASE.md || fail "historical v1.4.41 versionCode evidence missing"
grep -Fq 'versionName: **1.4.41**' docs/v.1.4.41/RELEASE.md || fail "historical v1.4.41 versionName evidence missing"

grep -Fq 'val authorizationInvalidated: Boolean' "$SEARCH" || fail "SearchResult authorizationInvalidated flag missing"
grep -Fq 'onAuthorizationInvalidated: (Throwable) -> Unit = {}' "$SEARCH" || fail "Search auth-invalid callback missing"
grep -Fq 'if (isAuthorizationFailure(error))' "$SEARCH" || fail "Search 401 branch missing"
grep -A24 -F 'if (isAuthorizationFailure(error))' "$SEARCH" | grep -Fq 'TrackStatus.NEW' || fail "Search 401 does not return current track to NEW"
grep -A24 -F 'if (isAuthorizationFailure(error))' "$SEARCH" | grep -Fq 'onAuthorizationInvalidated(error)' || fail "Search 401 does not propagate authorization invalidation"
grep -A30 -F 'if (isAuthorizationFailure(error))' "$SEARCH" | grep -Fq 'break' || fail "Search 401 does not stop the loop"

grep -Fq 'onAuthorizationInvalidated = { error ->' "$MAIN" || fail "Main does not receive Search auth invalidation"
grep -A6 -F 'onAuthorizationInvalidated = { error ->' "$MAIN" | grep -Fq 'invalidateAuthorizationIfNeeded(error)' || fail "Main does not route Search auth failure through shared invalidation"
grep -Fq '!result.authorizationInvalidated' "$MAIN" || fail "Review auto-open is not guarded after auth invalidation"
grep -Fq 'recoverPersistedAuthorizationFailures()' "$MAIN" || fail "legacy persisted auth-failure recovery missing"

grep -Fq '.setPositiveButton("Змінити")' "$MAIN" || fail "compact account action label missing"

if ! grep -Fq 'Плейлисти створюватимуться в цьому YouTube/YTM профілі.' "$MAIN" &&
   ! grep -Fq 'Доступ: пошук, створення та робота з плейлистами' "$MAIN"; then
  fail "compact account profile explanation missing"
fi

grep -Fq 'OAuth token у цьому вікні не показується' "$MAIN" ||
  fail "account dialog token-privacy explanation missing"

grep -Fq 'RESTORABLE_PREFS_NAMES' "$BACKUP" || fail "restorable preference group list missing"
grep -A4 -F 'private val RESTORABLE_PREFS_NAMES' "$BACKUP" | grep -Fq 'it == "quota_tracker_v1"' || fail "quota_tracker_v1 is not excluded from Restore"
grep -A12 -F 'private fun applyBackupJson' "$BACKUP" | grep -Fq 'RESTORABLE_PREFS_NAMES.forEach' || fail "Restore does not use quota-safe preference list"
grep -Fq 'Поточна локальна оцінка квоти НЕ відкочується з backup.' "$DATA" || fail "Restore quota-preservation copy missing"

grep -Fq 'fallbackPlaylistName(fileName)' "$PARSER" || fail "human playlist-name fallback missing"
grep -Fq ".replace('_', ' ')" "$PARSER" || fail "underscore humanization missing"
grep -Fq 'YTM(?:\\s+Importer)?' "$PARSER" || fail "trailing YTM service-marker cleanup missing"
grep -Fq 'Vol.${match.groupValues[1]}' "$PARSER" || fail "Vol number normalization missing"

grep -Fq 'orderHorizontalActions(actions)' "$UI" || fail "horizontal action ordering helper not used"
grep -Fq 'private fun isDismissiveAction' "$UI" || fail "dismissive action classifier missing"
for label in Скасувати Закрити "Не зараз" Назад; do
  grep -Fq "\"$label\"" "$UI" || fail "dismissive modal label missing from UiChrome: $label"
done

python - "$UI" <<'PY'
from pathlib import Path
import sys
text = Path(sys.argv[1]).read_text(encoding="utf-8")
start = text.index("fun showDangerConfirmDialog(")
end = text.index("fun showContentDialog(", start)
block = text[start:end]
if block.index("label = confirmLabel") >= block.index('label = "Скасувати"'):
    raise SystemExit("FAIL: danger confirmation must be before Cancel (left before right)")
PY

grep -Fq '## v1.4.41' CHANGELOG.md || fail "v1.4.41 changelog entry missing"
grep -Eq '^\| v1\.4\.41 \| \*\*PARTIALLY PHONE-TESTED — .+\*\* \|' RELEASE_TEST_STATUS.md || fail "v1.4.41 partial phone-test status missing"
grep -F '| v1.4.41 |' RELEASE_TEST_STATUS.md | grep -Fq 'NOT EXHAUSTIVE' || fail "v1.4.41 non-exhaustive QA qualifier missing"

# v1.4.41 is a historical behavior audit. Storage permission policy is
# intentionally not pinned to the live manifest because v1.4.42-R1 changes it.

echo "PASS:"
echo "- historical v1.4.41 / code 77 evidence"
echo "- Search HTTP 401 propagates once and stops retryable workspace search"
echo "- legacy auth-failed rows have re-login recovery"
echo "- account modal copy/action is compact"
echo "- full Restore preserves live quota tracker"
echo "- imported filename fallback is human-readable"
echo "- horizontal modal action sides are standardized"
echo "- v1.4.41 release/QA docs present"
echo "- historical v1.4.41 audit is decoupled from current storage-permission policy"
