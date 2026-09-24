#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh

BRANCH="$(ytm_branch)"
REMOTE_HEAD="$(ytm_remote_head "$BRANCH")"

VALIDATION_WORKFLOW="validate.yml"
VALIDATION_RUN_ID=""

echo "Validation gate"
echo "Branch: $BRANCH"
echo "Source: $REMOTE_HEAD"
echo

for _ in $(seq 1 90); do
  VALIDATION_RUN_ID="$(
    gh run list       --repo "$YTM_GH_REPO"       --workflow "$VALIDATION_WORKFLOW"       --branch "$BRANCH"       --limit 30       --json databaseId,headSha,createdAt       --jq ".[] | select(.headSha == \"$REMOTE_HEAD\") | .databaseId"       2>/dev/null |
      head -n 1 ||
    true
  )"

  [ -n "$VALIDATION_RUN_ID" ] && break
  sleep 2
done

[ -n "$VALIDATION_RUN_ID" ] ||
  ytm_fail "No exact-HEAD validation run found yet. Wait for Validate Android and try item 6 again."

echo "Validation run: $VALIDATION_RUN_ID"

if ! gh run watch "$VALIDATION_RUN_ID"   --repo "$YTM_GH_REPO"   --exit-status
then
  ytm_fail "Exact-HEAD validation failed (run $VALIDATION_RUN_ID). Signed build was NOT dispatched."
fi

VALIDATION_HEAD="$(
  gh run view "$VALIDATION_RUN_ID"     --repo "$YTM_GH_REPO"     --json headSha     --jq '.headSha'
)"

VALIDATION_CONCLUSION="$(
  gh run view "$VALIDATION_RUN_ID"     --repo "$YTM_GH_REPO"     --json conclusion     --jq '.conclusion'
)"

[ "$VALIDATION_HEAD" = "$REMOTE_HEAD" ] ||
  ytm_fail "Validation source mismatch"

[ "$VALIDATION_CONCLUSION" = "success" ] ||
  ytm_fail "Validation did not pass"

echo
echo "VALIDATION PASS"
echo


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
