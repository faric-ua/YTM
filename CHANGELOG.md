# Журнал змін (Changelog)

## v0.11.0
- Додано локальну Історію (History).
- Додано `HistoryEntry` / `HistoryTrack`.
- Додано `HistoryStore`.
- History синхронізується з write job і Pending Queue.
- Зберігаються account/channel, playlist ID, source і лічильники.
- Додано статуси Completed / Partial / Pending quota / Failed.
- Додано відкриття історичного playlist у YTM.
- Додано копіювання summary та problem/replacement log.
- Додано видалення одного запису та очищення історії.
- `PendingJob` тепер зберігає sourceLabel.
- `PendingTrack` зберігає historyIndex для правильного Resume.
- Додано `docs/v.0.11.0/`.

## v0.10.1
- Компактний головний екран.

## v0.10.0
- Quota Planner + Pending Queue.

## v0.9.1
- Hotfix Kotlin compilation.

## v0.9.0
- Account + Existing playlists.

## v0.8.0
- Direct text import.
