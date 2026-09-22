#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

VERSION="$(
  sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' \
    app/build.gradle.kts |
    head -n 1
)"
FORCE_FINAL=false

for arg in "$@"; do
  case "$arg" in
    --final)
      FORCE_FINAL=true
      ;;
    *)
      VERSION="$arg"
      ;;
  esac
done

test -n "$VERSION" || fail "cannot resolve versionName"

DOC_VERSION="$VERSION"

if [ ! -d "docs/v.$DOC_VERSION" ]; then
  case "$DOC_VERSION" in
    *-R*)
      BASE_VERSION="${DOC_VERSION%%-R*}"
      if [ -d "docs/v.$BASE_VERSION" ]; then
        DOC_VERSION="$BASE_VERSION"
      fi
      ;;
  esac
fi

ROOT="docs/v.$DOC_VERSION"
QA="$ROOT/qa"
DIAGRAMS="$ROOT/diagrams"

for path in \
  "$ROOT/RELEASE.md" \
  "$ROOT/REGRESSION_CHECKLIST.md" \
  "$QA/PHONE_TEST.md" \
  "$QA/BUG_REGISTER.md" \
  "$QA/EVIDENCE_MANIFEST.md" \
  "$DIAGRAMS/README.md"
do
  test -f "$path" || fail "release documentation missing: $path"
done

find "$DIAGRAMS" \
  -maxdepth 1 \
  -type f \
  -name '*.md' \
  ! -name 'README.md' |
  grep -q . ||
  fail "release has no flow/test diagram: $DIAGRAMS"

STATUS_LINE="$(
  grep -F "| v$VERSION |" RELEASE_TEST_STATUS.md || true
)"

FINAL="$FORCE_FINAL"
if printf '%s' "$STATUS_LINE" | grep -Fq 'PHONE QA PASS'; then
  FINAL=true
fi

if [ "$FINAL" = true ]; then
  compgen -G "$QA/TEST_RUN_*.md" >/dev/null ||
    fail "final phone-tested release has no TEST_RUN"

  compgen -G "$QA/PHONE_TEST_REPORT_*.md" >/dev/null ||
    fail "final phone-tested release has no PHONE_TEST_REPORT"

  test -f "$QA/STABILIZATION_CHECKPOINT.md" ||
    fail "final phone-tested release has no stabilization checkpoint"
fi

echo "PASS:"
echo "- release docs root: $ROOT"
echo "- release QA core"
echo "- release diagrams"
echo "- final evidence gate: $FINAL"
