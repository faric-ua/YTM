# Termux — v1.4.25 Accent Card System

The package is an additive overlay.

Before applying, run:
1. `python scripts/v1425-apply-selftest.py`
2. `python scripts/apply-v1.4.25.py --check`

Only then run the real apply.

The package self-test covers:
- clean first apply;
- second/repeat apply (idempotence);
- duplicate-anchor/zero-anchor failures;
- literal `\n` multiline-anchor regression.
