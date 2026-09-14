# Termux — встановлення YTM Importer v0.9.0 FULL

Архів повинен бути в:

`/sdcard/Download/YTM_Importer_v0.9.0_FULL.zip`

## 1. Перейдіть у корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

Має бути:

`On branch main`

## 2. Розпакуйте у тимчасову папку

```bash
rm -rf "$HOME/ytm-v090-temp"
mkdir -p "$HOME/ytm-v090-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.9.0_FULL.zip   -d "$HOME/ytm-v090-temp"
```

## 3. Скопіюйте повну версію поверх поточного проєкту

```bash
cp -a "$HOME/ytm-v090-temp/YTM_Importer_v0.9.0_FULL/." .
rm -rf "$HOME/ytm-v090-temp"
```

## 4. Перевірте зміни

```bash
git status
git diff --stat
```

Особливо перевірте:

```bash
grep -n 'versionName\|versionCode' app/build.gradle.kts
grep -n 'setup-android\|Locate Android SDK' .github/workflows/build-apk.yml
find docs/v.0.9.0 -maxdepth 2 -type f | sort
```

Очікується:

- `versionCode = 9`
- `versionName = "0.9.0"`
- НЕ повинно бути активного `uses: android-actions/setup-android@v3`
- має бути `Locate Android SDK`
- має існувати `docs/v.0.9.0/diagrams/`

## 5. Commit + push

```bash
git add -A
git commit -m "YTM Importer v0.9.0: accounts and existing playlists"
git push
```

## 6. GitHub Actions

GitHub → Actions → Build Signed Android APK → Run workflow

Очікуваний artifact (артефакт):

`YTM-Importer-v0.9.0-Release`

## 7. Після встановлення протестувати

1. Натиснути `2. Акаунт`.
2. Перевірити ім'я/email Google.
3. Перевірити YouTube/YTM channel + ID.
4. Натиснути `Змінити акаунт`.
5. Імпортувати 2-3 тестові треки.
6. Натиснути `Знайти`.
7. Натиснути `Створити`.
8. Перевірити:
   - `Створити новий плейлист`;
   - `Додати до існуючого плейлиста`.
9. В існуючих плейлистах перевірити пошук за назвою.
10. Додати 1-2 треки в тестовий існуючий плейлист.

У v0.9.0 duplicate detection (перевірки дублікатів) ще немає.
