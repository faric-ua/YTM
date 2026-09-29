# v1.4.54 — Stabilization Checkpoint

Date: 2026-09-29

## Functional checkpoint

Accepted source:
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Accepted signed run:
`36579457780`

Phone acceptance:
**Tests 1–9 = PASS.**

This SHA is the immutable functional reference for the next UX/control release.

## Locked behavior

Subsequent work must preserve:
- Search/write/rollback explicit-action rules;
- exact persisted playlist identity;
- no title-only remote linkage or ownership;
- Queue/Pending ownership;
- History semantics;
- backup/restore meaning;
- durable Bulk session semantics;
- exact mutation ledger;
- rollback ownership and recovery;
- quota/rate classification.

## Publication disposition

v1.4.54 is **functionally closed but not published as the stable public binary**.

Reason:
the accepted Test 1–9 source intentionally contains temporary Test 5/Test 8
fault-injection controls used for deterministic phone QA. Rebuilding v1.4.54 only
to remove those controls would create a new, unaccepted source after the completed
9/9 suite.

Instead:
- preserve this exact source as the phone-accepted reference;
- do not create a `v1.4.54` stable tag/release from the QA-enabled source;
- continue on `feat/v1.4.55-ux-hardening`;
- v1.4.55 compile-gates the QA controls behind `BuildConfig.DEBUG`;
- require normal exact-HEAD validation and consolidated phone QA before any public
  v1.4.55 candidate.

This is an intentional supersession, not a missing QA result.
