#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo

PATH_FILE="$YTM_STATE_DIR/latest-apk.path"
SOURCE_FILE="$YTM_STATE_DIR/latest-apk.source"
RUN_FILE="$YTM_STATE_DIR/latest-apk.run"
BRANCH_FILE="$YTM_STATE_DIR/latest-apk.branch"

for f in "$PATH_FILE" "$SOURCE_FILE" "$RUN_FILE" "$BRANCH_FILE"; do
  [ -s "$f" ] ||
    ytm_fail "No verified downloaded APK metadata. Use Download signed APK first."
done

APK="$(cat "$PATH_FILE")"
SOURCE="$(cat "$SOURCE_FILE")"
RUN_ID="$(cat "$RUN_FILE")"
DOWNLOADED_BRANCH="$(cat "$BRANCH_FILE")"

[ -s "$APK" ] ||
  ytm_fail "Downloaded APK no longer exists: $APK"

SHA="$APK.sha256"
[ -s "$SHA" ] ||
  ytm_fail "Checksum file missing: $SHA"

CURRENT_BRANCH="$(ytm_branch)"
[ "$CURRENT_BRANCH" = "$DOWNLOADED_BRANCH" ] ||
  ytm_fail "Downloaded APK belongs to branch $DOWNLOADED_BRANCH, current branch is $CURRENT_BRANCH"

REMOTE_HEAD="$(ytm_remote_head "$CURRENT_BRANCH")"

if [ "$REMOTE_HEAD" != "$SOURCE" ]; then
  git -C "$YTM_REPO_DIR" fetch origin "$CURRENT_BRANCH" --quiet

  git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$SOURCE" "$REMOTE_HEAD" ||
    ytm_fail "Downloaded APK source is not an ancestor of current remote HEAD"

  CHANGED_AFTER_SOURCE="$(
    git -C "$YTM_REPO_DIR" diff --name-only "$SOURCE" "$REMOTE_HEAD"
  )"

  UNSAFE_AFTER_SOURCE="$(
    printf '%s\n' "$CHANGED_AFTER_SOURCE" |
      grep -Ev '^(tools/termux/|docs/|FILE_MANIFEST\.txt$|CURRENT_HANDOFF\.md$|PROJECT_STATUS\.txt$|RELEASE_TEST_STATUS\.md$)' ||
    true
  )"

  [ -z "$UNSAFE_AFTER_SOURCE" ] ||
    ytm_fail "Remote app/build source moved since download. Download the current signed APK first."

  echo "Remote HEAD advanced only by tooling/docs after this APK source."
  echo "APK source remains install-compatible: $SOURCE"
fi

(
  cd "$(dirname "$APK")"
  sha256sum -c "$(basename "$SHA")"
)

command -v termux-open >/dev/null 2>&1 ||
  ytm_fail "termux-open is unavailable"

INSTALL_STAGE_DIR="$YTM_STATE_DIR/install-staging/run-$RUN_ID"
STAGED_APK="$INSTALL_STAGE_DIR/$(basename "$APK")"
STAGED_SHA="$STAGED_APK.sha256"

rm -rf "$INSTALL_STAGE_DIR"
mkdir -p "$INSTALL_STAGE_DIR"

cp "$APK" "$STAGED_APK"
cp "$SHA" "$STAGED_SHA"

(
  cd "$INSTALL_STAGE_DIR"
  sha256sum -c "$(basename "$STAGED_SHA")"
)

ORIGINAL_HASH="$(sha256sum "$APK" | awk '{print $1}')"
STAGED_HASH="$(sha256sum "$STAGED_APK" | awk '{print $1}')"

[ "$ORIGINAL_HASH" = "$STAGED_HASH" ] ||
  ytm_fail "Private Termux install staging hash mismatch"

echo "Opening Android installer from private Termux staging:"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$SOURCE"
echo "ARCHIVE_APK=$APK"
echo "STAGED_APK=$STAGED_APK"
echo "SHA256=$STAGED_HASH"

CONTENT_URI="content://com.termux.files$STAGED_APK"
APK_MIME="application/vnd.android.package-archive"

command -v am >/dev/null 2>&1 ||
  ytm_fail "Android activity manager (am) is unavailable"

echo
echo "Trying SAI rootless installer first:"

SAI_COMPONENT="com.aefyr.sai/com.aefyr.sai.ui.activities.ApkActionViewProxyActivity"

set +e
SAI_OUTPUT="$(
  am start \
    -W \
    -n "$SAI_COMPONENT" \
    -a android.intent.action.VIEW \
    -c android.intent.category.DEFAULT \
    -d "$CONTENT_URI" \
    -t "$APK_MIME" \
    -f 0x10000001 \
    2>&1
)"
SAI_RC=$?
set -e

printf '%s\n' "$SAI_OUTPUT"

if [ "$SAI_RC" -eq 0 ] &&
   ! printf '%s\n' "$SAI_OUTPUT" |
     grep -Eqi 'Error:|Exception|SecurityException|unable to resolve|not found'
then
  echo
  echo "SAI installer launched."
  echo "Finish the install in SAI, then return to the YTM menu."
  exit 0
fi

echo
echo "SAI direct launch was unavailable; opening Android chooser as fallback."
echo "If SAI is shown, choose SAI."

termux-open \
  --view \
  --content-type "$APK_MIME" \
  "$STAGED_APK"
