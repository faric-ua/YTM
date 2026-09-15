# YTM Importer — План розвитку (Roadmap)

## v0.15.1 — BuildConfig compilation hotfix
- [x] розібрано GitHub Actions log;
- [x] причина: `BuildConfig` не генерувався для модуля;
- [x] додано `buildFeatures { buildConfig = true }`;
- [ ] підтвердити успішну GitHub Actions збірку v0.15.1.

## Поточна версія
**v0.15.1 — Stabilization / підготовка Release Candidate v1.0**

## 1–13. Реалізовано раніше
- [x] SearchCache + MatchScorer;
- [x] manual candidate / manual URL;
- [x] account/channel;
- [x] new / existing playlist;
- [x] duplicate detection;
- [x] Quota Planner;
- [x] Pending Queue + Resume;
- [x] History;
- [x] compact UI;
- [x] Export / Backup / Restore;
- [x] Diagnostics / Share / Cache tools.

## 14. Stabilization / RC preparation — v0.15.0
- [x] adaptive app icon;
- [x] round icon;
- [x] Android 13+ monochrome icon;
- [x] `Сервіс → Про програму`;
- [x] dynamic version from BuildConfig;
- [x] centralized ErrorMessages;
- [x] friendly HTTP/API errors;
- [x] friendly network errors;
- [x] cleanup unused `markAllPending`;
- [x] cleanup unused button fields;
- [x] full regression checklist;
- [ ] прогнати regression checklist на телефоні;
- [ ] перевірити adaptive/themed icon на телефоні;
- [ ] протестувати Share / SearchCache tools v0.14+;
- [ ] зафіксувати всі blocker bugs перед v1.0 RC.

## 15. v1.0 Release Candidate — наступний етап після тестів
- [ ] закрити blocker bugs із regression testing;
- [ ] freeze core behavior;
- [ ] final version naming;
- [ ] final release notes;
- [ ] final clean build / signed upgrade test.

## 16. Після v1.0
- [ ] повний Material 3 UI/UX redesign;
- [ ] окремі screens замість великої кількості dialogs;
- [ ] додаткові джерела імпорту;
- [ ] optional advanced playlist management.
