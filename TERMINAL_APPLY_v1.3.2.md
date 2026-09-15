# Termux — YTM Importer v1.3.2 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.3.2_FULL.zip`

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v132-temp"
mkdir -p "$HOME/ytm-v132-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.3.2_FULL.zip   -d "$HOME/ytm-v132-temp"

cp -a "$HOME/ytm-v132-temp/YTM_Importer_v1.3.2_FULL/." .
rm -rf "$HOME/ytm-v132-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

grep -n 'Ручний вибір:\|Заміна для:'   app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt

grep -n 'Project «\|OpenableColumns.DISPLAY_NAME'   app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt

bash scripts/release-preflight.sh

git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.3.2: clarify manual selections and project save feedback"
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

Встановити поверх v1.3.1 без uninstall.

## Phone test

1. Відкрити список із manual selection.
2. Original track має бути великим основним рядком.
3. Під ним: `Ручний вибір: <selected result>`.
4. Праворуч: `✓ вибрано`.
5. Review має використовувати те саме формулювання.
6. Save Project.
7. Toast має показати:
   - `Project «<playlist name>» збережено`
   - фактичне ім'я `.ytm.json` файлу.
8. У file picker перейменувати файл і перевірити, що toast показує нове ім'я.

Якщо build впав:

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.3.2-build-errors.txt
```
