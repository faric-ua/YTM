#!/usr/bin/env python3
import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-navigation-progress-r7-fix3.py"

tree = ast.parse(APPLY.read_text(encoding="utf-8"))
OPS = None
for node in tree.body:
    if isinstance(node, ast.Assign):
        for t in node.targets:
            if isinstance(t, ast.Name) and t.id == "OPS":
                OPS = ast.literal_eval(node.value)
if OPS is None:
    raise SystemExit("FAIL: OPS missing")

tmp = Path(tempfile.mkdtemp(prefix="ytm-r7-fix3-"))
grouped = {}
for op in OPS:
    grouped.setdefault(op["file"], []).append(op["old"])

for rel, chunks in grouped.items():
    p = tmp / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text("\n".join(chunks), encoding="utf-8", newline="\n")

scripts = tmp / "scripts"
scripts.mkdir(parents=True, exist_ok=True)
(scripts / APPLY.name).write_text(APPLY.read_text(encoding="utf-8"), encoding="utf-8", newline="\n")

def run(*args, expect=0):
    r = subprocess.run(
        ["python", "-B", f"scripts/{APPLY.name}", *args],
        cwd=tmp, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT
    )
    if r.returncode != expect:
        raise SystemExit(f"FAIL: exit={r.returncode}, expected={expect}\n{r.stdout}")
    return r.stdout

run("--check")
print("PASS: clean --check")
run()
print("PASS: first apply")
run()
print("PASS: idempotent second apply")

for op in OPS:
    text = (tmp / op["file"]).read_text(encoding="utf-8")
    if text.count(op["new"]) != 1:
        raise SystemExit(f"FAIL: replacement missing: {op['name']}")

print("PASS: R7 FIX3 selftest complete")
