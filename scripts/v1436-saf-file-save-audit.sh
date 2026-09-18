#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

WRITER="app/src/main/java/com/saney/ytmimporter/storage/SafTreeFileWriter.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in   "$WRITER"   "$MANIFEST"   docs/v.1.4.36/RELEASE.md   docs/v.1.4.36/FILE_SAVE_AUDIT.md   docs/v.1.4.36/REGRESSION_CHECKLIST.md   docs/v.1.4.36/qa/PHONE_TEST.md   docs/v.1.4.36/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.36 historical file: $f"
done

grep -Fq 'versionName: **1.4.36**' docs/v.1.4.36/RELEASE.md   || fail "v1.4.36 versionName snapshot missing"
grep -Fq 'versionCode: **70**' docs/v.1.4.36/RELEASE.md   || fail "v1.4.36 versionCode snapshot missing"
grep -Fq 'four user-facing create-file workflows' docs/v.1.4.36/FILE_SAVE_AUDIT.md   || fail "v1.4.36 create-file inventory missing"
grep -Fq 'object SafTreeFileWriter' "$WRITER" || fail "SafTreeFileWriter missing"
grep -Fq 'uniqueFileName' "$WRITER" || fail "duplicate-safe filename helper missing"
grep -Fq '.deleteDocument(' "$WRITER" || fail "failed-write cleanup missing"

for permission in MANAGE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
do
  if grep -Fq "$permission" "$MANIFEST"; then
    fail "broad storage permission introduced: $permission"
  fi
done

echo "PASS:"
echo "- immutable v1.4.36 file-save snapshot present"
echo "- direct SAF writer safety invariants retained"
echo "- no broad filesystem permission"
