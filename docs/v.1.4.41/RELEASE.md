# YTM Importer v1.4.41 — Auth/Search Recovery + UI Consistency

- versionName: **1.4.41**
- versionCode: **77**
- status: **NOT PHONE-TESTED YET**
- branch: `feat/v1.4.41-auth-ui-consistency`

## Scope

This release packages fixes found during the real `House Dance Hit 2000 Vol.1`
migration on v1.4.40.

### BUG-004

Search-path HTTP 401 is now treated as an authorization/session failure:

- stop on the first 401;
- do not convert the whole playlist into repeated auth failures;
- return the active track to retryable `NEW`;
- invalidate shared/persistent ready state;
- keep the local workspace;
- do not auto-open Review after the interrupted search;
- repair legacy auth-failed persisted rows after successful re-login.

### BUG-009

Account modal phone fit:

- `Змінити акаунт` → `Змінити`;
- shorter profile explanation;
- shared horizontal modal placement applies.

### BUG-010

Full Restore no longer applies old `quota_tracker_v1` values.

Quota counters may remain inside backup JSON for diagnostics/backward compatibility,
but ordinary Restore and safety rollback preserve the live local quota tracker.

### UX-017

Filename-derived playlist names are humanized:

- `_` → spaces;
- trailing `YTM` / `YTM Importer` removed;
- `Vol1` / `Vol 1` → `Vol.1`;
- explicit in-file title remains authoritative.

### UX-018

Horizontal modal action contract:

- action/confirm on the left;
- cancel/close/no-op on the right;
- optional secondary action between them;
- vertical action sheets keep explicit top-to-bottom order.

## Boundaries

Not part of v1.4.41:

- Home layout redesign (UX-019);
- Blue/Green four-button state-palette redesign (UX-009);
- UX-008 Phase 2B;
- broad localization;
- final populated-History Restore/rollback proof.

Phone QA is required before closing BUG-004/009/010 or UX-017/018.
