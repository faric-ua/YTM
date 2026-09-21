#!/usr/bin/env python3
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix3.py"
OLD = 'for n in [\n    "`✓ Додано` — зелений",\n    "`≋ Дублікат • пропущено` — синій",\n    "`× Помилка` — червоний",\n    "## 10. Легенда / умовні позначення",\n]:\n    if n not in standard: raise SystemExit(f"FAIL: diagram contract missing: {n}")\n'

tmp = Path(tempfile.mkdtemp(prefix="ytm-r9-fix3-"))
target = tmp / "scripts/v1447-r3-write-progress-r8-audit.sh"
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(OLD, encoding="utf-8", newline="\n")

(tmp / "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix3.py").write_text(
    APPLY.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r = subprocess.run(
        ["python", "-B", "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix3.py", *args],
        cwd=tmp,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if r.returncode != 0:
        raise SystemExit(f"FAIL: exit={r.returncode}\n{r.stdout}")
    return r.stdout

run("--check")
print("PASS: clean --check")
run()
print("PASS: first apply")
run()
print("PASS: idempotent second apply")

patched = target.read_text(encoding="utf-8")
for needle in [
    "diagram_contract_groups",
    "`✓ Додано` — зелений",
    "`✓` + `Додано` — зелена галочка",
    "`≋ Дублікат • пропущено` — синій",
    "`≋` + `Дублікат • пропущено` — сині хвилі",
    "`× Помилка` — червоний",
    "`×` + `Помилка` — червоний знак помилки",
    "## 10. Легенда / умовні позначення",
]:
    if needle not in patched:
        raise SystemExit(f"FAIL: patched audit marker missing: {needle}")

print("PASS: R9 FIX3 selftest complete")
