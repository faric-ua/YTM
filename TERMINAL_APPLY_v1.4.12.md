# Termux — YTM Importer v1.4.12 FULL

Archive:

`/sdcard/Download/YTM_Importer_v1.4.12_FULL.zip`

## 1. Repository

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Extract

```bash
rm -rf "$HOME/ytm-v1412-temp"
mkdir -p "$HOME/ytm-v1412-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.12_FULL.zip   -d "$HOME/ytm-v1412-temp"
```

## 3. Apply

```bash
cp -a "$HOME/ytm-v1412-temp/YTM_Importer_v1.4.12_FULL/." .
rm -rf "$HOME/ytm-v1412-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Expected:

```text
versionCode = 46
versionName = "1.4.12"
```

## 5. Cleanup verification

```bash
wc -l app/src/main/java/com/saney/ytmimporter/MainActivity.kt

bash scripts/mainactivity-audit.sh
bash scripts/mainactivity-cleanup-audit.sh
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
```

Expected MainActivity line count is below 4000.

## 6. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.12: cleanup legacy MainActivity flows"
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

## 8. Install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Install over v1.4.11 without uninstall.

## 9. Phone regression

This release is mostly architectural. Check that the dedicated screens still
own their flows:

1. Import file + pasted text.
2. Review + manual URL + Project save/share.
3. Queue details + Resume.
4. History details/actions.
5. Data Backup/Restore/export.
6. Service Diagnostics/SearchCache/About.
7. Create new playlist.
8. Append to existing playlist.
9. Quota pause → Queue.
10. Current workspace survives normal navigation.

Q-002 dialog motion is intentionally deferred and is NOT a pass/fail item for
v1.4.12.

## Build failure

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.4.12-build-errors.txt
```
