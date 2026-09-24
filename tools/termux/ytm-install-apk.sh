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
      grep -Ev '^(tools/termux/|docs/|FILE_MANIFEST\.txt$|CURRENT_HANDOFF\.md$|PROJECT_STATUS\.txt$|RELEASE_TEST_STATUS\.md$|TERMUX_COMMANDS\.md$|YTM_ASSISTANT_WORKFLOW\.md$)' ||
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

OPEN_DIR="/storage/emulated/0/Download/YTM-Install/run-$RUN_ID"
OPEN_APK="$OPEN_DIR/$(basename "$APK")"
OPEN_SHA="$OPEN_APK.sha256"

rm -rf "$OPEN_DIR"
mkdir -p "$OPEN_DIR"

cp "$APK" "$OPEN_APK"
cp "$SHA" "$OPEN_SHA"

(
  cd "$OPEN_DIR"
  sha256sum -c "$(basename "$OPEN_SHA")"
)

ORIGINAL_HASH="$(sha256sum "$APK" | awk '{print $1}')"
OPEN_HASH="$(sha256sum "$OPEN_APK" | awk '{print $1}')"

[ "$ORIGINAL_HASH" = "$OPEN_HASH" ] ||
  ytm_fail "Open-folder staging hash mismatch"

echo
echo "APK ready:"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$SOURCE"
echo "FOLDER=$OPEN_DIR"
echo "APK=$OPEN_APK"
echo "SHA256=$OPEN_HASH"
echo
echo "Tap the APK in the opened folder to install it."

command -v am >/dev/null 2>&1 ||
  ytm_fail "Android activity manager (am) is unavailable"

FOLDER_URI="content://com.android.externalstorage.documents/document/primary%3ADownload%2FYTM-Install%2Frun-$RUN_ID"
FOLDER_MIME="vnd.android.document/directory"

try_open_folder() {
  local package_name="$1"
  local output
  local rc

  set +e
  if [ -n "$package_name" ]; then
    output="$(
      am start \
        -W \
        -a android.intent.action.VIEW \
        -d "$FOLDER_URI" \
        -t "$FOLDER_MIME" \
        -p "$package_name" \
        2>&1
    )"
    rc=$?
  else
    output="$(
      am start \
        -W \
        -a android.intent.action.VIEW \
        -d "$FOLDER_URI" \
        -t "$FOLDER_MIME" \
        2>&1
    )"
    rc=$?
  fi
  set -e

  printf '%s\n' "$output"

  [ "$rc" -eq 0 ] &&
    ! printf '%s\n' "$output" |
      grep -Eqi 'Error:|Exception|SecurityException|unable to resolve|not found'
}

echo
echo "Opening APK folder..."

if try_open_folder "com.sec.android.app.myfiles"; then
  echo "Opened with Samsung My Files."
  exit 0
fi

echo
echo "Samsung My Files direct folder open was unavailable; trying Android folder viewer."

if try_open_folder ""; then
  echo "Opened APK folder."
  exit 0
fi

echo
echo "Could not open the exact folder automatically."
echo "Open this folder manually:"
echo "$OPEN_DIR"
exit 1
