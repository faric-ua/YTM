#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from dataclasses import dataclass
from pathlib import Path


class PatchError(RuntimeError):
    pass


@dataclass(frozen=True)
class ReplaceOp:
    rel: str
    old: str
    new: str
    label: str


def read_text(root: Path, rel: str) -> str:
    path = root / rel
    if not path.is_file():
        raise PatchError(f"missing file: {rel}")
    return path.read_text(encoding="utf-8")


def write_text(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.write_text(text, encoding="utf-8", newline="\n")


def replace_once_or_skip(root: Path, op: ReplaceOp, dry_run: bool) -> None:
    text = read_text(root, op.rel)

    if op.new in text:
        print(f"SKIP already applied: {op.label}")
        return

    count = text.count(op.old)
    if count != 1:
        raise PatchError(
            f"{op.label}: expected exactly 1 old anchor; found {count}\n"
            f"FILE: {op.rel}\n"
            f"ANCHOR:\n{op.old[:1200]}"
        )

    if not dry_run:
        write_text(root, op.rel, text.replace(op.old, op.new, 1))

    print(("CHECK" if dry_run else "PATCH") + f": {op.label}")


def operations() -> list[ReplaceOp]:
    return [
        ReplaceOp(
            "RELEASE_TEST_STATUS.md",
            "| v1.4.25 | **NOT TESTED YET** | Accent Card System: consistent two-stroke accents on large cards, themed privacy radio and shorter search hints. |",
            "| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** | Blue Home/Review/Destination/History/Queue/Data and Neon Data/Service visual paths passed. Privacy radio, short hints and amber Security semantics confirmed. Import and non-empty Queue card were not separately tested. |",
            "release test status"
        ),
        ReplaceOp(
            "PROJECT_STATUS.txt",
            "v1.4.25 NOT TESTED YET",
            "v1.4.25 PARTIALLY PHONE-TESTED — ACCENT CARD TESTED PATHS PASS",
            "project status line"
        ),
        ReplaceOp(
            "CHANGELOG.md",
            "- v1.4.25 = NOT PHONE-TESTED YET.",
            "- v1.4.25 phone QA: PASS for tested Accent Card paths on Blue + Neon; Import and non-empty Queue card remain untested.\n"
            "- Added curated tutorial foundation under `docs/tutorial/`, built from preserved release/QA history.",
            "changelog v1.4.25 phone status"
        ),
        ReplaceOp(
            "scripts/qa-plan-audit.sh",
            "grep -Fq '| v1.4.25 | **NOT TESTED YET** |' \"$STATUS\" \\\n"
            "  || fail \"v1.4.25 must start NOT TESTED YET\"",
            "grep -Fq '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** |' \"$STATUS\" \\\n"
            "  || fail \"v1.4.25 tested-path phone status missing\"",
            "qa-plan v1.4.25 tested status guard"
        ),
        ReplaceOp(
            "scripts/release-preflight.sh",
            "grep -Fq '| v1.4.25 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\\n"
            "  || fail \"v1.4.25 must start NOT TESTED YET\"",
            "grep -Fq '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** |' RELEASE_TEST_STATUS.md \\\n"
            "  || fail \"v1.4.25 tested-path phone status missing\"",
            "release-preflight v1.4.25 tested status guard"
        ),
        ReplaceOp(
            "scripts/v1425-accent-card-audit.sh",
            "grep -Fq '| v1.4.25 | **NOT TESTED YET** |' \"$STATUS\" \\\n"
            "  || fail \"v1.4.25 NOT TESTED status missing\"",
            "grep -Fq '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** |' \"$STATUS\" \\\n"
            "  || fail \"v1.4.25 tested-path phone status missing\"",
            "accent-card audit tested status guard"
        ),
        ReplaceOp(
            "docs/v.1.4.25/RELEASE.md",
            "## Status\n\n**NOT PHONE-TESTED YET**",
            "## Status\n\n"
            "**PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS**\n\n"
            "## Phone validation — 2026-09-17\n\n"
            "Confirmed on a real Android phone:\n\n"
            "- Home workflow/current-playlist/main-track card accents;\n"
            "- Review large-card accents;\n"
            "- Destination Blue privacy radio tint;\n"
            "- History and Queue short search hints;\n"
            "- Data Blue + Neon large-card accents;\n"
            "- amber semantic `Безпека` strokes in both Blue and Neon;\n"
            "- Service Neon large-card accents.\n\n"
            "Not separately confirmed in this run:\n\n"
            "- v1.4.25 Import large-card screenshot;\n"
            "- non-empty Queue job card;\n"
            "- full functional regression of every search/write path.\n\n"
            "See `qa/TEST_RUN_2026-09-17.md` and the evidence manifest.",
            "release phone validation"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Home workflow card retains two accent strokes.",
            "- [x] Home workflow card retains two accent strokes.",
            "check Home workflow card"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Home current-playlist card now has two accent strokes.",
            "- [x] Home current-playlist card now has two accent strokes.",
            "check Home current playlist card"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] main track cards have two accent strokes.",
            "- [x] main track cards have two accent strokes.",
            "check main track cards"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Review large cards have two accent strokes.",
            "- [x] Review large cards have two accent strokes.",
            "check Review cards"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Destination large cards have two accent strokes.",
            "- [x] Destination large cards have two accent strokes.",
            "check Destination cards"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] History entry cards have two accent strokes.",
            "- [x] History entry cards have two accent strokes.",
            "check History cards"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Service cards have two accent strokes.",
            "- [x] Service cards have two accent strokes.",
            "check Service cards"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Data cards have two accent strokes.",
            "- [x] Data cards have two accent strokes.",
            "check Data cards"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] back buttons do not gain extra card strokes.",
            "- [x] back buttons do not gain extra card strokes.",
            "check quiet back buttons"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] search fields do not gain extra card strokes.",
            "- [x] search fields do not gain extra card strokes.",
            "check quiet search fields"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] compact Home utility buttons do not gain extra card strokes.",
            "- [x] compact Home utility buttons do not gain extra card strokes.",
            "check quiet Home utilities"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] `Безпека` remains amber.",
            "- [x] `Безпека` remains amber.",
            "check amber Security card"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] warning/error/success accents keep semantic colors.",
            "- [x] warning/error/success accents keep semantic colors.",
            "check semantic colors"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Destination selected privacy radio uses active theme accent.",
            "- [x] Destination selected privacy radio uses active theme accent.",
            "check Destination radio tint"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] History hint fits as `Пошук історії`.",
            "- [x] History hint fits as `Пошук історії`.",
            "check History hint"
        ),
        ReplaceOp(
            "docs/v.1.4.25/REGRESSION_CHECKLIST.md",
            "- [ ] Queue hint fits as `Пошук у черзі`.",
            "- [x] Queue hint fits as `Пошук у черзі`.",
            "check Queue hint"
        ),
    ]


THEME_SPOTCHECK = """## Theme spot-check

- [x] Blue Dark: Home / Review / Destination / History / Queue / Data.
- [x] Neon Dark: Data / Service.
- [ ] Green Dark utility-screen spot-check was not separately captured for v1.4.25.

"""


WORKFLOW_SECTION = """
## 15. Documentation as a learning asset

Documentation is a first-class project output, not an afterthought.

Keep two layers:

1. **Historical/evidence layer** — release folders, QA runs, bug records, screenshots, manifests, changelog. Preserve what actually happened, including failures and untested areas.
2. **Curated tutorial layer** — `docs/tutorial/`, which turns the real history into a step-by-step learning path.

Rules:

- document **why** a decision was made, not only what changed;
- keep failed approaches and the guard that was added because of them;
- never rewrite old QA evidence to make the project history look cleaner;
- sanitize personal identifiers before storing screenshots;
- tutorial chapters should point back to real code/releases whenever practical;
- future development should gradually expand the tutorial so the whole approach can be reproduced from a clean starting point.

The long-term goal is that the project can teach both the author and other developers how to recreate the development method step by step.
"""


PROJECT_PHONE_SUMMARY = """
v1.4.25 phone QA confirmed:
- Home workflow/current-playlist/main-track large-card accents PASS
- Review large-card accents PASS
- Destination Blue privacy radio tint PASS
- History short hint + history card accents PASS
- Queue short hint + empty state PASS
- Data Blue + Neon card accents PASS
- Security semantic amber strokes PASS in Blue + Neon
- Service Neon card accents PASS
- alternate-theme utility spot-check PASS (Neon Data + Service)

v1.4.25 still not separately phone-verified:
- Import large-card screenshot
- non-empty Queue job card
- full functional regression of every search/write path

"""


def custom_updates(root: Path, dry_run: bool) -> None:
    # Backlog: only mutate the v1.4.25 section.
    text = read_text(root, "BACKLOG.md")
    start = text.find("## v1.4.25\n")
    if start < 0:
        raise PatchError("BACKLOG v1.4.25 section missing")
    end = text.find("\n## Next\n", start)
    if end < 0:
        raise PatchError("BACKLOG ## Next marker missing")

    section = text[start:end]
    replacements = [
        ("- [ ] GitHub build", "- [x] GitHub build"),
        ("- [ ] phone test: Home large-card accents", "- [x] phone test: Home large-card accents"),
        ("- [ ] phone test: Destination radio tint", "- [x] phone test: Destination radio tint"),
        ("- [ ] phone test: History / Queue hints", "- [x] phone test: History / Queue hints"),
        ("- [ ] phone test: Data semantic card colors", "- [x] phone test: Data semantic card colors"),
        ("- [ ] navigation smoke", "- [x] navigation smoke"),
    ]

    changed = False
    for old, new in replacements:
        if new in section:
            continue
        if old not in section:
            raise PatchError(f"BACKLOG v1.4.25 missing line: {old}")
        section = section.replace(old, new, 1)
        changed = True

    extras = [
        "- [x] phone theme spot-check: Neon Data + Service",
        "- [x] tutorial foundation created from preserved release/QA history",
    ]
    for line in extras:
        if line not in section:
            section += line + "\n"
            changed = True

    if changed:
        if not dry_run:
            write_text(root, "BACKLOG.md", text[:start] + section + text[end:])
        print(("CHECK" if dry_run else "PATCH") + ": BACKLOG v1.4.25 phone/docs close")
    else:
        print("SKIP already applied: BACKLOG v1.4.25 phone/docs close")

    # Update Next statement conservatively.
    old_next = (
        "## Next\n"
        "Phone-test v1.4.25 Accent Card System. After the card language is stable, continue the visual/theme rollout and account-library work only if useful:"
    )
    new_next = (
        "## Next\n"
        "Expand the tutorial alongside the next useful feature wave. Preserve v1.4.25 as the Accent Card System phone-evidence baseline, then continue account-library work only if useful:"
    )
    op = ReplaceOp("BACKLOG.md", old_next, new_next, "BACKLOG next step")
    replace_once_or_skip(root, op, dry_run)

    # Add theme spot-check section to regression checklist.
    rel = "docs/v.1.4.25/REGRESSION_CHECKLIST.md"
    text = read_text(root, rel)
    if "## Theme spot-check" in text:
        print("SKIP already applied: regression theme spot-check section")
    else:
        marker = "## Functional smoke\n"
        if text.count(marker) != 1:
            raise PatchError("regression checklist Functional smoke marker missing/duplicated")
        if not dry_run:
            write_text(root, rel, text.replace(marker, THEME_SPOTCHECK + marker, 1))
        print(("CHECK" if dry_run else "PATCH") + ": regression theme spot-check section")

    # Project phone summary.
    rel = "PROJECT_STATUS.txt"
    text = read_text(root, rel)
    if "v1.4.25 phone QA confirmed:" in text:
        print("SKIP already applied: PROJECT_STATUS v1.4.25 phone summary")
    else:
        marker = "\nKnown:\n"
        if text.count(marker) != 1:
            raise PatchError("PROJECT_STATUS Known marker missing/duplicated")
        if not dry_run:
            write_text(root, rel, text.replace(marker, "\n" + PROJECT_PHONE_SUMMARY + marker, 1))
        print(("CHECK" if dry_run else "PATCH") + ": PROJECT_STATUS v1.4.25 phone summary")

    # Workflow documentation rule.
    rel = "YTM_ASSISTANT_WORKFLOW.md"
    text = read_text(root, rel)
    if "## 15. Documentation as a learning asset" in text:
        print("SKIP already applied: documentation learning-asset workflow rule")
    else:
        if not dry_run:
            write_text(root, rel, text.rstrip() + "\n\n" + WORKFLOW_SECTION.strip() + "\n")
        print(("CHECK" if dry_run else "PATCH") + ": documentation learning-asset workflow rule")


def run(root: Path, dry_run: bool = False) -> None:
    root = root.resolve()
    for op in operations():
        replace_once_or_skip(root, op, dry_run)
    custom_updates(root, dry_run)


def validate_ops(ops: list[ReplaceOp]) -> None:
    for op in ops:
        if r"\n" in op.old:
            raise PatchError(f"literal \\\\n anchor regression in: {op.label}")
        if r"\n" in op.new:
            raise PatchError(f"literal \\\\n replacement regression in: {op.label}")


def validate_operations() -> None:
    validate_ops(operations())


if __name__ == "__main__":
    validate_operations()
    print(f"PASS: {len(operations())} operations validated")
