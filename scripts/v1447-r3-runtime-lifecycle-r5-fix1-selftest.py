#!/usr/bin/env python3
from __future__ import annotations

import ast
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-runtime-lifecycle-r5-fix1.py"

def assignment(name):
    tree = ast.parse(APPLY.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign):
            for target in node.targets:
                if isinstance(target, ast.Name) and target.id == name:
                    return ast.literal_eval(node.value)
    raise SystemExit(f"FAIL: {name} missing")

OLD = assignment("OLD")
NEW = assignment("NEW")

def fixture():
    root = Path(tempfile.mkdtemp(prefix="ytm-r5-fix1-"))
    target = root / "scripts/v1447-r3-navigation-ownership-audit.sh"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(
        "#!/usr/bin/env bash\npython - <<'PY'\n" +
        OLD +
        "PY\n",
        encoding="utf-8",
        newline="\n",
    )

    apply = root / "scripts" / APPLY.name
    apply.write_text(
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
            f"FAIL: {' '.join(args) or 'apply'} exit={r.returncode}, "
            f"expected={expect}\n{r.stdout}"
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

    text = (
        root / "scripts/v1447-r3-navigation-ownership-audit.sh"
    ).read_text(encoding="utf-8")

    if text.count(NEW) != 1 or text.count(OLD) != 0:
        raise SystemExit("FAIL: exact replacement mismatch")

    print("PASS: exact historical-audit replacement")

    dup = fixture()
    p = dup / "scripts/v1447-r3-navigation-ownership-audit.sh"
    p.write_text(
        p.read_text(encoding="utf-8") + "\n" + OLD,
        encoding="utf-8",
        newline="\n",
    )

    output = run(
        dup,
        "--check",
        expect=1,
    ).stdout

    if "anchor mismatch" not in output:
        raise SystemExit(
            "FAIL: duplicate anchor did not fail closed"
        )

    print("PASS: duplicate anchor fails closed")
    print("PASS: R5 FIX1 selftest complete")

if __name__ == "__main__":
    main()
