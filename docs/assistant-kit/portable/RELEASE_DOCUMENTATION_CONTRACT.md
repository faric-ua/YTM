# Portable Release Documentation Contract

For each new application release create the documentation skeleton **before**
the first feature-code change.

```text
docs/v.X.Y.Z/
├── RELEASE_META.json
├── RELEASE.md
├── REGRESSION_CHECKLIST.md
├── diagrams/
│   └── README.md
└── qa/
    ├── BUG_REGISTER.md
    ├── PHONE_TEST.md
    └── EVIDENCE_MANIFEST.md
```

`RELEASE_META.json` should keep machine-readable release identity:

- versionName / versionCode;
- lifecycle phase;
- branch;
- tested source SHA;
- signed build/run;
- QA status;
- release tag;
- phone-test date.

When phone QA is executed also add:

- `qa/TEST_RUN_YYYY-MM-DD.md`;
- `qa/PHONE_TEST_REPORT_YYYY-MM-DD.md`.

When system/navigation/lifecycle behavior changes, add or update a real flow/test
diagram before release closeout.

Release-close automation should verify both the release folder and the mutable
root history/status documents.

Never fabricate historical evidence that was not captured.

A missing old artifact is a retrospective gap, not reconstructed evidence.
