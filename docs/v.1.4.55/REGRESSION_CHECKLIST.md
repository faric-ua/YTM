# v1.4.55 — Regression Checklist

## Identity / gates
- [x] versionName 1.4.55 / versionCode 98
- [x] hardening branch created from exact phone-accepted v1.4.54 source
- [x] UX change safety contract committed
- [x] app-wide readability audit committed
- [ ] exact-HEAD release preflight
- [ ] JVM tests
- [ ] unsigned release assemble
- [ ] signed exact-HEAD APK
- [ ] consolidated real-phone matrix

## Functional invariants
- [ ] no Search starts from rotation/recreation/opening Help
- [ ] no YTM write starts from rotation/recreation/opening recovery surfaces
- [ ] no rollback/delete starts without explicit action
- [ ] persisted playlistId remains authoritative; title never creates linkage
- [ ] Queue/Pending state remains readable and durable
- [ ] History restore preserves identity and does not auto Search/write
- [ ] Bulk session durable state and exact rollback ledger remain unchanged
- [ ] backup/restore data meaning remains backward compatible

## Wave A — shared non-functional hardening
- [ ] Menu/user-facing wording
- [ ] Bulk Preview readability hierarchy
- [ ] Bulk Session readability hierarchy
- [ ] shared action layout semantics
- [ ] destructive confirmation lifecycle
- [ ] scroll retention
- [ ] selectable-text retention
- [ ] critical state not transient-only
- [ ] Neon/Blue/Green semantic sanity

## Wave B — management/discoverability
- [ ] History filters/findability
- [ ] History logical grouping/provider affordances
- [ ] History Quick Restore
- [ ] Recovery Center + compact breathing attention icon
- [ ] type-aware YTM file library
- [ ] simplified Termux operator menu

## Wave C — local convenience
- [ ] local playlist Edit
- [ ] blank URL inline validation
- [ ] Review manual URL/manual-selection polish
- [ ] Bulk preparation presentation/state cleanup
