#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh
ytm_require_clean

BRANCH="$(ytm_branch)"
LOCAL_HEAD="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE_HEAD="$(ytm_remote_head "$BRANCH")"

[ "$LOCAL_HEAD" = "$REMOTE_HEAD" ] ||
  ytm_fail "Repository is not synced to remote HEAD. Run menu item 1 first."

VERSION="$(
  sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' \
    "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"
VERSION_CODE="$(
  sed -n 's/.*versionCode = \([0-9][0-9]*\).*/\1/p' \
    "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"
MIN_SDK="$(
  sed -n 's/.*minSdk = \([0-9][0-9]*\).*/\1/p' \
    "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"

[ -n "$VERSION" ] || ytm_fail "Cannot resolve versionName"
[ -n "$VERSION_CODE" ] || ytm_fail "Cannot resolve versionCode"
[ -n "$MIN_SDK" ] || ytm_fail "Cannot resolve minSdk"

META="$YTM_REPO_DIR/docs/v.$VERSION/RELEASE_META.json"
[ -f "$META" ] || ytm_fail "Release metadata missing: $META"

meta_value() {
  python - "$META" "$1" <<'PY'
import json
import sys

path, key = sys.argv[1], sys.argv[2]
value = json.load(open(path, encoding="utf-8")).get(key)
if value is None:
    print("")
else:
    print(value)
PY
}

APP_SOURCE="$(meta_value appSourceSha)"
SIGNED_RUN="$(meta_value signedRun)"
QA_STATUS="$(meta_value qaStatus)"
PHONE_TEST_DATE="$(meta_value phoneTestDate)"
TAG="$(meta_value tag)"
CHECKPOINT_TAG="$(meta_value checkpointTag)"

[ -n "$APP_SOURCE" ] || ytm_fail "RELEASE_META appSourceSha is missing"
[ -n "$SIGNED_RUN" ] || ytm_fail "RELEASE_META signedRun is missing"
[ -n "$PHONE_TEST_DATE" ] || ytm_fail "RELEASE_META phoneTestDate is missing"

case "$QA_STATUS" in
  *PASS*) ;;
  *) ytm_fail "Release QA status is not PASS: $QA_STATUS" ;;
esac

[ -n "$TAG" ] || TAG="v$VERSION"
[ -n "$CHECKPOINT_TAG" ] || CHECKPOINT_TAG="checkpoint-v$VERSION-phone-pass"

git -C "$YTM_REPO_DIR" cat-file -e "$APP_SOURCE^{commit}" 2>/dev/null ||
  ytm_fail "Phone-tested app source is not present locally: $APP_SOURCE"

VALIDATION_RUN="$(
  gh run list \
    --repo "$YTM_GH_REPO" \
    --workflow validate.yml \
    --branch "$BRANCH" \
    --limit 50 \
    --json databaseId,headSha,status,conclusion \
    --jq ".[] | select(.headSha == \"$REMOTE_HEAD\" and .conclusion == \"success\") | .databaseId" |
    head -n 1
)"

[ -n "$VALIDATION_RUN" ] ||
  ytm_fail "Current remote HEAD has no successful Validate Android run."

RUN_HEAD="$(
  gh run view "$SIGNED_RUN" \
    --repo "$YTM_GH_REPO" \
    --json headSha \
    --jq '.headSha'
)"
RUN_CONCLUSION="$(
  gh run view "$SIGNED_RUN" \
    --repo "$YTM_GH_REPO" \
    --json conclusion \
    --jq '.conclusion'
)"

[ "$RUN_HEAD" = "$APP_SOURCE" ] ||
  ytm_fail "Signed run source mismatch: $RUN_HEAD != $APP_SOURCE"
[ "$RUN_CONCLUSION" = "success" ] ||
  ytm_fail "Signed run $SIGNED_RUN is not successful"

PUBLISH_WORKFLOW="publish-release.yml"

ensure_user_tag() {
  local tag="$1"
  local remote
  local error_file

  remote="$(
    gh api "repos/$YTM_GH_REPO/git/ref/tags/$tag" \
      --jq '.object.sha' 2>/dev/null ||
    true
  )"

  if [ -n "$remote" ]; then
    [ "$remote" = "$APP_SOURCE" ] ||
      ytm_fail "Remote tag $tag points to $remote, expected $APP_SOURCE"
    echo "Tag already correct: $tag"
    return
  fi

  error_file="$(mktemp "${TMPDIR:-$HOME}/ytm-tag-error.XXXXXX")"

  if ! gh api \
    --method POST \
    "repos/$YTM_GH_REPO/git/refs" \
    -f "ref=refs/tags/$tag" \
    -f "sha=$APP_SOURCE" \
    >/dev/null 2>"$error_file"
  then
    cat "$error_file" >&2
    rm -f "$error_file"
    ytm_fail "Cannot create release tag with current GitHub CLI token. Run once: gh auth refresh -h github.com -s workflow ; then rerun menu item 8."
  fi

  rm -f "$error_file"

  remote="$(
    gh api "repos/$YTM_GH_REPO/git/ref/tags/$tag" \
      --jq '.object.sha'
  )"

  [ "$remote" = "$APP_SOURCE" ] ||
    ytm_fail "Created tag $tag points to $remote, expected $APP_SOURCE"

  echo "Created tag: $tag"
}

echo "Stable release publication"
echo "=========================="
echo "Version:       $VERSION ($VERSION_CODE)"
echo "Branch HEAD:   $REMOTE_HEAD"
echo "Validation:    PASS (run $VALIDATION_RUN)"
echo "Phone source:  $APP_SOURCE"
echo "Signed run:    $SIGNED_RUN"
echo "QA:            $QA_STATUS"
echo "Release tag:   $TAG"
echo "Checkpoint:    $CHECKPOINT_TAG"
echo "Publisher:     GitHub Actions"
echo
printf "Опублікувати stable %s? [y/N]: " "$TAG"
read -r answer
case "$answer" in
  y|Y|yes|YES|так|Так|ТАК) ;;
  *)
    echo "Cancelled."
    exit 0
    ;;
esac

BEFORE_IDS="$(
  gh run list \
    --repo "$YTM_GH_REPO" \
    --workflow "$PUBLISH_WORKFLOW" \
    --branch "$BRANCH" \
    --event workflow_dispatch \
    --limit 50 \
    --json databaseId \
    --jq '.[].databaseId' 2>/dev/null ||
    true
)"

echo
echo "Ensuring release tags with authenticated user token..."
ensure_user_tag "$TAG"
ensure_user_tag "$CHECKPOINT_TAG"

echo
echo "Dispatching guarded stable publisher..."

gh workflow run "$PUBLISH_WORKFLOW" \
  --repo "$YTM_GH_REPO" \
  --ref "$BRANCH" \
  -f "version=$VERSION" \
  -f "version_code=$VERSION_CODE" \
  -f "min_sdk=$MIN_SDK" \
  -f "app_source=$APP_SOURCE" \
  -f "signed_run=$SIGNED_RUN" \
  -f "tag=$TAG" \
  -f "checkpoint_tag=$CHECKPOINT_TAG"

PUBLISH_RUN=""

for _ in $(seq 1 60); do
  CANDIDATES="$(
    gh run list \
      --repo "$YTM_GH_REPO" \
      --workflow "$PUBLISH_WORKFLOW" \
      --branch "$BRANCH" \
      --event workflow_dispatch \
      --limit 20 \
      --json databaseId,headSha \
      --jq ".[] | select(.headSha == \"$REMOTE_HEAD\") | .databaseId" 2>/dev/null ||
      true
  )"

  while IFS= read -r candidate; do
    [ -n "$candidate" ] || continue
    if ! printf '%s\n' "$BEFORE_IDS" | grep -Fxq "$candidate"; then
      PUBLISH_RUN="$candidate"
      break
    fi
  done <<< "$CANDIDATES"

  [ -n "$PUBLISH_RUN" ] && break
  sleep 2
done

[ -n "$PUBLISH_RUN" ] ||
  ytm_fail "Could not resolve the newly dispatched stable publisher run"

echo "Publisher run: $PUBLISH_RUN"

if ! gh run watch "$PUBLISH_RUN" \
  --repo "$YTM_GH_REPO" \
  --exit-status
then
  ytm_fail "Stable publisher failed (run $PUBLISH_RUN). Release state must be inspected before retry."
fi

PUBLISH_HEAD="$(
  gh run view "$PUBLISH_RUN" \
    --repo "$YTM_GH_REPO" \
    --json headSha \
    --jq '.headSha'
)"
PUBLISH_CONCLUSION="$(
  gh run view "$PUBLISH_RUN" \
    --repo "$YTM_GH_REPO" \
    --json conclusion \
    --jq '.conclusion'
)"

[ "$PUBLISH_HEAD" = "$REMOTE_HEAD" ] ||
  ytm_fail "Publisher source mismatch: $PUBLISH_HEAD != $REMOTE_HEAD"
[ "$PUBLISH_CONCLUSION" = "success" ] ||
  ytm_fail "Publisher run $PUBLISH_RUN is not successful"

TAG_SHA="$(
  git -C "$YTM_REPO_DIR" ls-remote --tags origin "refs/tags/$TAG" |
    awk 'NR == 1 {print $1}'
)"
CHECKPOINT_SHA="$(
  git -C "$YTM_REPO_DIR" ls-remote --tags origin "refs/tags/$CHECKPOINT_TAG" |
    awk 'NR == 1 {print $1}'
)"

[ "$TAG_SHA" = "$APP_SOURCE" ] ||
  ytm_fail "Stable tag points to $TAG_SHA, expected $APP_SOURCE"
[ "$CHECKPOINT_SHA" = "$APP_SOURCE" ] ||
  ytm_fail "Checkpoint tag points to $CHECKPOINT_SHA, expected $APP_SOURCE"

LATEST_TAG="$(
  gh api "repos/$YTM_GH_REPO/releases/latest" \
    --jq '.tag_name'
)"
[ "$LATEST_TAG" = "$TAG" ] ||
  ytm_fail "Latest stable release is $LATEST_TAG, expected $TAG"

TMP="$(mktemp -d "${TMPDIR:-$HOME}/ytm-release-verify.XXXXXX")"
cleanup() {
  rm -rf "$TMP"
}
trap cleanup EXIT

APK_NAME="YTM-Importer-v$VERSION-release.apk"

gh release download "$TAG" \
  --repo "$YTM_GH_REPO" \
  --dir "$TMP" \
  --pattern "$APK_NAME" \
  --pattern "$APK_NAME.sha256" \
  --pattern "YTM-Importer-update.json"

[ -f "$TMP/$APK_NAME" ] ||
  ytm_fail "Published APK asset missing"
[ -f "$TMP/$APK_NAME.sha256" ] ||
  ytm_fail "Published checksum asset missing"
[ -f "$TMP/YTM-Importer-update.json" ] ||
  ytm_fail "Published update manifest missing"

(
  cd "$TMP"
  sha256sum -c "$APK_NAME.sha256"
)

APK_SHA="$(
  sha256sum "$TMP/$APK_NAME" |
    awk '{print $1}'
)"

python - \
  "$TMP/YTM-Importer-update.json" \
  "$VERSION" \
  "$VERSION_CODE" \
  "$TAG" \
  "$APK_NAME" \
  "$APK_SHA" \
  "$MIN_SDK" <<'PY'
import json
import sys

path, version, code, tag, apk, sha, min_sdk = sys.argv[1:]
data = json.load(open(path, encoding="utf-8"))
expected = {
    "schema": 1,
    "versionName": version,
    "versionCode": int(code),
    "tag": tag,
    "apkAsset": apk,
    "sha256": sha,
    "minSdk": int(min_sdk),
}
if data != expected:
    raise SystemExit(f"Published update manifest mismatch:\nactual={data!r}\nexpected={expected!r}")
PY

echo
echo "RELEASE PUBLICATION PASS"
echo "TAG=$TAG"
echo "CHECKPOINT=$CHECKPOINT_TAG"
echo "APP_SOURCE=$APP_SOURCE"
echo "SIGNED_RUN=$SIGNED_RUN"
echo "PUBLISH_RUN=$PUBLISH_RUN"
echo "APK_SHA256=$APK_SHA"
echo
echo "Next: report 8+ to ChatGPT for final docs + OTA equal-version smoke."
