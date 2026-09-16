# YTM Importer — Roadmap

## Current
**v1.4.10 — Rotation Layout + Stable Dialog Anchor**

## Open question carried forward
- [~] Q-001 remains OPEN — revisit later.

## v1.4.10
- [x] diagnose Step 2 shift as LinearLayout baseline alignment;
- [x] disable baseline alignment for Step rows;
- [x] harden other horizontal action rows;
- [x] diagnose remaining dialog movement as content-height centering;
- [x] switch custom dialogs to stable TOP anchoring;
- [x] keep safe system-bar/cutout/bottom insets;
- [x] add rotation-layout audit;
- [x] update dialog-bounds audit;
- [ ] GitHub build;
- [ ] repeated portrait/landscape phone regression.

## Next — cleanup wave 2
- [ ] remove unreachable legacy Import flow;
- [ ] remove unreachable legacy History flow;
- [ ] remove unreachable legacy Data/Backup flow;
- [ ] remove obsolete Pending paths;
- [ ] reduce MainActivity further;
- [ ] dead-code audit.
