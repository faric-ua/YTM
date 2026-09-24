#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
URL="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
CLEAR_ICON="app/src/main/res/drawable/ic_ytm_clear.xml"

for file in "$MAIN" "$URL" "$REVIEW" "$CLEAR_ICON"; do
  test -f "$file" || fail "missing v1.4.51 phone-finding corrective file: $file"
done

python - "$MAIN" "$URL" "$REVIEW" <<'PY'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text(encoding="utf-8")
url = Path(sys.argv[2]).read_text(encoding="utf-8")
review = Path(sys.argv[3]).read_text(encoding="utf-8")

quick_start = main.index('label =\n                        "Імпорт"')
quick_end = main.index('quickSection.addView(quickRow)', quick_start)
quick = main[quick_start:quick_end]

if quick.count("openImportScreen()") != 1:
    raise SystemExit("FAIL: Home quick actions must have exactly one Import callback")

if 'label =\n                        "Експорт"' not in quick:
    raise SystemExit("FAIL: Home quick Export action missing")

export_pos = quick.index('"Експорт"')
export_tail = quick[export_pos:]
if "openReviewScreen(" not in export_tail:
    raise SystemExit("FAIL: Home quick Export does not route through Review")
if "openReviewScreen(openProjectActions = true)" not in export_tail:
    raise SystemExit("FAIL: Home quick Export does not request project actions")

for needle in (
    "openProjectActions: Boolean = false",
    "EXTRA_OPEN_PROJECT_ACTIONS",
):
    if needle not in main:
        raise SystemExit("FAIL: Main export bridge missing: " + needle)

if "EXTRA_OPEN_PROJECT_ACTIONS" not in review:
    raise SystemExit("FAIL: existing Review project-action contract missing")

for needle in (
    "setHorizontallyScrolling(",
    "TYPE_TEXT_FLAG_MULTI_LINE",
    "minLines =\n                            2",
    "maxLines =\n                            3",
):
    if needle not in url:
        raise SystemExit("FAIL: URL multiline editor missing: " + needle)

for needle in (
    "FrameLayout(",
    "ImageButton(",
    '"Очистити URL"',
    "R.drawable.ic_ytm_clear",
    "dp(52)",
):
    if needle not in url:
        raise SystemExit("FAIL: URL clear-control contract missing: " + needle)

print("PASS: Home Import remains Import")
print("PASS: Home Export reuses Review YTM Project/export actions")
print("PASS: URL input is width-wrapping 2-3 line multiline editor")
print("PASS: URL input has right-center clear control with protected text padding")
PY
