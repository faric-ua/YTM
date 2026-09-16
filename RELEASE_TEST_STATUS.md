# YTM Importer — Release test status

This file is the mutable test-status register.

Immutable versioned release notes under `docs/v.*` are not rewritten when a
phone-test status changes later.

| Version | Status | Note |
|---|---|---|
| v1.4.11 | PARTIALLY PHONE-TESTED | Custom-dialog motion Q-002 still reproduced on the real phone. |
| v1.4.12 | **NOT TESTED** | Cleanup Wave 2 was generated and statically audited, but the user explicitly confirmed it was not phone-tested. |
| v1.4.13 | **NOT TESTED YET** | Cleanup Wave 3 / SearchCoordinator; requires GitHub build + phone regression. |

## Rule

`PASS` may only be recorded after the user confirms the relevant phone
regression. Static audits and GitHub compilation alone do not count as a
phone-tested release.
