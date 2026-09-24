# v1.4.52 — Regression Checklist

## UX-027 duplicate-choice row
- [ ] exactly three actions are visible: `Всі (N)`, `Унікальні (U)`, `Скасувати`
- [ ] all three actions stay in one horizontal row
- [ ] `Всі` preserves all source rows
- [ ] `Унікальні` keeps first exact-videoId occurrence in source order
- [ ] `Скасувати` closes only duplicate choice and returns to the same preview
- [ ] rotation preserves duplicate choice without auto-commit

## UX-028 Home → History detail
- [ ] URL snapshot result carries exact History entry id
- [ ] Home exposes an obvious detail affordance for that status
- [ ] tap opens the exact History detail directly
- [ ] missing/deleted target falls back safely to History list
- [ ] unrelated later status clears stale History drill-down
- [ ] recreation does not launch History automatically

## Existing-system regression
- [ ] version/build identity
- [ ] preview Cancel/Back remains no-op
- [ ] no URL auto-resolve on recreation
- [ ] no local auto-commit on recreation
- [ ] no accidental YTM write
- [ ] error path
