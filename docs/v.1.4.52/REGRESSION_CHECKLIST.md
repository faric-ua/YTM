# v1.4.52 — Regression Checklist

## UX-027 duplicate-choice row
- [x] exactly three actions are visible: `Всі (N)`, `Унікальні (U)`, `Скасувати`
- [x] all three actions stay in one horizontal row
- [x] `Всі` preserves all source rows
- [x] `Унікальні` keeps first exact-videoId occurrence in source order
- [x] `Скасувати` closes only duplicate choice and returns to the same preview
- [ ] rotation preserves duplicate choice without auto-commit

## UX-028 Home → History detail
- [x] URL snapshot result carries exact History entry id
- [x] Home exposes an obvious detail affordance for that status
- [x] tap opens the exact History detail directly
- [x] missing/deleted target falls back safely to History list
- [x] unrelated later status clears stale History drill-down
- [x] recreation does not launch History automatically

## Existing-system regression
- [ ] version/build identity
- [ ] preview Cancel/Back remains no-op
- [ ] no URL auto-resolve on recreation
- [ ] no local auto-commit on recreation
- [ ] no accidental YTM write
- [ ] error path
