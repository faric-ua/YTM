# v1.4.36 regression checklist

- [ ] v1436 saved-file-destination audit
- [ ] full release preflight
- [ ] signed APK
- [ ] Data export opens YTM Importer destination menu first
- [ ] Review Project save opens YTM Importer destination menu first
- [ ] History Project save opens YTM Importer destination menu first
- [ ] Service Diagnostics save opens YTM Importer destination menu first
- [ ] `Скасувати` exits without Android picker
- [ ] remembered write root saves directly without system picker
- [ ] add-folder path grants a reusable write root and saves directly
- [ ] system save path still opens Android CREATE_DOCUMENT
- [ ] system save path still allows changing filename/location
- [ ] duplicate direct-save name creates a non-destructive numbered copy
- [ ] direct write failure does not leave a newly-created empty file
- [ ] one v1.4.35 folder-tree flow still works
- [ ] Import CSV/TXT/YTM Project open-file path still works
- [ ] Data Restore JSON open-file path still works
- [ ] no broad storage permission is introduced

UX-008 remains open after v1.4.36 because file-open flows and a possible in-root browser are Phase 2B/3.
