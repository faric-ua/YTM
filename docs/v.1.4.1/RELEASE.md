# YTM Importer v1.4.1 — Legacy Destination Cleanup

## Мета

v1.4.0 переніс користувацький Step 4 у `DestinationActivity`.

v1.4.1 робить наступний технічний крок: прибирає старий destination/create
`AlertDialog` flow з `MainActivity`, не змінюючи фактичний YouTube write core.

## Видалено з MainActivity

- `chooseDestination`
- `chooseExistingPlaylist`
- `showExistingPlaylistDialog`
- `existingPlaylistLabel`
- `checkDuplicatesBeforeAppend`
- `showDuplicateChoiceDialog`
- `showDuplicateCheckFailureDialog`
- `confirmAppendToExisting`
- `choosePrivacyAndCreate`
- `confirmCreateWithQuota`

Ці функції були legacy-шляхом до появи `DestinationActivity`.

## Залишено

Новий screen bridge:

- `openDestinationStart`
- `handleDestinationResult`
- `loadExistingPlaylistsForDestination`
- `openDestinationExistingList`
- `checkDuplicatesForDestination`
- `openDestinationExistingConfirm`
- `openDestinationScanFailed`
- `finishExistingDestination`

Core write logic:

- `actuallyAppendToExisting`
- `actuallyCreatePlaylist`
- `executeWriteJob`
- Pending Queue
- History sync
- OAuth/account validation
- quota accounting

## Результат

`MainActivity.kt`:

- було: **5953 рядків**
- стало: **5434 рядків**
- прибрано: **519 рядків legacy destination code**

## Audit

Додано:

`scripts/mainactivity-audit.sh`

Він перевіряє, що старий destination-dialog flow уже відсутній,
а новий `DestinationActivity` bridge та write core залишилися.

## Відкрите питання Q-001

Пункт щодо wording manual selection / Project save feedback залишається
**OPEN — revisit later** у `OPEN_QUESTIONS.md`.

v1.4.1 його не закриває і не змінює.

## Важливо

Phone regression v1.4.x все ще потрібен перед тим, як вважати Step 4 стабільним.
Цей реліз не заявляє, що телефонний regression уже пройдено.

## Версія

```text
versionCode = 35
versionName = "1.4.1"
```
