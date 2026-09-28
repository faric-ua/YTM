# v1.4.54 — Project-wide UI Window / Button Audit

Date: 2026-09-28

Status: **STATIC CONSOLIDATION IN PROGRESS — PHONE QA PAUSED**

Purpose: stop rediscovering the same window/button defects one feature at a time.
This audit treats the shared UI layer and its consumers as one system.

## Trigger

During the BUG-044/Test 5 checkpoint, the `Створити Bulk-сесію?` confirmation
showed `Створити сесію` wrapped to two lines beside `Скасувати`.

The immediate BUG-045 root cause was not a bad string. `UiChrome.addDialogActions`
correctly checked available width and then a second fallback
(`actions.size <= 3`) forced the same actions back into a horizontal row.

That phone finding triggered a broader static audit instead of another local patch.

## Existing source-of-truth rules

The project already had the correct rules:

- `docs/design/UI_WINDOW_QA_CONTRACT.md`
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`

The gap was enforcement: some runtime paths and historical audits no longer matched
those documents.

## Systemic findings

### W-001 — action-bearing shared dialogs did not all use a fixed footer

Before this cleanup:
- `showFixedFooterMessageDialog` had the correct header / scroll / fixed-footer structure;
- `showMessageDialog`, `showRecordDialog`, `showContentDialog` and
  `showMultiChoiceDialog` appended actions to the same card that was then placed
  inside the whole-dialog ScrollView;
- `showMenuDialog` kept its dismiss action in the scrolling menu body.

Impact:
- long content could move the action controls;
- Help/results/confirmations did not have one common composition;
- phone QA could pass one window while another shared dialog family still violated
  the project contract.

Correction:
all action-bearing shared dialog families now converge on one fixed-footer content
shell.

### W-002 — dialog row selection could ignore available width

The old action-count-only fallback could force 2–3 actions into one row after the
width guard had already rejected that row.

Correction:
- the fallback was removed;
- modal row selection is now label-aware;
- actual rendered label width is included in the row decision;
- a non-fitting peer group stacks as a group.

### W-003 — explicit action layouts could be overridden by AUTO width logic

The old order evaluated the horizontal-row branch before
`VERTICAL_WITH_TEXT_CLOSE` / `PRIMARY_TOP`.

Correction:
- explicit layouts are resolved first;
- AUTO width-first behavior applies only when no explicit semantic layout owns the
  arrangement;
- PRIMARY_TOP keeps the primary action full-width and independently applies the
  width rule to its two secondary actions.

### W-004 — dialog footer labels were allowed to wrap and auto-shrink

Correction:
- shared dialog action labels are one line;
- shared dialog actions use stable 14sp text;
- the dialog action button no longer auto-shrinks text merely to preserve a row;
- when the group does not fit, layout changes instead of squeezing the label.

### W-005 — several full-screen footer groups had their own layout policy

Found:
- Storage chooser: shared adaptive footer already used;
- Recent-file chooser: shared adaptive footer already used;
- URL Snapshot: shared adaptive footer already used;
- ListSelector: permanently horizontal two-button footer;
- Bulk Preview: permanently vertical two-button footer;
- Bulk Session: permanently vertical two-button footer.

Correction:
ListSelector, Bulk Preview and Bulk Session now use
`UiChrome.addAdaptiveActionButtons(...)` as well.

### W-006 — static audits could report PASS while the runtime contract was split

Historical audits proved many older UI guarantees but did not require all
action-bearing shared dialogs to converge on the fixed-footer shell.

Correction:
- new gate: `scripts/ui-window-contract-audit.sh`;
- release preflight now runs that gate;
- older action/adaptive audits were updated so they no longer expect the obsolete
  forced-row behavior;
- the design contracts now require a static-first gate before phone QA.

## Shared modal inventory after cleanup

| Shared API | Content scrolls | Header fixed | Actions fixed | Width policy |
|---|---|---|---|---|
| `showMenuDialog` | menu actions | yes | dismiss action | label-aware AUTO |
| `showRecordDialog` | records | yes | yes | explicit/AUTO |
| `showMessageDialog` | message | yes | yes | label-aware AUTO |
| `showFixedFooterMessageDialog` | delegates to canonical message dialog | yes | yes | label-aware AUTO |
| `showDangerConfirmDialog` | via message dialog | yes | yes | label-aware AUTO |
| `showContentDialog` | message + custom content | yes | yes | label-aware AUTO |
| `showMultiChoiceDialog` | choices | yes | yes | label-aware AUTO |
| `StableAlertBuilder` | routes to the APIs above | yes | yes | inherited |

The old whole-card scroll shell may remain as an implementation utility for
actionless content only; action-bearing public dialog APIs must not route through it.

## Full-screen / overlay inventory

| Owner | Main structure | Bottom action state after audit |
|---|---|---|
| MainActivity | Home dashboard / workflow | content/navigation actions; not a chooser footer |
| ImportActivity | full-screen import workspace | content actions; modal actions inherit fixed-footer shell |
| ReviewActivity | toolbar/filter/workspace | content actions; modal actions inherit fixed-footer shell |
| DestinationActivity | destination workspace | content actions; modal actions inherit fixed-footer shell |
| HistoryActivity | list/detail workspace | content actions; modal actions inherit fixed-footer shell |
| DataActivity | data tools + restorable modal states | modal actions inherit fixed-footer shell |
| PendingActivity | Queue workspace | item actions are content; confirms inherit fixed-footer shell |
| ServiceActivity | service/diagnostics workspace | content actions; confirms inherit fixed-footer shell |
| MenuActivity | full-screen menu/settings | modal actions inherit fixed-footer shell |
| PlaylistActivity | Playlist Hub | record dialogs inherit fixed-footer shell |
| QuotaActivity | full-screen quota utility | content actions; no modal footer exception introduced |
| StorageChooserActivity | chooser + fixed footer | shared adaptive row/stack |
| RecentFileChooserActivity | chooser + fixed footer | shared adaptive row/stack |
| ListSelectorActivity | selector + fixed footer | **migrated to shared adaptive row/stack** |
| UrlSnapshotActivity | preview/chooser + fixed footer | shared adaptive row/stack |
| BulkSyncPreviewActivity | preview + fixed footer | **migrated to shared adaptive row/stack** |
| BulkSyncSessionActivity | durable session + fixed footer | **migrated to shared adaptive row/stack** |
| WorkflowRelayOverlay | progress rows scroll | completed `Готово` action remains outside scroll |

## Help lifecycle inventory

Known Help owners:
- ListSelectorActivity;
- StorageChooserActivity;
- RecentFileChooserActivity;
- BulkSyncPreviewActivity;
- BulkSyncSessionActivity.

Each owner has a semantic `helpDialogOpen` state and
`STATE_HELP_DIALOG_OPEN` recreation key. The shared shell now also guarantees the
same fixed-footer composition for their Help action.

Lifecycle rule remains:
rotation restores the Help window above the same parent screen; no Help recreation
may execute the underlying action.

## Static gate added

`scripts/ui-window-contract-audit.sh` now rejects, among other things:

- action-bearing shared dialogs bypassing the fixed-footer shell;
- the old `actions.size <= 3`/compact-row fallback;
- dialog footer labels allowed to wrap;
- dialog action auto-size used to force a row;
- explicit action layout branches placed behind AUTO behavior;
- chooser/selector/Bulk/URL Snapshot fixed footers that stop using the shared
  adaptive renderer;
- Help owners that lose their recreation state;
- a return of native `AlertDialog.Builder`.

The gate is part of `scripts/release-preflight.sh`.

## What is intentionally not normalized into one visual component

Not every `Button` in the app is a footer action.

The following remain feature/content controls and are not forced into the footer
renderer:
- Home workflow tiles;
- Review filter/toolbar buttons;
- per-item Queue/History/Destination controls;
- Service/Quota content actions;
- menu/navigation tiles;
- URL Snapshot in-content duplicate-choice controls.

They still follow theme, touch-target and readability rules, but moving them into a
footer would change information architecture rather than fix inconsistency.

## Phone evidence from the trigger run

Installed candidate at the time of the screenshot:
`552c387a5626ab0fd7e501aff946741610f41f6d`
(signed run `36374659541`).

Observed:
- portrait `Створити Bulk-сесію?` now showed `Створити сесію` and
  `Скасувати` as full-width stacked buttons;
- the primary label stayed on one line;
- no session creation was confirmed during this retest;
- latest landscape screenshot was back on Bulk Preview, with no write started.

This is sufficient evidence for the original portrait BUG-045 symptom, but **not**
for project-wide window acceptance.

## Consolidated phone QA still required

Do not test windows one-by-one while changing code between each screenshot.

After the static consolidation reaches a validated signed candidate, run one compact
representative matrix:

1. short 2-action confirmation — portrait + landscape;
2. long Help window — portrait + landscape + both rotations;
3. multi-choice/content dialog with enough content to scroll;
4. record/result dialog with 3 actions;
5. ListSelector footer portrait/landscape;
6. Bulk Preview footer portrait/landscape;
7. Bulk Session Help + footer on the preserved Test 5 session path;
8. one theme sanity check on Neon plus one alternate theme.

For every sample verify:
- header/content/footer separation;
- actions never scroll away;
- labels do not wrap because a row was forced;
- row ↔ stack changes do not execute actions;
- Back/Cancel/Close returns to the correct owner;
- rotation restores semantic state without remote work.

Only after this representative matrix passes should Test 5 continue.

## Stop rule

If the consolidated phone matrix finds another **shared** window defect:
stop the feature test, fix the shared layer, rerun static validation once, and resume
from the preserved phone checkpoint.

Do not continue generating feature-specific retest loops on top of a known shared
UI defect.


### W-007 — action confirmation lifecycle was not enforced

Phone evidence on signed source
`d34f37a46d115b288d64a609e2aead266345be53` exposed a lifecycle gap after
the layout/static consolidation:

- `Створити Bulk-сесію?` was open in portrait;
- rotating to landscape recreated the parent Preview;
- the confirmation disappeared;
- no action executed and no remote write started.

Root cause:
Bulk Preview Help persisted semantic open state, but its action confirmations did not.
The project already had `RestorableModalController`, so this was contract drift, not
missing infrastructure.

Correction:
- Bulk Preview create-session and active-session confirmations now use
  `RestorableModalController`;
- open modal id/args are saved/restored across Activity recreation;
- restoration never executes an action;
- explicit buttons clear semantic modal state before executing;
- the UI window static gate now requires the Bulk Preview confirmation lifecycle
  contract.

Phone acceptance:
portrait open → landscape → portrait must keep the same confirmation visible, allow
layout reflow only, execute nothing, then explicit Cancel returns to the same Preview.

This finding is tracked as BUG-046 / issue #38.
