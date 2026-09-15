# YTM Importer v1.3.1

Hotfix основного Review workflow.

## Виправлено

Ручний YouTube/YTM URL тепер застосовується до актуального Track object у поточному workspace, тому cached automatic candidate більше не повинен повертатися як вибраний трек.

Manual selections захищені від автоматичного перезапису SearchCache.

## YTM Project до History

Project тепер можна зберегти прямо з Review ДО створення плейлиста у YouTube/YTM:

- `Зберегти Project`;
- `Поділитися`.

Project schema v2 зберігає selected videoId, title/channel, manual flag, candidates, score, status/error.

## Autosave

Застосунок і далі автоматично відновлює останній робочий список після перезапуску. Це safety/autosave, а не бібліотека проектів.

Для кількох списків зберігайте окремі `.ytm.json` Project files. У Import screen є `Очистити поточний список`.
