# Termux — v1.4.26

Required order:

1. repository must be clean `main`;
2. copy this ZIP as an additive overlay;
3. run `python -B scripts/v1426-apply-selftest.py`;
4. run `python -B scripts/apply-v1.4.26.py --check`;
5. run the real apply;
6. run v1.4.26 audit + existing QA/release preflight;
7. verify no tracked deletions;
8. stage exact files;
9. commit/push/build.

The package self-test is intentionally independent of the phone repository state:
it builds a clean v1.4.25/current-main-style fixture and validates clean + repeat apply.
