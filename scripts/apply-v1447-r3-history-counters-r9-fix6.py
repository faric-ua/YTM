#!/usr/bin/env python3
from pathlib import Path
import sys

SEM = Path("app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt")
AUDIT = Path("scripts/v1447-r3-history-counters-r9-fix5-audit.sh")

DUP = '    private fun effectiveRemoteAddedCount(\n        entry: HistoryEntry\n    ): Int {\n        val safeLegacyNewPlaylistRecovery =\n            entry.destination ==\n                PendingDestination.NEW_PLAYLIST &&\n                entry.status ==\n                    HistoryStatus.COMPLETED &&\n                entry.addedCount == 0 &&\n                entry.writeTargetCount > 0 &&\n                entry.failedCount == 0 &&\n                entry.pendingCount == 0\n\n        return if (\n            safeLegacyNewPlaylistRecovery\n        ) {\n            entry.writeTargetCount\n        } else {\n            entry.addedCount\n        }\n    }\n\n    private fun effectiveRemoteAddedCount(\n        entry: HistoryEntry\n    ): Int {\n        val safeLegacyNewPlaylistRecovery =\n            entry.destination ==\n                PendingDestination.NEW_PLAYLIST &&\n                entry.status ==\n                    HistoryStatus.COMPLETED &&\n                entry.addedCount == 0 &&\n                entry.writeTargetCount > 0 &&\n                entry.failedCount == 0 &&\n                entry.pendingCount == 0\n\n        return if (\n            safeLegacyNewPlaylistRecovery\n        ) {\n            entry.writeTargetCount\n        } else {\n            entry.addedCount\n        }\n    }\n'
SINGLE = '    private fun effectiveRemoteAddedCount(\n        entry: HistoryEntry\n    ): Int {\n        val safeLegacyNewPlaylistRecovery =\n            entry.destination ==\n                PendingDestination.NEW_PLAYLIST &&\n                entry.status ==\n                    HistoryStatus.COMPLETED &&\n                entry.addedCount == 0 &&\n                entry.writeTargetCount > 0 &&\n                entry.failedCount == 0 &&\n                entry.pendingCount == 0\n\n        return if (\n            safeLegacyNewPlaylistRecovery\n        ) {\n            entry.writeTargetCount\n        } else {\n            entry.addedCount\n        }\n    }\n'
AUDIT_OLD = 'for n in [\n    "private fun effectiveRemoteAddedCount(",\n    "PendingDestination.NEW_PLAYLIST",\n    "HistoryStatus.COMPLETED",\n    "entry.addedCount == 0",\n    "entry.writeTargetCount > 0",\n    "entry.failedCount == 0",\n    "entry.pendingCount == 0",\n    "entry.writeTargetCount",\n]:\n    if n not in sem:\n        raise SystemExit(f"FAIL: legacy 0/N recovery guard missing: {n}")\n'
AUDIT_NEW = 'for n in [\n    "private fun effectiveRemoteAddedCount(",\n    "PendingDestination.NEW_PLAYLIST",\n    "HistoryStatus.COMPLETED",\n    "entry.addedCount == 0",\n    "entry.writeTargetCount > 0",\n    "entry.failedCount == 0",\n    "entry.pendingCount == 0",\n    "entry.writeTargetCount",\n]:\n    if n not in sem:\n        raise SystemExit(f"FAIL: legacy 0/N recovery guard missing: {n}")\n\nif sem.count("private fun effectiveRemoteAddedCount(") != 1:\n    raise SystemExit(\n        "FAIL: effectiveRemoteAddedCount must be declared exactly once"\n    )\n'

def main():
    check = "--check" in sys.argv[1:]

    if not SEM.exists():
        raise SystemExit(f"FAIL: missing {SEM}")
    if not AUDIT.exists():
        raise SystemExit(f"FAIL: missing {AUDIT}")

    sem = SEM.read_text(encoding="utf-8")
    dup_count = sem.count(DUP)
    decl_count = sem.count("private fun effectiveRemoteAddedCount(")

    if dup_count == 1:
        new_sem = sem.replace(DUP, SINGLE, 1)
        sem_changed = True
    elif dup_count == 0 and decl_count == 1:
        new_sem = sem
        sem_changed = False
    else:
        raise SystemExit(
            f"FAIL: unexpected HistoryResultSemantics state: "
            f"duplicate_block={dup_count}, declarations={decl_count}"
        )

    audit = AUDIT.read_text(encoding="utf-8")
    old_count = audit.count(AUDIT_OLD)
    new_count = audit.count(AUDIT_NEW)

    if new_count == 1:
        new_audit = audit
        audit_changed = False
    elif old_count == 1:
        new_audit = audit.replace(AUDIT_OLD, AUDIT_NEW, 1)
        audit_changed = True
    else:
        raise SystemExit(
            f"FAIL: FIX5 audit anchor mismatch: old={old_count}, new={new_count}"
        )

    if check:
        print(
            f"READY: remove_duplicate={sem_changed}, "
            f"harden_audit={audit_changed}"
        )
        print("PASS: R9 FIX6 check")
        return

    if sem_changed:
        SEM.write_text(new_sem, encoding="utf-8", newline="\n")
        print("APPLIED: removed duplicate effectiveRemoteAddedCount")
    else:
        print("SKIP: effectiveRemoteAddedCount already unique")

    if audit_changed:
        AUDIT.write_text(new_audit, encoding="utf-8", newline="\n")
        print("APPLIED: hardened FIX5 audit against duplicate declaration")
    else:
        print("SKIP: FIX5 audit already hardened")

    print("PASS: R9 FIX6 applied")

if __name__ == "__main__":
    main()
