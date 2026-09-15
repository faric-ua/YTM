#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

REPO="${1:-faric-ua/YTM}"
OUT_DIR="${2:-/sdcard/Download/YTM-APK}"

echo "Репозиторій: $REPO"
echo "Папка: $OUT_DIR"

RUN_ID="$(
  gh run list \
    --repo "$REPO" \
    --workflow "Build Signed Android APK" \
    --status success \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId'
)"

if [ -z "$RUN_ID" ] || [ "$RUN_ID" = "null" ]; then
  echo "Не знайдено успішної збірки."
  exit 1
fi

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

echo "Завантажую artifact (артефакт) з run ID: $RUN_ID"
gh run download "$RUN_ID" \
  --repo "$REPO" \
  --dir "$OUT_DIR"

APK="$(find "$OUT_DIR" -type f -name '*.apk' | head -n 1)"

if [ -z "$APK" ]; then
  echo "APK не знайдено після завантаження."
  exit 1
fi

echo
echo "Готово:"
echo "$APK"
echo
echo "Щоб відкрити APK в Android:"
echo "termux-open \"$APK\""
