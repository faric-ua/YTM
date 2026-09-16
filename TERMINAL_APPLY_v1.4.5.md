# Termux — YTM Importer v1.4.5 FULL

```bash
cd ~/storage/shared/Documents/YTM

rm -rf "$HOME/ytm-v145-temp"
mkdir -p "$HOME/ytm-v145-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.5_FULL.zip \
  -d "$HOME/ytm-v145-temp"

cp -a "$HOME/ytm-v145-temp/YTM_Importer_v1.4.5_FULL/." .
rm -rf "$HOME/ytm-v145-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/button-layout-audit.sh
bash scripts/compact-review-audit.sh
bash scripts/release-preflight.sh

git add -A
git commit -m "YTM Importer v1.4.5: compact review and quota actions"
git push
```

Build:

```bash
gh workflow run "Build Signed Android APK"
sleep 3
RUN_ID="$(gh run list --workflow "Build Signed Android APK" --limit 1 --json databaseId --jq '.[0].databaseId')"
echo "$RUN_ID"
gh run watch "$RUN_ID"
```
