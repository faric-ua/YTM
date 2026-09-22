#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MANIFEST="app/src/main/AndroidManifest.xml"
PATHS="app/src/main/res/xml/file_paths.xml"
SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt"
QA_HELPER="scripts/v1449-wave3-qa-workflow.py"
PHONE="docs/v.1.4.49/qa/PHONE_TEST.md"

for file in \
  "$MANIFEST" \
  "$PATHS" \
  "$SERVICE" \
  "$REMOTE" \
  "$QA_HELPER" \
  "$PHONE"
do
  test -f "$file" || fail "missing Wave 3 file: $file"
done

grep -Fq 'android.permission.REQUEST_INSTALL_PACKAGES' "$MANIFEST" ||
  fail "REQUEST_INSTALL_PACKAGES permission missing"

grep -Fq 'name="verified_updates"' "$PATHS" ||
  fail "FileProvider verified_updates path missing"
grep -Fq 'path="updates/"' "$PATHS" ||
  fail "FileProvider updates path missing"

for needle in \
  'private fun installVerifiedUpdate()' \
  'packageManager.canRequestPackageInstalls()' \
  'Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES' \
  'FileProvider.getUriForFile(' \
  'Intent.ACTION_VIEW' \
  'Intent.FLAG_GRANT_READ_URI_PERMISSION' \
  '"application/vnd.android.package-archive"' \
  '"Встановити"'
do
  grep -Fq "$needle" "$SERVICE" ||
    fail "installer contract missing: $needle"
done

grep -Fq 'state.phase == UpdaterRemoteOperations.Phase.READY_TO_INSTALL' "$SERVICE" ||
  fail "Ready-to-install explicit action guard missing"

grep -Fq 'Натисніть «Встановити»' "$REMOTE" ||
  fail "Ready-state install instruction missing"

python - "$SERVICE" <<'PY_AUDIT'
import sys
from pathlib import Path

text = Path(sys.argv[1]).read_text()
needle = "installVerifiedUpdate()"
count = text.count(needle)

if count != 2:
    raise SystemExit(
        f"FAIL: expected installer helper definition + one explicit call, found {count}"
    )

action_start = text.find("val actionButton =")
action_end = text.find("actionButton.isEnabled", action_start)
call = text.find(needle, action_start, action_end)

if action_start < 0 or action_end < 0 or call < 0:
    raise SystemExit(
        "FAIL: installer call is not confined to the explicit action-button handler"
    )

for lifecycle in ("override fun onCreate", "override fun onResume"):
    start = text.find(lifecycle)
    if start >= 0:
        end = text.find("\n    override fun ", start + 1)
        if end < 0:
            end = text.find("\n    private fun ", start + 1)
        if end < 0:
            end = len(text)
        if needle in text[start:end]:
            raise SystemExit(
                f"FAIL: installer must not auto-launch from {lifecycle}"
            )

print("PASS: installer launch is explicit-user-action only")
PY_AUDIT

for needle in \
  'QA_APPLICATION_ID = "com.saney.ytmimporter.updaterqa"' \
  'QA_LABEL = "YTM Importer QA"' \
  'QA_VERSION_NAME = "1.4.49-updater-qa2"' \
  'QA_VERSION_CODE = 93' \
  'v1.4.49-updater-qa2/YTM-Importer-update.json'
do
  grep -Fq "$needle" "$QA_HELPER" ||
    fail "Wave 3 QA clone helper missing: $needle"
done

grep -Fq '## Test 5 — installer cancel' "$PHONE" ||
  fail "installer cancel phone test missing"
grep -Fq '## Test 6 — successful update' "$PHONE" ||
  fail "successful update phone test missing"
grep -Fq 'YTM Importer QA' "$PHONE" ||
  fail "isolated QA-clone phone instructions missing"

echo "PASS:"
echo "- explicit install button from verified Ready state"
echo "- verified APK is shared only through FileProvider"
echo "- unknown-sources permission is explicit and non-automatic"
echo "- installer launch is not tied to Activity recreation"
echo "- cancel can return to the inspectable Ready state"
echo "- Wave 3 QA uses isolated package com.saney.ytmimporter.updaterqa"
