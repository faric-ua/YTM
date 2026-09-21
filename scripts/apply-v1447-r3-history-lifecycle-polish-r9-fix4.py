#!/usr/bin/env python3
from pathlib import Path
import sys

FILE = Path("scripts/v1447-r3-flow-result-lifecycle-r6-audit.sh")
OLD = 'for needle in [\n    \'private var writeScroll: ScrollView? = null\',\n    \'writeScroll = scroll\',\n    \'"✓ "\',\n    \'palette.success\',\n    \'activeRow = row\',\n    \'smoothScrollTo(\',\n]:\n    if needle not in relay:\n        raise SystemExit(f"FAIL: write progress visibility contract missing: {needle}")\n'
NEW = 'for needle in [\n    \'private var writeScroll: ScrollView? = null\',\n    \'writeScroll = scroll\',\n    \'palette.success\',\n    \'activeRow = row\',\n    \'smoothScrollTo(\',\n]:\n    if needle not in relay:\n        raise SystemExit(f"FAIL: write progress visibility contract missing: {needle}")\n\nsuccess_marker_ok = (\n    \'"✓ "\' in relay or\n    (\n        \'val semanticIcon =\' in relay and\n        \'"✓"\' in relay and\n        \'semanticIconColor\' in relay\n    )\n)\n\nif not success_marker_ok:\n    raise SystemExit(\n        \'FAIL: write progress visibility contract missing: \'\n        \'historical "✓ " or R9 semantic success icon\'\n    )\n'

def main():
    check = "--check" in sys.argv[1:]

    if not FILE.exists():
        raise SystemExit(f"FAIL: missing {FILE}")

    text = FILE.read_text(encoding="utf-8")
    old_count = text.count(OLD)
    new_count = text.count(NEW)

    if new_count == 1:
        print("SKIP: R9 FIX4 already applied")
        print("PASS: R6 audit accepts historical + R9 success-marker UI")
        return

    if old_count != 1:
        raise SystemExit(
            f"FAIL: R9 FIX4 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: make R6 write-progress audit successor-compatible")

    if not check:
        FILE.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: R6 write-progress audit successor compatibility")

    print("PASS: R9 FIX4 ready" if check else "PASS: R9 FIX4 applied")

if __name__ == "__main__":
    main()
