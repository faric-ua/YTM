# YTM Importer v1.0.0-rc1 — Release Candidate 1

## Статус

Це перший **Release Candidate (кандидат у стабільний реліз)**.

Основна функціональна логіка заморожена. До фінального `v1.0.0`
не додаємо нових великих функцій. Виправляємо тільки:

- blocker bugs (критичні помилки);
- втрату/пошкодження даних;
- помилки авторизації або запису плейлиста;
- помилки, які роблять основний сценарій непридатним до використання.

Візуальний redesign (переробка інтерфейсу) — **після v1.0.0**.
До релізу виправляємо UI лише тоді, коли елемент не видно,
не натискається або він реально блокує роботу.

## Функції, які входять у RC1

- CSV / TXT / прямий текст;
- Google OAuth;
- Google account + YouTube/YTM channel;
- SearchCache;
- MatchScorer;
- manual candidate;
- manual YouTube/YTM URL з реальною назвою/каналом;
- створення нового плейлиста;
- додавання до існуючого;
- duplicate detection за videoId;
- privacy selector;
- Quota Planner;
- Pending Queue + Resume;
- History;
- Export / Backup / Restore;
- Diagnostics / Share / SearchCache tools;
- friendly ErrorMessages;
- signed update поверх попередньої версії.

## Нове саме в RC1

Функцій для користувача не додаємо.

Додано **RC preflight** — автоматичну перевірку репозиторію перед
GitHub Actions build:

- package ID;
- versionCode/versionName;
- compileSdk/targetSdk;
- BuildConfig;
- відсутність JKS / signing properties у Git;
- збереження Android SDK workflow hotfix;
- наявність RC release docs/checklist.

Це захищає RC від випадкового повернення вже виправлених build-проблем.

## Версія

```text
versionCode = 21
versionName = "1.0.0-rc1"
```
