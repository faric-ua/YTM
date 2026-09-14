# Termux: як встановити v0.8.0 FULL у репозиторій

Архів очікується тут:

`/sdcard/Download/YTM_Importer_v0.8.0_FULL.zip`

Спочатку перейдіть у корінь вашого репозиторію YTM.
У вашому випадку це папка, де `git status` показує `On branch main`.

Перевірка:

```bash
pwd
git status
```

Далі:

```bash
rm -rf "$HOME/ytm-v080-temp"
mkdir -p "$HOME/ytm-v080-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.8.0_FULL.zip   -d "$HOME/ytm-v080-temp"

cp -a "$HOME/ytm-v080-temp/YTM_Importer_v0.8.0_FULL/." .

rm -rf "$HOME/ytm-v080-temp"

# Старий bootstrap більше не потрібний.
rm -f .github/workflows/bootstrap.yml

git status
git diff --stat
```

Після перевірки:

```bash
git add .
git commit -m "YTM Importer v0.8.0: direct text import"
git push
```

Потім GitHub:
Actions → Build Signed Android APK → Run workflow.

Після успішної збірки встановіть APK поверх v0.7.1.
