# v1.4.47-R3 — Runtime/Lifecycle R5

Base: `b694de18fa2cc427ea7b773d04b2e56136c17ab1`.

Phone QA from the R4 signed build found four functional roots:

1. Home step `3. Знайти / перевірити` opened Review without Playlist-parent
   metadata. Review therefore finished itself before opening Destination, so
   Destination Back exposed Home. R5 makes Review own Destination locally for
   every entry path.

2. `Повторити пошук?` stored an open flag, but its dismiss listener could clear
   that flag during configuration teardown. R5 keeps the flag when the Activity
   is changing configuration.

3. Destination already stores downloaded playlist data in its Intent, but the
   start-screen `Вибрати існуючий плейлист` action always fetched again. R5
   reuses the stored list for internal re-entry/Back and only hits the API when
   no cached list exists.

4. MainActivity owns the final YouTube write executor. Rotation recreated Main,
   `onDestroy()` shut that executor down, and the restored relay could become a
   stuck dark screen. R5 lets Main handle orientation/screen-size changes in
   place, preserving the active executor and UI. The write relay is upgraded
   from an empty spinner screen to a playlist + track list:
   - current row: `● Додаю…`
   - completed row: `✓ Додано`
   - skipped duplicate: `≋ Дублікат • пропущено`
   - failure: `× Помилка`

This wave intentionally does not guess the user's visual note about “Home blocks
3 and 6” accent contour lines. Functional Home rotation/write stability is
fixed first; the ambiguous style-only mapping remains a recorded visual finding.

## Phone acceptance

1. Home `3. Знайти / перевірити` → `Перевірка треків` →
   `Далі → Створити / додати` → Back.
   Expected: directly back to `Перевірка треків`, never Home.

2. `Перевірка треків` → `↻ Пошук` → `Повторити пошук?` → rotate both ways.
   Expected: confirmation remains open.

3. `Створити / додати` → `Вибрати існуючий плейлист` → Back to start →
   `Вибрати існуючий плейлист` again.
   Expected: cached list opens immediately; no server-sync dark wait.

4. Real test write: existing test playlist → confirmation →
   `Пропустити дублікати й додати` or `Додати до плейлиста`.
   Expected progress screen contains target playlist name and all visible track
   rows. Current track becomes `● Додаю…`; successful rows become `✓ Додано`.

5. Rotate during the middle of the write.
   Expected: same progress list remains visible and the write continues.
   No `Відновлено робочий список…` stuck relay and no duplicate restart.

6. When the result modal appears, rotate once more.
   Expected: operation remains completed and no second write starts.
