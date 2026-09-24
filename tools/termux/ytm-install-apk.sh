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

INSTALL_PICK_DIR="/storage/emulated/0/Download/YTM-Install/run-$RUN_ID"
PICK_APK="$INSTALL_PICK_DIR/$(basename "$APK")"
PICK_SHA="$PICK_APK.sha256"

rm -rf "$INSTALL_PICK_DIR"
mkdir -p "$INSTALL_PICK_DIR"

cp "$APK" "$PICK_APK"
cp "$SHA" "$PICK_SHA"

(
  cd "$INSTALL_PICK_DIR"
  sha256sum -c "$(basename "$PICK_SHA")"
)

ORIGINAL_HASH="$(sha256sum "$APK" | awk '{print $1}')"
PICK_HASH="$(sha256sum "$PICK_APK" | awk '{print $1}')"

[ "$ORIGINAL_HASH" = "$PICK_HASH" ] ||
  ytm_fail "System-picker staging hash mismatch"

echo
echo "APK is ready for Android's system file picker:"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$SOURCE"
echo "PICK_APK=$PICK_APK"
echo "SHA256=$PICK_HASH"
echo
echo "In SAI: tap 'Встановити APK' -> use the system file picker ->"
echo "Download -> YTM-Install -> run-$RUN_ID -> $(basename "$PICK_APK")"
echo
echo "The system file picker is intentional: it returns a SAF URI with"
echo "DISPLAY_NAME metadata, which SAI requires on this Samsung/Termux setup."

command -v am >/dev/null 2>&1 ||
  ytm_fail "Android activity manager (am) is unavailable"

SAI_MAIN="com.aefyr.sai/com.aefyr.sai.ui.activities.MainActivity"

set +e
SAI_OUTPUT="$(
  am start \
    -W \
    -n "$SAI_MAIN" \
    -a android.intent.action.MAIN \
    -c android.intent.category.LAUNCHER \
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
  echo "SAI opened. Select the prepared APK with its system file picker."
  exit 0
fi

echo >&2
echo "SAI could not be opened automatically." >&2
echo "Open SAI manually and select:" >&2
echo "$PICK_APK" >&2
exit 1
