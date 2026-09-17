# v1.4.26 — Screenshot Analysis

## EVIDENCE_01 — Import actions

Confirms the new selective-export action is visible alongside the pre-existing
single-playlist import and export-all actions.

This proves visibility, not full regression of the older actions.

## EVIDENCE_02 — Selective picker

Confirms checkbox multi-select and exactly two selected playlists.

Observation: very long playlist names wrap heavily. Functional result is still
usable; record as later UI polish.

## EVIDENCE_03 — Export result

Confirms the selected session completed with:

- selected = 2;
- projects = 2;
- skipped = 0;
- errors = 0;
- playlistItems.list = 2.

## EVIDENCE_04 — Export folder

Confirms the session folder contains:

- `manifest.json`;
- two YTM Project files.

## EVIDENCE_05 — Manifest

Confirms schema/version/mode/count fields for the actual phone-exported session.

## EVIDENCE_06 / 07 / 09 — Round trip

Together they confirm:

- v1.4.26 is installed;
- `top 3` reopens with 3 tracks;
- exact videoId = 3/3;
- Review shows 3 ready;
- an individual track remains matched.

## EVIDENCE_08 — BUG-005

This is the key defect evidence.

Despite 3/3 exact videoId already being present, manual Search proposes 3 new
`search.list` requests.

Do not interpret this screenshot as evidence that those requests were executed.
The QA instruction was to avoid pressing `Почати`.
