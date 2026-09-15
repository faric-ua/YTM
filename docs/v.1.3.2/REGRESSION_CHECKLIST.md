# YTM Importer v1.3.2 — Regression checklist

## Upgrade
- [ ] Install over v1.3.1 without uninstall.
- [ ] Current workspace restored.
- [ ] History/Data/Queue preserved.

## Manual selection wording
- [ ] Main row title always shows original imported track.
- [ ] Manual replacement subtitle starts with `Ручний вибір:`.
- [ ] Manual MATCHED status says `✓ вибрано`.
- [ ] Automatic result still says `Знайдено:`.
- [ ] Review list uses the same wording.
- [ ] Review detail uses `Ручний вибір:` for manual selection.

## Example regression
Original:
`The Weeknd — Blinding Lights`

Manual selection:
`So Much In Love (Sub Focus Remix) • D.O.D - Topic`

Expected UI:
`The Weeknd — Blinding Lights`
`Ручний вибір: So Much In Love (Sub Focus Remix) • D.O.D - Topic`

## Project save toast
- [ ] Save Project.
- [ ] Toast contains project name.
- [ ] Toast contains final document filename.
- [ ] Renaming in Android picker shows renamed filename in toast.

## Existing v1.3.1 fixes
- [ ] Manual URL opens correct track.
- [ ] Manual selection survives reopen.
- [ ] Repeat search does not overwrite sticky manual selection.
- [ ] Working Project can be saved before YouTube export.

## Build
- [ ] Release preflight PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
