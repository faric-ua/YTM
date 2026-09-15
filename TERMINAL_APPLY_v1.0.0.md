# Termux — YTM Importer v1.0.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.0.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v100-temp"
mkdir -p "$HOME/ytm-v100-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.0.0_FULL.zip   -d "$HOME/ytm-v100-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v100-temp/YTM_Importer_v1.0.0_FULL/." .
rm -rf "$HOME/ytm-v100-temp"
```

## 4. Перевірити version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 26
versionName = "1.0.0"
```

## 5. Перевірити workflow

```bash
grep -n 'setup-java@v5\|Verify signed APK\|apksigner\|zipalign\|sha256sum'   .github/workflows/build-apk.yml
```

## 6. Preflight

```bash
bash scripts/rc-preflight.sh
```

Очікується PASS.

## 7. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.0.0: stable release"
git push
```

## 8. Stable build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 9. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх RC4. Uninstall не робити.

## 10. Stable smoke test

1. `Сервіс → Про програму` → `1.0.0 (26)`.
2. History збережена.
3. Account доступний.
4. Відкрити YTM Project.
5. Existing Playlist.
6. Full Backup → Restore → Rollback.
7. Якщо немає blocker/data-loss bugs — stable підтверджено.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.0.0-build-errors.txt
```
