#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_PATH="$0"
case "$SCRIPT_PATH" in
  /*) ;;
  *) SCRIPT_PATH="$PWD/$SCRIPT_PATH" ;;
esac
cd "$HOME" || exit 1
SELF_DIR="$(cd "$(dirname "$SCRIPT_PATH")" && pwd)"
# shellcheck source=ytm-common.sh
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh

BRANCH="$(ytm_branch)"
LOCAL_HEAD="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE_HEAD="$(ytm_remote_head "$BRANCH")"

echo "GitHub Actions — останні runs"
echo "============================"
echo "Repo:        $YTM_GH_REPO"
echo "Branch:      $BRANCH"
echo "Local HEAD:  ${LOCAL_HEAD:0:12}"
echo "Remote HEAD: ${REMOTE_HEAD:0:12}"
echo
printf "    %-10s %-24s %-18s %-16s %-12s %s\n" "STATUS" "WORKFLOW" "EVENT" "SHA" "RUN ID" "TITLE"

RUNS="$(gh run list --repo "$YTM_GH_REPO" --branch "$BRANCH" --limit 10 \
  --json databaseId,displayTitle,workflowName,status,conclusion,event,headSha \
  --jq '.[] | [(.conclusion // "-"), (.status // "-"), (.workflowName // "-"), (.event // "-"), (.headSha // "-"), (.databaseId|tostring), (.displayTitle // "-")] | @tsv')"

if [ -z "$RUNS" ]; then
  echo "Немає GitHub Actions runs для цієї гілки."
  exit 0
fi

while IFS=$'\t' read -r conclusion status workflow event sha run_id title; do
  case "$status:$conclusion" in
    completed:success) symbol="✓" ;;
    completed:*)       symbol="X" ;;
    *)                 symbol="*" ;;
  esac
  printf "%-3s %-10s %-24.24s %-18.18s %-16.16s %-12s %s\n" \
    "$symbol" "${conclusion:--}" "$workflow" "$event" "$sha" "$run_id" "$title"
done <<< "$RUNS"

echo
echo "✓ = успіх, X = failure/cancelled, * = ще виконується."
echo

LATEST="$(printf '%s\n' "$RUNS" | head -n 1)"
IFS=$'\t' read -r latest_conclusion latest_status _ _ latest_sha latest_id _ <<< "$LATEST"
LATEST_URL="$(gh run view "$latest_id" --repo "$YTM_GH_REPO" --json url --jq '.url')"

echo "Останній run:"
echo "ID: $latest_id"
echo "Status: $latest_status"
echo "Conclusion: ${latest_conclusion:--}"
echo "SHA: $latest_sha"
echo "URL: $LATEST_URL"
