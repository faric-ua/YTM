# v1.4.25 — Package/apply self-test

Performed before delivering the ZIP to the user.

## Tests

- clean fixture `--check` — PASS;
- clean fixture first apply — PASS;
- post-apply `--check` — PASS;
- second real apply — PASS / no file changes (idempotent);
- duplicate anchor — correctly fails closed;
- missing anchor — correctly fails closed;
- literal `\\n` multiline-anchor regression — correctly rejected.

## Important

The self-test caught one generated literal-`\\n` version anchor during package preparation.
That defect was corrected **before** the package was delivered, and the full self-test was run again.

Phone QA remains required after build/install.
