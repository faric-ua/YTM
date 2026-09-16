# Termux — YTM Importer v1.4.11 FULL

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v1411-temp"
mkdir -p "$HOME/ytm-v1411-temp"
unzip -o /sdcard/Download/YTM_Importer_v1.4.11_FULL.zip -d "$HOME/ytm-v1411-temp"
cp -a "$HOME/ytm-v1411-temp/YTM_Importer_v1.4.11_FULL/." .
rm -rf "$HOME/ytm-v1411-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

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
bash scripts/dialog-animation-audit.sh
bash scripts/release-preflight.sh

git add -A
git commit -m "YTM Importer v1.4.11: disable custom dialog window animation"
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

Install over v1.4.10 without uninstall.
