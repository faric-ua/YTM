# Termux — YTM Importer v0.12.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.12.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0120-temp"
mkdir -p "$HOME/ytm-v0120-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.12.0_FULL.zip   -d "$HOME/ytm-v0120-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0120-temp/YTM_Importer_v0.12.0_FULL/." .
rm -rf "$HOME/ytm-v0120-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 14
versionName = "0.12.0"
```

Перевірити API duplicate scan:

```bash
grep -n 'listPlaylistVideoIds\|playlistItems'   app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt
```

Перевірити UI duplicate logic:

```bash
grep -n 'Пропустити дублікати\|DUPLICATE\|checkDuplicatesBeforeAppend'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head -30
```

Документація:

```bash
find docs/v.0.12.0 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.12.0: existing playlist duplicate detection"
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

## 9. Швидкий тест v0.12.0

1. Візьміть existing playlist, де точно є 1–2 треки з імпортованого списку.
2. `4. Створити`.
3. `Додати до існуючого плейлиста`.
4. Виберіть цей playlist.
5. Дочекайтесь `Перевіряю дублікати...`.
6. Переконайтесь, що показано правильну кількість.
7. Виберіть `Пропустити дублікати`.
8. Після завершення:
   - дублікати мають статус `⧉ дублікат`;
   - нові треки мають бути додані;
   - History має показати duplicate count;
   - `Заміни` має містити дублікати.
9. Окремо перевірте `Додати все одно`.
