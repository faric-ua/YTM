# v1.4.35 regression checklist

- [ ] v1435 SAF-navigation audit
- [ ] full release preflight
- [ ] signed APK
- [ ] first use with no remembered tree still shows YTM Importer menu first
- [ ] after granting a folder once, repeat operation opens YTM Importer folder menu first
- [ ] remembered folder can be selected without Android SAF
- [ ] `Скасувати` closes the in-app folder menu immediately, including before the first grant
- [ ] `Додати іншу папку…` still opens Android SAF
- [ ] read-only flow does not require write permission
- [ ] read/write flow only lists roots with persisted write permission
- [ ] export-all still writes successfully to a remembered root
- [ ] backup/manifest open still reads successfully from a remembered root
- [ ] incremental baseline still reads successfully from a remembered root
- [ ] existing CSV/TXT/YTM Project `ACTION_OPEN_DOCUMENT` path still opens
- [ ] no broad filesystem permission appears

UX-008 stays open after this release because file-level open/create flows and a possible in-root browser are later phases.
