#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "YTM Importer release preflight"
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
check_file "docs/v.1.3.0/RELEASE.md"
check_file "docs/v.1.3.0/REGRESSION_CHECKLIST.md"

check_file "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"

check_file "app/src/main/java/com/saney/ytmimporter/DataActivity.kt"

check_file "app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"

check_file "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt"

grep -q 'android:name=".ImportActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "ImportActivity is missing from manifest"

grep -q 'android:name=".ReviewActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "ReviewActivity is missing from manifest"

grep -q 'ImportActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open ImportActivity"

grep -q 'ReviewActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open ReviewActivity"

grep -q 'EXTRA_MANUAL_VIDEO_ID' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Review manual URL result contract is missing"

grep -q 'EXTRA_REPEAT_SEARCH' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Review repeat-search result contract is missing"

grep -q 'current_playlist_v1' \
  app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt \
  || fail "Current playlist persistence is missing"

grep -q 'android:name=".PendingActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "PendingActivity is missing from manifest"

grep -q 'PendingActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open PendingActivity"

grep -q 'EXTRA_RESUME_JOB_ID' \
  app/src/main/java/com/saney/ytmimporter/PendingActivity.kt \
  || fail "PendingActivity resume result contract is missing"

grep -q 'android:name=".DataActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "DataActivity is missing from manifest"

grep -q 'DataActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open DataActivity"

grep -q 'android:name=".HistoryActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "HistoryActivity is missing from manifest"

grep -q 'HistoryActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open HistoryActivity"

grep -q 'applicationId = "com.saney.ytmimporter"' app/build.gradle.kts \
  || fail "Unexpected applicationId"

grep -q 'versionCode = 31' app/build.gradle.kts \
  || fail "Expected versionCode = 31"

grep -q 'versionName = "1.3.0"' app/build.gradle.kts \
  || fail 'Expected versionName = "1.3.0"'

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
echo "- release version"
echo "- BuildConfig"
echo "- SDK levels"
echo "- signing material not tracked"
echo "- Android SDK workflow hotfix"
echo "- Data dialog string guard"
echo "- setup-java v5"
echo "- APK signature verification step"
echo "- APK zipalign verification step"
echo "- APK SHA-256 generation"
echo "- release docs"
echo "- dedicated HistoryActivity navigation"
echo "- dedicated DataActivity navigation"
echo "- dedicated PendingActivity navigation"
echo "- Pending Queue resume result contract"
echo "- dedicated ImportActivity navigation"
echo "- dedicated ReviewActivity navigation"
echo "- persistent current playlist workspace"
echo "- Review manual URL result contract"
echo "- Review repeat-search result contract"
