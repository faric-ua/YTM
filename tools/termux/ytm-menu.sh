#!/data/data/com.termux/files/usr/bin/bash
set -u

REPO="/storage/emulated/0/Documents/YTM"
TOOLS="$REPO/tools/termux"

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
  echo "1 — Sync YTM"
  echo "2 — Status"
  echo "3 — Download signed APK"
  echo "4 — Install downloaded APK"
  echo "5 — Open YTM shell"
  echo "6 — Build APK manually"
  echo "0 — Вийти"
  echo
  printf "Вибір: "
  read -r choice

  case "$choice" in
    1)
      run_tool "ytm-sync.sh"
      ;;
    2)
      run_tool "ytm-status.sh"
      ;;
    3)
      run_tool "ytm-download-apk.sh"
      ;;
    4)
      run_tool "ytm-install-apk.sh"
      ;;
    5)
      clear
      cd "$REPO" || exit 1
      echo "YTM Importer:"
      pwd
      echo
      exec bash -i
      ;;
    6)
      run_tool "ytm-build-apk.sh"
      ;;
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
