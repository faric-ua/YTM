#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
MANIFEST="app/src/main/AndroidManifest.xml"
PATHS="app/src/main/res/xml/file_paths.xml"
CHOOSER="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
ACCESS="app/src/main/java/com/saney/ytmimporter/storage/AllFilesAccess.kt"
DIRECT="app/src/main/java/com/saney/ytmimporter/storage/DirectDownloadFileQuery.kt"
R1="docs/v.1.4.42/R1.md"

for f in "$GRADLE" "$MANIFEST" "$PATHS" "$CHOOSER" "$ACCESS" "$DIRECT" "$R1"; do
  test -f "$f" || fail "missing v1.4.42-R1 file: $f"
done

grep -Fq 'versionCode = 81' "$GRADLE"   || fail "R1 versionCode 81 missing"
grep -Fq 'versionName = "1.4.42-R1"' "$GRADLE"   || fail "R1 versionName missing"

grep -Fq 'android.permission.MANAGE_EXTERNAL_STORAGE' "$MANIFEST"   || fail "MANAGE_EXTERNAL_STORAGE missing"

grep -Fq 'name="shared_download"' "$PATHS"   || fail "FileProvider Download path missing"
grep -Fq 'path="Download/"' "$PATHS"   || fail "FileProvider Download directory missing"

grep -Fq 'isExternalStorageManager()' "$ACCESS"   || fail "all-files grant check missing"
grep -Fq 'ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION' "$ACCESS"   || fail "per-app all-files settings intent missing"
grep -Fq 'ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION' "$ACCESS"   || fail "global all-files settings fallback missing"

grep -Fq 'DIRECTORY_DOWNLOADS' "$DIRECT"   || fail "direct Download path missing"
grep -Fq 'getExternalStoragePublicDirectory(' "$DIRECT"   || fail "direct shared-storage query missing"
grep -Fq 'compareByDescending<File>' "$DIRECT"   || fail "direct Download newest-first sort missing"
grep -Fq 'it.lastModified()' "$DIRECT"   || fail "direct Download lastModified key missing"
grep -Fq 'FileProvider' "$DIRECT"   || fail "direct Download FileProvider URI bridge missing"

grep -Fq 'Надати доступ до всіх файлів' "$CHOOSER"   || fail "all-files access action missing"
grep -Fq 'explainAndRequestAllFilesAccess' "$CHOOSER"   || fail "all-files rationale path missing"
grep -Fq 'DirectDownloadFileQuery.list' "$CHOOSER"   || fail "direct Download query not wired into selector"
grep -Fq 'Додати SAF-папку…' "$CHOOSER"   || fail "SAF folder fallback missing"
grep -Fq 'Системний вибір файла…' "$CHOOSER"   || fail "system picker fallback missing"

echo "PASS:"
echo "- v1.4.42-R1 / code 81"
echo "- MANAGE_EXTERNAL_STORAGE declared"
echo "- explicit rationale + Android special-access settings flow"
echo "- direct Download newest-first listing"
echo "- FileProvider bridge for direct Download file selection"
echo "- SAF and system picker fallbacks retained"
