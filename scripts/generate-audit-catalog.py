#!/usr/bin/env python3
import argparse
from pathlib import Path

OUTPUT = Path("docs/assistant-kit/AUDIT_CATALOG.md")
PORTABLE = Path("docs/assistant-kit/PORTABLE_AUDITS.txt")


def load_portable():
    return [
        line.strip()
        for line in PORTABLE.read_text().splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    ]


def audit_files():
    result = []

    for path in Path("scripts").iterdir():
        if not path.is_file():
            continue

        name = path.name

        if name.endswith("-audit.sh") or name.endswith("-audit.py"):
            result.append(path.as_posix())

    return sorted(result)


def render():
    audits = audit_files()
    portable = load_portable()

    missing = [
        path
        for path in portable
        if not Path(path).is_file()
    ]

    if missing:
        raise SystemExit(
            "portable audit paths missing:\n"
            + "\n".join(missing)
        )

    core = [
        path
        for path in audits
        if not Path(path).name.startswith("v")
    ]

    versioned = [
        path
        for path in audits
        if Path(path).name.startswith("v")
    ]

    lines = [
        "# YTM Importer — Audit Catalog",
        "",
        "This file is generated from the repository audit scripts.",
        "",
        "Do not hand-edit the inventory; update scripts or",
        "`docs/assistant-kit/PORTABLE_AUDITS.txt`, then regenerate.",
        "",
        f"Total audit scripts: **{len(audits)}**",
        "",
        "## Portable/system subset",
        "",
        "These audits are included in the migration kit as reusable/reference",
        "examples for lifecycle, navigation, modal, layout and documentation",
        "contracts.",
        "",
    ]

    lines.extend(f"- `{path}`" for path in portable)

    lines += [
        "",
        "## Core/current audits",
        "",
    ]

    lines.extend(f"- `{path}`" for path in core)

    lines += [
        "",
        "## Versioned/historical audits",
        "",
    ]

    lines.extend(f"- `{path}`" for path in versioned)

    lines += [
        "",
        "## Usage rule",
        "",
        "The catalog is an inventory, not a substitute for reading the audit.",
        "",
        "Before changing behavior protected by an audit, inspect the exact",
        "script and the contract it represents.",
        "",
    ]

    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    expected = render().rstrip() + "\n"

    if args.check:
        if not OUTPUT.exists():
            raise SystemExit("FAIL: audit catalog missing")

        if OUTPUT.read_text() != expected:
            raise SystemExit(
                "FAIL: audit catalog is stale; "
                "run scripts/generate-audit-catalog.py"
            )

        print("PASS: audit catalog current")
        return

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(expected)
    print(f"WROTE: {OUTPUT}")


if __name__ == "__main__":
    main()
