# v1.0.0 — Стани Pending Job

```mermaid
stateDiagram-v2
    [*] --> Prepared: створено локальний job

    Prepared --> PlaylistCreated: playlists.insert OK
    Prepared --> Paused: quota error до створення playlist

    PlaylistCreated --> Writing
    Writing --> Writing: track added
    Writing --> Paused: quotaExceeded
    Writing --> Completed: remaining = 0

    Paused --> Resuming: користувач натиснув Продовжити
    Resuming --> Paused: quotaExceeded знову
    Resuming --> Writing: account/channel правильний
    Resuming --> WrongAccount: account/channel інший
    WrongAccount --> Resuming: змінити акаунт

    Completed --> [*]
```
