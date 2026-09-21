#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("app/src/main/java/com/saney/ytmimporter/MainActivity.kt")

OLD = """                    if (
                        result.authorizationInvalidated
                    ) {
                        reopenDelegatedParentAfterAction()
                    } else if (openReviewAfter) {
                        openReviewScreen()
                    }
"""

NEW = """                    if (
                        result.authorizationInvalidated
                    ) {
                        reopenDelegatedParentAfterAction()
                    }

                    if (
                        openReviewAfter &&
                        !result.authorizationInvalidated
                    ) {
                        openReviewScreen()
                    }
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

    if new_count == 1:
        print("SKIP: historical auth-invalidated guard form already restored")
        return

    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"FAIL: FIX3 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: restore explicit !result.authorizationInvalidated guard")
    if not args.check:
        TARGET.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: explicit Review auto-open auth guard restored")
        print(f"MainActivity lines: {len(TARGET.read_text(encoding='utf-8').splitlines())}")

if __name__ == "__main__":
    main()
