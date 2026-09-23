#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh

BRANCH="$(ytm_branch)"
REMOTE_HEAD="$(ytm_remote_head "$BRANCH")"

BEFORE_IDS="$(
  gh run list     --repo "$YTM_GH_REPO"     --workflow "$YTM_WORKFLOW"     --branch "$BRANCH"     --event workflow_dispatch     --limit 50     --json databaseId     --jq '.[].databaseId' 2>/dev/null ||
  true
)"

echo "Manual signed build"
echo "Branch: $BRANCH"
echo "Source: $REMOTE_HEAD"
echo

gh workflow run "$YTM_WORKFLOW"   --repo "$YTM_GH_REPO"   --ref "$BRANCH"

RUN_ID=""

for _ in $(seq 1 60); do
  CANDIDATES="$(
    gh run list       --repo "$YTM_GH_REPO"       --workflow "$YTM_WORKFLOW"       --branch "$BRANCH"       --event workflow_dispatch       --limit 20       --json databaseId,headSha       --jq ".[] | select(.headSha == \"$REMOTE_HEAD\") | .databaseId" 2>/dev/null ||
    true
  )"

  while IFS= read -r candidate; do
    [ -n "$candidate" ] || continue
    if ! printf '%s\n' "$BEFORE_IDS" | grep -Fxq "$candidate"; then
      RUN_ID="$candidate"
      break
    fi
  done <<< "$CANDIDATES"

  [ -n "$RUN_ID" ] && break
  sleep 2
done

[ -n "$RUN_ID" ] ||
  ytm_fail "Could not resolve the newly dispatched exact-source run"

gh run watch "$RUN_ID"   --repo "$YTM_GH_REPO"   --exit-status

RUN_HEAD="$(
  gh run view "$RUN_ID"     --repo "$YTM_GH_REPO"     --json headSha     --jq '.headSha'
)"

[ "$RUN_HEAD" = "$REMOTE_HEAD" ] ||
  ytm_fail "Completed run source mismatch"

echo
echo "BUILD PASS"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$REMOTE_HEAD"
echo
echo "Use menu item 3 to download this exact signed APK."
