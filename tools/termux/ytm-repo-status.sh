#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo

BRANCH="$(ytm_branch)"
LOCAL="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE="$(ytm_remote_head "$BRANCH")"
DIRTY="$(git -C "$YTM_REPO_DIR" status --porcelain=v1 --untracked-files=all)"

git -C "$YTM_REPO_DIR" fetch --quiet origin "$BRANCH"
FETCHED="$(git -C "$YTM_REPO_DIR" rev-parse FETCH_HEAD)"

[ "$FETCHED" = "$REMOTE" ] ||
  ytm_fail "Код на GitHub змінився під час перевірки. Повтори «6 — Перевірити локальні зміни»."

if [ "$LOCAL" = "$REMOTE" ]; then
  RELATION="актуально ✅"
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$LOCAL" "$REMOTE"; then
  RELATION="потрібно оновити ⚠"
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$REMOTE" "$LOCAL"; then
  RELATION="локальна гілка попереду GitHub ❌"
else
  RELATION="локальна гілка розійшлася з GitHub ❌"
fi

echo "Локальний стан YTM"
echo "========================================"
echo "Гілка:          $BRANCH"
echo "Стан коду:      $RELATION"
echo "Код у Termux:   ${LOCAL:0:12}"
echo "Код на GitHub:  ${REMOTE:0:12}"

if [ -z "$DIRTY" ]; then
  echo "Локальні файли: без змін ✅"
else
  echo "Локальні файли: є зміни ⚠"
  echo
  printf '%s\n' "$DIRTY"
fi

echo
if [ "$LOCAL" != "$REMOTE" ]; then
  echo "Для звичайного оновлення використовуй:"
  echo "  1 — Оновити проєкт"
fi
