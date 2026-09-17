#!/usr/bin/env python3
from pathlib import Path
import argparse
import sys

sys.dont_write_bytecode = True

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from v1425_patchlib import PatchError, run, validate_patch_definitions

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
        help="validate all repository anchors without writing"
    )
    args = parser.parse_args()

    try:
        validate_patch_definitions()
        run(ROOT, dry_run=args.check)
    except PatchError as exc:
        print(f"STOP: {exc}")
        return 1

    if args.check:
        print("PASS: v1.4.25 repository anchors validated; no files changed")
    else:
        print("PASS: v1.4.25 Accent Card System applied")

    return 0

if __name__ == "__main__":
    raise SystemExit(main())
