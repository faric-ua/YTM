# Termux — YTM Importer v1.4.7 FULL

Archive: `/sdcard/Download/YTM_Importer_v1.4.7_FULL.zip`

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v147-temp"
mkdir -p "$HOME/ytm-v147-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.7_FULL.zip \
  -d "$HOME/ytm-v147-temp"

cp -a "$HOME/ytm-v147-temp/YTM_Importer_v1.4.7_FULL/." .
rm -rf "$HOME/ytm-v147-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/button-layout-audit.sh
bash scripts/compact-review-audit.sh
bash scripts/action-hierarchy-audit.sh
bash scripts/service-navigation-audit.sh
bash scripts/release-preflight.sh

git status
git diff --stat
git add -A
git commit -m "YTM Importer v1.4.7: fix service navigation and structured info UI"
git push
```

## Build

```bash
gh workflow run "Build Signed Android APK"
sleep 3
RUN_ID="$(gh run list --workflow "Build Signed Android APK" --limit 1 --json databaseId --jq '.[0].databaseId')"
echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## Install

```bash
bash scripts/download-latest-apk.sh
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Install over v1.4.6 without uninstall.
