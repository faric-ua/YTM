# v1.4.47-R3 Phone QA

Install the signed v1.4.47-R3 APK **over the currently installed app**.
Do not uninstall and do not clear application data.

Static/build PASS is not phone PASS.

## 1 — install/update + Home/R2 carry-forward

Expected:
- version shows `1.4.47-R3`;
- existing workspace/account/local data survive the in-place update;
- current-playlist heading remains inside its card;
- quick actions remain compact;
- bottom navigation remains rounded;
- portrait and landscape Home remain usable/scrollable.

## 2 — Menu Theme picker ownership + rotation

Path:
`Home → Меню → Тема`

Expected:
- Menu remains the parent behind the picker;
- rotate portrait → landscape → portrait while the picker is open;
- the picker remains/reappears open;
- no theme changes merely because of rotation;
- choose a different theme: Menu recreates/repaints and stays current;
- Back returns to Home normally.

## 3 — BUG-022 selector Help window

Use a ListSelector path such as:
`Імпорт → Імпорт із YouTube/YTM → ? → Що буде імпортовано?`

Expected:
- Help is open;
- rotate portrait ↔ landscape;
- the same Help returns over the same selector;
- selector choice/scroll parent is not replaced;
- nothing is selected or launched automatically;
- Close returns to the same selector.

Repeat a quick representative smoke for:
- Recent-file Help `Останні файли`;
- Storage chooser Help `Що це за список?`.

## 4 — BUG-023 Current YTM Project modal

Path:
`Перевірка треків → Проект → Поточний YTM Project`

Expected:
- modal shows Save / Share / Close;
- rotate portrait ↔ landscape while open;
- modal remains/reappears over the same Review parent;
- Save and Share do **not** run automatically;
- Close returns to the same Review screen/track context;
- reopen works and no duplicate modal appears.

## 5 — BUG-021 History completed YTM write

Open a History record that actually wrote tracks to YouTube/YTM.

Expected:
- completed status remains correct;
- primary result is `Додано в YTM: X/Y`;
- error/pending/duplicate/etc. lines remain separate when applicable.

## 6 — BUG-021 local import/restore semantics

Open available completed History records where no YTM write occurred.

Expected import-like record:
`Імпортовано: N треків`

Expected restore-like record:
`Відновлено: N треків`

Must not show the contradictory:
`✓ Завершено` + `Додано 0/N`

If no suitable historical record exists on the device, report:
`BLOCKED — NO REPRESENTATIVE HISTORY RECORD`

## 7 — BUG-004 normal online auth smoke

With the connected Google/YTM account:
- open existing-playlist destination;
- perform a harmless playlist-list/read path;
- run Search only when it will not waste quota;
- verify no unexpected forced re-login occurs during normal valid authorization.

## 8 — BUG-004 natural stale/401 acceptance

This case is accepted only if an aged/invalid token occurs naturally.

On the first real HTTP 401, expected:
- app attempts silent token recovery;
- if Google can refresh silently, the original request succeeds after one automatic retry;
- Step 2 remains connected/green after successful recovery;
- user is not forced through manual authorization;
- the requested operation continues instead of becoming a permanent auth failure.

If silent recovery cannot proceed because Google requires interactive resolution:
- app falls back to disconnected/red/manual auth;
- no infinite retry loop;
- workspace remains intact;
- unfinished write work remains retryable/in Queue where applicable.

Do **not** deliberately revoke credentials or burn Search quota merely to force this case.
If it does not naturally reproduce, report:
`DEFERRED — NATURAL 401 NOT REPRODUCED`

## 9 — short regression smoke

Verify:
- Playlist Hub still opens;
- Search/Create delegated Back behavior still returns correctly;
- replacement/problem dialog still survives rotation;
- Import clear-current-list confirmation still survives rotation;
- Account modal rotation remains working;
- destructive actions remain danger/red;
- OAuth token is never displayed.

## Report format

Suggested compact result:

`1+ 2+ 3+ 4+ 5+ 6+ 7+ 8=DEFERRED 9+`

Use `-` for FAIL and `BLOCKED`/`DEFERRED` where the prerequisite is unavailable.

Status: **PHONE QA NEEDED**.
