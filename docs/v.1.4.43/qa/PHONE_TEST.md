# v1.4.43 phone QA — Auth Freshness

Static/build PASS is not phone PASS.

## 1. Installed version

Expected:
- Home shows **v1.4.43**.

## 2. Primary BUG-013 retest

Precondition:
- Step 2 is green from the previously connected account.

Path:
`Home → 4. Створити / додати → додати в існуючий playlist`

Expected:
- YTM Importer performs a Google authorization refresh/check before using the token for
  the live playlist-list request;
- a silently refreshable session should continue into the existing-playlist list;
- if Google requires user interaction, the authorization resolution should appear
  before the live destination request fails;
- the old sequence “green → destination API auth error → only then red” should not be
  the normal first signal anymore.

## 3. Re-login recovery

If authorization UI is required:
- complete it using the same account;
- Step 2 becomes green;
- return to `4. Створити / додати → existing playlist`;
- existing playlist list loads normally.

## 4. Search smoke

Path:
`Home → 3. Знайти / перевірити`

For the current cached House Dance workspace:
- Search plan opens;
- if cache is still intact, expect 0 new `search.list`;
- do not intentionally consume Search quota just for this release test.

## 5. Write smoke

Without needing to finish a real write:
- open new-private-playlist destination;
- confirm the flow reaches its normal pre-write state;
- Back/Cancel should leave remote state unchanged.

If a real HTTP 401 is naturally reproduced during create/add:
- authorization must turn red;
- remaining tracks must stay retryable/pending;
- unfinished write must remain in Queue.

## 6. Separate UI findings

Do not mix into v1.4.43 acceptance:
- UX-021 landscape/wide horizontal action rows;
- UX-022 title emphasis across windows/modals.
