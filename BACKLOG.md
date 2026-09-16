# YTM Importer — Roadmap

## Current
**v1.4.8 — Safe Dialog Viewport**

## Open question carried forward
- [~] Q-001 remains OPEN — revisit later.

## v1.4.8
- [x] fix Quota top clipping;
- [x] fix Problem Tracks top clipping;
- [x] shared system-bar safe viewport for all custom dialogs;
- [x] display-cutout handling;
- [x] preserve centered short dialogs;
- [x] preserve scrollable tall dialogs;
- [x] audit all Menu / Message / Record call sites through shared UiChrome;
- [x] add dialog-bounds audit;
- [ ] GitHub build;
- [ ] phone regression.

## Next — cleanup wave 2
- [ ] remove old Import dialog/file flow that is no longer reachable;
- [ ] remove old History dialog flow that is no longer reachable;
- [ ] remove old Data/Backup dialog flow that is no longer reachable;
- [ ] remove old Pending detail paths where replaced by dedicated screens;
- [ ] reduce MainActivity further;
- [ ] dead-code audit after cleanup.
