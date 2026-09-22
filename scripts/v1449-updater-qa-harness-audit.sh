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
HELPER="scripts/v1449-updater-qa-workflow.py"
DOC="docs/v.1.4.49/qa/UPDATER_QA_CHANNEL.md"

for file in \
  "$WORKFLOW" \
  "$REMOTE" \
  "$SERVICE" \
  "$GRADLE" \
  "$HELPER" \
  "$DOC"
do
  test -f "$file" || fail "missing QA harness file: $file"
done

grep -Fq 'versionCode = 92' "$GRADLE" ||
  fail "production versionCode changed"
grep -Fq 'versionName = "1.4.49"' "$GRADLE" ||
  fail "production versionName changed"

grep -Fq \
  '"https://github.com/faric-ua/YTM/releases/latest/download/" +' \
  "$REMOTE" ||
  fail "production updater source no longer points to stable latest Release"

grep -Fq '"Джерело: офіційний GitHub Release faric-ua/YTM."' \
  "$SERVICE" ||
  fail "production updater source label changed"

for needle in \
  'QA_VERSION_NAME = "1.4.49-updater-qa1"' \
  'QA_VERSION_CODE = 93' \
  'v1.4.49-updater-qa1/YTM-Importer-update.json' \
  'fixture' \
  'client'
do
  grep -Fq "$needle" "$HELPER" ||
    fail "QA helper contract missing: $needle"
done

if grep -Fq '# QA TEMP BRANCH MODE: fixture' "$WORKFLOW"; then
  grep -Fq 'Apply QA fixture identity' "$WORKFLOW" ||
    fail "fixture workflow step missing"
  grep -Fq "sed -i 's/versionCode = 92/versionCode = 93/'" "$WORKFLOW" ||
    fail "fixture versionCode override missing"
  grep -Fq '1.4.49-updater-qa1' "$WORKFLOW" ||
    fail "fixture versionName override missing"
elif grep -Fq '# QA TEMP BRANCH MODE: client' "$WORKFLOW"; then
  grep -Fq 'Apply QA updater channel' "$WORKFLOW" ||
    fail "client updater-channel step missing"
  grep -Fq 'v1.4.49-updater-qa1/YTM-Importer-update.json' "$WORKFLOW" ||
    fail "client QA manifest URL missing"
  grep -Fq 'QA GitHub prerelease faric-ua/YTM' "$WORKFLOW" ||
    fail "client QA source label missing"
else
  grep -Fq 'on: [workflow_dispatch]' "$WORKFLOW" ||
    fail "base workflow_dispatch trigger missing"
  if grep -Fq 'qa_manifest_url:' "$WORKFLOW"; then
    fail "branch-only workflow_dispatch inputs returned"
  fi
  if grep -Fq 'Apply QA fixture identity' "$WORKFLOW" ||
     grep -Fq 'Apply QA updater channel' "$WORKFLOW"; then
    fail "QA runtime override leaked into production workflow"
  fi
fi

grep -Fq 'temporary QA branches' "$DOC" ||
  fail "temporary QA branch strategy not documented"
grep -Fq 'prerelease' "$DOC" ||
  fail "QA prerelease isolation not documented"
grep -Fq 'stable `latest` remains untouched' "$DOC" ||
  fail "stable latest isolation contract missing"

echo "PASS:"
echo "- production updater source remains stable/latest"
echo "- production app identity remains 1.4.49 / 92"
echo "- default workflow keeps ordinary workflow_dispatch"
echo "- QA fixture/client generation is isolated to temporary branches"
echo "- temporary branch workflow mode is statically validated"
echo "- QA prerelease does not redefine stable latest"
