# 06 — Account Library Export: від export-all до selective export

Цей розділ побудований на реальній еволюції YTM Importer:

- v1.4.18 — імпорт одного playlist з підключеного акаунта;
- v1.4.19 — export-all усіх доступних playlist;
- v1.4.26 — selective export кількох вибраних playlist.

## 1. Чому не починати одразу з «універсальної» функції

Перший корисний сценарій був простим:

> прочитати один playlist і відкрити його локально.

Після цього з'явився export-all:

> зробити локальний snapshot бібліотеки акаунта.

Лише коли обидва сценарії стали зрозумілими, виникла реальна потреба:

> вибрати 2–5 playlist і не витрачати час/API requests на інші.

Це хороший приклад incremental product design.

## 2. Межі відповідальності

Selective export має три окремі частини.

### Account API

Отримує список playlist та ordered playlist items.

Важливе правило: список потрібен для picker-а, але детальні
`playlistItems.list` запити мають виконуватися лише для підтвердженого selection.

### UI

UI відповідає за:

- checkbox selection;
- заборону порожнього export;
- folder picker;
- відображення підсумку.

UI не повинен вирішувати, як серіалізується YTM Project.

### Storage/export

`AccountLibraryExporter` відповідає за:

- session folder;
- project file naming;
- YTM Project output;
- manifest.

## 3. Навіщо exact videoId

Назва `Artist - Track` — це лише текст для пошуку.

`videoId` — точний YouTube resource identifier.

Якщо account export уже отримав exact videoId, зберігати лише назву означало б
викинути найціннішу інформацію й потім знову запускати search.

Тому round-trip test має вигляд:

`account → export → YTM Project → import → exact videoId still present`

## 4. Manifest як журнал операції

Manifest — не просто список файлів.

Він відповідає на питання:

- що користувач намагався експортувати;
- що реально було записано;
- що було skipped;
- що failed;
- скільки playlistItems requests було потрібно;
- який режим export створив цей snapshot.

У v1.4.26 додається:

- `schemaVersion = 2`;
- `selectionMode = ALL | SELECTED`.

Це стане важливим для майбутнього manifest import та incremental backup.

## 5. State restoration

Android може recreate Activity під час зовнішнього folder picker або rotation.

Тому confirmed selection не повинен існувати лише в локальній змінній dialog-а.

YTM Importer зберігає snapshot metadata вибраних playlist у
`savedInstanceState`, а після recreate відновлює його.

Навчальний висновок:

> якщо зовнішній Intent є частиною багатокрокового flow, зберігайте state до
> переходу назовні.

## 6. Read-only safety

Selective export не повинен викликати playlist insert/update/delete API.

Це окремий safety invariant, який має бути:

- у release docs;
- у static audit;
- у phone-test interpretation.

## 7. Практична вправа

1. Виберіть 2 playlist.
2. Експортуйте їх.
3. Порахуйте project files.
4. Прочитайте manifest.
5. Перевірте `selectionMode`.
6. Re-import один project.
7. Переконайтеся, що exact videoId не втрачений.

Це короткий, але повний вертикальний тест:
API → UI → storage → file → import → Review.
