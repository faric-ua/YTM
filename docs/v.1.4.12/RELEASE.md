# YTM Importer v1.4.12 — Cleanup Wave 2

## Мета

Цей реліз не намагається ще раз виправляти відкладений visual-motion bug
custom dialogs.

Після v1.4.11 користувач вирішив не витрачати на нього більше часу зараз.
Проблема задокументована як **Q-002 — DEFERRED** і не блокує roadmap.

v1.4.12 повертається до архітектурного cleanup.

## Головний результат

`MainActivity.kt`:

```text
до:    5668 lines
після: 3689 lines
мінус: 1979 lines (34.9%)
```

Було видалено старі UI-flow, які вже мають окремі Activity.

## Видалено з MainActivity

### Legacy Import

Видалені старі:

- `showImportMenu`
- `chooseFile`
- `loadFile`
- `showPasteTrackListDialog`
- `applyImportedPlaylist`
- `applyImportedProject`
- `queryFileName`

Import UI тепер має одного власника:

`ImportActivity`

### Legacy Pending detail

Видалені:

- `showPendingJobDetails`
- `confirmDeletePendingJob`

Список і деталі черги належать:

`PendingActivity`

MainActivity залишає лише resume/write bridge.

### Legacy Service popup flow

Видалені дублікати:

- About
- Regression checklist
- Diagnostics popup
- Diagnostics save/share
- SearchCache popup/clear
- форматування старого diagnostics popup

Це вже належить:

`ServiceActivity`

### Legacy Data / Backup / Export

Видалений старий MainActivity flow для:

- History TXT/JSON export
- Pending JSON export
- Full Backup
- Share Backup
- Restore
- Rollback
- document export helper

Це належить:

`DataActivity`

Також з MainActivity прибрано:

- `saveExportRequestCode`
- `restoreBackupRequestCode`
- `LocalBackupManager`
- pending-export state

### Legacy History detail/actions

Видалені старі MainActivity:

- detail dialog
- actions dialog
- Project save/share from old History dialog
- delete/clear confirmation flow
- old history formatting helpers

Це належить:

`HistoryActivity`

MainActivity зберігає тільки запис/синхронізацію History після write job.

### Legacy Review / Candidate UI

Видалені старі:

- `showTrackDialog`
- `showCandidateDialog`
- candidate-open helper
- old Paste URL dialog
- old local video-id parser

Це належить:

`ReviewActivity`

MainActivity залишає canonical manual-URL application після result contract
від ReviewActivity.

## Що свідомо залишилось у MainActivity

MainActivity усе ще відповідає за core orchestration, який ще не винесено:

- Home/navigation;
- Google OAuth bridge;
- search orchestration;
- current workspace state;
- DestinationActivity bridge;
- create/append write execution;
- Pending Queue resume;
- quota pause handling;
- History sync after write;
- manual URL canonical application;
- playlist result/open/copy;
- replacement-log entry point.

Це наступні кандидати на Cleanup Wave 3.

## File picker

Важливий permissive picker **не видалено**.

Він залишається в `ImportActivity`:

```kotlin
private fun chooseFile() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }
    startActivityForResult(intent, fileRequestCode)
}
```

## Q-002 — deferred

На реальному телефоні через v1.4.11 custom dialogs все ще можуть мати
короткий visual movement при відкритті.

Статус:

**DEFERRED BY USER — does not block roadmap.**

Не повертатися до цієї теми, доки користувач прямо не попросить.

## Version

```text
versionCode = 46
versionName = "1.4.12"
```
