# Навчальний roadmap

Поточна папка — foundation, а не завершений курс.

Наступні розділи варто додавати поступово, використовуючи реальний код і release history.

## План

- `06_ACCOUNT_LIBRARY_EXPORT.md` — one / all / selective account export, exact videoId, manifest;
- `07_ANDROID_PROJECT_SETUP.md` — структура Android/Kotlin проєкту;
- `08_IMPORT_AND_PROJECT_FORMAT.md` — імпорт, YTM Project, bulk manifest import (v1.4.28);
- `09_GOOGLE_YOUTUBE_AUTH.md` — авторизація, session state, recovery;
- `10_YOUTUBE_API_AND_QUOTA.md` — API requests, quota, failure modes;
- `11_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers, BUG-005/v1.4.27 quota invariant;
- `12_DESTINATION_AND_DUPLICATES.md` — existing playlist, duplicate scan, write plan;
- `13_PENDING_QUEUE_AND_RECOVERY.md` — відкладені операції;
- `14_BACKUP_AND_EXPORT.md` — local backup, account export, manifest import, incremental delta backup (v1.4.29);
- `15_THEME_SYSTEM.md` — palette, semantic colors, Accent Card System;
- `16_GITHUB_ACTIONS_RELEASE.md` — signed APK, checksum, artifact;
- `17_BUILD_A_FEATURE_FROM_ZERO.md` — повний практичний feature exercise;
- `18_RECREATE_YTM_IMPORTER.md` — фінальний покроковий прохід від чистого repo;
- `19_LOCALIZATION_UK_KO_EN.md` — Android resources, Ukrainian/Korean/English UI;
- `20_YERIN_EXCLUSIVE_SKIN.md` — hidden visual skin + URL feature-gate design.

## Правило розвитку tutorial

Новий навчальний розділ повинен:

1. посилатися на реальний код/реліз;
2. пояснювати проблему до рішення;
3. показувати рішення;
4. містити перевірку результату;
5. описувати failure modes;
6. не переписувати історію заднім числом.
