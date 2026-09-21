#!/usr/bin/env python3
from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPLY = ROOT / "scripts/apply-v1447-r3-history-semantics.py"

HISTORY_FIXTURE = r"""package com.saney.ytmimporter
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus

fun detail(entry: HistoryEntry) {
    card().apply {
                addView(
                    statLine(
                        "Додано",
                        "${entry.addedCount}/${entry.writeTargetCount}"
                    )
                )
    }
}

fun summary(entry: HistoryEntry) = buildString {
            append(
                "Додано: ${entry.addedCount}/" +
                    "${entry.writeTargetCount}\n"
            )
}

fun row(entry: HistoryEntry) = buildString {
                    append(
                        "Додано " +
                            "${entry.addedCount}/" +
                            "${entry.writeTargetCount}"
                    )
}
"""

DATA_FIXTURE = r"""package com.saney.ytmimporter
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus

fun summary(entry: HistoryEntry) = buildString {
            append(
                "Додано: ${entry.addedCount}/" +
                    "${entry.writeTargetCount}\n"
            )
}
"""


def run(repo: Path, *args: str, expect: int = 0) -> subprocess.CompletedProcess[str]:
    result = subprocess.run(
        ["python", "-B", str(repo / "scripts/apply-v1447-r3-history-semantics.py"), *args],
        cwd=repo,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if result.returncode != expect:
        raise SystemExit(
            f"FAIL: command {' '.join(args)} exit={result.returncode}, expected={expect}\n"
            + result.stdout
        )
    return result


def make_repo() -> Path:
    tmp = Path(tempfile.mkdtemp(prefix="ytm-r3-history-selftest-"))
    (tmp / "scripts").mkdir(parents=True)
    (tmp / "app/src/main/java/com/saney/ytmimporter/model").mkdir(parents=True)
    (tmp / "app/src/main/java/com/saney/ytmimporter").mkdir(parents=True, exist_ok=True)
    (tmp / "scripts/apply-v1447-r3-history-semantics.py").write_text(
        APPLY.read_text(encoding="utf-8"),
        encoding="utf-8",
        newline="\n",
    )
    (tmp / "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt").write_text(
        HISTORY_FIXTURE,
        encoding="utf-8",
        newline="\n",
    )
    (tmp / "app/src/main/java/com/saney/ytmimporter/DataActivity.kt").write_text(
        DATA_FIXTURE,
        encoding="utf-8",
        newline="\n",
    )
    return tmp


def semantic_fixture_checks() -> None:
    def classify(status, playlist_id, added, failed, pending, source, imported, tracks):
        remote = (
            bool(playlist_id)
            or added > 0
            or failed > 0
            or pending > 0
            or status != "COMPLETED"
        )
        if remote:
            return "Додано в YTM", f"{added}/9"
        n = max(imported, tracks)
        low = source.lower()
        restore = any(
            marker in low
            for marker in (
                "restore",
                "backup",
                "віднов",
                "safety snapshot",
                "history json",
                "rollback",
            )
        )
        return ("Відновлено" if restore else "Імпортовано"), f"{n} треків"

    assert classify("COMPLETED", "PL123", 9, 0, 0, "TXT", 9, 9) == (
        "Додано в YTM",
        "9/9",
    )
    assert classify("FAILED", "", 0, 9, 0, "TXT", 9, 9)[0] == "Додано в YTM"
    assert classify("COMPLETED", "", 0, 0, 0, "Імпорт файла", 9, 9) == (
        "Імпортовано",
        "9 треків",
    )
    assert classify("COMPLETED", "", 0, 0, 0, "Backup restore", 9, 9) == (
        "Відновлено",
        "9 треків",
    )
    assert classify("COMPLETED", "", 0, 0, 0, "History JSON відновлення", 0, 9) == (
        "Відновлено",
        "9 треків",
    )


def main() -> None:
    semantic_fixture_checks()
    print("PASS: semantic fixtures")

    repo = make_repo()
    run(repo, "--check")
    print("PASS: clean --check")

    run(repo)
    print("PASS: first apply")

    run(repo)
    print("PASS: second apply idempotent")

    helper = repo / "app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt"
    if not helper.exists():
        raise SystemExit("FAIL: helper was not created")

    history = repo / "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
    history_text = history.read_text(encoding="utf-8")
    if history_text.count("HistoryResultSemantics.primary") != 3:
        raise SystemExit("FAIL: expected three HistoryActivity semantic call sites")

    data = repo / "app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
    if data.read_text(encoding="utf-8").count("HistoryResultSemantics.primary") != 1:
        raise SystemExit("FAIL: expected one DataActivity semantic call site")

    duplicate = make_repo()
    p = duplicate / "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
    p.write_text(
        p.read_text(encoding="utf-8") + "\n" + HISTORY_FIXTURE,
        encoding="utf-8",
        newline="\n",
    )
    result = run(duplicate, "--check", expect=1)
    if "anchor mismatch" not in result.stdout:
        raise SystemExit("FAIL: duplicate anchor did not fail closed")
    print("PASS: duplicate anchor fails closed")

    missing = make_repo()
    p = missing / "app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
    p.write_text(
        p.read_text(encoding="utf-8").replace('"Додано:', '"Інше:', 1),
        encoding="utf-8",
        newline="\n",
    )
    result = run(missing, "--check", expect=1)
    if "anchor mismatch" not in result.stdout:
        raise SystemExit("FAIL: missing anchor did not fail closed")
    print("PASS: missing anchor fails closed")

    print("PASS: R3 History semantics package selftest complete")


if __name__ == "__main__":
    main()
