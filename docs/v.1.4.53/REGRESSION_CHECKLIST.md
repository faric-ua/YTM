# v1.4.53 — Regression Checklist

## Search quota recovery
- [ ] Search HTTP 429 creates one durable SEARCH resume job
- [ ] waiting tracks are not presented as ordinary permanent failures
- [ ] cached/exact-ready tracks remain preserved
- [ ] importing another playlist does not destroy the Search resume job
- [ ] app restart preserves the Search resume job
- [ ] explicit Resume restores the saved playlist snapshot and searches only unresolved tracks
- [ ] rotation of Queue/detail does not auto-resume
- [ ] deleting a Search resume job is local-only

## Existing write queue
- [ ] write quota job still appears in Queue
- [ ] write resume still targets the same playlist/account
- [ ] existing PendingJob JSON remains readable
- [ ] no duplicate write after recreation

## Quota semantics
- [ ] Search calls and general API units are labeled as separate local estimates
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
