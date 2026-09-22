#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

TEMPLATE_ROOT = Path("docs/assistant-kit/portable/templates")


def read_template(name):
    path = TEMPLATE_ROOT / name
    if not path.is_file():
        raise SystemExit(f"FAIL: template missing: {path}")
    return path.read_text()


def write_if_missing(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists():
        print(f"KEEP: {path}")
        return
    path.write_text(content.rstrip() + "\n")
    print(f"WROTE: {path}")


def render_template(name, project, version):
    return (
        read_template(name)
        .replace("[PROJECT]", project)
        .replace("[VERSION]", f"v{version}")
    )


def metadata(version, code, feature, branch):
    return {
        "schema": 1,
        "versionName": version,
        "versionCode": code,
        "phase": "planned",
        "feature": feature,
        "branch": branch,
        "appSourceSha": None,
        "signedRun": None,
        "qaStatus": "PLANNED — DOCUMENTATION SKELETON READY / APP CODE NOT STARTED",
        "tag": None,
        "checkpointTag": None,
        "phoneTestDate": None,
    }


def expected_paths(root):
    return [
        root / "RELEASE_META.json",
        root / "RELEASE.md",
        root / "REGRESSION_CHECKLIST.md",
        root / "qa" / "PHONE_TEST.md",
        root / "qa" / "BUG_REGISTER.md",
        root / "qa" / "EVIDENCE_MANIFEST.md",
        root / "diagrams" / "README.md",
    ]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", required=True)
    parser.add_argument("--code", required=True, type=int)
    parser.add_argument("--feature", required=True)
    parser.add_argument("--branch", required=True)
    parser.add_argument("--project", default="YTM Importer")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    root = Path(f"docs/v.{args.version}")

    if args.check:
        missing = [p.as_posix() for p in expected_paths(root) if not p.is_file()]
        if missing:
            raise SystemExit(
                "FAIL: release skeleton missing:\n" + "\n".join(missing)
            )

        meta = json.loads((root / "RELEASE_META.json").read_text())
        if meta.get("versionName") != args.version:
            raise SystemExit("FAIL: release metadata version mismatch")
        if meta.get("versionCode") != args.code:
            raise SystemExit("FAIL: release metadata versionCode mismatch")

        print(f"PASS: release skeleton v{args.version}")
        return

    release = render_template(
        "RELEASE.md.template",
        args.project,
        args.version,
    )
    release = release.replace(
        f"# {args.project} v{args.version} — Release",
        f"# {args.project} v{args.version} — {args.feature}",
        1,
    )
    release = release.replace(
        "## Version\n",
        (
            "## Version\n\n"
            f"- planned versionName: `{args.version}`\n"
            f"- planned versionCode: `{args.code}`\n"
            f"- branch: `{args.branch}`\n\n"
        ),
        1,
    )
    release = release.replace(
        "## Status\n",
        (
            "## Status\n\n"
            "**PLANNED — DOCUMENTATION SKELETON READY / APP CODE NOT STARTED**\n"
        ),
        1,
    )

    write_if_missing(
        root / "RELEASE_META.json",
        json.dumps(
            metadata(args.version, args.code, args.feature, args.branch),
            indent=2,
            ensure_ascii=False,
        ),
    )
    write_if_missing(root / "RELEASE.md", release)
    write_if_missing(
        root / "REGRESSION_CHECKLIST.md",
        render_template(
            "REGRESSION_CHECKLIST.md.template",
            args.project,
            args.version,
        ),
    )
    write_if_missing(
        root / "qa" / "PHONE_TEST.md",
        render_template(
            "PHONE_TEST.md.template",
            args.project,
            args.version,
        ),
    )
    write_if_missing(
        root / "qa" / "BUG_REGISTER.md",
        render_template(
            "BUG_REGISTER.md.template",
            args.project,
            args.version,
        ),
    )
    write_if_missing(
        root / "qa" / "EVIDENCE_MANIFEST.md",
        render_template(
            "EVIDENCE_MANIFEST.md.template",
            args.project,
            args.version,
        ),
    )
    write_if_missing(
        root / "diagrams" / "README.md",
        (
            f"# v{args.version} Diagrams\n\n"
            "Add Mermaid source diagrams for user-visible flow, navigation, "
            "lifecycle or system behavior changed by this release.\n\n"
            "Rendered binaries are optional; diffable Mermaid source is canonical.\n"
        ),
    )


if __name__ == "__main__":
    main()
