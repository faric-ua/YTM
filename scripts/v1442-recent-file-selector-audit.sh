#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MANIFEST="app/src/main/AndroidManifest.xml"
CHOOSER="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
QUERY="app/src/main/java/com/saney/ytmimporter/storage/SafRecentFileQuery.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"

for f in   "$MANIFEST"   "$CHOOSER"   "$QUERY"   "$IMPORT"   "$DATA"   docs/v.1.4.42/RELEASE.md   docs/v.1.4.42/FILE_OPEN_AUDIT.md   docs/v.1.4.42/qa/PHONE_TEST.md
do
  test -f "$f" || fail "missing v1.4.42 file: $f"
done

grep -Fq 'versionCode: **80**' docs/v.1.4.42/RELEASE.md   || fail "historical v1.4.42 versionCode evidence missing"
grep -Fq 'versionName: **1.4.42**' docs/v.1.4.42/RELEASE.md   || fail "historical v1.4.42 versionName evidence missing"

grep -Fq 'android:name=".RecentFileChooserActivity"' "$MANIFEST"   || fail "RecentFileChooserActivity missing from manifest"

grep -Fq 'RecentFileChooserActivity::class.java' "$IMPORT"   || fail "Import does not route through recent-file selector"
grep -Fq 'RecentFileChooserActivity.EXTRA_ALLOWED_EXTENSIONS' "$IMPORT"   || fail "Import file-extension filter contract missing"
for ext in '"txt"' '"csv"' '"json"'
do
  grep -Fq "$ext" "$IMPORT"     || fail "Import recent-file extension missing: $ext"
done

grep -Fq 'RecentFileChooserActivity::class.java' "$DATA"   || fail "Data JSON picker does not route through recent-file selector"
grep -Fq 'arrayOf("json")' "$DATA"   || fail "Data recent-file JSON filter missing"

grep -Fq 'compareByDescending<Entry>' "$QUERY"   || fail "newest-first comparator missing"
grep -Fq 'it.lastModified' "$QUERY"   || fail "lastModified sort key missing"
grep -Fq 'buildChildDocumentsUriUsingTree' "$QUERY"   || fail "persisted-root direct child query missing"
grep -Fq 'COLUMN_LAST_MODIFIED' "$QUERY"   || fail "provider lastModified column missing"

grep -Fq 'Intent.ACTION_OPEN_DOCUMENT_TREE' "$CHOOSER"   || fail "Add-folder SAF picker missing"
grep -Fq 'SafTreeAccess.persist' "$CHOOSER"   || fail "new folder persistence missing"
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$CHOOSER"   || fail "system document fallback missing"
grep -Fq 'Системний вибір файла…' "$CHOOSER"   || fail "system-picker fallback action missing"
grep -Fq 'Скасувати' "$CHOOSER"   || fail "selector Cancel action missing"
grep -Fq 'найсвіжіші зверху' "$CHOOSER"   || fail "newest-first UI copy missing"
grep -Fq 'last modified' "$CHOOSER"   || fail "lastModified help explanation missing"

grep -Fq 'No broad storage permission is added.' docs/v.1.4.42/RELEASE.md \
  || fail "historical v1.4.42 SAF-only boundary evidence missing"

echo "PASS:"
echo "- historical v1.4.42 / code 80 evidence"
echo "- Import + Data open-file flows use RecentFileChooserActivity"
echo "- remembered SAF direct-child files sort by lastModified descending"
echo "- add-folder + system-picker fallback + cancel remain available"
echo "- txt/csv/json Import filter + json Data filter"
echo "- historical v1.4.42 SAF-only storage boundary documented"
