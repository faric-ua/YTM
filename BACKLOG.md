# YTM Importer — Roadmap

## Current
**v1.4.13 — Cleanup Wave 3 / SearchCoordinator**

## Test status
- [!] v1.4.12 — **NOT TESTED**.
- [!] v1.4.13 — **NOT TESTED YET**.

## Deferred / open
- [~] Q-001 — OPEN.
- [~] Q-002 — custom dialog entrance motion — DEFERRED BY USER.

## v1.4.13
- [x] add explicit mutable release test-status register;
- [x] mark v1.4.12 NOT TESTED;
- [x] extract SearchCoordinator;
- [x] move search plan domain calculation out of MainActivity;
- [x] move SearchCache get/put out of MainActivity;
- [x] move search.list call out of MainActivity;
- [x] move search quota accounting out of MainActivity;
- [x] move auto best-candidate application out of MainActivity;
- [x] preserve manual selections and exact Project video IDs;
- [x] add search-coordinator audit;
- [ ] GitHub build;
- [ ] phone search regression;
- [ ] 50-track search regression.

## Cleanup Wave 4
Do not start until v1.4.13 search behavior is phone-tested.

Candidate:
- [ ] `PlaylistWriteCoordinator` for create/append/quota-pause/resume write logic.

Keep the extraction incremental; do not combine major UI redesign with write
engine extraction.
