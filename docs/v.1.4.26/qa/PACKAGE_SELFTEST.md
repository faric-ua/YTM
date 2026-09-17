# v1.4.26 — Package / Apply Self-Test

Performed before the package was delivered.

## Current-main anchor verification

Patch anchors were prepared from the current `main` versions of:

- `ImportActivity.kt`;
- `AccountLibraryExporter.kt`;
- `app/build.gradle.kts`;
- release/status/backlog/tutorial/preflight files.

## Apply tests

**PASS**

- clean current-main-style fixture `--check`;
- first apply;
- static v1.4.26 audit after apply;
- post-apply `--check`;
- second apply with no file changes (idempotent);
- duplicate-anchor failure;
- missing-anchor failure;
- literal `\\n` anchor rejection;
- LF-only generated files;
- no trailing whitespace in generated files;
- Python helper syntax.

## Kotlin parser smoke

Generated v1.4.26 versions of:

- `ImportActivity.kt`;
- `AccountLibraryExporter.kt`;

were passed through `kotlinc` parser smoke locally.

Expected unresolved Android/project symbols were ignored; no Kotlin parser/syntax errors
(`expecting`, `unexpected tokens`, missing braces/parentheses) were found.

## Important

A successful package self-test does not replace:

- GitHub Android build;
- signed APK verification;
- real-phone selective-export QA.
