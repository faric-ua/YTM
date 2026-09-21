#!/usr/bin/env python3
import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-history-counters-r9-fix5.py"

tree = ast.parse(APPLY.read_text(encoding="utf-8"))
vals = {}
for node in tree.body:
    if isinstance(node, ast.Assign):
        for target in node.targets:
            if isinstance(target, ast.Name) and target.id in {"OPS", "NEW_FILES"}:
                vals[target.id] = ast.literal_eval(node.value)

ops = vals["OPS"]
new_files = vals["NEW_FILES"]

tmp = Path(tempfile.mkdtemp(prefix="ytm-r9-fix5-"))
grouped = {}
for op in ops:
    grouped.setdefault(op["file"], []).append(op["old"])

for rel, chunks in grouped.items():
    p = tmp / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text("\n\n".join(chunks) + "\n", encoding="utf-8", newline="\n")

(tmp/"scripts").mkdir(parents=True, exist_ok=True)
(tmp/"scripts"/APPLY.name).write_text(
    APPLY.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r = subprocess.run(
        ["python","-B",f"scripts/{APPLY.name}",*args],
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

for op in ops:
    text = (tmp/op["file"]).read_text(encoding="utf-8")
    if text.count(op["new"]) != 1:
        raise SystemExit(f"FAIL: replacement missing: {op['name']}")

for rel, content in new_files.items():
    if (tmp/rel).read_text(encoding="utf-8") != content:
        raise SystemExit(f"FAIL: generated file mismatch: {rel}")

print("PASS: R9 FIX5 selftest complete")
