# Termux — YTM Importer v0.10.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.10.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v0100-temp"
mkdir -p "$HOME/ytm-v0100-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.10.0_FULL.zip   -d "$HOME/ytm-v0100-temp"
```

## 3. Скопіювати поверх репозиторію

```bash
cp -a "$HOME/ytm-v0100-temp/YTM_Importer_v0.10.0_FULL/." .
rm -rf "$HOME/ytm-v0100-temp"
```

## 4. Перевірити

```bash
git status
git diff --stat

grep -n 'versionCode\|versionName' app/build.gradle.kts

find docs/v.0.10.0 -maxdepth 2 -type f | sort
```

Очікується:

```text
versionCode = 11
versionName = "0.10.0"
```

Нові ключові файли:

```text
app/src/main/java/com/saney/ytmimporter/model/PendingJob.kt
app/src/main/java/com/saney/ytmimporter/storage/QuotaTracker.kt
app/src/main/java/com/saney/ytmimporter/storage/PendingJobStore.kt
scripts/download-latest-apk.sh
docs/v.0.10.0/
```

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.10.0: quota planner and pending queue"
git push
```

## 6. Запустити APK build прямо з Termux

```bash
gh workflow run "Build Signed Android APK"

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"

gh run watch "$RUN_ID"
```

## 7. Якщо збірка успішна — завантажити APK з Termux

Найпростіше:

```bash
bash scripts/download-latest-apk.sh
```

Або вручну:

```bash
mkdir -p /sdcard/Download/YTM-v0.10.0

gh run download "$RUN_ID"   --dir /sdcard/Download/YTM-v0.10.0
```

Знайти APK:

```bash
find /sdcard/Download/YTM-v0.10.0 -type f -name '*.apk'
```

Відкрити Android installer (інсталятор):

```bash
termux-open "$(find /sdcard/Download/YTM-v0.10.0 -type f -name '*.apk' | head -n 1)"
```

## 8. Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed
```

Надішліть цей текст у чат — скрін не потрібний.

## 9. Тест v0.10.0 на телефоні

1. Імпортувати 2–3 треки.
2. Натиснути `Знайти`.
3. Перевірити Search plan.
4. Натиснути `Квота`.
5. Створити тестовий playlist.
6. Перевірити quota plan перед write.
7. Перевірити `Черга` — після успішного job вона має бути порожньою.
8. Реальний quotaExceeded тестуватимемо окремо, коли ліміт буде вичерпаний.
