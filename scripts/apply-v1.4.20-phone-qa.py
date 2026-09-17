#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

def write(rel, text):
    (ROOT / rel).write_text(text, encoding="utf-8")

def replace_once(rel, old, new):
    text = read(rel)
    if new in text:
        print(f"SKIP already applied: {rel}")
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(
            f"STOP: expected exactly 1 anchor in {rel}; found {count}\nANCHOR:\n{old}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {rel}")

replace_once(
    "RELEASE_TEST_STATUS.md",
    '| v1.4.20 | **NOT TESTED YET** | Import-screen account action button height/spacing fix; requires short phone UI smoke. |',
    '| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** | Import account-action layout passed; bulk-export folder picker still opens; BUG-003 in-place update recovery retest passed. |',
)

replace_once(
    "PROJECT_STATUS.txt",
    "v1.4.20 NOT TESTED YET",
    "v1.4.20 PARTIALLY PHONE-TESTED — UI SMOKE PASS",
)

replace_once(
    "PROJECT_STATUS.txt",
    "BUG-003/Q-003 RETEST v1.4.17",
    "BUG-003/Q-003 CLOSED — PHONE RETEST PASS v1.4.20",
)

status = read("PROJECT_STATUS.txt")
if "v1.4.20 phone QA confirmed:" not in status:
    anchor = "\nKnown:\n"
    summary = """
v1.4.20 phone QA confirmed:
- Import account action layout PASS
- bulk-export folder picker opens PASS
- BUG-003 in-place update recovery PASS: Step 2 briefly gray, then automatically green
- BUG-004 still requires invalid-auth/401 phone retest
"""
    if anchor not in status:
        raise SystemExit("STOP: PROJECT_STATUS Known anchor missing")
    write("PROJECT_STATUS.txt", status.replace(anchor, summary + anchor, 1))
    print("PATCH: PROJECT_STATUS.txt (v1.4.20 phone QA summary)")

replace_once(
    "qa/BUG_REGISTER.md",
    '| BUG-003 / Q-003 | DEFERRED FOR LATER FIX | P1 | Silent Google/YTM recovery after in-place update fails: Step 2 remains red. | A-03, D-03 |',
    '| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update verified: Step 2 briefly gray, then automatically green. | A-03, D-03 |',
)

bug = read("qa/BUG_REGISTER.md")
old_decision = """Decision:
- documented now;
- fix later in dedicated bug-fix wave;
- do not claim fixed without a successful in-place update phone test.
"""
new_decision = """Retest result:
- successful in-place update phone test completed on v1.4.20;
- Step 2 briefly showed gray while silent recovery ran;
- Step 2 automatically returned to green without manual re-authorization;
- BUG-003 is closed for this recovery scenario.
"""
if new_decision not in bug:
    if old_decision not in bug:
        raise SystemExit("STOP: BUG-003 decision block missing")
    write("qa/BUG_REGISTER.md", bug.replace(old_decision, new_decision, 1))
    print("PATCH: qa/BUG_REGISTER.md (BUG-003 retest detail)")

backlog = read("BACKLOG.md")
for old, new in [
    ("- [ ] GitHub build\n- [ ] phone test: Import button layout screenshot",
     "- [x] GitHub build\n- [x] phone test: Import button layout screenshot"),
    ("- [ ] phone smoke: bulk-export folder picker still opens",
     "- [x] phone smoke: bulk-export folder picker still opens"),
    ("- [ ] BUG-003 retest",
     "- [x] BUG-003 retest — PASS on v1.4.20 in-place update"),
]:
    if old in backlog:
        backlog = backlog.replace(old, new, 1)

backlog = backlog.replace(
    "- BUG-003/Q-003 auth recovery FAIL — RETEST v1.4.17",
    "- BUG-003/Q-003 CLOSED — phone retest PASS on v1.4.20",
    1
)
write("BACKLOG.md", backlog)
print("PATCH: BACKLOG.md")

replace_once(
    "CHANGELOG.md",
    "- v1.4.20 = NOT PHONE-TESTED YET.",
    "- v1.4.20 phone UI smoke PASS: account action labels/spacing verified, bulk-export folder picker opens; BUG-003 in-place update recovery retest PASS.",
)

check = "docs/v.1.4.20/REGRESSION_CHECKLIST.md"
updates = [
    ("- [ ] v1.4.20 installs successfully.", "- [x] v1.4.20 installs successfully."),
    ("- [ ] Open **1. Імпорт**.", "- [x] Open **1. Імпорт**."),
    ("- [ ] `Вибрати плейлист з YTM` is fully visible.", "- [x] `Вибрати плейлист з YTM` is fully visible."),
    ("- [ ] `Експортувати всі плейлисти в папку` is fully visible on two lines if needed.", "- [x] `Експортувати всі плейлисти в папку` is fully visible on two lines if needed."),
    ("- [ ] No text is vertically clipped.", "- [x] No text is vertically clipped."),
    ("- [ ] There is a visible gap between the two account actions.", "- [x] There is a visible gap between the two account actions."),
    ("- [ ] Bulk-export folder picker still opens.", "- [x] Bulk-export folder picker still opens."),
]
for old, new in updates:
    text = read(check)
    if new not in text:
        if old not in text:
            raise SystemExit(f"STOP: v1.4.20 checklist anchor missing: {old}")
        write(check, text.replace(old, new, 1))
        print(f"PATCH: {check}: {new}")

# Update QA audit status expectations and require BUG-003 closed state.
qa_path = "scripts/qa-plan-audit.sh"
qa = read(qa_path)
replace_old = """grep -Fq '| v1.4.20 | **NOT TESTED YET** |' "$STATUS" \\
  || fail "v1.4.20 must start NOT TESTED YET"
grep -Fq 'BUG-003 / Q-003' "$BUG" \\
  || fail "BUG-003 auth recovery regression missing"
"""
replace_new = """grep -Fq '| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** |' "$STATUS" \\
  || fail "v1.4.20 UI-smoke PASS status missing"
grep -Fq '| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |' "$BUG" \\
  || fail "BUG-003 closed phone-retest status missing"
"""
if replace_new not in qa:
    if replace_old not in qa:
        raise SystemExit("STOP: qa-plan-audit v1.4.20/BUG-003 block missing")
    write(qa_path, qa.replace(replace_old, replace_new, 1))
    print("PATCH: scripts/qa-plan-audit.sh")

# Update release preflight status expectations.
pf_path = "scripts/release-preflight.sh"
pf = read(pf_path)
old_pf = """grep -Fq '| v1.4.20 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.20 must start NOT TESTED YET"
grep -Fq 'BUG-003 / Q-003' qa/BUG_REGISTER.md \\
  || fail "BUG-003 must stay documented"
echo "- BUG-003 auth recovery failure preserved"
"""
new_pf = """grep -Fq '| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.20 UI-smoke PASS status missing"
grep -Fq '| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |' qa/BUG_REGISTER.md \\
  || fail "BUG-003 closed phone-retest status missing"
echo "- BUG-003 in-place update recovery phone retest passed"
"""
if new_pf not in pf:
    if old_pf not in pf:
        raise SystemExit("STOP: release-preflight v1.4.20/BUG-003 block missing")
    write(pf_path, pf.replace(old_pf, new_pf, 1))
    print("PATCH: scripts/release-preflight.sh")

print()
print("PASS: v1.4.20 phone QA + BUG-003 closure applied")
