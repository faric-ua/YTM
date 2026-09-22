#!/usr/bin/env python3
import argparse
import hashlib
import re
import subprocess
from pathlib import Path

OUTPUT = Path("FILE_MANIFEST.txt")


def version():
    text = Path("app/build.gradle.kts").read_text()
    match = re.search(
        r'versionName\s*=\s*"([^"]+)"',
        text,
    )

    if not match:
        raise SystemExit("cannot resolve versionName")

    return match.group(1)


def files():
    raw = subprocess.check_output(
        [
            "git",
            "ls-files",
            "--cached",
            "--others",
            "--exclude-standard",
        ],
        text=True,
    )

    paths = sorted(
        {
            line.strip()
            for line in raw.splitlines()
            if line.strip() and line.strip() != OUTPUT.as_posix()
        }
    )

    return [
        Path(path)
        for path in paths
        if Path(path).is_file()
    ]


def render():
    lines = [
        f"YTM Importer v{version()} — FILE MANIFEST v2",
        "",
        "Generated deterministically from repository files.",
        "FILE_MANIFEST.txt intentionally excludes itself to avoid a recursive hash.",
        "",
    ]

    for path in files():
        digest = hashlib.sha256(
            path.read_bytes()
        ).hexdigest()[:16]

        lines.append(
            f"{digest}  {path.as_posix()}"
        )

    return "\n".join(lines).rstrip() + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    expected = render()

    if args.check:
        if not OUTPUT.exists():
            raise SystemExit(
                "FAIL: FILE_MANIFEST.txt missing"
            )

        if OUTPUT.read_text() != expected:
            raise SystemExit(
                "FAIL: FILE_MANIFEST.txt is stale; "
                "run scripts/generate-file-manifest.py"
            )

        print("PASS: FILE_MANIFEST current")
        return

    OUTPUT.write_text(expected)
    print(f"WROTE: {OUTPUT}")


if __name__ == "__main__":
    main()
