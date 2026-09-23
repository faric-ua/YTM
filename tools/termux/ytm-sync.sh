#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_clean

BRANCH="$(ytm_branch)"
LOCAL_BEFORE="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE_BEFORE="$(ytm_remote_head "$BRANCH")"

git -C "$YTM_REPO_DIR" fetch --quiet origin "$BRANCH"
FETCHED="$(git -C "$YTM_REPO_DIR" rev-parse FETCH_HEAD)"

[ "$FETCHED" = "$REMOTE_BEFORE" ] ||
  ytm_fail "Remote moved during fetch; run Sync again"

if [ "$LOCAL_BEFORE" = "$REMOTE_BEFORE" ]; then
  echo "Already synchronized."
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$LOCAL_BEFORE" "$REMOTE_BEFORE"; then
  git -C "$YTM_REPO_DIR" merge --ff-only "$REMOTE_BEFORE"
else
  ytm_fail "Local branch is ahead or diverged; automatic sync refused"
fi

LOCAL_AFTER="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE_AFTER="$(ytm_remote_head "$BRANCH")"

[ "$LOCAL_AFTER" = "$REMOTE_AFTER" ] ||
  ytm_fail "Remote moved again; local is safe but no longer current. Run Sync again."

ytm_require_clean

echo
echo "SYNCED"
echo "Branch: $BRANCH"
echo "HEAD:   $LOCAL_AFTER"
