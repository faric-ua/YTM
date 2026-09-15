# Termux — YTM Importer v1.4.2 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.4.2_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v142-temp"
mkdir -p "$HOME/ytm-v142-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.2_FULL.zip   -d "$HOME/ytm-v142-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v142-temp/YTM_Importer_v1.4.2_FULL/." .
rm -rf "$HOME/ytm-v142-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 36
versionName = "1.4.2"
```

## 5. Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/release-preflight.sh
```

## 6. Commit + push

```bash
git add -A
git commit -m "YTM Importer v1.4.2: safe insets and dialog polish"
git push
```

## 7. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 8. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Ставити поверх v1.4.1 без uninstall.

## 9. Phone regression

### Insets
- перевірити верхній відступ на Main і Review;
- перевірити нижній відступ над navigation area.

### Styled dialogs
- `Ще`
- `Імпорт трекліста`
- `Поточний YTM Project`
