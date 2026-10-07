#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MENU="$ROOT/tools/termux/ytm-menu.sh"
ADVANCED="$ROOT/tools/termux/ytm-advanced-menu.sh"
STATUS="$ROOT/tools/termux/ytm-status.sh"
REPO_STATUS="$ROOT/tools/termux/ytm-repo-status.sh"
RELEASE_STATUS="$ROOT/tools/termux/ytm-release-status.sh"
BUILD="$ROOT/tools/termux/ytm-build-apk.sh"
DOWNLOAD="$ROOT/tools/termux/ytm-download-apk.sh"
HELP="$ROOT/tools/termux/ytm-help.sh"
CONTRACT="$ROOT/docs/v.1.4.55/TERMUX_OPERATOR_MENU_CONTRACT.md"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

for f in "$MENU" "$ADVANCED" "$STATUS" "$REPO_STATUS" "$RELEASE_STATUS" "$BUILD" "$DOWNLOAD" "$HELP" "$CONTRACT"; do
  test -f "$f" || fail "missing #52 file: $f"
done

for label in \
  '1 — Оновити проєкт' \
  '2 — Перевірити, що зараз готово' \
  '3 — Завантажити готовий APK' \
  '4 — Відкрити папку з APK' \
  '5 — Зібрати новий APK' \
  '6 — Перевірити локальні зміни' \
  '7 — Розширені / релізні дії'
do
  grep -Fq "$label" "$MENU" || fail "main menu label missing: $label"
done

for forbidden in \
  'echo "2 — Status"' \
  'echo "5 — Open YTM shell"' \
  'echo "7 — Release status"' \
  'echo "8 — Finalize stable release"' \
  'echo "9 — GitHub Actions status"'
do
  if grep -Fq "$forbidden" "$MENU"; then
    fail "developer-oriented top-level menu item still present: $forbidden"
  fi
done

for label in \
  '1 — Відкрити YTM shell' \
  '2 — Технічний стан релізу' \
  '3 — Опублікувати stable release' \
  '4 — GitHub Actions' \
  '0 — Назад'
do
  grep -Fq "$label" "$ADVANCED" || fail "advanced menu label missing: $label"
done

grep -Fq 'Що робити далі:' "$STATUS" ||
  fail "primary readiness screen has no next-action guidance"
grep -Fq 'Перевірка поточного коду:' "$STATUS" ||
  fail "current-code validation summary missing"
grep -Fq 'Підписаний APK для поточного коду:' "$STATUS" ||
  fail "current-code signed APK summary missing"
grep -Fq 'APK на телефоні:' "$STATUS" ||
  fail "downloaded APK summary missing"
grep -Fq 'VALIDATION_ROW="$(run_for_exact_head validate.yml "$REMOTE")"' "$STATUS" ||
  fail "readiness Validate is not pinned to current remote HEAD"
grep -Fq 'SIGNED_ROW="$(run_for_exact_head "$YTM_WORKFLOW" "$REMOTE")"' "$STATUS" ||
  fail "readiness signed build is not pinned to current remote HEAD"

if grep -Fq 'Phone source:' "$STATUS" || grep -Fq 'RELEASE_META' "$STATUS"; then
  fail "historical release metadata leaked into primary readiness status"
fi

grep -Fq 'ПОТОЧНИЙ КАНДИДАТ' "$RELEASE_STATUS" ||
  fail "advanced release status lacks current-candidate section"
grep -Fq 'ЗАФІКСОВАНА ІСТОРІЯ РЕЛІЗУ' "$RELEASE_STATUS" ||
  fail "advanced release status lacks historical section"
grep -Fq 'CURRENT_SIGNED_ROW="$(run_for_exact_head "$YTM_WORKFLOW" "$REMOTE")"' "$RELEASE_STATUS" ||
  fail "advanced release status does not resolve current signed build separately"

grep -Fq '[ "$VALIDATION_HEAD" = "$REMOTE_HEAD" ]' "$BUILD" ||
  fail "exact-HEAD build validation guard changed"
grep -Fq '[ "$RUN_HEAD" = "$REMOTE_HEAD" ]' "$DOWNLOAD" ||
  fail "exact-HEAD APK download guard changed"

grep -Fq '1 — Оновити проєкт' "$HELP" ||
  fail "Help does not mirror the visible operator menu"
grep -Fq 'Для звичайної PHONE QA не потрібно вручну вводити git/gh команди.' "$HELP" ||
  fail "Help does not establish menu-first operator workflow"

echo "#52 Termux operator menu audit: PASS"
