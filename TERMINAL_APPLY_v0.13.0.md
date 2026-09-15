# Termux — YTM Importer v0.13.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.13.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0130-temp"
mkdir -p "$HOME/ytm-v0130-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.13.0_FULL.zip   -d "$HOME/ytm-v0130-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0130-temp/YTM_Importer_v0.13.0_FULL/." .
rm -rf "$HOME/ytm-v0130-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 15
versionName = "0.13.0"
```

Перевірити новий backup manager:

```bash
ls -l   app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt
```

Перевірити кнопку і операції:

```bash
grep -n 'Дані\|Експорт History\|Створити повний backup\|Відновити з backup'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head -30
```

Документація:

```bash
find docs/v.0.13.0 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.13.0: export backup and restore"
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

## 9. Швидкий тест v0.13.0

1. `Дані → Експорт History → TXT`.
2. Зберегти в Download.
3. Відкрити TXT та перевірити старі плейлисти.
4. `Дані → Експорт History → JSON`.
5. `Дані → Створити повний backup → JSON`.
6. Не видаляючи дані, вибрати:
   `Дані → Відновити з backup JSON`.
7. Перевірити:
   - History;
   - Чергу;
   - Квоту;
   - повторний пошук кешованого треку.

Тест `uninstall/clear data → restore` краще робити лише тоді,
коли backup точно збережено і поточні дані не шкода втратити.
