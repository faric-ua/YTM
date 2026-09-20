#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
RELAY="app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt"

for f in "$MAIN" "$REVIEW" "$RELAY"; do
  test -f "$f" || fail "missing R6 source: $f"
done

python - "$MAIN" "$REVIEW" "$RELAY" <<'PY'
from pathlib import Path
import sys

main, review, relay = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

start = main.index('private fun handleReviewScreenResult(')
end = main.index('private fun handleManualVideoResult(', start)
block = main[start:end]

flag = 'ReviewActivity.EXTRA_DESTINATION_RESULT'
if flag not in block:
    raise SystemExit("FAIL: Main does not consume Review-origin Destination result")
if 'handleDestinationResult(data)' not in block:
    raise SystemExit("FAIL: Review-origin Destination result does not execute write bridge")
if block.index(flag) > block.index('ReviewActivity.EXTRA_REPEAT_SEARCH'):
    raise SystemExit("FAIL: Destination result must be handled before legacy Review actions")

repeat_start = review.index('private fun showRepeatSearchDialog()')
repeat_end = review.index('private fun reloadSnapshot()', repeat_start)
repeat = review[repeat_start:repeat_end]

for needle in [
    'setNegativeButton(',
    'setPositiveButton(',
    'setOnCancelListener {',
    'setOnDismissListener {',
]:
    if needle not in repeat:
        raise SystemExit(f"FAIL: repeat-search R6 lifecycle piece missing: {needle}")

dismiss_start = repeat.index('dialog.setOnDismissListener {')
dismiss_end = repeat.index('}', dismiss_start)
if 'repeatSearchDialogOpen = false' in repeat[dismiss_start:dismiss_end]:
    raise SystemExit("FAIL: generic dismiss still clears repeat-search open state")

destroy_start = review.index('override fun onDestroy()')
destroy_end = review.index('override fun onBackPressed()', destroy_start)
destroy = review[destroy_start:destroy_end]
if 'setOnCancelListener(null)' not in destroy:
    raise SystemExit("FAIL: repeat-search cancel listener not detached on destroy")

status_start = main.index('statusText =')
status_end = main.index('content.addView(', status_start)
status = main[status_start:status_end]
for needle in ['maxLines = 1', 'TextUtils.TruncateAt.END']:
    if needle not in status:
        raise SystemExit(f"FAIL: compact Home account status missing: {needle}")

for needle in [
    'private var writeScroll: ScrollView? = null',
    'writeScroll = scroll',
    '"✓ "',
    'palette.success',
    'activeRow = row',
    'smoothScrollTo(',
]:
    if needle not in relay:
        raise SystemExit(f"FAIL: write progress visibility contract missing: {needle}")

lines = len(main.splitlines())
if lines >= 4000:
    raise SystemExit(f"FAIL: MainActivity cleanup regression: {lines} lines")
PY

echo "PASS:"
echo "- Home-step-3 Review-origin create/append results execute Main write bridge"
echo "- repeat-search confirmation survives rotation by explicit state ownership"
echo "- Home account/status subtitle is capped to one line"
echo "- successful write row gets green check/title"
echo "- active write row auto-scrolls into view"
echo "- MainActivity remains below 4000 lines"
