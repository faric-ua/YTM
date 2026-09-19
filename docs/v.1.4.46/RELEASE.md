# v1.4.46 — Home Layout Prototype Alignment Phase 1

## Scope

UX-019 starts aligning the Home screen with the previously approved top-left
prototype as a **layout/hierarchy reference only**.

This phase intentionally does not invent new quick actions or bottom-navigation
labels that are not yet explicitly captured in the repository.

## Implementation

- keeps the existing four workflow actions and their semantics unchanged;
- keeps the existing utility row: History / Queue / Quota / Menu;
- compacts the Home header slightly;
- separates the live Home status into its own theme-aware accent/info card;
- keeps the current-playlist summary in a separate `Поточний плейлист` card;
- establishes the approved upper-page hierarchy:
  compact header → four-step block → utility row → accent/info block → current playlist;
- preserves Neon / Blue / Green themes;
- preserves Home workflow-state colors and all auth/search/write behavior.

## Version

- versionName: **1.4.46**
- versionCode: **86**

Status: **IMPLEMENTED / PHONE QA NEEDED**.
