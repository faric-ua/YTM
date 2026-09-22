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
- [ ] signed v1.4.50 APK
- [ ] real-phone Neon smoke
- [ ] real-phone Blue smoke
- [ ] real-phone Green smoke
- [ ] representative success/warning/danger/duplicate state smoke
- [ ] theme-selection recreation/rotation smoke
- [ ] representative modal/tile readability smoke

## General release checks

- [ ] Back/navigation ownership
- [ ] rotation/recreation
- [ ] modal lifecycle
- [ ] cancel/no-op behavior
- [ ] error path
- [ ] no accidental duplicate operation
