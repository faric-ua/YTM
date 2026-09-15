# Termux — YTM Importer v0.14.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.14.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0140-temp"
mkdir -p "$HOME/ytm-v0140-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.14.0_FULL.zip   -d "$HOME/ytm-v0140-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0140-temp/YTM_Importer_v0.14.0_FULL/." .
rm -rf "$HOME/ytm-v0140-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 17
versionName = "0.14.0"
```

Перевірити FileProvider:

```bash
grep -n 'FileProvider\|file_paths' app/src/main/AndroidManifest.xml

cat app/src/main/res/xml/file_paths.xml
```

Перевірити Diagnostics / Share:

```bash
grep -n 'Сервіс\|shareTextFile\|buildDiagnosticsText\|showSearchCacheTools'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head -40
```

Перевірити SearchCache stats:

```bash
grep -n 'SearchCacheStats\|fun stats\|fun clearExpired'   app/src/main/java/com/saney/ytmimporter/youtube/SearchCache.kt
```

Документація:

```bash
find docs/v.0.14.0 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.14.0: diagnostics share and cache tools"
git push
```

## 6. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 7. APK

```bash
bash scripts/download-latest-apk.sh
```

Встановити:

```bash
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

## 8. Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed
```

## 9. Швидкий тест v0.14.0

1. `Сервіс → Діагностика`.
2. Перевірити version / Android / quota / cache.
3. `Сервіс → Поділитися Diagnostics TXT`.
4. Android має відкрити стандартне Share menu.
5. `Дані → Поділитися History TXT`.
6. `Сервіс → SearchCache`.
7. Запам'ятати кількість кешованих записів.
8. `Очистити прострочені` — безпечний тест.
9. `Квота → Google Cloud` — має відкритися браузер.
10. Повний `Очистити весь SearchCache` тестувати тільки якщо готові,
    що наступні пошуки знову витрачатимуть search quota.
