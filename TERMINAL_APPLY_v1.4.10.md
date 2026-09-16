# Termux — YTM Importer v1.4.10 FULL

Archive:

`/sdcard/Download/YTM_Importer_v1.4.10_FULL.zip`

## Apply

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v1410-temp"
mkdir -p "$HOME/ytm-v1410-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.10_FULL.zip   -d "$HOME/ytm-v1410-temp"

cp -a "$HOME/ytm-v1410-temp/YTM_Importer_v1.4.10_FULL/." .
rm -rf "$HOME/ytm-v1410-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Expected:

```text
versionCode = 44
versionName = "1.4.10"
```

## Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/button-layout-audit.sh
bash scripts/compact-review-audit.sh
bash scripts/action-hierarchy-audit.sh
bash scripts/service-navigation-audit.sh
bash scripts/dialog-bounds-audit.sh
bash scripts/configuration-state-audit.sh
bash scripts/rotation-layout-audit.sh
bash scripts/release-preflight.sh
```

## Commit

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.10: stabilize rotation layout and dialog anchor"
git push
```

## Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## Install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Install over v1.4.9 without uninstall.

## Phone regression

### Step 2
- account connected / green;
- rotate portrait → landscape → portrait several times;
- Step 2 remains level with Step 1;
- same top and bottom button bounds.

### Dialogs
Open:
- More;
- Quota;
- Problem Tracks;
- Project actions.

Expected:
- no center-first position;
- no jump upward;
- first visible frame already at safe top;
- long content still scrolls.

## Build failure

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.4.10-build-errors.txt
```
