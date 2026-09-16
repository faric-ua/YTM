# YTM Importer v1.4.16

Cleanup Wave 5:
`destination/DestinationCoordinator.kt` тепер відповідає за вибір треків для запису,
список/вибір цільового плейлиста, перевірку дублікатів за точним videoId,
облік квоти duplicate scan і підготовку duplicate write plan.

`MainActivity` лишається мостом авторизації/UI/executor, а фактичний запис плейлиста
виконує `write/PlaylistWriteCoordinator.kt`.

BUG-003/Q-003: після оновлення Step 2 лишається червоною. Баг зафіксований і
відкладений до окремої хвилі виправлень.

QA:
- поточні плани: `qa/`
- snapshot цього релізу: `docs/v.1.4.16/qa/`

Статус: NOT TESTED YET.


## Розробка з телефона

Основні Termux / Git / SSH / build-команди:
[`TERMUX_COMMANDS.md`](TERMUX_COMMANDS.md)

