#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

GRADLE="app/build.gradle.kts"
SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
POLICY="app/src/main/java/com/saney/ytmimporter/updater/UpdaterManifestPolicy.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt"
TEST="app/src/test/java/com/saney/ytmimporter/updater/UpdaterManifestPolicyTest.kt"
META="docs/v.1.4.49/RELEASE_META.json"

for file in \
  "$GRADLE" \
  "$SERVICE" \
  "$POLICY" \
  "$REMOTE" \
  "$TEST" \
  "$META"
do
  test -f "$file" || fail "missing updater Wave 1 file: $file"
done

grep -Fq 'versionCode = 92' "$GRADLE" ||
  fail "v1.4.49 versionCode missing"
grep -Fq 'versionName = "1.4.49"' "$GRADLE" ||
  fail "v1.4.49 versionName missing"

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
  fail "downgrade rejection missing"
grep -Fq 'manifest.versionCode == localVersionCode' "$POLICY" ||
  fail "same-version path missing"
grep -Fq 'YTM-Importer-v${versionName}-release.apk' "$POLICY" ||
  fail "expected APK asset contract missing"

TEST_COUNT="$(grep -c '@Test' "$TEST" || true)"
[ "$TEST_COUNT" -ge 9 ] ||
  fail "expected at least 9 updater JVM tests, found $TEST_COUNT"

grep -Fq '"phase": "development"' "$META" ||
  fail "v1.4.49 release metadata phase is not development"

if grep -Fq 'Intent.ACTION_VIEW' "$REMOTE"; then
  fail "Wave 1 remote owner unexpectedly launches external installer UI"
fi

if grep -Fq 'PackageInstaller' "$REMOTE"; then
  fail "Wave 1 remote owner unexpectedly contains installer implementation"
fi

echo "PASS:"
echo "- v1.4.49 / code 92 identity"
echo "- About -> Version -> Check update entry"
echo "- process-local updater check ownership"
echo "- duplicate active check guard"
echo "- official GitHub Release manifest source"
echo "- schema/version/asset policy"
echo "- >= 9 updater JVM tests"
echo "- release metadata phase development"
