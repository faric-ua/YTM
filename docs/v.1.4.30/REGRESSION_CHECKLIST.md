# v1.4.30 — Regression Checklist

## Discovery

- [ ] choose common parent folder containing baseline + delta;
- [ ] only YTM Importer backup sessions are considered;
- [ ] one delta head resolves automatically;
- [ ] multiple independent delta heads show a picker;
- [ ] missing baseSessionName target fails clearly;
- [ ] cycle fails clearly.

## Replay

- [ ] base `EXPORTED` project is loaded and validated;
- [ ] base `SKIPPED_EMPTY` is preserved as empty;
- [ ] `NEW` adds state;
- [ ] `UPDATED` replaces state;
- [ ] `UNCHANGED` inherits previous project;
- [ ] `UNCHANGED` validates metadata/fingerprint;
- [ ] `MISSING` removes final state;
- [ ] `FAILED` stops exact consolidation;
- [ ] playlistId/privacy mismatches fail safely.

## Scope

- [ ] `ALL` remains `ALL`;
- [ ] `SELECTED` remains `SELECTED`;
- [ ] selected `scopePlaylistIds` survive materialization;
- [ ] scope drift inside a chain fails.

## Materialization

- [x] output folder uses timestamp-first `YYMMDD-HHMMSS-YTM-Full`;
- [ ] output manifest schema = 3;
- [ ] `backupMode = CONSOLIDATED_FULL`;
- [ ] final non-empty playlists get YTM Project files;
- [ ] empty playlists use `SKIPPED_EMPTY`;
- [x] source folders remain readable/re-resolvable after materialization;
- [x] consolidated manifest opens through normal backup picker;
- [ ] consolidated backup can be read as an incremental baseline.

## API boundary

- [ ] storage chain restorer contains no `YouTubeApi`;
- [ ] no `search.list`;
- [ ] no `playlistItems.list`;
- [ ] no remote create/add/delete operations.

## Primary phone QA

Use the existing real-phone chain from v1.4.29:

- full selective baseline: 2 projects;
- one schema-v3 delta with `UNCHANGED=2`, 0 project files.

Expected:

- [x] select their common parent folder;
- [x] one chain head is found;
- [x] preview reports chain length 2;
- [x] scope = `SELECTED (2)`;
- [x] final playlists = 2;
- [x] YTM Project sources = 2;
- [x] MISSING events = 0;
- [x] materialization writes 2 YTM Project files + `manifest.json`;
- [x] normal manifest picker opens consolidated folder as v3 / SELECTED / 2 of 2;
- [x] `top 3` opens exact 3/3;
- [x] repeat Search still plans 0 new `search.list`;
- [x] source chain re-resolves after materialization and old baseline still opens v2 / SELECTED / 2 of 2.

## Not a full release regression

Phone coverage of real NEW/UPDATED/MISSING/FAILED chains can be added separately.
