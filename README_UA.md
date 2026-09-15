# YTM Importer v0.13.1

## Нове: Дані (Export / Backup)

У верхньому меню:

`Дані`

Доступні операції:

- `Експорт History → TXT`
- `Експорт History → JSON`
- `Експорт Черги → JSON`
- `Створити повний backup → JSON`
- `Відновити з backup JSON`

### Повний backup

Включає:

- History;
- Pending Queue;
- локальну статистику квоти;
- SearchCache.

Не включає:

- OAuth access token;
- Google password;
- signing key;
- GitHub secrets.

Backup може містити Google email, YouTube Channel ID,
назви плейлистів та історію треків, тому його краще не публікувати.

### Збереження файлу

Android сам відкриє системне вікно вибору папки.
Можна вибрати, наприклад, `Download`.

### Restore

Restore замінює поточні локальні History / Queue / Quota / Cache
даними з backup.

YouTube/YTM плейлисти в інтернеті restore не змінює.

## Документація

`docs/v.0.13.0/`

## Уточнення логіки експорту

`Історія → TXT`
- для читання людиною;
- можна відкрити звичайним текстовим редактором.

`Історія → JSON`
- технічна копія тільки History;
- НЕ використовується як повний Restore.

`Черга → JSON`
- технічна копія Pending Queue;
- НЕ є повним backup.

`Повний backup → JSON`
- саме цей файл містить History + Queue + Quota + SearchCache;
- саме його потрібно використовувати через `Restore повного backup`.
