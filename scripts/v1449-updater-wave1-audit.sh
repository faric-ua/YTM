#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
POLICY="app/src/main/java/com/saney/ytmimporter/updater/UpdaterManifestPolicy.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt"
TEST="app/src/test/java/com/saney/ytmimporter/updater/UpdaterManifestPolicyTest.kt"
META="docs/v.1.4.49/RELEASE_META.json"

for file in \
  "$SERVICE" \
  "$POLICY" \
  "$REMOTE" \
  "$TEST" \
  "$META"
do
  test -f "$file" || fail "missing updater Wave 1 file: $file"
done

grep -Fq 'Page.VERSION -> buildVersion()' "$SERVICE" ||
  fail "Version page is not routed"
grep -Fq 'Page.VERSION -> {' "$SERVICE" ||
  fail "Version Back route to About is missing"
grep -Fq '"Перевірити оновлення"' "$SERVICE" ||
  fail "check-update action missing"
grep -Fq 'UpdaterRemoteOperations.startCheck(' "$SERVICE" ||
  fail "Service does not start updater check through operation owner"
grep -Fq 'UpdaterRemoteOperations.addListener(' "$SERVICE" ||
  fail "Service does not reattach to updater state"
grep -Fq 'UpdaterRemoteOperations.removeListener(' "$SERVICE" ||
  fail "Service does not detach updater listener"

grep -Fq 'object UpdaterRemoteOperations' "$REMOTE" ||
  fail "UpdaterRemoteOperations owner missing"
grep -Fq 'if (state.running)' "$REMOTE" ||
  fail "duplicate active check guard missing"
grep -Fq 'Executors.newSingleThreadExecutor()' "$REMOTE" ||
  fail "remote check executor missing"
grep -Fq 'releases/latest/download/' "$REMOTE" ||
  fail "official GitHub Release latest-download source missing"
grep -Fq 'YTM-Importer-update.json' "$REMOTE" ||
  fail "stable update manifest asset missing"

grep -Fq 'SUPPORTED_SCHEMA = 1' "$POLICY" ||
  fail "manifest schema contract missing"
grep -Fq 'manifest.versionCode < localVersionCode' "$POLICY" ||
  fail "older-stable comparison missing"
grep -Fq 'UpdateDecision.InstalledBuildNewer' "$POLICY" ||
  fail "older stable must map to installed-build-newer state"
grep -Fq 'manifest.versionCode == localVersionCode' "$POLICY" ||
  fail "same-version path missing"
grep -Fq 'YTM-Importer-v${versionName}-release.apk' "$POLICY" ||
  fail "expected APK asset contract missing"

TEST_COUNT="$(grep -c '@Test' "$TEST" || true)"
[ "$TEST_COUNT" -ge 9 ] ||
  fail "expected at least 9 updater JVM tests, found $TEST_COUNT"

grep -Fq 'olderStableManifestMeansInstalledBuildNewer' "$TEST" ||
  fail "older-stable JVM policy test missing"

grep -Fq 'Встановлена версія новіша за поточну стабільну версію.' "$REMOTE" ||
  fail "older-stable Ukrainian result missing"

SDK_LABEL_COUNT="$(
  grep -Fc 'Цільовий SDK Android:' "$SERVICE" || true
)"

[ "$SDK_LABEL_COUNT" -eq 2 ] ||
  fail "expected two Ukrainian SDK labels, found $SDK_LABEL_COUNT"

if grep -Fq 'Android target SDK:' "$SERVICE"; then
  fail "English SDK label leaked into user UI"
fi

for leaked in   'Stable manifest is older than the installed build'   'Manifest JSON is malformed'   'Unsupported manifest schema'   'Update requires Android API'   'Missing or invalid string field'   'Missing or invalid integer field'   'Unexpected APK asset'   'Update manifest is unexpectedly large'
do
  if grep -Fq "$leaked" "$POLICY" "$REMOTE"; then
    fail "English updater user-facing text remains: $leaked"
  fi
done

if grep -Fq '"phase": "development"' "$META"; then
  META_PHASE="development"
elif grep -Fq '"phase": "final"' "$META"; then
  META_PHASE="final"
else
  fail "v1.4.49 release metadata phase must be development or final"
fi

if grep -Fq 'Intent.ACTION_VIEW' "$REMOTE"; then
  fail "Wave 1 remote owner unexpectedly launches external installer UI"
fi

if grep -Fq 'PackageInstaller' "$REMOTE"; then
  fail "Wave 1 remote owner unexpectedly contains installer implementation"
fi

echo "PASS:"
echo "- historical v1.4.49 updater contract preserved"
echo "- About -> Version -> Check update entry"
echo "- process-local updater check ownership"
echo "- duplicate active check guard"
echo "- official GitHub Release manifest source"
echo "- schema/version/asset policy"
echo "- >= 9 updater JVM tests"
echo "- release metadata phase $META_PHASE"
