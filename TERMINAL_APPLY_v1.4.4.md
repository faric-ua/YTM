# Termux — YTM Importer v1.4.4 FULL

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v144-temp"
mkdir -p "$HOME/ytm-v144-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.4_FULL.zip   -d "$HOME/ytm-v144-temp"

cp -a "$HOME/ytm-v144-temp/YTM_Importer_v1.4.4_FULL/." .
rm -rf "$HOME/ytm-v144-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/button-layout-audit.sh
bash scripts/release-preflight.sh

git add -A
git commit -m "YTM Importer v1.4.4: adaptive buttons and state colors"
git push

gh workflow run "Build Signed Android APK"
sleep 3
RUN_ID="$(gh run list --workflow "Build Signed Android APK" --limit 1 --json databaseId --jq '.[0].databaseId')"
echo "$RUN_ID"
gh run watch "$RUN_ID"
```

If green:

```bash
bash scripts/download-latest-apk.sh
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Install over v1.4.3 without uninstall.
