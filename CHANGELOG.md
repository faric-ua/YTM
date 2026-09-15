# Журнал змін (Changelog)

## v0.12.0
- Додано перевірку дублікатів для existing playlists.
- Додано `YouTubeApi.listPlaylistVideoIds`.
- `playlistItems.list` читається сторінками по 50.
- Додано exact videoId comparison.
- Визначаються duplicates у target playlist та повтори в import.
- Додано вибір `Пропустити дублікати` / `Додати все одно`.
- Додано `TrackStatus.DUPLICATE`.
- Дублікати можуть не витрачати write quota.
- Додано duplicate count у History.
- Дублікати додані в replacement/problem log.
- Додано fallback при помилці duplicate scan.
- Додано `docs/v.0.12.0/`.

## v0.11.0
- History / Jobs.

## v0.10.1
- Compact UI.

## v0.10.0
- Quota Planner + Pending Queue.

## v0.9.x
- Account + existing playlists.
