#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("scripts/v1447-r3-navigation-ownership-r4-fix2-audit.sh")

OLD = """if ".orEmpty()" in plan:
    raise SystemExit(
        "FAIL: forwarded primitive IntArray still uses unsupported nullable orEmpty()"
    )
if "?: IntArray(0)" not in plan:
    raise SystemExit(
        "FAIL: forwarded skip positions do not use explicit IntArray fallback"
    )
if "index in" not in plan or "index !in" not in plan:
    raise SystemExit(
        "FAIL: forwarded duplicate include/exclude contracts missing"
    )
"""

NEW = """skip_start = plan.index("        val skippedPositions =")
skip_end = plan.index("        val tracksToSkip =", skip_start)
skip_block = plan[skip_start:skip_end]

if ".orEmpty()" in skip_block:
    raise SystemExit(
        "FAIL: forwarded primitive IntArray still uses unsupported nullable orEmpty()"
    )
if "?: IntArray(0)" not in skip_block:
    raise SystemExit(
        "FAIL: forwarded skip positions do not use explicit IntArray fallback"
    )

if "index in" not in plan or "index !in" not in plan:
    raise SystemExit(
        "FAIL: forwarded duplicate include/exclude contracts missing"
    )
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
        print("SKIP: FIX2B audit scope already corrected")
        return

    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"FAIL: FIX2B anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: scope primitive-array check to skippedPositions block")
    if not args.check:
        TARGET.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: String?.orEmpty() is allowed; IntArray block remains strict")

if __name__ == "__main__":
    main()
