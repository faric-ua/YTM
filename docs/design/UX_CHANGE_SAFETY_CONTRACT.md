# YTM Importer — UX Change Safety Contract

Status: authoritative for UI/UX hardening work after the v1.4.54 phone-accepted baseline.

Baseline app source:
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

## 1. Core rule

A UX/readability/navigation change must not silently change business behavior.

Presentation work must preserve the existing functional contract unless a separate
feature issue explicitly changes that contract.

For a presentation-only change, do not change:
- YouTube/YTM request ordering or retry policy;
- Search/write/rollback auto-start rules;
- playlist ownership or identity rules;
- persisted playlistId/localPlaylistId semantics;
- Queue/Pending ownership;
- History operation semantics;
- durable Bulk mutation ledger semantics;
- backup/restore data meaning;
- remote write/delete scope;
- quota classification;
- existing storage schemas, unless a separately reviewed migration is required.

Repository truth beats chat memory. Real-phone QA remains authoritative.

## 2. Identity and remote safety

Remote identity is always exact persisted identity.

Never infer or replace YTM linkage from title similarity.

A UI refactor must not:
- invent playlistId;
- replace a persisted playlistId;
- retry uncertain remote work blindly;
- delete by title;
- merge local/remote entities only because names match.

Title is display metadata, not identity.

## 3. Explicit-action rule

Rotation, recreation, resume, returning from another Activity, opening Help,
opening a chooser, rendering a status screen, or opening Recovery Center must never
start Search, YTM write, rollback, delete, restore, share, save, or theme changes.

Remote/durable mutations require an explicit user action.

For interrupted work:
- reopen shows the durable state;
- no automatic continuation;
- Continue/Retry/Rollback is explicit;
- uncertain work remains blocked until safely reconciled.

## 4. Modal / Help / confirmation lifecycle

When a modal, Help window, editor, confirmation, result, or action menu is open and
Activity recreation occurs:
- restore the same semantic window over the same parent;
- restore the same target/entity/context;
- do not execute any action during restoration;
- Cancel/Close returns to the same parent state;
- drafts survive when editing is involved;
- destructive confirmation remains pending until an explicit confirm.

Persist semantic state/ids/args, never a Dialog object.

## 5. Navigation ownership

Back, system Back, Cancel and Close each have one documented destination.

Returning from delegated screens must preserve the navigation origin.

Do not make a child action finish an owner screen merely to simplify navigation.

Home remains a dashboard. Detailed playlist work belongs in Playlist Hub/Review or
the dedicated destination screen.

## 6. Scroll, focus and selection

Returning to the same logical screen must preserve:
- scroll position;
- selected/expanded item where practical;
- text draft;
- active selectable-text range where the same text still exists;
- focus when it is part of the current editing task.

A re-render must not jump to top unless the user navigated to a new logical root.

Dynamic lists should prefer stable item identity + offset over raw pixel position
where practical.

## 7. Action layout

Authoritative base:
- `RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
- `UI_WINDOW_QA_CONTRACT.md`

Additional project rule:
- horizontal peer actions: primary/destructive action first, Cancel/Close last;
- vertical stack: primary/destructive action above, Cancel/Close below;
- 2–3 peer actions use one row only when every label and touch target fits;
- otherwise the whole group stacks;
- never shrink text to force a row;
- footer actions stay outside scroll content;
- enabled/disabled/primary/secondary/danger meaning must not change when reflowing.

## 8. Tile contract

Authoritative base: `TILE_UI_CONTRACT.md`.

Every entity tile should be scannable in this order:
1. entity title;
2. semantic state/status;
3. primary result/counters;
4. secondary metadata;
5. warning/error/recovery note;
6. quick actions.

Rules:
- title is visually primary;
- status is separate from title;
- key result/counters are not buried in prose;
- technical diagnostics are secondary or expandable;
- frequent actions may use a right-side action rail;
- `⋮` is the complete visible action menu;
- long press opens the same complete menu;
- long press never executes a destructive action;
- destructive action always confirms explicitly.

## 9. Readability and language

Primary UI copy is for a normal user, not for the implementation.

Prefer:
- Ukrainian user-facing wording;
- short verbs on buttons;
- structured key/value rows;
- semantic badges/chips;
- one clear next action;
- visible reason when blocked/paused.

Move terms such as `PREPARED`, mutation ledger, remote baseline, checkpoint,
playlistId/videoId, API units, schema and HTTP details to secondary diagnostics or
Help unless they are genuinely needed to make a safe decision.

Do not use color as the only state signal.

Critical information must not exist only in a Toast/Snackbar that can truncate or
disappear. Durable/recovery state belongs inline on the owning screen.

## 10. Themes and accessibility

Neon / Blue / Green preserve the same semantics with theme-appropriate tokens.

All important states use at least text/icon/shape in addition to color.

Motion is optional decoration:
- subtle only;
- no rapid flashing;
- no continuous attention animation after acknowledgement;
- respect reduced-motion settings.

The Recovery/attention icon may use a slow breathing animation only while actionable
unfinished work exists.

## 11. File and backup UI

A file extension is not an artifact type.

Save/import/restore UI should:
- default to canonical type-specific folders;
- classify file type from content/schema markers;
- show only relevant artifacts in the primary scoped chooser;
- offer `Інший файл…` for legacy/external locations;
- never silently move legacy files;
- show human-readable metadata before raw filename where possible.

## 12. Change isolation

For every UX hardening commit, record whether it is:
- PRESENTATION_ONLY;
- NAVIGATION_ONLY;
- LIFECYCLE_ONLY;
- FUNCTIONAL_FEATURE.

PRESENTATION_ONLY work must not modify API/storage/business-policy files unless the
change is a pure shared UI helper with no semantic effect.

If a visual change requires business logic changes, split it into a separate issue
and test it as functionality.

## 13. Required gates

Before phone QA:
1. static UI/window audits;
2. JVM/policy tests for any touched policy/state helper;
3. release preflight;
4. exact-HEAD validation;
5. signed build from that exact source.

Phone QA for shared UI changes:
- portrait;
- landscape;
- both rotation directions;
- Neon + at least one alternate skin;
- Back/Cancel/Close;
- scroll retention if scrollable;
- no auto-side-effects;
- long content / long title stress where relevant.

Static/build PASS is not phone PASS.
