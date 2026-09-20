#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("scripts/v1447-r2-audit.sh")

OLD = r'''required_menu = [
    'private fun showThemePicker()',
    'if (action == ACTION_THEME)',
    'showThemePicker()',
    'AppThemeManager\n                                        .setStyle(',
    'recreate()',
]
for needle in required_menu:
    if needle not in menu:
        raise SystemExit(f"FAIL: Menu-owned theme contract missing: {needle}")
'''

NEW = r'''required_menu = [
    'private fun showThemePicker()',
    'if (action == ACTION_THEME)',
    'showThemePicker()',
    'recreate()',
]
for needle in required_menu:
    if needle not in menu:
        raise SystemExit(f"FAIL: Menu-owned theme contract missing: {needle}")

# R3 lifecycle state adds one nesting level around the Theme dialog body.
# Verify the semantic setStyle contract inside showThemePicker instead of
# coupling this historical R2 audit to a specific indentation width.
theme_start = menu.find('private fun showThemePicker()')
theme_end = menu.find('private fun addAction(', theme_start)
if theme_start < 0 or theme_end < 0:
    raise SystemExit("FAIL: Menu Theme picker block boundary missing")

theme_block = menu[theme_start:theme_end]
for needle in [
    'AppThemeManager',
    '.setStyle(',
]:
    if needle not in theme_block:
        raise SystemExit(f"FAIL: Menu-owned theme contract missing in Theme picker: {needle}")
'''


def fail(message: str) -> None:
    raise SystemExit(f"FAIL: {message}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not TARGET.is_file():
        fail(f"missing target: {TARGET}")

    text = TARGET.read_text(encoding="utf-8")
    old_count = text.count(OLD)
    new_count = text.count(NEW)

    if new_count == 1 and old_count == 0:
        print("SKIP R2 Theme audit indentation decoupling")
        return

    if old_count != 1 or new_count != 0:
        fail(
            "anchor mismatch for R2 Theme audit indentation decoupling: "
            f"old={old_count}, new={new_count}"
        )

    if args.check:
        print("PASS R2 Theme audit indentation decoupling")
        return

    TARGET.write_text(
        text.replace(OLD, NEW, 1),
        encoding="utf-8",
        newline="\n",
    )
    print("APPLY R2 Theme audit indentation decoupling")


if __name__ == "__main__":
    main()
