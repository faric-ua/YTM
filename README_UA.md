# YTM Importer v0.8.0

Android-застосунок для створення YouTube / YouTube Music плейлистів із CSV, TXT або вставленого тексту.

## Нове у v0.8.0

На головному екрані є кнопка **«1б. Текст»**.

Можна вставити список прямо в програму:

```text
Solarstone & JES - Like a Waterfall
Sultan & Tone Depth - Moments
Ahmet Ertenu - Why
```

Також підтримуються:

```text
1. Artist - Track
2) Artist – Track
• Artist — Track
- Artist - Track
```

Назву плейлиста можна вказати окремо. Після імпорту все працює так само, як із CSV:
SearchCache → YouTube API → MatchScorer → кандидати → створення плейлиста.

## Збірка

GitHub → Actions → **Build Signed Android APK** → **Run workflow**

Потрібні GitHub Secrets:

- `YTM_KEYSTORE_B64`
- `YTM_STORE_PASSWORD`
- `YTM_KEY_PASSWORD`

Alias: `ytmimporter`.

## Діаграми

`docs/v.0.8.0/diagrams/`

Історична документація v0.7.1:

`docs/v.0.7.1/diagrams/`

GitHub автоматично рендерить Mermaid-схеми.
