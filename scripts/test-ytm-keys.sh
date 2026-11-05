#!/usr/bin/env bash
# Local test of the «🔑 Ключі підпису» chain without GitHub and without the phone:
#   1) runs the REAL «Put the key into the locked box» step of key-backup.yml
#      on a throw-away keystore (gh is faked: no release → «не перевірено»);
#   2) opens that box with tools/termux/ytm-keys.sh fetch (test hook);
#   3) checks: the key lands in ~/Ключі/YTM, a second fetch changes nothing,
#      a DIFFERENT key never overwrites the old one, show prints the passwords,
#      search finds key files, a wrong key password stops the workflow.
# Needs: age, age-keygen, keytool, python3 (PyYAML). 7z (archive) is checked on the phone.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
for tool in age age-keygen keytool python3; do
  command -v "$tool" >/dev/null || { echo "SKIP: no $tool"; exit 0; }
done

T="$(mktemp -d)"
trap 'rm -rf "$T"' EXIT
export HOME="$T/home"
mkdir -p "$HOME" "$T/bin"
printf '#!/bin/sh\nexit 1\n' > "$T/bin/gh" && chmod +x "$T/bin/gh"
PATH="$T/bin:$PATH"

fail() { echo "FAIL: $*" >&2; exit 1; }

python3 - "$ROOT/.github/workflows/key-backup.yml" "$T/step.sh" <<'PY'
import sys, yaml
wf = yaml.safe_load(open(sys.argv[1]))
step = next(s for s in wf["jobs"]["box"]["steps"] if s["name"] == "Put the key into the locked box")
open(sys.argv[2], "w").write(step["run"])
PY

make_box() { # $1 = store password, $2 = key password, $3 = out box, $4 = recipient, [$5 = wrong key pw]
  local d="$T/runner_$RANDOM"
  mkdir -p "$d"
  keytool -genkeypair -storetype JKS -keystore "$d/k.jks" -storepass "$1" -keypass "$2" \
    -alias ytmimporter -keyalg RSA -keysize 2048 -dname CN=test -validity 30 >/dev/null 2>&1
  KEYSTORE_B64="$(base64 -w0 "$d/k.jks")" STORE_PASSWORD="$1" KEY_PASSWORD="${5:-$2}" \
    RECIPIENT="$4" RUNNER_TEMP="$d" GITHUB_STEP_SUMMARY="$d/summary" \
    GITHUB_REPOSITORY=faric-ua/YTM GH_TOKEN=x bash "$T/step.sh" > "$d/log" 2>&1 || return 1
  ! grep -Fq "$1" "$d/log" || fail "store password leaked into the log"
  ! grep -Fq "$2" "$d/log" || fail "key password leaked into the log"
  [ ! -e "$d/box" ] || fail "open box left on the runner"
  cp "$d/ytm-key-box.age" "$3"
}

KEYS="$ROOT/tools/termux/ytm-keys.sh"
mkdir -p "$HOME/Ключі/.замок"
age-keygen -o "$HOME/Ключі/.замок/телефон.key" >/dev/null 2>&1
PUB="$(age-keygen -y "$HOME/Ключі/.замок/телефон.key")"

# 1. workflow step makes a box only this lock opens
make_box 'st0re$pw"1' 'k3y pw!' "$T/box1.age" "$PUB"
OTHER="$(age-keygen 2>/dev/null | grep -o 'age1.*')"
age -d -i "$HOME/Ключі/.замок/телефон.key" "$T/box1.age" >/dev/null || fail "phone lock cannot open its box"

# wrong key password → workflow stops, no box
if make_box 'a1b2c3d4e5' 'right-key-pw' "$T/bad.age" "$PUB" 'wrong-key-pw'; then
  fail "workflow accepted a wrong key password"
fi

# 2. fetch puts the key into ~/Ключі/YTM
OUT="$(YTM_KEYS_TEST_BOX="$T/box1.age" bash "$KEYS" fetch)"
grep -Fq "✅ КЛЮЧ YTM ЗБЕРЕЖЕНО" <<<"$OUT" || { echo "$OUT"; fail "fetch did not finish"; }
grep -Fq "Перевірка з релізом: не перевірено" <<<"$OUT" || fail "release check line missing"
[ -s "$HOME/Ключі/YTM/ytm-importer-release.jks" ] || fail "key file not placed"
grep -Fq 'Пароль файлу (storePassword): st0re$pw"1' "$HOME/Ключі/YTM/паролі.txt" || fail "store password not saved"
grep -Fq 'Пароль ключа (keyPassword): k3y pw!' "$HOME/Ключі/YTM/паролі.txt" || fail "key password not saved"
[ ! -e "$HOME/Ключі/.робоча" ] || fail "work folder not cleaned"
[ "$(stat -c %a "$HOME/Ключі")" = 700 ] || fail "«Ключі» is readable by others"

# 3. same box again → nothing changes
OUT="$(YTM_KEYS_TEST_BOX="$T/box1.age" bash "$KEYS" fetch)"
grep -Fq "Цей самий ключ уже лежить" <<<"$OUT" || fail "second fetch did not notice the same key"

# 4. different key → old one stays, new goes to its own folder
make_box 'second-store-pw' 'second-key-pw' "$T/box2.age" "$PUB"
BEFORE="$(sha256sum "$HOME/Ключі/YTM/ytm-importer-release.jks")"
OUT="$(YTM_KEYS_TEST_BOX="$T/box2.age" bash "$KEYS" fetch)"
grep -Fq "уже лежить ІНШИЙ ключ" <<<"$OUT" || fail "different key not reported"
[ "$(sha256sum "$HOME/Ключі/YTM/ytm-importer-release.jks")" = "$BEFORE" ] || fail "old key was overwritten"
ls -d "$HOME/Ключі/YTM (новий "* >/dev/null || fail "new key folder missing"

# 5. a box for another lock does not open
make_box 'x1x2x3x4x5' 'y1y2y3y4y5' "$T/box3.age" "$OTHER"
if YTM_KEYS_TEST_BOX="$T/box3.age" bash "$KEYS" fetch >/dev/null 2>&1; then
  fail "a box for another lock was opened"
fi

# 6. show lists folders and prints passwords of the chosen one
OUT="$(printf '1\n\n' | bash "$KEYS" show)"
grep -Fq "1) YTM   ✓ ключ є" <<<"$OUT" || { echo "$OUT"; fail "show does not list YTM"; }
grep -Fq 'st0re$pw"1' <<<"$OUT" || fail "show did not print passwords"

# 7. search finds key files outside «Ключі» and skips «Ключі» itself
mkdir -p "$HOME/old"; cp "$HOME/Ключі/YTM/ytm-importer-release.jks" "$HOME/old/found.jks"
OUT="$(bash "$KEYS" search)"
grep -Fq "$HOME/old/found.jks" <<<"$OUT" || fail "search missed a key file"
! grep -Fq "$HOME/Ключі/" <<<"$OUT" || fail "search listed the «Ключі» folder"

echo "ytm-keys test: PASS (7 checks)"
