# v1.4.55 — Backlog / Rules Reconciliation

Date: 2026-09-29

Purpose: reconcile the phone-QA findings, repository backlog and locked UI contracts
before continuing the v1.4.55 hardening/control work.

Functional reference:
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Active branch:
`feat/v1.4.55-ux-hardening`

## 1. Locked project rules — confirmed

The project already has explicit authoritative rules. They are not chat-only notes.

- `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`
- `docs/design/UI_WINDOW_QA_CONTRACT.md`
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
- `docs/design/TILE_UI_CONTRACT.md`

The hard rule is that UI/readability/lifecycle work must preserve the accepted
v1.4.54 business/API/storage/rollback behavior unless a separate functional issue
explicitly changes that behavior.

Every implementation commit must be classifiable as:
- PRESENTATION_ONLY;
- NAVIGATION_ONLY;
- LIFECYCLE_ONLY;
- FUNCTIONAL_FEATURE.

Presentation/lifecycle work must not silently change:
- Search/write/rollback auto-start;
- API request/retry ordering;
- playlist identity/linkage;
- Queue ownership;
- History semantics;
- Bulk mutation ledger;
- exact rollback ownership;
- backup/restore data meaning;
- quota classification.

## 2. Readability audit — confirmed complete at planning level

Canonical audit:
`docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`

It covers:
- Home;
- Menu;
- Playlist Hub;
- Review;
- Destination;
- Bulk Preview;
- Bulk Session;
- Quota;
- Queue/Pending;
- History list/detail;
- Data/Backup;
- Import;
- URL Snapshot;
- Service;
- Storage/Recent chooser;
- ListSelector;
- shared dialogs/Help;
- Toast/Snackbar.

The dominant problem is information hierarchy, not only color.

Pass criterion: within about 1–2 seconds the user should understand:
- what object/screen this is;
- current state;
- primary result/counters;
- whether remote work already happened;
- whether attention is required;
- the safe next action.

Technical diagnostics stay available but secondary.

## 3. Current source progress already on v1.4.55

Already implemented on the hardening branch:
- first-pass Menu wording cleanup;
- History Quick Restore surfaced through the existing safe restore path;
- first-pass Bulk Preview user wording;
- first-pass Bulk Session status wording;
- reusable `ScrollPositionState`;
- Quota scroll retention;
- Playlist Hub scroll retention;
- Bulk Session scroll retention;
- Recent File Chooser scroll retention;
- Service scroll retention;
- full-screen action row decisions account for label width;
- History delete confirmation restores after recreation;
- one shared action-width fit policy now drives both full-screen and dialog row decisions;
- Queue delete confirmation now uses semantic restorable modal state;
- Service SearchCache destructive confirmations now use semantic restorable modal state.

These are presentation/lifecycle hardening changes. They must still pass exact-HEAD
static/build gates and consolidated real-phone QA.

## 4. Phase A — remaining shared hardening

Still required before moving deeply into management features:

1. #48 complete project-wide scroll-retention audit.
2. #49 selectable-text range restoration.
3. #42 finish destructive-confirmation phone matrix after the source audit/migrations.
4. #47 / #26 / #37 phone-verify one action-layout contract across screens/dialogs.
5. #23 / #45 / #50 finish Bulk Preview/Session visual hierarchy.
6. Critical state/error/recovery must not be Toast-only.
7. Neon + Blue/Green semantic/accessibility sanity.
8. Remove or compile-gate temporary phone-QA controls before public candidate.
9. Reconcile any old shared UI retest debt that is naturally covered by this matrix
   instead of creating separate APK loops.

## 5. Phase B — management / discoverability

Keep these together because they improve control without changing remote execution:

- #41 History text search + semantic filters;
- #25 History logical playlist/provider grouping while preserving operation audit;
- #55 visible History Quick Restore;
- #53 Recovery Center;
  - compact Home warning icon;
  - badge count;
  - slow breathing animation only while actionable work exists;
  - no auto-resume;
- #54 type-aware file library / canonical folders / scoped chooser;
- #52 simplified Termux operator menu/status;
- BACKLOG UX-028: Home last-action detail drill-down;
  reuse the existing History detail instead of duplicating data.

## 6. Phase C — local convenience

- #30 local playlist Edit;
- #29 blank URL inline validation;
- #40 Review multiline manual URL + Clear + manual-selection emphasis;
- #27 polished Bulk preparation state;
- #28 clear stale Bulk preparation status;
- BACKLOG UX-027 duplicate-choice action layout is folded into the shared adaptive
  action contract and should be accepted through the common phone matrix.

## 7. Theme / older UI debt folded into shared QA

BACKLOG UX-009 Theme State Contrast remains relevant.

Do not redesign Neon semantics. During the v1.4.55 shared matrix:
- Neon remains the semantic reference;
- Blue/Green must preserve ready/attention/error/inactive distinctions;
- color is not the only signal.

Older UI items already superseded by shared contracts should be tested through the
shared matrix instead of receiving isolated one-off implementations.

## 8. Completed v1.4.54 functionality — do not reopen as redesign work

Phone-accepted functionality includes:
- History Recovery identity-preserving restore;
- Bulk read-only preview;
- scoped executable-row selection;
- durable Bulk sessions;
- explicit restart recovery;
- exact rollback by ledger IDs;
- interrupted rollback recovery;
- no title-based ownership/linkage;
- no automatic remote continuation after restart.

Completed GitHub issues already closed include:
- #24 BUG-040;
- #31 BUG-042;
- #32 BUG-043;
- #34 BUG-044;
- #35 UX-037;
- #36 BUG-045;
- #38 BUG-046;
- #39 Wave 4 rollback;
- #43 UX-041;
- #44 UX-042;
- #46 UX-044;
- #51 BUG-050.

v1.4.55 may make these easier to understand, but must not reinterpret their behavior.

## 9. Legacy functional debt kept separate

Older unresolved functional/auth/distribution findings from BACKLOG are not silently
folded into presentation work.

Examples:
- BUG-001/BUG-002 deferred historical retests;
- BUG-004 / BUG-013 natural authorization-edge acceptance;
- BUG-030 Play Protect distribution/reputation;
- other old functional items explicitly marked pending.

If one of them is touched, it becomes its own functional scope and test gate.

## 10. Execution rule from here

Order:
1. finish Phase A shared source hardening;
2. static audits + JVM/policy tests where state helpers changed;
3. exact-HEAD release preflight + unsigned assemble;
4. one signed Wave-A candidate;
5. one consolidated phone matrix;
6. only then Phase B management/discoverability;
7. then Phase C local convenience.

Do not build a new APK for every small visual change.

Repository truth and real-phone evidence remain authoritative.
