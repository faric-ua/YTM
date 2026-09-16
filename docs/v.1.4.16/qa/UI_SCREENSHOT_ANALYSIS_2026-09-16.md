# v1.4.16 — UI screenshot analysis

Date: 2026-09-16

## Button/layout observations

### Home

The four-step action grid is visually stable:
- Step 1 and Step 2 are aligned vertically;
- connected `2. Google / YTM ✓` does not visibly drop relative to Step 1;
- Step 3 and Step 4 occupy the same row and height;
- utility buttons `Історія / Черга / Квота / Ще` are evenly aligned.

No recurrence of the earlier baseline-related vertical displacement is visible in the supplied v1.4.16 screenshots.

### Review screen

Top actions `Зберегти / Поділитись / Пошук` are kept on one stable row.

Filter actions `Усі / Перев. / Готові / Пробл.` also remain on one row without visible clipping.

### Search plan dialog

Functionally correct, but visually this still uses older plain text actions: `Скасувати / Почати`.

This is not treated as a v1.4.16 blocker. It is a UI-cleanup candidate because newer screens use boxed/card actions.

### Destination — create new playlist

`Створити новий плейлист` is correctly presented as a wide primary action below privacy and quota information.

### Destination — duplicate choice

Action hierarchy is clear:
- `Пропустити дублікати й додати` is primary;
- `Додати все одно` is secondary.

### Result modal

Current hierarchy is readable:
- `Відкрити в YTM` and `Копіювати` are paired boxed actions;
- `Закрити` is separated below.

No obvious clipping or overlap is visible.

## UX observations to track

### UX-OBS-001 — all-duplicates result lacks skipped count

When all selected tracks are duplicates, result can show `Додано: 0`.

This is technically correct but can look like a failed operation.

Suggested future result detail:
- Added: 0
- Skipped duplicates: 2
- Failed: 0

### UX-OBS-002 — mixed user/developer wording

Destination UI currently mixes localized text with developer terms such as `playlist`, `playlistItems.list`, `request(s)`, `videoId`.

Suggested future user-facing wording:
- `Уже є у плейлисті`
- `API-запитів перевірки: 1`
- keep `videoId` in technical details where useful.

## Conclusion

No button-position regression is visible in the supplied v1.4.16 screenshots.

The remaining issues here are polish/clarity improvements, not evidence that DestinationCoordinator broke layout behavior.
