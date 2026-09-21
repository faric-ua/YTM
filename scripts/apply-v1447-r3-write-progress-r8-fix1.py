#!/usr/bin/env python3
from pathlib import Path
import sys

FILE = Path("scripts/v1447-r3-navigation-progress-r7-audit.sh")
OLD = 'for needle in [\n    \'"Оброблено ${progress.processedTracks}/${progress.totalTracks} • "\',\n    \'"Додано ${progress.job.addedCount} • "\',\n    "TrackStatus.DUPLICATE",\n    "palette.duplicate",\n    "TrackStatus.FAILED",\n    "palette.danger",\n]:\n    if needle not in relay:\n        raise SystemExit(f"FAIL: progress semantic state missing: {needle}")\n'
NEW = 'semantic_groups = [\n    (\n        \'"Оброблено ${progress.processedTracks}/${progress.totalTracks} • "\',\n        \'"Оброблено: ${progress.processedTracks}/${progress.totalTracks} • "\',\n    ),\n    (\n        \'"Додано ${progress.job.addedCount} • "\',\n        \'"Додано: ${progress.job.addedCount} • "\',\n    ),\n]\n\nfor alternatives in semantic_groups:\n    if not any(needle in relay for needle in alternatives):\n        raise SystemExit(\n            "FAIL: progress semantic state missing: "\n            + " OR ".join(alternatives)\n        )\n\nfor needle in [\n    "TrackStatus.DUPLICATE",\n    "palette.duplicate",\n    "TrackStatus.FAILED",\n    "palette.danger",\n]:\n    if needle not in relay:\n        raise SystemExit(f"FAIL: progress semantic state missing: {needle}")\n'

def main():
    check = "--check" in sys.argv[1:]

    if not FILE.exists():
        raise SystemExit(f"FAIL: missing {FILE}")

    text = FILE.read_text(encoding="utf-8")
    old_count = text.count(OLD)
    new_count = text.count(NEW)

    if new_count == 1:
        print("SKIP: R8 FIX1 already applied")
        print("PASS: R7 audit accepts historical + R8 successor aggregate wording")
        return

    if old_count != 1:
        raise SystemExit(
            f"FAIL: R8 FIX1 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: make R7 progress audit successor-compatible")

    if not check:
        FILE.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: R7 progress audit successor compatibility")

    print("PASS: R8 FIX1 ready" if check else "PASS: R8 FIX1 applied")

if __name__ == "__main__":
    main()
