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

Status: **FIX IMPLEMENTED — FULL MODAL PHONE RETEST NEEDED v1.4.34.**

Observed on the real phone through v1.4.11: some custom UiChrome dialogs can
still show a short visual movement while opening instead of appearing
immediately at the final top position.

Attempts already made in v1.4.8–v1.4.11 included:

- safe system-bar/display-cutout viewport;
- hiding the provisional frame;
- `Gravity.TOP`;
- disabling the custom dialog Window animation.

The issue was deferred for several releases, then explicitly reopened by
the user after the same jump was visible again on the v1.4.31 incremental
backup preflight.

v1.4.32 changes the shared UiChrome custom-dialog architecture instead of
patching one individual dialog:

- dedicated `Dialog` owns custom content directly;
- full-screen transparent Window geometry is configured before `show()`;
- no `setOnShowListener` geometry correction remains;
- safe insets are applied while content is hidden;
- the first visible frame is gated by `OnPreDrawListener`.

Do not mark Q-002 closed until the same real-phone dialog opens without a
visible position jump.

## v1.4.12 note

Cleanup Wave 2 intentionally does not change UiChrome dialog positioning.
Q-001 remains OPEN. Q-002 remains DEFERRED.

## v1.4.13 note

SearchCoordinator extraction does not change UiChrome. Q-002 remains DEFERRED BY USER. v1.4.12 is marked NOT TESTED in RELEASE_TEST_STATUS.md.

## v1.4.14 note

Q-002 remains DEFERRED and is not reopened. This release changes auth recovery, result presentation and QA documentation.

## v1.4.27 note — real-phone reconfirmation

A fresh v1.4.27 phone recording reproduces the same entrance-motion problem:
the dialog can be visible at an offset position and then shift/settle into its
final position.

Evidence is preserved under:

`docs/issues/BUG-002/evidence/BUG002-dialog-entrance-motion-v1.4.27-2026-09-17.mp4`

The user explicitly chose to keep Q-002 deferred and continue product work.
This evidence does not reopen Q-002 and must not block the next feature release.

## v1.4.32 note — user reopened Q-002

The v1.4.31 phone observation reconfirmed the entrance jump on the incremental
backup preflight. The user asked to address it now.

Implementation is present in v1.4.32; phone retest remains authoritative.

## v1.4.33 note — full modal unification

v1.4.32 phone QA passed for the incremental backup preflight but still showed inconsistent entrance behavior on `Квота` and other modal windows. Repository inventory found 22 direct UiChrome modal calls plus 22 legacy `UiChrome.alertBuilder(...)` calls. v1.4.33 makes the builder a compatibility facade over the same stable custom Dialog engine and requires a representative full-modal phone retest.

## v1.4.34 note — carried forward with back-navigation fix

v1.4.33 was superseded before phone QA by v1.4.34. The modal engine is unchanged; the same representative real-phone retest is required on v1.4.34.


## Q-003 — Silent Google/YTM recovery after update

Status: **CLOSED — PHONE RETEST PASS v1.4.20.**

Historical reproduction on v1.4.14:
install over authorized v1.4.13 → launch → Step 2 remained red.

Related QA: A-03, D-03.

Closure evidence from the real-phone v1.4.20 in-place update retest:
- Step 2 briefly showed the neutral/gray recovery state;
- silent account recovery completed without pressing Step 2;
- Step 2 automatically returned to green/ready.

Keep the v1.4.14 failure as historical evidence, but do not treat Q-003 as an open current issue.

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