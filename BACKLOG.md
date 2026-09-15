# YTM Importer — Roadmap

## Current
**v1.4.2 — Safe Insets + Dialog Polish**

## Open question carried forward
- [~] **Q-001: v1.3.2 Review wording + Project save feedback** — OPEN; повернутися пізніше. Див. `OPEN_QUESTIONS.md`.

## v1.4.2
- [x] add shared `UiChrome` helper;
- [x] add top/bottom safe-area insets on primary screens;
- [x] style the `Ще` menu;
- [x] style the import source menu;
- [x] style Review project actions menu;
- [x] add UI chrome audit script;
- [ ] GitHub build;
- [ ] phone regression.

## Next — cleanup wave 2
- [ ] remove legacy Import dialog/file flow from MainActivity;
- [ ] remove legacy History dialog flow from MainActivity;
- [ ] remove legacy Data/Backup dialog flow from MainActivity;
- [ ] remove legacy Pending detail dialogs where no longer reachable;
- [ ] re-run dead-code audit.

## After cleanup
- [ ] extract account/auth presentation from MainActivity;
- [ ] extract search orchestration where safe;
- [ ] reduce MainActivity toward Home/navigation coordinator;
- [ ] Material 3 components or a fuller custom design system;
- [ ] accessibility and empty/loading/error consistency.
