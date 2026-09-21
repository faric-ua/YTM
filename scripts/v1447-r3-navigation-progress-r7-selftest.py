#!/usr/bin/env python3
from __future__ import annotations
import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-navigation-progress-r7.py"

def assignment(name):
    tree = ast.parse(APPLY.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign):
            for target in node.targets:
                if isinstance(target, ast.Name) and target.id == name:
                    return ast.literal_eval(node.value)
    raise SystemExit(f"FAIL: {name} missing")

OPS = assignment("OPS")
NEW_FILES = assignment("NEW_FILES")

def fixture():
    root = Path(tempfile.mkdtemp(prefix="ytm-r7-"))
    grouped = {}
    for op in OPS:
        grouped.setdefault(op["file"], []).append(op["old"])
    for rel, chunks in grouped.items():
        p = root / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text("\n\n".join(chunks) + "\n", encoding="utf-8", newline="\n")
    scripts = root / "scripts"
    scripts.mkdir(parents=True, exist_ok=True)
    (scripts / APPLY.name).write_text(APPLY.read_text(encoding="utf-8"), encoding="utf-8", newline="\n")
    return root

def run(root, *args, expect=0):
    p = subprocess.run(
        ["python", "-B", f"scripts/{APPLY.name}", *args],
        cwd=root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if p.returncode != expect:
        raise SystemExit(
            f"FAIL: {' '.join(args) or 'apply'} exit={p.returncode}, expected={expect}\n{p.stdout}"
        )
    return p.stdout

def main():
    root = fixture()
    run(root, "--check")
    print("PASS: clean --check")
    run(root)
    print("PASS: first apply")
    run(root)
    print("PASS: second apply idempotent")

    for op in OPS:
        text = (root / op["file"]).read_text(encoding="utf-8")
        if text.count(op["new"]) != 1:
            raise SystemExit(f"FAIL: replacement mismatch: {op['name']}")
    for rel, content in NEW_FILES.items():
        if (root / rel).read_text(encoding="utf-8") != content:
            raise SystemExit(f"FAIL: generated file mismatch: {rel}")

    dup = fixture()
    first = OPS[0]
    p = dup / first["file"]
    p.write_text(
        p.read_text(encoding="utf-8") + "\n" + first["old"],
        encoding="utf-8",
        newline="\n",
    )
    out = run(dup, "--check", expect=1)
    if "anchor mismatch" not in out:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")
    print("PASS: R7 selftest complete")

if __name__ == "__main__":
    main()
