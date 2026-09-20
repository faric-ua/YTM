#!/usr/bin/env python3
from __future__ import annotations

import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-corrective-navigation-fix5.py"

def assignment(name):
    tree = ast.parse(APPLY.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign):
            for target in node.targets:
                if isinstance(target, ast.Name) and target.id == name:
                    return ast.literal_eval(node.value)
    raise SystemExit(f"FAIL: {name} not found")

OLD = assignment("OLD")
NEW = assignment("NEW")

def fixture():
    root = Path(tempfile.mkdtemp(prefix="ytm-r3-nav-fix5-"))
    target = root / "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(
        "class MenuActivity {\n" + OLD + "\n}\n",
        encoding="utf-8",
        newline="\n",
    )
    scripts = root / "scripts"
    scripts.mkdir(parents=True, exist_ok=True)
    (scripts / APPLY.name).write_text(
        APPLY.read_text(encoding="utf-8"),
        encoding="utf-8",
        newline="\n",
    )
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
        raise SystemExit(
            f"FAIL: {' '.join(args) or 'apply'} exit={r.returncode}, expected={expect}\n{r.stdout}"
        )
    return r

def main():
    root = fixture()
    run(root, "--check")
    print("PASS: clean --check")
    run(root)
    print("PASS: first apply")
    run(root)
    print("PASS: second apply idempotent")

    text = (root / "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt").read_text(encoding="utf-8")
    if text.count(NEW) != 1 or text.count(OLD) != 0:
        raise SystemExit("FAIL: post-apply mismatch")
    print("PASS: exact replacement")

    dup = fixture()
    p = dup / "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
    p.write_text(
        p.read_text(encoding="utf-8") + "\n" + OLD,
        encoding="utf-8",
        newline="\n",
    )
    if "anchor mismatch" not in run(dup, "--check", expect=1).stdout:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")

    print("PASS: corrective navigation FIX5 selftest complete")

if __name__ == "__main__":
    main()
