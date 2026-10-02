#!/data/data/com.termux/files/usr/bin/bash
set -u

cd "$HOME" || exit 1

echo "========================================"
echo "          YTM COMMAND HELP"
echo "========================================"
echo
echo "MENU"
echo "  ytm"
echo "      Запустити YTM Importer Menu"
echo
echo "  ytm-code"
echo "      Запустити останній YTM code package"
echo
echo "PROJECT"
echo "  cd ~/YTM"
echo "      Перейти в локальний репозиторій YTM"
echo
echo "  git status"
echo "      Показати локальні зміни"
echo
echo "  git branch --show-current"
echo "      Показати поточну гілку"
echo
echo "  git log -1 --oneline"
echo "      Показати поточний commit"
echo
echo "GITHUB ACTIONS"
echo "  gh run list --repo faric-ua/YTM"
echo "      Показати останні GitHub Actions runs"
echo
echo "  gh run view RUN_ID --repo faric-ua/YTM"
echo "      Деталі конкретного run"
echo
echo "  gh run view RUN_ID --repo faric-ua/YTM --log-failed"
echo "      Показати лог кроків, які впали"
echo
echo "  gh run watch RUN_ID --repo faric-ua/YTM"
echo "      Стежити за виконанням run"
echo
echo "YTM MENU"
echo "  1   Sync YTM"
echo "  2   Status репозиторію"
echo "  3   Download signed APK"
echo "  4   Open APK folder"
echo "  5   Open YTM shell"
echo "  6   Validate + Build signed APK"
echo "  7   Release status"
echo "  8   Finalize stable release"
echo "  9   GitHub Actions status"
echo "  H   Help / Команди"
echo
echo "CURRENT PROJECT"
echo "  Repo:   faric-ua/YTM"
echo "  Local:  ~/YTM"
echo
echo "Підказка:"
echo "  Для звичайної роботи використовуй меню."
echo "  Команди вище потрібні переважно для діагностики."
