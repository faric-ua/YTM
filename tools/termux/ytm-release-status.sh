#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh

BRANCH="$(ytm_branch)"
LOCAL="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE="$(ytm_remote_head "$BRANCH")"
VERSION="$(
  sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' \
    "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"

META="$YTM_REPO_DIR/docs/v.$VERSION/RELEASE_META.json"

meta_value() {
  local key="$1"
  if [ ! -f "$META" ]; then
    printf '%s\n' ""
    return
  fi

  python - "$META" "$key" <<'PY'
import json
import sys

path, key = sys.argv[1], sys.argv[2]
try:
    value = json.load(open(path, encoding="utf-8")).get(key)
except Exception:
    value = None

if value is None:
    print("")
elif isinstance(value, bool):
    print("true" if value else "false")
else:
    print(value)
PY
}

run_for_exact_head() {
  local workflow="$1"
  local sha="$2"

  [ -n "$sha" ] || return 0

  gh run list \
    --repo "$YTM_GH_REPO" \
    --workflow "$workflow" \
    --branch "$BRANCH" \
    --limit 50 \
    --json databaseId,headSha,status,conclusion,createdAt \
    --jq ".[] | select(.headSha == \"$sha\") | [.databaseId,.status,(.conclusion // \""\"),.createdAt] | @tsv" \
    2>/dev/null |
    head -n 1 ||
    true
}

format_run() {
  local row="$1"
  local label="$2"

  if [ -z "$row" ]; then
    echo "$label: NOT FOUND"
    return
  fi

  local id status conclusion created
  IFS=$'\t' read -r id status conclusion created <<< "$row"

  case "$conclusion" in
    success)
      echo "$label: PASS (run $id)"
      ;;
    failure|cancelled|timed_out|action_required|startup_failure)
      echo "$label: FAIL ($conclusion, run $id)"
      ;;
    *)
      echo "$label: $status (run $id)"
      ;;
  esac
}

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

APP_SOURCE="$(meta_value appSourceSha)"
SIGNED_RUN_META="$(meta_value signedRun)"
QA_STATUS="$(meta_value qaStatus)"
PHASE="$(meta_value phase)"
TAG="$(meta_value tag)"
CHECKPOINT_TAG="$(meta_value checkpointTag)"

[ -n "$PHASE" ] || PHASE="UNKNOWN"

VALIDATION_ROW="$(run_for_exact_head validate.yml "$REMOTE")"
SIGNED_ROW="$(run_for_exact_head "$YTM_WORKFLOW" "$APP_SOURCE")"

if [ -z "$TAG" ]; then
  TAG="v$VERSION"
fi

TAG_SHA="$(
  git -C "$YTM_REPO_DIR" ls-remote --tags origin "refs/tags/$TAG" 2>/dev/null |
    awk 'NR == 1 {print $1}' ||
  true
)"

if gh release view "$TAG" --repo "$YTM_GH_REPO" >/dev/null 2>&1; then
  RELEASE_STATE="PUBLISHED"
else
  RELEASE_STATE="MISSING"
fi

echo "YTM release status"
echo "=================="
echo "Version:       $VERSION"
echo "Branch:        $BRANCH"
echo "Local HEAD:    $LOCAL"
echo "Remote HEAD:   $REMOTE"
echo "Repo:          $RELATION"
echo

format_run "$VALIDATION_ROW" "HEAD validation"

echo
if [ -n "$APP_SOURCE" ]; then
  echo "Phone source:  $APP_SOURCE"
else
  echo "Phone source:  NOT RECORDED"
fi

if [ -n "$SIGNED_RUN_META" ]; then
  echo "Signed run:    $SIGNED_RUN_META"
fi

format_run "$SIGNED_ROW" "Signed build"

echo
if [ -n "$QA_STATUS" ]; then
  echo "QA:            $QA_STATUS"
else
  echo "QA:            NOT RECORDED"
fi

echo "Meta phase:    $PHASE"

if [ -n "$TAG_SHA" ]; then
  echo "Release tag:   PRESENT ($TAG)"
else
  echo "Release tag:   MISSING ($TAG)"
fi

if [ -n "$CHECKPOINT_TAG" ]; then
  CHECKPOINT_SHA="$(
    git -C "$YTM_REPO_DIR" ls-remote --tags origin "refs/tags/$CHECKPOINT_TAG" 2>/dev/null |
      awk 'NR == 1 {print $1}' ||
    true
  )"
  if [ -n "$CHECKPOINT_SHA" ]; then
    echo "Checkpoint:    PRESENT ($CHECKPOINT_TAG)"
  else
    echo "Checkpoint:    MISSING ($CHECKPOINT_TAG)"
  fi
fi

echo "GitHub release: $RELEASE_STATE"

if [ "$PHASE" = "final" ] &&
   [ -n "$TAG_SHA" ] &&
   [ "$RELEASE_STATE" = "PUBLISHED" ]; then
  echo
  echo "CLOSEOUT: FINAL"
else
  echo
  echo "CLOSEOUT: IN PROGRESS"
fi
