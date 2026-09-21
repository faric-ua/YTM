#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

SCRIPT = Path(__file__).with_name("apply-v1447-r3-oauth-retry.py")

spec = importlib.util.spec_from_file_location("r3oauth_apply", SCRIPT)
if spec is None or spec.loader is None:
    raise SystemExit("FAIL: cannot load apply script")
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)

FIXTURES = {
    "app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt": mod.AUTH_STORE_OLD,
    "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt": mod.YOUTUBE_CLASS_OLD + "\n" + mod.YOUTUBE_REQUEST_OLD,
    "app/src/main/java/com/saney/ytmimporter/MainActivity.kt": mod.MAIN_IMPORT_OLD + "\n" + mod.MAIN_API_OLD + "\n" + mod.MAIN_IDENTITY_OLD,
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt": mod.IMPORT_IMPORT_OLD + "\n" + mod.IMPORT_API_OLD,
}


def write_fixture(root: Path) -> None:
    for rel, content in FIXTURES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="\n")


def run(root: Path, *args: str, expect: int = 0) -> str:
    p = subprocess.run(
        [sys.executable, "-B", str(SCRIPT), *args],
        cwd=root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if p.returncode != expect:
        raise SystemExit(
            f"FAIL: {' '.join(args) or 'apply'} returned {p.returncode}, expected {expect}\n{p.stdout}"
        )
    return p.stdout


with tempfile.TemporaryDirectory(prefix="r3-oauth-selftest-") as td:
    root = Path(td)
    write_fixture(root)

    run(root, "--check")
    run(root)
    run(root)
    run(root, "--check")

    helper = root / "app/src/main/java/com/saney/ytmimporter/auth/GoogleAccessTokenRecovery.kt"
    if helper.read_text(encoding="utf-8") != mod.GOOGLE_RECOVERY:
        raise SystemExit("FAIL: generated recovery helper mismatch")

    # Partial application: revert one independently patched file only.
    (root / "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt").write_text(
        FIXTURES["app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"],
        encoding="utf-8",
        newline="\n",
    )
    run(root, "--check")
    run(root)

    # Duplicate anchor must fail closed.
    dup = root / "duplicate"
    write_fixture(dup)
    p = dup / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
    p.write_text(p.read_text(encoding="utf-8") + "\n" + mod.MAIN_API_OLD, encoding="utf-8", newline="\n")
    run(dup, "--check", expect=1)

    # Missing anchor must fail closed.
    missing = root / "missing"
    write_fixture(missing)
    p = missing / "app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
    p.write_text(p.read_text(encoding="utf-8").replace(mod.YOUTUBE_REQUEST_OLD, "// missing\n"), encoding="utf-8", newline="\n")
    run(missing, "--check", expect=1)

print("PASS:")
print("- clean --check")
print("- first apply")
print("- second apply idempotent")
print("- partial application recovery")
print("- duplicate anchor fails closed")
print("- missing anchor fails closed")
