# YTM Importer — Roadmap

## Current
**v1.4.1 — Legacy Destination Cleanup**

## Open question carried forward
- [~] **Q-001: v1.3.2 Review wording + Project save feedback** — OPEN;
  повернутися пізніше. Не блокує roadmap. Див. `OPEN_QUESTIONS.md`.

## v1.4.1
- [x] remove old Destination/Create AlertDialog flow;
- [x] keep DestinationActivity navigation;
- [x] keep create/append write core unchanged;
- [x] add MainActivity audit script;
- [x] document MainActivity size reduction;
- [ ] GitHub build;
- [ ] v1.4.x phone regression.

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
- [ ] Material 3 components;
- [ ] responsive typography/spacing;
- [ ] accessibility and loading/empty/error consistency.

## Public distribution
- [ ] OAuth production readiness;
- [ ] hosted privacy-policy URL;
- [ ] non-developer Google account test;
- [ ] second-person usability test.
