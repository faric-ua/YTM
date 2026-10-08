#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REVIEW="$ROOT/app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
POLICY="$ROOT/app/src/main/java/com/saney/ytmimporter/review/ReviewManualPresentation.kt"
TEST="$ROOT/app/src/test/java/com/saney/ytmimporter/review/ReviewManualPresentationTest.kt"
CONTRACT="$ROOT/docs/v.1.4.55/REVIEW_MANUAL_FILTER_CONTRACT.md"
for path in "$REVIEW" "$POLICY" "$TEST" "$CONTRACT"; do
  test -f "$path" || { echo "FAIL #58: missing $path" >&2; exit 1; }
done
python3 - "$REVIEW" "$POLICY" "$TEST" "$CONTRACT" <<'PY_CHECK_58'
from pathlib import Path
import re
import sys
review, policy, tests, contract = [Path(p).read_text(encoding="utf-8") for p in sys.argv[1:]]

for text in (
    'ReviewFilter.MANUAL',
    'MANUAL\n',
    'compactFilterButton("✓ Ручні ($manualChoiceCount)")',
    'val manualChoiceCount =',
    'val manualFilterRow =',
    'root.addView(filterRow)',
    'root.addView(manualFilterRow)',
    'adapter.setFilter(ReviewFilter.MANUAL)',
    'STATE_REVIEW_FILTER',
    'ReviewFilter.valueOf(it)',
    'STATE_LIST_FIRST_POSITION',
):
    if text not in review:
        raise SystemExit("FAIL #58: missing manual filter / state ownership: " + text)
if review.count('ReviewManualPresentation.isManualChoice(') != 4:
    raise SystemExit('FAIL #58: the same predicate must drive badge, detail, count and filter')
if not re.search(r'ReviewFilter\.MANUAL\s*->\s*ReviewManualPresentation\.isManualChoice\(', review):
    raise SystemExit('FAIL #58: manual filter branch must reuse manual selection policy')
if not re.search(r'snapshot\.playlist\.tracks\.count\s*\{\s*track\s*->\s*ReviewManualPresentation\.isManualChoice\(', review):
    raise SystemExit('FAIL #58: visible count must use same policy as filter')
for old_label in ('≡ Усі', '! Перев.', '✓ Готові', '× Пробл.'):
    if old_label not in review:
        raise SystemExit('FAIL #58: old filter missing ' + old_label)
if 'manuallySelected && !selectedTitle.isNullOrBlank()' not in policy:
    raise SystemExit('FAIL #58: no skip/automatic protection')
if 'manualFilterCountExcludesAutomaticAndSkipped' not in tests:
    raise SystemExit('FAIL #58: JVM filter count regression missing')
if 'no remote' not in contract.lower():
    raise SystemExit('FAIL #58: no-remote policy missing')
print('#58 Review manual-only filter source audit: PASS')
PY_CHECK_58
