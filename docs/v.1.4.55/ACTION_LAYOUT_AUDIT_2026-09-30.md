# v1.4.55 — Shared Action Layout Audit — 2026-09-30

## Scope

Issues: #47 / #26 / #37.

Contract:
- width-first row/stack decision;
- label-aware fit;
- one shared policy for screen and dialog action groups;
- fixed-footer actions stay outside scrolling content;
- labels stay readable and one-line; stack the group instead of shrinking text;
- layout changes must not alter callbacks, action order or business semantics.

## Existing shared source

The project already contains the intended common fit policy from the 2026-09-29
source wave:

- `75f8d79316d6dc9e63bec29a0cd27275a13ab5a8` —
  full-screen rows became label-aware;
- `59fe9be4a73510e075b8355168768addc8d2093d` —
  action fit calculation was consolidated;
- `ad92a5575144ff04253851f2d2a3b4f1940e1eb1` —
  screen and dialog actions were routed through the same fit policy.

Current canonical path:
`fitsHorizontalActionGroup()` → `useHorizontalActionRow()`.

`useHorizontalDialogActionRow()` is only a semantic adapter and delegates to the
same `useHorizontalActionRow()`. AUTO dialogs and
`addAdaptiveActionButtons()` therefore share the same label-aware row/stack
decision.

## Inventory

### COVERED — fixed full-screen footers

- StorageChooserActivity;
- RecentFileChooserActivity;
- ListSelectorActivity;
- BulkSyncPreviewActivity;
- BulkSyncSessionActivity;
- UrlSnapshotActivity normal resolved Save/Cancel footer.

All use `UiChrome.addAdaptiveActionButtons()`.

### COVERED — modal fixed footers

`showMenuDialog`, `showRecordDialog`, `showMessageDialog`,
`showContentDialog`, `showMultiChoiceDialog`,
`showFixedFooterMessageDialog` and `showDangerConfirmDialog` converge on the
fixed-footer shell. AUTO action groups use the same shared fit policy.

Representative destructive/confirm surfaces covered by that path include History,
Import, Destination, Pending, Service, Data and Bulk rollback.

### INTENTIONAL EXCEPTIONS

- Main final result uses `VERTICAL_WITH_TEXT_CLOSE`: an explicit semantic
  three-action result sheet. Explicit layout is resolved before AUTO.
- Review project/filter/candidate rows are compact in-content toolbar/list controls,
  not fixed window footers.
- Data `twoActionCard` rows are in-content card controls with short paired export
  actions, not fixed window footers.

### VERIFIED GAPS

1. **QuotaActivity fixed footer**
   - before: hard-coded horizontal `Google Cloud / Черга` row;
   - problem: no narrow-width stack fallback and it bypassed the shared renderer.

2. **UrlSnapshot duplicate chooser**
   - before: hard-coded horizontal `Всі / Унікальні / Скасувати` row;
   - also used `compactChoiceButton()` auto-sizing down to 8sp;
   - problem: labels were squeezed to retain a row instead of stacking the complete
     action group.

## Patch

- Quota fixed footer now uses `UiChrome.addAdaptiveActionButtons()`.
  Existing callbacks are unchanged. Tone order is explicit:
  Google Cloud = NORMAL, Queue = ACCENT.
- URL Snapshot duplicate chooser now owns a separate adaptive action-group
  container and uses `UiChrome.addAdaptiveActionButtons()`.
  Existing KEEP_ALL, DROP_REPEATED_EXACT_VIDEO_IDS and Cancel callbacks are
  unchanged.
- Duplicate chooser no longer uses compact auto-shrink; shared label-aware fit
  chooses row vs stack.
- `scripts/ui-window-contract-audit.sh` now includes Quota among mandatory
  adaptive fixed footers and explicitly rejects a forced URL Snapshot
  `choiceRow` / compact shrinking regression.

## Safety

Classification: **PRESENTATION_ONLY**.

No Search/write/rollback/delete/storage/API semantics were changed.
No callback was reordered or replaced.

## Phone acceptance still required

Consolidated Phase A matrix must verify:
- Quota footer portrait + landscape;
- URL Snapshot duplicate chooser portrait + landscape;
- rollback confirmation;
- one ordinary two-action dialog;
- one destructive confirmation;
- Neon + one alternate skin;
- both rotation directions;
- no action executes on relayout/recreation.

Do not mark phone PASS from this source audit.


## Exact validation

Source/audit checkpoint:
`317d72e5445ba88074820f9743474621c15f7208`.

Validate Android run:
`36760309445` — **SUCCESS**.

- Release preflight: PASS.
- JVM unit tests: PASS.
- Unsigned release assemble: PASS.

Conclusion: **#47/#26/#37 SOURCE/STATIC/BUILD PASS** for the audited shared action
layout scope. Real-phone acceptance remains pending in the consolidated Phase A
matrix.
