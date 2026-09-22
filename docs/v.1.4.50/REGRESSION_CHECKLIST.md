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
