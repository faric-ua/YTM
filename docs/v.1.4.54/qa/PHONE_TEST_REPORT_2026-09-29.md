# v1.4.54 — Phone Test Report — 2026-09-29

## Overall status

**PHONE QA PASS — targeted Tests 1–9 complete.**

Accepted functional source:
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Accepted signed run for the final compatibility/recovery chain:
`36579457780`

## What was proved

The phone-QA series proved the release contract for:
- identity-preserving History Recovery;
- visible local ↔ YTM linkage without title-based invention;
- read-only Bulk planning before mutation;
- scoped executable-row selection;
- durable Bulk session state;
- explicit restart recovery with no auto-resume;
- durable quota/rate-limit pause behavior;
- exact mutation-ledger rollback;
- exact rollback inside a pre-existing playlist;
- interrupted rollback recovery without repeating completed reverse mutations;
- legacy v1.4.53-era History/Queue/backup compatibility.

## Safety invariants confirmed

- Rotation/recreation did not silently start remote work.
- Cold reopen did not automatically resume paused Bulk work or rollback.
- Remote ownership was based on exact persisted IDs, not equal titles.
- Session rollback removed only session-owned mutations.
- A pre-existing playlist survived Test 7 rollback.
- Test 8 resumed only after explicit user action.
- Legacy History restore did not auto-Search or auto-write.
- Legacy History restore did not invent YTM linkage from the playlist title.
- Existing Pending Queue data remained readable after in-place upgrades.

## Test 9 final evidence

Full Backup:
- created successfully before compatibility restore.

Legacy History:
- `URL snapshot • PLIrF7GkQzd-E`;
- 2026-09-23;
- restored as the current 813-track local workspace;
- state shown as `Лише локально`.

Legacy Queue:
- `The Prodigy - Baby's Got A Temper (2002)`;
- pause reason `Rate limit`;
- remaining 3;
- added 0/3.

Result: **9+ / PHONE PASS**.

## Publication note

The phone-accepted v1.4.54 source contains temporary Test 5/Test 8 QA controls used
to produce deterministic fault evidence. Therefore v1.4.54 is retained as the
immutable **functional phone-acceptance checkpoint**, not promoted as the public
stable binary.

The successor `v1.4.55` keeps the v1.4.54 behavior as its immutable functional
reference and compile-gates those QA controls to debug builds before a public
candidate.

This avoids modifying the already accepted v1.4.54 source after Tests 1–9 and avoids
publishing user-visible fault-injection controls.
