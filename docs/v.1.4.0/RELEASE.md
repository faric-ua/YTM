# YTM Importer v1.4.0 — Destination / Create screen

## Мета

Крок 4 винесено з набору `AlertDialog` у окремий `DestinationActivity`.

Було:

`Main → destination dialog → privacy dialog / existing list dialog → duplicate dialog → final confirm dialog`

Стало:

`Main → DestinationActivity → New / Existing → confirmation → write core`

## New playlist

На одному screen видно:

- Project / playlist name;
- imported count;
- tracks ready for write;
- questionable/review count;
- Google account context;
- YouTube/YTM channel context;
- privacy options;
- quota estimate;
- final create action.

Existing API/OAuth/write core лишається в `MainActivity`.

## Existing playlist

Existing playlist list завантажується тільки після вибору цього режиму.

Flow:

1. `DestinationActivity` просить список;
2. `MainActivity` авторизується та викликає `playlists.list`;
3. `DestinationActivity` показує searchable list;
4. user selects target;
5. `MainActivity` виконує `playlistItems.list` duplicate scan;
6. `DestinationActivity` показує duplicate preview;
7. user chooses `Skip duplicates` або `Add anyway`;
8. `MainActivity` запускає перевірений write core.

## Duplicate preview

Показується:

- already in selected playlist;
- repeated inside current import;
- new tracks;
- number of playlistItems.list requests;
- quota estimate for skip-vs-add-all modes.

Comparison is still exact YouTube `videoId` matching.

## Duplicate scan failure

Окремий screen пояснює помилку і дозволяє:

- cancel/back;
- continue without duplicate check.

## Back navigation

- existing list → destination start;
- duplicate confirmation → existing list;
- scan failure → existing list;
- destination start → Main.

## Open question carried forward

`Q-001 — v1.3.2 Review wording + Project save feedback`

Status: **open; revisit later; not a blocker**.

See root `OPEN_QUESTIONS.md`.

## Version

```text
versionCode = 34
versionName = "1.4.0"
```
