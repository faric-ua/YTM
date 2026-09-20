#!/usr/bin/env python3
from __future__ import annotations

import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-corrective-navigation.py"

def load_assign(name):
    tree = ast.parse(APPLY.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign):
            if any(isinstance(t, ast.Name) and t.id == name for t in node.targets):
                return ast.literal_eval(node.value)
    raise SystemExit(f"FAIL: {name} not found")

OPS = load_assign("OPS")
NEW_FILES = load_assign("NEW_FILES")

def make_repo():
    root = Path(tempfile.mkdtemp(prefix="ytm-r3-nav-selftest-"))
    by_file = {}
    for op in OPS:
        by_file.setdefault(op["file"], []).append(op["old"])
    for rel, chunks in by_file.items():
        p = root / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text("\n\n".join(chunks) + "\n", encoding="utf-8", newline="\n")
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
        raise SystemExit(
            f"FAIL: {' '.join(args) or 'apply'} returned {r.returncode}, expected {expect}\n{r.stdout}"
        )
    return r

def main():
    root = make_repo()
    run(root, "--check")
    print("PASS: clean --check")

    run(root)
    print("PASS: first apply")

    run(root)
    print("PASS: second apply idempotent")

    for op in OPS:
        text = (root / op["file"]).read_text(encoding="utf-8")
        if text.count(op["new"]) != 1:
            raise SystemExit(f"FAIL: post-apply mismatch: {op['name']}")
    for rel, content in NEW_FILES.items():
        if (root / rel).read_text(encoding="utf-8") != content:
            raise SystemExit(f"FAIL: new-file mismatch: {rel}")
    print("PASS: all replacements/new files exact")

    dup = make_repo()
    first = OPS[0]
    p = dup / first["file"]
    p.write_text(
        p.read_text(encoding="utf-8") + "\n" + first["old"] + "\n",
        encoding="utf-8",
        newline="\n",
    )
    if "anchor mismatch" not in run(dup, "--check", expect=1).stdout:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")

    missing = make_repo()
    last = OPS[-1]
    p = missing / last["file"]
    p.write_text(
        p.read_text(encoding="utf-8").replace(last["old"], "MISSING", 1),
        encoding="utf-8",
        newline="\n",
    )
    if "anchor mismatch" not in run(missing, "--check", expect=1).stdout:
        raise SystemExit("FAIL: missing anchor did not fail closed")
    print("PASS: missing anchor fails closed")

    print("PASS: R3 corrective navigation package selftest complete")

if __name__ == "__main__":
    main()
