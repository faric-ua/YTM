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

## Implementation / validation result — 2026-10-01

Source/static/build conclusion: **PASS**.

Implemented the smallest shared presentation layer:
- `BulkHierarchyChrome.card()` — shared themed container;
- `badge()` — state label with semantic tone plus text;
- `metrics()` / `primary()` — compact primary counts/planned work;
- `body()` / `secondary()` — reason and technical diagnostics.

Bulk Preview now renders:
1. playlist title;
2. semantic state badge;
3. explicit included/excluded CheckBox for executable rows;
4. track/ready/unresolved counters;
5. planned create/add result;
6. reason;
7. Search/cache/write-unit diagnostics as secondary text.

Bulk Session now renders:
1. session/playlist title;
2. semantic state badge;
3. primary created/inserted or applied row counts;
4. prepared/failure/rollback attention information;
5. durable error/failure detail;
6. checkpoint/baseline/YTM ID/lifecycle policy as secondary diagnostics.

Transient-message correction:
- Preview session-create failure keeps the exact error in `statusText` and uses a
  concise Toast directing the user back to the screen;
- Session exactness/sync/rollback failure Toasts no longer carry raw technical
  detail as the only transient payload; durable session/row/status state remains
  visible.

Static enforcement:
- `scripts/v1455-ux-hardening-audit.sh` requires the shared hierarchy in Preview
  and Session;
- it rejects the old dense `summaryText` summary path;
- it rejects reintroduction of raw Preview `errorText` or Session
  `error.message` / `exactnessError` Toast patterns.

Evidence:
- source commit: `6ad0b789bd6d0263d4ba65ce2fe92a703fe20e12`;
- manifest checkpoint: `490a1f0e218032aa46a723a455dac10e87136a9e`;
- Validate Android run: `36795036312` — **SUCCESS**;
- release preflight PASS;
- JVM unit tests PASS;
- unsigned release assemble PASS.

No phone PASS is claimed from this evidence.

## Phone acceptance later

Consolidated Phase A phone matrix must include:
- Preview with NEW, LINKED and no-op/blocked/needs-search where available;
- included vs excluded rows, with the selected plan summary updating visibly;
- Preview title/state/counters/planned mutation/reason/diagnostics readable in that order;
- Session READY/RUNNING/completed or pause state;
- Session primary outcome counters visible before checkpoint/baseline diagnostics;
- rollback state with completed + remaining counts;
- failure/diagnostic row where available, with technical detail durable after Toast disappears;
- portrait + landscape and both rotation directions;
- rotation/reopen must not auto-start sync, Search or rollback;
- Neon + at least one alternate skin.

Static/source/build PASS is not phone PASS.
