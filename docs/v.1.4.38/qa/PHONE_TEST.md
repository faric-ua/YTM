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
- Data screen has a separate snapshot delete action;
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
