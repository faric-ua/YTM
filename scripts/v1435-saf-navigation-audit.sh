#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
IMPORT="$SRC/ImportActivity.kt"
SAF="$SRC/storage/SafTreeAccess.kt"
GRADLE="app/build.gradle.kts"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in   "$IMPORT"   "$SAF"   "$GRADLE"   "$MANIFEST"   docs/v.1.4.35/RELEASE.md   docs/v.1.4.35/SAF_AUDIT.md   docs/v.1.4.35/REGRESSION_CHECKLIST.md   docs/v.1.4.35/qa/PHONE_TEST.md   docs/v.1.4.35/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.35 file: $f"
done

grep -Fq 'versionCode = 69' "$GRADLE" || fail "versionCode 69 missing"
grep -Fq 'versionName = "1.4.35"' "$GRADLE" || fail "versionName 1.4.35 missing"

grep -Fq 'object SafTreeAccess' "$SAF" || fail "SafTreeAccess missing"
grep -Fq 'persistedUriPermissions' "$SAF" || fail "persisted SAF catalog missing"
grep -Fq 'DocumentsContract.isTreeUri' "$SAF" || fail "tree URI filter missing"
grep -Fq 'permission.isReadPermission' "$SAF" || fail "read permission filter missing"
grep -Fq 'permission.isWritePermission' "$SAF" || fail "write permission filter missing"
grep -Fq 'takePersistableUriPermission' "$SAF" || fail "central persist helper missing"

TREE_COUNT="$(grep -o 'ACTION_OPEN_DOCUMENT_TREE' "$IMPORT" | wc -l | tr -d ' ')"
[ "$TREE_COUNT" -eq 1 ] || fail "expected one centralized ACTION_OPEN_DOCUMENT_TREE launcher, found $TREE_COUNT"

CHOOSER_COUNT="$(grep -o 'chooseSafTree(' "$IMPORT" | wc -l | tr -d ' ')"
[ "$CHOOSER_COUNT" -eq 8 ] || fail "expected seven chooseSafTree call sites plus definition, found $CHOOSER_COUNT"

for request in   selectiveExportFolderRequestCode   exportFolderRequestCode   incrementalBackupBaseRequestCode   incrementalBackupTargetRequestCode   deltaChainRootRequestCode   deltaChainTargetRequestCode   manifestImportFolderRequestCode
do
  grep -Fq "$request" "$IMPORT" || fail "missing SAF request path: $request"
done

grep -Fq 'Додати іншу папку…' "$IMPORT" || fail "new-folder action missing"
grep -Fq 'negativeLabel =' "$IMPORT" || fail "in-app cancel action missing"
grep -Fq '"Скасувати"' "$IMPORT" || fail "cancel label missing"

grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$IMPORT" || fail "single-file import regression path missing"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

grep -Fq '13 user-facing Android document-picker entry points' docs/v.1.4.35/SAF_AUDIT.md   || fail "picker inventory missing"
grep -Fq 'Folder-tree picker — 7' docs/v.1.4.35/SAF_AUDIT.md   || fail "tree-picker inventory missing"
grep -Fq 'Open single document — 2' docs/v.1.4.35/SAF_AUDIT.md   || fail "open-document inventory missing"
grep -Fq 'Create single document — 4' docs/v.1.4.35/SAF_AUDIT.md   || fail "create-document inventory missing"

echo "PASS:"
echo "- v1.4.35 / code 69"
echo "- seven folder flows route through one in-app remembered-root chooser"
echo "- Android system folder picker centralized to one launcher"
echo "- persisted SAF roots filtered by read/write access"
echo "- new-folder path remains available"
echo "- single-file import path retained"
echo "- no broad filesystem permission"
