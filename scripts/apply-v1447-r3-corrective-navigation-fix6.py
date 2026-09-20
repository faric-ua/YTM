#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("app/src/main/java/com/saney/ytmimporter/MainActivity.kt")

OLD = """        if (workflowRelayActive) {
            reopenDelegatedParentAfterAction()
        }
"""

NEW = """        reopenDelegatedParentAfterAction()
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

    if new_count >= 1 and "workflowRelayActive" not in text:
        print("SKIP: stale workflowRelayActive reference already removed")
        return

    if old_count != 1:
        raise SystemExit(
            f"FAIL: FIX6 anchor mismatch: old={old_count}"
        )

    print("READY: remove stale workflowRelayActive reference")
    if not args.check:
        TARGET.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: auth refresh failure now uses delegated-parent helper directly")

if __name__ == "__main__":
    main()
