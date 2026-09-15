# Termux — YTM Importer v1.2.0 FULL

Архів:
`/sdcard/Download/YTM_Importer_v1.2.0_FULL.zip`

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v120-temp"
mkdir -p "$HOME/ytm-v120-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.2.0_FULL.zip   -d "$HOME/ytm-v120-temp"

cp -a "$HOME/ytm-v120-temp/YTM_Importer_v1.2.0_FULL/." .
rm -rf "$HOME/ytm-v120-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

ls -l app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt

grep -n 'HistoryActivity'   app/src/main/AndroidManifest.xml   app/src/main/java/com/saney/ytmimporter/MainActivity.kt

bash scripts/release-preflight.sh

git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.2.0: add dedicated history screen"
git push

gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

Якщо зелений:

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх v1.1.0 без uninstall.

Перший тест:
1. Історія → окремий fullscreen screen.
2. Search.
3. Entry → detail.
4. Back → list.
5. Back → main.
6. Save/Share YTM Project.
7. Перевірити стару History.

Якщо build впав:

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.2.0-build-errors.txt
```
