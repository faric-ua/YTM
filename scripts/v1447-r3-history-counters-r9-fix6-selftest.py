#!/usr/bin/env python3
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-history-counters-r9-fix6.py"

DUP = '    private fun effectiveRemoteAddedCount(\n        entry: HistoryEntry\n    ): Int {\n        val safeLegacyNewPlaylistRecovery =\n            entry.destination ==\n                PendingDestination.NEW_PLAYLIST &&\n                entry.status ==\n                    HistoryStatus.COMPLETED &&\n                entry.addedCount == 0 &&\n                entry.writeTargetCount > 0 &&\n                entry.failedCount == 0 &&\n                entry.pendingCount == 0\n\n        return if (\n            safeLegacyNewPlaylistRecovery\n        ) {\n            entry.writeTargetCount\n        } else {\n            entry.addedCount\n        }\n    }\n\n    private fun effectiveRemoteAddedCount(\n        entry: HistoryEntry\n    ): Int {\n        val safeLegacyNewPlaylistRecovery =\n            entry.destination ==\n                PendingDestination.NEW_PLAYLIST &&\n                entry.status ==\n                    HistoryStatus.COMPLETED &&\n                entry.addedCount == 0 &&\n                entry.writeTargetCount > 0 &&\n                entry.failedCount == 0 &&\n                entry.pendingCount == 0\n\n        return if (\n            safeLegacyNewPlaylistRecovery\n        ) {\n            entry.writeTargetCount\n        } else {\n            entry.addedCount\n        }\n    }\n'
AUDIT_OLD = 'for n in [\n    "private fun effectiveRemoteAddedCount(",\n    "PendingDestination.NEW_PLAYLIST",\n    "HistoryStatus.COMPLETED",\n    "entry.addedCount == 0",\n    "entry.writeTargetCount > 0",\n    "entry.failedCount == 0",\n    "entry.pendingCount == 0",\n    "entry.writeTargetCount",\n]:\n    if n not in sem:\n        raise SystemExit(f"FAIL: legacy 0/N recovery guard missing: {n}")\n'

tmp = Path(tempfile.mkdtemp(prefix="ytm-r9-fix6-"))
sem = tmp/"app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt"
audit = tmp/"scripts/v1447-r3-history-counters-r9-fix5-audit.sh"
sem.parent.mkdir(parents=True, exist_ok=True)
audit.parent.mkdir(parents=True, exist_ok=True)

sem.write_text(
    "object X {\n" + DUP + "\n}\n",
    encoding="utf-8",
    newline="\n",
)
audit.write_text(
    AUDIT_OLD,
    encoding="utf-8",
    newline="\n",
)

(tmp/"scripts"/APPLY.name).write_text(
    APPLY.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r = subprocess.run(
        ["python","-B",f"scripts/{APPLY.name}",*args],
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
print("PASS: first repair")
run()
print("PASS: idempotent second repair")

sem_text = sem.read_text(encoding="utf-8")
if sem_text.count("private fun effectiveRemoteAddedCount(") != 1:
    raise SystemExit("FAIL: duplicate declaration remains")

audit_text = audit.read_text(encoding="utf-8")
if 'sem.count("private fun effectiveRemoteAddedCount(") != 1' not in audit_text:
    raise SystemExit("FAIL: duplicate guard not added to FIX5 audit")

print("PASS: R9 FIX6 selftest complete")
