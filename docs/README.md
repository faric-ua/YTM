# YTM Importer — Documentation convention

Current: `docs/v.1.4.16/`

Current QA: `qa/`

From v1.4.15 every release includes an immutable QA snapshot:
`docs/v.X.Y.Z/qa/`

Baseline QA files:
- MASTER_TEST_PLAN.md
- RELEASE_TEST_PLAN.md
- TEST_RUN_TEMPLATE.md
- TEST_DATA.md
- BUG_REGISTER.md

From v1.4.16, releases that receive phone testing also store actual evidence in the same version folder:
- `TEST_RUN_YYYY-MM-DD.md`
- `PHONE_TEST_REPORT_YYYY-MM-DD.md`
- `UI_SCREENSHOT_ANALYSIS_YYYY-MM-DD.md` when screenshot review is relevant.

Release architecture and QA-flow diagrams live under:
`docs/v.X.Y.Z/diagrams/`

Global evidence rules:
`qa/TEST_EVIDENCE_POLICY.md`
