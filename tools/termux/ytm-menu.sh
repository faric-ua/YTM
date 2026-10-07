#!/data/data/com.termux/files/usr/bin/bash
set -u

REPO="${YTM_REPO_DIR:-$HOME/YTM}"
TOOLS="$REPO/tools/termux"

cd "$HOME" || exit 1

pause_menu() {
  echo
  printf "Натисни Enter, щоб повернутися в меню..."
  read -r _
}

run_tool() {
  local script="$1"
  clear
  bash "$TOOLS/$script"
  local rc=$?
  echo
  if [ "$rc" -ne 0 ]; then
    echo "Команда завершилась з кодом $rc"
  fi
  pause_menu
}

while true; do
  clear
  echo "========================================"
  echo "          YTM Importer Menu"
  echo "========================================"
  echo
  echo "1 — Оновити проєкт"
  echo "2 — Перевірити, що зараз готово"
  echo "3 — Завантажити готовий APK"
  echo "4 — Відкрити папку з APK"
  echo "5 — Зібрати новий APK"
  echo "6 — Перевірити локальні зміни"
  echo "7 — Розширені / релізні дії"
  echo "H — Допомога"
  echo "0 — Вийти"
  echo
  printf "Вибір: "
  read -r choice

  case "$choice" in
    1) run_tool "ytm-sync.sh" ;;
    2) run_tool "ytm-status.sh" ;;
    3) run_tool "ytm-download-apk.sh" ;;
    4) run_tool "ytm-install-apk.sh" ;;
    5) run_tool "ytm-build-apk.sh" ;;
    6) run_tool "ytm-repo-status.sh" ;;
    7)
      clear
      bash "$TOOLS/ytm-advanced-menu.sh"
      ;;
    h|H) run_tool "ytm-help.sh" ;;
    0)
      clear
      exit 0
      ;;
    *)
      echo
      echo "Невідомий пункт: $choice"
      sleep 1
      ;;
  esac
done
