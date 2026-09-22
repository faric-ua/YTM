#!/usr/bin/env python3
import argparse
import re
from pathlib import Path

OUTPUT = Path("docs/documentation/HISTORICAL_RELEASE_MATRIX.md")


def version_key(path):
    match = re.fullmatch(r"v\.(\d+)\.(\d+)\.(\d+)", path.name)
    if not match:
        return None
    return tuple(int(x) for x in match.groups())


def active_version():
    lines = Path("BACKLOG.md").read_text().splitlines()
    for index, line in enumerate(lines):
        if line.strip() == "## Current" and index + 1 < len(lines):
            match = re.match(r"v([0-9]+\.[0-9]+\.[0-9]+)", lines[index + 1].strip())
            if match:
                return match.group(1)
    return None


def yes(path):
    return path.is_file()


def any_glob(path, pattern):
    return any(p.is_file() for p in path.glob(pattern))


def diagram_count(root):
    diagram_dir = root / "diagrams"
    if not diagram_dir.is_dir():
        return 0
    return len(
        [
            p
            for p in diagram_dir.glob("*.md")
            if p.is_file() and p.name != "README.md"
        ]
    )


def other_flow_docs(root):
    count = 0
    for folder in ("navigation",):
        target = root / folder
        if target.is_dir():
            count += len([p for p in target.glob("*.md") if p.is_file()])
    return count


def cell(present, current=False, final_only=False):
    if present:
        return "PRESENT"
    if current and final_only:
        return "PENDING"
    if current:
        return "MISSING"
    return "RETRO GAP"


def render():
    active = active_version()
    roots = []
    for path in Path("docs").glob("v.*"):
        key = version_key(path)
        if key:
            roots.append((key, path))
    roots.sort()

    lines = [
        "# YTM Importer — Historical Release Documentation Matrix",
        "",
        "Generated from the repository filesystem.",
        "",
        "This matrix measures old releases against the **current** documentation",
        "standard. `RETRO GAP` means the artifact is absent today; it does not",
        "claim that the artifact was mandatory when that historical release was made.",
        "Missing historical evidence must never be fabricated.",
        "",
        f"Active planned release from `BACKLOG.md`: **v{active or 'unknown'}**",
        "",
        "| Version | Release | Regression | Phone plan | Bug register | Evidence | Test run | Phone report | Diagrams | Other flow/nav docs |",
        "|---|---|---|---|---|---|---|---|---:|---:|",
    ]

    for _, root in roots:
        version = root.name[2:]
        current = version == active
        qa = root / "qa"
        diagrams = diagram_count(root)
        nav = other_flow_docs(root)

        row = [
            f"v{version}",
            cell(yes(root / "RELEASE.md"), current),
            cell(yes(root / "REGRESSION_CHECKLIST.md"), current),
            cell(yes(qa / "PHONE_TEST.md"), current),
            cell(yes(qa / "BUG_REGISTER.md"), current),
            cell(yes(qa / "EVIDENCE_MANIFEST.md"), current),
            cell(any_glob(qa, "TEST_RUN_*.md"), current, final_only=True),
            cell(any_glob(qa, "PHONE_TEST_REPORT_*.md"), current, final_only=True),
            str(diagrams),
            str(nav),
        ]
        lines.append("| " + " | ".join(row) + " |")

    lines += [
        "",
        "## Interpretation",
        "",
        "- `PRESENT` — artifact exists in Git.",
        "- `RETRO GAP` — historical artifact is absent; do not invent it.",
        "- `PENDING` — final-only artifact is expected later for the active release.",
        "- `MISSING` — active release is missing a core artifact and should be fixed now.",
        "- `Diagrams` counts Mermaid/Markdown flow files in the regular `diagrams/` folder, excluding its README.",
        "- `Other flow/nav docs` records navigation documentation stored outside the regular diagrams folder.",
        "",
        "The generated matrix is an inventory. Detailed QA truth remains in the",
        "release files, `RELEASE_TEST_STATUS.md`, bug registers and evidence manifests.",
        "",
    ]
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    expected = render().rstrip() + "\n"

    if args.check:
        if not OUTPUT.is_file():
            raise SystemExit("FAIL: historical release matrix missing")
        if OUTPUT.read_text() != expected:
            raise SystemExit(
                "FAIL: historical release matrix stale; "
                "run scripts/generate-release-documentation-matrix.py"
            )
        print("PASS: historical release matrix current")
        return

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(expected)
    print(f"WROTE: {OUTPUT}")


if __name__ == "__main__":
    main()
