# Termux — YTM Importer v1.4.8 FULL

Archive:

`/sdcard/Download/YTM_Importer_v1.4.8_FULL.zip`

## 1. Repository

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Extract

```bash
rm -rf "$HOME/ytm-v148-temp"
mkdir -p "$HOME/ytm-v148-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.8_FULL.zip   -d "$HOME/ytm-v148-temp"
```

## 3. Apply

```bash
cp -a "$HOME/ytm-v148-temp/YTM_Importer_v1.4.8_FULL/." .
rm -rf "$HOME/ytm-v148-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Expected:

```text
versionCode = 42
versionName = "1.4.8"
```

## 5. Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/button-layout-audit.sh
bash scripts/compact-review-audit.sh
bash scripts/action-hierarchy-audit.sh
bash scripts/service-navigation-audit.sh
bash scripts/dialog-bounds-audit.sh
bash scripts/release-preflight.sh
```

## 6. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.8: fix safe bounds for tall dialogs"
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

Install over v1.4.7 without uninstall.

## Phone test

1. Open `Квота`.
2. Confirm rounded top edge + full title are visible.
3. Scroll to Google Cloud / Queue / Close.
4. Open `Ще → Заміни`.
5. Confirm title/subtitle + first tile are visible.
6. Scroll to TikTok list / Full text / Close.
7. Open a short custom dialog and confirm it still appears centered.
8. Check bottom actions stay above the gesture/navigation area.

## If build fails

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.4.8-build-errors.txt
```
