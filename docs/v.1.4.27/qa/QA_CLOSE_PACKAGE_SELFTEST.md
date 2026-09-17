# v1.4.27 — QA Close Package Self-Test

The QA-close package is tested before delivery for:

- clean current-state `--check`;
- first apply;
- repeated/idempotent apply;
- partial apply completion;
- duplicate-anchor fail-closed behavior;
- missing-anchor fail-closed behavior;
- literal `\n` anchor rejection;
- LF-only generated text;
- no trailing whitespace;
- no blank line at EOF;
- no `__pycache__` / `.pyc`;
- `git diff --check` on the fixture;
- immutable v1.4.27 QA-close audit;
- evidence count = 4;
- evidence manifest includes sanitized screenshots.

This package changes QA/docs/status only. It does not change Android production behavior or versionCode/versionName.
