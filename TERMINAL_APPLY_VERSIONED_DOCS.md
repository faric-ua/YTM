# Termux — перенесення v0.8.0 FULL + історичних діаграм

Архів:

`/sdcard/Download/YTM_Importer_v0.8.0_FULL_VERSIONED_DOCS.zip`

## 1. Перейдіть у корінь Git-репозиторію

Там `git status` має показувати `On branch main`.

```bash
pwd
git status
```

## 2. Розпакуйте нову повну версію

```bash
rm -rf "$HOME/ytm-versioned-docs-temp"
mkdir -p "$HOME/ytm-versioned-docs-temp"

unzip -o /sdcard/Download/YTM_Importer_v0.8.0_FULL_VERSIONED_DOCS.zip   -d "$HOME/ytm-versioned-docs-temp"
```

## 3. Видаліть СТАРУ плоску папку діаграм

```bash
rm -rf docs/diagrams
```

Це важливо. Після міграції діаграми мають бути тільки у versioned folders.

## 4. Скопіюйте повний проєкт поверх поточного

```bash
cp -a "$HOME/ytm-versioned-docs-temp/YTM_Importer_v0.8.0_FULL_VERSIONED_DOCS/." .
rm -rf "$HOME/ytm-versioned-docs-temp"
```

## 5. Перевірте структуру

```bash
find docs -maxdepth 3 -type f | sort
git status
git diff --stat
```

Очікувані основні шляхи:

```text
docs/v.0.7.1/diagrams/
docs/v.0.8.0/diagrams/
```

Старого `docs/diagrams/` бути не повинно.

Перевірка:

```bash
test ! -d docs/diagrams && echo "OK: old docs/diagrams removed"
test -d docs/v.0.7.1/diagrams && echo "OK: v0.7.1 docs present"
test -d docs/v.0.8.0/diagrams && echo "OK: v0.8.0 docs present"
```

## 6. Commit + push

```bash
git add -A
git commit -m "Organize diagrams by release version"
git push
```

## 7. Після push

На GitHub відкрий:

```text
docs/v.0.7.1/diagrams/
docs/v.0.8.0/diagrams/
```

і перевір, що Mermaid-діаграми рендеряться.

- `TERMINAL_APPLY_v1.4.2.md` — safe insets + dialog polish

- `TERMINAL_APPLY_v1.4.4.md` — adaptive buttons + state colors

- `TERMINAL_APPLY_v1.4.8.md` — safe bounds for tall custom dialogs

- `TERMINAL_APPLY_v1.4.9.md` — rotation auth + stable dialog first frame

- `TERMINAL_APPLY_v1.4.10.md` — rotation layout + stable dialog anchor

- `TERMINAL_APPLY_v1.4.11.md` — disable custom dialog WindowManager animation
