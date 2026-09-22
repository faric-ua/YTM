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
