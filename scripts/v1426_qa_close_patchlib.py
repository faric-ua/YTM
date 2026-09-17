#!/usr/bin/env python3
from __future__ import annotations
import sys
sys.dont_write_bytecode = True
from pathlib import Path

class PatchError(RuntimeError):
    pass

OPS = [('replace',
  'RELEASE_TEST_STATUS.md',
  '| v1.4.26 | **NOT TESTED YET** | Selective Account Export: choose several connected-account playlists, export only '
  'the selection, manifest schema v2 with selectionMode. |',
  '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** | Two-playlist selective '
  'export passed: 2 projects + manifest, schema v2/SELECTED, exact-videoId round trip 3/3. Manual Search then proposed '
  'redundant search.list for exact tracks (BUG-005). |',
  'v1.4.26 release phone status'),
 ('replace',
  'PROJECT_STATUS.txt',
  'v1.4.26 NOT TESTED YET',
  'v1.4.26 PARTIALLY PHONE-TESTED — SELECTIVE EXPORT PASS / BUG-005 FOUND',
  'PROJECT_STATUS v1.4.26 status'),
 ('insert',
  'PROJECT_STATUS.txt',
  '\nKnown:\n',
  '\n'
  'v1.4.26 phone QA confirmed:\n'
  '- selective-export action visible\n'
  '- exactly 2 playlists selected\n'
  '- export result: 2 selected / 2 projects / 0 skipped / 0 failed\n'
  '- playlistItems.list = 2 requests\n'
  '- export folder = 2 YTM Projects + manifest\n'
  '- manifest schemaVersion = 2\n'
  '- manifest selectionMode = SELECTED\n'
  '- manifest playlistCount = 2\n'
  '- round-trip `top 3` project exact videoId = 3/3\n'
  '- Review = 3/3 ready\n'
  '\n'
  'v1.4.26 phone QA finding:\n'
  '- BUG-005 / Q-005 OPEN\n'
  '- manual Search proposed 3 new search.list calls despite 3/3 exact videoId\n'
  '- user did not press Start, so redundant quota was not consumed\n'
  '- planned next release: v1.4.27 Exact-ID Search Guard\n'
  '\n',
  'v1.4.26 phone QA confirmed:',
  'PROJECT_STATUS phone QA summary'),
 ('replace',
  'PROJECT_STATUS.txt',
  'BUG-004/Q-004 RETEST v1.4.17',
  'BUG-004/Q-004 RETEST v1.4.17\nBUG-005/Q-005 OPEN — redundant manual search for exact videoId tracks',
  'PROJECT_STATUS BUG-005 known'),
 ('replace',
  'CHANGELOG.md',
  '- v1.4.26 = NOT PHONE-TESTED YET.',
  '- v1.4.26 phone QA: PASS for the selective-export path (2 selected → 2 projects + manifest → exact-videoId round '
  'trip 3/3).\n'
  '- BUG-005 found: manual Search still proposes new search.list requests for already-exact tracks; user did not '
  'execute the redundant search.',
  'CHANGELOG v1.4.26 phone QA'),
 ('replace',
  'docs/v.1.4.26/RELEASE.md',
  '**NOT PHONE-TESTED YET**',
  '**PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND**\n'
  '\n'
  '## Phone validation — 2026-09-17\n'
  '\n'
  'Confirmed on a real Android phone:\n'
  '\n'
  '- selective export action visible alongside existing account actions;\n'
  '- checkbox multi-select opened and exactly 2 playlists were selected;\n'
  '- export completed with 2 projects, 0 skipped, 0 failed;\n'
  '- `playlistItems.list = 2`;\n'
  '- session folder contained exactly 2 YTM Projects + `manifest.json`;\n'
  '- manifest used `schemaVersion = 2`;\n'
  '- manifest used `selectionMode = SELECTED`;\n'
  '- manifest `playlistCount = 2`;\n'
  '- exported `top 3` project reopened with exact videoId 3/3;\n'
  '- Review showed 3/3 ready.\n'
  '\n'
  '### Finding — BUG-005\n'
  '\n'
  'Manual `Пошук` on the restored exact project proposed three new `search.list`\n'
  'requests despite all three tracks already having exact videoId.\n'
  '\n'
  'The user did not press `Почати`, so no unnecessary quota was consumed.\n'
  '\n'
  'Planned fix: **v1.4.27 — Exact-ID Search Guard**.\n'
  '\n'
  'Not separately phone-verified in this run:\n'
  '\n'
  '- zero-selection guard;\n'
  '- selection restoration after Activity recreation;\n'
  '- export-all `selectionMode = ALL`;\n'
  '- selected-empty playlist skip behavior;\n'
  '- selected-playlist failure continuation;\n'
  '- older single-account-import regression.\n',
  'v1.4.26 release phone validation'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] `Вибрати плейлисти для експорту` action is visible;',
  '- [x] `Вибрати плейлисти для експорту` action is visible;',
  'check selective action'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] playlist list is read-only;',
  '- [x] playlist list is read-only; (static audit: no account write API in Import flow)',
  'check read-only invariant'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] multiple playlists can be selected;',
  '- [x] multiple playlists can be selected;',
  'check multi-select'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] only selected playlists are processed;',
  '- [x] only selected playlists are processed; (2 selected → 2 playlistItems requests → 2 project files)',
  'check selected-only processing'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] exact videoId is preserved in exported YTM Project files;',
  '- [x] exact videoId is preserved in exported YTM Project files; (round trip 3/3)',
  'check exact videoId roundtrip'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] manifest uses schemaVersion 2;',
  '- [x] manifest uses schemaVersion 2;',
  'check manifest schema'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] manifest uses `selectionMode = SELECTED`;',
  '- [x] manifest uses `selectionMode = SELECTED`;',
  'check selection mode'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] manifest playlist count equals the selected-session record count;',
  '- [x] manifest playlist count equals the selected-session record count; (2)',
  'check manifest count'),
 ('replace',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '- [ ] no YouTube/YTM write API is used.',
  '- [x] no YouTube/YTM write API is used. (static audit)',
  'check no write API'),
 ('insert',
  'docs/v.1.4.26/REGRESSION_CHECKLIST.md',
  '## Phone evidence target\n',
  '## Finding\n'
  '\n'
  '- [x] BUG-005 reproduced: manual Search proposes new `search.list` for exact-videoId tracks.\n'
  '- [x] redundant search was **not** executed during QA.\n'
  '- [ ] v1.4.27 retest: exact 3/3 project → manual Search requires 0 new search.list.\n'
  '\n',
  '## Finding\n',
  'add BUG-005 checklist finding'),
 ('replace',
  'qa/BUG_REGISTER.md',
  '| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. | '
  'B-01 |',
  '| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. | '
  'B-01 |\n'
  '| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 | P2 | Manual Search plans new search.list requests for tracks that '
  'already have exact videoId. | v1.4.26 round-trip / manual Search |',
  'BUG register row'),
 ('append',
  'qa/BUG_REGISTER.md',
  '## BUG-005 reproduction\n'
  '\n'
  'Preconditions:\n'
  '- reopen/import a YTM Project with exact videoId already present;\n'
  '- phone evidence used `top 3`, exact videoId 3/3.\n'
  '\n'
  'Steps:\n'
  '1. Confirm Home/Review show all tracks ready.\n'
  '2. Open manual `Пошук`.\n'
  '3. Inspect the search plan.\n'
  '4. Do not press `Почати`.\n'
  '\n'
  'Actual:\n'
  '- search required for all 3 tracks;\n'
  '- 3 new `search.list` requests proposed.\n'
  '\n'
  'Expected:\n'
  '- exact-videoId tracks are excluded from search planning;\n'
  '- 3/3 exact tracks should require 0 new search.list requests.\n'
  '\n'
  'Impact:\n'
  '- can waste limited search quota if the user explicitly starts the redundant search;\n'
  '- no quota was wasted in the recorded reproduction because `Почати` was not pressed.\n'
  '\n'
  'Planned fix:\n'
  '- v1.4.27 Exact-ID Search Guard.\n'
  '\n',
  '## BUG-005 reproduction',
  'BUG-005 reproduction'),
 ('append',
  'OPEN_QUESTIONS.md',
  '## Q-005 — Manual Search should respect exact videoId\n'
  '\n'
  'Status: **OPEN — BUG-005, planned for v1.4.27.**\n'
  '\n'
  'Found during the v1.4.26 selective-export round-trip phone test.\n'
  '\n'
  'The re-imported `top 3` YTM Project correctly restored 3/3 exact videoId values,\n'
  'but opening manual `Пошук` still produced a plan for 3 new `search.list` calls.\n'
  '\n'
  'Decision for v1.4.27:\n'
  '\n'
  '- exact-videoId tracks must not be included in ordinary search planning;\n'
  '- if every track is exact, the UI should report that search is unnecessary\n'
  '  instead of offering quota-consuming work;\n'
  '- the user may still need an explicit future "re-search/replace" action if they\n'
  '  intentionally want to discard an exact selection.\n'
  '\n'
  'This separates "find missing matches" from "force a new search".\n',
  '## Q-005 — Manual Search should respect exact videoId',
  'Q-005 open question'),
 ('replace',
  'BACKLOG.md',
  '- BUG-004/Q-004 stale green authorization state — RETEST v1.4.17',
  '- BUG-004/Q-004 stale green authorization state — RETEST v1.4.17\n'
  '- BUG-005/Q-005 redundant manual search for exact videoId tracks — OPEN, v1.4.27',
  'BACKLOG known BUG-005'),
 ('replace',
  'BACKLOG.md',
  '- [ ] GitHub build\n- [ ] phone test: selective picker',
  '- [x] GitHub build\n- [x] phone test: selective picker',
  'BACKLOG v1.4.26 build + selective picker'),
 ('replace',
  'BACKLOG.md',
  '- [ ] phone test: exactly 2-playlist export',
  '- [x] phone test: exactly 2-playlist export',
  'BACKLOG 2 playlist export'),
 ('replace',
  'BACKLOG.md',
  '- [ ] verify 2 projects + manifest',
  '- [x] verify 2 projects + manifest',
  'BACKLOG files verify'),
 ('replace',
  'BACKLOG.md',
  '- [ ] verify manifest `selectionMode = SELECTED`',
  '- [x] verify manifest `selectionMode = SELECTED`',
  'BACKLOG manifest selected'),
 ('replace',
  'BACKLOG.md',
  '- [ ] reopen one exported project and verify exact videoId round trip',
  '- [x] reopen one exported project and verify exact videoId round trip',
  'BACKLOG roundtrip'),
 ('insert',
  'BACKLOG.md',
  '## Next\n',
  '## v1.4.27 — Exact-ID Search Guard\n'
  '- [ ] exclude tracks with exact/canonical videoId from ordinary search planning\n'
  '- [ ] when all tracks are exact, show 0 required searches / no quota work\n'
  '- [ ] preserve explicit manual candidate selections\n'
  '- [ ] keep an intentional future re-search path separate from normal search\n'
  '- [ ] add BUG-005 static regression audit\n'
  '- [ ] add release docs + phone-test plan\n'
  '- [ ] GitHub build\n'
  '- [ ] phone retest: reopen exact `top 3` project\n'
  '- [ ] phone retest: Search plan = 0 new search.list\n'
  '- [ ] confirm Review remains 3/3 ready\n'
  '\n',
  '## v1.4.27 — Exact-ID Search Guard',
  'BACKLOG v1.4.27 section'),
 ('replace',
  'BACKLOG.md',
  '## Next\n'
  'Expand the tutorial alongside the next useful feature wave. Preserve v1.4.25 as the Accent Card System '
  'phone-evidence baseline, then continue account-library work only if useful:',
  '## Next\nv1.4.27 — fix BUG-005 with an Exact-ID Search Guard before adding another account-library feature:',
  'BACKLOG Next v1.4.27'),
 ('append',
  'docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md',
  '## 8. Що знайшов реальний phone QA: quota invariant\n'
  '\n'
  'v1.4.26 пройшов основний selective-export round trip:\n'
  '\n'
  '`2 selected playlists → 2 projects + manifest → schema v2/SELECTED → re-import → exact videoId 3/3`\n'
  '\n'
  'Але після успішного re-import ручна кнопка `Пошук` показала план на три нові\n'
  '`search.list` запити.\n'
  '\n'
  'Це важлива різниця між двома твердженнями:\n'
  '\n'
  '1. **дані збережені правильно** — PASS;\n'
  "2. **усі наступні дії правильно використовують ці дані** — не обов'язково.\n"
  '\n'
  'Саме тому round-trip test не повинен закінчуватися на читанні файлу. Треба\n'
  'пройти наступну user action і перевірити, чи downstream logic поважає\n'
  'відновлений state.\n'
  '\n'
  '### Invariant для v1.4.27\n'
  '\n'
  'Якщо трек уже має canonical exact `videoId`, звичайний search planning не повинен\n'
  'витрачати quota на цей трек.\n'
  '\n'
  'Майбутня примусова заміна exact selection, якщо вона буде потрібна, повинна бути\n'
  'окремою свідомою дією користувача, а не побічним ефектом кнопки `Пошук`.\n'
  '\n'
  'Цей дефект записаний як **BUG-005 / Q-005** і стає навчальним прикладом:\n'
  'successful persistence не гарантує correct downstream behavior.\n',
  '## 8. Що знайшов реальний phone QA: quota invariant',
  'tutorial BUG-005 lesson'),
 ('replace',
  'scripts/qa-plan-audit.sh',
  'grep -Fq \'| v1.4.26 | **NOT TESTED YET** |\' "$STATUS" \\\n  || fail "v1.4.26 must start NOT TESTED YET"',
  'grep -Fq \'| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** |\' "$STATUS" \\\n'
  '  || fail "v1.4.26 selective-export phone status missing"',
  'qa-plan v1.4.26 status'),
 ('insert',
  'scripts/qa-plan-audit.sh',
  'grep -Fq \'| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |\' "$BUG" \\\n',
  'grep -Fq \'| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 |\' "$BUG" \\\n  || fail "BUG-005 phone finding missing"\n',
  'BUG-005 phone finding missing',
  'qa-plan BUG-005 guard'),
 ('replace',
  'scripts/v1426-selective-export-audit.sh',
  'grep -Fq \'| v1.4.26 | **NOT TESTED YET** |\' "$STATUS" || fail "v1.4.26 must start NOT TESTED YET"',
  'grep -Fq \'| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** |\' "$STATUS" || '
  'fail "v1.4.26 selective-export phone status missing"',
  'v1426 audit phone status'),
 ('insert',
  'scripts/release-preflight.sh',
  'bash scripts/v1426-selective-export-audit.sh\n',
  'bash scripts/v1426-qa-close-audit.sh\n',
  'bash scripts/v1426-qa-close-audit.sh',
  'release preflight QA-close audit')]

def read_text(root: Path, rel: str) -> str:
    path = root / rel
    if not path.is_file():
        raise PatchError(f"missing file: {rel}")
    return path.read_text(encoding="utf-8")

def write_text(root: Path, rel: str, text: str) -> None:
    (root / rel).write_text(text, encoding="utf-8", newline="\n")

def apply_one(root: Path, op, dry_run: bool) -> None:
    kind = op[0]
    if kind == "replace":
        _, rel, old, new, label = op
        text = read_text(root, rel)
        if new in text:
            print(f"SKIP already applied: {label}")
            return
        count = text.count(old)
        if count != 1:
            raise PatchError(f"{label}: expected 1 anchor, found {count} in {rel}")
        if not dry_run:
            write_text(root, rel, text.replace(old, new, 1))
        print(("CHECK" if dry_run else "PATCH") + f": {label}")
        return

    if kind == "insert":
        _, rel, marker, snippet, token, label = op
        text = read_text(root, rel)
        if token in text:
            print(f"SKIP already applied: {label}")
            return
        count = text.count(marker)
        if count != 1:
            raise PatchError(f"{label}: expected 1 marker, found {count} in {rel}")
        if not dry_run:
            write_text(root, rel, text.replace(marker, snippet + marker, 1))
        print(("CHECK" if dry_run else "PATCH") + f": {label}")
        return

    if kind == "append":
        _, rel, snippet, token, label = op
        text = read_text(root, rel)
        if token in text:
            print(f"SKIP already applied: {label}")
            return
        if not dry_run:
            write_text(root, rel, text.rstrip() + "\n\n" + snippet.strip() + "\n")
        print(("CHECK" if dry_run else "PATCH") + f": {label}")
        return

    raise PatchError(f"unknown op: {kind}")

def run(root: Path, dry_run: bool = False) -> None:
    for op in OPS:
        apply_one(root.resolve(), op, dry_run)

def validate_ops(ops=OPS) -> None:
    for op in ops:
        for value in op[2:-1]:
            if isinstance(value, str) and r"\n" in value and "\n" not in value:
                raise PatchError(f"literal \\n regression in {op[-1]}")

if __name__ == "__main__":
    validate_ops()
    print(f"PASS: {len(OPS)} QA-close operations validated")
