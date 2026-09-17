# v1.4.27 — Phone Test

## Перевірка 1 — exact project

Import/reopen the already exported `top 3` YTM Project.

Expected:

- 3 tracks;
- exact videoId: 3;
- missing videoId: 0;
- Review: 3/3 ready.

Take one screenshot if needed.

## Перевірка 2 — BUG-005 retest

In Review, tap `↻ Пошук`.

Expected search plan:

- tracks in list: 3;
- search required: **0**;
- new `search.list`: **0**.

Send the search-plan screenshot.

## Перевірка 3 — no state damage

Close/cancel the plan or complete the zero-work path if the UI allows it.

Expected:

- exact videoId remains 3/3;
- Review remains 3/3 ready.

## Перевірка 4 — non-exact smoke

Only if convenient, import a small plain-text list with at least one track that
has no exact videoId.

Expected:

- search plan is non-zero for tracks that genuinely need search.

This proves the guard is selective rather than disabling Search globally.
