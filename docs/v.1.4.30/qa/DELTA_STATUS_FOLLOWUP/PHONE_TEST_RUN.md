# v1.4.30 — Delta status follow-up, real phone run — 2026-09-18

Статус: **TARGETED PHONE QA PASS FOR DELTA CLASSIFICATION + CONSOLIDATED MATERIALIZATION; OFFLINE STATE VALIDATION PASS.**

Це не повна регресія застосунку. Normal-open / exact-search phone checks для кожного F1/F2/F3 окремо не виконувалися у цьому follow-up; вміст і порядок перевірені локальним валідатором.

| Поле | Фактичне значення |
|---|---|
| Дата | 2026-09-18 |
| Встановлений APK | 1.4.30 / code 64 |
| Git HEAD перед QA-пакетом | `c50e331a892e75eec6ffcb0b1808678729c9d392` |
| QA package | `YTM_Importer_v1.4.30_DELTA_STATUS_QA_R1.zip` |
| QA package SHA-256 | `be3cc798fdc0a994098713723e36722677bc1679906b41fbdac0f094b5a67202` |
| Fresh ALL baseline | `260918-052041-YTM-Export` |
| Baseline state | 21 playlists / 21 projects / failed 0 |
| QA target | `YTM-QA-1430-20260918-012657-TEST` |
| QA target playlist ID | kept out of public QA docs |
| NEW delta | `260918-055050-YTM-Sync` |
| NEW full | `260918-060716-YTM-Full` |
| UPDATED delta | `260918-061915-YTM-Sync` |
| UPDATED full | `260918-062233-YTM-Full` |
| MISSING delta | `260918-062847-YTM-Sync` |
| MISSING full | `260918-063100-YTM-Full` |

## Results

| Stage | Real phone scan | Chain/materialize phone evidence | Offline validator | Verdict |
|---|---|---|---|---|
| B0 ALL | 21/21 exported, failed 0 | N/A | baseline accepted | PASS |
| NEW | NEW 1 / UPDATED 0 / UNCHANGED 21 / MISSING 0 / FAILED 0 | chain 2 → final 22 / projects 22 / API 0 | `NEW DELTA + CONSOLIDATED FILE CHECK PASSED` | PASS |
| UPDATED | NEW 0 / UPDATED 1 / UNCHANGED 21 / MISSING 0 / FAILED 0 | chain 3 → final 22 / projects 22 / API 0 | `UPDATED DELTA + CONSOLIDATED FILE CHECK PASSED` | PASS |
| MISSING | NEW 0 / UPDATED 0 / UNCHANGED 21 / MISSING 1 / FAILED 0 | chain 4 → final 21 / projects 21 / applied MISSING 1 / API 0 | `MISSING DELTA + CONSOLIDATED FILE CHECK PASSED` | PASS |

## State transition proved by the run

`21 baseline → NEW → 22 → UPDATED → 22 → MISSING → 21`

The local validator also verified the linked source sessions and the resulting consolidated logical state.

## Deviations encountered

### Transient DNS failure before the accepted baseline

The first ALL export attempt produced 3 FAILED records with:

`Unable to resolve host "www.googleapis.com": No address associated with hostname`

The failed export was moved out of `sources/` and preserved under `rejected-baselines/`. A clean retry then exported all 21 playlists successfully and became the accepted baseline.

This single occurrence is recorded as a transient network/DNS event, not independently classified as an application defect.

### Output folder selection mistake

The first NEW delta was accidentally created inside the baseline folder. It was moved one level up without editing its files so baseline and delta became sibling sessions under `sources/`, which is what chain discovery requires.

The run exposed a UX opportunity: destination copy/guarding could make the required parent folder clearer and prevent nesting a new session inside an existing backup session.

## BUG-004 / Q-004 — reproduced

The run produced real HTTP 401 invalid-authentication responses.

After a 401, Home still showed the green/checked `2. Google / YTM ✓` ready state and connected status. The user had to use the account action and authorize again before backup API reads worked.

The same invalid-auth problem appeared again during the UPDATED stage.

**BUG-004 / Q-004: OPEN — REPRODUCED v1.4.30.**

Expected behavior remains:

- HTTP 401 invalidates the ready in-memory auth state;
- Step 2 immediately stops showing green/checked;
- the user is directed to reauthorize;
- local working playlist/search selections remain intact.

## UI polish findings

Non-blocking findings from this run:

- preflight action `Перевірити зміни` wraps to two lines beside `Скасувати`; proposed label: `Перевірити`;
- backup dialogs mix Ukrainian and English (`Backup chain`, `preview`, `state`, `source chain`, `self-contained full backup`);
- folder-picker flow does not clearly prevent choosing an existing backup session as the destination parent.

These should be fixed in a separate code/UI patch so the tested v1.4.30 binary remains unchanged during this QA evidence run.

## Scope limits

Not claimed by this follow-up:

- full release regression;
- a real FAILED-chain replay (the materializer intentionally rejects FAILED);
- normal-open phone verification of each F1/F2/F3;
- exact-search phone verification of each NEW/UPDATED consolidated target;
- long-term auth-token refresh behavior beyond the observed 401 reproductions.

The previous v1.4.30 SELECTED(2) unchanged-chain normal-open/exact-ID test remains valid historical evidence.
