#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SAF="app/src/main/java/com/saney/ytmimporter/storage/SafTreeAccess.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in   "$SAF"   "$MANIFEST"   docs/v.1.4.35/RELEASE.md   docs/v.1.4.35/SAF_AUDIT.md   docs/v.1.4.35/REGRESSION_CHECKLIST.md   docs/v.1.4.35/qa/PHONE_TEST.md   docs/v.1.4.35/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.35 historical file: $f"
done

grep -Fq 'versionName: **1.4.35**' docs/v.1.4.35/RELEASE.md   || fail "v1.4.35 versionName snapshot missing"
grep -Fq 'versionCode: **69**' docs/v.1.4.35/RELEASE.md   || fail "v1.4.35 versionCode snapshot missing"

grep -Fq '13 user-facing Android document-picker entry points' docs/v.1.4.35/SAF_AUDIT.md   || fail "v1.4.35 picker inventory missing"
grep -Fq 'Folder-tree picker — 7' docs/v.1.4.35/SAF_AUDIT.md   || fail "v1.4.35 tree-picker inventory missing"
grep -Fq 'Open single document — 2' docs/v.1.4.35/SAF_AUDIT.md   || fail "v1.4.35 open-document inventory missing"
grep -Fq 'Create single document — 4' docs/v.1.4.35/SAF_AUDIT.md   || fail "v1.4.35 create-document inventory missing"

grep -Fq 'object SafTreeAccess' "$SAF" || fail "SafTreeAccess missing"
grep -Fq 'persistedUriPermissions' "$SAF" || fail "persisted SAF catalog missing"
grep -Fq 'takePersistableUriPermission' "$SAF" || fail "persistable SAF helper missing"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

echo "PASS:"
echo "- immutable v1.4.35 SAF release snapshot present"
echo "- persisted SAF permission foundation still present"
echo "- no broad filesystem permission"
