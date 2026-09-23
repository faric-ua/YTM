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
- [x] representative modal/tile readability smoke — `UI-1+ / UI-2+ / UI-3+`; Data rotation follow-up `W3-1+..W3-4+`

## General release checks

- [x] Back/navigation ownership — representative UI `UI-3+`; modal Cancel returns to same parent in R1/W2/W3
- [x] rotation/recreation — R1, W2 and W3 signed phone tests
- [x] modal lifecycle — History + Skin preview + Data ordinary/prepared modal states
- [x] cancel/no-op behavior — R1-3+, W2-1+/W2-3+, W3-1+..W3-4+
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
- [x] signed Wave 2 APK — run `35787308504` / source `fdb2892c7b4fa0c858c55d5187a04ce296bde913`
- [x] phone W2-1 preview + Cancel no-op — PASS
- [x] phone W2-2 Apply + Menu/Home refresh — PASS
- [x] phone W2-3 preview rotation continuity — PASS

## Wave 3 — Restorable modal core

- [x] reusable `RestorableModalController` owns semantic modal save/restore
- [x] DataActivity uses one `DataModal` type instead of per-dialog boolean flags
- [x] all 11 Data modal states route through the shared controller
- [x] prepared Restore/History confirmations retain only validated cache + semantic modal state
- [x] result modals persist primitive summary args
- [x] recreation restores UI only and cannot auto-run positive/destructive actions
- [x] old Activity dialog listener is detached during destruction
- [x] dedicated Wave 3 modal lifecycle audit passes
- [x] full release preflight passes
- [x] signed Wave 3 APK — run `35796094108` / source `7e6fcb482387be92a7de54db0f4df5081d640495`
- [x] W3-1 Data full-backup confirmation rotation + Cancel no-op — PASS
- [x] W3-2 Data restore/history-picker confirmations rotation + Cancel no-op — PASS
- [x] W3-3 Data share confirmation rotation + Cancel no-op — PASS
- [x] W3-4 prepared History JSON confirmation rotation — PASS with real fixture

## Wave 3 R1 — Result modal lifecycle

- [x] controller tracks resumed/paused Activity lifecycle
- [x] controller marks state saved before Activity state snapshot
- [x] system/recreation dismiss cannot clear saved semantic modal state
- [x] user dismiss while resumed still clears semantic modal state
- [x] DataActivity forwards onResume/onPause to the controller
- [x] History/Restore/Rollback result modal semantic states remain unchanged
- [x] dedicated Wave 3 R1 audit passes
- [x] full release preflight passes
- [x] signed Wave 3 R1 APK — run `35799736192` / source `c25b9f2a6843caf8790e45467df5bf118d08656d`
- [ ] W3R1-1 `History відновлено` survives portrait → landscape → portrait — **FAIL on R1**
- [ ] W3R1-2 after rotation `Готово` closes result and it does not resurrect — **NOT ACCEPTED; R1 result state is lost**
- [ ] W3R1-3 after rotation `Відкотити` opens exactly one rollback confirmation — **FAIL on R1: rollback confirm disappears on rotation**

## Wave 3 R2 — Deterministic modal dismissal

- [x] controller `OnDismissListener` does not clear semantic modal state
- [x] controller `OnCancelListener` owns Back/touch-outside semantic cancellation
- [x] all Data modal buttons explicitly close/transition semantic state
- [x] prepared Restore/History cancel cleanup is routed by semantic modal id
- [x] no Activity lifecycle timing flag is used to decide semantic close
- [x] danger-confirm cancel button has explicit callback without breaking existing callers
- [x] result/rollback states remain semantic `DataModal` values
- [x] dedicated Wave 3 R2 audit passes
- [x] full release preflight passes
- [ ] signed Wave 3 R2 APK
- [ ] W3R2-1 `History відновлено` survives repeated rotation
- [ ] W3R2-2 `Готово` closes result and it stays closed after rotation
- [ ] W3R2-3 `Відкотити останній Restore?` survives repeated rotation
- [ ] W3R2-4 one explicit rollback tap opens one confirmation and does not execute rollback
