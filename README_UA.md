# YTM Importer v0.10.0

Android-застосунок для імпорту списків треків у YouTube / YouTube Music.

## Нове у v0.10.0

### Квота API (Quota Planner)

Застосунок локально рахує:

- `search.list` виклики;
- cache hits (попадання в кеш);
- приблизні units (одиниці) інших YouTube API операцій.

Перед пошуком і записом показується приблизний план.

**Це не точний залишок Google Cloud quota.**
Точний usage (використання) може відрізнятися, якщо той самий API project
використовується іншими пристроями.

### Черга (Pending Queue)

Якщо YouTube повертає quota error:

- уже додані треки залишаються;
- поточний і наступні треки стають `PENDING`;
- зберігаються playlist ID, account/channel та videoId;
- завдання переживає перезапуск програми;
- пізніше можна натиснути `Черга → Продовжити`.

### Termux: завантажити APK

Після успішної GitHub Actions збірки:

```bash
bash scripts/download-latest-apk.sh
```

APK буде завантажено в:

`/sdcard/Download/YTM-APK/`

Щоб відкрити його:

```bash
termux-open "$(find /sdcard/Download/YTM-APK -name '*.apk' | head -n 1)"
```

## Документація

Поточна версія:

`docs/v.0.10.0/`

Попередні snapshots (знімки версій) залишаються окремо.
