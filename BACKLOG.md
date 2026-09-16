# YTM Importer — Roadmap

## Current
v1.4.16 — Cleanup Wave 5 / DestinationCoordinator

## Known
- BUG-001/Q-001 OPEN
- BUG-002/Q-002 DEFERRED
- BUG-003/Q-003 auth recovery FAIL — DEFERRED FOR LATER FIX

## v1.4.16
- [x] extract DestinationCoordinator
- [x] destination playlist cache/selection extraction
- [x] duplicate scan + quota accounting extraction
- [x] duplicate write-plan extraction
- [x] destination coordinator audit
- [x] per-release docs/diagrams/QA snapshot
- [ ] GitHub build
- [ ] phone tests

## Next
Preferred v1.4.17:
continue MainActivity cleanup only after v1.4.16 build/phone regression confirms no destination behavior regression.

## Later bug-fix wave
Fix every FAIL/BLOCKED case accumulated in BUG_REGISTER and release test runs,
including BUG-003/Q-003 silent Google/YTM recovery after in-place update.
