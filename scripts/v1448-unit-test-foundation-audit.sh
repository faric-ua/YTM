#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

POLICY="app/src/main/java/com/saney/ytmimporter/youtube/PlaylistEditPolicy.kt"
TEST="app/src/test/java/com/saney/ytmimporter/youtube/PlaylistEditPolicyTest.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/destination/DestinationRemoteOperations.kt"
GRADLE="app/build.gradle.kts"
WORKFLOW=".github/workflows/build-apk.yml"
DOC="docs/testing/AUTOMATED_TEST_STRATEGY.md"

for f in \
  "$POLICY" \
  "$TEST" \
  "$API" \
  "$REMOTE" \
  "$GRADLE" \
  "$WORKFLOW" \
  "$DOC"
do
  test -f "$f" ||
    fail "missing test-foundation file: $f"
done

grep -Fq 'testImplementation("junit:junit:4.13.2")' "$GRADLE" ||
  fail "JUnit dependency missing"

grep -Fq 'object PlaylistEditPolicy' "$POLICY" ||
  fail "PlaylistEditPolicy missing"

grep -Fq 'fun buildSpec(' "$POLICY" ||
  fail "testable update-spec builder missing"

grep -Fq 'PlaylistEditPolicy' "$API" ||
  fail "YouTubeApi does not consume tested policy"

grep -Fq 'PlaylistEditPolicy' "$REMOTE" ||
  fail "Destination update does not consume tested policy"

TEST_COUNT="$(
  grep -c '@Test' "$TEST" || true
)"

[ "$TEST_COUNT" -ge 6 ] ||
  fail "expected at least 6 JUnit tests, found $TEST_COUNT"

grep -Fq 'gradle :app:testDebugUnitTest' "$WORKFLOW" ||
  fail "signed-build unit-test gate missing"

grep -Fq 'A failed unit test blocks the signed APK.' "$DOC" ||
  fail "automated-test project rule missing"

echo "PASS:"
echo "- real JVM/JUnit test source exists"
echo "- production API uses tested PlaylistEditPolicy"
echo "- Destination update uses tested normalization"
echo "- >= 6 unit tests"
echo "- signed build is blocked when unit tests fail"
echo "- automated test strategy documented"
