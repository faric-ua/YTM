# Termux — YTM Importer v1.0.0-rc1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v1.0.0-rc1_FULL.zip`

## 1. Корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакувати RC1

```bash
rm -rf "$HOME/ytm-v100rc1-temp"
mkdir -p "$HOME/ytm-v100rc1-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.0.0-rc1_FULL.zip   -d "$HOME/ytm-v100rc1-temp"
```

## 3. Скопіювати поверх проєкту

```bash
cp -a "$HOME/ytm-v100rc1-temp/YTM_Importer_v1.0.0-rc1_FULL/." .
rm -rf "$HOME/ytm-v100rc1-temp"
```

## 4. Перевірити версію

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 21
versionName = "1.0.0-rc1"
```

## 5. Запустити RC preflight прямо в Termux

```bash
bash scripts/rc-preflight.sh
```

Очікується фінальний блок `PASS`.

## 6. Перевірити зміни

```bash
git status
git diff --stat

find docs/v.1.0.0-rc1 -maxdepth 2 -type f | sort
```

## 7. Commit + push

```bash
git add -A
git commit -m "YTM Importer v1.0.0-rc1: release candidate"
git push
```

## 8. GitHub Actions build

```bash
gh workflow run "Build Signed Android APK"

sleep 3

RUN_ID="$(
  gh run list     --workflow "Build Signed Android APK"     --limit 1     --json databaseId     --jq '.[0].databaseId'
)"

echo "$RUN_ID"
gh run watch "$RUN_ID"
```

На початку job має окремо пройти:

```text
RC preflight
```

## 9. Завантажити APK

Якщо build зелений:

```bash
bash scripts/download-latest-apk.sh
```

Встановити поверх v0.15.2:

```bash
termux-open "$(find /sdcard/Download/YTM-APK -type f -name '*.apk' | head -n 1)"
```

**Не видаляти стару версію перед встановленням.**
Нам потрібен саме signed upgrade test.

## 10. Швидка перевірка після update

1. Відкрити `Сервіс → Про програму`.
2. Має бути:
   `1.0.0-rc1 (21)`.
3. Перевірити, що стара `Історія` залишилась.
4. Перевірити `Акаунт`.
5. Відкрити існуючий тестовий playlist.
6. Повний checklist:
   `docs/v.1.0.0-rc1/REGRESSION_CHECKLIST.md`.

## 11. Якщо build впав

```bash
gh run view "$RUN_ID" --log-failed   > /sdcard/Download/YTM-RC1-build-errors.txt
```

Надіслати `YTM-RC1-build-errors.txt` у чат.
