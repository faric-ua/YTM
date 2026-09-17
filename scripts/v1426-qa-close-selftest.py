#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path
import tempfile
import hashlib
import shutil
import subprocess

SCRIPT_DIR = Path(__file__).resolve().parent
PACKAGE_ROOT = SCRIPT_DIR.parent
sys.path.insert(0, str(SCRIPT_DIR))

from v1426_qa_close_patchlib import PatchError, OPS, apply_one, run, validate_ops

FIXTURE_FILES = {'RELEASE_TEST_STATUS.md': '# status\n'
                           '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** | prior |\n'
                           '| v1.4.26 | **NOT TESTED YET** | Selective Account Export: choose several '
                           'connected-account playlists, export only the selection, manifest schema v2 with '
                           'selectionMode. |\n',
 'PROJECT_STATUS.txt': 'YTM Importer\n'
                       'Version: 1.4.26\n'
                       'Version code: 60\n'
                       'v1.4.26 NOT TESTED YET\n'
                       '\n'
                       'v1.4.26 focus:\n'
                       '- selective export\n'
                       '\n'
                       'Future product direction:\n'
                       '- localization\n'
                       '\n'
                       'Known:\n'
                       'BUG-001/Q-001 OPEN\n'
                       'BUG-002/Q-002 DEFERRED\n'
                       'BUG-003/Q-003 CLOSED — PHONE RETEST PASS v1.4.20\n'
                       'BUG-004/Q-004 RETEST v1.4.17\n'
                       '\n'
                       'QA:\n'
                       'root qa/ = current definitions/policy\n',
 'CHANGELOG.md': '# Changelog\n\n## v1.4.26\n- feature\n- v1.4.26 = NOT PHONE-TESTED YET.\n\n## v1.4.25\n- previous\n',
 'docs/v.1.4.26/RELEASE.md': '# release\n\n## Status\n\n**NOT PHONE-TESTED YET**\n',
 'docs/v.1.4.26/REGRESSION_CHECKLIST.md': '# checklist\n'
                                          '\n'
                                          '## Selective export\n'
                                          '\n'
                                          '- [ ] `Вибрати плейлисти для експорту` action is visible;\n'
                                          '- [ ] playlist list is read-only;\n'
                                          '- [ ] multiple playlists can be selected;\n'
                                          '- [ ] previously selected items remain checked when picker is reopened;\n'
                                          '- [ ] zero selection does not open the folder picker;\n'
                                          '- [ ] confirmed selection survives Activity saved-instance-state '
                                          'restoration;\n'
                                          '- [ ] folder picker opens only after a non-empty selection;\n'
                                          '- [ ] only selected playlists are processed;\n'
                                          '- [ ] empty selected playlists are recorded as skipped;\n'
                                          '- [ ] failures are recorded per playlist rather than aborting the whole '
                                          'session;\n'
                                          '- [ ] exact videoId is preserved in exported YTM Project files;\n'
                                          '- [ ] source playlist id/privacy are preserved;\n'
                                          '- [ ] manifest uses schemaVersion 2;\n'
                                          '- [ ] manifest uses `selectionMode = SELECTED`;\n'
                                          '- [ ] manifest playlist count equals the selected-session record count;\n'
                                          '- [ ] no YouTube/YTM write API is used.\n'
                                          '\n'
                                          '## Phone evidence target\n'
                                          '\n'
                                          'target\n',
 'qa/BUG_REGISTER.md': '# bugs\n'
                       '\n'
                       '| ID | Status | Severity | Description | Related tests |\n'
                       '|---|---|---:|---|---|\n'
                       '| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. | '
                       'F-06 |\n'
                       '| BUG-002 / Q-002 | DEFERRED BY USER | P2 | dialog | M-02 |\n'
                       '| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | recovery | A-03 |\n'
                       '| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 '
                       'remains green/checked. | B-01 |\n'
                       '\n'
                       '## BUG-003 reproduction\n'
                       '\n'
                       'text\n',
 'OPEN_QUESTIONS.md': '# questions\n\n## Q-003 — old\n\ntext\n',
 'BACKLOG.md': '# roadmap\n'
               '\n'
               '## Current\n'
               'v1.4.26 — Selective Account Export\n'
               '\n'
               '## Known\n'
               '- BUG-001/Q-001 OPEN\n'
               '- BUG-002/Q-002 DEFERRED\n'
               '- BUG-003/Q-003 CLOSED — phone retest PASS on v1.4.20\n'
               '- BUG-004/Q-004 stale green authorization state — RETEST v1.4.17\n'
               '\n'
               '## v1.4.26\n'
               '- [x] feature\n'
               '- [ ] GitHub build\n'
               '- [ ] phone test: selective picker\n'
               '- [ ] phone test: exactly 2-playlist export\n'
               '- [ ] verify 2 projects + manifest\n'
               '- [ ] verify manifest `selectionMode = SELECTED`\n'
               '- [ ] reopen one exported project and verify exact videoId round trip\n'
               '\n'
               '## Next\n'
               'Expand the tutorial alongside the next useful feature wave. Preserve v1.4.25 as the Accent Card System '
               'phone-evidence baseline, then continue account-library work only if useful:\n'
               '- selective multi-playlist export;\n'
               '- import of a bulk-export manifest;\n'
               '- incremental/sync-style account backup.\n'
               '\n'
               '## Future product plan — localization + exclusive skin\n'
               '- [ ] Korean (`ko`) language\n'
               '\n'
               '## Later bug-fix wave\n'
               'later\n',
 'docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md': '# 06 export\n\n## 7. Практична вправа\n\ntest\n',
 'scripts/qa-plan-audit.sh': '#!/usr/bin/env bash\n'
                             'STATUS="RELEASE_TEST_STATUS.md"\n'
                             'BUG="qa/BUG_REGISTER.md"\n'
                             'grep -Fq \'| v1.4.26 | **NOT TESTED YET** |\' "$STATUS" \\\n'
                             '  || fail "v1.4.26 must start NOT TESTED YET"\n'
                             'grep -Fq \'| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |\' "$BUG" \\\n'
                             '  || fail "BUG-003 closed phone-retest status missing"\n',
 'scripts/v1426-selective-export-audit.sh': '#!/usr/bin/env bash\n'
                                            'STATUS="RELEASE_TEST_STATUS.md"\n'
                                            'grep -Fq \'| v1.4.26 | **NOT TESTED YET** |\' "$STATUS" || fail "v1.4.26 '
                                            'must start NOT TESTED YET"\n',
 'scripts/release-preflight.sh': '#!/usr/bin/env bash\n'
                                 'python -B scripts/v1426-apply-selftest.py\n'
                                 'bash scripts/v1426-selective-export-audit.sh\n'
                                 'bash scripts/qa-plan-audit.sh\n'}

PACKAGE_OWNED_RELS = ['docs/v.1.4.26/diagrams/PHONE_QA_SELECTIVE_EXPORT_2026-09-17.md', 'docs/v.1.4.26/qa/BUG_REGISTER.md', 'docs/v.1.4.26/qa/EVIDENCE_MANIFEST.md', 'docs/v.1.4.26/qa/PACKAGE_SELFTEST_QA_CLOSE.md', 'docs/v.1.4.26/qa/PHONE_TEST_REPORT_2026-09-17.md', 'docs/v.1.4.26/qa/TEST_DATA_SNAPSHOT_2026-09-17.md', 'docs/v.1.4.26/qa/TEST_RUN_2026-09-17.md', 'docs/v.1.4.26/qa/UI_SCREENSHOT_ANALYSIS_2026-09-17.md', 'docs/v.1.4.26/qa/evidence/EVIDENCE_01_IMPORT_ACTIONS.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_02_SELECTIVE_PICKER_TWO_SELECTED.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_03_EXPORT_RESULT_2_OF_2.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_04_EXPORT_FOLDER_2_PROJECTS_PLUS_MANIFEST.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_05_MANIFEST_SCHEMA2_SELECTED.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_06_ROUNDTRIP_HOME_3_EXACT_IDS.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_07_ROUNDTRIP_REVIEW_3_READY.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_08_BUG005_REDUNDANT_SEARCH_PLAN.jpg', 'docs/v.1.4.26/qa/evidence/EVIDENCE_09_ROUNDTRIP_TRACK_DETAIL.jpg', 'scripts/apply-v1.4.26-qa-close.py', 'scripts/v1426-qa-close-audit.sh', 'scripts/v1426-qa-close-selftest.py', 'scripts/v1426_qa_close_patchlib.py']

def write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")

def build_fixture(repo: Path) -> None:
    for rel, text in FIXTURE_FILES.items():
        write(repo, rel, text)

def copy_overlay(repo: Path) -> None:
    for rel in PACKAGE_OWNED_RELS:
        src = PACKAGE_ROOT / rel
        if not src.is_file():
            raise SystemExit(
                f"SELFTEST FAIL: package-owned file missing: {rel}"
            )
        dst = repo / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)

def tree_hash(root: Path) -> str:
    h = hashlib.sha256()
    for path in sorted(p for p in root.rglob("*") if p.is_file()):
        h.update(str(path.relative_to(root)).encode())
        h.update(b"\0")
        h.update(path.read_bytes())
        h.update(b"\0")
    return h.hexdigest()

def expect_error(fn, label: str) -> None:
    try:
        fn()
    except PatchError:
        print(f"PASS: {label}")
        return
    raise SystemExit(f"SELFTEST FAIL: expected PatchError: {label}")

def scan_text() -> None:
    for rel in PACKAGE_OWNED_RELS:
        path = PACKAGE_ROOT / rel
        if not path.is_file():
            raise SystemExit(
                f"SELFTEST FAIL: package-owned file missing: {rel}"
            )
        if path.suffix.lower() not in {".md", ".txt", ".py", ".sh", ".csv"}:
            continue
        data = path.read_bytes()
        if b"\r" in data:
            raise SystemExit(f"SELFTEST FAIL: CR in {path}")
        decoded = data.decode("utf-8")
        for i, line in enumerate(decoded.split("\n"), 1):
            if line.endswith(" ") or line.endswith("\t"):
                raise SystemExit(
                    f"SELFTEST FAIL: trailing whitespace {path}:{i}"
                )
    print(
        "PASS: package-owned text LF-only / no trailing whitespace"
    )

def main() -> int:
    validate_ops()
    print("PASS: production QA-close operations validated")
    scan_text()

    for path in SCRIPT_DIR.glob("*.py"):
        compile(path.read_text(encoding="utf-8"), str(path), "exec")
    print("PASS: Python syntax")

    bad = [("replace", "x.txt", r"line1\nline2", "new", "literal newline bad")]
    expect_error(lambda: validate_ops(bad), "literal \\n anchor rejected")

    with tempfile.TemporaryDirectory(prefix="ytm-v1426-qa-close-") as td:
        repo = Path(td) / "repo"
        repo.mkdir()
        build_fixture(repo)
        copy_overlay(repo)

        run(repo, dry_run=True)
        print("PASS: clean fixture --check")

        run(repo, dry_run=False)

        audit = subprocess.run(
            ["bash", "scripts/v1426-qa-close-audit.sh"],
            cwd=repo,
            text=True,
            capture_output=True
        )
        if audit.returncode != 0:
            print(audit.stdout)
            print(audit.stderr)
            raise SystemExit("SELFTEST FAIL: QA-close audit failed")
        print("PASS: QA-close audit on applied fixture")

        first = tree_hash(repo)
        run(repo, dry_run=True)
        print("PASS: post-apply --check")
        run(repo, dry_run=False)
        second = tree_hash(repo)
        if first != second:
            raise SystemExit("SELFTEST FAIL: repeat apply changed fixture")
        print("PASS: repeat apply idempotent")

        dup = Path(td) / "dup"
        dup.mkdir()
        write(dup, "x.txt", "A\nA\n")
        expect_error(
            lambda: apply_one(
                dup,
                ("replace", "x.txt", "A\n", "B\n", "duplicate anchor"),
                False
            ),
            "duplicate anchor fails closed"
        )

        miss = Path(td) / "miss"
        miss.mkdir()
        write(miss, "x.txt", "other\n")
        expect_error(
            lambda: apply_one(
                miss,
                ("replace", "x.txt", "A\n", "B\n", "missing anchor"),
                False
            ),
            "missing anchor fails closed"
        )

    print("PASS: v1.4.26 QA CLOSE PACKAGE SELFTEST COMPLETE")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
