# YTM Importer — Responsive Action Layout Contract

## Purpose

Action groups must use available space efficiently without changing business semantics.

The project rule is **width-first**, not orientation-name-first. Landscape often
provides enough width for a horizontal row, but the decision is based on actual
available width and readable button size.

## Action-group rule

For a group of 2–3 peer actions:

- use one horizontal row with equal-width buttons when every action can keep a
  readable/tappable minimum width;
- otherwise use a vertical stack;
- preserve action order, tone and meaning when the layout changes;
- do not hide, merge or auto-trigger actions merely to fit a row;
- do not shrink labels below readable limits just to force a horizontal layout.

Primary/secondary/destructive semantics remain identical in both layouts.

## Rotation contract

Rotation/recreation may reflow the same action group from vertical to horizontal
or back. Relayout must never execute an action, restart remote work, commit data,
or change semantic state.

## Fixed-footer rule

A fixed bottom action footer should minimize vertical obstruction in wide layouts.
If two footer actions have sufficient width, prefer a single horizontal equal-width row.

## Canonical YTM implementation

Use `UiChrome.useHorizontalActionRow(...)` and
`UiChrome.addAdaptiveActionButtons(...)` rather than one-off orientation checks.

Feature code may keep a vertical stack when labels/context make a horizontal group
materially less readable; that exception should be explicit and testable.

## Evidence

Static layout audits prove the shared adaptive mechanism is wired. Real-phone
portrait/landscape screenshots prove actual readability and spacing.

## Button-label readability

Footer/dialog action labels are treated as first-class layout constraints:

- prefer one-line labels for confirmation/footer actions;
- if a peer action label would wrap or become cramped, switch the whole action group to
  a vertical stack instead of squeezing one button;
- do not reduce text size merely to keep actions on one row;
- enabled, disabled, primary, secondary and destructive states must remain visually
  distinguishable in both portrait and landscape;
- user-facing labels use concise verbs and avoid raw implementation terminology.

The full per-window visual/lifecycle test matrix is defined in
`docs/design/UI_WINDOW_QA_CONTRACT.md`.

