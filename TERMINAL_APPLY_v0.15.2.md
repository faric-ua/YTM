# Termux — YTM Importer v0.15.2 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.15.2_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0152-temp"
mkdir -p "$HOME/ytm-v0152-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.15.2_FULL.zip   -d "$HOME/ytm-v0152-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0152-temp/YTM_Importer_v0.15.2_FULL/." .
rm -rf "$HOME/ytm-v0152-temp"
```

## 4. Перевірити версію

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 20
versionName = "0.15.2"
```

## 5. Перевірити BuildConfig hotfix

```bash
grep -n -A4 'buildFeatures' app/build.gradle.kts
```

Має залишитись:

```text
buildFeatures {
    buildConfig = true
}
```

## 6. Перевірити ручний URL hotfix

```bash
grep -n 'getVideoInfo\|applyManualUrl\|applyManualUrlFallback'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt   app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt
```

Перевірити відображення заміни:

```bash
grep -n 'Заміна для\|manualReplacement'   app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt
```

Документація:

```bash
find docs/v.0.15.2 -maxdepth 2 -type f | sort
```

## 7. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v0.15.2: resolve manual URL metadata"
git push
```

## 8. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 9. APK

Якщо build успішний:

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Якщо build впав:

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-build-errors.txt
```

## 10. Тест v0.15.2

1. Імпортувати тестовий список.
2. Для одного треку відкрити ручний вибір.
3. `Вставити YouTube / YouTube Music URL`.
4. Вставити URL іншої версії/міксу.
5. Після підтвердження у списку основним рядком має стати
   **реальна назва відео з YouTube**.
6. Нижче має бути:
   `Заміна для: Original Artist — Original Track`.
7. Створити плейлист.
8. Перевірити `Заміни` та `Історія`:
   вони мають містити `Original → реальна назва заміни`.
