#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path
import argparse

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from v1425_qa_patchlib import PatchError, run, validate_operations


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
        help="validate current repository anchors without writing"
    )
    args = parser.parse_args()

    try:
        validate_operations()
        run(ROOT, dry_run=args.check)
    except PatchError as exc:
        print(f"STOP: {exc}")
        return 1

    if args.check:
        print("PASS: v1.4.25 QA-close anchors validated; no files changed")
    else:
        print("PASS: v1.4.25 QA close + tutorial foundation applied")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
