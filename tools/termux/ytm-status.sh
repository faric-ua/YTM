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
  ytm_fail "Remote moved while checking status; run Status again"

if [ "$LOCAL" = "$REMOTE" ]; then
  RELATION="SYNCED"
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$LOCAL" "$REMOTE"; then
  RELATION="BEHIND"
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$REMOTE" "$LOCAL"; then
  RELATION="AHEAD"
else
  RELATION="DIVERGED"
fi

echo "YTM repository status"
echo "====================="
echo "Branch:      $BRANCH"
echo "Local HEAD:  $LOCAL"
echo "Remote HEAD: $REMOTE"
echo "Relation:    $RELATION"

if [ -z "$DIRTY" ]; then
  echo "Working tree: CLEAN"
else
  echo "Working tree: DIRTY"
  echo
  printf '%s\n' "$DIRTY"
fi
