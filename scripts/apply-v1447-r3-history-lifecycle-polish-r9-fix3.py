#!/usr/bin/env python3
from pathlib import Path
import sys

FILE = Path("scripts/v1447-r3-write-progress-r8-audit.sh")
OLD = 'for n in [\n    "`✓ Додано` — зелений",\n    "`≋ Дублікат • пропущено` — синій",\n    "`× Помилка` — червоний",\n    "## 10. Легенда / умовні позначення",\n]:\n    if n not in standard: raise SystemExit(f"FAIL: diagram contract missing: {n}")\n'
NEW = 'diagram_contract_groups = [\n    (\n        "`✓ Додано` — зелений",\n        "`✓` + `Додано` — зелена галочка",\n    ),\n    (\n        "`≋ Дублікат • пропущено` — синій",\n        "`≋` + `Дублікат • пропущено` — сині хвилі",\n    ),\n    (\n        "`× Помилка` — червоний",\n        "`×` + `Помилка` — червоний знак помилки",\n    ),\n]\n\nfor alternatives in diagram_contract_groups:\n    if not any(needle in standard for needle in alternatives):\n        raise SystemExit(\n            "FAIL: diagram contract missing: "\n            + " OR ".join(alternatives)\n        )\n\nfor n in [\n    "## 10. Легенда / умовні позначення",\n]:\n    if n not in standard:\n        raise SystemExit(f"FAIL: diagram contract missing: {n}")\n'

def main():
    check = "--check" in sys.argv[1:]

    if not FILE.exists():
        raise SystemExit(f"FAIL: missing {FILE}")

    text = FILE.read_text(encoding="utf-8")
    old_count = text.count(OLD)
    new_count = text.count(NEW)

    if new_count == 1:
        print("SKIP: R9 FIX3 already applied")
        print("PASS: R8 audit accepts historical + R9 diagram semantics")
        return

    if old_count != 1:
        raise SystemExit(
            f"FAIL: R9 FIX3 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: make R8 diagram audit successor-compatible")

    if not check:
        FILE.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: R8 diagram audit successor compatibility")

    print("PASS: R9 FIX3 ready" if check else "PASS: R9 FIX3 applied")

if __name__ == "__main__":
    main()
