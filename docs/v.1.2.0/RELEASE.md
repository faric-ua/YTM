# YTM Importer v1.2.0 — Navigation Foundation / Dedicated History

## Мета

Продовжуємо перетворювати застосунок із набору AlertDialog-вікон
на нормальний Android application flow.

У v1.2.0 перший великий модуль винесено в окремий screen:
**History**.

## Dedicated History screen

Було:

`Main → History AlertDialog → Entry AlertDialog → Actions AlertDialog`

Стало:

`Main → HistoryActivity → searchable History list → Entry detail`

Back navigation:

- detail → History list;
- History list → main screen.

## History list

- fullscreen screen;
- search by playlist/source/channel;
- status color;
- added/pending/failed/duplicate counters;
- clear-all with confirmation.

## History detail

Показує:

- status/date/source;
- destination/privacy;
- added/duplicate/skipped/pending/failed/missing;
- replacement/problem preview;
- masked Google email;
- YouTube/YTM channel;
- last error.

## Actions

- open playlist in YTM;
- save/share YTM Project;
- copy URL;
- copy summary;
- view/copy problem log;
- delete local History entry.

## Architecture

ADDED:

`app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt`

`HistoryActivity` має `exported=false`.

Core Search/OAuth/write/duplicates/Queue/Backup logic не змінювалась.

## Версія

```text
versionCode = 28
versionName = "1.2.0"
```

## Далі

- dedicated Data / Backup screen;
- dedicated Import / Review screen;
- менше AlertDialog;
- Material 3 / responsive polish.
