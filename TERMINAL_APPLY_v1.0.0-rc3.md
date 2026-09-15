# Termux — YTM Importer v1.0.0-rc3 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.0.0-rc3_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v100rc3-temp"
mkdir -p "$HOME/ytm-v100rc3-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.0.0-rc3_FULL.zip \
  -d "$HOME/ytm-v100rc3-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v100rc3-temp/YTM_Importer_v1.0.0-rc3_FULL/." .
rm -rf "$HOME/ytm-v100rc3-temp"
```

## 4. Версія

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 23
versionName = "1.0.0-rc3"
```

## 5. Перевірити backup hardening

```bash
grep -n 'SCHEMA_VERSION\|preferencesSha256\|restoreSafetySnapshot\|BuildConfig.VERSION_NAME' \
  app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt
```

Перевірити rollback UI:

```bash
grep -n 'Відкотити останній Restore\|confirmRestoreSafetySnapshot' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. RC preflight

```bash
bash scripts/rc-preflight.sh
```

Очікується `PASS`.

## 7. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.0.0-rc3: harden backup restore safety"
git push
```

## 8. Build

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

## 9. APK

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх RC2. Не uninstall.

## 10. Тест RC3

1. `Дані → Повний backup → JSON`.
2. Зберегти backup у Download.
3. `Дані → Restore повного backup`.
4. У preview має бути `Integrity: SHA-256 ✓`.
5. Виконати Restore.
6. Перевірити History / Queue / Quota.
7. `Дані → Відкотити останній Restore`.
8. Підтвердити rollback.
9. Перевірити, що попередній локальний стан повернувся.

Якщо build впав:

```bash
gh run view "$RUN_ID" --log-failed \
  > /sdcard/Download/YTM-RC3-build-errors.txt
```
