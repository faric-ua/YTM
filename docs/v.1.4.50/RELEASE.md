
# YTM Importer v1.4.50 — Skin System

## Goal

Introduce a reusable Skin contract while preserving existing Neon/Blue/Green
appearance and behavior.

## Scope

- common `Skin` model for the three existing built-in styles;
- named `SkinPalette` visual tokens;
- separate `SemanticPalette` state roles;
- static audit preventing semantic roles from collapsing back into unlabelled palette fields;
- no intentional visual redesign in Wave 1.

## Architecture / behavior changes

- `AppThemeManager` resolves the selected style through a built-in Skin registry;
- UI visual tokens still come from `AppThemeManager.palette(...)`;
- state colors are explicitly accessed as `palette.semantic.<role>`;
- existing theme storage keys and exact Wave 1 RGB values remain unchanged.

See `SKIN_CONTRACT.md` and `diagrams/SKIN_ARCHITECTURE.md`.

## System/lifecycle impact

No new remote work, navigation or modal ownership is introduced in Wave 1.
Theme selection continues to use the existing persisted style key and existing Activity recreation behavior.

## Version

- versionName: `1.4.50`
- versionCode: `93`
- branch: `feat/v1.4.50-skin-system`


## Phone QA checkpoint

Initial signed Wave 1:
- run `35772192953`;
- source `c3939849516124cd66c6a72b04f1683f5c6e161c`;
- result `1- / 2+ / 3-`.

Corrective R1:
- exact tested app source `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- signed GitHub Actions run `35782627453`;
- phone result `R1-1+ / R1-2+ / R1-3+`.

R1 confirms full Home Skin refresh after Neon/Blue/Green changes and preserves
the History clear-confirmation through rotation with explicit Cancel/no-op safety.
This is a targeted Wave 1/R1 phone PASS, not a full-app regression or final
v1.4.50 release closeout.

## Status

**TARGETED PHONE PASS — WAVE 1 R1 / BUG-031+032 CLOSED / NEXT SKIN WAVE PENDING**
