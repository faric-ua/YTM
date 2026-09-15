# YTM Importer v1.2.2 — Dedicated Pending Queue Screen

## Мета

Третій utility-flow винесено з AlertDialog у окремий Android screen.

Було:

`Main → Черга → jobs AlertDialog → job details AlertDialog`

Стало:

`Main → PendingActivity → searchable queue list → job detail`

## PendingActivity

Queue screen показує:

- playlist name;
- updated time;
- remaining track count;
- added/total progress;
- failed count.

Є пошук за:

- playlist name;
- import source;
- YouTube/YTM channel.

## Job detail

Показує:

- created/updated;
- source;
- new/existing destination;
- privacy;
- added/remaining/failed/progress;
- masked Google email;
- masked Channel ID / Playlist ID;
- last quota/write error;
- preview до 15 remaining tracks.

## Continue

Core resume logic НЕ переносилась у PendingActivity.

`Продовжити` повертає `jobId` у MainActivity:

`PendingActivity → RESULT_OK → MainActivity.resumePendingJob(job)`

Тому Google OAuth/account validation та playlist write залишаються
в одному перевіреному core code path.

## Delete

`Видалити з черги` видаляє тільки локальний PendingJob.

Вже додані YouTube/YTM tracks не видаляються.

History не очищається.

## Empty state

Черга тепер відкривається як screen навіть якщо порожня,
з поясненням коли Pending Queue з'являється.

## Architecture

ADDED:

`app/src/main/java/com/saney/ytmimporter/PendingActivity.kt`

UPDATED:

`PendingJobStore.get(jobId)`

Manifest:

`PendingActivity exported=false`

## Версія

```text
versionCode = 30
versionName = "1.2.2"
```

## Наступний великий етап

Після phone regression:

- Import / Review screen;
- track review flow;
- поступове видалення legacy dialogs;
- Material 3 / responsive layout.
