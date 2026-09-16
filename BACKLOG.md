# YTM Importer — Roadmap

## Current
**v1.4.12 — Cleanup Wave 2**

## Deferred / open
- [~] Q-001 — Review wording + Project-save feedback — OPEN.
- [~] Q-002 — custom dialog entrance motion — DEFERRED BY USER; do not block roadmap.

## v1.4.12
- [x] remove legacy Import UI from MainActivity;
- [x] remove legacy Review/candidate UI from MainActivity;
- [x] remove legacy Pending detail UI from MainActivity;
- [x] remove legacy History detail/action UI from MainActivity;
- [x] remove legacy Data/export/backup/restore UI from MainActivity;
- [x] remove legacy Service/Diagnostics/SearchCache UI from MainActivity;
- [x] remove obsolete Main request codes/export state/LocalBackupManager;
- [x] preserve original permissive file picker in ImportActivity;
- [x] add MainActivity cleanup audit;
- [ ] GitHub build;
- [ ] phone regression.

## Cleanup Wave 3
Do only one extraction at a time:
- [ ] extract search orchestration into `SearchCoordinator`, or
- [ ] extract playlist write execution into `PlaylistWriteCoordinator`.

Preferred next target: **SearchCoordinator** because it has a smaller blast
radius than the create/append write engine.

## Later
- [ ] extract auth presentation/orchestration;
- [ ] extract destination/write coordinator;
- [ ] reduce MainActivity toward Home + navigation + result bridges;
- [ ] public-release regression with the 50-track Clubland set.
