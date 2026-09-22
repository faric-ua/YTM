# v1.4.50 — Skin Contract

## Purpose

v1.4.50 introduces a reusable Skin architecture without intentionally changing
the accepted visual appearance or application behavior.

Wave 1 is an architecture migration.

## Identity

Existing persisted style identities remain stable:

- `neon_dark` → Neon Dark;
- `blue_dark` → Blue Dark;
- `green_dark` → Green Dark.

The preference namespace and key remain unchanged so existing installations keep
their selected style after update.

## Common Skin contract

`AppThemeManager.Skin` is the common built-in skin unit.

Each Skin contains:

- stable `ThemeStyle` identity;
- `SkinPalette`.

`SkinPalette` contains visual presentation tokens:

- background;
- surface;
- alternate surface;
- border;
- text;
- muted text;
- dim muted text;
- accent;
- accent fill.

## Semantic state contract

Semantic state colors are grouped under `SemanticPalette`:

- success;
- warning;
- danger;
- duplicate;
- corresponding state fills where required.

Application code accesses them through `palette.semantic.<role>`.

This prevents business/state meaning from becoming an unlabelled decorative
color token.

A Skin may choose different concrete colors to preserve contrast, but it may not
change what a semantic role means.

## Wave 1 compatibility

Wave 1 preserves the exact pre-v1.4.50 RGB values for Neon Dark, Blue Dark and
Green Dark.

It does not intentionally change:

- layout geometry;
- navigation;
- modal semantics;
- Back/Cancel behavior;
- destructive confirmation rules;
- remote-operation ownership;
- theme preference storage keys.

## Wave 2 — preview / selection lifecycle

The selector now separates **preview** from **commit**:

- tapping Neon/Blue/Green opens a candidate preview;
- preview reads the candidate `SkinPalette` directly and does not write prefs;
- `Застосувати` is the only path that commits `ThemeStyle`;
- `Скасувати`, system Back and dialog dismissal leave the active Skin unchanged;
- rotation stores only the candidate style key and rebuilds the same preview over
  `MenuActivity`;
- applying a different Skin recreates Menu, and the existing Home resume guard
  rebuilds Home when the user returns;
- preview is visual-only and must not start remote work or destructive actions.

The preview card exposes representative visual and semantic tokens:
background/surface/accent plus success/warning/danger/duplicate.

## Next waves

Later v1.4.50 work may add:

- richer packaged skin data;
- controlled visual redesign of Blue/Green semantic palettes;
- optional safe visual assets.

Arbitrary executable code is not part of the Skin contract.
