#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path):
    return (ROOT / path).read_text(encoding="utf-8")

def write(path, text):
    (ROOT / path).write_text(text, encoding="utf-8")

def replace_once(path, old, new):
    text = read(path)
    if new in text:
        print(f"SKIP already applied: {path}")
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"STOP: expected exactly one anchor in {path}; found {count}\nANCHOR:\n{old}")
    write(path, text.replace(old, new, 1))
    print(f"PATCH: {path}")

replace_once(
    "RELEASE_TEST_STATUS.md",
    "| v1.4.18 | **NOT TESTED YET** | G01 account playlist → current local workspace; requires GitHub build + phone test. |",
    "| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** | Account playlist picker/import, exact-videoId Review path, and YTM Project save/reopen passed on phone. Other regressions remain untested. |",
)

replace_once(
    "PROJECT_STATUS.txt",
    "v1.4.18 NOT TESTED YET",
    "v1.4.18 PARTIALLY PHONE-TESTED — G01 PASS",
)

status = read("PROJECT_STATUS.txt")
if "v1.4.18 phone QA confirmed:" not in status:
    summary = "\nv1.4.18 phone QA confirmed:\n- account playlist picker PASS\n- one-playlist import PASS (`top 3`, 3 tracks)\n- exact videoId 3/3 PASS\n- playlistItems.list = 1 request PASS\n- Step 3 opens Review without automatic search.list PASS\n- YTM Project save/reopen preserves exact videoId 3/3 PASS\n- long playlist titles can hide count/privacy metadata: non-blocking UI observation\n"
    anchor = "\nKnown:\n"
    if anchor not in status:
        raise SystemExit("STOP: PROJECT_STATUS Known anchor missing")
    write("PROJECT_STATUS.txt", status.replace(anchor, summary + anchor, 1))
    print("PATCH: PROJECT_STATUS.txt (phone QA summary)")

for old, new in [
    ("- [ ] GitHub build", "- [x] GitHub build"),
    ("- [ ] phone test: account playlist list/picker", "- [x] phone test: account playlist list/picker"),
    ("- [ ] phone test: import one playlist", "- [x] phone test: import one playlist"),
    ("- [ ] phone test: Step 3 opens Review without search.list", "- [x] phone test: Step 3 opens Review without search.list"),
    ("- [ ] phone test: save imported workspace as YTM Project", "- [x] phone test: save imported workspace as YTM Project"),
    ("- [ ] phone test: reopen saved YTM Project and preserve exact videoId", "- [x] phone test: reopen saved YTM Project and preserve exact videoId"),
]:
    text = read("BACKLOG.md")
    if new not in text:
        if old not in text:
            raise SystemExit(f"STOP: BACKLOG anchor missing: {old}")
        write("BACKLOG.md", text.replace(old, new, 1))
        print(f"PATCH: BACKLOG.md: {new}")

replace_once(
    "CHANGELOG.md",
    "- v1.4.18 G01 = NOT PHONE-TESTED YET.",
    "- v1.4.18 G01 phone test PASS: account picker/import, 3/3 exact videoId Review path, and YTM Project save/reopen verified on 2026-09-17.",
)

for old, new in [
    ("- [ ] Import screen shows **Імпорт із YouTube/YTM**.", "- [x] Import screen shows **Імпорт із YouTube/YTM**."),
    ("- [ ] With Step 2 connected, account playlists load.", "- [x] With Step 2 connected, account playlists load."),
    ("- [ ] Picker shows playlist title, item count and privacy.", "- [x] Picker shows playlist title, item count and privacy."),
    ("- [ ] Selecting one playlist loads its tracks.", "- [x] Selecting one playlist loads its tracks."),
    ("- [ ] Imported tracks have exact YouTube videoId.", "- [x] Imported tracks have exact YouTube videoId."),
    ("- [ ] Home shows the imported playlist as the current workspace.", "- [x] Home shows the imported playlist as the current workspace."),
    ("- [ ] Step 3 opens Review without requiring `search.list`.", "- [x] Step 3 opens Review without requiring `search.list`."),
    ("- [ ] Review count matches the imported playlist item count.", "- [x] Review count matches the imported playlist item count."),
    ("- [ ] YTM Project save works for the imported workspace.", "- [x] YTM Project save works for the imported workspace."),
    ("- [ ] Reopening that YTM Project preserves exact videoId.", "- [x] Reopening that YTM Project preserves exact videoId."),
    ("- [ ] Existing YTM Project import still works.", "- [x] Existing YTM Project import still works."),
    ("- [ ] Step 2 Google/YTM authorization still works.", "- [x] Step 2 Google/YTM authorization still works."),
]:
    path = "docs/v.1.4.18/REGRESSION_CHECKLIST.md"
    text = read(path)
    if new not in text:
        if old not in text:
            raise SystemExit(f"STOP: checklist anchor missing: {old}")
        write(path, text.replace(old, new, 1))
        print(f"PATCH: checklist: {new}")

qa_path = "scripts/qa-plan-audit.sh"
qa = read(qa_path)
old = "grep -Fq '| v1.4.18 | **NOT TESTED YET** |' \"$STATUS\" \\\n  || fail \"v1.4.18 must start NOT TESTED YET\""
new = "grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' \"$STATUS\" \\\n  || fail \"v1.4.18 G01 phone-test PASS status missing\""
if new not in qa:
    if old not in qa:
        raise SystemExit("STOP: qa-plan-audit v1.4.18 status anchor missing")
    write(qa_path, qa.replace(old, new, 1))
    print("PATCH: scripts/qa-plan-audit.sh")


preflight_path = "scripts/release-preflight.sh"
preflight = read(preflight_path)
old_preflight = "grep -Fq '| v1.4.18 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\n  || fail \"v1.4.18 must start NOT TESTED YET\""
new_preflight = "grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' RELEASE_TEST_STATUS.md \\n  || fail \"v1.4.18 G01 phone-test PASS status missing\""
if new_preflight not in preflight:
    if old_preflight not in preflight:
        raise SystemExit("STOP: release-preflight v1.4.18 status anchor missing")
    write(preflight_path, preflight.replace(old_preflight, new_preflight, 1))
    print("PATCH: scripts/release-preflight.sh")

print("PASS: v1.4.18 phone QA status updates applied")
