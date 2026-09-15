# Termux — YTM Importer v1.4.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.4.0_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v140-temp"
mkdir -p "$HOME/ytm-v140-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.0_FULL.zip \
  -d "$HOME/ytm-v140-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v140-temp/YTM_Importer_v1.4.0_FULL/." .
rm -rf "$HOME/ytm-v140-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 34
versionName = "1.4.0"
```

## 5. Destination screen

```bash
ls -l \
  app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt

grep -n 'DestinationActivity' \
  app/src/main/AndroidManifest.xml \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. Open question marker

```bash
grep -n 'Q-001\|revisit later\|повернутися пізніше' \
  OPEN_QUESTIONS.md BACKLOG.md
```

## 7. Preflight

```bash
bash scripts/release-preflight.sh
```

Очікується PASS, включно з:

```text
- dedicated DestinationActivity navigation
- destination duplicate/final-confirm result contract
- deferred UX question Q-001 documented
```

## 8. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.0: add destination and create screen"
git push
```

## 9. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list \
    --workflow "Build Signed Android APK" \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 10. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх v1.3.2 без uninstall.

## 11. Phone test

### New playlist
1. Import/search/review a 3-track list.
2. `4. Створити / додати`.
3. Dedicated Destination screen must open.
4. Check Project name / counts / account context.
5. Switch Private / Unlisted / Public.
6. Back → Main.
7. Reopen and create a small Private playlist.

### Existing playlist
1. Step 4 → Existing.
2. Existing list should load only now.
3. Search a playlist by title.
4. Select target.
5. Duplicate preview should open as a screen.
6. Test Back → existing list → Back → destination start.
7. Test `Пропустити дублікати`.
8. On another test, try `Додати все одно` only where safe.

### Regression
1. History entry appears after successful write.
2. Pending Queue still works on quota/write interruption.
3. Current Project remains available before write.
4. Q-001 remains deferred — do not spend this test cycle on it.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed \
  > /sdcard/Download/YTM-v1.4.0-build-errors.txt
```
