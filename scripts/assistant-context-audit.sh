#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

INDEX="ASSISTANT_CONTEXT_INDEX.md"
LIST="docs/assistant-kit/CONTEXT_FILES.txt"
AUDITS="docs/assistant-kit/PORTABLE_AUDITS.txt"

test -f "$INDEX" || fail "assistant context index missing"
test -f "$LIST" || fail "assistant context manifest missing"
test -f "$AUDITS" || fail "portable audit manifest missing"

count=0
while IFS= read -r path; do
  case "$path" in
    ""|\#*) continue ;;
  esac
  test -f "$path" || fail "context file missing: $path"
  count=$((count + 1))
done < "$LIST"

[ "$count" -ge 35 ] ||
  fail "assistant context manifest is unexpectedly small: $count"

while IFS= read -r path; do
  case "$path" in
    ""|\#*) continue ;;
  esac
  test -f "$path" || fail "portable audit missing: $path"
done < "$AUDITS"

grep -Fq 'ASSISTANT_CONTEXT_INDEX.md' START_HERE_ASSISTANT.md ||
  fail "START_HERE does not route through context index"

grep -Fq 'CONTEXT_FILES.txt' ASSISTANT_CONTEXT_INDEX.md ||
  fail "context index does not route through context manifest"

grep -Fq 'SYSTEM_BEHAVIOR_CONTRACT.md' ASSISTANT_CONTEXT_INDEX.md ||
  fail "system behavior contract missing from context index"

grep -Fq 'HISTORICAL_RELEASE_MATRIX.md' ASSISTANT_CONTEXT_INDEX.md ||
  fail "historical release matrix missing from context index"

grep -Fq 'docs/v.1.4.49/RELEASE_META.json' "$LIST" ||
  fail "active release metadata missing from context manifest"

grep -Fq 'docs/v.1.4.48/qa/TEST_RUN_2026-09-22.md' "$LIST" ||
  fail "accepted-release test run missing from context manifest"

grep -Fq 'Portable project skeleton' YTM_ASSISTANT_WORKFLOW.md ||
  fail "workflow does not preserve migration-kit contract"

echo "PASS:"
echo "- assistant context index"
echo "- $count mandatory context files"
echo "- accepted-release QA/evidence context"
echo "- active-release metadata/context"
echo "- historical release matrix"
echo "- portable system audit inventory"
echo "- portable system behavior contract"
echo "- migration-kit workflow contract"
