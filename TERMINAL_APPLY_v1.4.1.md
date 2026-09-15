# Termux — YTM Importer v1.4.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.4.1_FULL.zip`

## 1. Репозиторій

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v141-temp"
mkdir -p "$HOME/ytm-v141-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.1_FULL.zip   -d "$HOME/ytm-v141-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v141-temp/YTM_Importer_v1.4.1_FULL/." .
rm -rf "$HOME/ytm-v141-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 35
versionName = "1.4.1"
```

## 5. MainActivity cleanup audit

```bash
bash scripts/mainactivity-audit.sh
```

Очікується:

```text
PASS:
- legacy destination AlertDialog flow removed
- DestinationActivity bridge retained
- create/append write core retained
```

## 6. Q-001 remains open

```bash
grep -n 'Q-001\|OPEN' OPEN_QUESTIONS.md BACKLOG.md
```

## 7. Release preflight

```bash
bash scripts/release-preflight.sh
```

## 8. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.4.1: remove legacy destination dialogs"
git push
```

## 9. Build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

## 10. Download + install

```bash
bash scripts/download-latest-apk.sh

termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

Встановити поверх v1.4.0 без uninstall.

## 11. Phone regression

### New playlist
1. Import 3 tracks.
2. Search/Review.
3. Step 4.
4. New playlist.
5. Private.
6. Create.
7. Open result in YTM.

### Existing playlist
1. Step 4.
2. Existing playlist.
3. Search target.
4. Duplicate scan.
5. Test `Пропустити дублікати`.
6. Repeat with `Додати все одно`.

### Back navigation
- confirm → existing list;
- existing list → destination start;
- destination start → Main.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.4.1-build-errors.txt
```
