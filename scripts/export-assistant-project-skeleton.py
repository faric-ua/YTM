#!/usr/bin/env python3
import argparse
import zipfile
from pathlib import Path

CONTEXT = Path("docs/assistant-kit/CONTEXT_FILES.txt")
AUDITS = Path("docs/assistant-kit/PORTABLE_AUDITS.txt")
PORTABLE_ROOT = Path("docs/assistant-kit/portable")
DEFAULT_OUTPUT = Path("YTM-Assistant-Migration-Kit.zip")

SUPPORT = [
    Path("ASSISTANT_CONTEXT_INDEX.md"),
    Path("START_HERE_ASSISTANT.md"),
    Path("docs/assistant-kit/CONTEXT_FILES.txt"),
    Path("docs/assistant-kit/PORTABLE_AUDITS.txt"),
    Path("scripts/assistant-context-audit.sh"),
    Path("scripts/release-documentation-audit.sh"),
    Path("scripts/documentation-system-audit.sh"),
    Path("scripts/generate-audit-catalog.py"),
    Path("scripts/generate-file-manifest.py"),
    Path("scripts/export-assistant-project-skeleton.py"),
    Path("scripts/create-release-docs.py"),
    Path("scripts/release-metadata-audit.py"),
    Path("scripts/release-close-audit.sh"),
    Path("scripts/generate-release-documentation-matrix.py"),
    Path("FILE_MANIFEST.txt"),
]

FORBIDDEN_NAMES = {
    "release-signing.properties",
}

FORBIDDEN_SUFFIXES = {
    ".jks",
    ".keystore",
}


def listed(path):
    return [
        Path(line.strip())
        for line in path.read_text().splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    ]


def validate(paths):
    missing = [
        path.as_posix()
        for path in paths
        if not path.is_file()
    ]

    if missing:
        raise SystemExit(
            "FAIL: migration source missing:\n"
            + "\n".join(missing)
        )

    for path in paths:
        if path.name in FORBIDDEN_NAMES:
            raise SystemExit(
                f"FAIL: forbidden secret file: {path}"
            )

        if path.suffix.lower() in FORBIDDEN_SUFFIXES:
            raise SystemExit(
                f"FAIL: forbidden signing file: {path}"
            )


def build_entries():
    context = listed(CONTEXT)
    audits = listed(AUDITS)

    portable = sorted(
        path
        for path in PORTABLE_ROOT.rglob("*")
        if path.is_file()
    )

    current = [
        Path("ASSISTANT_CONTEXT_INDEX.md"),
        Path("START_HERE_ASSISTANT.md"),
        Path("docs/assistant-kit/CONTEXT_FILES.txt"),
    ] + context

    all_sources = list(
        dict.fromkeys(
            portable
            + current
            + audits
            + SUPPORT
        )
    )

    validate(all_sources)

    entries = {}

    for path in portable:
        relative = path.relative_to(PORTABLE_ROOT)
        archive = (
            Path("YTM-Assistant-Migration-Kit")
            / "portable"
            / relative
        )
        entries[archive.as_posix()] = path

    for path in current:
        archive = (
            Path("YTM-Assistant-Migration-Kit")
            / "current-project-context"
            / path
        )
        entries[archive.as_posix()] = path

    for path in audits:
        archive = (
            Path("YTM-Assistant-Migration-Kit")
            / "reference-system-audits"
            / path
        )
        entries[archive.as_posix()] = path

    for path in SUPPORT:
        archive = (
            Path("YTM-Assistant-Migration-Kit")
            / "tooling"
            / path.name
        )
        entries.setdefault(
            archive.as_posix(),
            path,
        )

    return entries


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
    )
    args = parser.parse_args()

    entries = build_entries()

    if args.check:
        print("PASS: migration kit sources valid")
        print(f"FILES: {len(entries)}")
        return

    args.output.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    if args.output.exists():
        args.output.unlink()

    with zipfile.ZipFile(
        args.output,
        "w",
        compression=zipfile.ZIP_DEFLATED,
    ) as archive:
        archive.writestr(
            "YTM-Assistant-Migration-Kit/README_FIRST.txt",
            (
                "portable/ contains the generic reusable project skeleton.\n"
                "current-project-context/ contains the YTM-specific recovery "
                "context as a worked example.\n"
                "reference-system-audits/ contains system/lifecycle/navigation "
                "audit examples.\n"
                "tooling/ contains the generators/audits used to maintain the kit.\n"
                "Do not blindly copy YTM-specific assertions into another project.\n"
            ),
        )

        for archive_path, source in sorted(entries.items()):
            archive.write(
                source,
                archive_path,
            )

    print(f"WROTE: {args.output}")
    print(f"FILES: {len(entries) + 1}")


if __name__ == "__main__":
    main()
