#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

HELPER_PATH = ROOT / "app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt"

HELPER_CONTENT = r'''package com.saney.ytmimporter.model

enum class HistoryResultKind {
    YTM_WRITE,
    IMPORT,
    RESTORE
}

data class HistoryPrimaryResult(
    val kind: HistoryResultKind,
    val label: String,
    val value: String
)

/**
 * Presentation semantics for History's primary result line.
 *
 * Legacy/local History entries do not carry an explicit operation kind.
 * Remote-write evidence therefore wins first. Only a clean COMPLETED entry
 * with no remote-write evidence is treated as a local import/restore.
 */
object HistoryResultSemantics {
    fun primary(
        entry: HistoryEntry
    ): HistoryPrimaryResult {
        if (hasRemoteWriteEvidence(entry)) {
            return HistoryPrimaryResult(
                kind = HistoryResultKind.YTM_WRITE,
                label = "Додано в YTM",
                value =
                    "${entry.addedCount}/${entry.writeTargetCount}"
            )
        }

        val localCount =
            maxOf(
                entry.totalImportedCount,
                entry.tracks.size
            )

        return if (looksLikeRestore(entry.sourceLabel)) {
            HistoryPrimaryResult(
                kind = HistoryResultKind.RESTORE,
                label = "Відновлено",
                value = "$localCount треків"
            )
        } else {
            HistoryPrimaryResult(
                kind = HistoryResultKind.IMPORT,
                label = "Імпортовано",
                value = "$localCount треків"
            )
        }
    }

    private fun hasRemoteWriteEvidence(
        entry: HistoryEntry
    ): Boolean =
        !entry.playlistId.isNullOrBlank() ||
            entry.addedCount > 0 ||
            entry.failedCount > 0 ||
            entry.pendingCount > 0 ||
            entry.status != HistoryStatus.COMPLETED

    private fun looksLikeRestore(
        sourceLabel: String
    ): Boolean {
        val normalized =
            sourceLabel.lowercase()

        return listOf(
            "restore",
            "backup",
            "віднов",
            "safety snapshot",
            "history json",
            "rollback"
        ).any { marker ->
            marker in normalized
        }
    }
}
'''

OPS = [
    {
        "name": "HistoryActivity semantics import",
        "path": "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt",
        "old": r'''import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
''',
        "new": r'''import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryResultSemantics
import com.saney.ytmimporter.model.HistoryStatus
''',
    },
    {
        "name": "History detail primary result",
        "path": "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt",
        "old": r'''                addView(
                    statLine(
                        "Додано",
                        "${entry.addedCount}/${entry.writeTargetCount}"
                    )
                )
''',
        "new": r'''                val primaryResult =
                    HistoryResultSemantics.primary(entry)

                addView(
                    statLine(
                        primaryResult.label,
                        primaryResult.value
                    )
                )
''',
    },
    {
        "name": "History copied summary primary result",
        "path": "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt",
        "old": r'''            append(
                "Додано: ${entry.addedCount}/" +
                    "${entry.writeTargetCount}\n"
            )
''',
        "new": r'''            val primaryResult =
                HistoryResultSemantics.primary(entry)

            append(
                "${primaryResult.label}: " +
                    "${primaryResult.value}\n"
            )
''',
    },
    {
        "name": "History list row primary result",
        "path": "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt",
        "old": r'''                    append(
                        "Додано " +
                            "${entry.addedCount}/" +
                            "${entry.writeTargetCount}"
                    )
''',
        "new": r'''                    val primaryResult =
                        HistoryResultSemantics.primary(
                            entry
                        )

                    append(
                        primaryResult.label +
                            " " +
                            primaryResult.value
                    )
''',
    },
    {
        "name": "DataActivity semantics import",
        "path": "app/src/main/java/com/saney/ytmimporter/DataActivity.kt",
        "old": r'''import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
''',
        "new": r'''import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryResultSemantics
import com.saney.ytmimporter.model.HistoryStatus
''',
    },
    {
        "name": "Data History summary primary result",
        "path": "app/src/main/java/com/saney/ytmimporter/DataActivity.kt",
        "old": r'''            append(
                "Додано: ${entry.addedCount}/" +
                    "${entry.writeTargetCount}\n"
            )
''',
        "new": r'''            val primaryResult =
                HistoryResultSemantics.primary(entry)

            append(
                "${primaryResult.label}: " +
                    "${primaryResult.value}\n"
            )
''',
    },
]


def check_helper(apply: bool) -> None:
    if HELPER_PATH.exists():
        current = HELPER_PATH.read_text(encoding="utf-8")
        if current == HELPER_CONTENT:
            print("SKIP: already applied: History result semantics helper")
            return
        raise SystemExit(
            f"FAIL: conflicting existing helper: {HELPER_PATH.relative_to(ROOT)}"
        )

    print("READY: History result semantics helper")
    if apply:
        HELPER_PATH.parent.mkdir(parents=True, exist_ok=True)
        HELPER_PATH.write_text(HELPER_CONTENT, encoding="utf-8", newline="\n")
        print("APPLIED: History result semantics helper")


def handle_op(op: dict[str, str], apply: bool) -> None:
    path = ROOT / op["path"]
    text = path.read_text(encoding="utf-8")

    old_count = text.count(op["old"])
    new_count = text.count(op["new"])

    if old_count == 1 and new_count == 0:
        print(f"READY: {op['name']}")
        if apply:
            text = text.replace(op["old"], op["new"], 1)
            path.write_text(text, encoding="utf-8", newline="\n")
            print(f"APPLIED: {op['name']}")
        return

    if old_count == 0 and new_count == 1:
        print(f"SKIP: already applied: {op['name']}")
        return

    raise SystemExit(
        "FAIL: anchor mismatch for "
        f"{op['name']}: old={old_count}, new={new_count}, file={op['path']}"
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    check_helper(apply=not args.check)

    for op in OPS:
        handle_op(op, apply=not args.check)

    if args.check:
        print("PASS: R3 History semantics anchors are ready or already applied")
    else:
        print("PASS: R3 History semantics patch applied")


if __name__ == "__main__":
    main()
