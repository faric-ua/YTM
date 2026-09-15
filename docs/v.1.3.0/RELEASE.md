# YTM Importer v1.3.0 — Import / Review Navigation

## Мета

Основний робочий flow починає виходити з MainActivity та nested AlertDialog.

v1.3.0 додає два окремі screens:

- `ImportActivity`
- `ReviewActivity`

Функціональна baseline пошуку/запису плейлиста залишається в перевіреному core.

## ImportActivity

`1. Імпорт` тепер відкриває окремий screen.

Підтримується:

- CSV;
- TXT;
- YTM Project;
- pasted `Artist - Track`;
- optional playlist name.

File picker зберігає workaround:

`ACTION_OPEN_DOCUMENT + CATEGORY_OPENABLE + type=*/*`

## Persistent current workspace

Додано:

`storage/CurrentPlaylistStore.kt`

Локально зберігаються:

- playlist name;
- source label;
- tracks;
- candidates;
- selected video IDs;
- statuses;
- manual selections;
- errors.

OAuth access token у цей store НЕ записується.

Незавершений робочий список може відновитися після перезапуску застосунку.

## ReviewActivity

Крок 3:

`Знайти / перевірити`

Fresh list:

`Search → ReviewActivity`

Already searched list:

`ReviewActivity` без повторного API search.

Partial YTM Project з unresolved NEW tracks запускає Search,
а повністю resolved project може одразу перейти в Review.

Filters:

- Усі;
- Перевірити;
- Готові;
- Проблеми.

Є окрема дія `Повторити пошук`, яка повертає керування в MainActivity
і знову використовує SearchCache / YouTube search.

## Track detail

На окремому screen:

- original track;
- current selection;
- status;
- error;
- до 10 search candidates;
- score;
- channel;
- selected marker.

Candidate actions:

- Use;
- Open in YTM.

Manual actions:

- Paste YouTube/YTM URL;
- Skip track.

## Manual URL metadata

Щоб не дублювати OAuth/API core logic:

`ReviewActivity → videoId + historyIndex → MainActivity`

MainActivity виконує існуючий:

`applyManualUrl() → YouTube videos.list metadata`

Після metadata lookup ReviewActivity знову відкривається на тому самому треку.

Таким чином v0.15.2 metadata fix збережено.

## Main screen

Тап по track row відкриває ReviewActivity на конкретному треку,
а не старий candidate AlertDialog.

Legacy import/review dialog code поки не видаляється до phone regression.

## Версія

```text
versionCode = 31
versionName = "1.3.0"
```
