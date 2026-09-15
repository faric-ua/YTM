# YTM Importer v1.3.1 — Manual Selection + Working Project Hotfix

## Причини релізу

v1.3.0 виявив дві важливі UX/logic проблеми під час реального phone test:

1. після ручного YouTube/YTM URL асинхронний metadata lookup міг завершитися вже після `MainActivity.onResume()`, коли current workspace було перечитано з диска; в результаті metadata застосовувались до старого `Track` object, а в актуальному workspace залишався автоматичний cached candidate;
2. YTM Project можна було зберегти тільки після появи History entry, тобто фактично вже після write/export у YouTube/YTM.

## Manual URL fix

Тепер async result не мутує старий `Track` reference.

Перед застосуванням metadata MainActivity повторно знаходить canonical track у поточному playlist за `historyIndex`:

`ReviewActivity -> videoId/historyIndex -> MainActivity -> resolveCurrentTrack() -> applyCandidate()`

Ручний вибір стає sticky:

- SearchCache не може автоматично перезаписати manual selection;
- звичайний Search пропускає track з `manuallySelected=true` і exact videoId;
- `applySearchCandidates()` має додатковий guard.

## Working YTM Project

Додано `PlaylistProjectCodec.exportWorkingPlaylist()`.

Project можна зберегти прямо з Review ДО History і ДО створення плейлиста в YouTube/YTM.

Зберігаються:

- playlist name;
- original artist/title;
- selected videoId/title/channel;
- manual-selection flag;
- track status;
- error;
- search candidates і scores;
- source label.

OAuth access token не входить у Project.

## Project schema v2

`schemaVersion = 2` додає optional `candidates` та збереження робочого `sourceStatus`.

Schema v1 лишається сумісною для import.

При import remote-only statuses нормалізуються:

- ADDED/DUPLICATE/PENDING -> MATCHED, якщо exact videoId є;
- SEARCHING -> MATCHED або NEW;
- SKIPPED/MISSING/FAILED/REVIEW/MATCHED з working Project зберігаються.

## Review UX

На Review list є видимі дії:

- `Зберегти Project`;
- `Поділитися`.

Також `Проект` доступний у top bar track detail.

Main `Ще` отримав `Поточний проект — review / save / share`.

## Last workspace / autosave

Автовідновлення останнього робочого списку лишається навмисно як crash/restart safety.

Але тепер це не є заміною Project library:

- останній workspace зберігається автоматично;
- будь-яку потрібну версію можна вручну зберегти як `.ytm.json`;
- у Import screen можна `Очистити поточний список`.

## Full Backup

`current_playlist_v1` тепер входить у Full Backup / Restore.

## Version

```text
versionCode = 32
versionName = "1.3.1"
```
