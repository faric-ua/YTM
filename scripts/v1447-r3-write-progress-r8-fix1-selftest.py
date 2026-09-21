#!/usr/bin/env python3
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-write-progress-r8-fix1.py"
OLD = 'for needle in [\n    \'"Оброблено ${progress.processedTracks}/${progress.totalTracks} • "\',\n    \'"Додано ${progress.job.addedCount} • "\',\n    "TrackStatus.DUPLICATE",\n    "palette.duplicate",\n    "TrackStatus.FAILED",\n    "palette.danger",\n]:\n    if needle not in relay:\n        raise SystemExit(f"FAIL: progress semantic state missing: {needle}")\n'

tmp = Path(tempfile.mkdtemp(prefix="ytm-r8-fix1-"))
target = tmp / "scripts/v1447-r3-navigation-progress-r7-audit.sh"
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(OLD, encoding="utf-8", newline="\n")

(tmp / "scripts/apply-v1447-r3-write-progress-r8-fix1.py").write_text(
    APPLY.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r = subprocess.run(
        ["python", "-B", "scripts/apply-v1447-r3-write-progress-r8-fix1.py", *args],
        cwd=tmp,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if r.returncode != 0:
        raise SystemExit(f"FAIL: exit={r.returncode}\n{r.stdout}")
    return r.stdout

run("--check")
print("PASS: clean --check")
run()
print("PASS: first apply")
run()
print("PASS: idempotent second apply")

patched = target.read_text(encoding="utf-8")
for needle in [
    '"Оброблено ${progress.processedTracks}/${progress.totalTracks} • "',
    '"Оброблено: ${progress.processedTracks}/${progress.totalTracks} • "',
    '"Додано ${progress.job.addedCount} • "',
    '"Додано: ${progress.job.addedCount} • "',
    "semantic_groups",
]:
    if needle not in patched:
        raise SystemExit(f"FAIL: missing patched contract: {needle}")

print("PASS: R8 FIX1 selftest complete")
