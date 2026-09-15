# Termux — YTM Importer v1.3.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.3.0_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v130-temp"
mkdir -p "$HOME/ytm-v130-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.3.0_FULL.zip   -d "$HOME/ytm-v130-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v130-temp/YTM_Importer_v1.3.0_FULL/." .
rm -rf "$HOME/ytm-v130-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 31
versionName = "1.3.0"
```

## 5. New screens/store

```bash
ls -l   app/src/main/java/com/saney/ytmimporter/ImportActivity.kt   app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt   app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt
```

## 6. Navigation

```bash
grep -n 'ImportActivity\|ReviewActivity\|CurrentPlaylistStore'   app/src/main/AndroidManifest.xml   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 7. Release preflight

```bash
bash scripts/release-preflight.sh
```

Очікується PASS, включно з:

```text
- dedicated ImportActivity navigation
- dedicated ReviewActivity navigation
- persistent current playlist workspace
- Review manual URL result contract
- Review repeat-search result contract
```

## 8. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.3.0: add import and review screens"
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

Встановити поверх v1.2.2 без uninstall.

## 11. First phone test

1. `1. Імпорт` → dedicated Import screen.
2. Import 3-track TXT/text sample.
3. Back on Main: summary shows 3 tracks.
4. `3. Знайти / перевірити`.
5. Search completes and Review screen opens.
6. Filters work.
7. Open one track.
8. Open candidate in YTM.
9. Select another candidate.
10. Back → Main summary updates.
11. Tap a row on Main → focused Review track.
12. Manual URL → metadata lookup → same Review track reopens.
13. `Повторити пошук` → Search plan → Review.
14. Close/reopen app → current playlist restores.
15. Create small Private playlist.

## Main stress regression

After the 3-track smoke test, use the 50-track Clubland list.

## If build fails

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.3.0-build-errors.txt
```
