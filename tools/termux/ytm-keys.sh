#!/data/data/com.termux/files/usr/bin/bash
# 🔑 Ключі підпису (faric 2026-11-06 01:26–01:50).
#
# Навіщо: щоб брат faric міг випускати оновлення застосунків, які стають
# поверх старих, потрібен той самий ключ підпису. Ключ YTM є тільки в
# секретах GitHub, а секрет не може прочитати ніхто. Цей розділ:
#   1 — шукає файли ключів на телефоні (тільки дивиться);
#   2 — забирає ключ YTM з GitHub у «замкненій скриньці» (age): замок
#       робиться тут, на телефоні, і його закрита половина нікуди не йде;
#   3 — показує, які ключі є, і за бажанням паролі (щоб переписати на папір);
#   4 — пакує всі ключі в архів 7z під паролем у Download.
#
# Папка «Ключі» лежить у Termux (~/Ключі): інші програми її не бачать.
# Вона НЕ в репозиторії — ключ ніколи не має потрапити на GitHub.
# Нічого не видаляється: якщо ключ уже є й відрізняється, новий кладеться
# поруч в окрему папку.
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

KEYS_DIR="${FARIC_KEYS_DIR:-$HOME/Ключі}"
LOCK_DIR="$KEYS_DIR/.замок"
LOCK="$LOCK_DIR/телефон.key"
WORK="$KEYS_DIR/.робоча"
DOWNLOAD_DIR="${FARIC_DOWNLOAD_DIR:-/storage/emulated/0/Download}"
BOX_WORKFLOW="key-backup.yml"
BOX_ARTIFACT="ytm-key-box"
APP="YTM"
KEY_FILE="ytm-importer-release.jks"

say_fail() {
  echo
  echo "❌ $*"
  echo "Надішли скрін Claude."
  exit 1
}

need_tool() {
  local cmd="$1" package="$2"
  command -v "$cmd" >/dev/null 2>&1 && return 0
  echo "Ставлю програму «$package» (один раз)…"
  pkg install -y "$package" >/dev/null 2>&1 || say_fail "Не вдалося поставити «$package»."
  command -v "$cmd" >/dev/null 2>&1 || say_fail "Програма «$package» не з'явилась."
}

prepare_dirs() {
  mkdir -p "$KEYS_DIR" "$LOCK_DIR"
  chmod 700 "$KEYS_DIR" "$LOCK_DIR"
}

clean_work() {
  # Only our own temporary folder inside «Ключі»; never the keys themselves.
  [ -n "$WORK" ] && [ -d "$WORK" ] && rm -rf "$WORK"
  return 0
}

# Folders with keys: every non-hidden folder of «Ключі».
key_folders() {
  [ -d "$KEYS_DIR" ] || return 0
  find "$KEYS_DIR" -mindepth 1 -maxdepth 1 -type d ! -name '.*' -printf '%f\n' | sort
}

# ── 1 — Пошук ключів на телефоні ────────────────────────────────────────────
do_search() {
  echo "🔎 ПОШУК КЛЮЧІВ НА ТЕЛЕФОНІ"
  echo "Тільки дивлюся — нічого не змінюю і не переношу."
  echo
  local found=0 root path
  for root in "$HOME" /storage/emulated/0; do
    [ -r "$root" ] || continue
    while IFS= read -r path; do
      [ -n "$path" ] || continue
      found=$((found + 1))
      echo "$found) $path"
      echo "   $(date -r "$path" '+%d.%m.%Y %H:%M') • $(du -h "$path" | cut -f1)"
    done < <(
      find "$root" -xdev \
        \( -path "$KEYS_DIR" -o -name .git -o -name node_modules -o -path /storage/emulated/0/Android \) -prune -o \
        -type f \( -iname '*.jks' -o -iname '*.keystore' -o -iname '*.p12' -o -name 'release-signing.properties' \) \
        -print 2>/dev/null | sort
    )
  done
  echo
  if [ "$found" -eq 0 ]; then
    echo "Не знайдено жодного файла ключа. Це нормально:"
    echo "ключ YTM заберемо з GitHub — пункт 2."
  else
    echo "Знайдено: $found. Надішли скрін Claude — він скаже, чи це той ключ."
    echo "(Папку «Ключі» я не рахую — там уже твої збережені ключі.)"
  fi
}

# ── 2 — Забрати ключ YTM з GitHub ───────────────────────────────────────────
make_lock() {
  need_tool age age
  prepare_dirs
  if [ ! -s "$LOCK" ]; then
    (umask 077 && age-keygen -o "$LOCK" >/dev/null 2>&1) || say_fail "Не вдалося зробити замок."
    echo "Зробив замок на телефоні. Його закрита половина нікуди не відправляється."
  fi
  chmod 600 "$LOCK"
}

dispatch_box() {
  local pub="$1" before run_id="" candidates candidate
  ytm_require_gh
  before="$(gh run list --repo "$YTM_GH_REPO" --workflow "$BOX_WORKFLOW" --limit 30 \
    --json databaseId --jq '.[].databaseId' 2>/dev/null || true)"
  gh workflow run "$BOX_WORKFLOW" --repo "$YTM_GH_REPO" --ref main -f recipient="$pub" \
    >/dev/null 2>&1 || say_fail "GitHub не прийняв запит на скриньку."
  echo "GitHub пакує скриньку… (зазвичай 1–2 хвилини, не закривай Termux)"
  for _ in $(seq 1 60); do
    candidates="$(gh run list --repo "$YTM_GH_REPO" --workflow "$BOX_WORKFLOW" --limit 10 \
      --event workflow_dispatch --json databaseId --jq '.[].databaseId' 2>/dev/null || true)"
    while IFS= read -r candidate; do
      [ -n "$candidate" ] || continue
      if ! printf '%s\n' "$before" | grep -Fxq "$candidate"; then run_id="$candidate"; break; fi
    done <<< "$candidates"
    [ -n "$run_id" ] && break
    sleep 2
  done
  [ -n "$run_id" ] || say_fail "Не бачу запуску скриньки на GitHub."
  if ! gh run watch "$run_id" --repo "$YTM_GH_REPO" --exit-status >/dev/null 2>&1; then
    gh run view "$run_id" --repo "$YTM_GH_REPO" --log-failed 2>/dev/null | tail -n 12 || true
    say_fail "GitHub не зміг зробити скриньку (запуск $run_id)."
  fi
  mkdir -p "$WORK/dl"
  gh run download "$run_id" --repo "$YTM_GH_REPO" -n "$BOX_ARTIFACT" -D "$WORK/dl" \
    >/dev/null 2>&1 || say_fail "Не вдалося завантажити скриньку."
  printf '%s\n' "$run_id" > "$WORK/run_id"
}

delete_remote_box() {
  local run_id ids id ok=1
  [ -s "$WORK/run_id" ] || return 0
  run_id="$(cat "$WORK/run_id")"
  ids="$(gh api "repos/$YTM_GH_REPO/actions/runs/$run_id/artifacts" --jq '.artifacts[].id' 2>/dev/null || true)"
  for id in $ids; do
    gh api -X DELETE "repos/$YTM_GH_REPO/actions/artifacts/$id" >/dev/null 2>&1 || ok=0
  done
  if [ "$ok" -eq 1 ]; then
    echo "Скриньку на GitHub видалено (відкрита вона вже лежить у тебе)."
  else
    echo "Скриньку на GitHub не вдалося видалити — вона сама зникне за добу."
    echo "Відкрити її однаково може тільки твій телефон."
  fi
}

do_fetch() {
  echo "🔑 ЗАБРАТИ КЛЮЧ $APP З GITHUB"
  echo
  make_lock
  clean_work
  mkdir -p "$WORK"
  trap clean_work EXIT
  local pub box opened dest
  pub="$(age-keygen -y "$LOCK")"

  if [ -n "${YTM_KEYS_TEST_BOX:-}" ]; then
    # Test hook (scripts/test-ytm-keys.sh): a box made locally, no GitHub.
    mkdir -p "$WORK/dl" && cp "$YTM_KEYS_TEST_BOX" "$WORK/dl/$BOX_ARTIFACT.age"
  else
    dispatch_box "$pub"
  fi

  box="$WORK/dl/$BOX_ARTIFACT.age"
  [ -s "$box" ] || say_fail "Скринька порожня."
  mkdir -p "$WORK/open"
  age -d -i "$LOCK" "$box" 2>/dev/null | tar -xz -C "$WORK/open" 2>/dev/null ||
    say_fail "Скринька не відкрилась цим замком."
  opened="$WORK/open/$APP"
  [ -s "$opened/$KEY_FILE" ] || say_fail "У скриньці нема файла ключа."
  (cd "$opened" && sha256sum -c --quiet контроль.sha256 >/dev/null 2>&1) ||
    say_fail "Файли в скриньці пошкоджені (контрольна сума не збігається)."
  echo "Скриньку відкрито, файли цілі ✓"

  dest="$KEYS_DIR/$APP"
  if [ -s "$dest/$KEY_FILE" ]; then
    if cmp -s "$dest/$KEY_FILE" "$opened/$KEY_FILE" && cmp -s "$dest/паролі.txt" "$opened/паролі.txt"; then
      echo "Цей самий ключ уже лежить у «Ключі → $APP». Нічого не змінив."
    else
      dest="$KEYS_DIR/$APP (новий $(date '+%Y-%m-%d_%H%M'))"
      mv "$opened" "$dest"
      echo "⚠ У «Ключі → $APP» уже лежить ІНШИЙ ключ. Його я не чіпав."
      echo "Новий поклав окремо: «$(basename "$dest")». Надішли скрін Claude."
    fi
  else
    mkdir -p "$dest"
    cp -p "$opened"/* "$dest"/
  fi
  chmod -R go-rwx "$KEYS_DIR"

  [ -n "${YTM_KEYS_TEST_BOX:-}" ] || delete_remote_box

  echo
  echo "✅ КЛЮЧ $APP ЗБЕРЕЖЕНО"
  echo "Де: Ключі → $(basename "$dest")"
  sed -n 's/^SHA-256 сертифіката: \(.\{16\}\).*/Відбиток: \1…/p; s/^Перевірка з релізом: /Перевірка з релізом: /p' "$dest/відбиток.txt"
  echo
  echo "Далі: 3 — Показати ключі"
}

# ── 3 — Показати ключі ──────────────────────────────────────────────────────
do_show() {
  echo "📁 КЛЮЧІ НА ТЕЛЕФОНІ"
  echo
  local folders name i=0 when choice
  folders="$(key_folders)"
  if [ -z "$folders" ]; then
    echo "Поки порожньо."
    echo "Спершу: 2 — Забрати ключ YTM з GitHub"
    return 0
  fi
  while IFS= read -r name; do
    i=$((i + 1))
    if [ -n "$(find "$KEYS_DIR/$name" -maxdepth 1 -type f \( -name '*.jks' -o -name '*.keystore' \) -print -quit)" ]; then
      when="$(sed -n 's/^Забрано з GitHub: //p' "$KEYS_DIR/$name/відбиток.txt" 2>/dev/null)"
      echo "$i) $name   ✓ ключ є${when:+   $when}"
    else
      echo "$i) $name   ✗ нема файла ключа"
    fi
  done <<< "$folders"
  echo
  printf "Введи номер, щоб показати паролі, або Enter — назад: "
  read -r choice || choice=""
  [ -n "$choice" ] || return 0
  name="$(printf '%s\n' "$folders" | sed -n "${choice}p" 2>/dev/null || true)"
  [ -n "$name" ] || { echo "Нема такого номера."; return 0; }
  clear 2>/dev/null || true
  echo "🔑 $name"
  echo
  cat "$KEYS_DIR/$name/паролі.txt" 2>/dev/null || echo "(нема файла паролі.txt)"
  echo
  cat "$KEYS_DIR/$name/відбиток.txt" 2>/dev/null || true
  echo
  echo "Перепиши паролі на папір. Нікому не надсилай фото екрана."
  printf "Натисни Enter — сховаю з екрана…"
  read -r _ || true
  clear 2>/dev/null || true
  echo "Сховано."
}

# ── 4 — Зібрати архів ключів у Download ─────────────────────────────────────
write_brother_note() {
  cat > "$1" <<'NOTE'
ЯК КОРИСТУВАТИСЯ ЦИМ АРХІВОМ
============================

Тут ключі підпису застосунків faric (GitHub: faric-ua). Для кожного
застосунку — своя папка:
  • файл ключа (.jks) — без нього нове оновлення НЕ стане поверх старого
    застосунку на телефоні;
  • паролі.txt — пароль файлу, назва ключа, пароль ключа і які секрети
    GitHub потрібні для збірки;
  • відбиток.txt — «відбиток пальця» ключа, щоб перевірити, що він той самий;
  • контроль.sha256 — контрольні суми файлів.

Як випускати оновлення зі свого GitHub:
  1. Перенести репозиторій до себе (або зробити копію).
  2. У репозиторії: Settings → Secrets and variables → Actions →
     New repository secret. Додати секрети з паролі.txt.
     Значення для *_KEYSTORE_B64 — файл ключа в base64:
        base64 -w0 ytm-importer-release.jks
  3. Запускати збірку, як раніше (Actions → Signed APK → Run workflow).

Цей архів і пароль від нього треба зберігати ОКРЕМО одне від одного.
Нікому не передавати файли ключів і паролі — хто їх має, той може
випустити «оновлення» від імені цих застосунків.
NOTE
}

do_archive() {
  echo "📦 ЗІБРАТИ АРХІВ КЛЮЧІВ"
  echo
  local folders stage out pw1 pw2 name
  folders="$(key_folders)"
  [ -n "$folders" ] || say_fail "Ключів ще нема. Спершу: 2 — Забрати ключ YTM з GitHub."
  [ -d "$DOWNLOAD_DIR" ] && [ -w "$DOWNLOAD_DIR" ] ||
    say_fail "Нема доступу до папки Download."
  need_tool 7z p7zip

  clean_work
  stage="$WORK/архів"
  mkdir -p "$stage"
  trap clean_work EXIT
  while IFS= read -r name; do
    cp -rp "$KEYS_DIR/$name" "$stage/"
  done <<< "$folders"
  write_brother_note "$stage/ЯК_КОРИСТУВАТИСЯ.txt"

  echo "У архів піде: $(printf '%s' "$folders" | tr '\n' ',' | sed 's/,/, /g')"
  echo
  echo "Придумай пароль на архів (щонайменше 8 знаків)."
  echo "Літер на екрані не видно — так і треба."
  printf "Пароль: "
  read -rs pw1 || pw1=""
  echo
  printf "Ще раз той самий пароль: "
  read -rs pw2 || pw2=""
  echo
  [ "$pw1" = "$pw2" ] || say_fail "Паролі різні. Архів не створено — спробуй ще раз."
  [ "${#pw1}" -ge 8 ] || say_fail "Пароль коротший за 8 знаків. Архів не створено."

  out="$DOWNLOAD_DIR/Ключі_$(date '+%Y-%m-%d_%H%M').7z"
  (cd "$stage" && 7z a -t7z -mhe=on -mx=9 -p"$pw1" "$out" . >/dev/null 2>&1) ||
    say_fail "Не вдалося створити архів."
  7z t -p"$pw1" "$out" >/dev/null 2>&1 || say_fail "Архів створено, але перевірка не пройшла."
  pw1="" pw2=""

  echo
  echo "✅ АРХІВ ГОТОВИЙ"
  echo "Файл: Download/$(basename "$out") ($(du -h "$out" | cut -f1))"
  echo "Перевірено: архів відкривається цим паролем ✓"
  echo
  echo "Далі:"
  echo "• пароль запиши на папір і зберігай ОКРЕМО від архіву;"
  echo "• архів скопіюй на флешку або в Google Drive."
}

# ── Меню ────────────────────────────────────────────────────────────────────
pause_keys() {
  echo
  printf "Натисни Enter, щоб повернутися..."
  read -r _ || true
}

run_step() {
  clear 2>/dev/null || true
  ( "$@" ) || true
  pause_keys
}

case "${1:-menu}" in
  search) do_search ;;
  fetch) do_fetch ;;
  show) do_show ;;
  archive) do_archive ;;
  menu)
    while true; do
      clear 2>/dev/null || true
      echo "========================================"
      echo "   🔑 Ключі підпису"
      echo "========================================"
      echo
      echo "1 — Пошук ключів на телефоні (тільки дивиться)"
      echo "2 — Забрати ключ YTM з GitHub"
      echo "3 — Показати ключі"
      echo "4 — Зібрати архів ключів у Download"
      echo "0 — Назад"
      echo
      printf "Вибір: "
      read -r choice || exit 0
      case "$choice" in
        1) run_step do_search ;;
        2) run_step do_fetch ;;
        3) run_step do_show ;;
        4) run_step do_archive ;;
        0) clear 2>/dev/null || true; exit 0 ;;
        *) echo; echo "Невідомий пункт: $choice"; sleep 1 ;;
      esac
    done
    ;;
  *) echo "Невідома дія: $1" >&2; exit 2 ;;
esac
