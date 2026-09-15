# Termux — YTM Importer v1.2.2 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.2.2_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v122-temp"
mkdir -p "$HOME/ytm-v122-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.2.2_FULL.zip   -d "$HOME/ytm-v122-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v122-temp/YTM_Importer_v1.2.2_FULL/." .
rm -rf "$HOME/ytm-v122-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 30
versionName = "1.2.2"
```

## 5. PendingActivity

```bash
ls -l   app/src/main/java/com/saney/ytmimporter/PendingActivity.kt

grep -n 'PendingActivity'   app/src/main/AndroidManifest.xml   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. Resume contract

```bash
grep -n 'EXTRA_RESUME_JOB_ID\|pendingQueueRequestCode\|resumePendingJob'   app/src/main/java/com/saney/ytmimporter/PendingActivity.kt   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 7. Release preflight

```bash
bash scripts/release-preflight.sh
```

Очікується PASS, включно з:

```text
- dedicated HistoryActivity navigation
- dedicated DataActivity navigation
- dedicated PendingActivity navigation
- Pending Queue resume result contract
```

## 8. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.2.2: add dedicated pending queue screen"
git push
```

## 9. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 10. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх v1.2.1 без uninstall.

## 11. Phone test

### Empty Queue
1. `Черга`.
2. Має відкритися окремий screen навіть якщо черга порожня.
3. Back → Main.

### Existing PendingJob
1. Відкрити `Черга`.
2. Search.
3. Відкрити job.
4. Перевірити counters/error/tracks.
5. Back → Queue list.
6. `Продовжити`.
7. Має повернути на MainActivity і запустити старий resume flow.
8. Account/channel mismatch protection має залишитися.
9. Перевірити Delete на тестовому job.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.2.2-build-errors.txt
```
