#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path

def fail(message: str) -> None:
    raise SystemExit("FAIL: " + message)

main = Path(
    "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
).read_text(encoding="utf-8")
review = Path(
    "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
).read_text(encoding="utf-8")
coord = Path(
    "app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt"
).read_text(encoding="utf-8")

repeat_block = '                if (\n                    data.getBooleanExtra(\n                        ReviewActivity.EXTRA_REPEAT_SEARCH,\n                        false\n                    )\n                ) {\n                    searchAll(\n                        openReviewAfter = true,\n                        preserveExistingExact = true\n                    )\n                    return\n                }\n'
if repeat_block not in main:
    fail("Review repeat-search does not explicitly preserve exact selections")

signature = '    private fun searchAll(\n        openReviewAfter: Boolean = false,\n        preserveExistingExact: Boolean = true\n    ) {\n'
if signature not in main:
    fail("ordinary searchAll does not preserve exact selections by default")

unsafe_search_all_signature = """    private fun searchAll(
        openReviewAfter: Boolean = false,
        preserveExistingExact: Boolean = false
    ) {
"""
if unsafe_search_all_signature in main:
    fail("ordinary searchAll still defaults to destructive exact re-search")

if "hasCanonicalExactSelection(track)" not in coord:
    fail("canonical exact-selection helper is not used")

helper = '    private fun hasCanonicalExactSelection(\n        track: Track\n    ): Boolean =\n        !track.selectedVideoId.isNullOrBlank() &&\n            track.status == TrackStatus.MATCHED &&\n            track.candidates.isEmpty()\n'
if helper not in coord:
    fail("canonical exact-selection predicate is incomplete")

if "PreservedSelection.MANUAL" not in coord:
    fail("manual preserved-selection state missing")

if "PreservedSelection.PROJECT_EXACT" not in coord:
    fail("project-exact preserved-selection state missing")

if "Треки з точним videoId буде збережено без нового search.list." not in review:
    fail("Review exact-ID quota-protection message missing")

if "пройде всі треки через SearchCache / YouTube search" in review:
    fail("old all-tracks repeat-search wording still present")

required_files = [
    "docs/v.1.4.27/RELEASE.md",
    "docs/v.1.4.27/REGRESSION_CHECKLIST.md",
    "docs/v.1.4.27/qa/PHONE_TEST.md",
    "docs/v.1.4.27/qa/BUG_REGISTER.md",
    "docs/v.1.4.27/diagrams/EXACT_ID_SEARCH_GUARD.md",
]

for rel in required_files:
    if not Path(rel).is_file():
        fail("missing file: " + rel)

print("PASS:")
print("- v1.4.27 exact-ID structural invariant is version-independent")
print("- Review repeat-search exact preservation")
print("- ordinary searchAll exact-preserving default")
print("- SearchCoordinator canonical exact predicate")
print("- manual/project exact preserved states")
print("- Review quota wording")
print("- phone QA status is checked by the separate immutable v1427 QA-close audit")
