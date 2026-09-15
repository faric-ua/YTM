# Termux — YTM Importer v1.1.0 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.1.0_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати

```bash
rm -rf "$HOME/ytm-v110-temp"
mkdir -p "$HOME/ytm-v110-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.1.0_FULL.zip   -d "$HOME/ytm-v110-temp"
```

## 3. Скопіювати

```bash
cp -a "$HOME/ytm-v110-temp/YTM_Importer_v1.1.0_FULL/." .
rm -rf "$HOME/ytm-v110-temp"
```

## 4. Version

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 27
versionName = "1.1.0"
```

## 5. UI / onboarding

```bash
grep -n '4 кроки до плейлиста\|showImportMenu\|showMoreActions\|maybeShowWelcome\|showPrivacyDialog'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

## 6. Release preflight

```bash
ls -l scripts/release-preflight.sh

bash scripts/release-preflight.sh
```

Старого `scripts/rc-preflight.sh` у v1.1.0 вже немає.

## 7. Public docs

```bash
ls -l PRIVACY.md PUBLIC_RELEASE_CHECKLIST.md
find docs/v.1.1.0 -maxdepth 2 -type f | sort
```

## 8. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v1.1.0: public UX foundation"
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

Встановити поверх v1.0.0. Uninstall не робити.

## 11. Перший тест v1.1.0

1. На першому запуску має з'явитися Quick Start.
2. Головний екран НЕ має горизонтального action-scroll.
3. Видно 4 кроки:
   - 1. Імпорт
   - 2. Google / YTM
   - 3. Знайти треки
   - 4. Створити / додати
4. Search disabled до імпорту.
5. `Історія / Черга / Квота / Ще` доступні.
6. `Сервіс → Приватність`.
7. Перевірити стару History.
8. Створити маленький тестовий playlist.

## Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-v1.1.0-build-errors.txt
```
