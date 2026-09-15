#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "YTM Importer RC preflight"
echo "========================="

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

check_file() {
  [ -f "$1" ] || fail "Missing required file: $1"
}

check_file "app/build.gradle.kts"
check_file "app/src/main/AndroidManifest.xml"
check_file ".github/workflows/build-apk.yml"
check_file "docs/v.1.0.0/RELEASE.md"
check_file "docs/v.1.0.0/REGRESSION_CHECKLIST.md"

grep -q 'applicationId = "com.saney.ytmimporter"' app/build.gradle.kts \
  || fail "Unexpected applicationId"

grep -q 'versionCode = 26' app/build.gradle.kts \
  || fail "Expected versionCode = 26"

grep -q 'versionName = "1.0.0"' app/build.gradle.kts \
  || fail 'Expected versionName = "1.0.0"'

grep -q 'buildConfig = true' app/build.gradle.kts \
  || fail "BuildConfig generation is not enabled"

grep -q 'compileSdk = 36' app/build.gradle.kts \
  || fail "Expected compileSdk = 36"

grep -q 'targetSdk = 36' app/build.gradle.kts \
  || fail "Expected targetSdk = 36"

tracked_secrets="$(
  git ls-files |
    grep -E '(^|/)(release-signing\.properties|[^/]+\.(jks|keystore))$' ||
    true
)"

if [ -n "$tracked_secrets" ]; then
  echo "$tracked_secrets"
  fail "Signing material is tracked by Git"
fi

if grep -q 'uses:[[:space:]]*android-actions/setup-android@v3' \
  .github/workflows/build-apk.yml; then
  fail "Obsolete android-actions/setup-android@v3 is active again"
fi

grep -q 'Locate Android SDK' .github/workflows/build-apk.yml \
  || fail "Android SDK locator hotfix is missing"


grep -Fq '"«Повний backup», а не History JSON.\n" +' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "Data dialog string guard failed"

grep -q 'uses:[[:space:]]*actions/setup-java@v5' \
  .github/workflows/build-apk.yml \
  || fail "Expected actions/setup-java@v5"

grep -q 'name:[[:space:]]*Verify signed APK' \
  .github/workflows/build-apk.yml \
  || fail "APK verification step is missing"

grep -q 'verify --verbose --print-certs' \
  .github/workflows/build-apk.yml \
  || fail "apksigner verification command is missing"

grep -q 'ZIPALIGN.*-c -v 4' \
  .github/workflows/build-apk.yml \
  || grep -q '"$ZIPALIGN" -c -v 4 "$APK"' \
  .github/workflows/build-apk.yml \
  || fail "zipalign verification is missing"

grep -q 'sha256sum' \
  .github/workflows/build-apk.yml \
  || fail "APK SHA-256 generation is missing"

echo
echo "PASS:"
echo "- package id"
echo "- RC version"
echo "- BuildConfig"
echo "- SDK levels"
echo "- signing material not tracked"
echo "- Android SDK workflow hotfix"
echo "- Data dialog string guard"
echo "- setup-java v5"
echo "- APK signature verification step"
echo "- APK zipalign verification step"
echo "- APK SHA-256 generation"
echo "- RC docs"
