# YTM Importer — Release test status

This is the mutable phone-test status register.
Immutable `docs/v.*` release notes are never rewritten to fake later validation.

| Version | Status | Note |
|---|---|---|
| v1.4.11 | PARTIALLY PHONE-TESTED | Q-002 custom-dialog entrance motion still reproduced. |
| v1.4.12 | **NOT TESTED** | Cleanup Wave 2 was generated and statically audited, but no phone regression was run. |
| v1.4.13 | PARTIALLY PHONE-TESTED | Installed on phone; screenshot confirms Home/account state and a successful 3-track playlist creation. Full regression was not run. |
| v1.4.14 | **NOT TESTED YET** | Silent auth recovery + result modal + master QA plan. Requires GitHub build and phone testing. |

## Rule

Only mark a version `PHONE TESTED` after the required real-device cases are
actually executed. Static audits and GitHub compilation are necessary but are
not equivalent to phone testing.
