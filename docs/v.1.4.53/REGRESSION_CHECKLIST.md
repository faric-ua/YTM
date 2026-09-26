# v1.4.53 — Regression Checklist

## Search quota recovery
- [x] Search HTTP 429 creates one durable SEARCH resume job
- [x] waiting tracks are not presented as ordinary permanent failures
- [x] cached/exact-ready tracks remain preserved
- [x] importing another playlist does not destroy the Search resume job
- [x] app restart preserves the Search resume job
- [ ] explicit Resume restores the saved playlist snapshot and searches only unresolved tracks
- [x] rotation of Queue/detail does not auto-resume
- [ ] deleting a Search resume job is local-only

## Existing write queue
- [x] write quota job still appears in Queue
- [x] write resume still targets the same playlist/account
- [x] existing PendingJob JSON remains readable
- [x] no duplicate write after recreation

## Quota semantics
- [x] Search calls and general API units are labeled as separate local estimates
- [ ] server HTTP 429 remains authoritative
- [ ] write preflight does not describe general units as total Search+write budget
- [ ] Pacific-time day reset still resets local daily counters

## History durability
- [ ] capture History JSON before quota test
- [ ] capture History JSON immediately after quota stop
- [ ] capture History JSON after restart/quota reset
- [ ] stable entry ids remain unless user explicitly deletes/clears/restores
- [ ] Queue deletion does not delete History

## Core regression
- [ ] version/build identity
- [ ] Back/navigation ownership
- [ ] rotation/recreation
- [ ] modal lifecycle
- [ ] cancel/no-op behavior
- [ ] error path
- [ ] no accidental duplicate operation

Phone QA progress 2026-09-26:
- Test 1 PASS — durable SEARCH job + rotation/no auto-resume;
- Test 2 PASS — restart + unrelated import isolation;
- Test 4 PASS — existing WRITE job preserved with 0/4, same account, no playlistId, no auto-write;
- BUG-037 PASS — Search bucket and non-Search 10k-unit bucket separated;
- Test 3 awaits Search quota reset;
- History baseline JSON captured; post-resume stable-ID comparison still pending.
