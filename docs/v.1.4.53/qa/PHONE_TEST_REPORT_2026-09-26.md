# v1.4.53 — Phone Test Report — 2026-09-26

## Verdict

**PASS for the targeted v1.4.53 Quota Recovery / Durable Resume scope.**

Exact tested app identity:
- source: `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`
- signed run: `36195438071`
- version: `1.4.53 (96)`

## Test 1 — Search quota stop / lifecycle

PASS.

Observed:
- real Search quota exhaustion occurred with a 4-track Firestarter workspace;
- 3 already resolved tracks remained resolved;
- 1 unresolved track became `WAITING_QUOTA`;
- a durable SEARCH Queue job was created;
- Queue detail survived portrait/landscape recreation;
- rotation did not resume Search automatically;
- no YTM write started from Search recovery.

## Test 2 — restart + unrelated import isolation

PASS.

Observed:
- SEARCH Firestarter recovery job survived app restart;
- existing WRITE What Evil Lurks job also remained intact;
- importing a separate 31-track workspace changed the current Home workspace only;
- pending SEARCH/WRITE snapshots were not overwritten.

## Test 3 — explicit Search resume after quota reset

PASS.

Observed:
- explicit Queue Resume consumed exactly one Search call for the one WAITING_QUOTA track;
- 3 previously resolved tracks stayed resolved;
- SEARCH recovery job disappeared only after completion;
- independent WRITE job remained queued;
- Search resume itself did not auto-create or modify a remote playlist.

## Test 4 — existing WRITE recovery

PASS.

Observed:
- pre-existing What Evil Lurks WRITE job preserved account/playlist semantics;
- explicit resume after quota recovery completed 4/4 tracks with 0 errors;
- Queue became empty;
- remote playlist id: `PLb2lfAgoEJr4`.

## Test 5 — History durability

PASS.

Observed:
- baseline export: 93 History records;
- post-resume export: 94 History records;
- removed IDs: 0;
- changed common records: 0;
- one expected completed Firestarter WRITE record was added;
- the original Firestarter local-import record and What Evil Lurks pending-write record remained unchanged.

## Accepted findings

- BUG-036 — CLOSED / PHONE PASS.
- BUG-037 — CLOSED / PHONE RETEST PASS.
- BUG-038 — CLOSED / CONTROLLED RETEST PASS.
- UX-029 — CLOSED / PHONE PASS.
- BUG-039 remains deferred: the earlier generic HTTP 429 condition cleared, but its exact Google quota/rate-limit dimension was not identified.
- UX-030 remains deferred: local-only / linked-to-YTM / pending-write linkage should be made more visible from persisted remote playlistId.

## Evidence limits

This was a targeted v1.4.53 acceptance. It does not claim that every historical full-app regression was rerun.

## Stable closeout — 2026-09-26

The accepted phone-tested package was published unchanged as stable `v1.4.53`.

- exact app source: `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`;
- signed run: `36195438071`;
- release tag: `v1.4.53`;
- checkpoint: `checkpoint-v1.4.53-phone-pass`;
- publisher run: `36250364471` — PASS;
- APK SHA-256: `841019959d02d4e5368a6cc954d815563ac85535e7bc4a577b69653d21acaf2e`.

An equal-version production updater smoke is still required after publication; it is not a rerun of the targeted functional phone tests above.
