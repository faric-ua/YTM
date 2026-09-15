# Termux — YTM Importer v1.0.0-rc3.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.0.0-rc3.1_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v100rc31-temp"
mkdir -p "$HOME/ytm-v100rc31-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.0.0-rc3.1_FULL.zip   -d "$HOME/ytm-v100rc31-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v100rc31-temp/YTM_Importer_v1.0.0-rc3.1_FULL/." .
rm -rf "$HOME/ytm-v100rc31-temp"
```

## 4. Перевірити exact hotfix

```bash
sed -n '2803,2815p'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Має бути один Kotlin рядок:

```text
"«Повний backup», а не History JSON.\n" +
```

а НЕ реальний перенос всередині лапок.

## 5. Версія

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 24
versionName = "1.0.0-rc3.1"
```

## 6. Preflight

```bash
bash scripts/rc-preflight.sh
```

Очікується PASS, включно з:

```text
- RC3.1 Data dialog string guard
```

## 7. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.0.0-rc3.1: fix data dialog Kotlin syntax"
git push
```

## 8. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 9. Якщо зелений

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Ставити поверх RC2 / RC3 attempt. Uninstall не робити.

## 10. Якщо знову впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-RC31-build-errors.txt
```

Надіслати файл у чат.
