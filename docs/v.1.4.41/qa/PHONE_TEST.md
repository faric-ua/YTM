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
