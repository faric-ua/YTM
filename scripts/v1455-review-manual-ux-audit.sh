#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REVIEW="$ROOT/app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
POLICY="$ROOT/app/src/main/java/com/saney/ytmimporter/review/ReviewManualPresentation.kt"
TEST="$ROOT/app/src/test/java/com/saney/ytmimporter/review/ReviewManualPresentationTest.kt"
CONTRACT="$ROOT/docs/v.1.4.55/REVIEW_MANUAL_URL_UX_CONTRACT.md"
for file in "$REVIEW" "$POLICY" "$TEST" "$CONTRACT"; do
  test -f "$file" || { echo "FAIL #40 missing $file"; exit 1; }
done
python3 - "$REVIEW" "$POLICY" "$TEST" "$CONTRACT" <<'PY_40'
from pathlib import Path
import sys
review, policy, tests, contract = [Path(x).read_text(encoding="utf-8") for x in sys.argv[1:]]
dialog = review.split('private fun showManualUrlDialog(', 1)[1].split('private fun extractVideoId(', 1)[0]
for tok in ('setSingleLine(false)', 'setHorizontallyScrolling(false)', 'minLines = 2', 'maxLines = 3',
            'TYPE_TEXT_FLAG_MULTI_LINE', 'contentDescription = "Очистити URL"',
            'setView(urlField)', 'input.setText("")'):
    if tok not in dialog:
        raise SystemExit('FAIL #40 dialog missing: ' + tok)
if dialog.count('ReviewRemoteOperations') != 1:
    raise SystemExit('FAIL #40 changed remote owner')
for tok in ('STATE_MANUAL_URL_DIALOG_OPEN', 'STATE_MANUAL_URL_DRAFT', 'STATE_MANUAL_URL_HISTORY_INDEX'):
    if tok not in review:
        raise SystemExit('FAIL #40 lost state: ' + tok)
if review.count('ReviewManualPresentation.isManualChoice(') != 2:
    raise SystemExit('FAIL #40 manual match policy not used in list and detail')
for tok in ('manualStatus.visibility =', 'row.getChildAt(2) as TextView', 'manualStatus.setTextColor(palette.accent)'):
    if tok not in review:
        raise SystemExit('FAIL #40 recycled row state missing: ' + tok)
if 'manuallySelected && !selectedTitle.isNullOrBlank()' not in policy:
    raise SystemExit('FAIL #40 manual skip semantics missing')
for tok in ('manualMatchHasDistinctVisualStatus', 'automaticMatchKeepsOrdinaryStatus',
            'manuallySkippedWithoutSelectedTitleMustNotLookSuccessful'):
    if tok not in tests:
        raise SystemExit('FAIL #40 unit test missing: ' + tok)
if 'automatically start' not in contract:
    raise SystemExit('FAIL #40 no-auto-start contract missing')
print('#40 Review manual presentation audit: PASS')
PY_40
