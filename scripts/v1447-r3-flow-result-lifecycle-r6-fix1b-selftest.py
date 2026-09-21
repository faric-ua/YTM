#!/usr/bin/env python3
from __future__ import annotations

import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FIXER = ROOT / "scripts/apply-v1447-r3-flow-result-lifecycle-r6-fix1b.py"


def fixer_constants():
    tree = ast.parse(FIXER.read_text(encoding="utf-8"))
    values = {}
    for node in tree.body:
        if isinstance(node, ast.Assign) and len(node.targets) == 1:
            t = node.targets[0]
            if isinstance(t, ast.Name) and t.id in {
                "OP_NAME", "OP_FILE", "WRONG_OLD", "WRONG_NEW",
                "CORRECT_OLD", "CORRECT_NEW"
            }:
                values[t.id] = ast.literal_eval(node.value)
    return values


def fixture():
    c = fixer_constants()
    root = Path(tempfile.mkdtemp(prefix="ytm-r6-fix1b-"))
    scripts = root / "scripts"
    scripts.mkdir(parents=True, exist_ok=True)

    ops = [
        {
            "file": "app/X.kt",
            "name": "unrelated",
            "old": "a",
            "new": "b",
        },
        {
            "file": c["OP_FILE"],
            "name": c["OP_NAME"],
            "old": c["WRONG_OLD"],
            "new": c["WRONG_NEW"],
        },
    ]

    target = scripts / "apply-v1447-r3-flow-result-lifecycle-r6.py"
    target.write_text(
        "#!/usr/bin/env python3\n"
        "from pathlib import Path\n"
        f"OPS = {repr(ops)}\n"
        "NEW_FILES = {}\n",
        encoding="utf-8",
        newline="\n",
    )

    (scripts / FIXER.name).write_text(
        FIXER.read_text(encoding="utf-8"),
        encoding="utf-8",
        newline="\n",
    )
    return root, c


def run(root, *args, expect=0):
    p = subprocess.run(
        ["python", "-B", f"scripts/{FIXER.name}", *args],
        cwd=root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if p.returncode != expect:
        raise SystemExit(
            f"FAIL: {' '.join(args) or 'apply'} exit={p.returncode}, "
            f"expected={expect}\n{p.stdout}"
        )
    return p.stdout


def read_target_op(root, c):
    path = root / "scripts/apply-v1447-r3-flow-result-lifecycle-r6.py"
    tree = ast.parse(path.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign) and any(
            isinstance(t, ast.Name) and t.id == "OPS" for t in node.targets
        ):
            ops = ast.literal_eval(node.value)
            matches = [
                op for op in ops
                if op.get("name") == c["OP_NAME"] and op.get("file") == c["OP_FILE"]
            ]
            if len(matches) == 1:
                return matches[0]
    raise SystemExit("FAIL: target op missing in selftest fixture")


def main():
    root, c = fixture()

    run(root, "--check")
    print("PASS: semantic --check")

    run(root)
    print("PASS: first semantic apply")

    run(root)
    print("PASS: second apply idempotent")

    op = read_target_op(root, c)
    if op["old"] != c["CORRECT_OLD"] or op["new"] != c["CORRECT_NEW"]:
        raise SystemExit("FAIL: corrected op values mismatch")
    print("PASS: corrected R6 OPS entry exact")

    print("PASS: R6 FIX1B selftest complete")


if __name__ == "__main__":
    main()
