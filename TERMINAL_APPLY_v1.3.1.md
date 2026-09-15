# Termux — YTM Importer v1.3.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.3.1_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v131-temp"
mkdir -p "$HOME/ytm-v131-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.3.1_FULL.zip \
  -d "$HOME/ytm-v131-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v131-temp/YTM_Importer_v1.3.1_FULL/." .
rm -rf "$HOME/ytm-v131-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 32
versionName = "1.3.1"
```

## 5. Manual URL fix

```bash
grep -n 'resolveCurrentTrack\|keepManualSelection\|track.manuallySelected &&' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. Working Project

```bash
grep -n 'exportWorkingPlaylist' \
  app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt

grep -n 'Зберегти Project\|Поділитися YTM Project' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt
```

## 7. Workspace clear / Backup

```bash
grep -n 'EXTRA_CLEAR_WORKSPACE\|Очистити поточний список' \
  app/src/main/java/com/saney/ytmimporter/ImportActivity.kt

grep -n 'current_playlist_v1' \
  app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt
```

## 8. Release preflight

```bash
bash scripts/release-preflight.sh
```

Очікується PASS, включно з:

```text
- working-list YTM Project export
- manual URL canonical-track guard
- manual selections protected from cache overwrite
- Full Backup includes current workspace
```

## 9. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.3.1: fix manual selections and working projects"
git push
```

## 10. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list \
    --workflow "Build Signed Android APK" \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 11. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх v1.3.0, без uninstall.

## 12. Найважливіший phone test

1. Імпортувати 3 треки.
2. Знайти автоматично.
3. Відкрити один track у Review.
4. Вставити вручну URL ІНШОГО відео.
5. Review має повернутися саме на цей original track.
6. `Зараз вибрано` має показати ручне відео.
7. Закрити Review і відкрити знову — ручний вибір лишився.
8. `Повторити пошук` — manual selection не має замінитися cached candidate.
9. До створення playlist: `Зберегти Project`.
10. Імпортувати інший список.
11. Знову відкрити saved `.ytm.json` — selections/candidates мають повернутися.
12. Перезапустити app — останній workspace відновлюється.
13. `1. Імпорт → Очистити поточний список` — workspace очищається.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed \
  > /sdcard/Download/YTM-v1.3.1-build-errors.txt
```
