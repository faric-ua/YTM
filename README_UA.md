# YTM Importer v0.7.1

Android-застосунок для імпорту CSV/TXT плейлистів у YouTube / YouTube Music.

## Реалізовано
- CSV/TXT;
- Google OAuth;
- YouTube search;
- до 10 кандидатів;
- improved MatchScorer;
- 30-day SearchCache;
- ручний вибір кандидата;
- manual URL / skip;
- Private / Unlisted / Public;
- create playlist / add tracks;
- result panel;
- open in YTM;
- copy playlist link;
- replacement log;
- Mermaid diagrams.

## Hotfix v0.7.1
Виправлено невидимий список кандидатів.
Причина: `AlertDialog.setMessage()` конфліктував із `setItems()`.

## Збірка
GitHub → Actions → **Build Signed Android APK** → **Run workflow**

Потрібні GitHub Secrets:
- `YTM_KEYSTORE_B64`
- `YTM_STORE_PASSWORD`
- `YTM_KEY_PASSWORD`

Alias: `ytmimporter`.

## Безпека
Не коміть:
- `.jks`;
- `release-signing.properties`;
- паролі/секрети.

## Діаграми
`docs/diagrams/`

GitHub сам відображає Mermaid як схеми.
