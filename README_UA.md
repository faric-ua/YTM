# YTM Importer v1.3.0

Основний Import / Review flow тепер має окремі screens.

## Import

`1. Імпорт → ImportActivity`

Підтримує:
- CSV;
- TXT;
- YTM Project;
- pasted Artist - Track;
- optional playlist name.

## Review

`3. Знайти / перевірити → ReviewActivity`

Є filters:
- Усі;
- Перевірити;
- Готові;
- Проблеми.

Кожен трек має окремий detail screen з search candidates,
score/channel, Use/Open YTM, manual URL та Skip.

## Workspace

Поточний незавершений playlist зберігається локально,
тому може відновитися після перезапуску застосунку.

OAuth token у workspace НЕ зберігається.

Тепер окремі screens:
- Import;
- Review;
- History;
- Data;
- Pending Queue.

Наступний великий крок:
destination/create flow.
