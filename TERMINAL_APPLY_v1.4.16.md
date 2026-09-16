# Termux — YTM Importer v1.4.16 FULL

Archive expected in:
`/sdcard/Download/YTM_Importer_v1.4.16_FULL.zip`

## 1. Open the repository and make sure v1.4.15 is clean

```bash
ytm
pwd
git status
git pull
```

Expected repository path:
`/storage/emulated/0/Documents/YTM`

Do not continue with unrelated uncommitted edits.

## 2. Ensure Python exists (needed only for the safe source patcher)

```bash
command -v python >/dev/null || pkg install -y python
```

## 3. Unpack the release overlay

```bash
rm -rf "$HOME/ytm-v1416-temp"
mkdir -p "$HOME/ytm-v1416-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.16_FULL.zip \
  -d "$HOME/ytm-v1416-temp"

cp -a "$HOME/ytm-v1416-temp/YTM_Importer_v1.4.16_FULL/." .
rm -rf "$HOME/ytm-v1416-temp"
```

## 4. Apply the MainActivity/preflight/changelog migration

```bash
python scripts/apply-v1.4.16.py
```

The patcher fails instead of guessing if the expected v1.4.15 code anchors are not present.

## 5. Verify version and architecture

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
grep -n 'DestinationCoordinator' app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head
grep -n 'v1.4.16' RELEASE_TEST_STATUS.md PROJECT_STATUS.txt BACKLOG.md
```

Expected:
```text
versionCode = 50
versionName = "1.4.16"
v1.4.16 = NOT TESTED YET
```

## 6. Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/mainactivity-cleanup-audit.sh
bash scripts/search-coordinator-audit.sh
bash scripts/playlist-write-coordinator-audit.sh
bash scripts/destination-coordinator-audit.sh
bash scripts/auth-persistence-audit.sh
bash scripts/result-modal-audit.sh
bash scripts/qa-plan-audit.sh
bash scripts/release-preflight.sh
```

## 7. Inspect the diff

```bash
git status
git diff --stat
git diff -- app/src/main/java/com/saney/ytmimporter/MainActivity.kt
git diff -- app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt
```

## 8. Commit + push

```bash
git add -A
git commit -m "YTM Importer v1.4.16: extract destination coordinator"
git push
```

## 9. GitHub Actions build

```bash
gh workflow run "Build Signed Android APK"
sleep 3
RUN_ID="$(
  gh run list \
    --workflow "Build Signed Android APK" \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId'
)"
echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 10. Download + install APK

```bash
bash scripts/download-latest-apk.sh
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

For update testing, install **over the existing app without uninstalling it**.

## 11. Phone QA

Use:
- `docs/v.1.4.16/REGRESSION_CHECKLIST.md`
- `docs/v.1.4.16/qa/RELEASE_TEST_PLAN.md`
- `docs/v.1.4.16/qa/MASTER_TEST_PLAN.md`

Do not mark the release PHONE TESTED until the required G-04..G-07 destination cases have actually run on the phone.
