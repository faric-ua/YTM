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

- [ ] output folder starts `YTM-Importer-Account-Consolidated-`;
- [ ] output manifest schema = 3;
- [ ] `backupMode = CONSOLIDATED_FULL`;
- [ ] final non-empty playlists get YTM Project files;
- [ ] empty playlists use `SKIPPED_EMPTY`;
- [ ] source folders are not modified;
- [ ] consolidated manifest opens through normal backup picker;
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

- [ ] select their common parent folder;
- [ ] one chain head is found;
- [ ] preview reports chain length 2;
- [ ] scope = `SELECTED (2)`;
- [ ] final playlists = 2;
- [ ] YTM Project sources = 2;
- [ ] MISSING events = 0;
- [ ] materialization writes 2 YTM Project files + `manifest.json`;
- [ ] normal manifest picker opens consolidated folder as v3 / SELECTED / 2 of 2;
- [ ] `top 3` opens exact 3/3;
- [ ] repeat Search still plans 0 new `search.list`;
- [ ] old baseline and old delta still exist and open as before.

## Not a full release regression

Phone coverage of real NEW/UPDATED/MISSING/FAILED chains can be added separately.
