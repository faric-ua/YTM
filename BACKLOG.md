# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v1.0.0-rc1 — Release Candidate 1**

## Реалізовано до RC1
- [x] CSV / TXT / direct text import;
- [x] Google OAuth + account/channel;
- [x] SearchCache + MatchScorer;
- [x] candidate review;
- [x] manual candidate;
- [x] manual URL → real title/channel;
- [x] create new playlist;
- [x] append existing playlist;
- [x] duplicate detection;
- [x] privacy;
- [x] Quota Planner;
- [x] Pending Queue + Resume;
- [x] History;
- [x] Export / Backup / Restore;
- [x] Diagnostics / Share / Cache tools;
- [x] centralized ErrorMessages;
- [x] signed GitHub Actions builds;
- [x] Termux build/download/install workflow.

## v1.0.0-rc1
- [x] core behavior freeze;
- [x] final RC naming;
- [x] RC release notes;
- [x] RC regression checklist;
- [x] automatic RC preflight script;
- [x] workflow runs preflight before build;
- [x] guard against tracked JKS / release-signing.properties;
- [x] preserve BuildConfig + Android SDK workflow hotfixes;
- [ ] GitHub Actions RC1 build;
- [ ] signed upgrade v0.15.2 → RC1;
- [ ] regression checklist on phone;
- [ ] fix blocker bugs only.

## v1.0.0 stable
- [ ] no blocker bugs;
- [ ] final signed upgrade test;
- [ ] final release notes;
- [ ] versionName `1.0.0`;
- [ ] versionCode > RC1;
- [ ] stable APK artifact.

## Після v1.0.0
### UI/UX redesign
- [ ] Material 3;
- [ ] окремі screens замість великої кількості dialogs;
- [ ] responsive layout для різних екранів;
- [ ] toolbar / navigation;
- [ ] покращення щільності інформації;
- [ ] accessibility / larger text;
- [ ] polishing animations/icons.

### Future functionality
- [ ] додаткові джерела імпорту;
- [ ] optional advanced playlist management;
- [ ] інші функції тільки після стабільного v1.0.
