# Termux

```bash
ytm
git status
git pull

rm -rf "$HOME/ytm-v1417-temp"
mkdir -p "$HOME/ytm-v1417-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.17_FULL.zip   -d "$HOME/ytm-v1417-temp"

cp -a "$HOME/ytm-v1417-temp/YTM_Importer_v1.4.17_FULL/." .

rm -rf "$HOME/ytm-v1417-temp"

python scripts/apply-v1.4.17.py

grep -n 'versionCode\|versionName' app/build.gradle.kts

bash scripts/v1417-auth-flow-audit.sh
bash scripts/qa-plan-audit.sh
bash scripts/release-preflight.sh

git diff --check
git status
git diff --stat
```

Do not commit until reviewed.
