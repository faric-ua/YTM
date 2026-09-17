#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path
import hashlib
import tempfile
import subprocess

SCRIPT_DIR = Path(__file__).resolve().parent
PACKAGE_ROOT = SCRIPT_DIR.parent
sys.path.insert(0, str(SCRIPT_DIR))

from v1426_patchlib import OPS, PatchError, _replace, run, validate_ops

FIXTURE_IMPORT = 'package com.saney.ytmimporter\nimport java.util.concurrent.Executors\n\nclass ImportActivity {\n    private val fileRequestCode =\n        2301\n\n    private val exportFolderRequestCode =\n        2302\n\n    private val executor =\n        Executors.newSingleThreadExecutor()\n\n    private lateinit var currentPlaylistStore:\n        CurrentPlaylistStore\n\n    override fun onCreate(\n        savedInstanceState: Bundle?\n    ) {\n        currentPlaylistStore =\n            CurrentPlaylistStore(this)\n\n        buildUi()\n    }\n\n    override fun onDestroy() {\n    }\n\n    override fun onActivityResult(\n        requestCode: Int,\n        resultCode: Int,\n        data: Intent?\n    ) {\n        when (requestCode) {\n            exportFolderRequestCode ->\n                data\n                    ?.data\n                    ?.let(\n                        ::exportAllYtmPlaylistsToFolder\n                    )\n        }\n    }\n\n    private fun buildUi() {\n                addView(\n                    actionButton(\n                        label =\n                            "Експортувати всі плейлисти в папку",\n                        primary = false,\n                        topMarginDp = 10\n                    ) {\n                        chooseYtmExportFolder()\n                    }\n                )\n    }\n\n    private fun chooseYtmExportFolder() {\n    }\n\n    private fun exportAllYtmPlaylistsToFolder(\n        treeUri: Uri\n    ) {\n        // historical export-all body\n    }\n\n    private data class BulkExportResult(\n        val folderName: String,\n        val manifestFile: String,\n        val playlistCount: Int,\n        val exportedProjects: Int,\n        val skippedPlaylists: Int,\n        val failedPlaylists: Int,\n        val playlistItemsRequests: Int\n    )\n\n    companion object {\n        const val EXTRA_IMPORT_MESSAGE =\n            "import_message"\n    }\n}\n'
FIXTURE_EXPORTER = 'object AccountLibraryExporter {\n    fun writeManifest(\n        resolver: ContentResolver,\n        session: ExportSession,\n        appVersion: String,\n        records: List<ExportRecord>\n    ): String {\n        val root =\n            JSONObject()\n                .put(\n                    "format",\n                    "ytm-importer-account-library-export"\n                )\n                .put("schemaVersion", 1)\n                .put("appVersion", appVersion)\n                .put("playlistCount", records.size)\n        return "manifest.json"\n    }\n}\n'
FIXTURE_STATUS = '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** | Blue Home/Review/Destination/History/Queue/Data and Neon Data/Service visual paths passed. Privacy radio, short hints and amber Security semantics confirmed. Import and non-empty Queue card were not separately tested. |\n'
FIXTURE_PROJECT = 'YTM Importer\nVersion: 1.4.25\nVersion code: 59\nv1.4.25 PARTIALLY PHONE-TESTED — ACCENT CARD TESTED PATHS PASS\n\nKnown:\nBUG-X\n'
FIXTURE_BACKLOG = '# YTM Importer — Roadmap\n\n## Current\nv1.4.25 — Accent Card System\n\n## v1.4.25\n- [x] done\n\n## Next\nExpand the tutorial alongside the next useful feature wave.\n\n## Later bug-fix wave\nFix later.\n'
FIXTURE_CHANGELOG = '# Журнал змін (Changelog)\n\n## v1.4.25\n- previous\n'
FIXTURE_QA_AUDIT = 'STATUS="RELEASE_TEST_STATUS.md"\nBUG="qa/BUG_REGISTER.md"\ngrep -Fq \'| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** |\' "$STATUS" \\\n  || fail "v1.4.25 tested-path phone status missing"\ngrep -Fq \'| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |\' "$BUG" \\\n  || fail "BUG-003 closed phone-retest status missing"\n'
FIXTURE_PREFLIGHT = 'check_file "docs/v.1.4.25/RELEASE.md"\ncheck_file "docs/v.1.4.25/REGRESSION_CHECKLIST.md"\npython scripts/v1425-apply-selftest.py\nbash scripts/v1425-accent-card-audit.sh\ncheck_file "scripts/v1425-apply-selftest.py"\ngrep -q \'versionCode = 59\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 59"\ngrep -q \'versionName = "1.4.25"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.25"\'\n'
FIXTURE_ROADMAP = '# Навчальний roadmap\n\n## План\n\n- `06_ANDROID_PROJECT_SETUP.md` — структура Android/Kotlin проєкту;\n- `07_IMPORT_AND_PROJECT_FORMAT.md` — імпорт та YTM Project;\n- `08_GOOGLE_YOUTUBE_AUTH.md` — авторизація, session state, recovery;\n- `09_YOUTUBE_API_AND_QUOTA.md` — API requests, quota, failure modes;\n- `10_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers;\n- `11_DESTINATION_AND_DUPLICATES.md` — existing playlist, duplicate scan, write plan;\n- `12_PENDING_QUEUE_AND_RECOVERY.md` — відкладені операції;\n- `13_BACKUP_AND_EXPORT.md` — local backup, account bulk export, manifest;\n- `14_THEME_SYSTEM.md` — palette, semantic colors, Accent Card System;\n- `15_GITHUB_ACTIONS_RELEASE.md` — signed APK, checksum, artifact;\n- `16_BUILD_A_FEATURE_FROM_ZERO.md` — повний практичний feature exercise;\n- `17_RECREATE_YTM_IMPORTER.md` — фінальний покроковий прохід від чистого repo.\n'

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
    write(root, "app/build.gradle.kts", 'defaultConfig {\n        versionCode = 59\n        versionName = "1.4.25"\n}\n')
    write(root, "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt", FIXTURE_IMPORT)
    write(root, "app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt", FIXTURE_EXPORTER)
    write(root, "RELEASE_TEST_STATUS.md", FIXTURE_STATUS)
    write(root, "PROJECT_STATUS.txt", FIXTURE_PROJECT)
    write(root, "BACKLOG.md", FIXTURE_BACKLOG)
    write(root, "CHANGELOG.md", FIXTURE_CHANGELOG)
    write(root, "scripts/qa-plan-audit.sh", FIXTURE_QA_AUDIT)
    write(root, "scripts/release-preflight.sh", FIXTURE_PREFLIGHT)
    write(root, "docs/tutorial/ROADMAP.md", FIXTURE_ROADMAP)

def copy_overlay(repo: Path) -> None:
    rels = [
        "docs/v.1.4.26/RELEASE.md",
        "docs/v.1.4.26/REGRESSION_CHECKLIST.md",
        "docs/v.1.4.26/qa/PHONE_TEST.md",
        "docs/v.1.4.26/diagrams/SELECTIVE_EXPORT_FLOW.md",
        "docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md",
        "scripts/v1426-apply-selftest.py",
        "scripts/v1426_patchlib.py",
        "scripts/apply-v1.4.26.py",
    ]
    for rel in rels:
        src = PACKAGE_ROOT / rel
        dst = repo / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_bytes(src.read_bytes())

def expect_error(fn, label: str) -> None:
    try:
        fn()
    except PatchError:
        print(f"PASS: {label}")
        return
    raise SystemExit(f"SELFTEST FAIL: expected PatchError: {label}")

def scan_package_text() -> None:
    generated = [
        PACKAGE_ROOT / "docs/v.1.4.26/RELEASE.md",
        PACKAGE_ROOT / "docs/v.1.4.26/REGRESSION_CHECKLIST.md",
        PACKAGE_ROOT / "docs/v.1.4.26/qa/PHONE_TEST.md",
        PACKAGE_ROOT / "docs/v.1.4.26/qa/PACKAGE_SELFTEST.md",
        PACKAGE_ROOT / "docs/v.1.4.26/diagrams/SELECTIVE_EXPORT_FLOW.md",
        PACKAGE_ROOT / "docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md",
        PACKAGE_ROOT / "scripts/apply-v1.4.26.py",
        PACKAGE_ROOT / "scripts/v1426_patchlib.py",
        PACKAGE_ROOT / "scripts/v1426-apply-selftest.py",
        PACKAGE_ROOT / "scripts/v1426-selective-export-audit.sh",
    ]

    for path in generated:
        if not path.is_file():
            raise SystemExit(
                f"SELFTEST FAIL: generated file missing: {path}"
            )
        data = path.read_bytes()
        if b"\r" in data:
            raise SystemExit(
                f"SELFTEST FAIL: CR character in {path}"
            )
        decoded = data.decode("utf-8")
        for line_no, line in enumerate(
            decoded.split("\n"),
            start=1
        ):
            if line.endswith(" ") or line.endswith("\t"):
                raise SystemExit(
                    f"SELFTEST FAIL: trailing whitespace "
                    f"in {path}:{line_no}"
                )

    print(
        "PASS: generated package text LF-only / "
        "no trailing whitespace"
    )

def assert_result(repo: Path) -> None:
    imp = (repo / "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt").read_text(encoding="utf-8")
    exp = (repo / "app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt").read_text(encoding="utf-8")
    tokens = [
        '"Вибрати плейлисти для експорту"',
        "setMultiChoiceItems",
        "selectiveExportFolderRequestCode",
        "override fun onSaveInstanceState",
        'selectionMode = "SELECTED"',
        'selectionMode = "ALL"',
        "playlists = selected",
        "private fun exportAccountPlaylistsToFolder",
    ]
    for token in tokens:
        if token not in imp:
            raise SystemExit(f"SELFTEST FAIL: missing result token: {token}")
    if '.put("schemaVersion", 2)' not in exp:
        raise SystemExit("SELFTEST FAIL: manifest schema v2 missing")
    if '"selectionMode"' not in exp:
        raise SystemExit("SELFTEST FAIL: manifest selectionMode missing")
    print("PASS: selective-export result structure")

def main() -> int:
    validate_ops()
    print("PASS: production patch definitions validated")

    bad = [("replace", "x.txt", r"line1\nline2", "replacement", "literal-newline regression")]
    expect_error(lambda: validate_ops(bad), "literal \\n anchor rejected")

    scan_package_text()

    for path in sorted(SCRIPT_DIR.glob("*.py")):
        compile(path.read_text(encoding="utf-8"), str(path), "exec")
    print("PASS: Python helper syntax")

    with tempfile.TemporaryDirectory(prefix="ytm-v1426-selftest-") as td:
        repo = Path(td) / "repo"
        repo.mkdir()
        build_fixture(repo)
        copy_overlay(repo)

        run(repo, dry_run=True)
        print("PASS: current-main fixture --check")

        run(repo, dry_run=False)
        assert_result(repo)

        print(
            "PASS: v1.4.26 frozen structural audit on applied fixture"
        )

        first = tree_hash(repo)
        print("PASS: current-main fixture first apply")

        run(repo, dry_run=True)
        print("PASS: post-apply --check")

        run(repo, dry_run=False)
        second = tree_hash(repo)
        if first != second:
            raise SystemExit("SELFTEST FAIL: repeat apply changed files")
        print("PASS: repeat apply idempotent")

        dup = Path(td) / "dup"
        dup.mkdir()
        write(dup, "x.txt", "ANCHOR\nANCHOR\n")
        expect_error(
            lambda: _replace(dup, "x.txt", "ANCHOR\n", "NEW\n", "duplicate anchor test", False),
            "duplicate anchor fails closed"
        )

        missing = Path(td) / "missing"
        missing.mkdir()
        write(missing, "x.txt", "other\n")
        expect_error(
            lambda: _replace(missing, "x.txt", "ANCHOR\n", "NEW\n", "missing anchor test", False),
            "missing anchor fails closed"
        )

    print("PASS: v1.4.26 APPLY SELFTEST COMPLETE")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
