#!/usr/bin/env python3
from __future__ import annotations

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

@dataclass(frozen=True)
class InsertBeforeOp:
    rel: str
    marker: str
    snippet: str
    token: str
    label: str

def _read(root: Path, rel: str) -> str:
    path = root / rel
    if not path.is_file():
        raise PatchError(f'missing file: {rel}')
    return path.read_text(encoding='utf-8')

def _write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')

def _replace(root: Path, op: ReplaceOp, dry_run: bool) -> str:
    text = _read(root, op.rel)
    if op.new in text:
        print(f'SKIP already applied: {op.label}')
        return 'new'
    count = text.count(op.old)
    if count != 1:
        raise PatchError(
            f'{op.label}: expected exactly 1 old anchor; found {count}\n'
            f'FILE: {op.rel}\nANCHOR:\n{op.old[:1200]}'
        )
    if not dry_run:
        _write(root, op.rel, text.replace(op.old, op.new, 1))
    print(('CHECK' if dry_run else 'PATCH') + f': {op.label}')
    return 'old'

def _insert_before(root: Path, op: InsertBeforeOp, dry_run: bool) -> str:
    text = _read(root, op.rel)
    if op.token in text:
        print(f'SKIP already applied: {op.label}')
        return 'new'
    count = text.count(op.marker)
    if count != 1:
        raise PatchError(
            f'{op.label}: expected exactly 1 marker; found {count}\n'
            f'FILE: {op.rel}\nMARKER:\n{op.marker[:1000]}'
        )
    if not dry_run:
        _write(root, op.rel, text.replace(op.marker, op.snippet + op.marker, 1))
    print(('CHECK' if dry_run else 'PATCH') + f': {op.label}')
    return 'old'

ROUNDED_OLD = '        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n'
ROUNDED_NEW = '        val useAccentStroke =\n            radiusDp >= 14 ||\n                color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n'
ROUNDED_FILES = ['app/src/main/java/com/saney/ytmimporter/ImportActivity.kt', 'app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt', 'app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', 'app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt', 'app/src/main/java/com/saney/ytmimporter/PendingActivity.kt', 'app/src/main/java/com/saney/ytmimporter/DataActivity.kt', 'app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt']

def operations() -> list[object]:
    ops: list[object] = [
        ReplaceOp('app/build.gradle.kts', '        versionCode = 58\n        versionName = "1.4.24"', '        versionCode = 59\n        versionName = "1.4.25"', 'release version'),
        ReplaceOp('app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt', '    fun neutralButtonDrawable(\n        context: Context,\n        radiusDp: Int = 12\n    ): Drawable =\n        surfaceDrawable(\n            context = context,\n            fill = palette(context).surfaceAlt,\n            radiusDp = radiusDp,\n            accentStroke = false\n        )\n\n', '    fun largeCardDrawable(\n        context: Context,\n        fill: Int = palette(context).surface,\n        radiusDp: Int = 14,\n        accentOverride: Int? = null\n    ): Drawable =\n        surfaceDrawable(\n            context = context,\n            fill = fill,\n            radiusDp = radiusDp,\n            accentStroke = true,\n            accentOverride = accentOverride\n        )\n\n    fun neutralButtonDrawable(\n        context: Context,\n        radiusDp: Int = 12\n    ): Drawable =\n        surfaceDrawable(\n            context = context,\n            fill = palette(context).surfaceAlt,\n            radiusDp = radiusDp,\n            accentStroke = false\n        )\n\n', 'AppThemeManager large-card drawable'),
        ReplaceOp('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '                background =\n                    AppThemeManager\n                        .surfaceDrawable(\n                            context =\n                                this@MainActivity,\n                            fill =\n                                palette.surface,\n                            radiusDp = 14,\n                            accentStroke = false\n                        )\n', '                background =\n                    AppThemeManager\n                        .largeCardDrawable(\n                            context =\n                                this@MainActivity,\n                            fill =\n                                palette.surface,\n                            radiusDp = 14\n                        )\n', 'Home current-playlist card accent'),
        ReplaceOp('app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt', '                background =\n                    GradientDrawable().apply {\n                        shape = GradientDrawable.RECTANGLE\n                        cornerRadius = dp(12).toFloat()\n                        setColor(palette.surface)\n                        setStroke(dp(1), palette.border)\n                    }\n', '                background =\n                    AppThemeManager.largeCardDrawable(\n                        context = context,\n                        fill = palette.surface,\n                        radiusDp = 12\n                    )\n', 'main track-card accent'),
        ReplaceOp('app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', '            setTextColor(Color.WHITE)\n            setPadding(0, dp(4), 0, dp(4))\n', '            setTextColor(Color.WHITE)\n            buttonTintList =\n                android.content.res.ColorStateList.valueOf(\n                    AppThemeManager\n                        .palette(this@DestinationActivity)\n                        .accent\n                )\n            setPadding(0, dp(4), 0, dp(4))\n', 'Destination privacy radio theme tint'),
        ReplaceOp('app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt', '            hint = "Пошук за назвою, джерелом або каналом"\n', '            hint = "Пошук історії"\n', 'History short search hint'),
        ReplaceOp('app/src/main/java/com/saney/ytmimporter/PendingActivity.kt', '                hint =\n                    "Пошук за плейлистом, джерелом або каналом"\n', '                hint =\n                    "Пошук у черзі"\n', 'Queue short search hint'),
        ReplaceOp('RELEASE_TEST_STATUS.md', '| v1.4.24 | **NOT TESTED YET** | Theme Wave 2: extend selected palette to Import, Review, Destination, History, Queue, Data and Service. |', '| v1.4.24 | **PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS** | Blue Dark Import/Review/History/Queue/Service/Data passed; Destination functional path passed. Findings: old privacy-radio tint and long History/Queue hints. Full alternate-theme Wave 2 regression not run. |\n| v1.4.25 | **NOT TESTED YET** | Accent Card System: consistent two-stroke accents on large cards, themed privacy radio and shorter search hints. |', 'release test status'),
        ReplaceOp('PROJECT_STATUS.txt', 'Version: 1.4.24\nVersion code: 58', 'Version: 1.4.25\nVersion code: 59', 'project version'),
        ReplaceOp('PROJECT_STATUS.txt', 'v1.4.24 NOT TESTED YET', 'v1.4.24 PARTIALLY PHONE-TESTED — WAVE 2 PASS WITH UI POLISH FINDINGS\nv1.4.25 NOT TESTED YET', 'project release status'),
        ReplaceOp('BACKLOG.md', '## Current\nv1.4.24 — Theme Wave 2', '## Current\nv1.4.25 — Accent Card System', 'BACKLOG current release'),
        ReplaceOp('BACKLOG.md', '## Next\nPhone-test v1.4.23 button fit. After the Home geometry is stable, continue the visual/theme rollout and account-library work only if useful:', '## Next\nPhone-test v1.4.25 Accent Card System. After the card language is stable, continue the visual/theme rollout and account-library work only if useful:', 'BACKLOG next action'),
        ReplaceOp('scripts/qa-plan-audit.sh', 'grep -Fq \'| v1.4.24 | **NOT TESTED YET** |\' "$STATUS" \\\n  || fail "v1.4.24 must start NOT TESTED YET"', 'grep -Fq \'| v1.4.24 | **PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS** |\' "$STATUS" \\\n  || fail "v1.4.24 Wave 2 phone status missing"\ngrep -Fq \'| v1.4.25 | **NOT TESTED YET** |\' "$STATUS" \\\n  || fail "v1.4.25 must start NOT TESTED YET"', 'qa-plan v1.4.25 guards'),
        ReplaceOp('scripts/release-preflight.sh', 'check_file "docs/v.1.4.24/RELEASE.md"', 'check_file "docs/v.1.4.25/RELEASE.md"', 'preflight release doc'),
        ReplaceOp('scripts/release-preflight.sh', 'check_file "docs/v.1.4.24/REGRESSION_CHECKLIST.md"', 'check_file "docs/v.1.4.25/REGRESSION_CHECKLIST.md"', 'preflight regression doc'),
        ReplaceOp('scripts/release-preflight.sh', 'bash scripts/v1424-theme-wave2-audit.sh', 'bash scripts/v1425-accent-card-audit.sh', 'active v1.4.25 audit'),
        ReplaceOp('scripts/release-preflight.sh', "grep -q 'versionCode = 58' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 58\"", "grep -q 'versionCode = 59' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 59\"", 'preflight versionCode'),
        ReplaceOp('scripts/release-preflight.sh', 'grep -q \'versionName = "1.4.24"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.24"\'', 'grep -q \'versionName = "1.4.25"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.25"\'', 'preflight versionName'),
        ReplaceOp('scripts/release-preflight.sh', 'grep -Fq \'| v1.4.24 | **NOT TESTED YET** |\' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.24 must start NOT TESTED YET"', 'grep -Fq \'| v1.4.24 | **PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS** |\' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.24 Wave 2 phone status missing"\ngrep -Fq \'| v1.4.25 | **NOT TESTED YET** |\' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.25 must start NOT TESTED YET"', 'preflight release-status guards'),
    ]
    for rel in ROUNDED_FILES:
        ops.append(ReplaceOp(rel, ROUNDED_OLD, ROUNDED_NEW, f'{Path(rel).name} large-card stroke rule'))
    return ops

def custom_updates(root: Path, dry_run: bool) -> None:
    project_focus = (
        "\nv1.4.25 focus:\n"
        "- Accent Card System\n"
        "- large cards use the same two quiet contour strokes as the Home 4-step card\n"
        "- small buttons/search/back controls remain visually quiet\n"
        "- semantic cards keep semantic accent colors\n"
        "- Destination privacy radio follows active theme\n"
        "- History/Queue search hints are shortened\n"
        "- package scripts must pass clean-apply + repeat/idempotence self-tests before delivery\n"
    )
    _insert_before(
        root,
        InsertBeforeOp(
            "PROJECT_STATUS.txt",
            "\nKnown:\n",
            project_focus,
            "v1.4.25 focus:",
            "project v1.4.25 focus"
        ),
        dry_run
    )

    text = _read(root, "BACKLOG.md")
    start = text.find("## v1.4.24\n")
    if start < 0:
        raise PatchError("BACKLOG v1.4.24 section missing")
    end = text.find("\n## ", start + 1)
    if end < 0:
        end = len(text)
    section = text[start:end]

    replacements = [
        ("- [ ] GitHub build", "- [x] GitHub build"),
        ("- [ ] phone test: Import", "- [x] phone test: Import"),
        ("- [ ] phone test: Review", "- [x] phone test: Review"),
        ("- [ ] phone test: Destination", "- [x] phone test: Destination"),
        ("- [ ] phone test: History / Queue / Data / Service", "- [x] phone test: History / Queue / Data / Service"),
    ]
    changed = False
    for old, new in replacements:
        if new in section:
            continue
        if old not in section:
            raise PatchError(f"BACKLOG v1.4.24 missing: {old}")
        section = section.replace(old, new, 1)
        changed = True

    finding_token = "privacy-radio tint + long search hints"
    if finding_token not in section:
        section += (
            "- [x] phone findings: privacy-radio tint + long search hints "
            "moved to v1.4.25\n"
        )
        changed = True

    if changed:
        if dry_run:
            print("CHECK: BACKLOG v1.4.24 phone results")
        else:
            _write(root, "BACKLOG.md", text[:start] + section + text[end:])
            print("PATCH: BACKLOG v1.4.24 phone results")
    else:
        print("SKIP already applied: BACKLOG v1.4.24 phone results")

    section25 = (
        "## v1.4.25\n"
        "- [x] add shared large-card accent drawable\n"
        "- [x] accent Home current-playlist card\n"
        "- [x] accent main track cards\n"
        "- [x] accent large cards across Import / Review / Destination / History / Queue / Data / Service\n"
        "- [x] keep small controls visually quiet\n"
        "- [x] theme Destination privacy radio\n"
        "- [x] shorten History / Queue search hints\n"
        "- [x] record v1.4.24 phone evidence + findings\n"
        "- [x] add clean/repeat apply self-test\n"
        "- [x] add assistant workflow package self-test rule\n"
        "- [x] add v1.4.25 static audit\n"
        "- [ ] GitHub build\n"
        "- [ ] phone test: Home large-card accents\n"
        "- [ ] phone test: Destination radio tint\n"
        "- [ ] phone test: History / Queue hints\n"
        "- [ ] phone test: Data semantic card colors\n"
        "- [ ] navigation smoke\n\n"
    )
    _insert_before(
        root,
        InsertBeforeOp(
            "BACKLOG.md",
            "## Next\n",
            section25,
            "## v1.4.25\n",
            "BACKLOG v1.4.25 section"
        ),
        dry_run
    )

    changelog = (
        "## v1.4.25\n"
        "- Added a consistent Accent Card System: large cards use two quiet theme-colored contour strokes.\n"
        "- Kept compact controls/search/back buttons visually quiet.\n"
        "- Added two-stroke accents to the Home current-playlist card and main track cards.\n"
        "- Large cards across Import, Review, Destination, History, Queue, Data and Service now follow the same accent rule.\n"
        "- Destination privacy radio now follows the active theme accent.\n"
        "- Shortened History/Queue search hints to avoid clipping.\n"
        "- Recorded v1.4.24 Theme Wave 2 phone evidence and UI-polish findings.\n"
        "- Added clean-apply + repeat/idempotence package self-tests and workflow policy.\n"
        "- versionCode 59 / versionName 1.4.25.\n"
        "- v1.4.25 = NOT PHONE-TESTED YET.\n\n"
    )
    _insert_before(
        root,
        InsertBeforeOp(
            "CHANGELOG.md",
            "## v1.4.24\n",
            changelog,
            "## v1.4.25\n",
            "CHANGELOG v1.4.25"
        ),
        dry_run
    )

    workflow_section = (
        "\n\n## 14. Package/apply self-test rule\n\n"
        "Before ChatGPT gives the user a new code-changing ZIP/apply script:\n\n"
        "1. Run a clean first-apply test against a fixture based on the current repository anchors.\n"
        "2. Run the same apply a second time and require idempotent PASS/SKIP behavior.\n"
        "3. Test duplicate-anchor and missing-anchor failure behavior.\n"
        "4. Reject patch definitions that accidentally contain literal `\\\\n` where real multiline newlines are required.\n"
        "5. Provide an apply script `--check` mode when practical so the phone can validate anchors before mutation.\n"
        "6. Do not ask the user to be the first execution environment for a newly generated patch script.\n\n"
        "Phone QA still remains necessary; these self-tests only prevent packaging/apply-script mistakes.\n"
    )
    text = _read(root, "YTM_ASSISTANT_WORKFLOW.md")
    if "## 14. Package/apply self-test rule" in text:
        print("SKIP already applied: assistant workflow self-test rule")
    else:
        if dry_run:
            print("CHECK: assistant workflow self-test rule")
        else:
            _write(root, "YTM_ASSISTANT_WORKFLOW.md", text.rstrip() + workflow_section)
            print("PATCH: assistant workflow self-test rule")

    preflight_lines = [
        'check_file "scripts/v1425-accent-card-audit.sh"\n',
        'check_file "scripts/v1425-apply-selftest.py"\n',
    ]
    text = _read(root, "scripts/release-preflight.sh")
    marker = 'check_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"\n'
    if marker not in text:
        raise PatchError("release-preflight insert marker missing")
    changed = False
    for line in preflight_lines:
        if line in text:
            continue
        if dry_run:
            print(f"CHECK: preflight requires {line.strip()}")
        else:
            text = text.replace(marker, line + marker, 1)
            changed = True
            print(f"PATCH: preflight requires {line.strip()}")
    run_line = "python scripts/v1425-apply-selftest.py\n"
    active_audit = "bash scripts/v1425-accent-card-audit.sh\n"
    old_active_audit = "bash scripts/v1424-theme-wave2-audit.sh\n"
    if run_line not in text:
        if dry_run:
            if active_audit not in text and old_active_audit not in text:
                raise PatchError(
                    "release-preflight active v1.4.24/v1.4.25 audit marker missing"
                )
            print("CHECK: release preflight runs v1.4.25 selftest")
        else:
            if active_audit not in text:
                raise PatchError(
                    "release-preflight active v1.4.25 audit marker missing"
                )
            text = text.replace(
                active_audit,
                run_line + active_audit,
                1
            )
            changed = True
            print("PATCH: release preflight runs v1.4.25 selftest")

    if changed and not dry_run:
        _write(root, "scripts/release-preflight.sh", text)

def run(root: Path, dry_run: bool = False) -> None:
    root = root.resolve()
    for op in operations():
        if not isinstance(op, ReplaceOp):
            raise PatchError(f"unknown operation: {op}")
        _replace(root, op, dry_run)
    custom_updates(root, dry_run)

def validate_operations(ops) -> None:
    for op in ops:
        if not isinstance(op, ReplaceOp):
            continue
        if r"\n" in op.old:
            raise PatchError(f"literal \\n anchor regression in: {op.label}")
        if r"\n" in op.new:
            raise PatchError(f"literal \\n replacement regression in: {op.label}")

def validate_patch_definitions() -> None:
    validate_operations(operations())

if __name__ == "__main__":
    validate_patch_definitions()
    print(f"PASS: {len(operations())} patch operations validated")
