#!/usr/bin/env python3
from __future__ import annotations

import argparse
import ast
from pathlib import Path

TARGET = Path("scripts/apply-v1447-r3-flow-result-lifecycle-r6.py")
OP_NAME = "Release preflight checks R6 audit"
OP_FILE = "scripts/release-preflight.sh"

WRONG_OLD = '''check_file "scripts/v1447-r3-runtime-lifecycle-r5-audit.sh"\ncheck_file "app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"\n'''
WRONG_NEW = '''check_file "scripts/v1447-r3-runtime-lifecycle-r5-audit.sh"\ncheck_file "scripts/v1447-r3-flow-result-lifecycle-r6-audit.sh"\ncheck_file "app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"\n'''

CORRECT_OLD = '''check_file "scripts/project-handoff-audit.sh"\ncheck_file "scripts/v1447-r3-runtime-lifecycle-r5-audit.sh"\n'''
CORRECT_NEW = '''check_file "scripts/project-handoff-audit.sh"\ncheck_file "scripts/v1447-r3-runtime-lifecycle-r5-audit.sh"\ncheck_file "scripts/v1447-r3-flow-result-lifecycle-r6-audit.sh"\n'''


def load_ops_assignment(text: str):
    tree = ast.parse(text)
    for node in tree.body:
        if not isinstance(node, ast.Assign):
            continue
        if any(isinstance(t, ast.Name) and t.id == "OPS" for t in node.targets):
            return node, ast.literal_eval(node.value)
    raise SystemExit("FAIL: OPS assignment missing from R6 apply script")


def locate_target_op(ops):
    matches = [
        op for op in ops
        if op.get("name") == OP_NAME and op.get("file") == OP_FILE
    ]
    if len(matches) != 1:
        raise SystemExit(
            f"FAIL: expected exactly one R6 preflight-check op, found {len(matches)}"
        )
    return matches[0]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not TARGET.exists():
        raise SystemExit(f"FAIL: missing {TARGET}")

    text = TARGET.read_text(encoding="utf-8")
    node, ops = load_ops_assignment(text)
    op = locate_target_op(ops)

    current = (op.get("old"), op.get("new"))
    correct = (CORRECT_OLD, CORRECT_NEW)
    wrong = (WRONG_OLD, WRONG_NEW)

    if current == correct:
        print("SKIP: R6 preflight-check op already has the correct current anchor")
        return

    if current != wrong:
        raise SystemExit(
            "FAIL: R6 FIX1B semantic guard mismatch; "
            "preflight-check op is neither the known wrong form nor corrected form"
        )

    print("READY: replace wrong R6 release-preflight anchor semantically")
    if args.check:
        return

    op["old"] = CORRECT_OLD
    op["new"] = CORRECT_NEW

    lines = text.splitlines(keepends=True)
    start = node.lineno - 1
    end = node.end_lineno
    replacement = "OPS = " + repr(ops) + "\n"
    new_text = "".join(lines[:start]) + replacement + "".join(lines[end:])

    TARGET.write_text(new_text, encoding="utf-8", newline="\n")

    # Verify semantic result after write.
    _, verify_ops = load_ops_assignment(TARGET.read_text(encoding="utf-8"))
    verify_op = locate_target_op(verify_ops)
    if (verify_op.get("old"), verify_op.get("new")) != correct:
        raise SystemExit("FAIL: semantic verification failed after FIX1B write")

    print("APPLIED: R6 preflight-check op now matches current release-preflight order")


if __name__ == "__main__":
    main()
