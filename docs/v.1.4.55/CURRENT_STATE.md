# v1.4.55 — Current Working State / Assistant Handoff

Last updated: 2026-09-29

## Release / branch

- Release: `v1.4.55`
- Branch: `feat/v1.4.55-ux-hardening`
- Immutable functional reference:
  `e553c6dcb0f918a51f40bfa4d783cb11b3086472`
  (phone-accepted v1.4.54 Test 1–9 functional baseline)
- Umbrella: issue #56 — `v1.4.55: UX hardening and control wave`

## Product rule

v1.4.55 is a hardening/control release.

The default assumption is:
**working v1.4.54 functionality is correct and must be preserved.**

Presentation, layout, lifecycle and discoverability work must not silently change:
- YouTube/YTM API request order/retry behavior;
- Search/write/rollback auto-start rules;
- persisted playlist identity/linkage;
- Queue/Pending ownership;
- History semantics;
- Bulk durable mutation ledger;
- exact rollback ownership;
- backup/restore data meaning;
- quota/rate classification.

Any change that needs different functional semantics becomes a separate
FUNCTIONAL_FEATURE issue and gets its own tests.

## Locked contracts — read before code changes

1. `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`
2. `docs/design/UI_WINDOW_QA_CONTRACT.md`
3. `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
4. `docs/design/TILE_UI_CONTRACT.md`
5. relevant legacy/system contract and audit from
   `docs/assistant-kit/AUDIT_CATALOG.md`

Repository truth beats chat memory.

## Readability audit

Canonical source:
`docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`

Audit is complete at the planning level.

Main conclusion:
the dominant defect is information hierarchy, not color.
Users must be able to identify in about 1–2 seconds:
- what object/screen this is;
- current state;
- primary result/counters;
- whether remote work already happened;
- whether attention is required;
- safe next action.

Technical diagnostics remain available but secondary.

## Phase A — shared non-functional hardening

Purpose: improve UI/lifecycle only, without business behavior changes.

Current source work already on branch:
- Bulk Preview primary wording/readability cleanup;
- Bulk Session primary status wording cleanup;
- Menu wording cleanup;
- History Quick Restore surfaced;
- reusable scroll state exists;
- scroll preservation extended to Playlist Hub, Bulk Session,
  Recent File Chooser and Service;
- full-screen adaptive action row now accounts for label fit;
- History delete confirmation is lifecycle-restorable.

Still open in Phase A:
- project-wide scroll-retention completion / phone matrix (#48);
- selectable-text range preservation (#49);
- destructive-confirmation project-wide audit beyond History (#42);
- one canonical action layout policy across screen + dialog footers (#47/#26/#37);
- Bulk Preview/Session semantic hierarchy and diagnostics separation (#23/#45/#50);
- critical information must not be Toast-only;
- Neon + alternate-skin accessibility/readability sanity;
- temporary QA controls must be removed or compile-gated before public candidate.

## Phase B — management / discoverability

After Phase A source is coherent:
- History search + semantic filters (#41);
- History grouping/provider findability (#25);
- History Quick Restore acceptance (#55);
- Recovery Center + compact breathing attention icon (#53);
- type-aware file/backup library and scoped chooser (#54);
- simplified Termux operator menu (#52).

These may add local navigation/discoverability, but must not auto-start remote work.

## Phase C — local workflow convenience

- local playlist Edit (#30);
- blank URL validation (#29);
- Review manual URL/manual selection UX (#40);
- Bulk preparation presentation/state cleanup (#27/#28).

## Issue grouping

Umbrella:
- #56

Phase A core:
- #50 readability
- #47 action layout
- #26 action styling/labels
- #37 window/footer consolidation
- #48 scroll retention
- #49 text selection retention
- #42 destructive confirmation lifecycle
- #23 / #45 Bulk Preview readability

Phase B:
- #41 / #25 History
- #55 Quick Restore
- #53 Recovery Center
- #54 file library
- #52 Termux menu

Phase C:
- #30 / #29 / #40 / #27 / #28

## Change classification

Every implementation commit should be classifiable as one of:
- `PRESENTATION_ONLY`
- `NAVIGATION_ONLY`
- `LIFECYCLE_ONLY`
- `FUNCTIONAL_FEATURE`

If a commit mixes categories in a risky way, split it.

## QA strategy

Do not build one APK per tiny visual change.

Batch coherent Phase A changes, run static/exact-HEAD gates, then one representative
phone matrix:
- Home + Menu;
- Bulk Preview;
- Bulk Session;
- History list/detail;
- Queue;
- Data/file chooser;
- long Help + destructive confirmation;
- portrait / landscape / both rotations;
- Neon + at least one alternate skin;
- scroll retention;
- selectable text;
- Back/Cancel/Close;
- no automatic Search/write/rollback/delete.

Static/build PASS is not phone PASS.

## Immediate next work

1. finish assistant/bootstrap safety wiring so future sessions must read this release;
2. finish Phase A source audit against open issues;
3. implement remaining shared helpers at the shared layer, not per-screen patches;
4. refresh manifest + run exact-HEAD validation;
5. only then ask for one consolidated phone candidate.

Do not start Phase B functional/discoverability work until Phase A shared contracts
are coherent enough that later screens can reuse them.
