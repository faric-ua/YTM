# Termux — YTM Importer v0.11.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.11.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0110-temp"
mkdir -p "$HOME/ytm-v0110-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.11.0_FULL.zip   -d "$HOME/ytm-v0110-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v0110-temp/YTM_Importer_v0.11.0_FULL/." .
rm -rf "$HOME/ytm-v0110-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 13
versionName = "0.11.0"
```

Нові ключові файли:

```bash
ls -l app/src/main/java/com/saney/ytmimporter/model/HistoryEntry.kt
ls -l app/src/main/java/com/saney/ytmimporter/storage/HistoryStore.kt
```

Перевірити кнопку:

```bash
grep -n 'Історія'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt | head
```

Документація:

```bash
find docs/v.0.11.0 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.11.0: operation history"
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

## 7. Якщо build успішний — APK

```bash
bash scripts/download-latest-apk.sh
```

Відкрити APK:

```bash
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

## 8. Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed
```

Надішліть текст помилки в чат.

## 9. Тест v0.11.0

1. Створити маленький новий плейлист.
2. Відкрити `Історія`.
3. Перевірити статус `завершено`.
4. Відкрити плейлист з Історії в YTM.
5. Скопіювати підсумок.
6. Додати треки до існуючого плейлиста.
7. Перевірити другий History Entry.
8. Перевірити історичний журнал проблем на списку з ручною заміною/skip.
9. Видалити один локальний History Entry.
10. `Очистити` тестувати тільки якщо ці тестові записи вже не потрібні.

Історія видаляє тільки локальні записи — YouTube/YTM плейлисти не видаляються.
