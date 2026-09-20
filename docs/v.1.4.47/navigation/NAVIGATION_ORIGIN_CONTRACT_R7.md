# R7 — контракт власника навігаційного маршруту

Мета: не змішувати прямі Home-маршрути з delegated-маршрутами через `Поточний плейлист` або `Меню`.

## Прямий Home route

### Home block `4 кроки до плейлиста` → `3. Знайти / перевірити`

```text
[Головна]
→ 4 кроки до плейлиста
→ 3. Знайти / перевірити
→ [Перевірка треків]
```

`←` та Android system Back із `[Перевірка треків]`:
**повернення на `[Головна]`**.

Цей маршрут не має права успадковувати старий `returnToPlaylistHubAfterDelegatedAction`.

### Home block `4 кроки до плейлиста` → `4. Створити / додати`

Це також прямий Home route. Back із Destination flow повертає до прямого Home/Review контексту, а не самовільно відкриває `Поточний плейлист`.

## Playlist-owned route

### Home block `Поточний плейлист`

```text
[Головна]
→ Поточний плейлист
→ [Поточний плейлист]
→ Знайти / перевірити
→ [Перевірка треків]
```

Якщо Review був відкритий як delegated action із `[Поточний плейлист]`,
Back повертає у `[Поточний плейлист]`.

### Нижня панель → `Плейлист`

```text
[Головна]
→ нижня панель
→ Плейлист
→ [Поточний плейлист]
→ ...
```

Це окрема точка входу для діаграми, хоча вона використовує той самий Playlist screen і той самий delegated parent contract.

## Нижня панель → `Пошук`

Це прямий search/review route:
Back із `[Перевірка треків]` повертає на `[Головна]`, якщо користувач не зайшов у Review через Playlist-owned flow.

## Технічний контракт

- direct Home/bottom-search entry очищає delegated return flags перед відкриттям Review/Destination;
- direct Playlist entry очищає старі delegated flags перед відкриттям Playlist root;
- тільки `handlePlaylistHubResult(...)` має встановлювати Playlist delegated return ownership через `beginPlaylistRelay(...)`;
- тільки Menu delegated actions мають встановлювати Menu return ownership через `beginMenuRelay(...)`;
- Back/Cancel не повинен перекидати користувача в іншу гілку Home, ніж та, з якої почався маршрут.
