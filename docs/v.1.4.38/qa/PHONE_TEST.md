# v1.4.38 phone test — Selectors, Safety, Mobile Copy

## A. YTM playlist import selector

1. Open Import.
2. Tap `Вибрати плейлист з YTM`.

PASS:

- dedicated full-screen selector opens;
- Back/title/`?` remain fixed;
- list scrolls independently;
- footer remains visible;
- selecting one item shows one selected state;
- `Вибрати` imports exactly that playlist.

## B. Selective export selector

1. Tap `Вибрати плейлисти для експорту`.
2. Select several playlists.

PASS:

- full-screen multi-select opens;
- count changes as boxes are toggled;
- footer remains visible;
- `Далі` opens the folder chooser;
- Cancel does not unexpectedly mutate previous pending selection.

## C. Backup / manifest selector

Open a backup folder with multiple exported projects.

PASS: project selection uses the same full-screen selector, not a tall modal.

## D. Delta-chain head selector

Use a root that contains more than one independent delta-chain head.

PASS: head selection uses the full-screen selector.

## E. Destructive confirmation

Check at least:

- one History record;
- clear-all History;
- one Pending Queue job;
- expired SearchCache deletion;
- safety snapshot deletion.

PASS:

- destructive action is visibly different;
- exact target is named;
- explicit `Так, ...` confirmation is required;
- Cancel performs no mutation.

## F. Restore safety snapshot

1. Perform Restore so a safety snapshot exists.
2. Perform rollback.

PASS:

- rollback-success dialog contains `Готово`;
- it does not expose direct snapshot deletion;
- Data screen has a separate `Видалити знімок` action;
- deleting the snapshot requires a dedicated warning.

## G. Mobile copy

Verify in portrait:

- `Вибрати файл`;
- `Готово`;
- `Додати папку…`;
- `Зберегти як…`;
- `Відкотити Restore`.

PASS: critical actions are readable without awkward wrapping/clipping.

## H. Theme guard

Neon Dark Home must still match the locked reference. UX-009 Green Dark contrast remains separate.


## Real-phone result — first v1.4.38 pass

User result:

1. Home / utility navigation smoke — PASS.
2. Full-screen YTM single-select — PASS.
3. Full-screen selective-export multi-select — FUNCTIONAL PASS; checkbox visual alignment needs R1 polish.
4. Backup / manifest full-screen selector — PASS.
5. Destructive History confirmation — PASS.
6. Data / Restore / short-copy safety flow — PASS for the tested actions.

Phone evidence supplied in chat:

- selective-export selector: SHA-256 `793f06d77c13da313f8197610865cdf93762fcbed7ff90b1b5d31ddcfcf2999e`
- clear-all History danger confirmation: SHA-256 `9427527d66d11ea060edc7543b2761fa582dd39661c8fcadbbe7a97d07d7f799`
- single History record danger confirmation: SHA-256 `be894a46dba00f030df9e430ac0b04cdbb46260823b87ffbd881a9a96c48783a`
- safety-snapshot danger confirmation: SHA-256 `063b5a1d90663e1301807b01853f8a4bd5850d70a6f2e16565e7d91c02df86c3`

### R1 finding — checkbox alignment

The multi-select checkbox is functional but visually sits too close to the card's left edge relative to the start of the text.

R1 implementation:
- checkbox moved into its own centered fixed touch column;
- label rendered separately;
- tapping the whole row still toggles selection.

Status: **FIX IMPLEMENTED — PHONE RETEST NEEDED.**

### BUG-008 — Restore confirmation lost on rotation

Reproduced on v1.4.38:

- choose a valid full backup;
- wait for the information/confirmation dialog;
- rotate the phone;
- dialog disappears and the backup must be found again.

R1 implementation:
- validated pending backup copied into app cache;
- pending confirmation flag saved in instance state;
- after recreation, the same cached backup is inspected again and the confirmation is recreated;
- Cancel / Restore clears the temporary cache file.

Status: **FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.38 R1.**

### History JSON format finding

The user supplied `YTM_History_20260915_045457.json`.

It is a raw History export array, not a full `ytm-importer-local-backup`, so the full Restore path correctly rejects it.

Immediate recovery path:
- convert it into a History-only backup wrapper;
- restore that wrapper through the existing full Restore UI;
- because only `history_store_v1` is present, Queue/quota/SearchCache/current playlist are not overwritten.

Track a native History JSON restore/import UX as UX-015.


## R1 phone result — rotation PASS / checkbox FAIL

R1 real-phone result:

- Restore confirmation survives rotation without choosing the backup file again: **PASS**.
- selective-export checkbox visual alignment: **FAIL**.

The R1 screenshot still shows the visible checkbox square too close to the card's left edge and too much empty space between the square and playlist text.

Evidence:

- 783×1536 screenshot;
- SHA-256 `00963abcc4c37ce876aa933e9a2a2c34fb5c9150e565350dd367e39447c9dada`.

R2 root-cause correction:

- the 48dp touch column is retained;
- the Android `CheckBox` no longer fills that column directly;
- a `FrameLayout` owns the 48dp column;
- the actual `CheckBox` is `WRAP_CONTENT` and centered with `Gravity.CENTER`.

BUG-008 is closed by the R1 phone PASS. R2 requires only the checkbox visual retest.


## R2 phone result — checkbox PASS

Selective-export checkbox visual alignment: **PASS**.

The visible checkbox square is centered/balanced within the left touch column and no longer sits too close to the card edge.

Evidence:
- 783×1536 screenshot
- SHA-256 `b86f70c820d6d2332b124a67cf77fa0c2c9f5ade5a581f4f3406d9666ca1c85d`

UX-014 is closed for the tested phone path.
