#!/data/data/com.termux/files/usr/bin/bash
set -u

REPO="${YTM_REPO_DIR:-$HOME/YTM}"
TOOLS="$REPO/tools/termux"

cd "$HOME" || exit 1

pause_menu() {
  echo
  printf "Натисни Enter, щоб повернутися..."
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
  echo "   YTM — Розширені / релізні дії"
  echo "========================================"
  echo
  echo "1 — Відкрити YTM shell"
  echo "2 — Технічний стан релізу"
  echo "3 — Опублікувати stable release"
  echo "4 — GitHub Actions"
  echo "0 — Назад"
  echo
  printf "Вибір: "
  read -r choice

  case "$choice" in
    1)
      clear
      cd "$REPO" || exit 1
      echo "YTM shell:"
      pwd
      echo "Для повернення введи: exit"
      echo
      bash -i
      cd "$HOME" || exit 1
      ;;
    2) run_tool "ytm-release-status.sh" ;;
    3) run_tool "ytm-finalize-release.sh" ;;
    4) run_tool "ytm-actions-status.sh" ;;
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
