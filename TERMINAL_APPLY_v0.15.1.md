# Termux — YTM Importer v0.15.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.15.1_FULL.zip`

## 1. Перейти в корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0151-temp"
mkdir -p "$HOME/ytm-v0151-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.15.1_FULL.zip   -d "$HOME/ytm-v0151-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0151-temp/YTM_Importer_v0.15.1_FULL/." .
rm -rf "$HOME/ytm-v0151-temp"
```

## 4. Перевірити саме hotfix

```bash
grep -n -A4 'buildFeatures' app/build.gradle.kts
```

Має бути:

```text
buildFeatures {
    buildConfig = true
}
```

Перевірити версію:

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 19
versionName = "0.15.1"
```

Перевірити, що MainActivity все ще використовує BuildConfig:

```bash
grep -n 'BuildConfig'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Документація:

```bash
find docs/v.0.15.1 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v0.15.1: enable BuildConfig generation"
git push
```

## 6. Запустити build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 7. Якщо build успішний

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

## 8. Якщо build знову впав

```bash
gh run view "$RUN_ID" --log-failed > /sdcard/Download/YTM-build-errors.txt
```

Файл:

`/sdcard/Download/YTM-build-errors.txt`

можна надіслати в чат.
