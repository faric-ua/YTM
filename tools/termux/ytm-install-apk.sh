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

command -v am >/dev/null 2>&1 ||
  ytm_fail "Android activity manager (am) is unavailable"

CONTENT_URI="content://com.termux.files$APK"
INSTALLER_PACKAGE=""

for package_name in   com.samsung.android.packageinstaller   com.google.android.packageinstaller   com.android.packageinstaller
do
  if pm path "$package_name" >/dev/null 2>&1; then
    INSTALLER_PACKAGE="$package_name"
    break
  fi
done

echo "Opening Android installer directly:"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$SOURCE"
echo "APK=$APK"

if [ -n "$INSTALLER_PACKAGE" ]; then
  echo "INSTALLER=$INSTALLER_PACKAGE"

  if am start       -W       -a android.intent.action.VIEW       -d "$CONTENT_URI"       -t application/vnd.android.package-archive       -f 0x10000001       -p "$INSTALLER_PACKAGE"
  then
    exit 0
  fi

  echo
  echo "Direct installer launch failed; falling back to Android resolver."
fi

command -v termux-open >/dev/null 2>&1 ||
  ytm_fail "termux-open is unavailable and direct installer launch failed"

termux-open --view "$APK"
