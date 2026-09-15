# Termux — YTM Importer v0.10.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.10.1_FULL.zip`

## 1. Перейти в репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0101-temp"
mkdir -p "$HOME/ytm-v0101-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.10.1_FULL.zip   -d "$HOME/ytm-v0101-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0101-temp/YTM_Importer_v0.10.1_FULL/." .
rm -rf "$HOME/ytm-v0101-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 12
versionName = "0.10.1"
```

Перевірити, що великі постійні панелі прибрані:

```bash
grep -n 'accountPanel\|quotaText\|googleAccountText\|youtubeChannelText'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Команда не повинна знайти старі UI-панелі.

Перевірити нові компактні кнопки:

```bash
grep -n 'accountButton\|quotaButton'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Документація:

```bash
find docs/v.0.10.1 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.10.1: compact main screen"
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

## 7. Завантажити APK

```bash
bash scripts/download-latest-apk.sh
```

Відкрити:

```bash
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

## 8. Тест UI

Перевірити:

1. Після запуску немає великих блоків Account/Quota над списком.
2. `2. Акаунт` відкриває всі дані акаунта.
3. Після авторизації кнопка стає `2. Акаунт ✓`.
4. `Квота` відкриває повну інформацію квоти.
5. `Черга` працює як у v0.10.0.
6. Під час створення плейлиста account/quota інформація все одно показується на потрібному кроці.
7. Список треків займає основну частину екрана.
