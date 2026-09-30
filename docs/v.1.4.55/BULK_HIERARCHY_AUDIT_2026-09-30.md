# v1.4.55 — Bulk Semantic Hierarchy Audit — 2026-09-30

## Scope

Issues: #23 / #45 / #50.

Classification: **PRESENTATION_ONLY**.

This task must not change:
- Bulk plan classification or selection policy;
- Search planning;
- session creation/checkpoint semantics;
- write order/retry behavior;
- mutation ledger;
- rollback ownership/resume rules;
- quota/rate classification;
- remote playlist identity.

## What is already good

Bulk Preview already exposes Ukrainian user-facing plan-state labels:
- Новий;
- Пов’язано з YTM;
- Уже синхронізовано;
- Потрібен пошук;
- У черзі;
- Заблоковано.

Bulk Session also maps session/row states to readable Ukrainian labels.

Therefore this task is not an enum-renaming rewrite. The remaining problem is
information hierarchy.

## Preview findings

### Summary block

`renderSelectionSummary()` currently puts all of these into one TextView:
- total playlist count;
- every plan-state count;
- included NEW/LINKED counts;
- excluded count;
- Search call estimate;
- write-unit estimate;
- remote-read units;
- long policy explanation.

The information is correct but same-weight and slow to scan.

### Plan card

`planRow()` currently combines in one details TextView:
- state + playlist title;
- tracks / ready / unresolved;
- Search/cache diagnostics;
- planned create/add;
- write units;
- reason/explanation.

Selection is a separate CheckBox, which is good, but the entity/title, state,
planned mutation and diagnostics still compete visually.

Target hierarchy:
1. playlist title;
2. state + included/excluded signal;
3. compact track/ready/unresolved counters;
4. planned create/add result;
5. reason/safety result;
6. Search/cache/API diagnostics as secondary detail.

## Session findings

### Session summary

`render()` already separates the top-level session state into `statusText`, but
`summaryText` still merges:
- plan count;
- created playlists / inserted tracks;
- checkpoint;
- remote baseline size;
- PREPARED attention count;
- terminal failures;
- rollback completed / remaining;
- lastError;
- restart/explicit-resume policy paragraph.

Target hierarchy:
1. current session state;
2. primary outcome counters;
3. attention/remaining work;
4. checkpoint/baseline identity facts;
5. durable error detail;
6. lifecycle policy copy as secondary text/help.

### Session row

`rowView()` is one TextView containing:
- playlist title;
- row state;
- applied insert count for NEW;
- YTM ID;
- up to five terminal-failure track names/reasons;
- row lastError.

Target hierarchy:
1. playlist title;
2. row state/result count;
3. failure count if any;
4. YTM ID and detailed errors as secondary diagnostics.

## Transient-message finding

Preview session-creation failure currently writes the durable failure into
`statusText` **and** emits a long Toast containing the same technical error. The
durable owning-screen state is the correct source of truth; transient copy should
be concise and must not be the only place carrying technical detail.

Bulk Session also has generic LONG Toast plumbing. During this task, preserve
durable details on-screen and shorten transient summaries where technical text can
truncate.

## Existing theme support

`AppThemeManager.SemanticPalette` already provides skin-aware:
- success / successFill;
- warning / warningFill;
- danger / dangerFill;
- accent / neutral palette values.

Use text + shape/typography with color. Do not make color the only signal.

## Next source step

Design the smallest reusable presentation helpers necessary for:
- state label/badge;
- compact key/value counters;
- secondary diagnostics.

Then patch Preview and Session using existing model values only.

## Phone acceptance later

Consolidated Phase A phone matrix must include:
- Preview with NEW, LINKED, no-op/blocked/needs-search where available;
- included vs excluded rows;
- Session READY/RUNNING/completed or pause state;
- rollback state with completed + remaining counts;
- failure/diagnostic row where available;
- portrait + landscape;
- Neon + at least one alternate skin.

Static/source/build PASS is not phone PASS.
