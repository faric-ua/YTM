# Termux — YTM Importer v1.4.13 FULL

Archive:

`/sdcard/Download/YTM_Importer_v1.4.13_FULL.zip`

## Important

v1.4.12 is explicitly marked **NOT TESTED**.

v1.4.13 is also **NOT TESTED YET** until the phone regression is completed.

## Apply

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v1413-temp"
mkdir -p "$HOME/ytm-v1413-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.13_FULL.zip   -d "$HOME/ytm-v1413-temp"

cp -a "$HOME/ytm-v1413-temp/YTM_Importer_v1.4.13_FULL/." .
rm -rf "$HOME/ytm-v1413-temp"
```

## Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Expected:

```text
versionCode = 47
versionName = "1.4.13"
```

## Test-status marker

```bash
grep -n 'v1.4.12\|v1.4.13' RELEASE_TEST_STATUS.md
```

Expected:
- v1.4.12 = NOT TESTED
- v1.4.13 = NOT TESTED YET

## Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/mainactivity-cleanup-audit.sh
bash scripts/search-coordinator-audit.sh
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

## Commit

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.13: extract SearchCoordinator"
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

Install over the current app without uninstall.

## Phone regression priority

Because v1.4.12 was not tested, do not test only the new coordinator.

Minimum:
1. Import file/text.
2. Search a small list.
3. Repeat search and confirm cache.
4. Manual URL → repeat search → manual choice survives.
5. Save/open YTM Project with exact IDs.
6. Queue/History/Data/Service open.
7. Create or append one playlist.

Then use the 50-track Clubland set for the search stress test.

## Build failure

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.4.13-build-errors.txt
```
