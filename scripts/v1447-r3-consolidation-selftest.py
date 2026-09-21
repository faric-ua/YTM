#!/usr/bin/env python3
from __future__ import annotations
import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-consolidation.py"

def load_ops():
    tree = ast.parse(APPLY.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign):
            if any(isinstance(t, ast.Name) and t.id == "OPS" for t in node.targets):
                return ast.literal_eval(node.value)
    raise SystemExit("FAIL: OPS not found")

def fixture(ops):
    root = Path(tempfile.mkdtemp(prefix="ytm-r3-consolidation-"))
    (root / "scripts").mkdir(parents=True, exist_ok=True)
    (root / "scripts/apply-v1447-r3-consolidation.py").write_text(
        APPLY.read_text(encoding="utf-8"), encoding="utf-8", newline="\n"
    )
    grouped = {}
    for op in ops:
        grouped.setdefault(op["path"], []).append(op["old"])
    for rel, chunks in grouped.items():
        p = root / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text("\n\n".join(chunks) + "\n", encoding="utf-8", newline="\n")
    return root

def run(root, *args, expect=0):
    r = subprocess.run(
        ["python", "-B", "scripts/apply-v1447-r3-consolidation.py", *args],
        cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT
    )
    if r.returncode != expect:
        raise SystemExit(f"FAIL: {' '.join(args)} exit={r.returncode} expected={expect}\n{r.stdout}")
    return r

def main():
    ops = load_ops()
    root = fixture(ops)
    run(root, "--check")
    print("PASS: clean --check")
    run(root)
    print("PASS: first apply")
    run(root)
    print("PASS: second apply idempotent")
    for op in ops:
        text = (root / op["path"]).read_text(encoding="utf-8")
        if text.count(op["new"]) != 1:
            raise SystemExit(f"FAIL: post-apply mismatch: {op['name']}")
    print("PASS: all replacements present exactly once")
    dup = fixture(ops)
    op = ops[0]
    p = dup / op["path"]
    p.write_text(p.read_text(encoding="utf-8") + "\n" + op["old"] + "\n",
                 encoding="utf-8", newline="\n")
    if "anchor mismatch" not in run(dup, "--check", expect=1).stdout:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")
    missing = fixture(ops)
    op = ops[0]
    p = missing / op["path"]
    p.write_text(p.read_text(encoding="utf-8").replace(op["old"], "MISSING", 1),
                 encoding="utf-8", newline="\n")
    if "anchor mismatch" not in run(missing, "--check", expect=1).stdout:
        raise SystemExit("FAIL: missing anchor did not fail closed")
    print("PASS: missing anchor fails closed")
    print("PASS: R3 consolidation package selftest complete")

if __name__ == "__main__":
    main()
