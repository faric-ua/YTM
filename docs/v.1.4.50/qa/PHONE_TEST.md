# v1.4.50 — Phone Test

## Wave 1 — Skin contract smoke

Precondition:

- install the signed v1.4.50 / code 93 APK over the accepted v1.4.49 build;
- preserve existing local/account state;
- do not clear app data.

### Test 1 — built-in skins

For each built-in skin:

1. open the existing theme/skin selector;
2. select Neon Dark, Blue Dark and Green Dark in turn;
3. confirm the selected style survives the existing recreation/navigation path;
4. inspect Home, one representative full-screen utility page, one tile/list and
   one modal.

Expected:

- no crash;
- no missing controls;
- no navigation change;
- no unexpected geometry change;
- colors remain consistent with the pre-v1.4.50 built-in style;
- text remains readable.

Result format: `1+` / `1-`

### Test 2 — semantic state roles

Inspect representative UI that exposes:

- success/ready;
- warning/attention;
- danger/error/destructive;
- duplicate.

Expected:

- each role remains distinguishable;
- the role meaning is unchanged across Neon/Blue/Green;
- no semantic state silently becomes an ordinary accent state.

Result format: `2+` / `2-`

### Test 3 — recreation smoke

While a representative modal/form is open:

1. rotate portrait → landscape → portrait;
2. confirm the existing lifecycle contract still holds.

Expected:

- no remote operation restarts;
- no destructive action auto-runs;
- existing draft/modal semantics remain consistent with the common lifecycle
  contract.

Result format: `3+` / `3-`

## Initial signed Wave 1 result

Signed workflow:

- run: `35772192953`
- source: `c3939849516124cd66c6a72b04f1683f5c6e161c`
- installed version: `1.4.50 (93)`

Initial signed Wave 1 phone result: `1- / 2+ / 3-`

Observed:

- `1-`: after changing Skin in Menu, Home retained the previous Neon visual chrome;
  only the four workflow/state buttons refreshed because `updatePrimaryActions()`
  re-read the new palette while the rest of MainActivity's existing views were not
  rebuilt.
- `2+`: semantic success/warning/danger/duplicate roles remained distinguishable.
- `3-`: `History → Очистити` destructive confirmation disappeared after rotation.
  History was not automatically cleared.

Corrective R1 targets:

- BUG-031: AppThemeManager records the Skin applied to each Activity window; MainActivity
  uses a one-line resume guard to recreate when that applied Skin differs from the
  persisted Skin, rebuilding the whole Home without regrowing MainActivity.
- BUG-032: History stores the clear-confirm open flag and restores the confirmation
  after recreation without executing `historyStore.clear()` automatically.

### R1 retest

- `R1-1+`: Neon → Blue → Green; after returning from Menu the whole Home chrome
  uses the active Skin, not only the four workflow buttons.
- `R1-2+`: `History → Очистити`; rotate portrait → landscape → portrait; the
  confirmation remains/reappears and History is unchanged until explicit confirm.
- `R1-3+`: press Cancel after rotation; History remains unchanged and user stays on
  History.

## R1 signed result

Exact corrective package:
- source: `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- signed run: `35782627453`;
- installed version: `1.4.50 (93)`.

Real-phone result:
- `R1-1+` — Neon → Blue → Green refreshes the whole Home, not only workflow buttons;
- `R1-2+` — `History → Очистити` confirmation survives portrait → landscape → portrait and History does not auto-clear;
- `R1-3+` — Cancel after rotation closes only the confirmation, keeps the user on History and leaves History unchanged.

Combined Wave 1 evidence:
- initial semantic-state test remains `2+` from signed run `35772192953`;
- corrective R1 scope is `R1-1+ / R1-2+ / R1-3+`.

Scope note:
- this closes BUG-031 and BUG-032 for their tested phone paths;
- this does not claim a full-app regression PASS;
- no R1 screenshot/video was committed; the R1 result is conversation-reported phone evidence.
