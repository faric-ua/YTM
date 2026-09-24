#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_clean

CURRENT_BRANCH="$(ytm_branch)"
CURRENT_HEAD="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
CURRENT_REMOTE="$(ytm_remote_head "$CURRENT_BRANCH")"

# "Sync YTM" follows the newest release feature branch so the phone does not
# stay on the just-closed release when ChatGPT starts the next release branch.
TARGET_BRANCH="$(
  git -C "$YTM_REPO_DIR" ls-remote --heads origin 'refs/heads/feat/v*' |
    awk '{sub("refs/heads/", "", $2); print $2}' |
    sort -V |
    tail -n 1
)"

[ -n "$TARGET_BRANCH" ] ||
  TARGET_BRANCH="$CURRENT_BRANCH"

if [ "$CURRENT_HEAD" != "$CURRENT_REMOTE" ]; then
  if ! git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$CURRENT_HEAD" "$CURRENT_REMOTE"; then
    ytm_fail "Current branch is ahead or diverged; automatic release switch refused"
  fi
fi

if [ "$TARGET_BRANCH" != "$CURRENT_BRANCH" ]; then
  echo "Active release branch changed:"
  echo "  from: $CURRENT_BRANCH"
  echo "  to:   $TARGET_BRANCH"
  echo

  git -C "$YTM_REPO_DIR" fetch --quiet origin "$TARGET_BRANCH"
  TARGET_REMOTE="$(git -C "$YTM_REPO_DIR" rev-parse FETCH_HEAD)"

  if git -C "$YTM_REPO_DIR" show-ref --verify --quiet "refs/heads/$TARGET_BRANCH"; then
    TARGET_LOCAL="$(
      git -C "$YTM_REPO_DIR" rev-parse "refs/heads/$TARGET_BRANCH"
    )"

    if [ "$TARGET_LOCAL" != "$TARGET_REMOTE" ] &&
       ! git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$TARGET_LOCAL" "$TARGET_REMOTE"; then
      ytm_fail "Local target branch is ahead or diverged; automatic switch refused"
    fi

    git -C "$YTM_REPO_DIR" switch "$TARGET_BRANCH"

    if [ "$TARGET_LOCAL" != "$TARGET_REMOTE" ]; then
      git -C "$YTM_REPO_DIR" merge --ff-only "$TARGET_REMOTE"
    fi
  else
    git -C "$YTM_REPO_DIR" switch       --track       -c "$TARGET_BRANCH"       "origin/$TARGET_BRANCH"
  fi
else
  git -C "$YTM_REPO_DIR" fetch --quiet origin "$CURRENT_BRANCH"
  FETCHED="$(git -C "$YTM_REPO_DIR" rev-parse FETCH_HEAD)"

  [ "$FETCHED" = "$CURRENT_REMOTE" ] ||
    ytm_fail "Remote moved during fetch; run Sync again"

  if [ "$CURRENT_HEAD" = "$CURRENT_REMOTE" ]; then
    echo "Already synchronized."
  elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$CURRENT_HEAD" "$CURRENT_REMOTE"; then
    git -C "$YTM_REPO_DIR" merge --ff-only "$CURRENT_REMOTE"
  else
    ytm_fail "Local branch is ahead or diverged; automatic sync refused"
  fi
fi

BRANCH_AFTER="$(ytm_branch)"
LOCAL_AFTER="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE_AFTER="$(ytm_remote_head "$BRANCH_AFTER")"

[ "$LOCAL_AFTER" = "$REMOTE_AFTER" ] ||
  ytm_fail "Remote moved again; local is safe but no longer current. Run Sync again."

ytm_require_clean

echo
echo "SYNCED"
echo "Branch: $BRANCH_AFTER"
echo "HEAD:   $LOCAL_AFTER"
