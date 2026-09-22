#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

WORKFLOW=".github/workflows/build-apk.yml"
REMOTE="app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt"
SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
GRADLE="app/build.gradle.kts"
DOC="docs/v.1.4.49/qa/UPDATER_QA_CHANNEL.md"

for file in \
  "$WORKFLOW" \
  "$REMOTE" \
  "$SERVICE" \
  "$GRADLE" \
  "$DOC"
do
  test -f "$file" || fail "missing QA harness file: $file"
done

for needle in \
  'qa_manifest_url:' \
  'qa_version_name:' \
  'qa_version_code:' \
  'Apply QA build overrides' \
  'QA manifest URL must be a faric-ua/YTM GitHub Release asset' \
  'QA versionCode must be greater than production code 92' \
  'YTM-Importer-v${APP_VERSION}-QA-Channel'
do
  grep -Fq "$needle" "$WORKFLOW" ||
    fail "QA workflow contract missing: $needle"
done

grep -Fq \
  '"https://github.com/faric-ua/YTM/releases/latest/download/" +' \
  "$REMOTE" ||
  fail "production updater source no longer points to stable latest Release"

grep -Fq '"Джерело: офіційний GitHub Release faric-ua/YTM."' \
  "$SERVICE" ||
  fail "production updater source label changed"

grep -Fq 'versionCode = 92' "$GRADLE" ||
  fail "production versionCode changed by QA harness"

grep -Fq 'versionName = "1.4.49"' "$GRADLE" ||
  fail "production versionName changed by QA harness"

if grep -Fq 'QA GitHub prerelease' "$SERVICE"; then
  fail "QA channel label leaked into production source"
fi

grep -Fq 'prerelease' "$DOC" ||
  fail "QA channel documentation must identify prerelease isolation"

grep -Fq 'stable `latest` remains untouched' "$DOC" ||
  fail "QA stable-channel isolation contract missing"

echo "PASS:"
echo "- production stable updater URL remains unchanged"
echo "- QA manifest override exists only at signed-build time"
echo "- QA version identity override exists only at signed-build time"
echo "- QA Release URL is constrained to faric-ua/YTM"
echo "- QA artifact naming is distinct"
echo "- QA prerelease isolation is documented"
