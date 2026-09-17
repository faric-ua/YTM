#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path

class PatchError(RuntimeError):
    pass

OPS = [('replace',
  'app/build.gradle.kts',
  '        versionCode = 60\n        versionName = "1.4.26"',
  '        versionCode = 61\n        versionName = "1.4.27"',
  'release version'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/MainActivity.kt',
  '                if (\n'
  '                    data.getBooleanExtra(\n'
  '                        ReviewActivity.EXTRA_REPEAT_SEARCH,\n'
  '                        false\n'
  '                    )\n'
  '                ) {\n'
  '                    searchAll(\n'
  '                        openReviewAfter = true\n'
  '                    )\n'
  '                    return\n'
  '                }\n',
  '                if (\n'
  '                    data.getBooleanExtra(\n'
  '                        ReviewActivity.EXTRA_REPEAT_SEARCH,\n'
  '                        false\n'
  '                    )\n'
  '                ) {\n'
  '                    searchAll(\n'
  '                        openReviewAfter = true,\n'
  '                        preserveExistingExact = true\n'
  '                    )\n'
  '                    return\n'
  '                }\n',
  'Review repeat-search exact preservation'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/MainActivity.kt',
  '    private fun searchAll(\n'
  '        openReviewAfter: Boolean = false,\n'
  '        preserveExistingExact: Boolean = false\n'
  '    ) {\n',
  '    private fun searchAll(\n'
  '        openReviewAfter: Boolean = false,\n'
  '        preserveExistingExact: Boolean = true\n'
  '    ) {\n',
  'ordinary searchAll preserves exact by default'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt',
  '            .setMessage(\n'
  '                "YTM Importer повернеться на головний екран і знову " +\n'
  '                    "пройде всі треки через SearchCache / YouTube search. " +\n'
  '                    "Кешовані результати не витрачають search.list quota."\n'
  '            )\n',
  '            .setMessage(\n'
  '                "YTM Importer повернеться на головний екран і повторить пошук " +\n'
  '                    "лише для треків, яким він справді потрібен. " +\n'
  '                    "Треки з точним videoId буде збережено без нового search.list. " +\n'
  '                    "Кешовані результати також не витрачають search.list quota."\n'
  '            )\n',
  'Review repeat-search quota message'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt',
  '    private fun shouldSearch(\n'
  '        track: Track,\n'
  '        preserveExistingExact: Boolean\n'
  '    ): Boolean {\n'
  '        val manualExact =\n'
  '            track.manuallySelected &&\n'
  '                !track.selectedVideoId.isNullOrBlank()\n'
  '\n'
  '        if (manualExact) {\n'
  '            return false\n'
  '        }\n'
  '\n'
  '        return if (preserveExistingExact) {\n'
  '            track.selectedVideoId.isNullOrBlank() ||\n'
  '                track.status != TrackStatus.MATCHED ||\n'
  '                track.candidates.isNotEmpty()\n'
  '        } else {\n'
  '            true\n'
  '        }\n'
  '    }\n',
  '    private fun shouldSearch(\n'
  '        track: Track,\n'
  '        preserveExistingExact: Boolean\n'
  '    ): Boolean {\n'
  '        val manualExact =\n'
  '            track.manuallySelected &&\n'
  '                !track.selectedVideoId.isNullOrBlank()\n'
  '\n'
  '        if (manualExact) {\n'
  '            return false\n'
  '        }\n'
  '\n'
  '        if (\n'
  '            preserveExistingExact &&\n'
  '            hasCanonicalExactSelection(track)\n'
  '        ) {\n'
  '            return false\n'
  '        }\n'
  '\n'
  '        return true\n'
  '    }\n',
  'SearchCoordinator canonical exact planning guard'),
 ('replace',
  'app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt',
  '        val keepExactSelection =\n'
  '            preserveExistingExact &&\n'
  '                !track.selectedVideoId.isNullOrBlank() &&\n'
  '                track.status == TrackStatus.MATCHED &&\n'
  '                track.candidates.isEmpty()\n'
  '\n'
  '        return if (keepExactSelection) {\n'
  '            PreservedSelection.PROJECT_EXACT\n'
  '        } else {\n'
  '            null\n'
  '        }\n'
  '    }\n',
  '        val keepExactSelection =\n'
  '            preserveExistingExact &&\n'
  '                hasCanonicalExactSelection(track)\n'
  '\n'
  '        return if (keepExactSelection) {\n'
  '            PreservedSelection.PROJECT_EXACT\n'
  '        } else {\n'
  '            null\n'
  '        }\n'
  '    }\n'
  '\n'
  '    private fun hasCanonicalExactSelection(\n'
  '        track: Track\n'
  '    ): Boolean =\n'
  '        !track.selectedVideoId.isNullOrBlank() &&\n'
  '            track.status == TrackStatus.MATCHED &&\n'
  '            track.candidates.isEmpty()\n',
  'SearchCoordinator canonical exact helper'),
 ('replace',
  'RELEASE_TEST_STATUS.md',
  '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** | Two-playlist '
  'selective export passed: 2 projects + manifest, schema v2/SELECTED, exact-videoId round trip 3/3. Manual '
  'Search then proposed redundant search.list for exact tracks (BUG-005). |',
  '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** | Two-playlist '
  'selective export passed: 2 projects + manifest, schema v2/SELECTED, exact-videoId round trip 3/3. Manual '
  'Search then proposed redundant search.list for exact tracks (BUG-005). |\n'
  '| v1.4.27 | **NOT TESTED YET** | Exact-ID Search Guard: ordinary repeat-search preserves canonical exact '
  'videoId tracks and avoids redundant search.list quota. |',
  'release status v1.4.27'),
 ('replace',
  'PROJECT_STATUS.txt',
  'Version: 1.4.26\nVersion code: 60',
  'Version: 1.4.27\nVersion code: 61',
  'project version'),
 ('replace',
  'PROJECT_STATUS.txt',
  'v1.4.26 PARTIALLY PHONE-TESTED — SELECTIVE EXPORT PASS / BUG-005 FOUND',
  'v1.4.26 PARTIALLY PHONE-TESTED — SELECTIVE EXPORT PASS / BUG-005 FOUND\nv1.4.27 NOT TESTED YET',
  'project v1.4.27 status'),
 ('replace',
  'PROJECT_STATUS.txt',
  'BUG-005/Q-005 OPEN — redundant manual search for exact videoId tracks',
  'BUG-005/Q-005 FIX IMPLEMENTED v1.4.27 — PHONE RETEST REQUIRED',
  'project BUG-005 status'),
 ('replace',
  'BACKLOG.md',
  '## Current\nv1.4.26 — Selective Account Export',
  '## Current\nv1.4.27 — Exact-ID Search Guard',
  'BACKLOG current'),
 ('replace',
  'BACKLOG.md',
  '- BUG-005/Q-005 redundant manual search for exact videoId tracks — OPEN, v1.4.27',
  '- BUG-005/Q-005 redundant manual search for exact videoId tracks — FIX IMPLEMENTED v1.4.27, PHONE RETEST '
  'REQUIRED',
  'BACKLOG BUG-005 status'),
 ('replace',
  'BACKLOG.md',
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
  '- [ ] confirm Review remains 3/3 ready\n',
  '## v1.4.27 — Exact-ID Search Guard\n'
  '- [x] exclude tracks with exact/canonical videoId from ordinary search planning\n'
  '- [x] ordinary repeat-search explicitly preserves existing exact selections\n'
  '- [x] ordinary searchAll defaults to exact-selection preservation\n'
  '- [x] preserve explicit manual candidate selections\n'
  '- [x] keep candidate-based matches eligible for intentional repeat search\n'
  '- [x] clarify Review repeat-search quota message\n'
  '- [x] add BUG-005 static regression audit\n'
  '- [x] add release docs + phone-test plan\n'
  '- [ ] GitHub build\n'
  '- [ ] phone retest: reopen exact `top 3` project\n'
  '- [ ] phone retest: Search plan = 0 new search.list\n'
  '- [ ] confirm Review remains 3/3 ready\n',
  'BACKLOG v1.4.27 implementation'),
 ('insert',
  'CHANGELOG.md',
  '## v1.4.26\n',
  '## v1.4.27\n'
  '- Fixed BUG-005 by preserving canonical exact videoId selections during ordinary Review repeat-search.\n'
  '- Review repeat-search explicitly calls search with exact-selection preservation.\n'
  '- Ordinary `searchAll` now defaults to preserving existing exact selections.\n'
  '- SearchCoordinator uses a shared canonical-exact predicate for planning and execution preservation.\n'
  '- Candidate-based searched matches remain eligible for intentional repeat search.\n'
  '- Updated Review repeat-search message to explain exact-ID quota protection.\n'
  '- Added v1.4.27 audit, release docs, QA plan and bug snapshot.\n'
  '- versionCode 61 / versionName 1.4.27.\n'
  '- v1.4.27 = NOT PHONE-TESTED YET.\n'
  '\n',
  '## v1.4.27\n',
  'CHANGELOG v1.4.27'),
 ('replace',
  'qa/BUG_REGISTER.md',
  '| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 | P2 | Manual Search plans new search.list requests for tracks that '
  'already have exact videoId. | v1.4.26 round-trip / manual Search |',
  '| BUG-005 / Q-005 | FIX IMPLEMENTED v1.4.27 — PHONE RETEST REQUIRED | P2 | Ordinary repeat-search now '
  'preserves canonical exact videoId tracks; phone retest must confirm 0 redundant search.list. | v1.4.26 repro → '
  'v1.4.27 retest |',
  'BUG-005 fix-implemented status'),
 ('replace',
  'OPEN_QUESTIONS.md',
  'Status: **OPEN — BUG-005, planned for v1.4.27.**',
  'Status: **FIX IMPLEMENTED v1.4.27 — PHONE RETEST REQUIRED.**',
  'Q-005 status'),
 ('replace',
  'OPEN_QUESTIONS.md',
  'This separates "find missing matches" from "force a new search".\n',
  'This separates "find missing matches" from "force a new search".\n'
  '\n'
  'Implementation note for v1.4.27:\n'
  '\n'
  '- ordinary repeat-search preserves canonical exact selections;\n'
  '- exact 3/3 project should plan 0 new search.list;\n'
  '- BUG-005 remains open until real-phone retest passes.\n',
  'Q-005 implementation note'),
 ('insert',
  'scripts/search-coordinator-audit.sh',
  'echo "PASS:"\n',
  'grep -q \'hasCanonicalExactSelection\' "$COORD" \\\n'
  '  || fail "canonical exact-selection helper missing"\n'
  '\n'
  'grep -q \'track.status == TrackStatus.MATCHED\' "$COORD" \\\n'
  '  || fail "canonical exact-selection MATCHED guard missing"\n'
  '\n'
  'grep -q \'track.candidates.isEmpty()\' "$COORD" \\\n'
  '  || fail "canonical exact-selection empty-candidates guard missing"\n'
  '\n'
  'grep -q \'preserveExistingExact = true\' "$MAIN" \\\n'
  '  || fail "MainActivity does not explicitly preserve exact selections on repeat search"\n'
  '\n'
  'if grep -q \'preserveExistingExact: Boolean = false\' "$MAIN"; then\n'
  '  fail "ordinary searchAll still defaults to destructive exact re-search"\n'
  'fi\n'
  '\n',
  'canonical exact-selection helper missing',
  'strengthen search coordinator audit'),
 ('replace',
  'scripts/release-preflight.sh',
  'check_file "docs/v.1.4.26/RELEASE.md"',
  'check_file "docs/v.1.4.27/RELEASE.md"',
  'preflight v1.4.27 release doc'),
 ('replace',
  'scripts/release-preflight.sh',
  'check_file "docs/v.1.4.26/REGRESSION_CHECKLIST.md"',
  'check_file "docs/v.1.4.27/REGRESSION_CHECKLIST.md"',
  'preflight v1.4.27 checklist'),
 ('replace',
  'scripts/release-preflight.sh',
  'python -B scripts/v1426-apply-selftest.py\n'
  'bash scripts/v1426-qa-close-audit.sh\n'
  'bash scripts/v1426-selective-export-audit.sh',
  'python -B scripts/v1426-apply-selftest.py\n'
  'bash scripts/v1426-qa-close-audit.sh\n'
  'bash scripts/v1426-selective-export-audit.sh\n'
  'python -B scripts/v1427-apply-selftest.py\n'
  'python -B scripts/v1427-exact-id-search-audit.py',
  'preflight v1.4.27 active checks'),
 ('insert',
  'scripts/release-preflight.sh',
  'check_file "scripts/v1426-selective-export-audit.sh"\n',
  'check_file "scripts/v1427-apply-selftest.py"\ncheck_file "scripts/v1427-exact-id-search-audit.py"\n',
  'check_file "scripts/v1427-apply-selftest.py"',
  'preflight v1.4.27 script files'),
 ('replace',
  'scripts/release-preflight.sh',
  'grep -q \'versionCode = 60\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 60"',
  'grep -q \'versionCode = 61\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 61"',
  'preflight versionCode 61'),
 ('replace',
  'scripts/release-preflight.sh',
  'grep -q \'versionName = "1.4.26"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.26"\'',
  'grep -q \'versionName = "1.4.27"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.27"\'',
  'preflight versionName 1.4.27'),
 ('replace',
  'scripts/qa-plan-audit.sh',
  "grep -Fq '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** |' "
  '"$STATUS" \\\n'
  '  || fail "v1.4.26 selective-export phone status missing"\n'
  'grep -Fq \'| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 |\' "$BUG" \\\n'
  '  || fail "BUG-005 phone finding missing"\n',
  "grep -Fq '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** |' "
  '"$STATUS" \\\n'
  '  || fail "v1.4.26 selective-export phone status missing"\n'
  'grep -Fq \'| v1.4.27 | **NOT TESTED YET** |\' "$STATUS" \\\n'
  '  || fail "v1.4.27 must start NOT TESTED YET"\n'
  'grep -Fq \'| BUG-005 / Q-005 | FIX IMPLEMENTED v1.4.27 — PHONE RETEST REQUIRED |\' "$BUG" \\\n'
  '  || fail "BUG-005 v1.4.27 fix-implemented status missing"\n',
  'qa-plan v1.4.27 and BUG-005 status guards'),
 ('replace',
  'scripts/v1426-qa-close-audit.sh',
  'BUG="qa/BUG_REGISTER.md"',
  'BUG="docs/v.1.4.26/qa/BUG_REGISTER.md"',
  'freeze v1.4.26 BUG audit to release snapshot'),
 ('replace',
  'scripts/v1426-qa-close-audit.sh',
  '  || fail "BUG-005 missing from root bug register"',
  '  || fail "BUG-005 missing from v1.4.26 bug snapshot"',
  'v1.4.26 BUG snapshot message'),
 ('replace',
  'docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md',
  'successful persistence не гарантує correct downstream behavior.\n',
  'successful persistence не гарантує correct downstream behavior.\n'
  '\n'
  '## 9. v1.4.27: fix має бути у call path і domain rule\n'
  '\n'
  'BUG-005 виявив дві різні точки, які треба узгодити.\n'
  '\n'
  'Перша — **call path**. Review повертав `EXTRA_REPEAT_SEARCH`, а Main запускав\n'
  '`searchAll()` без exact-preservation flag. Тобто UI-дія ненавмисно обирала\n'
  'режим «шукати все заново».\n'
  '\n'
  'Друга — **domain rule** у `SearchCoordinator`. Саме coordinator повинен\n'
  'визначати, що canonical exact selection має такі ознаки:\n'
  '\n'
  '- є `selectedVideoId`;\n'
  '- `status == MATCHED`;\n'
  '- список search candidates порожній.\n'
  '\n'
  'Для такого треку ordinary search planning повертає skip/preserve, а не\n'
  'SearchCache/API work.\n'
  '\n'
  'Чому не достатньо виправити лише кнопку? Бо інший caller у майбутньому міг би\n'
  'знову передати неправильний flag. Тому v1.4.27 робить обидва кроки:\n'
  '\n'
  '1. Review repeat-search явно передає `preserveExistingExact = true`;\n'
  '2. ordinary `searchAll()` також має safe default `true`;\n'
  '3. SearchCoordinator використовує один helper\n'
  '   `hasCanonicalExactSelection(track)` і в `plan`, і в `run`.\n'
  '\n'
  'При цьому candidate-based automatic matches не прирівнюються до canonical\n'
  'exact import. Це дозволяє intentional repeat-search для звичайних результатів\n'
  'пошуку, не витрачаючи quota на exact account/project data.\n'
  '\n'
  'Phone invariant для закриття BUG-005:\n'
  '\n'
  '`top 3 exact 3/3 → Review → ↻ Пошук → search required 0 → new search.list 0`\n',
  'tutorial v1.4.27 exact-ID lesson'),
 ('replace',
  'docs/tutorial/ROADMAP.md',
  '- `11_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers;',
  '- `11_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers, BUG-005/v1.4.27 quota invariant;',
  'tutorial roadmap exact-ID lesson')]
PROJECT_FOCUS = '\nv1.4.27 focus:\n- BUG-005 Exact-ID Search Guard\n- ordinary repeat-search preserves canonical exact videoId tracks\n- project/account exact selections skip SearchCache/search.list work\n- candidate-based searched matches can still participate in repeat search\n- manual selections remain protected\n- phone retest target: exact `top 3` 3/3 → 0 new search.list\n'

def read_text(root: Path, rel: str) -> str:
    path = root / rel
    if not path.is_file():
        raise PatchError(f"missing file: {rel}")
    return path.read_text(encoding="utf-8")

def write_text(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.write_text(text, encoding="utf-8", newline="\n")

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
            raise PatchError(
                f"{label}: expected exactly 1 old anchor; found {count} in {rel}"
            )
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
            raise PatchError(
                f"{label}: expected exactly 1 marker; found {count} in {rel}"
            )
        if not dry_run:
            write_text(root, rel, text.replace(marker, snippet + marker, 1))
        print(("CHECK" if dry_run else "PATCH") + f": {label}")
        return

    raise PatchError(f"unknown operation: {kind}")

def custom_updates(root: Path, dry_run: bool) -> None:
    rel = "PROJECT_STATUS.txt"
    text = read_text(root, rel)

    if "v1.4.27 focus:" in text:
        print("SKIP already applied: PROJECT_STATUS v1.4.27 focus")
    else:
        marker = "\nKnown:\n"
        if text.count(marker) != 1:
            raise PatchError(
                "PROJECT_STATUS Known marker missing/duplicated"
            )
        if not dry_run:
            write_text(
                root,
                rel,
                text.replace(
                    marker,
                    "\n" + PROJECT_FOCUS + "\n" + marker,
                    1
                )
            )
        print(
            ("CHECK" if dry_run else "PATCH") +
            ": PROJECT_STATUS v1.4.27 focus"
        )

def run(root: Path, dry_run: bool = False) -> None:
    root = root.resolve()
    for op in OPS:
        apply_one(root, op, dry_run)
    custom_updates(root, dry_run)

def validate_ops(ops=OPS) -> None:
    for op in ops:
        for value in op[2:-1]:
            if (
                isinstance(value, str) and
                r"\n" in value and
                "\n" not in value
            ):
                raise PatchError(
                    f"literal \\n regression in operation: {op[-1]}"
                )

if __name__ == "__main__":
    validate_ops()
    print(f"PASS: {len(OPS)} v1.4.27 operations validated")
