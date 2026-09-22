#!/usr/bin/env python3
import argparse
import json
import re
import subprocess
from pathlib import Path


def fail(message):
    raise SystemExit("FAIL: " + message)


def app_identity():
    text = Path("app/build.gradle.kts").read_text()
    version_match = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    code_match = re.search(r"versionCode\s*=\s*(\d+)", text)
    if not version_match or not code_match:
        fail("cannot parse app identity")
    return version_match.group(1), int(code_match.group(1))


def git_commit_for_tag(tag):
    try:
        return subprocess.check_output(
            ["git", "rev-parse", f"refs/tags/{tag}^{{commit}}"],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
    except subprocess.CalledProcessError:
        return None


def status_line(version):
    path = Path("RELEASE_TEST_STATUS.md")
    prefix = f"| v{version} |"
    for line in path.read_text().splitlines():
        if line.startswith(prefix):
            return line
    return ""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("version")
    parser.add_argument("--planned", action="store_true")
    parser.add_argument("--final", action="store_true")
    parser.add_argument("--require-current-app", action="store_true")
    args = parser.parse_args()

    if args.planned and args.final:
        fail("--planned and --final are mutually exclusive")

    root = Path(f"docs/v.{args.version}")
    meta_path = root / "RELEASE_META.json"
    if not meta_path.is_file():
        fail(f"metadata missing: {meta_path}")

    meta = json.loads(meta_path.read_text())

    required = [
        "schema",
        "versionName",
        "versionCode",
        "phase",
        "feature",
        "branch",
        "appSourceSha",
        "signedRun",
        "qaStatus",
        "tag",
        "checkpointTag",
        "phoneTestDate",
    ]
    missing = [key for key in required if key not in meta]
    if missing:
        fail("metadata keys missing: " + ", ".join(missing))

    if meta["schema"] != 1:
        fail("unsupported RELEASE_META schema")
    if meta["versionName"] != args.version:
        fail("metadata versionName mismatch")
    if not isinstance(meta["versionCode"], int) or meta["versionCode"] <= 0:
        fail("metadata versionCode must be a positive integer")
    if meta["phase"] not in {"planned", "development", "final"}:
        fail("metadata phase must be planned/development/final")
    if not isinstance(meta["branch"], str) or not meta["branch"].strip():
        fail("metadata branch missing")
    if not isinstance(meta["qaStatus"], str) or not meta["qaStatus"].strip():
        fail("metadata qaStatus missing")

    release = root / "RELEASE.md"
    if not release.is_file():
        fail(f"release document missing: {release}")
    release_text = release.read_text()

    if args.version not in release_text:
        fail("RELEASE.md does not mention versionName")
    if str(meta["versionCode"]) not in release_text:
        fail("RELEASE.md does not mention metadata versionCode")

    backlog = Path("BACKLOG.md").read_text()
    if f"## v{args.version}" not in backlog:
        fail("BACKLOG release section missing")

    line = status_line(args.version)
    if not line:
        fail("RELEASE_TEST_STATUS row missing")

    if args.planned:
        if meta["phase"] not in {"planned", "development", "final"}:
            fail("planned audit phase invalid")

    if args.final:
        if meta["phase"] != "final":
            fail("final release metadata phase is not final")

        sha = meta["appSourceSha"]
        if not isinstance(sha, str) or not re.fullmatch(r"[0-9a-f]{40}", sha):
            fail("final appSourceSha missing/invalid")

        run = str(meta["signedRun"] or "")
        if not run.isdigit():
            fail("final signedRun missing/invalid")

        tag = meta["tag"]
        if not isinstance(tag, str) or not tag.strip():
            fail("final release tag missing")

        if not meta["phoneTestDate"]:
            fail("final phoneTestDate missing")

        if "PASS" not in meta["qaStatus"].upper():
            fail("final qaStatus does not contain PASS")
        if "PASS" not in line.upper():
            fail("RELEASE_TEST_STATUS final row does not contain PASS")

        tag_commit = git_commit_for_tag(tag)
        if not tag_commit:
            fail(f"release tag not found locally: {tag}")
        if tag_commit != sha:
            fail(
                f"release tag {tag} points to {tag_commit}, "
                f"expected appSourceSha {sha}"
            )

        checkpoint = meta["checkpointTag"]
        if checkpoint:
            checkpoint_commit = git_commit_for_tag(checkpoint)
            if not checkpoint_commit:
                fail(f"checkpoint tag not found locally: {checkpoint}")
            if checkpoint_commit != sha:
                fail(
                    f"checkpoint tag {checkpoint} points to {checkpoint_commit}, "
                    f"expected appSourceSha {sha}"
                )

    if args.require_current_app:
        app_version, app_code = app_identity()
        if app_version != args.version:
            fail(
                f"current app version {app_version} != release {args.version}"
            )
        if app_code != meta["versionCode"]:
            fail(
                f"current app versionCode {app_code} != metadata "
                f"{meta['versionCode']}"
            )

    print("PASS:")
    print(f"- RELEASE_META v{args.version}")
    print(f"- phase: {meta['phase']}")
    print(f"- versionCode: {meta['versionCode']}")
    print(f"- root status row present")
    if args.final:
        print("- final source/run/tag evidence")
    if args.require_current_app:
        print("- current app identity matches metadata")


if __name__ == "__main__":
    main()
