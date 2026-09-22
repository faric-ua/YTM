
# Portable Release Documentation Contract

For each release create:

```text
docs/v.X.Y.Z/
├── RELEASE.md
├── REGRESSION_CHECKLIST.md
├── diagrams/
│   └── README.md
└── qa/
    ├── BUG_REGISTER.md
    ├── PHONE_TEST.md
    └── EVIDENCE_MANIFEST.md
```

When phone QA is executed also add:

- `qa/TEST_RUN_YYYY-MM-DD.md`
- `qa/PHONE_TEST_REPORT_YYYY-MM-DD.md`

When system/navigation/lifecycle behavior changes, add or update diagrams.

Never fabricate historical evidence that was not captured.

A missing old artifact is recorded as a retrospective gap, not reconstructed as
if it existed at the time.
