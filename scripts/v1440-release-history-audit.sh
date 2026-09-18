#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
SERVICE="app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
CHANGELOG="CHANGELOG.md"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in   "$GRADLE"   "$SERVICE"   "$CHANGELOG"   "$MANIFEST"   docs/v.1.4.40/RELEASE.md   docs/v.1.4.40/UI_AUDIT.md   docs/v.1.4.40/REGRESSION_CHECKLIST.md   docs/v.1.4.40/qa/PHONE_TEST.md   docs/v.1.4.40/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.40 file: $f"
done

grep -Fq 'versionCode = 76' "$GRADLE" || fail "versionCode 76 missing"
grep -Fq 'versionName = "1.4.40"' "$GRADLE" || fail "versionName 1.4.40 missing"

grep -Fq 'generatedChangelogAssetsDir' "$GRADLE"   || fail "generated changelog asset directory missing"
grep -Fq 'rootProject.file(' "$GRADLE"   || fail "root changelog source guard missing"
grep -Fq '"CHANGELOG.md"' "$GRADLE"   || fail "root CHANGELOG.md is not copied into assets"
grep -Fq 'assets' "$GRADLE"   || fail "generated assets source set missing"
grep -Fq 'generateChangelogAsset' "$GRADLE"   || fail "changelog asset task missing"
grep -Fq 'dependsOn(' "$GRADLE"   || fail "preBuild changelog dependency missing"

grep -Fq 'Page.CHANGELOG -> buildChangelog()' "$SERVICE"   || fail "changelog page route missing"
grep -Fq '"Історія змін"' "$SERVICE"   || fail "release-history UI label missing"
grep -Fq '"Що змінювалося у кожному релізі"' "$SERVICE"   || fail "release-history subtitle missing"
grep -Fq 'private fun buildChangelog()' "$SERVICE"   || fail "release-history screen missing"
grep -Fq 'private fun loadReleaseHistory()' "$SERVICE"   || fail "release-history loader missing"
grep -Fq 'assets' "$SERVICE"   || fail "runtime asset read missing"
grep -Fq 'CHANGELOG_ASSET' "$SERVICE"   || fail "runtime changelog asset constant missing"
grep -Fq 'line.startsWith(' "$SERVICE"   || fail "release-section parser missing"
grep -Fq '"## "' "$SERVICE"   || fail "release heading parser missing"
grep -Fq '"• "' "$SERVICE"   || fail "bullet rendering missing"
grep -Fq 'cleanReleaseMarkdown' "$SERVICE"   || fail "lightweight markdown cleanup missing"

grep -Fq 'Page.CHANGELOG -> {' "$SERVICE"   || fail "release-history Back parent missing"
grep -Fq 'page = Page.ABOUT' "$SERVICE"   || fail "Back from release history does not return to About"

grep -Fq '## v1.4.40' "$CHANGELOG"   || fail "v1.4.40 changelog entry missing"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

echo "PASS:"
echo "- v1.4.40 / code 76"
echo "- About page exposes in-app release history"
echo "- root CHANGELOG.md is embedded automatically at build time"
echo "- release sections render as cards from one source of truth"
echo "- Back from History returns to About"
echo "- no broad filesystem permission"
