# Termux — оновити тільки roadmap після v0.8.0

Архів:
`/sdcard/Download/YTM_Roadmap_Update_after_v0.8.0.zip`

Перейдіть у корінь репозиторію:

```bash
cd ~/storage/shared/Documents/YTM
git status
```

Розпакуйте:

```bash
rm -rf "$HOME/ytm-roadmap-update"
mkdir -p "$HOME/ytm-roadmap-update"

unzip -o /sdcard/Download/YTM_Roadmap_Update_after_v0.8.0.zip   -d "$HOME/ytm-roadmap-update"

cp -a "$HOME/ytm-roadmap-update/." .

rm -rf "$HOME/ytm-roadmap-update"
```

Перевірте:

```bash
git status
git diff -- BACKLOG.md
find docs/v.0.8.0 -maxdepth 2 -type f | sort
```

Має бути:

- `BACKLOG.md` — modified;
- `docs/v.0.8.0/FUTURE_FEATURES.md` — new;
- `docs/v.0.8.0/diagrams/FUTURE_ROADMAP.md` — new.

Це лише документація/roadmap, APK перебудовувати не обов'язково.

Commit:

```bash
git add BACKLOG.md
git add docs/v.0.8.0/FUTURE_FEATURES.md
git add docs/v.0.8.0/diagrams/FUTURE_ROADMAP.md

git commit -m "Expand roadmap: accounts playlists quota and pending jobs"
git push
```
