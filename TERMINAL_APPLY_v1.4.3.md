# Termux — YTM Importer v1.4.3 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.4.3_FULL.zip`

## Apply

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v143-temp"
mkdir -p "$HOME/ytm-v143-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.3_FULL.zip   -d "$HOME/ytm-v143-temp"

cp -a "$HOME/ytm-v143-temp/YTM_Importer_v1.4.3_FULL/." .
rm -rf "$HOME/ytm-v143-temp"
```

## Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 37
versionName = "1.4.3"
```

## Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/release-preflight.sh
```

## Commit

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.3: unify dialogs and improve button spacing"
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

Install over v1.4.2 without uninstall.

## Visual test

Open:
- More;
- Quota;
- History → actions / clear;
- Replacements;
- Data → backup / restore / share;
- Review → Project / Repeat Search / manual URL.

There should be no old flat gray dialog styling.
