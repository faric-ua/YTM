#!/usr/bin/env python3
from pathlib import Path
import sys

IMPORT = Path("app/src/main/java/com/saney/ytmimporter/ImportActivity.kt")
APPLIER = Path("scripts/apply-v1447-r3-history-lifecycle-polish-r9.py")

TARGET_IMPORTS = [
    "import com.saney.ytmimporter.model.HistoryEntry",
    "import com.saney.ytmimporter.model.HistoryStatus",
    "import com.saney.ytmimporter.model.HistoryTrack",
    "import com.saney.ytmimporter.storage.HistoryStore",
    "import java.util.UUID",
]

NEW_PATCH = '''def patch(op, do_apply):
    p=Path(op["file"])
    if not p.exists():
        raise SystemExit(f"FAIL: missing file: {p}")

    text=p.read_text(encoding="utf-8")
    old,new=op["old"],op["new"]
    old_count=text.count(old)
    new_count=text.count(new) if new else 0

    substring_applied_ops={
        "ImportActivity history model imports",
        "ImportActivity HistoryStore import",
        "ImportActivity UUID import",
    }

    already_applied_without_unique_new={
        "Reuse History primary result",
    }

    if (
        op["name"] in substring_applied_ops and
        new and
        new_count >= 1
    ):
        print(f"SKIP: already applied: {op['name']}")
        return

    if (
        op["name"] in already_applied_without_unique_new and
        old_count == 0
    ):
        print(f"SKIP: already applied: {op['name']}")
        return

    if new and new_count==1 and old_count==0:
        print(f"SKIP: already applied: {op['name']}")
        return

    if not new and old_count==0:
        print(f"SKIP: already removed: {op['name']}")
        return

    if old_count!=1:
        raise SystemExit(
            f"FAIL: R9 anchor mismatch for {op['name']}: old={old_count}, new={new_count}"
        )

    print(f"READY: {op['name']}")
    if do_apply:
        p.write_text(
            text.replace(old,new,1),
            encoding="utf-8",
            newline="\\n"
        )
        print(f"APPLIED: {op['name']}")

'''

def dedupe_imports(text: str):
    lines = text.splitlines()
    seen = {needle: False for needle in TARGET_IMPORTS}
    out = []
    changed = False

    for line in lines:
        if line in seen:
            if seen[line]:
                changed = True
                continue
            seen[line] = True
        out.append(line)

    for needle, found in seen.items():
        if not found:
            raise SystemExit(f"FAIL: expected R9 import missing: {needle}")

    return "\n".join(out) + "\n", changed

def patch_applier(text: str):
    marker_start = "def patch(op, do_apply):"
    marker_end = "def ensure_file(rel, content, do_apply):"

    start = text.find(marker_start)
    end = text.find(marker_end)

    if start < 0 or end < 0 or end <= start:
        raise SystemExit(
            "FAIL: could not locate patch()/ensure_file() boundaries in R9 applier"
        )

    current = text[start:end]

    if (
        "substring_applied_ops" in current and
        "already_applied_without_unique_new" in current and
        '"Reuse History primary result"' in current
    ):
        return text, False

    return text[:start] + NEW_PATCH + text[end:], True

def main():
    check = "--check" in sys.argv[1:]

    if not IMPORT.exists():
        raise SystemExit(f"FAIL: missing {IMPORT}")
    if not APPLIER.exists():
        raise SystemExit(f"FAIL: missing {APPLIER}")

    import_text, import_changed = dedupe_imports(
        IMPORT.read_text(encoding="utf-8")
    )
    applier_text, applier_changed = patch_applier(
        APPLIER.read_text(encoding="utf-8")
    )

    for needle in TARGET_IMPORTS:
        if import_text.count(needle) != 1:
            raise SystemExit(
                f"FAIL: normalized import count for {needle} is "
                f"{import_text.count(needle)}, expected 1"
            )

    if check:
        print(
            "READY: "
            f"dedupe_imports={import_changed}, "
            f"patch_applier={applier_changed}"
        )
        print("PASS: R9 FIX2 check")
        return

    if import_changed:
        IMPORT.write_text(
            import_text,
            encoding="utf-8",
            newline="\n"
        )
        print("APPLIED: deduplicated R9 ImportActivity imports")
    else:
        print("SKIP: R9 ImportActivity imports already unique")

    if applier_changed:
        APPLIER.write_text(
            applier_text,
            encoding="utf-8",
            newline="\n"
        )
        print("APPLIED: replaced R9 patch() idempotence logic")
    else:
        print("SKIP: R9 patch() idempotence logic already fixed")

    print("PASS: R9 FIX2 applied")

if __name__ == "__main__":
    main()
