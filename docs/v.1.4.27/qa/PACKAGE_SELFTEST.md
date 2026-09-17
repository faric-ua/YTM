# v1.4.27 — Package Self-Test

Status: **PASS before delivery**

Validated against a current-main-style temporary fixture:

- clean `--check`;
- clean first apply;
- post-apply `--check`;
- repeat/idempotent apply;
- partially applied state completion;
- duplicate-anchor fail-closed behavior;
- missing-anchor fail-closed behavior;
- literal `\n` anchor rejection;
- package-owned text is LF-only;
- no trailing whitespace;
- no extra blank line at EOF;
- no `__pycache__` / `.pyc`;
- Python helper syntax;
- `git diff --check`;
- v1.4.27 exact-ID static audit;
- strengthened SearchCoordinator audit.

## Kotlin smoke

A real Kotlin/JVM compile-and-run smoke was also executed before delivery using
the patched `SearchCoordinator.kt` plus small dependency stubs.

Verified behavior:

- canonical exact track (`MATCHED`, exact `selectedVideoId`, empty candidates)
  plans **0** searches and executes **0** API searches;
- its exact videoId remains unchanged;
- a candidate-based automatic match remains eligible for intentional repeat
  search;
- an explicit manual exact selection remains protected.

The delivered phone-side package self-test does not require `kotlinc`; this
compile smoke was a pre-delivery validation step.

## R2 audit correction

The first delivered v1.4.27 audit used a file-wide string check for
`preserveExistingExact: Boolean = false`.

That was too broad: `startSearch(...)` legitimately keeps a local default of
`false`, while the safety invariant applies specifically to the ordinary
`searchAll(...)` entry point and to the Review repeat-search call site.

R2 narrows the audit to the exact `searchAll(...)` signature and updates the
self-test to inject a realistic `startSearch(... = false)` helper and verify
that the audit still passes. This prevents the same false-positive from passing
package self-test again.
