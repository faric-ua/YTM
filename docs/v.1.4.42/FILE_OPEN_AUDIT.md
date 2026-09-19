# v1.4.42 static file-open audit

Contract:
- `ImportActivity` and `DataActivity` route file-open flows through
  `RecentFileChooserActivity`;
- `RecentFileChooserActivity` owns the Android `ACTION_OPEN_DOCUMENT` fallback;
- `SafRecentFileQuery` sorts by `lastModified` descending;
- direct child files from persisted read roots are shown;
- Import allows txt/csv/json;
- Data allows json;
- no broad filesystem permission is introduced.

Phone QA remains authoritative for visible ordering and SAF-provider behavior.
