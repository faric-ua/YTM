#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt"
POLICY="app/src/main/java/com/saney/ytmimporter/updater/UpdaterDownloadPolicy.kt"
TEST="app/src/test/java/com/saney/ytmimporter/updater/UpdaterDownloadPolicyTest.kt"
ROADMAP="docs/roadmap/UPDATER.md"
META="docs/v.1.4.49/RELEASE_META.json"

for file in \
  "$SERVICE" \
  "$REMOTE" \
  "$POLICY" \
  "$TEST" \
  "$ROADMAP" \
  "$META"
do
  test -f "$file" || fail "missing updater Wave 2 file: $file"
done

for needle in \
  'DOWNLOADING' \
  'VERIFYING' \
  'READY_TO_INSTALL' \
  'fun startDownload(' \
  'context.filesDir' \
  'UpdaterDownloadPolicy.assetUrl(' \
  'UpdaterDownloadPolicy.verifySha256(' \
  'manifest.apkAsset + PART_SUFFIX' \
  'StandardCopyOption.REPLACE_EXISTING'
do
  grep -Fq "$needle" "$REMOTE" ||
    fail "Wave 2 remote contract missing: $needle"
done

grep -Fq 'phase == Phase.DOWNLOADING' "$REMOTE" ||
  fail "download running-state ownership missing"
grep -Fq 'phase == Phase.VERIFYING' "$REMOTE" ||
  fail "verification running-state ownership missing"
grep -Fq 'snapshot.running' "$REMOTE" ||
  fail "duplicate active download guard missing"

grep -Fq 'MAX_APK_BYTES' "$POLICY" ||
  fail "APK size cap missing"
grep -Fq 'MessageDigest.getInstance("SHA-256")' "$POLICY" ||
  fail "downloaded-file SHA-256 implementation missing"
grep -Fq 'releases/download/' "$POLICY" ||
  fail "exact tagged release asset URL missing"

TEST_COUNT="$(grep -c '@Test' "$TEST" || true)"
[ "$TEST_COUNT" -ge 5 ] ||
  fail "expected at least 5 Wave 2 JVM tests, found $TEST_COUNT"

grep -Fq '"Завантажити APK"' "$SERVICE" ||
  fail "explicit APK download action missing"
grep -Fq '"Перевірка SHA-256"' "$SERVICE" ||
  fail "verification UI state missing"
grep -Fq '"APK перевірено"' "$SERVICE" ||
  fail "verified APK UI state missing"
grep -Fq 'UpdaterRemoteOperations.startDownload(' "$SERVICE" ||
  fail "Service does not route Download through operation owner"

if grep -Eq 'Intent\.ACTION_VIEW|PackageInstaller|FileProvider\.getUriForFile' "$REMOTE"; then
  fail "Wave 2 updater owner unexpectedly contains installer launch implementation"
fi

if grep -Fq 'UpdaterRemoteOperations.startInstall(' "$SERVICE"; then
  fail "Wave 2 Version UI unexpectedly exposes installer execution"
fi

grep -Fq 'Wave 2' "$ROADMAP" ||
  fail "Wave 2 roadmap status missing"
if grep -Fq '"phase": "development"' "$META"; then
  grep -Fq 'WAVE 2 DOWNLOAD/SHA IMPLEMENTED' "$META" ||
    fail "Wave 2 development metadata status missing"
elif grep -Fq '"phase": "final"' "$META"; then
  grep -Fq 'PASS' "$META" ||
    fail "Wave 2 final metadata does not preserve release PASS evidence"
else
  fail "Wave 2 release metadata phase must be development or final"
fi

echo "PASS:"
echo "- explicit APK download action"
echo "- process-owned Downloading/Verifying state"
echo "- duplicate active download guard"
echo "- app-private .part -> verified APK promotion"
echo "- exact tagged GitHub Release asset URL"
echo "- downloaded-file SHA-256 verification"
echo "- >= 5 Wave 2 JVM tests"
echo "- installer launch remains out of Wave 2"
