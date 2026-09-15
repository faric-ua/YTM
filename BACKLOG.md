# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v1.0.0-rc3.1 — Data Safety / Restore Guard**

## Підтверджено реальними тестами
- [x] signed update збережує History;
- [x] History navigation;
- [x] YTM Project save/import;
- [x] duplicate detection;
- [x] manual URL metadata replacement;
- [x] основний playlist flow.

## RC3
- [x] backup appVersion більше не hardcoded `0.14.0`;
- [x] History TXT використовує BuildConfig version;
- [x] backup schema v2;
- [x] SHA-256 integrity check;
- [x] structural/type validation;
- [x] automatic safety snapshot before Restore;
- [x] rollback last Restore;
- [x] automatic rollback attempt on Restore failure;
- [x] schema v1 backward compatibility;
- [x] RC3 build failure identified: malformed Kotlin string;
- [x] RC3.1 syntax hotfix prepared;
- [ ] GitHub Actions RC3.1 build;
- [ ] signed update RC2 → RC3;
- [ ] real backup → restore → rollback test.

## До v1.0.0 stable
- [ ] finish remaining blocker regression;
- [ ] verify full backup/restore/rollback;
- [ ] no data-loss bugs;
- [ ] final signed upgrade;
- [ ] versionName `1.0.0`.

## Після v1.0.0
- [ ] Material 3;
- [ ] responsive UI;
- [ ] separate screens;
- [ ] layout/spacing/typography polish;
- [ ] accessibility.
