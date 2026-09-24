# v1.4.51 — Stabilization Checkpoint

Status: **PHONE QA PASS / FINAL**

- versionName: `1.4.51`
- versionCode: `94`
- branch: `feat/v1.4.51-url-mix-snapshot`
- exact tested app source: `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`
- signed run: `35943953149`
- phone-test date: `2026-09-24`
- release tag: `v1.4.51`
- checkpoint tag: `checkpoint-v1.4.51-phone-pass`

Accepted targeted scope:
- U51-1..U51-6 PASS;
- concrete-playlist URL snapshot import;
- explicit dynamic-Mix unsupported behavior;
- preview Cancel/Back and recreation safety;
- invalid/unsupported source error path;
- explicit local snapshot handoff with no remote playlist write;
- UX-024/025/026 and BUG-035 corrective acceptance.

Final U51-6 handoff evidence:
- source snapshot: 813 rows;
- explicit dedupe result: 320 saved / 493 duplicates;
- History: local import semantics, no YTM write counters;
- Home isolation: no automatic create/add flow, queue or write.

UX-027 and UX-028 remain non-blocking future polish and do not invalidate this checkpoint.
