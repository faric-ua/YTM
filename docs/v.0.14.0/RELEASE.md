# YTM Importer v0.14.0 — Diagnostics / Share / Cache tools

## Головна мета

Додати інструменти для діагностики і технічного обслуговування,
не займаючи нову вертикальну площу основного списку треків.

## Нове у головному меню

Компактна кнопка:

`Сервіс`

Вона відкриває:

- `Діагностика`;
- `Поділитися Diagnostics TXT`;
- `Зберегти Diagnostics TXT`;
- `SearchCache`;
- `Google Cloud Console`.

## Diagnostics TXT містить

- версію застосунку;
- Android / модель телефона;
- стан авторизації;
- замаскований Google email;
- замаскований Channel ID;
- поточний playlist і кількість track states;
- локальну оцінку quota;
- останню quota error;
- SearchCache statistics;
- кількість History / Pending jobs;
- приблизний розмір локальних JSON.

Diagnostics не містить OAuth token, пароль Google або signing keys.

## Android Share

У меню `Дані` додано:

- `Поділитися History TXT`;
- `Поділитися повним backup`.

Для передачі файлів використовується Android `FileProvider`.
Файли створюються у внутрішньому cache і передаються іншому застосунку
тільки з тимчасовим read permission (дозволом читання).

## SearchCache tools

Показуються:

- total entries;
- valid entries;
- expired entries;
- malformed entries;
- приблизний розмір;
- oldest / newest timestamp.

Доступно:

- очистити лише прострочені/пошкоджені записи;
- очистити весь SearchCache.

History, Queue і YouTube playlists при очищенні кешу не видаляються.
