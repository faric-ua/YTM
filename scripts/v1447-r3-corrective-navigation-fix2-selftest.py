#!/usr/bin/env python3
from __future__ import annotations
import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-corrective-navigation-fix2.py"

def assignment(name):
    tree = ast.parse(APPLY.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign) and any(isinstance(t, ast.Name) and t.id == name for t in node.targets):
            return ast.literal_eval(node.value)
    raise SystemExit(f"FAIL: {name} not found")

OPS = assignment("OPS")
NEW_FILES = assignment("NEW_FILES")

def fixture():
    root = Path(tempfile.mkdtemp(prefix="ytm-r3-nav-fix2-"))
    target = root / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n\n".join(op["old"] for op in OPS) + "\n" + ("// filler\n" * 2800), encoding="utf-8", newline="\n")
    scripts = root / "scripts"
    scripts.mkdir(parents=True, exist_ok=True)
    (scripts / APPLY.name).write_text(APPLY.read_text(encoding="utf-8"), encoding="utf-8", newline="\n")
    return root

def run(root, *args, expect=0):
    r = subprocess.run(
        ["python", "-B", f"scripts/{APPLY.name}", *args],
        cwd=root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if r.returncode != expect:
        raise SystemExit(f"FAIL: {' '.join(args) or 'apply'} exit={r.returncode}, expected={expect}\n{r.stdout}")
    return r

def main():
    root = fixture()
    run(root, "--check")
    print("PASS: clean --check")
    run(root)
    print("PASS: first apply")
    run(root)
    print("PASS: second apply idempotent")

    text = (root / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt").read_text(encoding="utf-8")
    for op in OPS:
        if text.count(op["new"]) != 1:
            raise SystemExit(f"FAIL: compact replacement mismatch: {op['name']}")
    for rel, content in NEW_FILES.items():
        if (root / rel).read_text(encoding="utf-8") != content:
            raise SystemExit(f"FAIL: new FIX2 file mismatch: {rel}")
    print("PASS: compact replacements + helper files exact")

    dup = fixture()
    first = OPS[0]
    p = dup / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
    p.write_text(p.read_text(encoding="utf-8") + "\n" + first["old"], encoding="utf-8", newline="\n")
    if "anchor mismatch" not in run(dup, "--check", expect=1).stdout:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")

    missing = fixture()
    last = OPS[-1]
    p = missing / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
    p.write_text(p.read_text(encoding="utf-8").replace(last["old"], "MISSING", 1), encoding="utf-8", newline="\n")
    if "anchor mismatch" not in run(missing, "--check", expect=1).stdout:
        raise SystemExit("FAIL: missing anchor did not fail closed")
    print("PASS: missing anchor fails closed")
    print("PASS: corrective navigation FIX2 selftest complete")

if __name__ == "__main__":
    main()
