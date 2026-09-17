#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
MANIFEST="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryManifestImporter.kt"
CODEC="app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt"

for f in \
  "$IMPORT" \
  "$MANIFEST" \
  "$CODEC" \
  docs/v.1.4.28/RELEASE.md \
  docs/v.1.4.28/REGRESSION_CHECKLIST.md \
  docs/v.1.4.28/qa/PHONE_TEST.md \
  docs/v.1.4.28/qa/BUG_REGISTER.md \
  docs/v.1.4.28/diagrams/MANIFEST_IMPORT_FLOW.md \
  docs/tutorial/08_IMPORT_AND_PROJECT_FORMAT.md
do
  test -f "$f" || fail "missing v1.4.28 manifest-import file: $f"
done

grep -Fq 'private val manifestImportFolderRequestCode' "$IMPORT" \
  || fail "manifest folder request code missing"
grep -Fq '2404' "$IMPORT" \
  || fail "manifest folder request code value missing"
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT_TREE' "$IMPORT" \
  || fail "manifest folder picker must use ACTION_OPEN_DOCUMENT_TREE"
grep -Fq 'FLAG_GRANT_PERSISTABLE_URI_PERMISSION' "$IMPORT" \
  || fail "persistable folder permission missing"
grep -Fq 'Відкрити backup / manifest.json' "$IMPORT" \
  || fail "manifest-import UI action missing"
grep -Fq 'AccountLibraryManifestImporter' "$IMPORT" \
  || fail "manifest importer dependency missing from ImportActivity"
grep -Fq '.readManifest(' "$IMPORT" \
  || fail "ImportActivity does not read manifest"
grep -Fq '.loadProject(' "$IMPORT" \
  || fail "ImportActivity does not load selected manifest project"
grep -Fq 'manifest.entries.map' "$IMPORT" \
  || fail "manifest project catalog UI missing"

grep -Fq '"ytm-importer-account-library-export"' "$MANIFEST" \
  || fail "manifest format guard missing"
grep -Fq 'schemaVersion in 1..MAX_SCHEMA_VERSION' "$MANIFEST" \
  || fail "manifest schema compatibility guard missing"
grep -Fq '"selectionMode"' "$MANIFEST" \
  || fail "selectionMode support missing"
grep -Fq '"SELECTED"' "$MANIFEST" \
  || fail "schema-v2 SELECTED mode support missing"
grep -Fq 'Непідтримуваний selectionMode' "$MANIFEST" \
  || fail "selectionMode validation missing"
grep -Fq '"playlistCount"' "$MANIFEST" \
  || fail "playlistCount validation missing"
grep -Fq '"exportedProjects"' "$MANIFEST" \
  || fail "exportedProjects validation missing"
grep -Fq '"EXPORTED"' "$MANIFEST" \
  || fail "EXPORTED record filter missing"
grep -Fq 'buildChildDocumentsUriUsingTree' "$MANIFEST" \
  || fail "tree-folder child resolution missing"
grep -Fq 'PlaylistProjectCodec.importProject' "$MANIFEST" \
  || fail "YTM Project codec reuse missing"
grep -Fq 'project.sourcePlaylistId' "$MANIFEST" \
  || fail "playlistId cross-check missing"
grep -Fq 'project.sourcePrivacyStatus' "$MANIFEST" \
  || fail "privacyStatus cross-check missing"

if grep -Fq 'YouTubeApi' "$MANIFEST"; then
  fail "manifest importer must not depend on YouTubeApi"
fi
if grep -Fq 'search.list' "$MANIFEST"; then
  fail "manifest importer must not perform search.list work"
fi
if grep -Fq 'playlistItems.list' "$MANIFEST"; then
  fail "manifest importer must not perform playlistItems.list work"
fi

grep -Fq 'val sourcePrivacyStatus: String?' "$CODEC" \
  || fail "PlaylistProjectImport privacy metadata missing"
grep -Fq 'sourcePrivacyStatus =' "$CODEC" \
  || fail "PlaylistProjectCodec does not restore privacy metadata"

grep -Fq 'versionCode: **62**' docs/v.1.4.28/RELEASE.md \
  || fail "v1.4.28 release versionCode missing"
grep -Fq 'versionName: **1.4.28**' docs/v.1.4.28/RELEASE.md \
  || fail "v1.4.28 release versionName missing"
grep -Fq '**NOT PHONE-TESTED YET**' docs/v.1.4.28/RELEASE.md \
  || fail "v1.4.28 initial phone status missing"

echo "PASS:"
echo "- manifest folder picker"
echo "- schema v1/v2 account-export manifest parsing"
echo "- manifest count/file validation"
echo "- one-project catalog selection"
echo "- YTM Project codec reuse"
echo "- playlistId/privacy integrity checks"
echo "- local-only / zero YouTube API manifest path"
echo "- v1.4.28 release docs"
