# Termux — YTM Importer v0.9.1 FULL

Архів:

`/sdcard/Download/YTM_Importer_v0.9.1_FULL.zip`

## 1. Перейдіть у корінь репозиторію

```bash
cd ~/storage/shared/Documents/YTM
pwd
git status
```

## 2. Розпакуйте

```bash
rm -rf "$HOME/ytm-v091-temp"
mkdir -p "$HOME/ytm-v091-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.9.1_FULL.zip   -d "$HOME/ytm-v091-temp"
```

## 3. Скопіюйте поверх проєкту

```bash
cp -a "$HOME/ytm-v091-temp/YTM_Importer_v0.9.1_FULL/." .
rm -rf "$HOME/ytm-v091-temp"
```

## 4. Перевірте саме hotfix

```bash
grep -n -A4 'private fun authorize'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Очікується:

```text
forceAccountPicker: Boolean = false,
after: (() -> Unit)? = null
```

Перевірте всі виклики:

```bash
grep -n 'authorize'   app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Перевірте версію:

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

Очікується:

```text
versionCode = 10
versionName = "0.9.1"
```

Перевірте документацію:

```bash
find docs/v.0.9.1 -maxdepth 2 -type f | sort
```

## 5. Commit + push

```bash
git status
git diff --stat

git add -A
git commit -m "YTM Importer v0.9.1: fix authorize compilation"
git push
```

## 6. Запустіть збірку

```bash
gh workflow run "Build Signed Android APK"
gh run watch
```

Якщо збірка впаде:

```bash
gh run view --log-failed
```

Очікуваний artifact (артефакт):

`YTM-Importer-v0.9.1-Release`
