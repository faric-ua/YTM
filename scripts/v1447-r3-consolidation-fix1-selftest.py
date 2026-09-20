#!/usr/bin/env python3
from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-consolidation-fix1.py"

FIXTURE = r"""#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
SELECTOR="app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"
RECENT="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
STORAGE="app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
PLAYLIST="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
GRADLE="app/build.gradle.kts"

for f in "$UI" "$SELECTOR" "$RECENT" "$STORAGE" "$REVIEW" "$MENU" "$IMPORT" "$DATA" "$PLAYLIST" "$GRADLE"; do
  test -f "$f" || fail "missing lifecycle audit file: $f"
done

# Wave 1 intentionally stays on the R2 application identity until the whole R3 fix wave is ready.
grep -Fq 'versionCode = 89' "$GRADLE" || fail "Wave 1 unexpectedly changed versionCode"
grep -Fq 'versionName = "1.4.47-R2"' "$GRADLE" || fail "Wave 1 unexpectedly changed versionName"

echo "PASS:"
echo "- Wave 1 keeps app identity at v1.4.47-R2 / code 89 until full R3 scope is complete"
"""


def make_repo() -> Path:
    root = Path(tempfile.mkdtemp(prefix="ytm-r3-consolidation-fix1-"))
    (root / "scripts").mkdir(parents=True)
    (root / "scripts/apply-v1447-r3-consolidation-fix1.py").write_text(
        APPLY.read_text(encoding="utf-8"),
        encoding="utf-8",
        newline="\n",
    )
    (root / "scripts/v1447-r3-lifecycle-wave1-audit.sh").write_text(
        FIXTURE,
        encoding="utf-8",
        newline="\n",
    )
    return root


def run(root: Path, *args: str, expect: int = 0):
    r = subprocess.run(
        ["python", "-B", "scripts/apply-v1447-r3-consolidation-fix1.py", *args],
        cwd=root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if r.returncode != expect:
        raise SystemExit(
            f"FAIL: {' '.join(args)} exit={r.returncode}, expected={expect}\n{r.stdout}"
        )
    return r


def main() -> None:
    root = make_repo()
    run(root, "--check")
    print("PASS: clean --check")
    run(root)
    print("PASS: first apply")
    run(root)
    print("PASS: second apply idempotent")

    text = (root / "scripts/v1447-r3-lifecycle-wave1-audit.sh").read_text(encoding="utf-8")
    if 'versionCode = 89' in text or 'versionName = "1.4.47-R2"' in text:
        raise SystemExit("FAIL: current-version pin remains")
    if 'does **not** change `versionName` / `versionCode` yet' not in text:
        raise SystemExit("FAIL: historical documentation guard missing")
    print("PASS: current-version pin removed")

    duplicate = make_repo()
    p = duplicate / "scripts/v1447-r3-lifecycle-wave1-audit.sh"
    p.write_text(p.read_text(encoding="utf-8") + "\n" + FIXTURE, encoding="utf-8", newline="\n")
    if "anchor mismatch" not in run(duplicate, "--check", expect=1).stdout:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")

    print("PASS: consolidation FIX1 selftest complete")


if __name__ == "__main__":
    main()
