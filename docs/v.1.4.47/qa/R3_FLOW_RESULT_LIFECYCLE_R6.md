# v1.4.47-R3 — Flow Result / Lifecycle R6

Base signed R5:
- commit `bd4c3cef97ce05cc2b9579ba3da852de9d867ccf`
- Actions run `35516838529`.

Phone QA found:

1. Home `3. Знайти / перевірити` now preserves Review→Destination Back,
   but final Destination results were returned through
   `ReviewActivity.EXTRA_DESTINATION_RESULT` and Main ignored that flag.
   As a result, create/append actions could close the child flow without
   starting a write. R6 consumes that flag first and calls
   `handleDestinationResult(data)`.

2. `Повторити пошук?` still disappeared on rotation. R6 lets only explicit
   user actions/cancel clear the open-state flag. Generic dialog dismiss clears
   only the Dialog reference, so configuration teardown cannot erase restore state.

3. Write rows are made visually explicit:
   - active row auto-scrolls into view and gets `●`;
   - successful row title becomes green with `✓`;
   - duplicate status remains blue.

4. Home account/status subtitle is capped to one ellipsized line so long
   yellow/orange auth/problem messages cannot stretch the dashboard.

Recorded but not guessed in this wave:
- exact visual mapping of missing Home contour accents on user “blocks 3 and 6”;
- one cancellation path that collapses to `Поточний плейлист`; the exact visible
  cancel control is still ambiguous, so R6 does not blindly change it.

Phone acceptance:
1. Home → `3. Знайти / перевірити` → `Далі → Створити / додати` →
   `Створити новий плейлист`: write must start.
2. Same route → existing playlist → `Пропустити дублікати й додати`:
   write must start.
3. `Перевірка треків` → `↻ Пошук` → `Повторити пошук?` → rotate both ways:
   dialog must remain open.
4. Real write with at least one new track: active row must scroll into view;
   successful row must become green with `✓`.
5. Natural Google/YTM problem state: Home status remains one compact line.
