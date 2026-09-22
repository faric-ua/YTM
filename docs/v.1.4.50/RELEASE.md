
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


## Status

**PHONE QA FAIL — WAVE 1 1-/2+/3- / R1 FIX STATIC+FULL PREFLIGHT PASS / SIGNED RETEST PENDING**
