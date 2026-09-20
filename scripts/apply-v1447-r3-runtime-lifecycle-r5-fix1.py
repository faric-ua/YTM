#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("scripts/v1447-r3-navigation-ownership-audit.sh")

OLD = """start_existing = dest.index('label = "Вибрати існуючий плейлист"')
start_existing_end = dest.index('content.addView(existingCard)', start_existing)
if 'requestExistingPlaylists()' not in dest[start_existing:start_existing_end]:
    raise SystemExit("FAIL: Destination existing-list load still relays through parent")
"""

NEW = """start_existing = dest.index('label = "Вибрати існуючий плейлист"')
start_existing_end = dest.index('content.addView(existingCard)', start_existing)
existing_entry = dest[start_existing:start_existing_end]

if (
    'requestExistingPlaylists()' not in existing_entry
    and 'openExistingPlaylists()' not in existing_entry
):
    raise SystemExit(
        "FAIL: Destination existing-list entry has no local load/open path"
    )

if 'openExistingPlaylists()' in existing_entry:
    helper_start = dest.index('private fun openExistingPlaylists()')
    helper_end = dest.index(
        'private fun requestExistingPlaylists()',
        helper_start
    )
    helper = dest[helper_start:helper_end]

    for needle in [
        'intent.hasExtra(',
        'EXTRA_EXISTING_IDS',
        'showExistingListScreen()',
        'requestExistingPlaylists()',
    ]:
        if needle not in helper:
            raise SystemExit(
                "FAIL: Destination cached existing-list successor "
                f"contract missing: {needle}"
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
        print("SKIP: R5 FIX1 historical audit compatibility already applied")
        return

    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"FAIL: R5 FIX1 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: teach historical navigation audit the R5 cache-aware successor")
    if not args.check:
        TARGET.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: historical audit accepts old direct-load or exact R5 cache-aware local path")

if __name__ == "__main__":
    main()
