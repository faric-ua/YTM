# Termux — YTM Importer v1.4.14 FULL

Archive:
`/sdcard/Download/YTM_Importer_v1.4.14_FULL.zip`

## Apply

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v1414-temp"
mkdir -p "$HOME/ytm-v1414-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.14_FULL.zip   -d "$HOME/ytm-v1414-temp"

cp -a "$HOME/ytm-v1414-temp/YTM_Importer_v1.4.14_FULL/." .
rm -rf "$HOME/ytm-v1414-temp"
```

## Verify version/status

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
grep -n 'v1.4.12\|v1.4.13\|v1.4.14' RELEASE_TEST_STATUS.md
```

Expected:
```text
versionCode = 48
versionName = "1.4.14"

v1.4.12 = NOT TESTED
v1.4.13 = PARTIALLY PHONE-TESTED
v1.4.14 = NOT TESTED YET
```

## Audits

```bash
bash scripts/mainactivity-audit.sh
bash scripts/mainactivity-cleanup-audit.sh
bash scripts/search-coordinator-audit.sh
bash scripts/auth-persistence-audit.sh
bash scripts/result-modal-audit.sh
bash scripts/qa-plan-audit.sh
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
git commit -m "YTM Importer v1.4.14: recover auth and modalize playlist result"
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

Install **over** the existing app. Do not uninstall if testing silent account
recovery across update.

## First v1.4.14 phone tests

1. Confirm old build is authorized.
2. Install v1.4.14 over it.
3. Launch and DO NOT tap Step 2.
4. Confirm Step 2 becomes green automatically when Google can restore grant.
5. Run 3-track create.
6. Confirm completion is a modal, not inline frame.
7. Test Open / Copy / Close.
8. Use `qa/TEST_RUN_TEMPLATE.md` for the rest of the run.

Full plan:
`qa/MASTER_TEST_PLAN.md`

## Build failure

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.4.14-build-errors.txt
```
