# Termux — YTM Importer v1.0.0-rc2 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.0.0-rc2_FULL.zip`

## 1. Перейти в репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати RC2

```bash
rm -rf "$HOME/ytm-v100rc2-temp"
mkdir -p "$HOME/ytm-v100rc2-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.0.0-rc2_FULL.zip   -d "$HOME/ytm-v100rc2-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v100rc2-temp/YTM_Importer_v1.0.0-rc2_FULL/." .
rm -rf "$HOME/ytm-v100rc2-temp"
```

## 4. Перевірити версію

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 22
versionName = "1.0.0-rc2"
```

## 5. Перевірити Playlist Project

```bash
ls -l   app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt

grep -n 'Зберегти YTM Project\|Поділитися YTM Project\|applyImportedProject'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. Перевірити History Back

```bash
grep -n 'setNegativeButton("Назад")'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt | tail -10
```

## 7. RC preflight

```bash
bash scripts/rc-preflight.sh
```

Очікується `PASS`.

## 8. Документація

```bash
find docs/v.1.0.0-rc2 -maxdepth 2 -type f | sort
```

## 9. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.0.0-rc2: history navigation and playlist projects"
git push
```

## 10. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 11. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх RC1. Не робити uninstall.

## 12. Перший тест RC2

### History navigation

1. `Історія`.
2. Відкрити Playlist A.
3. Натиснути `Назад`.
4. Має повернути до списку History.
5. Відкрити Playlist B без повторного виходу з History.

### YTM Project

1. `Історія → Playlist → Дії`.
2. `Зберегти YTM Project`.
3. Зберегти `.ytm.json` у Download.
4. На головному екрані натиснути `1. Файл`.
5. Вибрати збережений `.ytm.json`.
6. Перевірити:
   - назву playlist;
   - кількість треків;
   - ручні заміни;
   - exact videoId.
7. Якщо всі videoId відновлено — НЕ натискати Search.
8. Натиснути `4. Створити` і створити тестовий playlist.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-RC2-build-errors.txt
```

Надіслати `YTM-RC2-build-errors.txt` у чат.
