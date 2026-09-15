# Termux — YTM Importer v0.15.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.15.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0150-temp"
mkdir -p "$HOME/ytm-v0150-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.15.0_FULL.zip \
  -d "$HOME/ytm-v0150-temp"
```

## 3. Скопіювати поверх репозиторію

```bash
cp -a "$HOME/ytm-v0150-temp/YTM_Importer_v0.15.0_FULL/." .
rm -rf "$HOME/ytm-v0150-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 18
versionName = "0.15.0"
```

Перевірити icon resources:

```bash
find app/src/main/res -maxdepth 2 \
  \( -name 'ic_launcher*.xml' -o -name 'colors.xml' \) | sort

 grep -n 'android:icon\|android:roundIcon' app/src/main/AndroidManifest.xml
```

Перевірити error helper:

```bash
ls -l app/src/main/java/com/saney/ytmimporter/util/ErrorMessages.kt

grep -n 'ErrorMessages\|showAboutDialog\|Regression checklist' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head -40
```

Документація:

```bash
find docs/v.0.15.0 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.15.0: stabilize before release candidate"
git push
```

## 6. Build

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

## 7. APK

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

## 8. Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed
```

## 9. Швидкий тест v0.15.0

1. APK встановлюється поверх попередньої версії.
2. На launcher перевірити нову іконку.
3. `Сервіс → Про програму`.
4. Переконатись, що версія = 0.15.0 (18).
5. `Сервіс → Regression checklist`.
6. Зробити пошук 1–2 треків і перевірити, що все старе працює.
7. Повний regression test — `docs/v.0.15.0/REGRESSION_CHECKLIST.md`.
