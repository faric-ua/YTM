# Termux — YTM Importer v1.0.0-rc4 FULL

Архів:
`/sdcard/Download/YTM_Importer_v1.0.0-rc4_FULL.zip`

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status

rm -rf "$HOME/ytm-v100rc4-temp"
mkdir -p "$HOME/ytm-v100rc4-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.0.0-rc4_FULL.zip   -d "$HOME/ytm-v100rc4-temp"

cp -a "$HOME/ytm-v100rc4-temp/YTM_Importer_v1.0.0-rc4_FULL/." .
rm -rf "$HOME/ytm-v100rc4-temp"

grep -n 'versionCode\|versionName' app/build.gradle.kts

grep -n 'setup-java@v5\|Verify signed APK\|apksigner\|zipalign\|sha256sum'   .github/workflows/build-apk.yml

bash scripts/rc-preflight.sh

git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.0.0-rc4: harden signed release workflow"
git push

gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

Якщо зелений:

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Не видаляти стару версію.

Якщо build впав:

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-RC4-build-errors.txt
```
