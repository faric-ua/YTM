# v1.4.41 phone QA

Use the signed release APK. Static checks alone are not PASS.

## 1. Version / update

Install over the existing app without clearing data.

PASS:
- header shows v1.4.41;
- existing local workspace/settings remain.

## 2. Account modal — BUG-009 + UX-018

Tap `2. Google / YTM` while authorized.

PASS:
- account details are readable;
- copy says `Плейлисти створюватимуться в цьому YouTube/YTM профілі.`;
- left button: `Змінити`;
- right button: `Закрити`;
- `Змінити` stays on one line.

Rotate once for a smoke check.

## 3. Modal position spot checks — UX-018

Open at least:
- one normal confirmation with an action + Cancel;
- one destructive confirmation such as local History/Queue/SearchCache clear/delete.

PASS:
- primary/destructive action is on the left;
- `Скасувати` / `Закрити` is on the right;
- no modal swaps the semantic sides.

## 4. BUG-004 — real auth invalidation

Use a genuine/reproduced invalid Google/YTM authorization condition when available.

Start Search.

PASS:
- first HTTP 401 stops Search;
- Step 2 stops showing green/checked ready state;
- current playlist stays loaded;
- current auth-interrupted track remains retryable rather than permanent auth FAILED;
- remaining tracks are not converted into repeated auth-error cards;
- Review does not auto-open.

Then re-authorize.

PASS:
- account/channel identity loads;
- Step 2 becomes ready;
- legacy persisted auth-failed rows, if any, become retryable/reviewable;
- Search can be started again.

If a real 401 cannot be reproduced, record this case BLOCKED, not PASS.

## 5. BUG-010 — quota-preserving full Restore

Before Restore:
- note Search calls and general-unit estimate.

Restore an older full backup whose stored counters are lower/different.

PASS:
- Restore works for normal local data;
- current quota estimate does not jump backward to the backup value;
- Restore UI explicitly states quota estimate is preserved.

Then, if safe, use `Відкотити`.

PASS:
- rollback restores local snapshot groups;
- live local quota estimate still does not rewind.

## 6. UX-017 — playlist title

Test A — explicit title fixture:

`docs/test-data/collections/House_Dance_Hit_2000/House_Dance_Hit_2000_Vol1_YTM.txt`

Expected current playlist title:

`House Dance Hit 2000 Vol.1`

Test B — fallback:
use equivalent track data without the explicit title line and filename:

`House_Dance_Hit_2000_Vol1_YTM.txt`

Expected fallback title:

`House Dance Hit 2000 Vol.1`

If creating a YouTube/YTM playlist, confirm:
- title has no underscores;
- title has no trailing YTM marker;
- description remains `Створено через YTM Importer`.

## 7. End-to-end optional smoke

Only if Search quota is comfortable:

House Dance Vol.1 → Search → Review → Create private playlist.

Expected:
- 9/9 usable tracks if current YouTube search results allow;
- successful create/add;
- correct human playlist title.

Do not spend quota only to prove UI items already covered elsewhere.


## Phone evidence — 2026-09-19

Current release statement:

**v1.4.41 has broad real-phone coverage, but this is not an exhaustive full-app regression claim.**
Many major workflows have been exercised across recent releases and some v1.4.41 paths
are now directly confirmed, while the still-pending cases below remain explicitly open.

Confirmed on v1.4.41:

- signed APK installed on the real phone;
- header visibly shows v1.4.41;
- existing local House Dance workspace survived the update;
- BUG-009 portrait account modal PASS:
  - account/profile details readable;
  - copy reads `Плейлисти створюватимуться в цьому YouTube/YTM профілі.`;
  - `Змінити` is on the left;
  - `Закрити` is on the right;
  - `Змінити` remains single-line;
- existing-playlist destination list renders with search + playlist count + rows;
- **no footer buttons on the existing-playlist list are intentional**:
  - it is a single-select screen;
  - tapping a playlist row immediately selects that playlist;
  - the next screen performs duplicate scan / confirmation before any write;
  - the top-left Back arrow returns without selecting.

Not yet promoted to PASS:

- account-modal rotation smoke;
- representative destructive confirmation ordering;
- BUG-004 real/reproduced 401 invalidation/re-login path;
- BUG-010 quota-preserving full Restore / rollback;
- UX-017 fallback-only filename normalization;
- optional v1.4.41 House Dance end-to-end write smoke.

The raw account screenshot contains personal account identifiers and is not committed
to repository evidence without redaction.


Additional v1.4.41 phone evidence:
- existing-target row tap → confirmation screen PASS;
- tapping `top 3` on the existing-playlist list opened `Перевірка перед додаванням`;
- this confirms the list screen's direct-tap single-select behavior end-to-end up to the pre-write confirmation screen;
- no tracks were added as part of this check.
