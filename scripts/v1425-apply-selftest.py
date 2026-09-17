#!/usr/bin/env python3
from pathlib import Path
import hashlib
import shutil
import sys

sys.dont_write_bytecode = True
import tempfile

SCRIPT_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPT_DIR))

from v1425_patchlib import (
    PatchError,
    ReplaceOp,
    _replace,
    operations,
    run,
    validate_operations,
    validate_patch_definitions,
    ROUNDED_OLD,
)


def write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def build_fixture(root: Path) -> None:
    # Start with each replace op's old anchor so every production patch path is exercised.
    grouped = {}
    for op in operations():
        grouped.setdefault(op.rel, [])
        if op.old not in grouped[op.rel]:
            grouped[op.rel].append(op.old)

    for rel, anchors in grouped.items():
        write(
            root,
            rel,
            "\n---fixture-anchor---\n".join(anchors) + "\n"
        )

    # Custom-update files need section structure/markers.
    write(
        root,
        "PROJECT_STATUS.txt",
        """YTM Importer
Version: 1.4.24
Version code: 58
v1.4.24 NOT TESTED YET

Known:
BUG-X
"""
    )

    write(
        root,
        "BACKLOG.md",
        """# YTM Importer — Roadmap

## Current
v1.4.24 — Theme Wave 2

## v1.4.24
- [ ] GitHub build
- [ ] phone test: Import
- [ ] phone test: Review
- [ ] phone test: Destination
- [ ] phone test: History / Queue / Data / Service
- [ ] phone theme spot-check on one utility screen

## Next
Phone-test v1.4.23 button fit. After the Home geometry is stable, continue the visual/theme rollout and account-library work only if useful:
"""
    )

    write(
        root,
        "CHANGELOG.md",
        """# Журнал змін (Changelog)

## v1.4.24
- Theme Wave 2.
"""
    )

    write(
        root,
        "YTM_ASSISTANT_WORKFLOW.md",
        """# Workflow

## 13. Recovery
Existing rule.
"""
    )

    # Rebuild release-preflight as all required old anchors + custom marker.
    pf_ops = [
        op.old for op in operations()
        if op.rel == "scripts/release-preflight.sh"
    ]
    write(
        root,
        "scripts/release-preflight.sh",
        "\n".join(pf_ops) +
        '\ncheck_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"\n'
    )


def tree_hash(root: Path) -> str:
    h = hashlib.sha256()
    for path in sorted(p for p in root.rglob("*") if p.is_file()):
        h.update(str(path.relative_to(root)).encode())
        h.update(b"\0")
        h.update(path.read_bytes())
        h.update(b"\0")
    return h.hexdigest()


def expect_patch_error(fn, label: str) -> None:
    try:
        fn()
    except PatchError:
        print(f"PASS: {label}")
        return
    raise SystemExit(f"SELFTEST FAIL: expected PatchError: {label}")


def main() -> int:
    validate_patch_definitions()
    print("PASS: production patch definitions contain no literal \\\\n anchors")

    with tempfile.TemporaryDirectory(prefix="ytm-v1425-selftest-") as td:
        fixture = Path(td) / "repo"
        fixture.mkdir()
        build_fixture(fixture)

        # Pass 1: check-only on clean fixture.
        run(fixture, dry_run=True)
        print("PASS: clean fixture --check")

        # Pass 2: real first apply.
        run(fixture, dry_run=False)
        first_hash = tree_hash(fixture)
        print("PASS: clean fixture first apply")

        # Pass 3: check-only after apply.
        run(fixture, dry_run=True)
        print("PASS: post-apply --check")

        # Pass 4: real repeat apply must be idempotent.
        run(fixture, dry_run=False)
        second_hash = tree_hash(fixture)

        if first_hash != second_hash:
            raise SystemExit(
                "SELFTEST FAIL: repeat apply changed files"
            )

        print("PASS: repeat apply is idempotent")

        # Duplicate-anchor failure.
        dup = Path(td) / "dup"
        dup.mkdir()
        write(dup, "x.txt", "ANCHOR\nANCHOR\n")
        dup_op = ReplaceOp(
            "x.txt",
            "ANCHOR\n",
            "NEW\n",
            "duplicate-anchor test"
        )
        expect_patch_error(
            lambda: _replace(dup, dup_op, False),
            "duplicate anchor fails closed"
        )

        # Missing-anchor failure.
        missing = Path(td) / "missing"
        missing.mkdir()
        write(missing, "x.txt", "something else\n")
        miss_op = ReplaceOp(
            "x.txt",
            "ANCHOR\n",
            "NEW\n",
            "missing-anchor test"
        )
        expect_patch_error(
            lambda: _replace(missing, miss_op, False),
            "missing anchor fails closed"
        )

        # Literal-backslash-n regression failure.
        bad = [
            ReplaceOp(
                "x.txt",
                r"line1\nline2",
                "replacement",
                "literal-newline regression test"
            )
        ]
        expect_patch_error(
            lambda: validate_operations(bad),
            "literal \\\\n anchor is rejected"
        )

    print("PASS: v1.4.25 APPLY SELFTEST COMPLETE")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
