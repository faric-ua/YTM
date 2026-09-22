# v1.4.50 — Regression Checklist

## Wave 1 — Skin contract

- [x] version/build identity `1.4.50 / 93`
- [x] reusable `Skin` contract exists
- [x] Neon/Blue/Green are represented through the common Skin registry
- [x] exact pre-Wave-1 RGB values are preserved
- [x] semantic state roles are structurally separated under `SemanticPalette`
- [x] persisted theme storage keys are unchanged
- [x] dedicated static Skin contract audit passes
- [x] full release preflight passes
- [x] signed v1.4.50 APK — R1 run `35782627453` / source `81d5ebd988d08d3ddb80d78b73fd94e20280c980`
- [x] real-phone Neon smoke — R1-1+
- [x] real-phone Blue smoke — R1-1+
- [x] real-phone Green smoke — R1-1+
- [x] representative success/warning/danger/duplicate state smoke — initial Wave 1 `2+`; R1 did not change semantic roles
- [x] theme-selection recreation/rotation smoke — R1-1+ Skin recreation + R1-2+ confirmation rotation
- [ ] representative modal/tile readability smoke

## General release checks

- [ ] Back/navigation ownership
- [ ] rotation/recreation
- [ ] modal lifecycle
- [ ] cancel/no-op behavior
- [ ] error path
- [ ] no accidental duplicate operation

## Wave 2 — Skin preview / selection

- [x] selector opens candidate preview instead of committing immediately
- [x] preview reads candidate Skin palette without changing prefs
- [x] Apply is the only persisted Skin-change path
- [x] Cancel / Back / dismiss are no-op for persisted Skin
- [x] candidate preview style key is saved/restored across Activity recreation
- [x] candidate preview exposes surface/accent + success/warning/danger/duplicate
- [x] Wave 1 exact RGB values remain unchanged
- [x] dedicated Wave 2 static audit passes
- [x] full release preflight passes
- [ ] signed Wave 2 APK
- [ ] phone W2-1 preview + Cancel no-op
- [ ] phone W2-2 Apply + Menu/Home refresh
- [ ] phone W2-3 preview rotation continuity
