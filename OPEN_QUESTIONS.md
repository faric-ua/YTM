# YTM Importer — Open questions / revisit later

## Q-001 — v1.3.2 Review wording + Project save feedback

Status: **OPEN — revisit later; does not block current roadmap.**

The v1.3.2 UX correction improved manual-selection wording and Project-save
confirmation, but the user explicitly asked to keep this area marked as having
open questions and return to it later instead of spending more time on it now.

When revisiting, verify on a real phone:

- how original track vs selected YouTube/YTM result is visually presented;
- whether `Ручний вибір` / `Знайдено` wording is clear enough;
- what exact Project name/file-name feedback should be shown after save;
- whether the save feedback should remain a Toast or become an in-screen result.

Do not treat this item as a blocker for Destination/Create work.

## v1.4.1 note

Q-001 is intentionally still OPEN. The legacy Destination cleanup does not resolve or redefine it.

## v1.4.2 note

Top/bottom insets and secondary-dialog styling are addressed in v1.4.2. Q-001 still remains OPEN.

## v1.4.3 note

Dialog styling and button spacing were standardized. Q-001 remains OPEN and is intentionally not resolved by this release.

## v1.4.4 note

Adaptive button/layout fixes do not close Q-001. It remains OPEN.

## v1.4.5 note

Compact Review and quota layout changed. Q-001 remains OPEN.

## v1.4.6 note

Dialog hierarchy and Service screen were updated. Q-001 remains OPEN.

## v1.4.7 note

Service navigation and structured info pages were fixed. Q-001 remains OPEN.

## v1.4.8 note

Custom dialog top clipping was fixed globally in UiChrome. Q-001 remains OPEN.

## v1.4.9 note

Rotation/session retention and custom-dialog first-frame stabilization were fixed. Q-001 remains OPEN.

## v1.4.10 note

Step-button baseline alignment and custom-dialog top anchoring were fixed. Q-001 remains OPEN.

## v1.4.11 note

Phone-video analysis identified the remaining custom-dialog movement as AlertDialog Window animation. It is disabled for UiChrome custom dialogs. Q-001 remains OPEN.

## Q-002 — Custom dialog entrance motion on real device

Status: **DEFERRED BY USER — does not block the roadmap.**

Observed on the real phone through v1.4.11: some custom UiChrome dialogs can
still show a short visual movement while opening instead of appearing
immediately at the final top position.

Attempts already made in v1.4.8–v1.4.11 included:

- safe system-bar/display-cutout viewport;
- hiding the provisional frame;
- `Gravity.TOP`;
- disabling the custom dialog Window animation.

The user explicitly chose to stop spending time on this issue for now and
continue with YTM development.

Do not reopen or block a release on Q-002 unless the user explicitly asks to
return to it.

## v1.4.12 note

Cleanup Wave 2 intentionally does not change UiChrome dialog positioning.
Q-001 remains OPEN. Q-002 remains DEFERRED.

## v1.4.13 note

SearchCoordinator extraction does not change UiChrome. Q-002 remains DEFERRED BY USER. v1.4.12 is marked NOT TESTED in RELEASE_TEST_STATUS.md.

## v1.4.14 note

Q-002 remains DEFERRED and is not reopened. This release changes auth recovery, result presentation and QA documentation.

## Q-003 — Silent Google/YTM recovery after update

Status: **DEFERRED FOR LATER FIX**

Reproduced on v1.4.14:
install over authorized v1.4.13 → launch → Step 2 remains red.

Related QA: A-03, D-03.
Do not claim fixed until an in-place update test passes without pressing Step 2.

## Q-005 — Manual Search should respect exact videoId

Status: **CLOSED — PHONE RETEST PASS v1.4.27.**

Found during the v1.4.26 selective-export round-trip phone test.

The re-imported `top 3` YTM Project correctly restored 3/3 exact videoId values,
but opening manual `Пошук` still produced a plan for 3 new `search.list` calls.

Decision for v1.4.27:

- exact-videoId tracks must not be included in ordinary search planning;
- if every track is exact, the UI should report that search is unnecessary
  instead of offering quota-consuming work;
- the user may still need an explicit future "re-search/replace" action if they
  intentionally want to discard an exact selection.

This separates "find missing matches" from "force a new search".

Implementation note for v1.4.27:

- ordinary repeat-search preserves canonical exact selections;
- exact 3/3 project should plan 0 new search.list;
Phone retest result (2026-09-17):

- `top 3` opened with exact/ready 3/3;
- Review showed 3/3 ready;
- ordinary repeat-search plan showed 0 tracks requiring search;
- new `search.list` = 0;
- BUG-005 / Q-005 is closed for this targeted path.

A future explicit force-research/replace-exact action, if desired, remains a separate product decision rather than part of Q-005.
