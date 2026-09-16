# YTM Importer — Termux / Git command guide

Цей файл містить основні команди, які використовуються під час розробки, тестування,
оновлення версій і відправки змін YTM Importer з телефона.

> Основна робоча папка репозиторію:
>
> `/storage/emulated/0/Documents/YTM`
>
> У Termux зазвичай використовується коротка команда:
>
> `ytm`

---

## 1. Перейти в репозиторій

```bash
ytm
```

Перевірити поточну папку:

```bash
pwd
```

Показати файли:

```bash
ls -la
```

---

## 2. Перевірити стан Git

```bash
git status
```

Короткий список змін:

```bash
git status --short
```

Статистика змінених файлів:

```bash
git diff --stat
```

Переглянути самі зміни:

```bash
git diff
```

Перевірити проблеми з пробілами/форматом diff:

```bash
git diff --check
```

---

## 3. Отримати останні зміни з GitHub

Перед початком нової роботи:

```bash
ytm
git status
git pull
```

Окремо завантажити інформацію без автоматичного merge:

```bash
git fetch origin
```

---

## 4. Типове застосування ZIP-пакета версії

Приклад для версії `v1.4.17`:

```bash
ytm

rm -rf "$HOME/ytm-v1417-temp"
mkdir -p "$HOME/ytm-v1417-temp"

unzip -o /sdcard/Download/YTM_Importer_v1.4.17_FULL.zip \
  -d "$HOME/ytm-v1417-temp"

cp -a "$HOME/ytm-v1417-temp/YTM_Importer_v1.4.17_FULL/." .

rm -rf "$HOME/ytm-v1417-temp"

python scripts/apply-v1.4.17.py
```

Назви ZIP, тимчасової папки та apply-script змінюються для кожної версії.

---

## 5. Перевірка версії

```bash
grep -n 'versionCode\|versionName' app/build.gradle.kts
```

---

## 6. Запуск аудитів

Для конкретної версії:

```bash
bash scripts/v1417-auth-flow-audit.sh
```

Глобальний QA-аудит:

```bash
bash scripts/qa-plan-audit.sh
```

Повний release preflight:

```bash
bash scripts/release-preflight.sh
```

Типова група перед commit:

```bash
bash scripts/v1417-auth-flow-audit.sh
bash scripts/qa-plan-audit.sh
bash scripts/release-preflight.sh

git diff --check
git status
git diff --stat
```

---

## 7. Додати зміни в commit

Додати всі зміни:

```bash
git add -A
```

Перевірити, що саме буде закомічено:

```bash
git status
git diff --cached --stat
```

Переглянути повний staged diff:

```bash
git diff --cached
```

---

## 8. Commit

Приклад:

```bash
git commit -m "YTM Importer v1.4.17: fix auth readiness and review destination flow"
```

Останній commit:

```bash
git log -1 --oneline
```

Кілька останніх commit:

```bash
git log --oneline -10
```

---

## 9. Push на GitHub

```bash
git push
```

Після push:

```bash
git status
```

Очікуваний чистий стан:

```text
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean
```

---

## 10. Remote / SSH

Перевірити remote:

```bash
git remote -v
```

Очікуваний SSH remote:

```text
git@github.com:Faric-ua/YTM.git
```

Змінити remote на SSH:

```bash
git remote set-url origin git@github.com:Faric-ua/YTM.git
```

Перевірити SSH GitHub:

```bash
ssh -T git@github.com
```

---

## 11. SSH-ключ

Створити ключ:

```bash
ssh-keygen -t ed25519 -C "your-email@example.com"
```

Показати public key:

```bash
cat ~/.ssh/id_ed25519.pub
```

Сам файл `~/.ssh/id_ed25519` є приватним ключем — його не можна публікувати.

---

## 12. Відновити один файл до версії Git

Увага: незбережені зміни цього файла буде втрачено.

```bash
git restore path/to/file
```

Наприклад:

```bash
git restore README.md
```

---

## 13. Прибрати файл зі staged, але залишити зміни

```bash
git restore --staged path/to/file
```

Або все:

```bash
git restore --staged .
```

---

## 14. Перевірити конкретний файл

```bash
git diff -- app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

Staged-версія:

```bash
git diff --cached -- app/src/main/java/com/saney/ytmimporter/MainActivity.kt
```

---

## 15. Пошук у коді

```bash
grep -R "текст_для_пошуку" -n app scripts docs
```

Приклад:

```bash
grep -R "versionName" -n app scripts
```

---

## 16. Перевірити файл або папку

```bash
ls -la docs/v.1.4.17
```

```bash
find docs/v.1.4.17 -maxdepth 3 -type f | sort
```

---

## 17. Регенерація FILE_MANIFEST.txt

Запускати після того, як усі файли версії вже додані в робочу папку:

```bash
{
  echo "YTM Importer v1.4.17 FULL — FILE MANIFEST"
  echo

  git ls-files --cached --others --exclude-standard |
    sort |
    grep -vx 'FILE_MANIFEST.txt' |
    while IFS= read -r file; do
      [ -f "$file" ] || continue
      hash="$(sha256sum "$file" | cut -c1-16)"
      printf '%s  %s\n' "$hash" "$file"
    done
} > FILE_MANIFEST.txt
```

Після цього:

```bash
wc -l FILE_MANIFEST.txt
head -20 FILE_MANIFEST.txt
git diff --stat FILE_MANIFEST.txt
```

---

## 18. Типовий повний цикл роботи

```bash
ytm
git status
git pull

# застосувати пакет / внести зміни

bash scripts/v1417-auth-flow-audit.sh
bash scripts/qa-plan-audit.sh
bash scripts/release-preflight.sh

git diff --check
git status
git diff --stat

git add -A
git diff --cached --stat

git commit -m "опис змін"
git push

git status
```

---

## 19. Важливі застереження

Не виконувати без чіткого розуміння:

```bash
git reset --hard
git clean -fd
rm -rf
```

Ці команди можуть безповоротно видалити локальні зміни або файли.

Перед такими операціями завжди спочатку:

```bash
git status
```

і, якщо потрібно, зробити копію важливих файлів.
