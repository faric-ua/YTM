#!/usr/bin/env python3
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix4.py"
OLD = 'for needle in [\n    \'private var writeScroll: ScrollView? = null\',\n    \'writeScroll = scroll\',\n    \'"✓ "\',\n    \'palette.success\',\n    \'activeRow = row\',\n    \'smoothScrollTo(\',\n]:\n    if needle not in relay:\n        raise SystemExit(f"FAIL: write progress visibility contract missing: {needle}")\n'

tmp = Path(tempfile.mkdtemp(prefix="ytm-r9-fix4-"))
target = tmp / "scripts/v1447-r3-flow-result-lifecycle-r6-audit.sh"
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(OLD, encoding="utf-8", newline="\n")

(tmp / "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix4.py").write_text(
    APPLY.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r = subprocess.run(
        ["python", "-B", "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix4.py", *args],
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
    "success_marker_ok",
    "val semanticIcon =",
    "semanticIconColor",
    "historical",
    "R9 semantic success icon",
]:
    if needle not in patched:
        raise SystemExit(f"FAIL: patched R6 audit marker missing: {needle}")

print("PASS: R9 FIX4 selftest complete")
