#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("scripts/v1447-r1-audit.sh")

OLD = """grep -Fq '"Швидкі дії"' "$MAIN" || fail "Home quick-actions section missing"
grep -Fq '"Поточний плейлист"' "$MAIN" || fail "Home current-playlist section missing"
"""

NEW = """if grep -Fq '"Швидкі дії файл/плейлист"' "$MAIN"; then
  python - "$MAIN" <<'PY'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text(encoding="utf-8")
start = main.find('title = "Швидкі дії файл/плейлист"')
end = main.find('quickSection.addView(quickRow)', start)

if start < 0 or end < 0:
    raise SystemExit("FAIL: R4 quick-actions block boundary missing")

block = main[start:end]

for needle in [
    'label =\\n                        "Імпорт"',
    'label =\\n                        "Експорт"',
    'dp(48)',
]:
    if needle not in block:
        raise SystemExit(
            f"FAIL: R4 Home quick-actions successor contract missing: {needle}"
        )
PY
else
  grep -Fq '"Швидкі дії"' "$MAIN" ||
    fail "Home quick-actions section missing"
fi
grep -Fq '"Поточний плейлист"' "$MAIN" || fail "Home current-playlist section missing"
"""

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not TARGET.exists():
        raise SystemExit(f"FAIL: missing {TARGET}")

    text = TARGET.read_text(encoding="utf-8")
    old_count = text.count(OLD)
    new_count = text.count(NEW)

    if new_count == 1 and old_count == 0:
        print("SKIP: historical R1 audit already recognizes R4 quick-actions successor")
        return

    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"FAIL: R4 FIX1 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: teach historical R1 audit the documented R4 quick-actions successor")
    if not args.check:
        TARGET.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: R1 audit accepts only old contract or exact R4 successor contract")

if __name__ == "__main__":
    main()
