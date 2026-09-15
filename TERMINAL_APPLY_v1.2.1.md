# Termux — YTM Importer v1.2.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.2.1_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v121-temp"
mkdir -p "$HOME/ytm-v121-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.2.1_FULL.zip   -d "$HOME/ytm-v121-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v121-temp/YTM_Importer_v1.2.1_FULL/." .
rm -rf "$HOME/ytm-v121-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 29
versionName = "1.2.1"
```

## 5. DataActivity

```bash
ls -l   app/src/main/java/com/saney/ytmimporter/DataActivity.kt

grep -n 'DataActivity'   app/src/main/AndroidManifest.xml   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. Release preflight

```bash
bash scripts/release-preflight.sh
```

Очікується PASS, включно з:

```text
- dedicated HistoryActivity navigation
- dedicated DataActivity navigation
```

## 7. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.2.1: add dedicated data screen"
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

## 9. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх v1.2.0 без uninstall.

## 10. Тест

1. `Ще → Дані`.
2. Має відкритися окремий fullscreen screen.
3. Перевірити History/Queue/Cache/quota summary.
4. Зберегти Full Backup.
5. Restore цього backup.
6. Перевірити safety snapshot.
7. Rollback.
8. History TXT/JSON.
9. Queue JSON.
10. Share History TXT.
11. Share Full Backup.
12. Back → main screen.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.2.1-build-errors.txt
```
