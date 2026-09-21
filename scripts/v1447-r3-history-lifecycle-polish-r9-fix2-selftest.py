#!/usr/bin/env python3
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FIX = ROOT / "scripts/apply-v1447-r3-history-lifecycle-polish-r9-fix2.py"

tmp = Path(tempfile.mkdtemp(prefix="ytm-r9-fix2-"))
imp = tmp / "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
app = tmp / "scripts/apply-v1447-r3-history-lifecycle-polish-r9.py"
fix = tmp / "scripts" / FIX.name

imp.parent.mkdir(parents=True, exist_ok=True)
app.parent.mkdir(parents=True, exist_ok=True)

imp.write_text(
'''package x
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.HistoryStatus
import com.saney.ytmimporter.model.HistoryTrack
import com.saney.ytmimporter.storage.HistoryStore
import com.saney.ytmimporter.storage.HistoryStore
import java.util.UUID
import java.util.UUID
class X
''',
encoding="utf-8",
newline="\n",
)

app.write_text(
'''from pathlib import Path
def patch(op, do_apply):
    p=Path(op["file"])
    if not p.exists():
        raise SystemExit(f"FAIL: missing file: {p}")
    text=p.read_text(encoding="utf-8")
    old,new=op["old"],op["new"]
    old_count=text.count(old)
    new_count=text.count(new) if new else 0

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
        p.write_text(text.replace(old,new,1),encoding="utf-8",newline="\\n")
        print(f"APPLIED: {op['name']}")

def ensure_file(rel, content, do_apply):
    pass
''',
encoding="utf-8",
newline="\n",
)

fix.write_text(
    FIX.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r = subprocess.run(
        ["python", "-B", f"scripts/{FIX.name}", *args],
        cwd=tmp,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if r.returncode != 0:
        raise SystemExit(
            f"FAIL: exit={r.returncode}\n{r.stdout}"
        )
    return r.stdout

run("--check")
print("PASS: clean --check")
run()
print("PASS: first repair")
run()
print("PASS: idempotent second repair")

text = imp.read_text(encoding="utf-8")
for needle in [
    "import com.saney.ytmimporter.model.HistoryEntry",
    "import com.saney.ytmimporter.model.HistoryStatus",
    "import com.saney.ytmimporter.model.HistoryTrack",
    "import com.saney.ytmimporter.storage.HistoryStore",
    "import java.util.UUID",
]:
    if text.count(needle) != 1:
        raise SystemExit(f"FAIL: import dedupe: {needle}")

patched = app.read_text(encoding="utf-8")
for needle in [
    "substring_applied_ops",
    "already_applied_without_unique_new",
    '"Reuse History primary result"',
    "new_count >= 1",
]:
    if needle not in patched:
        raise SystemExit(f"FAIL: applier idempotence marker missing: {needle}")

print("PASS: R9 FIX2 selftest complete")
