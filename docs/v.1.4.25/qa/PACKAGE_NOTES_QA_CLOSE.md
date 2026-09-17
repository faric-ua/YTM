# v1.4.25 — QA Close Package Notes

This package is documentation-only.

It:

- records real-phone v1.4.25 evidence;
- sanitizes Destination account identity;
- updates release/test status conservatively;
- preserves untested items as untested;
- creates the first curated tutorial layer under `docs/tutorial/`;
- does **not** change Android application code or version numbers.

The package apply script is tested for:

- clean apply;
- repeat/idempotent apply;
- missing/duplicate anchor failure;
- literal `\\n` regression;
- LF-only generated text;
- no Python bytecode cache pollution.
