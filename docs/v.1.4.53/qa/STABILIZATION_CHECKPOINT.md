# v1.4.53 — Stabilization Checkpoint

Status: **PHONE QA PASS / FINAL**

- versionName: `1.4.53`
- versionCode: `96`
- branch: `feat/v1.4.53-quota-recovery`
- exact tested app source: `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`
- signed run: `36195438071`
- phone-test date: `2026-09-26`
- release tag: `v1.4.53`
- checkpoint tag: `checkpoint-v1.4.53-phone-pass`
- stable publisher run: `36250364471` — PASS

Accepted targeted scope:
- durable SEARCH recovery job on real Search quota stop;
- rotation/restart/unrelated-import isolation;
- explicit Search resume retries only WAITING_QUOTA work;
- separate Search 100-call bucket vs non-Search 10,000-unit estimate;
- existing WRITE recovery semantics preserved and later completed 4/4;
- History durability controlled comparison: 0 removed IDs / 0 changed pre-existing records;
- BUG-036/037/038 and UX-029 closed for the accepted release scope.

Deferred non-blocking follow-ups:
- BUG-039 — ambiguous generic write HTTP 429 classification;
- UX-030 — explicit local ↔ YTM linkage visibility.

Stable release/checkpoint tags and durable GitHub Release publication are complete. Equal-version OTA smoke remains the final post-publication phone check.
