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

command -v cmd >/dev/null 2>&1 ||
  ytm_fail "Android cmd utility is unavailable"

CONTENT_URI="content://com.termux.files$APK"
APK_MIME="application/vnd.android.package-archive"

echo "Resolving Android APK handlers:"
echo "RUN_ID=$RUN_ID"
echo "SOURCE=$SOURCE"
echo "APK=$APK"
echo "URI=$CONTENT_URI"

VIEW_CANDIDATES="$(
  cmd package query-activities     --brief     --components     --user current     -a android.intent.action.VIEW     -c android.intent.category.DEFAULT     -d "$CONTENT_URI"     -t "$APK_MIME"     2>/dev/null ||
  true
)"

INSTALL_CANDIDATES="$(
  cmd package query-activities     --brief     --components     --user current     -a android.intent.action.INSTALL_PACKAGE     -c android.intent.category.DEFAULT     -d "$CONTENT_URI"     -t "$APK_MIME"     2>/dev/null ||
  true
)"

echo
echo "VIEW candidates:"
if [ -n "$VIEW_CANDIDATES" ]; then
  printf '%s\n' "$VIEW_CANDIDATES"
else
  echo "(none)"
fi

echo
echo "INSTALL_PACKAGE candidates:"
if [ -n "$INSTALL_CANDIDATES" ]; then
  printf '%s\n' "$INSTALL_CANDIDATES"
else
  echo "(none)"
fi

ALL_CANDIDATES="$(
  printf '%s\n%s\n' "$VIEW_CANDIDATES" "$INSTALL_CANDIDATES" |
    sed '/^[[:space:]]*$/d' |
    awk '!seen[$0]++'
)"

INSTALL_COMPONENT="$(
  printf '%s\n' "$ALL_CANDIDATES" |
    grep -Ei 'packageinstaller|permissioncontroller' |
    head -n 1 ||
  true
)"

if [ -z "$INSTALL_COMPONENT" ]; then
  echo >&2
  echo "FAIL: Android did not expose a system package-installer component." >&2
  echo "Copy the candidate list above for diagnosis." >&2
  exit 1
fi

echo
echo "Launching system installer component directly:"
echo "COMPONENT=$INSTALL_COMPONENT"

set +e
START_OUTPUT="$(
  am start     -W     -n "$INSTALL_COMPONENT"     -a android.intent.action.VIEW     -c android.intent.category.DEFAULT     -d "$CONTENT_URI"     -t "$APK_MIME"     -f 0x10000001     2>&1
)"
START_RC=$?
set -e

printf '%s\n' "$START_OUTPUT"

if [ "$START_RC" -ne 0 ]; then
  echo >&2
  echo "FAIL: explicit package-installer launch returned code $START_RC." >&2
  exit "$START_RC"
fi

if printf '%s\n' "$START_OUTPUT" | grep -Eqi 'Error:|Exception|SecurityException|unable to resolve'; then
  echo >&2
  echo "FAIL: Android reported an explicit installer launch error." >&2
  exit 1
fi

echo
echo "Installer intent sent to the exact system component."
echo "If the installer closes after tapping Install, the launcher is no longer the cause."
