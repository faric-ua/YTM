# 01 — Як проєкт еволюціонував

Цей розділ корисний тому, що показує: архітектура не з'являється ідеальною в перший день.

## Етап 1 — робочий сценарій

Спочатку важливо було пройти шлях:

`імпорт → пошук/перевірка → створення/додавання playlist`

Функціональність мала вищий пріоритет за красу.

## Етап 2 — винесення відповідальностей

Коли MainActivity почав брати на себе забагато, доменну логіку поступово винесли:

- `SearchCoordinator` — orchestration пошуку;
- `DestinationCoordinator` — вибір/перевірка цільового playlist;
- `PlaylistWriteCoordinator` — запис треків;
- окремі Activity — Import, Review, Destination, History, Pending, Data, Service.

Навчальний висновок:

> спочатку знайдіть стабільний сценарій, потім рефакторте межі відповідальності.

## Етап 3 — відновлюваність

Реальний мобільний застосунок мусить переживати:

- поворот екрана;
- перезапуск процесу;
- quota error;
- перерваний запис;
- оновлення APK.

Тому з'явилися:

- current workspace persistence;
- history;
- pending queue;
- auth-state marker;
- backup/export.

## Етап 4 — account library

Після основного сценарію додали:

- імпорт одного playlist з підключеного акаунта;
- збереження exact `videoId`;
- bulk export усіх доступних playlist;
- manifest для відтворюваності експорту.

## Етап 5 — системний UI

Теми не починалися з тотального redesign.

Вони йшли хвилями:

- v1.4.21 — Theme System Wave 1;
- v1.4.22 — Visual Structure Polish;
- v1.4.23 — real-phone button fit;
- v1.4.24 — Theme Wave 2;
- v1.4.25 — Accent Card System.

Це важливий підхід:

> UI-систему краще стабілізувати маленькими перевірюваними хвилями, а не одним великим переписуванням.

## Етап 6 — workflow сам став частиною продукту

Після помилок у patch/apply процесі були додані правила:

- clean apply test;
- repeat/idempotence test;
- duplicate/missing anchor guards;
- literal `\n` guard;
- `__pycache__` guard;
- LF/CRLF guard;
- `git diff --check`;
- deletion checks.

Тобто розвивався не лише застосунок, а й **система безпечного внесення змін**.
