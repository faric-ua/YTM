#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

VERSION="${1:-}"

[ -n "$VERSION" ] || fail "usage: release-close-audit.sh VERSION"

bash scripts/release-documentation-audit.sh "$VERSION" --final

python -B scripts/release-metadata-audit.py \
  "$VERSION" \
  --final \
  --require-current-app

grep -Fq "## v$VERSION" CHANGELOG.md ||
  fail "CHANGELOG section missing for v$VERSION"

grep -Fq "## v$VERSION" BACKLOG.md ||
  fail "BACKLOG section missing for v$VERSION"

STATUS_LINE="$(
  grep -F "| v$VERSION |" RELEASE_TEST_STATUS.md || true
)"
[ -n "$STATUS_LINE" ] ||
  fail "RELEASE_TEST_STATUS row missing for v$VERSION"

printf '%s' "$STATUS_LINE" | grep -Fq 'PASS' ||
  fail "RELEASE_TEST_STATUS does not contain PASS for v$VERSION"

grep -Fq "versionName: **$VERSION**" CURRENT_HANDOFF.md ||
  fail "CURRENT_HANDOFF current version mismatch"

grep -Fq "Version: $VERSION" PROJECT_STATUS.txt ||
  fail "PROJECT_STATUS current version mismatch"

python -B scripts/generate-release-documentation-matrix.py --check

echo "PASS:"
echo "- final release package"
echo "- final metadata/source/run/tag"
echo "- current app identity"
echo "- CHANGELOG"
echo "- BACKLOG"
echo "- RELEASE_TEST_STATUS"
echo "- CURRENT_HANDOFF"
echo "- PROJECT_STATUS"
echo "- historical release matrix"
