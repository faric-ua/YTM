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
  sed -n 's/.*versionName = "\([^"]*\)".*/\1/p'     "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"
VERSION_CODE="$(
  sed -n 's/.*versionCode = \([0-9][0-9]*\).*/\1/p'     "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"
MIN_SDK="$(
  sed -n 's/.*minSdk = \([0-9][0-9]*\).*/\1/p'     "$YTM_REPO_DIR/app/build.gradle.kts" |
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
  gh run list     --repo "$YTM_GH_REPO"     --workflow validate.yml     --branch "$BRANCH"     --limit 50     --json databaseId,headSha,status,conclusion     --jq ".[] | select(.headSha == \"$REMOTE_HEAD\" and .conclusion == \"success\") | .databaseId" |
    head -n 1
)"

[ -n "$VALIDATION_RUN" ] ||
  ytm_fail "Current remote HEAD has no successful Validate Android run."

RUN_HEAD="$(
  gh run view "$SIGNED_RUN"     --repo "$YTM_GH_REPO"     --json headSha     --jq '.headSha'
)"
RUN_CONCLUSION="$(
  gh run view "$SIGNED_RUN"     --repo "$YTM_GH_REPO"     --json conclusion     --jq '.conclusion'
)"

[ "$RUN_HEAD" = "$APP_SOURCE" ] ||
  ytm_fail "Signed run source mismatch: $RUN_HEAD != $APP_SOURCE"
[ "$RUN_CONCLUSION" = "success" ] ||
  ytm_fail "Signed run $SIGNED_RUN is not successful"

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

TMP="$(mktemp -d "${TMPDIR:-$HOME}/ytm-release.XXXXXX")"
cleanup() {
  rm -rf "$TMP"
}
trap cleanup EXIT

echo
echo "Downloading exact signed artifact from run $SIGNED_RUN..."
gh run download "$SIGNED_RUN"   --repo "$YTM_GH_REPO"   --dir "$TMP/artifact"

mapfile -t APKS < <(
  find "$TMP/artifact" -type f -name "YTM-Importer-v$VERSION-release.apk" -print
)

[ "${#APKS[@]}" -eq 1 ] ||
  ytm_fail "Expected exactly one signed APK for v$VERSION; found ${#APKS[@]}"

APK="${APKS[0]}"
SHA_FILE="$APK.sha256"
[ -s "$SHA_FILE" ] ||
  ytm_fail "Signed APK checksum file missing"

(
  cd "$(dirname "$APK")"
  sha256sum -c "$(basename "$SHA_FILE")"
)

APK_NAME="$(basename "$APK")"
APK_SHA="$(
  sha256sum "$APK" |
    awk '{print $1}'
)"

UPDATE_MANIFEST="$TMP/YTM-Importer-update.json"
python - "$UPDATE_MANIFEST" "$VERSION" "$VERSION_CODE" "$TAG" "$APK_NAME" "$APK_SHA" "$MIN_SDK" <<'PY'
import json
import sys

path, version, code, tag, apk, sha, min_sdk = sys.argv[1:]
data = {
    "schema": 1,
    "versionName": version,
    "versionCode": int(code),
    "tag": tag,
    "apkAsset": apk,
    "sha256": sha,
    "minSdk": int(min_sdk),
}
with open(path, "w", encoding="utf-8") as fh:
    json.dump(data, fh, ensure_ascii=False, indent=2)
    fh.write("\n")
PY

NOTES="$TMP/RELEASE_NOTES.md"
cat > "$NOTES" <<EOF
# YTM Importer v$VERSION

Stable release — URL Snapshot / Home UX Polish.

- UX-027: one-row duplicate chooser — PHONE PASS
- UX-028: exact Home → History detail drill-down — PHONE PASS
- targeted phone Tests 1–3: PASS
- unique snapshot handoff: 320 saved / 493 duplicates
- local History semantics; no automatic YTM write

Exact phone-tested app source:
$APP_SOURCE

Signed GitHub Actions run:
$SIGNED_RUN
EOF

ensure_remote_tag() {
  local tag="$1"
  local remote

  if remote="$(
    gh api "repos/$YTM_GH_REPO/git/ref/tags/$tag"       --jq '.object.sha'       2>/dev/null
  )"; then
    [ "$remote" = "$APP_SOURCE" ] ||
      ytm_fail "Remote tag $tag points to $remote, expected $APP_SOURCE"
    echo "Tag already correct: $tag"
    return
  fi

  remote=""

  gh api     --method POST     "repos/$YTM_GH_REPO/git/refs"     -f "ref=refs/tags/$tag"     -f "sha=$APP_SOURCE"     >/dev/null

  remote="$(
    gh api "repos/$YTM_GH_REPO/git/ref/tags/$tag"       --jq '.object.sha'
  )"

  [ "$remote" = "$APP_SOURCE" ] ||
    ytm_fail "Created tag $tag points to $remote, expected $APP_SOURCE"

  echo "Created tag: $tag"
}

ensure_remote_tag "$TAG"
ensure_remote_tag "$CHECKPOINT_TAG"

git -C "$YTM_REPO_DIR" fetch --quiet --tags origin

if gh release view "$TAG" --repo "$YTM_GH_REPO" >/dev/null 2>&1; then
  echo "Release already exists; refreshing exact assets."
else
  gh release create "$TAG"     --repo "$YTM_GH_REPO"     --verify-tag     --title "YTM Importer v$VERSION"     --notes-file "$NOTES"     --latest
fi

gh release upload "$TAG"   "$APK"   "$SHA_FILE"   "$UPDATE_MANIFEST"   --repo "$YTM_GH_REPO"   --clobber

LATEST_TAG="$(
  gh api "repos/$YTM_GH_REPO/releases/latest"     --jq '.tag_name'
)"
[ "$LATEST_TAG" = "$TAG" ] ||
  ytm_fail "Latest stable release is $LATEST_TAG, expected $TAG"

VERIFY_DIR="$TMP/verify"
mkdir -p "$VERIFY_DIR"

gh release download "$TAG"   --repo "$YTM_GH_REPO"   --dir "$VERIFY_DIR"   --pattern "$APK_NAME"   --pattern "$APK_NAME.sha256"   --pattern "YTM-Importer-update.json"

[ -f "$VERIFY_DIR/$APK_NAME" ] ||
  ytm_fail "Published APK asset missing"
[ -f "$VERIFY_DIR/$APK_NAME.sha256" ] ||
  ytm_fail "Published checksum asset missing"
[ -f "$VERIFY_DIR/YTM-Importer-update.json" ] ||
  ytm_fail "Published update manifest missing"

PUBLISHED_SHA="$(
  sha256sum "$VERIFY_DIR/$APK_NAME" |
    awk '{print $1}'
)"
[ "$PUBLISHED_SHA" = "$APK_SHA" ] ||
  ytm_fail "Published APK SHA-256 mismatch"

cmp -s "$UPDATE_MANIFEST" "$VERIFY_DIR/YTM-Importer-update.json" ||
  ytm_fail "Published update manifest differs from generated manifest"

echo
echo "RELEASE PUBLICATION PASS"
echo "TAG=$TAG"
echo "CHECKPOINT=$CHECKPOINT_TAG"
echo "APP_SOURCE=$APP_SOURCE"
echo "SIGNED_RUN=$SIGNED_RUN"
echo "APK_SHA256=$APK_SHA"
echo
echo "Next: report 8+ to ChatGPT for final docs + OTA equal-version smoke."
