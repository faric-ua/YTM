# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v1.0.0-rc2 — History navigation + Playlist Project**

## Реалізовано до RC2
- [x] CSV / TXT / direct text import;
- [x] Google OAuth + account/channel;
- [x] SearchCache + MatchScorer;
- [x] candidate review / manual candidate;
- [x] manual URL → real title/channel;
- [x] new / existing playlist;
- [x] duplicate detection;
- [x] privacy;
- [x] Quota Planner;
- [x] Pending Queue + Resume;
- [x] History;
- [x] Export / Backup / Restore;
- [x] Diagnostics / Share / Cache tools;
- [x] friendly errors;
- [x] signed update;
- [x] RC preflight.

## v1.0.0-rc2
- [x] History detail → Back to History list;
- [x] History actions → Back to detail;
- [x] export one History entry as reusable YTM Project;
- [x] Android Share for YTM Project;
- [x] import `.ytm.json` through `1. Файл`;
- [x] restore exact YouTube videoId choices;
- [x] restore manual replacement title/channel;
- [x] warn that Existing Playlist export contains only this import batch;
- [ ] GitHub Actions RC2 build;
- [ ] signed upgrade RC1 → RC2;
- [ ] phone test: export project → re-import → create test playlist.

## До v1.0.0 stable
- [ ] finish regression checklist;
- [ ] fix blocker/data-loss bugs;
- [ ] final signed upgrade test;
- [ ] versionName `1.0.0`;
- [ ] stable APK.

## Після v1.0.0 — UI/UX
- [ ] Material 3;
- [ ] responsive layout;
- [ ] separate screens instead of many dialogs;
- [ ] navigation / toolbar;
- [ ] typography and spacing;
- [ ] small-screen fitting;
- [ ] accessibility.

Косметичний UI до v1.0 не є пріоритетом.
Виправляємо його зараз тільки якщо він блокує функціональну дію.
