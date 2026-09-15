# Termux — YTM Importer v0.13.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.13.1_FULL.zip`

## 1. Перейти в репозиторій

```bash
cd ~/storage/shared/Documents/YTM
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0131-temp"
mkdir -p "$HOME/ytm-v0131-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.13.1_FULL.zip   -d "$HOME/ytm-v0131-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v0131-temp/YTM_Importer_v0.13.1_FULL/." .
rm -rf "$HOME/ytm-v0131-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 16
versionName = "0.13.1"
```

Перевірити hotfix:

```bash
grep -n 'private fun showDataTools\|ListView\|Повний backup → JSON\|Restore повного backup'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head -30
```

Документація:

```bash
find docs/v.0.13.1 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.13.1: fix data menu"
git push
```

## 6. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

gh run watch "$RUN_ID"
```

## 7. Завантажити й встановити

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

## 8. Тест

Натиснути `Дані`.

Має бути видно 5 окремих пунктів:

1. Історія → TXT
2. Історія → JSON
3. Черга → JSON
4. Повний backup → JSON
5. Restore повного backup

Для першого безпечного тесту:
`Історія → TXT` → зберегти у Download → відкрити файл.
