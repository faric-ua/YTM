# YTM Importer v1.0.0-rc4 — Release hardening

## Мета

RC4 не додає нової користувацької функціональності.
Це фінальне технічне укріплення перед `v1.0.0 stable`.

## GitHub Actions

Оновлено:

`actions/setup-java@v4` → `actions/setup-java@v5`

## Перевірка готового APK

Після `assembleRelease` workflow тепер перевіряє:

1. підпис через `apksigner verify`;
2. zip alignment через `zipalign -c`;
3. package/version через `aapt dump badging`;
4. SHA-256 готового APK.

Artifact містить:

- `YTM-Importer-v1.0.0-rc4-release.apk`
- `YTM-Importer-v1.0.0-rc4-release.apk.sha256`

## Що не змінювалось

Логіка імпорту, Search, History, YTM Project, duplicates,
Quota/Queue та Backup/Restore/Rollback не змінювалась.

## Версія

```text
versionCode = 25
versionName = "1.0.0-rc4"
```

## Stable gate

Якщо RC4 збирається, проходить APK verification,
встановлюється поверх RC3.1 і проходить короткий smoke/data-safety test,
наступна версія — `v1.0.0 stable`.
