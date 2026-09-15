# YTM Importer v1.0.0 — Stable

## Статус

Перший стабільний реліз YTM Importer.

Ця версія базується на RC4 і не додає нової функціональної логіки.
Мета — зафіксувати перевірений функціональний стан як `v1.0.0 stable`.

## Основні можливості

- CSV / TXT / direct text import;
- Google OAuth;
- account + YouTube/YTM channel;
- SearchCache + MatchScorer;
- candidate review;
- manual candidate;
- manual YouTube/YTM URL з реальною назвою та channel;
- створення нового playlist;
- додавання до existing playlist;
- duplicate detection;
- privacy selector;
- Quota Planner;
- Pending Queue + Resume;
- History;
- History Back navigation;
- YTM Playlist Project export/share/re-import;
- Export / Backup / Restore;
- backup SHA-256 integrity;
- restore safety snapshot + rollback;
- Diagnostics / Share / SearchCache tools;
- centralized user-friendly errors;
- signed GitHub Actions release;
- APK signature / zipalign / package/version verification;
- APK SHA-256 checksum.

## Build / release safety

Workflow використовує:

- `actions/setup-java@v5`;
- `apksigner verify`;
- `zipalign -c`;
- `aapt dump badging`;
- `sha256sum`.

Artifact:

```text
YTM-Importer-v1.0.0-release.apk
YTM-Importer-v1.0.0-release.apk.sha256
```

## Версія

```text
versionCode = 26
versionName = "1.0.0"
```

## Після v1.0.0

Наступний великий етап — UI/UX redesign:

- Material 3;
- responsive layout;
- окремі screens;
- нормальна navigation;
- кращі spacing/typography;
- small-screen polish;
- accessibility.

Функціональні зміни після stable повинні йти окремими versioned релізами.
