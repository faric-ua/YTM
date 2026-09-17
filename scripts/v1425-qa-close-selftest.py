#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path
import hashlib
import tempfile

SCRIPT_DIR = Path(__file__).resolve().parent
PACKAGE_ROOT = SCRIPT_DIR.parent
sys.path.insert(0, str(SCRIPT_DIR))

from v1425_qa_patchlib import (
    PatchError,
    ReplaceOp,
    operations,
    replace_once_or_skip,
    run,
    validate_ops,
    validate_operations,
)


def write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")


def tree_hash(root: Path) -> str:
    h = hashlib.sha256()
    for path in sorted(p for p in root.rglob("*") if p.is_file()):
        h.update(str(path.relative_to(root)).encode("utf-8"))
        h.update(b"\0")
        h.update(path.read_bytes())
        h.update(b"\0")
    return h.hexdigest()


def build_fixture(root: Path) -> None:
    write(
        root,
        "RELEASE_TEST_STATUS.md",
        "# status\n"
        "| v1.4.25 | **NOT TESTED YET** | Accent Card System: consistent two-stroke accents on large cards, themed privacy radio and shorter search hints. |\n"
    )

    write(
        root,
        "PROJECT_STATUS.txt",
        "YTM Importer\n"
        "Version: 1.4.25\n"
        "Version code: 59\n"
        "v1.4.25 NOT TESTED YET\n\n"
        "Known:\n"
        "BUG-X\n"
    )

    write(
        root,
        "CHANGELOG.md",
        "# Changelog\n\n"
        "## v1.4.25\n"
        "- feature\n"
        "- v1.4.25 = NOT PHONE-TESTED YET.\n"
    )

    write(
        root,
        "scripts/qa-plan-audit.sh",
        "grep -Fq '| v1.4.25 | **NOT TESTED YET** |' \"$STATUS\" \\\n"
        "  || fail \"v1.4.25 must start NOT TESTED YET\"\n"
    )

    write(
        root,
        "scripts/release-preflight.sh",
        "grep -Fq '| v1.4.25 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\\n"
        "  || fail \"v1.4.25 must start NOT TESTED YET\"\n"
    )

    write(
        root,
        "scripts/v1425-accent-card-audit.sh",
        "grep -Fq '| v1.4.25 | **NOT TESTED YET** |' \"$STATUS\" \\\n"
        "  || fail \"v1.4.25 NOT TESTED status missing\"\n"
    )

    write(
        root,
        "docs/v.1.4.25/RELEASE.md",
        "# Release\n\n"
        "## Status\n\n"
        "**NOT PHONE-TESTED YET**\n"
    )

    checklist_lines = [
        "# Checklist",
        "",
        "## Card accent system",
        "",
        "- [ ] Home workflow card retains two accent strokes.",
        "- [ ] Home current-playlist card now has two accent strokes.",
        "- [ ] main track cards have two accent strokes.",
        "- [ ] Import large cards have two accent strokes.",
        "- [ ] Review large cards have two accent strokes.",
        "- [ ] Destination large cards have two accent strokes.",
        "- [ ] History entry cards have two accent strokes.",
        "- [ ] Queue job cards have two accent strokes when present.",
        "- [ ] Service cards have two accent strokes.",
        "- [ ] Data cards have two accent strokes.",
        "",
        "## Quiet controls",
        "",
        "- [ ] back buttons do not gain extra card strokes.",
        "- [ ] search fields do not gain extra card strokes.",
        "- [ ] compact Home utility buttons do not gain extra card strokes.",
        "",
        "## Semantic cards",
        "",
        "- [ ] `Безпека` remains amber.",
        "- [ ] warning/error/success accents keep semantic colors.",
        "",
        "## Polish",
        "",
        "- [ ] Destination selected privacy radio uses active theme accent.",
        "- [ ] History hint fits as `Пошук історії`.",
        "- [ ] Queue hint fits as `Пошук у черзі`.",
        "",
        "## Functional smoke",
        "",
        "- [ ] Home → Import → Back.",
    ]
    write(
        root,
        "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
        "\n".join(checklist_lines) + "\n"
    )

    write(
        root,
        "BACKLOG.md",
        "# Roadmap\n\n"
        "## v1.4.25\n"
        "- [x] add shared large-card accent drawable\n"
        "- [ ] GitHub build\n"
        "- [ ] phone test: Home large-card accents\n"
        "- [ ] phone test: Destination radio tint\n"
        "- [ ] phone test: History / Queue hints\n"
        "- [ ] phone test: Data semantic card colors\n"
        "- [ ] navigation smoke\n\n"
        "## Next\n"
        "Phone-test v1.4.25 Accent Card System. After the card language is stable, continue the visual/theme rollout and account-library work only if useful:\n"
        "- next\n"
    )

    write(
        root,
        "YTM_ASSISTANT_WORKFLOW.md",
        "# Workflow\n\n"
        "## 14. Package/apply self-test rule\n\n"
        "Existing rules.\n"
    )


def expect_error(fn, label: str) -> None:
    try:
        fn()
    except PatchError:
        print(f"PASS: {label}")
        return
    raise SystemExit(f"SELFTEST FAIL: expected PatchError: {label}")


def scan_package_text() -> None:
    generated = [
        PACKAGE_ROOT / "docs/tutorial/README.md",
        PACKAGE_ROOT / "docs/tutorial/00_START_HERE.md",
        PACKAGE_ROOT / "docs/tutorial/01_PROJECT_EVOLUTION.md",
        PACKAGE_ROOT / "docs/tutorial/02_DEVELOPMENT_LOOP.md",
        PACKAGE_ROOT / "docs/tutorial/03_ARCHITECTURE_MAP.md",
        PACKAGE_ROOT / "docs/tutorial/04_QA_AND_EVIDENCE.md",
        PACKAGE_ROOT / "docs/tutorial/05_FAILURES_AND_GUARDS.md",
        PACKAGE_ROOT / "docs/tutorial/ROADMAP.md",
        PACKAGE_ROOT / "docs/v.1.4.25/diagrams/PHONE_QA_FLOW.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/EVIDENCE_MANIFEST.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/PACKAGE_NOTES_QA_CLOSE.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/PACKAGE_SELFTEST_QA_CLOSE.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/PHONE_TEST_REPORT_2026-09-17.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/TEST_DATA_SNAPSHOT_2026-09-17.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/TEST_RUN_2026-09-17.md",
        PACKAGE_ROOT / "docs/v.1.4.25/qa/UI_SCREENSHOT_ANALYSIS_2026-09-17.md",
        PACKAGE_ROOT / "scripts/apply-v1.4.25-qa-close.py",
        PACKAGE_ROOT / "scripts/v1425-qa-close-selftest.py",
        PACKAGE_ROOT / "scripts/v1425-qa-tutorial-audit.sh",
        PACKAGE_ROOT / "scripts/v1425_qa_patchlib.py",
    ]

    for path in generated:
        if not path.is_file():
            raise SystemExit(f"SELFTEST FAIL: generated text missing: {path}")
        data = path.read_bytes()
        if b"\r" in data:
            raise SystemExit(f"SELFTEST FAIL: CR character in {path}")
        decoded = data.decode("utf-8")
        for idx, line in enumerate(decoded.split("\n"), start=1):
            if line.endswith(" ") or line.endswith("\t"):
                raise SystemExit(
                    f"SELFTEST FAIL: trailing whitespace in {path}:{idx}"
                )

    print("PASS: generated package text is LF-only and has no trailing whitespace")


def compile_scripts() -> None:
    for path in sorted(SCRIPT_DIR.glob("*.py")):
        source = path.read_text(encoding="utf-8")
        compile(source, str(path), "exec")
    print("PASS: Python helper syntax")


def main() -> int:
    validate_operations()
    print("PASS: production patch definitions validated")

    synthetic_bad = [
        ReplaceOp(
            "x.txt",
            r"line1\nline2",
            "replacement",
            "literal-newline test"
        )
    ]
    expect_error(
        lambda: validate_ops(synthetic_bad),
        "literal \\\\n anchor rejected"
    )

    scan_package_text()
    compile_scripts()

    with tempfile.TemporaryDirectory(prefix="ytm-v1425-qa-selftest-") as td:
        repo = Path(td) / "repo"
        repo.mkdir()
        build_fixture(repo)

        run(repo, dry_run=True)
        print("PASS: clean fixture --check")

        run(repo, dry_run=False)
        first = tree_hash(repo)
        print("PASS: clean fixture first apply")

        run(repo, dry_run=True)
        print("PASS: post-apply --check")

        run(repo, dry_run=False)
        second = tree_hash(repo)
        if first != second:
            raise SystemExit("SELFTEST FAIL: repeat apply changed fixture")
        print("PASS: repeat apply idempotent")

        dup_root = Path(td) / "dup"
        dup_root.mkdir()
        write(dup_root, "x.txt", "ANCHOR\nANCHOR\n")
        dup_op = ReplaceOp("x.txt", "ANCHOR\n", "NEW\n", "duplicate test")
        expect_error(
            lambda: replace_once_or_skip(dup_root, dup_op, False),
            "duplicate anchor fails closed"
        )

        miss_root = Path(td) / "missing"
        miss_root.mkdir()
        write(miss_root, "x.txt", "other\n")
        miss_op = ReplaceOp("x.txt", "ANCHOR\n", "NEW\n", "missing test")
        expect_error(
            lambda: replace_once_or_skip(miss_root, miss_op, False),
            "missing anchor fails closed"
        )

    print("PASS: v1.4.25 QA/TUTORIAL PACKAGE SELFTEST COMPLETE")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
