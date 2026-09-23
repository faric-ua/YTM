#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh

BRANCH="$(ytm_branch)"
REMOTE_HEAD="$(ytm_remote_head "$BRANCH")"

RUN_ID="$(
  gh run list     --repo "$YTM_GH_REPO"     --workflow "$YTM_WORKFLOW"     --branch "$BRANCH"     --status success     --limit 50     --json databaseId,headSha,createdAt     --jq ".[] | select(.headSha == \"$REMOTE_HEAD\") | .databaseId" |
    head -n 1
)"

[ -n "$RUN_ID" ] ||
  ytm_fail "No successful signed APK build exists for current remote HEAD $REMOTE_HEAD"

RUN_HEAD="$(
  gh run view "$RUN_ID"     --repo "$YTM_GH_REPO"     --json headSha     --jq '.headSha'
)"

[ "$RUN_HEAD" = "$REMOTE_HEAD" ] ||
  ytm_fail "Resolved run does not match remote HEAD"

TMP="$(mktemp -d "${TMPDIR:-$HOME}/ytm-apk.XXXXXX")"
cleanup() {
  rm -rf "$TMP"
}
trap cleanup EXIT

gh run download "$RUN_ID"   --repo "$YTM_GH_REPO"   --dir "$TMP"

mapfile -t APKS < <(
  find "$TMP" -type f -name 'YTM-Importer-v*-release.apk' -print
)

[ "${#APKS[@]}" -eq 1 ] ||
  ytm_fail "Expected exactly one release APK in run $RUN_ID; found ${#APKS[@]}"

APK="${APKS[0]}"
SHA="$APK.sha256"

[ -s "$SHA" ] ||
  ytm_fail "APK checksum file missing: $SHA"

(
  cd "$(dirname "$APK")"
  sha256sum -c "$(basename "$SHA")"
)

REMOTE_AFTER="$(ytm_remote_head "$BRANCH")"
[ "$REMOTE_AFTER" = "$REMOTE_HEAD" ] ||
  ytm_fail "Remote branch moved during download; refusing stale APK handoff"

APK_NAME="$(basename "$APK")"
VERSION="${APK_NAME#YTM-Importer-v}"
VERSION="${VERSION%-release.apk}"
PHONE_DIR="$YTM_REPO_DIR/artifacts/apk/v${VERSION}/run-${RUN_ID}"

mkdir -p "$PHONE_DIR" "$YTM_STATE_DIR"
cp -f "$APK" "$SHA" "$PHONE_DIR/"
sync

COPIED_APK="$PHONE_DIR/$APK_NAME"
(
  cd "$PHONE_DIR"
  sha256sum -c "$APK_NAME.sha256"
)

APK_SHA="$(
  sha256sum "$COPIED_APK" |
    awk '{print $1}'
)"

printf '%s\n' "$COPIED_APK" > "$YTM_STATE_DIR/latest-apk.path"
printf '%s\n' "$REMOTE_HEAD" > "$YTM_STATE_DIR/latest-apk.source"
printf '%s\n' "$RUN_ID" > "$YTM_STATE_DIR/latest-apk.run"
printf '%s\n' "$BRANCH" > "$YTM_STATE_DIR/latest-apk.branch"

echo
echo "SIGNED APK READY"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$REMOTE_HEAD"
echo "APK=$COPIED_APK"
echo "APK_SHA256=$APK_SHA"
