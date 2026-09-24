# v1.4.52 — Regression Checklist

## UX-027 duplicate-choice row
- [x] exactly three actions are visible: `Всі (N)`, `Унікальні (U)`, `Скасувати`
- [x] all three actions stay in one horizontal row
- [x] `Всі` preserves all source rows
- [x] `Унікальні` keeps first exact-videoId occurrence in source order
- [x] `Скасувати` closes only duplicate choice and returns to the same preview
- [x] rotation preserves duplicate choice without auto-commit

## UX-028 Home → History detail
- [x] URL snapshot result carries exact History entry id
- [x] Home exposes an obvious detail affordance for that status
- [x] tap opens the exact History detail directly
- [x] missing/deleted target falls back safely to History list
- [x] unrelated later status clears stale History drill-down
- [x] recreation does not launch History automatically

## Existing-system regression
- [x] version/build identity — installed phone build shows v1.4.52
- [x] preview Cancel/Back remains no-op — Test 1 Cancel left playlist/History unchanged
- [x] no URL auto-resolve on recreation — chooser restored across rotation without restarting resolution
- [x] no local auto-commit on recreation — chooser remained pending until explicit action
- [x] no accidental YTM write — unique local handoff completed without create/add/write flow
- [ ] error path — not re-exercised in this narrow v1.4.52 phone pass; v1.4.51 evidence remains historical
