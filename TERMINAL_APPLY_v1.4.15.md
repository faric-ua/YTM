# Termux — YTM Importer v1.4.15 FULL

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v1415-temp"
mkdir -p "$HOME/ytm-v1415-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.15_FULL.zip   -d "$HOME/ytm-v1415-temp"

cp -a "$HOME/ytm-v1415-temp/YTM_Importer_v1.4.15_FULL/." .
rm -rf "$HOME/ytm-v1415-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts
grep -n 'v1.4.14\|v1.4.15' RELEASE_TEST_STATUS.md
grep -n 'BUG-003' qa/BUG_REGISTER.md
```

Expected:
```text
versionCode = 49
versionName = "1.4.15"
v1.4.14 = PARTIALLY PHONE-TESTED — FAIL
v1.4.15 = NOT TESTED YET
```

Audits:
```bash
bash scripts/mainactivity-audit.sh
bash scripts/mainactivity-cleanup-audit.sh
bash scripts/search-coordinator-audit.sh
bash scripts/playlist-write-coordinator-audit.sh
bash scripts/auth-persistence-audit.sh
bash scripts/result-modal-audit.sh
bash scripts/qa-plan-audit.sh
bash scripts/release-preflight.sh
```

Commit/build:
```bash
git add -A
git commit -m "YTM Importer v1.4.15: extract playlist write coordinator"
git push

gh workflow run "Build Signed Android APK"
sleep 3
RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"
echo "$RUN_ID"
gh run watch "$RUN_ID"
```

Download/install:
```bash
bash scripts/download-latest-apk.sh
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

QA:
- root current plans: `qa/`
- release snapshot: `docs/v.1.4.15/qa/`
- known bugs: `qa/BUG_REGISTER.md`
